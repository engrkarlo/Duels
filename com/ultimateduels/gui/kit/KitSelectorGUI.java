/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.gui.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.kit.KitPreviewGUI;
import com.ultimateduels.gui.kit.PlayerKitEditorGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class KitSelectorGUI
extends AbstractGUI {
    private final Mode mode;
    private int page = 1;
    private static final int KITS_PER_PAGE = 28;

    public KitSelectorGUI(UltimateDuels plugin, Mode mode) {
        super(plugin, "&6&l" + mode.getTitle(), 6);
        this.mode = mode;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        ArrayList<DuelKit> kits = new ArrayList<DuelKit>(this.plugin.getKitManager().getAllAdminKits());
        int totalPages = Math.max(1, (int)Math.ceil((double)kits.size() / 28.0));
        int startIndex = (this.page - 1) * 28;
        int endIndex = Math.min(startIndex + 28, kits.size());
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
            DuelKit kit = (DuelKit)kits.get(i);
            int slot = slots[slotIndex++];
            this.setItem(slot, this.createKitItem(kit), event -> this.handleKitClick((AbstractGUI.GUIClickEvent)event, kit));
        }
        this.setItem(4, this.createInfoItem(kits.size()));
        if (this.page > 1) {
            this.setItem(45, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(49, this.createPageIndicator(totalPages));
        if (this.page < totalPages) {
            this.setItem(53, GUIItem.nextPage(this.page, totalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(48, GUIItem.closeButton());
        if (this.mode == Mode.EDIT) {
            this.setItem(50, this.createNewKitButton());
        }
    }

    private ItemStack createKitItem(DuelKit kit) {
        Material iconMaterial;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        String description = kit.getDescription();
        if (description != null && !description.isEmpty()) {
            lore.add("&7" + description);
            lore.add("");
        }
        int itemCount = this.countKitItems(kit);
        lore.add("&7Items: &f" + itemCount);
        lore.add("&7Armor: &f" + (this.hasArmor(kit) ? "Yes" : "No"));
        lore.add("&7Offhand: &f" + (this.hasOffhand(kit) ? "Yes" : "No"));
        lore.add("&7Effects: &f" + kit.getEffects().size());
        lore.add("");
        String action = switch (this.mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "&eClick to edit";
            case 1 -> "&eClick to preview";
            case 2 -> "&eClick to select";
            case 3 -> "&cClick to delete";
        };
        lore.add(action);
        if (this.mode == Mode.EDIT || this.mode == Mode.PREVIEW) {
            lore.add("&eRight-click to preview");
        }
        if ((iconMaterial = kit.getIcon()) == null) {
            iconMaterial = Material.DIAMOND_SWORD;
        }
        return new GUIItem.Builder(iconMaterial).name("&e&l" + kit.getDisplayName()).lore(lore).glowing(true).buildItem();
    }

    private ItemStack createInfoItem(int totalKits) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7" + this.mode.getDescription());
        lore.add("");
        lore.add("&7Total Kits: &f" + totalKits);
        lore.add("");
        return this.createItem(Material.BOOK, "&6&l" + this.mode.getTitle(), lore);
    }

    private ItemStack createPageIndicator(int totalPages) {
        return this.createItem(Material.PAPER, "&7Page " + this.page + "/" + totalPages, "", "&7Showing kits on this page");
    }

    private GUIItem createNewKitButton() {
        return new GUIItem.Builder(Material.EMERALD).name("&a&lCreate New Kit").lore("", "&7Create a new kit from", "&7your current inventory.", "", "&eClick to create").onClick(event -> {
            event.getPlayer().closeInventory();
            event.getPlayer().performCommand("kit create");
        }).build();
    }

    private void handleKitClick(AbstractGUI.GUIClickEvent event, DuelKit kit) {
        Player player = event.getPlayer();
        if (event.isRightClick() && (this.mode == Mode.EDIT || this.mode == Mode.PREVIEW)) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
                KitPreviewGUI previewGUI = new KitPreviewGUI(this.plugin, kit);
                previewGUI.open(player);
            });
            return;
        }
        switch (this.mode.ordinal()) {
            case 0: {
                if (!player.hasPermission("ultimateduels.admin.kit")) {
                    MessageUtils.sendMessage(player, "&cYou don't have permission to edit kits!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                player.closeInventory();
                this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
                    PlayerKitEditorGUI editorGUI = new PlayerKitEditorGUI(this.plugin, player, kit);
                    editorGUI.open(player);
                });
                break;
            }
            case 1: {
                player.closeInventory();
                this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
                    KitPreviewGUI previewGUI = new KitPreviewGUI(this.plugin, kit);
                    previewGUI.open(player);
                });
                break;
            }
            case 2: {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                break;
            }
            case 3: {
                if (!player.hasPermission("ultimateduels.admin.kit")) {
                    MessageUtils.sendMessage(player, "&cYou don't have permission to delete kits!");
                    return;
                }
                MessageUtils.sendMessage(player, "&cType &e/kit delete " + kit.getName() + " &cto confirm deletion.");
                player.closeInventory();
            }
        }
    }

    private int countKitItems(DuelKit kit) {
        int count = 0;
        ItemStack[] contents = kit.getInventoryContents();
        if (contents != null) {
            for (ItemStack item : contents) {
                if (item == null || item.getType().isAir()) continue;
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
            if (item == null || item.getType().isAir()) continue;
            return true;
        }
        return false;
    }

    private boolean hasOffhand(DuelKit kit) {
        ItemStack offhand = kit.getOffhand();
        return offhand != null && !offhand.getType().isAir();
    }

    @Override
    protected boolean onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
        return true;
    }

    public static enum Mode {
        EDIT("Edit Kit", "Select a kit to edit"),
        PREVIEW("Preview Kit", "Select a kit to preview"),
        SELECT("Select Kit", "Select a kit"),
        DELETE("Delete Kit", "Select a kit to delete");

        private final String title;
        private final String description;

        private Mode(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return this.title;
        }

        public String getDescription() {
            return this.description;
        }
    }
}

