/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerChangedWorldEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldChangeListener
implements Listener {
    private final UltimateDuels plugin;
    private final Set<UUID> recentlyFinishedMatch;

    public WorldChangeListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.recentlyFinishedMatch = ConcurrentHashMap.newKeySet();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();
        List<String> duelLobbyWorlds = this.plugin.getConfig().getStringList("scoreboard.duel-lobby-worlds");
        if (duelLobbyWorlds.isEmpty()) {
            duelLobbyWorlds = List.of("duellobby", "duels_lobby", "duels");
        }
        boolean wasInDuelLobby = duelLobbyWorlds.contains(fromWorld);
        boolean isInDuelLobby = duelLobbyWorlds.contains(toWorld);
        boolean activelyInMatch = this.plugin.getDuelManager().isInMatch(uuid);
        boolean activelyInFFA = this.plugin.getFFAManager().isInFFA(uuid);
        boolean justFinishedMatch = this.recentlyFinishedMatch.contains(uuid);
        this.plugin.debug("WorldChange: " + fromWorld + " \u2192 " + toWorld + " (inMatch=" + activelyInMatch + ", inFFA=" + activelyInFFA + ", justFinished=" + justFinishedMatch + ")");
        if (activelyInMatch || activelyInFFA) {
            this.plugin.debug("Player " + player.getName() + " is actively in match/FFA - skipping");
            return;
        }
        if (justFinishedMatch && isInDuelLobby) {
            this.plugin.debug("Player " + player.getName() + " finished match - giving lobby items");
            this.recentlyFinishedMatch.remove(uuid);
            this.handlePostMatchLobbyReturn(player, toWorld);
            return;
        }
        boolean pluginAllowed = this.plugin.getWorldRestrictionManager() == null
                || this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(player.getWorld());
        if (!wasInDuelLobby && isInDuelLobby && pluginAllowed) {
            this.handleEnterDuelLobby(player, toWorld);
        }
        if (wasInDuelLobby && !isInDuelLobby) {
            if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().hasLobbyState(uuid)) {
                this.plugin.getPlayerStateManager().restoreLobbyState(player);
            }
            this.handleLeaveDuelLobby(player, fromWorld);
        }
    }

    public void markPlayerFinishingMatch(final UUID uuid) {
        this.recentlyFinishedMatch.add(uuid);
        new BukkitRunnable(){

            public void run() {
                WorldChangeListener.this.recentlyFinishedMatch.remove(uuid);
            }
        }.runTaskLater((Plugin)this.plugin, 200L);
    }

    private void handlePostMatchLobbyReturn(final Player player, final String worldName) {
        this.plugin.debug("Post-match lobby return: " + player.getName());
        new BukkitRunnable(){

            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                if (!player.getWorld().getName().equals(worldName)) {
                    return;
                }
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getInventory().setItemInOffHand(null);
                WorldChangeListener.this.plugin.getLobbyManager().giveHotbarItems(player);
                if (WorldChangeListener.this.plugin.getScoreboardManager() != null) {
                    WorldChangeListener.this.plugin.getScoreboardManager().updateScoreboard(player);
                }
                WorldChangeListener.this.plugin.debug("Gave post-match lobby items to " + player.getName());
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
    }

    private void handleEnterDuelLobby(final Player player, final String worldName) {
        this.plugin.debug("Player " + player.getName() + " entered duel lobby: " + worldName);
        new BukkitRunnable(){

            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                if (!player.getWorld().getName().equals(worldName)) {
                    return;
                }
                if (WorldChangeListener.this.plugin.getDuelManager().isInMatch(player.getUniqueId()) || WorldChangeListener.this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
                    return;
                }
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getInventory().setItemInOffHand(null);
                WorldChangeListener.this.plugin.getLobbyManager().giveHotbarItems(player);
                if (WorldChangeListener.this.plugin.getScoreboardManager() != null) {
                    WorldChangeListener.this.plugin.getScoreboardManager().updateScoreboard(player);
                }
                WorldChangeListener.this.plugin.debug("Gave duel lobby items to " + player.getName());
            }
        }.runTaskLater((Plugin)this.plugin, 15L);
    }

    private void handleLeaveDuelLobby(Player player, String fromWorld) {
        this.plugin.debug("Player " + player.getName() + " left duel lobby: " + fromWorld);
        if (this.plugin.getDuelManager().isInMatch(player.getUniqueId()) || this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            return;
        }
        this.plugin.getLobbyManager().clearHotbarItems(player);
        if (this.plugin.getScoreboardManager() != null) {
            this.plugin.getScoreboardManager().removeScoreboard(player);
        }
    }
}

