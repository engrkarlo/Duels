/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 *  org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
 */
package com.ultimateduels.models.kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializedInventory {
    private static final Logger LOGGER = Logger.getLogger("UltimateDuels-Inventory");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String inventoryData;
    private String armorData;
    private String offhandData;
    private String effectsData;
    private Format format = Format.BASE64;
    private long serializedAt = System.currentTimeMillis();
    private int itemCount = 0;
    private String checksum;

    private SerializedInventory() {
    }

    public static SerializedInventory fromPlayer(Player player) {
        return SerializedInventory.fromPlayer(player, Format.BASE64);
    }

    public static SerializedInventory fromPlayer(Player player, Format format) {
        SerializedInventory serialized = new SerializedInventory();
        serialized.format = format;
        PlayerInventory inv = player.getInventory();
        try {
            ItemStack[] contents = inv.getContents();
            ItemStack[] mainInventory = Arrays.copyOfRange(contents, 0, 36);
            serialized.inventoryData = SerializedInventory.serializeItems(mainInventory, format);
            ItemStack[] armor = inv.getArmorContents();
            serialized.armorData = SerializedInventory.serializeItems(armor, format);
            ItemStack offhand = inv.getItemInOffHand();
            if (offhand != null && offhand.getType() != Material.AIR) {
                serialized.offhandData = SerializedInventory.serializeItem(offhand, format);
            }
            serialized.itemCount = SerializedInventory.countItems(mainInventory) + SerializedInventory.countItems(armor);
            if (offhand != null && offhand.getType() != Material.AIR) {
                ++serialized.itemCount;
            }
            serialized.checksum = SerializedInventory.generateChecksum(serialized);
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize inventory for " + player.getName(), e);
        }
        return serialized;
    }

    public static SerializedInventory fromItems(ItemStack[] inventory, ItemStack[] armor, ItemStack offhand) {
        return SerializedInventory.fromItems(inventory, armor, offhand, Format.BASE64);
    }

    public static SerializedInventory fromItems(ItemStack[] inventory, ItemStack[] armor, ItemStack offhand, Format format) {
        SerializedInventory serialized = new SerializedInventory();
        serialized.format = format;
        try {
            if (inventory != null) {
                serialized.inventoryData = SerializedInventory.serializeItems(inventory, format);
            }
            if (armor != null) {
                serialized.armorData = SerializedInventory.serializeItems(armor, format);
            }
            if (offhand != null && offhand.getType() != Material.AIR) {
                serialized.offhandData = SerializedInventory.serializeItem(offhand, format);
            }
            serialized.itemCount = SerializedInventory.countItems(inventory) + SerializedInventory.countItems(armor);
            if (offhand != null && offhand.getType() != Material.AIR) {
                ++serialized.itemCount;
            }
            serialized.checksum = SerializedInventory.generateChecksum(serialized);
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize items", e);
        }
        return serialized;
    }

    public static SerializedInventory fromData(String inventoryData, String armorData, String offhandData, Format format) {
        SerializedInventory serialized = new SerializedInventory();
        serialized.inventoryData = inventoryData;
        serialized.armorData = armorData;
        serialized.offhandData = offhandData;
        serialized.format = format;
        return serialized;
    }

    public void applyTo(Player player) {
        this.applyTo(player, true);
    }

    public void applyTo(Player player, boolean clearFirst) {
        PlayerInventory inv = player.getInventory();
        if (clearFirst) {
            inv.clear();
            inv.setArmorContents(null);
            inv.setItemInOffHand(null);
        }
        try {
            ItemStack offhand;
            ItemStack[] armor;
            ItemStack[] items;
            if (this.inventoryData != null && !this.inventoryData.isEmpty() && (items = SerializedInventory.deserializeItems(this.inventoryData, this.format)) != null) {
                for (int i = 0; i < Math.min(items.length, 36); ++i) {
                    if (items[i] == null) continue;
                    inv.setItem(i, items[i]);
                }
            }
            if (this.armorData != null && !this.armorData.isEmpty() && (armor = SerializedInventory.deserializeItems(this.armorData, this.format)) != null) {
                inv.setArmorContents(armor);
            }
            if (this.offhandData != null && !this.offhandData.isEmpty() && (offhand = SerializedInventory.deserializeItem(this.offhandData, this.format)) != null) {
                inv.setItemInOffHand(offhand);
            }
            player.updateInventory();
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to apply inventory to " + player.getName(), e);
        }
    }

    public ItemStack[] getInventory() {
        if (this.inventoryData == null || this.inventoryData.isEmpty()) {
            return new ItemStack[36];
        }
        try {
            return SerializedInventory.deserializeItems(this.inventoryData, this.format);
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize inventory", e);
            return new ItemStack[36];
        }
    }

    public ItemStack[] getArmor() {
        if (this.armorData == null || this.armorData.isEmpty()) {
            return new ItemStack[4];
        }
        try {
            return SerializedInventory.deserializeItems(this.armorData, this.format);
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize armor", e);
            return new ItemStack[4];
        }
    }

    public ItemStack getOffhand() {
        if (this.offhandData == null || this.offhandData.isEmpty()) {
            return null;
        }
        try {
            return SerializedInventory.deserializeItem(this.offhandData, this.format);
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize offhand", e);
            return null;
        }
    }

    public String getInventoryData() {
        return this.inventoryData;
    }

    public String getArmorData() {
        return this.armorData;
    }

    public String getOffhandData() {
        return this.offhandData;
    }

    public String getEffectsData() {
        return this.effectsData;
    }

    public Format getFormat() {
        return this.format;
    }

    public long getSerializedAt() {
        return this.serializedAt;
    }

    public int getItemCount() {
        return this.itemCount;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public void setInventoryData(String data) {
        this.inventoryData = data;
    }

    public void setArmorData(String data) {
        this.armorData = data;
    }

    public void setOffhandData(String data) {
        this.offhandData = data;
    }

    public void setEffectsData(String data) {
        this.effectsData = data;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    private static String itemsToBase64(ItemStack[] items) throws IOException {
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
    }

    private static ItemStack[] itemsFromBase64(String data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)data));){
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
    }

    private static String itemToBase64(ItemStack item) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            String string;
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);){
                dataOutput.writeObject((Object)item);
                string = Base64Coder.encodeLines((byte[])outputStream.toByteArray());
            }
            return string;
        }
    }

    private static ItemStack itemFromBase64(String data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)data));){
            ItemStack itemStack;
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);){
                itemStack = (ItemStack)dataInput.readObject();
            }
            return itemStack;
        }
    }

    private static String itemsToJson(ItemStack[] items) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < items.length; ++i) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("slot", (Number)i);
            obj.addProperty("type", item.getType().name());
            obj.addProperty("amount", (Number)item.getAmount());
            if (item.hasItemMeta()) {
                Map serialized = item.serialize();
                obj.addProperty("data", GSON.toJson((Object)serialized));
            }
            array.add((JsonElement)obj);
        }
        return GSON.toJson((JsonElement)array);
    }

    private static ItemStack[] itemsFromJson(String data) {
        ItemStack[] items = new ItemStack[36];
        try {
            JsonArray array = JsonParser.parseString((String)data).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                int slot = obj.get("slot").getAsInt();
                String type = obj.get("type").getAsString();
                int amount = obj.get("amount").getAsInt();
                Material material = Material.getMaterial((String)type);
                if (material == null || slot < 0 || slot >= items.length) continue;
                items[slot] = new ItemStack(material, amount);
                if (!obj.has("data")) continue;
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse JSON inventory", e);
        }
        return items;
    }

    private static String itemToJson(ItemStack item) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", item.getType().name());
        obj.addProperty("amount", (Number)item.getAmount());
        if (item.hasItemMeta()) {
            Map serialized = item.serialize();
            obj.addProperty("data", GSON.toJson((Object)serialized));
        }
        return GSON.toJson((JsonElement)obj);
    }

    private static ItemStack itemFromJson(String data) {
        try {
            JsonObject obj = JsonParser.parseString((String)data).getAsJsonObject();
            String type = obj.get("type").getAsString();
            int amount = obj.get("amount").getAsInt();
            Material material = Material.getMaterial((String)type);
            if (material != null) {
                return new ItemStack(material, amount);
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse JSON item", e);
        }
        return null;
    }

    private static String serializeItems(ItemStack[] items, Format format) {
        try {
            return switch (format.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SerializedInventory.itemsToBase64(items);
                case 1 -> SerializedInventory.itemsToJson(items);
            };
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize items", e);
            return null;
        }
    }

    private static ItemStack[] deserializeItems(String data, Format format) {
        try {
            return switch (format.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SerializedInventory.itemsFromBase64(data);
                case 1 -> SerializedInventory.itemsFromJson(data);
            };
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize items", e);
            return new ItemStack[36];
        }
    }

    private static String serializeItem(ItemStack item, Format format) {
        try {
            return switch (format.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SerializedInventory.itemToBase64(item);
                case 1 -> SerializedInventory.itemToJson(item);
            };
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize item", e);
            return null;
        }
    }

    private static ItemStack deserializeItem(String data, Format format) {
        try {
            return switch (format.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SerializedInventory.itemFromBase64(data);
                case 1 -> SerializedInventory.itemFromJson(data);
            };
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize item", e);
            return null;
        }
    }

    private static int countItems(ItemStack[] items) {
        if (items == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            ++count;
        }
        return count;
    }

    private static String generateChecksum(SerializedInventory serialized) {
        StringBuilder sb = new StringBuilder();
        if (serialized.inventoryData != null) {
            sb.append(serialized.inventoryData.length());
        }
        sb.append("-");
        if (serialized.armorData != null) {
            sb.append(serialized.armorData.length());
        }
        sb.append("-");
        if (serialized.offhandData != null) {
            sb.append(serialized.offhandData.length());
        }
        sb.append("-");
        sb.append(serialized.itemCount);
        return Integer.toHexString(sb.toString().hashCode());
    }

    public boolean isValid() {
        String currentChecksum = SerializedInventory.generateChecksum(this);
        return currentChecksum.equals(this.checksum);
    }

    public SerializedInventory convertTo(Format newFormat) {
        if (this.format == newFormat) {
            return this;
        }
        return SerializedInventory.fromItems(this.getInventory(), this.getArmor(), this.getOffhand(), newFormat);
    }

    public boolean isEmpty() {
        return this.itemCount == 0;
    }

    public boolean hasOffhand() {
        return this.offhandData != null && !this.offhandData.isEmpty();
    }

    public int getApproximateSize() {
        int size = 0;
        if (this.inventoryData != null) {
            size += this.inventoryData.length();
        }
        if (this.armorData != null) {
            size += this.armorData.length();
        }
        if (this.offhandData != null) {
            size += this.offhandData.length();
        }
        if (this.effectsData != null) {
            size += this.effectsData.length();
        }
        return size;
    }

    public String toString() {
        return "SerializedInventory{format=" + String.valueOf((Object)this.format) + ", itemCount=" + this.itemCount + ", hasOffhand=" + this.hasOffhand() + ", size=" + this.getApproximateSize() + " bytes}";
    }

    public static enum Format {
        BASE64,
        JSON;

    }
}

