/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Location
 */
package com.ultimateduels.schematic;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.DuelArena;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;

public class SchematicManager {
    private final UltimateDuels plugin;
    private final File schematicsFolder;
    private final Map<String, SchematicData> loadedSchematics;
    private final Map<UUID, Long> pendingOperations;

    public SchematicManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics/arenas");
        this.loadedSchematics = new ConcurrentHashMap<String, SchematicData>();
        this.pendingOperations = new ConcurrentHashMap<UUID, Long>();
        if (!this.schematicsFolder.exists()) {
            this.schematicsFolder.mkdirs();
        }
        plugin.getLogger().info("\u00a7a[SchematicManager] Initialized successfully!");
    }

    public CompletableFuture<Boolean> saveArenaSchematic(@Nonnull DuelArena arena) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String arenaName = arena.getName();
                File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
                if (!this.plugin.isWorldEditEnabled()) {
                    this.plugin.getLogger().warning("WorldEdit not available for schematic operations!");
                    return false;
                }
                Location pos1 = arena.getCorner1();
                Location pos2 = arena.getCorner2();
                if (pos1 == null || pos2 == null) {
                    this.plugin.getLogger().warning("Arena " + arenaName + " has no defined corners!");
                    return false;
                }
                boolean success = this.saveSchematicWorldEdit(pos1, pos2, schematicFile);
                if (success) {
                    SchematicData data = new SchematicData(arenaName, schematicFile, pos1, pos2);
                    this.loadedSchematics.put(arenaName, data);
                    this.plugin.getLogger().info("Saved schematic for arena: " + arenaName);
                }
                return success;
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save arena schematic!", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> restoreArenaSchematic(@Nonnull DuelArena arena) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String arenaName = arena.getName();
                File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
                if (!schematicFile.exists()) {
                    this.plugin.getLogger().warning("No schematic found for arena: " + arenaName);
                    return false;
                }
                if (!this.plugin.isWorldEditEnabled()) {
                    this.plugin.getLogger().warning("WorldEdit not available for schematic operations!");
                    return false;
                }
                Location pasteLocation = arena.getCorner1();
                if (pasteLocation == null) {
                    this.plugin.getLogger().warning("Arena " + arenaName + " has no paste location!");
                    return false;
                }
                boolean success = this.pasteSchematicWorldEdit(schematicFile, pasteLocation);
                if (success) {
                    this.plugin.getLogger().info("Restored schematic for arena: " + arenaName);
                }
                return success;
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to restore arena schematic!", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> restoreArena(@Nonnull String arenaName) {
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            this.plugin.getLogger().warning("Cannot restore arena - ArenaManager not available");
            return CompletableFuture.completedFuture(false);
        }
        DuelArena arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            return this.restoreArenaByName(arenaName);
        }
        return this.restoreArenaSchematic(arena);
    }

    public CompletableFuture<Boolean> restoreArenaByName(@Nonnull String arenaName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
                if (!schematicFile.exists()) {
                    this.plugin.getLogger().warning("No schematic found for arena: " + arenaName);
                    return false;
                }
                if (!this.plugin.isWorldEditEnabled()) {
                    this.plugin.getLogger().warning("WorldEdit not available for schematic operations!");
                    return false;
                }
                SchematicData cachedData = this.loadedSchematics.get(arenaName);
                Location pasteLocation = null;
                if (cachedData != null) {
                    pasteLocation = cachedData.getCorner1();
                } else {
                    DuelArena arena;
                    ArenaManager arenaManager = this.plugin.getArenaManager();
                    if (arenaManager != null && (arena = arenaManager.getArena(arenaName)) != null) {
                        pasteLocation = arena.getCorner1();
                    }
                }
                if (pasteLocation == null) {
                    this.plugin.getLogger().warning("Cannot restore arena " + arenaName + " - no paste location available!");
                    return false;
                }
                boolean success = this.pasteSchematicWorldEdit(schematicFile, pasteLocation);
                if (success) {
                    this.plugin.getLogger().info("Restored schematic for arena: " + arenaName);
                }
                return success;
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to restore arena schematic!", e);
                return false;
            }
        });
    }

    public boolean restoreArenaSync(@Nonnull DuelArena arena) {
        try {
            if (!this.plugin.isWorldEditEnabled()) {
                return false;
            }
            String arenaName = arena.getName();
            File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
            if (!schematicFile.exists()) {
                return false;
            }
            Location pasteLocation = arena.getCorner1();
            if (pasteLocation == null) {
                return false;
            }
            return this.pasteSchematicWorldEdit(schematicFile, pasteLocation);
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to restore arena schematic sync!", e);
            return false;
        }
    }

    public boolean restoreArenaSync(@Nonnull String arenaName) {
        if (!this.plugin.isWorldEditEnabled()) {
            this.plugin.getLogger().warning("Cannot restore arena sync - WorldEdit not available");
            return false;
        }
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            this.plugin.getLogger().warning("Cannot restore arena sync - ArenaManager not available");
            return false;
        }
        DuelArena arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            return this.restoreArenaSync(arena);
        }
        SchematicData cachedData = this.loadedSchematics.get(arenaName);
        if (cachedData != null && cachedData.getCorner1() != null) {
            try {
                File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
                if (!schematicFile.exists()) {
                    return false;
                }
                return this.pasteSchematicWorldEdit(schematicFile, cachedData.getCorner1());
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to restore arena schematic sync!", e);
                return false;
            }
        }
        return false;
    }

    public void restoreAllArenasSync() {
        Collection<DuelArena> ffaArenas;
        this.plugin.getLogger().info("Restoring all arena schematics...");
        if (!this.plugin.isWorldEditEnabled()) {
            this.plugin.getLogger().warning("WorldEdit not available - skipping schematic restoration");
            return;
        }
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            this.plugin.getLogger().warning("ArenaManager is null - skipping schematic restoration");
            return;
        }
        int restoredCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        Collection<DuelArena> duelArenas = arenaManager.getAllDuelArenas();
        if (duelArenas != null && !duelArenas.isEmpty()) {
            for (DuelArena arena : duelArenas) {
                if (arena == null) {
                    ++skippedCount;
                    continue;
                }
                try {
                    if (this.arenaHasSchematic(arena)) {
                        if (this.restoreArenaSync(arena)) {
                            ++restoredCount;
                            this.plugin.debug("Restored schematic for duel arena: " + arena.getName());
                            continue;
                        }
                        ++failedCount;
                        this.plugin.getLogger().warning("Failed to restore duel arena: " + arena.getName());
                        continue;
                    }
                    ++skippedCount;
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Error restoring arena " + arena.getName() + ": " + e.getMessage());
                    ++failedCount;
                }
            }
        }
        if ((ffaArenas = arenaManager.getAllFFAArenas()) != null && !ffaArenas.isEmpty()) {
            for (DuelArena arena : ffaArenas) {
                if (arena == null) {
                    ++skippedCount;
                    continue;
                }
                try {
                    if (this.arenaHasSchematic(arena)) {
                        if (this.restoreArenaSync(arena)) {
                            ++restoredCount;
                            this.plugin.debug("Restored schematic for FFA arena: " + arena.getName());
                            continue;
                        }
                        ++failedCount;
                        this.plugin.getLogger().warning("Failed to restore FFA arena: " + arena.getName());
                        continue;
                    }
                    ++skippedCount;
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Error restoring FFA arena " + arena.getName() + ": " + e.getMessage());
                    ++failedCount;
                }
            }
        }
        this.plugin.getLogger().info("Arena schematic restoration complete:");
        this.plugin.getLogger().info("  Restored: " + restoredCount);
        this.plugin.getLogger().info("  Failed: " + failedCount);
        this.plugin.getLogger().info("  Skipped (no schematic): " + skippedCount);
    }

    public boolean arenaHasSchematic(@Nullable DuelArena arena) {
        if (arena == null) {
            return false;
        }
        String schematicName = arena.getSchematicName();
        if (schematicName == null || schematicName.isEmpty()) {
            return false;
        }
        return this.hasSchematic(arena.getName());
    }

    public boolean hasSchematic(@Nonnull String arenaName) {
        File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
        return schematicFile.exists();
    }

    public boolean deleteSchematic(@Nonnull String arenaName) {
        File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
        this.loadedSchematics.remove(arenaName);
        if (schematicFile.exists()) {
            try {
                return schematicFile.delete();
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to delete schematic file: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Nullable
    public File getSchematicFile(@Nonnull String arenaName) {
        File schematicFile = new File(this.schematicsFolder, arenaName + ".schem");
        return schematicFile.exists() ? schematicFile : null;
    }

    @Nonnull
    public File[] getAllSchematics() {
        File[] files = this.schematicsFolder.listFiles((dir, name) -> name.endsWith(".schem"));
        return files != null ? files : new File[]{};
    }

    @Nonnull
    public File getSchematicsFolder() {
        return this.schematicsFolder;
    }

    @Nullable
    public SchematicData getCachedSchematic(@Nonnull String arenaName) {
        return this.loadedSchematics.get(arenaName);
    }

    public void clearCache() {
        this.loadedSchematics.clear();
    }

    private boolean saveSchematicWorldEdit(Location pos1, Location pos2, File file) {
        try {
            if (!this.plugin.isWorldEditEnabled()) {
                this.plugin.getLogger().warning("WorldEdit not available for saving schematic");
                return false;
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            this.plugin.debug("Schematic saved (placeholder): " + file.getName());
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "WorldEdit schematic save failed", e);
            return false;
        }
    }

    private boolean pasteSchematicWorldEdit(File file, Location location) {
        try {
            if (!this.plugin.isWorldEditEnabled()) {
                this.plugin.getLogger().warning("WorldEdit not available for pasting schematic");
                return false;
            }
            this.plugin.debug("Schematic pasted (placeholder): " + file.getName());
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "WorldEdit schematic paste failed", e);
            return false;
        }
    }

    public void shutdown() {
        this.loadedSchematics.clear();
        this.pendingOperations.clear();
        this.plugin.getLogger().info("\u00a7a[SchematicManager] Shutdown complete!");
    }

    public static class SchematicData {
        private final String arenaName;
        private final File file;
        private final Location corner1;
        private final Location corner2;
        private final long savedTime;

        public SchematicData(String arenaName, File file, Location corner1, Location corner2) {
            this.arenaName = arenaName;
            this.file = file;
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.savedTime = System.currentTimeMillis();
        }

        public String getArenaName() {
            return this.arenaName;
        }

        public File getFile() {
            return this.file;
        }

        public Location getCorner1() {
            return this.corner1;
        }

        public Location getCorner2() {
            return this.corner2;
        }

        public long getSavedTime() {
            return this.savedTime;
        }
    }
}

