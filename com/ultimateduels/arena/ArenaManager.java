/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Mob
 *  org.bukkit.entity.Player
 *  org.bukkit.generator.WorldInfo
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.ultimateduels.arena;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.ArenaSetupSession;
import com.ultimateduels.arena.model.ArenaSpawnPoint;
import com.ultimateduels.arena.model.ArenaState;
import com.ultimateduels.arena.model.ArenaType;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.hooks.WorldEditHook;
import com.ultimateduels.listeners.arena.ArenaWandListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class ArenaManager {
    private final UltimateDuels plugin;
    @Nullable
    private final WorldEditHook worldEditHook;
    private final Map<String, DuelArena> arenas;
    private final Map<String, DuelArena> ffaArenas;
    private final Map<String, UUID> arenaMatchMap;
    private final Queue<String> availableArenas;
    private final Queue<String> availableFFAArenas;
    private final Queue<ArenaRegenerationTask> regenerationQueue;
    private BukkitTask regenerationProcessor;
    private final Map<UUID, ArenaSetupSession> setupSessions;
    private File arenasFile;
    private FileConfiguration arenasConfig;
    private File ffaArenasFile;
    private FileConfiguration ffaArenasConfig;
    private int maxConcurrentRegenerations;
    private int regenerationDelayTicks;
    private boolean autoRegenerateOnMatch;
    private boolean cleanupDroppedItems;

    public ArenaManager(@Nonnull UltimateDuels plugin) {
        this.plugin = plugin;
        WorldEditHook tempHook = null;
        try {
            if (plugin.isWorldEditEnabled()) {
                tempHook = new WorldEditHook(plugin);
                plugin.getLogger().info("  \u2713 WorldEditHook initialized successfully");
            } else {
                plugin.getLogger().warning("  \u25cb WorldEdit not available - schematic features will be disabled");
            }
        }
        catch (NoClassDefFoundError e) {
            plugin.getLogger().warning("  \u2717 WorldEdit classes not found: " + e.getMessage());
            plugin.getLogger().warning("    Schematic features will be disabled");
        }
        catch (Exception e) {
            plugin.getLogger().warning("  \u2717 Failed to initialize WorldEditHook: " + e.getMessage());
            plugin.getLogger().warning("    Schematic features will be disabled");
        }
        this.worldEditHook = tempHook;
        this.arenas = new ConcurrentHashMap<String, DuelArena>();
        this.ffaArenas = new ConcurrentHashMap<String, DuelArena>();
        this.arenaMatchMap = new ConcurrentHashMap<String, UUID>();
        this.availableArenas = new ConcurrentLinkedQueue<String>();
        this.availableFFAArenas = new ConcurrentLinkedQueue<String>();
        this.regenerationQueue = new ConcurrentLinkedQueue<ArenaRegenerationTask>();
        this.setupSessions = new ConcurrentHashMap<UUID, ArenaSetupSession>();
        this.loadSettings();
        this.initializeStorage();
        this.loadAllArenas();
        this.startRegenerationProcessor();
        plugin.getLogger().info("\u00a7a[ArenaManager] Initialized with " + this.arenas.size() + " duel arenas and " + this.ffaArenas.size() + " FFA arenas");
    }

    private void loadSettings() {
        FileConfiguration config = this.plugin.getConfig();
        this.maxConcurrentRegenerations = config.getInt("arenas.max-concurrent-regenerations", 2);
        this.regenerationDelayTicks = config.getInt("arenas.regeneration-delay-ticks", 20);
        this.autoRegenerateOnMatch = config.getBoolean("arenas.auto-regenerate", true);
        this.cleanupDroppedItems = config.getBoolean("arenas.cleanup-dropped-items", true);
    }

    private void initializeStorage() {
        this.arenasFile = new File(this.plugin.getDataFolder(), "arenas/duel-arenas.yml");
        if (!this.arenasFile.getParentFile().exists()) {
            this.arenasFile.getParentFile().mkdirs();
        }
        if (!this.arenasFile.exists()) {
            try {
                this.arenasFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to create duel-arenas.yml", e);
            }
        }
        this.arenasConfig = YamlConfiguration.loadConfiguration((File)this.arenasFile);
        this.ffaArenasFile = new File(this.plugin.getDataFolder(), "arenas/ffa-arenas.yml");
        if (!this.ffaArenasFile.getParentFile().exists()) {
            this.ffaArenasFile.getParentFile().mkdirs();
        }
        if (!this.ffaArenasFile.exists()) {
            try {
                this.ffaArenasFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to create arenas/ffa-arenas.yml", e);
            }
        }
        this.ffaArenasConfig = YamlConfiguration.loadConfiguration((File)this.ffaArenasFile);
        File schematicsFolder = new File(this.plugin.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }

    public void loadArenas() {
        this.loadAllArenas();
    }

    private void loadAllArenas() {
        this.loadDuelArenas();
        this.loadFFAArenas();
        this.updateAvailableQueues();
        this.plugin.getLogger().info("\u00a7a[ArenaManager] Loaded " + this.arenas.size() + " duel arenas and " + this.ffaArenas.size() + " FFA arenas");
    }

    public void retryLoadArenas() {
        this.plugin.getLogger().info("\u00a7e[ArenaManager] Retrying arena loading after world initialization...");
        int beforeDuel = this.arenas.size();
        int beforeFFA = this.ffaArenas.size();
        this.loadAllArenas();
        int afterDuel = this.arenas.size();
        int afterFFA = this.ffaArenas.size();
        if (afterDuel > beforeDuel || afterFFA > beforeFFA) {
            this.plugin.getLogger().info("\u00a7a[ArenaManager] Successfully loaded additional arenas:");
            this.plugin.getLogger().info("\u00a7a  Duel arenas: " + beforeDuel + " \u2192 " + afterDuel);
            this.plugin.getLogger().info("\u00a7a  FFA arenas: " + beforeFFA + " \u2192 " + afterFFA);
        } else {
            this.plugin.getLogger().info("\u00a77[ArenaManager] No additional arenas loaded on retry.");
        }
        this.revalidateArenas();
        this.updateAvailableQueues();
    }

    private void revalidateArenas() {
        this.plugin.getLogger().info("\u00a7e[ArenaManager] Re-validating all arenas...");
        int disabledCount = 0;
        for (DuelArena arena : new ArrayList<DuelArena>(this.ffaArenas.values())) {
            if (!arena.isEnabled() || arena.isFullyConfigured()) continue;
            this.plugin.getLogger().warning("\u00a7c[ArenaManager] FFA Arena '" + arena.getName() + "' is no longer valid after reload!");
            this.plugin.getLogger().warning("  - Spawn points: " + arena.getSpawnPoints().size());
            this.plugin.getLogger().warning("  - Linked kit: " + arena.getLinkedKit());
            arena.setEnabled(false);
            arena.setState(ArenaState.DISABLED);
            ++disabledCount;
        }
        for (DuelArena arena : new ArrayList<DuelArena>(this.arenas.values())) {
            if (!arena.isEnabled() || arena.isFullyConfigured()) continue;
            this.plugin.getLogger().warning("\u00a7c[ArenaManager] Duel Arena '" + arena.getName() + "' is no longer valid after reload!");
            arena.setEnabled(false);
            arena.setState(ArenaState.DISABLED);
            ++disabledCount;
        }
        if (disabledCount > 0) {
            this.plugin.getLogger().warning("\u00a7e[ArenaManager] Disabled " + disabledCount + " invalid arenas. Use /arena info <name> to check details.");
            this.saveAll();
        } else {
            this.plugin.getLogger().info("\u00a7a[ArenaManager] All arenas validated successfully!");
        }
    }

    private void loadDuelArenas() {
        this.arenas.clear();
        this.arenasConfig = YamlConfiguration.loadConfiguration((File)this.arenasFile);
        ConfigurationSection section = this.arenasConfig.getConfigurationSection("arenas");
        if (section == null) {
            return;
        }
        for (String arenaName : section.getKeys(false)) {
            try {
                DuelArena arena = this.deserializeArena(section.getConfigurationSection(arenaName), arenaName);
                if (arena == null) continue;
                this.arenas.put(arenaName.toLowerCase(), arena);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load arena: " + arenaName, e);
            }
        }
    }

    private void loadFFAArenas() {
        this.ffaArenas.clear();
        this.ffaArenasConfig = YamlConfiguration.loadConfiguration((File)this.ffaArenasFile);
        ConfigurationSection section = this.ffaArenasConfig.getConfigurationSection("arenas");
        if (section != null && !section.getKeys(false).isEmpty()) {
            this.plugin.getLogger().info("\u00a7e[ArenaManager] Loading FFA arenas from internal storage...");
            int successCount = 0;
            int failCount = 0;
            for (String arenaName : section.getKeys(false)) {
                try {
                    DuelArena arena = this.deserializeArena(section.getConfigurationSection(arenaName), arenaName);
                    if (arena != null) {
                        arena.setArenaType(ArenaType.FFA);
                        this.ffaArenas.put(arenaName.toLowerCase(), arena);
                        int spawnCount = arena.getSpawnPoints().size();
                        String status = spawnCount > 0 ? "\u00a7a\u2713" : "\u00a7c\u2717";
                        this.plugin.getLogger().info("  " + status + " " + arenaName + " - " + spawnCount + " spawn points" + (String)(arena.getLinkedKit() != null ? " | kit=" + arena.getLinkedKit() : " | \u00a7cno kit"));
                        ++successCount;
                        continue;
                    }
                    this.plugin.getLogger().warning("  \u00a7c\u2717 " + arenaName + " - Failed to deserialize");
                    ++failCount;
                }
                catch (Exception e) {
                    this.plugin.getLogger().log(Level.WARNING, "  \u00a7c\u2717 " + arenaName + " - Exception during load", e);
                    ++failCount;
                }
            }
            this.plugin.getLogger().info("\u00a7e[ArenaManager] FFA arena loading complete: \u00a7a" + successCount + " success\u00a77, \u00a7c" + failCount + " failed");
            return;
        }
        File resourceStyleFile = new File(this.plugin.getDataFolder(), "ffa-arenas.yml");
        if (!resourceStyleFile.exists()) {
            this.plugin.getLogger().info("\u00a77[ArenaManager] No FFA arenas found in internal storage or ffa-arenas.yml");
            return;
        }
        this.plugin.getLogger().info("\u00a7e[ArenaManager] No internal FFA arenas found. Importing from ffa-arenas.yml (resource format)...");
        YamlConfiguration resourceConfig = YamlConfiguration.loadConfiguration((File)resourceStyleFile);
        ConfigurationSection arenasSection = resourceConfig.getConfigurationSection("arenas");
        if (arenasSection == null) {
            this.plugin.getLogger().warning("\u00a7c[ArenaManager] ffa-arenas.yml has no 'arenas:' section!");
            return;
        }
        int imported = 0;
        int skipped = 0;
        for (String arenaId : arenasSection.getKeys(false)) {
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaId);
            if (arenaSection == null) continue;
            try {
                DuelArena arena = this.importFromResourceFormat(arenaId, arenaSection);
                if (arena != null) {
                    this.ffaArenas.put(arenaId.toLowerCase(), arena);
                    int spawnCount = arena.getSpawnPoints().size();
                    this.plugin.getLogger().info("  \u00a7a\u2713 Imported '" + arenaId + "' - " + spawnCount + " spawns | kit=" + arena.getLinkedKit());
                    ++imported;
                    continue;
                }
                this.plugin.getLogger().warning("  \u00a7c\u2717 Failed to import '" + arenaId + "'");
                ++skipped;
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "  \u00a7c\u2717 Exception importing '" + arenaId + "'", e);
                ++skipped;
            }
        }
        this.plugin.getLogger().info("\u00a7a[ArenaManager] Imported " + imported + " FFA arenas from ffa-arenas.yml (" + skipped + " skipped)");
        if (imported > 0) {
            this.saveFFAArenas();
            this.plugin.getLogger().info("\u00a7a[ArenaManager] Imported arenas saved to internal storage.");
        }
    }

    private void updateAvailableQueues() {
        this.availableArenas.clear();
        this.availableFFAArenas.clear();
        for (DuelArena arena : this.arenas.values()) {
            if (arena.getState() != ArenaState.AVAILABLE || !arena.isEnabled()) continue;
            this.availableArenas.add(arena.getName().toLowerCase());
        }
        for (DuelArena arena : this.ffaArenas.values()) {
            if (arena.getState() != ArenaState.AVAILABLE || !arena.isEnabled()) continue;
            this.availableFFAArenas.add(arena.getName().toLowerCase());
        }
    }

    public boolean isWorldEditAvailable() {
        return this.worldEditHook != null;
    }

    @Nullable
    public WorldEditHook getWorldEditHook() {
        return this.worldEditHook;
    }

    public boolean createArena(@Nonnull String name, @Nonnull ArenaType type) {
        if (name == null || name.trim().isEmpty()) {
            this.plugin.getLogger().warning("[ArenaManager] Cannot create arena with empty name!");
            return false;
        }
        String key = name.toLowerCase().trim();
        if (key.isEmpty()) {
            this.plugin.getLogger().warning("[ArenaManager] Arena name is empty after processing!");
            return false;
        }
        if (this.arenas.containsKey(key) || this.ffaArenas.containsKey(key)) {
            return false;
        }
        DuelArena arena = new DuelArena(name, type);
        arena.setState(ArenaState.SETUP);
        arena.setEnabled(false);
        if (type == ArenaType.FFA) {
            this.ffaArenas.put(key, arena);
            this.saveFFAArenas();
        } else {
            this.arenas.put(key, arena);
            this.saveDuelArenas();
        }
        this.plugin.getLogger().info("\u00a7a[ArenaManager] Created arena: " + name + " (Type: " + String.valueOf((Object)type) + ")");
        return true;
    }

    public boolean deleteArena(@Nonnull String name) {
        String key = name.toLowerCase();
        DuelArena arena = this.arenas.remove(key);
        if (arena != null) {
            this.deleteSchematic(arena);
            this.saveDuelArenas();
            this.updateAvailableQueues();
            this.plugin.getLogger().info("\u00a7c[ArenaManager] Deleted duel arena: " + name);
            return true;
        }
        arena = this.ffaArenas.remove(key);
        if (arena != null) {
            this.deleteSchematic(arena);
            this.saveFFAArenas();
            this.updateAvailableQueues();
            this.plugin.getLogger().info("\u00a7c[ArenaManager] Deleted FFA arena: " + name);
            return true;
        }
        return false;
    }

    public boolean renameArena(@Nonnull String oldName, @Nonnull String newName) {
        String oldKey = oldName.toLowerCase();
        String newKey = newName.toLowerCase();
        if (this.arenas.containsKey(newKey) || this.ffaArenas.containsKey(newKey)) {
            return false;
        }
        DuelArena arena = this.arenas.remove(oldKey);
        if (arena != null) {
            arena.setName(newName);
            this.arenas.put(newKey, arena);
            this.saveDuelArenas();
            this.updateAvailableQueues();
            return true;
        }
        arena = this.ffaArenas.remove(oldKey);
        if (arena != null) {
            arena.setName(newName);
            this.ffaArenas.put(newKey, arena);
            this.saveFFAArenas();
            this.updateAvailableQueues();
            return true;
        }
        return false;
    }

    public boolean setArenaEnabled(@Nonnull String name, boolean enabled) {
        DuelArena arena = this.getArena(name);
        if (arena == null) {
            return false;
        }
        if (enabled && !arena.isFullyConfigured()) {
            return false;
        }
        arena.setEnabled(enabled);
        if (enabled) {
            arena.setState(ArenaState.AVAILABLE);
        } else {
            arena.setState(ArenaState.DISABLED);
        }
        this.saveArena(arena);
        this.updateAvailableQueues();
        return true;
    }

    public void setSpawnPoint1(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.setSpawnPoint1(location.clone());
        this.saveArena(arena);
    }

    public void setSpawnPoint2(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.setSpawnPoint2(location.clone());
        this.saveArena(arena);
    }

    public void setSpectatorSpawn(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.setSpectatorSpawn(location.clone());
        this.saveArena(arena);
    }

    public void addFFASpawnPoint(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return;
        }
        arena.addSpawnPoint(new ArenaSpawnPoint(location.clone(), arena.getSpawnPoints().size()));
        this.saveFFAArenas();
    }

    public boolean removeFFASpawnPoint(@Nonnull String arenaName, int index) {
        DuelArena arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return false;
        }
        List<ArenaSpawnPoint> spawnPoints = arena.getSpawnPoints();
        if (index < 0 || index >= spawnPoints.size()) {
            return false;
        }
        spawnPoints.remove(index);
        for (int i = 0; i < spawnPoints.size(); ++i) {
            spawnPoints.get(i).setIndex(i);
        }
        this.saveFFAArenas();
        return true;
    }

    public void clearFFASpawnPoints(@Nonnull String arenaName) {
        DuelArena arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return;
        }
        arena.getSpawnPoints().clear();
        this.saveFFAArenas();
    }

    public void setCorner1(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.setCorner1(location.clone());
        this.saveArena(arena);
    }

    public void setCorner2(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.setCorner2(location.clone());
        this.saveArena(arena);
    }

    public boolean isWithinArenaBounds(@Nonnull String arenaName, @Nonnull Location location) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null || !arena.hasBounds()) {
            return false;
        }
        return arena.isWithinBounds(location);
    }

    public void giveArenaWand(@Nonnull Player player, @Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            player.sendMessage("\u00a7cArena not found!");
            return;
        }
        ItemStack wand = ArenaWandListener.createWand(this.plugin, arenaName);
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(), wand);
            player.sendMessage("\u00a7eInventory full! Wand dropped at your feet.");
        } else {
            player.getInventory().addItem(new ItemStack[]{wand});
        }
    }

    @Nonnull
    public CompletableFuture<Boolean> saveSchematic(@Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null || !arena.hasBounds()) {
            return CompletableFuture.completedFuture(false);
        }
        Location min = arena.getMinPoint();
        Location max = arena.getMaxPoint();
        if (min == null || max == null) {
            return CompletableFuture.completedFuture(false);
        }
        if (this.worldEditHook == null) {
            this.plugin.getLogger().warning("Cannot save schematic - WorldEdit not available");
            return CompletableFuture.completedFuture(false);
        }
        String schematicName = "arena_" + arenaName.toLowerCase();
        arena.setSchematicName(schematicName);
        this.saveArena(arena);
        return this.worldEditHook.saveSchematic(min, max, schematicName).thenApply(success -> {
            if (success.booleanValue()) {
                // empty if block
            }
            return success;
        });
    }

    @Nonnull
    public CompletableFuture<Boolean> pasteSchematic(@Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null || arena.getSchematicName() == null || !arena.hasBounds()) {
            return CompletableFuture.completedFuture(false);
        }
        Location origin = arena.getMinPoint();
        if (origin == null) {
            return CompletableFuture.completedFuture(false);
        }
        if (this.worldEditHook == null) {
            this.plugin.getLogger().warning("Cannot paste schematic - WorldEdit not available");
            return CompletableFuture.completedFuture(false);
        }
        arena.setState(ArenaState.REGENERATING);
        return this.worldEditHook.pasteSchematic(arena.getSchematicName(), origin).thenApply(success -> {
            if (success.booleanValue()) {
                arena.setState(ArenaState.AVAILABLE);
                arena.incrementRegenerationCount();
                this.updateAvailableQueues();
                this.plugin.debug("[ArenaManager] Regenerated arena: " + arenaName);
            } else {
                arena.setState(ArenaState.DISABLED);
                this.plugin.getLogger().warning("\u00a7c[ArenaManager] Failed to regenerate arena: " + arenaName);
            }
            return success;
        });
    }

    @Nonnull
    public CompletableFuture<Boolean> regenerateArena(@Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return CompletableFuture.completedFuture(false);
        }
        if (this.worldEditHook == null) {
            this.plugin.getLogger().warning("Cannot regenerate arena - WorldEdit not available");
            arena.setState(ArenaState.AVAILABLE);
            return CompletableFuture.completedFuture(false);
        }
        arena.setState(ArenaState.REGENERATING);
        return this.pasteSchematic(arenaName).thenApply(success -> {
            if (success.booleanValue()) {
                arena.setState(ArenaState.AVAILABLE);
                this.saveArena(arena);
            } else {
                arena.setState(ArenaState.AVAILABLE);
                this.plugin.getLogger().warning("Arena regeneration failed, but arena is still available: " + arenaName);
            }
            return success;
        });
    }

    private void deleteSchematic(@Nonnull DuelArena arena) {
        if (arena.getSchematicName() != null && this.worldEditHook != null) {
            try {
                this.worldEditHook.deleteSchematic(arena.getSchematicName());
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to delete schematic: " + e.getMessage());
            }
        }
    }

    @Nullable
    public DuelArena allocateArena(@Nullable String preferredArena, @Nullable String kitName) {
        DuelArena arena;
        if (preferredArena != null && !preferredArena.equalsIgnoreCase("random") && (arena = this.arenas.get(preferredArena.toLowerCase())) != null && arena.getState() == ArenaState.AVAILABLE && arena.isEnabled() && (kitName == null || arena.canUseKit(kitName))) {
            this.markArenaInUse(arena);
            return arena;
        }
        for (String arenaName : new ArrayList<String>(this.availableArenas)) {
            DuelArena arena2 = this.arenas.get(arenaName);
            if (arena2 == null || arena2.getState() != ArenaState.AVAILABLE || !arena2.isEnabled() || kitName != null && !arena2.canUseKit(kitName)) continue;
            this.markArenaInUse(arena2);
            return arena2;
        }
        return null;
    }

    @Nullable
    public DuelArena allocateFFAArena(@Nonnull String kitName) {
        for (DuelArena arena : this.ffaArenas.values()) {
            if (!arena.isEnabled() || arena.getState() != ArenaState.AVAILABLE || arena.getLinkedKit() == null || !arena.getLinkedKit().equalsIgnoreCase(kitName)) continue;
            return arena;
        }
        return null;
    }

    private void markArenaInUse(@Nonnull DuelArena arena) {
        arena.setState(ArenaState.IN_USE);
        arena.setLastUsed(System.currentTimeMillis());
        this.availableArenas.remove(arena.getName().toLowerCase());
    }

    public void markArenaInUse(@Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena != null) {
            this.markArenaInUse(arena);
        }
    }

    public void markArenaAvailable(@Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena != null && arena.isEnabled()) {
            arena.setState(ArenaState.AVAILABLE);
            arena.incrementTotalMatches();
            this.availableArenas.add(arenaName.toLowerCase());
            this.saveArena(arena);
        }
    }

    public void releaseArena(@Nonnull String arenaName, @Nullable UUID matchUUID) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        this.arenaMatchMap.remove(arenaName.toLowerCase());
        arena.incrementTotalMatches();
        if (this.cleanupDroppedItems) {
            this.cleanupArenaItems(arena);
        }
        arena.setState(ArenaState.AVAILABLE);
        this.availableArenas.add(arenaName.toLowerCase());
        this.saveArena(arena);
        if (this.autoRegenerateOnMatch && arena.getSchematicName() != null && this.worldEditHook != null) {
            arena.setState(ArenaState.REGENERATING);
            this.queueRegeneration(arena);
        }
    }

    @Nonnull
    public CompletableFuture<Void> releaseArena(@Nonnull String arenaName, boolean regenerate) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return CompletableFuture.completedFuture(null);
        }
        arena.incrementTotalMatches();
        this.arenaMatchMap.remove(arenaName.toLowerCase());
        if (this.cleanupDroppedItems) {
            this.cleanupArenaItems(arena);
        }
        if (regenerate && arena.getSchematicName() != null && this.worldEditHook != null) {
            return this.regenerateArena(arenaName).thenAccept(success -> {
                if (success.booleanValue()) {
                    this.availableArenas.add(arenaName.toLowerCase());
                }
            });
        }
        arena.setState(ArenaState.AVAILABLE);
        this.availableArenas.add(arenaName.toLowerCase());
        this.saveArena(arena);
        return CompletableFuture.completedFuture(null);
    }

    public void cleanupArenaItems(@Nonnull DuelArena arena) {
        double maxZ;
        double minZ;
        double maxY;
        double minY;
        double maxX;
        double minX;
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        Location spawn1 = arena.getSpawnPoint1();
        if (spawn1 == null) {
            return;
        }
        World world = spawn1.getWorld();
        if (world == null) {
            return;
        }
        if (corner1 != null && corner2 != null && corner1.getWorld() != null) {
            minX = Math.min(corner1.getX(), corner2.getX()) - 2.0;
            maxX = Math.max(corner1.getX(), corner2.getX()) + 2.0;
            minY = Math.min(corner1.getY(), corner2.getY()) - 5.0;
            maxY = Math.max(corner1.getY(), corner2.getY()) + 5.0;
            minZ = Math.min(corner1.getZ(), corner2.getZ()) - 2.0;
            maxZ = Math.max(corner1.getZ(), corner2.getZ()) + 2.0;
        } else {
            Location spawn2 = arena.getSpawnPoint2();
            double cx = spawn2 != null ? (spawn1.getX() + spawn2.getX()) / 2.0 : spawn1.getX();
            double cy = spawn2 != null ? (spawn1.getY() + spawn2.getY()) / 2.0 : spawn1.getY();
            double cz = spawn2 != null ? (spawn1.getZ() + spawn2.getZ()) / 2.0 : spawn1.getZ();
            double r = 60.0;
            minX = cx - r;
            maxX = cx + r;
            minY = cy - r;
            maxY = cy + r;
            minZ = cz - r;
            maxZ = cz + r;
        }
        block46: for (Entity entity : world.getEntities()) {
            Mob mob;
            Location loc;
            if (entity instanceof Player || (loc = entity.getLocation()).getX() < minX || loc.getX() > maxX || loc.getY() < minY || loc.getY() > maxY || loc.getZ() < minZ || loc.getZ() > maxZ) continue;
            switch (entity.getType().name()) {
                case "ARROW": 
                case "SPECTRAL_ARROW": 
                case "TRIDENT": 
                case "SNOWBALL": 
                case "EGG": 
                case "FIREBALL": 
                case "SMALL_FIREBALL": 
                case "DRAGON_FIREBALL": 
                case "WITHER_SKULL": 
                case "SHULKER_BULLET": 
                case "LLAMA_SPIT": 
                case "FISHING_BOBBER": 
                case "POTION": 
                case "AREA_EFFECT_CLOUD": 
                case "WIND_CHARGE": 
                case "BREEZE_WIND_CHARGE": {
                    entity.remove();
                    continue block46;
                }
                case "BOAT": 
                case "CHEST_BOAT": 
                case "MINECART": 
                case "CHEST_MINECART": 
                case "HOPPER_MINECART": 
                case "TNT_MINECART": 
                case "FURNACE_MINECART": 
                case "COMMAND_BLOCK_MINECART": {
                    entity.remove();
                    continue block46;
                }
                case "END_CRYSTAL": 
                case "ARMOR_STAND": 
                case "ITEM_FRAME": 
                case "GLOW_ITEM_FRAME": {
                    entity.remove();
                    continue block46;
                }
                case "ITEM": 
                case "FALLING_BLOCK": 
                case "TNT": 
                case "FIREWORK_ROCKET": 
                case "EXPERIENCE_ORB": {
                    entity.remove();
                    continue block46;
                }
                case "TEXT_DISPLAY": 
                case "BLOCK_DISPLAY": 
                case "ITEM_DISPLAY": 
                case "INTERACTION": {
                    if (entity.isPersistent()) continue block46;
                    entity.remove();
                    continue block46;
                }
            }
            if (!(entity instanceof Mob) || (mob = (Mob)entity).getCustomName() != null || mob.isPersistent()) continue;
            mob.remove();
        }
        this.plugin.debug("[ArenaManager] Cleaned arena entities for: " + arena.getName());
    }

    public void queueRegeneration(@Nonnull DuelArena arena) {
        if (this.worldEditHook == null) {
            this.plugin.getLogger().warning("Cannot queue regeneration - WorldEdit not available");
            arena.setState(ArenaState.AVAILABLE);
            return;
        }
        arena.setState(ArenaState.REGENERATING);
        this.regenerationQueue.add(new ArenaRegenerationTask(arena, System.currentTimeMillis()));
    }

    private void startRegenerationProcessor() {
        if (this.worldEditHook == null) {
            this.plugin.getLogger().info("\u00a7e[ArenaManager] Regeneration processor not started - WorldEdit not available");
            return;
        }
        this.regenerationProcessor = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, () -> {
            ArenaRegenerationTask task;
            int processing = 0;
            while (!this.regenerationQueue.isEmpty() && processing < this.maxConcurrentRegenerations && (task = this.regenerationQueue.poll()) != null) {
                long elapsed = System.currentTimeMillis() - task.queuedTime();
                if (elapsed < (long)this.regenerationDelayTicks * 50L) {
                    this.regenerationQueue.add(task);
                    continue;
                }
                ++processing;
                this.processRegeneration(task.arena());
            }
        }, 20L, 10L);
    }

    private void processRegeneration(@Nonnull DuelArena arena) {
        if (this.worldEditHook == null) {
            arena.setState(ArenaState.AVAILABLE);
            if (!this.availableArenas.contains(arena.getName().toLowerCase())) {
                this.availableArenas.add(arena.getName().toLowerCase());
            }
            return;
        }
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.cleanupArenaItems(arena));
        this.pasteSchematic(arena.getName()).thenAccept(success -> Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            this.cleanupArenaItems(arena);
            arena.setState(ArenaState.AVAILABLE);
            if (!this.availableArenas.contains(arena.getName().toLowerCase())) {
                this.availableArenas.add(arena.getName().toLowerCase());
            }
            if (success.booleanValue()) {
                arena.incrementRegenerationCount();
            }
            this.saveArena(arena);
        }));
    }

    public void regenerateAllArenas() {
        if (this.worldEditHook == null) {
            this.plugin.getLogger().warning("\u00a7e[ArenaManager] Cannot regenerate arenas - WorldEdit not available");
            return;
        }
        for (DuelArena arena : this.arenas.values()) {
            if (arena.getSchematicName() == null || !arena.isEnabled()) continue;
            this.queueRegeneration(arena);
        }
        this.plugin.getLogger().info("\u00a7e[ArenaManager] Queued " + this.arenas.size() + " arenas for regeneration");
    }

    @Nullable
    public DuelArena getArena(@Nonnull String name) {
        String key = name.toLowerCase();
        DuelArena arena = this.arenas.get(key);
        if (arena != null) {
            return arena;
        }
        return this.ffaArenas.get(key);
    }

    @Nullable
    public DuelArena getDuelArena(@Nonnull String name) {
        return this.arenas.get(name.toLowerCase());
    }

    @Nullable
    public DuelArena getFFAArena(@Nonnull String name) {
        return this.ffaArenas.get(name.toLowerCase());
    }

    @Nonnull
    public Collection<DuelArena> getAllDuelArenas() {
        return new ArrayList<DuelArena>(this.arenas.values());
    }

    @Nonnull
    public Collection<DuelArena> getAllFFAArenas() {
        return new ArrayList<DuelArena>(this.ffaArenas.values());
    }

    @Nonnull
    public List<DuelArena> getEnabledArenas() {
        return this.arenas.values().stream().filter(DuelArena::isEnabled).collect(Collectors.toList());
    }

    @Nonnull
    public List<DuelArena> getEnabledFFAArenas() {
        return this.ffaArenas.values().stream().filter(DuelArena::isEnabled).collect(Collectors.toList());
    }

    @Nonnull
    public Set<String> getDuelArenaNames() {
        return new HashSet<String>(this.arenas.keySet());
    }

    @Nonnull
    public Set<String> getFFAArenaNames() {
        return new HashSet<String>(this.ffaArenas.keySet());
    }

    @Nonnull
    public List<DuelArena> getAvailableArenas(@Nonnull ArenaType type) {
        if (type == ArenaType.FFA) {
            return this.ffaArenas.values().stream().filter(DuelArena::isAvailable).collect(Collectors.toList());
        }
        return this.arenas.values().stream().filter(a -> a.isAvailable() && a.getArenaType() == type).collect(Collectors.toList());
    }

    @Nullable
    public DuelArena getRandomAvailableArena(@Nonnull ArenaType type) {
        List<DuelArena> available = this.getAvailableArenas(type);
        if (available.isEmpty()) {
            return null;
        }
        return available.get(new Random().nextInt(available.size()));
    }

    @Nonnull
    public List<DuelArena> getAvailableArenasForKit(@Nonnull String kitName) {
        return this.arenas.values().stream().filter(a -> a.isAvailable() && a.isKitCompatible(kitName)).collect(Collectors.toList());
    }

    public int getAvailableArenaCount() {
        return this.availableArenas.size();
    }

    @Nonnull
    public List<DuelArena> getArenasByState(@Nonnull ArenaState state) {
        return this.arenas.values().stream().filter(a -> a.getState() == state).collect(Collectors.toList());
    }

    @Nonnull
    public List<DuelArena> getArenasForKit(@Nonnull String kitName) {
        return this.arenas.values().stream().filter(a -> a.isEnabled() && a.isKitCompatible(kitName)).collect(Collectors.toList());
    }

    public boolean arenaExists(@Nonnull String name) {
        String key = name.toLowerCase();
        return this.arenas.containsKey(key) || this.ffaArenas.containsKey(key);
    }

    public void startSetupSession(@Nonnull Player player, @Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        this.setupSessions.put(player.getUniqueId(), new ArenaSetupSession(player.getUniqueId(), arena));
    }

    @Nullable
    public ArenaSetupSession getSetupSession(@Nonnull UUID playerUUID) {
        return this.setupSessions.get(playerUUID);
    }

    public void endSetupSession(@Nonnull UUID playerUUID) {
        ArenaSetupSession session = this.setupSessions.remove(playerUUID);
        if (session != null) {
            this.saveArena(session.getArena());
        }
    }

    public boolean isInSetupMode(@Nonnull UUID playerUUID) {
        return this.setupSessions.containsKey(playerUUID);
    }

    public void setArenaKits(@Nonnull String arenaName, @Nonnull List<String> kits) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.setCompatibleKits(kits);
        this.saveArena(arena);
    }

    public void addArenaKit(@Nonnull String arenaName, @Nonnull String kitName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.addCompatibleKit(kitName);
        this.saveArena(arena);
    }

    public void removeArenaKit(@Nonnull String arenaName, @Nonnull String kitName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return;
        }
        arena.removeCompatibleKit(kitName);
        this.saveArena(arena);
    }

    public void setFFAArenaKit(@Nonnull String arenaName, @Nonnull String kitName) {
        DuelArena arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return;
        }
        arena.setLinkedKit(kitName);
        this.saveFFAArenas();
    }

    public boolean setArenaUseAssignedKits(@Nonnull String arenaName, boolean enable) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return false;
        }
        arena.setUseAssignedKits(enable);
        this.saveArena(arena);
        this.plugin.getLogger().info("Arena " + arenaName + " exclusive kit assignment: " + (enable ? "ENABLED" : "DISABLED"));
        return true;
    }

    public boolean assignKitToArena(@Nonnull String arenaName, @Nonnull String kitName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return false;
        }
        arena.assignKit(kitName);
        this.saveArena(arena);
        this.plugin.getLogger().info("Assigned kit '" + kitName + "' to arena '" + arenaName + "'");
        return true;
    }

    public boolean unassignKitFromArena(@Nonnull String arenaName, @Nonnull String kitName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return false;
        }
        arena.unassignKit(kitName);
        this.saveArena(arena);
        this.plugin.getLogger().info("Unassigned kit '" + kitName + "' from arena '" + arenaName + "'");
        return true;
    }

    public boolean clearArenaAssignedKits(@Nonnull String arenaName) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return false;
        }
        arena.clearAssignedKits();
        this.saveArena(arena);
        this.plugin.getLogger().info("Cleared all assigned kits from arena '" + arenaName + "'");
        return true;
    }

    public boolean setArenaAllowBlockBreak(@Nonnull String arenaName, boolean allow) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return false;
        }
        arena.setAllowBlockBreak(allow);
        this.saveArena(arena);
        this.plugin.getLogger().info("Arena " + arenaName + " block breaking: " + (allow ? "ALLOWED" : "BLOCKED"));
        return true;
    }

    public boolean setArenaAllowBlockPlace(@Nonnull String arenaName, boolean allow) {
        DuelArena arena = this.getArena(arenaName);
        if (arena == null) {
            return false;
        }
        arena.setAllowBlockPlace(allow);
        this.saveArena(arena);
        this.plugin.getLogger().info("Arena " + arenaName + " block placing: " + (allow ? "ALLOWED" : "BLOCKED"));
        return true;
    }

    @Nonnull
    public ArenaStatistics getStatistics() {
        int total = this.arenas.size() + this.ffaArenas.size();
        int available = (int)this.arenas.values().stream().filter(a -> a.getState() == ArenaState.AVAILABLE).count();
        int inUse = (int)this.arenas.values().stream().filter(a -> a.getState() == ArenaState.IN_USE).count();
        int regenerating = (int)this.arenas.values().stream().filter(a -> a.getState() == ArenaState.REGENERATING).count();
        int disabled = (int)this.arenas.values().stream().filter(a -> !a.isEnabled()).count();
        int ffaActive = this.ffaArenas.size();
        return new ArenaStatistics(total, available, inUse, regenerating, disabled, ffaActive);
    }

    public void cleanupInactiveArenas() {
        long timeSinceLastUsed;
        long now = System.currentTimeMillis();
        long timeout = 300000L;
        for (DuelArena arena : this.arenas.values()) {
            if (arena.getState() != ArenaState.REGENERATING || (timeSinceLastUsed = now - arena.getLastUsed()) <= timeout) continue;
            this.plugin.getLogger().warning("Arena " + arena.getName() + " stuck in REGENERATING state. Resetting to AVAILABLE.");
            arena.setState(ArenaState.AVAILABLE);
            this.availableArenas.add(arena.getName().toLowerCase());
            this.saveArena(arena);
        }
        for (DuelArena arena : this.ffaArenas.values()) {
            if (arena.getState() != ArenaState.REGENERATING || (timeSinceLastUsed = now - arena.getLastUsed()) <= timeout) continue;
            this.plugin.getLogger().warning("FFA Arena " + arena.getName() + " stuck in REGENERATING state. Resetting to AVAILABLE.");
            arena.setState(ArenaState.AVAILABLE);
            this.availableFFAArenas.add(arena.getName().toLowerCase());
            this.saveArena(arena);
        }
    }

    @Nullable
    private DuelArena importFromResourceFormat(@Nonnull String arenaId, @Nonnull ConfigurationSection section) {
        boolean fullyConfigured;
        ConfigurationSection boundaries;
        String kitName = section.getString("kit");
        if (kitName == null || kitName.isEmpty()) {
            this.plugin.getLogger().warning("[ArenaManager] Arena '" + arenaId + "' has no 'kit:' field \u2014 skipping.");
            return null;
        }
        String worldName = section.getString("world", "world");
        World world = Bukkit.getWorld((String)worldName);
        if (world == null) {
            this.plugin.getLogger().warning("[ArenaManager] Arena '" + arenaId + "': world '" + worldName + "' is not loaded. Spawn points will be skipped.");
        }
        DuelArena arena = new DuelArena(arenaId, ArenaType.FFA);
        arena.setLinkedKit(kitName.toLowerCase());
        arena.setEnabled(section.getBoolean("enabled", false));
        arena.setDisplayName(section.getString("display-name", arenaId));
        ConfigurationSection spawnsSection = section.getConfigurationSection("spawns");
        if (spawnsSection != null && world != null) {
            ArrayList keys = new ArrayList(spawnsSection.getKeys(false));
            keys.sort((a, b) -> {
                try {
                    return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                }
                catch (NumberFormatException e) {
                    return a.compareTo((String)b);
                }
            });
            int spawnIndex = 0;
            for (String key : keys) {
                ConfigurationSection spawnSection = spawnsSection.getConfigurationSection(key);
                Location loc = null;
                if (spawnSection != null) {
                    double x = spawnSection.getDouble("x", 0.0);
                    double y = spawnSection.getDouble("y", 64.0);
                    double z = spawnSection.getDouble("z", 0.0);
                    float yaw = (float)spawnSection.getDouble("yaw", 0.0);
                    float pitch = (float)spawnSection.getDouble("pitch", 0.0);
                    loc = new Location(world, x, y, z, yaw, pitch);
                } else {
                    String raw = spawnsSection.getString(key);
                    if (raw != null) {
                        loc = this.deserializeLocation(raw);
                    }
                }
                if (loc != null) {
                    arena.addSpawnPoint(new ArenaSpawnPoint(loc, spawnIndex++));
                    continue;
                }
                this.plugin.getLogger().warning("[ArenaManager] Could not parse spawn '" + key + "' for arena '" + arenaId + "'");
            }
        }
        if ((boundaries = section.getConfigurationSection("boundaries")) != null && world != null && boundaries.getBoolean("enabled", false)) {
            ConfigurationSection minSec = boundaries.getConfigurationSection("min");
            ConfigurationSection maxSec = boundaries.getConfigurationSection("max");
            if (minSec != null && maxSec != null) {
                arena.setCorner1(new Location(world, minSec.getDouble("x"), minSec.getDouble("y"), minSec.getDouble("z")));
                arena.setCorner2(new Location(world, maxSec.getDouble("x"), maxSec.getDouble("y"), maxSec.getDouble("z")));
            }
        }
        boolean bl = fullyConfigured = !arena.getSpawnPoints().isEmpty() && arena.getLinkedKit() != null;
        if (arena.isEnabled() && fullyConfigured) {
            arena.setState(ArenaState.AVAILABLE);
        } else if (!arena.isEnabled()) {
            arena.setState(ArenaState.DISABLED);
        } else {
            arena.setState(ArenaState.SETUP);
            if (world == null) {
                arena.setEnabled(false);
                this.plugin.getLogger().warning("[ArenaManager] Arena '" + arenaId + "' disabled because world '" + worldName + "' is not loaded.");
            }
        }
        return arena;
    }

    @Nullable
    private DuelArena deserializeArena(@Nullable ConfigurationSection section, @Nonnull String name) {
        if (section == null) {
            return null;
        }
        try {
            ArenaType type;
            String typeName = section.getString("type", "DUEL_1V1");
            try {
                type = ArenaType.valueOf(typeName);
            }
            catch (IllegalArgumentException e) {
                type = ArenaType.DUEL_1V1;
            }
            DuelArena arena = new DuelArena(name, type);
            arena.setDisplayName(section.getString("displayName", name));
            arena.setEnabled(section.getBoolean("enabled", false));
            arena.setSchematicName(section.getString("schematic"));
            arena.setLinkedKit(section.getString("linkedKit"));
            if (section.contains("spawn1")) {
                arena.setSpawnPoint1(this.deserializeLocation(section.getString("spawn1")));
            }
            if (section.contains("spawn2")) {
                arena.setSpawnPoint2(this.deserializeLocation(section.getString("spawn2")));
            }
            if (section.contains("spectatorSpawn")) {
                arena.setSpectatorSpawn(this.deserializeLocation(section.getString("spectatorSpawn")));
            }
            if (section.contains("corner1")) {
                arena.setCorner1(this.deserializeLocation(section.getString("corner1")));
            }
            if (section.contains("corner2")) {
                arena.setCorner2(this.deserializeLocation(section.getString("corner2")));
            }
            if (section.contains("ffaSpawnPoints")) {
                List spawnStrs = section.getStringList("ffaSpawnPoints");
                this.plugin.getLogger().info("\u00a7e[ArenaManager] Loading " + spawnStrs.size() + " FFA spawn points for arena: " + name);
                int successCount = 0;
                for (int i = 0; i < spawnStrs.size(); ++i) {
                    String spawnStr = (String)spawnStrs.get(i);
                    this.plugin.debug("  Attempting to load spawn point " + (i + 1) + ": " + spawnStr);
                    Location loc = this.deserializeLocation(spawnStr);
                    if (loc != null) {
                        arena.addSpawnPoint(new ArenaSpawnPoint(loc, i));
                        ++successCount;
                        this.plugin.debug("  \u00a7a\u2713 Successfully loaded spawn point " + (i + 1) + " at " + this.formatLocation(loc));
                        continue;
                    }
                    this.plugin.getLogger().warning("  \u00a7c\u2717 Failed to load spawn point " + (i + 1) + " for arena '" + name + "'");
                    this.plugin.getLogger().warning("    Raw data: " + spawnStr);
                }
                if (successCount == 0 && !spawnStrs.isEmpty()) {
                    this.plugin.getLogger().severe("\u00a7c[ArenaManager] CRITICAL: Arena '" + name + "' has " + spawnStrs.size() + " spawn points in config but NONE loaded!");
                    this.plugin.getLogger().severe("\u00a7c  This arena will NOT be functional!");
                } else {
                    this.plugin.getLogger().info("\u00a7a[ArenaManager] Successfully loaded " + successCount + "/" + spawnStrs.size() + " FFA spawn points for arena: " + name);
                }
            }
            List kits = section.getStringList("compatibleKits");
            arena.setCompatibleKits(new ArrayList<String>(kits));
            List assignedKitsList = section.getStringList("assigned-kits");
            arena.setAssignedKits(new HashSet<String>(assignedKitsList));
            arena.setUseAssignedKits(section.getBoolean("use-assigned-kits", false));
            arena.setAllowBlockBreak(section.getBoolean("allow-block-break", false));
            arena.setAllowBlockPlace(section.getBoolean("allow-block-place", false));
            arena.setAllowBuilding(section.getBoolean("allowBuilding", false));
            arena.setAllowBreaking(section.getBoolean("allowBreaking", false));
            arena.setBuildHeightLimit(section.getInt("buildHeightLimit", 256));
            arena.setDeathY(section.getInt("deathY", 0));
            arena.setVoidKills(section.getBoolean("voidKills", true));
            arena.setTotalMatches(section.getInt("stats.totalMatches", 0));
            arena.setRegenerationCount(section.getInt("stats.regenerations", 0));
            arena.setCreatedAt(section.getLong("stats.createdAt", System.currentTimeMillis()));
            arena.setLastUsed(section.getLong("stats.lastUsed", 0L));
            if (arena.isEnabled() && arena.isFullyConfigured()) {
                arena.setState(ArenaState.AVAILABLE);
            } else if (!arena.isEnabled()) {
                arena.setState(ArenaState.DISABLED);
            } else {
                arena.setState(ArenaState.SETUP);
            }
            return arena;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Error deserializing arena: " + name, e);
            return null;
        }
    }

    private void serializeArena(@Nonnull DuelArena arena, @Nonnull ConfigurationSection section) {
        section.set("type", (Object)arena.getArenaType().name());
        section.set("displayName", (Object)arena.getDisplayName());
        section.set("enabled", (Object)arena.isEnabled());
        section.set("schematic", (Object)arena.getSchematicName());
        section.set("linkedKit", (Object)arena.getLinkedKit());
        if (arena.getSpawnPoint1() != null) {
            section.set("spawn1", (Object)this.serializeLocation(arena.getSpawnPoint1()));
        }
        if (arena.getSpawnPoint2() != null) {
            section.set("spawn2", (Object)this.serializeLocation(arena.getSpawnPoint2()));
        }
        if (arena.getSpectatorSpawn() != null) {
            section.set("spectatorSpawn", (Object)this.serializeLocation(arena.getSpectatorSpawn()));
        }
        if (arena.getCorner1() != null) {
            section.set("corner1", (Object)this.serializeLocation(arena.getCorner1()));
        }
        if (arena.getCorner2() != null) {
            section.set("corner2", (Object)this.serializeLocation(arena.getCorner2()));
        }
        ArrayList<String> spawnStrs = new ArrayList<String>();
        for (ArenaSpawnPoint sp : arena.getSpawnPoints()) {
            spawnStrs.add(this.serializeLocation(sp.getLocation()));
        }
        section.set("ffaSpawnPoints", spawnStrs);
        section.set("compatibleKits", arena.getCompatibleKits());
        section.set("assigned-kits", new ArrayList<String>(arena.getAssignedKits()));
        section.set("use-assigned-kits", (Object)arena.isUsingAssignedKits());
        section.set("allow-block-break", (Object)arena.isAllowBlockBreak());
        section.set("allow-block-place", (Object)arena.isAllowBlockPlace());
        section.set("allowBuilding", (Object)arena.isAllowBuilding());
        section.set("allowBreaking", (Object)arena.isAllowBreaking());
        section.set("buildHeightLimit", (Object)arena.getBuildHeightLimit());
        section.set("deathY", (Object)arena.getDeathY());
        section.set("voidKills", (Object)arena.isVoidKills());
        section.set("stats.totalMatches", (Object)arena.getTotalMatches());
        section.set("stats.regenerations", (Object)arena.getRegenerationCount());
        section.set("stats.createdAt", (Object)arena.getCreatedAt());
        section.set("stats.lastUsed", (Object)arena.getLastUsed());
    }

    @Nonnull
    private String serializeLocation(@Nonnull Location loc) {
        String worldName = "world";
        if (loc.getWorld() != null) {
            worldName = loc.getWorld().getName();
        } else {
            this.plugin.getLogger().warning("\u00a7c[ArenaManager] Serializing location with NULL world! Using 'world' as fallback.");
        }
        return String.format("%s;%.3f;%.3f;%.3f;%.2f;%.2f", worldName, loc.getX(), loc.getY(), loc.getZ(), Float.valueOf(loc.getYaw()), Float.valueOf(loc.getPitch()));
    }

    @Nullable
    private Location deserializeLocation(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            String[] parts = str.split(";");
            String worldName = parts[0];
            World world = Bukkit.getWorld((String)worldName);
            if (world == null) {
                this.plugin.getLogger().warning("\u00a7e[ArenaManager] World '" + worldName + "' not found during arena loading.");
                this.plugin.getLogger().warning("\u00a7e  Available worlds: " + Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.joining(", ")));
                this.plugin.getLogger().warning("\u00a7e  This location will be skipped. Arena may not function correctly.");
                return null;
            }
            return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("\u00a7c[ArenaManager] Failed to deserialize location: " + str);
            return null;
        }
    }

    private String formatLocation(@Nonnull Location loc) {
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private void saveArena(@Nonnull DuelArena arena) {
        if (arena.getArenaType() == ArenaType.FFA) {
            this.saveFFAArenas();
        } else {
            this.saveDuelArenas();
        }
    }

    public void saveDuelArenas() {
        this.arenasConfig = new YamlConfiguration();
        for (Map.Entry<String, DuelArena> entry : this.arenas.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.trim().isEmpty()) {
                this.plugin.getLogger().warning("[ArenaManager] Skipping arena with empty key!");
                continue;
            }
            ConfigurationSection section = this.arenasConfig.createSection("arenas." + key);
            this.serializeArena(entry.getValue(), section);
        }
        try {
            this.arenasConfig.save(this.arenasFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save duel arenas", e);
        }
    }

    public void saveFFAArenas() {
        this.ffaArenasConfig = new YamlConfiguration();
        for (Map.Entry<String, DuelArena> entry : this.ffaArenas.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.trim().isEmpty()) {
                this.plugin.getLogger().warning("[ArenaManager] Skipping FFA arena with empty key!");
                continue;
            }
            ConfigurationSection section = this.ffaArenasConfig.createSection("arenas." + key);
            this.serializeArena(entry.getValue(), section);
        }
        try {
            this.ffaArenasConfig.save(this.ffaArenasFile);
            if (this.ffaArenasFile.exists()) {
                this.plugin.getLogger().info("\u00a7a[ArenaManager] Saved " + this.ffaArenas.size() + " FFA arenas to disk");
            }
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save FFA arenas", e);
        }
    }

    public void verifyFFAArenaData(@Nonnull String arenaName) {
        DuelArena arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            this.plugin.getLogger().warning("\u00a7c[ArenaManager] Arena not found: " + arenaName);
            return;
        }
        this.plugin.getLogger().info("\u00a7e[ArenaManager] Verifying FFA arena: " + arenaName);
        this.plugin.getLogger().info("  - Corner 1: " + (arena.getCorner1() != null ? this.formatLocation(arena.getCorner1()) : "NOT SET"));
        this.plugin.getLogger().info("  - Corner 2: " + (arena.getCorner2() != null ? this.formatLocation(arena.getCorner2()) : "NOT SET"));
        this.plugin.getLogger().info("  - FFA Spawn Points: " + arena.getSpawnPoints().size());
        for (int i = 0; i < arena.getSpawnPoints().size(); ++i) {
            ArenaSpawnPoint sp = arena.getSpawnPoints().get(i);
            this.plugin.getLogger().info("    [" + (i + 1) + "] " + this.formatLocation(sp.getLocation()));
        }
        this.plugin.getLogger().info("  - Linked Kit: " + (arena.getLinkedKit() != null ? arena.getLinkedKit() : "NOT SET"));
        this.plugin.getLogger().info("  - Enabled: " + arena.isEnabled());
        this.plugin.getLogger().info("  - Configured: " + arena.isFullyConfigured());
    }

    public void saveAll() {
        this.saveDuelArenas();
        this.saveFFAArenas();
    }

    public void reloadArenas() {
        this.loadSettings();
        this.arenas.clear();
        this.ffaArenas.clear();
        this.loadAllArenas();
        this.plugin.getLogger().info("\u00a7a[ArenaManager] Reloaded " + this.arenas.size() + " duel arenas and " + this.ffaArenas.size() + " FFA arenas");
    }

    public void reload() {
        this.reloadArenas();
    }

    public void shutdown() {
        this.plugin.getLogger().info("\u00a7e[ArenaManager] Shutting down...");
        if (this.regenerationProcessor != null) {
            try {
                this.regenerationProcessor.cancel();
                this.plugin.getLogger().info("  \u00a7a\u2713 Cancelled regeneration processor");
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("  \u00a7c\u2717 Error cancelling regeneration processor: " + e.getMessage());
            }
        }
        try {
            this.plugin.getLogger().info("  \u00a7e\u2699 Saving " + this.arenas.size() + " duel arenas...");
            this.saveDuelArenas();
            this.plugin.getLogger().info("  \u00a7a\u2713 Duel arenas saved");
            this.plugin.getLogger().info("  \u00a7e\u2699 Saving " + this.ffaArenas.size() + " FFA arenas...");
            this.saveFFAArenas();
            this.plugin.getLogger().info("  \u00a7a\u2713 FFA arenas saved");
            if (this.ffaArenasFile.exists() && this.ffaArenasFile.length() > 0L) {
                this.plugin.getLogger().info("  \u00a7a\u2713 FFA arenas file verified on disk (" + this.ffaArenasFile.length() + " bytes)");
            } else {
                this.plugin.getLogger().severe("  \u00a7c\u2717 FFA arenas file is empty or missing!");
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "  \u00a7c\u2717 Error saving arenas during shutdown!", e);
        }
        this.setupSessions.clear();
        this.plugin.getLogger().info("\u00a7a[ArenaManager] Shutdown complete");
    }

    private record ArenaRegenerationTask(DuelArena arena, long queuedTime) {
    }

    public record ArenaStatistics(int totalArenas, int available, int inUse, int regenerating, int disabled, int ffaArenas) {
    }
}

