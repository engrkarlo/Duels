/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.gui.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.kit.KitSelectorGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class KitPreviewGUI
extends AbstractGUI {
    private final DuelKit kit;

    public KitPreviewGUI(UltimateDuels plugin, DuelKit kit) {
        super(plugin, "&6&lPreview: &e" + kit.getDisplayName(), 6);
        this.kit = kit;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        this.setItem(4, this.createInfoItem());
        this.displayArmor();
        this.displayOffhand();
        for (int i = 9; i < 18; ++i) {
            if (i == 13) continue;
            this.inventory.setItem(i, this.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        }
        this.displayInventory();
        this.setItem(45, new GUIItem.Builder(Material.ARROW).name("&c&lBack").lore("&7Click to go back").onClick(event -> {
            Player player = event.getPlayer();
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
                KitSelectorGUI selectorGUI = new KitSelectorGUI(this.plugin, KitSelectorGUI.Mode.PREVIEW);
                selectorGUI.open(player);
            });
        }).build());
        this.setItem(49, this.createLoadButton());
        this.setItem(53, GUIItem.closeButton());
    }

    private void displayArmor() {
        ItemStack[] armor = this.kit.getArmorContents();
        if (armor != null && armor.length > 3 && armor[3] != null && !armor[3].getType().isAir()) {
            this.setItem(0, this.createArmorDisplay(armor[3], "Helmet"));
        } else {
            this.setItem(0, this.createEmptyArmorSlot("Helmet", Material.LEATHER_HELMET));
        }
        if (armor != null && armor.length > 2 && armor[2] != null && !armor[2].getType().isAir()) {
            this.setItem(1, this.createArmorDisplay(armor[2], "Chestplate"));
        } else {
            this.setItem(1, this.createEmptyArmorSlot("Chestplate", Material.LEATHER_CHESTPLATE));
        }
        if (armor != null && armor.length > 1 && armor[1] != null && !armor[1].getType().isAir()) {
            this.setItem(2, this.createArmorDisplay(armor[1], "Leggings"));
        } else {
            this.setItem(2, this.createEmptyArmorSlot("Leggings", Material.LEATHER_LEGGINGS));
        }
        if (armor != null && armor.length > 0 && armor[0] != null && !armor[0].getType().isAir()) {
            this.setItem(3, this.createArmorDisplay(armor[0], "Boots"));
        } else {
            this.setItem(3, this.createEmptyArmorSlot("Boots", Material.LEATHER_BOOTS));
        }
    }

    private void displayOffhand() {
        ItemStack offhand = this.kit.getOffhand();
        if (offhand != null && !offhand.getType().isAir()) {
            ItemStack display = offhand.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                ArrayList<TextComponent> lore = meta.lore() != null ? new ArrayList<TextComponent>(meta.lore()) : new ArrayList();
                lore.add(Component.empty());
                lore.add(Component.text((String)"\u00a7d\u00a7lOFFHAND SLOT"));
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            this.setItem(13, display);
        } else {
            this.setItem(13, new GUIItem.Builder(Material.SHIELD).name("&d&lOffhand Slot").lore("", "&7This kit has no", "&7offhand item set.", "").buildItem());
        }
    }

    private void displayInventory() {
        ItemStack[] contents = this.kit.getInventoryContents();
        int[] invSlots = new int[]{19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int[] hotbarDisplaySlots = new int[]{46, 47, 48, 50, 51, 52};
        int[] hotbarIndices = new int[]{0, 1, 2, 4, 5, 6};
        for (int i = 0; i < hotbarDisplaySlots.length; ++i) {
            int invIndex = hotbarIndices[i];
            if (contents == null || invIndex >= contents.length || contents[invIndex] == null || contents[invIndex].getType().isAir()) continue;
            ItemStack display = contents[invIndex].clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                ArrayList<TextComponent> lore = meta.lore() != null ? new ArrayList<TextComponent>(meta.lore()) : new ArrayList();
                lore.add(Component.empty());
                lore.add(Component.text((String)("\u00a7e\u00a7lHotbar Slot " + (invIndex + 1))));
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            this.inventory.setItem(hotbarDisplaySlots[i], display);
        }
        int slotIndex = 0;
        for (int i = 9; i < 36 && slotIndex < invSlots.length; ++i) {
            int guiSlot = invSlots[slotIndex++];
            if (contents != null && i < contents.length && contents[i] != null && !contents[i].getType().isAir()) {
                ItemStack display = contents[i].clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    ArrayList<TextComponent> lore = meta.lore() != null ? new ArrayList<TextComponent>(meta.lore()) : new ArrayList();
                    lore.add(Component.empty());
                    lore.add(Component.text((String)("\u00a77Inventory Slot " + (i - 8))));
                    meta.lore(lore);
                    display.setItemMeta(meta);
                }
                this.inventory.setItem(guiSlot, display);
                continue;
            }
            this.inventory.setItem(guiSlot, this.createFiller(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
        }
    }

    private ItemStack createArmorDisplay(ItemStack armor, String slotName) {
        ItemStack display = armor.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            ArrayList<TextComponent> lore = meta.lore() != null ? new ArrayList<TextComponent>(meta.lore()) : new ArrayList();
            lore.add(Component.empty());
            lore.add(Component.text((String)("\u00a7e\u00a7l" + slotName)));
            meta.lore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    private ItemStack createEmptyArmorSlot(String slotName, Material placeholder) {
        return new GUIItem.Builder(placeholder).name("&7" + slotName + " (Empty)").lore("", "&7No " + slotName.toLowerCase() + " in this kit").buildItem();
    }

    private ItemStack createInfoItem() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        String description = this.kit.getDescription();
        if (description != null && !description.isEmpty()) {
            lore.add("&7" + description);
            lore.add("");
        }
        int itemCount = this.countKitItems();
        int armorCount = this.countArmorPieces();
        boolean hasOffhand = this.hasOffhand();
        lore.add("&7Items: &f" + itemCount);
        lore.add("&7Armor Pieces: &f" + armorCount + "/4");
        lore.add("&7Offhand: &f" + (hasOffhand ? "Yes" : "No"));
        lore.add("&7Effects: &f" + this.kit.getEffects().size());
        lore.add("");
        Material iconMaterial = this.kit.getIcon();
        if (iconMaterial == null) {
            iconMaterial = Material.DIAMOND_SWORD;
        }
        return this.createItem(iconMaterial, "&6&l" + this.kit.getDisplayName(), lore);
    }

    private GUIItem createLoadButton() {
        return new GUIItem.Builder(Material.LIME_DYE).name("&a&lLoad This Kit").lore("", "&7Apply this kit to", "&7your current inventory.", "", "&c\u26a0 This will clear your", "&c  current inventory!", "", "&aClick to load").onClick(event -> {
            Player player = event.getPlayer();
            player.closeInventory();
            this.plugin.getKitManager().applyKit(player, this.kit, true);
            MessageUtils.sendMessage(player, "&aLoaded kit: &e" + this.kit.getDisplayName());
            ItemStack offhand = this.kit.getOffhand();
            if (offhand != null && !offhand.getType().isAir()) {
                MessageUtils.sendMessage(player, "&7Offhand: &f" + offhand.getType().name());
            }
        }).build();
    }

    private int countKitItems() {
        int count = 0;
        ItemStack[] contents = this.kit.getInventoryContents();
        if (contents != null) {
            for (ItemStack item : contents) {
                if (item == null || item.getType().isAir()) continue;
                ++count;
            }
        }
        return count;
    }

    private int countArmorPieces() {
        int count = 0;
        ItemStack[] armor = this.kit.getArmorContents();
        if (armor != null) {
            for (ItemStack item : armor) {
                if (item == null || item.getType().isAir()) continue;
                ++count;
            }
        }
        return count;
    }

    private boolean hasOffhand() {
        ItemStack offhand = this.kit.getOffhand();
        return offhand != null && !offhand.getType().isAir();
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

