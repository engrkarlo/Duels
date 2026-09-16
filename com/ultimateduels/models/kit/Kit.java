/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package com.ultimateduels.models.kit;

import com.ultimateduels.models.kit.KitType;
import com.ultimateduels.models.kit.SerializedInventory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Kit {
    private final String id;
    private String displayName;
    private List<String> description;
    private KitType type;
    private UUID ownerUuid;
    private String baseKitId;
    private ItemStack icon;
    private boolean enabled;
    private boolean queueEnabled;
    private boolean allowCustomLayout;
    private String permission;
    private Set<String> compatibleArenas;
    private Set<String> incompatibleArenas;
    private SerializedInventory serializedInventory;
    private ItemStack[] armor;
    private ItemStack offhand;
    private Map<Integer, ItemStack> inventory;
    private List<PotionEffect> effects;
    private Map<String, Object> specialSettings;
    private long createdAt;
    private long updatedAt;

    public Kit(String id, KitType type) {
        this.id = id.toLowerCase();
        this.displayName = id;
        this.type = type;
        this.description = new ArrayList<String>();
        this.enabled = true;
        this.queueEnabled = true;
        this.allowCustomLayout = true;
        this.permission = "";
        this.compatibleArenas = new HashSet<String>();
        this.incompatibleArenas = new HashSet<String>();
        this.armor = new ItemStack[4];
        this.inventory = new HashMap<Integer, ItemStack>();
        this.effects = new ArrayList<PotionEffect>();
        this.specialSettings = new HashMap<String, Object>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public static Kit fromConfig(String id, ConfigurationSection section) {
        ConfigurationSection specialSection;
        ConfigurationSection effectsSection;
        ConfigurationSection invSection;
        ConfigurationSection offhandSection;
        Kit kit = new Kit(id, KitType.ADMIN_STANDARD);
        kit.displayName = section.getString("display-name", id);
        kit.description = section.getStringList("description");
        kit.enabled = section.getBoolean("enabled", true);
        kit.queueEnabled = section.getBoolean("queue-enabled", true);
        kit.allowCustomLayout = section.getBoolean("allow-custom-layout", true);
        kit.permission = section.getString("permission", "");
        ConfigurationSection iconSection = section.getConfigurationSection("icon");
        if (iconSection != null) {
            kit.icon = Kit.loadItemFromConfig(iconSection);
        }
        kit.compatibleArenas = new HashSet<String>(section.getStringList("compatible-arenas"));
        kit.incompatibleArenas = new HashSet<String>(section.getStringList("incompatible-arenas"));
        ConfigurationSection armorSection = section.getConfigurationSection("armor");
        if (armorSection != null) {
            kit.armor[3] = Kit.loadItemFromConfig(armorSection.getConfigurationSection("helmet"));
            kit.armor[2] = Kit.loadItemFromConfig(armorSection.getConfigurationSection("chestplate"));
            kit.armor[1] = Kit.loadItemFromConfig(armorSection.getConfigurationSection("leggings"));
            kit.armor[0] = Kit.loadItemFromConfig(armorSection.getConfigurationSection("boots"));
        }
        if ((offhandSection = section.getConfigurationSection("offhand")) != null) {
            kit.offhand = Kit.loadItemFromConfig(offhandSection);
        }
        if ((invSection = section.getConfigurationSection("inventory")) != null) {
            for (Object slotKey : invSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt((String)slotKey);
                    ItemStack item = Kit.loadItemFromConfig(invSection.getConfigurationSection((String)slotKey));
                    if (item == null) continue;
                    kit.inventory.put(slot, item);
                }
                catch (NumberFormatException slot) {}
            }
        }
        if ((effectsSection = section.getConfigurationSection("effects")) != null) {
            for (String effectName : effectsSection.getKeys(false)) {
                PotionEffectType effectType;
                ConfigurationSection effectData = effectsSection.getConfigurationSection(effectName);
                if (effectData == null || (effectType = PotionEffectType.getByName((String)effectName)) == null) continue;
                int amplifier = effectData.getInt("amplifier", 0);
                int duration = effectData.getInt("duration", 999999);
                boolean ambient = effectData.getBoolean("ambient", false);
                boolean particles = effectData.getBoolean("particles", false);
                kit.effects.add(new PotionEffect(effectType, duration, amplifier, ambient, particles));
            }
        }
        if ((specialSection = section.getConfigurationSection("special-settings")) != null) {
            for (String key : specialSection.getKeys(false)) {
                kit.specialSettings.put(key, specialSection.get(key));
            }
        }
        return kit;
    }

    private static ItemStack loadItemFromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String materialName = section.getString("material", "AIR");
        if (materialName.equals("AIR")) {
            return null;
        }
        Material material = Material.getMaterial((String)materialName.toUpperCase());
        if (material == null) {
            return null;
        }
        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);
        return item;
    }

    public void applyTo(Player player) {
        this.applyTo(player, true, true);
    }

    public void applyTo(Player player, boolean clearFirst, boolean applyEffects) {
        PlayerInventory inv = player.getInventory();
        if (clearFirst) {
            inv.clear();
            inv.setArmorContents(null);
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
        }
        if (this.armor != null) {
            inv.setArmorContents(this.armor);
        }
        if (this.offhand != null) {
            inv.setItemInOffHand(this.offhand.clone());
        }
        for (Map.Entry entry : this.inventory.entrySet()) {
            int slot = (Integer)entry.getKey();
            ItemStack item = (ItemStack)entry.getValue();
            if (slot < 0 || slot >= 36 || item == null) continue;
            inv.setItem(slot, item.clone());
        }
        if (applyEffects) {
            for (PotionEffect potionEffect : this.effects) {
                player.addPotionEffect(potionEffect);
            }
        }
        player.updateInventory();
    }

    public void applyEffects(Player player) {
        for (PotionEffect effect : this.effects) {
            player.addPotionEffect(effect);
        }
    }

    public void saveFrom(Player player) {
        PlayerInventory inv = player.getInventory();
        this.armor = (ItemStack[])inv.getArmorContents().clone();
        ItemStack offhandItem = inv.getItemInOffHand();
        this.offhand = offhandItem != null && offhandItem.getType() != Material.AIR ? offhandItem.clone() : null;
        this.inventory.clear();
        for (int i = 0; i < 36; ++i) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            this.inventory.put(i, item.clone());
        }
        this.updatedAt = System.currentTimeMillis();
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getDescription() {
        return this.description;
    }

    public KitType getType() {
        return this.type;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public String getBaseKitId() {
        return this.baseKitId;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isQueueEnabled() {
        return this.queueEnabled;
    }

    public boolean isAllowCustomLayout() {
        return this.allowCustomLayout;
    }

    public String getPermission() {
        return this.permission;
    }

    public Set<String> getCompatibleArenas() {
        return this.compatibleArenas;
    }

    public Set<String> getIncompatibleArenas() {
        return this.incompatibleArenas;
    }

    public ItemStack[] getArmor() {
        return this.armor;
    }

    public ItemStack getOffhand() {
        return this.offhand;
    }

    public Map<Integer, ItemStack> getInventory() {
        return this.inventory;
    }

    public List<PotionEffect> getEffects() {
        return this.effects;
    }

    public Map<String, Object> getSpecialSettings() {
        return this.specialSettings;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getUpdatedAt() {
        return this.updatedAt;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setDescription(List<String> description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setType(KitType type) {
        this.type = type;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setBaseKitId(String baseKitId) {
        this.baseKitId = baseKitId;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setQueueEnabled(boolean queueEnabled) {
        this.queueEnabled = queueEnabled;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setAllowCustomLayout(boolean allowCustomLayout) {
        this.allowCustomLayout = allowCustomLayout;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
        this.updatedAt = System.currentTimeMillis();
    }

    public <T> T getSpecialSetting(String key, T defaultValue) {
        Object value = this.specialSettings.get(key);
        return (T)(value != null ? value : defaultValue);
    }

    public void setSpecialSetting(String key, Object value) {
        this.specialSettings.put(key, value);
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isDamageDisabled() {
        return this.getSpecialSetting("disable-damage", false);
    }

    public boolean isVoidDeath() {
        return this.getSpecialSetting("void-death", false);
    }

    public int getHitsToWin() {
        return this.getSpecialSetting("hits-to-win", -1);
    }

    public boolean isCompatibleWith(String arenaId) {
        if (this.incompatibleArenas.contains(arenaId)) {
            return false;
        }
        return this.compatibleArenas.isEmpty() || this.compatibleArenas.contains(arenaId);
    }

    public void addCompatibleArena(String arenaId) {
        this.compatibleArenas.add(arenaId);
        this.incompatibleArenas.remove(arenaId);
        this.updatedAt = System.currentTimeMillis();
    }

    public void addIncompatibleArena(String arenaId) {
        this.incompatibleArenas.add(arenaId);
        this.compatibleArenas.remove(arenaId);
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean hasPermission(Player player) {
        if (this.permission == null || this.permission.isEmpty()) {
            return true;
        }
        return player.hasPermission(this.permission);
    }

    public Kit clone(String newId) {
        Kit clone = new Kit(newId, this.type);
        clone.displayName = this.displayName;
        clone.description = new ArrayList<String>(this.description);
        clone.icon = this.icon != null ? this.icon.clone() : null;
        clone.enabled = this.enabled;
        clone.queueEnabled = this.queueEnabled;
        clone.allowCustomLayout = this.allowCustomLayout;
        clone.permission = this.permission;
        clone.compatibleArenas = new HashSet<String>(this.compatibleArenas);
        clone.incompatibleArenas = new HashSet<String>(this.incompatibleArenas);
        clone.armor = this.armor != null ? (ItemStack[])this.armor.clone() : new ItemStack[4];
        clone.offhand = this.offhand != null ? this.offhand.clone() : null;
        clone.inventory = new HashMap<Integer, ItemStack>();
        for (Map.Entry<Integer, ItemStack> entry : this.inventory.entrySet()) {
            clone.inventory.put(entry.getKey(), entry.getValue().clone());
        }
        clone.effects = new ArrayList<PotionEffect>(this.effects);
        clone.specialSettings = new HashMap<String, Object>(this.specialSettings);
        return clone;
    }

    public Kit createCustomLayout(UUID ownerUuid, String customId) {
        Kit custom = this.clone(customId);
        custom.type = KitType.PLAYER_CUSTOM;
        custom.ownerUuid = ownerUuid;
        custom.baseKitId = this.id;
        return custom;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Kit kit = (Kit)o;
        return Objects.equals(this.id, kit.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "Kit{id='" + this.id + "', displayName='" + this.displayName + "', type=" + String.valueOf((Object)this.type) + ", enabled=" + this.enabled + "}";
    }
}

