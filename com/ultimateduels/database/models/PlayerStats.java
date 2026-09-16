/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.database.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class PlayerStats {
    private final long id;
    private final UUID uuid;
    private String username;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int winStreak;
    private int bestWinStreak;
    private int elo;
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
    private Timestamp firstJoin;
    private Timestamp lastSeen;

    private PlayerStats(Builder builder) {
        this.id = builder.id;
        this.uuid = builder.uuid;
        this.username = builder.username;
        this.kills = builder.kills;
        this.deaths = builder.deaths;
        this.wins = builder.wins;
        this.losses = builder.losses;
        this.winStreak = builder.winStreak;
        this.bestWinStreak = builder.bestWinStreak;
        this.elo = builder.elo;
        this.highestElo = builder.highestElo;
        this.gamesPlayed = builder.gamesPlayed;
        this.totalDamageDealt = builder.totalDamageDealt;
        this.totalDamageTaken = builder.totalDamageTaken;
        this.arrowsShot = builder.arrowsShot;
        this.arrowsHit = builder.arrowsHit;
        this.potionsUsed = builder.potionsUsed;
        this.goldenApplesEaten = builder.goldenApplesEaten;
        this.pearlsThrown = builder.pearlsThrown;
        this.totalPlaytime = builder.totalPlaytime;
        this.ffaKills = builder.ffaKills;
        this.ffaDeaths = builder.ffaDeaths;
        this.firstJoin = builder.firstJoin;
        this.lastSeen = builder.lastSeen;
    }

    public static PlayerStats fromResultSet(ResultSet rs) throws SQLException {
        return new Builder(UUID.fromString(rs.getString("uuid"))).id(rs.getLong("id")).username(rs.getString("username")).kills(rs.getInt("kills")).deaths(rs.getInt("deaths")).wins(rs.getInt("wins")).losses(rs.getInt("losses")).winStreak(rs.getInt("win_streak")).bestWinStreak(rs.getInt("best_win_streak")).elo(rs.getInt("elo")).highestElo(rs.getInt("highest_elo")).gamesPlayed(rs.getInt("games_played")).totalDamageDealt(rs.getDouble("total_damage_dealt")).totalDamageTaken(rs.getDouble("total_damage_taken")).arrowsShot(rs.getInt("arrows_shot")).arrowsHit(rs.getInt("arrows_hit")).potionsUsed(rs.getInt("potions_used")).goldenApplesEaten(rs.getInt("golden_apples_eaten")).pearlsThrown(rs.getInt("pearls_thrown")).totalPlaytime(rs.getLong("total_playtime")).ffaKills(rs.getInt("ffa_kills")).ffaDeaths(rs.getInt("ffa_deaths")).firstJoin(rs.getTimestamp("first_join")).lastSeen(rs.getTimestamp("last_seen")).build();
    }

    public static PlayerStats createNew(UUID uuid, String username) {
        return new Builder(uuid).username(username).elo(1000).highestElo(1000).firstJoin(new Timestamp(System.currentTimeMillis())).lastSeen(new Timestamp(System.currentTimeMillis())).build();
    }

    public long getId() {
        return this.id;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getWins() {
        return this.wins;
    }

    public int getLosses() {
        return this.losses;
    }

    public int getWinStreak() {
        return this.winStreak;
    }

    public int getBestWinStreak() {
        return this.bestWinStreak;
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

    public Timestamp getFirstJoin() {
        return this.firstJoin;
    }

    public Timestamp getLastSeen() {
        return this.lastSeen;
    }

    public double getKillDeathRatio() {
        if (this.deaths == 0) {
            return this.kills;
        }
        return (double)Math.round((double)this.kills / (double)this.deaths * 100.0) / 100.0;
    }

    public double getWinLossRatio() {
        if (this.losses == 0) {
            return this.wins;
        }
        return (double)Math.round((double)this.wins / (double)this.losses * 100.0) / 100.0;
    }

    public double getWinRate() {
        if (this.gamesPlayed == 0) {
            return 0.0;
        }
        return (double)Math.round((double)this.wins / (double)this.gamesPlayed * 10000.0) / 100.0;
    }

    public double getArrowAccuracy() {
        if (this.arrowsShot == 0) {
            return 0.0;
        }
        return (double)Math.round((double)this.arrowsHit / (double)this.arrowsShot * 10000.0) / 100.0;
    }

    public double getAverageDamagePerGame() {
        if (this.gamesPlayed == 0) {
            return 0.0;
        }
        return (double)Math.round(this.totalDamageDealt / (double)this.gamesPlayed * 100.0) / 100.0;
    }

    public double getFfaKillDeathRatio() {
        if (this.ffaDeaths == 0) {
            return this.ffaKills;
        }
        return (double)Math.round((double)this.ffaKills / (double)this.ffaDeaths * 100.0) / 100.0;
    }

    public String getFormattedPlaytime() {
        long hours = this.totalPlaytime / 3600L;
        long minutes = this.totalPlaytime % 3600L / 60L;
        if (hours > 0L) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    public String getEloRank() {
        if (this.elo >= 2000) {
            return "Grandmaster";
        }
        if (this.elo >= 1800) {
            return "Master";
        }
        if (this.elo >= 1600) {
            return "Diamond";
        }
        if (this.elo >= 1400) {
            return "Platinum";
        }
        if (this.elo >= 1200) {
            return "Gold";
        }
        if (this.elo >= 1000) {
            return "Silver";
        }
        if (this.elo >= 800) {
            return "Bronze";
        }
        return "Unranked";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public void setBestWinStreak(int bestWinStreak) {
        this.bestWinStreak = bestWinStreak;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public void setHighestElo(int highestElo) {
        this.highestElo = highestElo;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setTotalDamageDealt(double damage) {
        this.totalDamageDealt = damage;
    }

    public void setTotalDamageTaken(double damage) {
        this.totalDamageTaken = damage;
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

    public void setGoldenApplesEaten(int count) {
        this.goldenApplesEaten = count;
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

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void incrementKills() {
        ++this.kills;
    }

    public void incrementDeaths() {
        ++this.deaths;
    }

    public void incrementWins() {
        ++this.wins;
        ++this.gamesPlayed;
    }

    public void incrementLosses() {
        ++this.losses;
        ++this.gamesPlayed;
    }

    public void incrementWinStreak() {
        ++this.winStreak;
        if (this.winStreak > this.bestWinStreak) {
            this.bestWinStreak = this.winStreak;
        }
    }

    public void resetWinStreak() {
        this.winStreak = 0;
    }

    public void incrementFfaKills() {
        ++this.ffaKills;
    }

    public void incrementFfaDeaths() {
        ++this.ffaDeaths;
    }

    public void addDamageDealt(double damage) {
        this.totalDamageDealt += damage;
    }

    public void addDamageTaken(double damage) {
        this.totalDamageTaken += damage;
    }

    public void incrementArrowsShot() {
        ++this.arrowsShot;
    }

    public void incrementArrowsHit() {
        ++this.arrowsHit;
    }

    public void incrementPotionsUsed() {
        ++this.potionsUsed;
    }

    public void incrementGoldenApplesEaten() {
        ++this.goldenApplesEaten;
    }

    public void incrementPearlsThrown() {
        ++this.pearlsThrown;
    }

    public void addPlaytime(long seconds) {
        this.totalPlaytime += seconds;
    }

    public void updateElo(int newElo) {
        this.elo = Math.max(0, newElo);
        if (this.elo > this.highestElo) {
            this.highestElo = this.elo;
        }
    }

    public void addElo(int change) {
        this.updateElo(this.elo + change);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PlayerStats that = (PlayerStats)o;
        return Objects.equals(this.uuid, that.uuid);
    }

    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    public String toString() {
        return "PlayerStats{uuid=" + String.valueOf(this.uuid) + ", username='" + this.username + "', kills=" + this.kills + ", deaths=" + this.deaths + ", wins=" + this.wins + ", losses=" + this.losses + ", elo=" + this.elo + ", winStreak=" + this.winStreak + "}";
    }

    public PlayerStats copy() {
        return new Builder(this.uuid).id(this.id).username(this.username).kills(this.kills).deaths(this.deaths).wins(this.wins).losses(this.losses).winStreak(this.winStreak).bestWinStreak(this.bestWinStreak).elo(this.elo).highestElo(this.highestElo).gamesPlayed(this.gamesPlayed).totalDamageDealt(this.totalDamageDealt).totalDamageTaken(this.totalDamageTaken).arrowsShot(this.arrowsShot).arrowsHit(this.arrowsHit).potionsUsed(this.potionsUsed).goldenApplesEaten(this.goldenApplesEaten).pearlsThrown(this.pearlsThrown).totalPlaytime(this.totalPlaytime).ffaKills(this.ffaKills).ffaDeaths(this.ffaDeaths).firstJoin(this.firstJoin).lastSeen(this.lastSeen).build();
    }

    public static class Builder {
        private long id = 0L;
        private final UUID uuid;
        private String username = "";
        private int kills = 0;
        private int deaths = 0;
        private int wins = 0;
        private int losses = 0;
        private int winStreak = 0;
        private int bestWinStreak = 0;
        private int elo = 1000;
        private int highestElo = 1000;
        private int gamesPlayed = 0;
        private double totalDamageDealt = 0.0;
        private double totalDamageTaken = 0.0;
        private int arrowsShot = 0;
        private int arrowsHit = 0;
        private int potionsUsed = 0;
        private int goldenApplesEaten = 0;
        private int pearlsThrown = 0;
        private long totalPlaytime = 0L;
        private int ffaKills = 0;
        private int ffaDeaths = 0;
        private Timestamp firstJoin = new Timestamp(System.currentTimeMillis());
        private Timestamp lastSeen = new Timestamp(System.currentTimeMillis());

        public Builder(UUID uuid) {
            this.uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder kills(int kills) {
            this.kills = kills;
            return this;
        }

        public Builder deaths(int deaths) {
            this.deaths = deaths;
            return this;
        }

        public Builder wins(int wins) {
            this.wins = wins;
            return this;
        }

        public Builder losses(int losses) {
            this.losses = losses;
            return this;
        }

        public Builder winStreak(int winStreak) {
            this.winStreak = winStreak;
            return this;
        }

        public Builder bestWinStreak(int bestWinStreak) {
            this.bestWinStreak = bestWinStreak;
            return this;
        }

        public Builder elo(int elo) {
            this.elo = elo;
            return this;
        }

        public Builder highestElo(int highestElo) {
            this.highestElo = highestElo;
            return this;
        }

        public Builder gamesPlayed(int gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
            return this;
        }

        public Builder totalDamageDealt(double damage) {
            this.totalDamageDealt = damage;
            return this;
        }

        public Builder totalDamageTaken(double damage) {
            this.totalDamageTaken = damage;
            return this;
        }

        public Builder arrowsShot(int arrowsShot) {
            this.arrowsShot = arrowsShot;
            return this;
        }

        public Builder arrowsHit(int arrowsHit) {
            this.arrowsHit = arrowsHit;
            return this;
        }

        public Builder potionsUsed(int potionsUsed) {
            this.potionsUsed = potionsUsed;
            return this;
        }

        public Builder goldenApplesEaten(int count) {
            this.goldenApplesEaten = count;
            return this;
        }

        public Builder pearlsThrown(int pearlsThrown) {
            this.pearlsThrown = pearlsThrown;
            return this;
        }

        public Builder totalPlaytime(long totalPlaytime) {
            this.totalPlaytime = totalPlaytime;
            return this;
        }

        public Builder ffaKills(int ffaKills) {
            this.ffaKills = ffaKills;
            return this;
        }

        public Builder ffaDeaths(int ffaDeaths) {
            this.ffaDeaths = ffaDeaths;
            return this;
        }

        public Builder firstJoin(Timestamp firstJoin) {
            this.firstJoin = firstJoin;
            return this;
        }

        public Builder lastSeen(Timestamp lastSeen) {
            this.lastSeen = lastSeen;
            return this;
        }

        public PlayerStats build() {
            return new PlayerStats(this);
        }
    }
}

