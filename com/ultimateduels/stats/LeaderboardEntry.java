/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.stats;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaderboardEntry {
    private final UUID playerUUID;
    private final String playerName;
    private final long value;
    private final double doubleValue;
    private final boolean isDouble;

    public LeaderboardEntry(@NotNull UUID playerUUID, @Nullable String playerName, long value) {
        this.playerUUID = playerUUID;
        this.playerName = playerName != null ? playerName : "Unknown";
        this.value = value;
        this.doubleValue = value;
        this.isDouble = false;
    }

    public LeaderboardEntry(@NotNull UUID playerUUID, @Nullable String playerName, double doubleValue) {
        this.playerUUID = playerUUID;
        this.playerName = playerName != null ? playerName : "Unknown";
        this.value = (long)doubleValue;
        this.doubleValue = doubleValue;
        this.isDouble = true;
    }

    @NotNull
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @NotNull
    public String getPlayerName() {
        return this.playerName;
    }

    public long getValue() {
        return this.value;
    }

    public double getDoubleValue() {
        return this.doubleValue;
    }

    public boolean isDoubleValue() {
        return this.isDouble;
    }

    public String toString() {
        return "LeaderboardEntry{playerName='" + this.playerName + "', value=" + (this.isDouble ? this.doubleValue : (double)this.value) + "}";
    }
}

