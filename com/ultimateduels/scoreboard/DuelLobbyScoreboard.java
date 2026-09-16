/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.scoreboard;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.queue.model.QueueEntry;
import com.ultimateduels.scoreboard.ScoreboardBuilder;
import com.ultimateduels.stats.PlayerStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelLobbyScoreboard {
    private final UltimateDuels plugin;
    private final DecimalFormat kdFormat = new DecimalFormat("#.##");
    private List<LeaderboardCacheEntry> cachedLeaderboard = new ArrayList<LeaderboardCacheEntry>();
    private long lastLeaderboardUpdate = 0L;
    private static final long LEADERBOARD_CACHE_TIME = 5000L;

    public DuelLobbyScoreboard(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void update(ScoreboardBuilder board, Player player) {
        String title = this.plugin.getConfigManager().getConfig().getString("scoreboard.duel-lobby.title", "&6&lDUEL LOBBY");
        board.updateTitle(title);
        List configLines = this.plugin.getConfigManager().getConfig().getStringList("scoreboard.duel-lobby.lines");
        ArrayList<String> lines = new ArrayList<String>();
        if (configLines.isEmpty()) {
            this.buildDefaultLeaderboard(lines, player);
        } else {
            this.updateLeaderboardCache();
            for (String line : configLines) {
                String processed = this.replacePlaceholders(line, player);
                if (processed.contains("{top1_name}") || processed.contains("{top2_name}") || processed.contains("{top3_name}")) {
                    this.addLeaderboardLine(lines, processed);
                    continue;
                }
                lines.add(processed);
            }
        }
        board.updateLines(lines);
    }

    private void buildDefaultLeaderboard(List<String> lines, Player player) {
        PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
        lines.add("&7&m                    ");
        lines.add("");
        String queueStatus = this.getQueueStatus(player);
        lines.add("&e&lQueue Status:");
        lines.add("  " + queueStatus);
        lines.add("");
        lines.add("&a&lYour Stats:");
        lines.add("  &7Wins: &f" + this.getWins(stats));
        lines.add("  &7K/D: &f" + this.getKDRatio(stats));
        lines.add("  &7Streak: &f" + this.getStreak(stats));
        lines.add("");
        lines.add("&e&lTop Players:");
        this.addLeaderboardLines(lines, player);
        lines.add("");
        lines.add("&7Online: &f" + Bukkit.getOnlinePlayers().size() + " &8| &7Fighting: &f" + this.getPlayersInDuels());
        lines.add("");
        lines.add("&7&m                    ");
        lines.add("&e" + this.plugin.getConfigManager().getWebsiteUrl());
    }

    private String replacePlaceholders(String line, Player player) {
        PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
        String processed = line.replace("{player}", player.getName()).replace("{wins}", String.valueOf(this.getWins(stats))).replace("{losses}", String.valueOf(stats != null ? stats.getTotalLosses() : 0)).replace("{kd}", this.getKDRatio(stats)).replace("{streak}", String.valueOf(this.getStreak(stats))).replace("{elo}", String.valueOf(stats != null ? stats.getGlobalElo() : 1000)).replace("{online_count}", String.valueOf(Bukkit.getOnlinePlayers().size())).replace("{queue_count}", String.valueOf(this.getTotalInQueues())).replace("{fighting_count}", String.valueOf(this.getPlayersInDuels())).replace("{website}", this.plugin.getConfigManager().getWebsiteUrl()).replace("{queue_status}", this.getQueueStatus(player));
        return processed;
    }

    private void addLeaderboardLine(List<String> lines, String template) {
        this.updateLeaderboardCache();
        for (int i = 0; i < Math.min(3, this.cachedLeaderboard.size()); ++i) {
            LeaderboardCacheEntry entry = this.cachedLeaderboard.get(i);
            String line = template.replace("{top" + (i + 1) + "_name}", entry.playerName).replace("{top" + (i + 1) + "_wins}", String.valueOf(entry.wins));
            if (!template.contains("{top" + (i + 1) + "_name}")) continue;
            lines.add(line);
            break;
        }
    }

    private void addLeaderboardLines(List<String> lines, Player viewer) {
        int viewerPosition;
        this.updateLeaderboardCache();
        int position = 1;
        UUID viewerUUID = viewer.getUniqueId();
        boolean viewerInTop = false;
        for (LeaderboardCacheEntry entry : this.cachedLeaderboard) {
            if (position > 5) break;
            String prefix = position <= 3 ? this.getPositionColor(position) : "&7";
            Object name = entry.playerName;
            if (entry.uuid.equals(viewerUUID)) {
                name = "&a&l" + (String)name;
                viewerInTop = true;
            }
            lines.add("  " + prefix + "#" + position + " &f" + (String)name + " &7- &e" + entry.wins);
            ++position;
        }
        if (!viewerInTop && (viewerPosition = this.getPlayerPosition(viewerUUID)) > 0) {
            PlayerStats viewerStats = this.plugin.getStatsManager().getStats(viewerUUID);
            lines.add("  &8...");
            lines.add("  &7#" + viewerPosition + " &a&l" + viewer.getName() + " &7- &e" + this.getWins(viewerStats));
        }
    }

    private void updateLeaderboardCache() {
        long now = System.currentTimeMillis();
        if (now - this.lastLeaderboardUpdate < 5000L && !this.cachedLeaderboard.isEmpty()) {
            return;
        }
        this.cachedLeaderboard.clear();
        if (this.plugin.getStatsManager() != null) {
            List<PlayerStats> topPlayers = this.plugin.getStatsManager().getTopPlayersByWins(10);
            for (PlayerStats stats : topPlayers) {
                String name = this.getPlayerName(stats.getPlayerUUID());
                this.cachedLeaderboard.add(new LeaderboardCacheEntry(stats.getPlayerUUID(), name, stats.getTotalWins()));
            }
        }
        this.lastLeaderboardUpdate = now;
    }

    private int getPlayerPosition(UUID uuid) {
        this.updateLeaderboardCache();
        for (int i = 0; i < this.cachedLeaderboard.size(); ++i) {
            if (!this.cachedLeaderboard.get((int)i).uuid.equals(uuid)) continue;
            return i + 1;
        }
        if (this.plugin.getStatsManager() != null) {
            int rank = this.plugin.getStatsManager().getPlayerRank(uuid, "wins");
            return rank > 0 ? rank : 0;
        }
        return 0;
    }

    private String getPositionColor(int position) {
        return switch (position) {
            case 1 -> "&6&l";
            case 2 -> "&7&l";
            case 3 -> "&c&l";
            default -> "&7";
        };
    }

    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer((UUID)uuid);
        if (online != null) {
            return online.getName();
        }
        String name = Bukkit.getOfflinePlayer((UUID)uuid).getName();
        return name != null ? name : "Unknown";
    }

    private String getQueueStatus(Player player) {
        if (this.plugin.getQueueManager() == null) {
            return "&7Not in queue";
        }
        if (this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            String kit = this.plugin.getQueueManager().getQueuedKit(player.getUniqueId());
            if (kit == null) {
                kit = "Unknown";
            }
            int position = this.getQueuePositionForPlayer(player.getUniqueId(), kit);
            int queueSize = this.plugin.getQueueManager().getQueueSize(kit);
            return "&aQueued for &f" + kit + " &7(" + position + "/" + queueSize + ")";
        }
        return "&7Not in queue - &e/duel queue";
    }

    private int getQueuePositionForPlayer(UUID uuid, String kitName) {
        if (this.plugin.getQueueManager() == null) {
            return 0;
        }
        Queue<QueueEntry> queue = this.plugin.getQueueManager().getKitQueue(kitName);
        if (queue == null || queue.isEmpty()) {
            return 0;
        }
        int position = 1;
        for (QueueEntry entry : queue) {
            if (entry.getPlayerUUID().equals(uuid)) {
                return position;
            }
            ++position;
        }
        return 0;
    }

    private int getWins(PlayerStats stats) {
        return stats != null ? stats.getTotalWins() : 0;
    }

    private int getStreak(PlayerStats stats) {
        return stats != null ? stats.getCurrentWinStreak() : 0;
    }

    private String getKDRatio(PlayerStats stats) {
        if (stats == null) {
            return "0.00";
        }
        int kills = stats.getTotalKills();
        int deaths = stats.getTotalDeaths();
        if (deaths == 0) {
            return kills > 0 ? kills + ".00" : "0.00";
        }
        return this.kdFormat.format((double)kills / (double)deaths);
    }

    private int getPlayersInDuels() {
        return this.plugin.getDuelManager() != null ? this.plugin.getDuelManager().getTotalPlayersInMatches() : 0;
    }

    private int getTotalInQueues() {
        if (this.plugin.getQueueManager() == null) {
            return 0;
        }
        return this.plugin.getQueueManager().getTotalQueueSize();
    }

    private static class LeaderboardCacheEntry {
        final UUID uuid;
        final String playerName;
        final int wins;

        LeaderboardCacheEntry(UUID uuid, String playerName, int wins) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.wins = wins;
        }
    }
}

