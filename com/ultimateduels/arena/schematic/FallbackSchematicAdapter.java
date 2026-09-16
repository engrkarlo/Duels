/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.arena.schematic;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.schematic.ISchematicAdapter;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class FallbackSchematicAdapter
implements ISchematicAdapter {
    private final UltimateDuels plugin;
    private final File schematicsFolder;

    public FallbackSchematicAdapter(@Nonnull UltimateDuels plugin, @Nonnull File schematicsFolder) {
        this.plugin = plugin;
        this.schematicsFolder = schematicsFolder;
    }

    @Override
    @Nonnull
    public CompletableFuture<Boolean> save(@Nonnull String name, @Nonnull Location min, @Nonnull Location max) {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                YamlConfiguration config = new YamlConfiguration();
                int minX = Math.min(min.getBlockX(), max.getBlockX());
                int minY = Math.min(min.getBlockY(), max.getBlockY());
                int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
                int maxX = Math.max(min.getBlockX(), max.getBlockX());
                int maxY = Math.max(min.getBlockY(), max.getBlockY());
                int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
                config.set("origin.world", (Object)min.getWorld().getName());
                config.set("origin.x", (Object)minX);
                config.set("origin.y", (Object)minY);
                config.set("origin.z", (Object)minZ);
                config.set("size.x", (Object)(maxX - minX + 1));
                config.set("size.y", (Object)(maxY - minY + 1));
                config.set("size.z", (Object)(maxZ - minZ + 1));
                int blockCount = 0;
                for (int x = minX; x <= maxX; ++x) {
                    for (int y = minY; y <= maxY; ++y) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            int fx = x;
                            int fy = y;
                            int fz = z;
                            CompletableFuture blockFuture = new CompletableFuture();
                            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                                Block block = min.getWorld().getBlockAt(fx, fy, fz);
                                blockFuture.complete(block.getBlockData().getAsString());
                            });
                            String blockData = (String)blockFuture.join();
                            String key = x - minX + "," + (y - minY) + "," + (z - minZ);
                            config.set("blocks." + key, (Object)blockData);
                            ++blockCount;
                        }
                    }
                }
                config.set("block-count", (Object)blockCount);
                File file = new File(this.schematicsFolder, name + ".fallback.yml");
                config.save(file);
                future.complete(true);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save fallback schematic", e);
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
                File file = new File(this.schematicsFolder, name + ".fallback.yml");
                if (!file.exists()) {
                    future.complete(false);
                    return;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
                int originX = origin.getBlockX();
                int originY = origin.getBlockY();
                int originZ = origin.getBlockZ();
                ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
                if (blocksSection == null) {
                    future.complete(false);
                    return;
                }
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                    try {
                        for (String key : blocksSection.getKeys(false)) {
                            String[] parts = key.split(",");
                            int x = Integer.parseInt(parts[0]) + originX;
                            int y = Integer.parseInt(parts[1]) + originY;
                            int z = Integer.parseInt(parts[2]) + originZ;
                            String blockDataStr = blocksSection.getString(key);
                            if (blockDataStr == null) continue;
                            BlockData blockData = Bukkit.createBlockData((String)blockDataStr);
                            origin.getWorld().getBlockAt(x, y, z).setBlockData(blockData, false);
                        }
                        future.complete(true);
                    }
                    catch (Exception e) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to paste fallback schematic", e);
                        future.complete(false);
                    }
                });
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load fallback schematic", e);
                future.complete(false);
            }
        });
        return future;
    }
}

