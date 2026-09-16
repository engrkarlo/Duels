/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 *  org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
 */
package com.ultimateduels.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class ItemSerializer {
    private static final Logger LOGGER = Logger.getLogger("UltimateDuels-ItemSerializer");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ItemSerializer() {
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static String toBase64(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            String string;
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);){
                dataOutput.writeObject((Object)item);
                string = Base64Coder.encodeLines((byte[])outputStream.toByteArray());
            }
            return string;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to serialize item to Base64", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static ItemStack fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)base64));){
            ItemStack itemStack;
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);){
                itemStack = (ItemStack)dataInput.readObject();
            }
            return itemStack;
        }
        catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize item from Base64", e);
            return null;
        }
    }

    public static Optional<ItemStack> fromBase64Safe(String base64) {
        return Optional.ofNullable(ItemSerializer.fromBase64(base64));
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static String itemArrayToBase64(ItemStack[] items) {
        if (items == null || items.length == 0) {
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
            LOGGER.log(Level.WARNING, "Failed to serialize item array to Base64", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static ItemStack[] itemArrayFromBase64(String base64) {
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
            LOGGER.log(Level.WARNING, "Failed to deserialize item array from Base64", e);
            return new ItemStack[0];
        }
    }

    public static String toJson(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        try {
            Map serialized = item.serialize();
            return GSON.toJson((Object)serialized);
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to serialize item to JSON", e);
            return null;
        }
    }

    public static ItemStack fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            Map serialized = (Map)GSON.fromJson(json, Map.class);
            return ItemStack.deserialize((Map)serialized);
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize item from JSON", e);
            return null;
        }
    }

    public static String toCompactString(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "AIR:0";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(item.getType().name());
        sb.append(":");
        sb.append(item.getAmount());
        if (item.hasItemMeta()) {
            sb.append("|");
            sb.append(ItemSerializer.toBase64(item));
        }
        return sb.toString();
    }

    public static ItemStack fromCompactString(String compact) {
        if (compact == null || compact.isEmpty()) {
            return null;
        }
        String[] parts = compact.split("\\|");
        String[] basic = parts[0].split(":");
        if (basic.length < 2) {
            return null;
        }
        Material material = Material.getMaterial((String)basic[0]);
        if (material == null) {
            return null;
        }
        int amount = Integer.parseInt(basic[1]);
        if (parts.length > 1) {
            return ItemSerializer.fromBase64(parts[1]);
        }
        return new ItemStack(material, amount);
    }

    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    public static ItemStack cloneSafe(ItemStack item) {
        if (ItemSerializer.isAirOrNull(item)) {
            return null;
        }
        return item.clone();
    }

    public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) {
            return true;
        }
        if (item1 == null || item2 == null) {
            return false;
        }
        return item1.isSimilar(item2);
    }

    public static boolean isEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) {
            return true;
        }
        if (item1 == null || item2 == null) {
            return false;
        }
        return item1.equals((Object)item2);
    }

    public static int getSerializedSize(ItemStack item) {
        String base64 = ItemSerializer.toBase64(item);
        return base64 != null ? base64.length() : 0;
    }

    public static boolean validateSerialization(ItemStack item) {
        if (ItemSerializer.isAirOrNull(item)) {
            return true;
        }
        String base64 = ItemSerializer.toBase64(item);
        if (base64 == null) {
            return false;
        }
        ItemStack deserialized = ItemSerializer.fromBase64(base64);
        return ItemSerializer.isEqual(item, deserialized);
    }

    public static String getItemHash(ItemStack item) {
        if (ItemSerializer.isAirOrNull(item)) {
            return "null";
        }
        String base64 = ItemSerializer.toBase64(item);
        if (base64 == null) {
            return String.valueOf(item.hashCode());
        }
        return Integer.toHexString(base64.hashCode());
    }
}

