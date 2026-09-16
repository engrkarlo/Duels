/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.spectator;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.spectator.SpectatorState;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorManager {
    private final UltimateDuels plugin;
    private final Map<UUID, UUID> spectators;
    private final Map<UUID, Set<UUID>> matchSpectators;
    private final Map<UUID, SpectatorState> savedStates;
    private final Set<UUID> postMatchSpectators;
    private static final int TELEPORT_SLOT = 0;
    private static final int PLAYERS_SLOT = 1;
    private static final int SETTINGS_SLOT = 7;
    private static final int LEAVE_SLOT = 8;

    public SpectatorManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.spectators = new ConcurrentHashMap<UUID, UUID>();
        this.matchSpectators = new ConcurrentHashMap<UUID, Set<UUID>>();
        this.savedStates = new ConcurrentHashMap<UUID, SpectatorState>();
        this.postMatchSpectators = ConcurrentHashMap.newKeySet();
    }

    public boolean startSpectating(Player spectator, DuelMatch match) {
        if (match == null) {
            MessageUtils.sendMessage(spectator, "&cThis match no longer exists!");
            return false;
        }
        if (match.getState() == MatchState.COMPLETED || match.getState() == MatchState.CANCELLED) {
            MessageUtils.sendMessage(spectator, "&cThis match has already ended!");
            return false;
        }
        if (this.plugin.getDuelManager().isInMatch(spectator.getUniqueId())) {
            MessageUtils.sendMessage(spectator, "&cYou cannot spectate while in a duel!");
            return false;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(spectator.getUniqueId())) {
            MessageUtils.sendMessage(spectator, "&cYou cannot spectate while in an FFA arena!");
            return false;
        }
        if (this.isSpectating(spectator)) {
            this.stopSpectating(spectator, false);
        }
        this.savePlayerState(spectator);
        UUID matchId = match.getMatchId();
        this.spectators.put(spectator.getUniqueId(), matchId);
        this.matchSpectators.computeIfAbsent(matchId, k -> ConcurrentHashMap.newKeySet()).add(spectator.getUniqueId());
        this.applySpectatorMode(spectator);
        Location spectatorSpawn = this.getSpectatorSpawn(match);
        if (spectatorSpawn != null) {
            spectator.teleport(spectatorSpawn);
        }
        this.hideFromParticipants(spectator, match);
        this.showOtherSpectators(spectator, matchId);
        if (this.plugin.getHealthDisplayManager() != null) {
            this.plugin.getHealthDisplayManager().handleSpectatorJoin(spectator, match);
        }
        MessageUtils.sendMessage(spectator, "&aYou are now spectating the match!");
        MessageUtils.sendMessage(spectator, "&7Use /spectate leave or /unspectate to stop.");
        spectator.playSound(spectator.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player participantPlayer = Bukkit.getPlayer((UUID)participant.getUuid());
            if (participantPlayer == null || !participantPlayer.isOnline() || this.plugin.getSettingsManager() == null || !this.plugin.getSettingsManager().getSettings(participantPlayer.getUniqueId()).isSpectatorNotifyEnabled()) continue;
            MessageUtils.sendMessage(participantPlayer, "&7" + spectator.getName() + " is now spectating.");
        }
        return true;
    }

    public boolean startSpectating(Player spectator, Player target) {
        DuelMatch match = this.plugin.getDuelManager().getMatch(target);
        if (match == null) {
            MessageUtils.sendMessage(spectator, "&c" + target.getName() + " is not in a duel!");
            return false;
        }
        boolean result = this.startSpectating(spectator, match);
        if (result) {
            spectator.teleport(target.getLocation().add(0.0, 2.0, 0.0));
            MessageUtils.sendMessage(spectator, "&7Spectating &e" + target.getName());
        }
        return result;
    }

    public boolean stopSpectating(Player spectator, boolean teleportToLobby) {
        Object specs;
        UUID matchId = this.spectators.remove(spectator.getUniqueId());
        if (matchId == null && !this.postMatchSpectators.contains(spectator.getUniqueId())) {
            return false;
        }
        if (matchId != null && (specs = this.matchSpectators.get(matchId)) != null) {
            specs.remove(spectator.getUniqueId());
            if (specs.isEmpty()) {
                this.matchSpectators.remove(matchId);
            }
        }
        this.postMatchSpectators.remove(spectator.getUniqueId());
        if (this.plugin.getHealthDisplayManager() != null) {
            this.plugin.getHealthDisplayManager().handleSpectatorLeave(spectator);
        }
        this.restorePlayerState(spectator);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer((Plugin)this.plugin, spectator);
            spectator.showPlayer((Plugin)this.plugin, online);
        }
        if (teleportToLobby) {
            Location lobby = this.plugin.getLobbyManager().getLobbySpawn();
            if (lobby != null) {
                spectator.teleport(lobby);
            }
            this.plugin.getLobbyManager().giveHotbarItems(spectator);
        }
        MessageUtils.sendMessage(spectator, "&cYou stopped spectating.");
        return true;
    }

    public void makePostMatchSpectator(final Player player, DuelMatch match, long duration) {
        if (!this.savedStates.containsKey(player.getUniqueId())) {
            this.savePlayerState(player);
        }
        this.postMatchSpectators.add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        int winningTeam = match.getWinningTeam();
        for (DuelParticipant winner : match.getTeamParticipants(winningTeam)) {
            Player winnerPlayer = Bukkit.getPlayer((UUID)winner.getUuid());
            if (winnerPlayer == null) continue;
            player.showPlayer((Plugin)this.plugin, winnerPlayer);
        }
        if (this.plugin.getHealthDisplayManager() != null) {
            this.plugin.getHealthDisplayManager().handleSpectatorJoin(player, match);
        }
        new BukkitRunnable(){

            public void run() {
                if (player.isOnline() && SpectatorManager.this.postMatchSpectators.contains(player.getUniqueId())) {
                    SpectatorManager.this.stopSpectating(player, true);
                }
            }
        }.runTaskLater((Plugin)this.plugin, duration);
    }

    private void savePlayerState(Player player) {
        SpectatorState state = new SpectatorState();
        state.setLocation(player.getLocation().clone());
        state.setInventory((ItemStack[])player.getInventory().getContents().clone());
        state.setArmor((ItemStack[])player.getInventory().getArmorContents().clone());
        state.setOffhand(player.getInventory().getItemInOffHand().clone());
        state.setGameMode(player.getGameMode());
        state.setFlying(player.isFlying());
        state.setAllowFlight(player.getAllowFlight());
        state.setHealth(player.getHealth());
        state.setFoodLevel(player.getFoodLevel());
        state.setExp(player.getExp());
        state.setLevel(player.getLevel());
        ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>(player.getActivePotionEffects());
        state.setPotionEffects(effects);
        this.savedStates.put(player.getUniqueId(), state);
        this.plugin.getLogger().info("Saved spectator state for " + player.getName() + " (GameMode: " + String.valueOf(player.getGameMode()) + ")");
    }

    private void restorePlayerState(Player player) {
        SpectatorState state = this.savedStates.remove(player.getUniqueId());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (state != null) {
            this.plugin.getLogger().info("Restoring spectator state for " + player.getName() + " (Saved GameMode: " + String.valueOf(state.getGameMode()) + ")");
            player.setGameMode(state.getGameMode());
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            player.getInventory().setContents(state.getInventory());
            player.getInventory().setArmorContents(state.getArmor());
            player.getInventory().setItemInOffHand(state.getOffhand());
            player.setAllowFlight(state.isAllowFlight());
            player.setFlying(state.isFlying());
            player.setHealth(Math.min(state.getHealth(), player.getMaxHealth()));
            player.setFoodLevel(state.getFoodLevel());
            player.setSaturation(20.0f);
            player.setExp(state.getExp());
            player.setLevel(state.getLevel());
            for (PotionEffect effect2 : state.getPotionEffects()) {
                player.addPotionEffect(effect2);
            }
            if (state.getLocation() != null && state.getLocation().getWorld() != null) {
                // empty if block
            }
        } else {
            this.plugin.getLogger().warning("No saved state found for spectator " + player.getName() + "! Using fallback restoration.");
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setFireTicks(0);
        }
        player.updateInventory();
    }

    private void applySpectatorMode(Player spectator) {
        spectator.getInventory().clear();
        spectator.getInventory().setArmorContents(null);
        spectator.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        spectator.getActivePotionEffects().forEach(effect -> spectator.removePotionEffect(effect.getType()));
        spectator.setHealth(spectator.getMaxHealth());
        spectator.setFoodLevel(20);
        spectator.setSaturation(20.0f);
        spectator.setFireTicks(0);
    }

    private void giveSpectatorItems(Player spectator) {
        ItemStack teleporter = this.createSpectatorItem(Material.COMPASS, "&a&lTeleport to Player", "&7Click to teleport between players");
        spectator.getInventory().setItem(0, teleporter);
        ItemStack playerList = this.createSpectatorItem(Material.PAPER, "&e&lPlayer List", "&7View all players in this match");
        spectator.getInventory().setItem(1, playerList);
        ItemStack settings = this.createSpectatorItem(Material.COMPARATOR, "&b&lSpectator Settings", "&7Configure your spectator experience");
        spectator.getInventory().setItem(7, settings);
        ItemStack leave = this.createSpectatorItem(Material.RED_DYE, "&c&lStop Spectating", "&7Return to the lobby");
        spectator.getInventory().setItem(8, leave);
    }

    private ItemStack createSpectatorItem(Material material, String name, String ... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize(name));
            ArrayList<String> loreList = new ArrayList<String>();
            for (String line : lore) {
                loreList.add(MessageUtils.colorize(line));
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void hideFromParticipants(Player spectator, DuelMatch match) {
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player participantPlayer = Bukkit.getPlayer((UUID)participant.getUuid());
            if (participantPlayer == null || !participantPlayer.isOnline()) continue;
            participantPlayer.hidePlayer((Plugin)this.plugin, spectator);
        }
    }

    private void showOtherSpectators(Player spectator, UUID matchId) {
        Set<UUID> otherSpectators = this.matchSpectators.get(matchId);
        if (otherSpectators == null) {
            return;
        }
        for (UUID otherId : otherSpectators) {
            Player other;
            if (otherId.equals(spectator.getUniqueId()) || (other = Bukkit.getPlayer((UUID)otherId)) == null || !other.isOnline()) continue;
            spectator.showPlayer((Plugin)this.plugin, other);
            other.showPlayer((Plugin)this.plugin, spectator);
        }
    }

    private Location getSpectatorSpawn(DuelMatch match) {
        Location spectatorSpawn = match.getArena().getSpectatorSpawn();
        if (spectatorSpawn != null) {
            return spectatorSpawn;
        }
        Location center = match.getArena().getCenter();
        if (center != null) {
            return center.clone().add(0.0, 5.0, 0.0);
        }
        Location pos1 = match.getArena().getSpawnPoint1();
        Location pos2 = match.getArena().getSpawnPoint2();
        if (pos1 != null && pos2 != null) {
            return new Location(pos1.getWorld(), (pos1.getX() + pos2.getX()) / 2.0, Math.max(pos1.getY(), pos2.getY()) + 10.0, (pos1.getZ() + pos2.getZ()) / 2.0);
        }
        return pos1 != null ? pos1.clone().add(0.0, 10.0, 0.0) : null;
    }

    public void teleportToNextPlayer(Player spectator) {
        UUID matchId = this.spectators.get(spectator.getUniqueId());
        if (matchId == null) {
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatchById(matchId);
        if (match == null) {
            return;
        }
        List<UUID> alivePlayers = this.getAlivePlayerUUIDs(match);
        if (alivePlayers.isEmpty()) {
            return;
        }
        UUID currentTarget = this.getCurrentSpectatorTarget(spectator, match);
        int currentIndex = currentTarget != null ? alivePlayers.indexOf(currentTarget) : -1;
        int nextIndex = (currentIndex + 1) % alivePlayers.size();
        Player nextPlayer = Bukkit.getPlayer((UUID)alivePlayers.get(nextIndex));
        if (nextPlayer != null && nextPlayer.isOnline()) {
            spectator.teleport(nextPlayer.getLocation().add(0.0, 2.0, 0.0));
            MessageUtils.sendMessage(spectator, "&7Now spectating: &e" + nextPlayer.getName());
            spectator.playSound(spectator.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    public void teleportToPreviousPlayer(Player spectator) {
        UUID matchId = this.spectators.get(spectator.getUniqueId());
        if (matchId == null) {
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatchById(matchId);
        if (match == null) {
            return;
        }
        List<UUID> alivePlayers = this.getAlivePlayerUUIDs(match);
        if (alivePlayers.isEmpty()) {
            return;
        }
        UUID currentTarget = this.getCurrentSpectatorTarget(spectator, match);
        int currentIndex = currentTarget != null ? alivePlayers.indexOf(currentTarget) : 0;
        int prevIndex = (currentIndex - 1 + alivePlayers.size()) % alivePlayers.size();
        Player prevPlayer = Bukkit.getPlayer((UUID)alivePlayers.get(prevIndex));
        if (prevPlayer != null && prevPlayer.isOnline()) {
            spectator.teleport(prevPlayer.getLocation().add(0.0, 2.0, 0.0));
            MessageUtils.sendMessage(spectator, "&7Now spectating: &e" + prevPlayer.getName());
            spectator.playSound(spectator.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    public void teleportToPlayer(Player spectator, Player target) {
        if (!this.isSpectating(spectator)) {
            return;
        }
        UUID matchId = this.spectators.get(spectator.getUniqueId());
        if (matchId == null) {
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatchById(matchId);
        if (match == null) {
            return;
        }
        List<UUID> alivePlayers = this.getAlivePlayerUUIDs(match);
        if (!alivePlayers.contains(target.getUniqueId())) {
            MessageUtils.sendMessage(spectator, "&cThat player is not an active fighter in this match!");
            return;
        }
        spectator.teleport(target.getLocation().add(0.0, 2.0, 0.0));
        MessageUtils.sendMessage(spectator, "&7Now spectating: &e" + target.getName());
        spectator.playSound(spectator.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    private List<UUID> getAlivePlayerUUIDs(DuelMatch match) {
        ArrayList<UUID> alive = new ArrayList<UUID>();
        for (DuelParticipant participant : match.getAllParticipants()) {
            DuelMatch currentMatch;
            Player player;
            if (!participant.isAlive() || (player = Bukkit.getPlayer((UUID)participant.getUuid())) == null || !player.isOnline() || (currentMatch = this.plugin.getDuelManager().getMatch(player)) == null || !currentMatch.getMatchId().equals(match.getMatchId()) || player.getGameMode() == GameMode.SPECTATOR) continue;
            alive.add(participant.getUuid());
        }
        return alive;
    }

    private UUID getCurrentSpectatorTarget(Player spectator, DuelMatch match) {
        UUID closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (UUID uuid : this.getAlivePlayerUUIDs(match)) {
            double distance;
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline() || !((distance = spectator.getLocation().distanceSquared(player.getLocation())) < closestDistance)) continue;
            closestDistance = distance;
            closest = uuid;
        }
        return closest;
    }

    public Set<UUID> getSpectators(UUID matchId) {
        Set<UUID> specs = this.matchSpectators.get(matchId);
        return specs != null ? Collections.unmodifiableSet(specs) : Collections.emptySet();
    }

    public Set<Player> getSpectatorPlayers(UUID matchId) {
        Set<UUID> specs = this.matchSpectators.get(matchId);
        if (specs == null) {
            return Collections.emptySet();
        }
        HashSet<Player> players = new HashSet<Player>();
        for (UUID uuid : specs) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            players.add(player);
        }
        return players;
    }

    public int getSpectatorCount(UUID matchId) {
        Set<UUID> specs = this.matchSpectators.get(matchId);
        return specs != null ? specs.size() : 0;
    }

    public void broadcastToSpectators(UUID matchId, String message) {
        Set<UUID> specs = this.matchSpectators.get(matchId);
        if (specs == null) {
            return;
        }
        for (UUID specId : specs) {
            Player spectator = Bukkit.getPlayer((UUID)specId);
            if (spectator == null || !spectator.isOnline()) continue;
            MessageUtils.sendMessage(spectator, message);
        }
    }

    public void broadcastTitleToSpectators(UUID matchId, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Set<UUID> specs = this.matchSpectators.get(matchId);
        if (specs == null) {
            return;
        }
        for (UUID specId : specs) {
            Player spectator = Bukkit.getPlayer((UUID)specId);
            if (spectator == null || !spectator.isOnline()) continue;
            spectator.sendTitle(MessageUtils.colorize(title), MessageUtils.colorize(subtitle), fadeIn, stay, fadeOut);
        }
    }

    public void playSoundToSpectators(UUID matchId, Sound sound, float volume, float pitch) {
        Set<UUID> specs = this.matchSpectators.get(matchId);
        if (specs == null) {
            return;
        }
        for (UUID specId : specs) {
            Player spectator = Bukkit.getPlayer((UUID)specId);
            if (spectator == null || !spectator.isOnline()) continue;
            spectator.playSound(spectator.getLocation(), sound, volume, pitch);
        }
    }

    public boolean isSpectating(Player player) {
        return this.spectators.containsKey(player.getUniqueId()) || this.postMatchSpectators.contains(player.getUniqueId());
    }

    public boolean isSpectating(UUID uuid) {
        return this.spectators.containsKey(uuid) || this.postMatchSpectators.contains(uuid);
    }

    public boolean isPostMatchSpectator(Player player) {
        return this.postMatchSpectators.contains(player.getUniqueId());
    }

    public DuelMatch getSpectatedMatch(Player spectator) {
        UUID matchId = this.spectators.get(spectator.getUniqueId());
        if (matchId == null) {
            return null;
        }
        return this.plugin.getDuelManager().getMatchById(matchId);
    }

    public UUID getSpectatedMatchId(Player spectator) {
        return this.spectators.get(spectator.getUniqueId());
    }

    public void handleDisconnect(Player player) {
        this.stopSpectating(player, false);
    }

    public void handleMatchEnd(UUID matchId) {
        Set<UUID> specs = this.matchSpectators.remove(matchId);
        if (specs == null) {
            return;
        }
        for (UUID specId : new HashSet<UUID>(specs)) {
            this.spectators.remove(specId);
            this.postMatchSpectators.remove(specId);
        }
    }

    public void handlePlayerDeath(UUID matchId, Player victim, Player killer) {
        String message = killer != null ? "&c" + victim.getName() + " &7was killed by &a" + killer.getName() : "&c" + victim.getName() + " &7died";
        this.broadcastToSpectators(matchId, message);
    }

    public void handleRoundEnd(UUID matchId, String winnerName, int round, int maxRounds) {
        this.broadcastToSpectators(matchId, "&a" + winnerName + " &7won round &e" + round + "/" + maxRounds);
    }

    public List<DuelMatch> getSpectatableMatches() {
        return this.plugin.getDuelManager().getActiveMatches().stream().filter(match -> match.getState() == MatchState.IN_PROGRESS || match.getState() == MatchState.STARTING).toList();
    }

    public Set<UUID> getAllSpectators() {
        HashSet<UUID> all = new HashSet<UUID>(this.spectators.keySet());
        all.addAll(this.postMatchSpectators);
        return all;
    }

    public int getTotalSpectatorCount() {
        return this.spectators.size() + this.postMatchSpectators.size();
    }

    public void toggleSpectatorSpeed(Player spectator) {
        if (!this.isSpectating(spectator)) {
            return;
        }
        float currentSpeed = spectator.getFlySpeed();
        if (currentSpeed >= 0.2f) {
            spectator.setFlySpeed(0.1f);
            MessageUtils.sendMessage(spectator, "&7Flight speed: &eNormal");
        } else {
            spectator.setFlySpeed(0.2f);
            MessageUtils.sendMessage(spectator, "&7Flight speed: &aFast");
        }
        spectator.playSound(spectator.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    public void toggleSpectatorVisibility(Player spectator) {
        Player other;
        if (!this.isSpectating(spectator)) {
            return;
        }
        UUID matchId = this.spectators.get(spectator.getUniqueId());
        if (matchId == null) {
            return;
        }
        Set<UUID> otherSpectators = this.matchSpectators.get(matchId);
        if (otherSpectators == null || otherSpectators.size() <= 1) {
            MessageUtils.sendMessage(spectator, "&7No other spectators to hide/show.");
            return;
        }
        boolean currentlyVisible = false;
        for (UUID otherId : otherSpectators) {
            if (otherId.equals(spectator.getUniqueId()) || (other = Bukkit.getPlayer((UUID)otherId)) == null || !spectator.canSee(other)) continue;
            currentlyVisible = true;
            break;
        }
        for (UUID otherId : otherSpectators) {
            if (otherId.equals(spectator.getUniqueId()) || (other = Bukkit.getPlayer((UUID)otherId)) == null) continue;
            if (currentlyVisible) {
                spectator.hidePlayer((Plugin)this.plugin, other);
                continue;
            }
            spectator.showPlayer((Plugin)this.plugin, other);
        }
        if (currentlyVisible) {
            MessageUtils.sendMessage(spectator, "&7Other spectators: &cHidden");
        } else {
            MessageUtils.sendMessage(spectator, "&7Other spectators: &aVisible");
        }
        spectator.playSound(spectator.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    public void shutdown() {
        Player spectator;
        if (this.plugin.getHealthDisplayManager() != null) {
            for (UUID spectatorId : new HashSet<UUID>(this.spectators.keySet())) {
                spectator = Bukkit.getPlayer((UUID)spectatorId);
                if (spectator == null || !spectator.isOnline()) continue;
                this.plugin.getHealthDisplayManager().handleSpectatorLeave(spectator);
            }
            for (UUID spectatorId : new HashSet<UUID>(this.postMatchSpectators)) {
                spectator = Bukkit.getPlayer((UUID)spectatorId);
                if (spectator == null || !spectator.isOnline()) continue;
                this.plugin.getHealthDisplayManager().handleSpectatorLeave(spectator);
            }
        }
        for (UUID spectatorId : new HashSet<UUID>(this.spectators.keySet())) {
            spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            this.stopSpectating(spectator, true);
        }
        for (UUID spectatorId : new HashSet<UUID>(this.postMatchSpectators)) {
            spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            this.stopSpectating(spectator, true);
        }
        this.spectators.clear();
        this.matchSpectators.clear();
        this.savedStates.clear();
        this.postMatchSpectators.clear();
    }
}

