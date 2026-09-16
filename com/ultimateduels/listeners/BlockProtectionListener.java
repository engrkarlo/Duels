/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.EntityExplodeEvent
 *  org.bukkit.event.hanging.HangingBreakByEntityEvent
 *  org.bukkit.event.player.PlayerBucketEmptyEvent
 *  org.bukkit.event.player.PlayerBucketFillEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockProtectionListener
implements Listener {
    private final UltimateDuels plugin;

    public BlockProtectionListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null || ffaManager == null) {
            return;
        }
        if (duelManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        if (duelManager.isInMatch(playerUUID)) {
            this.handleDuelBlockBreak(event, player, block);
            return;
        }
        if (ffaManager.isInFFA(playerUUID)) {
            this.handleFFABlockBreak(event, player, block);
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        if (this.isInLobby(player) && !player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    private void handleDuelBlockBreak(BlockBreakEvent event, Player player, Block block) {
        DuelManager duelManager = this.plugin.getDuelManager();
        DuelMatch match = duelManager.getMatch(player.getUniqueId());
        if (match == null) {
            event.setCancelled(true);
            return;
        }
        if (match.getState() != MatchState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }
        DuelArena arena = match.getArena();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(match.getKitName());
        if (kit != null && kit.isAllowBuilding()) {
            event.setDropItems(true);
            return;
        }
        if (arena.isAllowBlockBreak()) {
            event.setDropItems(true);
            return;
        }
        event.setCancelled(true);
    }

    private void handleFFABlockBreak(BlockBreakEvent event, Player player, Block block) {
        FFAManager ffaManager = this.plugin.getFFAManager();
        String arenaName = ffaManager.getPlayerArena(player.getUniqueId());
        if (arenaName == null) {
            event.setCancelled(true);
            return;
        }
        FFAArenaInstance arenaInstance = ffaManager.getArena(arenaName);
        if (arenaInstance == null) {
            event.setCancelled(true);
            return;
        }
        DuelKit kit = this.plugin.getKitManager().getAdminKit(arenaInstance.getKitName());
        if (kit != null && kit.isAllowBuilding()) {
            event.setDropItems(true);
            return;
        }
        boolean globalAllow = this.plugin.getConfig().getBoolean("ffa.allow-block-break", false);
        if (globalAllow) {
            event.setDropItems(true);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null || ffaManager == null) {
            return;
        }
        if (duelManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        if (duelManager.isInMatch(playerUUID)) {
            this.handleDuelBlockPlace(event, player, block);
            return;
        }
        if (ffaManager.isInFFA(playerUUID)) {
            this.handleFFABlockPlace(event, player, block);
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        if (this.isInLobby(player) && !player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    private void handleDuelBlockPlace(BlockPlaceEvent event, Player player, Block block) {
        DuelManager duelManager = this.plugin.getDuelManager();
        DuelMatch match = duelManager.getMatch(player.getUniqueId());
        if (match == null) {
            event.setCancelled(true);
            return;
        }
        if (match.getState() != MatchState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }
        DuelArena arena = match.getArena();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(match.getKitName());
        if (kit != null && kit.isAllowBuilding()) {
            if (arena.hasBounds() && !arena.isWithinBounds(block.getLocation())) {
                event.setCancelled(true);
                MessageUtils.sendActionBar(player, "&cYou cannot place blocks outside the arena!");
                return;
            }
            return;
        }
        if (arena.isAllowBlockPlace()) {
            if (arena.hasBounds() && !arena.isWithinBounds(block.getLocation())) {
                event.setCancelled(true);
                MessageUtils.sendActionBar(player, "&cYou cannot place blocks outside the arena!");
                return;
            }
            return;
        }
        event.setCancelled(true);
    }

    private void handleFFABlockPlace(BlockPlaceEvent event, Player player, Block block) {
        FFAManager ffaManager = this.plugin.getFFAManager();
        String arenaName = ffaManager.getPlayerArena(player.getUniqueId());
        if (arenaName == null) {
            event.setCancelled(true);
            return;
        }
        FFAArenaInstance arenaInstance = ffaManager.getArena(arenaName);
        if (arenaInstance == null) {
            event.setCancelled(true);
            return;
        }
        DuelArena arena = arenaInstance.getArena();
        DuelKit kit = this.plugin.getKitManager().getAdminKit(arenaInstance.getKitName());
        if (kit != null && kit.isAllowBuilding()) {
            if (arena.hasBounds() && !arena.isWithinBounds(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        boolean globalAllow = this.plugin.getConfig().getBoolean("ffa.allow-block-place", false);
        if (globalAllow) {
            if (arena.hasBounds() && !arena.isWithinBounds(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null) {
            return;
        }
        if (duelManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        if (duelManager.isInMatch(playerUUID)) {
            DuelMatch match = duelManager.getMatch(playerUUID);
            if (match == null) {
                event.setCancelled(true);
                return;
            }
            DuelKit kit = this.plugin.getKitManager().getAdminKit(match.getKitName());
            if (kit != null && kit.isAllowBuilding()) {
                return;
            }
            if (match.getArena().isAllowBlockPlace()) {
                return;
            }
            event.setCancelled(true);
            return;
        }
        if (ffaManager != null && ffaManager.isInFFA(playerUUID)) {
            DuelKit kit;
            FFAArenaInstance arenaInstance;
            String arenaName = ffaManager.getPlayerArena(playerUUID);
            if (arenaName != null && (arenaInstance = ffaManager.getArena(arenaName)) != null && (kit = this.plugin.getKitManager().getAdminKit(arenaInstance.getKitName())) != null && kit.isAllowBuilding()) {
                return;
            }
            boolean globalAllow = this.plugin.getConfig().getBoolean("ffa.allow-block-place", false);
            if (!globalAllow) {
                event.setCancelled(true);
            }
            return;
        }
        if (!player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null) {
            return;
        }
        if (duelManager.isSpectating(playerUUID)) {
            event.setCancelled(true);
            return;
        }
        if (duelManager.isInMatch(playerUUID)) {
            DuelMatch match = duelManager.getMatch(playerUUID);
            if (match == null) {
                event.setCancelled(true);
                return;
            }
            DuelKit kit = this.plugin.getKitManager().getAdminKit(match.getKitName());
            if (kit != null && kit.isAllowBuilding()) {
                return;
            }
            if (match.getArena().isAllowBlockBreak()) {
                return;
            }
            event.setCancelled(true);
            return;
        }
        if (ffaManager != null && ffaManager.isInFFA(playerUUID)) {
            DuelKit kit;
            FFAArenaInstance arenaInstance;
            String arenaName = ffaManager.getPlayerArena(playerUUID);
            if (arenaName != null && (arenaInstance = ffaManager.getArena(arenaName)) != null && (kit = this.plugin.getKitManager().getAdminKit(arenaInstance.getKitName())) != null && kit.isAllowBuilding()) {
                return;
            }
            boolean globalAllow = this.plugin.getConfig().getBoolean("ffa.allow-block-break", false);
            if (!globalAllow) {
                event.setCancelled(true);
            }
            return;
        }
        if (!player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location explosionLoc = event.getLocation();
        if (this.plugin.getArenaManager() != null) {
            for (DuelArena arena : this.plugin.getArenaManager().getAllDuelArenas()) {
                if (!arena.hasBounds() || !arena.isWithinBounds(explosionLoc)) continue;
                DuelManager duelManager = this.plugin.getDuelManager();
                if (duelManager != null) {
                    for (DuelMatch match : duelManager.getActiveMatches()) {
                        if (!match.getArena().getName().equalsIgnoreCase(arena.getName())) continue;
                        DuelKit kit = this.plugin.getKitManager().getAdminKit(match.getKitName());
                        if (kit != null && kit.isAllowBuilding()) {
                            this.plugin.debug("[BlockProtection] Explosion in duel arena - kit allows building, allowing block damage");
                            return;
                        }
                        if (arena.isAllowBlockBreak()) {
                            this.plugin.debug("[BlockProtection] Explosion in duel arena - arena allows breaking, allowing block damage");
                            return;
                        }
                        if (!this.allowExplosionDamageInArenas()) {
                            event.blockList().clear();
                        }
                        return;
                    }
                }
                if (!this.allowExplosionDamageInArenas()) {
                    event.blockList().clear();
                }
                return;
            }
        }
        if (this.plugin.getFFAManager() != null) {
            for (FFAArenaInstance ffaArena : this.plugin.getFFAManager().getAllArenas()) {
                DuelArena arena = ffaArena.getArena();
                if (!arena.hasBounds() || !arena.isWithinBounds(explosionLoc)) continue;
                DuelKit kit = this.plugin.getKitManager().getAdminKit(ffaArena.getKitName());
                if (kit != null && kit.isAllowBuilding()) {
                    this.plugin.debug("[BlockProtection] Explosion in FFA arena - kit allows building, allowing block damage");
                    return;
                }
                event.blockList().clear();
                return;
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Entity entity = event.getRemover();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (!player.hasPermission("ultimateduels.admin.build")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Material type = block.getType();
        Player player = event.getPlayer();
        if (this.isInteractable(type)) {
            return;
        }
        if (this.isContainer(type)) {
            DuelManager duelManager = this.plugin.getDuelManager();
            FFAManager ffaManager = this.plugin.getFFAManager();
            UUID playerUUID = player.getUniqueId();
            if (duelManager != null && ffaManager != null && (duelManager.isInMatch(playerUUID) || ffaManager.isInFFA(playerUUID) || duelManager.isSpectating(playerUUID))) {
                event.setCancelled(true);
            }
        }
    }

    private boolean allowExplosionDamageInArenas() {
        return this.plugin.getConfig().getBoolean("arenas.allow-explosion-damage", false);
    }

    private boolean isInLobby(Player player) {
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null || ffaManager == null) {
            return true;
        }
        return !duelManager.isInMatch(playerUUID) && !ffaManager.isInFFA(playerUUID) && !duelManager.isSpectating(playerUUID);
    }

    private boolean isInteractable(Material material) {
        String name = material.name();
        return name.contains("BUTTON") || name.contains("LEVER") || name.contains("DOOR") || name.contains("GATE") || name.contains("PRESSURE_PLATE") || name.contains("TRIPWIRE");
    }

    private boolean isContainer(Material material) {
        return switch (material) {
            case Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.HOPPER, Material.DROPPER, Material.DISPENSER, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.BREWING_STAND, Material.ANVIL, Material.ENCHANTING_TABLE, Material.CRAFTING_TABLE, Material.SMITHING_TABLE, Material.CARTOGRAPHY_TABLE, Material.LOOM, Material.STONECUTTER, Material.GRINDSTONE -> true;
            default -> material.name().contains("SHULKER_BOX");
        };
    }

    public void clearAllTrackedBlocks() {
    }
}

