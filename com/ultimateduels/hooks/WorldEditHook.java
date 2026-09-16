/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sk89q.worldedit.EditSession
 *  com.sk89q.worldedit.WorldEdit
 *  com.sk89q.worldedit.WorldEditException
 *  com.sk89q.worldedit.bukkit.BukkitAdapter
 *  com.sk89q.worldedit.extent.Extent
 *  com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
 *  com.sk89q.worldedit.extent.clipboard.Clipboard
 *  com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
 *  com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
 *  com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
 *  com.sk89q.worldedit.extent.clipboard.io.ClipboardReader
 *  com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter
 *  com.sk89q.worldedit.function.operation.ForwardExtentCopy
 *  com.sk89q.worldedit.function.operation.Operation
 *  com.sk89q.worldedit.function.operation.Operations
 *  com.sk89q.worldedit.math.BlockVector3
 *  com.sk89q.worldedit.regions.CuboidRegion
 *  com.sk89q.worldedit.regions.Region
 *  com.sk89q.worldedit.session.ClipboardHolder
 *  com.sk89q.worldedit.world.World
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.hooks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.ultimateduels.UltimateDuels;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldEditHook {
    private static final String SCHEM_EXTENSION = ".schem";
    private static final String LEGACY_EXTENSION = ".schematic";
    private static final String SCHEMATICS_FOLDER_NAME = "schematics";
    private static final String FAWE_PLUGIN_NAME = "FastAsyncWorldEdit";
    private static final String WORLDEDIT_PLUGIN_NAME = "WorldEdit";
    private final UltimateDuels plugin;
    private final File schematicsFolder;
    private final ConcurrentHashMap<String, Clipboard> clipboardCache;
    private boolean faweEnabled;
    private boolean worldEditEnabled;
    private ClipboardFormat schematicFormat;
    private String activePluginName;
    private String activePluginVersion;

    public WorldEditHook(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), SCHEMATICS_FOLDER_NAME);
        this.clipboardCache = new ConcurrentHashMap();
        this.initializeFolder();
        this.detectDependencies();
        if (this.worldEditEnabled) {
            this.initializeSchematicFormat();
        }
    }

    private void initializeFolder() {
        if (!this.schematicsFolder.exists() && !this.schematicsFolder.mkdirs()) {
            this.plugin.getLogger().warning("[WorldEditHook] Failed to create schematics folder!");
        }
    }

    private void detectDependencies() {
        Plugin fawePlugin = Bukkit.getPluginManager().getPlugin(FAWE_PLUGIN_NAME);
        if (fawePlugin != null && fawePlugin.isEnabled()) {
            this.faweEnabled = true;
            this.worldEditEnabled = true;
            this.activePluginName = FAWE_PLUGIN_NAME;
            this.activePluginVersion = fawePlugin.getDescription().getVersion();
            this.plugin.getLogger().info("\u00a7a[WorldEditHook] \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            this.plugin.getLogger().info("\u00a7a[WorldEditHook] FastAsyncWorldEdit v" + this.activePluginVersion + " detected!");
            this.plugin.getLogger().info("\u00a7a[WorldEditHook] Async schematic operations: \u00a72ENABLED");
            this.plugin.getLogger().info("\u00a7a[WorldEditHook] \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            return;
        }
        Plugin wePlugin = Bukkit.getPluginManager().getPlugin(WORLDEDIT_PLUGIN_NAME);
        if (wePlugin != null && wePlugin.isEnabled()) {
            this.faweEnabled = false;
            this.worldEditEnabled = true;
            this.activePluginName = WORLDEDIT_PLUGIN_NAME;
            this.activePluginVersion = wePlugin.getDescription().getVersion();
            this.plugin.getLogger().info("\u00a7e[WorldEditHook] \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            this.plugin.getLogger().info("\u00a7e[WorldEditHook] WorldEdit v" + this.activePluginVersion + " detected");
            this.plugin.getLogger().info("\u00a7e[WorldEditHook] Using synchronous operations (may cause lag)");
            this.plugin.getLogger().info("\u00a7e[WorldEditHook] \u00a77TIP: Install FastAsyncWorldEdit for better performance!");
            this.plugin.getLogger().info("\u00a7e[WorldEditHook] \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            return;
        }
        this.faweEnabled = false;
        this.worldEditEnabled = false;
        this.activePluginName = null;
        this.activePluginVersion = null;
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] No WorldEdit/FAWE found!");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] Schematic features: \u00a74DISABLED");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] Arena regeneration will NOT work!");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] ");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] Please install one of:");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook]  - FastAsyncWorldEdit (recommended)");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook]  - WorldEdit");
        this.plugin.getLogger().warning("\u00a7c[WorldEditHook] \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
    }

    private void initializeSchematicFormat() {
        if (!this.worldEditEnabled) {
            return;
        }
        String[] formatNames = new String[]{"FAST_SCHEMATIC", "SPONGE_V3_SCHEMATIC", "SPONGE_SCHEMATIC", "MCEDIT_SCHEMATIC"};
        for (String formatName : formatNames) {
            try {
                this.schematicFormat = BuiltInClipboardFormat.valueOf((String)formatName);
                this.plugin.getLogger().info("[WorldEditHook] Using schematic format: " + formatName);
                return;
            }
            catch (IllegalArgumentException illegalArgumentException) {
            }
        }
        this.schematicFormat = ClipboardFormats.findByAlias((String)"schem");
        if (this.schematicFormat != null) {
            this.plugin.getLogger().info("[WorldEditHook] Using schematic format by alias: schem");
            return;
        }
        for (ClipboardFormat format : ClipboardFormats.getAll()) {
            if (!format.getPrimaryFileExtension().equals("schem")) continue;
            this.schematicFormat = format;
            this.plugin.getLogger().info("[WorldEditHook] Using schematic format: " + format.getName());
            return;
        }
        this.plugin.getLogger().warning("[WorldEditHook] Could not find suitable schematic format!");
    }

    @Nullable
    private ClipboardFormat getSchematicFormat() {
        if (this.schematicFormat != null) {
            return this.schematicFormat;
        }
        ClipboardFormat format = ClipboardFormats.findByAlias((String)"schem");
        if (format != null) {
            return format;
        }
        format = ClipboardFormats.findByAlias((String)"sponge");
        if (format != null) {
            return format;
        }
        Iterator iterator = ClipboardFormats.getAll().iterator();
        if (iterator.hasNext()) {
            ClipboardFormat f = (ClipboardFormat)iterator.next();
            return f;
        }
        return null;
    }

    public boolean isAvailable() {
        return this.worldEditEnabled;
    }

    public boolean isFAWEAvailable() {
        return this.faweEnabled;
    }

    @Nullable
    public String getActivePluginName() {
        return this.activePluginName;
    }

    @Nullable
    public String getActivePluginVersion() {
        return this.activePluginVersion;
    }

    private boolean ensureAvailable(@NotNull String operation) {
        if (!this.worldEditEnabled) {
            this.plugin.getLogger().warning("[WorldEditHook] WorldEdit/FAWE not available for: " + operation);
            this.plugin.getLogger().warning("[WorldEditHook] Please install FastAsyncWorldEdit or WorldEdit!");
            return false;
        }
        return true;
    }

    @NotNull
    public CompletableFuture<Boolean> saveSchematic(@NotNull Location corner1, @NotNull Location corner2, @NotNull String name) {
        if (!this.ensureAvailable("saveSchematic: " + name)) {
            return CompletableFuture.completedFuture(false);
        }
        if (corner1.getWorld() == null || corner2.getWorld() == null) {
            this.plugin.getLogger().warning("[WorldEditHook] Invalid locations for schematic: " + name);
            return CompletableFuture.completedFuture(false);
        }
        if (!corner1.getWorld().equals((Object)corner2.getWorld())) {
            this.plugin.getLogger().warning("[WorldEditHook] Corners must be in the same world: " + name);
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        Runnable saveTask = () -> {
            try {
                boolean result = this.performSchematicSave(corner1, corner2, name);
                future.complete(result);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "[WorldEditHook] Failed to save schematic: " + name, e);
                future.complete(false);
            }
        };
        this.executeTask(saveTask, future);
        return future;
    }

    private boolean performSchematicSave(@NotNull Location corner1, @NotNull Location corner2, @NotNull String name) throws WorldEditException, IOException {
        World world = BukkitAdapter.adapt((org.bukkit.World)corner1.getWorld());
        BlockVector3 min = BlockVector3.at((int)Math.min(corner1.getBlockX(), corner2.getBlockX()), (int)Math.min(corner1.getBlockY(), corner2.getBlockY()), (int)Math.min(corner1.getBlockZ(), corner2.getBlockZ()));
        BlockVector3 max = BlockVector3.at((int)Math.max(corner1.getBlockX(), corner2.getBlockX()), (int)Math.max(corner1.getBlockY(), corner2.getBlockY()), (int)Math.max(corner1.getBlockZ(), corner2.getBlockZ()));
        CuboidRegion region = new CuboidRegion(world, min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard((Region)region);
        clipboard.setOrigin(min);
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build();){
            ForwardExtentCopy copy = new ForwardExtentCopy((Extent)editSession, (Region)region, (Extent)clipboard, min);
            copy.setCopyingEntities(false);
            copy.setCopyingBiomes(false);
            Operations.complete((Operation)copy);
        }
        ClipboardFormat format = this.getSchematicFormat();
        if (format == null) {
            this.plugin.getLogger().severe("[WorldEditHook] No schematic format available!");
            return false;
        }
        File schematicFile = new File(this.schematicsFolder, name + SCHEM_EXTENSION);
        try (ClipboardWriter writer = format.getWriter((OutputStream)new FileOutputStream(schematicFile));){
            writer.write((Clipboard)clipboard);
        }
        this.clipboardCache.put(name.toLowerCase(), (Clipboard)clipboard);
        String asyncIndicator = this.faweEnabled ? " \u00a77(async)" : " \u00a77(sync)";
        this.plugin.getLogger().info(String.format("\u00a7a[WorldEditHook] Saved schematic: %s (%,d blocks, %.2f KB)%s", name, region.getVolume(), (double)schematicFile.length() / 1024.0, asyncIndicator));
        return true;
    }

    @Nullable
    public Clipboard loadSchematic(@NotNull String name) {
        Clipboard clipboard;
        block11: {
            String cacheKey = name.toLowerCase();
            Clipboard cached = this.clipboardCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            File schematicFile = this.resolveSchematicFile(name);
            if (schematicFile == null || !schematicFile.exists()) {
                this.plugin.getLogger().warning("[WorldEditHook] Schematic file not found: " + name);
                return null;
            }
            ClipboardFormat format = ClipboardFormats.findByFile((File)schematicFile);
            if (format == null) {
                this.plugin.getLogger().warning("[WorldEditHook] Unknown schematic format: " + name);
                return null;
            }
            ClipboardReader reader = format.getReader((InputStream)new FileInputStream(schematicFile));
            try {
                Clipboard clipboard2 = reader.read();
                this.clipboardCache.put(cacheKey, clipboard2);
                this.plugin.getLogger().fine("[WorldEditHook] Loaded schematic: " + name);
                clipboard = clipboard2;
                if (reader == null) break block11;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    this.plugin.getLogger().log(Level.WARNING, "[WorldEditHook] Failed to load schematic: " + name, e);
                    return null;
                }
            }
            reader.close();
        }
        return clipboard;
    }

    @NotNull
    public CompletableFuture<Clipboard> loadSchematicAsync(@NotNull String name) {
        CompletableFuture<Clipboard> future = new CompletableFuture<Clipboard>();
        if (this.faweEnabled) {
            CompletableFuture.runAsync(() -> future.complete(this.loadSchematic(name)));
        } else {
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> future.complete(this.loadSchematic(name)));
        }
        return future;
    }

    @NotNull
    public CompletableFuture<Boolean> pasteSchematic(@NotNull String name, @NotNull Location location) {
        return this.pasteSchematic(name, location, false, false);
    }

    @NotNull
    public CompletableFuture<Boolean> pasteSchematic(@NotNull String name, @NotNull Location location, boolean ignoreAir, boolean copyEntities) {
        if (!this.ensureAvailable("pasteSchematic: " + name)) {
            return CompletableFuture.completedFuture(false);
        }
        if (location.getWorld() == null) {
            this.plugin.getLogger().warning("[WorldEditHook] Invalid paste location for: " + name);
            return CompletableFuture.completedFuture(false);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        Runnable pasteTask = () -> {
            try {
                boolean result = this.performSchematicPaste(name, location, ignoreAir, copyEntities);
                future.complete(result);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "[WorldEditHook] Failed to paste schematic: " + name, e);
                future.complete(false);
            }
        };
        this.executeTask(pasteTask, future);
        return future;
    }

    private boolean performSchematicPaste(@NotNull String name, @NotNull Location location, boolean ignoreAir, boolean copyEntities) throws WorldEditException {
        Clipboard clipboard = this.loadSchematic(name);
        if (clipboard == null) {
            return false;
        }
        World world = BukkitAdapter.adapt((org.bukkit.World)location.getWorld());
        BlockVector3 pasteLocation = BlockVector3.at((int)location.getBlockX(), (int)location.getBlockY(), (int)location.getBlockZ());
        try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).maxBlocks(-1).build();){
            Operation operation = new ClipboardHolder(clipboard).createPaste((Extent)editSession).to(pasteLocation).ignoreAirBlocks(ignoreAir).copyEntities(copyEntities).copyBiomes(false).build();
            Operations.complete((Operation)operation);
            if (this.faweEnabled) {
                this.tryFlushEditSession(editSession);
            }
        }
        String asyncIndicator = this.faweEnabled ? " \u00a77(async)" : " \u00a77(sync)";
        this.plugin.getLogger().info(String.format("\u00a7a[WorldEditHook] Pasted schematic: %s at [%d, %d, %d]%s", name, location.getBlockX(), location.getBlockY(), location.getBlockZ(), asyncIndicator));
        return true;
    }

    private void tryFlushEditSession(EditSession editSession) {
        try {
            Method flushQueue = editSession.getClass().getMethod("flushQueue", new Class[0]);
            flushQueue.invoke((Object)editSession, new Object[0]);
        }
        catch (NoSuchMethodException flushQueue) {
        }
        catch (Exception e) {
            this.plugin.getLogger().fine("[WorldEditHook] Could not flush edit session: " + e.getMessage());
        }
    }

    public boolean deleteSchematic(@NotNull String name) {
        this.clipboardCache.remove(name.toLowerCase());
        File schematicFile = this.resolveSchematicFile(name);
        if (schematicFile != null && schematicFile.exists()) {
            try {
                Files.delete(schematicFile.toPath());
                this.plugin.getLogger().info("\u00a7c[WorldEditHook] Deleted schematic: " + name);
                return true;
            }
            catch (IOException e) {
                this.plugin.getLogger().warning("[WorldEditHook] Failed to delete schematic: " + name);
                return false;
            }
        }
        return false;
    }

    public boolean schematicExists(@NotNull String name) {
        File file = this.resolveSchematicFile(name);
        return file != null && file.exists();
    }

    public long getSchematicSize(@NotNull String name) {
        File file = this.resolveSchematicFile(name);
        return file != null && file.exists() ? file.length() : 0L;
    }

    @NotNull
    public String[] getSchematicNames() {
        File[] files = this.schematicsFolder.listFiles((dir, fileName) -> fileName.endsWith(SCHEM_EXTENSION) || fileName.endsWith(LEGACY_EXTENSION));
        if (files == null) {
            return new String[0];
        }
        return (String[])Arrays.stream(files).map(f -> f.getName().replaceAll("\\.(schem|schematic)$", "")).sorted().toArray(String[]::new);
    }

    public boolean copySchematic(@NotNull String source, @NotNull String destination) {
        File sourceFile = this.resolveSchematicFile(source);
        if (sourceFile == null || !sourceFile.exists()) {
            this.plugin.getLogger().warning("[WorldEditHook] Source schematic not found: " + source);
            return false;
        }
        File destFile = new File(this.schematicsFolder, destination + SCHEM_EXTENSION);
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.plugin.getLogger().info("[WorldEditHook] Copied schematic: " + source + " -> " + destination);
            return true;
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("[WorldEditHook] Failed to copy schematic: " + source + " -> " + destination);
            return false;
        }
    }

    @Nullable
    private File resolveSchematicFile(@NotNull String name) {
        File schemFile = new File(this.schematicsFolder, name + SCHEM_EXTENSION);
        if (schemFile.exists()) {
            return schemFile;
        }
        File schematicFile = new File(this.schematicsFolder, name + LEGACY_EXTENSION);
        if (schematicFile.exists()) {
            return schematicFile;
        }
        return schemFile;
    }

    public void clearCache() {
        int size = this.clipboardCache.size();
        this.clipboardCache.clear();
        this.plugin.getLogger().info("[WorldEditHook] Clipboard cache cleared (" + size + " entries)");
    }

    public void removeFromCache(@NotNull String name) {
        this.clipboardCache.remove(name.toLowerCase());
    }

    public void preloadSchematics(String ... names) {
        this.plugin.getLogger().info("[WorldEditHook] Preloading " + names.length + " schematics...");
        for (String name : names) {
            if (this.clipboardCache.containsKey(name.toLowerCase()) || this.loadSchematic(name) == null) continue;
            this.plugin.getLogger().fine("[WorldEditHook] Preloaded: " + name);
        }
        this.plugin.getLogger().info("[WorldEditHook] Preload complete. Cache size: " + this.clipboardCache.size());
    }

    public int getCacheSize() {
        return this.clipboardCache.size();
    }

    public boolean isCached(@NotNull String name) {
        return this.clipboardCache.containsKey(name.toLowerCase());
    }

    public long calculateVolume(@NotNull Location corner1, @NotNull Location corner2) {
        int dx = Math.abs(corner1.getBlockX() - corner2.getBlockX()) + 1;
        int dy = Math.abs(corner1.getBlockY() - corner2.getBlockY()) + 1;
        int dz = Math.abs(corner1.getBlockZ() - corner2.getBlockZ()) + 1;
        return (long)dx * (long)dy * (long)dz;
    }

    @NotNull
    public File getSchematicsFolder() {
        return this.schematicsFolder;
    }

    private void executeTask(@NotNull Runnable task, @NotNull CompletableFuture<?> future) {
        if (this.faweEnabled) {
            CompletableFuture.runAsync(task).exceptionally(throwable -> {
                this.plugin.getLogger().log(Level.SEVERE, "[WorldEditHook] Async task failed", (Throwable)throwable);
                if (!future.isDone()) {
                    future.completeExceptionally((Throwable)throwable);
                }
                return null;
            });
        } else {
            Bukkit.getScheduler().runTask((Plugin)this.plugin, task);
        }
    }

    public void shutdown() {
        this.clearCache();
        this.plugin.getLogger().info("[WorldEditHook] Shutdown complete");
    }

    @NotNull
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u00a76WorldEdit Hook Status:\n");
        sb.append("\u00a77\u251c Available: ").append(this.worldEditEnabled ? "\u00a7aYes" : "\u00a7cNo").append("\n");
        if (this.worldEditEnabled) {
            sb.append("\u00a77\u251c Plugin: \u00a7f").append(this.activePluginName);
            sb.append(" v").append(this.activePluginVersion).append("\n");
            sb.append("\u00a77\u251c Async Mode: ").append(this.faweEnabled ? "\u00a7aEnabled (FAWE)" : "\u00a7eDisabled (WorldEdit)").append("\n");
            sb.append("\u00a77\u251c Format: \u00a7f").append(this.schematicFormat != null ? this.schematicFormat.getName() : "None").append("\n");
        }
        sb.append("\u00a77\u251c Cache Size: \u00a7f").append(this.clipboardCache.size()).append(" schematics\n");
        sb.append("\u00a77\u2514 Schematics: \u00a7f").append(this.getSchematicNames().length).append(" files");
        return sb.toString();
    }
}

