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
package com.ultimateduels.commands.admin;

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

public class DuelAdminCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "save", "status", "debug", "forceend", "forceduel", "setlobby", "arena", "kit", "stats");

    public DuelAdminCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String subCommand;
        if (!sender.hasPermission("ultimateduels.admin")) {
            sender.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            this.showHelp(sender);
            return true;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "reload": {
                if (this.plugin.reload()) {
                    sender.sendMessage("\u00a7aPlugin reloaded successfully!");
                    break;
                }
                sender.sendMessage("\u00a7cReload completed with errors. Check console.");
                break;
            }
            case "save": {
                this.plugin.forceSave();
                sender.sendMessage("\u00a7aAll data saved successfully!");
                break;
            }
            case "status": {
                sender.sendMessage("\u00a7aPlugin is running. Use \u00a7e/ultimateduels status \u00a7afor details.");
                break;
            }
            case "debug": {
                boolean debug = !this.plugin.getConfigManager().isDebugMode();
                this.plugin.getConfig().set("general.debug", (Object)debug);
                this.plugin.saveConfig();
                sender.sendMessage("\u00a7aDebug mode: " + (debug ? "\u00a7eENABLED" : "\u00a7cDISABLED"));
                break;
            }
            default: {
                sender.sendMessage("\u00a7cUnknown subcommand: " + subCommand);
                this.showHelp(sender);
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac DUEL ADMIN \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/dueladmin reload \u00a77- Reload plugin");
        sender.sendMessage("\u00a7e/dueladmin save \u00a77- Force save data");
        sender.sendMessage("\u00a7e/dueladmin status \u00a77- View status");
        sender.sendMessage("\u00a7e/dueladmin debug \u00a77- Toggle debug");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e/forceduel <p1> <p2> [kit] \u00a77- Force duel");
        sender.sendMessage("\u00a7e/forceend <player|all> \u00a77- End duels");
        sender.sendMessage("\u00a7e/setlobby \u00a77- Set lobby spawn");
        sender.sendMessage("\u00a7e/arena \u00a77- Arena management");
        sender.sendMessage("");
        sender.sendMessage("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

