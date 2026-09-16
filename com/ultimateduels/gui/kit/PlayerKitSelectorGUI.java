/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.gui.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.gui.kit.PlayerKitEditorGUI;
import com.ultimateduels.kit.model.DuelKit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class PlayerKitSelectorGUI
extends AbstractGUI {
    private static final String SECTION = "player-kit-selector";
    private final Player editor;
    private static final int START_SLOT = 10;
    private static final int END_SLOT = 43;
    private static final List<Integer> EXCLUDED_SLOTS = List.of(Integer.valueOf(17), Integer.valueOf(18), Integer.valueOf(26), Integer.valueOf(27), Integer.valueOf(35), Integer.valueOf(36));
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int INFO_SLOT = 4;
    private final Map<Integer, DuelKit> slotKitMap = new HashMap<Integer, DuelKit>();

    public PlayerKitSelectorGUI(UltimateDuels plugin, Player player) {
        super(plugin, GUIMessages.getTitle(player, SECTION, "&6&lSelect Kit to Edit"), 6);
        this.editor = player;
    }

    @Override
    protected void initializeItems() {
        this.slotKitMap.clear();
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        this.inventory.setItem(4, this.createInfoItem());
        String backName = GUIMessages.getItemName(this.editor, SECTION, "back-name", "<red>Back");
        List<String> backLore = GUIMessages.getItemLore(this.editor, SECTION, "back", List.of("<gray>Return to previous menu"));
        this.inventory.setItem(49, this.createItem(Material.ARROW, backName, backLore));
        int slot = 10;
        ArrayList<DuelKit> kits = new ArrayList<DuelKit>(this.plugin.getKitManager().getAllAdminKits());
        for (DuelKit kit : kits) {
            while (EXCLUDED_SLOTS.contains(slot) && slot <= 43) {
                ++slot;
            }
            if (slot > 43) break;
            ItemStack kitItem = this.createKitItem(kit);
            this.inventory.setItem(slot, kitItem);
            this.slotKitMap.put(slot, kit);
            ++slot;
        }
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            return;
        }
        DuelKit kit = this.slotKitMap.get(slot);
        if (kit != null) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> new PlayerKitEditorGUI(this.plugin, player, kit).open(player));
        }
    }

    private ItemStack createKitItem(DuelKit kit) {
        ItemStack item;
        ItemMeta meta;
        Material icon = kit.getIcon();
        if (icon == null || icon == Material.AIR) {
            icon = Material.DIAMOND_SWORD;
        }
        if ((meta = (item = new ItemStack(icon)).getItemMeta()) != null) {
            String displayName = kit.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = kit.getName();
            }
            meta.displayName(this.plugin.parseText("&e&l" + displayName));
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add(Component.empty());
            String description = kit.getDescription();
            if (description != null && !description.isEmpty()) {
                lore.add(this.plugin.parseText("&7" + description));
                lore.add(Component.empty());
            }
            int itemCount = this.countKitItems(kit);
            boolean hasArmor = this.hasArmor(kit);
            boolean hasOffhand = this.hasOffhand(kit);
            String itemsText = GUIMessages.getText(this.editor, SECTION, "kit-items", "&7Items: &f{count}").replace("{count}", String.valueOf(itemCount));
            String armorText = GUIMessages.getText(this.editor, SECTION, "kit-armor", "&7Armor: &f{status}").replace("{status}", hasArmor ? "Yes" : "No");
            String offhandText = GUIMessages.getText(this.editor, SECTION, "kit-offhand", "&7Offhand: &f{status}").replace("{status}", hasOffhand ? "Yes" : "No");
            lore.add(this.plugin.parseText(itemsText));
            lore.add(this.plugin.parseText(armorText));
            lore.add(this.plugin.parseText(offhandText));
            lore.add(Component.empty());
            if (this.hasCustomLayout(kit)) {
                lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "has-custom-layout", "&a\u2713 Custom layout saved")));
            } else {
                lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "using-default", "&7Using default layout")));
            }
            lore.add(Component.empty());
            lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "click-to-edit", "&eClick to edit layout!")));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean hasCustomLayout(DuelKit kit) {
        try {
            return this.plugin.getKitManager().hasCustomLayout(this.editor.getUniqueId(), kit.getName());
        }
        catch (Exception e) {
            return false;
        }
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String titleText = GUIMessages.getText(this.editor, SECTION, "info-title", "&6&lKit Layout Editor");
            meta.displayName(this.plugin.parseText(titleText));
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add(Component.empty());
            lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "info-line1", "&7Customize where items appear")));
            lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "info-line2", "&7in your inventory for each kit.")));
            lore.add(Component.empty());
            lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "info-line3", "&7Your layouts are saved")));
            lore.add(this.plugin.parseText(GUIMessages.getText(this.editor, SECTION, "info-line4", "&7separately from other players!")));
            lore.add(Component.empty());
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int countKitItems(DuelKit kit) {
        int count = 0;
        ItemStack[] contents = kit.getInventoryContents();
        if (contents != null) {
            for (ItemStack item : contents) {
                if (item == null || item.getType() == Material.AIR) continue;
                ++count;
            }
        }
        return count;
    }

    private boolean hasArmor(DuelKit kit) {
        ItemStack[] armor = kit.getArmorContents();
        if (armor == null) {
            return false;
        }
        for (ItemStack item : armor) {
            if (item == null || item.getType() == Material.AIR) continue;
            return true;
        }
        return false;
    }

    private boolean hasOffhand(DuelKit kit) {
        ItemStack offhand = kit.getOffhand();
        return offhand != null && offhand.getType() != Material.AIR;
    }
}

