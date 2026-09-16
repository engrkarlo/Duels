/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitTask
 */
package com.ultimateduels.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.cooldown.CooldownManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.ffa.model.FFAInfo;
import com.ultimateduels.ffa.model.FFAPlayerData;
import com.ultimateduels.ffa.model.FFAResult;
import com.ultimateduels.ffa.model.FFAResultType;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.player.PlayerDataManager;
import com.ultimateduels.player.PlayerStateManager;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.schematic.SchematicManager;
import com.ultimateduels.scoreboard.ScoreboardManager;
import com.ultimateduels.utils.MessageUtils;
import com.ultimateduels.visuals.HealthDisplayManager;
import com.ultimateduels.world.WorldRestrictionManager;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class FFAManager {
    private final UltimateDuels plugin;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private final PlayerStateManager playerStateManager;
    private final PlayerDataManager playerDataManager;
    private final ScoreboardManager scoreboardManager;
    private final CooldownManager cooldownManager;
    private final Map<String, FFAArenaInstance> ffaArenas;
    private final Map<UUID, String> playerArenaMap;
    private final Map<UUID, FFAPlayerData> playerDataMap;
    private final Map<UUID, CombatData> combatTracking;
    private final Map<String, Long> pendingRegeneration;
    private boolean ffaEnabled;
    private int combatTagSeconds;
    private int minPlayersForStats;
    private boolean announceKills;
    private boolean spawnProtectionEnabled;
    private int spawnProtectionTicks;
    private boolean respawnTeleportToLobby;
    private int respawnDelaySeconds;
    private boolean respawnShowDeathMessage;
    private boolean killRewardsEnabled;
    private boolean healOnKill;
    private int healOnKillAmount;
    private boolean rekitOnKill;
    private boolean clearEffectsOnKill;
    private boolean playKillSound;
    private boolean giveGoldenAppleOnKill;
    private int goldenApplesOnKill;
    private boolean autoRegenerateArenas;
    private int regenerationDelaySeconds;
    private BukkitTask cleanupTask;
    private BukkitTask regenerationTask;

    public FFAManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerStateManager = plugin.getPlayerStateManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.scoreboardManager = plugin.getScoreboardManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.ffaArenas = new ConcurrentHashMap<String, FFAArenaInstance>();
        this.playerArenaMap = new ConcurrentHashMap<UUID, String>();
        this.playerDataMap = new ConcurrentHashMap<UUID, FFAPlayerData>();
        this.combatTracking = new ConcurrentHashMap<UUID, CombatData>();
        this.pendingRegeneration = new ConcurrentHashMap<String, Long>();
        this.loadSettings();
        this.initializeFFAArenas();
        this.startCleanupTask();
        this.startRegenerationTask();
        plugin.getLogger().info("[FFAManager] Initialized with " + this.ffaArenas.size() + " FFA arenas");
    }

    private void loadSettings() {
        FileConfiguration config = this.plugin.getConfig();
        this.ffaEnabled = config.getBoolean("ffa.enabled", true);
        this.combatTagSeconds = config.getInt("ffa.combat-tag-seconds", 10);
        this.minPlayersForStats = config.getInt("ffa.min-players-for-stats", 2);
        this.announceKills = config.getBoolean("ffa.announce-kills", true);
        this.spawnProtectionEnabled = config.getBoolean("ffa.spawn-protection.enabled", true);
        this.spawnProtectionTicks = config.getInt("ffa.spawn-protection.ticks", 60);
        this.respawnTeleportToLobby = config.getBoolean("ffa.respawn.teleport-to-lobby", true);
        this.respawnDelaySeconds = config.getInt("ffa.respawn.delay", 0);
        this.respawnShowDeathMessage = config.getBoolean("ffa.respawn.show-death-message", true);
        this.killRewardsEnabled = config.getBoolean("ffa.kill-rewards.enabled", true);
        this.healOnKill = config.getBoolean("ffa.kill-rewards.heal-on-kill", true);
        this.healOnKillAmount = config.getInt("ffa.kill-rewards.heal-amount", -1);
        this.rekitOnKill = config.getBoolean("ffa.kill-rewards.rekit-on-kill", true);
        this.clearEffectsOnKill = config.getBoolean("ffa.kill-rewards.clear-effects-on-kill", true);
        this.playKillSound = config.getBoolean("ffa.kill-rewards.play-sound", true);
        this.giveGoldenAppleOnKill = config.getBoolean("ffa.kill-rewards.give-golden-apple", false);
        this.goldenApplesOnKill = config.getInt("ffa.kill-rewards.golden-apples-amount", 1);
        this.autoRegenerateArenas = config.getBoolean("ffa.regeneration.enabled", true);
        this.regenerationDelaySeconds = config.getInt("ffa.regeneration.delay-seconds", 5);
        this.plugin.getLogger().info("[FFAManager] Settings loaded - Respawn: " + (this.respawnTeleportToLobby ? "Lobby" : "Arena") + " | Kill Rewards: " + this.killRewardsEnabled + " | Heal: " + this.healOnKill + " | Rekit: " + this.rekitOnKill);
    }

    private void initializeFFAArenas() {
        this.ffaArenas.clear();
        YamlConfiguration ffaConfig = null;
        try {
            File ffaFile = new File(this.plugin.getDataFolder(), "ffa-arenas.yml");
            if (ffaFile.exists()) {
                ffaConfig = YamlConfiguration.loadConfiguration((File)ffaFile);
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("[FFAManager] Could not load ffa-arenas.yml for per-arena config: " + e.getMessage());
        }
        ArrayList<DuelArena> ffaArenaList = new ArrayList<DuelArena>(this.arenaManager.getAllFFAArenas());
        this.plugin.getLogger().info("[FFAManager] Loading " + ffaArenaList.size() + " FFA arenas from ArenaManager...");
        for (DuelArena arena : ffaArenaList) {
            this.plugin.getLogger().info("[FFAManager] Processing arena: '" + arena.getName() + "' | enabled=" + arena.isEnabled() + " | linkedKit='" + arena.getLinkedKit() + "'");
            if (!arena.isEnabled()) {
                this.plugin.getLogger().info("[FFAManager]   \u2192 Skipping (disabled)");
                continue;
            }
            if (arena.getLinkedKit() == null || arena.getLinkedKit().isEmpty()) {
                this.plugin.getLogger().warning("[FFAManager]   \u2192 Skipping '" + arena.getName() + "' - NO linked kit configured!");
                this.plugin.getLogger().warning("[FFAManager]     Fix: /ffaadmin setkit " + arena.getName() + " <kitName>");
                continue;
            }
            String linkedKit = arena.getLinkedKit();
            DuelKit kit = this.kitManager.getAdminKit(linkedKit);
            if (kit == null) {
                this.plugin.getLogger().severe("[FFAManager]   \u2192 CRITICAL: Arena '" + arena.getName() + "' has linked kit '" + linkedKit + "' but that kit does NOT exist!");
                this.plugin.getLogger().severe("[FFAManager]     Available kits: " + String.valueOf(this.kitManager.getAdminKitNames()));
                this.plugin.getLogger().severe("[FFAManager]     Fix: Create kit '" + linkedKit + "' or relink the arena to an existing kit");
            } else {
                this.plugin.getLogger().info("[FFAManager]   \u2192 Kit '" + linkedKit + "' verified \u2713 | spawnPoints=" + arena.getSpawnPoints().size());
            }
            FFAArenaInstance instance = new FFAArenaInstance(arena.getName(), arena, linkedKit);
            if (ffaConfig != null) {
                String arenaKey = this.findArenaConfigKey((FileConfiguration)ffaConfig, arena.getName());
                if (arenaKey != null) {
                    String krKey;
                    this.plugin.getLogger().info("[FFAManager]   \u2192 Loading per-arena config from key: " + arenaKey);
                    if (ffaConfig.contains(arenaKey + ".max-players")) {
                        int maxP = ffaConfig.getInt(arenaKey + ".max-players", 50);
                        instance.setMaxPlayers(maxP);
                        this.plugin.getLogger().info("[FFAManager]     max-players=" + maxP);
                    }
                    if (ffaConfig.contains(arenaKey + ".enabled")) {
                        boolean ena = ffaConfig.getBoolean(arenaKey + ".enabled", true);
                        instance.setEnabled(ena);
                    }
                    if (ffaConfig.contains(krKey = arenaKey + ".kill-rewards")) {
                        boolean arenaHealOnKill = ffaConfig.getBoolean(krKey + ".heal-on-kill", this.healOnKill);
                        int arenaHealAmount = ffaConfig.getInt(krKey + ".heal-amount", this.healOnKillAmount);
                        boolean arenaRekit = ffaConfig.getBoolean(krKey + ".rekit-on-kill", this.rekitOnKill);
                        boolean arenaClearEffects = ffaConfig.getBoolean(krKey + ".clear-effects-on-kill", this.clearEffectsOnKill);
                        boolean arenaGiveGapple = ffaConfig.getBoolean(krKey + ".give-golden-apple", this.giveGoldenAppleOnKill);
                        int arenaGappleAmount = ffaConfig.getInt(krKey + ".golden-apples-amount", this.goldenApplesOnKill);
                        instance.setKillRewardSettings(arenaHealOnKill, arenaHealAmount, arenaRekit, arenaClearEffects, arenaGiveGapple, arenaGappleAmount);
                        this.plugin.getLogger().info("[FFAManager]     kill-rewards loaded: heal=" + arenaHealOnKill + " amount=" + arenaHealAmount + " rekit=" + arenaRekit + " clearFx=" + arenaClearEffects + " gapple=" + arenaGiveGapple + "x" + arenaGappleAmount);
                    }
                    if (ffaConfig.contains(arenaKey + ".spawn-protection")) {
                        int spawnProtSec = ffaConfig.getInt(arenaKey + ".spawn-protection", this.spawnProtectionTicks / 20);
                        instance.setSpawnProtectionSeconds(spawnProtSec);
                    }
                    if (ffaConfig.contains(arenaKey + ".min-players-for-stats")) {
                        int minP = ffaConfig.getInt(arenaKey + ".min-players-for-stats", this.minPlayersForStats);
                        instance.setMinPlayersForStats(minP);
                    }
                    if (ffaConfig.contains(arenaKey + ".announce-kills")) {
                        instance.setAnnounceKills(ffaConfig.getBoolean(arenaKey + ".announce-kills", this.announceKills));
                    }
                } else {
                    this.plugin.getLogger().info("[FFAManager]   \u2192 No per-arena config found in ffa-arenas.yml for: " + arena.getName());
                }
            }
            this.ffaArenas.put(arena.getName().toLowerCase(), instance);
            this.plugin.getLogger().info("[FFAManager]   \u2192 Loaded FFA arena '" + arena.getName() + "' successfully | maxPlayers=" + instance.getMaxPlayers());
        }
        this.plugin.getLogger().info("[FFAManager] Finished loading: " + this.ffaArenas.size() + "/" + ffaArenaList.size() + " FFA arenas active");
        if (this.ffaArenas.isEmpty() && !ffaArenaList.isEmpty()) {
            this.plugin.getLogger().severe("[FFAManager] WARNING: No FFA arenas loaded despite " + ffaArenaList.size() + " being configured!");
            this.plugin.getLogger().severe("[FFAManager] Check ffa-arenas.yml and ensure arenas have enabled=true and linkedKit set.");
        }
    }

    @Nullable
    private String findArenaConfigKey(@Nonnull FileConfiguration ffaConfig, @Nonnull String arenaName) {
        ConfigurationSection arenasSection = ffaConfig.getConfigurationSection("arenas");
        if (arenasSection == null) {
            return null;
        }
        String nameLower = arenaName.toLowerCase();
        if (arenasSection.contains(arenaName)) {
            return "arenas." + arenaName;
        }
        String withFfa = nameLower + "-ffa";
        if (arenasSection.contains(withFfa)) {
            return "arenas." + withFfa;
        }
        for (String key : arenasSection.getKeys(false)) {
            if (!key.toLowerCase().startsWith(nameLower) && !nameLower.startsWith(key.toLowerCase())) continue;
            return "arenas." + key;
        }
        return null;
    }

    private void startCleanupTask() {
        this.cleanupTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            this.cleanupOfflinePlayers();
            this.cleanupExpiredCombatTags();
        }, 600L, 600L);
    }

    private void startRegenerationTask() {
        if (!this.autoRegenerateArenas) {
            return;
        }
        this.regenerationTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.checkAndRegenerateArenas(), 100L, 100L);
    }

    private void checkAndRegenerateArenas() {
        if (!this.autoRegenerateArenas) {
            return;
        }
        long now = System.currentTimeMillis();
        long delayMs = (long)this.regenerationDelaySeconds * 1000L;
        Iterator<Map.Entry<String, Long>> iterator = this.pendingRegeneration.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String arenaName = entry.getKey();
            long emptyTime = entry.getValue();
            if (now - emptyTime < delayMs) continue;
            FFAArenaInstance arena = this.ffaArenas.get(arenaName);
            if (arena != null && arena.getPlayerCount() == 0) {
                this.regenerateArena(arenaName);
            }
            iterator.remove();
        }
    }

    public void regenerateArena(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return;
        }
        DuelArena duelArena = arena.getArena();
        if (duelArena == null) {
            return;
        }
        if (arena.getPlayerCount() > 0) {
            this.plugin.getLogger().warning("[FFAManager] Cannot regenerate arena " + arenaName + " - still has " + arena.getPlayerCount() + " players!");
            return;
        }
        this.plugin.getLogger().info("[FFAManager] Regenerating FFA arena: " + arenaName);
        SchematicManager schematicManager = this.plugin.getSchematicManager();
        if (schematicManager == null) {
            this.plugin.getLogger().warning("[FFAManager] SchematicManager not available for regeneration!");
            return;
        }
        ((CompletableFuture)schematicManager.restoreArena(duelArena.getName()).thenAccept(success -> {
            if (success.booleanValue()) {
                this.plugin.getLogger().info("[FFAManager] Successfully regenerated FFA arena: " + arenaName);
                arena.markRegenerated();
            } else {
                this.plugin.getLogger().warning("[FFAManager] Failed to regenerate FFA arena: " + arenaName + " - schematic may not exist");
            }
        })).exceptionally(ex -> {
            this.plugin.getLogger().severe("[FFAManager] Error regenerating arena " + arenaName + ": " + ex.getMessage());
            return null;
        });
    }

    private void scheduleArenaRegeneration(@Nonnull String arenaName) {
        if (!this.autoRegenerateArenas) {
            return;
        }
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null || arena.getPlayerCount() > 0) {
            return;
        }
        this.pendingRegeneration.put(arenaName.toLowerCase(), System.currentTimeMillis());
        this.plugin.getLogger().info("[FFAManager] Arena " + arenaName + " is now empty, scheduled for regeneration in " + this.regenerationDelaySeconds + " seconds");
    }

    private void cancelArenaRegeneration(@Nonnull String arenaName) {
        if (this.pendingRegeneration.remove(arenaName.toLowerCase()) != null) {
            this.plugin.debug("[FFAManager] Cancelled pending regeneration for arena: " + arenaName);
        }
    }

    public FFAResult joinByKit(@Nonnull Player player, @Nonnull String kitName) {
        FFAArenaInstance arena = this.findArenaForKit(kitName);
        if (arena == null) {
            return new FFAResult(false, FFAResultType.NO_ARENA, "\u00a7cNo FFA arena exists for the kit \u00a7e" + kitName + "\u00a7c!");
        }
        return this.join(player, arena.getArenaName());
    }

    public FFAResult join(@Nonnull Player player, @Nonnull String arenaName) {
        UUID uuid = player.getUniqueId();
        WorldRestrictionManager worldRestriction = this.plugin.getWorldRestrictionManager();
        if (worldRestriction != null && !worldRestriction.canJoinFFA(player)) {
            worldRestriction.sendFFABlockedMessage(player);
            return new FFAResult(false, FFAResultType.WORLD_RESTRICTED, this.plugin.getLanguageManager().getRaw(player, "world-restriction.ffa-blocked"));
        }
        if (!this.ffaEnabled) {
            return new FFAResult(false, FFAResultType.DISABLED, "\u00a7cFFA is currently disabled!");
        }
        if (this.isInFFA(uuid)) {
            return new FFAResult(false, FFAResultType.ALREADY_IN_FFA, "\u00a7cYou are already in an FFA arena! Use \u00a7e/ffa leave \u00a7cto leave first.");
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(uuid)) {
            return new FFAResult(false, FFAResultType.IN_MATCH, "\u00a7cYou cannot join FFA while in a duel!");
        }
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager != null && queueManager.isInQueue(uuid)) {
            return new FFAResult(false, FFAResultType.IN_QUEUE, "\u00a7cYou cannot join FFA while in a queue!");
        }
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return new FFAResult(false, FFAResultType.INVALID_ARENA, "\u00a7cThe FFA arena \u00a7e" + arenaName + " \u00a7cdoes not exist!");
        }
        if (!arena.isEnabled()) {
            return new FFAResult(false, FFAResultType.ARENA_DISABLED, "\u00a7cThis FFA arena is currently disabled!");
        }
        if (this.cooldownManager != null && this.cooldownManager.isOnCooldown(player, "ffa_join")) {
            String remaining = this.cooldownManager.getFormattedRemainingCooldown(player, "ffa_join");
            return new FFAResult(false, FFAResultType.ON_COOLDOWN, "\u00a7cYou must wait \u00a7e" + remaining + " \u00a7cbefore joining FFA again!");
        }
        DuelKit kit = this.kitManager.getAdminKit(arena.getKitName());
        if (kit == null) {
            this.plugin.getLogger().severe("[FFAManager] Kit '" + arena.getKitName() + "' not found for arena '" + arenaName + "'!");
            return new FFAResult(false, FFAResultType.INVALID_KIT, "\u00a7cThe kit \u00a7e" + arena.getKitName() + "\u00a7c for this FFA arena does not exist!");
        }
        FFAPlayerData playerData = new FFAPlayerData(uuid, player.getName(), arenaName);
        this.playerDataMap.put(uuid, playerData);
        this.playerArenaMap.put(uuid, arenaName.toLowerCase());
        arena.addPlayer(uuid);
        this.cancelArenaRegeneration(arenaName);
        DuelArena duelArena = arena.getArena();
        if (duelArena.getSpawnPoints().isEmpty()) {
            this.playerDataMap.remove(uuid);
            this.playerArenaMap.remove(uuid);
            arena.removePlayer(uuid);
            this.plugin.getLogger().severe("[FFAManager] Arena '" + arenaName + "' has NO spawn points!");
            arena.setEnabled(false);
            this.plugin.getArenaManager().saveFFAArenas();
            MessageUtils.sendMessage(player, "&c&lERROR: &7This arena is misconfigured!");
            MessageUtils.sendMessage(player, "&7No spawn points available. Contact an admin.");
            MessageUtils.sendMessage(player, "&7This arena has been automatically disabled.");
            return new FFAResult(false, FFAResultType.NO_SPAWN_POINTS, "\u00a7cArena has no spawn points!");
        }
        Location spawn = duelArena.getRandomSpawnPoint();
        if (spawn == null && !duelArena.getSpawnPoints().isEmpty()) {
            spawn = duelArena.getSpawnPoints().get(0).getLocation();
        }
        if (spawn == null) {
            this.playerDataMap.remove(uuid);
            this.playerArenaMap.remove(uuid);
            arena.removePlayer(uuid);
            MessageUtils.sendMessage(player, "&cFailed to get spawn location!");
            return new FFAResult(false, FFAResultType.NO_SPAWN_POINTS, "\u00a7cFailed to get spawn location!");
        }
        if (spawn.getWorld() == null) {
            this.playerDataMap.remove(uuid);
            this.playerArenaMap.remove(uuid);
            arena.removePlayer(uuid);
            MessageUtils.sendMessage(player, "&cSpawn world is not loaded!");
            return new FFAResult(false, FFAResultType.TELEPORT_FAILED, "\u00a7cSpawn world not loaded!");
        }
        this.playerStateManager.saveState(player);
        this.playerStateManager.clearPlayer(player);
        this.plugin.getLobbyManager().removeFromLobby(player);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SATURATION);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        Location finalSpawn = spawn.clone();
        try {
            boolean teleported = player.teleport(finalSpawn);
            if (!teleported) {
                this.playerDataMap.remove(uuid);
                this.playerArenaMap.remove(uuid);
                arena.removePlayer(uuid);
                this.playerStateManager.restoreState(player);
                this.plugin.getLogger().warning("[FFAManager] Teleport returned FALSE for " + player.getName() + " to arena: " + arenaName);
                MessageUtils.sendMessage(player, "&cFailed to teleport to arena!");
                return new FFAResult(false, FFAResultType.TELEPORT_FAILED, "\u00a7cTeleport failed!");
            }
            this.plugin.debug("[FFAManager] Teleported " + player.getName() + " to FFA arena: " + arenaName);
        }
        catch (Exception e) {
            this.playerDataMap.remove(uuid);
            this.playerArenaMap.remove(uuid);
            arena.removePlayer(uuid);
            this.playerStateManager.restoreState(player);
            this.plugin.getLogger().log(Level.SEVERE, "[FFAManager] Exception during FFA teleport for " + player.getName(), e);
            MessageUtils.sendMessage(player, "&cTeleport error!");
            return new FFAResult(false, FFAResultType.TELEPORT_FAILED, "\u00a7cTeleport error!");
        }
        DuelKit finalKit = kit;
        String finalArenaName = arenaName;
        String finalKitName = arena.getKitName();
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline() || !this.isInFFA(uuid)) {
                return;
            }
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            this.kitManager.applyKit(player, finalKit, true);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setWalkSpeed(0.2f);
            this.plugin.debug("[FFAManager] Kit pass 1: Applied '" + finalKitName + "' to " + player.getName());
        }, 2L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            HealthDisplayManager healthDisplayManager;
            if (!player.isOnline() || !this.isInFFA(uuid)) {
                return;
            }
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            this.kitManager.applyKit(player, finalKit, true);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            this.plugin.debug("[FFAManager] Kit pass 2: Re-enforced '" + finalKitName + "' on " + player.getName());
            if (this.spawnProtectionEnabled) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (player.isOnline() && this.isInFFA(uuid)) {
                        this.applySpawnProtection(player);
                    }
                }, 1L);
            }
            if (this.scoreboardManager != null) {
                this.scoreboardManager.refreshScoreboardType(player);
            }
            if ((healthDisplayManager = this.plugin.getHealthDisplayManager()) != null) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (player.isOnline() && this.isInFFA(uuid)) {
                        healthDisplayManager.createDisplayForFFA(player);
                    }
                }, 2L);
            }
        }, 10L);
        if (this.cooldownManager != null) {
            this.cooldownManager.setCooldown(player, "ffa_join");
        }
        player.sendMessage("");
        player.sendMessage("\u00a7a\u00a7lJOINED FFA!");
        player.sendMessage("\u00a77Arena: \u00a7e" + arena.getArenaName());
        player.sendMessage("\u00a77Kit: \u00a7e" + arena.getKitName());
        player.sendMessage("\u00a77Players: \u00a7e" + arena.getPlayerCount());
        player.sendMessage("");
        player.sendMessage("\u00a77Use \u00a7e/ffa leave \u00a77to leave the arena.");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        arena.broadcast("\u00a7e" + player.getName() + " \u00a77joined the arena! \u00a78(\u00a7e" + arena.getPlayerCount() + "\u00a78)");
        this.plugin.getLogger().fine("[FFAManager] " + player.getName() + " joined FFA arena '" + arenaName + "' with kit '" + arena.getKitName() + "'");
        return new FFAResult(true, FFAResultType.SUCCESS, "\u00a7aYou joined the \u00a7e" + arena.getKitName() + " \u00a7aFFA arena!");
    }

    public FFAResult leave(@Nonnull Player player) {
        return this.leave(player, true);
    }

    public FFAResult leave(@Nonnull Player player, boolean notify) {
        FFAArenaInstance arena;
        UUID uuid = player.getUniqueId();
        if (!this.isInFFA(uuid)) {
            return new FFAResult(false, FFAResultType.NOT_IN_FFA, "\u00a7cYou are not in an FFA arena!");
        }
        String arenaName = this.playerArenaMap.remove(uuid);
        FFAPlayerData playerData = this.playerDataMap.remove(uuid);
        this.combatTracking.remove(uuid);
        if (arenaName != null && (arena = this.ffaArenas.get(arenaName)) != null) {
            arena.removePlayer(uuid);
            if (notify) {
                arena.broadcast("\u00a7e" + player.getName() + " \u00a77left the arena! \u00a78(\u00a7e" + arena.getPlayerCount() + "\u00a78)");
            }
            if (arena.getPlayerCount() == 0) {
                this.scheduleArenaRegeneration(arenaName);
            }
        }
        this.removeSpawnProtection(player);
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.removeDisplayForFFA(player);
        }
        this.plugin.getLobbyManager().sendToLobby(player, true, true);
        if (this.scoreboardManager != null) {
            this.scoreboardManager.refreshScoreboardType(player);
        }
        if (notify) {
            player.sendMessage("\u00a7aYou left the FFA arena.");
            if (playerData != null && (playerData.getKills() > 0 || playerData.getDeaths() > 0)) {
                player.sendMessage("\u00a77Session Stats: \u00a7e" + playerData.getKills() + " kills \u00a77/ \u00a7e" + playerData.getDeaths() + " deaths \u00a77(\u00a7e" + String.format("%.2f", playerData.getKDRatio()) + " K/D\u00a77)");
            }
        }
        this.plugin.getLogger().fine("Player " + player.getName() + " left FFA");
        return new FFAResult(true, FFAResultType.SUCCESS, "\u00a7aYou left the FFA arena.");
    }

    public void removePlayer(@Nonnull UUID uuid) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player != null) {
            this.leave(player, false);
        } else {
            FFAArenaInstance arena;
            String arenaName = this.playerArenaMap.remove(uuid);
            this.playerDataMap.remove(uuid);
            this.combatTracking.remove(uuid);
            if (arenaName != null && (arena = this.ffaArenas.get(arenaName)) != null) {
                arena.removePlayer(uuid);
                if (arena.getPlayerCount() == 0) {
                    this.scheduleArenaRegeneration(arenaName);
                }
            }
        }
    }

    public void handleDeath(@Nonnull Player victim, @Nullable Player killer) {
        FFAPlayerData killerData;
        UUID victimUUID = victim.getUniqueId();
        if (!this.isInFFA(victimUUID)) {
            return;
        }
        String arenaName = this.playerArenaMap.get(victimUUID);
        FFAArenaInstance arena = this.ffaArenas.get(arenaName);
        FFAPlayerData victimData = this.playerDataMap.get(victimUUID);
        if (arena == null || victimData == null) {
            return;
        }
        victimData.incrementDeaths();
        UUID killerUUID = null;
        if (killer != null && this.isInFFA(killer.getUniqueId())) {
            killerUUID = killer.getUniqueId();
        } else {
            CombatData combatData = this.combatTracking.get(victimUUID);
            if (combatData != null && !combatData.isExpired((long)this.combatTagSeconds * 1000L)) {
                killerUUID = combatData.getLastAttacker();
            }
        }
        if (killerUUID != null && (killerData = this.playerDataMap.get(killerUUID)) != null) {
            killerData.incrementKills();
            killerData.incrementKillstreak();
            victimData.resetKillstreak();
            Player killerPlayer = Bukkit.getPlayer((UUID)killerUUID);
            if (killerPlayer != null) {
                HealthDisplayManager healthDisplayManager;
                if (this.playerDataManager != null && arena.getPlayerCount() >= this.minPlayersForStats) {
                    this.playerDataManager.addKills(killerUUID, arena.getKitName(), 1);
                }
                if (this.killRewardsEnabled) {
                    this.applyKillRewards(killerPlayer, victim.getName(), arena, killerData.getKillstreak());
                } else {
                    killerPlayer.sendMessage("\u00a7aYou killed \u00a7e" + victim.getName() + "\u00a7a! \u00a77(\u00a7e" + killerData.getKillstreak() + " \u00a77killstreak)");
                    killerPlayer.playSound(killerPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                }
                this.announceKillstreak(arena, killerPlayer, killerData.getKillstreak());
                if (this.scoreboardManager != null) {
                    this.scoreboardManager.updateScoreboard(killerPlayer);
                }
                if ((healthDisplayManager = this.plugin.getHealthDisplayManager()) != null) {
                    healthDisplayManager.updateHealth(killerPlayer);
                }
            }
        }
        if (this.playerDataManager != null && arena.getPlayerCount() >= this.minPlayersForStats) {
            this.playerDataManager.addDeaths(victimUUID, arena.getKitName(), 1);
        }
        if (this.respawnShowDeathMessage) {
            String deathMessage = this.formatDeathMessage(victim, killerUUID != null ? Bukkit.getPlayer((UUID)killerUUID) : null);
            arena.broadcast(deathMessage);
        }
        this.combatTracking.remove(victimUUID);
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.removeDisplayForFFA(victim);
        }
        this.handleDeathRespawn(victim, victimData, arena);
    }

    private void applyKillRewards(@Nonnull Player killer, @Nonnull String victimName, @Nonnull FFAArenaInstance arena, int killstreak) {
        DuelKit kit;
        boolean effectiveHealOnKill = arena.getArenaHealOnKill() != null ? arena.getArenaHealOnKill() : this.healOnKill;
        int effectiveHealAmount = arena.getArenaHealAmount() != null ? arena.getArenaHealAmount() : this.healOnKillAmount;
        boolean effectiveRekit = arena.getArenaRekitOnKill() != null ? arena.getArenaRekitOnKill() : this.rekitOnKill;
        boolean effectiveClearEffects = arena.getArenaClearEffectsOnKill() != null ? arena.getArenaClearEffectsOnKill() : this.clearEffectsOnKill;
        boolean effectiveGiveGapple = arena.getArenaGiveGoldenApple() != null ? arena.getArenaGiveGoldenApple() : this.giveGoldenAppleOnKill;
        int effectiveGappleAmount = arena.getArenaGoldenApplesAmount() != null ? arena.getArenaGoldenApplesAmount() : this.goldenApplesOnKill;
        ArrayList<Object> rewardsList = new ArrayList<Object>();
        if (effectiveClearEffects) {
            killer.getActivePotionEffects().forEach(effect -> killer.removePotionEffect(effect.getType()));
        }
        if (effectiveRekit && (kit = this.kitManager.getAdminKit(arena.getKitName())) != null) {
            this.kitManager.applyKit(killer, kit, true);
            rewardsList.add("\u00a7b\u2694 Rekit");
        }
        killer.setWalkSpeed(0.2f);
        killer.setFlySpeed(0.1f);
        killer.removePotionEffect(PotionEffectType.SPEED);
        if (effectiveHealOnKill) {
            if (effectiveHealAmount < 0) {
                killer.setHealth(killer.getMaxHealth());
                killer.setFoodLevel(20);
                killer.setSaturation(20.0f);
                rewardsList.add("\u00a7c\u2764 Full Heal");
            } else if (effectiveHealAmount > 0) {
                double newHealth = Math.min(killer.getHealth() + (double)effectiveHealAmount, killer.getMaxHealth());
                killer.setHealth(newHealth);
                int newFood = Math.min(killer.getFoodLevel() + 4, 20);
                killer.setFoodLevel(newFood);
                rewardsList.add("\u00a7c\u2764 +" + effectiveHealAmount / 2 + " Hearts");
            }
        }
        if (effectiveGiveGapple && effectiveGappleAmount > 0) {
            ItemStack gapple = new ItemStack(Material.GOLDEN_APPLE, effectiveGappleAmount);
            killer.getInventory().addItem(new ItemStack[]{gapple});
            rewardsList.add("\u00a76\ud83c\udf4e +" + effectiveGappleAmount + " Gapple" + (effectiveGappleAmount > 1 ? "s" : ""));
        }
        if (this.playKillSound) {
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        }
        StringBuilder message = new StringBuilder();
        message.append("\u00a7aYou killed \u00a7e").append(victimName).append("\u00a7a!");
        message.append(" \u00a77(\u00a7e").append(killstreak).append(" \u00a77killstreak)");
        if (!rewardsList.isEmpty()) {
            message.append(" \u00a78[");
            message.append(String.join((CharSequence)" \u00a77| ", rewardsList));
            message.append("\u00a78]");
        }
        killer.sendMessage(message.toString());
    }

    private void handleDeathRespawn(@Nonnull Player victim, @Nonnull FFAPlayerData victimData, @Nonnull FFAArenaInstance arena) {
        UUID victimUUID = victim.getUniqueId();
        if (this.respawnTeleportToLobby) {
            if (this.respawnDelaySeconds <= 0) {
                this.showDeathTitle(victim, true);
                this.handleLobbyTeleport(victim, victimData);
            } else {
                this.showDeathTitle(victim, true);
                victim.sendMessage("\u00a7cYou died! \u00a77Returning to lobby in \u00a7e" + this.respawnDelaySeconds + " \u00a77seconds...");
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (victim.isOnline() && this.isInFFA(victimUUID)) {
                        this.handleLobbyTeleport(victim, victimData);
                    }
                }, (long)this.respawnDelaySeconds * 20L);
            }
        } else {
            this.showDeathTitle(victim, false);
            int respawnTicks = this.respawnDelaySeconds > 0 ? this.respawnDelaySeconds * 20 : 60;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (victim.isOnline() && this.isInFFA(victimUUID)) {
                    this.handleArenaRespawn(victim, arena);
                }
            }, (long)respawnTicks);
        }
    }

    private void handleLobbyTeleport(@Nonnull Player player, @Nonnull FFAPlayerData playerData) {
        FFAArenaInstance arena;
        UUID uuid = player.getUniqueId();
        if (!this.isInFFA(uuid)) {
            return;
        }
        int kills = playerData.getKills();
        int deaths = playerData.getDeaths();
        double kd = playerData.getKDRatio();
        String arenaName = this.playerArenaMap.remove(uuid);
        this.playerDataMap.remove(uuid);
        this.combatTracking.remove(uuid);
        if (arenaName != null && (arena = this.ffaArenas.get(arenaName)) != null) {
            arena.removePlayer(uuid);
            if (arena.getPlayerCount() == 0) {
                this.scheduleArenaRegeneration(arenaName);
            }
        }
        this.removeSpawnProtection(player);
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.removeDisplayForFFA(player);
        }
        this.plugin.getLobbyManager().sendToLobby(player, true, true);
        if (this.scoreboardManager != null) {
            this.scoreboardManager.refreshScoreboardType(player);
        }
        player.sendMessage("");
        player.sendMessage("\u00a7c\u00a7lYOU DIED!");
        player.sendMessage("");
        if (kills > 0 || deaths > 0) {
            player.sendMessage("\u00a77Session Stats: \u00a7e" + kills + " kills \u00a77/ \u00a7e" + deaths + " deaths \u00a77(\u00a7e" + String.format("%.2f", kd) + " K/D\u00a77)");
        }
        player.sendMessage("\u00a77Use \u00a7e/ffa <kit> \u00a77to rejoin!");
        player.sendMessage("");
        this.plugin.getLogger().fine("Player " + player.getName() + " died in FFA and was sent to lobby");
    }

    private void handleArenaRespawn(@Nonnull Player player, @Nonnull FFAArenaInstance arena) {
        UUID uuid = player.getUniqueId();
        if (!this.isInFFA(uuid)) {
            return;
        }
        DuelKit kit = this.kitManager.getAdminKit(arena.getKitName());
        if (kit == null) {
            this.plugin.getLogger().severe("[FFAManager] Kit '" + arena.getKitName() + "' not found during respawn for " + player.getName() + "!");
            this.leave(player, true);
            player.sendMessage("\u00a7cError: Kit not found. Removed from FFA.");
            return;
        }
        this.playerStateManager.clearPlayer(player);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SATURATION);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        Location spawn = arena.getArena().getRandomSpawnPoint();
        if (spawn == null) {
            spawn = arena.getArena().getSpawnPoint1();
        }
        if (spawn == null) {
            this.plugin.getLogger().severe("[FFAManager] No spawn point for respawn in arena: " + arena.getArenaName());
            this.leave(player, true);
            player.sendMessage("\u00a7cError: No spawn point. Removed from FFA.");
            return;
        }
        player.teleport(spawn);
        DuelKit finalKit = kit;
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline() || !this.isInFFA(uuid)) {
                return;
            }
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            this.kitManager.applyKit(player, finalKit, true);
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            this.plugin.debug("[FFAManager] Respawn kit pass 1: Applied '" + arena.getKitName() + "' to " + player.getName());
        }, 2L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            HealthDisplayManager healthDisplayManager;
            if (!player.isOnline() || !this.isInFFA(uuid)) {
                return;
            }
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            this.kitManager.applyKit(player, finalKit, true);
            this.plugin.debug("[FFAManager] Respawn kit pass 2: Re-enforced '" + arena.getKitName() + "' on " + player.getName());
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            if (this.spawnProtectionEnabled) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (player.isOnline() && this.isInFFA(uuid)) {
                        this.applySpawnProtection(player);
                    }
                }, 1L);
            }
            if ((healthDisplayManager = this.plugin.getHealthDisplayManager()) != null) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (player.isOnline() && this.isInFFA(uuid)) {
                        healthDisplayManager.createDisplayForFFA(player);
                    }
                }, 2L);
            }
            player.sendMessage("\u00a7aYou have respawned!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            if (this.scoreboardManager != null) {
                this.scoreboardManager.updateScoreboard(player);
            }
        }, 10L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline() || !this.isInFFA(uuid)) {
                return;
            }
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
        }, 11L);
    }

    public void handleDamage(@Nonnull Player victim, @Nonnull Player attacker) {
        if (!this.isInFFA(victim.getUniqueId()) || !this.isInFFA(attacker.getUniqueId())) {
            return;
        }
        CombatData combatData = this.combatTracking.computeIfAbsent(victim.getUniqueId(), k -> new CombatData(victim.getUniqueId()));
        combatData.setLastAttacker(attacker.getUniqueId());
        combatData.setLastDamageTime(System.currentTimeMillis());
        this.removeSpawnProtection(victim);
        this.removeSpawnProtection(attacker);
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.updateHealth(victim);
            healthDisplayManager.updateHealth(attacker);
        }
    }

    private void showDeathTitle(@Nonnull Player player, boolean goingToLobby) {
        String subtitle = goingToLobby ? "\u00a77Returning to lobby..." : "\u00a77Respawning...";
        Title title = Title.title((Component)Component.text((String)"\u00a7c\u00a7lYOU DIED"), (Component)Component.text((String)subtitle), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
    }

    @Nonnull
    private String formatDeathMessage(@Nonnull Player victim, @Nullable Player killer) {
        if (killer != null) {
            int killstreak;
            FFAPlayerData killerData = this.playerDataMap.get(killer.getUniqueId());
            int n = killstreak = killerData != null ? killerData.getKillstreak() : 1;
            if (killstreak >= 5) {
                return "\u00a7c" + victim.getName() + " \u00a77was slain by \u00a7a" + killer.getName() + " \u00a77(\u00a7e" + killstreak + " \u00a77killstreak)";
            }
            return "\u00a7c" + victim.getName() + " \u00a77was slain by \u00a7a" + killer.getName();
        }
        return "\u00a7c" + victim.getName() + " \u00a77died";
    }

    private void announceKillstreak(@Nonnull FFAArenaInstance arena, @Nonnull Player player, int streak) {
        if (streak == 5) {
            arena.broadcast("\u00a76\u00a7l" + player.getName() + " \u00a7eis on a \u00a765 kill \u00a7ekillstreak!");
        } else if (streak == 10) {
            arena.broadcast("\u00a7c\u00a7l" + player.getName() + " \u00a7eis on a \u00a7c10 kill \u00a7ekillstreak! \u00a76DOMINATING!");
        } else if (streak == 15) {
            arena.broadcast("\u00a74\u00a7l" + player.getName() + " \u00a7eis on a \u00a7415 kill \u00a7ekillstreak! \u00a74\u00a7lUNSTOPPABLE!");
        } else if (streak == 20) {
            arena.broadcast("\u00a75\u00a7l" + player.getName() + " \u00a7eis on a \u00a7520 kill \u00a7ekillstreak! \u00a75\u00a7lGODLIKE!");
        } else if (streak > 20 && streak % 5 == 0) {
            arena.broadcast("\u00a7d\u00a7l" + player.getName() + " \u00a7eis on a \u00a7d" + streak + " kill \u00a7ekillstreak! \u00a7d\u00a7lLEGENDARY!");
        }
    }

    private void applySpawnProtection(@Nonnull Player player) {
        FFAPlayerData data = this.playerDataMap.get(player.getUniqueId());
        if (data != null) {
            data.setSpawnProtected(true);
            player.setGlowing(true);
            player.sendMessage("\u00a7a\u00a7lSpawn Protection \u00a77active for \u00a7e" + this.spawnProtectionTicks / 20 + " seconds\u00a77. Attack to remove.");
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (player.isOnline() && this.isInFFA(player.getUniqueId())) {
                    this.removeSpawnProtection(player);
                }
            }, (long)this.spawnProtectionTicks);
        }
    }

    private void removeSpawnProtection(@Nonnull Player player) {
        FFAPlayerData data = this.playerDataMap.get(player.getUniqueId());
        if (data != null && data.isSpawnProtected()) {
            data.setSpawnProtected(false);
            player.setGlowing(false);
        }
    }

    public boolean hasSpawnProtection(@Nonnull UUID uuid) {
        FFAPlayerData data = this.playerDataMap.get(uuid);
        return data != null && data.isSpawnProtected();
    }

    public boolean isInFFA(@Nonnull UUID uuid) {
        return this.playerArenaMap.containsKey(uuid);
    }

    public boolean isInFFAArena(@Nonnull Player player) {
        return this.isInFFA(player.getUniqueId());
    }

    @Nullable
    public String getPlayerArena(@Nonnull UUID uuid) {
        return this.playerArenaMap.get(uuid);
    }

    @Nullable
    public String getPlayerArena(@Nonnull Player player) {
        return this.playerArenaMap.get(player.getUniqueId());
    }

    @Nullable
    public FFAPlayerData getPlayerData(@Nonnull UUID uuid) {
        return this.playerDataMap.get(uuid);
    }

    @Nullable
    public FFAArenaInstance getArena(@Nonnull String arenaName) {
        return this.ffaArenas.get(arenaName.toLowerCase());
    }

    @Nullable
    public FFAArenaInstance getFFAArena(@Nullable String arenaName) {
        if (arenaName == null) {
            return null;
        }
        return this.ffaArenas.get(arenaName.toLowerCase());
    }

    @Nullable
    public FFAArenaInstance findArenaForKit(@Nonnull String kitName) {
        for (FFAArenaInstance arena : this.ffaArenas.values()) {
            if (!arena.getKitName().equalsIgnoreCase(kitName)) continue;
            return arena;
        }
        return null;
    }

    @Nonnull
    public Collection<FFAArenaInstance> getAllArenas() {
        return Collections.unmodifiableCollection(this.ffaArenas.values());
    }

    public int getPlayerCount(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        return arena != null ? arena.getPlayerCount() : 0;
    }

    public int getTotalPlayers() {
        return this.playerArenaMap.size();
    }

    @Nonnull
    public Set<UUID> getPlayersInArena(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        return arena != null ? arena.getPlayers() : Collections.emptySet();
    }

    @Nonnull
    public List<FFAPlayerData> getTopPlayers(@Nonnull String arenaName, int limit) {
        Set<UUID> players = this.getPlayersInArena(arenaName);
        return players.stream().map(this.playerDataMap::get).filter(Objects::nonNull).sorted(Comparator.comparingInt(FFAPlayerData::getKills).reversed()).limit(limit).collect(Collectors.toList());
    }

    @Nonnull
    public FFAInfo getArenaInfo(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return new FFAInfo(arenaName, "Unknown", 0, false);
        }
        return new FFAInfo(arena.getArenaName(), arena.getKitName(), arena.getPlayerCount(), arena.isEnabled());
    }

    @Nonnull
    public List<FFAInfo> getAllArenaInfo() {
        return this.ffaArenas.values().stream().map(a -> new FFAInfo(a.getArenaName(), a.getKitName(), a.getPlayerCount(), a.isEnabled())).collect(Collectors.toList());
    }

    public boolean enableArena(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena != null) {
            arena.setEnabled(true);
            return true;
        }
        return false;
    }

    public boolean disableArena(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena != null) {
            arena.setEnabled(false);
            for (UUID uuid : new ArrayList<UUID>(arena.getPlayers())) {
                Player player = Bukkit.getPlayer((UUID)uuid);
                if (player == null) continue;
                this.leave(player, true);
                player.sendMessage("\u00a7cThe FFA arena has been disabled.");
            }
            return true;
        }
        return false;
    }

    public boolean forceRegenerateArena(@Nonnull String arenaName) {
        FFAArenaInstance arena = this.ffaArenas.get(arenaName.toLowerCase());
        if (arena == null) {
            return false;
        }
        for (UUID uuid : new ArrayList<UUID>(arena.getPlayers())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            this.leave(player, true);
            player.sendMessage("\u00a7cThe FFA arena is being regenerated.");
        }
        this.regenerateArena(arenaName);
        return true;
    }

    public void reloadArenas() {
        for (UUID uuid : new ArrayList<UUID>(this.playerArenaMap.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            this.leave(player, true);
            player.sendMessage("\u00a7cFFA arenas are being reloaded.");
        }
        this.pendingRegeneration.clear();
        this.initializeFFAArenas();
    }

    private void cleanupOfflinePlayers() {
        for (UUID uuid : new ArrayList<UUID>(this.playerArenaMap.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player != null && player.isOnline()) continue;
            this.removePlayer(uuid);
        }
    }

    private void cleanupExpiredCombatTags() {
        long now = System.currentTimeMillis();
        long expiry = (long)this.combatTagSeconds * 1000L;
        this.combatTracking.entrySet().removeIf(entry -> ((CombatData)entry.getValue()).isExpired(expiry));
    }

    public void reload() {
        this.loadSettings();
        this.reloadArenas();
        if (this.regenerationTask != null) {
            this.regenerationTask.cancel();
        }
        this.startRegenerationTask();
        this.plugin.getLogger().info("[FFAManager] Reloaded successfully!");
    }

    public void shutdown() {
        for (UUID uuid : new ArrayList<UUID>(this.playerArenaMap.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            this.leave(player, false);
            player.sendMessage("\u00a7cFFA has been shut down.");
        }
        if (this.cleanupTask != null) {
            this.cleanupTask.cancel();
        }
        if (this.regenerationTask != null) {
            this.regenerationTask.cancel();
        }
        this.ffaArenas.clear();
        this.playerArenaMap.clear();
        this.playerDataMap.clear();
        this.combatTracking.clear();
        this.pendingRegeneration.clear();
        this.plugin.getLogger().info("[FFAManager] Shutdown complete");
    }

    private static class CombatData {
        private final UUID playerUUID;
        private UUID lastAttacker;
        private long lastDamageTime;

        public CombatData(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        public UUID getLastAttacker() {
            return this.lastAttacker;
        }

        public void setLastAttacker(UUID lastAttacker) {
            this.lastAttacker = lastAttacker;
        }

        public void setLastDamageTime(long time) {
            this.lastDamageTime = time;
        }

        public boolean isExpired(long expiryMs) {
            return System.currentTimeMillis() - this.lastDamageTime > expiryMs;
        }
    }
}

