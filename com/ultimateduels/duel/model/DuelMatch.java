/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package com.ultimateduels.duel.model;

import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.duel.model.MatchType;
import com.ultimateduels.duel.model.WinCondition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuelMatch {
    private final UUID matchId;
    private final MatchType matchType;
    private final List<DuelParticipant> team1;
    private final List<DuelParticipant> team2;
    private final String kitName;
    private final DuelArena arena;
    private final int totalRounds;
    private final WinCondition winCondition;
    private MatchState state;
    private int currentRound;
    private int team1Score;
    private int team2Score;
    private int winningTeam;
    private long startTime;
    private long endTime;
    private long roundStartTime;

    public DuelMatch(@Nonnull UUID matchId, @Nonnull MatchType matchType, @Nonnull List<DuelParticipant> team1, @Nonnull List<DuelParticipant> team2, @Nonnull String kitName, @Nonnull DuelArena arena, int totalRounds, @Nonnull WinCondition winCondition) {
        this.matchId = matchId;
        this.matchType = matchType;
        this.team1 = new ArrayList<DuelParticipant>(team1);
        this.team2 = new ArrayList<DuelParticipant>(team2);
        this.kitName = kitName;
        this.arena = arena;
        this.totalRounds = Math.max(1, Math.min(totalRounds, 20));
        this.winCondition = winCondition;
        this.state = MatchState.PENDING;
        this.currentRound = 1;
        this.team1Score = 0;
        this.team2Score = 0;
        this.winningTeam = 0;
        this.startTime = 0L;
        this.endTime = 0L;
        this.roundStartTime = 0L;
    }

    @Nonnull
    public UUID getMatchId() {
        return this.matchId;
    }

    @Nonnull
    public MatchType getMatchType() {
        return this.matchType;
    }

    @Nonnull
    public List<DuelParticipant> getTeam1() {
        return Collections.unmodifiableList(this.team1);
    }

    @Nonnull
    public List<DuelParticipant> getTeam2() {
        return Collections.unmodifiableList(this.team2);
    }

    @Nonnull
    public List<DuelParticipant> getTeamParticipants(int teamId) {
        return teamId == 1 ? this.getTeam1() : this.getTeam2();
    }

    @Nonnull
    public List<DuelParticipant> getAllParticipants() {
        ArrayList<DuelParticipant> all = new ArrayList<DuelParticipant>(this.team1);
        all.addAll(this.team2);
        return all;
    }

    @Nullable
    public DuelParticipant getParticipant(@Nonnull UUID playerUUID) {
        for (DuelParticipant p : this.team1) {
            if (!p.getUuid().equals(playerUUID)) continue;
            return p;
        }
        for (DuelParticipant p : this.team2) {
            if (!p.getUuid().equals(playerUUID)) continue;
            return p;
        }
        return null;
    }

    @Nullable
    public UUID getOpponent(@Nonnull UUID playerUUID) {
        List<DuelParticipant> opponentTeam;
        DuelParticipant participant = this.getParticipant(playerUUID);
        if (participant == null) {
            return null;
        }
        List<DuelParticipant> list = opponentTeam = participant.getTeamId() == 1 ? this.team2 : this.team1;
        if (opponentTeam.isEmpty()) {
            return null;
        }
        return opponentTeam.get(0).getUuid();
    }

    @Nonnull
    public String getKitName() {
        return this.kitName;
    }

    @Nonnull
    public DuelArena getArena() {
        return this.arena;
    }

    public int getTotalRounds() {
        return this.totalRounds;
    }

    @Nonnull
    public WinCondition getWinCondition() {
        return this.winCondition;
    }

    @Nonnull
    public MatchState getState() {
        return this.state;
    }

    public void setState(@Nonnull MatchState state) {
        this.state = state;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public void incrementRound() {
        ++this.currentRound;
        this.roundStartTime = System.currentTimeMillis();
    }

    public int getTeamScore(int teamId) {
        return teamId == 1 ? this.team1Score : this.team2Score;
    }

    public void addScore(int teamId, int points) {
        if (teamId == 1) {
            this.team1Score += points;
        } else {
            this.team2Score += points;
        }
    }

    public int getScore(@Nonnull UUID playerUUID) {
        DuelParticipant participant = this.getParticipant(playerUUID);
        if (participant == null) {
            return 0;
        }
        return this.getTeamScore(participant.getTeamId());
    }

    @Nonnull
    public String getScoreString() {
        return this.team1Score + " - " + this.team2Score;
    }

    public int getWinningTeam() {
        return this.winningTeam;
    }

    public void setWinningTeam(int winningTeam) {
        this.winningTeam = winningTeam;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        this.roundStartTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getRoundStartTime() {
        return this.roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public long getMatchDuration() {
        if (this.startTime == 0L) {
            return 0L;
        }
        if (this.endTime > 0L) {
            return this.endTime - this.startTime;
        }
        return System.currentTimeMillis() - this.startTime;
    }

    public long getRoundDuration() {
        if (this.roundStartTime == 0L) {
            return 0L;
        }
        return System.currentTimeMillis() - this.roundStartTime;
    }

    public boolean isTeamMatch() {
        return this.team1.size() > 1 || this.team2.size() > 1;
    }

    public int getTeamSize() {
        return Math.max(this.team1.size(), this.team2.size());
    }

    public int getAliveCount(int teamId) {
        return (int)this.getTeamParticipants(teamId).stream().filter(DuelParticipant::isAlive).count();
    }

    @Nonnull
    public String getTeamNames(int teamId) {
        return this.getTeamParticipants(teamId).stream().map(DuelParticipant::getPlayerName).collect(Collectors.joining(", "));
    }

    public boolean isActive() {
        return this.state == MatchState.IN_PROGRESS || this.state == MatchState.ROUND_ENDING;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelMatch match = (DuelMatch)o;
        return Objects.equals(this.matchId, match.matchId);
    }

    public int hashCode() {
        return Objects.hash(this.matchId);
    }

    public String toString() {
        return "DuelMatch{id=" + this.matchId.toString().substring(0, 8) + ", type=" + String.valueOf((Object)this.matchType) + ", kit=" + this.kitName + ", arena=" + this.arena.getName() + ", round=" + this.currentRound + "/" + this.totalRounds + ", score=" + this.team1Score + "-" + this.team2Score + ", state=" + String.valueOf((Object)this.state) + "}";
    }

    public boolean isPartyFFA() {
        return this.matchType == MatchType.PARTY_FFA;
    }

    @Nonnull
    public List<DuelParticipant> getAliveParticipants() {
        return this.getAllParticipants().stream().filter(DuelParticipant::isAlive).collect(Collectors.toList());
    }

    public int getAliveCount() {
        return (int)this.getAllParticipants().stream().filter(DuelParticipant::isAlive).count();
    }
}

