/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TaskUtils {
    private static Plugin plugin;
    private static final Map<String, BukkitTask> namedTasks;
    private static final Map<UUID, Map<String, BukkitTask>> playerTasks;

    private TaskUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void init(@NotNull Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    @NotNull
    private static Plugin getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("TaskUtils has not been initialized! Call TaskUtils.init(plugin) first.");
        }
        return plugin;
    }

    public static BukkitTask runSync(@NotNull Runnable task) {
        return Bukkit.getScheduler().runTask(TaskUtils.getPlugin(), task);
    }

    public static BukkitTask runSyncLater(@NotNull Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(TaskUtils.getPlugin(), task, delayTicks);
    }

    public static BukkitTask runSyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(TaskUtils.getPlugin(), task, delayTicks, periodTicks);
    }

    public static BukkitTask runSync(@NotNull BukkitRunnable runnable) {
        return runnable.runTask(TaskUtils.getPlugin());
    }

    public static BukkitTask runSyncLater(@NotNull BukkitRunnable runnable, long delayTicks) {
        return runnable.runTaskLater(TaskUtils.getPlugin(), delayTicks);
    }

    public static BukkitTask runSyncTimer(@NotNull BukkitRunnable runnable, long delayTicks, long periodTicks) {
        return runnable.runTaskTimer(TaskUtils.getPlugin(), delayTicks, periodTicks);
    }

    public static BukkitTask runAsync(@NotNull Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(TaskUtils.getPlugin(), task);
    }

    public static BukkitTask runAsyncLater(@NotNull Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(TaskUtils.getPlugin(), task, delayTicks);
    }

    public static BukkitTask runAsyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(TaskUtils.getPlugin(), task, delayTicks, periodTicks);
    }

    public static BukkitTask runAsync(@NotNull BukkitRunnable runnable) {
        return runnable.runTaskAsynchronously(TaskUtils.getPlugin());
    }

    public static BukkitTask runAsyncLater(@NotNull BukkitRunnable runnable, long delayTicks) {
        return runnable.runTaskLaterAsynchronously(TaskUtils.getPlugin(), delayTicks);
    }

    public static BukkitTask runAsyncTimer(@NotNull BukkitRunnable runnable, long delayTicks, long periodTicks) {
        return runnable.runTaskTimerAsynchronously(TaskUtils.getPlugin(), delayTicks, periodTicks);
    }

    @NotNull
    public static <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        CompletableFuture future = new CompletableFuture();
        TaskUtils.runAsync(() -> {
            try {
                future.complete(supplier.get());
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @NotNull
    public static CompletableFuture<Void> runAsyncFuture(@NotNull Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        TaskUtils.runAsync(() -> {
            try {
                task.run();
                future.complete(null);
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static <T> void asyncThenSync(@NotNull Supplier<T> asyncSupplier, @NotNull Consumer<T> syncConsumer) {
        TaskUtils.runAsync(() -> {
            Object result = asyncSupplier.get();
            TaskUtils.runSync(() -> syncConsumer.accept(result));
        });
    }

    public static void asyncThenSync(@NotNull Runnable asyncTask, @NotNull Runnable syncTask) {
        TaskUtils.runAsync(() -> {
            asyncTask.run();
            TaskUtils.runSync(syncTask);
        });
    }

    @NotNull
    public static Executor getSyncExecutor() {
        return runnable -> TaskUtils.runSync(runnable);
    }

    @NotNull
    public static Executor getAsyncExecutor() {
        return runnable -> TaskUtils.runAsync(runnable);
    }

    public static void runNamedTask(@NotNull String name, @NotNull Runnable task) {
        TaskUtils.cancelNamedTask(name);
        BukkitTask bukkitTask = TaskUtils.runSync(task);
        namedTasks.put(name, bukkitTask);
    }

    public static void runNamedTaskLater(@NotNull String name, @NotNull Runnable task, long delayTicks) {
        TaskUtils.cancelNamedTask(name);
        BukkitTask bukkitTask = TaskUtils.runSyncLater(task, delayTicks);
        namedTasks.put(name, bukkitTask);
    }

    public static void runNamedTaskTimer(@NotNull String name, @NotNull Runnable task, long delayTicks, long periodTicks) {
        TaskUtils.cancelNamedTask(name);
        BukkitTask bukkitTask = TaskUtils.runSyncTimer(task, delayTicks, periodTicks);
        namedTasks.put(name, bukkitTask);
    }

    public static boolean cancelNamedTask(@NotNull String name) {
        BukkitTask task = namedTasks.remove(name);
        if (task != null && !task.isCancelled()) {
            task.cancel();
            return true;
        }
        return false;
    }

    public static boolean isNamedTaskRunning(@NotNull String name) {
        BukkitTask task = namedTasks.get(name);
        return task != null && !task.isCancelled();
    }

    public static void cancelAllNamedTasks() {
        namedTasks.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel();
            }
        });
        namedTasks.clear();
    }

    public static void runPlayerTask(@NotNull UUID playerId, @NotNull String taskName, @NotNull Runnable task) {
        TaskUtils.cancelPlayerTask(playerId, taskName);
        BukkitTask bukkitTask = TaskUtils.runSync(task);
        playerTasks.computeIfAbsent(playerId, k -> new ConcurrentHashMap()).put(taskName, bukkitTask);
    }

    public static void runPlayerTaskLater(@NotNull UUID playerId, @NotNull String taskName, @NotNull Runnable task, long delayTicks) {
        TaskUtils.cancelPlayerTask(playerId, taskName);
        BukkitTask bukkitTask = TaskUtils.runSyncLater(task, delayTicks);
        playerTasks.computeIfAbsent(playerId, k -> new ConcurrentHashMap()).put(taskName, bukkitTask);
    }

    public static void runPlayerTaskTimer(@NotNull UUID playerId, @NotNull String taskName, @NotNull Runnable task, long delayTicks, long periodTicks) {
        TaskUtils.cancelPlayerTask(playerId, taskName);
        BukkitTask bukkitTask = TaskUtils.runSyncTimer(task, delayTicks, periodTicks);
        playerTasks.computeIfAbsent(playerId, k -> new ConcurrentHashMap()).put(taskName, bukkitTask);
    }

    public static boolean cancelPlayerTask(@NotNull UUID playerId, @NotNull String taskName) {
        BukkitTask task;
        Map<String, BukkitTask> tasks = playerTasks.get(playerId);
        if (tasks != null && (task = tasks.remove(taskName)) != null && !task.isCancelled()) {
            task.cancel();
            return true;
        }
        return false;
    }

    public static void cancelAllPlayerTasks(@NotNull UUID playerId) {
        Map<String, BukkitTask> tasks = playerTasks.remove(playerId);
        if (tasks != null) {
            tasks.values().forEach(task -> {
                if (!task.isCancelled()) {
                    task.cancel();
                }
            });
        }
    }

    public static boolean hasPlayerTask(@NotNull UUID playerId, @NotNull String taskName) {
        Map<String, BukkitTask> tasks = playerTasks.get(playerId);
        if (tasks != null) {
            BukkitTask task = tasks.get(taskName);
            return task != null && !task.isCancelled();
        }
        return false;
    }

    public static BukkitTask countdown(int seconds, final @NotNull Consumer<Integer> onTick, final @NotNull Runnable onComplete) {
        final AtomicInteger remaining = new AtomicInteger(seconds);
        return TaskUtils.runSyncTimer(new BukkitRunnable(){

            public void run() {
                int current = remaining.getAndDecrement();
                if (current <= 0) {
                    this.cancel();
                    onComplete.run();
                } else {
                    onTick.accept(current);
                }
            }
        }, 0L, 20L);
    }

    public static BukkitTask countdown(int count, long intervalTicks, final @NotNull Consumer<Integer> onTick, final @NotNull Runnable onComplete) {
        final AtomicInteger remaining = new AtomicInteger(count);
        return TaskUtils.runSyncTimer(new BukkitRunnable(){

            public void run() {
                int current = remaining.getAndDecrement();
                if (current <= 0) {
                    this.cancel();
                    onComplete.run();
                } else {
                    onTick.accept(current);
                }
            }
        }, 0L, intervalTicks);
    }

    public static void namedCountdown(final @NotNull String name, int seconds, final @NotNull Consumer<Integer> onTick, final @NotNull Runnable onComplete) {
        TaskUtils.cancelNamedTask(name);
        final AtomicInteger remaining = new AtomicInteger(seconds);
        BukkitTask task = TaskUtils.runSyncTimer(new BukkitRunnable(){

            public void run() {
                int current = remaining.getAndDecrement();
                if (current <= 0) {
                    this.cancel();
                    namedTasks.remove(name);
                    onComplete.run();
                } else {
                    onTick.accept(current);
                }
            }
        }, 0L, 20L);
        namedTasks.put(name, task);
    }

    public static BukkitTask repeatUntil(final @NotNull Supplier<Boolean> condition, final @NotNull Runnable task, long intervalTicks, final @Nullable Runnable onComplete) {
        return TaskUtils.runSyncTimer(new BukkitRunnable(){

            public void run() {
                if (((Boolean)condition.get()).booleanValue()) {
                    this.cancel();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    task.run();
                }
            }
        }, 0L, intervalTicks);
    }

    public static BukkitTask repeatTimes(final int times, final @NotNull Consumer<Integer> task, long intervalTicks, final @Nullable Runnable onComplete) {
        final AtomicInteger count = new AtomicInteger(0);
        return TaskUtils.runSyncTimer(new BukkitRunnable(){

            public void run() {
                int current = count.incrementAndGet();
                if (current > times) {
                    this.cancel();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    task.accept(current);
                }
            }
        }, 0L, intervalTicks);
    }

    @NotNull
    public static TaskChain chain() {
        return new TaskChain();
    }

    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    public static void ensureSync(@NotNull Runnable task) {
        if (TaskUtils.isMainThread()) {
            task.run();
        } else {
            TaskUtils.runSync(task);
        }
    }

    public static void ensureAsync(@NotNull Runnable task) {
        if (TaskUtils.isMainThread()) {
            TaskUtils.runAsync(task);
        } else {
            task.run();
        }
    }

    public static void cancelTask(@Nullable BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public static void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(TaskUtils.getPlugin());
        namedTasks.clear();
        playerTasks.clear();
    }

    public static int getPendingTaskCount() {
        return Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getOwner().equals((Object)TaskUtils.getPlugin())).mapToInt(task -> 1).sum();
    }

    public static long secondsToTicks(double seconds) {
        return (long)(seconds * 20.0);
    }

    public static double ticksToSeconds(long ticks) {
        return (double)ticks / 20.0;
    }

    public static long minutesToTicks(double minutes) {
        return (long)(minutes * 1200.0);
    }

    static {
        namedTasks = new ConcurrentHashMap<String, BukkitTask>();
        playerTasks = new ConcurrentHashMap<UUID, Map<String, BukkitTask>>();
    }

    public static class TaskChain {
        private long currentDelay = 0L;

        public TaskChain then(@NotNull Runnable task) {
            TaskUtils.runSyncLater(task, this.currentDelay);
            return this;
        }

        public TaskChain delay(long ticks) {
            this.currentDelay += ticks;
            return this;
        }

        public TaskChain delaySeconds(double seconds) {
            this.currentDelay += (long)(seconds * 20.0);
            return this;
        }

        public TaskChain thenAsync(@NotNull Runnable task) {
            TaskUtils.runAsyncLater(task, this.currentDelay);
            return this;
        }
    }
}

