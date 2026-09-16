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

public class ToggleQueueCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public ToggleQueueCommand(UltimateDuels plugin) {
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
        if (this.plugin.getQueueManager() == null) {
            player.sendMessage("\u00a7cQueue system is not available!");
            return true;
        }
        if (this.plugin.getQueueManager().isInQueue(player)) {
            this.plugin.getQueueManager().leaveQueue(player);
        } else {
            if (args.length == 0) {
                player.sendMessage("\u00a7cUsage: /togglequeue <kit>");
                return true;
            }
            this.plugin.getQueueManager().joinQueue(player, args[0]);
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && this.plugin.getKitManager() != null) {
            return this.plugin.getKitManager().getAdminKitNames().stream().filter(kit -> kit.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

