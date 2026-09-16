/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands;

import com.ultimateduels.UltimateDuels;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PingCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public PingCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player target;
        if (!sender.hasPermission("ultimateduels.ping")) {
            sender.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00a7cUsage: /ping <player>");
                return true;
            }
            target = (Player)sender;
        } else {
            target = Bukkit.getPlayer((String)args[0]);
            if (target == null) {
                sender.sendMessage("\u00a7cPlayer not found: " + args[0]);
                return true;
            }
        }
        int ping = target.getPing();
        String color = this.getPingColor(ping);
        if (target.equals((Object)sender)) {
            sender.sendMessage("\u00a77Your ping: " + color + ping + "ms");
        } else {
            sender.sendMessage("\u00a77" + target.getName() + "'s ping: " + color + ping + "ms");
        }
        return true;
    }

    private String getPingColor(int ping) {
        if (ping < 50) {
            return "\u00a7a";
        }
        if (ping < 100) {
            return "\u00a7e";
        }
        if (ping < 150) {
            return "\u00a76";
        }
        return "\u00a7c";
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

