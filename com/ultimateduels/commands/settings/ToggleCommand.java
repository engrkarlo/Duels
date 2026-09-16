/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands.settings;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.settings.SettingsManager;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private final SettingsManager settingsManager;
    private static final Map<String, List<String>> SETTING_ALIASES = new LinkedHashMap<String, List<String>>();

    public ToggleCommand(UltimateDuels plugin) {
        this.plugin = plugin;
        this.settingsManager = plugin.getSettingsManager();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.toggle")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }
        if (this.settingsManager == null) {
            MessageUtils.sendMessage(player, "&cSettings system is not available!");
            return true;
        }
        if (args.length == 0) {
            this.sendHelpMessage(player);
            return true;
        }
        String settingInput = args[0].toLowerCase();
        String settingName = this.resolveSettingName(settingInput);
        if (settingName == null) {
            MessageUtils.sendMessage(player, "&cUnknown setting: &f" + args[0]);
            MessageUtils.sendMessage(player, "&7Use &e/toggle &7to see available settings.");
            return true;
        }
        boolean newState = this.toggleSetting(player, settingName);
        String displayName = this.formatSettingName(settingName);
        String status = newState ? "&aenabled" : "&cdisabled";
        MessageUtils.sendMessage(player, "&7" + displayName + " has been " + status + "&7!");
        this.playToggleSound(player, newState);
        return true;
    }

    @Nullable
    private String resolveSettingName(String input) {
        if (SETTING_ALIASES.containsKey(input)) {
            return input;
        }
        for (Map.Entry<String, List<String>> entry : SETTING_ALIASES.entrySet()) {
            if (!entry.getValue().contains(input)) continue;
            return entry.getKey();
        }
        return null;
    }

    private boolean toggleSetting(Player player, String settingName) {
        return switch (settingName) {
            case "scoreboard" -> {
                boolean newState = !this.settingsManager.hasScoreboardEnabled(player);
                this.settingsManager.setScoreboardEnabled(player, newState);
                if (this.plugin.getScoreboardManager() != null) {
                    if (newState) {
                        this.plugin.getScoreboardManager().createScoreboard(player);
                    } else {
                        this.plugin.getScoreboardManager().removeScoreboard(player);
                    }
                }
                yield newState;
            }
            case "duelrequests" -> {
                boolean newState = !this.settingsManager.canReceiveDuelRequests(player);
                this.settingsManager.setDuelRequestsEnabled(player, newState);
                yield newState;
            }
            case "partyinvites" -> {
                boolean newState = !this.settingsManager.canReceivePartyInvites(player);
                this.settingsManager.setPartyInvitesEnabled(player, newState);
                yield newState;
            }
            case "deathmessages" -> {
                boolean newState = !this.settingsManager.showDeathMessages(player);
                this.settingsManager.setDeathMessagesEnabled(player, newState);
                yield newState;
            }
            case "spectatornotify" -> {
                boolean newState = !this.settingsManager.showSpectatorJoinMessages(player);
                this.settingsManager.setSpectatorNotifyEnabled(player, newState);
                yield newState;
            }
            case "sounds" -> {
                boolean newState = !this.settingsManager.hasSoundsEnabled(player);
                this.settingsManager.setSoundsEnabled(player, newState);
                yield newState;
            }
            case "autorequeue" -> {
                boolean newState = !this.settingsManager.hasAutoRequeue(player);
                this.settingsManager.setAutoRequeue(player, newState);
                yield newState;
            }
            case "ping" -> {
                boolean newState = !this.settingsManager.showPing(player);
                this.settingsManager.setShowPing(player, newState);
                yield newState;
            }
            case "spectators" -> {
                boolean newState = !this.settingsManager.allowsSpectators(player);
                this.settingsManager.setAllowSpectators(player, newState);
                yield newState;
            }
            default -> false;
        };
    }

    private String formatSettingName(String settingName) {
        return switch (settingName) {
            case "scoreboard" -> "Scoreboard";
            case "duelrequests" -> "Duel Requests";
            case "partyinvites" -> "Party Invites";
            case "deathmessages" -> "Death Messages";
            case "spectatornotify" -> "Spectator Notifications";
            case "sounds" -> "Sounds";
            case "autorequeue" -> "Auto Re-Queue";
            case "ping" -> "Show Ping";
            case "spectators" -> "Allow Spectators";
            default -> settingName;
        };
    }

    private void playToggleSound(Player player, boolean enabled) {
        if (this.settingsManager.hasSoundsEnabled(player)) {
            player.playSound(player.getLocation(), enabled ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, enabled ? 1.5f : 0.5f);
        }
    }

    private void sendHelpMessage(Player player) {
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&6&l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac TOGGLE SETTINGS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&7Usage: &e/toggle <setting>");
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&bAvailable Settings:");
        MessageUtils.sendMessage(player, "");
        this.sendSettingInfo(player, "scoreboard", "Scoreboard", this.settingsManager.hasScoreboardEnabled(player));
        this.sendSettingInfo(player, "duelrequests", "Duel Requests", this.settingsManager.canReceiveDuelRequests(player));
        this.sendSettingInfo(player, "partyinvites", "Party Invites", this.settingsManager.canReceivePartyInvites(player));
        this.sendSettingInfo(player, "deathmessages", "Death Messages", this.settingsManager.showDeathMessages(player));
        this.sendSettingInfo(player, "spectatornotify", "Spectator Notify", this.settingsManager.showSpectatorJoinMessages(player));
        this.sendSettingInfo(player, "sounds", "Sounds", this.settingsManager.hasSoundsEnabled(player));
        this.sendSettingInfo(player, "autorequeue", "Auto Re-Queue", this.settingsManager.hasAutoRequeue(player));
        this.sendSettingInfo(player, "ping", "Show Ping", this.settingsManager.showPing(player));
        this.sendSettingInfo(player, "spectators", "Allow Spectators", this.settingsManager.allowsSpectators(player));
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&7Or use &e/settings &7to open the GUI.");
        MessageUtils.sendMessage(player, "&6&l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        MessageUtils.sendMessage(player, "");
    }

    private void sendSettingInfo(Player player, String settingKey, String displayName, boolean enabled) {
        String status = enabled ? "&a\u2714" : "&c\u2718";
        String statusText = enabled ? "&aON" : "&cOFF";
        MessageUtils.sendMessage(player, "  " + status + " &e" + settingKey + " &8- &f" + displayName + " &8[" + statusText + "&8]");
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            ArrayList<String> completions = new ArrayList<String>();
            for (Map.Entry<String, List<String>> entry : SETTING_ALIASES.entrySet()) {
                completions.add(entry.getKey());
                completions.addAll((Collection)entry.getValue());
            }
            return completions.stream().filter(s -> s.startsWith(input)).sorted().collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }

    static {
        SETTING_ALIASES.put("scoreboard", Arrays.asList("sb", "board"));
        SETTING_ALIASES.put("duelrequests", Arrays.asList("duels", "duel", "requests", "dr"));
        SETTING_ALIASES.put("partyinvites", Arrays.asList("party", "invites", "pi"));
        SETTING_ALIASES.put("deathmessages", Arrays.asList("death", "dm", "deathmsg"));
        SETTING_ALIASES.put("spectatornotify", Arrays.asList("specnotify", "sn", "spectatornotifications"));
        SETTING_ALIASES.put("sounds", Arrays.asList("sound", "sfx", "audio"));
        SETTING_ALIASES.put("autorequeue", Arrays.asList("autoqueue", "aq", "requeue", "ar"));
        SETTING_ALIASES.put("ping", Arrays.asList("showping", "latency"));
        SETTING_ALIASES.put("spectators", Arrays.asList("allowspectators", "specs", "allowspecs", "as"));
    }
}

