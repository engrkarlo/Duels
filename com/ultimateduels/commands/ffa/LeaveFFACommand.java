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
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaveFFACommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public LeaveFFACommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (ffaManager == null) {
            player.sendMessage("\u00a7cFFA system is not available!");
            return true;
        }
        if (!ffaManager.isInFFA(player.getUniqueId())) {
            player.sendMessage("\u00a7cYou are not in an FFA arena!");
            return true;
        }
        ffaManager.leave(player);
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}

