/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerAttemptPickupItemEvent
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerGameModeChangeEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;

public class SpectatorListener
implements Listener {
    private final UltimateDuels plugin;

    public SpectatorListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    private boolean isSpectating(Player player) {
        if (this.plugin.getDuelManager() == null) {
            return false;
        }
        return this.plugin.getDuelManager().isSpectating(player.getUniqueId());
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorDamageEntity(EntityDamageByEntityEvent event) {
        Player damager;
        Entity entity = event.getDamager();
        if (entity instanceof Player && this.isSpectating(damager = (Player)entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorDamage(EntityDamageEvent event) {
        Player player;
        Entity entity = event.getEntity();
        if (entity instanceof Player && this.isSpectating(player = (Player)entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorBlockBreak(BlockBreakEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorBlockPlace(BlockPlaceEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorInteract(PlayerInteractEvent event) {
        if (this.isSpectating(event.getPlayer()) && event.getItem() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorInteractEntity(PlayerInteractEntityEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorDropItem(PlayerDropItemEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorPickupItem(PlayerAttemptPickupItemEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorInventoryClick(InventoryClickEvent event) {
        Player player;
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player && this.isSpectating(player = (Player)humanEntity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorSwapHands(PlayerSwapHandItemsEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SPECTATOR && this.isSpectating(event.getPlayer())) {
            Player player = event.getPlayer();
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (player.isOnline() && player.getGameMode() == GameMode.SPECTATOR) {
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                    player.getInventory().setItemInOffHand(null);
                    player.updateInventory();
                }
            }, 1L);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSpectatorCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.isSpectating(player)) {
            return;
        }
        String command = event.getMessage().toLowerCase();
        if ((command.startsWith("/gamemode") || command.startsWith("/gm") || command.startsWith("/tp") && !command.startsWith("/tpa")) && !player.hasPermission("ultimateduels.admin")) {
            event.setCancelled(true);
            player.sendMessage("\u00a7cYou cannot use this command while spectating!");
        }
    }
}

