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
package com.ultimateduels.commands.admin;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
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

public class ForceDuelCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public ForceDuelCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String kitName;
        if (!sender.hasPermission("ultimateduels.admin.forceduel")) {
            sender.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /forceduel <player1> <player2> [kit]");
            return true;
        }
        Player player1 = Bukkit.getPlayer((String)args[0]);
        Player player2 = Bukkit.getPlayer((String)args[1]);
        if (player1 == null) {
            sender.sendMessage("\u00a7cPlayer not found: " + args[0]);
            return true;
        }
        if (player2 == null) {
            sender.sendMessage("\u00a7cPlayer not found: " + args[1]);
            return true;
        }
        if (player1.equals((Object)player2)) {
            sender.sendMessage("\u00a7cYou cannot force a player to duel themselves!");
            return true;
        }
        String string = kitName = args.length > 2 ? args[2] : "nodebuff";
        if (this.plugin.getKitManager() != null && !this.plugin.getKitManager().adminKitExists(kitName)) {
            sender.sendMessage("\u00a7cKit not found: " + kitName);
            return true;
        }
        if (this.plugin.getQueueManager() != null) {
            this.plugin.getQueueManager().removeFromAllQueues(player1.getUniqueId());
            this.plugin.getQueueManager().removeFromAllQueues(player2.getUniqueId());
        }
        if (this.plugin.getDuelManager() != null) {
            DuelArena arena = this.plugin.getArenaManager().allocateArena(null, kitName);
            if (arena == null) {
                sender.sendMessage("\u00a7cNo arenas available!");
                return true;
            }
            this.plugin.getDuelManager().startMatch(player1, player2, kitName, arena, 1, false);
            sender.sendMessage("\u00a7aForced duel started between \u00a7e" + player1.getName() + " \u00a7aand \u00a7e" + player2.getName() + " \u00a7awith kit \u00a7e" + kitName + "\u00a7a.");
            player1.sendMessage("\u00a7eYou have been forced into a duel by an admin!");
            player2.sendMessage("\u00a7eYou have been forced into a duel by an admin!");
        } else {
            sender.sendMessage("\u00a7cDuel system is not available!");
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 || args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && this.plugin.getKitManager() != null) {
            return this.plugin.getKitManager().getAdminKitNames().stream().filter(kit -> kit.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

