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
package com.ultimateduels.commands.kit;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.kit.PlayerKitEditorGUI;
import com.ultimateduels.gui.kit.PlayerKitSelectorGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitEditorCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;

    public KitEditorCommand(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("ultimateduels.kiteditor")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot edit kits while in a duel!");
            return true;
        }
        if (this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou cannot edit kits while in FFA!");
            return true;
        }
        if (this.plugin.getQueueManager().isInQueue(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot edit kits while in queue!");
            return true;
        }
        if (args.length == 0) {
            PlayerKitSelectorGUI selectorGUI = new PlayerKitSelectorGUI(this.plugin, player);
            selectorGUI.open(player);
            return true;
        }
        String kitName = args[0].toLowerCase();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(kitName);
        if (kit == null) {
            MessageUtils.sendMessage(player, "&cKit '&f" + args[0] + "&c' not found!");
            MessageUtils.sendMessage(player, "&7Use &e/kiteditor &7to see available kits.");
            return true;
        }
        PlayerKitEditorGUI editorGUI = new PlayerKitEditorGUI(this.plugin, player, kit);
        editorGUI.open(player);
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return this.plugin.getKitManager().getAllAdminKits().stream().map(DuelKit::getName).filter(name -> name.toLowerCase().startsWith(input)).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

