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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaveQueueCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public LeaveQueueCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            player.sendMessage("\u00a7cQueue system is not available!");
            return true;
        }
        if (!queueManager.isInQueue(player)) {
            player.sendMessage("\u00a7cYou are not in any queue!");
            return true;
        }
        QueueResult result = queueManager.leaveQueue(player);
        player.sendMessage(result.message());
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}

