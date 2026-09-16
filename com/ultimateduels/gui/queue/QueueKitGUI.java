/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
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
import com.ultimateduels.gui.kit.KitPreviewGUI;
import com.ultimateduels.gui.queue.QueueMainGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.queue.model.QueueEntry;
import com.ultimateduels.queue.model.QueueResult;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class QueueKitGUI
extends AbstractGUI {
    private final String kitName;
    private final DuelKit kit;
    private final QueueManager queueManager;
    private static final String SECTION = "queue-kit";

    public QueueKitGUI(UltimateDuels plugin, String kitName) {
        super(plugin, GUIMessages.getTitle(null, SECTION, "&6&l" + kitName + " Queue").replace("{kit}", kitName), 4);
        this.kitName = kitName;
        this.kit = plugin.getKitManager().getAdminKit(kitName);
        this.queueManager = plugin.getQueueManager();
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        if (this.kit == null) {
            this.setItem(13, this.createItem(Material.BARRIER, "&c&lKit Not Found", "&7The kit '" + this.kitName + "' does not exist."));
            return;
        }
        this.setItem(13, this.createKitInfoItem());
        this.setItem(11, this.createQueueStatsItem());
        this.setItem(15, this.createPlayersInQueueItem());
        this.setItem(29, this.createJoinQueueButton());
        this.setItem(31, this.createPreviewKitButton());
        this.setItem(33, this.createLeaveQueueButton());
        this.setItem(27, GUIItem.backButton(event -> {
            Player player = event.getPlayer();
            player.closeInventory();
            new QueueMainGUI(this.plugin).open(player);
        }));
        this.setItem(35, GUIItem.closeButton());
    }

    private ItemStack createKitInfoItem() {
        Material iconMaterial;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        String description = this.kit.getDescription();
        if (description != null && !description.isEmpty()) {
            lore.add("&7" + description);
            lore.add("");
        }
        int itemCount = this.kit.getItemCount();
        String itemsLabel = GUIMessages.getText(null, SECTION, "kit-items-label", "&7Items: &f{count}").replace("{count}", String.valueOf(itemCount));
        String armorLabel = GUIMessages.getText(null, SECTION, "kit-armor-label", "&7Armor: &f{status}").replace("{status}", this.hasArmor() ? "Yes" : "No");
        String offhandLabel = GUIMessages.getText(null, SECTION, "kit-offhand-label", "&7Offhand: &f{status}").replace("{status}", this.kit.hasOffhand() ? "Yes" : "No");
        lore.add(itemsLabel);
        lore.add(armorLabel);
        lore.add(offhandLabel);
        lore.add("");
        if (this.kit.isHungerLocked()) {
            lore.add(GUIMessages.getText(null, SECTION, "no-hunger", "&a\u2713 &7No Hunger"));
        }
        if (this.kit.isAllowBuilding()) {
            lore.add(GUIMessages.getText(null, SECTION, "building-enabled", "&a\u2713 &7Building Enabled"));
        }
        if ((iconMaterial = this.kit.getIcon()) == Material.AIR) {
            iconMaterial = Material.DIAMOND_SWORD;
        }
        String kitDisplayName = GUIMessages.getText(null, SECTION, "kit-display-name", "&e&l{kit}").replace("{kit}", this.kit.getDisplayName());
        return this.createItem(iconMaterial, kitDisplayName, lore);
    }

    private ItemStack createQueueStatsItem() {
        int inQueue = this.queueManager.getQueueSize(this.kitName);
        int fighting = this.getFightingCount();
        String titleText = GUIMessages.getText(null, SECTION, "queue-stats-title", "&6&lQueue Statistics");
        String inQueueText = GUIMessages.getText(null, SECTION, "players-in-queue", "&7Players in Queue: &a{count}").replace("{count}", String.valueOf(inQueue));
        String fightingText = GUIMessages.getText(null, SECTION, "currently-fighting", "&7Currently Fighting: &c{count}").replace("{count}", String.valueOf(fighting));
        String avgWaitText = GUIMessages.getText(null, SECTION, "average-wait-time", "&7Average Wait Time: &f{time}").replace("{time}", this.getAverageWaitTime());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(inQueueText);
        lore.add(fightingText);
        lore.add("");
        lore.add(avgWaitText);
        lore.add("");
        return this.createItem(Material.CLOCK, titleText, lore);
    }

    private ItemStack createPlayersInQueueItem() {
        Queue<QueueEntry> queue = this.queueManager.getKitQueue(this.kitName);
        String titleText = GUIMessages.getText(null, SECTION, "queued-players-title", "&b&lQueued Players");
        String noPlayersText = GUIMessages.getText(null, SECTION, "no-players-queued", "&7No players in queue");
        String playersWaitingText = GUIMessages.getText(null, SECTION, "players-waiting-label", "&7Players waiting:");
        String andMoreText = GUIMessages.getText(null, SECTION, "and-more", "&7... and {count} more");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (queue.isEmpty()) {
            lore.add(noPlayersText);
        } else {
            lore.add(playersWaitingText);
            int shown = 0;
            for (QueueEntry entry : queue) {
                if (shown >= 10) {
                    lore.add(andMoreText.replace("{count}", String.valueOf(queue.size() - shown)));
                    break;
                }
                Player p = Bukkit.getPlayer((UUID)entry.getPlayerUUID());
                if (p == null) continue;
                long waitTime = this.queueManager.getQueueTime(entry.getPlayerUUID());
                lore.add("&8- &f" + p.getName() + " &7(" + this.formatTime(waitTime) + ")");
                ++shown;
            }
        }
        lore.add("");
        return this.createItem(Material.PLAYER_HEAD, titleText, lore);
    }

    private ItemStack createJoinQueueButton() {
        String titleText = GUIMessages.getText(null, SECTION, "join-queue-title", "&a&lJoin Queue");
        String clickText = GUIMessages.getText(null, SECTION, "join-queue-description", "&7Click to join the queue");
        String forKitText = GUIMessages.getText(null, SECTION, "for-kit", "&7for &e{kit}").replace("{kit}", this.kit.getDisplayName());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(clickText);
        lore.add(forKitText);
        lore.add("");
        return this.createItem(Material.LIME_WOOL, titleText, lore);
    }

    private ItemStack createPreviewKitButton() {
        String titleText = GUIMessages.getText(null, SECTION, "preview-kit-title", "&e&lPreview Kit");
        return this.createItem(Material.CHEST, titleText, "", GUIMessages.getText(null, SECTION, "preview-kit-line1", "&7Click to preview the"), GUIMessages.getText(null, SECTION, "preview-kit-line2", "&7contents of this kit"), "");
    }

    private ItemStack createLeaveQueueButton() {
        String titleText = GUIMessages.getText(null, SECTION, "leave-queue-title", "&c&lLeave Queue");
        return this.createItem(Material.RED_WOOL, titleText, "", GUIMessages.getText(null, SECTION, "leave-queue-line1", "&7Click to leave the queue"), GUIMessages.getText(null, SECTION, "leave-queue-line2", "&7if you are currently queued"), "");
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
        switch (slot) {
            case 29: {
                this.handleJoinQueue(player);
                break;
            }
            case 31: {
                this.handlePreviewKit(player);
                break;
            }
            case 33: {
                this.handleLeaveQueue(player);
            }
        }
    }

    private void handleJoinQueue(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (this.queueManager.isInQueue(playerUUID)) {
            String currentKit = this.queueManager.getQueuedKit(playerUUID);
            if (currentKit != null && currentKit.equalsIgnoreCase(this.kitName)) {
                MessageUtils.sendMessage(player, "&cYou are already queued for this kit!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            this.queueManager.leaveQueue(player);
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot queue while in a duel!");
            return;
        }
        QueueResult result = this.queueManager.joinQueue(player, this.kitName);
        if (result.success()) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            MessageUtils.sendMessage(player, result.message());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    private void handlePreviewKit(Player player) {
        player.closeInventory();
        new KitPreviewGUI(this.plugin, this.kit).open(player);
    }

    private void handleLeaveQueue(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!this.queueManager.isInQueue(playerUUID)) {
            MessageUtils.sendMessage(player, "&cYou are not in a queue!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        this.queueManager.leaveQueue(player);
        MessageUtils.sendMessage(player, "&cYou left the queue.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        this.refresh(player);
    }

    private boolean hasArmor() {
        ItemStack[] armor = this.kit.getArmorContents();
        if (armor == null) {
            return false;
        }
        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            return true;
        }
        return false;
    }

    private int getFightingCount() {
        if (this.plugin.getDuelManager() != null) {
            return this.plugin.getDuelManager().getPlayersInMatchForKit(this.kitName.toLowerCase());
        }
        return 0;
    }

    private String getAverageWaitTime() {
        Queue<QueueEntry> queue = this.queueManager.getKitQueue(this.kitName);
        if (queue.isEmpty()) {
            return "N/A";
        }
        long totalWait = 0L;
        int count = 0;
        for (QueueEntry entry : queue) {
            Player p = Bukkit.getPlayer((UUID)entry.getPlayerUUID());
            if (p == null) continue;
            totalWait += this.queueManager.getQueueTime(entry.getPlayerUUID());
            ++count;
        }
        if (count == 0) {
            return "N/A";
        }
        return this.formatTime(totalWait / (long)count);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000L;
        if (seconds < 60L) {
            return seconds + "s";
        }
        long minutes = seconds / 60L;
        return minutes + "m " + (seconds %= 60L) + "s";
    }
}

