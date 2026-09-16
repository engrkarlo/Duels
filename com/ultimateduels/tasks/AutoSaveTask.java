/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.utils.MessageUtils;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final int saveInterval;
    private volatile long lastSaveTime;
    private final AtomicInteger saveCount;
    private final AtomicLong totalSaveTime;
    private final boolean announceSaves;
    private final AtomicBoolean isSaving;
    private final AtomicInteger consecutiveFailures;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    public AutoSaveTask(UltimateDuels plugin) {
        this.plugin = plugin;
        this.saveInterval = plugin.getConfig().getInt("performance.auto-save-interval", plugin.getConfig().getInt("auto-save.interval-minutes", 5));
        this.announceSaves = plugin.getConfig().getBoolean("auto-save.announce-to-admins", false);
        this.lastSaveTime = System.currentTimeMillis();
        this.saveCount = new AtomicInteger(0);
        this.totalSaveTime = new AtomicLong(0L);
        this.isSaving = new AtomicBoolean(false);
        this.consecutiveFailures = new AtomicInteger(0);
    }

    private boolean isVerbose() {
        return this.plugin.getConfig().getBoolean("performance.verbose-logging", false);
    }

    private void verboseLog(String message) {
        if (this.isVerbose()) {
            this.plugin.getLogger().info(message);
        }
    }

    public void start() {
        if (this.saveInterval <= 0) {
            this.plugin.getLogger().info("Auto-save is disabled (interval <= 0)");
            return;
        }
        long intervalTicks = (long)(this.saveInterval * 60) * 20L;
        this.runTaskTimerAsynchronously((Plugin)this.plugin, intervalTicks, intervalTicks);
        this.plugin.getLogger().info("Auto-save task started (interval: " + this.saveInterval + " minutes)");
    }

    public void run() {
        this.performSave();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void performSave() {
        if (!this.isSaving.compareAndSet(false, true)) {
            this.plugin.getLogger().warning("Auto-save already in progress, skipping...");
            return;
        }
        long startTime = System.currentTimeMillis();
        int count = this.saveCount.incrementAndGet();
        this.verboseLog("Starting auto-save #" + count + "...");
        if (!this.checkDatabaseHealth()) {
            this.plugin.getLogger().severe("Database is not healthy, attempting recovery before save...");
            if (!this.attemptDatabaseRecovery()) {
                this.plugin.getLogger().severe("Database recovery failed, skipping auto-save #" + count);
                this.isSaving.set(false);
                int failures = this.consecutiveFailures.incrementAndGet();
                if (failures >= 3) {
                    this.plugin.getLogger().severe("Max consecutive failures reached (" + failures + "), consider restarting the server");
                    if (this.announceSaves) {
                        this.announceToAdmins("&c&lWARNING: Database connection issues! " + failures + " consecutive save failures.");
                    }
                }
                return;
            }
            this.verboseLog("Database recovery successful, continuing with save...");
        }
        int playersSaved = 0;
        int settingsSaved = 0;
        int customKitsSaved = 0;
        try {
            playersSaved = this.savePlayerStats();
            settingsSaved = this.savePlayerSettings();
            this.saveKits();
            this.saveArenas();
            customKitsSaved = this.saveCustomKits();
            this.savePartyData();
            this.saveQueueData();
            long duration = System.currentTimeMillis() - startTime;
            this.lastSaveTime = System.currentTimeMillis();
            this.totalSaveTime.addAndGet(duration);
            this.consecutiveFailures.set(0);
            this.verboseLog(String.format("Auto-save #%d completed in %dms (Players: %d, Settings: %d, Custom Kits: %d)", count, duration, playersSaved, settingsSaved, customKitsSaved));
            if (this.announceSaves) {
                this.announceToAdmins("&aAuto-save completed in " + duration + "ms");
            }
        }
        catch (Exception e) {
            int failures = this.consecutiveFailures.incrementAndGet();
            long duration = System.currentTimeMillis() - startTime;
            this.plugin.getLogger().severe("Auto-save #" + count + " failed after " + duration + "ms: " + e.getMessage());
            e.printStackTrace();
            if (this.announceSaves) {
                this.announceToAdmins("&cAuto-save failed! Check console. (Failure #" + failures + ")");
            }
            if (failures >= 3) {
                this.plugin.getLogger().severe("===========================================");
                this.plugin.getLogger().severe("CRITICAL: " + failures + " consecutive auto-save failures!");
                this.plugin.getLogger().severe("Database may be unavailable or corrupted.");
                this.plugin.getLogger().severe("Consider restarting the server or checking disk space.");
                this.plugin.getLogger().severe("===========================================");
            }
        }
        finally {
            this.isSaving.set(false);
        }
    }

    private boolean checkDatabaseHealth() {
        try {
            if (this.plugin.getDatabaseManager() == null) {
                this.verboseLog("DatabaseManager is null");
                return false;
            }
            if (this.plugin.getDatabaseManager().getConnectionPool() == null) {
                this.verboseLog("ConnectionPool is null");
                return false;
            }
            return this.plugin.getDatabaseManager().getConnectionPool().isAvailable();
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Error checking database health: " + e.getMessage());
            return false;
        }
    }

    private boolean attemptDatabaseRecovery() {
        try {
            if (this.plugin.getDatabaseManager() == null) {
                return false;
            }
            if (this.plugin.getDatabaseManager().getConnectionPool() == null) {
                return false;
            }
            this.verboseLog("Attempting database connection recovery...");
            return this.plugin.getDatabaseManager().getConnectionPool().ensureConnection();
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Database recovery attempt failed: " + e.getMessage());
            return false;
        }
    }

    private int savePlayerStats() {
        int saved = 0;
        int failed = 0;
        if (this.plugin.getStatsManager() == null) {
            return saved;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                this.plugin.getStatsManager().saveStats(player.getUniqueId());
                ++saved;
            }
            catch (Exception e) {
                if (this.isVerbose() || ++failed == 1) {
                    this.plugin.getLogger().warning("Failed to save stats for " + player.getName() + ": " + e.getMessage());
                }
                if (failed != 1 || !this.isVerbose()) continue;
                e.printStackTrace();
            }
        }
        if (failed > 0) {
            this.plugin.getLogger().warning("Failed to save stats for " + failed + " player(s)");
        }
        return saved;
    }

    private int savePlayerSettings() {
        int saved = 0;
        int failed = 0;
        if (this.plugin.getSettingsManager() == null) {
            return saved;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                this.plugin.getSettingsManager().saveSettings(player.getUniqueId());
                ++saved;
            }
            catch (Exception e) {
                if (this.isVerbose() || ++failed == 1) {
                    this.plugin.getLogger().warning("Failed to save settings for " + player.getName() + ": " + e.getMessage());
                }
                if (failed != 1 || !this.isVerbose()) continue;
                e.printStackTrace();
            }
        }
        if (failed > 0) {
            this.plugin.getLogger().warning("Failed to save settings for " + failed + " player(s)");
        }
        return saved;
    }

    private void saveKits() {
        try {
            if (this.plugin.getKitManager() != null) {
                this.plugin.getKitManager().saveAdminKits();
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to save kits: " + e.getMessage());
        }
    }

    private void saveArenas() {
        try {
            if (this.plugin.getArenaManager() != null) {
                this.plugin.getArenaManager().saveDuelArenas();
                this.plugin.getArenaManager().saveFFAArenas();
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to save arenas: " + e.getMessage());
        }
    }

    private int saveCustomKits() {
        int saved = 0;
        int failed = 0;
        if (this.plugin.getKitManager() == null) {
            return saved;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                this.plugin.getKitManager().savePlayerKits(player.getUniqueId());
                ++saved;
            }
            catch (Exception e) {
                if (this.isVerbose() || ++failed == 1) {
                    this.plugin.getLogger().warning("Failed to save custom kits for " + player.getName() + ": " + e.getMessage());
                }
                if (failed != 1 || !this.isVerbose()) continue;
                e.printStackTrace();
            }
        }
        if (failed > 0) {
            this.plugin.getLogger().warning("Failed to save custom kits for " + failed + " player(s)");
        }
        return saved;
    }

    private void savePartyData() {
        try {
            if (this.plugin.getPartyManager() != null) {
                try {
                    Method saveMethod = this.plugin.getPartyManager().getClass().getMethod("save", new Class[0]);
                    saveMethod.invoke((Object)this.plugin.getPartyManager(), new Object[0]);
                }
                catch (NoSuchMethodException saveMethod) {}
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().fine("Party data save skipped: " + e.getMessage());
        }
    }

    private void saveQueueData() {
        try {
            if (this.plugin.getQueueManager() != null) {
                try {
                    Method saveMethod = this.plugin.getQueueManager().getClass().getMethod("save", new Class[0]);
                    saveMethod.invoke((Object)this.plugin.getQueueManager(), new Object[0]);
                }
                catch (NoSuchMethodException saveMethod) {}
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().fine("Queue data save skipped: " + e.getMessage());
        }
    }

    private void announceToAdmins(String message) {
        if (!this.plugin.isEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("ultimateduels.admin")) continue;
                MessageUtils.sendMessage(player, "&8[&eAutoSave&8] " + message);
            }
        });
    }

    public void forceSave() {
        if (!this.plugin.isEnabled()) {
            this.performSave();
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, this::performSave);
    }

    public void forceSaveSync() {
        this.performSave();
    }

    public long getLastSaveTime() {
        return this.lastSaveTime;
    }

    public long getTimeSinceLastSave() {
        return System.currentTimeMillis() - this.lastSaveTime;
    }

    public int getSaveCount() {
        return this.saveCount.get();
    }

    public long getAverageSaveTime() {
        int count = this.saveCount.get();
        if (count == 0) {
            return 0L;
        }
        return this.totalSaveTime.get() / (long)count;
    }

    public int getSaveInterval() {
        return this.saveInterval;
    }

    public boolean isSaving() {
        return this.isSaving.get();
    }

    public int getConsecutiveFailures() {
        return this.consecutiveFailures.get();
    }

    public String getFormattedTimeSinceLastSave() {
        long millis = this.getTimeSinceLastSave();
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return minutes + "m " + seconds + "s ago";
        }
        return seconds + "s ago";
    }

    public String getFormattedNextSaveTime() {
        if (this.saveInterval <= 0) {
            return "Disabled";
        }
        long intervalMillis = (long)(this.saveInterval * 60) * 1000L;
        long nextSave = this.lastSaveTime + intervalMillis;
        long remaining = nextSave - System.currentTimeMillis();
        if (remaining <= 0L) {
            return "Soon";
        }
        long seconds = remaining / 1000L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return "in " + minutes + "m " + seconds + "s";
        }
        return "in " + seconds + "s";
    }

    public void shutdown() {
        try {
            this.cancel();
        }
        catch (IllegalStateException illegalStateException) {
            // empty catch block
        }
        this.plugin.getLogger().info("Performing final save before shutdown...");
        for (int waitCount = 0; this.isSaving.get() && waitCount < 60; ++waitCount) {
            try {
                Thread.sleep(500L);
                continue;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (this.isSaving.get()) {
            this.plugin.getLogger().warning("Previous save still in progress, proceeding with shutdown save anyway...");
        }
        try {
            if (!this.checkDatabaseHealth()) {
                this.plugin.getLogger().warning("Database not healthy during shutdown, attempting recovery...");
                if (!this.attemptDatabaseRecovery()) {
                    this.plugin.getLogger().severe("Could not recover database connection during shutdown!");
                    this.plugin.getLogger().severe("Player data may not have been saved!");
                    return;
                }
            }
            int playersSaved = 0;
            int playersFailed = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                try {
                    if (this.plugin.getStatsManager() != null) {
                        this.plugin.getStatsManager().saveStats(uuid);
                    }
                    if (this.plugin.getSettingsManager() != null) {
                        this.plugin.getSettingsManager().saveSettings(uuid);
                    }
                    if (this.plugin.getKitManager() != null) {
                        this.plugin.getKitManager().savePlayerKits(uuid);
                    }
                    ++playersSaved;
                }
                catch (Exception e) {
                    ++playersFailed;
                    this.plugin.getLogger().warning("Failed to save data for " + player.getName() + ": " + e.getMessage());
                }
            }
            if (this.plugin.getKitManager() != null) {
                try {
                    this.plugin.getKitManager().saveAdminKits();
                    this.plugin.getKitManager().saveAllPlayerKits();
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Failed to save kit configurations: " + e.getMessage());
                }
            }
            this.saveArenas();
            if (this.plugin.getStatsManager() != null) {
                try {
                    this.plugin.getStatsManager().saveAll();
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Failed to save all stats: " + e.getMessage());
                }
            }
            if (this.plugin.getSettingsManager() != null) {
                try {
                    this.plugin.getSettingsManager().saveAll();
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Failed to save all settings: " + e.getMessage());
                }
            }
            if (playersFailed > 0) {
                this.plugin.getLogger().warning("Final save completed with " + playersFailed + " failure(s)");
            } else {
                this.plugin.getLogger().info("Final save completed successfully! (" + playersSaved + " players saved)");
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Error during final save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getStatsString() {
        return String.format("Saves: %d | Last: %s | Next: %s | Interval: %dm | Avg: %dms | Failures: %d", this.getSaveCount(), this.getFormattedTimeSinceLastSave(), this.getFormattedNextSaveTime(), this.saveInterval, this.getAverageSaveTime(), this.getConsecutiveFailures());
    }
}

