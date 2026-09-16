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
package com.ultimateduels.commands.settings;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.settings.SettingsGUI;
import com.ultimateduels.settings.SettingsManager;
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

public class DuelSettingsCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public DuelSettingsCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.settings")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }
        if (args.length > 0) {
            String subCommand;
            switch (subCommand = args[0].toLowerCase()) {
                case "chat": 
                case "list": 
                case "show": {
                    this.showSettingsInChat(player);
                    return true;
                }
                case "reset": {
                    if (this.plugin.getSettingsManager() != null) {
                        this.plugin.getSettingsManager().resetToDefaults(player);
                        MessageUtils.sendMessage(player, "&a\u2714 All settings have been reset to defaults!");
                    }
                    return true;
                }
                case "help": {
                    this.showHelp(player);
                    return true;
                }
            }
        }
        if (this.plugin.getSettingsManager() == null) {
            MessageUtils.sendMessage(player, "&cSettings system is not available!");
            return true;
        }
        SettingsGUI settingsGUI = new SettingsGUI(this.plugin);
        settingsGUI.open(player);
        return true;
    }

    private void showSettingsInChat(Player player) {
        if (this.plugin.getSettingsManager() == null) {
            MessageUtils.sendMessage(player, "&cSettings system is not available!");
            return;
        }
        SettingsManager sm = this.plugin.getSettingsManager();
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&6&l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac YOUR SETTINGS \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        MessageUtils.sendMessage(player, "");
        this.sendSettingStatus(player, "Scoreboard", sm.hasScoreboardEnabled(player));
        this.sendSettingStatus(player, "Duel Requests", sm.canReceiveDuelRequests(player));
        this.sendSettingStatus(player, "Party Invites", sm.canReceivePartyInvites(player));
        this.sendSettingStatus(player, "Death Messages", sm.showDeathMessages(player));
        this.sendSettingStatus(player, "Spectator Notifications", sm.showSpectatorJoinMessages(player));
        this.sendSettingStatus(player, "Sounds", sm.hasSoundsEnabled(player));
        this.sendSettingStatus(player, "Auto Re-Queue", sm.hasAutoRequeue(player));
        this.sendSettingStatus(player, "Show Ping", sm.showPing(player));
        this.sendSettingStatus(player, "Allow Spectators", sm.allowsSpectators(player));
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&7Use &e/settings &7to open the GUI.");
        MessageUtils.sendMessage(player, "&7Use &e/toggle <setting> &7to toggle settings.");
        MessageUtils.sendMessage(player, "&6&l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        MessageUtils.sendMessage(player, "");
    }

    private void sendSettingStatus(Player player, String name, boolean enabled) {
        String status = enabled ? "&a\u2714 Enabled" : "&c\u2718 Disabled";
        MessageUtils.sendMessage(player, "  &7" + name + ": " + status);
    }

    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&6&l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac SETTINGS HELP \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&e/settings &7- Open settings GUI");
        MessageUtils.sendMessage(player, "&e/settings chat &7- Show settings in chat");
        MessageUtils.sendMessage(player, "&e/settings reset &7- Reset all settings");
        MessageUtils.sendMessage(player, "&e/toggle <setting> &7- Toggle a specific setting");
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&6&l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        MessageUtils.sendMessage(player, "");
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("chat", "list", "reset", "help").stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

