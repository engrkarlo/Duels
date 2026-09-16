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
package com.ultimateduels.gui.stats;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.stats.StatsManager;
import com.ultimateduels.utils.MessageUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class LeaderboardGUI
extends AbstractGUI {
    private final StatsManager statsManager;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private LeaderboardType currentType;
    private int page = 1;
    private static final int ENTRIES_PER_PAGE = 10;

    public LeaderboardGUI(UltimateDuels plugin) {
        this(plugin, "elo");
    }

    public LeaderboardGUI(UltimateDuels plugin, String type) {
        super(plugin, "&6&lLeaderboard", 6);
        this.statsManager = plugin.getStatsManager();
        this.currentType = this.getTypeFromString(type);
    }

    private LeaderboardType getTypeFromString(String type) {
        for (LeaderboardType t : LeaderboardType.values()) {
            if (!t.getKey().equalsIgnoreCase(type)) continue;
            return t;
        }
        return LeaderboardType.ELO;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.GOLD_BLOCK);
        this.setupTypeTabs();
        for (int i = 9; i < 18; ++i) {
            this.inventory.setItem(i, this.createFiller(Material.YELLOW_STAINED_GLASS_PANE));
        }
        this.displayLeaderboard();
        int totalPages = this.getTotalPages();
        this.setItem(49, this.createPageInfo(totalPages));
        if (this.page > 1) {
            this.setItem(45, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        if (this.page < totalPages) {
            this.setItem(53, GUIItem.nextPage(this.page, totalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(47, this.createYourRankButton());
        this.setItem(51, new GUIItem.Builder(Material.SUNFLOWER).name("&e&lRefresh").lore("", "&7Refresh leaderboard", "", "&eClick to refresh").onClick(event -> {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        }).build());
    }

    private void setupTypeTabs() {
        int slot = 1;
        for (LeaderboardType type : LeaderboardType.values()) {
            if (slot > 7) break;
            boolean selected = type == this.currentType;
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("");
            lore.add("&7View the " + type.getDisplayName().toLowerCase());
            lore.add("");
            lore.add(selected ? "&a&lSELECTED" : "&eClick to view");
            ItemStack item = new GUIItem.Builder(type.getIcon()).name((selected ? "&a&l" : "&e") + type.getDisplayName()).lore(lore).glowing(selected).buildItem();
            LeaderboardType finalType = type;
            this.setItem(slot, item, event -> {
                this.currentType = finalType;
                this.page = 1;
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                this.refresh(event.getPlayer());
            });
            ++slot;
        }
    }

    private void displayLeaderboard() {
        List<PlayerStats> topPlayers = this.getTopPlayers();
        int[] slots = new int[]{19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        if (topPlayers.isEmpty()) {
            this.setItem(31, this.createItem(Material.BARRIER, "&c&lNo Data", "", "&7No players found for", "&7this leaderboard.", ""));
            return;
        }
        int startRank = (this.page - 1) * 10 + 1;
        for (int i = 0; i < topPlayers.size() && i < slots.length; ++i) {
            PlayerStats stats = topPlayers.get(i);
            int rank = startRank + i;
            int slot = slots[i];
            this.setItem(slot, this.createLeaderboardEntry(stats, rank), event -> {
                event.getPlayer().closeInventory();
                event.getPlayer().performCommand("stats " + stats.getPlayerName());
            });
        }
        if (this.page == 1 && topPlayers.size() >= 3) {
            this.setItem(13, this.createTopThreeDisplay(topPlayers));
        }
    }

    private List<PlayerStats> getTopPlayers() {
        int offset = (this.page - 1) * 10;
        return switch (this.currentType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.statsManager.getTopByElo(10, offset);
            case 1 -> this.statsManager.getTopByKills(10, offset);
            case 2 -> this.statsManager.getTopByWins(10, offset);
            case 3 -> this.statsManager.getTopByStreak(10, offset);
            case 4 -> this.statsManager.getTopByKD(10, offset);
            case 5 -> this.statsManager.getTopByWinRate(10, offset);
        };
    }

    private int getTotalPages() {
        int totalPlayers = this.statsManager.getTotalPlayersWithStats();
        return Math.max(1, (int)Math.ceil((double)totalPlayers / 10.0));
    }

    private ItemStack createLeaderboardEntry(PlayerStats stats, int rank) {
        String rankDisplay = this.getRankDisplay(rank);
        String value = this.getValueDisplay(stats);
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Rank: " + rankDisplay);
        lore.add("&7" + this.currentType.getDisplayName() + ": " + value);
        lore.add("");
        lore.add("&7Elo: &f" + stats.getGlobalElo());
        lore.add("&7W/L: &a" + stats.getTotalWins() + "&7/&c" + stats.getTotalLosses());
        lore.add("&7K/D: &f" + this.decimalFormat.format(stats.getKDR()));
        lore.add("");
        lore.add("&eClick to view full stats");
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(stats.getPlayerName()).name(rankDisplay + " &f" + stats.getPlayerName()).lore(lore).glowing(rank <= 3).buildItem();
    }

    private ItemStack createTopThreeDisplay(List<PlayerStats> topPlayers) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&6&l\u2726 TOP 3 \u2726");
        lore.add("");
        for (int i = 0; i < Math.min(3, topPlayers.size()); ++i) {
            PlayerStats stats = topPlayers.get(i);
            String rankIcon = switch (i) {
                case 0 -> "&6&l#1 \ud83e\udd47";
                case 1 -> "&7&l#2 \ud83e\udd48";
                case 2 -> "&c&l#3 \ud83e\udd49";
                default -> "#" + (i + 1);
            };
            lore.add(rankIcon + " &f" + stats.getPlayerName() + " &7- " + this.getValueDisplay(stats));
        }
        lore.add("");
        return this.createItem(Material.GOLD_INGOT, "&6&lTop Players", lore);
    }

    private String getValueDisplay(PlayerStats stats) {
        return switch (this.currentType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "&e" + stats.getGlobalElo() + " elo";
            case 1 -> "&a" + stats.getTotalKills() + " kills";
            case 2 -> "&a" + stats.getTotalWins() + " wins";
            case 3 -> "&6" + stats.getBestWinStreak() + " streak";
            case 4 -> "&f" + this.decimalFormat.format(stats.getKDR()) + " K/D";
            case 5 -> {
                double rate = stats.getWinRate();
                yield "&f" + this.decimalFormat.format(rate) + "%";
            }
        };
    }

    private String getRankDisplay(int rank) {
        return switch (rank) {
            case 1 -> "&6&l#1 \ud83e\udd47";
            case 2 -> "&7&l#2 \ud83e\udd48";
            case 3 -> "&c&l#3 \ud83e\udd49";
            default -> "&8#" + rank;
        };
    }

    private ItemStack createPageInfo(int totalPages) {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Viewing: &f" + this.currentType.getDisplayName());
        lore.add("&7Page: &f" + this.page + "/" + totalPages);
        lore.add("");
        return this.createItem(Material.PAPER, "&7Page " + this.page + "/" + totalPages, lore);
    }

    private GUIItem createYourRankButton() {
        return new GUIItem.Builder(Material.COMPASS).name("&b&lYour Rank").lore("", "&7See where you stand", "&7on this leaderboard.", "", "&eClick to find your rank").onClick(event -> {
            Player player = event.getPlayer();
            int rank = this.statsManager.getPlayerRank(player.getUniqueId(), this.currentType.getKey());
            if (rank > 0) {
                MessageUtils.sendMessage(player, "&aYour rank for &e" + this.currentType.getDisplayName() + "&a: &6#" + rank);
                int targetPage = (rank - 1) / 10 + 1;
                if (targetPage != this.page) {
                    this.page = targetPage;
                    this.refresh(player);
                }
            } else {
                MessageUtils.sendMessage(player, "&cYou are not ranked yet. Play some duels!");
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }).build();
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }

    public static enum LeaderboardType {
        ELO("Top Elo", Material.NETHER_STAR, "elo"),
        KILLS("Top Killers", Material.DIAMOND_SWORD, "kills"),
        WINS("Most Wins", Material.GOLDEN_SWORD, "wins"),
        STREAK("Best Streaks", Material.BLAZE_POWDER, "streak"),
        KD("Best K/D", Material.BOW, "kd"),
        WIN_RATE("Best Win Rate", Material.TARGET, "winrate");

        private final String displayName;
        private final Material icon;
        private final String key;

        private LeaderboardType(String displayName, Material icon, String key) {
            this.displayName = displayName;
            this.icon = icon;
            this.key = key;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Material getIcon() {
            return this.icon;
        }

        public String getKey() {
            return this.key;
        }
    }
}

