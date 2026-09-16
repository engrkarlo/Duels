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
package com.ultimateduels.commands.spectate;

import com.ultimateduels.UltimateDuels;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnspectateCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public UnspectateCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (this.plugin.getDuelManager() == null) {
            player.sendMessage("\u00a7cDuel system is not available!");
            return true;
        }
        if (!this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            player.sendMessage("\u00a7cYou are not spectating any match!");
            return true;
        }
        this.plugin.getDuelManager().removeSpectator(player);
        player.sendMessage("\u00a7aYou have stopped spectating.");
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}

