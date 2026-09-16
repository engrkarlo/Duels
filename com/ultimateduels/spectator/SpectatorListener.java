/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityPickupItemEvent
 *  org.bukkit.event.entity.FoodLevelChangeEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.spectator;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class SpectatorListener
implements Listener {
    private final UltimateDuels plugin;
    private final DuelManager duelManager;

    public SpectatorListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
    }

    private boolean isSpectating(Player player) {
        return this.duelManager != null && this.duelManager.isSpectating(player.getUniqueId());
    }

    private DuelMatch getSpectatedMatch(Player spectator) {
        if (this.duelManager == null) {
            return null;
        }
        for (DuelMatch match : this.duelManager.getActiveMatches()) {
            if (!this.duelManager.getMatchSpectators(match.getMatchId()).contains(spectator.getUniqueId())) continue;
            return match;
        }
        return null;
    }

    private void teleportToNextPlayer(Player spectator) {
        int nextIndex;
        Player nextTarget;
        DuelMatch match = this.getSpectatedMatch(spectator);
        if (match == null) {
            MessageUtils.sendMessage(spectator, "&cYou are not spectating any match!");
            return;
        }
        ArrayList<DuelParticipant> aliveParticipants = new ArrayList<DuelParticipant>();
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player p;
            if (!participant.isAlive() || (p = Bukkit.getPlayer((UUID)participant.getUuid())) == null || !p.isOnline()) continue;
            aliveParticipants.add(participant);
        }
        if (aliveParticipants.isEmpty()) {
            MessageUtils.sendMessage(spectator, "&cNo alive players to spectate!");
            return;
        }
        Player currentTarget = spectator.getSpectatorTarget() instanceof Player ? (Player)spectator.getSpectatorTarget() : null;
        int currentIndex = -1;
        if (currentTarget != null) {
            for (int i = 0; i < aliveParticipants.size(); ++i) {
                if (!((DuelParticipant)aliveParticipants.get(i)).getUuid().equals(currentTarget.getUniqueId())) continue;
                currentIndex = i;
                break;
            }
        }
        if ((nextTarget = Bukkit.getPlayer((UUID)((DuelParticipant)aliveParticipants.get(nextIndex = (currentIndex + 1) % aliveParticipants.size())).getUuid())) != null) {
            spectator.teleport(nextTarget.getLocation());
            MessageUtils.sendMessage(spectator, "&7Now spectating: &e" + nextTarget.getName());
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.isSpectating(player)) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Material type = item.getType();
        if (type == Material.COMPASS) {
            this.teleportToNextPlayer(player);
        } else if (type == Material.PAPER) {
            this.openPlayerListGUI(player);
        } else if (type == Material.COMPARATOR) {
            player.performCommand("settings");
        } else if (type == Material.RED_DYE) {
            this.duelManager.removeSpectator(player);
        }
    }

    private void openPlayerListGUI(Player spectator) {
        DuelMatch match = this.getSpectatedMatch(spectator);
        if (match == null) {
            MessageUtils.sendMessage(spectator, "&cYou are not spectating any match!");
            return;
        }
        MessageUtils.sendMessage(spectator, "&6&lPlayers in this duel:");
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player p;
            if (!participant.isAlive() || (p = Bukkit.getPlayer((UUID)participant.getUuid())) == null) continue;
            int health = (int)Math.ceil(p.getHealth());
            String teamColor = participant.getTeamId() == 1 ? "&a" : "&c";
            MessageUtils.sendMessage(spectator, "&7- " + teamColor + p.getName() + " &c" + health + "\u2764");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (this.isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player damager = (Player)entity;
        if (this.isSpectating(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        if (this.isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (event.getView().getTopInventory().getHolder() instanceof AbstractGUI) {
            return;
        }
        if (this.isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (this.isSpectating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity humanEntity = event.getEntity();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (this.isSpectating(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.isSpectating(player)) {
            this.duelManager.removeSpectator(player);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.isSpectating(player)) {
            return;
        }
        String command = event.getMessage().toLowerCase().split(" ")[0];
        if (command.equals("/spectate") || command.equals("/spec") || command.equals("/unspectate") || command.equals("/settings") || command.equals("/msg") || command.equals("/r") || command.equals("/reply") || command.equals("/tell")) {
            return;
        }
        event.setCancelled(true);
        MessageUtils.sendMessage(player, "&cYou cannot use this command while spectating!");
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!this.isSpectating(player)) {
            return;
        }
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            event.setCancelled(true);
        }
    }
}

