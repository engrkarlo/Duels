/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package com.ultimateduels.kit.model;

import com.ultimateduels.kit.model.KitType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DuelKit
implements Cloneable {
    private String name;
    private String displayName;
    private String description;
    private Material icon;
    private KitType kitType;
    private UUID ownerUUID;
    private String baseKitName;
    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private ItemStack offhand;
    private List<PotionEffect> effects;
    private double maxHealth;
    private boolean hungerLocked;
    private boolean healthLocked;
    private boolean allowBuilding;
    private boolean allowBreaking;
    private boolean allowDropping;
    private boolean allowPickup;
    private long createdAt;
    private long lastModified;
    private int timesUsed;

    public DuelKit(@Nonnull String name, @Nonnull KitType kitType) {
        this.name = name;
        this.displayName = "\u00a7f" + name;
        this.description = "";
        this.icon = Material.DIAMOND_SWORD;
        this.kitType = kitType;
        this.inventoryContents = new ItemStack[36];
        this.armorContents = new ItemStack[4];
        this.offhand = null;
        this.effects = new ArrayList<PotionEffect>();
        this.maxHealth = 20.0;
        this.hungerLocked = false;
        this.healthLocked = false;
        this.allowBuilding = false;
        this.allowBreaking = false;
        this.allowDropping = true;
        this.allowPickup = true;
        this.lastModified = this.createdAt = System.currentTimeMillis();
        this.timesUsed = 0;
    }

    @Nullable
    public ItemStack[] getInventoryContents() {
        return this.inventoryContents;
    }

    public void setInventoryContents(@Nullable ItemStack[] contents) {
        if (contents == null) {
            this.inventoryContents = new ItemStack[36];
            return;
        }
        this.inventoryContents = new ItemStack[36];
        for (int i = 0; i < Math.min(contents.length, 36); ++i) {
            this.inventoryContents[i] = contents[i] != null ? contents[i].clone() : null;
        }
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack getInventorySlot(int slot) {
        if (slot < 0 || slot >= 36) {
            return null;
        }
        return this.inventoryContents[slot];
    }

    public void setInventorySlot(int slot, @Nullable ItemStack item) {
        if (slot < 0 || slot >= 36) {
            return;
        }
        this.inventoryContents[slot] = item != null ? item.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }

    public void setArmorContents(@Nullable ItemStack[] armor) {
        if (armor == null) {
            this.armorContents = new ItemStack[4];
            return;
        }
        this.armorContents = new ItemStack[4];
        for (int i = 0; i < Math.min(armor.length, 4); ++i) {
            this.armorContents[i] = armor[i] != null ? armor[i].clone() : null;
        }
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack getHelmet() {
        return this.armorContents[3];
    }

    public void setHelmet(@Nullable ItemStack helmet) {
        this.armorContents[3] = helmet != null ? helmet.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack getChestplate() {
        return this.armorContents[2];
    }

    public void setChestplate(@Nullable ItemStack chestplate) {
        this.armorContents[2] = chestplate != null ? chestplate.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack getLeggings() {
        return this.armorContents[1];
    }

    public void setLeggings(@Nullable ItemStack leggings) {
        this.armorContents[1] = leggings != null ? leggings.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack getBoots() {
        return this.armorContents[0];
    }

    public void setBoots(@Nullable ItemStack boots) {
        this.armorContents[0] = boots != null ? boots.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public ItemStack getOffhand() {
        return this.offhand;
    }

    public void setOffhand(@Nullable ItemStack offhand) {
        this.offhand = offhand != null ? offhand.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean hasOffhand() {
        return this.offhand != null && this.offhand.getType() != Material.AIR;
    }

    @Nonnull
    public List<PotionEffect> getEffects() {
        return new ArrayList<PotionEffect>(this.effects);
    }

    public void setEffects(@Nullable List<PotionEffect> effects) {
        this.effects = effects != null ? new ArrayList<PotionEffect>(effects) : new ArrayList();
        this.lastModified = System.currentTimeMillis();
    }

    public void addEffect(@Nonnull PotionEffect effect) {
        this.effects.removeIf(e -> e.getType().equals(effect.getType()));
        this.effects.add(effect);
        this.lastModified = System.currentTimeMillis();
    }

    public void removeEffect(@Nonnull PotionEffectType type) {
        this.effects.removeIf(e -> e.getType().equals(type));
        this.lastModified = System.currentTimeMillis();
    }

    public void clearEffects() {
        this.effects.clear();
        this.lastModified = System.currentTimeMillis();
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
        this.lastModified = System.currentTimeMillis();
    }

    @Nonnull
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(@Nonnull String displayName) {
        this.displayName = displayName;
        this.lastModified = System.currentTimeMillis();
    }

    @Nonnull
    public String getDescription() {
        return this.description;
    }

    public void setDescription(@Nonnull String description) {
        this.description = description;
        this.lastModified = System.currentTimeMillis();
    }

    @Nonnull
    public Material getIcon() {
        return this.icon;
    }

    public void setIcon(@Nonnull Material icon) {
        this.icon = icon;
        this.lastModified = System.currentTimeMillis();
    }

    @Nonnull
    public KitType getKitType() {
        return this.kitType;
    }

    public void setKitType(@Nonnull KitType kitType) {
        this.kitType = kitType;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.lastModified = System.currentTimeMillis();
    }

    @Nullable
    public String getBaseKitName() {
        return this.baseKitName;
    }

    public void setBaseKitName(@Nullable String baseKitName) {
        this.baseKitName = baseKitName;
        this.lastModified = System.currentTimeMillis();
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = Math.max(1.0, Math.min(maxHealth, 1024.0));
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isHungerLocked() {
        return this.hungerLocked;
    }

    public void setHungerLocked(boolean hungerLocked) {
        this.hungerLocked = hungerLocked;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isHealthLocked() {
        return this.healthLocked;
    }

    public void setHealthLocked(boolean healthLocked) {
        this.healthLocked = healthLocked;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isAllowBuilding() {
        return this.allowBuilding;
    }

    public void setAllowBuilding(boolean allowBuilding) {
        this.allowBuilding = allowBuilding;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isAllowBreaking() {
        return this.allowBreaking;
    }

    public void setAllowBreaking(boolean allowBreaking) {
        this.allowBreaking = allowBreaking;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isAllowDropping() {
        return this.allowDropping;
    }

    public void setAllowDropping(boolean allowDropping) {
        this.allowDropping = allowDropping;
        this.lastModified = System.currentTimeMillis();
    }

    public boolean isAllowPickup() {
        return this.allowPickup;
    }

    public void setAllowPickup(boolean allowPickup) {
        this.allowPickup = allowPickup;
        this.lastModified = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public int getTimesUsed() {
        return this.timesUsed;
    }

    public void incrementTimesUsed() {
        ++this.timesUsed;
    }

    public boolean isEmpty() {
        for (ItemStack item : this.inventoryContents) {
            if (item == null || item.getType() == Material.AIR) continue;
            return false;
        }
        for (ItemStack item : this.armorContents) {
            if (item == null || item.getType() == Material.AIR) continue;
            return false;
        }
        return this.offhand == null || this.offhand.getType() == Material.AIR;
    }

    public int getItemCount() {
        int count = 0;
        for (ItemStack item : this.inventoryContents) {
            if (item == null || item.getType() == Material.AIR) continue;
            ++count;
        }
        for (ItemStack item : this.armorContents) {
            if (item == null || item.getType() == Material.AIR) continue;
            ++count;
        }
        if (this.offhand != null && this.offhand.getType() != Material.AIR) {
            ++count;
        }
        return count;
    }

    public boolean isAdminKit() {
        return this.kitType == KitType.ADMIN;
    }

    public boolean isPlayerKit() {
        return this.kitType == KitType.PLAYER_CUSTOM;
    }

    @Nonnull
    public List<String> getFormattedLore() {
        ArrayList<String> lore = new ArrayList<String>();
        if (!this.description.isEmpty()) {
            lore.add(this.description);
            lore.add("");
        }
        lore.add("\u00a77Items: \u00a7f" + this.getItemCount());
        lore.add("\u00a77Effects: \u00a7f" + this.effects.size());
        if (this.hasOffhand()) {
            lore.add("\u00a77Offhand: \u00a7f" + this.offhand.getType().name());
        }
        lore.add("");
        lore.add("\u00a77Type: " + (this.isAdminKit() ? "\u00a7cAdmin" : "\u00a7aCustom"));
        if (this.ownerUUID != null) {
            lore.add("\u00a77Owner: \u00a7f" + this.ownerUUID.toString().substring(0, 8) + "...");
        }
        lore.add("");
        lore.add("\u00a7eClick to select!");
        return lore;
    }

    public DuelKit clone() {
        try {
            int i;
            DuelKit cloned = (DuelKit)super.clone();
            cloned.inventoryContents = new ItemStack[36];
            for (i = 0; i < 36; ++i) {
                if (this.inventoryContents[i] == null) continue;
                cloned.inventoryContents[i] = this.inventoryContents[i].clone();
            }
            cloned.armorContents = new ItemStack[4];
            for (i = 0; i < 4; ++i) {
                if (this.armorContents[i] == null) continue;
                cloned.armorContents[i] = this.armorContents[i].clone();
            }
            if (this.offhand != null) {
                cloned.offhand = this.offhand.clone();
            }
            cloned.effects = new ArrayList<PotionEffect>();
            for (PotionEffect effect : this.effects) {
                cloned.effects.add(new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles()));
            }
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone DuelKit", e);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelKit duelKit = (DuelKit)o;
        return Objects.equals(this.name, duelKit.name) && this.kitType == duelKit.kitType && Objects.equals(this.ownerUUID, duelKit.ownerUUID);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.kitType, this.ownerUUID});
    }

    public String toString() {
        return "DuelKit{name='" + this.name + "', displayName='" + this.displayName + "', kitType=" + String.valueOf((Object)this.kitType) + ", ownerUUID=" + String.valueOf(this.ownerUUID) + ", itemCount=" + this.getItemCount() + ", hasOffhand=" + this.hasOffhand() + "}";
    }
}

