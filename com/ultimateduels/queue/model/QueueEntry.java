/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.queue.model;

import com.ultimateduels.queue.model.QueueType;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class QueueEntry {
    private final UUID playerUUID;
    private final String playerName;
    private final String kitName;
    private final long joinTime;
    private final int elo;
    private final int ping;
    private final QueueType queueType;
    private String preferredArena;
    private boolean rankedMode;

    public QueueEntry(@Nonnull UUID playerUUID, @Nonnull String playerName, @Nonnull String kitName, long joinTime, int elo, int ping, @Nonnull QueueType queueType) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.kitName = kitName.toLowerCase();
        this.joinTime = joinTime;
        this.elo = elo;
        this.ping = ping;
        this.queueType = queueType;
        this.preferredArena = null;
        this.rankedMode = false;
    }

    @Nonnull
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Nonnull
    public String getPlayerName() {
        return this.playerName;
    }

    @Nonnull
    public String getKitName() {
        return this.kitName;
    }

    public long getJoinTime() {
        return this.joinTime;
    }

    public int getElo() {
        return this.elo;
    }

    public int getPing() {
        return this.ping;
    }

    @Nonnull
    public QueueType getQueueType() {
        return this.queueType;
    }

    @Nonnull
    public String getPreferredArena() {
        return this.preferredArena != null ? this.preferredArena : "any";
    }

    public void setPreferredArena(String preferredArena) {
        this.preferredArena = preferredArena;
    }

    public boolean isRankedMode() {
        return this.rankedMode;
    }

    public void setRankedMode(boolean rankedMode) {
        this.rankedMode = rankedMode;
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
        QueueEntry that = (QueueEntry)o;
        return Objects.equals(this.playerUUID, that.playerUUID);
    }

    public int hashCode() {
        return Objects.hash(this.playerUUID);
    }

    public String toString() {
        return "QueueEntry{player=" + this.playerName + ", kit=" + this.kitName + ", elo=" + this.elo + ", waitTime=" + this.getQueueDurationSeconds() + "s}";
    }
}

