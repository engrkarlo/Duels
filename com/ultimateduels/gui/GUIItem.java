/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Material
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.SkullMeta
 */
package com.ultimateduels.gui;

import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.utils.TextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIItem {
    private ItemStack itemStack;
    private Consumer<AbstractGUI.GUIClickEvent> clickHandler;

    public GUIItem(ItemStack itemStack) {
        this(itemStack, null);
    }

    public GUIItem(ItemStack itemStack, Consumer<AbstractGUI.GUIClickEvent> clickHandler) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Consumer<AbstractGUI.GUIClickEvent> getClickHandler() {
        return this.clickHandler;
    }

    public void setClickHandler(Consumer<AbstractGUI.GUIClickEvent> clickHandler) {
        this.clickHandler = clickHandler;
    }

    public boolean hasClickHandler() {
        return this.clickHandler != null;
    }

    public static GUIItem of(Material material, String name, String ... lore) {
        return new Builder(material).name(name).lore(lore).build();
    }

    public static GUIItem of(Material material, String name, Consumer<AbstractGUI.GUIClickEvent> handler, String ... lore) {
        return new Builder(material).name(name).lore(lore).onClick(handler).build();
    }

    public static GUIItem filler() {
        return GUIItem.filler(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static GUIItem filler(Material material) {
        return new Builder(material).name(" ").build();
    }

    public static GUIItem backButton(Consumer<AbstractGUI.GUIClickEvent> handler) {
        return new Builder(Material.ARROW).name("&c&lBack").lore("", "&7Click to go back", "").onClick(handler).build();
    }

    public static GUIItem closeButton() {
        return new Builder(Material.BARRIER).name("&c&lClose").lore("", "&7Click to close", "").onClick(event -> event.getPlayer().closeInventory()).build();
    }

    public static GUIItem toggle(String name, boolean enabled, Consumer<AbstractGUI.GUIClickEvent> handler) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "&aEnabled" : "&cDisabled";
        return new Builder(material).name(name).lore("", "&7Status: " + status, "", "&eClick to toggle", "").glowing(enabled).onClick(handler).build();
    }

    public static GUIItem confirm(Consumer<AbstractGUI.GUIClickEvent> handler) {
        return new Builder(Material.LIME_WOOL).name("&a&lConfirm").lore("", "&7Click to confirm", "").onClick(handler).build();
    }

    public static GUIItem cancel(Consumer<AbstractGUI.GUIClickEvent> handler) {
        return new Builder(Material.RED_WOOL).name("&c&lCancel").lore("", "&7Click to cancel", "").onClick(handler).build();
    }

    public static GUIItem previousPage(int currentPage, Consumer<AbstractGUI.GUIClickEvent> handler) {
        return new Builder(Material.ARROW).name("&e&l\u25c0 Previous Page").lore("", "&7Current Page: &f" + currentPage, "", "&eClick to go back", "").onClick(handler).build();
    }

    public static GUIItem nextPage(int currentPage, int totalPages, Consumer<AbstractGUI.GUIClickEvent> handler) {
        return new Builder(Material.ARROW).name("&e&lNext Page \u25b6").lore("", "&7Page: &f" + currentPage + "&7/&f" + totalPages, "", "&eClick to continue", "").onClick(handler).build();
    }

    public static GUIItem pageIndicator(int currentPage, int totalPages) {
        return new Builder(Material.PAPER).name("&6&lPage " + currentPage + "/" + totalPages).lore("", "&7Navigate using the arrows", "").amount(Math.min(currentPage, 64)).build();
    }

    public static GUIItem info(String name, String ... lore) {
        return new Builder(Material.BOOK).name("&b&l" + name).lore(lore).build();
    }

    public static GUIItem warning(String name, String ... lore) {
        return new Builder(Material.ORANGE_WOOL).name("&6&l\u26a0 " + name).lore(lore).build();
    }

    public static GUIItem error(String name, String ... lore) {
        return new Builder(Material.RED_WOOL).name("&c&l\u2716 " + name).lore(lore).build();
    }

    public static GUIItem success(String name, String ... lore) {
        return new Builder(Material.LIME_WOOL).name("&a&l\u2714 " + name).lore(lore).build();
    }

    public static GUIItem playerHead(String playerName, String displayName, String ... lore) {
        return new Builder(Material.PLAYER_HEAD).skullOwner(playerName).name(displayName).lore(lore).build();
    }

    public static GUIItem number(int number, String name, Consumer<AbstractGUI.GUIClickEvent> handler, String ... lore) {
        return new Builder(Material.PAPER).name(name).lore(lore).amount(Math.max(1, Math.min(64, number))).onClick(handler).build();
    }

    public static GUIItem selector(String name, String currentValue, Consumer<AbstractGUI.GUIClickEvent> handler, String ... options) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Current: &f" + currentValue);
        lore.add("");
        for (String option : options) {
            if (option.equals(currentValue)) {
                lore.add("&a\u25b8 " + option);
                continue;
            }
            lore.add("&7  " + option);
        }
        lore.add("");
        lore.add("&eClick to cycle");
        lore.add("");
        return new Builder(Material.HOPPER).name("&6&l" + name).lore(lore).onClick(handler).build();
    }

    public static GUIItem increaseDecrease(String name, int currentValue, int min, int max, Consumer<AbstractGUI.GUIClickEvent> handler) {
        return new Builder(Material.REPEATER).name("&6&l" + name).lore("", "&7Current: &f" + currentValue, "&7Range: &f" + min + " - " + max, "", "&eLeft-click &7to increase", "&eRight-click &7to decrease", "&eShift-click &7for \u00b15", "").amount(Math.max(1, Math.min(64, currentValue))).onClick(handler).build();
    }

    public static class Builder {
        private Material material = Material.STONE;
        private int amount = 1;
        private String displayName;
        private List<String> lore = new ArrayList<String>();
        private boolean glowing = false;
        private boolean hideAttributes = true;
        private Consumer<AbstractGUI.GUIClickEvent> clickHandler;
        private String skullOwner;
        private int customModelData = -1;

        public Builder(Material material) {
            this.material = material;
        }

        public Builder amount(int amount) {
            this.amount = Math.max(1, Math.min(64, amount));
            return this;
        }

        public Builder name(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder lore(String ... lines) {
            for (String line : lines) {
                this.lore.add(line);
            }
            return this;
        }

        public Builder lore(List<String> lines) {
            this.lore.addAll(lines);
            return this;
        }

        public Builder addLore(String line) {
            this.lore.add(line);
            return this;
        }

        public Builder clearLore() {
            this.lore.clear();
            return this;
        }

        public Builder glowing(boolean glowing) {
            this.glowing = glowing;
            return this;
        }

        public Builder glowing() {
            return this.glowing(true);
        }

        public Builder hideAttributes(boolean hide) {
            this.hideAttributes = hide;
            return this;
        }

        public Builder onClick(Consumer<AbstractGUI.GUIClickEvent> handler) {
            this.clickHandler = handler;
            return this;
        }

        public Builder skullOwner(String playerName) {
            this.material = Material.PLAYER_HEAD;
            this.skullOwner = playerName;
            return this;
        }

        public Builder customModelData(int data) {
            this.customModelData = data;
            return this;
        }

        public GUIItem build() {
            ItemStack item = new ItemStack(this.material, this.amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (this.displayName != null) {
                    meta.displayName(TextUtil.parse(this.displayName));
                }
                if (!this.lore.isEmpty()) {
                    ArrayList<Component> coloredLore = new ArrayList<Component>();
                    for (String line : this.lore) {
                        coloredLore.add(TextUtil.parse(line));
                    }
                    meta.lore(coloredLore);
                }
                if (this.glowing) {
                    meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                    meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
                }
                if (this.hideAttributes) {
                    meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                    meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
                    try {
                        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
                    }
                    catch (IllegalArgumentException | NoSuchFieldError coloredLore) {
                        // empty catch block
                    }
                }
                if (this.customModelData >= 0) {
                    meta.setCustomModelData(Integer.valueOf(this.customModelData));
                }
                if (this.skullOwner != null && meta instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta)meta;
                    skullMeta.setOwner(this.skullOwner);
                }
                item.setItemMeta(meta);
            }
            return new GUIItem(item, this.clickHandler);
        }

        public ItemStack buildItem() {
            return this.build().getItemStack();
        }
    }
}

