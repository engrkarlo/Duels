/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.SkullMeta
 */
package com.ultimateduels.gui.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIMessages;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class DuelPlayerGUI
extends AbstractGUI {
    private static final String SECTION = "duel-player";
    private final UltimateDuels plugin;
    private final UUID openerUUID;
    private int page = 0;
    private static final int PLAYERS_PER_PAGE = 36;

    public DuelPlayerGUI(UltimateDuels plugin, Player opener) {
        super(plugin, GUIMessages.getTitle(opener, SECTION, "&6&lSelect a Player to Duel"), 6);
        this.plugin = plugin;
        this.openerUUID = opener.getUniqueId();
    }

    @Override
    protected void initializeItems() {
        this.inventory.clear();
        Player opener = Bukkit.getPlayer((UUID)this.openerUUID);
        ArrayList<Player> targets = new ArrayList<Player>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(this.openerUUID) || this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(online.getUniqueId()) || this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(online.getUniqueId()) || this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(online.getUniqueId())) continue;
            targets.add(online);
        }
        int startIndex = this.page * 36;
        int endIndex = Math.min(startIndex + 36, targets.size());
        String clickToDuel = GUIMessages.getText(opener, SECTION, "click-to-duel", "Click to duel!");
        String pingLabel = GUIMessages.getText(opener, SECTION, "ping-label", "Ping: {ping}ms");
        for (int i = startIndex; i < endIndex; ++i) {
            Player target = (Player)targets.get(i);
            int slot = i - startIndex;
            String pingText = pingLabel.replace("{ping}", String.valueOf(target.getPing()));
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta)head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer((OfflinePlayer)target);
                meta.displayName((Component)Component.text((String)target.getName(), (TextColor)NamedTextColor.YELLOW));
                meta.lore(List.of(Component.text((String)clickToDuel, (TextColor)NamedTextColor.GRAY), Component.text((String)pingText, (TextColor)NamedTextColor.DARK_GRAY)));
                head.setItemMeta((ItemMeta)meta);
            }
            this.setItem(slot, head, event -> {
                Player clicker = event.getPlayer();
                this.handlePlayerHeadClick(clicker, target);
            });
        }
        if (this.page > 0) {
            String prevText = GUIMessages.getItemName(opener, SECTION, "previous-page", "&e&lPrevious Page");
            ItemStack prevArrow = this.createItem(Material.ARROW, prevText, "&7Click to go back");
            this.setItem(45, prevArrow, event -> {
                --this.page;
                this.initializeItems();
            });
        }
        if (endIndex < targets.size()) {
            String nextText = GUIMessages.getItemName(opener, SECTION, "next-page", "&e&lNext Page");
            ItemStack nextArrow = this.createItem(Material.ARROW, nextText, "&7Click to continue");
            this.setItem(53, nextArrow, event -> {
                ++this.page;
                this.initializeItems();
            });
        }
        String closeText = GUIMessages.getItemName(opener, SECTION, "close", "&c&lClose");
        ItemStack closeButton = this.createItem(Material.BARRIER, closeText, "&7Click to close");
        this.setItem(49, closeButton, event -> event.getPlayer().closeInventory());
    }

    private void handlePlayerHeadClick(Player clicker, Player target) {
        if (target == null || !target.isOnline()) {
            clicker.sendMessage((Component)Component.text((String)"Player is no longer online!", (TextColor)NamedTextColor.RED));
            clicker.closeInventory();
            return;
        }
        clicker.closeInventory();
        this.plugin.getGUIManager().openDuelRequestGUI(clicker, target);
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

