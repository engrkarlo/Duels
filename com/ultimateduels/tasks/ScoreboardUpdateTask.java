/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.scoreboard.ScoreboardManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class ScoreboardUpdateTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final ScoreboardManager scoreboardManager;
    private final DuelManager duelManager;
    private final FFAManager ffaManager;
    private static final int DUEL_UPDATE_FREQUENCY = 5;
    private static final int SPECTATOR_UPDATE_FREQUENCY = 5;
    private static final int FFA_UPDATE_FREQUENCY = 10;
    private static final int DUEL_LOBBY_UPDATE_FREQUENCY = 20;
    private static final int QUEUE_UPDATE_FREQUENCY = 20;
    private static final int LOBBY_UPDATE_FREQUENCY = 40;
    private volatile int tickCounter;
    private final Set<UUID> playersToUpdate;

    public ScoreboardUpdateTask(@NotNull UltimateDuels plugin, @NotNull ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
        this.duelManager = plugin.getDuelManager();
        this.ffaManager = plugin.getFFAManager();
        this.playersToUpdate = ConcurrentHashMap.newKeySet();
        this.tickCounter = 0;
    }

    public void run() {
        ArrayList onlinePlayers;
        ++this.tickCounter;
        this.playersToUpdate.clear();
        try {
            onlinePlayers = new ArrayList(Bukkit.getOnlinePlayers());
        }
        catch (Exception e) {
            return;
        }
        for (Player player : onlinePlayers) {
            if (player == null || !player.isOnline()) continue;
            try {
                if (!this.scoreboardManager.hasScoreboard(player) || !this.shouldUpdateThisTick(player)) continue;
                this.playersToUpdate.add(player.getUniqueId());
            }
            catch (Exception e) {
                if (this.plugin.getConfigManager() == null || !this.plugin.getConfigManager().isDebugMode()) continue;
                this.plugin.getLogger().warning("Error checking scoreboard for " + player.getName() + ": " + e.getMessage());
            }
        }
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask((Plugin)this.plugin, this::executeUpdates);
        } else {
            this.executeUpdates();
        }
        if (this.tickCounter >= 1200) {
            this.tickCounter = 0;
        }
    }

    private void executeUpdates() {
        ArrayList<UUID> playerIds;
        try {
            playerIds = new ArrayList<UUID>(this.playersToUpdate);
        }
        catch (Exception e) {
            return;
        }
        for (UUID playerId : playerIds) {
            if (playerId == null) continue;
            try {
                Player player = Bukkit.getPlayer((UUID)playerId);
                if (player == null || !player.isOnline()) continue;
                this.scoreboardManager.updateScoreboard(player);
            }
            catch (Exception e) {
                if (this.plugin.getConfigManager() == null || !this.plugin.getConfigManager().isDebugMode()) continue;
                this.plugin.getLogger().warning("Error updating scoreboard for " + String.valueOf(playerId) + ": " + e.getMessage());
            }
        }
    }

    private boolean shouldUpdateThisTick(@NotNull Player player) {
        int updateFrequency = this.getUpdateFrequency(player);
        return this.tickCounter % updateFrequency == 0;
    }

    private int getUpdateFrequency(@NotNull Player player) {
        block7: {
            UUID uuid = player.getUniqueId();
            try {
                if (this.duelManager != null && this.duelManager.isInDuel(player)) {
                    return 5;
                }
                if (this.duelManager != null && this.duelManager.isSpectating(uuid)) {
                    return 5;
                }
                if (this.ffaManager != null && this.ffaManager.isInFFA(uuid)) {
                    return 10;
                }
                if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(uuid)) {
                    return 20;
                }
                if (this.isInDuelLobbyWorld(player)) {
                    return 20;
                }
            }
            catch (Exception e) {
                if (this.plugin.getConfigManager() == null || !this.plugin.getConfigManager().isDebugMode()) break block7;
                this.plugin.getLogger().warning("Error determining update frequency for " + player.getName() + ": " + e.getMessage());
            }
        }
        return 40;
    }

    private boolean isInDuelLobbyWorld(@NotNull Player player) {
        if (this.plugin.getConfigManager() == null) {
            return false;
        }
        try {
            String worldName = player.getWorld().getName();
            List duelLobbyWorlds = this.plugin.getConfigManager().getConfig().getStringList("scoreboard.duel-lobby-worlds");
            return duelLobbyWorlds != null && duelLobbyWorlds.contains(worldName);
        }
        catch (Exception e) {
            return false;
        }
    }

    public void forceUpdate(@NotNull Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        try {
            if (this.scoreboardManager.hasScoreboard(player)) {
                if (Bukkit.isPrimaryThread()) {
                    this.scoreboardManager.updateScoreboard(player);
                } else {
                    Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                        if (player.isOnline()) {
                            this.scoreboardManager.updateScoreboard(player);
                        }
                    });
                }
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Error force updating scoreboard for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void forceUpdate(@NotNull Iterable<Player> players) {
        ArrayList<Player> playerList = new ArrayList<Player>();
        try {
            for (Player player : players) {
                if (player == null || !player.isOnline()) continue;
                playerList.add(player);
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Error collecting players for force update: " + e.getMessage());
            return;
        }
        Runnable updateTask = () -> {
            for (Player player : playerList) {
                try {
                    if (!player.isOnline() || !this.scoreboardManager.hasScoreboard(player)) continue;
                    this.scoreboardManager.updateScoreboard(player);
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Error force updating scoreboard for " + player.getName() + ": " + e.getMessage());
                }
            }
        };
        if (Bukkit.isPrimaryThread()) {
            updateTask.run();
        } else {
            Bukkit.getScheduler().runTask((Plugin)this.plugin, updateTask);
        }
    }

    public void forceUpdateByType(@NotNull ScoreboardManager.ScoreboardType type) {
        ArrayList<Player> playersToForceUpdate = new ArrayList<Player>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            try {
                ScoreboardManager.ScoreboardType playerType = this.getPlayerScoreboardType(player);
                if (playerType != type) continue;
                playersToForceUpdate.add(player);
            }
            catch (Exception exception) {}
        }
        if (!playersToForceUpdate.isEmpty()) {
            this.forceUpdate(playersToForceUpdate);
        }
    }

    private ScoreboardManager.ScoreboardType getPlayerScoreboardType(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (this.duelManager != null && this.duelManager.isInDuel(player)) {
            return ScoreboardManager.ScoreboardType.DUEL;
        }
        if (this.ffaManager != null && this.ffaManager.isInFFA(uuid)) {
            return ScoreboardManager.ScoreboardType.FFA;
        }
        if (this.duelManager != null && this.duelManager.isSpectating(uuid)) {
            return ScoreboardManager.ScoreboardType.SPECTATOR;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(uuid)) {
            return ScoreboardManager.ScoreboardType.DUEL_LOBBY;
        }
        if (this.isInDuelLobbyWorld(player)) {
            return ScoreboardManager.ScoreboardType.DUEL_LOBBY;
        }
        return ScoreboardManager.ScoreboardType.LOBBY;
    }

    public int getTickCounter() {
        return this.tickCounter;
    }

    public static int getDuelUpdateFrequency() {
        return 5;
    }

    public static int getSpectatorUpdateFrequency() {
        return 5;
    }

    public static int getFfaUpdateFrequency() {
        return 10;
    }

    public static int getDuelLobbyUpdateFrequency() {
        return 20;
    }

    public static int getLobbyUpdateFrequency() {
        return 40;
    }

    public void shutdown() {
        try {
            this.cancel();
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
        this.playersToUpdate.clear();
    }
}

