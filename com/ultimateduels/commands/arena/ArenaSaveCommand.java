/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.commands.arena;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.commands.BaseCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ArenaSaveCommand
extends BaseCommand {
    public ArenaSaveCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.arena");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (this.plugin.getArenaManager() == null) {
            this.sendMessage(player, "&cArena system is not available!");
            return;
        }
        if (args.length < 1) {
            this.sendUsage((CommandSender)player, "/arenasave <arena>");
            this.sendMessage(player, "&7This will save the arena schematic for regeneration.");
            return;
        }
        String arenaName = args[0].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        if (!arena.hasBounds()) {
            this.sendMessage(player, "&cArena bounds must be set before saving!");
            this.sendMessage(player, "&7Use &e/arenasetup " + arenaName + " &7to set corners.");
            return;
        }
        if ((arena.getSpawnPoint1() == null || arena.getSpawnPoint2() == null) && arena.getSpawnPoints().isEmpty()) {
            this.sendMessage(player, "&cSpawn points must be set before saving!");
            this.sendMessage(player, "&7Use &e/setspawn " + arenaName + " <1|2>");
            return;
        }
        this.sendMessage(player, "&eSaving arena schematic for &f" + arenaName + "&e...");
        this.plugin.getArenaManager().saveSchematic(arenaName).thenAccept(success -> this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            if (success.booleanValue()) {
                this.sendMessage(player, "&aArena schematic saved successfully!");
                this.sendMessage(player, "&7The arena will now regenerate after each match.");
            } else {
                this.sendMessage(player, "&cFailed to save arena schematic!");
                this.sendMessage(player, "&7Make sure WorldEdit/FAWE is installed and properly configured.");
            }
        }));
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.filterCompletions(this.getArenaNames(), args[0]);
        }
        return new ArrayList<String>();
    }
}

