/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.duel.model;

import com.ultimateduels.duel.model.MatchType;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public record MatchResult(@Nonnull UUID matchId, @Nonnull MatchType matchType, @Nonnull String kitName, int team1Score, int team2Score, int winningTeam, long duration, long timestamp, @Nonnull List<UUID> participants) {
    @Nonnull
    public String getScoreString() {
        return this.team1Score + " - " + this.team2Score;
    }

    @Nonnull
    public String getDurationString() {
        long seconds = this.duration / 1000L;
        long minutes = seconds / 60L;
        if (minutes > 0L) {
            return String.format("%d:%02d", minutes, seconds % 60L);
        }
        return String.format("0:%02d", seconds);
    }

    public boolean hadParticipant(@Nonnull UUID playerUUID) {
        return this.participants.contains(playerUUID);
    }

    public long getAge() {
        return System.currentTimeMillis() - this.timestamp;
    }
}

