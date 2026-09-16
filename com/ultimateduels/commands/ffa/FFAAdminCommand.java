/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.ArenaState;
import com.ultimateduels.arena.model.ArenaType;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFAAdminCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public FFAAdminCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ultimateduels.admin.ffa")) {
            sender.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            this.showHelp(sender);
            return true;
        }
        String subCommand = args[0].toLowerCase();
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            sender.sendMessage("\u00a7cArena system is not available!");
            return true;
        }
        switch (subCommand) {
            case "create": {
                this.handleCreate(sender, args, arenaManager);
                break;
            }
            case "delete": 
            case "remove": {
                this.handleDelete(sender, args, arenaManager);
                break;
            }
            case "setspawn": 
            case "addspawn": {
                this.handleSetSpawn(sender, args, arenaManager);
                break;
            }
            case "clearspawns": {
                this.handleClearSpawns(sender, args, arenaManager);
                break;
            }
            case "enable": {
                this.handleEnable(sender, args, arenaManager);
                break;
            }
            case "disable": {
                this.handleDisable(sender, args, arenaManager);
                break;
            }
            case "list": {
                this.handleList(sender, arenaManager);
                break;
            }
            case "info": {
                this.handleInfo(sender, args, arenaManager);
                break;
            }
            case "setkit": 
            case "linkkit": {
                this.handleSetKit(sender, args, arenaManager);
                break;
            }
            case "setcorner1": 
            case "corner1": 
            case "pos1": {
                this.handleSetCorner1(sender, args, arenaManager);
                break;
            }
            case "setcorner2": 
            case "corner2": 
            case "pos2": {
                this.handleSetCorner2(sender, args, arenaManager);
                break;
            }
            case "saveschematic": 
            case "save": {
                this.handleSaveSchematic(sender, args, arenaManager);
                break;
            }
            case "regenerate": 
            case "regen": {
                this.handleRegenerate(sender, args, arenaManager);
                break;
            }
            case "reload": {
                this.handleReload(sender);
                break;
            }
            default: {
                this.showHelp(sender);
            }
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 3) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin create <name> <kit>");
            return;
        }
        String name = args[1].toLowerCase();
        String kitName = args[2].toLowerCase();
        if (arenaManager.arenaExists(name)) {
            sender.sendMessage("\u00a7cAn arena with that name already exists!");
            return;
        }
        if (this.plugin.getKitManager() == null || this.plugin.getKitManager().getAdminKit(kitName) == null) {
            sender.sendMessage("\u00a7cKit '" + kitName + "' does not exist!");
            if (this.plugin.getKitManager() != null) {
                sender.sendMessage("\u00a77Available kits: \u00a7e" + String.join((CharSequence)", ", this.plugin.getKitManager().getAdminKitNames()));
            }
            return;
        }
        boolean created = arenaManager.createArena(name, ArenaType.FFA);
        if (!created) {
            sender.sendMessage("\u00a7cFailed to create FFA arena!");
            return;
        }
        arenaManager.setFFAArenaKit(name, kitName);
        sender.sendMessage("\u00a7aFFA arena '\u00a7e" + name + "\u00a7a' created!");
        sender.sendMessage("\u00a7aLinked to kit: \u00a7e" + kitName);
        sender.sendMessage("");
        sender.sendMessage("\u00a77Next steps:");
        sender.sendMessage("\u00a7e1. \u00a77Stand at corner 1: \u00a7f/ffaadmin setcorner1 " + name);
        sender.sendMessage("\u00a7e2. \u00a77Stand at corner 2: \u00a7f/ffaadmin setcorner2 " + name);
        sender.sendMessage("\u00a7e3. \u00a77Save schematic: \u00a7f/ffaadmin save " + name);
        sender.sendMessage("\u00a7e4. \u00a77Add spawns: \u00a7f/ffaadmin setspawn " + name);
        sender.sendMessage("\u00a7e5. \u00a77Enable: \u00a7f/ffaadmin enable " + name);
    }

    private void handleDelete(CommandSender sender, String[] args, ArenaManager arenaManager) {
        FFAArenaInstance ffaArena;
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin delete <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        if (this.plugin.getFFAManager() != null && (ffaArena = this.plugin.getFFAManager().getArena(name)) != null && ffaArena.getPlayerCount() > 0) {
            sender.sendMessage("\u00a7cCannot delete arena while players are in it!");
            return;
        }
        arenaManager.deleteArena(name);
        sender.sendMessage("\u00a7cFFA arena '\u00a7e" + name + "\u00a7c' has been deleted!");
    }

    private void handleSetSpawn(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return;
        }
        Player player = (Player)sender;
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin setspawn <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        arenaManager.addFFASpawnPoint(name, player.getLocation());
        int spawnCount = arena.getSpawnPoints().size();
        sender.sendMessage("\u00a7aSpawn point \u00a7e#" + spawnCount + " \u00a7aadded!");
        sender.sendMessage("\u00a77Total spawn points: \u00a7e" + spawnCount);
        if (spawnCount < 5) {
            sender.sendMessage("\u00a77Tip: Add at least \u00a7e5 \u00a77spawn points for best experience.");
        }
    }

    private void handleClearSpawns(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin clearspawns <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        arenaManager.clearFFASpawnPoints(name);
        sender.sendMessage("\u00a7cCleared all spawn points from '\u00a7e" + name + "\u00a7c'!");
    }

    private void handleSetCorner1(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return;
        }
        Player player = (Player)sender;
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin setcorner1 <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        arenaManager.setCorner1(name, player.getLocation());
        sender.sendMessage("\u00a7aCorner 1 set for '\u00a7e" + name + "\u00a7a'!");
        sender.sendMessage("\u00a77Location: \u00a7f" + this.formatLocation(player.getLocation()));
        if (arena.getCorner2() != null) {
            sender.sendMessage("\u00a77Both corners set! Use \u00a7e/ffaadmin save " + name + " \u00a77to save schematic.");
        } else {
            sender.sendMessage("\u00a77Now set corner 2: \u00a7e/ffaadmin setcorner2 " + name);
        }
    }

    private void handleSetCorner2(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return;
        }
        Player player = (Player)sender;
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin setcorner2 <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        arenaManager.setCorner2(name, player.getLocation());
        sender.sendMessage("\u00a7aCorner 2 set for '\u00a7e" + name + "\u00a7a'!");
        sender.sendMessage("\u00a77Location: \u00a7f" + this.formatLocation(player.getLocation()));
        if (arena.getCorner1() != null) {
            sender.sendMessage("\u00a77Both corners set! Use \u00a7e/ffaadmin save " + name + " \u00a77to save schematic.");
        } else {
            sender.sendMessage("\u00a77Now set corner 1: \u00a7e/ffaadmin setcorner1 " + name);
        }
    }

    private void handleSaveSchematic(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin save <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        if (!arena.hasBounds()) {
            sender.sendMessage("\u00a7cArena corners not set!");
            sender.sendMessage("\u00a77Use \u00a7e/ffaadmin setcorner1 " + name + " \u00a77and \u00a7e/ffaadmin setcorner2 " + name);
            return;
        }
        sender.sendMessage("\u00a7eSaving schematic for '\u00a7f" + name + "\u00a7e'...");
        arenaManager.saveSchematic(name).thenAccept(success -> {
            if (success.booleanValue()) {
                sender.sendMessage("\u00a7aSchematic saved successfully!");
                sender.sendMessage("\u00a77The arena can now be regenerated after matches.");
            } else {
                sender.sendMessage("\u00a7cFailed to save schematic!");
                sender.sendMessage("\u00a77Make sure WorldEdit is installed and corners are set correctly.");
            }
        });
    }

    private void handleRegenerate(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin regenerate <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        if (arena.getSchematicName() == null) {
            sender.sendMessage("\u00a7cNo schematic saved for this arena!");
            sender.sendMessage("\u00a77Use \u00a7e/ffaadmin save " + name + " \u00a77first.");
            return;
        }
        sender.sendMessage("\u00a7eRegenerating arena '\u00a7f" + name + "\u00a7e'...");
        arenaManager.regenerateArena(name).thenAccept(success -> {
            if (success.booleanValue()) {
                sender.sendMessage("\u00a7aArena regenerated successfully!");
            } else {
                sender.sendMessage("\u00a7cFailed to regenerate arena!");
            }
        });
    }

    private void handleEnable(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin enable <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        ArrayList<CallSite> missing = new ArrayList<CallSite>();
        if (arena.getLinkedKit() == null) {
            missing.add((CallSite)((Object)("\u00a7c  \u2717 No kit linked \u00a77(/ffaadmin setkit " + name + " <kit>)")));
        }
        if (arena.getSpawnPoints().isEmpty()) {
            missing.add((CallSite)((Object)("\u00a7c  \u2717 No spawn points \u00a77(/ffaadmin setspawn " + name + ")")));
        }
        if (!missing.isEmpty()) {
            sender.sendMessage("\u00a7cCannot enable arena. Missing requirements:");
            for (String string : missing) {
                sender.sendMessage(string);
            }
            return;
        }
        arena.setEnabled(true);
        arena.setState(ArenaState.AVAILABLE);
        arenaManager.saveFFAArenas();
        sender.sendMessage("\u00a7aFFA arena '\u00a7e" + name + "\u00a7a' has been enabled!");
        if (this.plugin.getFFAManager() != null) {
            this.plugin.getFFAManager().reloadArenas();
            sender.sendMessage("\u00a77FFA system reloaded.");
        }
    }

    private void handleDisable(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin disable <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        arena.setEnabled(false);
        arena.setState(ArenaState.DISABLED);
        arenaManager.saveFFAArenas();
        sender.sendMessage("\u00a7cFFA arena '\u00a7e" + name + "\u00a7c' has been disabled!");
        if (this.plugin.getFFAManager() != null) {
            this.plugin.getFFAManager().reloadArenas();
        }
    }

    private void handleList(CommandSender sender, ArenaManager arenaManager) {
        Collection<DuelArena> ffaArenas = arenaManager.getAllFFAArenas();
        if (ffaArenas.isEmpty()) {
            sender.sendMessage("\u00a7cNo FFA arenas have been created.");
            sender.sendMessage("\u00a77Use \u00a7e/ffaadmin create <name> <kit> \u00a77to create one.");
            return;
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac FFA ARENAS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        for (DuelArena arena : ffaArenas) {
            FFAArenaInstance ffaInstance;
            String status = arena.isEnabled() ? "\u00a7a\u2713" : "\u00a7c\u2717";
            String kit = arena.getLinkedKit() != null ? arena.getLinkedKit() : "\u00a77None";
            int spawns = arena.getSpawnPoints().size();
            String schematic = arena.getSchematicName() != null ? "\u00a7a\u2713" : "\u00a7c\u2717";
            int players = 0;
            if (this.plugin.getFFAManager() != null && (ffaInstance = this.plugin.getFFAManager().getArena(arena.getName())) != null) {
                players = ffaInstance.getPlayerCount();
            }
            sender.sendMessage(status + " \u00a7f" + arena.getName() + " \u00a77| Kit: \u00a7e" + kit + " \u00a77| Spawns: \u00a7e" + spawns + " \u00a77| Schem: " + schematic + " \u00a77| Players: \u00a7e" + players);
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
    }

    private void handleInfo(CommandSender sender, String[] args, ArenaManager arenaManager) {
        FFAArenaInstance ffaInstance;
        if (args.length < 2) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin info <name>");
            return;
        }
        String name = args[1].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(name);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + name + "' not found!");
            return;
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac FFA Arena: " + arena.getName() + " \u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        sender.sendMessage("\u00a77Status: " + (arena.isEnabled() ? "\u00a7aEnabled" : "\u00a7cDisabled"));
        sender.sendMessage("\u00a77State: \u00a7e" + String.valueOf((Object)arena.getState()));
        sender.sendMessage("\u00a77Linked Kit: \u00a7e" + (arena.getLinkedKit() != null ? arena.getLinkedKit() : "None"));
        sender.sendMessage("\u00a77Spawn Points: \u00a7e" + arena.getSpawnPoints().size());
        sender.sendMessage("\u00a77Schematic: " + (String)(arena.getSchematicName() != null ? "\u00a7a" + arena.getSchematicName() : "\u00a7cNot saved"));
        sender.sendMessage("\u00a77Corners: " + (arena.hasBounds() ? "\u00a7aSet" : "\u00a7cNot set"));
        if (this.plugin.getFFAManager() != null && (ffaInstance = this.plugin.getFFAManager().getArena(name)) != null) {
            sender.sendMessage("");
            sender.sendMessage("\u00a77\u00a7lRuntime Stats:");
            sender.sendMessage("\u00a77  Current Players: \u00a7e" + ffaInstance.getPlayerCount());
            sender.sendMessage("\u00a77  Total Kills: \u00a7e" + ffaInstance.getTotalKills());
            sender.sendMessage("\u00a77  Peak Players: \u00a7e" + ffaInstance.getPeakPlayerCount());
        }
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
    }

    private void handleSetKit(CommandSender sender, String[] args, ArenaManager arenaManager) {
        if (args.length < 3) {
            sender.sendMessage("\u00a7cUsage: /ffaadmin setkit <arena> <kit>");
            return;
        }
        String arenaName = args[1].toLowerCase();
        String kitName = args[2].toLowerCase();
        DuelArena arena = arenaManager.getFFAArena(arenaName);
        if (arena == null) {
            sender.sendMessage("\u00a7cFFA arena '" + arenaName + "' not found!");
            return;
        }
        if (this.plugin.getKitManager() == null || this.plugin.getKitManager().getAdminKit(kitName) == null) {
            sender.sendMessage("\u00a7cKit '" + kitName + "' does not exist!");
            return;
        }
        arenaManager.setFFAArenaKit(arenaName, kitName);
        sender.sendMessage("\u00a7aFFA arena '\u00a7e" + arenaName + "\u00a7a' linked to kit '\u00a7e" + kitName + "\u00a7a'!");
        if (this.plugin.getFFAManager() != null) {
            this.plugin.getFFAManager().reloadArenas();
        }
    }

    private void handleReload(CommandSender sender) {
        if (this.plugin.getFFAManager() != null) {
            this.plugin.getFFAManager().reload();
            sender.sendMessage("\u00a7aFFA system reloaded!");
        } else {
            sender.sendMessage("\u00a7cFFA system is not available!");
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac FFA ADMIN \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lArena Setup:");
        sender.sendMessage("\u00a7e/ffaadmin create <name> <kit> \u00a77- Create FFA arena");
        sender.sendMessage("\u00a7e/ffaadmin delete <name> \u00a77- Delete FFA arena");
        sender.sendMessage("\u00a7e/ffaadmin setkit <name> <kit> \u00a77- Link kit");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lSpawn Points:");
        sender.sendMessage("\u00a7e/ffaadmin setspawn <name> \u00a77- Add spawn point");
        sender.sendMessage("\u00a7e/ffaadmin clearspawns <name> \u00a77- Clear spawns");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lSchematic (for regeneration):");
        sender.sendMessage("\u00a7e/ffaadmin setcorner1 <name> \u00a77- Set corner 1");
        sender.sendMessage("\u00a7e/ffaadmin setcorner2 <name> \u00a77- Set corner 2");
        sender.sendMessage("\u00a7e/ffaadmin save <name> \u00a77- Save schematic");
        sender.sendMessage("\u00a7e/ffaadmin regenerate <name> \u00a77- Regenerate arena");
        sender.sendMessage("");
        sender.sendMessage("\u00a7e\u00a7lManagement:");
        sender.sendMessage("\u00a7e/ffaadmin enable <name> \u00a77- Enable arena");
        sender.sendMessage("\u00a7e/ffaadmin disable <name> \u00a77- Disable arena");
        sender.sendMessage("\u00a7e/ffaadmin list \u00a77- List all FFA arenas");
        sender.sendMessage("\u00a7e/ffaadmin info <name> \u00a77- Show arena info");
        sender.sendMessage("\u00a7e/ffaadmin reload \u00a77- Reload FFA system");
        sender.sendMessage("");
        sender.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String sub;
        if (!sender.hasPermission("ultimateduels.admin.ffa")) {
            return new ArrayList<String>();
        }
        if (args.length == 1) {
            return this.filterStartsWith(Arrays.asList("create", "delete", "setspawn", "clearspawns", "enable", "disable", "list", "info", "setkit", "setcorner1", "setcorner2", "save", "regenerate", "reload"), args[0]);
        }
        if (args.length == 2) {
            switch (sub = args[0].toLowerCase()) {
                case "delete": 
                case "setspawn": 
                case "clearspawns": 
                case "enable": 
                case "disable": 
                case "info": 
                case "setkit": 
                case "setcorner1": 
                case "setcorner2": 
                case "save": 
                case "regenerate": {
                    if (this.plugin.getArenaManager() == null) break;
                    return this.filterStartsWith(new ArrayList<String>(this.plugin.getArenaManager().getFFAArenaNames()), args[1]);
                }
                case "create": {
                    return List.of("<name>");
                }
            }
        }
        if (args.length == 3 && ((sub = args[0].toLowerCase()).equals("create") || sub.equals("setkit")) && this.plugin.getKitManager() != null) {
            return this.filterStartsWith(new ArrayList<String>(this.plugin.getKitManager().getAdminKitNames()), args[2]);
        }
        return new ArrayList<String>();
    }

    private List<String> filterStartsWith(List<String> list, String prefix) {
        return list.stream().filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase())).collect(Collectors.toList());
    }
}

