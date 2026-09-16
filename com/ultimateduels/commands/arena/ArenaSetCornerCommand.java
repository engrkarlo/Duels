/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.arena;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.commands.BaseCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaSetCornerCommand
extends BaseCommand {
    public ArenaSetCornerCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.arena");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (this.plugin.getArenaManager() == null) {
            this.sendMessage(player, "&cArena system is not available!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/setcorner <arena> <1|2>");
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Sets corners for arena schematic region.");
            this.sendMessage(player, "&7Both corners must be set before saving schematic.");
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Example:");
            this.sendMessage(player, "&e/setcorner myarena 1 &7- Set corner 1");
            this.sendMessage(player, "&e/setcorner myarena 2 &7- Set corner 2");
            return;
        }
        String arenaName = args[0].toLowerCase();
        String cornerType = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: &e" + arenaName);
            this.sendMessage(player, "&7Use &e/arena list &7to see all arenas.");
            return;
        }
        Location location = player.getLocation().getBlock().getLocation();
        switch (cornerType) {
            case "1": 
            case "one": 
            case "first": 
            case "pos1": 
            case "corner1": {
                this.plugin.getArenaManager().setCorner1(arenaName, location);
                this.sendMessage(player, "&aCorner &e1 &aset for arena &e" + arena.getDisplayName() + "&a!");
                this.sendLocationInfo(player, location);
                break;
            }
            case "2": 
            case "two": 
            case "second": 
            case "pos2": 
            case "corner2": {
                this.plugin.getArenaManager().setCorner2(arenaName, location);
                this.sendMessage(player, "&aCorner &e2 &aset for arena &e" + arena.getDisplayName() + "&a!");
                this.sendLocationInfo(player, location);
                break;
            }
            default: {
                this.sendMessage(player, "&cInvalid corner number: &e" + cornerType);
                this.sendMessage(player, "&7Valid options: &f1, 2");
                return;
            }
        }
        arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena != null) {
            this.showSetupProgress(player, arena);
        }
    }

    private void sendLocationInfo(Player player, Location loc) {
        this.sendMessage(player, "&7Position: &f" + String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    private void showSetupProgress(Player player, DuelArena arena) {
        this.sendMessage(player, "");
        this.sendMessage(player, "&7\u2501\u2501\u2501 Schematic Region Setup \u2501\u2501\u2501");
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        boolean hasCorner1 = corner1 != null;
        boolean hasCorner2 = corner2 != null;
        this.sendMessage(player, (hasCorner1 ? "&a\u2713" : "&c\u2717") + " &7Corner 1" + (String)(hasCorner1 ? " &8(" + this.formatBlockLoc(corner1) + ")" : ""));
        this.sendMessage(player, (hasCorner2 ? "&a\u2713" : "&c\u2717") + " &7Corner 2" + (String)(hasCorner2 ? " &8(" + this.formatBlockLoc(corner2) + ")" : ""));
        if (hasCorner1 && hasCorner2) {
            int sizeX = Math.abs(corner2.getBlockX() - corner1.getBlockX()) + 1;
            int sizeY = Math.abs(corner2.getBlockY() - corner1.getBlockY()) + 1;
            int sizeZ = Math.abs(corner2.getBlockZ() - corner1.getBlockZ()) + 1;
            int volume = sizeX * sizeY * sizeZ;
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Region Size: &f" + sizeX + " x " + sizeY + " x " + sizeZ);
            this.sendMessage(player, "&7Total Blocks: &f" + this.formatNumber(volume));
            if (volume > 500000) {
                this.sendMessage(player, "&c\u26a0 Warning: Large region may cause lag during regeneration!");
            }
            this.sendMessage(player, "");
            this.sendMessage(player, "&aReady to save! Use: &e/arena save " + arena.getName());
        } else if (!hasCorner1) {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Next: &e/setcorner " + arena.getName() + " 1");
        } else {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Next: &e/setcorner " + arena.getName() + " 2");
        }
    }

    private String formatBlockLoc(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", (double)number / 1000000.0);
        }
        if (number >= 1000) {
            return String.format("%.1fK", (double)number / 1000.0);
        }
        return String.valueOf(number);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.filterCompletions(this.getArenaNames(), args[0]);
        }
        if (args.length == 2) {
            return this.filterCompletions(List.of("1", "2"), args[1]);
        }
        return new ArrayList<String>();
    }
}

