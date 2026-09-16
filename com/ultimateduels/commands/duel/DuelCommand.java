/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.commands.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelRequest;
import com.ultimateduels.duel.model.DuelRequestResult;
import com.ultimateduels.duel.model.WinCondition;
import com.ultimateduels.gui.duel.DuelRequestGUI;
import com.ultimateduels.gui.duel.DuelRoundSettingsGUI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DuelCommand
extends BaseCommand {
    public DuelCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.duel");
    }

    @Override
    protected void execute(Player player, String[] args) {
        DuelRequest existingRequest;
        if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
            this.plugin.getGUIManager().openDuelPlayerGUI(player);
            return;
        }
        if (args.length == 0) {
            this.sendUsage((CommandSender)player, "/duel <player> [kit] [rounds] [bestof|playall]");
            this.sendMessage(player, "&7Send a duel request to another player.");
            this.sendMessage(player, "");
            this.sendMessage(player, "&e/duel <player> &7- Open duel configuration GUI");
            this.sendMessage(player, "&e/duel <player> <kit> &7- Choose rounds for that kit");
            this.sendMessage(player, "&e/duel <player> <kit> <rounds> &7- Quick duel with rounds");
            return;
        }
        if (!this.checkPlayerState(player)) {
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            this.sendMessage(player, "&cYou must leave the queue before sending a duel request!");
            this.sendMessage(player, "&7Use &e/leavequeue &7to leave the queue.");
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            this.sendMessage(player, "&cYou must leave FFA before sending a duel request!");
            this.sendMessage(player, "&7Use &e/ffa leave &7to leave FFA.");
            return;
        }
        String targetName = args[0];
        Player target = Bukkit.getPlayer((String)targetName);
        if (target == null) {
            this.sendMessage(player, "&cPlayer &e" + targetName + " &cis not online!");
            return;
        }
        if (target.equals((Object)player)) {
            this.sendMessage(player, "&cYou cannot duel yourself!");
            return;
        }
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager != null && duelManager.isInMatch(target.getUniqueId())) {
            this.sendMessage(player, "&c" + target.getName() + " is currently in a duel!");
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(target.getUniqueId())) {
            this.sendMessage(player, "&c" + target.getName() + " is currently in an FFA arena!");
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(target)) {
            this.sendMessage(player, "&c" + target.getName() + " is currently in a queue!");
            return;
        }
        if (duelManager != null && duelManager.hasPendingRequest(target.getUniqueId()) && (existingRequest = duelManager.getPendingRequest(target.getUniqueId())) != null && existingRequest.getSenderUUID().equals(player.getUniqueId())) {
            this.sendMessage(player, "&cYou already have a pending request to " + target.getName() + "!");
            return;
        }
        if (duelManager != null && duelManager.hasPendingRequest(player.getUniqueId()) && (existingRequest = duelManager.getPendingRequest(player.getUniqueId())) != null && existingRequest.getSenderUUID().equals(target.getUniqueId())) {
            this.sendMessage(player, "&e" + target.getName() + " has already sent you a request!");
            this.sendMessage(player, "&7Use &a/duel accept &7to accept their request.");
            return;
        }
        if (args.length == 1) {
            this.openDuelRequestGUI(player, target);
        } else if (args.length == 2) {
            kitName = args[1];
            if (!this.plugin.getKitManager().adminKitExists(kitName)) {
                this.sendMessage(player, "&cKit &e" + kitName + " &cdoes not exist!");
                this.sendMessage(player, "&7Available kits: &e" + String.join((CharSequence)", ", this.plugin.getKitManager().getAdminKitNames()));
                return;
            }
            this.openRoundSettingsGUI(player, target, kitName);
        } else if (args.length >= 3) {
            int rounds;
            kitName = args[1];
            if (!this.plugin.getKitManager().adminKitExists(kitName)) {
                this.sendMessage(player, "&cKit &e" + kitName + " &cdoes not exist!");
                this.sendMessage(player, "&7Available kits: &e" + String.join((CharSequence)", ", this.plugin.getKitManager().getAdminKitNames()));
                return;
            }
            try {
                rounds = Integer.parseInt(args[2]);
                if (rounds < 1 || rounds > 20) {
                    this.sendMessage(player, "&cRounds must be between 1 and 20!");
                    return;
                }
            }
            catch (NumberFormatException e) {
                this.sendMessage(player, "&cInvalid round number: &e" + args[2]);
                return;
            }
            WinCondition winCondition = WinCondition.BEST_OF;
            if (args.length >= 4) {
                String modeArg = args[3].toLowerCase();
                if (modeArg.equals("playall") || modeArg.equals("play_all") || modeArg.equals("all")) {
                    winCondition = WinCondition.PLAY_ALL;
                } else if (modeArg.equals("bestof") || modeArg.equals("best_of") || modeArg.equals("best")) {
                    winCondition = WinCondition.BEST_OF;
                } else if (modeArg.equals("firsttowin") || modeArg.equals("first_to_win") || modeArg.equals("first")) {
                    winCondition = WinCondition.FIRST_TO_WIN;
                } else {
                    this.sendMessage(player, "&cInvalid mode! Use &ebestof&c, &eplayall&c, or &efirsttowin");
                    return;
                }
            }
            this.sendDuelRequest(player, target, kitName, rounds, winCondition);
        }
    }

    private void openDuelRequestGUI(Player sender, Player target) {
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> new DuelRequestGUI(this.plugin, sender, target).open(sender));
    }

    private void openRoundSettingsGUI(Player sender, Player target, String kitName) {
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> new DuelRoundSettingsGUI(this.plugin, sender, target, kitName).open(sender));
    }

    private void sendDuelRequest(Player sender, Player target, String kitName, int rounds, WinCondition winCondition) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            this.sendMessage(sender, "&cDuel system is not available!");
            return;
        }
        DuelRequestResult result = duelManager.sendRequest(sender, target, kitName, null, rounds, winCondition);
        if (!result.success()) {
            this.sendMessage(sender, result.message());
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.plugin.getServer().getOnlinePlayers().stream().filter(p -> !p.equals((Object)sender)).filter(p -> this.plugin.getDuelManager() == null || !this.plugin.getDuelManager().isInMatch(p.getUniqueId())).filter(p -> this.plugin.getFFAManager() == null || !this.plugin.getFFAManager().isInFFA(p.getUniqueId())).map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return this.plugin.getKitManager().getAdminKitNames().stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3) {
            ArrayList<String> rounds = new ArrayList<String>();
            rounds.add("1");
            rounds.add("3");
            rounds.add("5");
            rounds.add("7");
            rounds.add("9");
            return rounds.stream().filter(r -> r.startsWith(args[2])).collect(Collectors.toList());
        }
        if (args.length == 4) {
            ArrayList<String> modes = new ArrayList<String>();
            modes.add("bestof");
            modes.add("playall");
            return modes.stream().filter(m -> m.toLowerCase().startsWith(args[3].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

