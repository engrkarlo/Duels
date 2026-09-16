/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Sound
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 *  org.bukkit.block.Sign
 *  org.bukkit.block.sign.Side
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.ffa.model.FFAResult;
import com.ultimateduels.gui.GUIManager;
import com.ultimateduels.lobby.LobbyManager;
import com.ultimateduels.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignClickListener
implements Listener {
    private final UltimateDuels plugin;
    private final LobbyManager lobbyManager;
    private final GUIManager guiManager;

    public SignClickListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.lobbyManager = plugin.getLobbyManager();
        this.guiManager = plugin.getGUIManager();
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onSignClick(PlayerInteractEvent event) {
        String parameter;
        String[] lines;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        BlockState blockState = block.getState();
        if (!(blockState instanceof Sign)) {
            return;
        }
        Sign sign = (Sign)blockState;
        Player player = event.getPlayer();
        try {
            lines = sign.getSide(Side.FRONT).getLines();
        }
        catch (NoSuchMethodError e) {
            lines = sign.getLines();
        }
        String firstLine = ChatColor.stripColor((String)lines[0]).trim();
        if (!(firstLine.equalsIgnoreCase("[UDuels]") || firstLine.equalsIgnoreCase("[Duels]") || firstLine.equalsIgnoreCase("[UD]"))) {
            return;
        }
        event.setCancelled(true);
        String action = ChatColor.stripColor((String)lines[1]).trim().toUpperCase();
        String string = parameter = lines.length > 2 ? ChatColor.stripColor((String)lines[2]).trim() : "";
        if (this.isPlayerBusy(player, action)) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        this.executeSignAction(player, action, parameter);
    }

    private void executeSignAction(Player player, String action, String parameter) {
        switch (action) {
            case "QUEUE": 
            case "DUEL": 
            case "RANKED": {
                if (this.guiManager != null) {
                    this.guiManager.openQueueGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cQueue system is not available!");
                break;
            }
            case "PARTY": 
            case "TEAM": {
                if (this.guiManager != null) {
                    this.guiManager.openPartyGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cParty system is not available!");
                break;
            }
            case "KIT": 
            case "KITS": 
            case "KITEDITOR": {
                if (this.guiManager != null) {
                    this.guiManager.openKitEditorGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cKit editor is not available!");
                break;
            }
            case "FFA": 
            case "FREEFORALL": {
                if (this.plugin.getFFAManager() != null) {
                    if (!parameter.isEmpty()) {
                        FFAResult result = this.plugin.getFFAManager().join(player, parameter);
                        if (!result.success()) {
                            result = this.plugin.getFFAManager().joinByKit(player, parameter);
                        }
                        if (result.success() || result.message() == null) break;
                        player.sendMessage(result.message());
                        break;
                    }
                    if (this.guiManager != null) {
                        this.guiManager.openFFAGUI(player);
                        break;
                    }
                    MessageUtils.sendMessage(player, "&cFFA GUI is not available!");
                    break;
                }
                MessageUtils.sendMessage(player, "&cFFA system is not available!");
                break;
            }
            case "SPECTATE": 
            case "SPEC": 
            case "WATCH": {
                if (this.guiManager != null) {
                    this.guiManager.openSpectateGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cSpectate system is not available!");
                break;
            }
            case "STATS": 
            case "STATISTICS": {
                if (this.guiManager != null) {
                    this.guiManager.openStatsGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cStats system is not available!");
                break;
            }
            case "SETTINGS": 
            case "OPTIONS": {
                if (this.guiManager != null) {
                    this.guiManager.openSettingsGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cSettings system is not available!");
                break;
            }
            case "LEADERBOARD": 
            case "TOP": 
            case "LEADERS": {
                if (this.guiManager != null) {
                    this.guiManager.openLeaderboardGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cLeaderboard is not available!");
                break;
            }
            case "LOBBY": 
            case "HUB": 
            case "SPAWN": {
                if (this.lobbyManager != null) {
                    this.lobbyManager.teleportToLobby(player);
                    MessageUtils.sendMessage(player, "&aTeleported to lobby!");
                    break;
                }
                player.performCommand("spawn");
                break;
            }
            case "LEAVE": 
            case "QUIT": 
            case "EXIT": {
                this.handleLeave(player);
                break;
            }
            default: {
                MessageUtils.sendMessage(player, "&cInvalid sign action: &e" + action);
                MessageUtils.sendMessage(player, "&7Valid actions: QUEUE, PARTY, FFA, KITS, SPECTATE, STATS, SETTINGS, LEADERBOARD, LOBBY, LEAVE");
            }
        }
    }

    private void handleLeave(Player player) {
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            this.plugin.getQueueManager().leaveQueue(player);
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            this.plugin.getDuelManager().removeSpectator(player);
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            this.plugin.getFFAManager().leave(player);
            return;
        }
        if (this.plugin.getPartyManager() != null && this.plugin.getPartyManager().isInParty(player.getUniqueId())) {
            this.plugin.getPartyManager().leaveParty(player);
            return;
        }
        if (this.lobbyManager != null && !this.lobbyManager.isInLobby(player)) {
            this.lobbyManager.teleportToLobby(player);
            MessageUtils.sendMessage(player, "&aTeleported to lobby!");
        } else {
            player.performCommand("hub");
        }
    }

    private boolean isPlayerBusy(Player player, String action) {
        if (action.equals("LEAVE") || action.equals("QUIT") || action.equals("EXIT")) {
            return false;
        }
        if (action.equals("LOBBY") || action.equals("SETTINGS") || action.equals("STATS") || action.equals("LEADERBOARD") || action.equals("HUB") || action.equals("SPAWN")) {
            return false;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot use this while in a duel!");
            MessageUtils.sendMessage(player, "&7Use a \u00a7e[LEAVE]\u00a77 sign to exit.");
            return true;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot use this while in FFA!");
            MessageUtils.sendMessage(player, "&7Use a \u00a7e[LEAVE]\u00a77 sign to exit.");
            return true;
        }
        return false;
    }
}

