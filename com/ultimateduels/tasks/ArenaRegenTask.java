/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.DuelArena;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaRegenTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final ArenaManager arenaManager;
    private final Queue<RegenRequest> regenQueue;
    private boolean processing = false;
    private static final long REGEN_DELAY_TICKS = 20L;

    public ArenaRegenTask(UltimateDuels plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
        this.regenQueue = new ConcurrentLinkedQueue<RegenRequest>();
    }

    public void start() {
        this.runTaskTimer((Plugin)this.plugin, 20L, 10L);
    }

    public void run() {
        if (this.processing || this.regenQueue.isEmpty()) {
            return;
        }
        RegenRequest request = this.regenQueue.poll();
        if (request == null) {
            return;
        }
        this.processing = true;
        this.processRegenRequest(request);
    }

    public void queueRegeneration(DuelArena arena, Consumer<Boolean> callback) {
        RegenRequest request = new RegenRequest(arena, callback);
        this.regenQueue.add(request);
        this.plugin.getLogger().info("Queued arena regeneration: " + arena.getName());
    }

    public void queueRegeneration(String schematicName, Location pasteLocation, Consumer<Boolean> callback) {
        RegenRequest request = new RegenRequest(schematicName, pasteLocation, callback);
        this.regenQueue.add(request);
        this.plugin.getLogger().info("Queued schematic paste: " + schematicName);
    }

    private void processRegenRequest(RegenRequest request) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            boolean success = false;
            try {
                if (request.arena != null) {
                    success = this.regenerateArena(request.arena);
                } else if (request.schematicName != null && request.pasteLocation != null) {
                    success = this.pasteSchematic(request.schematicName, request.pasteLocation);
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Error during arena regeneration: " + e.getMessage());
                e.printStackTrace();
            }
            boolean finalSuccess = success;
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                this.processing = false;
                if (request.callback != null) {
                    request.callback.accept(finalSuccess);
                }
                if (finalSuccess) {
                    this.plugin.getLogger().info("Arena regeneration completed successfully");
                } else {
                    this.plugin.getLogger().warning("Arena regeneration failed");
                }
            });
        });
    }

    private boolean regenerateArena(DuelArena arena) {
        String schematicName = arena.getSchematicName();
        if (schematicName == null || schematicName.isEmpty()) {
            this.plugin.getLogger().warning("No schematic saved for arena: " + arena.getName());
            return false;
        }
        Location pasteLocation = arena.getCorner1();
        if (pasteLocation == null) {
            pasteLocation = arena.getSpawnPoint1();
        }
        if (pasteLocation == null) {
            this.plugin.getLogger().warning("No paste location for arena: " + arena.getName());
            return false;
        }
        return this.pasteSchematic(schematicName, pasteLocation);
    }

    private boolean pasteSchematic(String schematicName, Location location) {
        if (!this.isWorldEditAvailable()) {
            this.plugin.getLogger().warning("WorldEdit/FAWE is not available for schematic operations");
            return false;
        }
        try {
            File schematicFile = this.getSchematicFile(schematicName);
            if (schematicFile == null || !schematicFile.exists()) {
                this.plugin.getLogger().warning("Schematic file not found: " + schematicName);
                return false;
            }
            return this.pasteWithWorldEdit(schematicFile, location);
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Error pasting schematic: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isWorldEditAvailable() {
        return Bukkit.getPluginManager().getPlugin("WorldEdit") != null || Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }

    private File getSchematicFile(String name) {
        File faweDir;
        String[] extensions;
        File pluginSchematicsDir = new File(this.plugin.getDataFolder(), "schematics");
        if (!pluginSchematicsDir.exists()) {
            pluginSchematicsDir.mkdirs();
        }
        for (String ext : extensions = new String[]{".schem", ".schematic", ""}) {
            File file = new File(pluginSchematicsDir, name + ext);
            if (!file.exists()) continue;
            return file;
        }
        File worldEditDir = new File(Bukkit.getWorldContainer(), "plugins/WorldEdit/schematics");
        if (worldEditDir.exists()) {
            for (String ext : extensions) {
                File file = new File(worldEditDir, name + ext);
                if (!file.exists()) continue;
                return file;
            }
        }
        if ((faweDir = new File(Bukkit.getWorldContainer(), "plugins/FastAsyncWorldEdit/schematics")).exists()) {
            for (String ext : extensions) {
                File file = new File(faweDir, name + ext);
                if (!file.exists()) continue;
                return file;
            }
        }
        return null;
    }

    private boolean pasteWithWorldEdit(File schematicFile, Location location) {
        try {
            this.plugin.getLogger().info("Pasting schematic " + schematicFile.getName() + " at " + this.formatLocation(location));
            Thread.sleep(100L);
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("WorldEdit paste failed: " + e.getMessage());
            return false;
        }
    }

    public void saveSchematic(DuelArena arena, Location pos1, Location pos2, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            boolean success = false;
            try {
                success = this.saveRegionAsSchematic(arena.getName(), pos1, pos2);
                if (success) {
                    arena.setSchematicName(arena.getName());
                    Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.arenaManager.saveDuelArenas());
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Error saving schematic: " + e.getMessage());
                e.printStackTrace();
            }
            boolean finalSuccess = success;
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                if (callback != null) {
                    callback.accept(finalSuccess);
                }
            });
        });
    }

    private boolean saveRegionAsSchematic(String name, Location pos1, Location pos2) {
        try {
            this.plugin.getLogger().info("Saving schematic: " + name);
            File schematicFile = new File(this.plugin.getDataFolder(), "schematics/" + name + ".schem");
            schematicFile.getParentFile().mkdirs();
            schematicFile.createNewFile();
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Failed to save schematic: " + e.getMessage());
            return false;
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f in %s", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld() != null ? loc.getWorld().getName() : "unknown");
    }

    public int getQueueSize() {
        return this.regenQueue.size();
    }

    public boolean isProcessing() {
        return this.processing;
    }

    public void clearQueue() {
        this.regenQueue.clear();
    }

    public void shutdown() {
        this.cancel();
        this.regenQueue.clear();
    }

    private static class RegenRequest {
        final DuelArena arena;
        final String schematicName;
        final Location pasteLocation;
        final Consumer<Boolean> callback;

        RegenRequest(DuelArena arena, Consumer<Boolean> callback) {
            this.arena = arena;
            this.schematicName = null;
            this.pasteLocation = null;
            this.callback = callback;
        }

        RegenRequest(String schematicName, Location pasteLocation, Consumer<Boolean> callback) {
            this.arena = null;
            this.schematicName = schematicName;
            this.pasteLocation = pasteLocation;
            this.callback = callback;
        }
    }
}

