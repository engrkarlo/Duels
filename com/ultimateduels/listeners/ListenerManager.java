/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.listeners.BlockProtectionListener;
import com.ultimateduels.listeners.ChatListener;
import com.ultimateduels.listeners.FoodLevelListener;
import com.ultimateduels.listeners.InventoryClickListener;
import com.ultimateduels.listeners.PlayerDamageListener;
import com.ultimateduels.listeners.PlayerDeathListener;
import com.ultimateduels.listeners.PlayerDropListener;
import com.ultimateduels.listeners.PlayerInteractListener;
import com.ultimateduels.listeners.PlayerJoinQuitListener;
import com.ultimateduels.listeners.PlayerMoveListener;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {
    private final UltimateDuels plugin;
    private final List<Listener> listeners;

    public ListenerManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.listeners = new ArrayList<Listener>();
    }

    public void registerListeners() {
        PluginManager pm = this.plugin.getServer().getPluginManager();
        this.registerListener(pm, new PlayerJoinQuitListener(this.plugin));
        this.registerListener(pm, new PlayerDeathListener(this.plugin));
        this.registerListener(pm, new PlayerDamageListener(this.plugin));
        this.registerListener(pm, new PlayerMoveListener(this.plugin));
        this.registerListener(pm, new PlayerInteractListener(this.plugin));
        this.registerListener(pm, new InventoryClickListener(this.plugin));
        this.registerListener(pm, new PlayerDropListener(this.plugin));
        this.registerListener(pm, new FoodLevelListener(this.plugin));
        this.registerListener(pm, new BlockProtectionListener(this.plugin));
        this.registerListener(pm, new ChatListener(this.plugin));
        this.registerListener(pm, new CommandBlockListener(this.plugin));
        this.plugin.getLogger().info("Registered " + this.listeners.size() + " listeners!");
    }

    private void registerListener(PluginManager pm, Listener listener) {
        pm.registerEvents(listener, (Plugin)this.plugin);
        this.listeners.add(listener);
    }

    public List<Listener> getListeners() {
        return new ArrayList<Listener>(this.listeners);
    }

    public <T extends Listener> T getListener(Class<T> clazz) {
        for (Listener listener : this.listeners) {
            if (!clazz.isInstance(listener)) continue;
            return (T)listener;
        }
        return null;
    }
}

