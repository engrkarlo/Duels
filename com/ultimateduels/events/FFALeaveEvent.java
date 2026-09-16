/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.events;

import com.ultimateduels.models.ffa.FFAArena;
import com.ultimateduels.models.kit.Kit;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FFALeaveEvent
extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final UUID playerId;
    private final FFAArena arena;
    private final Kit kit;
    private final LeaveReason leaveReason;
    private final int sessionKills;
    private final int sessionDeaths;
    private final int bestStreak;
    private final long sessionDurationMillis;
    private final int playerCountAfter;
    private final long timestamp;

    public FFALeaveEvent(@NotNull Player player, @NotNull FFAArena arena, @NotNull Kit kit, @NotNull LeaveReason leaveReason, int sessionKills, int sessionDeaths, int bestStreak, long sessionDurationMillis, int playerCountAfter) {
        super(false);
        this.player = player;
        this.playerId = player.getUniqueId();
        this.arena = arena;
        this.kit = kit;
        this.leaveReason = leaveReason;
        this.sessionKills = sessionKills;
        this.sessionDeaths = sessionDeaths;
        this.bestStreak = bestStreak;
        this.sessionDurationMillis = sessionDurationMillis;
        this.playerCountAfter = playerCountAfter;
        this.timestamp = System.currentTimeMillis();
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public UUID getPlayerId() {
        return this.playerId;
    }

    @NotNull
    public String getPlayerName() {
        return this.player.getName();
    }

    @NotNull
    public FFAArena getArena() {
        return this.arena;
    }

    @NotNull
    public String getArenaId() {
        return this.arena.getArenaId();
    }

    @NotNull
    public String getArenaName() {
        return this.arena.getDisplayName();
    }

    @NotNull
    public Kit getKit() {
        return this.kit;
    }

    @NotNull
    public String getKitId() {
        return this.kit.getId();
    }

    @NotNull
    public String getKitName() {
        return this.kit.getDisplayName();
    }

    @NotNull
    public LeaveReason getLeaveReason() {
        return this.leaveReason;
    }

    public int getSessionKills() {
        return this.sessionKills;
    }

    public int getSessionDeaths() {
        return this.sessionDeaths;
    }

    public int getBestStreak() {
        return this.bestStreak;
    }

    public double getSessionKDR() {
        if (this.sessionDeaths == 0) {
            return this.sessionKills;
        }
        return (double)this.sessionKills / (double)this.sessionDeaths;
    }

    @NotNull
    public String getFormattedKDR() {
        return String.format("%.2f", this.getSessionKDR());
    }

    public long getSessionDurationMillis() {
        return this.sessionDurationMillis;
    }

    public long getSessionDurationSeconds() {
        return this.sessionDurationMillis / 1000L;
    }

    @NotNull
    public String getFormattedSessionDuration() {
        long seconds = this.getSessionDurationSeconds();
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public int getPlayerCountAfter() {
        return this.playerCountAfter;
    }

    public boolean isArenaEmptyAfter() {
        return this.playerCountAfter == 0;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean wasDeath() {
        return this.leaveReason == LeaveReason.DEATH;
    }

    public boolean wasVoluntary() {
        return this.leaveReason == LeaveReason.MANUAL;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public String toString() {
        return "FFALeaveEvent{player=" + this.player.getName() + ", arena=" + this.arena.getDisplayName() + ", kit=" + this.kit.getDisplayName() + ", reason=" + String.valueOf((Object)this.leaveReason) + ", kills=" + this.sessionKills + ", deaths=" + this.sessionDeaths + ", duration=" + this.getFormattedSessionDuration() + "}";
    }

    public static enum LeaveReason {
        DEATH,
        MANUAL,
        DISCONNECT,
        ADMIN_KICK,
        ARENA_CLOSED,
        SERVER_SHUTDOWN,
        JOINED_QUEUE,
        JOINED_DUEL;

    }
}

