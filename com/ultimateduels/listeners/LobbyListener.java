/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 *  org.bukkit.event.entity.FoodLevelChangeEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerAttemptPickupItemEvent
 *  org.bukkit.event.player.PlayerChangedWorldEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.kit.PlayerKitSelectorGUI;
import com.ultimateduels.gui.queue.QueueMainGUI;
import com.ultimateduels.gui.settings.SettingsGUI;
import com.ultimateduels.lobby.LobbyManager;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class LobbyListener
implements Listener {
    private final UltimateDuels plugin;
    private final LobbyManager lobbyManager;

    public LobbyListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.lobbyManager = plugin.getLobbyManager();
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getWorldRestrictionManager().isPlayerInAllowedWorld(player)) {
            return;
        }
        if (this.lobbyManager == null) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!this.lobbyManager.isInLobby(player) && !this.lobbyManager.isInLobbyWorld(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !this.lobbyManager.isLobbyItem(item)) {
            return;
        }
        event.setCancelled(true);
        LobbyManager.LobbyItemAction lobbyAction = this.lobbyManager.getLobbyItemAction(item);
        if (lobbyAction == null || lobbyAction == LobbyManager.LobbyItemAction.NONE) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        this.handleLobbyItemAction(player, lobbyAction);
    }

    private void handleLobbyItemAction(Player player, LobbyManager.LobbyItemAction action) {
        switch (action) {
            case QUEUE_MENU: {
                this.openQueueMenu(player);
                break;
            }
            case PARTY_MENU: {
                this.openPartyMenu(player);
                break;
            }
            case KIT_EDITOR: {
                this.openKitEditor(player);
                break;
            }
            case FFA_MENU: {
                this.openFFAMenu(player);
                break;
            }
            case SETTINGS_MENU: {
                this.openSettingsMenu(player);
                break;
            }
            case SPECTATE_MENU: {
                this.openSpectateMenu(player);
                break;
            }
            case STATS_MENU: {
                this.openStatsMenu(player);
                break;
            }
            case SHOP_MENU: {
                this.openShopMenu(player);
                break;
            }
            case LEAVE_LOBBY: {
                this.handleLeaveLobby(player);
                break;
            }
        }
    }

    private void openQueueMenu(Player player) {
        try {
            new QueueMainGUI(this.plugin).open(player);
        }
        catch (Exception e) {
            player.sendMessage("\u00a7cFailed to open queue menu!");
        }
    }

    private void openPartyMenu(Player player) {
        player.performCommand("party");
    }

    private void openKitEditor(Player player) {
        try {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (player.isOnline()) {
                    new PlayerKitSelectorGUI(this.plugin, player).open(player);
                }
            }, 1L);
        }
        catch (Exception e) {
            player.sendMessage("\u00a7cFailed to open kit editor!");
        }
    }

    private void openFFAMenu(Player player) {
        block4: {
            try {
                Class<?> ffaGuiClass = Class.forName("com.ultimateduels.gui.ffa.FFAMenuGUI");
                Object ffaGui = ffaGuiClass.getConstructor(UltimateDuels.class).newInstance(new Object[]{this.plugin});
                ffaGuiClass.getMethod("open", Player.class).invoke(ffaGui, player);
            }
            catch (Exception e) {
                player.sendMessage("\u00a76\u00a7lFFA Arenas:");
                if (this.plugin.getFFAManager() == null) break block4;
                Collection<FFAArenaInstance> arenas = this.plugin.getFFAManager().getAllArenas();
                if (arenas.isEmpty()) {
                    player.sendMessage("\u00a77No FFA arenas available.");
                }
                for (FFAArenaInstance arena : arenas) {
                    String status = arena.isEnabled() ? "\u00a7a\u25cf" : "\u00a7c\u25cf";
                    player.sendMessage("\u00a77  " + status + " \u00a7f" + arena.getKitName() + " \u00a77(\u00a7e" + arena.getPlayerCount() + " \u00a77players)");
                }
            }
        }
    }

    private void openSettingsMenu(Player player) {
        try {
            new SettingsGUI(this.plugin).open(player);
        }
        catch (Exception e) {
            player.sendMessage("\u00a7cFailed to open settings menu!");
        }
    }

    private void openSpectateMenu(Player player) {
        player.sendMessage("\u00a79\u00a7lActive Duels:");
        if (this.plugin.getDuelManager() != null) {
            int activeDuels = this.plugin.getDuelManager().getActiveMatchCount();
            if (activeDuels == 0) {
                player.sendMessage("\u00a77No active duels to spectate.");
            } else {
                player.sendMessage("\u00a77There are \u00a7e" + activeDuels + " \u00a77active duels.");
                player.sendMessage("\u00a77Use \u00a7e/spectate <player> \u00a77to spectate.");
            }
        }
    }

    private void openStatsMenu(Player player) {
        player.performCommand("stats");
    }

    private void openShopMenu(Player player) {
        player.sendMessage("\u00a7cShop coming soon!");
    }

    private void handleLeaveLobby(Player player) {
        this.lobbyManager.removeFromLobby(player);
        if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().hasState(player)) {
            this.plugin.getPlayerStateManager().restoreState(player);
        }
        if (this.plugin.getConfig().getBoolean("bungeecord.enabled", false)) {
            String hubServer = this.plugin.getConfig().getString("bungeecord.hub-server", "hub");
            this.connectToServer(player, hubServer);
        } else {
            String leaveCommand = this.plugin.getConfig().getString("lobby.leave-command", "");
            if (!leaveCommand.isEmpty()) {
                player.performCommand(leaveCommand);
            } else {
                player.sendMessage("\u00a7aYou have left the duels lobby.");
            }
        }
    }

    private void connectToServer(Player player, String server) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage((Plugin)this.plugin, "BungeeCord", b.toByteArray());
        }
        catch (IOException e) {
            player.sendMessage("\u00a7cFailed to connect to " + server + "!");
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null || !this.lobbyManager.isLobbyEnabled()) {
            return;
        }
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
                return;
            }
            if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
                return;
            }
            if (this.lobbyManager.isInLobbyWorld(player) && !this.lobbyManager.isInLobby(player)) {
                this.lobbyManager.sendToLobby(player, false);
            }
        }, 20L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (this.lobbyManager != null) {
            this.lobbyManager.removeFromLobby(event.getPlayer());
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        boolean wasInFFA;
        Player player = event.getPlayer();
        if (this.lobbyManager == null || !this.lobbyManager.isLobbyEnabled()) {
            return;
        }
        boolean wasInDuel = this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId());
        boolean bl = wasInFFA = this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId());
        if ((wasInDuel || wasInFFA || this.lobbyManager.isInLobby(player)) && this.lobbyManager.getLobbySpawn() != null) {
            event.setRespawnLocation(this.lobbyManager.getLobbySpawn());
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (player.isOnline()) {
                    this.lobbyManager.sendToLobby(player, false);
                }
            }, 1L);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null) {
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            return;
        }
        boolean pluginAllowed = this.plugin.getWorldRestrictionManager() == null
                || this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(player.getWorld());
        if (this.lobbyManager.isInLobbyWorld(player) && pluginAllowed && !this.lobbyManager.isInLobby(player)) {
            this.lobbyManager.sendToLobby(player, false);
        } else if (!this.lobbyManager.isInLobbyWorld(player) && this.lobbyManager.isInLobby(player)) {
            if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().hasLobbyState(player.getUniqueId())) {
                this.plugin.getPlayerStateManager().restoreLobbyState(player);
            }
            this.lobbyManager.removeFromLobby(player);
        }
    }

    private boolean isInLobbyArea(Player player) {
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
            return false;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            return false;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            return false;
        }
        return this.lobbyManager.isInLobby(player) || this.lobbyManager.isInLobbyWorld(player);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        if (this.lobbyManager == null) {
            return;
        }
        if (this.isInLobbyArea(victim) && !this.lobbyManager.isPvpEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (this.lobbyManager == null) {
            return;
        }
        if (this.isInLobbyArea(player)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && !this.lobbyManager.isFallDamageEnabled()) {
                event.setCancelled(true);
                return;
            }
            if (!this.lobbyManager.isDamageEnabled() && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity humanEntity = event.getEntity();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (this.lobbyManager == null) {
            return;
        }
        if (this.isInLobbyArea(player) && !this.lobbyManager.isHungerEnabled()) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null || !this.lobbyManager.isLobbyEnabled()) {
            return;
        }
        boolean inLobby = this.lobbyManager.isInLobby(player);
        boolean inLobbyWorld = this.lobbyManager.isInLobbyWorld(player);
        if (inLobby || inLobbyWorld) {
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("ultimateduels.admin.build")) {
                return;
            }
            if (!this.lobbyManager.isBlockBreakEnabled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null || !this.lobbyManager.isLobbyEnabled()) {
            return;
        }
        boolean inLobby = this.lobbyManager.isInLobby(player);
        boolean inLobbyWorld = this.lobbyManager.isInLobbyWorld(player);
        if (inLobby || inLobbyWorld) {
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("ultimateduels.admin.build")) {
                return;
            }
            if (!this.lobbyManager.isBlockPlaceEnabled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null) {
            return;
        }
        if (this.isInLobbyArea(player) && !this.lobbyManager.isItemDropEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null) {
            return;
        }
        if (this.isInLobbyArea(player) && !this.lobbyManager.isItemPickupEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (this.lobbyManager == null) {
            return;
        }
        if (event.getView().getTopInventory().getHolder() instanceof AbstractGUI) {
            return;
        }
        if (this.isInLobbyArea(player)) {
            ItemStack clicked = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (this.lobbyManager.isLobbyItem(clicked) || this.lobbyManager.isLobbyItem(cursor)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (this.lobbyManager == null) {
            return;
        }
        if (this.isInLobbyArea(player) && (this.lobbyManager.isLobbyItem(event.getMainHandItem()) || this.lobbyManager.isLobbyItem(event.getOffHandItem()))) {
            event.setCancelled(true);
        }
    }
}

