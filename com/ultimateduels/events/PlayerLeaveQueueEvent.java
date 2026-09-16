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

public class PlayerLeaveQueueEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final UUID playerId;
    private final Kit kit;
    private final QueueType queueType;
    private final LeaveReason leaveReason;
    private final long queueDurationMillis;
    private final int queueSizeAfter;
    private final long timestamp;
    private boolean cancelled;
    private String cancelReason;
    private final boolean wasMatched;
    private final UUID matchedOpponentId;

    public PlayerLeaveQueueEvent(@NotNull Player player, @NotNull Kit kit, @NotNull QueueType queueType, @NotNull LeaveReason leaveReason, long queueDurationMillis, int queueSizeAfter) {
        super(false);
        this.player = player;
        this.playerId = player.getUniqueId();
        this.kit = kit;
        this.queueType = queueType;
        this.leaveReason = leaveReason;
        this.queueDurationMillis = queueDurationMillis;
        this.queueSizeAfter = queueSizeAfter;
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
        this.wasMatched = leaveReason == LeaveReason.MATCHED;
        this.matchedOpponentId = null;
    }

    public PlayerLeaveQueueEvent(@NotNull Player player, @NotNull Kit kit, @NotNull QueueType queueType, long queueDurationMillis, int queueSizeAfter, @NotNull UUID opponentId) {
        super(false);
        this.player = player;
        this.playerId = player.getUniqueId();
        this.kit = kit;
        this.queueType = queueType;
        this.leaveReason = LeaveReason.MATCHED;
        this.queueDurationMillis = queueDurationMillis;
        this.queueSizeAfter = queueSizeAfter;
        this.timestamp = System.currentTimeMillis();
        this.cancelled = false;
        this.wasMatched = true;
        this.matchedOpponentId = opponentId;
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
    public String getKitId() {
        return this.kit.getId();
    }

    @NotNull
    public String getKitName() {
        return this.kit.getDisplayName();
    }

    @NotNull
    public QueueType getQueueType() {
        return this.queueType;
    }

    @NotNull
    public LeaveReason getLeaveReason() {
        return this.leaveReason;
    }

    public long getQueueDurationMillis() {
        return this.queueDurationMillis;
    }

    public long getQueueDurationSeconds() {
        return this.queueDurationMillis / 1000L;
    }

    @NotNull
    public String getFormattedQueueDuration() {
        long seconds = this.getQueueDurationSeconds();
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public int getQueueSizeAfter() {
        return this.queueSizeAfter;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean wasMatched() {
        return this.wasMatched;
    }

    @Nullable
    public UUID getMatchedOpponentId() {
        return this.matchedOpponentId;
    }

    public boolean isVoluntary() {
        return this.leaveReason == LeaveReason.MANUAL || this.leaveReason == LeaveReason.QUEUE_SWITCH;
    }

    public boolean isPositiveLeave() {
        return this.leaveReason == LeaveReason.MATCHED;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        if (cancel && (this.leaveReason == LeaveReason.MATCHED || this.leaveReason == LeaveReason.DISCONNECT || this.leaveReason == LeaveReason.SERVER_SHUTDOWN)) {
            return;
        }
        this.cancelled = cancel;
    }

    public void setCancelled(boolean cancel, @Nullable String reason) {
        this.setCancelled(cancel);
        this.cancelReason = reason;
    }

    @Nullable
    public String getCancelReason() {
        return this.cancelReason;
    }

    public boolean canBeCancelled() {
        return this.leaveReason != LeaveReason.MATCHED && this.leaveReason != LeaveReason.DISCONNECT && this.leaveReason != LeaveReason.SERVER_SHUTDOWN;
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
        return "PlayerLeaveQueueEvent{player=" + this.player.getName() + ", kit=" + this.kit.getDisplayName() + ", reason=" + String.valueOf((Object)this.leaveReason) + ", duration=" + this.getFormattedQueueDuration() + ", matched=" + this.wasMatched + ", cancelled=" + this.cancelled + "}";
    }

    public static enum LeaveReason {
        MANUAL,
        MATCHED,
        DISCONNECT,
        ADMIN_REMOVE,
        QUEUE_RESET,
        QUEUE_SWITCH,
        PARTY_DISBANDED,
        LEFT_PARTY,
        SERVER_SHUTDOWN,
        TIMEOUT;

    }
}

