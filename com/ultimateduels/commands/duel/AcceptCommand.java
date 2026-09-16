/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.duel.model.DuelRequest;
import com.ultimateduels.duel.model.DuelRequestResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand
extends BaseCommand {
    public AcceptCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.duel");
    }

    @Override
    protected void execute(Player player, String[] args) {
        Player sender;
        if (!this.checkPlayerState(player)) {
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            this.sendMessage(player, "&cYou must leave the queue before accepting a duel!");
            return;
        }
        if (this.plugin.getDuelManager() == null) {
            this.sendMessage(player, "&cDuel system is not available!");
            return;
        }
        if (!this.plugin.getDuelManager().hasPendingRequest(player.getUniqueId())) {
            this.sendMessage(player, "&cYou don't have any pending duel requests!");
            return;
        }
        DuelRequest request = this.plugin.getDuelManager().getPendingRequest(player.getUniqueId());
        if (request == null) {
            this.sendMessage(player, "&cYou don't have any pending duel requests!");
            return;
        }
        if (args.length > 0) {
            String senderName = args[0];
            Player sender2 = Bukkit.getPlayer((String)senderName);
            if (sender2 == null) {
                this.sendMessage(player, "&cPlayer &e" + senderName + " &cis not online!");
                return;
            }
            if (!request.getSenderUUID().equals(sender2.getUniqueId())) {
                this.sendMessage(player, "&cYou don't have a pending request from &e" + sender2.getName() + "&c!");
                return;
            }
        }
        if ((sender = Bukkit.getPlayer((UUID)request.getSenderUUID())) == null || !sender.isOnline()) {
            this.sendMessage(player, "&cThe player who sent this request is no longer online!");
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(sender)) {
            this.sendMessage(player, "&c" + sender.getName() + " is now in a duel!");
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(sender)) {
            this.sendMessage(player, "&c" + sender.getName() + " is now in a queue!");
            return;
        }
        DuelRequestResult result = this.plugin.getDuelManager().acceptRequest(player, sender);
        if (!result.success()) {
            player.sendMessage(this.plugin.colorize(result.message()));
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        sender.playSound(sender.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        String senderName;
        Player requestSender;
        DuelRequest request;
        if (!(sender instanceof Player)) {
            return new ArrayList<String>();
        }
        Player player = (Player)sender;
        if (args.length == 1 && this.plugin.getDuelManager() != null && (request = this.plugin.getDuelManager().getPendingRequest(player.getUniqueId())) != null && (requestSender = Bukkit.getPlayer((UUID)request.getSenderUUID())) != null && requestSender.isOnline() && (senderName = requestSender.getName()).toLowerCase().startsWith(args[0].toLowerCase())) {
            return List.of(senderName);
        }
        return new ArrayList<String>();
    }
}

