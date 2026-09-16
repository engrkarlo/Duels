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
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyInviteGUI
extends AbstractGUI {
    private final PartyManager partyManager;
    private Player currentPlayer;
    private int page = 1;
    private static final int PLAYERS_PER_PAGE = 28;
    private boolean showOnlyAvailable = true;
    private String searchFilter = null;

    public PartyInviteGUI(UltimateDuels plugin) {
        super(plugin, "&5&lInvite Player", 6);
        this.partyManager = plugin.getPartyManager();
    }

    @Override
    public void open(Player player) {
        this.currentPlayer = player;
        super.open(player);
    }

    private Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.PURPLE_STAINED_GLASS_PANE);
        List<Player> players = this.getFilteredPlayers();
        int totalPages = Math.max(1, (int)Math.ceil((double)players.size() / 28.0));
        int startIndex = (this.page - 1) * 28;
        int endIndex = Math.min(startIndex + 28, players.size());
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
            Player target = players.get(i);
            int slot = slots[slotIndex++];
            this.setItem(slot, this.createPlayerItem(target), event -> this.handleInviteClick(event.getPlayer(), target));
        }
        this.setItem(2, this.createFilterToggle());
        this.setItem(4, this.createSearchButton());
        this.setItem(6, this.createInfoItem(players.size()));
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
        this.setItem(50, new GUIItem.Builder(Material.SUNFLOWER).name("&e&lRefresh").lore("", "&7Refresh player list", "", "&eClick to refresh").onClick(event -> {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        }).build());
    }

    private List<Player> getFilteredPlayers() {
        Player viewer = this.getCurrentPlayer();
        return Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals((Object)viewer)).filter(p -> {
            if (this.showOnlyAvailable) {
                UUID playerUUID = p.getUniqueId();
                if (this.partyManager.isInParty(playerUUID)) {
                    return false;
                }
                if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(playerUUID)) {
                    return false;
                }
                if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(playerUUID)) {
                    return false;
                }
            }
            return true;
        }).filter(p -> {
            if (this.searchFilter != null && !this.searchFilter.isEmpty()) {
                return p.getName().toLowerCase().contains(this.searchFilter.toLowerCase());
            }
            return true;
        }).sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList());
    }

    private ItemStack createPlayerItem(Player target) {
        boolean inQueue;
        UUID targetUUID = target.getUniqueId();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        boolean inParty = this.partyManager.isInParty(targetUUID);
        boolean inDuel = this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(targetUUID);
        boolean inFFA = this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(targetUUID);
        boolean bl = inQueue = this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(target);
        if (inParty) {
            Party party = this.partyManager.getParty(targetUUID);
            lore.add("&7Status: &dIn Party");
            if (party != null) {
                lore.add("&7Leader: &f" + party.getLeaderName());
            }
        } else if (inDuel) {
            lore.add("&7Status: &cIn Duel");
        } else if (inFFA) {
            lore.add("&7Status: &6In FFA");
        } else if (inQueue) {
            lore.add("&7Status: &eIn Queue");
        } else {
            lore.add("&7Status: &aAvailable");
        }
        PlayerStats stats = this.plugin.getStatsManager().getStats(targetUUID);
        if (stats != null) {
            lore.add("");
            lore.add("&7Elo: &f" + stats.getGlobalElo());
            lore.add("&7K/D: &f" + String.format("%.2f", stats.getKDR()));
        }
        lore.add("");
        if (inParty) {
            lore.add("&cAlready in a party");
        } else {
            lore.add("&aClick to invite");
        }
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(target.getName()).name("&e" + target.getName()).lore(lore).glowing(!inParty && !inDuel && !inFFA).buildItem();
    }

    private GUIItem createFilterToggle() {
        return new GUIItem.Builder(this.showOnlyAvailable ? Material.LIME_DYE : Material.GRAY_DYE).name("&6&lFilter").lore("", "&7Show only available: " + (this.showOnlyAvailable ? "&aYes" : "&cNo"), "", "&eClick to toggle").onClick(event -> {
            this.showOnlyAvailable = !this.showOnlyAvailable;
            this.page = 1;
            this.refresh(event.getPlayer());
        }).build();
    }

    private ItemStack createSearchButton() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (this.searchFilter != null) {
            lore.add("&7Current: &f" + this.searchFilter);
            lore.add("");
            lore.add("&eClick to clear");
        } else {
            lore.add("&7Type in chat to search");
            lore.add("");
            lore.add("&eClick to enable search");
        }
        return new GUIItem.Builder(Material.OAK_SIGN).name("&b&lSearch").lore(lore).onClick(event -> {
            if (this.searchFilter != null) {
                this.searchFilter = null;
                this.refresh(event.getPlayer());
            } else {
                event.getPlayer().closeInventory();
                MessageUtils.sendMessage(event.getPlayer(), "&eType a player name to search, or 'cancel' to cancel:");
            }
        }).buildItem();
    }

    private ItemStack createInfoItem(int playerCount) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Players shown: &f" + playerCount);
        lore.add("&7Online: &f" + Bukkit.getOnlinePlayers().size());
        lore.add("");
        lore.add("&7Click a player to invite");
        lore.add("&7them to your party.");
        lore.add("");
        return this.createItem(Material.BOOK, "&5&lInvite Players", lore);
    }

    private ItemStack createPageIndicator(int totalPages) {
        return this.createItem(Material.PAPER, "&7Page " + this.page + "/" + totalPages, new String[0]);
    }

    private void handleInviteClick(Player player, Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!target.isOnline()) {
            MessageUtils.sendMessage(player, "&c" + target.getName() + " is no longer online!");
            this.refresh(player);
            return;
        }
        if (this.partyManager.isInParty(targetUUID)) {
            MessageUtils.sendMessage(player, "&c" + target.getName() + " is already in a party!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        PartyResult result = this.partyManager.invite(player, target);
        if (result.success()) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            this.refresh(player);
        } else {
            player.sendMessage(result.message());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

