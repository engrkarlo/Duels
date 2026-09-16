/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands.stats;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.stats.PlayerStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaderboardCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private static final List<String> LEADERBOARD_TYPES = Arrays.asList("elo", "wins", "kdr", "kills", "winstreak", "killstreak", "winrate");

    public LeaderboardCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String type = args.length > 0 ? args[0].toLowerCase() : "elo";
        int limit = 10;
        if (args.length > 1) {
            try {
                limit = Integer.parseInt(args[1]);
                limit = Math.max(1, Math.min(limit, 50));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        switch (type) {
            case "elo": {
                this.showEloLeaderboard(sender, limit);
                break;
            }
            case "wins": {
                this.showWinsLeaderboard(sender, limit);
                break;
            }
            case "kdr": 
            case "kd": {
                this.showKDRLeaderboard(sender, limit);
                break;
            }
            case "kills": {
                this.showKillsLeaderboard(sender, limit);
                break;
            }
            case "winstreak": 
            case "streak": {
                this.showWinStreakLeaderboard(sender, limit);
                break;
            }
            case "killstreak": {
                this.showKillStreakLeaderboard(sender, limit);
                break;
            }
            case "winrate": 
            case "wr": {
                this.showWinRateLeaderboard(sender, limit);
                break;
            }
            default: {
                sender.sendMessage("\u00a7cUnknown leaderboard type: " + type);
                sender.sendMessage("\u00a77Available: \u00a7eelo, wins, kdr, kills, winstreak, killstreak, winrate");
            }
        }
        return true;
    }

    private void showEloLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByElo(limit);
        this.sendHeader(sender, "ELO Leaderboard", limit);
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- \u00a76" + stats.getGlobalElo() + " ELO \u00a77(W:" + stats.getTotalWins() + " L:" + stats.getTotalLosses() + ")");
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void showWinsLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByWins(limit);
        this.sendHeader(sender, "Wins Leaderboard", limit);
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- \u00a7a" + stats.getTotalWins() + " Wins \u00a77(" + this.df.format(stats.getWinRate()) + "% WR)");
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void showKDRLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByKDR(limit);
        this.sendHeader(sender, "K/D Ratio Leaderboard", limit);
        sender.sendMessage("\u00a77\u00a7o(Minimum 5 games required)");
        sender.sendMessage("");
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            String kdrColor = this.getKDRColor(stats.getKDR());
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- " + kdrColor + "\u00a7l" + stats.getFormattedKDR() + " KDR \u00a77(" + stats.getTotalKills() + "K / " + stats.getTotalDeaths() + "D)");
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void showKillsLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByKills(limit);
        this.sendHeader(sender, "Kills Leaderboard", limit);
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- \u00a7a" + stats.getTotalKills() + " Kills \u00a77(KDR: " + stats.getFormattedKDR() + ")");
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void showWinStreakLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByWinStreak(limit);
        this.sendHeader(sender, "Win Streak Leaderboard", limit);
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            String currentIndicator = stats.getCurrentWinStreak() == stats.getBestWinStreak() ? " \u00a7a\u00a7l(ACTIVE!)" : "";
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- \u00a76" + stats.getBestWinStreak() + " Win Streak" + currentIndicator);
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void showKillStreakLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByKillStreak(limit);
        this.sendHeader(sender, "Kill Streak Leaderboard", limit);
        sender.sendMessage("\u00a77\u00a7o(Most kills in a single match)");
        sender.sendMessage("");
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- \u00a7c" + stats.getBestKillStreak() + " Kills in one match");
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void showWinRateLeaderboard(CommandSender sender, int limit) {
        List<PlayerStats> top = this.plugin.getStatsManager().getTopPlayersByWinRate(limit, 10);
        this.sendHeader(sender, "Win Rate Leaderboard", limit);
        sender.sendMessage("\u00a77\u00a7o(Minimum 10 games required)");
        sender.sendMessage("");
        int rank = 1;
        for (PlayerStats stats : top) {
            String rankColor = this.getRankColor(rank);
            String wrColor = this.getWinRateColor(stats.getWinRate());
            sender.sendMessage(rankColor + "#" + rank + " \u00a7f" + stats.getPlayerName() + " \u00a77- " + wrColor + this.df.format(stats.getWinRate()) + "% \u00a77(" + stats.getTotalWins() + "W / " + stats.getTotalLosses() + "L)");
            ++rank;
        }
        this.sendFooter(sender);
    }

    private void sendHeader(CommandSender sender, String title, int limit) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("\u00a7e\u00a7l  " + title + " \u00a77(Top " + limit + ")");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
    }

    private void sendFooter(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("\u00a77Use \u00a7e/stats <player>\u00a77 to view detailed stats");
        sender.sendMessage("\u00a77Use \u00a7e/leaderboard <type> [limit]\u00a77 to view other leaderboards");
    }

    private String getRankColor(int rank) {
        return switch (rank) {
            case 1 -> "\u00a76\u00a7l";
            case 2 -> "\u00a7f\u00a7l";
            case 3 -> "\u00a7c\u00a7l";
            default -> "\u00a77";
        };
    }

    private String getKDRColor(double kdr) {
        if (kdr >= 3.0) {
            return "\u00a76";
        }
        if (kdr >= 2.0) {
            return "\u00a7a";
        }
        if (kdr >= 1.5) {
            return "\u00a7e";
        }
        if (kdr >= 1.0) {
            return "\u00a7f";
        }
        if (kdr >= 0.5) {
            return "\u00a7c";
        }
        return "\u00a74";
    }

    private String getWinRateColor(double winRate) {
        if (winRate >= 75.0) {
            return "\u00a76";
        }
        if (winRate >= 60.0) {
            return "\u00a7a";
        }
        if (winRate >= 50.0) {
            return "\u00a7e";
        }
        if (winRate >= 40.0) {
            return "\u00a7f";
        }
        return "\u00a7c";
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return LEADERBOARD_TYPES.stream().filter(type -> type.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Arrays.asList("5", "10", "15", "20", "25", "50");
        }
        return new ArrayList<String>();
    }
}

