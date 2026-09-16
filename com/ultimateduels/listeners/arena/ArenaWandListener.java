/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.listeners.arena;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ArenaWandListener
implements Listener {
    private final UltimateDuels plugin;
    private final NamespacedKey wandKey;
    private final NamespacedKey arenaKey;

    public ArenaWandListener(@Nonnull UltimateDuels plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey((Plugin)plugin, "arena_wand");
        this.arenaKey = new NamespacedKey((Plugin)plugin, "arena_name");
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOLDEN_AXE) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(this.wandKey, PersistentDataType.BYTE)) {
            return;
        }
        String arenaName = (String)container.get(this.arenaKey, PersistentDataType.STRING);
        if (arenaName == null) {
            return;
        }
        if (!player.hasPermission("ultimateduels.admin.arena")) {
            return;
        }
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Arena no longer exists: " + arenaName);
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        event.setCancelled(true);
        Location location = clickedBlock.getLocation();
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            this.plugin.getArenaManager().setCorner1(arenaName, location);
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u2713 " + String.valueOf(ChatColor.YELLOW) + "Corner 1" + String.valueOf(ChatColor.GREEN) + " set at " + String.valueOf(ChatColor.WHITE) + this.formatLocation(location));
            this.showRegionInfo(player, arenaName);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            this.plugin.getArenaManager().setCorner2(arenaName, location);
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u2713 " + String.valueOf(ChatColor.YELLOW) + "Corner 2" + String.valueOf(ChatColor.GREEN) + " set at " + String.valueOf(ChatColor.WHITE) + this.formatLocation(location));
            this.showRegionInfo(player, arenaName);
        }
    }

    private void showRegionInfo(Player player, String arenaName) {
        DuelArena arena = this.plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            return;
        }
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        if (corner1 != null && corner2 != null) {
            int sizeX = Math.abs(corner2.getBlockX() - corner1.getBlockX()) + 1;
            int sizeY = Math.abs(corner2.getBlockY() - corner1.getBlockY()) + 1;
            int sizeZ = Math.abs(corner2.getBlockZ() - corner1.getBlockZ()) + 1;
            int volume = sizeX * sizeY * sizeZ;
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Region: " + String.valueOf(ChatColor.WHITE) + sizeX + " x " + sizeY + " x " + sizeZ + String.valueOf(ChatColor.GRAY) + " (" + String.valueOf(ChatColor.WHITE) + this.formatNumber(volume) + String.valueOf(ChatColor.GRAY) + " blocks)");
            if (volume > 500000) {
                player.sendMessage(String.valueOf(ChatColor.RED) + "\u26a0 Warning: Large region may cause lag!");
            }
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Ready to save with: " + String.valueOf(ChatColor.YELLOW) + "/arena save " + arenaName);
        } else if (corner1 == null) {
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Left-click to set Corner 1");
        } else {
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Right-click to set Corner 2");
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", (double)number / 1000000.0);
        }
        if (number >= 1000) {
            return String.format("%.1fK", (double)number / 1000.0);
        }
        return String.valueOf(number);
    }

    @Nonnull
    public static ItemStack createWand(@Nonnull UltimateDuels plugin, @Nonnull String arenaName) {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(String.valueOf(ChatColor.GOLD) + "\u00a7l\u2692 " + String.valueOf(ChatColor.YELLOW) + "Arena Wand " + String.valueOf(ChatColor.GRAY) + "(" + arenaName + ")");
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add("");
            lore.add(String.valueOf(ChatColor.YELLOW) + "Left-click" + String.valueOf(ChatColor.GRAY) + " \u2192 Set Corner 1");
            lore.add(String.valueOf(ChatColor.YELLOW) + "Right-click" + String.valueOf(ChatColor.GRAY) + " \u2192 Set Corner 2");
            lore.add("");
            lore.add(String.valueOf(ChatColor.GRAY) + "Arena: " + String.valueOf(ChatColor.WHITE) + arenaName);
            lore.add("");
            lore.add(String.valueOf(ChatColor.DARK_GRAY) + "Used for schematic region selection");
            meta.setLore(lore);
            meta.setUnbreakable(true);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey wandKey = new NamespacedKey((Plugin)plugin, "arena_wand");
            NamespacedKey arenaKey = new NamespacedKey((Plugin)plugin, "arena_name");
            container.set(wandKey, PersistentDataType.BYTE, (Object)1);
            container.set(arenaKey, PersistentDataType.STRING, (Object)arenaName);
            wand.setItemMeta(meta);
        }
        return wand;
    }
}

