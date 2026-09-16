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
package com.ultimateduels.commands.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.party.PartyManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PChatCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public PChatCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.party.chat")) {
            player.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        PartyManager partyManager = this.plugin.getPartyManager();
        if (partyManager == null) {
            player.sendMessage("\u00a7cParty system is not available!");
            return true;
        }
        if (!partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("\u00a7cYou are not in a party!");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("\u00a7cUsage: /pchat <message>");
            return true;
        }
        String message = String.join((CharSequence)" ", args);
        partyManager.sendPartyChat(player, message);
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}

