/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.queue.model;

import com.ultimateduels.queue.model.PartyQueueType;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PartyQueueEntry {
    private final UUID partyId;
    private final UUID leaderUUID;
    private final List<UUID> members;
    private final String kitName;
    private final PartyQueueType queueType;
    private final long joinTime;
    private final int averageElo;
    private final int averagePing;

    public PartyQueueEntry(@Nonnull UUID partyId, @Nonnull UUID leaderUUID, @Nonnull List<UUID> members, @Nonnull String kitName, @Nonnull PartyQueueType queueType, long joinTime, int averageElo, int averagePing) {
        this.partyId = partyId;
        this.leaderUUID = leaderUUID;
        this.members = members;
        this.kitName = kitName.toLowerCase();
        this.queueType = queueType;
        this.joinTime = joinTime;
        this.averageElo = averageElo;
        this.averagePing = averagePing;
    }

    @Nonnull
    public UUID getPartyId() {
        return this.partyId;
    }

    @Nonnull
    public UUID getLeaderUUID() {
        return this.leaderUUID;
    }

    @Nonnull
    public List<UUID> getMembers() {
        return this.members;
    }

    public int getSize() {
        return this.members.size();
    }

    @Nonnull
    public String getKitName() {
        return this.kitName;
    }

    @Nonnull
    public PartyQueueType getQueueType() {
        return this.queueType;
    }

    public long getJoinTime() {
        return this.joinTime;
    }

    public int getAverageElo() {
        return this.averageElo;
    }

    public int getAveragePing() {
        return this.averagePing;
    }

    public long getQueueDuration() {
        return System.currentTimeMillis() - this.joinTime;
    }

    public long getQueueDurationSeconds() {
        return this.getQueueDuration() / 1000L;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PartyQueueEntry that = (PartyQueueEntry)o;
        return Objects.equals(this.partyId, that.partyId);
    }

    public int hashCode() {
        return Objects.hash(this.partyId);
    }

    public String toString() {
        return "PartyQueueEntry{partyId=" + String.valueOf(this.partyId) + ", size=" + this.members.size() + ", kit=" + this.kitName + ", type=" + String.valueOf((Object)this.queueType) + ", avgElo=" + this.averageElo + "}";
    }
}

