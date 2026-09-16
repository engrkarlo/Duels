/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.potion.PotionType
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 */
package com.ultimateduels.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.kit.CustomKitLayout;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.kit.model.KitType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class KitManager {
    private final UltimateDuels plugin;
    private final Map<String, DuelKit> adminKits;
    private final Map<UUID, Map<String, DuelKit>> playerCustomKits;
    private final Map<UUID, Map<String, CustomKitLayout>> customLayouts;
    private final Map<String, Material> kitIcons;
    private final Map<String, DuelKit> defaultKitTemplates;
    private File adminKitsFile;
    private FileConfiguration adminKitsConfig;
    private File playerKitsFolder;
    private File customLayoutsFolder;

    public KitManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.adminKits = new ConcurrentHashMap<String, DuelKit>();
        this.playerCustomKits = new ConcurrentHashMap<UUID, Map<String, DuelKit>>();
        this.customLayouts = new ConcurrentHashMap<UUID, Map<String, CustomKitLayout>>();
        this.kitIcons = new ConcurrentHashMap<String, Material>();
        this.defaultKitTemplates = new ConcurrentHashMap<String, DuelKit>();
        this.initializeStorage();
    }

    private boolean isVerbose() {
        return this.plugin.getConfig().getBoolean("performance.verbose-logging", false);
    }

    private void initializeStorage() {
        this.adminKitsFile = new File(this.plugin.getDataFolder(), "kits/admin-kits.yml");
        if (!this.adminKitsFile.getParentFile().exists()) {
            this.adminKitsFile.getParentFile().mkdirs();
        }
        if (!this.adminKitsFile.exists()) {
            try {
                this.adminKitsFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to create admin-kits.yml", e);
            }
        }
        this.adminKitsConfig = YamlConfiguration.loadConfiguration((File)this.adminKitsFile);
        this.playerKitsFolder = new File(this.plugin.getDataFolder(), "kits/players");
        if (!this.playerKitsFolder.exists()) {
            this.playerKitsFolder.mkdirs();
        }
        this.customLayoutsFolder = new File(this.plugin.getDataFolder(), "player-layouts");
        if (!this.customLayoutsFolder.exists()) {
            this.customLayoutsFolder.mkdirs();
        }
        this.plugin.getLogger().info("[KitManager] Storage initialized. Layouts folder: " + this.customLayoutsFolder.getAbsolutePath());
    }

    public void loadKits() {
        this.loadAllKits();
    }

    public void loadAllKits() {
        this.loadAdminKits();
        this.loadAllPlayerKits();
        this.initializeDefaultKitTemplates();
        this.plugin.getLogger().info("[KitManager] Loaded " + this.adminKits.size() + " admin kits");
    }

    public void reloadKits() {
        this.adminKits.clear();
        this.playerCustomKits.clear();
        this.kitIcons.clear();
        this.defaultKitTemplates.clear();
        this.loadAllKits();
        this.plugin.getLogger().info("[KitManager] Kits reloaded!");
    }

    private void loadAdminKits() {
        this.adminKits.clear();
        this.adminKitsConfig = YamlConfiguration.loadConfiguration((File)this.adminKitsFile);
        ConfigurationSection kitsSection = this.adminKitsConfig.getConfigurationSection("kits");
        if (kitsSection == null) {
            this.createDefaultAdminKits();
            return;
        }
        for (String kitName : kitsSection.getKeys(false)) {
            try {
                DuelKit kit = this.deserializeKitFromConfig(kitsSection.getConfigurationSection(kitName), kitName);
                if (kit == null) continue;
                this.adminKits.put(kitName.toLowerCase(), kit);
                String iconName = kitsSection.getString(kitName + ".icon", "DIAMOND_SWORD");
                try {
                    this.kitIcons.put(kitName.toLowerCase(), Material.valueOf((String)iconName));
                }
                catch (IllegalArgumentException e) {
                    this.kitIcons.put(kitName.toLowerCase(), Material.DIAMOND_SWORD);
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load kit: " + kitName, e);
            }
        }
    }

    private void loadAllPlayerKits() {
        this.playerCustomKits.clear();
        File[] playerFiles = this.playerKitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (playerFiles == null) {
            return;
        }
        for (File playerFile : playerFiles) {
            try {
                String uuidStr = playerFile.getName().replace(".yml", "");
                UUID playerUUID = UUID.fromString(uuidStr);
                this.loadPlayerKits(playerUUID);
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid player kit file: " + playerFile.getName());
            }
        }
    }

    public void loadPlayerKits(@Nonnull UUID playerUUID) {
        File playerFile = new File(this.playerKitsFolder, playerUUID.toString() + ".yml");
        if (!playerFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)playerFile);
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) {
            return;
        }
        ConcurrentHashMap<String, DuelKit> kits = new ConcurrentHashMap<String, DuelKit>();
        for (String kitName : kitsSection.getKeys(false)) {
            try {
                DuelKit kit = this.deserializeKitFromConfig(kitsSection.getConfigurationSection(kitName), kitName);
                if (kit == null) continue;
                kit.setOwnerUUID(playerUUID);
                kit.setKitType(KitType.PLAYER_CUSTOM);
                kits.put(kitName.toLowerCase(), kit);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load player kit: " + kitName, e);
            }
        }
        this.playerCustomKits.put(playerUUID, kits);
    }

    private void createDefaultAdminKits() {
        this.plugin.getLogger().info("[KitManager] Creating default kits...");
        this.createNoDebuffKit();
        this.createSumoKit();
        this.createClassicKit();
        this.createDiamondKit();
        this.createArcherKit();
        this.createComboKit();
        this.createBuildUHCKit();
        this.saveAdminKits();
    }

    private void createNoDebuffKit() {
        DuelKit kit = new DuelKit("NoDebuff", KitType.ADMIN);
        kit.setDisplayName("\u00a7c\u00a7lNoDebuff");
        kit.setDescription("\u00a77Classic NoDebuff PvP");
        kit.setIcon(Material.POTION);
        ItemStack[] inventory = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        inventory[0] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inventory[1] = new ItemStack(Material.ENDER_PEARL, 16);
        for (int i = 2; i < 36; ++i) {
            inventory[i] = this.createHealthPotion();
        }
        armor[3] = new ItemStack(Material.DIAMOND_HELMET, 1);
        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        armor[0] = new ItemStack(Material.DIAMOND_BOOTS, 1);
        kit.setInventoryContents(inventory);
        kit.setArmorContents(armor);
        kit.setOffhand(new ItemStack(Material.SHIELD, 1));
        kit.addEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        this.adminKits.put("nodebuff", kit);
        this.kitIcons.put("nodebuff", Material.POTION);
    }

    private void createSumoKit() {
        DuelKit kit = new DuelKit("Sumo", KitType.ADMIN);
        kit.setDisplayName("\u00a7e\u00a7lSumo");
        kit.setDescription("\u00a77Knock your opponent off!");
        kit.setIcon(Material.PORKCHOP);
        kit.setInventoryContents(new ItemStack[36]);
        kit.setArmorContents(new ItemStack[4]);
        kit.addEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255, false, false));
        kit.addEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255, false, false));
        kit.setHungerLocked(true);
        kit.setHealthLocked(true);
        this.adminKits.put("sumo", kit);
        this.kitIcons.put("sumo", Material.PORKCHOP);
    }

    private void createClassicKit() {
        DuelKit kit = new DuelKit("Classic", KitType.ADMIN);
        kit.setDisplayName("\u00a7a\u00a7lClassic");
        kit.setDescription("\u00a77Traditional combat");
        kit.setIcon(Material.IRON_SWORD);
        ItemStack[] inventory = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        inventory[0] = new ItemStack(Material.IRON_SWORD, 1);
        inventory[1] = new ItemStack(Material.BOW, 1);
        inventory[2] = new ItemStack(Material.GOLDEN_APPLE, 8);
        inventory[3] = new ItemStack(Material.ARROW, 32);
        armor[3] = new ItemStack(Material.IRON_HELMET, 1);
        armor[2] = new ItemStack(Material.IRON_CHESTPLATE, 1);
        armor[1] = new ItemStack(Material.IRON_LEGGINGS, 1);
        armor[0] = new ItemStack(Material.IRON_BOOTS, 1);
        kit.setInventoryContents(inventory);
        kit.setArmorContents(armor);
        kit.setOffhand(new ItemStack(Material.SHIELD, 1));
        this.adminKits.put("classic", kit);
        this.kitIcons.put("classic", Material.IRON_SWORD);
    }

    private void createDiamondKit() {
        DuelKit kit = new DuelKit("Diamond", KitType.ADMIN);
        kit.setDisplayName("\u00a7b\u00a7lDiamond");
        kit.setDescription("\u00a77Full diamond gear");
        kit.setIcon(Material.DIAMOND_SWORD);
        ItemStack[] inventory = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        inventory[0] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inventory[1] = new ItemStack(Material.GOLDEN_APPLE, 16);
        inventory[2] = new ItemStack(Material.ENDER_PEARL, 16);
        armor[3] = new ItemStack(Material.DIAMOND_HELMET, 1);
        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        armor[0] = new ItemStack(Material.DIAMOND_BOOTS, 1);
        kit.setInventoryContents(inventory);
        kit.setArmorContents(armor);
        this.adminKits.put("diamond", kit);
        this.kitIcons.put("diamond", Material.DIAMOND_SWORD);
    }

    private void createArcherKit() {
        DuelKit kit = new DuelKit("Archer", KitType.ADMIN);
        kit.setDisplayName("\u00a76\u00a7lArcher");
        kit.setDescription("\u00a77Bow combat");
        kit.setIcon(Material.BOW);
        ItemStack[] inventory = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        inventory[0] = new ItemStack(Material.BOW, 1);
        inventory[1] = new ItemStack(Material.IRON_SWORD, 1);
        inventory[8] = new ItemStack(Material.ARROW, 64);
        armor[3] = new ItemStack(Material.LEATHER_HELMET, 1);
        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        armor[1] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        armor[0] = new ItemStack(Material.LEATHER_BOOTS, 1);
        kit.setInventoryContents(inventory);
        kit.setArmorContents(armor);
        this.adminKits.put("archer", kit);
        this.kitIcons.put("archer", Material.BOW);
    }

    private void createComboKit() {
        DuelKit kit = new DuelKit("Combo", KitType.ADMIN);
        kit.setDisplayName("\u00a7d\u00a7lCombo");
        kit.setDescription("\u00a77Combo practice");
        kit.setIcon(Material.DIAMOND_AXE);
        ItemStack[] inventory = new ItemStack[36];
        inventory[0] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inventory[1] = new ItemStack(Material.GOLDEN_APPLE, 64);
        kit.setInventoryContents(inventory);
        kit.setArmorContents(new ItemStack[4]);
        kit.addEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        this.adminKits.put("combo", kit);
        this.kitIcons.put("combo", Material.DIAMOND_AXE);
    }

    private void createBuildUHCKit() {
        DuelKit kit = new DuelKit("BuildUHC", KitType.ADMIN);
        kit.setDisplayName("\u00a74\u00a7lBuildUHC");
        kit.setDescription("\u00a77UHC with building");
        kit.setIcon(Material.LAVA_BUCKET);
        ItemStack[] inventory = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        inventory[0] = new ItemStack(Material.DIAMOND_SWORD, 1);
        inventory[1] = new ItemStack(Material.BOW, 1);
        inventory[2] = new ItemStack(Material.GOLDEN_APPLE, 6);
        inventory[3] = new ItemStack(Material.OAK_PLANKS, 64);
        inventory[4] = new ItemStack(Material.COBBLESTONE, 64);
        inventory[5] = new ItemStack(Material.WATER_BUCKET, 1);
        inventory[6] = new ItemStack(Material.LAVA_BUCKET, 1);
        inventory[8] = new ItemStack(Material.ARROW, 64);
        armor[3] = new ItemStack(Material.DIAMOND_HELMET, 1);
        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        armor[0] = new ItemStack(Material.DIAMOND_BOOTS, 1);
        kit.setInventoryContents(inventory);
        kit.setArmorContents(armor);
        kit.setAllowBuilding(true);
        this.adminKits.put("builduhc", kit);
        this.kitIcons.put("builduhc", Material.LAVA_BUCKET);
    }

    private ItemStack createHealthPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION, 1);
        PotionMeta meta = (PotionMeta)potion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.STRONG_HEALING);
            potion.setItemMeta((ItemMeta)meta);
        }
        return potion;
    }

    private void initializeDefaultKitTemplates() {
        for (Map.Entry<String, DuelKit> entry : this.adminKits.entrySet()) {
            this.defaultKitTemplates.put(entry.getKey(), entry.getValue().clone());
        }
    }

    @Nullable
    public DuelKit getAdminKit(@Nonnull String kitName) {
        return this.adminKits.get(kitName.toLowerCase());
    }

    @Nullable
    public DuelKit getPlayerKit(@Nonnull UUID playerUUID, @Nonnull String kitName) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        return playerKits != null ? playerKits.get(kitName.toLowerCase()) : null;
    }

    @Nullable
    public DuelKit getKit(@Nullable UUID playerUUID, @Nonnull String kitName) {
        DuelKit playerKit;
        if (playerUUID != null && (playerKit = this.getPlayerKit(playerUUID, kitName)) != null) {
            return playerKit;
        }
        return this.getAdminKit(kitName);
    }

    public boolean adminKitExists(@Nonnull String kitName) {
        return this.adminKits.containsKey(kitName.toLowerCase());
    }

    public boolean playerKitExists(@Nonnull UUID playerUUID, @Nonnull String kitName) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        return playerKits != null && playerKits.containsKey(kitName.toLowerCase());
    }

    public boolean hasPlayerKits(@Nonnull UUID playerUUID) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        return playerKits != null && !playerKits.isEmpty();
    }

    public int getPlayerKitCount(@Nonnull UUID playerUUID) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        return playerKits != null ? playerKits.size() : 0;
    }

    @Nonnull
    public Set<String> getPlayerKitNames(@Nonnull UUID playerUUID) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        return playerKits != null ? new HashSet<String>(playerKits.keySet()) : Collections.emptySet();
    }

    @Nonnull
    public Collection<DuelKit> getPlayerKits(@Nonnull UUID playerUUID) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        return playerKits != null ? new ArrayList<DuelKit>(playerKits.values()) : Collections.emptyList();
    }

    @Nonnull
    public Set<String> getAdminKitNames() {
        return new HashSet<String>(this.adminKits.keySet());
    }

    @Nonnull
    public Collection<DuelKit> getAllAdminKits() {
        return new ArrayList<DuelKit>(this.adminKits.values());
    }

    @Nonnull
    public List<DuelKit> getEnabledKits() {
        return new ArrayList<DuelKit>(this.adminKits.values());
    }

    @Nonnull
    public List<DuelKit> getSortedAdminKits() {
        return this.adminKits.values().stream().sorted(Comparator.comparing(DuelKit::getName)).collect(Collectors.toList());
    }

    @Nonnull
    public Material getKitIcon(@Nonnull String kitName) {
        return this.kitIcons.getOrDefault(kitName.toLowerCase(), Material.DIAMOND_SWORD);
    }

    public int getMaxPlayerKits(@Nonnull Player player) {
        if (player.hasPermission("ultimateduels.kit.unlimited")) {
            return Integer.MAX_VALUE;
        }
        for (int i = 50; i >= 1; --i) {
            if (!player.hasPermission("ultimateduels.kit.limit." + i)) continue;
            return i;
        }
        return 3;
    }

    public void applyKit(@Nonnull Player player, @Nonnull DuelKit kit, boolean clearInventory) {
        UUID playerUuid = player.getUniqueId();
        CustomKitLayout customLayout = this.getCustomLayout(playerUuid, kit.getName());
        if (customLayout != null) {
            this.plugin.debug("[KitManager] Applying custom layout for " + player.getName() + " (" + String.valueOf(playerUuid) + ") - Kit: " + kit.getName());
            this.applyCustomLayout(player, kit, customLayout, clearInventory);
        } else {
            this.plugin.debug("[KitManager] Applying default kit for " + player.getName() + " (" + String.valueOf(playerUuid) + ") - Kit: " + kit.getName());
            this.applyDefaultKit(player, kit, clearInventory);
        }
    }

    private void applyDefaultKit(@Nonnull Player player, @Nonnull DuelKit kit, boolean clearInventory) {
        PlayerInventory inv = player.getInventory();
        if (clearInventory) {
            inv.clear();
            player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        }
        if (kit.getArmorContents() != null) {
            inv.setArmorContents(this.cloneItemArray(kit.getArmorContents()));
        }
        if (kit.getInventoryContents() != null) {
            ItemStack[] contents = kit.getInventoryContents();
            for (int i = 0; i < Math.min(contents.length, 36); ++i) {
                if (contents[i] == null) continue;
                inv.setItem(i, contents[i].clone());
            }
        }
        if (kit.getOffhand() != null) {
            inv.setItemInOffHand(kit.getOffhand().clone());
        }
        this.applyKitEffects(player, kit);
    }

    private void applyCustomLayout(@Nonnull Player player, @Nonnull DuelKit kit, @Nonnull CustomKitLayout layout, boolean clearInventory) {
        ItemStack[] customArmor;
        ItemStack[] customInv;
        PlayerInventory inv = player.getInventory();
        if (clearInventory) {
            inv.clear();
            player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        }
        if ((customInv = layout.getInventoryContents()) != null) {
            for (int i = 0; i < Math.min(customInv.length, 36); ++i) {
                if (customInv[i] == null) continue;
                inv.setItem(i, customInv[i].clone());
            }
        }
        if ((customArmor = layout.getArmorContents()) != null) {
            inv.setArmorContents(this.cloneItemArray(customArmor));
        } else if (kit.getArmorContents() != null) {
            inv.setArmorContents(this.cloneItemArray(kit.getArmorContents()));
        }
        ItemStack customOffhand = layout.getOffhand();
        if (customOffhand != null) {
            inv.setItemInOffHand(customOffhand.clone());
        } else if (kit.getOffhand() != null) {
            inv.setItemInOffHand(kit.getOffhand().clone());
        }
        this.applyKitEffects(player, kit);
    }

    private void applyKitEffects(@Nonnull Player player, @Nonnull DuelKit kit) {
        for (PotionEffect effect : kit.getEffects()) {
            player.addPotionEffect(effect);
        }
        if (kit.getMaxHealth() > 0.0) {
            player.setMaxHealth(kit.getMaxHealth());
        }
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(kit.isHungerLocked() ? 20.0f : 12.8f);
        player.updateInventory();
    }

    public boolean applyKitByName(@Nonnull Player player, @Nonnull String kitName, boolean clearInventory) {
        DuelKit kit = this.getKit(player.getUniqueId(), kitName);
        if (kit == null) {
            return false;
        }
        this.applyKit(player, kit, clearInventory);
        return true;
    }

    public boolean hasCustomLayout(@Nonnull UUID playerUuid, @Nonnull String kitName) {
        String kitLower = kitName.toLowerCase();
        Map<String, CustomKitLayout> playerLayouts = this.customLayouts.get(playerUuid);
        if (playerLayouts != null && playerLayouts.containsKey(kitLower)) {
            return true;
        }
        File playerFile = new File(this.customLayoutsFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return false;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)playerFile);
        return config.contains("layouts." + kitLower);
    }

    @Nullable
    public CustomKitLayout getCustomLayout(@Nonnull UUID playerUuid, @Nonnull String kitName) {
        CustomKitLayout cached;
        String kitLower = kitName.toLowerCase();
        Map<String, CustomKitLayout> playerLayouts = this.customLayouts.get(playerUuid);
        if (playerLayouts != null && (cached = playerLayouts.get(kitLower)) != null) {
            this.plugin.debug("[KitManager] Found cached layout for " + String.valueOf(playerUuid) + " - " + kitLower);
            return cached;
        }
        File playerFile = new File(this.customLayoutsFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) {
            this.plugin.debug("[KitManager] No layout file exists for " + String.valueOf(playerUuid));
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)playerFile);
        ConfigurationSection section = config.getConfigurationSection("layouts." + kitLower);
        if (section == null) {
            this.plugin.debug("[KitManager] No layout section for " + String.valueOf(playerUuid) + " - " + kitLower);
            return null;
        }
        CustomKitLayout layout = this.loadLayoutFromSection(playerUuid, kitLower, section);
        if (layout != null) {
            this.customLayouts.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap()).put(kitLower, layout);
            this.plugin.debug("[KitManager] Loaded and cached layout for " + String.valueOf(playerUuid) + " - " + kitLower);
        }
        return layout;
    }

    @Nullable
    private CustomKitLayout loadLayoutFromSection(@Nonnull UUID playerUuid, @Nonnull String kitName, @Nonnull ConfigurationSection section) {
        try {
            ItemStack[] inventory = new ItemStack[36];
            ConfigurationSection invSection = section.getConfigurationSection("inventory");
            if (invSection != null) {
                for (String key : invSection.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        if (slot < 0 || slot >= 36) continue;
                        inventory[slot] = invSection.getItemStack(key);
                    }
                    catch (NumberFormatException slot) {}
                }
            }
            ItemStack[] armor = new ItemStack[4];
            ConfigurationSection armorSection = section.getConfigurationSection("armor");
            if (armorSection != null) {
                armor[0] = armorSection.getItemStack("boots");
                armor[1] = armorSection.getItemStack("leggings");
                armor[2] = armorSection.getItemStack("chestplate");
                armor[3] = armorSection.getItemStack("helmet");
            }
            ItemStack offhand = section.getItemStack("offhand");
            return new CustomKitLayout(playerUuid, kitName, inventory, armor, offhand);
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load layout for " + String.valueOf(playerUuid) + " kit " + kitName, e);
            return null;
        }
    }

    public void saveCustomLayout(@Nonnull CustomKitLayout layout) {
        UUID playerUuid = layout.getPlayerUuid();
        String kitName = layout.getKitName().toLowerCase();
        this.customLayouts.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap()).put(kitName, layout);
        if (this.isVerbose()) {
            this.plugin.getLogger().info("[KitManager] Cached custom layout for " + String.valueOf(playerUuid) + " - " + kitName);
        }
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                File playerFile = new File(this.customLayoutsFolder, playerUuid.toString() + ".yml");
                YamlConfiguration config = playerFile.exists() ? YamlConfiguration.loadConfiguration((File)playerFile) : new YamlConfiguration();
                ConfigurationSection section = config.createSection("layouts." + kitName);
                layout.toConfig(section);
                config.save(playerFile);
                if (this.isVerbose()) {
                    this.plugin.getLogger().info("[KitManager] Saved custom layout to file: " + playerFile.getName() + " -> " + kitName);
                }
            }
            catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to save custom layout for " + String.valueOf(playerUuid), e);
            }
        });
    }

    public void deleteCustomLayout(@Nonnull UUID playerUuid, @Nonnull String kitName) {
        String kitLower = kitName.toLowerCase();
        Map<String, CustomKitLayout> playerLayouts = this.customLayouts.get(playerUuid);
        if (playerLayouts != null) {
            playerLayouts.remove(kitLower);
            if (this.isVerbose()) {
                this.plugin.getLogger().info("[KitManager] Removed layout from cache: " + String.valueOf(playerUuid) + " - " + kitLower);
            }
        }
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                File playerFile = new File(this.customLayoutsFolder, playerUuid.toString() + ".yml");
                if (playerFile.exists()) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration((File)playerFile);
                    config.set("layouts." + kitLower, null);
                    config.save(playerFile);
                    if (this.isVerbose()) {
                        this.plugin.getLogger().info("[KitManager] Deleted layout from file: " + playerFile.getName() + " -> " + kitLower);
                    }
                }
            }
            catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to delete custom layout", e);
            }
        });
    }

    public void loadCustomLayouts(@Nonnull UUID playerUuid) {
        File playerFile = new File(this.customLayoutsFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) {
            this.plugin.debug("[KitManager] No custom layouts file for " + String.valueOf(playerUuid));
            return;
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration((File)playerFile);
            ConfigurationSection layoutsSection = config.getConfigurationSection("layouts");
            if (layoutsSection == null) {
                this.plugin.debug("[KitManager] No layouts section in file for " + String.valueOf(playerUuid));
                return;
            }
            ConcurrentHashMap<String, CustomKitLayout> playerLayouts = new ConcurrentHashMap<String, CustomKitLayout>();
            for (String kitName : layoutsSection.getKeys(false)) {
                CustomKitLayout layout;
                ConfigurationSection section = layoutsSection.getConfigurationSection(kitName);
                if (section == null || (layout = this.loadLayoutFromSection(playerUuid, kitName, section)) == null) continue;
                playerLayouts.put(kitName.toLowerCase(), layout);
            }
            if (!playerLayouts.isEmpty()) {
                this.customLayouts.put(playerUuid, playerLayouts);
                if (this.isVerbose()) {
                    this.plugin.getLogger().info("[KitManager] Loaded " + playerLayouts.size() + " custom layouts for " + String.valueOf(playerUuid));
                }
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load custom layouts for " + String.valueOf(playerUuid), e);
        }
    }

    public void unloadCustomLayouts(@Nonnull UUID playerUuid) {
        Map<String, CustomKitLayout> removed = this.customLayouts.remove(playerUuid);
        if (removed != null && !removed.isEmpty()) {
            this.plugin.debug("[KitManager] Unloaded " + removed.size() + " custom layouts for " + String.valueOf(playerUuid));
        }
    }

    @Nonnull
    public Map<String, CustomKitLayout> getAllCustomLayouts(@Nonnull UUID playerUuid) {
        Map<String, CustomKitLayout> playerLayouts = this.customLayouts.get(playerUuid);
        return playerLayouts != null ? new HashMap<String, CustomKitLayout>(playerLayouts) : Collections.emptyMap();
    }

    public int getCustomLayoutCount(@Nonnull UUID playerUuid) {
        Map<String, CustomKitLayout> playerLayouts = this.customLayouts.get(playerUuid);
        return playerLayouts != null ? playerLayouts.size() : 0;
    }

    public boolean createAdminKitFromPlayer(@Nonnull Player player, @Nonnull String kitName) {
        if (this.adminKits.containsKey(kitName.toLowerCase())) {
            return false;
        }
        DuelKit kit = this.capturePlayerInventory(player, kitName, KitType.ADMIN);
        this.adminKits.put(kitName.toLowerCase(), kit);
        this.kitIcons.put(kitName.toLowerCase(), Material.DIAMOND_SWORD);
        this.saveAdminKits();
        return true;
    }

    public boolean createPlayerKit(@Nonnull Player player, @Nonnull String kitName) {
        UUID playerUUID = player.getUniqueId();
        if (this.getPlayerKitCount(playerUUID) >= this.getMaxPlayerKits(player)) {
            return false;
        }
        DuelKit kit = this.capturePlayerInventory(player, kitName, KitType.PLAYER_CUSTOM);
        kit.setOwnerUUID(playerUUID);
        this.playerCustomKits.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap()).put(kitName.toLowerCase(), kit);
        this.savePlayerKits(playerUUID);
        return true;
    }

    public boolean deletePlayerKit(@Nonnull UUID playerUUID, @Nonnull String kitName) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        if (playerKits == null) {
            return false;
        }
        DuelKit removed = playerKits.remove(kitName.toLowerCase());
        if (removed != null) {
            this.savePlayerKits(playerUUID);
            return true;
        }
        return false;
    }

    public boolean deleteAdminKit(@Nonnull String kitName) {
        if (!this.adminKits.containsKey(kitName.toLowerCase())) {
            return false;
        }
        this.adminKits.remove(kitName.toLowerCase());
        this.kitIcons.remove(kitName.toLowerCase());
        this.saveAdminKits();
        return true;
    }

    @Nonnull
    public DuelKit capturePlayerInventory(@Nonnull Player player, @Nonnull String kitName, @Nonnull KitType type) {
        DuelKit kit = new DuelKit(kitName, type);
        PlayerInventory inv = player.getInventory();
        ItemStack[] mainInventory = new ItemStack[36];
        for (int i = 0; i < 36; ++i) {
            ItemStack item = inv.getItem(i);
            mainInventory[i] = item != null ? item.clone() : null;
        }
        kit.setInventoryContents(mainInventory);
        kit.setArmorContents(this.cloneItemArray(inv.getArmorContents()));
        ItemStack offhandItem = inv.getItemInOffHand();
        kit.setOffhand(offhandItem.getType() != Material.AIR ? offhandItem.clone() : null);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            kit.addEffect(new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles()));
        }
        kit.setMaxHealth(player.getMaxHealth());
        return kit;
    }

    @Nullable
    private ItemStack[] cloneItemArray(@Nullable ItemStack[] original) {
        if (original == null) {
            return null;
        }
        ItemStack[] cloned = new ItemStack[original.length];
        for (int i = 0; i < original.length; ++i) {
            cloned[i] = original[i] != null ? original[i].clone() : null;
        }
        return cloned;
    }

    @Nullable
    public String itemStackToBase64(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);
            dataOutput.writeObject((Object)item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public ItemStack itemStackFromBase64(@Nullable String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);
            ItemStack item = (ItemStack)dataInput.readObject();
            dataInput.close();
            return item;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public String itemStackArrayToBase64(@Nullable ItemStack[] items) {
        if (items == null) {
            return null;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject((Object)item);
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public ItemStack[] itemStackArrayFromBase64(@Nullable String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; ++i) {
                items[i] = (ItemStack)dataInput.readObject();
            }
            dataInput.close();
            return items;
        }
        catch (Exception e) {
            return null;
        }
    }

    public void serializeKitToConfig(@Nonnull DuelKit kit, @Nonnull ConfigurationSection section) {
        section.set("name", (Object)kit.getName());
        section.set("displayName", (Object)kit.getDisplayName());
        section.set("description", (Object)kit.getDescription());
        section.set("icon", (Object)(kit.getIcon() != null ? kit.getIcon().name() : "DIAMOND_SWORD"));
        section.set("type", (Object)kit.getKitType().name());
        section.set("inventory", (Object)this.itemStackArrayToBase64(kit.getInventoryContents()));
        section.set("armor", (Object)this.itemStackArrayToBase64(kit.getArmorContents()));
        section.set("offhand", (Object)this.itemStackToBase64(kit.getOffhand()));
        section.set("maxHealth", (Object)kit.getMaxHealth());
        section.set("hungerLocked", (Object)kit.isHungerLocked());
        section.set("healthLocked", (Object)kit.isHealthLocked());
        section.set("allowBuilding", (Object)kit.isAllowBuilding());
    }

    @Nullable
    public DuelKit deserializeKitFromConfig(@Nullable ConfigurationSection section, @Nonnull String kitName) {
        if (section == null) {
            return null;
        }
        try {
            String offhandBase64;
            String armorBase64;
            KitType type = KitType.valueOf(section.getString("type", "ADMIN"));
            DuelKit kit = new DuelKit(kitName, type);
            kit.setDisplayName(section.getString("displayName", kitName));
            kit.setDescription(section.getString("description", ""));
            try {
                kit.setIcon(Material.valueOf((String)section.getString("icon", "DIAMOND_SWORD")));
            }
            catch (IllegalArgumentException e) {
                kit.setIcon(Material.DIAMOND_SWORD);
            }
            String inventoryBase64 = section.getString("inventory");
            if (inventoryBase64 != null) {
                kit.setInventoryContents(this.itemStackArrayFromBase64(inventoryBase64));
            }
            if ((armorBase64 = section.getString("armor")) != null) {
                kit.setArmorContents(this.itemStackArrayFromBase64(armorBase64));
            }
            if ((offhandBase64 = section.getString("offhand")) != null) {
                kit.setOffhand(this.itemStackFromBase64(offhandBase64));
            }
            kit.setMaxHealth(section.getDouble("maxHealth", 20.0));
            kit.setHungerLocked(section.getBoolean("hungerLocked", false));
            kit.setHealthLocked(section.getBoolean("healthLocked", false));
            kit.setAllowBuilding(section.getBoolean("allowBuilding", false));
            return kit;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to deserialize kit: " + kitName, e);
            return null;
        }
    }

    public void saveAdminKits() {
        this.adminKitsConfig = new YamlConfiguration();
        for (Map.Entry<String, DuelKit> entry : this.adminKits.entrySet()) {
            ConfigurationSection section = this.adminKitsConfig.createSection("kits." + entry.getKey());
            this.serializeKitToConfig(entry.getValue(), section);
        }
        try {
            this.adminKitsConfig.save(this.adminKitsFile);
            if (this.isVerbose()) {
                this.plugin.getLogger().info("[KitManager] Saved " + this.adminKits.size() + " admin kits");
            }
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save admin kits", e);
        }
    }

    public void savePlayerKits(@Nonnull UUID playerUUID) {
        Map<String, DuelKit> playerKits = this.playerCustomKits.get(playerUUID);
        if (playerKits == null || playerKits.isEmpty()) {
            File playerFile = new File(this.playerKitsFolder, playerUUID.toString() + ".yml");
            if (playerFile.exists()) {
                playerFile.delete();
            }
            return;
        }
        File playerFile = new File(this.playerKitsFolder, playerUUID.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, DuelKit> entry : playerKits.entrySet()) {
            ConfigurationSection section = config.createSection("kits." + entry.getKey());
            this.serializeKitToConfig(entry.getValue(), section);
        }
        try {
            config.save(playerFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save player kits for " + String.valueOf(playerUUID), e);
        }
    }

    public void saveAllPlayerKits() {
        for (UUID uuid : this.playerCustomKits.keySet()) {
            this.savePlayerKits(uuid);
        }
    }

    public void shutdown() {
        this.saveAdminKits();
        this.saveAllPlayerKits();
        this.plugin.getLogger().info("[KitManager] Shutdown complete");
    }

    public void reload() {
        this.reloadKits();
    }
}

