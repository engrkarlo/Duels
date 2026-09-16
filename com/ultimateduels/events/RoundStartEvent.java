/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.Cancellable
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.events;

import com.ultimateduels.models.duel.Duel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RoundStartEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Duel duel;
    private final int roundNumber;
    private final int totalRounds;
    private final int[] currentScore;
    private final List<UUID> team1Alive;
    private final List<UUID> team2Alive;
    private boolean cancelled = false;

    public RoundStartEvent(Duel duel, int roundNumber, List<UUID> team1Alive, List<UUID> team2Alive) {
        this.duel = duel;
        this.roundNumber = roundNumber;
        this.totalRounds = duel.getMaxRounds();
        this.currentScore = new int[]{duel.getTeam1Score(), duel.getTeam2Score()};
        this.team1Alive = team1Alive;
        this.team2Alive = team2Alive;
    }

    public Duel getDuel() {
        return this.duel;
    }

    public String getDuelId() {
        return this.duel.getDuelId();
    }

    public int getRoundNumber() {
        return this.roundNumber;
    }

    public int getTotalRounds() {
        return this.totalRounds;
    }

    public boolean isFirstRound() {
        return this.roundNumber == 1;
    }

    public boolean isFinalRound() {
        return this.roundNumber == this.totalRounds;
    }

    public boolean isMatchPointRound() {
        int toWin = this.totalRounds / 2 + 1;
        return this.currentScore[0] == toWin - 1 || this.currentScore[1] == toWin - 1;
    }

    public int[] getCurrentScore() {
        return this.currentScore;
    }

    public int getTeam1Score() {
        return this.currentScore[0];
    }

    public int getTeam2Score() {
        return this.currentScore[1];
    }

    public String getFormattedScore() {
        return this.currentScore[0] + " - " + this.currentScore[1];
    }

    public List<UUID> getTeam1Alive() {
        return this.team1Alive;
    }

    public List<UUID> getTeam2Alive() {
        return this.team2Alive;
    }

    public List<UUID> getAllAlivePlayers() {
        ArrayList<UUID> all = new ArrayList<UUID>(this.team1Alive);
        all.addAll(this.team2Alive);
        return all;
    }

    public int getRemainingRounds() {
        return this.totalRounds - this.roundNumber + 1;
    }

    public String getKitName() {
        return this.duel.getKit().getDisplayName();
    }

    public String getKitId() {
        return this.duel.getKit().getId();
    }

    public String getArenaName() {
        return this.duel.getArena().getDisplayName();
    }

    public String getArenaId() {
        return this.duel.getArena().getId();
    }

    public boolean isPartyDuel() {
        return this.duel.isTeamDuel();
    }

    public boolean isTeamDuel() {
        return this.duel.isTeamDuel();
    }

    public UUID getTeamOnMatchPoint() {
        int toWin = this.totalRounds / 2 + 1;
        if (this.currentScore[0] == toWin - 1) {
            return this.team1Alive.isEmpty() ? null : this.team1Alive.get(0);
        }
        if (this.currentScore[1] == toWin - 1) {
            return this.team2Alive.isEmpty() ? null : this.team2Alive.get(0);
        }
        return null;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

