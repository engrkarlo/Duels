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
import com.ultimateduels.settings.PlayerSettings;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleDuelsCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public ToggleDuelsCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.settings")) {
            player.sendMessage("\u00a7cYou don't have permission to use this command!");
            return true;
        }
        if (this.plugin.getSettingsManager() == null) {
            player.sendMessage("\u00a7cSettings system is not available!");
            return true;
        }
        PlayerSettings settings = this.plugin.getSettingsManager().getSettings(player.getUniqueId());
        if (settings == null) {
            player.sendMessage("\u00a7cCould not load your settings!");
            return true;
        }
        boolean newValue = !settings.isDuelRequestsEnabled();
        settings.setDuelRequestsEnabled(newValue);
        this.plugin.getSettingsManager().saveSettings(player.getUniqueId());
        if (newValue) {
            player.sendMessage("\u00a7aDuel requests have been \u00a7eENABLED\u00a7a.");
        } else {
            player.sendMessage("\u00a7aDuel requests have been \u00a7cDISABLED\u00a7a.");
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<String>();
    }
}

