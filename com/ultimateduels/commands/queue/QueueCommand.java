/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands.queue;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.queue.model.QueueResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public QueueCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.queue")) {
            player.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            player.sendMessage("\u00a7cQueue system is not available!");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            if (this.plugin.getGUIManager() != null) {
                this.plugin.getGUIManager().openQueueGUI(player);
            } else {
                player.sendMessage("\u00a7cGUI system is not available!");
            }
            return true;
        }
        if (args.length == 0) {
            this.showQueueHelp(player);
            return true;
        }
        String kitName = args[0];
        QueueResult result = queueManager.joinQueue(player, kitName);
        if (!result.success()) {
            player.sendMessage(result.message());
        }
        return true;
    }

    private void showQueueHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac QUEUE COMMANDS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
        player.sendMessage("\u00a7e/queue <kit> \u00a77- Join queue for a kit");
        player.sendMessage("\u00a7e/leavequeue \u00a77- Leave the current queue");
        player.sendMessage("");
        player.sendMessage("\u00a7e\u00a7lAvailable Kits:");
        if (this.plugin.getKitManager() != null && this.plugin.getQueueManager() != null) {
            for (String kitName : this.plugin.getKitManager().getAdminKitNames()) {
                int inQueue = this.plugin.getQueueManager().getQueueSize(kitName);
                int fighting = this.plugin.getQueueManager().getFightingCount(kitName);
                player.sendMessage("\u00a77  \u2022 \u00a7f" + kitName + " \u00a77(\u00a7a" + inQueue + " \u00a77queued, \u00a7c" + fighting + " \u00a77fighting)");
            }
        }
        player.sendMessage("");
        player.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            ArrayList<String> suggestions = new ArrayList<String>();
            suggestions.add("gui");
            if (this.plugin.getKitManager() != null) {
                suggestions.addAll(this.plugin.getKitManager().getAdminKitNames().stream().filter(kit -> kit.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList()));
            }
            return suggestions;
        }
        return new ArrayList<String>();
    }
}

