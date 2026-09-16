/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.ultimateduels.gui;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.utils.TextUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class AbstractGUI
implements InventoryHolder {
    protected final UltimateDuels plugin;
    protected Inventory inventory;
    protected Component title;
    protected int size;
    protected final Map<Integer, GUIItem> items;
    protected boolean playClickSound = true;
    protected boolean allowPlayerInventoryInteraction = false;
    protected boolean handleEmptyClicks = false;
    protected boolean allowDragging = false;
    protected boolean allowItemMovement = false;

    public AbstractGUI(UltimateDuels plugin, String title, int rows) {
        this.plugin = plugin;
        this.title = TextUtil.parse(title);
        this.size = rows * 9;
        this.items = new HashMap<Integer, GUIItem>();
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)this.size, (Component)this.title);
    }

    protected abstract void initializeItems();

    public void open(Player player) {
        this.initializeItems();
        player.openInventory(this.inventory);
        if (this.playClickSound) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
        }
    }

    public void refresh(Player player) {
        this.inventory.clear();
        this.items.clear();
        this.initializeItems();
    }

    public boolean handleClick(Player player, int slot, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
        GUIItem guiItem;
        if (this.allowItemMovement) {
            return this.onClick(player, slot, clickType, clickedItem, event);
        }
        if (this.playClickSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
        if ((guiItem = this.items.get(slot)) != null && guiItem.hasClickHandler()) {
            guiItem.getClickHandler().accept(new GUIClickEvent(player, slot, clickType, clickedItem, event));
            return true;
        }
        this.onClick(player, slot, clickType, clickedItem);
        return true;
    }

    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }

    protected boolean onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
        this.onClick(player, slot, clickType, clickedItem);
        return true;
    }

    public void handlePlayerInventoryClick(InventoryClickEvent event) {
    }

    public void onClose(Player player) {
    }

    protected void setItem(int slot, ItemStack item) {
        this.setItem(slot, item, null);
    }

    protected void setItem(int slot, ItemStack item, Consumer<GUIClickEvent> clickHandler) {
        if (slot < 0 || slot >= this.size) {
            return;
        }
        GUIItem guiItem = new GUIItem(item, clickHandler);
        this.items.put(slot, guiItem);
        this.inventory.setItem(slot, item);
    }

    protected void setItem(int slot, GUIItem guiItem) {
        if (slot < 0 || slot >= this.size) {
            return;
        }
        this.items.put(slot, guiItem);
        this.inventory.setItem(slot, guiItem.getItemStack());
    }

    protected ItemStack createItem(Material material, String name, String ... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.parse(name));
            if (lore.length > 0) {
                ArrayList<Component> loreList = new ArrayList<Component>();
                for (String line : lore) {
                    loreList.add(TextUtil.parse(line));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.parse(name));
            if (lore != null && !lore.isEmpty()) {
                ArrayList<Component> coloredLore = new ArrayList<Component>();
                for (String line : lore) {
                    coloredLore.add(TextUtil.parse(line));
                }
                meta.lore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName((Component)Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    protected void fillEmpty(Material material) {
        String fillerName = GUIMessages.getItemName(null, "common", "filler", " ");
        ItemStack filler = this.createFiller(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.parse(fillerName));
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < this.size; ++i) {
            if (this.inventory.getItem(i) != null) continue;
            this.inventory.setItem(i, filler);
        }
    }

    protected void fillBorder(Material material) {
        int i;
        String fillerName = GUIMessages.getItemName(null, "common", "filler", " ");
        ItemStack filler = this.createFiller(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.parse(fillerName));
            filler.setItemMeta(meta);
        }
        int rows = this.size / 9;
        for (i = 0; i < 9; ++i) {
            this.inventory.setItem(i, filler);
            this.inventory.setItem(this.size - 9 + i, filler);
        }
        for (i = 1; i < rows - 1; ++i) {
            this.inventory.setItem(i * 9, filler);
            this.inventory.setItem(i * 9 + 8, filler);
        }
    }

    protected void fillRow(int row, Material material) {
        ItemStack filler = this.createFiller(material);
        int startSlot = row * 9;
        for (int i = 0; i < 9; ++i) {
            this.inventory.setItem(startSlot + i, filler);
        }
    }

    protected ItemStack createBackButton() {
        String backName = GUIMessages.getItemName(null, "common", "back", "&c&lBack");
        String backLore = GUIMessages.getText(null, "common", "back-lore", "&7Click to go back");
        return this.createItem(Material.ARROW, backName, backLore);
    }

    protected ItemStack createCloseButton() {
        String closeName = GUIMessages.getItemName(null, "common", "close", "&c&lClose");
        String closeLore = GUIMessages.getText(null, "common", "close-lore", "&7Click to close");
        return this.createItem(Material.BARRIER, closeName, closeLore);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Component getTitle() {
        return this.title;
    }

    public int getSize() {
        return this.size;
    }

    public Map<Integer, GUIItem> getItems() {
        return this.items;
    }

    public boolean allowsPlayerInventoryInteraction() {
        return this.allowPlayerInventoryInteraction;
    }

    public boolean handlesEmptyClicks() {
        return this.handleEmptyClicks;
    }

    public boolean allowsDragging() {
        return this.allowDragging;
    }

    public boolean allowsItemMovement() {
        return this.allowItemMovement;
    }

    public static class GUIClickEvent {
        private final Player player;
        private final int slot;
        private final ClickType clickType;
        private final ItemStack clickedItem;
        private final InventoryClickEvent originalEvent;

        public GUIClickEvent(Player player, int slot, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
            this.player = player;
            this.slot = slot;
            this.clickType = clickType;
            this.clickedItem = clickedItem;
            this.originalEvent = event;
        }

        public Player getPlayer() {
            return this.player;
        }

        public int getSlot() {
            return this.slot;
        }

        public ClickType getClickType() {
            return this.clickType;
        }

        public ItemStack getClickedItem() {
            return this.clickedItem;
        }

        public InventoryClickEvent getOriginalEvent() {
            return this.originalEvent;
        }

        public boolean isLeftClick() {
            return this.clickType.isLeftClick();
        }

        public boolean isRightClick() {
            return this.clickType.isRightClick();
        }

        public boolean isShiftClick() {
            return this.clickType.isShiftClick();
        }
    }
}

