/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyAcceptCommand
extends BaseCommand {
    public PartyAcceptCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.party");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (this.plugin.getPartyManager() == null) {
            this.sendMessage(player, "&cParty system is not available!");
            return;
        }
        if (!this.plugin.getPartyManager().hasPendingInvite(player.getUniqueId())) {
            this.sendMessage(player, "&cYou don't have any pending party invites!");
            return;
        }
        if (this.plugin.getPartyManager().isInParty(player.getUniqueId())) {
            this.sendMessage(player, "&cYou are already in a party!");
            this.sendMessage(player, "&7Use &e/party leave &7to leave first.");
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            this.sendMessage(player, "&cYou cannot join a party while in a duel!");
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            this.sendMessage(player, "&cYou cannot join a party while in an FFA arena!");
            return;
        }
        this.plugin.getPartyManager().acceptInvite(player);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<String>();
    }
}

