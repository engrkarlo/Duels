/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Color
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.Damageable
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.LeatherArmorMeta
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.inventory.meta.SkullMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.potion.PotionType
 *  org.bukkit.profile.PlayerProfile
 */
package com.ultimateduels.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, amount));
    }

    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item);
    }

    public static ItemBuilder of(String materialName) {
        Material material = Material.getMaterial((String)materialName.toUpperCase());
        if (material == null) {
            material = Material.STONE;
        }
        return ItemBuilder.of(material);
    }

    public static ItemBuilder skull() {
        return ItemBuilder.of(Material.PLAYER_HEAD);
    }

    public static ItemBuilder potion(PotionType potionType) {
        Material material = Material.POTION;
        ItemBuilder builder = ItemBuilder.of(material);
        ItemMeta itemMeta = builder.meta;
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)itemMeta;
            potionMeta.setBasePotionType(potionType);
            builder.meta = potionMeta;
        }
        return builder;
    }

    public static ItemBuilder splashPotion() {
        return ItemBuilder.of(Material.SPLASH_POTION);
    }

    public static ItemBuilder leatherArmor(LeatherArmorType armorType) {
        return ItemBuilder.of(armorType.getMaterial());
    }

    public ItemBuilder name(String name) {
        if (this.meta != null && name != null) {
            Component component = LEGACY_SERIALIZER.deserialize(name).decoration(TextDecoration.ITALIC, false);
            this.meta.displayName(component);
        }
        return this;
    }

    public ItemBuilder nameMini(String name) {
        if (this.meta != null && name != null) {
            Component component = MINI_MESSAGE.deserialize((Object)name).decoration(TextDecoration.ITALIC, false);
            this.meta.displayName(component);
        }
        return this;
    }

    public ItemBuilder name(Component name) {
        if (this.meta != null && name != null) {
            this.meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        }
        return this;
    }

    public ItemBuilder lore(String ... lines) {
        return this.lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<String> lines) {
        if (this.meta != null && lines != null) {
            ArrayList<Component> lore = new ArrayList<Component>();
            for (String line : lines) {
                Component component = LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false);
                lore.add(component);
            }
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder loreComponents(List<Component> lines) {
        if (this.meta != null && lines != null) {
            ArrayList<Component> lore = new ArrayList<Component>();
            for (Component line : lines) {
                lore.add(line.decoration(TextDecoration.ITALIC, false));
            }
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder addLore(String ... lines) {
        if (this.meta != null && lines != null) {
            ArrayList<Component> lore = this.meta.lore();
            if (lore == null) {
                lore = new ArrayList<Component>();
            }
            for (String line : lines) {
                Component component = LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false);
                lore.add(component);
            }
            this.meta.lore(lore);
        }
        return this;
    }

    public ItemBuilder clearLore() {
        if (this.meta != null) {
            this.meta.lore(null);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder damage(int damage) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable)itemMeta;
            damageable.setDamage(damage);
        }
        return this;
    }

    public ItemBuilder maxDurability() {
        return this.damage(0);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (this.meta != null) {
            this.meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        if (enchantments != null) {
            enchantments.forEach(this::enchant);
        }
        return this;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        if (this.meta != null) {
            this.meta.removeEnchant(enchantment);
        }
        return this;
    }

    public ItemBuilder clearEnchants() {
        if (this.meta != null) {
            this.meta.getEnchants().keySet().forEach(arg_0 -> ((ItemMeta)this.meta).removeEnchant(arg_0));
        }
        return this;
    }

    public ItemBuilder glow() {
        if (this.meta != null) {
            this.meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        }
        return this;
    }

    public ItemBuilder glow(boolean glowing) {
        if (glowing) {
            return this.glow();
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag ... flags) {
        if (this.meta != null) {
            this.meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder removeFlags(ItemFlag ... flags) {
        if (this.meta != null) {
            this.meta.removeItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder hideFlags() {
        return this.flags(ItemFlag.values());
    }

    public ItemBuilder unbreakable() {
        return this.unbreakable(true);
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (this.meta != null) {
            this.meta.setUnbreakable(unbreakable);
            if (unbreakable) {
                this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
            }
        }
        return this;
    }

    public ItemBuilder customModelData(int data) {
        if (this.meta != null) {
            this.meta.setCustomModelData(Integer.valueOf(data));
        }
        return this;
    }

    public ItemBuilder data(NamespacedKey key, String value) {
        if (this.meta != null) {
            this.meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, (Object)value);
        }
        return this;
    }

    public ItemBuilder data(NamespacedKey key, int value) {
        if (this.meta != null) {
            this.meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, (Object)value);
        }
        return this;
    }

    public ItemBuilder data(NamespacedKey key, double value) {
        if (this.meta != null) {
            this.meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, (Object)value);
        }
        return this;
    }

    public ItemBuilder data(NamespacedKey key, boolean value) {
        if (this.meta != null) {
            this.meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (Object)((byte)(value ? 1 : 0)));
        }
        return this;
    }

    public ItemBuilder potionType(PotionType type) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)itemMeta;
            potionMeta.setBasePotionType(type);
        }
        return this;
    }

    public ItemBuilder potionEffect(PotionEffect effect, boolean overwrite) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)itemMeta;
            potionMeta.addCustomEffect(effect, overwrite);
        }
        return this;
    }

    public ItemBuilder potionEffect(PotionEffectType type, int duration, int amplifier) {
        return this.potionEffect(new PotionEffect(type, duration, amplifier), true);
    }

    public ItemBuilder potionColor(Color color) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)itemMeta;
            potionMeta.setColor(color);
        }
        return this;
    }

    public ItemBuilder potionColor(String hex) {
        try {
            String cleanHex = hex.replace("#", "");
            int rgb = Integer.parseInt(cleanHex, 16);
            return this.potionColor(Color.fromRGB((int)rgb));
        }
        catch (Exception e) {
            return this;
        }
    }

    public ItemBuilder leatherColor(Color color) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta)itemMeta;
            leatherMeta.setColor(color);
        }
        return this;
    }

    public ItemBuilder leatherColor(String hex) {
        try {
            String cleanHex = hex.replace("#", "");
            int rgb = Integer.parseInt(cleanHex, 16);
            return this.leatherColor(Color.fromRGB((int)rgb));
        }
        catch (Exception e) {
            return this;
        }
    }

    public ItemBuilder leatherColor(int r, int g, int b) {
        return this.leatherColor(Color.fromRGB((int)r, (int)g, (int)b));
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemBuilder skullProfile(PlayerProfile profile) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            skullMeta.setOwnerProfile(profile);
        }
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        if (this.meta != null) {
            metaConsumer.accept(this.meta);
        }
        return this;
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        if (this.meta != null && metaClass.isInstance(this.meta)) {
            metaConsumer.accept((ItemMeta)metaClass.cast(this.meta));
        }
        return this;
    }

    public ItemBuilder when(boolean condition, Consumer<ItemBuilder> consumer) {
        if (condition) {
            consumer.accept(this);
        }
        return this;
    }

    public ItemBuilder when(boolean condition, Consumer<ItemBuilder> ifTrue, Consumer<ItemBuilder> ifFalse) {
        if (condition) {
            ifTrue.accept(this);
        } else {
            ifFalse.accept(this);
        }
        return this;
    }

    public ItemBuilder placeholder(String placeholder, String value) {
        List lore;
        Component displayName;
        String toReplace = "{" + placeholder + "}";
        if (this.meta != null && this.meta.hasDisplayName() && (displayName = this.meta.displayName()) != null) {
            String nameStr = LEGACY_SERIALIZER.serialize(displayName);
            nameStr = nameStr.replace(toReplace, value);
            this.meta.displayName(LEGACY_SERIALIZER.deserialize(nameStr).decoration(TextDecoration.ITALIC, false));
        }
        if (this.meta != null && this.meta.hasLore() && (lore = this.meta.lore()) != null) {
            ArrayList<Component> newLore = new ArrayList<Component>();
            for (Component line : lore) {
                String lineStr = LEGACY_SERIALIZER.serialize(line);
                lineStr = lineStr.replace(toReplace, value);
                newLore.add(LEGACY_SERIALIZER.deserialize(lineStr).decoration(TextDecoration.ITALIC, false));
            }
            this.meta.lore(newLore);
        }
        return this;
    }

    public ItemBuilder placeholders(Map<String, String> placeholders) {
        placeholders.forEach(this::placeholder);
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.item.setItemMeta(this.meta);
        }
        return this.item;
    }

    public ItemStack buildClone() {
        return this.build().clone();
    }

    public List<ItemStack> buildMultiple(int count) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ItemStack built = this.build();
        for (int i = 0; i < count; ++i) {
            items.add(built.clone());
        }
        return items;
    }

    public ItemBuilder clone() {
        return ItemBuilder.from(this.build());
    }

    public static enum LeatherArmorType {
        HELMET(Material.LEATHER_HELMET),
        CHESTPLATE(Material.LEATHER_CHESTPLATE),
        LEGGINGS(Material.LEATHER_LEGGINGS),
        BOOTS(Material.LEATHER_BOOTS);

        private final Material material;

        private LeatherArmorType(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return this.material;
        }
    }
}

