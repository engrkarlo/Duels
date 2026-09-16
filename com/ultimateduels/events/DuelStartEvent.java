/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
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
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DuelStartEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Duel duel;
    private final Kit kit;
    private final Arena arena;
    private final List<UUID> team1;
    private final List<UUID> team2;
    private final boolean partyDuel;
    private final int totalRounds;
    private boolean cancelled = false;
    private String cancelReason = null;

    public DuelStartEvent(Duel duel, Player player1, Player player2, Kit kit, Arena arena, int rounds) {
        this.duel = duel;
        this.kit = kit;
        this.arena = arena;
        this.team1 = List.of(player1.getUniqueId());
        this.team2 = List.of(player2.getUniqueId());
        this.partyDuel = false;
        this.totalRounds = rounds;
    }

    public DuelStartEvent(Duel duel, List<UUID> team1, List<UUID> team2, Kit kit, Arena arena, int rounds) {
        this.duel = duel;
        this.kit = kit;
        this.arena = arena;
        this.team1 = team1;
        this.team2 = team2;
        this.partyDuel = team1.size() > 1 || team2.size() > 1;
        this.totalRounds = rounds;
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

    public String getKitId() {
        return this.kit.getId();
    }

    public Arena getArena() {
        return this.arena;
    }

    public String getArenaName() {
        return this.arena.getDisplayName();
    }

    public String getArenaId() {
        return this.arena.getId();
    }

    public List<UUID> getTeam1() {
        return this.team1;
    }

    public List<UUID> getTeam2() {
        return this.team2;
    }

    public List<UUID> getAllParticipants() {
        ArrayList<UUID> all = new ArrayList<UUID>(this.team1);
        all.addAll(this.team2);
        return all;
    }

    public boolean isPartyDuel() {
        return this.partyDuel;
    }

    public int getTotalRounds() {
        return this.totalRounds;
    }

    public boolean isMultiRound() {
        return this.totalRounds > 1;
    }

    public int getTeamSize() {
        return Math.max(this.team1.size(), this.team2.size());
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setCancelled(boolean cancelled, String reason) {
        this.cancelled = cancelled;
        this.cancelReason = reason;
    }

    public String getCancelReason() {
        return this.cancelReason;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

