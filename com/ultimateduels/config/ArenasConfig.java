/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.config;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.models.arena.Arena;
import com.ultimateduels.models.arena.ArenaState;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArenasConfig {
    private final UltimateDuels plugin;
    private File arenasFile;
    private FileConfiguration arenasConfig;

    public ArenasConfig(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public void load() {
        if (!this.arenasFile.exists()) {
            this.plugin.saveResource("arenas.yml", false);
        }
        this.arenasConfig = YamlConfiguration.loadConfiguration((File)this.arenasFile);
        this.plugin.getLogger().info("Arenas configuration loaded!");
    }

    public void reload() {
        this.arenasConfig = YamlConfiguration.loadConfiguration((File)this.arenasFile);
        this.plugin.getLogger().info("Arenas configuration reloaded!");
    }

    public void save() {
        try {
            this.arenasConfig.save(this.arenasFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save arenas configuration!", e);
        }
    }

    @NotNull
    public Map<String, Arena> loadAllArenas() {
        LinkedHashMap<String, Arena> arenas = new LinkedHashMap<String, Arena>();
        ConfigurationSection section = this.arenasConfig.getConfigurationSection("arenas");
        if (section == null) {
            this.plugin.getLogger().info("No arenas found in configuration.");
            return arenas;
        }
        for (String arenaId : section.getKeys(false)) {
            try {
                Arena arena = this.loadArena(arenaId);
                if (arena == null) continue;
                arenas.put(arenaId, arena);
                this.plugin.debug("Loaded arena: " + arenaId);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load arena: " + arenaId, e);
            }
        }
        this.plugin.getLogger().info("Loaded " + arenas.size() + " arena(s)!");
        return arenas;
    }

    @Nullable
    public Arena loadArena(@NotNull String arenaId) {
        ConfigurationSection section = this.arenasConfig.getConfigurationSection("arenas." + arenaId);
        if (section == null) {
            return null;
        }
        String worldName = section.getString("world", "world");
        World world = Bukkit.getWorld((String)worldName);
        if (world == null) {
            this.plugin.getLogger().warning("Arena '" + arenaId + "' has invalid world: " + worldName);
            return null;
        }
        Arena arena = Arena.fromConfig(arenaId, section, world);
        arena.setState(ArenaState.AVAILABLE);
        return arena;
    }

    @Nullable
    private Location loadLocation(@Nullable ConfigurationSection section, @NotNull World world) {
        if (section == null) {
            return null;
        }
        double x = section.getDouble("x", 0.0);
        double y = section.getDouble("y", 64.0);
        double z = section.getDouble("z", 0.0);
        float yaw = (float)section.getDouble("yaw", 0.0);
        float pitch = (float)section.getDouble("pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void saveArena(@NotNull Arena arena) {
        Arena.ArenaBoundingBox goal2;
        Arena.ArenaBoundingBox goal1;
        Material iconMaterial;
        Set<String> incompatibleKits;
        Set<String> compatibleKits;
        String path = "arenas." + arena.getId();
        this.arenasConfig.set(path + ".display-name", (Object)arena.getDisplayName());
        List<String> description = arena.getDescription();
        if (description != null && !description.isEmpty()) {
            this.arenasConfig.set(path + ".description", description);
        }
        this.arenasConfig.set(path + ".enabled", (Object)arena.isEnabled());
        this.arenasConfig.set(path + ".type", (Object)arena.getType().name());
        this.arenasConfig.set(path + ".world", (Object)arena.getWorldName());
        this.arenasConfig.set(path + ".weight", (Object)arena.getWeight());
        this.arenasConfig.set(path + ".max-team-size", (Object)arena.getMaxTeamSize());
        if (arena.getSpawn1() != null) {
            this.saveLocation(path + ".spawn1", arena.getSpawn1());
        }
        if (arena.getSpawn2() != null) {
            this.saveLocation(path + ".spawn2", arena.getSpawn2());
        }
        if (arena.getSpectatorSpawn() != null) {
            this.saveLocation(path + ".spectator-spawn", arena.getSpectatorSpawn());
        }
        if (arena.getCenter() != null) {
            this.saveLocation(path + ".center", arena.getCenter());
        }
        this.arenasConfig.set(path + ".boundaries.enabled", (Object)arena.isBoundariesEnabled());
        if (arena.getBoundaryMin() != null) {
            this.saveLocation(path + ".boundaries.min", arena.getBoundaryMin());
        }
        if (arena.getBoundaryMax() != null) {
            this.saveLocation(path + ".boundaries.max", arena.getBoundaryMax());
        }
        this.arenasConfig.set(path + ".void-death.enabled", (Object)arena.isVoidDeathEnabled());
        this.arenasConfig.set(path + ".void-death.y-level", (Object)arena.getVoidDeathYLevel());
        if (arena.getSchematicFile() != null) {
            this.arenasConfig.set(path + ".schematic.file", (Object)arena.getSchematicFile());
            this.arenasConfig.set(path + ".schematic.ignore-air", (Object)arena.isSchematicIgnoreAir());
            if (arena.getSchematicOrigin() != null) {
                this.saveLocation(path + ".schematic.origin", arena.getSchematicOrigin());
            }
        }
        if ((compatibleKits = arena.getCompatibleKits()) != null && !compatibleKits.isEmpty()) {
            this.arenasConfig.set(path + ".compatible-kits", new ArrayList<String>(compatibleKits));
        }
        if ((incompatibleKits = arena.getIncompatibleKits()) != null && !incompatibleKits.isEmpty()) {
            this.arenasConfig.set(path + ".incompatible-kits", new ArrayList<String>(incompatibleKits));
        }
        if ((iconMaterial = arena.getIconMaterial()) != null) {
            this.arenasConfig.set(path + ".icon.material", (Object)iconMaterial.name());
        }
        this.arenasConfig.set(path + ".icon.glow", (Object)arena.isIconGlow());
        Map<String, Object> settings = arena.getSettings();
        if (settings != null && !settings.isEmpty()) {
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                this.arenasConfig.set(path + ".settings." + entry.getKey(), entry.getValue());
            }
        }
        if ((goal1 = arena.getGoalTeam1()) != null) {
            this.saveLocation(path + ".goals.team1.min", goal1.getMin());
            this.saveLocation(path + ".goals.team1.max", goal1.getMax());
        }
        if ((goal2 = arena.getGoalTeam2()) != null) {
            this.saveLocation(path + ".goals.team2.min", goal2.getMin());
            this.saveLocation(path + ".goals.team2.max", goal2.getMax());
        }
        this.save();
        this.plugin.debug("Saved arena: " + arena.getId());
    }

    private void saveLocation(@NotNull String path, @NotNull Location location) {
        this.arenasConfig.set(path + ".x", (Object)location.getX());
        this.arenasConfig.set(path + ".y", (Object)location.getY());
        this.arenasConfig.set(path + ".z", (Object)location.getZ());
        this.arenasConfig.set(path + ".yaw", (Object)Float.valueOf(location.getYaw()));
        this.arenasConfig.set(path + ".pitch", (Object)Float.valueOf(location.getPitch()));
    }

    public boolean deleteArena(@NotNull String arenaId) {
        if (!this.arenasConfig.contains("arenas." + arenaId)) {
            return false;
        }
        this.arenasConfig.set("arenas." + arenaId, null);
        this.save();
        this.plugin.debug("Deleted arena: " + arenaId);
        return true;
    }

    public boolean arenaExists(@NotNull String arenaId) {
        return this.arenasConfig.contains("arenas." + arenaId);
    }

    @NotNull
    public Set<String> getArenaIds() {
        ConfigurationSection section = this.arenasConfig.getConfigurationSection("arenas");
        if (section == null) {
            return Collections.emptySet();
        }
        return section.getKeys(false);
    }

    @NotNull
    public FileConfiguration getConfig() {
        return this.arenasConfig;
    }

    public void updateArenaField(@NotNull String arenaId, @NotNull String field, @Nullable Object value) {
        this.arenasConfig.set("arenas." + arenaId + "." + field, value);
        this.save();
    }

    public void setArenaEnabled(@NotNull String arenaId, boolean enabled) {
        this.updateArenaField(arenaId, "enabled", enabled);
    }

    public void setSpawnLocation(@NotNull String arenaId, int spawnNumber, @NotNull Location location) {
        String path = "arenas." + arenaId + ".spawn" + spawnNumber;
        this.saveLocation(path, location);
    }

    public void setSpectatorSpawn(@NotNull String arenaId, @NotNull Location location) {
        String path = "arenas." + arenaId + ".spectator-spawn";
        this.saveLocation(path, location);
    }

    public void setBoundaries(@NotNull String arenaId, @NotNull Location min, @NotNull Location max) {
        String path = "arenas." + arenaId;
        this.arenasConfig.set(path + ".boundaries.enabled", (Object)true);
        this.saveLocation(path + ".boundaries.min", min);
        this.saveLocation(path + ".boundaries.max", max);
        this.save();
    }

    public void setVoidDeath(@NotNull String arenaId, boolean enabled, int yLevel) {
        String path = "arenas." + arenaId;
        this.arenasConfig.set(path + ".void-death.enabled", (Object)enabled);
        this.arenasConfig.set(path + ".void-death.y-level", (Object)yLevel);
        this.save();
    }

    public void setSchematic(@NotNull String arenaId, @NotNull String schematicFile) {
        this.updateArenaField(arenaId, "schematic.file", schematicFile);
    }

    public void addCompatibleKit(@NotNull String arenaId, @NotNull String kitId) {
        List kits = this.arenasConfig.getStringList("arenas." + arenaId + ".compatible-kits");
        if (!kits.contains(kitId)) {
            kits.add(kitId);
            this.arenasConfig.set("arenas." + arenaId + ".compatible-kits", (Object)kits);
            this.save();
        }
    }

    public void removeCompatibleKit(@NotNull String arenaId, @NotNull String kitId) {
        List kits = this.arenasConfig.getStringList("arenas." + arenaId + ".compatible-kits");
        if (kits.remove(kitId)) {
            this.arenasConfig.set("arenas." + arenaId + ".compatible-kits", (Object)kits);
            this.save();
        }
    }
}

