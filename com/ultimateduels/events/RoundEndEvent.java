/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.events;

import com.ultimateduels.models.duel.Duel;
import com.ultimateduels.models.kit.Kit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundEndEvent
extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Duel duel;
    private final int roundNumber;
    private final int totalRounds;
    private final Set<UUID> roundWinners;
    private final Set<UUID> roundLosers;
    private final int winningTeam;
    private final int newTeam1Score;
    private final int newTeam2Score;
    private final EndReason endReason;
    private final Kit kit;
    private final long roundDurationMillis;
    private final long timestamp;
    private final boolean matchEnded;
    private final boolean wasKill;
    private final Map<UUID, RoundStatistics> playerStats;

    public RoundEndEvent(@NotNull Duel duel, int roundNumber, @NotNull Set<UUID> roundWinners, @NotNull Set<UUID> roundLosers, int winningTeam, @NotNull EndReason endReason, long roundDurationMillis) {
        super(false);
        this.duel = duel;
        this.roundNumber = roundNumber;
        this.totalRounds = duel.getSettings().getRounds();
        this.roundWinners = roundWinners;
        this.roundLosers = roundLosers;
        this.winningTeam = winningTeam;
        this.newTeam1Score = duel.getTeam1Score();
        this.newTeam2Score = duel.getTeam2Score();
        this.endReason = endReason;
        this.kit = duel.getKit();
        this.roundDurationMillis = roundDurationMillis;
        this.timestamp = System.currentTimeMillis();
        this.matchEnded = duel.isMatchOver();
        this.wasKill = endReason == EndReason.DEATH || endReason == EndReason.VOID_DEATH;
        this.playerStats = new HashMap<UUID, RoundStatistics>();
    }

    @NotNull
    public Duel getDuel() {
        return this.duel;
    }

    @NotNull
    public String getDuelId() {
        return this.duel.getDuelId();
    }

    @NotNull
    public UUID getMatchId() {
        return this.duel.getMatchId();
    }

    public int getRoundNumber() {
        return this.roundNumber;
    }

    public int getTotalRounds() {
        return this.totalRounds;
    }

    @NotNull
    public String getRoundDisplay() {
        return this.roundNumber + "/" + this.totalRounds;
    }

    @NotNull
    public Set<UUID> getRoundWinners() {
        return Collections.unmodifiableSet(this.roundWinners);
    }

    @Nullable
    public UUID getRoundWinner() {
        if (this.roundWinners.size() == 1) {
            return this.roundWinners.iterator().next();
        }
        return null;
    }

    @NotNull
    public Set<UUID> getRoundLosers() {
        return Collections.unmodifiableSet(this.roundLosers);
    }

    @Nullable
    public UUID getRoundLoser() {
        if (this.roundLosers.size() == 1) {
            return this.roundLosers.iterator().next();
        }
        return null;
    }

    public int getWinningTeam() {
        return this.winningTeam;
    }

    public int getTeam1Score() {
        return this.newTeam1Score;
    }

    public int getTeam2Score() {
        return this.newTeam2Score;
    }

    @NotNull
    public String getFormattedScore() {
        return this.newTeam1Score + " - " + this.newTeam2Score;
    }

    @NotNull
    public EndReason getEndReason() {
        return this.endReason;
    }

    @NotNull
    public Kit getKit() {
        return this.kit;
    }

    public long getRoundDurationMillis() {
        return this.roundDurationMillis;
    }

    public long getRoundDurationSeconds() {
        return this.roundDurationMillis / 1000L;
    }

    @NotNull
    public String getFormattedRoundDuration() {
        long seconds = this.getRoundDurationSeconds();
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean hasMatchEnded() {
        return this.matchEnded;
    }

    public boolean hasNextRound() {
        return !this.matchEnded;
    }

    public boolean wasKill() {
        return this.wasKill;
    }

    public boolean isRoundWinner(@NotNull UUID playerId) {
        return this.roundWinners.contains(playerId);
    }

    public boolean isRoundWinner(@NotNull Player player) {
        return this.isRoundWinner(player.getUniqueId());
    }

    public boolean isRoundLoser(@NotNull UUID playerId) {
        return this.roundLosers.contains(playerId);
    }

    public boolean isRoundLoser(@NotNull Player player) {
        return this.isRoundLoser(player.getUniqueId());
    }

    public int getMatchLeader() {
        if (this.newTeam1Score > this.newTeam2Score) {
            return 1;
        }
        if (this.newTeam2Score > this.newTeam1Score) {
            return 2;
        }
        return 0;
    }

    public boolean isMatchTied() {
        return this.newTeam1Score == this.newTeam2Score;
    }

    public void addPlayerStats(@NotNull UUID playerId, @NotNull RoundStatistics stats) {
        this.playerStats.put(playerId, stats);
    }

    @Nullable
    public RoundStatistics getPlayerStats(@NotNull UUID playerId) {
        return this.playerStats.get(playerId);
    }

    @NotNull
    public Map<UUID, RoundStatistics> getAllPlayerStats() {
        return Collections.unmodifiableMap(this.playerStats);
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public String toString() {
        return "RoundEndEvent{duelId=" + this.getDuelId() + ", matchId=" + String.valueOf(this.getMatchId()) + ", round=" + this.getRoundDisplay() + ", winner=" + String.valueOf(this.getRoundWinner()) + ", loser=" + String.valueOf(this.getRoundLoser()) + ", score=" + this.getFormattedScore() + ", reason=" + String.valueOf((Object)this.endReason) + ", duration=" + this.getFormattedRoundDuration() + ", matchEnded=" + this.matchEnded + "}";
    }

    public static enum EndReason {
        DEATH,
        DISCONNECT,
        VOID_DEATH,
        FORFEIT,
        TIMEOUT,
        ADMIN;

    }

    public static class RoundStatistics {
        private final double damageDealt;
        private final double damageTaken;
        private final double healthRemaining;
        private final int potionsUsed;
        private final int pearlsUsed;
        private final int hits;
        private final int criticalHits;

        public RoundStatistics(double damageDealt, double damageTaken, double healthRemaining, int potionsUsed, int pearlsUsed, int hits, int criticalHits) {
            this.damageDealt = damageDealt;
            this.damageTaken = damageTaken;
            this.healthRemaining = healthRemaining;
            this.potionsUsed = potionsUsed;
            this.pearlsUsed = pearlsUsed;
            this.hits = hits;
            this.criticalHits = criticalHits;
        }

        public double getDamageDealt() {
            return this.damageDealt;
        }

        public double getDamageTaken() {
            return this.damageTaken;
        }

        public double getHealthRemaining() {
            return this.healthRemaining;
        }

        public int getPotionsUsed() {
            return this.potionsUsed;
        }

        public int getPearlsUsed() {
            return this.pearlsUsed;
        }

        public int getHits() {
            return this.hits;
        }

        public int getCriticalHits() {
            return this.criticalHits;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private double damageDealt = 0.0;
            private double damageTaken = 0.0;
            private double healthRemaining = 0.0;
            private int potionsUsed = 0;
            private int pearlsUsed = 0;
            private int hits = 0;
            private int criticalHits = 0;

            public Builder damageDealt(double damage) {
                this.damageDealt = damage;
                return this;
            }

            public Builder damageTaken(double damage) {
                this.damageTaken = damage;
                return this;
            }

            public Builder healthRemaining(double health) {
                this.healthRemaining = health;
                return this;
            }

            public Builder potionsUsed(int count) {
                this.potionsUsed = count;
                return this;
            }

            public Builder pearlsUsed(int count) {
                this.pearlsUsed = count;
                return this;
            }

            public Builder hits(int count) {
                this.hits = count;
                return this;
            }

            public Builder criticalHits(int count) {
                this.criticalHits = count;
                return this;
            }

            public RoundStatistics build() {
                return new RoundStatistics(this.damageDealt, this.damageTaken, this.healthRemaining, this.potionsUsed, this.pearlsUsed, this.hits, this.criticalHits);
            }
        }
    }
}

