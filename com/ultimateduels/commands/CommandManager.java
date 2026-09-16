/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.admin.AdminCommand;
import com.ultimateduels.commands.admin.ForceEndCommand;
import com.ultimateduels.commands.admin.ReloadCommand;
import com.ultimateduels.commands.admin.SetLobbyCommand;
import com.ultimateduels.commands.arena.ArenaCommand;
import com.ultimateduels.commands.arena.ArenaSaveCommand;
import com.ultimateduels.commands.arena.ArenaSetSpawnCommand;
import com.ultimateduels.commands.duel.AcceptCommand;
import com.ultimateduels.commands.duel.DenyCommand;
import com.ultimateduels.commands.duel.DuelCommand;
import com.ultimateduels.commands.kit.KitCommand;
import com.ultimateduels.commands.kit.KitGiveCommand;
import com.ultimateduels.commands.party.PartyAcceptCommand;
import com.ultimateduels.commands.party.PartyCommand;
import com.ultimateduels.commands.party.PartyLeaveCommand;
import com.ultimateduels.commands.stats.LeaderboardCommand;
import com.ultimateduels.commands.stats.StatsCommand;
import com.ultimateduels.world.WorldRestrictionManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandManager {
    private final UltimateDuels plugin;
    private final Map<String, CommandExecutor> commands;

    public CommandManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<String, CommandExecutor>();
        this.registerCommands();
    }

    private void registerCommands() {
        this.registerCommand("duel", new DuelCommand(this.plugin));
        this.registerCommand("accept", new AcceptCommand(this.plugin));
        this.registerCommand("deny", new DenyCommand(this.plugin));
        ArenaCommand arenaCommand = new ArenaCommand(this.plugin);
        this.registerCommand("arena", arenaCommand);
        this.registerCommand("arenas", arenaCommand);
        this.registerCommand("setspawn", new ArenaSetSpawnCommand(this.plugin));
        this.registerCommand("arenasave", new ArenaSaveCommand(this.plugin));
        KitCommand kitCommand = new KitCommand(this.plugin);
        this.registerCommand("kit", kitCommand);
        this.registerCommand("kits", kitCommand);
        this.registerCommand("kitgive", new KitGiveCommand(this.plugin));
        PartyCommand partyCommand = new PartyCommand(this.plugin);
        this.registerCommand("party", partyCommand);
        this.registerCommand("p", partyCommand);
        this.registerCommand("partyaccept", new PartyAcceptCommand(this.plugin));
        this.registerCommand("partyleave", new PartyLeaveCommand(this.plugin));
        AdminCommand adminCommand = new AdminCommand(this.plugin);
        this.registerCommand("duels", adminCommand);
        this.registerCommand("ultimateduels", adminCommand);
        this.registerCommand("ud", adminCommand);
        this.registerCommand("setlobby", new SetLobbyCommand(this.plugin));
        this.registerCommand("duelsreload", new ReloadCommand(this.plugin));
        this.registerCommand("forceend", new ForceEndCommand(this.plugin));
        this.registerCommand("stats", new StatsCommand(this.plugin));
        this.registerCommand("leaderboard", new LeaderboardCommand(this.plugin));
        this.registerCommand("lb", new LeaderboardCommand(this.plugin));
        this.plugin.getLogger().info("Registered " + this.commands.size() + " commands!");
    }

    private void registerCommand(String name, CommandExecutor command) {
        PluginCommand pluginCommand = this.plugin.getCommand(name);
        if (pluginCommand == null) {
            this.plugin.getLogger().warning("Command /" + name + " is not defined in plugin.yml!");
            return;
        }
        pluginCommand.setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player)) {
                return command.onCommand(sender, cmd, label, args);
            }
            Player player = (Player)sender;
            WorldRestrictionManager worldRestriction = this.plugin.getWorldRestrictionManager();
            if (worldRestriction != null && !worldRestriction.canUseCommands(player)) {
                worldRestriction.sendCommandBlockedMessage(player);
                return true;
            }
            return command.onCommand(sender, cmd, label, args);
        });
        if (command instanceof TabCompleter) {
            pluginCommand.setTabCompleter((TabCompleter)command);
        }
        this.commands.put(name.toLowerCase(), command);
    }

    public CommandExecutor getCommand(String name) {
        return this.commands.get(name.toLowerCase());
    }

    public boolean isCommandRegistered(String name) {
        return this.commands.containsKey(name.toLowerCase());
    }

    public Map<String, CommandExecutor> getCommands() {
        return new HashMap<String, CommandExecutor>(this.commands);
    }

    public void reload() {
    }
}

