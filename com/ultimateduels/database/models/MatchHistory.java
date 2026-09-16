/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.database.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class MatchHistory {
    private final long id;
    private final UUID matchUuid;
    private final MatchType matchType;
    private final UUID winnerUuid;
    private final UUID loserUuid;
    private final String winnerTeam;
    private final String loserTeam;
    private final String kit;
    private final String arena;
    private final int roundsPlayed;
    private final int maxRounds;
    private final int winnerScore;
    private final int loserScore;
    private final WinCondition winCondition;
    private final int durationSeconds;
    private final int eloChange;
    private final Timestamp startedAt;
    private final Timestamp endedAt;

    private MatchHistory(Builder builder) {
        this.id = builder.id;
        this.matchUuid = builder.matchUuid;
        this.matchType = builder.matchType;
        this.winnerUuid = builder.winnerUuid;
        this.loserUuid = builder.loserUuid;
        this.winnerTeam = builder.winnerTeam;
        this.loserTeam = builder.loserTeam;
        this.kit = builder.kit;
        this.arena = builder.arena;
        this.roundsPlayed = builder.roundsPlayed;
        this.maxRounds = builder.maxRounds;
        this.winnerScore = builder.winnerScore;
        this.loserScore = builder.loserScore;
        this.winCondition = builder.winCondition;
        this.durationSeconds = builder.durationSeconds;
        this.eloChange = builder.eloChange;
        this.startedAt = builder.startedAt;
        this.endedAt = builder.endedAt;
    }

    public static MatchHistory fromResultSet(ResultSet rs) throws SQLException {
        return new Builder(UUID.fromString(rs.getString("match_uuid"))).id(rs.getLong("id")).matchType(MatchType.valueOf(rs.getString("match_type"))).winnerUuid(MatchHistory.parseUuid(rs.getString("winner_uuid"))).loserUuid(MatchHistory.parseUuid(rs.getString("loser_uuid"))).winnerTeam(rs.getString("winner_team")).loserTeam(rs.getString("loser_team")).kit(rs.getString("kit")).arena(rs.getString("arena")).roundsPlayed(rs.getInt("rounds_played")).maxRounds(rs.getInt("max_rounds")).winnerScore(rs.getInt("winner_score")).loserScore(rs.getInt("loser_score")).winCondition(WinCondition.valueOf(rs.getString("win_condition"))).durationSeconds(rs.getInt("duration_seconds")).eloChange(rs.getInt("elo_change")).startedAt(rs.getTimestamp("started_at")).endedAt(rs.getTimestamp("ended_at")).build();
    }

    private static UUID parseUuid(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(str);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getId() {
        return this.id;
    }

    public UUID getMatchUuid() {
        return this.matchUuid;
    }

    public MatchType getMatchType() {
        return this.matchType;
    }

    public UUID getWinnerUuid() {
        return this.winnerUuid;
    }

    public UUID getLoserUuid() {
        return this.loserUuid;
    }

    public String getWinnerTeam() {
        return this.winnerTeam;
    }

    public String getLoserTeam() {
        return this.loserTeam;
    }

    public String getKit() {
        return this.kit;
    }

    public String getArena() {
        return this.arena;
    }

    public int getRoundsPlayed() {
        return this.roundsPlayed;
    }

    public int getMaxRounds() {
        return this.maxRounds;
    }

    public int getWinnerScore() {
        return this.winnerScore;
    }

    public int getLoserScore() {
        return this.loserScore;
    }

    public WinCondition getWinCondition() {
        return this.winCondition;
    }

    public int getDurationSeconds() {
        return this.durationSeconds;
    }

    public int getEloChange() {
        return this.eloChange;
    }

    public Timestamp getStartedAt() {
        return this.startedAt;
    }

    public Timestamp getEndedAt() {
        return this.endedAt;
    }

    public String getFormattedDuration() {
        int minutes = this.durationSeconds / 60;
        int seconds = this.durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFormattedScore() {
        return this.winnerScore + " - " + this.loserScore;
    }

    public boolean involvedPlayer(UUID playerUuid) {
        return Objects.equals(this.winnerUuid, playerUuid) || Objects.equals(this.loserUuid, playerUuid);
    }

    public boolean didPlayerWin(UUID playerUuid) {
        return Objects.equals(this.winnerUuid, playerUuid);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MatchHistory that = (MatchHistory)o;
        return Objects.equals(this.matchUuid, that.matchUuid);
    }

    public int hashCode() {
        return Objects.hash(this.matchUuid);
    }

    public static class Builder {
        private long id;
        private final UUID matchUuid;
        private MatchType matchType = MatchType.DUEL_1V1;
        private UUID winnerUuid;
        private UUID loserUuid;
        private String winnerTeam;
        private String loserTeam;
        private String kit;
        private String arena;
        private int roundsPlayed = 1;
        private int maxRounds = 1;
        private int winnerScore = 1;
        private int loserScore = 0;
        private WinCondition winCondition = WinCondition.BEST_OF;
        private int durationSeconds = 0;
        private int eloChange = 0;
        private Timestamp startedAt = new Timestamp(System.currentTimeMillis());
        private Timestamp endedAt;

        public Builder(UUID matchUuid) {
            this.matchUuid = matchUuid;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder matchType(MatchType type) {
            this.matchType = type;
            return this;
        }

        public Builder winnerUuid(UUID uuid) {
            this.winnerUuid = uuid;
            return this;
        }

        public Builder loserUuid(UUID uuid) {
            this.loserUuid = uuid;
            return this;
        }

        public Builder winnerTeam(String team) {
            this.winnerTeam = team;
            return this;
        }

        public Builder loserTeam(String team) {
            this.loserTeam = team;
            return this;
        }

        public Builder kit(String kit) {
            this.kit = kit;
            return this;
        }

        public Builder arena(String arena) {
            this.arena = arena;
            return this;
        }

        public Builder roundsPlayed(int rounds) {
            this.roundsPlayed = rounds;
            return this;
        }

        public Builder maxRounds(int rounds) {
            this.maxRounds = rounds;
            return this;
        }

        public Builder winnerScore(int score) {
            this.winnerScore = score;
            return this;
        }

        public Builder loserScore(int score) {
            this.loserScore = score;
            return this;
        }

        public Builder winCondition(WinCondition condition) {
            this.winCondition = condition;
            return this;
        }

        public Builder durationSeconds(int seconds) {
            this.durationSeconds = seconds;
            return this;
        }

        public Builder eloChange(int change) {
            this.eloChange = change;
            return this;
        }

        public Builder startedAt(Timestamp timestamp) {
            this.startedAt = timestamp;
            return this;
        }

        public Builder endedAt(Timestamp timestamp) {
            this.endedAt = timestamp;
            return this;
        }

        public MatchHistory build() {
            return new MatchHistory(this);
        }
    }

    public static enum MatchType {
        DUEL_1V1,
        PARTY_VS_PARTY,
        PARTY_SPLIT,
        FFA;

    }

    public static enum WinCondition {
        BEST_OF,
        PLAY_ALL;

    }
}

