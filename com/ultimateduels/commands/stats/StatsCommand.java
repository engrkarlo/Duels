/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands.stats;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.stats.KitStats;
import com.ultimateduels.stats.PlayerStats;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public StatsCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String targetName;
        UUID targetUUID;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00a7cConsole must specify a player!");
                return true;
            }
            Player player = (Player)sender;
            targetUUID = player.getUniqueId();
            targetName = player.getName();
        } else {
            String playerName = args[0];
            Player onlinePlayer = Bukkit.getPlayer((String)playerName);
            if (onlinePlayer != null) {
                targetUUID = onlinePlayer.getUniqueId();
                targetName = onlinePlayer.getName();
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((String)playerName);
                if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                    targetUUID = offlinePlayer.getUniqueId();
                    targetName = offlinePlayer.getName() != null ? offlinePlayer.getName() : playerName;
                } else {
                    sender.sendMessage("\u00a7cPlayer not found: " + playerName);
                    return true;
                }
            }
        }
        PlayerStats stats = this.plugin.getStatsManager().getStats(targetUUID);
        this.sendStatsMessage(sender, targetName, stats, args.length > 1 ? args[1] : null);
        return true;
    }

    private void sendStatsMessage(CommandSender sender, String playerName, PlayerStats stats, @Nullable String kitName) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("\u00a7e\u00a7l  " + playerName + "'s Statistics");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7lGLOBAL STATS:");
        sender.sendMessage("\u00a77\u251c \u00a7fELO: \u00a76" + stats.getGlobalElo());
        sender.sendMessage("\u00a77\u251c \u00a7fGames Played: \u00a7e" + stats.getTotalGames());
        sender.sendMessage("\u00a77\u251c \u00a7fWins: \u00a7a" + stats.getTotalWins());
        sender.sendMessage("\u00a77\u251c \u00a7fLosses: \u00a7c" + stats.getTotalLosses());
        sender.sendMessage("\u00a77\u251c \u00a7fWin Rate: \u00a7e" + this.df.format(stats.getWinRate()) + "%");
        sender.sendMessage("\u00a77\u2502");
        sender.sendMessage("\u00a77\u251c \u00a7fKills: \u00a7a" + stats.getTotalKills());
        sender.sendMessage("\u00a77\u251c \u00a7fDeaths: \u00a7c" + stats.getTotalDeaths());
        sender.sendMessage("\u00a77\u251c \u00a7f\u00a7lK/D Ratio: \u00a7b\u00a7l" + stats.getFormattedKDR());
        sender.sendMessage("\u00a77\u251c \u00a7fKills/Game: \u00a7e" + this.df.format(stats.getKillsPerGame()));
        sender.sendMessage("\u00a77\u2502");
        sender.sendMessage("\u00a77\u251c \u00a7fCurrent Win Streak: \u00a76" + stats.getCurrentWinStreak());
        sender.sendMessage("\u00a77\u251c \u00a7fBest Win Streak: \u00a76" + stats.getBestWinStreak());
        sender.sendMessage("\u00a77\u2514 \u00a7fBest Kill Streak: \u00a76" + stats.getBestKillStreak());
        Map<String, KitStats> kitStatsMap = stats.getAllKitStats();
        if (!kitStatsMap.isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage("\u00a76\u00a7lKIT STATS:");
            if (kitName != null) {
                KitStats kitStats = stats.getKitStats(kitName);
                if (kitStats != null) {
                    this.sendDetailedKitStats(sender, kitStats);
                } else {
                    sender.sendMessage("\u00a77No stats found for kit: \u00a7e" + kitName);
                }
            } else {
                for (KitStats kitStats : kitStatsMap.values()) {
                    this.sendKitStatsSummary(sender, kitStats);
                }
                sender.sendMessage("");
                sender.sendMessage("\u00a77Tip: Use \u00a7e/stats " + stats.getPlayerName() + " <kit>\u00a77 for detailed kit stats");
            }
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
    }

    private void sendKitStatsSummary(CommandSender sender, KitStats kitStats) {
        String kdrColor = this.getKDRColor(kitStats.getKDR());
        sender.sendMessage("\u00a7e" + this.capitalize(kitStats.getKitName()) + "\u00a77: \u00a7a" + kitStats.getWins() + "W \u00a77/ \u00a7c" + kitStats.getLosses() + "L \u00a77| \u00a7a" + kitStats.getKills() + "K \u00a77/ \u00a7c" + kitStats.getDeaths() + "D \u00a77| \u00a7fKDR: " + kdrColor + kitStats.getFormattedKDR() + " \u00a77| \u00a76" + kitStats.getElo() + " ELO");
    }

    private void sendDetailedKitStats(CommandSender sender, KitStats kitStats) {
        String kdrColor = this.getKDRColor(kitStats.getKDR());
        sender.sendMessage("\u00a7e\u00a7l" + this.capitalize(kitStats.getKitName()) + " Kit Stats:");
        sender.sendMessage("\u00a77\u251c \u00a7fELO: \u00a76" + kitStats.getElo());
        sender.sendMessage("\u00a77\u251c \u00a7fGames: \u00a7e" + kitStats.getTotalGames());
        sender.sendMessage("\u00a77\u251c \u00a7fWins: \u00a7a" + kitStats.getWins() + " \u00a77(" + this.df.format(kitStats.getWinRate()) + "%)");
        sender.sendMessage("\u00a77\u251c \u00a7fLosses: \u00a7c" + kitStats.getLosses());
        sender.sendMessage("\u00a77\u251c \u00a7fKills: \u00a7a" + kitStats.getKills());
        sender.sendMessage("\u00a77\u251c \u00a7fDeaths: \u00a7c" + kitStats.getDeaths());
        sender.sendMessage("\u00a77\u251c \u00a7f\u00a7lK/D Ratio: " + kdrColor + "\u00a7l" + kitStats.getFormattedKDR());
        sender.sendMessage("\u00a77\u251c \u00a7fKills/Game: \u00a7e" + this.df.format(kitStats.getKillsPerGame()));
        sender.sendMessage("\u00a77\u251c \u00a7fBest Win Streak: \u00a76" + kitStats.getBestWinStreak());
        sender.sendMessage("\u00a77\u2514 \u00a7fBest Kill Streak: \u00a76" + kitStats.getBestKillStreak());
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return this.plugin.getKitManager().getAdminKitNames().stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

