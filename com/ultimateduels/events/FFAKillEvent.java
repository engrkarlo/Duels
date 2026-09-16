/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Cancellable
 *  org.bukkit.event.Event
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 *  org.bukkit.inventory.ItemStack
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.events;

import com.ultimateduels.models.ffa.FFAArena;
import com.ultimateduels.models.kit.Kit;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFAKillEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player killer;
    private final Player victim;
    private final UUID killerId;
    private final UUID victimId;
    private final FFAArena arena;
    private final Kit kit;
    private final EntityDamageEvent.DamageCause deathCause;
    private final ItemStack weaponUsed;
    private final double finalDamage;
    private final Location deathLocation;
    private final long timestamp;
    private final int killerNewKillCount;
    private final int killerCurrentStreak;
    private final double killerHealthRemaining;
    private final int victimDeathCount;
    private final int victimStreakLost;
    private int killRewardPoints;
    private int streakBonusPoints;
    private boolean cancelled;
    private String cancelReason;

    public FFAKillEvent(@NotNull Player killer, @NotNull Player victim, @NotNull FFAArena arena, @NotNull Kit kit, @NotNull EntityDamageEvent.DamageCause deathCause, @Nullable ItemStack weaponUsed, double finalDamage, int killerNewKillCount, int killerCurrentStreak, double killerHealthRemaining, int victimDeathCount, int victimStreakLost) {
        super(false);
        this.killer = killer;
        this.victim = victim;
        this.killerId = killer.getUniqueId();
        this.victimId = victim.getUniqueId();
        this.arena = arena;
        this.kit = kit;
        this.deathCause = deathCause;
        this.weaponUsed = weaponUsed != null ? weaponUsed.clone() : null;
        this.finalDamage = finalDamage;
        this.deathLocation = victim.getLocation().clone();
        this.timestamp = System.currentTimeMillis();
        this.killerNewKillCount = killerNewKillCount;
        this.killerCurrentStreak = killerCurrentStreak;
        this.killerHealthRemaining = killerHealthRemaining;
        this.victimDeathCount = victimDeathCount;
        this.victimStreakLost = victimStreakLost;
        this.killRewardPoints = 10;
        this.streakBonusPoints = this.calculateStreakBonus(killerCurrentStreak);
        this.cancelled = false;
    }

    private int calculateStreakBonus(int streak) {
        if (streak >= 20) {
            return 50;
        }
        if (streak >= 15) {
            return 30;
        }
        if (streak >= 10) {
            return 20;
        }
        if (streak >= 5) {
            return 10;
        }
        if (streak >= 3) {
            return 5;
        }
        return 0;
    }

    @NotNull
    public Player getKiller() {
        return this.killer;
    }

    @NotNull
    public UUID getKillerId() {
        return this.killerId;
    }

    @NotNull
    public String getKillerName() {
        return this.killer.getName();
    }

    @NotNull
    public Player getVictim() {
        return this.victim;
    }

    @NotNull
    public UUID getVictimId() {
        return this.victimId;
    }

    @NotNull
    public String getVictimName() {
        return this.victim.getName();
    }

    @NotNull
    public FFAArena getArena() {
        return this.arena;
    }

    @NotNull
    public String getArenaName() {
        return this.arena.getDisplayName();
    }

    @NotNull
    public String getArenaId() {
        return this.arena.getArenaId();
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
    public EntityDamageEvent.DamageCause getDeathCause() {
        return this.deathCause;
    }

    @Nullable
    public ItemStack getWeaponUsed() {
        return this.weaponUsed != null ? this.weaponUsed.clone() : null;
    }

    public double getFinalDamage() {
        return this.finalDamage;
    }

    @NotNull
    public Location getDeathLocation() {
        return this.deathLocation.clone();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getKillerNewKillCount() {
        return this.killerNewKillCount;
    }

    public int getKillerCurrentStreak() {
        return this.killerCurrentStreak;
    }

    public double getKillerHealthRemaining() {
        return this.killerHealthRemaining;
    }

    public int getVictimDeathCount() {
        return this.victimDeathCount;
    }

    public int getVictimStreakLost() {
        return this.victimStreakLost;
    }

    public boolean victimHadStreak() {
        return this.victimStreakLost > 0;
    }

    public boolean isStreakShutdown() {
        return this.victimStreakLost >= 5;
    }

    public int getKillRewardPoints() {
        return this.killRewardPoints;
    }

    public void setKillRewardPoints(int points) {
        this.killRewardPoints = Math.max(0, points);
    }

    public int getStreakBonusPoints() {
        return this.streakBonusPoints;
    }

    public void setStreakBonusPoints(int points) {
        this.streakBonusPoints = Math.max(0, points);
    }

    public int getTotalPoints() {
        return this.killRewardPoints + this.streakBonusPoints;
    }

    public boolean isStreakMilestone() {
        return this.killerCurrentStreak == 5 || this.killerCurrentStreak == 10 || this.killerCurrentStreak == 15 || this.killerCurrentStreak == 20 || this.killerCurrentStreak == 25 || this.killerCurrentStreak == 50 || this.killerCurrentStreak == 100;
    }

    @NotNull
    public String getStreakTitle() {
        if (this.killerCurrentStreak >= 25) {
            return "GODLIKE";
        }
        if (this.killerCurrentStreak >= 20) {
            return "UNSTOPPABLE";
        }
        if (this.killerCurrentStreak >= 15) {
            return "DOMINATING";
        }
        if (this.killerCurrentStreak >= 10) {
            return "RAMPAGE";
        }
        if (this.killerCurrentStreak >= 5) {
            return "KILLING SPREE";
        }
        return "";
    }

    public boolean isVoidKill() {
        return this.deathCause == EntityDamageEvent.DamageCause.VOID;
    }

    public boolean isCloseCall() {
        return this.killerHealthRemaining <= 4.0;
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
        return "FFAKillEvent{killer=" + this.killer.getName() + ", victim=" + this.victim.getName() + ", arena=" + this.arena.getDisplayName() + ", kit=" + this.kit.getDisplayName() + ", streak=" + this.killerCurrentStreak + ", cause=" + String.valueOf(this.deathCause) + ", cancelled=" + this.cancelled + "}";
    }
}

