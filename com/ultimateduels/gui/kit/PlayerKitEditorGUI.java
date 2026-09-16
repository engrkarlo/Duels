/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.gui.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.kit.PlayerKitSelectorGUI;
import com.ultimateduels.kit.CustomKitLayout;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class PlayerKitEditorGUI
extends AbstractGUI {
    private final Player editor;
    private final UUID editorUUID;
    private final DuelKit baseKit;
    private final ItemStack[] armorContents;
    private ItemStack offhandItem;
    private static final int HELMET_SLOT = 0;
    private static final int CHESTPLATE_SLOT = 1;
    private static final int LEGGINGS_SLOT = 2;
    private static final int BOOTS_SLOT = 3;
    private static final int OFFHAND_DISPLAY_SLOT = 4;
    private static final int SAVE_SLOT = 6;
    private static final int RESET_SLOT = 7;
    private static final int BACK_SLOT = 8;
    private static final int OFFHAND_EDIT_SLOT = 17;
    private static final int GUI_INV_START = 18;
    private static final int GUI_INV_END = 53;
    private final Set<Integer> lockedSlots = new HashSet<Integer>();

    public PlayerKitEditorGUI(UltimateDuels plugin, Player editor, DuelKit baseKit) {
        super(plugin, "&6&lEditing: " + PlayerKitEditorGUI.stripColors(baseKit.getDisplayName()), 6);
        this.editor = editor;
        this.editorUUID = editor.getUniqueId();
        this.baseKit = baseKit;
        this.armorContents = new ItemStack[4];
        this.allowItemMovement = true;
        this.handleEmptyClicks = true;
        this.allowDragging = true;
        this.playClickSound = false;
        this.loadLayout();
    }

    private static String stripColors(String text) {
        if (text == null) {
            return "Kit";
        }
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    private void loadLayout() {
        CustomKitLayout customLayout = this.plugin.getKitManager().getCustomLayout(this.editorUUID, this.baseKit.getName());
        if (customLayout != null) {
            ItemStack[] armor = customLayout.getArmorContents();
            if (armor != null) {
                for (int i = 0; i < 4 && i < armor.length; ++i) {
                    this.armorContents[i] = armor[i] != null ? armor[i].clone() : null;
                }
            }
            this.offhandItem = customLayout.getOffhand() != null ? customLayout.getOffhand().clone() : null;
        } else {
            ItemStack[] armor = this.baseKit.getArmorContents();
            if (armor != null) {
                for (int i = 0; i < 4 && i < armor.length; ++i) {
                    this.armorContents[i] = armor[i] != null ? armor[i].clone() : null;
                }
            }
            this.offhandItem = this.baseKit.getOffhand() != null ? this.baseKit.getOffhand().clone() : null;
        }
    }

    @Override
    protected void initializeItems() {
        int i;
        this.lockedSlots.clear();
        this.setArmorDisplaySlot(0, this.armorContents[3], "Helmet", Material.CHAINMAIL_HELMET);
        this.setArmorDisplaySlot(1, this.armorContents[2], "Chestplate", Material.CHAINMAIL_CHESTPLATE);
        this.setArmorDisplaySlot(2, this.armorContents[1], "Leggings", Material.CHAINMAIL_LEGGINGS);
        this.setArmorDisplaySlot(3, this.armorContents[0], "Boots", Material.CHAINMAIL_BOOTS);
        this.setOffhandDisplaySlot();
        this.inventory.setItem(5, this.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        this.lockedSlots.add(5);
        this.setupControlButtons();
        for (i = 0; i <= 4; ++i) {
            this.lockedSlots.add(i);
        }
        for (i = 9; i <= 16; ++i) {
            if (i == 13) {
                this.inventory.setItem(i, this.createInfoItem());
            } else {
                this.inventory.setItem(i, this.createFiller(Material.BLACK_STAINED_GLASS_PANE));
            }
            this.lockedSlots.add(i);
        }
        this.setupOffhandEditSlot();
        this.setupKitItems();
    }

    private void setupControlButtons() {
        ItemStack saveItem = new ItemStack(Material.LIME_DYE);
        ItemMeta saveMeta = saveItem.getItemMeta();
        if (saveMeta != null) {
            saveMeta.displayName(this.plugin.parseText("&a&lSave Layout"));
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&7Click to save your"));
            lore.add(this.plugin.parseText("&7custom layout"));
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&eOffhand item will be saved!"));
            saveMeta.lore(lore);
            saveItem.setItemMeta(saveMeta);
        }
        this.inventory.setItem(6, saveItem);
        this.lockedSlots.add(6);
        ItemStack resetItem = new ItemStack(Material.RED_DYE);
        ItemMeta resetMeta = resetItem.getItemMeta();
        if (resetMeta != null) {
            resetMeta.displayName(this.plugin.parseText("&c&lReset to Default"));
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&7Click to reset to"));
            lore.add(this.plugin.parseText("&7the original kit layout"));
            lore.add(Component.empty());
            resetMeta.lore(lore);
            resetItem.setItemMeta(resetMeta);
        }
        this.inventory.setItem(7, resetItem);
        this.lockedSlots.add(7);
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(this.plugin.parseText("&e&lBack"));
            ArrayList<Component> lore = new ArrayList<Component>();
            lore.add(this.plugin.parseText("&7Return to kit selection"));
            backMeta.lore(lore);
            backItem.setItemMeta(backMeta);
        }
        this.inventory.setItem(8, backItem);
        this.lockedSlots.add(8);
    }

    private void setupOffhandEditSlot() {
        ItemStack displayItem;
        if (this.offhandItem != null && this.offhandItem.getType() != Material.AIR) {
            displayItem = this.offhandItem.clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                ArrayList<Object> lore = meta.lore() != null ? new ArrayList<Object>(meta.lore()) : new ArrayList();
                lore.add(Component.empty());
                lore.add(this.plugin.parseText("&6&l\u2b06 OFFHAND SLOT \u2b06"));
                lore.add(this.plugin.parseText("&eClick with item to swap"));
                lore.add(this.plugin.parseText("&eShift-click to move to inventory"));
                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
        } else {
            displayItem = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.displayName(this.plugin.parseText("&6&l\u2b06 OFFHAND SLOT \u2b06"));
                ArrayList<Object> lore = new ArrayList<Object>();
                lore.add(Component.empty());
                lore.add(this.plugin.parseText("&7Click with an item to set"));
                lore.add(this.plugin.parseText("&7your offhand item"));
                lore.add(Component.empty());
                lore.add(this.plugin.parseText("&eSupports: Shields, Totems,"));
                lore.add(this.plugin.parseText("&eFood, Maps, Arrows, etc."));
                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
        }
        this.inventory.setItem(17, displayItem);
    }

    private void setOffhandDisplaySlot() {
        if (this.offhandItem != null && this.offhandItem.getType() != Material.AIR) {
            ItemStack display = this.offhandItem.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                ArrayList<Object> lore = meta.lore() != null ? new ArrayList<Object>(meta.lore()) : new ArrayList();
                lore.add(Component.empty());
                lore.add(this.plugin.parseText("&eOffhand"));
                lore.add(this.plugin.parseText("&7(Edit in slot below)"));
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            this.inventory.setItem(4, display);
        } else {
            ItemStack emptyOffhand = new ItemStack(Material.SHIELD);
            ItemMeta meta = emptyOffhand.getItemMeta();
            if (meta != null) {
                meta.displayName(this.plugin.parseText("&7&oEmpty Offhand"));
                ArrayList<Object> lore = new ArrayList<Object>();
                lore.add(Component.empty());
                lore.add(this.plugin.parseText("&7Edit in slot below"));
                meta.lore(lore);
                emptyOffhand.setItemMeta(meta);
            }
            this.inventory.setItem(4, emptyOffhand);
        }
    }

    private void setArmorDisplaySlot(int slot, ItemStack item, String name, Material placeholder) {
        if (item != null && item.getType() != Material.AIR) {
            ItemStack display = item.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                ArrayList<Object> lore = meta.lore() != null ? new ArrayList<Object>(meta.lore()) : new ArrayList();
                lore.add(Component.empty());
                lore.add(this.plugin.parseText("&e" + name));
                lore.add(this.plugin.parseText("&7(Cannot move)"));
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            this.inventory.setItem(slot, display);
        } else {
            ItemStack placeholderItem = new ItemStack(placeholder);
            ItemMeta meta = placeholderItem.getItemMeta();
            if (meta != null) {
                meta.displayName(this.plugin.parseText("&7&oEmpty " + name));
                placeholderItem.setItemMeta(meta);
            }
            this.inventory.setItem(slot, placeholderItem);
        }
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(this.plugin.parseText("&6&lLayout Editor"));
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&7Drag and drop items below"));
            lore.add(this.plugin.parseText("&7to customize your layout."));
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&b&lOffhand Editing:"));
            lore.add(this.plugin.parseText("&7Use the &6golden slot &7(right)"));
            lore.add(this.plugin.parseText("&7to set your offhand item!"));
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&eYour changes are saved"));
            lore.add(this.plugin.parseText("&eseparately from other players!"));
            lore.add(Component.empty());
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setupKitItems() {
        CustomKitLayout custom = this.plugin.getKitManager().getCustomLayout(this.editorUUID, this.baseKit.getName());
        ItemStack[] contents = custom != null && custom.getInventoryContents() != null ? custom.getInventoryContents() : this.baseKit.getInventoryContents();
        for (int i = 0; i < 36; ++i) {
            ItemStack item;
            int guiSlot = 18 + i;
            ItemStack itemStack = item = contents != null && i < contents.length ? contents[i] : null;
            if (item != null && item.getType() != Material.AIR) {
                this.inventory.setItem(guiSlot, item.clone());
                continue;
            }
            this.inventory.setItem(guiSlot, null);
        }
    }

    @Override
    protected boolean onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
        if (!player.getUniqueId().equals(this.editorUUID)) {
            return true;
        }
        if (slot == 17) {
            this.handleOffhandSlotClick(player, clickType, event);
            return true;
        }
        if (slot == 6) {
            this.saveLayout();
            return true;
        }
        if (slot == 7) {
            this.resetLayout();
            return true;
        }
        if (slot == 8) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> new PlayerKitSelectorGUI(this.plugin, player).open(player));
            return true;
        }
        if (this.lockedSlots.contains(slot)) {
            return true;
        }
        return slot < 18 || slot > 53;
    }

    private void handleOffhandSlotClick(Player player, ClickType clickType, InventoryClickEvent event) {
        ItemStack cursorItem = event.getCursor();
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                ItemStack oldOffhand = null;
                if (this.offhandItem != null && this.offhandItem.getType() != Material.AIR) {
                    oldOffhand = this.offhandItem.clone();
                }
                this.offhandItem = cursorItem.clone();
                if (oldOffhand != null) {
                    player.setItemOnCursor(oldOffhand);
                } else {
                    player.setItemOnCursor(null);
                }
                this.updateOffhandDisplays();
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
                MessageUtils.sendMessage(player, "&aOffhand set to: &e" + this.offhandItem.getType().name());
            } else if (this.offhandItem != null && this.offhandItem.getType() != Material.AIR) {
                player.setItemOnCursor(this.offhandItem.clone());
                this.offhandItem = null;
                this.updateOffhandDisplays();
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 0.8f);
                MessageUtils.sendMessage(player, "&eOffhand item picked up");
            }
        } else if ((clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) && this.offhandItem != null && this.offhandItem.getType() != Material.AIR) {
            boolean placed = false;
            for (int i = 18; i <= 53; ++i) {
                ItemStack existing = this.inventory.getItem(i);
                if (existing != null && existing.getType() != Material.AIR) continue;
                this.inventory.setItem(i, this.offhandItem.clone());
                this.offhandItem = null;
                placed = true;
                break;
            }
            if (placed) {
                this.updateOffhandDisplays();
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 0.8f);
                MessageUtils.sendMessage(player, "&eOffhand item moved to inventory");
            } else {
                MessageUtils.sendMessage(player, "&cNo empty slot available!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    private void updateOffhandDisplays() {
        this.setOffhandDisplaySlot();
        this.setupOffhandEditSlot();
    }

    private void saveLayout() {
        if (!this.editor.isOnline()) {
            return;
        }
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; ++i) {
            ItemStack item = this.inventory.getItem(18 + i);
            if (item == null || item.getType() == Material.AIR) continue;
            contents[i] = item.clone();
        }
        CustomKitLayout layout = new CustomKitLayout(this.editorUUID, this.baseKit.getName(), contents, (ItemStack[])this.armorContents.clone(), this.offhandItem != null ? this.offhandItem.clone() : null);
        this.plugin.getKitManager().saveCustomLayout(layout);
        String kitName = PlayerKitEditorGUI.stripColors(this.baseKit.getDisplayName());
        String offhandInfo = this.offhandItem != null ? " &7(Offhand: " + this.offhandItem.getType().name() + ")" : "";
        MessageUtils.sendMessage(this.editor, "&a\u2714 Layout saved for &e" + kitName + "&a!" + offhandInfo);
        this.editor.playSound(this.editor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.editor.closeInventory();
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> new PlayerKitSelectorGUI(this.plugin, this.editor).open(this.editor));
    }

    private void resetLayout() {
        this.plugin.getKitManager().deleteCustomLayout(this.editorUUID, this.baseKit.getName());
        MessageUtils.sendMessage(this.editor, "&eLayout reset to default!");
        this.editor.playSound(this.editor.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        ItemStack[] armor = this.baseKit.getArmorContents();
        if (armor != null) {
            for (int i = 0; i < 4 && i < armor.length; ++i) {
                this.armorContents[i] = armor[i] != null ? armor[i].clone() : null;
            }
        }
        this.offhandItem = this.baseKit.getOffhand() != null ? this.baseKit.getOffhand().clone() : null;
        this.refresh(this.editor);
    }

    @Override
    public void onClose(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            for (int slot = 18; slot <= 53; ++slot) {
                ItemStack existing = this.inventory.getItem(slot);
                if (existing != null && existing.getType() != Material.AIR) continue;
                this.inventory.setItem(slot, cursor);
                player.setItemOnCursor(null);
                return;
            }
            player.getWorld().dropItemNaturally(player.getLocation(), cursor);
            player.setItemOnCursor(null);
        }
    }
}

