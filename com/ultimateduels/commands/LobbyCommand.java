/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.GUIManager;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LobbyCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private final GUIManager guiManager;
    private static final List<String> SUBCOMMANDS = Arrays.asList("kits", "settings", "spectate", "leaderboard", "stats", "party", "ffa", "queue");

    public LobbyCommand(UltimateDuels plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGUIManager();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String subCommand;
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.lobby")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            this.teleportToLobby(player);
            return true;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "kits": 
            case "kit": 
            case "kiteditor": {
                this.openKitEditor(player);
                break;
            }
            case "settings": 
            case "setting": 
            case "options": {
                this.openSettings(player);
                break;
            }
            case "spectate": 
            case "spec": 
            case "watch": {
                this.openSpectateMenu(player);
                break;
            }
            case "leaderboard": 
            case "lb": 
            case "top": {
                this.openLeaderboard(player);
                break;
            }
            case "stats": 
            case "statistics": {
                this.openStats(player);
                break;
            }
            case "party": 
            case "parties": {
                this.openPartyMenu(player);
                break;
            }
            case "ffa": 
            case "freef orall": {
                this.openFFAMenu(player);
                break;
            }
            case "queue": 
            case "ranked": {
                this.openQueueMenu(player);
                break;
            }
            default: {
                MessageUtils.sendMessage(player, "&cUnknown subcommand: &e" + subCommand);
                MessageUtils.sendMessage(player, "&7Available: &f" + String.join((CharSequence)", ", SUBCOMMANDS));
            }
        }
        return true;
    }

    private void teleportToLobby(Player player) {
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
            if (!this.plugin.getDuelManager().isActiveParticipant(player.getUniqueId())) {
                this.plugin.getDuelManager().removeDeadSpectatorFromMatch(player.getUniqueId());
                this.plugin.getLobbyManager().sendToLobby(player, true, true);
                MessageUtils.sendMessage(player, "&aYou have been sent to the lobby.");
                return;
            }
            MessageUtils.sendMessage(player, "&cYou cannot go to the lobby while in an active duel!");
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            this.plugin.getQueueManager().leaveQueue(player);
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            this.plugin.getFFAManager().leave(player);
        }
        if (this.plugin.getLobbyManager() != null) {
            this.plugin.getLobbyManager().sendToLobby(player, true, true);
            MessageUtils.sendMessage(player, "&aYou have been teleported to the lobby.");
        } else {
            MessageUtils.sendMessage(player, "&cLobby system is not available!");
        }
    }

    private void openKitEditor(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cKit editor is not available!");
            return;
        }
        if (this.isPlayerBusy(player)) {
            return;
        }
        this.guiManager.openKitEditorGUI(player);
    }

    private void openSettings(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cSettings menu is not available!");
            return;
        }
        this.guiManager.openSettingsGUI(player);
    }

    private void openSpectateMenu(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cSpectate menu is not available!");
            return;
        }
        if (this.isPlayerBusy(player)) {
            return;
        }
        this.guiManager.openSpectateGUI(player);
    }

    private void openLeaderboard(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cLeaderboard is not available!");
            return;
        }
        this.guiManager.openLeaderboardGUI(player);
    }

    private void openStats(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cStats menu is not available!");
            return;
        }
        this.guiManager.openStatsGUI(player);
    }

    private void openPartyMenu(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cParty menu is not available!");
            return;
        }
        if (this.isPlayerBusy(player)) {
            return;
        }
        this.guiManager.openPartyGUI(player);
    }

    private void openFFAMenu(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cFFA menu is not available!");
            return;
        }
        if (this.isPlayerBusy(player)) {
            return;
        }
        this.guiManager.openFFAGUI(player);
    }

    private void openQueueMenu(Player player) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cQueue menu is not available!");
            return;
        }
        if (this.isPlayerBusy(player)) {
            return;
        }
        this.guiManager.openQueueGUI(player);
    }

    private boolean isPlayerBusy(Player player) {
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot use this while in a duel!");
            return true;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot use this while in FFA!");
            return true;
        }
        return false;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

