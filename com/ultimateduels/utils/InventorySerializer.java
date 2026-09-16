/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 *  org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
 */
package com.ultimateduels.utils;

import com.ultimateduels.utils.ItemSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class InventorySerializer {
    private static final Logger LOGGER = Logger.getLogger("UltimateDuels-InventorySerializer");

    private InventorySerializer() {
    }

    public static SerializedInventoryData serializePlayer(Player player) {
        return InventorySerializer.serializePlayer(player, true, true, true, true);
    }

    public static SerializedInventoryData serializePlayer(Player player, boolean includeInventory, boolean includeArmor, boolean includeOffhand, boolean includeEffects) {
        SerializedInventoryData data = new SerializedInventoryData();
        PlayerInventory inv = player.getInventory();
        try {
            Collection effects;
            ItemStack offhand;
            if (includeInventory) {
                ItemStack[] contents = inv.getContents();
                ItemStack[] mainInventory = new ItemStack[36];
                System.arraycopy(contents, 0, mainInventory, 0, Math.min(36, contents.length));
                data.setInventoryBase64(InventorySerializer.serializeItemArray(mainInventory));
            }
            if (includeArmor) {
                ItemStack[] armor = inv.getArmorContents();
                data.setArmorBase64(InventorySerializer.serializeItemArray(armor));
            }
            if (includeOffhand && (offhand = inv.getItemInOffHand()) != null && offhand.getType() != Material.AIR) {
                data.setOffhandBase64(ItemSerializer.toBase64(offhand));
            }
            if (includeEffects && !(effects = player.getActivePotionEffects()).isEmpty()) {
                data.setEffectsBase64(InventorySerializer.serializeEffects(effects));
            }
            data.setHealth(player.getHealth());
            data.setFoodLevel(player.getFoodLevel());
            data.setSaturation(player.getSaturation());
            data.setTotalExperience(player.getTotalExperience());
            data.setExperienceProgress(player.getExp());
            data.setLevel(player.getLevel());
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize player inventory for " + player.getName(), e);
        }
        return data;
    }

    public static void deserializeToPlayer(Player player, SerializedInventoryData data) {
        InventorySerializer.deserializeToPlayer(player, data, true, true, true, true, true);
    }

    public static void deserializeToPlayer(Player player, SerializedInventoryData data, boolean applyInventory, boolean applyArmor, boolean applyOffhand, boolean applyEffects, boolean applyStats) {
        if (data == null) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        try {
            Collection<PotionEffect> effects;
            ItemStack offhand;
            ItemStack[] armor;
            ItemStack[] items;
            inv.clear();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            if (applyInventory && data.getInventoryBase64() != null && (items = InventorySerializer.deserializeItemArray(data.getInventoryBase64())) != null) {
                for (int i = 0; i < Math.min(items.length, 36); ++i) {
                    if (items[i] == null) continue;
                    inv.setItem(i, items[i]);
                }
            }
            if (applyArmor && data.getArmorBase64() != null && (armor = InventorySerializer.deserializeItemArray(data.getArmorBase64())) != null) {
                inv.setArmorContents(armor);
            }
            if (applyOffhand && data.getOffhandBase64() != null && (offhand = ItemSerializer.fromBase64(data.getOffhandBase64())) != null) {
                inv.setItemInOffHand(offhand);
            }
            if (applyEffects && data.getEffectsBase64() != null && (effects = InventorySerializer.deserializeEffects(data.getEffectsBase64())) != null) {
                for (PotionEffect effect : effects) {
                    player.addPotionEffect(effect);
                }
            }
            if (applyStats) {
                player.setHealth(Math.min(data.getHealth(), player.getMaxHealth()));
                player.setFoodLevel(data.getFoodLevel());
                player.setSaturation(data.getSaturation());
                player.setTotalExperience(data.getTotalExperience());
                player.setExp(data.getExperienceProgress());
                player.setLevel(data.getLevel());
            }
            player.updateInventory();
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize inventory to " + player.getName(), e);
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static String serializeItemArray(ItemStack[] items) {
        if (items == null) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            String string;
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);){
                dataOutput.writeInt(items.length);
                for (ItemStack item : items) {
                    dataOutput.writeObject((Object)item);
                }
                string = Base64Coder.encodeLines((byte[])outputStream.toByteArray());
            }
            return string;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to serialize item array", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static ItemStack[] deserializeItemArray(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new ItemStack[0];
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)base64));){
            ItemStack[] itemStackArray;
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);){
                int length = dataInput.readInt();
                ItemStack[] items = new ItemStack[length];
                for (int i = 0; i < length; ++i) {
                    items[i] = (ItemStack)dataInput.readObject();
                }
                itemStackArray = items;
            }
            return itemStackArray;
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize item array", e);
            return new ItemStack[0];
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static String serializeEffects(Collection<PotionEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            String string;
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);){
                dataOutput.writeInt(effects.size());
                for (PotionEffect effect : effects) {
                    dataOutput.writeObject((Object)effect.getType().getName());
                    dataOutput.writeInt(effect.getDuration());
                    dataOutput.writeInt(effect.getAmplifier());
                    dataOutput.writeBoolean(effect.isAmbient());
                    dataOutput.writeBoolean(effect.hasParticles());
                    dataOutput.writeBoolean(effect.hasIcon());
                }
                string = Base64Coder.encodeLines((byte[])outputStream.toByteArray());
            }
            return string;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to serialize potion effects", e);
            return null;
        }
    }

    public static Collection<PotionEffect> deserializeEffects(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)base64));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);){
            int count = dataInput.readInt();
            for (int i = 0; i < count; ++i) {
                String typeName = (String)dataInput.readObject();
                int duration = dataInput.readInt();
                int amplifier = dataInput.readInt();
                boolean ambient = dataInput.readBoolean();
                boolean particles = dataInput.readBoolean();
                boolean icon = dataInput.readBoolean();
                PotionEffectType effectType = PotionEffectType.getByName((String)typeName);
                if (effectType == null) continue;
                effects.add(new PotionEffect(effectType, duration, amplifier, ambient, particles, icon));
            }
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize potion effects", e);
        }
        return effects;
    }

    public static String serializeMainInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] mainInventory = new ItemStack[36];
        System.arraycopy(contents, 0, mainInventory, 0, Math.min(36, contents.length));
        return InventorySerializer.serializeItemArray(mainInventory);
    }

    public static String serializeArmor(Player player) {
        return InventorySerializer.serializeItemArray(player.getInventory().getArmorContents());
    }

    public static String serializeOffhand(Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            return ItemSerializer.toBase64(offhand);
        }
        return null;
    }

    public static void applyMainInventory(Player player, String base64) {
        if (base64 == null) {
            return;
        }
        ItemStack[] items = InventorySerializer.deserializeItemArray(base64);
        if (items != null) {
            for (int i = 0; i < Math.min(items.length, 36); ++i) {
                player.getInventory().setItem(i, items[i]);
            }
        }
        player.updateInventory();
    }

    public static void applyArmor(Player player, String base64) {
        if (base64 == null) {
            return;
        }
        ItemStack[] armor = InventorySerializer.deserializeItemArray(base64);
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
        player.updateInventory();
    }

    public static void applyOffhand(Player player, String base64) {
        if (base64 == null) {
            player.getInventory().setItemInOffHand(null);
            return;
        }
        ItemStack offhand = ItemSerializer.fromBase64(base64);
        player.getInventory().setItemInOffHand(offhand);
        player.updateInventory();
    }

    public static int countItems(Player player) {
        int count = 0;
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            ++count;
        }
        return count;
    }

    public static boolean isInventoryEmpty(Player player) {
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            return false;
        }
        for (ItemStack item : inv.getArmorContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            return false;
        }
        ItemStack offhand = inv.getItemInOffHand();
        return offhand == null || offhand.getType() == Material.AIR;
    }

    public static void clearAll(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setItemInOffHand(null);
        player.updateInventory();
    }

    public static Map<String, Object> createBackup(Player player) {
        HashMap<String, Object> backup = new HashMap<String, Object>();
        SerializedInventoryData data = InventorySerializer.serializePlayer(player);
        backup.put("inventory", data.getInventoryBase64());
        backup.put("armor", data.getArmorBase64());
        backup.put("offhand", data.getOffhandBase64());
        backup.put("effects", data.getEffectsBase64());
        backup.put("health", data.getHealth());
        backup.put("food", data.getFoodLevel());
        backup.put("saturation", Float.valueOf(data.getSaturation()));
        backup.put("exp", data.getTotalExperience());
        backup.put("level", data.getLevel());
        backup.put("timestamp", data.getSerializedAt());
        return backup;
    }

    public static void restoreBackup(Player player, Map<String, Object> backup) {
        if (backup == null) {
            return;
        }
        SerializedInventoryData data = new SerializedInventoryData();
        data.setInventoryBase64((String)backup.get("inventory"));
        data.setArmorBase64((String)backup.get("armor"));
        data.setOffhandBase64((String)backup.get("offhand"));
        data.setEffectsBase64((String)backup.get("effects"));
        data.setHealth((Double)backup.getOrDefault("health", 20.0));
        data.setFoodLevel((Integer)backup.getOrDefault("food", 20));
        data.setSaturation(((Number)backup.getOrDefault("saturation", Float.valueOf(20.0f))).floatValue());
        data.setTotalExperience((Integer)backup.getOrDefault("exp", 0));
        data.setLevel((Integer)backup.getOrDefault("level", 0));
        InventorySerializer.deserializeToPlayer(player, data);
    }

    public static class SerializedInventoryData {
        private String inventoryBase64;
        private String armorBase64;
        private String offhandBase64;
        private String effectsBase64;
        private double health;
        private int foodLevel;
        private float saturation;
        private int totalExperience;
        private float experienceProgress;
        private int level;
        private long serializedAt = System.currentTimeMillis();

        public String getInventoryBase64() {
            return this.inventoryBase64;
        }

        public void setInventoryBase64(String data) {
            this.inventoryBase64 = data;
        }

        public String getArmorBase64() {
            return this.armorBase64;
        }

        public void setArmorBase64(String data) {
            this.armorBase64 = data;
        }

        public String getOffhandBase64() {
            return this.offhandBase64;
        }

        public void setOffhandBase64(String data) {
            this.offhandBase64 = data;
        }

        public String getEffectsBase64() {
            return this.effectsBase64;
        }

        public void setEffectsBase64(String data) {
            this.effectsBase64 = data;
        }

        public double getHealth() {
            return this.health;
        }

        public void setHealth(double health) {
            this.health = health;
        }

        public int getFoodLevel() {
            return this.foodLevel;
        }

        public void setFoodLevel(int foodLevel) {
            this.foodLevel = foodLevel;
        }

        public float getSaturation() {
            return this.saturation;
        }

        public void setSaturation(float saturation) {
            this.saturation = saturation;
        }

        public int getTotalExperience() {
            return this.totalExperience;
        }

        public void setTotalExperience(int exp) {
            this.totalExperience = exp;
        }

        public float getExperienceProgress() {
            return this.experienceProgress;
        }

        public void setExperienceProgress(float progress) {
            this.experienceProgress = progress;
        }

        public int getLevel() {
            return this.level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public long getSerializedAt() {
            return this.serializedAt;
        }

        public void setSerializedAt(long time) {
            this.serializedAt = time;
        }

        public boolean hasContent() {
            return this.inventoryBase64 != null || this.armorBase64 != null || this.offhandBase64 != null;
        }

        public boolean hasOffhand() {
            return this.offhandBase64 != null && !this.offhandBase64.isEmpty();
        }

        public long getAge() {
            return System.currentTimeMillis() - this.serializedAt;
        }
    }
}

