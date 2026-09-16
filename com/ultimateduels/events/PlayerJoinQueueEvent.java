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

import com.ultimateduels.models.kit.Kit;
import com.ultimateduels.models.queue.QueueType;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinQueueEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final UUID playerId;
    private final Kit kit;
    private final QueueType queueType;
    private final int currentQueueSize;
    private final int currentFightingCount;
    private final long timestamp;
    private boolean cancelled;
    private String cancelReason;
    private final boolean isPartyQueue;
    private final UUID partyId;
    private final int partySize;

    public PlayerJoinQueueEvent(@NotNull Player player, @NotNull Kit kit, int currentQueueSize, int currentFightingCount) {
        super(false);
        this.player = player;
        this.playerId = player.getUniqueId();
        this.kit = kit;
        this.queueType = QueueType.SOLO;
        this.currentQueueSize = currentQueueSize;
        this.currentFightingCount = currentFightingCount;
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
        this.isPartyQueue = false;
        this.partyId = null;
        this.partySize = 1;
    }

    public PlayerJoinQueueEvent(@NotNull Player player, @NotNull Kit kit, @NotNull UUID partyId, int partySize, int currentQueueSize, int currentFightingCount) {
        super(false);
        this.player = player;
        this.playerId = player.getUniqueId();
        this.kit = kit;
        this.queueType = QueueType.PARTY;
        this.currentQueueSize = currentQueueSize;
        this.currentFightingCount = currentFightingCount;
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
        this.isPartyQueue = true;
        this.partyId = partyId;
        this.partySize = partySize;
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
    public Kit getKit() {
        return this.kit;
    }

    @NotNull
    public String getKitName() {
        return this.kit.getDisplayName();
    }

    @NotNull
    public String getKitId() {
        return this.kit.getId();
    }

    @NotNull
    public QueueType getQueueType() {
        return this.queueType;
    }

    public int getCurrentQueueSize() {
        return this.currentQueueSize;
    }

    public int getCurrentFightingCount() {
        return this.currentFightingCount;
    }

    public int getNewQueueSize() {
        return this.currentQueueSize + (this.isPartyQueue ? this.partySize : 1);
    }

    public boolean isLikelyQuickMatch() {
        return this.currentQueueSize > 0;
    }

    public boolean isPartyQueue() {
        return this.isPartyQueue;
    }

    @Nullable
    public UUID getPartyId() {
        return this.partyId;
    }

    public int getPartySize() {
        return this.partySize;
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
        return "PlayerJoinQueueEvent{player=" + this.player.getName() + ", kit=" + this.kit.getDisplayName() + ", type=" + String.valueOf((Object)this.queueType) + ", queueSize=" + this.currentQueueSize + ", partyQueue=" + this.isPartyQueue + ", cancelled=" + this.cancelled + "}";
    }
}

