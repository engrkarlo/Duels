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
 *  org.bukkit.event.entity.EntityRegainHealthEvent
 *  org.bukkit.event.entity.EntityRegainHealthEvent$RegainReason
 *  org.bukkit.event.entity.FoodLevelChangeEvent
 *  org.bukkit.event.player.PlayerItemConsumeEvent
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.kit.model.DuelKit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class FoodLevelListener
implements Listener {
    private final UltimateDuels plugin;
    private final DuelManager duelManager;
    private final FFAManager ffaManager;
    private final KitManager kitManager;

    public FoodLevelListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
        this.ffaManager = plugin.getFFAManager();
        this.kitManager = plugin.getKitManager();
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity humanEntity = event.getEntity();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (!this.plugin.getWorldRestrictionManager().isPlayerInAllowedWorld(player)) {
            return;
        }
        if (this.duelManager.isSpectating(player.getUniqueId())) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            return;
        }
        if (this.duelManager.isInDuel(player)) {
            this.handleDuelFoodChange(event, player);
            return;
        }
        if (this.ffaManager.isInFFAArena(player)) {
            this.handleFFAFoodChange(event, player);
            return;
        }
        if (this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
            return;
        }
        if (this.plugin.getLobbyManager().isLobbyEnabled() && this.isInLobby(player) && this.plugin.getConfigManager().getLobbySettings().preventHunger() && event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
        }
    }

    private void handleDuelFoodChange(FoodLevelChangeEvent event, Player player) {
        DuelMatch match = this.duelManager.getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        DuelKit kit = this.kitManager.getAdminKit(match.getKitName());
        if (match.getState() == MatchState.STARTING) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            return;
        }
        if (match.getState() == MatchState.ROUND_ENDING || match.getState() == MatchState.ENDING || match.getState() == MatchState.COMPLETED) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            return;
        }
        if (match.getState() == MatchState.IN_PROGRESS && kit != null && kit.isHungerLocked()) {
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
            return;
        }
    }

    private void handleFFAFoodChange(FoodLevelChangeEvent event, Player player) {
        String arenaName = this.ffaManager.getPlayerArena(player.getUniqueId());
        if (arenaName == null) {
            return;
        }
        FFAArenaInstance arena = this.ffaManager.getArena(arenaName);
        if (arena == null) {
            return;
        }
        DuelKit kit = this.kitManager.getAdminKit(arena.getKitName());
        if (kit != null && kit.isHungerLocked()) {
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
            return;
        }
        if (this.ffaManager.hasSpawnProtection(player.getUniqueId())) {
            event.setFoodLevel(20);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (this.duelManager.isInDuel(player)) {
            DuelMatch match = this.duelManager.getMatch(player.getUniqueId());
            if (match == null) {
                return;
            }
            DuelKit kit = this.kitManager.getAdminKit(match.getKitName());
            if (kit == null) {
                return;
            }
            if (kit.isHealthLocked()) {
                switch (event.getRegainReason()) {
                    case SATIATED: 
                    case REGEN: 
                    case MAGIC: 
                    case EATING: 
                    case MAGIC_REGEN: {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
            return;
        }
        if (this.ffaManager.isInFFAArena(player)) {
            String arenaName = this.ffaManager.getPlayerArena(player.getUniqueId());
            if (arenaName == null) {
                return;
            }
            FFAArenaInstance arena = this.ffaManager.getArena(arenaName);
            if (arena == null) {
                return;
            }
            DuelKit kit = this.kitManager.getAdminKit(arena.getKitName());
            if (kit != null && kit.isHealthLocked() && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (this.duelManager.isSpectating(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (this.duelManager.isInDuel(player)) {
            DuelMatch match = this.duelManager.getMatch(player.getUniqueId());
            if (match == null) {
                return;
            }
            if (match.getState() == MatchState.STARTING) {
                event.setCancelled(true);
                return;
            }
            if (match.getState() == MatchState.ROUND_ENDING || match.getState() == MatchState.ENDING || match.getState() == MatchState.COMPLETED) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (this.ffaManager.isInFFAArena(player)) {
            // empty if block
        }
    }

    private boolean isInLobby(Player player) {
        return !this.duelManager.isInDuel(player) && !this.ffaManager.isInFFAArena(player) && !this.duelManager.isSpectating(player.getUniqueId());
    }
}

