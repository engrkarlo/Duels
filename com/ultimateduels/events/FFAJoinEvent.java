/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.events;

import com.ultimateduels.models.ffa.FFAArena;
import com.ultimateduels.models.kit.Kit;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFAJoinEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final UUID playerId;
    private final FFAArena arena;
    private final Kit kit;
    private final int currentPlayerCount;
    private final int maxPlayers;
    private final long timestamp;
    private boolean cancelled;
    private String cancelReason;

    public FFAJoinEvent(@NotNull Player player, @NotNull FFAArena arena, @NotNull Kit kit, int currentPlayerCount, int maxPlayers) {
        super(false);
        this.player = player;
        this.playerId = player.getUniqueId();
        this.arena = arena;
        this.kit = kit;
        this.currentPlayerCount = currentPlayerCount;
        this.maxPlayers = maxPlayers;
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
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

    public int getCurrentPlayerCount() {
        return this.currentPlayerCount;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getPlayerCountAfterJoin() {
        return this.currentPlayerCount + 1;
    }

    public boolean isArenaFull() {
        return this.maxPlayers > 0 && this.currentPlayerCount >= this.maxPlayers;
    }

    public boolean isArenaEmpty() {
        return this.currentPlayerCount == 0;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void setCancelled(boolean cancel, @Nullable String reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    @Nullable
    public String getCancelReason() {
        return this.cancelReason;
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
        return "FFAJoinEvent{player=" + this.player.getName() + ", arena=" + this.arena.getDisplayName() + ", kit=" + this.kit.getDisplayName() + ", playerCount=" + this.currentPlayerCount + ", cancelled=" + this.cancelled + "}";
    }
}

