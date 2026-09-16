/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.duel.model;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class DuelParticipant {
    private final UUID uuid;
    private final String playerName;
    private final int teamId;
    private boolean alive;
    private int kills;
    private int deaths;
    private int damageDealt;
    private int damageTaken;
    private int roundsWon;

    public DuelParticipant(@Nonnull UUID uuid, @Nonnull String playerName, int teamId) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.teamId = teamId;
        this.alive = true;
        this.kills = 0;
        this.deaths = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.roundsWon = 0;
    }

    @Nonnull
    public UUID getUuid() {
        return this.uuid;
    }

    @Nonnull
    public String getPlayerName() {
        return this.playerName;
    }

    public int getTeamId() {
        return this.teamId;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getKills() {
        return this.kills;
    }

    public void incrementKills() {
        ++this.kills;
    }

    public void addKills(int amount) {
        this.kills += amount;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void incrementDeaths() {
        ++this.deaths;
    }

    public void addDeaths(int amount) {
        this.deaths += amount;
    }

    public int getDamageDealt() {
        return this.damageDealt;
    }

    public void addDamageDealt(int amount) {
        this.damageDealt += amount;
    }

    public int getDamageTaken() {
        return this.damageTaken;
    }

    public void addDamageTaken(int amount) {
        this.damageTaken += amount;
    }

    public int getRoundsWon() {
        return this.roundsWon;
    }

    public void incrementRoundsWon() {
        ++this.roundsWon;
    }

    public double getKDRatio() {
        if (this.deaths == 0) {
            return this.kills;
        }
        return (double)this.kills / (double)this.deaths;
    }

    public void resetRoundStats() {
        this.alive = true;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelParticipant that = (DuelParticipant)o;
        return Objects.equals(this.uuid, that.uuid);
    }

    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    public String toString() {
        return "DuelParticipant{name=" + this.playerName + ", team=" + this.teamId + ", alive=" + this.alive + ", kills=" + this.kills + ", deaths=" + this.deaths + "}";
    }
}

