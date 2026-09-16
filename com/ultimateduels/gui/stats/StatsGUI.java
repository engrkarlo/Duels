/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.gui.stats.KitStatsDetailGUI;
import com.ultimateduels.stats.KitStats;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.stats.StatsManager;
import com.ultimateduels.utils.MessageUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class StatsGUI
extends AbstractGUI {
    private final StatsManager statsManager;
    private final Player targetPlayer;
    private final PlayerStats stats;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static final String SECTION = "stats";

    public StatsGUI(UltimateDuels plugin, Player target) {
        super(plugin, GUIMessages.getTitle(target, SECTION, "&6&l{player}'s Stats").replace("{player}", target.getName()), 5);
        this.statsManager = plugin.getStatsManager();
        this.targetPlayer = target;
        this.stats = this.statsManager.getStats(target.getUniqueId());
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.ORANGE_STAINED_GLASS_PANE);
        if (this.stats == null) {
            String noStatsTitle = GUIMessages.getText(this.targetPlayer, SECTION, "no-stats-title", "&c&lNo Stats Found");
            String noStatsLine1 = GUIMessages.getText(this.targetPlayer, SECTION, "no-stats-line1", "&7This player has no");
            String noStatsLine2 = GUIMessages.getText(this.targetPlayer, SECTION, "no-stats-line2", "&7recorded statistics.");
            this.setItem(22, this.createItem(Material.BARRIER, noStatsTitle, "", noStatsLine1, noStatsLine2));
            this.setItem(40, GUIItem.closeButton());
            return;
        }
        this.setItem(4, this.createPlayerHead());
        this.setItem(19, this.createCombatStats());
        this.setItem(21, this.createMatchStats());
        this.setItem(23, this.createStreakStats());
        this.setItem(25, this.createRankStats());
        this.setItem(31, this.createKitStats());
        String compareTitle = GUIMessages.getText(this.targetPlayer, SECTION, "compare-title", "&b&lCompare");
        String compareLine1 = GUIMessages.getText(this.targetPlayer, SECTION, "compare-line1", "&7Compare stats with");
        String compareLine2 = GUIMessages.getText(this.targetPlayer, SECTION, "compare-line2", "&7another player.");
        String clickToCompare = GUIMessages.getText(this.targetPlayer, SECTION, "click-to-compare", "&eClick to compare");
        this.setItem(38, new GUIItem.Builder(Material.COMPARATOR).name(compareTitle).lore("", compareLine1, compareLine2, "", clickToCompare).onClick(event -> {
            event.getPlayer().closeInventory();
            String compareHint = GUIMessages.getText(event.getPlayer(), SECTION, "compare-hint", "&eUse &6/stats <player> &eto view another player's stats.");
            MessageUtils.sendMessage(event.getPlayer(), compareHint);
        }).build());
        String leaderboardTitle = GUIMessages.getText(this.targetPlayer, SECTION, "leaderboard-title", "&e&lLeaderboard");
        String leaderboardLine = GUIMessages.getText(this.targetPlayer, SECTION, "leaderboard-line", "&7View the top players.");
        String clickToView = GUIMessages.getText(this.targetPlayer, SECTION, "click-to-view", "&eClick to view");
        this.setItem(40, new GUIItem.Builder(Material.GOLD_INGOT).name(leaderboardTitle).lore("", leaderboardLine, "", clickToView).onClick(event -> {
            event.getPlayer().closeInventory();
            event.getPlayer().performCommand("leaderboard");
        }).build());
        this.setItem(42, GUIItem.closeButton());
    }

    private ItemStack createPlayerHead() {
        String rankLabel = GUIMessages.getText(this.targetPlayer, SECTION, "rank-label", "&7Rank: {rank}").replace("{rank}", this.getRankDisplay());
        String eloLabel = GUIMessages.getText(this.targetPlayer, SECTION, "elo-label", "&7Elo: &f{elo}").replace("{elo}", String.valueOf(this.stats.getGlobalElo()));
        String gamesPlayedLabel = GUIMessages.getText(this.targetPlayer, SECTION, "games-played-short", "&7Games Played:");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(rankLabel);
        lore.add(eloLabel);
        lore.add("");
        lore.add(gamesPlayedLabel);
        lore.add("&f" + this.stats.getTotalGames());
        lore.add("");
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(this.targetPlayer.getName()).name("&6&l" + this.targetPlayer.getName()).lore(lore).buildItem();
    }

    private ItemStack createCombatStats() {
        int kills = this.stats.getTotalKills();
        int deaths = this.stats.getTotalDeaths();
        double kd = this.stats.getKDR();
        String combatTitle = GUIMessages.getText(this.targetPlayer, SECTION, "combat-stats-title", "&c&lCombat Stats");
        String killsLabel = GUIMessages.getText(this.targetPlayer, SECTION, "kills-label", "&7Kills: &a{kills}").replace("{kills}", String.valueOf(kills));
        String deathsLabel = GUIMessages.getText(this.targetPlayer, SECTION, "deaths-label", "&7Deaths: &c{deaths}").replace("{deaths}", String.valueOf(deaths));
        String kdLabel = GUIMessages.getText(this.targetPlayer, SECTION, "kd-label", "&7K/D Ratio: {kd}").replace("{kd}", this.getKDColor(kd) + this.decimalFormat.format(kd));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(killsLabel);
        lore.add(deathsLabel);
        lore.add(kdLabel);
        lore.add("");
        lore.add(this.createProgressBar("K/D", kd, 3.0));
        lore.add("");
        return this.createItem(Material.DIAMOND_SWORD, combatTitle, lore);
    }

    private ItemStack createMatchStats() {
        int wins = this.stats.getTotalWins();
        int losses = this.stats.getTotalLosses();
        int total = this.stats.getTotalGames();
        double winRate = this.stats.getWinRate();
        String matchTitle = GUIMessages.getText(this.targetPlayer, SECTION, "match-stats-title", "&a&lMatch Stats");
        String winsLabel = GUIMessages.getText(this.targetPlayer, SECTION, "wins-label", "&7Wins: &a{wins}").replace("{wins}", String.valueOf(wins));
        String lossesLabel = GUIMessages.getText(this.targetPlayer, SECTION, "losses-label", "&7Losses: &c{losses}").replace("{losses}", String.valueOf(losses));
        String totalLabel = GUIMessages.getText(this.targetPlayer, SECTION, "total-matches-label", "&7Total Matches: &f{total}").replace("{total}", String.valueOf(total));
        String winRateLabel = GUIMessages.getText(this.targetPlayer, SECTION, "win-rate-label", "&7Win Rate: {rate}%").replace("{rate}", this.getWinRateColor(winRate) + this.decimalFormat.format(winRate));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(winsLabel);
        lore.add(lossesLabel);
        lore.add(totalLabel);
        lore.add("");
        lore.add(winRateLabel);
        lore.add(this.createProgressBar("Win Rate", winRate, 100.0));
        lore.add("");
        return this.createItem(Material.GOLDEN_SWORD, matchTitle, lore);
    }

    private ItemStack createStreakStats() {
        int currentStreak = this.stats.getCurrentWinStreak();
        int bestStreak = this.stats.getBestWinStreak();
        String streakTitle = GUIMessages.getText(this.targetPlayer, SECTION, "streak-stats-title", "&6&lStreak Stats");
        String currentStreakLabel = GUIMessages.getText(this.targetPlayer, SECTION, "current-streak-label", "&7Current Streak: {streak}").replace("{streak}", this.getStreakColor(currentStreak) + currentStreak);
        String bestStreakLabel = GUIMessages.getText(this.targetPlayer, SECTION, "best-streak-label", "&7Best Streak: &6{streak}").replace("{streak}", String.valueOf(bestStreak));
        String bestTierLabel = GUIMessages.getText(this.targetPlayer, SECTION, "best-tier-label", "&7Best Tier: {tier}").replace("{tier}", this.getStreakTier(bestStreak));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(currentStreakLabel);
        lore.add(bestStreakLabel);
        lore.add("");
        lore.add(bestTierLabel);
        lore.add("");
        int nextTierTarget = this.getNextStreakTarget(bestStreak);
        if (nextTierTarget > 0) {
            String nextTierLabel = GUIMessages.getText(this.targetPlayer, SECTION, "next-tier-at", "&7Next tier at: &f{target} streak").replace("{target}", String.valueOf(nextTierTarget));
            lore.add(nextTierLabel);
        }
        lore.add("");
        return this.createItem(Material.BLAZE_POWDER, streakTitle, lore);
    }

    private ItemStack createRankStats() {
        int elo = this.stats.getGlobalElo();
        String rank = this.getRankName(elo);
        String rankColor = this.getRankColor(elo);
        String rankingTitle = GUIMessages.getText(this.targetPlayer, SECTION, "ranking-title", "&d&lRanking");
        String currentEloLabel = GUIMessages.getText(this.targetPlayer, SECTION, "current-elo-label", "&7Current Elo: &f{elo}").replace("{elo}", String.valueOf(elo));
        String rankLabel = GUIMessages.getText(this.targetPlayer, SECTION, "rank-display", "&7Rank: {rank}").replace("{rank}", rankColor + rank);
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(currentEloLabel);
        lore.add(rankLabel);
        lore.add("");
        lore.add(this.createEloProgressBar(elo));
        lore.add("");
        int nextRankElo = this.getNextRankElo(elo);
        if (nextRankElo > 0) {
            String nextRank = this.getRankName(nextRankElo);
            String nextRankLabel = GUIMessages.getText(this.targetPlayer, SECTION, "next-rank-label", "&7Next rank: {rank}").replace("{rank}", this.getRankColor(nextRankElo) + nextRank);
            String eloNeededLabel = GUIMessages.getText(this.targetPlayer, SECTION, "elo-needed-label", "&7ELO needed: &f{needed}").replace("{needed}", String.valueOf(nextRankElo - elo));
            lore.add(nextRankLabel);
            lore.add(eloNeededLabel);
        } else {
            lore.add(GUIMessages.getText(this.targetPlayer, SECTION, "max-rank-achieved", "&6&lMax Rank Achieved!"));
        }
        lore.add("");
        return this.createItem(Material.NETHER_STAR, rankingTitle, lore);
    }

    private ItemStack createKitStats() {
        String kitStatsTitle = GUIMessages.getText(this.targetPlayer, SECTION, "kit-stats-title", "&e&lKit Stats");
        String noKitStats = GUIMessages.getText(this.targetPlayer, SECTION, "no-kit-stats-yet", "&7No kit stats recorded yet.");
        String mostPlayedLabel = GUIMessages.getText(this.targetPlayer, SECTION, "most-played-kit", "&7Most Played Kit: &f{kit}");
        String bestKitLabel = GUIMessages.getText(this.targetPlayer, SECTION, "best-kit", "&7Best Kit: &f{kit} &7({wr}% WR)");
        String clickToViewDetailed = GUIMessages.getText(this.targetPlayer, SECTION, "click-view-detailed", "&eClick to view detailed kit stats!");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        Map<String, KitStats> allKitStats = this.stats.getAllKitStats();
        if (allKitStats.isEmpty()) {
            lore.add(noKitStats);
        } else {
            String mostPlayed = null;
            int maxGames = 0;
            String bestKit = null;
            double bestWinRate = 0.0;
            for (Map.Entry<String, KitStats> entry : allKitStats.entrySet()) {
                KitStats kitStat = entry.getValue();
                if (kitStat.getTotalGames() > maxGames) {
                    maxGames = kitStat.getTotalGames();
                    mostPlayed = entry.getKey();
                }
                if (kitStat.getTotalGames() < 5 || !(kitStat.getWinRate() > bestWinRate)) continue;
                bestWinRate = kitStat.getWinRate();
                bestKit = entry.getKey();
            }
            if (mostPlayed != null) {
                lore.add(mostPlayedLabel.replace("{kit}", this.capitalize(mostPlayed)));
            }
            if (bestKit != null) {
                lore.add(bestKitLabel.replace("{kit}", this.capitalize(bestKit)).replace("{wr}", this.decimalFormat.format(bestWinRate)));
            }
        }
        lore.add("");
        lore.add(clickToViewDetailed);
        lore.add("");
        return this.createItem(Material.CHEST, kitStatsTitle, lore);
    }

    private String createProgressBar(String label, double value, double max) {
        int bars = 10;
        int filled = (int)Math.min((double)bars, value / max * (double)bars);
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

    private String createEloProgressBar(int elo) {
        int tier = elo / 200 * 200;
        int nextTier = tier + 200;
        int progress = elo - tier;
        int bars = 10;
        int filled = (int)((double)progress / 200.0 * (double)bars);
        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < bars; ++i) {
            if (i < filled) {
                bar.append(this.getRankColor(elo)).append("\u2588");
                continue;
            }
            bar.append("&7\u2591");
        }
        bar.append("&8] &7(").append(elo).append("/").append(nextTier).append(")");
        return bar.toString();
    }

    private String getRankDisplay() {
        int elo = this.stats.getGlobalElo();
        return this.getRankColor(elo) + this.getRankName(elo);
    }

    private String getRankName(int elo) {
        if (elo >= 2000) {
            return "Champion";
        }
        if (elo >= 1800) {
            return "Master";
        }
        if (elo >= 1600) {
            return "Diamond";
        }
        if (elo >= 1400) {
            return "Platinum";
        }
        if (elo >= 1200) {
            return "Gold";
        }
        if (elo >= 1000) {
            return "Silver";
        }
        if (elo >= 800) {
            return "Bronze";
        }
        return "Unranked";
    }

    private String getRankColor(int elo) {
        if (elo >= 2000) {
            return "&4&l";
        }
        if (elo >= 1800) {
            return "&c";
        }
        if (elo >= 1600) {
            return "&b";
        }
        if (elo >= 1400) {
            return "&d";
        }
        if (elo >= 1200) {
            return "&6";
        }
        if (elo >= 1000) {
            return "&7";
        }
        if (elo >= 800) {
            return "&8";
        }
        return "&f";
    }

    private int getNextRankElo(int currentElo) {
        if (currentElo >= 2000) {
            return -1;
        }
        if (currentElo >= 1800) {
            return 2000;
        }
        if (currentElo >= 1600) {
            return 1800;
        }
        if (currentElo >= 1400) {
            return 1600;
        }
        if (currentElo >= 1200) {
            return 1400;
        }
        if (currentElo >= 1000) {
            return 1200;
        }
        if (currentElo >= 800) {
            return 1000;
        }
        return 800;
    }

    private String getKDColor(double kd) {
        if (kd >= 3.0) {
            return "&a";
        }
        if (kd >= 2.0) {
            return "&e";
        }
        if (kd >= 1.0) {
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
            return "&c";
        }
        return "&4";
    }

    private String getStreakColor(int streak) {
        if (streak >= 15) {
            return "&4&l";
        }
        if (streak >= 10) {
            return "&c&l";
        }
        if (streak >= 5) {
            return "&6";
        }
        if (streak >= 3) {
            return "&e";
        }
        return "&f";
    }

    private String getStreakTier(int streak) {
        if (streak >= 20) {
            return "&4&lLegendary";
        }
        if (streak >= 15) {
            return "&c&lGodlike";
        }
        if (streak >= 10) {
            return "&6&lUnstoppable";
        }
        if (streak >= 5) {
            return "&e&lOn Fire";
        }
        if (streak >= 3) {
            return "&a&lWarming Up";
        }
        return "&7None";
    }

    private int getNextStreakTarget(int current) {
        if (current >= 20) {
            return -1;
        }
        if (current >= 15) {
            return 20;
        }
        if (current >= 10) {
            return 15;
        }
        if (current >= 5) {
            return 10;
        }
        if (current >= 3) {
            return 5;
        }
        return 3;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
        if (slot == 31) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> new KitStatsDetailGUI(this.plugin, this.targetPlayer).open(player), 1L);
        }
    }
}

