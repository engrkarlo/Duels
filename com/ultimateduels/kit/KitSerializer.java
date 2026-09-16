/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
 */
package com.ultimateduels.kit;

import com.ultimateduels.UltimateDuels;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class KitSerializer {
    private final UltimateDuels plugin;

    public KitSerializer(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public String serializeItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
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
            this.plugin.getLogger().log(Level.WARNING, "Failed to serialize item", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public ItemStack deserializeItem(@Nullable String base64) {
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
            this.plugin.getLogger().log(Level.WARNING, "Failed to deserialize item", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public String serializeInventory(@Nullable ItemStack[] contents) {
        if (contents == null) {
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            String string;
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);){
                dataOutput.writeInt(contents.length);
                for (ItemStack item : contents) {
                    dataOutput.writeObject((Object)item);
                }
                string = Base64Coder.encodeLines((byte[])outputStream.toByteArray());
            }
            return string;
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to serialize inventory", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public ItemStack[] deserializeInventory(@Nullable String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)base64));){
            ItemStack[] itemStackArray;
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);){
                int length = dataInput.readInt();
                ItemStack[] contents = new ItemStack[length];
                for (int i = 0; i < length; ++i) {
                    contents[i] = (ItemStack)dataInput.readObject();
                }
                itemStackArray = contents;
            }
            return itemStackArray;
        }
        catch (IOException | ClassNotFoundException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to deserialize inventory", e);
            return null;
        }
    }

    @Nullable
    public String serializeArmor(@Nullable ItemStack[] armor) {
        if (armor == null) {
            return null;
        }
        ItemStack[] normalizedArmor = new ItemStack[4];
        for (int i = 0; i < Math.min(armor.length, 4); ++i) {
            normalizedArmor[i] = armor[i];
        }
        return this.serializeInventory(normalizedArmor);
    }

    @Nullable
    public ItemStack[] deserializeArmor(@Nullable String base64) {
        ItemStack[] deserialized = this.deserializeInventory(base64);
        if (deserialized == null) {
            return null;
        }
        if (deserialized.length == 4) {
            return deserialized;
        }
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < Math.min(deserialized.length, 4); ++i) {
            armor[i] = deserialized[i];
        }
        return armor;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public String serializeCompleteKit(@Nullable ItemStack[] inventory, @Nullable ItemStack[] armor, @Nullable ItemStack offhand) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            String string;
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);){
                if (inventory != null) {
                    dataOutput.writeInt(inventory.length);
                    for (ItemStack item : inventory) {
                        dataOutput.writeObject((Object)item);
                    }
                } else {
                    dataOutput.writeInt(0);
                }
                if (armor != null) {
                    dataOutput.writeInt(armor.length);
                    for (ItemStack item : armor) {
                        dataOutput.writeObject((Object)item);
                    }
                } else {
                    dataOutput.writeInt(0);
                }
                dataOutput.writeObject((Object)offhand);
                string = Base64Coder.encodeLines((byte[])outputStream.toByteArray());
            }
            return string;
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to serialize complete kit", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public KitContents deserializeCompleteKit(@Nullable String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines((String)base64));){
            KitContents kitContents;
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);){
                int invLength = dataInput.readInt();
                ItemStack[] inventory = new ItemStack[invLength > 0 ? invLength : 36];
                for (int i = 0; i < invLength; ++i) {
                    inventory[i] = (ItemStack)dataInput.readObject();
                }
                int armorLength = dataInput.readInt();
                ItemStack[] armor = new ItemStack[armorLength > 0 ? armorLength : 4];
                for (int i = 0; i < armorLength; ++i) {
                    armor[i] = (ItemStack)dataInput.readObject();
                }
                ItemStack offhand = (ItemStack)dataInput.readObject();
                kitContents = new KitContents(inventory, armor, offhand);
            }
            return kitContents;
        }
        catch (IOException | ClassNotFoundException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to deserialize complete kit", e);
            return null;
        }
    }

    @NotNull
    public static ItemStack[] cloneItems(@Nullable ItemStack[] items) {
        if (items == null) {
            return new ItemStack[0];
        }
        ItemStack[] cloned = new ItemStack[items.length];
        for (int i = 0; i < items.length; ++i) {
            if (items[i] == null) continue;
            cloned[i] = items[i].clone();
        }
        return cloned;
    }

    @Nullable
    public static ItemStack cloneItem(@Nullable ItemStack item) {
        return item != null ? item.clone() : null;
    }

    public static boolean isEmpty(@Nullable ItemStack[] items) {
        if (items == null) {
            return true;
        }
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            return false;
        }
        return true;
    }

    public static int countItems(@Nullable ItemStack[] items) {
        if (items == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            ++count;
        }
        return count;
    }

    public static class KitContents {
        private final ItemStack[] inventory;
        private final ItemStack[] armor;
        private final ItemStack offhand;

        public KitContents(@Nullable ItemStack[] inventory, @Nullable ItemStack[] armor, @Nullable ItemStack offhand) {
            this.inventory = inventory != null ? inventory : new ItemStack[36];
            this.armor = armor != null ? armor : new ItemStack[4];
            this.offhand = offhand;
        }

        @NotNull
        public ItemStack[] getInventory() {
            return this.inventory;
        }

        @NotNull
        public ItemStack[] getArmor() {
            return this.armor;
        }

        @Nullable
        public ItemStack getOffhand() {
            return this.offhand;
        }

        @NotNull
        public KitContents clone() {
            return new KitContents(KitSerializer.cloneItems(this.inventory), KitSerializer.cloneItems(this.armor), KitSerializer.cloneItem(this.offhand));
        }
    }
}

