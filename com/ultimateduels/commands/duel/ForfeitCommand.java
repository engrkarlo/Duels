/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.ffa.FFAManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForfeitCommand
extends BaseCommand {
    public ForfeitCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.forfeit");
    }

    @Override
    protected void execute(Player player, String[] args) {
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager != null && duelManager.isInMatch(player.getUniqueId())) {
            duelManager.forfeitMatch(player);
            return;
        }
        if (ffaManager != null && ffaManager.isInFFA(player.getUniqueId())) {
            ffaManager.leave(player);
            return;
        }
        this.sendMessage(player, "&cYou are not in a duel or FFA match!");
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<String>();
    }
}

