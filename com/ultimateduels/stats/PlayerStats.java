/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.stats;

import com.ultimateduels.stats.KitStats;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerStats {
    private final UUID playerUUID;
    private String playerName;
    private int globalElo;
    private int totalWins;
    private int totalLosses;
    private int totalKills;
    private int totalDeaths;
    private int totalMatchesPlayed;
    private int currentWinStreak;
    private int bestWinStreak;
    private int bestKillStreak;
    private int totalHeadshotKills;
    private final Map<String, KitStats> kitStats;
    private long firstJoin;
    private long lastSeen;
    public static final int DEFAULT_ELO = 1000;

    public PlayerStats(@NotNull UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.playerName = "";
        this.globalElo = 1000;
        this.totalWins = 0;
        this.totalLosses = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalMatchesPlayed = 0;
        this.currentWinStreak = 0;
        this.bestWinStreak = 0;
        this.bestKillStreak = 0;
        this.totalHeadshotKills = 0;
        this.kitStats = new ConcurrentHashMap<String, KitStats>();
        this.firstJoin = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
    }

    public PlayerStats(@NotNull UUID playerUUID, @NotNull String playerName) {
        this(playerUUID);
        this.playerName = playerName;
    }

    @NotNull
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @NotNull
    public String getPlayerName() {
        return this.playerName;
    }

    public int getGlobalElo() {
        return this.globalElo;
    }

    public int getTotalWins() {
        return this.totalWins;
    }

    public int getTotalLosses() {
        return this.totalLosses;
    }

    public int getTotalKills() {
        return this.totalKills;
    }

    public int getTotalDeaths() {
        return this.totalDeaths;
    }

    public int getTotalMatchesPlayed() {
        return this.totalMatchesPlayed;
    }

    public int getTotalGames() {
        return this.totalWins + this.totalLosses;
    }

    public int getGamesPlayed() {
        return this.getTotalGames();
    }

    public int getCurrentWinStreak() {
        return this.currentWinStreak;
    }

    public int getBestWinStreak() {
        return this.bestWinStreak;
    }

    public int getBestKillStreak() {
        return this.bestKillStreak;
    }

    public int getTotalHeadshotKills() {
        return this.totalHeadshotKills;
    }

    public long getFirstJoin() {
        return this.firstJoin;
    }

    public long getLastSeen() {
        return this.lastSeen;
    }

    public double getWinRate() {
        int total = this.getTotalGames();
        if (total == 0) {
            return 0.0;
        }
        return (double)this.totalWins / (double)total * 100.0;
    }

    public double getKDR() {
        if (this.totalDeaths == 0) {
            return this.totalKills;
        }
        return (double)this.totalKills / (double)this.totalDeaths;
    }

    public double getKillDeathRatio() {
        return this.getKDR();
    }

    public double getWinLossRatio() {
        if (this.totalLosses == 0) {
            return this.totalWins;
        }
        return (double)this.totalWins / (double)this.totalLosses;
    }

    @NotNull
    public String getFormattedKDR() {
        if (this.totalDeaths == 0) {
            if (this.totalKills == 0) {
                return "0.00";
            }
            return this.totalKills + ".00";
        }
        return String.format("%.2f", this.getKDR());
    }

    @NotNull
    public String getFormattedWLR() {
        if (this.totalLosses == 0) {
            if (this.totalWins == 0) {
                return "0.00";
            }
            return this.totalWins + ".00";
        }
        return String.format("%.2f", this.getWinLossRatio());
    }

    public double getKillsPerGame() {
        int games = this.getTotalGames();
        if (games == 0) {
            return 0.0;
        }
        return (double)this.totalKills / (double)games;
    }

    public double getDeathsPerGame() {
        int games = this.getTotalGames();
        if (games == 0) {
            return 0.0;
        }
        return (double)this.totalDeaths / (double)games;
    }

    @NotNull
    public Map<String, KitStats> getAllKitStats() {
        return new HashMap<String, KitStats>(this.kitStats);
    }

    @Nullable
    public KitStats getKitStats(@NotNull String kitName) {
        return this.kitStats.get(kitName.toLowerCase());
    }

    @NotNull
    public KitStats getOrCreateKitStats(@NotNull String kitName) {
        String key = kitName.toLowerCase();
        return this.kitStats.computeIfAbsent(key, k -> new KitStats(kitName));
    }

    public void setPlayerName(@NotNull String playerName) {
        this.playerName = playerName;
    }

    public void setGlobalElo(int elo) {
        this.globalElo = Math.max(0, elo);
    }

    public void setTotalWins(int wins) {
        this.totalWins = Math.max(0, wins);
    }

    public void setTotalLosses(int losses) {
        this.totalLosses = Math.max(0, losses);
    }

    public void setTotalKills(int kills) {
        this.totalKills = Math.max(0, kills);
    }

    public void setTotalDeaths(int deaths) {
        this.totalDeaths = Math.max(0, deaths);
    }

    public void setTotalMatchesPlayed(int matches) {
        this.totalMatchesPlayed = Math.max(0, matches);
    }

    public void setCurrentWinStreak(int streak) {
        this.currentWinStreak = Math.max(0, streak);
    }

    public void setBestWinStreak(int streak) {
        this.bestWinStreak = Math.max(0, streak);
    }

    public void setBestKillStreak(int streak) {
        this.bestKillStreak = Math.max(0, streak);
    }

    public void setTotalHeadshotKills(int kills) {
        this.totalHeadshotKills = Math.max(0, kills);
    }

    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void addWin() {
        ++this.totalWins;
        ++this.totalMatchesPlayed;
        ++this.currentWinStreak;
        if (this.currentWinStreak > this.bestWinStreak) {
            this.bestWinStreak = this.currentWinStreak;
        }
    }

    public void addLoss() {
        ++this.totalLosses;
        ++this.totalMatchesPlayed;
        this.currentWinStreak = 0;
    }

    public void addKills(int kills) {
        this.totalKills += kills;
        if (kills > this.bestKillStreak) {
            this.bestKillStreak = kills;
        }
    }

    public void addDeaths(int deaths) {
        this.totalDeaths += deaths;
    }

    public void addElo(int amount) {
        this.globalElo = Math.max(0, this.globalElo + amount);
    }

    public void addKitStats(@NotNull String kitName, @NotNull KitStats stats) {
        this.kitStats.put(kitName.toLowerCase(), stats);
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    public void recordMatch(@NotNull String kitName, boolean won, int kills, int deaths) {
        if (won) {
            this.addWin();
        } else {
            this.addLoss();
        }
        this.addKills(kills);
        this.addDeaths(deaths);
        KitStats kit = this.getOrCreateKitStats(kitName);
        if (won) {
            kit.addWin();
        } else {
            kit.addLoss();
        }
        kit.addKills(kills);
        kit.addDeaths(deaths);
        if (kills > kit.getBestKillStreak()) {
            kit.setBestKillStreak(kills);
        }
        this.updateLastSeen();
    }

    @NotNull
    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("uuid", this.playerUUID.toString());
        data.put("name", this.playerName);
        data.put("globalElo", this.globalElo);
        data.put("totalWins", this.totalWins);
        data.put("totalLosses", this.totalLosses);
        data.put("totalKills", this.totalKills);
        data.put("totalDeaths", this.totalDeaths);
        data.put("totalMatchesPlayed", this.totalMatchesPlayed);
        data.put("currentWinStreak", this.currentWinStreak);
        data.put("bestWinStreak", this.bestWinStreak);
        data.put("bestKillStreak", this.bestKillStreak);
        data.put("totalHeadshotKills", this.totalHeadshotKills);
        data.put("firstJoin", this.firstJoin);
        data.put("lastSeen", this.lastSeen);
        HashMap<String, Map<String, Object>> kitsData = new HashMap<String, Map<String, Object>>();
        for (Map.Entry<String, KitStats> entry : this.kitStats.entrySet()) {
            kitsData.put(entry.getKey(), entry.getValue().serialize());
        }
        data.put("kitStats", kitsData);
        return data;
    }

    @NotNull
    public static PlayerStats deserialize(@NotNull Map<String, Object> data) {
        UUID uuid = UUID.fromString((String)data.get("uuid"));
        PlayerStats stats = new PlayerStats(uuid);
        stats.playerName = (String)data.getOrDefault("name", "");
        stats.globalElo = PlayerStats.getInt(data, "globalElo", 1000);
        stats.totalWins = PlayerStats.getInt(data, "totalWins", 0);
        stats.totalLosses = PlayerStats.getInt(data, "totalLosses", 0);
        stats.totalKills = PlayerStats.getInt(data, "totalKills", 0);
        stats.totalDeaths = PlayerStats.getInt(data, "totalDeaths", 0);
        stats.totalMatchesPlayed = PlayerStats.getInt(data, "totalMatchesPlayed", 0);
        stats.currentWinStreak = PlayerStats.getInt(data, "currentWinStreak", 0);
        stats.bestWinStreak = PlayerStats.getInt(data, "bestWinStreak", 0);
        stats.bestKillStreak = PlayerStats.getInt(data, "bestKillStreak", 0);
        stats.totalHeadshotKills = PlayerStats.getInt(data, "totalHeadshotKills", 0);
        stats.firstJoin = PlayerStats.getLong(data, "firstJoin", System.currentTimeMillis());
        stats.lastSeen = PlayerStats.getLong(data, "lastSeen", System.currentTimeMillis());
        Object kitsDataObj = data.get("kitStats");
        if (kitsDataObj instanceof Map) {
            Map kitsData = (Map)kitsDataObj;
            for (Map.Entry entry : kitsData.entrySet()) {
                if (!(entry.getValue() instanceof Map)) continue;
                KitStats kitStats = KitStats.deserialize((Map)entry.getValue());
                stats.kitStats.put(((String)entry.getKey()).toLowerCase(), kitStats);
            }
        }
        return stats;
    }

    private static int getInt(Map<String, Object> data, String key, int def) {
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number)val).intValue();
        }
        return def;
    }

    private static long getLong(Map<String, Object> data, String key, long def) {
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number)val).longValue();
        }
        return def;
    }

    public String toString() {
        return "PlayerStats{playerUUID=" + String.valueOf(this.playerUUID) + ", playerName='" + this.playerName + "', globalElo=" + this.globalElo + ", totalWins=" + this.totalWins + ", totalLosses=" + this.totalLosses + ", totalKills=" + this.totalKills + ", totalDeaths=" + this.totalDeaths + ", KDR=" + this.getFormattedKDR() + ", WLR=" + this.getFormattedWLR() + "}";
    }
}

