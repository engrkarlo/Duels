/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.player;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.player.PlayerData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.bukkit.entity.Player;

public class PlayerDataManager {
    private final UltimateDuels plugin;
    private final Map<UUID, PlayerData> playerDataCache;

    public PlayerDataManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<UUID, PlayerData>();
        plugin.getLogger().info("\u00a7a[PlayerDataManager] Initialized successfully with auto-save!");
    }

    @Nonnull
    public PlayerData getPlayerData(@Nonnull UUID uuid) {
        return this.playerDataCache.computeIfAbsent(uuid, this::loadPlayerData);
    }

    @Nonnull
    public PlayerData getPlayerData(@Nonnull Player player) {
        return this.getPlayerData(player.getUniqueId());
    }

    @Nonnull
    private PlayerData loadPlayerData(@Nonnull UUID uuid) {
        PlayerData data = this.plugin.getDatabaseManager().loadPlayerData(uuid);
        if (data != null) {
            this.plugin.getLogger().fine("Loaded player data from database for " + String.valueOf(uuid));
            return data;
        }
        this.plugin.getLogger().fine("Creating new player data for " + String.valueOf(uuid));
        return new PlayerData(uuid);
    }

    public void savePlayerData(@Nonnull UUID uuid) {
        PlayerData data = this.playerDataCache.get(uuid);
        if (data != null) {
            this.plugin.getDatabaseManager().savePlayerData(data);
        }
    }

    public void savePlayerDataAsync(@Nonnull UUID uuid) {
        PlayerData data = this.playerDataCache.get(uuid);
        if (data != null) {
            this.plugin.getDatabaseManager().savePlayerDataAsync(data);
        }
    }

    public void saveAll() {
        this.plugin.getLogger().info("Saving all player data...");
        int saved = 0;
        for (Map.Entry<UUID, PlayerData> entry : this.playerDataCache.entrySet()) {
            this.plugin.getDatabaseManager().savePlayerData(entry.getValue());
            ++saved;
        }
        this.plugin.getLogger().info("Saved " + saved + " player data entries.");
    }

    public void unloadPlayerData(@Nonnull UUID uuid) {
        PlayerData data = this.playerDataCache.remove(uuid);
        if (data != null) {
            this.plugin.getDatabaseManager().savePlayerData(data);
            this.plugin.getLogger().fine("Unloaded and saved player data for " + String.valueOf(uuid));
        }
    }

    public void recordWin(@Nonnull UUID uuid, @Nonnull String kitName) {
        PlayerData data = this.getPlayerData(uuid);
        data.addWin(kitName);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Recorded win for " + String.valueOf(uuid) + " with kit " + kitName);
    }

    public void recordLoss(@Nonnull UUID uuid, @Nonnull String kitName) {
        PlayerData data = this.getPlayerData(uuid);
        data.addLoss(kitName);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Recorded loss for " + String.valueOf(uuid) + " with kit " + kitName);
    }

    public void addKills(@Nonnull UUID uuid, @Nonnull String kitName, int kills) {
        PlayerData data = this.getPlayerData(uuid);
        data.addKills(kitName, kills);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Added " + kills + " kills for " + String.valueOf(uuid) + " with kit " + kitName);
    }

    public void addDeaths(@Nonnull UUID uuid, @Nonnull String kitName, int deaths) {
        PlayerData data = this.getPlayerData(uuid);
        data.addDeaths(kitName, deaths);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Added " + deaths + " deaths for " + String.valueOf(uuid) + " with kit " + kitName);
    }

    public void updateElo(@Nonnull UUID uuid, int newElo) {
        PlayerData data = this.getPlayerData(uuid);
        data.setElo(newElo);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Updated ELO for " + String.valueOf(uuid) + " to " + newElo);
    }

    public void addElo(@Nonnull UUID uuid, int eloChange) {
        PlayerData data = this.getPlayerData(uuid);
        int currentElo = data.getElo();
        int newElo = Math.max(0, currentElo + eloChange);
        data.setElo(newElo);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Added ELO change " + eloChange + " for " + String.valueOf(uuid) + " (old: " + currentElo + ", " + newElo + ")");
    }

    public void recordMatchStats(@Nonnull UUID uuid, @Nonnull String kitName, boolean won, int kills, int deaths) {
        PlayerData data = this.getPlayerData(uuid);
        if (won) {
            data.addWin(kitName);
        } else {
            data.addLoss(kitName);
        }
        data.addKills(kitName, kills);
        data.addDeaths(kitName, deaths);
        this.savePlayerDataAsync(uuid);
        this.plugin.getLogger().fine("Recorded complete match stats for " + String.valueOf(uuid) + " (won: " + won + ", kills: " + kills + ", deaths: " + deaths + ")");
    }

    public int getTotalWins(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getTotalWins();
    }

    public int getTotalLosses(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getTotalLosses();
    }

    public int getKitWins(@Nonnull UUID uuid, @Nonnull String kitName) {
        return this.getPlayerData(uuid).getKitWins(kitName);
    }

    public int getKitLosses(@Nonnull UUID uuid, @Nonnull String kitName) {
        return this.getPlayerData(uuid).getKitLosses(kitName);
    }

    public int getTotalKills(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getTotalKills();
    }

    public int getTotalDeaths(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getTotalDeaths();
    }

    public int getElo(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getElo();
    }

    public double getKDR(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getTotalKDR();
    }

    public double getWLR(@Nonnull UUID uuid) {
        PlayerData data = this.getPlayerData(uuid);
        int wins = data.getTotalWins();
        int losses = data.getTotalLosses();
        return losses > 0 ? (double)wins / (double)losses : (double)wins;
    }

    public double getWinRate(@Nonnull UUID uuid) {
        return this.getPlayerData(uuid).getTotalWinRate();
    }

    public int getCacheSize() {
        return this.playerDataCache.size();
    }

    public void clearCache() {
        this.saveAll();
        this.playerDataCache.clear();
        this.plugin.getLogger().info("Player data cache cleared!");
    }

    public void forceSave(@Nonnull UUID uuid) {
        this.savePlayerData(uuid);
        this.plugin.getLogger().info("Force saved data for " + String.valueOf(uuid));
    }

    public void shutdown() {
        this.plugin.getLogger().info("Shutting down PlayerDataManager...");
        this.saveAll();
        this.playerDataCache.clear();
        this.plugin.getLogger().info("PlayerDataManager shutdown complete!");
    }
}

