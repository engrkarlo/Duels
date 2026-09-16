/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.AsyncPlayerPreLoginEvent
 *  org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.utils.MessageUtils;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinQuitListener
implements Listener {
    private final UltimateDuels plugin;

    public PlayerJoinQuitListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        UUID playerId = event.getUniqueId();
        String playerName = event.getName();
        try {
            this.plugin.getStatsManager().getStats(playerId);
            this.plugin.getSettingsManager().getSettings(playerId);
            this.plugin.getKitManager().loadPlayerKits(playerId);
            this.plugin.getLogger().info("[PreLogin] Pre-loaded data for " + playerName);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to pre-load data for " + playerName + ": " + e.getMessage());
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerStats stats = this.plugin.getStatsManager().getStats(player);
        stats.setPlayerName(player.getName());
        stats.updateLastSeen();
        this.plugin.getLogger().info("[Join] Loaded stats for " + player.getName() + " - W:" + stats.getTotalWins() + " L:" + stats.getTotalLosses() + " K:" + stats.getTotalKills() + " D:" + stats.getTotalDeaths() + " ELO:" + stats.getGlobalElo());
        new BukkitRunnable(){

            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                PlayerJoinQuitListener.this.handlePlayerJoin(player);
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
        if (this.useCustomJoinMessage()) {
            event.setJoinMessage(null);
            if (this.showJoinMessage()) {
                String joinMessage = this.getJoinMessage(player);
                Bukkit.broadcastMessage((String)MessageUtils.colorize(joinMessage));
            }
        }
    }

    private String getJoinMessage(Player player) {
        if (this.plugin.getLanguageManager() != null) {
            try {
                String langMessage = this.plugin.getLanguageManager().getRaw(player, "general.join-broadcast");
                if (langMessage != null && !langMessage.contains("Missing message")) {
                    return langMessage.replace("{player}", player.getName()).replace("%player%", player.getName());
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to get join message from LanguageManager: " + e.getMessage());
            }
        }
        String configMessage = this.plugin.getConfig().getString("messages.join-message", "&e{player} joined the game!");
        return configMessage.replace("{player}", player.getName()).replace("%player%", player.getName());
    }

    private void handlePlayerJoin(Player player) {
        UUID playerId = player.getUniqueId();
        if (!this.plugin.getWorldRestrictionManager().isPlayerInAllowedWorld(player)) {
            this.plugin.debug("Player " + player.getName() + " joined in blacklisted world - skipping plugin features");
            return;
        }
        DuelManager duelManager = this.plugin.getDuelManager();
        DuelMatch existingMatch = duelManager.getMatch(playerId);
        if (existingMatch != null) {
            this.handleDuelReconnect(player, existingMatch);
            return;
        }
        this.cleanupStaleData(player);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            Location lobbySpawn;
            if (!player.isOnline()) {
                return;
            }
            if (this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
                return;
            }
            if (this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
                return;
            }
            this.resetPlayerState(player);
            if (this.shouldTeleportToLobbyOnJoin() && (lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn()) != null) {
                player.teleport(lobbySpawn);
            }
            if (this.shouldGiveLobbyItemsOnJoin()) {
                this.plugin.getLobbyManager().giveHotbarItems(player);
            }
            this.plugin.getKitManager().loadCustomLayouts(playerId);
            if (this.plugin.getSettingsManager().hasScoreboardEnabled(player)) {
                this.plugin.getScoreboardManager().createScoreboard(player);
            }
            this.sendWelcomeMessage(player);
            if (this.hasSoundsEnabled(player)) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            }
            this.checkPendingInvites(player);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.equals((Object)player)) continue;
                online.showPlayer((Plugin)this.plugin, player);
                player.showPlayer((Plugin)this.plugin, online);
            }
            this.plugin.debug("Player " + player.getName() + " joined and was fully initialized");
        }, 10L);
    }

    private void cleanupStaleData(Player player) {
        UUID playerId = player.getUniqueId();
        this.plugin.getQueueManager().removeFromAllQueues(playerId);
        if (this.plugin.getFFAManager().isInFFA(playerId)) {
            this.plugin.getFFAManager().removePlayer(playerId);
        }
        if (this.plugin.getDuelManager().isSpectating(playerId)) {
            this.plugin.getDuelManager().removeSpectator(player);
        }
    }

    private void handleDuelReconnect(final Player player, final DuelMatch match) {
        MessageUtils.sendMessage(player, "&eReconnecting to your duel...");
        new BukkitRunnable(){

            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                DuelMatch currentMatch = PlayerJoinQuitListener.this.plugin.getDuelManager().getMatch(player.getUniqueId());
                if (currentMatch != null && currentMatch.getMatchId().equals(match.getMatchId())) {
                    MessageUtils.sendMessage(player, "&aYou have been reconnected to your duel!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    PlayerJoinQuitListener.this.plugin.getScoreboardManager().updateScoreboard(player);
                } else {
                    Location lobby;
                    MessageUtils.sendMessage(player, "&cYour duel has ended.");
                    PlayerJoinQuitListener.this.resetPlayerState(player);
                    if (PlayerJoinQuitListener.this.shouldTeleportToLobbyOnJoin() && (lobby = PlayerJoinQuitListener.this.plugin.getLobbyManager().getLobbySpawn()) != null) {
                        player.teleport(lobby);
                    }
                    if (PlayerJoinQuitListener.this.shouldGiveLobbyItemsOnJoin()) {
                        PlayerJoinQuitListener.this.plugin.getLobbyManager().giveHotbarItems(player);
                    }
                    PlayerJoinQuitListener.this.plugin.getScoreboardManager().createScoreboard(player);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 10L);
    }

    private void resetPlayerState(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.ADVENTURE);
        }
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setExp(0.0f);
        player.setLevel(0);
        player.updateInventory();
    }

    private void sendWelcomeMessage(Player player) {
        block18: {
            if (!this.isWelcomeMessageEnabled()) {
                return;
            }
            PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
            MessageUtils.sendMessage(player, "");
            MessageUtils.sendMessage(player, "&6&l\u2501\u2501\u2501\u2501\u2501\u2501 Welcome to UltimateDuels! \u2501\u2501\u2501\u2501\u2501\u2501");
            MessageUtils.sendMessage(player, "");
            if (stats != null && stats.getTotalGames() > 0) {
                if (this.plugin.getLanguageManager() != null) {
                    try {
                        String welcomeBack = this.plugin.getLanguageManager().getRaw(player, "general.welcome-back");
                        if (welcomeBack != null && !welcomeBack.contains("Missing message")) {
                            MessageUtils.sendMessage(player, welcomeBack.replace("{player}", player.getName()));
                        } else {
                            MessageUtils.sendMessage(player, "&7Welcome back, &e" + player.getName() + "&7!");
                        }
                        String statsLine = this.plugin.getLanguageManager().getRaw(player, "general.welcome-stats");
                        if (statsLine != null && !statsLine.contains("Missing message")) {
                            MessageUtils.sendMessage(player, statsLine.replace("{wins}", String.valueOf(stats.getTotalWins())).replace("{losses}", String.valueOf(stats.getTotalLosses())).replace("{elo}", String.valueOf(stats.getGlobalElo())));
                            break block18;
                        }
                        MessageUtils.sendMessage(player, "&7Your Stats: &a" + stats.getTotalWins() + " Wins &7| &c" + stats.getTotalLosses() + " Losses &7| &6" + stats.getGlobalElo() + " Elo");
                    }
                    catch (Exception e) {
                        MessageUtils.sendMessage(player, "&7Welcome back, &e" + player.getName() + "&7!");
                        MessageUtils.sendMessage(player, "&7Your Stats: &a" + stats.getTotalWins() + " Wins &7| &c" + stats.getTotalLosses() + " Losses &7| &6" + stats.getGlobalElo() + " Elo");
                    }
                } else {
                    MessageUtils.sendMessage(player, "&7Welcome back, &e" + player.getName() + "&7!");
                    MessageUtils.sendMessage(player, "&7Your Stats: &a" + stats.getTotalWins() + " Wins &7| &c" + stats.getTotalLosses() + " Losses &7| &6" + stats.getGlobalElo() + " Elo");
                }
            } else if (this.plugin.getLanguageManager() != null) {
                try {
                    String welcomeNew = this.plugin.getLanguageManager().getRaw(player, "general.welcome-new");
                    if (welcomeNew != null && !welcomeNew.contains("Missing message")) {
                        MessageUtils.sendMessage(player, welcomeNew.replace("{player}", player.getName()));
                    } else {
                        MessageUtils.sendMessage(player, "&7Welcome, &e" + player.getName() + "&7!");
                    }
                    String welcomeHint = this.plugin.getLanguageManager().getRaw(player, "general.welcome-hint");
                    if (welcomeHint != null && !welcomeHint.contains("Missing message")) {
                        MessageUtils.sendMessage(player, welcomeHint);
                        break block18;
                    }
                    MessageUtils.sendMessage(player, "&7Use the items in your hotbar to get started.");
                }
                catch (Exception e) {
                    MessageUtils.sendMessage(player, "&7Welcome, &e" + player.getName() + "&7!");
                    MessageUtils.sendMessage(player, "&7Use the items in your hotbar to get started.");
                }
            } else {
                MessageUtils.sendMessage(player, "&7Welcome, &e" + player.getName() + "&7!");
                MessageUtils.sendMessage(player, "&7Use the items in your hotbar to get started.");
            }
        }
        MessageUtils.sendMessage(player, "");
        int online = Bukkit.getOnlinePlayers().size();
        int inDuels = this.plugin.getDuelManager().getTotalPlayersInMatches();
        int inQueue = this.plugin.getQueueManager().getTotalQueueSize();
        MessageUtils.sendMessage(player, "&7Online: &f" + online + " &8| &7Dueling: &f" + inDuels + " &8| &7In Queue: &f" + inQueue);
        MessageUtils.sendMessage(player, "&6&l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        MessageUtils.sendMessage(player, "");
        if (this.plugin.getLanguageManager() != null) {
            String langCode = this.plugin.getLanguageManager().getPlayerLanguage(player);
            String langName = this.plugin.getLanguageManager().getLanguageName(langCode);
            MessageUtils.sendMessage(player, "&7Language: &f" + langName + " &8(&e/language&8)");
            MessageUtils.sendMessage(player, "");
        }
    }

    private void checkPendingInvites(final Player player) {
        if (this.plugin.getPartyManager().hasPendingInvite(player.getUniqueId())) {
            new BukkitRunnable(this){

                public void run() {
                    if (player.isOnline()) {
                        MessageUtils.sendMessage(player, "&eYou have a pending party invite! &7Use &a/party accept");
                    }
                }
            }.runTaskLater((Plugin)this.plugin, 40L);
        }
        if (this.plugin.getDuelManager().hasPendingRequest(player.getUniqueId())) {
            new BukkitRunnable(this){

                public void run() {
                    if (player.isOnline()) {
                        MessageUtils.sendMessage(player, "&eYou have a pending duel request! &7Use &a/duel accept");
                    }
                }
            }.runTaskLater((Plugin)this.plugin, 60L);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (this.useCustomQuitMessage()) {
            event.setQuitMessage(null);
            if (this.showQuitMessage()) {
                String quitMessage = this.getQuitMessage(player);
                Bukkit.broadcastMessage((String)MessageUtils.colorize(quitMessage));
            }
        }
        this.handlePlayerQuit(player);
        this.plugin.getStatsManager().unloadStats(uuid);
        this.plugin.getLogger().info("[Quit] Saved and unloaded stats for " + player.getName());
    }

    private String getQuitMessage(Player player) {
        if (this.plugin.getLanguageManager() != null) {
            try {
                String langMessage = this.plugin.getLanguageManager().getRaw(player, "general.quit-broadcast");
                if (langMessage != null && !langMessage.contains("Missing message")) {
                    return langMessage.replace("{player}", player.getName()).replace("%player%", player.getName());
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to get quit message from LanguageManager: " + e.getMessage());
            }
        }
        String configMessage = this.plugin.getConfig().getString("messages.quit-message", "&e{player} left the game!");
        return configMessage.replace("{player}", player.getName()).replace("%player%", player.getName());
    }

    private void handlePlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        if (this.plugin.getDuelManager().isInMatch(playerId)) {
            this.plugin.getDuelManager().handleDisconnect(playerId);
        }
        if (this.plugin.getFFAManager().isInFFA(playerId)) {
            this.plugin.getFFAManager().removePlayer(playerId);
        }
        if (this.plugin.getDuelManager().isSpectating(playerId)) {
            this.plugin.getDuelManager().removeSpectator(player);
        }
        if (this.plugin.getPartyManager().isInParty(playerId)) {
            this.plugin.getPartyManager().handleDisconnect(playerId);
        }
        if (this.plugin.getQueueManager().isInQueue(playerId)) {
            this.plugin.getQueueManager().leaveQueue(playerId, false);
        }
        this.plugin.getScoreboardManager().removeScoreboard(player);
        if (this.plugin.getGUIManager() != null) {
            this.plugin.getGUIManager().removeOpenGUI(player);
        }
        this.plugin.getKitManager().unloadCustomLayouts(playerId);
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.plugin.getSettingsManager().saveSettings(playerId);
                this.plugin.getKitManager().savePlayerKits(playerId);
                if (this.plugin.getLanguageManager() != null) {
                    this.plugin.getLanguageManager().savePlayerPreferences();
                }
                this.plugin.debug("Saved all data for " + player.getName());
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to save data for " + player.getName() + ": " + e.getMessage());
            }
        });
        this.plugin.debug("Player " + player.getName() + " quit and data cleanup completed");
    }

    private boolean isWelcomeMessageEnabled() {
        return this.plugin.getConfig().getBoolean("general.welcome-message-enabled", true);
    }

    private boolean useCustomJoinMessage() {
        return this.plugin.getConfig().getBoolean("messages.custom-join-message", false);
    }

    private boolean showJoinMessage() {
        return this.plugin.getConfig().getBoolean("messages.show-join-message", true);
    }

    private boolean useCustomQuitMessage() {
        return this.plugin.getConfig().getBoolean("messages.custom-quit-message", false);
    }

    private boolean showQuitMessage() {
        return this.plugin.getConfig().getBoolean("messages.show-quit-message", true);
    }

    private boolean hasSoundsEnabled(Player player) {
        try {
            return this.plugin.getSettingsManager().getSettings(player.getUniqueId()).isSoundsEnabled();
        }
        catch (Exception e) {
            return true;
        }
    }

    private boolean shouldTeleportToLobbyOnJoin() {
        if (this.plugin.getConfig().contains("lobby.teleport-on-join")) {
            return this.plugin.getConfig().getBoolean("lobby.teleport-on-join", true);
        }
        return this.plugin.getConfig().getBoolean("lobby.enabled", true);
    }

    private boolean shouldGiveLobbyItemsOnJoin() {
        if (this.plugin.getConfig().contains("lobby.give-items-on-join")) {
            return this.plugin.getConfig().getBoolean("lobby.give-items-on-join", true);
        }
        if (this.plugin.getConfig().contains("lobby.give-lobby-items")) {
            return this.plugin.getConfig().getBoolean("lobby.give-lobby-items", true);
        }
        return this.plugin.getConfig().getBoolean("lobby.enabled", true);
    }
}

