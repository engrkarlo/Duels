/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityPickupItemEvent
 *  org.bukkit.event.entity.ItemSpawnEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDropListener
implements Listener {
    private final UltimateDuels plugin;
    private final Set<UUID> duelDroppedItems;
    private final Set<UUID> ffaDroppedItems;
    private static final int DEFAULT_DUEL_ITEM_DESPAWN_TIME = 30;
    private static final int DEFAULT_FFA_ITEM_DESPAWN_TIME = 30;
    private static final boolean DEFAULT_PREVENT_LOBBY_ITEM_DROP = true;
    private static final boolean DEFAULT_PREVENT_LOBBY_ITEM_PICKUP = true;
    private static final boolean DEFAULT_FFA_ALLOWS_ITEM_DROPPING = true;
    private static final boolean DEFAULT_ONLY_PICKUP_DUEL_ITEMS = true;

    public PlayerDropListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.duelDroppedItems = new HashSet<UUID>();
        this.ffaDroppedItems = new HashSet<UUID>();
        this.startCleanupTask();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            this.handleDuelDrop(event, player);
            return;
        }
        if (this.plugin.getFFAManager().isInFFAArena(player)) {
            this.handleFFADrop(event, player);
            return;
        }
        if (this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "&cYou cannot drop items while in queue!");
            return;
        }
        if (this.isLobbyItem(droppedItem)) {
            event.setCancelled(true);
            return;
        }
        if (this.plugin.getLobbyManager().isInLobby(player) && !this.plugin.getLobbyManager().isItemDropEnabled()) {
            event.setCancelled(true);
        }
    }

    private void handleDuelDrop(PlayerDropItemEvent event, Player player) {
        DuelMatch match = this.plugin.getDuelManager().getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        MatchState state = match.getState();
        if (state != MatchState.IN_PROGRESS) {
            event.setCancelled(true);
            if (state == MatchState.STARTING) {
                MessageUtils.sendActionBar(player, "&cWait for the match to start!");
            } else if (state == MatchState.ROUND_ENDING || state == MatchState.ENDING) {
                MessageUtils.sendActionBar(player, "&cThe round is ending!");
            } else if (state == MatchState.RESETTING) {
                MessageUtils.sendActionBar(player, "&cPlease wait for the next round!");
            }
            return;
        }
        DuelKit kit = this.plugin.getKitManager().getAdminKit(match.getKitName());
        if (kit != null && !kit.isAllowDropping()) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(player, "&cItem dropping is disabled for this kit!");
            return;
        }
        Item droppedItem = event.getItemDrop();
        this.duelDroppedItems.add(droppedItem.getUniqueId());
        int despawnTime = this.getDuelItemDespawnTime();
        if (despawnTime > 0) {
            this.scheduleItemRemoval(droppedItem, despawnTime);
        }
    }

    private void handleFFADrop(PlayerDropItemEvent event, Player player) {
        if (!this.ffaAllowsItemDropping()) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(player, "&cItem dropping is disabled in FFA!");
            return;
        }
        Item droppedItem = event.getItemDrop();
        this.ffaDroppedItems.add(droppedItem.getUniqueId());
        int despawnTime = this.getFFAItemDespawnTime();
        if (despawnTime > 0) {
            this.scheduleItemRemoval(droppedItem, despawnTime);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        Item item = event.getItem();
        UUID itemId = item.getUniqueId();
        if (this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            DuelMatch match = this.plugin.getDuelManager().getMatch(player.getUniqueId());
            if (match == null) {
                event.setCancelled(true);
                return;
            }
            if (match.getState() != MatchState.IN_PROGRESS) {
                event.setCancelled(true);
                return;
            }
            if (!this.duelDroppedItems.contains(itemId) && this.onlyPickupDuelItems()) {
                event.setCancelled(true);
                return;
            }
            this.duelDroppedItems.remove(itemId);
            return;
        }
        if (this.plugin.getFFAManager().isInFFAArena(player)) {
            this.ffaDroppedItems.remove(itemId);
            return;
        }
        if (this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (this.preventLobbyItemPickup()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
    }

    private void scheduleItemRemoval(final Item item, int seconds) {
        new BukkitRunnable(){

            public void run() {
                if (item.isValid() && !item.isDead()) {
                    UUID itemId = item.getUniqueId();
                    PlayerDropListener.this.duelDroppedItems.remove(itemId);
                    PlayerDropListener.this.ffaDroppedItems.remove(itemId);
                    item.remove();
                }
            }
        }.runTaskLater((Plugin)this.plugin, (long)seconds * 20L);
    }

    private boolean isLobbyItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return false;
        }
        List lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }
        String lastLine = (String)lore.get(lore.size() - 1);
        return lastLine.contains("\u00a70\u00a7l\u00a7u\u00a7d");
    }

    public void cleanupDuelItems(DuelMatch match) {
        if (match == null || match.getArena() == null) {
            return;
        }
        DuelArena arena = match.getArena();
        Location spawn1 = arena.getSpawnPoint1();
        if (spawn1 == null) {
            return;
        }
        World world = spawn1.getWorld();
        if (world == null) {
            return;
        }
        for (Entity entity : world.getEntities()) {
            Item item;
            if (!(entity instanceof Item) || !this.duelDroppedItems.contains((item = (Item)entity).getUniqueId())) continue;
            this.duelDroppedItems.remove(item.getUniqueId());
            item.remove();
        }
    }

    public void cleanupFFAItems(String arenaName) {
        FFAArenaInstance arenaInstance = this.plugin.getFFAManager().getArena(arenaName);
        if (arenaInstance == null) {
            return;
        }
        DuelArena arena = arenaInstance.getArena();
        if (arena == null) {
            return;
        }
        Location spawn1 = arena.getSpawnPoint1();
        if (spawn1 == null) {
            return;
        }
        World world = spawn1.getWorld();
        if (world == null) {
            return;
        }
        for (Entity entity : world.getEntities()) {
            Item item;
            if (!(entity instanceof Item) || !this.ffaDroppedItems.contains((item = (Item)entity).getUniqueId())) continue;
            this.ffaDroppedItems.remove(item.getUniqueId());
            item.remove();
        }
    }

    private void startCleanupTask() {
        new BukkitRunnable(){

            public void run() {
                PlayerDropListener.this.duelDroppedItems.clear();
                PlayerDropListener.this.ffaDroppedItems.clear();
            }
        }.runTaskTimer((Plugin)this.plugin, 6000L, 6000L);
    }

    public void clearTrackedItems() {
        this.duelDroppedItems.clear();
        this.ffaDroppedItems.clear();
    }

    private boolean preventLobbyItemDrop() {
        return true;
    }

    private boolean preventLobbyItemPickup() {
        return true;
    }

    private int getDuelItemDespawnTime() {
        return 30;
    }

    private int getFFAItemDespawnTime() {
        return 30;
    }

    private boolean ffaAllowsItemDropping() {
        return true;
    }

    private boolean onlyPickupDuelItems() {
        return true;
    }
}

