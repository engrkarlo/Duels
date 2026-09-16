/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.party.model.Party;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyLeaveCommand
extends BaseCommand {
    public PartyLeaveCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.party");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (this.plugin.getPartyManager() == null) {
            this.sendMessage(player, "&cParty system is not available!");
            return;
        }
        UUID playerUUID = player.getUniqueId();
        if (!this.plugin.getPartyManager().isInParty(playerUUID)) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        Party party = this.plugin.getPartyManager().getParty(playerUUID);
        if (party != null && this.plugin.getDuelManager() != null) {
            boolean inMatch = false;
            for (UUID memberUUID : party.getAllMembers()) {
                Player memberPlayer = Bukkit.getPlayer((UUID)memberUUID);
                if (memberPlayer == null || !memberPlayer.isOnline() || !this.plugin.getDuelManager().isInDuel(memberPlayer)) continue;
                inMatch = true;
                break;
            }
            if (inMatch) {
                this.sendMessage(player, "&cYou cannot leave the party during a match!");
                this.sendMessage(player, "&7Wait for the match to end.");
                return;
            }
        }
        if (this.plugin.getPartyManager().isPartyLeader(playerUUID) && (args.length == 0 || !args[0].equalsIgnoreCase("confirm"))) {
            this.sendMessage(player, "&eYou are the party leader!");
            if (party != null && party.getSize() > 1) {
                this.sendMessage(player, "&7Leaving will transfer leadership to another member.");
            } else {
                this.sendMessage(player, "&7Leaving will disband the party.");
            }
            this.sendMessage(player, "&7Type &e/partyleave confirm &7to confirm.");
            return;
        }
        this.plugin.getPartyManager().leaveParty(player);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player)sender;
            UUID playerUUID = player.getUniqueId();
            if (this.plugin.getPartyManager() != null && this.plugin.getPartyManager().isPartyLeader(playerUUID)) {
                return this.filterCompletions(List.of("confirm"), args[0]);
            }
        }
        return new ArrayList<String>();
    }
}

