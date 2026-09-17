/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.SkullMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitTask
 */
package com.ultimateduels.lobby;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.config.LanguageManager;
import com.ultimateduels.player.PlayerStateManager;
import com.ultimateduels.utils.TextUtil;
import com.ultimateduels.world.WorldRestrictionManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class LobbyManager {
    private final UltimateDuels plugin;
    private final PlayerStateManager playerStateManager;
    private Location lobbySpawn;
    private String lobbyWorldName;
    private World lobbyWorld;
    private final Map<Integer, LobbyItem> hotbarItems;
    private final NamespacedKey lobbyItemKey;
    private final Set<UUID> playersInLobby;
    private boolean lobbyEnabled;
    private boolean teleportOnJoin;
    private boolean giveItemsOnJoin;
    private boolean firstJoinTeleport;
    private boolean worldRestriction;
    private boolean pvpEnabled;
    private boolean hungerEnabled;
    private boolean fallDamageEnabled;
    private boolean damageEnabled;
    private boolean blockBreakEnabled;
    private boolean blockPlaceEnabled;
    private boolean itemDropEnabled;
    private boolean itemPickupEnabled;
    private boolean fixedWeather;
    private String weatherType;
    private boolean fixedTime;
    private long lobbyTime;
    private boolean hotbarEnabled;
    private boolean lockHotbar;
    private boolean clearInventory;
    private final List<PotionEffect> lobbyEffects;
    private File lobbyFile;
    private FileConfiguration lobbyConfig;
    private BukkitTask effectTask;
    public static final int SLOT_QUEUE_MENU = 0;
    public static final int SLOT_PARTY_MENU = 1;
    public static final int SLOT_KIT_EDITOR = 2;
    public static final int SLOT_FFA_MENU = 4;
    public static final int SLOT_STATS = 6;
    public static final int SLOT_SPECTATE = 7;
    public static final int SLOT_SETTINGS = 8;

    public LobbyManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.playerStateManager = plugin.getPlayerStateManager();
        this.hotbarItems = new ConcurrentHashMap<Integer, LobbyItem>();
        this.lobbyItemKey = new NamespacedKey((Plugin)plugin, "lobby_item");
        this.playersInLobby = ConcurrentHashMap.newKeySet();
        this.lobbyEffects = new ArrayList<PotionEffect>();
        this.initializeStorage();
        this.loadConfiguration();
        this.loadHotbarItemsFromConfig();
        this.startEffectTask();
        plugin.getLogger().info("\u00a7a[LobbyManager] Initialized successfully!");
    }

    private void initializeStorage() {
        this.lobbyFile = new File(this.plugin.getDataFolder(), "lobby.yml");
        if (!this.lobbyFile.exists()) {
            try {
                this.plugin.saveResource("lobby.yml", false);
            }
            catch (Exception e) {
                this.plugin.getLogger().info("[LobbyManager] No lobby.yml resource found, using config.yml");
            }
        }
        this.lobbyConfig = this.lobbyFile.exists() ? YamlConfiguration.loadConfiguration((File)this.lobbyFile) : new YamlConfiguration();
    }

    private void loadConfiguration() {
        ConfigurationSection protectionSection;
        ConfigurationSection spawnSection;
        if (this.lobbyFile.exists()) {
            this.lobbyConfig = YamlConfiguration.loadConfiguration((File)this.lobbyFile);
        }
        FileConfiguration mainConfig = this.plugin.getConfig();
        this.lobbyEnabled = mainConfig.getBoolean("lobby.enabled", this.lobbyConfig.getBoolean("lobby.enabled", true));
        this.lobbyWorldName = mainConfig.getString("lobby.world", this.lobbyConfig.getString("lobby.world", "world"));
        this.teleportOnJoin = mainConfig.getBoolean("lobby.teleport-on-join", this.lobbyConfig.getBoolean("lobby.teleport-on-join", true));
        this.giveItemsOnJoin = mainConfig.getBoolean("lobby.give-items-on-join", this.lobbyConfig.getBoolean("lobby.give-items-on-join", true));
        this.firstJoinTeleport = mainConfig.getBoolean("lobby.first-join-teleport", this.lobbyConfig.getBoolean("lobby.first-join-teleport", true));
        this.worldRestriction = mainConfig.getBoolean("lobby.world-restriction", this.lobbyConfig.getBoolean("lobby.world-restriction", true));
        this.hotbarEnabled = mainConfig.getBoolean("hotbar-items.enabled", this.lobbyConfig.getBoolean("hotbar.enabled", true));
        this.lockHotbar = mainConfig.getBoolean("hotbar-items.lock-hotbar", this.lobbyConfig.getBoolean("hotbar.lock-hotbar", true));
        this.clearInventory = mainConfig.getBoolean("hotbar-items.clear-inventory", this.lobbyConfig.getBoolean("hotbar.clear-inventory", true));
        this.lobbySpawn = null;
        if (this.lobbyConfig.contains("lobby.spawn")) {
            ConfigurationSection spawnSection2;
            Object spawnData = this.lobbyConfig.get("lobby.spawn");
            if (spawnData instanceof String) {
                this.lobbySpawn = this.deserializeLocation((String)spawnData);
            } else if (this.lobbyConfig.isConfigurationSection("lobby.spawn") && (spawnSection2 = this.lobbyConfig.getConfigurationSection("lobby.spawn")) != null) {
                this.lobbySpawn = this.deserializeLocationFromSection(spawnSection2, this.lobbyWorldName);
            }
        }
        if (this.lobbySpawn == null && mainConfig.isConfigurationSection("lobby.spawn") && (spawnSection = mainConfig.getConfigurationSection("lobby.spawn")) != null) {
            this.lobbySpawn = this.deserializeLocationFromSection(spawnSection, this.lobbyWorldName);
        }
        this.lobbyWorld = Bukkit.getWorld((String)this.lobbyWorldName);
        if (this.lobbyWorld == null && this.lobbyEnabled) {
            this.plugin.getLogger().warning("\u00a7c[LobbyManager] Lobby world '" + this.lobbyWorldName + "' not found!");
        }
        if ((protectionSection = mainConfig.getConfigurationSection("lobby.protection")) != null) {
            this.pvpEnabled = !protectionSection.getBoolean("prevent-pvp", true);
            this.hungerEnabled = !protectionSection.getBoolean("prevent-hunger", true);
            this.fallDamageEnabled = !protectionSection.getBoolean("prevent-fall-damage", true);
            this.damageEnabled = !protectionSection.getBoolean("prevent-damage", true);
            this.blockBreakEnabled = !protectionSection.getBoolean("prevent-building", true);
            this.blockPlaceEnabled = !protectionSection.getBoolean("prevent-building", true);
            this.itemDropEnabled = !protectionSection.getBoolean("prevent-item-drop", true);
            this.itemPickupEnabled = !protectionSection.getBoolean("prevent-item-pickup", true);
            this.fixedWeather = protectionSection.getBoolean("fixed-weather", true);
            this.weatherType = protectionSection.getString("weather", "CLEAR");
            this.fixedTime = protectionSection.getBoolean("fixed-time", true);
            this.lobbyTime = protectionSection.getLong("time", 6000L);
        } else {
            this.pvpEnabled = false;
            this.hungerEnabled = false;
            this.fallDamageEnabled = false;
            this.damageEnabled = false;
            this.blockBreakEnabled = false;
            this.blockPlaceEnabled = false;
            this.itemDropEnabled = false;
            this.itemPickupEnabled = false;
            this.fixedWeather = true;
            this.weatherType = "CLEAR";
            this.fixedTime = true;
            this.lobbyTime = 6000L;
        }
        this.lobbyEffects.clear();
        List effectList = mainConfig.getStringList("lobby.effects");
        if (effectList.isEmpty()) {
            effectList = this.lobbyConfig.getStringList("lobby.effects");
        }
        for (String effectStr : effectList) {
            PotionEffect effect = this.parseEffect(effectStr);
            if (effect == null) continue;
            this.lobbyEffects.add(effect);
        }
        if (this.lobbyWorld != null) {
            if (this.fixedWeather) {
                this.lobbyWorld.setStorm(false);
                this.lobbyWorld.setThundering(false);
            }
            if (this.fixedTime) {
                this.lobbyWorld.setTime(this.lobbyTime);
            }
        }
        if (this.plugin.getConfigManager() != null && this.plugin.getConfigManager().isDebugMode()) {
            this.plugin.getLogger().info("\u00a7a[LobbyManager] Configuration loaded:");
            this.plugin.getLogger().info("\u00a77  - lobbyEnabled: " + this.lobbyEnabled);
            this.plugin.getLogger().info("\u00a77  - teleportOnJoin: " + this.teleportOnJoin);
            this.plugin.getLogger().info("\u00a77  - giveItemsOnJoin: " + this.giveItemsOnJoin);
            this.plugin.getLogger().info("\u00a77  - firstJoinTeleport: " + this.firstJoinTeleport);
            this.plugin.getLogger().info("\u00a77  - worldRestriction: " + this.worldRestriction);
            this.plugin.getLogger().info("\u00a77  - hotbarEnabled: " + this.hotbarEnabled);
            this.plugin.getLogger().info("\u00a77  - lobbyWorld: " + this.lobbyWorldName);
            this.plugin.getLogger().info("\u00a77  - lobbySpawn: " + (this.lobbySpawn != null ? "SET" : "NOT SET"));
        }
    }

    private void loadHotbarItemsFromConfig() {
        this.hotbarItems.clear();
        if (!this.hotbarEnabled) {
            this.plugin.getLogger().info("[LobbyManager] Hotbar items disabled");
            return;
        }
        FileConfiguration mainConfig = this.plugin.getConfig();
        ConfigurationSection hotbarSection = mainConfig.getConfigurationSection("hotbar-items.items");
        if (hotbarSection == null) {
            hotbarSection = this.lobbyConfig.getConfigurationSection("hotbar.items");
        }
        if (hotbarSection == null) {
            this.plugin.getLogger().info("[LobbyManager] No hotbar items configured, using defaults");
            this.createDefaultHotbarItems();
            return;
        }
        for (String key : hotbarSection.getKeys(false)) {
            Material material;
            ConfigurationSection itemSection = hotbarSection.getConfigurationSection(key);
            if (itemSection == null || !itemSection.getBoolean("enabled", true)) continue;
            int slot = itemSection.getInt("slot", 0);
            String materialName = itemSection.getString("material", "STONE");
            String name = itemSection.getString("name", "<white>Item");
            List lore = itemSection.getStringList("lore");
            boolean glow = itemSection.getBoolean("glow", itemSection.getBoolean("enchanted", false));
            String actionStr = itemSection.getString("action", key.toUpperCase());
            try {
                material = Material.valueOf((String)materialName.toUpperCase().replace(" ", "_"));
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("[LobbyManager] Invalid material: " + materialName + " for item: " + key);
                continue;
            }
            ItemStack item = this.createLobbyItem(material, name, lore, glow);
            LobbyItemAction action = this.getActionFromString(actionStr);
            if (action == null) {
                action = this.getActionFromKey(key);
            }
            this.hotbarItems.put(slot, new LobbyItem(item, action));
            if (this.plugin.getConfigManager() == null || !this.plugin.getConfigManager().isDebugMode()) continue;
            this.plugin.getLogger().info("\u00a77  - Loaded hotbar item: " + key + " at slot " + slot + " with action " + String.valueOf((Object)action));
        }
        this.plugin.getLogger().info("\u00a7a[LobbyManager] Loaded " + this.hotbarItems.size() + " hotbar items from config");
    }

    @Nullable
    private LobbyItemAction getActionFromString(String actionStr) {
        if (actionStr == null) {
            return null;
        }
        try {
            return LobbyItemAction.valueOf(actionStr.toUpperCase().replace("-", "_").replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LobbyItemAction getActionFromKey(String key) {
        return switch (key.toLowerCase().replace("-", "_").replace(" ", "_")) {
            case "queue", "duel", "queue_menu", "duel_menu" -> LobbyItemAction.QUEUE_MENU;
            case "party", "party_menu" -> LobbyItemAction.PARTY_MENU;
            case "kit_editor", "kiteditor", "kit", "kits" -> LobbyItemAction.KIT_EDITOR;
            case "ffa", "ffa_menu" -> LobbyItemAction.FFA_MENU;
            case "settings", "settings_menu" -> LobbyItemAction.SETTINGS_MENU;
            case "spectate", "spectate_menu" -> LobbyItemAction.SPECTATE_MENU;
            case "stats", "stats_menu", "statistics" -> LobbyItemAction.STATS_MENU;
            case "shop", "shop_menu", "store" -> LobbyItemAction.SHOP_MENU;
            case "leave", "hub", "leave_lobby", "back" -> LobbyItemAction.LEAVE_LOBBY;
            default -> LobbyItemAction.NONE;
        };
    }

    private void createDefaultHotbarItems() {
        this.hotbarItems.put(0, new LobbyItem(this.createLobbyItem(Material.DIAMOND_SWORD, "&b&lQueue Menu", Arrays.asList("", "&7Click to open the queue menu", "&7and find an opponent!", "", "&eClick to open!"), true), LobbyItemAction.QUEUE_MENU));
        this.hotbarItems.put(1, new LobbyItem(this.createLobbyItem(Material.SPYGLASS, "&d&lParty Menu", Arrays.asList("", "&7Create or manage your party", "&7Invite friends and duel together!", "", "&eClick to open!"), true), LobbyItemAction.PARTY_MENU));
        this.hotbarItems.put(2, new LobbyItem(this.createLobbyItem(Material.BOOK, "&a&lKit Editor", Arrays.asList("", "&7Customize your kits", "&7Arrange items to your preference!", "", "&eClick to open!"), true), LobbyItemAction.KIT_EDITOR));
        this.hotbarItems.put(4, new LobbyItem(this.createLobbyItem(Material.TOTEM_OF_UNDYING, "&c&lFFA Arenas", Arrays.asList("", "&7Join a Free For All arena", "&7Fight against everyone!", "", "&eClick to open!"), true), LobbyItemAction.FFA_MENU));
        this.hotbarItems.put(6, new LobbyItem(this.createLobbyItem(Material.PLAYER_HEAD, "&9&lStatistics", Arrays.asList("", "&7View your statistics", "&7Kills, Deaths, K/D and more!", "", "&eClick to view!"), false), LobbyItemAction.STATS_MENU));
        this.hotbarItems.put(7, new LobbyItem(this.createLobbyItem(Material.ENDER_EYE, "&3&lSpectate", Arrays.asList("", "&7Watch ongoing duels", "&7Learn from other players!", "", "&eClick to open!"), false), LobbyItemAction.SPECTATE_MENU));
        this.hotbarItems.put(8, new LobbyItem(this.createLobbyItem(Material.COMPARATOR, "&6&lSettings", Arrays.asList("", "&7Customize your experience", "&7Toggle various options!", "", "&eClick to open!"), true), LobbyItemAction.SETTINGS_MENU));
    }

    private ItemStack createLobbyItem(Material material, String name, List<String> lore, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component displayName = TextUtil.parse(name);
            meta.displayName(displayName);
            if (lore != null && !lore.isEmpty()) {
                ArrayList<Component> loreComponents = new ArrayList<Component>();
                for (String line : lore) {
                    loreComponents.add(TextUtil.parse(line));
                }
                meta.lore(loreComponents);
            }
            if (glow) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            }
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
            try {
                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
            }
            catch (IllegalArgumentException | NoSuchFieldError loreComponents) {
                // empty catch block
            }
            meta.setUnbreakable(true);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(this.lobbyItemKey, PersistentDataType.BYTE, (Object)1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void startEffectTask() {
        if (this.effectTask != null) {
            this.effectTask.cancel();
        }
        this.effectTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            for (UUID uuid : this.playersInLobby) {
                Player player = Bukkit.getPlayer((UUID)uuid);
                if (player == null || !player.isOnline()) continue;
                for (PotionEffect effect : this.lobbyEffects) {
                    player.addPotionEffect(effect);
                }
                if (this.hungerEnabled) continue;
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            }
            if (this.lobbyWorld != null) {
                if (this.fixedTime) {
                    this.lobbyWorld.setTime(this.lobbyTime);
                }
                if (this.fixedWeather) {
                    this.lobbyWorld.setStorm(false);
                    this.lobbyWorld.setThundering(false);
                }
            }
        }, 20L, 100L);
    }

    public void sendToLobby(@Nonnull Player player) {
        this.sendToLobby(player, true, false);
    }

    public void sendToLobby(@Nonnull Player player, boolean teleport) {
        this.sendToLobby(player, teleport, false);
    }

    public void sendToLobby(@Nonnull Player player, boolean teleport, boolean restoreState) {
        UUID uuid = player.getUniqueId();
        boolean alreadyInLobbyWorld = this.isInLobbyWorld(player);
        if (restoreState && this.playerStateManager != null && this.playerStateManager.hasState(uuid)) {
            this.playerStateManager.restoreState(player);
        } else if (!restoreState && !alreadyInLobbyWorld && this.playerStateManager != null && !this.playerStateManager.hasLobbyState(uuid)) {
            this.playerStateManager.saveLobbyState(player);
        }
        this.preparePlayerForLobby(player);
        if (teleport && this.lobbySpawn != null) {
            player.teleport(this.lobbySpawn);
        }
        if (this.hotbarEnabled) {
            this.giveHotbarItems(player);
        }
        this.playersInLobby.add(uuid);
        for (PotionEffect effect : this.lobbyEffects) {
            player.addPotionEffect(effect);
        }
        GameMode lobbyGamemode = this.plugin.getConfigManager().getLobbySettings().lobbyGamemode();
        player.setGameMode(lobbyGamemode);
        player.updateInventory();
        this.plugin.debug("Player " + player.getName() + " sent to lobby (teleport=" + teleport + ", restoreState=" + restoreState + ")");
    }

    public void removeFromLobby(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        if (this.playersInLobby.remove(uuid)) {
            this.clearHotbarItems(player);
            for (PotionEffect effect : this.lobbyEffects) {
                player.removePotionEffect(effect.getType());
            }
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.removePotionEffect(PotionEffectType.SATURATION);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            player.removePotionEffect(PotionEffectType.RESISTANCE);
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.WEAKNESS);
        }
    }

    public void forceRemoveFromLobbyTracking(@Nonnull UUID uuid) {
        this.playersInLobby.remove(uuid);
    }

    private void preparePlayerForLobby(@Nonnull Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGlowing(false);
        player.setInvisible(false);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    public void giveHotbarItems(@Nonnull Player player) {
        if (!this.hotbarEnabled) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(uuid)) {
            this.plugin.debug("Skipping hotbar items for " + player.getName() + " - still in active match");
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(uuid)) {
            this.plugin.debug("Skipping hotbar items for " + player.getName() + " - in FFA");
            return;
        }
        WorldRestrictionManager worldRestriction = this.plugin.getWorldRestrictionManager();
        if (worldRestriction != null && !worldRestriction.canUseLobbyItems(player)) {
            worldRestriction.sendLobbyItemsBlockedMessage(player);
            return;
        }
        PlayerInventory inv = player.getInventory();
        LanguageManager langMgr = this.plugin.getLanguageManager();
        for (Map.Entry<Integer, LobbyItem> entry : this.hotbarItems.entrySet()) {
            int slot = entry.getKey();
            LobbyItem lobbyItem = entry.getValue();
            ItemStack item = lobbyItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && langMgr != null) {
                String actionKey = lobbyItem.getAction().name().toLowerCase().replace("_", "-");
                String namePath = "gui.items." + actionKey + ".name";
                String lorePath = "gui.items." + actionKey + ".lore";
                try {
                    String translatedName = langMgr.getRaw(player, namePath);
                    if (translatedName != null && !translatedName.contains("Missing") && !translatedName.contains("[Missing:")) {
                        meta.displayName(TextUtil.parseLegacy(translatedName));
                    }
                }
                catch (Exception e) {
                    this.plugin.debug("Failed to load hotbar name for " + actionKey + ": " + e.getMessage());
                }
                try {
                    String firstLine;
                    List<Component> translatedLore = langMgr.getComponentList(player, lorePath);
                    if (!(translatedLore == null || translatedLore.isEmpty() || (firstLine = TextUtil.toLegacy(translatedLore.get(0))).contains("Missing") || firstLine.contains("[Missing:"))) {
                        meta.lore(translatedLore);
                    }
                }
                catch (Exception e) {
                    this.plugin.debug("Failed to load hotbar lore for " + actionKey + ": " + e.getMessage());
                }
                if (item.getType() == Material.PLAYER_HEAD) {
                    SkullMeta skullMeta = (SkullMeta)meta;
                    skullMeta.setOwningPlayer((OfflinePlayer)player);
                }
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
        }
        player.updateInventory();
    }

    public void clearHotbarItems(@Nonnull Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 9; ++i) {
            ItemStack item = inv.getItem(i);
            if (!this.isLobbyItem(item)) continue;
            inv.setItem(i, null);
        }
        player.updateInventory();
    }

    public void refreshHotbarItems(@Nonnull Player player) {
        if (this.isInLobby(player)) {
            this.clearHotbarItems(player);
            this.giveHotbarItems(player);
        }
    }

    public boolean isLobbyItem(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(this.lobbyItemKey, PersistentDataType.BYTE);
    }

    @Nullable
    public LobbyItemAction getLobbyItemAction(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        for (LobbyItem lobbyItem : this.hotbarItems.values()) {
            if (!this.isSameLobbyItem(item, lobbyItem.getItem())) continue;
            return lobbyItem.getAction();
        }
        return null;
    }

    @Nullable
    public LobbyItemAction getLobbyItemAction(int slot) {
        LobbyItem lobbyItem = this.hotbarItems.get(slot);
        return lobbyItem != null ? lobbyItem.getAction() : null;
    }

    private boolean isSameLobbyItem(@Nonnull ItemStack item1, @Nonnull ItemStack item2) {
        if (item1.getType() != item2.getType()) {
            return false;
        }
        if (!item1.hasItemMeta() || !item2.hasItemMeta()) {
            return false;
        }
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();
        if (meta1 == null || meta2 == null) {
            return false;
        }
        Component name1 = meta1.displayName();
        Component name2 = meta2.displayName();
        if (name1 == null || name2 == null) {
            return false;
        }
        return name1.equals((Object)name2);
    }

    public boolean isInLobby(@Nonnull Player player) {
        return this.playersInLobby.contains(player.getUniqueId());
    }

    public boolean isInLobby(@Nonnull UUID uuid) {
        return this.playersInLobby.contains(uuid);
    }

    public boolean isInLobbyWorld(@Nonnull Location location) {
        return this.lobbyWorld != null && location.getWorld() != null && location.getWorld().equals((Object)this.lobbyWorld);
    }

    public boolean isInLobbyWorld(@Nonnull Player player) {
        return this.isInLobbyWorld(player.getLocation());
    }

    public boolean isLobbyWorld(@Nonnull World world) {
        if (!this.lobbyEnabled) {
            return false;
        }
        if (this.lobbyWorld == null) {
            return false;
        }
        return world.equals((Object)this.lobbyWorld);
    }

    @Nonnull
    public Set<UUID> getPlayersInLobby() {
        return Collections.unmodifiableSet(this.playersInLobby);
    }

    public int getLobbyPlayerCount() {
        return this.playersInLobby.size();
    }

    public void setLobbySpawn(@Nonnull Location location) {
        this.lobbySpawn = location.clone();
        this.lobbyWorld = location.getWorld();
        this.lobbyWorldName = this.lobbyWorld != null ? this.lobbyWorld.getName() : "world";
        this.saveLobbySpawn();
        this.plugin.getLogger().info("\u00a7a[LobbyManager] Lobby spawn set to: " + this.serializeLocation(this.lobbySpawn));
    }

    @Nullable
    public Location getLobbySpawn() {
        return this.lobbySpawn != null ? this.lobbySpawn.clone() : null;
    }

    @Nullable
    public World getLobbyWorld() {
        return this.lobbyWorld;
    }

    public void teleportToLobby(@Nonnull Player player) {
        if (this.lobbySpawn != null) {
            player.teleport(this.lobbySpawn);
        } else if (this.lobbyWorld != null) {
            player.teleport(this.lobbyWorld.getSpawnLocation());
        }
    }

    public boolean isPvpEnabled() {
        return this.pvpEnabled;
    }

    public boolean isHungerEnabled() {
        return this.hungerEnabled;
    }

    public boolean isFallDamageEnabled() {
        return this.fallDamageEnabled;
    }

    public boolean isDamageEnabled() {
        return this.damageEnabled;
    }

    public boolean isBlockBreakEnabled() {
        return this.blockBreakEnabled;
    }

    public boolean isBlockPlaceEnabled() {
        return this.blockPlaceEnabled;
    }

    public boolean isItemDropEnabled() {
        return this.itemDropEnabled;
    }

    public boolean isItemPickupEnabled() {
        return this.itemPickupEnabled;
    }

    public boolean isLobbyEnabled() {
        return this.lobbyEnabled;
    }

    public boolean isTeleportOnJoin() {
        return this.teleportOnJoin;
    }

    public boolean isGiveItemsOnJoin() {
        return this.giveItemsOnJoin;
    }

    public boolean isFirstJoinTeleport() {
        return this.firstJoinTeleport;
    }

    public boolean isWorldRestriction() {
        return this.worldRestriction;
    }

    public boolean isHotbarEnabled() {
        return this.hotbarEnabled;
    }

    public boolean isLockHotbar() {
        return this.lockHotbar;
    }

    private void saveLobbySpawn() {
        if (this.lobbySpawn != null) {
            this.lobbyConfig.set("lobby.spawn", (Object)this.serializeLocation(this.lobbySpawn));
            this.lobbyConfig.set("lobby.world", (Object)this.lobbyWorldName);
        }
        this.saveConfig();
    }

    private void saveConfig() {
        try {
            this.lobbyConfig.save(this.lobbyFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save lobby.yml", e);
        }
    }

    @Nonnull
    private String serializeLocation(@Nonnull Location loc) {
        return String.format("%s;%.3f;%.3f;%.3f;%.2f;%.2f", loc.getWorld() != null ? loc.getWorld().getName() : "world", loc.getX(), loc.getY(), loc.getZ(), Float.valueOf(loc.getYaw()), Float.valueOf(loc.getPitch()));
    }

    @Nullable
    private Location deserializeLocation(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            String[] parts = str.split(";");
            World world = Bukkit.getWorld((String)parts[0]);
            if (world == null) {
                this.plugin.getLogger().warning("\u00a7c[LobbyManager] Could not find world: " + parts[0]);
                return null;
            }
            return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("\u00a7c[LobbyManager] Failed to parse location: " + str);
            return null;
        }
    }

    @Nullable
    private Location deserializeLocationFromSection(@Nonnull ConfigurationSection section, String worldName) {
        try {
            World world = Bukkit.getWorld((String)worldName);
            if (world == null) {
                this.plugin.getLogger().warning("\u00a7c[LobbyManager] Could not find world: " + worldName);
                return null;
            }
            return new Location(world, section.getDouble("x", 0.5), section.getDouble("y", 100.0), section.getDouble("z", 0.5), (float)section.getDouble("yaw", 0.0), (float)section.getDouble("pitch", 0.0));
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("\u00a7c[LobbyManager] Failed to parse location from section");
            return null;
        }
    }

    @Nullable
    private PotionEffect parseEffect(@Nonnull String effectStr) {
        try {
            int duration;
            String[] parts = effectStr.split(":");
            PotionEffectType type = PotionEffectType.getByKey((NamespacedKey)NamespacedKey.minecraft((String)parts[0].toLowerCase()));
            if (type == null) {
                this.plugin.getLogger().warning("\u00a7c[LobbyManager] Unknown effect type: " + parts[0]);
                return null;
            }
            int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int n = duration = parts.length > 2 ? Integer.parseInt(parts[2]) : Integer.MAX_VALUE;
            if (duration == -1) {
                duration = Integer.MAX_VALUE;
            }
            return new PotionEffect(type, duration, amplifier, false, false);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("\u00a7c[LobbyManager] Failed to parse effect: " + effectStr);
            return null;
        }
    }

    public void reload() {
        this.loadConfiguration();
        this.loadHotbarItemsFromConfig();
        for (UUID uuid : new ArrayList<UUID>(this.playersInLobby)) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            this.refreshHotbarItems(player);
        }
        this.plugin.getLogger().info("\u00a7a[LobbyManager] Reloaded successfully!");
    }

    public void shutdown() {
        if (this.effectTask != null) {
            this.effectTask.cancel();
        }
        this.playersInLobby.clear();
        this.saveConfig();
        this.plugin.getLogger().info("\u00a7a[LobbyManager] Shutdown complete");
    }

    public static enum LobbyItemAction {
        QUEUE_MENU,
        PARTY_MENU,
        KIT_EDITOR,
        FFA_MENU,
        SETTINGS_MENU,
        SPECTATE_MENU,
        STATS_MENU,
        SHOP_MENU,
        LEAVE_LOBBY,
        NONE;

    }

    public static class LobbyItem {
        private final ItemStack item;
        private final LobbyItemAction action;

        public LobbyItem(@Nonnull ItemStack item, @Nonnull LobbyItemAction action) {
            this.item = item;
            this.action = action;
        }

        @Nonnull
        public ItemStack getItem() {
            return this.item;
        }

        @Nonnull
        public LobbyItemAction getAction() {
            return this.action;
        }
    }
}

