/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.config;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.kit.KitSerializer;
import com.ultimateduels.models.kit.Kit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitsConfig {
    private final UltimateDuels plugin;
    private final KitSerializer serializer;
    private File kitsFile;
    private FileConfiguration kitsConfig;

    public KitsConfig(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.serializer = new KitSerializer(plugin);
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
    }

    public void load() {
        if (!this.kitsFile.exists()) {
            this.plugin.saveResource("kits.yml", false);
        }
        this.kitsConfig = YamlConfiguration.loadConfiguration((File)this.kitsFile);
        this.plugin.getLogger().info("Kits configuration loaded!");
    }

    public void reload() {
        this.kitsConfig = YamlConfiguration.loadConfiguration((File)this.kitsFile);
        this.plugin.getLogger().info("Kits configuration reloaded!");
    }

    public void save() {
        try {
            this.kitsConfig.save(this.kitsFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save kits configuration!", e);
        }
    }

    @NotNull
    public Map<String, Kit> loadAllKits() {
        LinkedHashMap<String, Kit> kits = new LinkedHashMap<String, Kit>();
        ConfigurationSection section = this.kitsConfig.getConfigurationSection("kits");
        if (section == null) {
            this.plugin.getLogger().info("No kits found in configuration.");
            return kits;
        }
        for (String kitId : section.getKeys(false)) {
            try {
                Kit kit = this.loadKit(kitId);
                if (kit == null) continue;
                kits.put(kitId.toLowerCase(), kit);
                this.plugin.debug("Loaded kit: " + kitId);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load kit: " + kitId, e);
            }
        }
        this.plugin.getLogger().info("Loaded " + kits.size() + " kit(s)!");
        return kits;
    }

    @Nullable
    public Kit loadKit(@NotNull String kitId) {
        ItemStack[] armor;
        ItemStack[] inventory;
        ConfigurationSection section = this.kitsConfig.getConfigurationSection("kits." + kitId);
        if (section == null) {
            return null;
        }
        Kit kit = Kit.fromConfig(kitId, section);
        String inventoryBase64 = section.getString("contents.inventory-base64");
        String armorBase64 = section.getString("contents.armor-base64");
        String offhandBase64 = section.getString("contents.offhand-base64");
        if (inventoryBase64 != null && (inventory = this.serializer.deserializeInventory(inventoryBase64)) != null) {
            kit.getInventory().clear();
            for (int i = 0; i < inventory.length; ++i) {
                if (inventory[i] == null || inventory[i].getType().isAir()) continue;
                kit.getInventory().put(i, inventory[i]);
            }
        }
        if (armorBase64 != null && (armor = this.serializer.deserializeArmor(armorBase64)) != null) {
            ItemStack[] kitArmor = kit.getArmor();
            System.arraycopy(armor, 0, kitArmor, 0, Math.min(armor.length, 4));
        }
        if (offhandBase64 != null) {
            ItemStack offhand = this.serializer.deserializeItem(offhandBase64);
            kit.setOffhand(offhand);
        }
        return kit;
    }

    @Nullable
    private ItemStack[] loadInventoryFromSection(@Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        ItemStack[] inventory = new ItemStack[36];
        for (String key : section.getKeys(false)) {
            try {
                int slot = Integer.parseInt(key);
                if (slot < 0 || slot >= 36) continue;
                inventory[slot] = this.loadItemFromSection(section.getConfigurationSection(key));
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return inventory;
    }

    @Nullable
    private ItemStack[] loadArmorFromSection(@Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        ItemStack[] armor = new ItemStack[4];
        armor[3] = this.loadItemFromSection(section.getConfigurationSection("helmet"));
        armor[2] = this.loadItemFromSection(section.getConfigurationSection("chestplate"));
        armor[1] = this.loadItemFromSection(section.getConfigurationSection("leggings"));
        armor[0] = this.loadItemFromSection(section.getConfigurationSection("boots"));
        return armor;
    }

    @Nullable
    private ItemStack loadItemFromSection(@Nullable ConfigurationSection section) {
        Material material;
        if (section == null) {
            return null;
        }
        String materialName = section.getString("material", "AIR");
        try {
            material = Material.valueOf((String)materialName.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        if (material == Material.AIR) {
            return null;
        }
        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);
        return item;
    }

    @NotNull
    private List<PotionEffect> loadPotionEffects(@Nullable ConfigurationSection section) {
        ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
        if (section == null) {
            return effects;
        }
        for (String key : section.getKeys(false)) {
            try {
                PotionEffectType type;
                ConfigurationSection effectSection = section.getConfigurationSection(key);
                if (effectSection == null || (type = PotionEffectType.getByName((String)key.toUpperCase())) == null) continue;
                int duration = effectSection.getInt("duration", Integer.MAX_VALUE);
                int amplifier = effectSection.getInt("amplifier", 0);
                boolean ambient = effectSection.getBoolean("ambient", false);
                boolean particles = effectSection.getBoolean("particles", true);
                boolean icon = effectSection.getBoolean("icon", true);
                effects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to load potion effect: " + key);
            }
        }
        return effects;
    }

    @NotNull
    private ItemStack[] mapToArray(@NotNull Map<Integer, ItemStack> inventoryMap) {
        ItemStack[] array = new ItemStack[36];
        for (Map.Entry<Integer, ItemStack> entry : inventoryMap.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= 36) continue;
            array[slot] = entry.getValue();
        }
        return array;
    }

    public void saveKit(@NotNull Kit kit) {
        String path = "kits." + kit.getId();
        this.kitsConfig.set(path + ".display-name", (Object)kit.getDisplayName());
        this.kitsConfig.set(path + ".description", kit.getDescription());
        this.kitsConfig.set(path + ".enabled", (Object)kit.isEnabled());
        this.kitsConfig.set(path + ".queue-enabled", (Object)kit.isQueueEnabled());
        this.kitsConfig.set(path + ".allow-custom-layout", (Object)kit.isAllowCustomLayout());
        this.kitsConfig.set(path + ".permission", (Object)kit.getPermission());
        if (kit.getIcon() != null) {
            this.kitsConfig.set(path + ".icon.material", (Object)kit.getIcon().getType().name());
            this.kitsConfig.set(path + ".icon.amount", (Object)kit.getIcon().getAmount());
        }
        this.kitsConfig.set(path + ".compatible-arenas", new ArrayList<String>(kit.getCompatibleArenas()));
        this.kitsConfig.set(path + ".incompatible-arenas", new ArrayList<String>(kit.getIncompatibleArenas()));
        ItemStack[] inventoryArray = this.mapToArray(kit.getInventory());
        this.kitsConfig.set(path + ".contents.inventory-base64", (Object)this.serializer.serializeInventory(inventoryArray));
        this.kitsConfig.set(path + ".contents.armor-base64", (Object)this.serializer.serializeArmor(kit.getArmor()));
        if (kit.getOffhand() != null) {
            this.kitsConfig.set(path + ".contents.offhand-base64", (Object)this.serializer.serializeItem(kit.getOffhand()));
        } else {
            this.kitsConfig.set(path + ".contents.offhand-base64", null);
        }
        this.savePotionEffects(path + ".effects", kit.getEffects());
        if (!kit.getSpecialSettings().isEmpty()) {
            for (Map.Entry<String, Object> entry : kit.getSpecialSettings().entrySet()) {
                this.kitsConfig.set(path + ".special-settings." + entry.getKey(), entry.getValue());
            }
        }
        this.save();
        this.plugin.debug("Saved kit: " + kit.getId());
    }

    private void savePotionEffects(@NotNull String path, @NotNull List<PotionEffect> effects) {
        this.kitsConfig.set(path, null);
        for (PotionEffect effect : effects) {
            String effectPath = path + "." + effect.getType().getName().toLowerCase();
            this.kitsConfig.set(effectPath + ".duration", (Object)effect.getDuration());
            this.kitsConfig.set(effectPath + ".amplifier", (Object)effect.getAmplifier());
            this.kitsConfig.set(effectPath + ".ambient", (Object)effect.isAmbient());
            this.kitsConfig.set(effectPath + ".particles", (Object)effect.hasParticles());
            this.kitsConfig.set(effectPath + ".icon", (Object)effect.hasIcon());
        }
    }

    public boolean deleteKit(@NotNull String kitId) {
        if (!this.kitsConfig.contains("kits." + kitId)) {
            return false;
        }
        this.kitsConfig.set("kits." + kitId, null);
        this.save();
        this.plugin.debug("Deleted kit: " + kitId);
        return true;
    }

    public boolean kitExists(@NotNull String kitId) {
        return this.kitsConfig.contains("kits." + kitId);
    }

    @NotNull
    public Set<String> getKitIds() {
        ConfigurationSection section = this.kitsConfig.getConfigurationSection("kits");
        if (section == null) {
            return Collections.emptySet();
        }
        return section.getKeys(false);
    }

    @NotNull
    public FileConfiguration getConfig() {
        return this.kitsConfig;
    }

    public void updateKitField(@NotNull String kitId, @NotNull String field, @Nullable Object value) {
        this.kitsConfig.set("kits." + kitId + "." + field, value);
        this.save();
    }

    public void setKitEnabled(@NotNull String kitId, boolean enabled) {
        this.updateKitField(kitId, "enabled", enabled);
    }

    @NotNull
    public KitSerializer getSerializer() {
        return this.serializer;
    }
}

