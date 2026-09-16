/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.commands.arena;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.ArenaState;
import com.ultimateduels.arena.model.ArenaType;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.commands.BaseCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ArenaCommand
extends BaseCommand {
    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "delete", "list", "setspawn", "setspectator", "setcorner1", "setcorner2", "setcorner", "corners", "save", "info", "enable", "disable", "tp", "regenerate", "wand", "assignkit", "unassignkit", "listassignedkits", "clearassignedkits", "enableassignment", "disableassignment", "setblockbreak", "setblockplace", "rename");

    public ArenaCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.arena");
    }

    @Override
    protected void execute(Player player, String[] args) {
        String subCommand;
        if (this.plugin.getArenaManager() == null) {
            this.sendMessage(player, "&cArena system is not available!");
            return;
        }
        if (args.length == 0) {
            this.sendHelp(player);
            return;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "create": {
                this.handleCreate(player, args);
                break;
            }
            case "delete": 
            case "remove": {
                this.handleDelete(player, args);
                break;
            }
            case "list": {
                this.handleList(player);
                break;
            }
            case "setspawn": {
                this.handleSetSpawn(player, args);
                break;
            }
            case "setspectator": 
            case "setspec": {
                this.handleSetSpectator(player, args);
                break;
            }
            case "setcorner1": 
            case "corner1": 
            case "pos1": 
            case "c1": {
                this.handleSetCorner1(player, args);
                break;
            }
            case "setcorner2": 
            case "corner2": 
            case "pos2": 
            case "c2": {
                this.handleSetCorner2(player, args);
                break;
            }
            case "setcorner": 
            case "corner": {
                this.handleSetCorner(player, args);
                break;
            }
            case "corners": {
                this.handleShowCorners(player, args);
                break;
            }
            case "wand": {
                this.handleWand(player, args);
                break;
            }
            case "save": {
                this.handleSave(player, args);
                break;
            }
            case "info": {
                this.handleInfo(player, args);
                break;
            }
            case "enable": {
                this.handleEnable(player, args);
                break;
            }
            case "disable": {
                this.handleDisable(player, args);
                break;
            }
            case "tp": 
            case "teleport": {
                this.handleTeleport(player, args);
                break;
            }
            case "regenerate": 
            case "regen": {
                this.handleRegenerate(player, args);
                break;
            }
            case "assignkit": {
                this.handleAssignKit(player, args);
                break;
            }
            case "unassignkit": {
                this.handleUnassignKit(player, args);
                break;
            }
            case "listassignedkits": 
            case "assignedkits": {
                this.handleListAssignedKits(player, args);
                break;
            }
            case "clearassignedkits": {
                this.handleClearAssignedKits(player, args);
                break;
            }
            case "enableassignment": {
                this.handleEnableAssignment(player, args);
                break;
            }
            case "disableassignment": {
                this.handleDisableAssignment(player, args);
                break;
            }
            case "setblockbreak": {
                this.handleSetBlockBreak(player, args);
                break;
            }
            case "setblockplace": {
                this.handleSetBlockPlace(player, args);
                break;
            }
            case "rename": {
                this.handleRename(player, args);
                break;
            }
            default: {
                this.sendHelp(player);
            }
        }
    }

    private void handleCreate(Player player, String[] args) {
        boolean success;
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena create <name> [type]");
            this.sendMessage(player, "&7Types: DUEL_1V1, DUEL_2V2, DUEL_3V3, PARTY, FFA, SUMO, BOXING, BRIDGE, BUILD_UHC");
            return;
        }
        String name = args[1].toLowerCase();
        if (this.plugin.getArenaManager().getArena(name) != null) {
            this.sendMessage(player, "&cAn arena with that name already exists!");
            return;
        }
        ArenaType type = ArenaType.DUEL_1V1;
        if (args.length >= 3) {
            try {
                type = ArenaType.valueOf(args[2].toUpperCase());
            }
            catch (IllegalArgumentException e) {
                this.sendMessage(player, "&cInvalid arena type! Valid types: DUEL_1V1, DUEL_2V2, DUEL_3V3, PARTY, FFA, SUMO, BOXING, BRIDGE, BUILD_UHC");
                return;
            }
        }
        if (!(success = this.plugin.getArenaManager().createArena(name, type))) {
            this.sendMessage(player, "&cFailed to create arena!");
            return;
        }
        DuelArena arena = this.plugin.getArenaManager().getArena(name);
        if (arena != null) {
            this.plugin.getArenaManager().setSpawnPoint1(name, player.getLocation().clone());
        }
        this.sendMessage(player, "&aArena &e" + name + " &acreated successfully!");
        this.sendMessage(player, "&7Type: &f" + type.name());
        this.sendMessage(player, "&7Spawn 1 set to your current location.");
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Next steps:");
        this.sendMessage(player, "&e/arena setspawn " + name + " 2 &7- Set spawn point 2");
        this.sendMessage(player, "&e/arena setspectator " + name + " &7- Set spectator spawn");
        this.sendMessage(player, "&e/arena setcorner1 " + name + " &7- Set schematic corner 1");
        this.sendMessage(player, "&e/arena setcorner2 " + name + " &7- Set schematic corner 2");
        this.sendMessage(player, "&e/arena save " + name + " &7- Save arena schematic");
        this.sendMessage(player, "&e/arena enable " + name + " &7- Enable the arena");
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena delete <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(name);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + name);
            return;
        }
        if (arena.getState() == ArenaState.IN_USE) {
            this.sendMessage(player, "&cThis arena is currently in use! Wait for the match to end.");
            return;
        }
        this.plugin.getArenaManager().deleteArena(name);
        this.sendMessage(player, "&cArena &e" + name + " &chas been deleted.");
    }

    private void handleList(Player player) {
        ArrayList<DuelArena> arenas = new ArrayList<DuelArena>(this.plugin.getArenaManager().getAllDuelArenas());
        arenas.addAll(this.plugin.getArenaManager().getAllFFAArenas());
        if (arenas.isEmpty()) {
            this.sendMessage(player, "&cNo arenas have been created yet.");
            this.sendMessage(player, "&7Use &e/arena create <name> &7to create one.");
            return;
        }
        this.sendHeader((CommandSender)player, "Arenas (" + arenas.size() + ")");
        for (DuelArena arena : arenas) {
            String status = arena.isEnabled() ? "&a\u2713" : "&c\u2717";
            String inUse = arena.getState() == ArenaState.IN_USE ? " &6(In Use)" : "";
            String hasBounds = arena.hasBounds() ? " &b[Schematic]" : "";
            this.sendMessage(player, status + " &f" + arena.getName() + " &7(" + arena.getArenaType().name() + ")" + inUse + hasBounds);
        }
        this.sendFooter((CommandSender)player);
    }

    private void handleSetSpawn(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena setspawn <arena> <1|2>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        String point = args[2];
        Location location = player.getLocation().clone();
        if (point.equals("1") || point.equalsIgnoreCase("pos1")) {
            this.plugin.getArenaManager().setSpawnPoint1(arenaName, location);
            this.sendMessage(player, "&aSpawn point 1 set for arena &e" + arenaName + "&a!");
        } else if (point.equals("2") || point.equalsIgnoreCase("pos2")) {
            this.plugin.getArenaManager().setSpawnPoint2(arenaName, location);
            this.sendMessage(player, "&aSpawn point 2 set for arena &e" + arenaName + "&a!");
        } else {
            this.sendUsage((CommandSender)player, "/arena setspawn <arena> <1|2>");
            return;
        }
        this.sendMessage(player, "&7Location: &f" + this.formatLocation(location));
    }

    private void handleSetSpectator(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena setspectator <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        Location location = player.getLocation().clone();
        this.plugin.getArenaManager().setSpectatorSpawn(arenaName, location);
        this.sendMessage(player, "&aSpectator spawn set for arena &e" + arenaName + "&a!");
        this.sendMessage(player, "&7Location: &f" + this.formatLocation(location));
    }

    private void handleSetCorner1(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena setcorner1 <arena>");
            this.sendMessage(player, "&7Sets the first corner of the arena schematic region.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        Location location = player.getLocation().getBlock().getLocation();
        this.plugin.getArenaManager().setCorner1(arenaName, location);
        this.sendMessage(player, "&aCorner 1 set for arena &e" + arenaName + "&a!");
        this.sendMessage(player, "&7Location: &f" + this.formatBlockLocation(location));
        arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena != null && arena.getCorner2() != null) {
            this.showRegionInfo(player, arena);
        } else {
            this.sendMessage(player, "&7Next: Set corner 2 with &e/arena setcorner2 " + arenaName);
        }
    }

    private void handleSetCorner2(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena setcorner2 <arena>");
            this.sendMessage(player, "&7Sets the second corner of the arena schematic region.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        Location location = player.getLocation().getBlock().getLocation();
        this.plugin.getArenaManager().setCorner2(arenaName, location);
        this.sendMessage(player, "&aCorner 2 set for arena &e" + arenaName + "&a!");
        this.sendMessage(player, "&7Location: &f" + this.formatBlockLocation(location));
        arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena != null && arena.getCorner1() != null) {
            this.showRegionInfo(player, arena);
            this.sendMessage(player, "&7Ready to save! Use &e/arena save " + arenaName);
        } else {
            this.sendMessage(player, "&7Next: Set corner 1 with &e/arena setcorner1 " + arenaName);
        }
    }

    private void handleSetCorner(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena setcorner <arena> <1|2>");
            this.sendMessage(player, "&7Sets a corner of the arena schematic region.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        String cornerNum = args[2];
        if (cornerNum.equals("1")) {
            this.handleSetCorner1(player, new String[]{"setcorner1", arenaName});
        } else if (cornerNum.equals("2")) {
            this.handleSetCorner2(player, new String[]{"setcorner2", arenaName});
        } else {
            this.sendMessage(player, "&cInvalid corner number! Use 1 or 2.");
        }
    }

    private void handleShowCorners(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena corners <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        this.sendHeader((CommandSender)player, "Arena Corners: " + arena.getDisplayName());
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        if (corner1 != null) {
            this.sendMessage(player, "&aCorner 1: &f" + this.formatBlockLocation(corner1));
        } else {
            this.sendMessage(player, "&cCorner 1: &7Not set");
        }
        if (corner2 != null) {
            this.sendMessage(player, "&aCorner 2: &f" + this.formatBlockLocation(corner2));
        } else {
            this.sendMessage(player, "&cCorner 2: &7Not set");
        }
        if (arena.hasBounds()) {
            this.showRegionInfo(player, arena);
        } else {
            this.sendMessage(player, "");
            this.sendMessage(player, "&7Both corners must be set for schematic saving.");
        }
        this.sendFooter((CommandSender)player);
    }

    private void handleWand(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena wand <arena>");
            this.sendMessage(player, "&7Gives you a wand to set corners by clicking.");
            this.sendMessage(player, "&7Left-click: Set Corner 1");
            this.sendMessage(player, "&7Right-click: Set Corner 2");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        this.plugin.getArenaManager().giveArenaWand(player, arenaName);
        this.sendMessage(player, "&aYou received the arena selection wand!");
        this.sendMessage(player, "&7Left-click a block to set &eCorner 1");
        this.sendMessage(player, "&7Right-click a block to set &eCorner 2");
        this.sendMessage(player, "&7Arena: &f" + arena.getDisplayName());
    }

    private void showRegionInfo(Player player, DuelArena arena) {
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        if (corner1 == null || corner2 == null) {
            return;
        }
        int sizeX = Math.abs(corner2.getBlockX() - corner1.getBlockX()) + 1;
        int sizeY = Math.abs(corner2.getBlockY() - corner1.getBlockY()) + 1;
        int sizeZ = Math.abs(corner2.getBlockZ() - corner1.getBlockZ()) + 1;
        int volume = sizeX * sizeY * sizeZ;
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Region Size: &f" + sizeX + " x " + sizeY + " x " + sizeZ);
        this.sendMessage(player, "&7Volume: &f" + this.formatNumber(volume) + " blocks");
        if (volume > 500000) {
            this.sendMessage(player, "&c\u26a0 Large region! Regeneration may cause lag.");
        } else if (volume > 100000) {
            this.sendMessage(player, "&e\u26a0 Medium region. Consider async regeneration.");
        }
    }

    private void handleSave(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena save <arena>");
            this.sendMessage(player, "&7This will save the arena schematic for regeneration.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        if (!arena.hasBounds()) {
            this.sendMessage(player, "&cArena bounds (corner1 and corner2) must be set before saving!");
            this.sendMessage(player, "&7Use &e/arena setcorner1 " + arenaName + " &7and &e/arena setcorner2 " + arenaName);
            this.sendMessage(player, "&7Or use &e/arena wand " + arenaName + " &7for click-based selection.");
            return;
        }
        this.sendMessage(player, "&eSaving arena schematic...");
        this.showRegionInfo(player, arena);
        this.plugin.getArenaManager().saveSchematic(arenaName).thenAccept(success -> this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            if (success.booleanValue()) {
                this.sendMessage(player, "&aArena schematic saved successfully!");
                this.sendMessage(player, "&7The arena will now regenerate after each match.");
            } else {
                this.sendMessage(player, "&cFailed to save arena schematic!");
                this.sendMessage(player, "&7Make sure WorldEdit/FAWE is installed.");
            }
        }));
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena info <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        this.sendHeader((CommandSender)player, "Arena: " + arena.getDisplayName());
        this.sendMessage(player, "&7Name: &f" + arena.getName());
        this.sendMessage(player, "&7Display Name: &f" + arena.getDisplayName());
        this.sendMessage(player, "&7Type: &f" + arena.getArenaType().name());
        this.sendMessage(player, "&7Status: " + (arena.isEnabled() ? "&aEnabled" : "&cDisabled"));
        this.sendMessage(player, "&7State: " + arena.getState().getColoredName());
        this.sendMessage(player, "&7In Use: " + (arena.getState() == ArenaState.IN_USE ? "&cYes" : "&aNo"));
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Spawn 1: &f" + (arena.getSpawnPoint1() != null ? this.formatLocation(arena.getSpawnPoint1()) : "&cNot set"));
        this.sendMessage(player, "&7Spawn 2: &f" + (arena.getSpawnPoint2() != null ? this.formatLocation(arena.getSpawnPoint2()) : "&cNot set"));
        this.sendMessage(player, "&7Spectator: &f" + (arena.getSpectatorSpawn() != null ? this.formatLocation(arena.getSpectatorSpawn()) : "&cNot set"));
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lKit Assignment:");
        this.sendMessage(player, "&7Exclusive Mode: " + (arena.isUsingAssignedKits() ? "&aENABLED" : "&cDISABLED"));
        if (arena.hasAssignedKits()) {
            this.sendMessage(player, "&7Assigned Kits: &e" + String.join((CharSequence)", ", arena.getAssignedKits()));
        } else {
            this.sendMessage(player, "&7Assigned Kits: &7None");
        }
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lBlock Protection:");
        this.sendMessage(player, "&7Block Breaking: " + (arena.isAllowBlockBreak() ? "&aALLOWED" : "&cBLOCKED"));
        this.sendMessage(player, "&7Block Placing: " + (arena.isAllowBlockPlace() ? "&aALLOWED" : "&cBLOCKED"));
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lSchematic Region:");
        this.sendMessage(player, "&7Corner 1: &f" + (arena.getCorner1() != null ? this.formatBlockLocation(arena.getCorner1()) : "&cNot set"));
        this.sendMessage(player, "&7Corner 2: &f" + (arena.getCorner2() != null ? this.formatBlockLocation(arena.getCorner2()) : "&cNot set"));
        this.sendMessage(player, "&7Has Bounds: " + (arena.hasBounds() ? "&aYes" : "&cNo"));
        if (arena.hasBounds()) {
            Location c1 = arena.getCorner1();
            Location c2 = arena.getCorner2();
            int sizeX = Math.abs(c2.getBlockX() - c1.getBlockX()) + 1;
            int sizeY = Math.abs(c2.getBlockY() - c1.getBlockY()) + 1;
            int sizeZ = Math.abs(c2.getBlockZ() - c1.getBlockZ()) + 1;
            this.sendMessage(player, "&7Region Size: &f" + sizeX + " x " + sizeY + " x " + sizeZ);
        }
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Schematic: &f" + (arena.getSchematicName() != null ? arena.getSchematicName() : "&cNot saved"));
        this.sendMessage(player, "&7Fully Configured: " + (arena.isFullyConfigured() ? "&aYes" : "&cNo"));
        this.sendFooter((CommandSender)player);
    }

    private void handleEnable(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena enable <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        if (!arena.isFullyConfigured()) {
            this.sendMessage(player, "&cArena must be fully configured before enabling!");
            this.sendMessage(player, "&7" + arena.getConfigurationStatus());
            return;
        }
        boolean success = this.plugin.getArenaManager().setArenaEnabled(arenaName, true);
        if (success) {
            this.sendMessage(player, "&aArena &e" + arenaName + " &ahas been enabled!");
        } else {
            this.sendMessage(player, "&cFailed to enable arena!");
        }
    }

    private void handleDisable(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena disable <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        if (arena.getState() == ArenaState.IN_USE) {
            this.sendMessage(player, "&cThis arena is currently in use!");
            return;
        }
        boolean success = this.plugin.getArenaManager().setArenaEnabled(arenaName, false);
        if (success) {
            this.sendMessage(player, "&cArena &e" + arenaName + " &chas been disabled!");
        } else {
            this.sendMessage(player, "&cFailed to disable arena!");
        }
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena tp <arena> [1|2|spec|corner1|corner2]");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        Location teleportLoc = null;
        String pointName = "spawn 1";
        if (args.length >= 3) {
            String point;
            switch (point = args[2].toLowerCase()) {
                case "1": 
                case "pos1": 
                case "spawn1": {
                    teleportLoc = arena.getSpawnPoint1();
                    pointName = "spawn 1";
                    break;
                }
                case "2": 
                case "pos2": 
                case "spawn2": {
                    teleportLoc = arena.getSpawnPoint2();
                    pointName = "spawn 2";
                    break;
                }
                case "spec": 
                case "spectator": {
                    teleportLoc = arena.getSpectatorSpawn();
                    pointName = "spectator spawn";
                    break;
                }
                case "corner1": 
                case "c1": {
                    teleportLoc = arena.getCorner1();
                    pointName = "corner 1";
                    break;
                }
                case "corner2": 
                case "c2": {
                    teleportLoc = arena.getCorner2();
                    pointName = "corner 2";
                }
            }
        } else {
            teleportLoc = arena.getSpawnPoint1();
        }
        if (teleportLoc == null) {
            this.sendMessage(player, "&cThis spawn point is not set!");
            return;
        }
        player.teleport(teleportLoc);
        this.sendMessage(player, "&aTeleported to &e" + arenaName + " &a" + pointName + "!");
    }

    private void handleRegenerate(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena regenerate <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        if (arena.getSchematicName() == null) {
            this.sendMessage(player, "&cThis arena doesn't have a saved schematic!");
            this.sendMessage(player, "&7Use &e/arena save " + arenaName + " &7first.");
            return;
        }
        this.sendMessage(player, "&eRegenerating arena...");
        this.plugin.getArenaManager().pasteSchematic(arenaName).thenAccept(success -> this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            if (success.booleanValue()) {
                this.sendMessage(player, "&aArena regenerated successfully!");
            } else {
                this.sendMessage(player, "&cFailed to regenerate arena!");
            }
        }));
    }

    private void handleAssignKit(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena assignkit <arena> <kit>");
            this.sendMessage(player, "&7Assigns a kit to this arena EXCLUSIVELY.");
            this.sendMessage(player, "&7Arena will ONLY accept duels using this kit.");
            this.sendMessage(player, "&7Use &e/arena enableassignment <arena> &7to activate.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        String kitName = args[2];
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        if (!this.plugin.getKitManager().adminKitExists(kitName)) {
            this.sendMessage(player, "&cKit not found: " + kitName);
            return;
        }
        boolean success = this.plugin.getArenaManager().assignKitToArena(arenaName, kitName);
        if (success) {
            this.sendMessage(player, "&aAssigned kit &e" + kitName + " &ato arena &e" + arenaName + "&a!");
            if (!arena.isUsingAssignedKits()) {
                this.sendMessage(player, "");
                this.sendMessage(player, "&7Kit assigned, but &cexclusive assignment is disabled&7.");
                this.sendMessage(player, "&7Use &e/arena enableassignment " + arenaName + " &7to activate.");
            }
        } else {
            this.sendMessage(player, "&cFailed to assign kit!");
        }
    }

    private void handleUnassignKit(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena unassignkit <arena> <kit>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        String kitName = args[2];
        boolean success = this.plugin.getArenaManager().unassignKitFromArena(arenaName, kitName);
        if (success) {
            this.sendMessage(player, "&aUnassigned kit &e" + kitName + " &afrom arena &e" + arenaName + "&a!");
        } else {
            this.sendMessage(player, "&cFailed to unassign kit!");
        }
    }

    private void handleListAssignedKits(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena listassignedkits <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        this.sendHeader((CommandSender)player, "Assigned Kits: " + arena.getDisplayName());
        this.sendMessage(player, "&7Exclusive Assignment: " + (arena.isUsingAssignedKits() ? "&aENABLED" : "&cDISABLED"));
        this.sendMessage(player, "");
        if (arena.getAssignedKits().isEmpty()) {
            this.sendMessage(player, "&7No kits assigned.");
            if (arena.isUsingAssignedKits()) {
                this.sendMessage(player, "&c\u26a0 Exclusive assignment is ON but no kits assigned!");
                this.sendMessage(player, "&c  This arena will NOT accept ANY duels!");
            }
        } else {
            this.sendMessage(player, "&6Assigned Kits:");
            for (String kit : arena.getAssignedKits()) {
                this.sendMessage(player, "  &7- &e" + kit);
            }
        }
        this.sendMessage(player, "");
        this.sendFooter((CommandSender)player);
    }

    private void handleClearAssignedKits(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena clearassignedkits <arena>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        int count = arena.getAssignedKits().size();
        boolean success = this.plugin.getArenaManager().clearArenaAssignedKits(arenaName);
        if (success) {
            this.sendMessage(player, "&aCleared &e" + count + " &aassigned kits from arena &e" + arenaName + "&a!");
        } else {
            this.sendMessage(player, "&cFailed to clear assigned kits!");
        }
    }

    private void handleEnableAssignment(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena enableassignment <arena>");
            this.sendMessage(player, "&7Enables exclusive kit assignment for this arena.");
            this.sendMessage(player, "&7Arena will ONLY accept kits in the assigned list.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: " + arenaName);
            return;
        }
        boolean success = this.plugin.getArenaManager().setArenaUseAssignedKits(arenaName, true);
        if (success) {
            this.sendMessage(player, "&aEnabled exclusive kit assignment for &e" + arenaName + "&a!");
            if (arena.getAssignedKits().isEmpty()) {
                this.sendMessage(player, "");
                this.sendMessage(player, "&c\u26a0 WARNING: No kits assigned!");
                this.sendMessage(player, "&c  This arena will NOT accept ANY duels!");
                this.sendMessage(player, "&7Use &e/arena assignkit " + arenaName + " <kit> &7to assign kits.");
            } else {
                this.sendMessage(player, "&7This arena will ONLY accept these kits:");
                for (String kit : arena.getAssignedKits()) {
                    this.sendMessage(player, "  &7- &e" + kit);
                }
            }
        } else {
            this.sendMessage(player, "&cFailed to enable assignment!");
        }
    }

    private void handleDisableAssignment(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/arena disableassignment <arena>");
            this.sendMessage(player, "&7Disables exclusive kit assignment.");
            this.sendMessage(player, "&7Arena will use the compatibility system instead.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        boolean success = this.plugin.getArenaManager().setArenaUseAssignedKits(arenaName, false);
        if (success) {
            this.sendMessage(player, "&aDisabled exclusive kit assignment for &e" + arenaName + "&a!");
            this.sendMessage(player, "&7Arena will now use the compatibility system.");
        } else {
            this.sendMessage(player, "&cFailed to disable assignment!");
        }
    }

    private void handleSetBlockBreak(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena setblockbreak <arena> <true|false>");
            this.sendMessage(player, "&7Controls whether players can break blocks in this arena.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        String value = args[2].toLowerCase();
        if (!value.equals("true") && !value.equals("false")) {
            this.sendMessage(player, "&cInvalid value! Use &etrue &cor &efalse&c.");
            return;
        }
        boolean allow = Boolean.parseBoolean(value);
        boolean success = this.plugin.getArenaManager().setArenaAllowBlockBreak(arenaName, allow);
        if (success) {
            this.sendMessage(player, "&aBlock breaking in &e" + arenaName + "&a: " + (allow ? "&aALLOWED" : "&cBLOCKED"));
        } else {
            this.sendMessage(player, "&cFailed to update block break setting!");
        }
    }

    private void handleSetBlockPlace(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena setblockplace <arena> <true|false>");
            this.sendMessage(player, "&7Controls whether players can place blocks in this arena.");
            return;
        }
        String arenaName = args[1].toLowerCase();
        String value = args[2].toLowerCase();
        if (!value.equals("true") && !value.equals("false")) {
            this.sendMessage(player, "&cInvalid value! Use &etrue &cor &efalse&c.");
            return;
        }
        boolean allow = Boolean.parseBoolean(value);
        boolean success = this.plugin.getArenaManager().setArenaAllowBlockPlace(arenaName, allow);
        if (success) {
            this.sendMessage(player, "&aBlock placing in &e" + arenaName + "&a: " + (allow ? "&aALLOWED" : "&cBLOCKED"));
        } else {
            this.sendMessage(player, "&cFailed to update block place setting!");
        }
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/arena rename <oldName> <newName>");
            this.sendMessage(player, "&7Renames an arena. The arena must not be in use.");
            return;
        }
        String oldName = args[1].toLowerCase();
        String newName = args[2].toLowerCase();
        DuelArena arena = this.plugin.getArenaManager().getArena(oldName);
        if (arena == null) {
            this.sendMessage(player, "&cArena not found: &e" + oldName);
            return;
        }
        if (arena.getState() == ArenaState.IN_USE) {
            this.sendMessage(player, "&cThis arena is currently in use! Wait for the match to end.");
            return;
        }
        if (!newName.matches("[a-z0-9_\\-]+")) {
            this.sendMessage(player, "&cInvalid arena name! Use only letters, numbers, underscores, and hyphens.");
            return;
        }
        if (this.plugin.getArenaManager().arenaExists(newName)) {
            this.sendMessage(player, "&cAn arena with the name &e" + newName + " &calready exists!");
            return;
        }
        boolean success = this.plugin.getArenaManager().renameArena(oldName, newName);
        if (success) {
            this.sendMessage(player, "&aArena renamed: &e" + oldName + " &a\u2192 &e" + newName);
            this.sendMessage(player, "&7Note: Schematic file name has NOT changed.");
            this.sendMessage(player, "&7If this arena has a schematic, re-save it with &e/arena save " + newName);
        } else {
            this.sendMessage(player, "&cFailed to rename arena! The new name may already be taken.");
        }
    }

    private void sendHelp(Player player) {
        this.sendHeader((CommandSender)player, "Arena Commands");
        this.sendMessage(player, "&e/arena create <name> [type] &7- Create an arena");
        this.sendMessage(player, "&e/arena delete <name> &7- Delete an arena");
        this.sendMessage(player, "&e/arena list &7- List all arenas");
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lKit Assignment (Optional):");
        this.sendMessage(player, "&e/arena assignkit <arena> <kit> &7- Assign kit (exclusive)");
        this.sendMessage(player, "&e/arena unassignkit <arena> <kit> &7- Remove assignment");
        this.sendMessage(player, "&e/arena listassignedkits <arena> &7- Show assigned kits");
        this.sendMessage(player, "&e/arena clearassignedkits <arena> &7- Clear all assignments");
        this.sendMessage(player, "&e/arena enableassignment <arena> &7- Enable exclusive mode");
        this.sendMessage(player, "&e/arena disableassignment <arena> &7- Disable exclusive mode");
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lBlock Protection:");
        this.sendMessage(player, "&e/arena setblockbreak <arena> <true|false> &7- Toggle breaking");
        this.sendMessage(player, "&e/arena setblockplace <arena> <true|false> &7- Toggle placing");
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lSpawn Points:");
        this.sendMessage(player, "&e/arena setspawn <arena> <1|2> &7- Set spawn point");
        this.sendMessage(player, "&e/arena setspectator <arena> &7- Set spectator spawn");
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lSchematic Region:");
        this.sendMessage(player, "&e/arena setcorner1 <arena> &7- Set schematic corner 1");
        this.sendMessage(player, "&e/arena setcorner2 <arena> &7- Set schematic corner 2");
        this.sendMessage(player, "&e/arena corners <arena> &7- View corner positions");
        this.sendMessage(player, "&e/arena wand <arena> &7- Get selection wand");
        this.sendMessage(player, "&e/arena save <arena> &7- Save arena schematic");
        this.sendMessage(player, "&e/arena regenerate <arena> &7- Regenerate arena");
        this.sendMessage(player, "");
        this.sendMessage(player, "&b&lOther:");
        this.sendMessage(player, "&e/arena info <arena> &7- View arena info");
        this.sendMessage(player, "&e/arena enable <arena> &7- Enable an arena");
        this.sendMessage(player, "&e/arena disable <arena> &7- Disable an arena");
        this.sendMessage(player, "&e/arena tp <arena> [point] &7- Teleport to arena");
        this.sendMessage(player, "&e/arena rename <old> <new> &7- Rename an arena");
        this.sendFooter((CommandSender)player);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        String subCommand;
        if (args.length == 1) {
            return this.filterCompletions(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2) {
            switch (subCommand = args[0].toLowerCase()) {
                case "create": {
                    return List.of("<name>");
                }
                case "delete": 
                case "remove": 
                case "setspawn": 
                case "setspectator": 
                case "setspec": 
                case "setcorner1": 
                case "setcorner2": 
                case "setcorner": 
                case "corner1": 
                case "corner2": 
                case "corners": 
                case "wand": 
                case "save": 
                case "info": 
                case "enable": 
                case "disable": 
                case "tp": 
                case "teleport": 
                case "regenerate": 
                case "regen": 
                case "assignkit": 
                case "unassignkit": 
                case "listassignedkits": 
                case "clearassignedkits": 
                case "enableassignment": 
                case "disableassignment": 
                case "setblockbreak": 
                case "setblockplace": 
                case "rename": {
                    return this.filterCompletions(this.getAllArenaNames(), args[1]);
                }
            }
        }
        if (args.length == 3) {
            switch (subCommand = args[0].toLowerCase()) {
                case "create": {
                    return this.filterCompletions(Arrays.stream(ArenaType.values()).map(Enum::name).collect(Collectors.toList()), args[2]);
                }
                case "setspawn": {
                    return this.filterCompletions(List.of("1", "2"), args[2]);
                }
                case "setcorner": 
                case "corner": {
                    return this.filterCompletions(List.of("1", "2"), args[2]);
                }
                case "tp": 
                case "teleport": {
                    return this.filterCompletions(List.of("1", "2", "spec", "corner1", "corner2"), args[2]);
                }
                case "assignkit": 
                case "unassignkit": {
                    return this.filterCompletions(new ArrayList<String>(this.plugin.getKitManager().getAdminKitNames()), args[2]);
                }
                case "setblockbreak": 
                case "setblockplace": {
                    return this.filterCompletions(List.of("true", "false"), args[2]);
                }
                case "rename": {
                    return List.of("<newName>");
                }
            }
        }
        return new ArrayList<String>();
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f (%s)", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld() != null ? loc.getWorld().getName() : "unknown");
    }

    private String formatBlockLocation(Location loc) {
        return String.format("%d, %d, %d (%s)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld() != null ? loc.getWorld().getName() : "unknown");
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

    private List<String> getAllArenaNames() {
        if (this.plugin.getArenaManager() == null) {
            return new ArrayList<String>();
        }
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(this.plugin.getArenaManager().getDuelArenaNames());
        names.addAll(this.plugin.getArenaManager().getFFAArenaNames());
        return names;
    }
}

