/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 */
package com.ultimateduels.duel.model;

import com.ultimateduels.duel.model.WinCondition;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuelRequest {
    private final UUID senderUUID;
    private final UUID targetUUID;
    private final String kitName;
    private final String arenaName;
    private final int rounds;
    private final boolean bestOf;
    private final long timestamp;
    private WinCondition winCondition;

    public DuelRequest(@Nonnull UUID senderUUID, @Nonnull UUID targetUUID, @Nonnull String kitName, @Nullable String arenaName, int rounds, boolean bestOf, long timestamp) {
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.rounds = rounds;
        this.bestOf = bestOf;
        this.timestamp = timestamp;
    }

    @Nonnull
    public UUID getSenderUUID() {
        return this.senderUUID;
    }

    @Nonnull
    public UUID getTargetUUID() {
        return this.targetUUID;
    }

    @Nonnull
    public String getKitName() {
        return this.kitName;
    }

    @Nullable
    public String getArenaName() {
        return this.arenaName;
    }

    public int getRounds() {
        return this.rounds;
    }

    public boolean isBestOf() {
        return this.bestOf;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public WinCondition getWinCondition() {
        return this.winCondition;
    }

    public void setWinCondition(WinCondition winCondition) {
        this.winCondition = winCondition;
    }

    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - this.timestamp > timeoutMillis;
    }

    public long getAgeSeconds() {
        return (System.currentTimeMillis() - this.timestamp) / 1000L;
    }

    public String toString() {
        return "DuelRequest{sender=" + String.valueOf(this.senderUUID) + ", target=" + String.valueOf(this.targetUUID) + ", kit=" + this.kitName + ", rounds=" + this.rounds + ", bestOf=" + this.bestOf + "}";
    }
}

