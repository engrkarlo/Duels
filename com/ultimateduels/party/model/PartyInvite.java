/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.party.model;

import java.util.UUID;
import javax.annotation.Nonnull;

public class PartyInvite {
    private final UUID partyId;
    private final UUID inviterUUID;
    private final UUID targetUUID;
    private final long timestamp;

    public PartyInvite(@Nonnull UUID partyId, @Nonnull UUID inviterUUID, @Nonnull UUID targetUUID, long timestamp) {
        this.partyId = partyId;
        this.inviterUUID = inviterUUID;
        this.targetUUID = targetUUID;
        this.timestamp = timestamp;
    }

    @Nonnull
    public UUID getPartyId() {
        return this.partyId;
    }

    @Nonnull
    public UUID getInviterUUID() {
        return this.inviterUUID;
    }

    @Nonnull
    public UUID getTargetUUID() {
        return this.targetUUID;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - this.timestamp > timeoutMillis;
    }

    public long getAgeSeconds() {
        return (System.currentTimeMillis() - this.timestamp) / 1000L;
    }

    public String toString() {
        return "PartyInvite{party=" + this.partyId.toString().substring(0, 8) + ", inviter=" + String.valueOf(this.inviterUUID) + ", target=" + String.valueOf(this.targetUUID) + "}";
    }
}

