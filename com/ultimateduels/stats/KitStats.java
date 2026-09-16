/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.stats;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class KitStats {
    private final String kitName;
    private int elo;
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int matchesPlayed;
    private int currentWinStreak;
    private int bestWinStreak;
    private int bestKillStreak;
    public static final int DEFAULT_ELO = 1000;

    public KitStats(@NotNull String kitName) {
        this.kitName = kitName;
        this.elo = 1000;
        this.wins = 0;
        this.losses = 0;
        this.kills = 0;
        this.deaths = 0;
        this.matchesPlayed = 0;
        this.currentWinStreak = 0;
        this.bestWinStreak = 0;
        this.bestKillStreak = 0;
    }

    @NotNull
    public String getKitName() {
        return this.kitName;
    }

    public int getElo() {
        return this.elo;
    }

    public int getWins() {
        return this.wins;
    }

    public int getLosses() {
        return this.losses;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getMatchesPlayed() {
        return this.matchesPlayed;
    }

    public int getTotalGames() {
        return this.wins + this.losses;
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

    public double getWinRate() {
        int total = this.getTotalGames();
        if (total == 0) {
            return 0.0;
        }
        return (double)this.wins / (double)total * 100.0;
    }

    public double getKDR() {
        if (this.deaths == 0) {
            return this.kills;
        }
        return (double)this.kills / (double)this.deaths;
    }

    public double getWinLossRatio() {
        if (this.losses == 0) {
            return this.wins;
        }
        return (double)this.wins / (double)this.losses;
    }

    public double getWLR() {
        return this.getWinLossRatio();
    }

    @NotNull
    public String getFormattedKDR() {
        if (this.deaths == 0) {
            if (this.kills == 0) {
                return "0.00";
            }
            return this.kills + ".00";
        }
        return String.format("%.2f", this.getKDR());
    }

    @NotNull
    public String getFormattedWLR() {
        if (this.losses == 0) {
            if (this.wins == 0) {
                return "0.00";
            }
            return this.wins + ".00";
        }
        return String.format("%.2f", this.getWinLossRatio());
    }

    public double getKillsPerGame() {
        int games = this.getTotalGames();
        if (games == 0) {
            return 0.0;
        }
        return (double)this.kills / (double)games;
    }

    public double getDeathsPerGame() {
        int games = this.getTotalGames();
        if (games == 0) {
            return 0.0;
        }
        return (double)this.deaths / (double)games;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
    }

    public void setWins(int wins) {
        this.wins = Math.max(0, wins);
    }

    public void setLosses(int losses) {
        this.losses = Math.max(0, losses);
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = Math.max(0, matchesPlayed);
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

    public void addWin() {
        ++this.wins;
        ++this.matchesPlayed;
        ++this.currentWinStreak;
        if (this.currentWinStreak > this.bestWinStreak) {
            this.bestWinStreak = this.currentWinStreak;
        }
    }

    public void addLoss() {
        ++this.losses;
        ++this.matchesPlayed;
        this.currentWinStreak = 0;
    }

    public void addKills(int amount) {
        this.kills += amount;
    }

    public void addDeaths(int amount) {
        this.deaths += amount;
    }

    public void addElo(int amount) {
        this.elo = Math.max(0, this.elo + amount);
    }

    @NotNull
    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("kitName", this.kitName);
        data.put("elo", this.elo);
        data.put("wins", this.wins);
        data.put("losses", this.losses);
        data.put("kills", this.kills);
        data.put("deaths", this.deaths);
        data.put("matchesPlayed", this.matchesPlayed);
        data.put("currentWinStreak", this.currentWinStreak);
        data.put("bestWinStreak", this.bestWinStreak);
        data.put("bestKillStreak", this.bestKillStreak);
        return data;
    }

    @NotNull
    public static KitStats deserialize(@NotNull Map<String, Object> data) {
        String kitName = (String)data.getOrDefault("kitName", "unknown");
        KitStats stats = new KitStats(kitName);
        stats.elo = KitStats.getInt(data, "elo", 1000);
        stats.wins = KitStats.getInt(data, "wins", 0);
        stats.losses = KitStats.getInt(data, "losses", 0);
        stats.kills = KitStats.getInt(data, "kills", 0);
        stats.deaths = KitStats.getInt(data, "deaths", 0);
        stats.matchesPlayed = KitStats.getInt(data, "matchesPlayed", 0);
        stats.currentWinStreak = KitStats.getInt(data, "currentWinStreak", 0);
        stats.bestWinStreak = KitStats.getInt(data, "bestWinStreak", 0);
        stats.bestKillStreak = KitStats.getInt(data, "bestKillStreak", 0);
        return stats;
    }

    private static int getInt(Map<String, Object> data, String key, int def) {
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number)val).intValue();
        }
        return def;
    }

    public String toString() {
        return "KitStats{kitName='" + this.kitName + "', elo=" + this.elo + ", wins=" + this.wins + ", losses=" + this.losses + ", kills=" + this.kills + ", deaths=" + this.deaths + ", KDR=" + this.getFormattedKDR() + ", WLR=" + this.getFormattedWLR() + "}";
    }
}

