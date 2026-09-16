/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.scoreboard;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.scoreboard.ScoreboardBuilder;
import com.ultimateduels.stats.PlayerStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyScoreboard {
    private final UltimateDuels plugin;
    private final DecimalFormat kdFormat = new DecimalFormat("#.##");

    public LobbyScoreboard(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void update(ScoreboardBuilder board, Player player) {
        String title = this.plugin.getConfigManager().getConfig().getString("scoreboard.lobby.title", "&6&lULTIMATE&e&lDUELS");
        board.updateTitle(title);
        List configLines = this.plugin.getConfigManager().getConfig().getStringList("scoreboard.lobby.lines");
        ArrayList<String> lines = new ArrayList<String>();
        if (configLines.isEmpty()) {
            lines.add("&7&m                    ");
            lines.add("");
            lines.add("&fPlayer: &a" + player.getName());
            lines.add("");
            lines.add("&e&lYour Stats:");
            lines.add("  &7Kills: &f" + this.getKills(player));
            lines.add("  &7Deaths: &f" + this.getDeaths(player));
            lines.add("  &7K/D: &f" + this.getKDRatio(player));
            lines.add("");
            lines.add("&e&lServer:");
            lines.add("  &7Online: &f" + Bukkit.getOnlinePlayers().size());
            lines.add("  &7In Queue: &f" + this.getPlayersInQueue());
            lines.add("  &7Fighting: &f" + this.getPlayersInDuels());
            lines.add("");
            lines.add("&7&m                    ");
            lines.add("&e" + this.plugin.getConfigManager().getWebsiteUrl());
        } else {
            for (String line : configLines) {
                lines.add(this.replacePlaceholders(line, player));
            }
        }
        board.updateLines(lines);
    }

    private String replacePlaceholders(String line, Player player) {
        PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
        return line.replace("{player}", player.getName()).replace("{kills}", String.valueOf(this.getKills(player))).replace("{deaths}", String.valueOf(this.getDeaths(player))).replace("{kd}", this.getKDRatio(player)).replace("{online_count}", String.valueOf(Bukkit.getOnlinePlayers().size())).replace("{queue_count}", String.valueOf(this.getPlayersInQueue())).replace("{in_queue}", String.valueOf(this.getPlayersInQueue())).replace("{fighting_count}", String.valueOf(this.getPlayersInDuels())).replace("{website}", this.plugin.getConfigManager().getWebsiteUrl()).replace("{wins}", String.valueOf(stats != null ? stats.getTotalWins() : 0)).replace("{losses}", String.valueOf(stats != null ? stats.getTotalLosses() : 0)).replace("{streak}", String.valueOf(stats != null ? stats.getCurrentWinStreak() : 0));
    }

    private int getKills(Player player) {
        PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
        return stats != null ? stats.getTotalKills() : 0;
    }

    private int getDeaths(Player player) {
        PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
        return stats != null ? stats.getTotalDeaths() : 0;
    }

    private String getKDRatio(Player player) {
        PlayerStats stats = this.plugin.getStatsManager().getStats(player.getUniqueId());
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

    private int getPlayersInQueue() {
        if (this.plugin.getQueueManager() == null) {
            return 0;
        }
        try {
            return this.plugin.getQueueManager().getTotalQueueSize();
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Error getting queue size for scoreboard: " + e.getMessage());
            return 0;
        }
    }
}

