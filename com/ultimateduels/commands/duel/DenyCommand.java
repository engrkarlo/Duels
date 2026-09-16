/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.duel.model.DuelRequestResult;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand
extends BaseCommand {
    public DenyCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.duel");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (this.plugin.getDuelManager() == null) {
            this.sendMessage(player, "&cDuel system is not available!");
            return;
        }
        if (!this.plugin.getDuelManager().hasPendingRequest(player.getUniqueId())) {
            this.sendMessage(player, "&cYou don't have any pending duel requests!");
            return;
        }
        DuelRequestResult result = this.plugin.getDuelManager().denyRequest(player);
        if (!result.success()) {
            this.sendMessage(player, result.message());
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<String>();
    }
}

