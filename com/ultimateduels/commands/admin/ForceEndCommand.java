/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.admin;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceEndCommand
extends BaseCommand {
    public ForceEndCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.forceend", false);
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
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            this.sendMessage(sender, "&cDuel system is not available!");
            return;
        }
        if (args.length == 0) {
            this.showActiveDuels(sender, duelManager);
            return;
        }
        String target = args[0].toLowerCase();
        String reason = args.length > 1 ? this.joinArgs(args, 1) : "Ended by administrator";
        switch (target) {
            case "all": {
                this.forceEndAll(sender, duelManager, reason);
                break;
            }
            case "list": {
                this.showActiveDuels(sender, duelManager);
                break;
            }
            default: {
                this.forceEndPlayer(sender, duelManager, target, reason);
            }
        }
    }

    private void showActiveDuels(CommandSender sender, DuelManager duelManager) {
        Collection<DuelMatch> activeMatches = duelManager.getActiveMatches();
        if (activeMatches.isEmpty()) {
            this.sendMessage(sender, "&7No active duels.");
            return;
        }
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&6&lActive Duels: &f" + activeMatches.size());
        this.sendMessage(sender, "&7&m                              ");
        int index = 1;
        for (DuelMatch match : activeMatches) {
            String participants = this.getParticipantNames(match);
            String kit = match.getKitName();
            String arena = match.getArena().getName();
            String duration = this.formatDuration(match.getMatchDuration());
            this.sendMessage(sender, "&e" + index + ". &f" + participants);
            this.sendMessage(sender, "   &7Kit: &f" + kit + " &7| Arena: &f" + arena + " &7| Time: &f" + duration);
            if (this.isPartyDuel(match)) {
                this.sendMessage(sender, "   &7Type: &dParty Duel &8(" + match.getTeam1().size() + "v" + match.getTeam2().size() + ")");
            }
            ++index;
        }
        this.sendMessage(sender, "&7&m                              ");
        this.sendMessage(sender, "&7Usage: &e/forceend <player|all> [reason]");
        this.sendMessage(sender, "");
    }

    private void forceEndPlayer(CommandSender sender, DuelManager duelManager, String playerName, String reason) {
        Player target = Bukkit.getPlayer((String)playerName);
        if (target == null) {
            this.sendMessage(sender, "&cPlayer not found or not online: &e" + playerName);
            return;
        }
        DuelMatch match = duelManager.getMatch(target);
        if (match == null) {
            this.sendMessage(sender, "&c" + target.getName() + " is not in a duel!");
            return;
        }
        String participants = this.getParticipantNames(match);
        duelManager.cancelMatch(match, reason);
        this.sendMessage(sender, "&cForce ended duel: &f" + participants);
        this.sendMessage(sender, "&7Reason: &f" + reason);
        this.plugin.getLogger().info("Duel force ended by " + sender.getName() + ": " + participants + " - Reason: " + reason);
    }

    private void forceEndAll(CommandSender sender, DuelManager duelManager, String reason) {
        Collection<DuelMatch> activeMatches = duelManager.getActiveMatches();
        if (activeMatches.isEmpty()) {
            this.sendMessage(sender, "&7No active duels to end.");
            return;
        }
        int count = 0;
        for (DuelMatch match : new ArrayList<DuelMatch>(activeMatches)) {
            duelManager.cancelMatch(match, reason);
            ++count;
        }
        this.sendMessage(sender, "&cForce ended &e" + count + " &cduels.");
        this.sendMessage(sender, "&7Reason: &f" + reason);
        this.plugin.getLogger().info("All duels (" + count + ") force ended by " + sender.getName() + " - Reason: " + reason);
    }

    private String getParticipantNames(DuelMatch match) {
        if (this.isPartyDuel(match)) {
            return "Party (" + match.getTeam1().size() + ") vs Party (" + match.getTeam2().size() + ")";
        }
        ArrayList<String> names = new ArrayList<String>();
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player p = Bukkit.getPlayer((UUID)participant.getUuid());
            names.add(p != null ? p.getName() : participant.getPlayerName());
        }
        return String.join((CharSequence)" vs ", names);
    }

    private boolean isPartyDuel(DuelMatch match) {
        return match.getTeam1().size() > 1 || match.getTeam2().size() > 1;
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%d:%02d", minutes, seconds %= 60L);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            completions.add("all");
            completions.add("list");
            DuelManager duelManager = this.plugin.getDuelManager();
            if (duelManager != null) {
                for (DuelMatch match : duelManager.getActiveMatches()) {
                    for (DuelParticipant participant : match.getAllParticipants()) {
                        Player p = Bukkit.getPlayer((UUID)participant.getUuid());
                        if (p == null) continue;
                        completions.add(p.getName());
                    }
                }
            }
            return this.filterCompletions(completions, args[0]);
        }
        return new ArrayList<String>();
    }
}

