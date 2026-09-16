/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Scoreboard
 */
package com.ultimateduels.scoreboard;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.MatchType;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.scoreboard.DuelLobbyScoreboard;
import com.ultimateduels.scoreboard.DuelScoreboard;
import com.ultimateduels.scoreboard.FFAScoreboard;
import com.ultimateduels.scoreboard.LobbyScoreboard;
import com.ultimateduels.scoreboard.ScoreboardBuilder;
import com.ultimateduels.scoreboard.SpectatorScoreboard;
import com.ultimateduels.settings.PlayerSettings;
import com.ultimateduels.world.WorldRestrictionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {
    private final UltimateDuels plugin;
    private final Map<UUID, ScoreboardBuilder> scoreboards;
    private final LobbyScoreboard lobbyScoreboard;
    private final DuelLobbyScoreboard duelLobbyScoreboard;
    private final DuelScoreboard duelScoreboard;
    private final FFAScoreboard ffaScoreboard;
    private final SpectatorScoreboard spectatorScoreboard;
    private BukkitTask updateTask;
    private static final long UPDATE_INTERVAL = 10L;
    private boolean takesPriority;

    public ScoreboardManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.scoreboards = new ConcurrentHashMap<UUID, ScoreboardBuilder>();
        this.lobbyScoreboard = new LobbyScoreboard(plugin);
        this.duelLobbyScoreboard = new DuelLobbyScoreboard(plugin);
        this.duelScoreboard = new DuelScoreboard(plugin);
        this.ffaScoreboard = new FFAScoreboard(plugin);
        this.spectatorScoreboard = new SpectatorScoreboard(plugin);
        this.takesPriority = plugin.getConfig().getBoolean("scoreboard.take-priority", false);
    }

    public void createScoreboard(Player player) {
        if (!this.hasScoreboardEnabled(player)) {
            return;
        }
        WorldRestrictionManager worldRestriction = this.plugin.getWorldRestrictionManager();
        if (worldRestriction != null && !worldRestriction.canShowScoreboard(player)) {
            this.plugin.debug("Scoreboard disabled for " + player.getName() + " - world restricted");
            return;
        }
        if (!this.shouldShowScoreboardInWorld(player)) {
            return;
        }
        ScoreboardBuilder board = new ScoreboardBuilder(player);
        this.scoreboards.put(player.getUniqueId(), board);
        this.updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        ScoreboardBuilder board = this.scoreboards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void updateScoreboard(Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (!this.hasScoreboardEnabled(player)) {
            if (this.scoreboards.containsKey(player.getUniqueId())) {
                this.removeScoreboard(player);
            }
            return;
        }
        if (!this.shouldShowScoreboardInWorld(player)) {
            if (this.scoreboards.containsKey(player.getUniqueId())) {
                this.removeScoreboard(player);
            }
            return;
        }
        ScoreboardBuilder board = this.scoreboards.get(player.getUniqueId());
        if (board == null) {
            this.createScoreboard(player);
            board = this.scoreboards.get(player.getUniqueId());
            if (board == null) {
                return;
            }
        }
        ScoreboardType type = this.determineScoreboardType(player);
        switch (type.ordinal()) {
            case 2: {
                DuelMatch match = this.plugin.getDuelManager().getMatch(player);
                if (match != null) {
                    this.duelScoreboard.update(board, player, match);
                    this.reapplyMatchTeamsIfNeeded(player, match);
                    break;
                }
                this.lobbyScoreboard.update(board, player);
                break;
            }
            case 1: {
                this.duelLobbyScoreboard.update(board, player);
                break;
            }
            case 3: {
                String arenaName = this.plugin.getFFAManager().getPlayerArena(player);
                if (arenaName != null) {
                    FFAArenaInstance arena = this.plugin.getFFAManager().getFFAArena(arenaName);
                    if (arena != null) {
                        this.ffaScoreboard.update(board, player, arena);
                        break;
                    }
                    this.lobbyScoreboard.update(board, player);
                    break;
                }
                this.lobbyScoreboard.update(board, player);
                break;
            }
            case 4: {
                DuelMatch spectatedMatch = this.getSpectatedMatch(player.getUniqueId());
                if (spectatedMatch != null) {
                    this.spectatorScoreboard.update(board, player, spectatedMatch);
                    break;
                }
                this.lobbyScoreboard.update(board, player);
                break;
            }
            case 0: {
                this.lobbyScoreboard.update(board, player);
            }
        }
    }

    private void reapplyMatchTeamsIfNeeded(@Nonnull Player player, @Nonnull DuelMatch match) {
        if (match.getMatchType() == MatchType.DUEL_1V1 || match.getMatchType() == MatchType.PARTY_FFA) {
            return;
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline() && this.plugin.getDuelManager() != null) {
                this.plugin.getDuelManager().reapplyMatchTeams(player);
            }
        }, 1L);
    }

    public void applyDuelScoreboard(@Nonnull Player player, @Nonnull Scoreboard scoreboard) {
        player.setScoreboard(scoreboard);
        if (this.takesPriority) {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (player.isOnline()) {
                    player.setScoreboard(scoreboard);
                }
            }, 1L);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (player.isOnline()) {
                    player.setScoreboard(scoreboard);
                }
            }, 3L);
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (this.plugin.getDuelManager() == null) {
                return;
            }
            DuelMatch match = this.plugin.getDuelManager().getMatch(player.getUniqueId());
            if (match != null) {
                this.reapplyMatchTeamsIfNeeded(player, match);
            }
        }, 2L);
    }

    private boolean shouldShowScoreboardInWorld(Player player) {
        if (this.plugin.getConfigManager() == null) {
            return false;
        }
        String worldName = player.getWorld().getName();
        try {
            return this.plugin.getConfigManager().isScoreboardEnabledInWorld(worldName);
        }
        catch (Exception e) {
            if (this.plugin.getConfigManager().isDebugMode()) {
                this.plugin.getLogger().warning("Error checking scoreboard world " + worldName + ": " + e.getMessage());
            }
            return false;
        }
    }

    private boolean hasScoreboardEnabled(Player player) {
        if (!this.plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return false;
        }
        if (this.plugin.getSettingsManager() == null) {
            return true;
        }
        PlayerSettings settings = this.plugin.getSettingsManager().getSettings(player.getUniqueId());
        return settings == null || settings.isScoreboardEnabled();
    }

    private ScoreboardType determineScoreboardType(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            return ScoreboardType.DUEL;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(uuid)) {
            return ScoreboardType.FFA;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(uuid)) {
            return ScoreboardType.SPECTATOR;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(uuid)) {
            return ScoreboardType.DUEL_LOBBY;
        }
        if (this.isInLeaderboardWorld(player)) {
            return ScoreboardType.DUEL_LOBBY;
        }
        return ScoreboardType.LOBBY;
    }

    private boolean isInLeaderboardWorld(Player player) {
        if (this.plugin.getConfigManager() == null) {
            return false;
        }
        String worldName = player.getWorld().getName();
        try {
            return this.plugin.getConfigManager().isLeaderboardEnabledInWorld(worldName);
        }
        catch (Exception e) {
            if (this.plugin.getConfigManager().isDebugMode()) {
                this.plugin.getLogger().warning("Error checking leaderboard world " + worldName + ": " + e.getMessage());
            }
            return false;
        }
    }

    private DuelMatch getSpectatedMatch(UUID spectatorUUID) {
        if (this.plugin.getDuelManager() == null) {
            return null;
        }
        for (DuelMatch match : this.plugin.getDuelManager().getActiveMatches()) {
            if (!this.plugin.getDuelManager().getMatchSpectators(match.getMatchId()).contains(spectatorUUID)) continue;
            return match;
        }
        return null;
    }

    public void updateAllScoreboards() {
        for (UUID playerId : this.scoreboards.keySet()) {
            Player player = Bukkit.getPlayer((UUID)playerId);
            if (player == null || !player.isOnline()) continue;
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.updateScoreboard(player));
        }
    }

    public void refreshScoreboardType(Player player) {
        this.updateScoreboard(player);
    }

    public void updateScoreboardType(ScoreboardType type) {
        for (UUID playerId : this.scoreboards.keySet()) {
            Player player = Bukkit.getPlayer((UUID)playerId);
            if (player == null || !player.isOnline() || this.determineScoreboardType(player) != type) continue;
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.updateScoreboard(player));
        }
    }

    public void toggleScoreboard(Player player) {
        if (!this.plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            player.sendMessage("\u00a7cScoreboards are disabled by the server administrator.");
            return;
        }
        if (this.plugin.getSettingsManager() == null) {
            return;
        }
        PlayerSettings settings = this.plugin.getSettingsManager().getSettings(player.getUniqueId());
        if (settings == null) {
            return;
        }
        boolean current = settings.isScoreboardEnabled();
        settings.setScoreboardEnabled(!current);
        this.plugin.getSettingsManager().saveSettings(player.getUniqueId());
        if (current) {
            this.removeScoreboard(player);
            player.sendMessage("\u00a7cScoreboard \u00a7ldisabled\u00a7c.");
        } else {
            this.createScoreboard(player);
            player.sendMessage("\u00a7aScoreboard \u00a7lenabled\u00a7a.");
        }
    }

    public void handleJoin(Player player) {
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                this.createScoreboard(player);
            }
        }, 10L);
    }

    public void handleQuit(Player player) {
        this.removeScoreboard(player);
    }

    public boolean hasScoreboard(Player player) {
        return this.scoreboards.containsKey(player.getUniqueId());
    }

    public ScoreboardBuilder getScoreboard(Player player) {
        return this.scoreboards.get(player.getUniqueId());
    }

    public int getActiveScoreboardCount() {
        return this.scoreboards.size();
    }

    public void reloadConfig() {
        this.takesPriority = this.plugin.getConfig().getBoolean("scoreboard.take-priority", false);
        this.plugin.getLogger().info("[ScoreboardManager] Configuration reloaded");
        this.updateAllScoreboards();
    }

    public void shutdown() {
        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
        for (ScoreboardBuilder board : this.scoreboards.values()) {
            board.delete();
        }
        this.scoreboards.clear();
    }

    public List<String> getEnabledWorldsDebug() {
        return this.plugin.getConfigManager().getScoreboardEnabledWorlds();
    }

    public List<String> getLeaderboardWorldsDebug() {
        return this.plugin.getConfigManager().getLeaderboardWorlds();
    }

    public boolean isWorldEnabledDebug(String worldName) {
        return this.plugin.getConfigManager().isScoreboardEnabledInWorld(worldName);
    }

    public boolean isLeaderboardWorldDebug(String worldName) {
        return this.plugin.getConfigManager().isLeaderboardEnabledInWorld(worldName);
    }

    public ScoreboardType getPlayerScoreboardTypeDebug(Player player) {
        return this.determineScoreboardType(player);
    }

    public Map<String, Object> getDebugInfo() {
        HashMap<String, Object> debug = new HashMap<String, Object>();
        debug.put("activeScoreboards", this.scoreboards.size());
        debug.put("enabledWorlds", this.getEnabledWorldsDebug());
        debug.put("leaderboardWorlds", this.getLeaderboardWorldsDebug());
        debug.put("scoreboardEnabled", this.plugin.getConfigManager().getConfig().getBoolean("scoreboard.enabled", true));
        return debug;
    }

    public static enum ScoreboardType {
        LOBBY,
        DUEL_LOBBY,
        DUEL,
        FFA,
        SPECTATOR;

    }
}

