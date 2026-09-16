/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 */
package com.ultimateduels.gui;

import com.ultimateduels.gui.AbstractGUI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener
implements Listener {
    private static final Set<String> processingClicks = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> lastClickTime = new ConcurrentHashMap<String, Long>();
    private static final long COOLDOWN_MS = 200L;
    private static final Map<String, Long> specialSlotCooldowns = new ConcurrentHashMap<String, Long>();
    private static final long SPECIAL_SLOT_COOLDOWN_MS = 300L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder inventoryHolder = topInventory.getHolder();
        if (!(inventoryHolder instanceof AbstractGUI)) {
            return;
        }
        AbstractGUI gui = (AbstractGUI)inventoryHolder;
        if (event.getClickedInventory() == null) {
            event.setCancelled(true);
            return;
        }
        if (gui.allowsItemMovement()) {
            this.handleItemMovementClick(event, player, gui, topInventory);
            return;
        }
        event.setCancelled(true);
        if (!event.getClickedInventory().equals((Object)topInventory)) {
            if (gui.allowsPlayerInventoryInteraction()) {
                gui.handlePlayerInventoryClick(event);
            }
            return;
        }
        ClickType clickType = event.getClick();
        if (clickType == ClickType.DOUBLE_CLICK || clickType == ClickType.CREATIVE || clickType == ClickType.UNKNOWN) {
            return;
        }
        String lockKey = String.valueOf(player.getUniqueId()) + ":" + event.getSlot();
        long now = System.currentTimeMillis();
        Long lastClick = lastClickTime.get(lockKey);
        if (lastClick != null && now - lastClick < 200L) {
            return;
        }
        if (!processingClicks.add(lockKey)) {
            return;
        }
        try {
            lastClickTime.put(lockKey, now);
            gui.handleClick(player, event.getSlot(), event.getClick(), event.getCurrentItem(), event);
        }
        finally {
            processingClicks.remove(lockKey);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleItemMovementClick(InventoryClickEvent event, Player player, AbstractGUI gui, Inventory topInventory) {
        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        if (!event.getClickedInventory().equals((Object)topInventory)) {
            event.setCancelled(true);
            gui.handlePlayerInventoryClick(event);
            return;
        }
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            if (slot >= 18) {
                event.setCancelled(true);
                gui.handleClick(player, slot, clickType, event.getCurrentItem(), event);
                return;
            }
            boolean shouldCancel = gui.handleClick(player, slot, clickType, event.getCurrentItem(), event);
            event.setCancelled(shouldCancel);
            return;
        }
        if (clickType == ClickType.NUMBER_KEY) {
            event.setCancelled(true);
            return;
        }
        if (clickType == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }
        if (clickType == ClickType.CREATIVE || clickType == ClickType.UNKNOWN) {
            event.setCancelled(true);
            return;
        }
        if (slot < 18) {
            String lockKey = String.valueOf(player.getUniqueId()) + ":special:" + slot;
            long now = System.currentTimeMillis();
            Long lastClick = specialSlotCooldowns.get(lockKey);
            if (lastClick != null && now - lastClick < 300L) {
                event.setCancelled(true);
                return;
            }
            if (!processingClicks.add(lockKey)) {
                event.setCancelled(true);
                return;
            }
            try {
                specialSlotCooldowns.put(lockKey, now);
                boolean shouldCancel = gui.handleClick(player, slot, event.getClick(), event.getCurrentItem(), event);
                event.setCancelled(shouldCancel);
            }
            finally {
                processingClicks.remove(lockKey);
            }
            return;
        }
        boolean shouldCancel = gui.handleClick(player, slot, event.getClick(), event.getCurrentItem(), event);
        event.setCancelled(shouldCancel);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Inventory topInventory = event.getView().getTopInventory();
        Object object = topInventory.getHolder();
        if (!(object instanceof AbstractGUI)) {
            return;
        }
        AbstractGUI gui = (AbstractGUI)object;
        if (gui.allowsItemMovement()) {
            object = event.getRawSlots().iterator();
            while (object.hasNext()) {
                int rawSlot = (Integer)object.next();
                if (rawSlot < 18 && rawSlot < topInventory.getSize()) {
                    event.setCancelled(true);
                    return;
                }
                if (rawSlot < topInventory.getSize()) continue;
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (gui.allowsDragging()) {
            return;
        }
        object = event.getRawSlots().iterator();
        while (object.hasNext()) {
            int rawSlot = (Integer)object.next();
            if (rawSlot >= topInventory.getSize()) continue;
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder inventoryHolder = topInventory.getHolder();
        if (inventoryHolder instanceof AbstractGUI) {
            AbstractGUI gui = (AbstractGUI)inventoryHolder;
            gui.onClose(player);
        }
        String prefix = String.valueOf(player.getUniqueId()) + ":";
        lastClickTime.keySet().removeIf(key -> key.startsWith(prefix));
        specialSlotCooldowns.keySet().removeIf(key -> key.startsWith(prefix));
        processingClicks.removeIf(key -> key.startsWith(prefix));
    }

    public static void cleanupCooldowns() {
        long now = System.currentTimeMillis();
        lastClickTime.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 60000L);
        specialSlotCooldowns.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 60000L);
    }
}

