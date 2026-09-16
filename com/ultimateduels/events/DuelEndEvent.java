/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.events;

import com.ultimateduels.models.arena.Arena;
import com.ultimateduels.models.duel.Duel;
import com.ultimateduels.models.kit.Kit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DuelEndEvent
extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Duel duel;
    private final Kit kit;
    private final Arena arena;
    private final List<UUID> winners;
    private final List<UUID> losers;
    private final EndReason endReason;
    private final long durationMillis;
    private final int[] finalScore;
    private final boolean partyDuel;

    public DuelEndEvent(Duel duel, List<UUID> winners, List<UUID> losers, EndReason endReason) {
        this.duel = duel;
        this.kit = duel.getKit();
        this.arena = duel.getArena();
        this.winners = winners;
        this.losers = losers;
        this.endReason = endReason;
        this.durationMillis = duel.getMatchDuration();
        this.finalScore = new int[]{duel.getTeam1Score(), duel.getTeam2Score()};
        this.partyDuel = duel.isTeamDuel();
    }

    public Duel getDuel() {
        return this.duel;
    }

    public String getDuelId() {
        return this.duel.getDuelId();
    }

    public Kit getKit() {
        return this.kit;
    }

    public String getKitName() {
        return this.kit.getDisplayName();
    }

    public Arena getArena() {
        return this.arena;
    }

    public String getArenaName() {
        return this.arena.getDisplayName();
    }

    public List<UUID> getWinners() {
        return this.winners;
    }

    public List<UUID> getLosers() {
        return this.losers;
    }

    public List<UUID> getAllParticipants() {
        ArrayList<UUID> all = new ArrayList<UUID>(this.winners);
        all.addAll(this.losers);
        return all;
    }

    public EndReason getEndReason() {
        return this.endReason;
    }

    public boolean wasNormalEnd() {
        return this.endReason == EndReason.NORMAL_WIN;
    }

    public boolean isWinner(UUID playerId) {
        return this.winners.contains(playerId);
    }

    public boolean isLoser(UUID playerId) {
        return this.losers.contains(playerId);
    }

    public long getDurationMillis() {
        return this.durationMillis;
    }

    public long getDurationSeconds() {
        return this.durationMillis / 1000L;
    }

    public String getFormattedDuration() {
        long seconds = this.durationMillis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%d:%02d", minutes, seconds %= 60L);
    }

    public int[] getFinalScore() {
        return this.finalScore;
    }

    public int getWinnerScore() {
        return Math.max(this.finalScore[0], this.finalScore[1]);
    }

    public int getLoserScore() {
        return Math.min(this.finalScore[0], this.finalScore[1]);
    }

    public boolean isPartyDuel() {
        return this.partyDuel;
    }

    public boolean wasCloseMatch() {
        return Math.abs(this.finalScore[0] - this.finalScore[1]) <= 1;
    }

    public boolean wasShutout() {
        return Math.min(this.finalScore[0], this.finalScore[1]) == 0 && Math.max(this.finalScore[0], this.finalScore[1]) > 0;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public static enum EndReason {
        NORMAL_WIN("Match completed normally"),
        FORFEIT("Player forfeited"),
        DISCONNECT("Player disconnected"),
        ADMIN_END("Ended by administrator"),
        TIMEOUT("Match timed out"),
        ERROR("Error occurred");

        private final String description;

        private EndReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }
}

