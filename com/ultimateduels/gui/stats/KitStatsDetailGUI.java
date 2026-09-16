/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.gui.stats;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.stats.StatsGUI;
import com.ultimateduels.stats.KitStats;
import com.ultimateduels.stats.PlayerStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class KitStatsDetailGUI
extends AbstractGUI {
    private final PlayerStats stats;
    private final UUID targetUUID;
    private final String targetName;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public KitStatsDetailGUI(UltimateDuels plugin, Player targetPlayer) {
        super(plugin, "&6&l" + targetPlayer.getName() + "'s Kit Stats", 6);
        this.targetUUID = targetPlayer.getUniqueId();
        this.targetName = targetPlayer.getName();
        this.stats = plugin.getStatsManager().getStats(targetPlayer.getUniqueId());
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.ORANGE_STAINED_GLASS_PANE);
        if (this.stats == null || this.stats.getAllKitStats().isEmpty()) {
            this.setItem(22, this.createItem(Material.BARRIER, "&c&lNo Kit Stats", "", "&7This player has no", "&7kit statistics yet."));
            this.setItem(49, this.createBackButtonItem());
            return;
        }
        Map<String, KitStats> allKitStats = this.stats.getAllKitStats();
        int slot = 10;
        for (Map.Entry<String, KitStats> entry : allKitStats.entrySet()) {
            if (slot >= 43) break;
            String kitName = entry.getKey();
            KitStats kitStats = entry.getValue();
            this.setItem(slot, this.createKitStatItem(kitName, kitStats));
            if (++slot % 9 != 8) continue;
            slot += 2;
        }
        this.setItem(49, this.createBackButtonItem());
    }

    private GUIItem createBackButtonItem() {
        return new GUIItem.Builder(Material.ARROW).name("&e&l\u2190 Back").lore("", "&7Return to main stats").onClick(event -> {
            event.getPlayer().closeInventory();
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                Player target = Bukkit.getPlayer((UUID)this.targetUUID);
                if (target != null && target.isOnline()) {
                    new StatsGUI(this.plugin, target).open(event.getPlayer());
                } else {
                    event.getPlayer().sendMessage(this.plugin.parseText("&cPlayer is no longer online!"));
                }
            }, 1L);
        }).build();
    }

    private ItemStack createKitStatItem(String kitName, KitStats kitStats) {
        Material material = this.getKitMaterial(kitName);
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&6\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501 Stats \u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        lore.add("");
        lore.add("&7ELO: &e" + kitStats.getElo());
        lore.add("");
        int games = kitStats.getTotalGames();
        double winRate = kitStats.getWinRate();
        lore.add("&7Games Played: &f" + games);
        lore.add("&7Wins: &a" + kitStats.getWins());
        lore.add("&7Losses: &c" + kitStats.getLosses());
        if (games > 0) {
            String winRateColor = this.getWinRateColor(winRate);
            lore.add("&7Win Rate: " + winRateColor + this.df.format(winRate) + "%");
        }
        lore.add("");
        double kdr = kitStats.getKDR();
        lore.add("&7Kills: &a" + kitStats.getKills());
        lore.add("&7Deaths: &c" + kitStats.getDeaths());
        String kdrColor = this.getKDRColor(kdr);
        lore.add("&7K/D Ratio: " + kdrColor + this.df.format(kdr));
        lore.add("");
        if (games > 0) {
            lore.add("&7Avg. Kills/Game: &e" + this.df.format(kitStats.getKillsPerGame()));
            lore.add("&7Avg. Deaths/Game: &e" + this.df.format(kitStats.getDeathsPerGame()));
            lore.add("");
        }
        lore.add("&7Current Streak: " + this.getStreakColor(kitStats.getCurrentWinStreak()) + kitStats.getCurrentWinStreak());
        lore.add("&7Best Win Streak: &6" + kitStats.getBestWinStreak());
        if (kitStats.getBestKillStreak() > 0) {
            lore.add("&7Best Kill Streak: &c" + kitStats.getBestKillStreak());
        }
        lore.add("");
        lore.add("&6\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        return this.createItem(material, "&b&l" + this.capitalize(kitName), lore);
    }

    private Material getKitMaterial(String kitName) {
        return switch (kitName.toLowerCase()) {
            case "nodebuff", "no debuff", "pot", "pots" -> Material.POTION;
            case "sword", "classic" -> Material.DIAMOND_SWORD;
            case "axe" -> Material.DIAMOND_AXE;
            case "bow", "archer" -> Material.BOW;
            case "soup" -> Material.MUSHROOM_STEW;
            case "sumo" -> Material.STICK;
            case "uhc" -> Material.GOLDEN_APPLE;
            case "gapple" -> Material.GOLDEN_APPLE;
            case "debuff" -> Material.SPLASH_POTION;
            case "combo" -> Material.FISHING_ROD;
            case "builduhc" -> Material.COBBLESTONE;
            default -> Material.IRON_SWORD;
        };
    }

    private String getKDRColor(double kdr) {
        if (kdr >= 3.0) {
            return "&a";
        }
        if (kdr >= 2.0) {
            return "&e";
        }
        if (kdr >= 1.0) {
            return "&f";
        }
        return "&c";
    }

    private String getWinRateColor(double winRate) {
        if (winRate >= 70.0) {
            return "&a";
        }
        if (winRate >= 50.0) {
            return "&e";
        }
        if (winRate >= 30.0) {
            return "&6";
        }
        return "&c";
    }

    private String getStreakColor(int streak) {
        if (streak >= 10) {
            return "&4&l";
        }
        if (streak >= 5) {
            return "&c";
        }
        if (streak >= 3) {
            return "&e";
        }
        return "&f";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

