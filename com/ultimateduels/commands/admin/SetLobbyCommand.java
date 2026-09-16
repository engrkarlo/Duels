/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.admin;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.lobby.LobbyManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand
extends BaseCommand {
    public SetLobbyCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.setlobby", true);
    }

    @Override
    protected void execute(Player player, String[] args) {
        LobbyManager lobbyManager = this.plugin.getLobbyManager();
        if (lobbyManager == null) {
            this.sendMessage(player, "&cLobby system is not available!");
            return;
        }
        lobbyManager.setLobbySpawn(player.getLocation());
        this.sendMessage(player, "&a&lLobby spawn has been set!");
        this.sendMessage(player, "&7Location: &e" + this.formatLocation(player));
        this.sendMessage(player, "&7World: &e" + player.getWorld().getName());
    }

    private String formatLocation(Player player) {
        return String.format("%.2f, %.2f, %.2f", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<String>();
    }
}

