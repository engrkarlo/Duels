/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.cooldown.CooldownManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CooldownCleanupTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final CooldownManager cooldownManager;
    private int cleanupCount;
    private int totalEntriesRemoved;

    public CooldownCleanupTask(@NotNull UltimateDuels plugin, @NotNull CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.cleanupCount = 0;
        this.totalEntriesRemoved = 0;
    }

    public void run() {
        int removed = this.cooldownManager.cleanupExpired();
        ++this.cleanupCount;
        this.totalEntriesRemoved += removed;
        if (removed > 0) {
            this.plugin.getLogger().fine("Cooldown cleanup: removed " + removed + " expired entries");
        }
    }

    public int getCleanupCount() {
        return this.cleanupCount;
    }

    public int getTotalEntriesRemoved() {
        return this.totalEntriesRemoved;
    }

    public void resetStatistics() {
        this.cleanupCount = 0;
        this.totalEntriesRemoved = 0;
    }

    public double getAverageEntriesRemoved() {
        if (this.cleanupCount == 0) {
            return 0.0;
        }
        return (double)this.totalEntriesRemoved / (double)this.cleanupCount;
    }
}

