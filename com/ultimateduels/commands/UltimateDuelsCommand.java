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
package com.ultimateduels.commands;

import com.ultimateduels.UltimateDuels;
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

public class UltimateDuelsCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("help", "reload", "version", "info", "stats", "debug", "save", "status");
    private static final List<String> ADMIN_SUBCOMMANDS = Arrays.asList("reload", "debug", "save", "status");

    public UltimateDuelsCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String subCommand;
        if (args.length == 0) {
            this.showInfo(sender);
            return true;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "help": {
                this.showHelp(sender);
                break;
            }
            case "version": 
            case "ver": 
            case "v": {
                this.showVersion(sender);
                break;
            }
            case "info": 
            case "about": {
                this.showInfo(sender);
                break;
            }
            case "reload": {
                this.handleReload(sender);
                break;
            }
            case "debug": {
                this.handleDebug(sender);
                break;
            }
            case "save": {
                this.handleSave(sender);
                break;
            }
            case "status": {
                this.handleStatus(sender);
                break;
            }
            case "stats": {
                this.handleStats(sender);
                break;
            }
            default: {
                sender.sendMessage("\u00a7cUnknown subcommand. Use \u00a7e/ultimateduels help \u00a7cfor help.");
            }
        }
        return true;
    }

    private void showInfo(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("\u00a7e\u00a7lULTIMATE DUELS");
        sender.sendMessage("");
        sender.sendMessage("\u00a77Version: \u00a7a" + this.plugin.getDescription().getVersion());
        sender.sendMessage("\u00a77Author: \u00a7ageturplugins");
        sender.sendMessage("\u00a77API Version: \u00a7aPaper 1.21.x");
        sender.sendMessage("");
        sender.sendMessage("\u00a77A professional competitive dueling plugin");
        sender.sendMessage("\u00a77featuring ranked matches, parties, and more!");
        sender.sendMessage("");
        sender.sendMessage("\u00a77Use \u00a7e/ultimateduels help \u00a77for commands.");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
    }

    private void showVersion(CommandSender sender) {
        sender.sendMessage("\u00a76UltimateDuels \u00a77v" + this.plugin.getDescription().getVersion());
        sender.sendMessage("\u00a77Running on: \u00a7a" + this.plugin.getServer().getName() + " " + this.plugin.getServer().getVersion());
        sender.sendMessage("\u00a77Java: \u00a7a" + System.getProperty("java.version"));
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac ULTIMATEDUELS HELP \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lPlayer Commands:");
        sender.sendMessage("\u00a76/duel <player> \u00a77- Challenge a player to a duel");
        sender.sendMessage("\u00a76/accept \u00a77- Accept a duel request");
        sender.sendMessage("\u00a76/deny \u00a77- Deny a duel request");
        sender.sendMessage("\u00a76/queue <kit> \u00a77- Join the ranked queue");
        sender.sendMessage("\u00a76/leavequeue \u00a77- Leave the queue");
        sender.sendMessage("\u00a76/party \u00a77- Party management commands");
        sender.sendMessage("\u00a76/stats [player] \u00a77- View player statistics");
        sender.sendMessage("\u00a76/leaderboard \u00a77- View top players");
        sender.sendMessage("\u00a76/kit \u00a77- Kit management");
        sender.sendMessage("\u00a76/ffa <arena> \u00a77- Join a free-for-all arena");
        sender.sendMessage("\u00a76/spectate <player> \u00a77- Spectate a match");
        sender.sendMessage("\u00a76/lobby \u00a77- Return to lobby");
        sender.sendMessage("\u00a76/ping [player] \u00a77- Check ping");
        sender.sendMessage("");
        if (sender.hasPermission("ultimateduels.admin")) {
            sender.sendMessage("\u00a7c\u00a7lAdmin Commands:");
            sender.sendMessage("\u00a76/ultimateduels reload \u00a77- Reload configuration");
            sender.sendMessage("\u00a76/ultimateduels save \u00a77- Force save all data");
            sender.sendMessage("\u00a76/ultimateduels status \u00a77- View plugin status");
            sender.sendMessage("\u00a76/ultimateduels debug \u00a77- Toggle debug mode");
            sender.sendMessage("\u00a76/arena \u00a77- Arena management");
            sender.sendMessage("\u00a76/dueladmin \u00a77- Admin duel controls");
            sender.sendMessage("\u00a76/forceduel \u00a77- Force players to duel");
            sender.sendMessage("\u00a76/forceend \u00a77- Force end duels");
            sender.sendMessage("\u00a76/setlobby \u00a77- Set lobby spawn");
            sender.sendMessage("");
        }
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin.reload")) {
            sender.sendMessage("\u00a7cYou don't have permission to do this!");
            return;
        }
        sender.sendMessage("\u00a7eReloading UltimateDuels...");
        try {
            boolean success = this.plugin.reload();
            if (success) {
                sender.sendMessage("\u00a7aUltimateDuels has been reloaded successfully!");
            } else {
                sender.sendMessage("\u00a7cReload completed with some errors. Check console for details.");
            }
        }
        catch (Exception e) {
            sender.sendMessage("\u00a7cError during reload: " + e.getMessage());
            this.plugin.getLogger().severe("Error during reload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin.debug")) {
            sender.sendMessage("\u00a7cYou don't have permission to do this!");
            return;
        }
        boolean currentDebug = this.plugin.getConfigManager().isDebugMode();
        boolean newDebug = !currentDebug;
        this.plugin.getConfig().set("general.debug", (Object)newDebug);
        this.plugin.saveConfig();
        this.plugin.getConfigManager().reloadConfig();
        if (newDebug) {
            sender.sendMessage("\u00a7aDebug mode has been \u00a7eENABLED\u00a7a.");
            sender.sendMessage("\u00a77Debug messages will now appear in the console.");
        } else {
            sender.sendMessage("\u00a7aDebug mode has been \u00a7cDISABLED\u00a7a.");
        }
    }

    private void handleSave(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin")) {
            sender.sendMessage("\u00a7cYou don't have permission to do this!");
            return;
        }
        sender.sendMessage("\u00a7eForcing data save...");
        try {
            this.plugin.forceSave();
            sender.sendMessage("\u00a7aAll data has been saved successfully!");
        }
        catch (Exception e) {
            sender.sendMessage("\u00a7cError during save: " + e.getMessage());
        }
    }

    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin")) {
            sender.sendMessage("\u00a7cYou don't have permission to do this!");
            return;
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac PLUGIN STATUS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lPlugin:");
        sender.sendMessage("\u00a77  Version: \u00a7a" + this.plugin.getDescription().getVersion());
        sender.sendMessage("\u00a77  Enabled: " + (this.plugin.isFullyEnabled() ? "\u00a7aYes" : "\u00a7cNo"));
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lManagers:");
        sender.sendMessage("\u00a77  KitManager: " + this.getStatusColor(this.plugin.getKitManager() != null));
        sender.sendMessage("\u00a77  ArenaManager: " + this.getStatusColor(this.plugin.getArenaManager() != null));
        sender.sendMessage("\u00a77  DuelManager: " + this.getStatusColor(this.plugin.getDuelManager() != null));
        sender.sendMessage("\u00a77  QueueManager: " + this.getStatusColor(this.plugin.getQueueManager() != null));
        sender.sendMessage("\u00a77  PartyManager: " + this.getStatusColor(this.plugin.getPartyManager() != null));
        sender.sendMessage("\u00a77  FFAManager: " + this.getStatusColor(this.plugin.getFFAManager() != null));
        sender.sendMessage("\u00a77  StatsManager: " + this.getStatusColor(this.plugin.getStatsManager() != null));
        sender.sendMessage("\u00a77  DatabaseManager: " + this.getStatusColor(this.plugin.getDatabaseManager() != null));
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lStatistics:");
        if (this.plugin.getQueueManager() != null) {
            sender.sendMessage("\u00a77  Players in Queue: \u00a7e" + this.plugin.getQueueManager().getTotalQueueSize());
        }
        if (this.plugin.getDuelManager() != null) {
            sender.sendMessage("\u00a77  Active Matches: \u00a7e" + this.plugin.getDuelManager().getActiveMatchCount());
        }
        if (this.plugin.getPartyManager() != null) {
            sender.sendMessage("\u00a77  Active Parties: \u00a7e" + this.plugin.getPartyManager().getPartyCount());
        }
        if (this.plugin.getArenaManager() != null) {
            sender.sendMessage("\u00a77  Available Arenas: \u00a7e" + this.plugin.getArenaManager().getAvailableArenaCount());
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lHooks:");
        sender.sendMessage("\u00a77  WorldEdit: " + this.getStatusColor(this.plugin.isWorldEditEnabled()));
        sender.sendMessage("\u00a77  FAWE: " + this.getStatusColor(this.plugin.isFAWEEnabled()));
        sender.sendMessage("\u00a77  PlaceholderAPI: " + this.getStatusColor(this.plugin.getPlaceholderAPIHook() != null));
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lDatabase:");
        if (this.plugin.getDatabaseManager() != null) {
            sender.sendMessage("\u00a77  Type: \u00a7a" + this.plugin.getDatabaseManager().getConnectionPool().getDatabaseType().getDisplayName());
            sender.sendMessage("\u00a77  Connected: " + this.getStatusColor(this.plugin.getDatabaseManager().isHealthy()));
        } else {
            sender.sendMessage("\u00a77  Status: \u00a7cNot initialized");
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
    }

    private void handleStats(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac SERVER STATS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        if (this.plugin.getQueueManager() != null) {
            sender.sendMessage("\u00a7e\u00a7lQueue Statistics:");
            sender.sendMessage("\u00a77  Total in Queue: \u00a7a" + this.plugin.getQueueManager().getTotalQueueSize());
            sender.sendMessage("");
        }
        if (this.plugin.getDuelManager() != null) {
            sender.sendMessage("\u00a7e\u00a7lMatch Statistics:");
            sender.sendMessage("\u00a77  Active Matches: \u00a7a" + this.plugin.getDuelManager().getActiveMatchCount());
            sender.sendMessage("");
        }
        if (this.plugin.getPartyManager() != null) {
            sender.sendMessage("\u00a7e\u00a7lParty Statistics:");
            sender.sendMessage("\u00a77  Active Parties: \u00a7a" + this.plugin.getPartyManager().getPartyCount());
            sender.sendMessage("\u00a77  Players in Parties: \u00a7a" + this.plugin.getPartyManager().getTotalPlayersInParties());
            sender.sendMessage("");
        }
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
    }

    private String getStatusColor(boolean enabled) {
        return enabled ? "\u00a7aEnabled" : "\u00a7cDisabled";
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>(Arrays.asList("help", "version", "info", "stats"));
            if (sender.hasPermission("ultimateduels.admin")) {
                completions.addAll(ADMIN_SUBCOMMANDS);
            }
            return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

