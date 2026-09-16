/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands.spectate;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelMatch;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectateCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public SpectateCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.spectate")) {
            player.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("\u00a7cUsage: /spectate <player> or /spectate leave");
            return true;
        }
        if (args[0].equalsIgnoreCase("leave")) {
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
                this.plugin.getDuelManager().removeSpectator(player);
            } else {
                player.sendMessage("\u00a7cYou are not spectating any match!");
            }
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            player.sendMessage("\u00a7cPlayer not found: " + args[0]);
            return true;
        }
        if (target.equals((Object)player)) {
            player.sendMessage("\u00a7cYou cannot spectate yourself!");
            return true;
        }
        if (this.plugin.getDuelManager() == null || !this.plugin.getDuelManager().isInMatch(target.getUniqueId())) {
            player.sendMessage("\u00a7c" + target.getName() + " is not in a match!");
            return true;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            player.sendMessage("\u00a7cYou cannot spectate while in queue!");
            return true;
        }
        if (this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
            player.sendMessage("\u00a7cYou cannot spectate while in a match!");
            return true;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatch(target);
        if (match == null) {
            player.sendMessage("\u00a7cCould not find the match. It may have ended.");
            return true;
        }
        Location spectatorSpawn = match.getArena().getSpectatorSpawn();
        if (spectatorSpawn == null) {
            spectatorSpawn = match.getArena().getCenter();
        }
        if (spectatorSpawn == null) {
            spectatorSpawn = match.getArena().getSpawnPoint1();
        }
        if (spectatorSpawn == null || spectatorSpawn.getWorld() == null) {
            player.sendMessage("\u00a7cThis arena has no valid spectator location!");
            player.sendMessage("\u00a77Ask an admin to set a spectator spawn for this arena.");
            return true;
        }
        boolean success = this.plugin.getDuelManager().addSpectator(player, match.getMatchId());
        if (success) {
            player.sendMessage("\u00a7aYou are now spectating \u00a7e" + target.getName() + "\u00a7a's match.");
        } else {
            player.sendMessage("\u00a7cFailed to start spectating. The match may have ended or arena has no spectator spawn.");
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && this.plugin.getDuelManager() != null) {
            ArrayList<String> completions = new ArrayList<String>();
            if ("leave".startsWith(args[0].toLowerCase())) {
                completions.add("leave");
            }
            completions.addAll(Bukkit.getOnlinePlayers().stream().filter(p -> this.plugin.getDuelManager().isInMatch(p.getUniqueId())).map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList()));
            return completions;
        }
        return new ArrayList<String>();
    }
}

