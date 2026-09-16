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

public class ArenaSetSpawnCommand
extends BaseCommand {
    public ArenaSetSpawnCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.arena");
    }

    @Override
    protected void execute(Player player, String[] args) {
        if (this.plugin.getArenaManager() == null) {
            this.sendMessage(player, "&cArena system is not available!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/setspawn <arena> <1|2|spectator>");
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Quick command to set arena spawn points.");
            this.sendMessage(player, "&7Example: &e/setspawn myarena 1");
            return;
        }
        String arenaName = args[0].toLowerCase();
        String spawnType = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: &e" + arenaName);
            this.sendMessage(player, "&7Use &e/arena list &7to see all arenas.");
            return;
        }
        Location location = player.getLocation().clone();
        switch (spawnType) {
            case "1": 
            case "pos1": 
            case "spawn1": 
            case "one": {
                this.plugin.getArenaManager().setSpawnPoint1(arenaName, location);
                this.sendMessage(player, "&aSpawn point &e1 &aset for arena &e" + arena.getDisplayName() + "&a!");
                this.sendLocationInfo(player, location);
                break;
            }
            case "2": 
            case "pos2": 
            case "spawn2": 
            case "two": {
                this.plugin.getArenaManager().setSpawnPoint2(arenaName, location);
                this.sendMessage(player, "&aSpawn point &e2 &aset for arena &e" + arena.getDisplayName() + "&a!");
                this.sendLocationInfo(player, location);
                break;
            }
            case "spec": 
            case "spectator": 
            case "spectate": {
                this.plugin.getArenaManager().setSpectatorSpawn(arenaName, location);
                this.sendMessage(player, "&aSpectator spawn set for arena &e" + arena.getDisplayName() + "&a!");
                this.sendLocationInfo(player, location);
                break;
            }
            default: {
                this.sendMessage(player, "&cInvalid spawn type: &e" + spawnType);
                this.sendMessage(player, "&7Valid types: &f1, 2, spectator");
            }
        }
        arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena != null) {
            this.showSetupProgress(player, arena);
        }
    }

    private void sendLocationInfo(Player player, Location loc) {
        this.sendMessage(player, "&7Position: &f" + String.format("%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ()));
        this.sendMessage(player, "&7Facing: &f" + String.format("%.1f, %.1f", Float.valueOf(loc.getYaw()), Float.valueOf(loc.getPitch())));
    }

    private void showSetupProgress(Player player, DuelArena arena) {
        this.sendMessage(player, "");
        this.sendMessage(player, "&7\u2501\u2501\u2501 Arena Setup Progress \u2501\u2501\u2501");
        boolean hasSpawn1 = arena.getSpawnPoint1() != null;
        this.sendMessage(player, (hasSpawn1 ? "&a\u2713" : "&c\u2717") + " &7Spawn Point 1");
        boolean hasSpawn2 = arena.getSpawnPoint2() != null;
        this.sendMessage(player, (hasSpawn2 ? "&a\u2713" : "&c\u2717") + " &7Spawn Point 2");
        boolean hasSpec = arena.getSpectatorSpawn() != null;
        this.sendMessage(player, (hasSpec ? "&a\u2713" : "&7\u25cb") + " &7Spectator Spawn &8(optional)");
        boolean hasSchematic = arena.getSchematicName() != null;
        this.sendMessage(player, (hasSchematic ? "&a\u2713" : "&7\u25cb") + " &7Schematic Saved &8(optional)");
        boolean isEnabled = arena.isEnabled();
        this.sendMessage(player, (isEnabled ? "&a\u2713" : "&c\u2717") + " &7Enabled");
        if (!hasSpawn1) {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Next: &e/setspawn " + arena.getName() + " 1");
        } else if (!hasSpawn2) {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Next: &e/setspawn " + arena.getName() + " 2");
        } else if (!isEnabled) {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Ready! Enable with: &e/arena enable " + arena.getName());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.filterCompletions(this.getArenaNames(), args[0]);
        }
        if (args.length == 2) {
            return this.filterCompletions(List.of("1", "2", "spectator"), args[1]);
        }
        return new ArrayList<String>();
    }
}

