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

import com.ultimateduels.models.duel.DuelSettings;
import com.ultimateduels.models.kit.Kit;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuelRequestEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player sender;
    private final Player target;
    private final UUID senderId;
    private final UUID targetId;
    private final DuelSettings settings;
    private final String kitId;
    private final int rounds;
    private final long timestamp;
    private final long expirationTime;
    private Kit kit;
    private boolean cancelled;
    private String cancelReason;

    public DuelRequestEvent(@NotNull Player sender, @NotNull Player target, @NotNull DuelSettings settings, long expirationTime) {
        super(false);
        this.sender = sender;
        this.target = target;
        this.senderId = sender.getUniqueId();
        this.targetId = target.getUniqueId();
        this.settings = settings;
        this.kitId = settings.getKitId();
        this.rounds = settings.getRounds();
        this.timestamp = System.currentTimeMillis();
        this.expirationTime = expirationTime;
        this.cancelled = false;
    }

    public DuelRequestEvent(@NotNull Player sender, @NotNull Player target, @NotNull DuelSettings settings, @Nullable Kit kit, long expirationTime) {
        this(sender, target, settings, expirationTime);
        this.kit = kit;
    }

    @NotNull
    public Player getSender() {
        return this.sender;
    }

    @NotNull
    public UUID getSenderId() {
        return this.senderId;
    }

    @NotNull
    public String getSenderName() {
        return this.sender.getName();
    }

    @NotNull
    public Player getTarget() {
        return this.target;
    }

    @NotNull
    public UUID getTargetId() {
        return this.targetId;
    }

    @NotNull
    public String getTargetName() {
        return this.target.getName();
    }

    @NotNull
    public DuelSettings getSettings() {
        return this.settings;
    }

    @NotNull
    public String getKitId() {
        return this.kitId != null ? this.kitId : "unknown";
    }

    @Nullable
    public Kit getKit() {
        return this.kit;
    }

    public void setKit(@Nullable Kit kit) {
        this.kit = kit;
    }

    public boolean hasKit() {
        return this.kit != null;
    }

    @NotNull
    public String getKitName() {
        if (this.kit != null) {
            return this.kit.getDisplayName();
        }
        return this.kitId != null ? this.kitId : "Unknown";
    }

    public int getRounds() {
        return this.rounds;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getExpirationTime() {
        return this.expirationTime;
    }

    public long getTimeUntilExpiration() {
        return this.expirationTime - System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.expirationTime;
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
        return "DuelRequestEvent{sender=" + this.sender.getName() + ", target=" + this.target.getName() + ", kitId=" + this.kitId + ", kitName=" + this.getKitName() + ", rounds=" + this.rounds + ", cancelled=" + this.cancelled + "}";
    }
}

