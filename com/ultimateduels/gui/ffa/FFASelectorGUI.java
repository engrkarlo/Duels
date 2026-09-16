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
package com.ultimateduels.gui.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.ffa.model.FFAPlayerData;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class FFASelectorGUI
extends AbstractGUI {
    private final FFAManager ffaManager;
    private Player currentPlayer;
    private int page = 1;
    private static final int ARENAS_PER_PAGE = 21;
    private static final String SECTION = "ffa-selector";

    public FFASelectorGUI(UltimateDuels plugin) {
        super(plugin, GUIMessages.getTitle(null, SECTION, "&6&lFFA Arenas"), 5);
        this.ffaManager = plugin.getFFAManager();
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
        this.fillBorder(Material.ORANGE_STAINED_GLASS_PANE);
        List arenas = this.ffaManager.getAllArenas().stream().filter(FFAArenaInstance::isEnabled).collect(Collectors.toList());
        int totalPages = Math.max(1, (int)Math.ceil((double)arenas.size() / 21.0));
        int startIndex = (this.page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, arenas.size());
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        if (arenas.isEmpty()) {
            String noArenasTitle = GUIMessages.getText(this.getCurrentPlayer(), SECTION, "no-arenas-title", "&c&lNo FFA Arenas");
            String noArenasLine1 = GUIMessages.getText(this.getCurrentPlayer(), SECTION, "no-arenas-line1", "&7No FFA arenas are available.");
            String noArenasLine2 = GUIMessages.getText(this.getCurrentPlayer(), SECTION, "no-arenas-line2", "&7Please try again later.");
            this.setItem(22, this.createItem(Material.BARRIER, noArenasTitle, "", noArenasLine1, noArenasLine2));
        } else {
            int slotIndex = 0;
            for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
                FFAArenaInstance arena = (FFAArenaInstance)arenas.get(i);
                int slot = slots[slotIndex++];
                this.setItem(slot, this.createArenaItem(arena), event -> this.handleArenaClick((AbstractGUI.GUIClickEvent)event, arena));
            }
        }
        this.setItem(4, this.createInfoItem());
        if (this.page > 1) {
            this.setItem(36, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        if (this.page < totalPages) {
            this.setItem(44, GUIItem.nextPage(this.page, totalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(40, this.createCurrentArenaItemOrClose());
    }

    private ItemStack createArenaItem(FFAArenaInstance arena) {
        DuelKit kit = this.plugin.getKitManager().getAdminKit(arena.getKitName());
        Material icon = Material.IRON_SWORD;
        Player viewer = this.getCurrentPlayer();
        String kitLabel = GUIMessages.getText(viewer, SECTION, "arena-kit-label", "&7Kit: &f{kit}").replace("{kit}", arena.getKitName());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(kitLabel);
        int players = arena.getPlayerCount();
        int maxPlayers = 50;
        String playerColor = players >= maxPlayers ? "&c" : ((double)players >= (double)maxPlayers * 0.8 ? "&e" : "&a");
        String playersLabel = GUIMessages.getText(viewer, SECTION, "arena-players-label", "&7Players: {color}{current}&7/&f{max}").replace("{color}", playerColor).replace("{current}", String.valueOf(players)).replace("{max}", String.valueOf(maxPlayers));
        lore.add(playersLabel);
        lore.add(this.createFillBar(players, maxPlayers));
        lore.add("");
        if (kit != null && kit.getDescription() != null) {
            lore.add("&7" + kit.getDescription());
            lore.add("");
        }
        if (players >= maxPlayers) {
            lore.add(GUIMessages.getText(viewer, SECTION, "arena-full", "&c\u26a0 Arena is full!"));
        } else {
            lore.add(GUIMessages.getText(viewer, SECTION, "click-to-join", "&aClick to join!"));
        }
        return new GUIItem.Builder(icon).name("&e&l" + arena.getArenaName()).lore(lore).glowing(players > 0).buildItem();
    }

    private String createFillBar(int current, int max) {
        int bars = 10;
        int filled = max > 0 ? (int)((double)current / (double)max * (double)bars) : 0;
        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < bars; ++i) {
            if (i < filled) {
                bar.append("&a\u2588");
                continue;
            }
            bar.append("&7\u2591");
        }
        bar.append("&8]");
        return bar.toString();
    }

    private ItemStack createInfoItem() {
        int totalPlayers = 0;
        int enabledArenas = 0;
        for (FFAArenaInstance arena : this.ffaManager.getAllArenas()) {
            totalPlayers += arena.getPlayerCount();
            if (!arena.isEnabled()) continue;
            ++enabledArenas;
        }
        Player viewer = this.getCurrentPlayer();
        String infoTitle = GUIMessages.getText(viewer, SECTION, "info-title", "&6&lFFA Arenas");
        String totalInFfaLabel = GUIMessages.getText(viewer, SECTION, "total-in-ffa", "&7Total in FFA: &f{count}").replace("{count}", String.valueOf(totalPlayers));
        String availableArenasLabel = GUIMessages.getText(viewer, SECTION, "available-arenas", "&7Available Arenas: &f{count}").replace("{count}", String.valueOf(enabledArenas));
        String joinLine1 = GUIMessages.getText(viewer, SECTION, "join-line1", "&7Join an FFA arena to");
        String joinLine2 = GUIMessages.getText(viewer, SECTION, "join-line2", "&7practice and fight!");
        String deathInfo = GUIMessages.getText(viewer, SECTION, "death-sends-lobby", "&eDeath sends you to lobby");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(totalInFfaLabel);
        lore.add(availableArenasLabel);
        lore.add("");
        lore.add(joinLine1);
        lore.add(joinLine2);
        lore.add("");
        lore.add(deathInfo);
        lore.add("");
        return this.createItem(Material.TOTEM_OF_UNDYING, infoTitle, lore);
    }

    private GUIItem createCurrentArenaItemOrClose() {
        String arenaName;
        FFAArenaInstance arena;
        UUID playerUUID;
        Player player = this.getCurrentPlayer();
        UUID uUID = playerUUID = player != null ? player.getUniqueId() : null;
        if (playerUUID != null && this.ffaManager.isInFFA(playerUUID) && (arena = this.ffaManager.getArena(arenaName = this.ffaManager.getPlayerArena(playerUUID))) != null) {
            FFAPlayerData data = this.ffaManager.getPlayerData(playerUUID);
            int streak = data != null ? data.getKillstreak() : 0;
            String leaveTitle = GUIMessages.getText(player, SECTION, "leave-ffa-title", "&c&lLeave FFA");
            String arenaLabel = GUIMessages.getText(player, SECTION, "current-arena-label", "&7Arena: &f{arena}").replace("{arena}", arena.getArenaName());
            String kitLabel = GUIMessages.getText(player, SECTION, "current-kit-label", "&7Kit: &f{kit}").replace("{kit}", arena.getKitName());
            String yourStatsLabel = GUIMessages.getText(player, SECTION, "your-stats-label", "&7Your Stats:");
            String killsLabel = GUIMessages.getText(player, SECTION, "kills-stat", "&a  Kills: &f{kills}").replace("{kills}", String.valueOf(data != null ? data.getKills() : 0));
            String deathsLabel = GUIMessages.getText(player, SECTION, "deaths-stat", "&c  Deaths: &f{deaths}").replace("{deaths}", String.valueOf(data != null ? data.getDeaths() : 0));
            String streakLabel = GUIMessages.getText(player, SECTION, "streak-stat", "&6  Streak: &f{streak}").replace("{streak}", String.valueOf(streak));
            String clickToLeave = GUIMessages.getText(player, SECTION, "click-to-leave", "&cClick to leave");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("");
            lore.add(arenaLabel);
            lore.add(kitLabel);
            lore.add("");
            lore.add(yourStatsLabel);
            lore.add(killsLabel);
            lore.add(deathsLabel);
            lore.add(streakLabel);
            lore.add("");
            lore.add(clickToLeave);
            return new GUIItem.Builder(Material.RED_BED).name(leaveTitle).lore(lore).onClick(event -> {
                event.getPlayer().closeInventory();
                this.ffaManager.leave(event.getPlayer(), true);
            }).build();
        }
        return GUIItem.closeButton();
    }

    private void handleArenaClick(AbstractGUI.GUIClickEvent event, FFAArenaInstance arena) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (this.ffaManager.isInFFA(playerUUID)) {
            String current = this.ffaManager.getPlayerArena(playerUUID);
            if (current != null && current.equalsIgnoreCase(arena.getArenaName())) {
                MessageUtils.sendMessage(player, "&cYou are already in this arena!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            this.ffaManager.leave(player, false);
        }
        if (this.plugin.getDuelManager().isInMatch(playerUUID)) {
            MessageUtils.sendMessage(player, "&cYou cannot join FFA while in a duel!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (this.plugin.getQueueManager().isInQueue(playerUUID)) {
            MessageUtils.sendMessage(player, "&cYou must leave the queue first!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        int maxPlayers = 50;
        if (arena.getPlayerCount() >= maxPlayers) {
            MessageUtils.sendMessage(player, "&cThis arena is full!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        player.closeInventory();
        this.ffaManager.join(player, arena.getArenaName());
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

