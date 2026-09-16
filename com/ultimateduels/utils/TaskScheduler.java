/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.utils;

import com.ultimateduels.UltimateDuels;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class TaskScheduler {
    private final UltimateDuels plugin;
    private final List<BukkitTask> activeTasks;
    private final ConcurrentLinkedQueue<Runnable> pendingTasks;
    private boolean shuttingDown;

    public TaskScheduler(UltimateDuels plugin) {
        this.plugin = plugin;
        this.activeTasks = new ArrayList<BukkitTask>();
        this.pendingTasks = new ConcurrentLinkedQueue();
        this.shuttingDown = false;
        plugin.getLogger().info("\u00a7a[TaskScheduler] Initialized successfully!");
    }

    @Nonnull
    public BukkitTask runTask(@Nonnull Runnable task) {
        if (this.shuttingDown) {
            this.pendingTasks.add(task);
            return null;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTask((Plugin)this.plugin, task);
        this.activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    @Nonnull
    public BukkitTask runTaskAsync(@Nonnull Runnable task) {
        if (this.shuttingDown) {
            this.pendingTasks.add(task);
            return null;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, task);
        this.activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    @Nonnull
    public BukkitTask runTaskLater(@Nonnull Runnable task, long delay) {
        if (this.shuttingDown) {
            this.pendingTasks.add(task);
            return null;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, task, delay);
        this.activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    @Nonnull
    public BukkitTask runTaskLaterAsync(@Nonnull Runnable task, long delay) {
        if (this.shuttingDown) {
            this.pendingTasks.add(task);
            return null;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)this.plugin, task, delay);
        this.activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    @Nonnull
    public BukkitTask runTaskTimer(@Nonnull Runnable task, long delay, long period) {
        if (this.shuttingDown) {
            return null;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, task, delay, period);
        this.activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    @Nonnull
    public BukkitTask runTaskTimerAsync(@Nonnull Runnable task, long delay, long period) {
        if (this.shuttingDown) {
            return null;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, task, delay, period);
        this.activeTasks.add(bukkitTask);
        return bukkitTask;
    }

    public void cancelTask(@Nonnull BukkitTask task) {
        if (!task.isCancelled()) {
            task.cancel();
        }
        this.activeTasks.remove(task);
    }

    public void cancelAllTasks() {
        for (BukkitTask task : new ArrayList<BukkitTask>(this.activeTasks)) {
            if (task.isCancelled()) continue;
            task.cancel();
        }
        this.activeTasks.clear();
    }

    public int getActiveTaskCount() {
        this.activeTasks.removeIf(BukkitTask::isCancelled);
        return this.activeTasks.size();
    }

    @NotNull
    public Executor getAsyncExecutor() {
        return task -> Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, task);
    }

    public void shutdown() {
        this.shuttingDown = true;
        this.plugin.getLogger().info("Shutting down TaskScheduler...");
        this.cancelAllTasks();
        while (!this.pendingTasks.isEmpty()) {
            Runnable task = this.pendingTasks.poll();
            if (task == null) continue;
            try {
                task.run();
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to run pending task: " + e.getMessage());
            }
        }
        this.plugin.getLogger().info("TaskScheduler shutdown complete.");
    }

    public boolean isShuttingDown() {
        return this.shuttingDown;
    }
}

