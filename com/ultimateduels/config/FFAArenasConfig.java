/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.config;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.ArenaSpawnPoint;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.models.ffa.FFAArena;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFAArenasConfig {
    private final UltimateDuels plugin;
    private File ffaArenasFile;
    private FileConfiguration ffaArenasConfig;

    public FFAArenasConfig(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.ffaArenasFile = new File(plugin.getDataFolder(), "ffa-arenas.yml");
    }

    public void load() {
        if (!this.ffaArenasFile.exists()) {
            this.plugin.saveResource("ffa-arenas.yml", false);
        }
        this.ffaArenasConfig = YamlConfiguration.loadConfiguration((File)this.ffaArenasFile);
        this.plugin.getLogger().info("FFA arenas configuration loaded!");
    }

    public void reload() {
        this.ffaArenasConfig = YamlConfiguration.loadConfiguration((File)this.ffaArenasFile);
        this.plugin.getLogger().info("FFA arenas configuration reloaded!");
    }

    public void save() {
        try {
            this.ffaArenasConfig.save(this.ffaArenasFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save FFA arenas configuration!", e);
        }
    }

    @NotNull
    public Map<String, ConfigurationSection> loadAllFFAArenaConfigs() {
        LinkedHashMap<String, ConfigurationSection> arenaConfigs = new LinkedHashMap<String, ConfigurationSection>();
        ConfigurationSection section = this.ffaArenasConfig.getConfigurationSection("ffa-arenas");
        if (section == null) {
            this.plugin.getLogger().info("No FFA arenas found in configuration.");
            return arenaConfigs;
        }
        for (String arenaId : section.getKeys(false)) {
            ConfigurationSection arenaSection = section.getConfigurationSection(arenaId);
            if (arenaSection == null) continue;
            arenaConfigs.put(arenaId, arenaSection);
            this.plugin.debug("Found FFA arena config: " + arenaId);
        }
        this.plugin.getLogger().info("Found " + arenaConfigs.size() + " FFA arena configuration(s)!");
        return arenaConfigs;
    }

    @Nullable
    public FFAArena createFFAArenaFromConfig(@NotNull String arenaId, @NotNull DuelArena baseArena, @NotNull DuelKit kit) {
        ConfigurationSection section = this.ffaArenasConfig.getConfigurationSection("ffa-arenas." + arenaId);
        if (section == null) {
            return null;
        }
        FFAArena arena = new FFAArena(arenaId, baseArena, kit);
        String displayName = section.getString("display-name", arenaId);
        arena.setDisplayName(displayName);
        List description = section.getStringList("description");
        if (!description.isEmpty()) {
            arena.setDescription(description);
        }
        boolean enabled = section.getBoolean("enabled", true);
        arena.setEnabled(enabled);
        int maxPlayers = section.getInt("max-players", 50);
        arena.setMaxPlayers(maxPlayers);
        int spawnProtection = section.getInt("spawn-protection-seconds", 3);
        arena.setSpawnProtectionSeconds(spawnProtection);
        boolean healOnKill = section.getBoolean("heal-on-kill", true);
        arena.setHealOnKill(healOnKill);
        int healAmount = section.getInt("heal-amount", 4);
        arena.setHealAmount(healAmount);
        boolean rekitOnKill = section.getBoolean("rekit-on-kill", false);
        arena.setRekitOnKill(rekitOnKill);
        boolean dropItemsOnDeath = section.getBoolean("drop-items-on-death", false);
        arena.setDropItemsOnDeath(dropItemsOnDeath);
        World world = Bukkit.getWorld((String)this.getWorldName(arenaId));
        if (world != null) {
            ConfigurationSection spawnsSection;
            Location spawn2;
            Location spawn1;
            Location corner2;
            Location corner1 = this.loadLocation(section.getConfigurationSection("corner1"), world);
            if (corner1 != null) {
                baseArena.setCorner1(corner1);
            }
            if ((corner2 = this.loadLocation(section.getConfigurationSection("corner2"), world)) != null) {
                baseArena.setCorner2(corner2);
            }
            if ((spawn1 = this.loadLocation(section.getConfigurationSection("spawn1"), world)) != null) {
                baseArena.setSpawnPoint1(spawn1);
            }
            if ((spawn2 = this.loadLocation(section.getConfigurationSection("spawn2"), world)) != null) {
                baseArena.setSpawnPoint2(spawn2);
            }
            if ((spawnsSection = section.getConfigurationSection("spawns")) != null) {
                for (String key : spawnsSection.getKeys(false)) {
                    Location spawnLoc = this.loadLocation(spawnsSection.getConfigurationSection(key), world);
                    if (spawnLoc == null) continue;
                    try {
                        int spawnIndex = Integer.parseInt(key);
                        baseArena.addSpawnPoint(new ArenaSpawnPoint(spawnLoc, spawnIndex, ArenaSpawnPoint.SpawnPointType.FFA));
                    }
                    catch (NumberFormatException e) {
                        this.plugin.getLogger().warning("Invalid spawn index for FFA arena " + arenaId + ": " + key);
                    }
                }
            }
        }
        this.plugin.debug("Created FFA arena from config: " + arenaId);
        return arena;
    }

    public void saveFFAArena(@NotNull FFAArena arena) {
        List<ArenaSpawnPoint> allSpawns;
        String path = "ffa-arenas." + arena.getArenaId();
        this.ffaArenasConfig.set(path + ".display-name", (Object)arena.getDisplayName());
        this.ffaArenasConfig.set(path + ".description", arena.getDescription());
        this.ffaArenasConfig.set(path + ".enabled", (Object)arena.isEnabled());
        this.ffaArenasConfig.set(path + ".linked-kit", (Object)arena.getKitId());
        this.ffaArenasConfig.set(path + ".world", (Object)arena.getWorldName());
        DuelArena baseArena = arena.getBaseArena();
        this.ffaArenasConfig.set(path + ".base-arena", (Object)baseArena.getName());
        if (baseArena.getCorner1() != null) {
            this.saveLocation(path + ".corner1", baseArena.getCorner1());
        }
        if (baseArena.getCorner2() != null) {
            this.saveLocation(path + ".corner2", baseArena.getCorner2());
        }
        if (baseArena.getSpawnPoint1() != null) {
            this.saveLocation(path + ".spawn1", baseArena.getSpawnPoint1());
        }
        if (baseArena.getSpawnPoint2() != null) {
            this.saveLocation(path + ".spawn2", baseArena.getSpawnPoint2());
        }
        if ((allSpawns = baseArena.getSpawnPoints()) != null && !allSpawns.isEmpty()) {
            this.ffaArenasConfig.set(path + ".spawns", null);
            for (int i = 0; i < allSpawns.size(); ++i) {
                ArenaSpawnPoint spawnPoint = allSpawns.get(i);
                this.saveLocation(path + ".spawns." + (i + 1), spawnPoint.getLocation());
            }
        }
        this.ffaArenasConfig.set(path + ".max-players", (Object)arena.getMaxPlayers());
        this.ffaArenasConfig.set(path + ".spawn-protection-seconds", (Object)arena.getSpawnProtectionSeconds());
        this.ffaArenasConfig.set(path + ".heal-on-kill", (Object)arena.isHealOnKill());
        this.ffaArenasConfig.set(path + ".heal-amount", (Object)arena.getHealAmount());
        this.ffaArenasConfig.set(path + ".rekit-on-kill", (Object)arena.isRekitOnKill());
        this.ffaArenasConfig.set(path + ".drop-items-on-death", (Object)arena.isDropItemsOnDeath());
        this.save();
        int spawnCount = allSpawns != null ? allSpawns.size() : 0;
        this.plugin.debug("Saved FFA arena: " + arena.getArenaId() + " with " + spawnCount + " spawn points");
    }

    @Nullable
    public String getLinkedKitName(@NotNull String arenaId) {
        return this.ffaArenasConfig.getString("ffa-arenas." + arenaId + ".linked-kit");
    }

    @NotNull
    public String getBaseArenaName(@NotNull String arenaId) {
        return this.ffaArenasConfig.getString("ffa-arenas." + arenaId + ".base-arena", arenaId);
    }

    @NotNull
    public String getWorldName(@NotNull String arenaId) {
        return this.ffaArenasConfig.getString("ffa-arenas." + arenaId + ".world", "world");
    }

    public boolean isEnabled(@NotNull String arenaId) {
        return this.ffaArenasConfig.getBoolean("ffa-arenas." + arenaId + ".enabled", true);
    }

    @NotNull
    public String getDisplayName(@NotNull String arenaId) {
        return this.ffaArenasConfig.getString("ffa-arenas." + arenaId + ".display-name", arenaId);
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

    private void saveLocation(@NotNull String path, @NotNull Location location) {
        this.ffaArenasConfig.set(path + ".x", (Object)location.getX());
        this.ffaArenasConfig.set(path + ".y", (Object)location.getY());
        this.ffaArenasConfig.set(path + ".z", (Object)location.getZ());
        this.ffaArenasConfig.set(path + ".yaw", (Object)Float.valueOf(location.getYaw()));
        this.ffaArenasConfig.set(path + ".pitch", (Object)Float.valueOf(location.getPitch()));
    }

    public void addSpawnLocation(@NotNull String arenaId, @NotNull Location location) {
        String path = "ffa-arenas." + arenaId + ".spawns";
        ConfigurationSection section = this.ffaArenasConfig.getConfigurationSection(path);
        int nextIndex = 1;
        if (section != null) {
            nextIndex = section.getKeys(false).size() + 1;
        }
        this.saveLocation(path + "." + nextIndex, location);
        this.save();
        this.plugin.debug("Added spawn location " + nextIndex + " to FFA arena: " + arenaId);
    }

    public void clearSpawnLocations(@NotNull String arenaId) {
        this.ffaArenasConfig.set("ffa-arenas." + arenaId + ".spawns", null);
        this.save();
        this.plugin.debug("Cleared spawn locations for FFA arena: " + arenaId);
    }

    public boolean deleteFFAArena(@NotNull String arenaId) {
        if (!this.ffaArenasConfig.contains("ffa-arenas." + arenaId)) {
            return false;
        }
        this.ffaArenasConfig.set("ffa-arenas." + arenaId, null);
        this.save();
        this.plugin.debug("Deleted FFA arena: " + arenaId);
        return true;
    }

    public boolean ffaArenaExists(@NotNull String arenaId) {
        return this.ffaArenasConfig.contains("ffa-arenas." + arenaId);
    }

    @NotNull
    public Set<String> getFFAArenaIds() {
        ConfigurationSection section = this.ffaArenasConfig.getConfigurationSection("ffa-arenas");
        if (section == null) {
            return Collections.emptySet();
        }
        return section.getKeys(false);
    }

    @NotNull
    public FileConfiguration getConfig() {
        return this.ffaArenasConfig;
    }

    public void updateFFAArenaField(@NotNull String arenaId, @NotNull String field, @Nullable Object value) {
        this.ffaArenasConfig.set("ffa-arenas." + arenaId + "." + field, value);
        this.save();
    }

    public void setFFAArenaEnabled(@NotNull String arenaId, boolean enabled) {
        this.updateFFAArenaField(arenaId, "enabled", enabled);
    }

    public void setFFAArenaDisplayName(@NotNull String arenaId, @NotNull String displayName) {
        this.updateFFAArenaField(arenaId, "display-name", displayName);
    }

    public void setFFAArenaMaxPlayers(@NotNull String arenaId, int maxPlayers) {
        this.updateFFAArenaField(arenaId, "max-players", maxPlayers);
    }

    public void setFFAArenaSpawnProtection(@NotNull String arenaId, int seconds) {
        this.updateFFAArenaField(arenaId, "spawn-protection-seconds", seconds);
    }

    public void setFFAArenaHealOnKill(@NotNull String arenaId, boolean healOnKill) {
        this.updateFFAArenaField(arenaId, "heal-on-kill", healOnKill);
    }

    public void setFFAArenaHealAmount(@NotNull String arenaId, int healAmount) {
        this.updateFFAArenaField(arenaId, "heal-amount", healAmount);
    }

    public void setFFAArenaRekitOnKill(@NotNull String arenaId, boolean rekitOnKill) {
        this.updateFFAArenaField(arenaId, "rekit-on-kill", rekitOnKill);
    }

    public void setFFAArenaDropItemsOnDeath(@NotNull String arenaId, boolean dropItems) {
        this.updateFFAArenaField(arenaId, "drop-items-on-death", dropItems);
    }

    public int getSpawnCount(@NotNull String arenaId) {
        ConfigurationSection section = this.ffaArenasConfig.getConfigurationSection("ffa-arenas." + arenaId + ".spawns");
        return section != null ? section.getKeys(false).size() : 0;
    }

    public int getMaxPlayers(@NotNull String arenaId) {
        return this.ffaArenasConfig.getInt("ffa-arenas." + arenaId + ".max-players", 50);
    }

    public int getSpawnProtectionSeconds(@NotNull String arenaId) {
        return this.ffaArenasConfig.getInt("ffa-arenas." + arenaId + ".spawn-protection-seconds", 3);
    }

    public boolean isHealOnKill(@NotNull String arenaId) {
        return this.ffaArenasConfig.getBoolean("ffa-arenas." + arenaId + ".heal-on-kill", true);
    }

    public int getHealAmount(@NotNull String arenaId) {
        return this.ffaArenasConfig.getInt("ffa-arenas." + arenaId + ".heal-amount", 4);
    }

    public boolean isRekitOnKill(@NotNull String arenaId) {
        return this.ffaArenasConfig.getBoolean("ffa-arenas." + arenaId + ".rekit-on-kill", false);
    }

    public boolean isDropItemsOnDeath(@NotNull String arenaId) {
        return this.ffaArenasConfig.getBoolean("ffa-arenas." + arenaId + ".drop-items-on-death", false);
    }
}

