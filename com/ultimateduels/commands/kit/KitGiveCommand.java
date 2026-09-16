/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.kit.model.DuelKit;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitGiveCommand
extends BaseCommand {
    public KitGiveCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.kit", false);
    }

    @Override
    protected void execute(Player player, String[] args) {
        this.handleCommand((CommandSender)player, args);
    }

    @Override
    protected void executeConsole(CommandSender sender, String[] args) {
        this.handleCommand(sender, args);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        if (this.plugin.getKitManager() == null) {
            this.sendMessage(sender, "&cKit system is not available!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage(sender, "/kitgive <player> <kit>");
            return;
        }
        String targetName = args[0];
        String kitName = args[1].toLowerCase();
        Player target = Bukkit.getPlayer((String)targetName);
        if (target == null) {
            this.sendMessage(sender, "&cPlayer not found: &e" + targetName);
            return;
        }
        DuelKit kit = this.plugin.getKitManager().getAdminKit(kitName);
        if (kit == null) {
            kit = this.plugin.getKitManager().getPlayerKit(target.getUniqueId(), kitName);
        }
        if (kit == null) {
            this.sendMessage(sender, "&cKit not found: &e" + kitName);
            return;
        }
        boolean clearInventory = true;
        if (args.length >= 3 && args[2].equalsIgnoreCase("-noclear")) {
            clearInventory = false;
        }
        this.plugin.getKitManager().applyKit(target, kit, clearInventory);
        this.sendMessage(sender, "&aGave kit &e" + kit.getDisplayName() + " &ato &e" + target.getName() + "&a!");
        if (!sender.equals((Object)target)) {
            this.sendMessage(target, "&aYou received the &e" + kit.getDisplayName() + " &akit!");
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.filterCompletions(this.getOnlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return this.filterCompletions(new ArrayList<String>(this.plugin.getKitManager().getAdminKitNames()), args[1]);
        }
        if (args.length == 3) {
            return this.filterCompletions(List.of("-noclear"), args[2]);
        }
        return new ArrayList<String>();
    }
}

