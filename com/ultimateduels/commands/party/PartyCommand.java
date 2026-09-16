/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.party.model.PartyResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand
extends BaseCommand {
    private static final List<String> SUBCOMMANDS = Arrays.asList("create", "disband", "invite", "accept", "deny", "kick", "leave", "promote", "info", "list", "chat", "open", "close", "warp", "pvp", "split", "ffa");

    public PartyCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.party");
    }

    @Override
    protected void execute(Player player, String[] args) {
        String subCommand;
        if (this.plugin.getPartyManager() == null) {
            this.sendMessage(player, "&cParty system is not available!");
            return;
        }
        if (args.length == 0) {
            if (this.plugin.getPartyManager().isInParty(player.getUniqueId())) {
                this.showPartyInfo(player);
            } else {
                this.sendHelp(player);
            }
            return;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "create": {
                this.handleCreate(player);
                break;
            }
            case "disband": 
            case "delete": {
                this.handleDisband(player);
                break;
            }
            case "invite": 
            case "inv": 
            case "add": {
                this.handleInvite(player, args);
                break;
            }
            case "accept": 
            case "join": {
                this.handleAccept(player);
                break;
            }
            case "deny": 
            case "decline": {
                this.handleDeny(player);
                break;
            }
            case "kick": 
            case "remove": {
                this.handleKick(player, args);
                break;
            }
            case "leave": 
            case "quit": {
                this.handleLeave(player);
                break;
            }
            case "promote": 
            case "leader": 
            case "transfer": {
                this.handlePromote(player, args);
                break;
            }
            case "info": 
            case "status": {
                this.showPartyInfo(player);
                break;
            }
            case "list": 
            case "members": {
                this.showPartyMembers(player);
                break;
            }
            case "chat": 
            case "c": {
                this.handleChat(player, args);
                break;
            }
            case "open": {
                this.handleOpen(player);
                break;
            }
            case "close": {
                this.handleClose(player);
                break;
            }
            case "warp": {
                this.handleWarp(player);
                break;
            }
            case "pvp": 
            case "duel": 
            case "fight": {
                this.handlePvP(player, args);
                break;
            }
            case "split": {
                this.handleSplit(player, args);
                break;
            }
            case "ffa": {
                this.handleFFA(player, args);
                break;
            }
            default: {
                Player target = Bukkit.getPlayer((String)subCommand);
                if (target != null) {
                    this.handleInvite(player, new String[]{"invite", subCommand});
                    break;
                }
                this.sendHelp(player);
            }
        }
    }

    private void handleCreate(Player player) {
        if (!player.hasPermission("ultimateduels.party.create")) {
            this.sendMessage(player, "&cYou don't have permission to create a party!");
            this.sendMessage(player, "&7You can still join parties when invited.");
            return;
        }
        if (this.plugin.getPartyManager().isInParty(player.getUniqueId())) {
            this.sendMessage(player, "&cYou are already in a party!");
            this.sendMessage(player, "&7Use &e/party leave &7to leave first.");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().createParty(player);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handleDisband(Player player) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can disband the party!");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().disbandParty(player);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/party invite <player>");
            return;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayer((String)targetName);
        if (target == null) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.getName().equalsIgnoreCase(targetName)) continue;
                target = online;
                break;
            }
        }
        if (target == null) {
            this.sendMessage(player, "&cPlayer not found or not online: &e" + targetName);
            this.sendMessage(player, "&7Note: Player names are case-sensitive.");
            return;
        }
        if (target.equals((Object)player)) {
            this.sendMessage(player, "&cYou cannot invite yourself!");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().invite(player, target);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handleAccept(Player player) {
        if (!this.plugin.getPartyManager().hasPendingInvite(player.getUniqueId())) {
            this.sendMessage(player, "&cYou don't have any pending party invites!");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().acceptInvite(player);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handleDeny(Player player) {
        if (!this.plugin.getPartyManager().hasPendingInvite(player.getUniqueId())) {
            this.sendMessage(player, "&cYou don't have any pending party invites!");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().denyInvite(player);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handleKick(Player player, String[] args) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can kick members!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/party kick <player>");
            return;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayer((String)targetName);
        if (target == null) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.getName().equalsIgnoreCase(targetName)) continue;
                target = online;
                break;
            }
        }
        if (target == null) {
            this.sendMessage(player, "&cPlayer not found or not online: &e" + targetName);
            return;
        }
        PartyResult result = this.plugin.getPartyManager().kick(player, target);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handleLeave(Player player) {
        if (!this.plugin.getPartyManager().isInParty(player.getUniqueId())) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().leaveParty(player);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void handlePromote(Player player, String[] args) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can promote members!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/party promote <player>");
            return;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayer((String)targetName);
        if (target == null) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.getName().equalsIgnoreCase(targetName)) continue;
                target = online;
                break;
            }
        }
        if (target == null) {
            this.sendMessage(player, "&cPlayer not found or not online: &e" + targetName);
            return;
        }
        PartyResult result = this.plugin.getPartyManager().transferLeader(player, target);
        if (!result.success()) {
            this.sendMessage(player, result.message());
        }
    }

    private void showPartyInfo(Player player) {
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            this.sendMessage(player, "&cYou are not in a party!");
            this.sendMessage(player, "&7Use &e/party create &7to create one.");
            return;
        }
        this.plugin.getPartyManager().sendPartyInfo(player);
    }

    private void showPartyMembers(Player player) {
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        this.plugin.getPartyManager().listMembers(player);
    }

    private void handleChat(Player player, String[] args) {
        if (!this.plugin.getPartyManager().isInParty(player.getUniqueId())) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/party chat <message>");
            this.sendMessage(player, "&7You can also use &e@p <message> &7in chat.");
            return;
        }
        String message = this.joinArgs(args, 1);
        this.plugin.getPartyManager().sendPartyChat(player, message);
    }

    private void handleOpen(Player player) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can change party settings!");
            return;
        }
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        party.setPublic(true);
        party.broadcast("\u00a7aThe party is now open! Anyone can join.");
        this.sendMessage(player, "&aThe party is now open!");
    }

    private void handleClose(Player player) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can change party settings!");
            return;
        }
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        party.setPublic(false);
        party.broadcast("\u00a7cThe party is now closed. Invites required.");
        this.sendMessage(player, "&cThe party is now closed!");
    }

    private void handleWarp(Player player) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can warp members!");
            return;
        }
        Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            this.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        int warped = 0;
        for (UUID memberId : party.getMembers()) {
            Player member;
            if (memberId.equals(player.getUniqueId()) || (member = Bukkit.getPlayer((UUID)memberId)) == null || !member.isOnline()) continue;
            member.teleport(player.getLocation());
            this.sendMessage(member, "&aYou have been warped to the party leader!");
            ++warped;
        }
        this.sendMessage(player, "&aWarped &e" + warped + " &amembers to your location!");
    }

    private void handlePvP(Player player, String[] args) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can start party PvP!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/party pvp <kit>");
            return;
        }
        String kitName = args[1];
        PartyResult result = this.plugin.getPartyManager().queuePartyVsParty(player, kitName);
        this.sendMessage(player, result.message());
    }

    private void handleSplit(Player player, String[] args) {
        int teamSize;
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can start party split!");
            return;
        }
        if (args.length < 3) {
            this.sendUsage((CommandSender)player, "/party split <kit> <team-size>");
            this.sendMessage(player, "&7Example: &e/party split NoDebuff 2 &7for 2vRest");
            this.sendMessage(player, "&7Team size = how many players on team 1.");
            this.sendMessage(player, "&7Remaining players go to team 2 (can be uneven!)");
            return;
        }
        String kitName = args[1];
        try {
            teamSize = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            this.sendMessage(player, "&cInvalid team size! Must be a number.");
            return;
        }
        if (teamSize < 1) {
            this.sendMessage(player, "&cTeam size must be at least 1!");
            return;
        }
        PartyResult result = this.plugin.getPartyManager().queuePartySplit(player, kitName, teamSize);
        this.sendMessage(player, result.message());
    }

    private void handleFFA(Player player, String[] args) {
        if (!this.plugin.getPartyManager().isPartyLeader(player.getUniqueId())) {
            this.sendMessage(player, "&cOnly the party leader can join FFA!");
            return;
        }
        if (args.length < 2) {
            this.sendUsage((CommandSender)player, "/party ffa <kit>");
            return;
        }
        String kitName = args[1];
        PartyResult result = this.plugin.getPartyManager().joinPartyFFA(player, kitName);
        this.sendMessage(player, result.message());
    }

    private void sendHelp(Player player) {
        this.sendHeader((CommandSender)player, "Party Commands");
        if (player.hasPermission("ultimateduels.party.create")) {
            this.sendMessage(player, "&e/party create &7- Create a new party");
        }
        this.sendMessage(player, "&e/party invite <player> &7- Invite a player");
        this.sendMessage(player, "&e/party accept &7- Accept party invite");
        this.sendMessage(player, "&e/party deny &7- Deny party invite");
        this.sendMessage(player, "&e/party leave &7- Leave your party");
        this.sendMessage(player, "&e/party info &7- View party info");
        this.sendMessage(player, "&e/party chat <msg> &7- Send party message");
        this.sendMessage(player, "&e/party open &7- Make party public (anyone can join)");
        this.sendMessage(player, "&e/party close &7- Make party invite-only");
        this.sendMessage(player, "");
        this.sendMessage(player, "&c&lLeader Commands:");
        this.sendMessage(player, "&e/party disband &7- Disband the party");
        this.sendMessage(player, "&e/party kick <player> &7- Kick a member");
        this.sendMessage(player, "&e/party promote <player> &7- Transfer leadership");
        this.sendMessage(player, "&e/party warp &7- Teleport all members to you");
        this.sendMessage(player, "&e/party pvp <kit> &7- Queue for Party vs Party");
        this.sendMessage(player, "&e/party split <kit> <size> &7- Internal team fight");
        this.sendMessage(player, "&e/party ffa <kit> &7- All members join FFA");
        this.sendFooter((CommandSender)player);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        String sub;
        if (!(sender instanceof Player)) {
            return new ArrayList<String>();
        }
        Player player = (Player)sender;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>(SUBCOMMANDS);
            completions.addAll(Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals((Object)player)).map(Player::getName).collect(Collectors.toList()));
            return this.filterCompletions(completions, args[0]);
        }
        if (args.length == 2) {
            switch (sub = args[0].toLowerCase()) {
                case "invite": 
                case "inv": 
                case "add": {
                    return this.filterCompletions(Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals((Object)player)).filter(p -> this.plugin.getPartyManager() == null || !this.plugin.getPartyManager().isInParty(p.getUniqueId())).map(Player::getName).collect(Collectors.toList()), args[1]);
                }
                case "kick": 
                case "remove": 
                case "promote": 
                case "leader": 
                case "transfer": {
                    if (this.plugin.getPartyManager() == null) {
                        return new ArrayList<String>();
                    }
                    Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
                    if (party == null) {
                        return new ArrayList<String>();
                    }
                    return this.filterCompletions(party.getMembers().stream().filter(id -> !id.equals(player.getUniqueId())).map(id -> {
                        Player p = Bukkit.getPlayer((UUID)id);
                        return p != null ? p.getName() : null;
                    }).filter(name -> name != null).collect(Collectors.toList()), args[1]);
                }
                case "pvp": 
                case "duel": 
                case "fight": 
                case "split": 
                case "ffa": {
                    return this.filterCompletions(this.getKitNames(), args[1]);
                }
            }
        }
        if (args.length == 3 && (sub = args[0].toLowerCase()).equals("split")) {
            return this.filterCompletions(List.of("1", "2", "3", "4", "5"), args[2]);
        }
        return new ArrayList<String>();
    }
}

