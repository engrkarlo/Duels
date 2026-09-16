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
package com.ultimateduels.commands.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.ffa.model.FFAResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFACommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public FFACommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.ffa")) {
            player.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (ffaManager == null) {
            player.sendMessage("\u00a7cFFA system is not available!");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
            this.plugin.getGUIManager().openFFAGUI(player);
            return true;
        }
        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("leave")) {
            if (!ffaManager.isInFFA(player.getUniqueId())) {
                player.sendMessage("\u00a7cYou are not in an FFA arena!");
                return true;
            }
            FFAResult result = ffaManager.leave(player);
            player.sendMessage(result.message());
            return true;
        }
        if (subCommand.equals("list")) {
            this.showFFAList(player, ffaManager);
            return true;
        }
        String kitName = args[0];
        if (ffaManager.isInFFA(player.getUniqueId())) {
            player.sendMessage("\u00a7cYou are already in an FFA arena! Use \u00a7e/ffa leave \u00a7cor \u00a7e/leaveffa \u00a7cto leave.");
            return true;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            player.sendMessage("\u00a7cYou cannot join FFA while in queue!");
            return true;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
            player.sendMessage("\u00a7cYou cannot join FFA while in a match!");
            return true;
        }
        FFAResult result = ffaManager.joinByKit(player, kitName);
        if (!result.success()) {
            player.sendMessage(result.message());
        }
        return true;
    }

    private void showFFAList(Player player, FFAManager ffaManager) {
        player.sendMessage("");
        player.sendMessage("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac FFA ARENAS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
        boolean hasArenas = false;
        if (this.plugin.getKitManager() != null) {
            for (String kitName : this.plugin.getKitManager().getAdminKitNames()) {
                FFAArenaInstance arena = ffaManager.findArenaForKit(kitName);
                if (arena == null || !arena.isEnabled()) continue;
                hasArenas = true;
                int playerCount = arena.getPlayerCount();
                String status = "\u00a7a\u25cf";
                String clickHint = " \u00a78[\u00a7eClick to join\u00a78]";
                player.sendMessage("\u00a77  " + status + " \u00a7f" + kitName + " \u00a77(\u00a7e" + playerCount + " \u00a77players)" + clickHint);
            }
        }
        if (!hasArenas) {
            player.sendMessage("\u00a77  \u00a7cNo FFA arenas available");
        }
        player.sendMessage("");
        player.sendMessage("\u00a77Commands:");
        player.sendMessage("\u00a7e  /ffa \u00a77or \u00a7e/ffa gui \u00a77- Open FFA menu");
        player.sendMessage("\u00a7e  /ffa <kit> \u00a77- Join specific arena");
        player.sendMessage("\u00a7e  /ffa leave \u00a77- Leave arena");
        player.sendMessage("");
        player.sendMessage("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            List<String> subCommands = Arrays.asList("gui", "menu", "leave", "list");
            completions.addAll(subCommands.stream().filter(sub -> sub.startsWith(args[0].toLowerCase())).collect(Collectors.toList()));
            if (this.plugin.getKitManager() != null) {
                completions.addAll(this.plugin.getKitManager().getAdminKitNames().stream().filter(kit -> kit.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList()));
            }
            return completions;
        }
        return new ArrayList<String>();
    }
}

