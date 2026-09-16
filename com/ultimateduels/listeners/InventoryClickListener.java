/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryCreativeEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.inventory.InventoryOpenEvent
 *  org.bukkit.inventory.Inventory
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.kit.model.DuelKit;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class InventoryClickListener
implements Listener {
    private final UltimateDuels plugin;
    private final DuelManager duelManager;
    private final FFAManager ffaManager;
    private final KitManager kitManager;

    public InventoryClickListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
        this.ffaManager = plugin.getFFAManager();
        this.kitManager = plugin.getKitManager();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof AbstractGUI) {
            return;
        }
        UUID playerUUID = player.getUniqueId();
        if (this.duelManager != null && this.duelManager.isInMatch(playerUUID)) {
            this.handleDuelInventoryClick(event, player);
            return;
        }
        if (this.ffaManager != null && this.ffaManager.isInFFA(playerUUID)) {
            this.handleFFAInventoryClick(event, player);
            return;
        }
        if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
    }

    private void handleDuelInventoryClick(InventoryClickEvent event, Player player) {
        UUID playerUUID = player.getUniqueId();
        DuelMatch match = this.duelManager.getMatch(playerUUID);
        if (match == null) {
            return;
        }
        DuelKit kit = null;
        if (this.kitManager != null) {
            kit = this.kitManager.getAdminKit(match.getKitName());
        }
        if (kit == null) {
            return;
        }
    }

    private void handleFFAInventoryClick(InventoryClickEvent event, Player player) {
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof AbstractGUI) {
            return;
        }
        UUID playerUUID = player.getUniqueId();
        if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof AbstractGUI) {
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof AbstractGUI) {
            return;
        }
        UUID playerUUID = player.getUniqueId();
        if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCreativeInventory(InventoryCreativeEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof AbstractGUI) {
            return;
        }
        UUID playerUUID = player.getUniqueId();
        if (this.duelManager != null && this.duelManager.isInMatch(playerUUID)) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            return;
        }
        if (this.ffaManager != null && this.ffaManager.isInFFA(playerUUID)) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            return;
        }
        if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().isSpectating(playerUUID)) {
            event.setCancelled(true);
        }
    }
}

