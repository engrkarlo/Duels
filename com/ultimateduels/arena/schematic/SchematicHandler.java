/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Location
 */
package com.ultimateduels.arena.schematic;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.schematic.FAWESchematicAdapter;
import com.ultimateduels.arena.schematic.FallbackSchematicAdapter;
import com.ultimateduels.arena.schematic.ISchematicAdapter;
import com.ultimateduels.arena.schematic.WorldEditSchematicAdapter;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Location;

public class SchematicHandler {
    private final UltimateDuels plugin;
    private final File schematicsFolder;
    private final boolean worldEditAvailable;
    private final boolean faweAvailable;
    private ISchematicAdapter adapter;

    public SchematicHandler(@Nonnull UltimateDuels plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        if (!this.schematicsFolder.exists()) {
            this.schematicsFolder.mkdirs();
        }
        this.faweAvailable = plugin.getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
        this.worldEditAvailable = plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit");
        this.initializeAdapter();
    }

    private void initializeAdapter() {
        if (this.faweAvailable) {
            try {
                this.adapter = new FAWESchematicAdapter(this.plugin, this.schematicsFolder);
                this.plugin.getLogger().info("Using FastAsyncWorldEdit for schematics.");
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to initialize FAWE adapter", e);
            }
        }
        if (this.adapter == null && this.worldEditAvailable) {
            try {
                this.adapter = new WorldEditSchematicAdapter(this.plugin, this.schematicsFolder);
                this.plugin.getLogger().info("Using WorldEdit for schematics.");
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to initialize WorldEdit adapter", e);
            }
        }
        if (this.adapter == null) {
            this.adapter = new FallbackSchematicAdapter(this.plugin, this.schematicsFolder);
            this.plugin.getLogger().warning("No WorldEdit/FAWE found! Using fallback schematic handler.");
        }
    }

    @Nonnull
    public CompletableFuture<Boolean> saveSchematic(@Nonnull String name, @Nonnull Location min, @Nonnull Location max) {
        return this.adapter.save(name, min, max);
    }

    @Nonnull
    public CompletableFuture<Boolean> pasteSchematic(@Nonnull String name, @Nonnull Location origin) {
        return this.adapter.paste(name, origin);
    }

    public boolean deleteSchematic(@Nonnull String name) {
        File file = new File(this.schematicsFolder, name + ".schem");
        if (file.exists()) {
            return file.delete();
        }
        file = new File(this.schematicsFolder, name + ".schematic");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public boolean schematicExists(@Nonnull String name) {
        return new File(this.schematicsFolder, name + ".schem").exists() || new File(this.schematicsFolder, name + ".schematic").exists();
    }

    public boolean isWorldEditAvailable() {
        return this.worldEditAvailable || this.faweAvailable;
    }
}

