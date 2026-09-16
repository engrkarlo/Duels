/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityRegainHealthEvent
 *  org.bukkit.event.entity.FoodLevelChangeEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.visuals.HealthDisplayManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthDisplayListener
implements Listener {
    private final UltimateDuels plugin;
    private final HealthDisplayManager healthDisplayManager;
    private final DuelManager duelManager;
    private final FFAManager ffaManager;

    public HealthDisplayListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.healthDisplayManager = plugin.getHealthDisplayManager();
        this.duelManager = plugin.getDuelManager();
        this.ffaManager = plugin.getFFAManager();
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player)entity;
        if (!this.isInCombat(player)) {
            return;
        }
        double newHealth = Math.max(0.0, player.getHealth() - event.getFinalDamage());
        final double maxHealth = player.getMaxHealth();
        new BukkitRunnable(){

            public void run() {
                if (player.isOnline() && !player.isDead()) {
                    HealthDisplayListener.this.healthDisplayManager.updateHealth(player, player.getHealth(), maxHealth);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 1L);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player)entity;
        if (!this.isInCombat(player)) {
            return;
        }
        double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + event.getAmount());
        final double maxHealth = player.getMaxHealth();
        new BukkitRunnable(){

            public void run() {
                if (player.isOnline() && !player.isDead()) {
                    HealthDisplayListener.this.healthDisplayManager.updateHealth(player, player.getHealth(), maxHealth);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 1L);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player victim = (Player)entity;
        Player damager = null;
        if (event.getDamager() instanceof Player) {
            damager = (Player)event.getDamager();
        }
        if (!this.isInCombat(victim)) {
            return;
        }
        new BukkitRunnable(){

            public void run() {
                if (victim.isOnline() && !victim.isDead()) {
                    HealthDisplayListener.this.healthDisplayManager.updateHealth(victim);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 1L);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity humanEntity = event.getEntity();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        final Player player = (Player)humanEntity;
        if (!this.isInCombat(player)) {
            return;
        }
        new BukkitRunnable(){

            public void run() {
                if (player.isOnline() && !player.isDead()) {
                    HealthDisplayListener.this.healthDisplayManager.updateHealth(player);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 2L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.healthDisplayManager.removeDisplay(player);
        this.healthDisplayManager.handleSpectatorLeave(player);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        new BukkitRunnable(){

            public void run() {
                if (player.isOnline() && HealthDisplayListener.this.isInCombat(player)) {
                    HealthDisplayListener.this.healthDisplayManager.createDisplay(player);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
    }

    private boolean isInCombat(Player player) {
        if (this.duelManager != null && this.duelManager.isInDuel(player)) {
            return true;
        }
        return this.ffaManager != null && this.ffaManager.isInFFAArena(player);
    }
}

