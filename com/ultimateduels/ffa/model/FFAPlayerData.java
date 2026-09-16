/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.ffa.model;

import java.util.UUID;
import javax.annotation.Nonnull;

public class FFAPlayerData {
    private final UUID playerUUID;
    private final String playerName;
    private final String arenaName;
    private final long joinTime;
    private int kills;
    private int deaths;
    private int killstreak;
    private int highestKillstreak;
    private boolean spawnProtected;
    private boolean respawning;

    public FFAPlayerData(@Nonnull UUID playerUUID, @Nonnull String playerName, @Nonnull String arenaName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.arenaName = arenaName;
        this.joinTime = System.currentTimeMillis();
        this.kills = 0;
        this.deaths = 0;
        this.killstreak = 0;
        this.highestKillstreak = 0;
        this.spawnProtected = false;
        this.respawning = false;
    }

    public int getKills() {
        return this.kills;
    }

    public void incrementKills() {
        ++this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void incrementDeaths() {
        ++this.deaths;
    }

    public int getKillstreak() {
        return this.killstreak;
    }

    public void incrementKillstreak() {
        ++this.killstreak;
        if (this.killstreak > this.highestKillstreak) {
            this.highestKillstreak = this.killstreak;
        }
    }

    public void resetKillstreak() {
        this.killstreak = 0;
    }

    public int getHighestKillstreak() {
        return this.highestKillstreak;
    }

    public double getKDRatio() {
        if (this.deaths == 0) {
            return this.kills;
        }
        return (double)this.kills / (double)this.deaths;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - this.joinTime;
    }

    public boolean isSpawnProtected() {
        return this.spawnProtected;
    }

    public void setSpawnProtected(boolean spawnProtected) {
        this.spawnProtected = spawnProtected;
    }

    public boolean isRespawning() {
        return this.respawning;
    }

    public void setRespawning(boolean respawning) {
        this.respawning = respawning;
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
    public String getArenaName() {
        return this.arenaName;
    }

    public long getJoinTime() {
        return this.joinTime;
    }

    public String toString() {
        return "FFAPlayerData{player=" + this.playerName + ", kills=" + this.kills + ", deaths=" + this.deaths + ", streak=" + this.killstreak + "}";
    }
}

