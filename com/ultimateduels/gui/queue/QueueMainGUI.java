/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.gui.queue;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.queue.model.QueueResult;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class QueueMainGUI
extends AbstractGUI {
    private static final String SECTION = "queue-main";
    private final QueueManager queueManager;
    private int page = 1;
    private static final int ITEMS_PER_PAGE = 21;
    private Player currentPlayer;

    public QueueMainGUI(UltimateDuels plugin) {
        super(plugin, GUIMessages.getTitle(null, SECTION, "&6&lSelect a Kit"), 6);
        this.queueManager = plugin.getQueueManager();
    }

    @Override
    public void open(Player player) {
        this.currentPlayer = player;
        super.open(player);
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        ArrayList<DuelKit> kits = new ArrayList<DuelKit>();
        for (String kitName : this.plugin.getKitManager().getAdminKitNames()) {
            DuelKit kit = this.plugin.getKitManager().getAdminKit(kitName);
            if (kit == null) continue;
            kits.add(kit);
        }
        int totalPages = (int)Math.ceil((double)kits.size() / 21.0);
        if (totalPages == 0) {
            totalPages = 1;
        }
        int startIndex = (this.page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, kits.size());
        int[] kitSlots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < kitSlots.length; ++i) {
            DuelKit kit = (DuelKit)kits.get(i);
            int slot = kitSlots[slotIndex++];
            this.setItem(slot, this.createKitItem(kit), event -> this.handleKitClick(event.getPlayer(), kit, event.getClickType()));
        }
        if (this.page > 1) {
            this.setItem(48, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        String pageText = GUIMessages.getText(this.currentPlayer, SECTION, "page-info", "&ePage {page}/{total}").replace("{page}", String.valueOf(this.page)).replace("{total}", String.valueOf(Math.max(1, totalPages)));
        String selectKitText = GUIMessages.getText(this.currentPlayer, SECTION, "select-kit-hint", "&7Select a kit to queue");
        String leftClickText = GUIMessages.getText(this.currentPlayer, SECTION, "left-click-join", "&eLeft-click &7to join queue");
        String rightClickText = GUIMessages.getText(this.currentPlayer, SECTION, "right-click-info", "&eRight-click &7for kit info");
        this.setItem(49, this.createItem(Material.PAPER, pageText, selectKitText, "", leftClickText, rightClickText));
        if (this.page < totalPages) {
            int finalTotalPages = totalPages;
            this.setItem(50, GUIItem.nextPage(this.page, finalTotalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(45, this.createQueueStatusItem());
        this.setItem(53, GUIItem.closeButton());
    }

    private ItemStack createKitItem(DuelKit kit) {
        Material icon = kit.getIcon();
        String displayName = kit.getDisplayName();
        String name = GUIMessages.getText(this.currentPlayer, SECTION, "kit-name", "&e&l{kit}").replace("{kit}", displayName);
        int inQueue = this.queueManager.getQueueSize(kit.getName());
        int fighting = this.plugin.getDuelManager().getPlayersInMatchForKit(kit.getName());
        String inQueueLabel = GUIMessages.getText(this.currentPlayer, SECTION, "in-queue-label", "&7In Queue: &a{count}").replace("{count}", String.valueOf(inQueue));
        String fightingLabel = GUIMessages.getText(this.currentPlayer, SECTION, "fighting-label", "&7Fighting: &c{count}").replace("{count}", String.valueOf(fighting));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(inQueueLabel);
        lore.add(fightingLabel);
        lore.add("");
        String description = kit.getDescription();
        if (description != null && !description.isEmpty()) {
            lore.add("&7" + description);
            lore.add("");
        }
        String leftClickText = GUIMessages.getText(this.currentPlayer, SECTION, "left-click-queue", "&eLeft-click &7to join queue");
        String rightClickText = GUIMessages.getText(this.currentPlayer, SECTION, "right-click-preview", "&eRight-click &7to preview kit");
        lore.add(leftClickText);
        lore.add(rightClickText);
        return this.createItem(icon, name, lore);
    }

    private ItemStack createQueueStatusItem() {
        String statusTitle = GUIMessages.getText(this.currentPlayer, SECTION, "queue-status-title", "&6&lQueue Status");
        int totalInQueue = this.queueManager.getTotalQueueSize();
        int totalFighting = this.plugin.getDuelManager().getTotalPlayersInMatches();
        String totalInQueueLabel = GUIMessages.getText(this.currentPlayer, SECTION, "total-in-queue", "&7Total in Queue: &a{count}").replace("{count}", String.valueOf(totalInQueue));
        String totalFightingLabel = GUIMessages.getText(this.currentPlayer, SECTION, "total-fighting", "&7Total Fighting: &c{count}").replace("{count}", String.valueOf(totalFighting));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(totalInQueueLabel);
        lore.add(totalFightingLabel);
        lore.add("");
        return this.createItem(Material.CLOCK, statusTitle, lore);
    }

    private void handleKitClick(Player player, DuelKit kit, ClickType clickType) {
        if (clickType.isRightClick()) {
            player.closeInventory();
            this.showKitPreview(player, kit);
            return;
        }
        if (this.queueManager.isInQueue(player)) {
            String currentKit = this.queueManager.getQueuedKit(player.getUniqueId());
            if (currentKit != null && currentKit.equalsIgnoreCase(kit.getName())) {
                String alreadyQueuedMsg = GUIMessages.getText(player, SECTION, "already-queued-kit", "&cYou are already queued for this kit!");
                MessageUtils.sendMessage(player, alreadyQueuedMsg);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            this.queueManager.leaveQueue(player);
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            String inDuelMsg = GUIMessages.getText(player, SECTION, "cannot-queue-in-duel", "&cYou cannot queue while in a duel!");
            MessageUtils.sendMessage(player, inDuelMsg);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            String inFFAMsg = GUIMessages.getText(player, SECTION, "cannot-queue-in-ffa", "&cYou cannot queue while in FFA!");
            MessageUtils.sendMessage(player, inFFAMsg);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        QueueResult result = this.queueManager.joinQueue(player, kit.getName());
        if (result.success()) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            player.sendMessage(result.message());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    private void showKitPreview(Player player, DuelKit kit) {
        String headerLine = GUIMessages.getText(player, SECTION, "preview-header", "\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        String titleLine = GUIMessages.getText(player, SECTION, "preview-title", "\u00a7e\u00a7lKit: \u00a7f{kit}").replace("{kit}", kit.getDisplayName());
        player.sendMessage("");
        player.sendMessage(headerLine);
        player.sendMessage(titleLine);
        player.sendMessage("");
        String description = kit.getDescription();
        if (description != null && !description.isEmpty()) {
            player.sendMessage("\u00a77" + description);
            player.sendMessage("");
        }
        String itemsLabel = GUIMessages.getText(player, SECTION, "preview-items", "\u00a77Items: \u00a7f{count}").replace("{count}", String.valueOf(kit.getItemCount()));
        String effectsLabel = GUIMessages.getText(player, SECTION, "preview-effects", "\u00a77Effects: \u00a7f{count}").replace("{count}", String.valueOf(kit.getEffects().size()));
        player.sendMessage(itemsLabel);
        player.sendMessage(effectsLabel);
        if (kit.hasOffhand()) {
            String offhandLabel = GUIMessages.getText(player, SECTION, "preview-offhand", "\u00a77Offhand: \u00a7f{item}").replace("{item}", kit.getOffhand().getType().name());
            player.sendMessage(offhandLabel);
        }
        player.sendMessage(headerLine);
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

