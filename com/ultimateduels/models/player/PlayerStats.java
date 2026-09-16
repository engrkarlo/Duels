/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    private final UUID uuid;
    private String username;
    private int totalWins;
    private int totalLosses;
    private int totalKills;
    private int totalDeaths;
    private int currentWinStreak;
    private int bestWinStreak;
    private int globalElo;
    private int highestElo;
    private int gamesPlayed;
    private double totalDamageDealt;
    private double totalDamageTaken;
    private int arrowsShot;
    private int arrowsHit;
    private int potionsUsed;
    private int goldenApplesEaten;
    private int pearlsThrown;
    private long totalPlaytime;
    private int ffaKills;
    private int ffaDeaths;
    private int ffaBestKillStreak;
    private final Map<String, KitStats> kitStats;
    private final Map<String, Integer> eloRatings;
    private static final int DEFAULT_ELO = 1000;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.totalWins = 0;
        this.totalLosses = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.currentWinStreak = 0;
        this.bestWinStreak = 0;
        this.globalElo = 1000;
        this.highestElo = 1000;
        this.gamesPlayed = 0;
        this.totalDamageDealt = 0.0;
        this.totalDamageTaken = 0.0;
        this.arrowsShot = 0;
        this.arrowsHit = 0;
        this.potionsUsed = 0;
        this.goldenApplesEaten = 0;
        this.pearlsThrown = 0;
        this.totalPlaytime = 0L;
        this.ffaKills = 0;
        this.ffaDeaths = 0;
        this.ffaBestKillStreak = 0;
        this.kitStats = new HashMap<String, KitStats>();
        this.eloRatings = new HashMap<String, Integer>();
    }

    public void addWin(String kitName) {
        ++this.totalWins;
        ++this.currentWinStreak;
        ++this.gamesPlayed;
        if (this.currentWinStreak > this.bestWinStreak) {
            this.bestWinStreak = this.currentWinStreak;
        }
        this.getOrCreateKitStats(kitName).addWin();
    }

    public void addLoss(String kitName) {
        ++this.totalLosses;
        this.currentWinStreak = 0;
        ++this.gamesPlayed;
        this.getOrCreateKitStats(kitName).addLoss();
    }

    public void addKills(String kitName, int kills) {
        this.totalKills += kills;
        this.getOrCreateKitStats(kitName).addKills(kills);
    }

    public void addDeaths(String kitName, int deaths) {
        this.totalDeaths += deaths;
        this.getOrCreateKitStats(kitName).addDeaths(deaths);
    }

    public void addFfaKill() {
        ++this.ffaKills;
        ++this.totalKills;
    }

    public void addFfaDeath() {
        ++this.ffaDeaths;
        ++this.totalDeaths;
    }

    public void updateFfaBestStreak(int streak) {
        if (streak > this.ffaBestKillStreak) {
            this.ffaBestKillStreak = streak;
        }
    }

    public double getFfaKDR() {
        if (this.ffaDeaths == 0) {
            return this.ffaKills;
        }
        return (double)this.ffaKills / (double)this.ffaDeaths;
    }

    public int getElo(String kitName) {
        return this.eloRatings.getOrDefault(kitName.toLowerCase(), 1000);
    }

    public void setElo(String kitName, int elo) {
        this.eloRatings.put(kitName.toLowerCase(), elo);
        this.updateGlobalElo();
    }

    public void addElo(String kitName, int amount) {
        int current = this.getElo(kitName);
        this.setElo(kitName, Math.max(0, current + amount));
    }

    public int getGlobalElo() {
        if (this.eloRatings.isEmpty()) {
            return this.globalElo;
        }
        int total = 0;
        for (int elo : this.eloRatings.values()) {
            total += elo;
        }
        return total / this.eloRatings.size();
    }

    private void updateGlobalElo() {
        if (this.eloRatings.isEmpty()) {
            return;
        }
        int total = 0;
        for (int elo : this.eloRatings.values()) {
            total += elo;
        }
        this.globalElo = total / this.eloRatings.size();
        if (this.globalElo > this.highestElo) {
            this.highestElo = this.globalElo;
        }
    }

    public double getWinLossRatio() {
        if (this.totalLosses == 0) {
            return this.totalWins;
        }
        return (double)this.totalWins / (double)this.totalLosses;
    }

    public double getKillDeathRatio() {
        if (this.totalDeaths == 0) {
            return this.totalKills;
        }
        return (double)this.totalKills / (double)this.totalDeaths;
    }

    public double getWinRate() {
        int totalGames = this.totalWins + this.totalLosses;
        if (totalGames == 0) {
            return 0.0;
        }
        return (double)this.totalWins / (double)totalGames * 100.0;
    }

    public double getArrowAccuracy() {
        if (this.arrowsShot == 0) {
            return 0.0;
        }
        return (double)this.arrowsHit / (double)this.arrowsShot * 100.0;
    }

    private KitStats getOrCreateKitStats(String kitName) {
        return this.kitStats.computeIfAbsent(kitName.toLowerCase(), k -> new KitStats(kitName));
    }

    public KitStats getKitStats(String kitName) {
        return this.kitStats.getOrDefault(kitName.toLowerCase(), new KitStats(kitName));
    }

    public Map<String, KitStats> getAllKitStats() {
        return new HashMap<String, KitStats>(this.kitStats);
    }

    public void setKitStats(String kitName, KitStats stats) {
        this.kitStats.put(kitName.toLowerCase(), stats);
    }

    public void addDamageDealt(double damage) {
        this.totalDamageDealt += damage;
    }

    public void addDamageTaken(double damage) {
        this.totalDamageTaken += damage;
    }

    public void addArrowShot() {
        ++this.arrowsShot;
    }

    public void addArrowHit() {
        ++this.arrowsHit;
    }

    public void addPotionUsed() {
        ++this.potionsUsed;
    }

    public void addGoldenAppleEaten() {
        ++this.goldenApplesEaten;
    }

    public void addPearlThrown() {
        ++this.pearlsThrown;
    }

    public void addPlaytime(long millis) {
        this.totalPlaytime += millis;
    }

    public void addPlaytimeSeconds(int seconds) {
        this.totalPlaytime += (long)seconds * 1000L;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
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

    public int getCurrentWinStreak() {
        return this.currentWinStreak;
    }

    public int getBestWinStreak() {
        return this.bestWinStreak;
    }

    public int getHighestElo() {
        return this.highestElo;
    }

    public int getGamesPlayed() {
        return this.gamesPlayed;
    }

    public int getTotalGames() {
        return this.totalWins + this.totalLosses;
    }

    public double getTotalDamageDealt() {
        return this.totalDamageDealt;
    }

    public double getTotalDamageTaken() {
        return this.totalDamageTaken;
    }

    public int getArrowsShot() {
        return this.arrowsShot;
    }

    public int getArrowsHit() {
        return this.arrowsHit;
    }

    public int getPotionsUsed() {
        return this.potionsUsed;
    }

    public int getGoldenApplesEaten() {
        return this.goldenApplesEaten;
    }

    public int getPearlsThrown() {
        return this.pearlsThrown;
    }

    public long getTotalPlaytime() {
        return this.totalPlaytime;
    }

    public int getFfaKills() {
        return this.ffaKills;
    }

    public int getFfaDeaths() {
        return this.ffaDeaths;
    }

    public int getFfaBestKillStreak() {
        return this.ffaBestKillStreak;
    }

    public Map<String, Integer> getEloRatings() {
        return new HashMap<String, Integer>(this.eloRatings);
    }

    public String getFormattedPlaytime() {
        long seconds = this.totalPlaytime / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        minutes %= 60L;
        if (hours > 0L) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public void setCurrentWinStreak(int currentWinStreak) {
        this.currentWinStreak = currentWinStreak;
    }

    public void setBestWinStreak(int bestWinStreak) {
        this.bestWinStreak = bestWinStreak;
    }

    public void setGlobalElo(int globalElo) {
        this.globalElo = globalElo;
    }

    public void setHighestElo(int highestElo) {
        this.highestElo = highestElo;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setTotalDamageDealt(double totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }

    public void setTotalDamageTaken(double totalDamageTaken) {
        this.totalDamageTaken = totalDamageTaken;
    }

    public void setArrowsShot(int arrowsShot) {
        this.arrowsShot = arrowsShot;
    }

    public void setArrowsHit(int arrowsHit) {
        this.arrowsHit = arrowsHit;
    }

    public void setPotionsUsed(int potionsUsed) {
        this.potionsUsed = potionsUsed;
    }

    public void setGoldenApplesEaten(int goldenApplesEaten) {
        this.goldenApplesEaten = goldenApplesEaten;
    }

    public void setPearlsThrown(int pearlsThrown) {
        this.pearlsThrown = pearlsThrown;
    }

    public void setTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    public void setFfaKills(int ffaKills) {
        this.ffaKills = ffaKills;
    }

    public void setFfaDeaths(int ffaDeaths) {
        this.ffaDeaths = ffaDeaths;
    }

    public void setFfaBestKillStreak(int ffaBestKillStreak) {
        this.ffaBestKillStreak = ffaBestKillStreak;
    }

    public String toString() {
        return "PlayerStats{uuid=" + String.valueOf(this.uuid) + ", wins=" + this.totalWins + ", losses=" + this.totalLosses + ", kills=" + this.totalKills + ", deaths=" + this.totalDeaths + ", elo=" + this.globalElo + "}";
    }

    public static class KitStats {
        private String kitName;
        private int wins = 0;
        private int losses = 0;
        private int kills = 0;
        private int deaths = 0;
        private int elo = 1000;
        private int highestElo = 1000;
        private int gamesPlayed = 0;
        private int winStreak = 0;
        private int bestWinStreak = 0;
        private static final int DEFAULT_ELO = 1000;

        public KitStats() {
        }

        public KitStats(String kitName) {
            this();
            this.kitName = kitName;
        }

        public void addWin() {
            ++this.wins;
            ++this.gamesPlayed;
            ++this.winStreak;
            if (this.winStreak > this.bestWinStreak) {
                this.bestWinStreak = this.winStreak;
            }
        }

        public void addLoss() {
            ++this.losses;
            ++this.gamesPlayed;
            this.winStreak = 0;
        }

        public void addKills(int amount) {
            this.kills += amount;
        }

        public void addDeaths(int amount) {
            this.deaths += amount;
        }

        public String getKitName() {
            return this.kitName;
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

        public int getElo() {
            return this.elo;
        }

        public int getHighestElo() {
            return this.highestElo;
        }

        public int getGamesPlayed() {
            return this.gamesPlayed;
        }

        public int getWinStreak() {
            return this.winStreak;
        }

        public int getBestWinStreak() {
            return this.bestWinStreak;
        }

        public double getWinRate() {
            int total = this.wins + this.losses;
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

        public void setKitName(String kitName) {
            this.kitName = kitName;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public void setLosses(int losses) {
            this.losses = losses;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }

        public void setDeaths(int deaths) {
            this.deaths = deaths;
        }

        public void setElo(int elo) {
            this.elo = elo;
            if (elo > this.highestElo) {
                this.highestElo = elo;
            }
        }

        public void setHighestElo(int highestElo) {
            this.highestElo = highestElo;
        }

        public void setGamesPlayed(int gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public void setWinStreak(int winStreak) {
            this.winStreak = winStreak;
        }

        public void setBestWinStreak(int bestWinStreak) {
            this.bestWinStreak = bestWinStreak;
        }
    }
}

