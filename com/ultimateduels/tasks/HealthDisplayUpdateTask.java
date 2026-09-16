/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.tasks;

import com.ultimateduels.visuals.HealthDisplayManager;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthDisplayUpdateTask
extends BukkitRunnable {
    private final HealthDisplayManager displayManager;

    public HealthDisplayUpdateTask(HealthDisplayManager displayManager) {
        this.displayManager = displayManager;
    }

    public void run() {
        try {
            this.displayManager.updateAllPositions();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

