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
package com.ultimateduels.gui.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.party.PartyMainGUI;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.party.model.PartyResult;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyBrowseGUI
extends AbstractGUI {
    private final PartyManager partyManager;
    private int page = 1;
    private static final int PARTIES_PER_PAGE = 28;

    public PartyBrowseGUI(UltimateDuels plugin) {
        super(plugin, "&5&lBrowse Public Parties", 6);
        this.partyManager = plugin.getPartyManager();
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.PURPLE_STAINED_GLASS_PANE);
        List<Party> publicParties = this.partyManager.getPublicParties();
        int totalPages = Math.max(1, (int)Math.ceil((double)publicParties.size() / 28.0));
        int startIndex = (this.page - 1) * 28;
        int endIndex = Math.min(startIndex + 28, publicParties.size());
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        if (publicParties.isEmpty()) {
            this.setItem(22, this.createNoPartiesItem());
        } else {
            int slotIndex = 0;
            for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
                Party party = publicParties.get(i);
                int slot = slots[slotIndex++];
                this.setItem(slot, this.createPartyItem(party), event -> this.handleJoinClick(event.getPlayer(), party));
            }
        }
        this.setItem(4, this.createInfoItem(publicParties.size()));
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
        this.setItem(48, GUIItem.backButton(event -> new PartyMainGUI(this.plugin).open(event.getPlayer())));
        this.setItem(50, new GUIItem.Builder(Material.SUNFLOWER).name("&e&lRefresh").lore("", "&7Refresh party list", "", "&eClick to refresh").onClick(event -> {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        }).build());
    }

    private ItemStack createPartyItem(Party party) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Leader: &f" + party.getLeaderName());
        lore.add("&7Members: &e" + party.getSize() + "/" + this.partyManager.getMaxPartySize());
        lore.add("");
        lore.add("&7Members:");
        int count = 0;
        for (UUID memberId : party.getAllMembers()) {
            if (count >= 5) {
                int remaining = party.getSize() - 5;
                if (remaining <= 0) break;
                lore.add("  &7... and " + remaining + " more");
                break;
            }
            Player member = Bukkit.getPlayer((UUID)memberId);
            String status = member != null && member.isOnline() ? "&a\u25cf" : "&c\u25cf";
            String name = party.getMemberName(memberId);
            if (party.isLeader(memberId)) {
                lore.add("  " + status + " &6\u2605 &f" + name);
            } else {
                lore.add("  " + status + " &f" + name);
            }
            ++count;
        }
        lore.add("");
        lore.add("&aClick to join this party!");
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(party.getLeaderName()).name("&e" + party.getLeaderName() + "'s Party").lore(lore).glowing(true).buildItem();
    }

    private ItemStack createNoPartiesItem() {
        return new GUIItem.Builder(Material.BARRIER).name("&c&lNo Public Parties").lore("", "&7There are no public parties", "&7available right now.", "", "&7Create your own with:", "&e/party create", "&e/party open", "").buildItem();
    }

    private ItemStack createInfoItem(int partyCount) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Public parties: &f" + partyCount);
        lore.add("");
        lore.add("&7Click on a party to join it!");
        lore.add("&7No invite needed for public parties.");
        lore.add("");
        return this.createItem(Material.BOOK, "&5&lPublic Parties", lore);
    }

    private ItemStack createPageIndicator(int totalPages) {
        return this.createItem(Material.PAPER, "&7Page " + this.page + "/" + totalPages, new String[0]);
    }

    private void handleJoinClick(Player player, Party party) {
        Party currentParty = this.partyManager.getPartyById(party.getPartyId());
        if (currentParty == null) {
            MessageUtils.sendMessage(player, "&cThis party no longer exists!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            this.refresh(player);
            return;
        }
        if (!currentParty.isPublic()) {
            MessageUtils.sendMessage(player, "&cThis party is no longer public!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            this.refresh(player);
            return;
        }
        PartyResult result = this.partyManager.joinPublicParty(player, party.getPartyId());
        if (result.success()) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        } else {
            player.sendMessage(result.message());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            this.refresh(player);
        }
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

