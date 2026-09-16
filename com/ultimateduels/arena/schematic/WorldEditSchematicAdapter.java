/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sk89q.worldedit.EditSession
 *  com.sk89q.worldedit.WorldEdit
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
 *  javax.annotation.Nonnull
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.arena.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
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
import com.ultimateduels.arena.schematic.ISchematicAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class WorldEditSchematicAdapter
implements ISchematicAdapter {
    private final UltimateDuels plugin;
    private final File schematicsFolder;

    public WorldEditSchematicAdapter(@Nonnull UltimateDuels plugin, @Nonnull File schematicsFolder) {
        this.plugin = plugin;
        this.schematicsFolder = schematicsFolder;
    }

    @Override
    @Nonnull
    public CompletableFuture<Boolean> save(@Nonnull String name, @Nonnull Location min, @Nonnull Location max) {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            try {
                World world = BukkitAdapter.adapt((org.bukkit.World)min.getWorld());
                BlockVector3 minVec = BlockVector3.at((int)min.getBlockX(), (int)min.getBlockY(), (int)min.getBlockZ());
                BlockVector3 maxVec = BlockVector3.at((int)max.getBlockX(), (int)max.getBlockY(), (int)max.getBlockZ());
                CuboidRegion region = new CuboidRegion(world, minVec, maxVec);
                BlockArrayClipboard clipboard = new BlockArrayClipboard((Region)region);
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world);){
                    ForwardExtentCopy copy = new ForwardExtentCopy((Extent)editSession, (Region)region, (Extent)clipboard, region.getMinimumPoint());
                    copy.setCopyingEntities(false);
                    Operations.complete((Operation)copy);
                }
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
                    try {
                        File file = new File(this.schematicsFolder, name + ".schem");
                        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter((OutputStream)new FileOutputStream(file));){
                            writer.write((Clipboard)clipboard);
                        }
                        future.complete(true);
                    }
                    catch (Exception e) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to write schematic file", e);
                        future.complete(false);
                    }
                });
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save schematic: " + name, e);
                future.complete(false);
            }
        });
        return future;
    }

    @Override
    @Nonnull
    public CompletableFuture<Boolean> paste(@Nonnull String name, @Nonnull Location origin) {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                Clipboard clipboard;
                File file = new File(this.schematicsFolder, name + ".schem");
                if (!file.exists()) {
                    file = new File(this.schematicsFolder, name + ".schematic");
                }
                if (!file.exists()) {
                    this.plugin.getLogger().warning("Schematic file not found: " + name);
                    future.complete(false);
                    return;
                }
                ClipboardFormat format = ClipboardFormats.findByFile((File)file);
                if (format == null) {
                    future.complete(false);
                    return;
                }
                try (ClipboardReader reader = format.getReader((InputStream)new FileInputStream(file));){
                    clipboard = reader.read();
                }
                Clipboard finalClipboard = clipboard;
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    try {
                        World world = BukkitAdapter.adapt((org.bukkit.World)origin.getWorld());
                        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world);){
                            Operation operation = new ClipboardHolder(finalClipboard).createPaste((Extent)editSession).to(BlockVector3.at((int)origin.getBlockX(), (int)origin.getBlockY(), (int)origin.getBlockZ())).ignoreAirBlocks(false).build();
                            Operations.complete((Operation)operation);
                        }
                        future.complete(true);
                    }
                    catch (Exception e) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to paste schematic", e);
                        future.complete(false);
                    }
                });
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load schematic: " + name, e);
                future.complete(false);
            }
        });
        return future;
    }
}

