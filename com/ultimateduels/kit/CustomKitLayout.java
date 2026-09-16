/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.inventory.ItemStack
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.kit;

import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomKitLayout {
    private final UUID playerUuid;
    private final String kitName;
    private final ItemStack[] inventoryContents;
    private final ItemStack[] armorContents;
    private final ItemStack offhand;
    private long lastModified;

    public CustomKitLayout(@NotNull UUID playerUuid, @NotNull String kitName, @Nullable ItemStack[] inventoryContents, @Nullable ItemStack[] armorContents, @Nullable ItemStack offhand) {
        this.playerUuid = playerUuid;
        this.kitName = kitName.toLowerCase();
        this.inventoryContents = inventoryContents != null ? this.cloneArray(inventoryContents) : new ItemStack[36];
        this.armorContents = armorContents != null ? this.cloneArray(armorContents) : new ItemStack[4];
        this.offhand = offhand != null ? offhand.clone() : null;
        this.lastModified = System.currentTimeMillis();
    }

    private ItemStack[] cloneArray(ItemStack[] original) {
        ItemStack[] clone = new ItemStack[original.length];
        for (int i = 0; i < original.length; ++i) {
            clone[i] = original[i] != null ? original[i].clone() : null;
        }
        return clone;
    }

    @NotNull
    public UUID getPlayerUuid() {
        return this.playerUuid;
    }

    @NotNull
    public String getKitName() {
        return this.kitName;
    }

    @NotNull
    public ItemStack[] getInventoryContents() {
        return this.cloneArray(this.inventoryContents);
    }

    @NotNull
    public ItemStack[] getArmorContents() {
        return this.cloneArray(this.armorContents);
    }

    @Nullable
    public ItemStack getOffhand() {
        return this.offhand != null ? this.offhand.clone() : null;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void toConfig(@NotNull ConfigurationSection section) {
        section.set("player-uuid", (Object)this.playerUuid.toString());
        section.set("kit-name", (Object)this.kitName);
        section.set("last-modified", (Object)this.lastModified);
        ConfigurationSection invSection = section.createSection("inventory");
        for (int i = 0; i < this.inventoryContents.length; ++i) {
            if (this.inventoryContents[i] == null || this.inventoryContents[i].getType() == Material.AIR) continue;
            invSection.set(String.valueOf(i), (Object)this.inventoryContents[i]);
        }
        ConfigurationSection armorSection = section.createSection("armor");
        String[] armorSlots = new String[]{"boots", "leggings", "chestplate", "helmet"};
        for (int i = 0; i < this.armorContents.length && i < armorSlots.length; ++i) {
            if (this.armorContents[i] == null || this.armorContents[i].getType() == Material.AIR) continue;
            armorSection.set(armorSlots[i], (Object)this.armorContents[i]);
        }
        if (this.offhand != null && this.offhand.getType() != Material.AIR) {
            section.set("offhand", (Object)this.offhand);
        }
    }

    @Nullable
    public static CustomKitLayout fromConfig(@NotNull ConfigurationSection section) {
        try {
            String uuidStr = section.getString("player-uuid");
            if (uuidStr == null || uuidStr.isEmpty()) {
                return null;
            }
            UUID uuid = UUID.fromString(uuidStr);
            String kitName = section.getString("kit-name", "");
            if (kitName.isEmpty()) {
                return null;
            }
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
            CustomKitLayout layout = new CustomKitLayout(uuid, kitName, inventory, armor, offhand);
            layout.lastModified = section.getLong("last-modified", System.currentTimeMillis());
            return layout;
        }
        catch (Exception e) {
            return null;
        }
    }

    public String toString() {
        return "CustomKitLayout{player=" + String.valueOf(this.playerUuid) + ", kit=" + this.kitName + ", modified=" + this.lastModified + "}";
    }
}

