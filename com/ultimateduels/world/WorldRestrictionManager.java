/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.World
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.world;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.config.LanguageManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldRestrictionManager {
    private final UltimateDuels plugin;
    private RestrictionMode mode;
    private final Set<String> blacklistedWorlds;

    public WorldRestrictionManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.blacklistedWorlds = new HashSet<String>();
        this.loadConfiguration();
        plugin.getLogger().info("[WorldRestrictionManager] Initialized - Mode: " + String.valueOf((Object)this.mode));
    }

    public void loadConfiguration() {
        FileConfiguration config = this.plugin.getConfig();
        String modeStr = config.getString("world-restrictions.mode", "DISABLED");
        try {
            this.mode = RestrictionMode.valueOf(modeStr.toUpperCase());
            if (this.mode == RestrictionMode.WHITELIST) {
                this.plugin.getLogger().warning("WHITELIST mode is no longer supported. Using DISABLED.");
                this.mode = RestrictionMode.DISABLED;
            }
        }
        catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("Invalid world restriction mode: " + modeStr + ". Using DISABLED.");
            this.mode = RestrictionMode.DISABLED;
        }
        this.blacklistedWorlds.clear();
        List blacklist = config.getStringList("world-restrictions.blacklist");
        blacklist.forEach(world -> this.blacklistedWorlds.add(world.toLowerCase()));
        this.plugin.getLogger().info("[WorldRestrictionManager] Mode: " + String.valueOf((Object)this.mode) + " | Blacklisted worlds: " + this.blacklistedWorlds.size());
        if (!this.blacklistedWorlds.isEmpty()) {
            this.plugin.getLogger().info("[WorldRestrictionManager] Blacklisted: " + String.join((CharSequence)", ", this.blacklistedWorlds));
        }
    }

    public boolean isWorldAllowed(@Nonnull World world) {
        return this.isWorldAllowed(world.getName());
    }

    public boolean isWorldAllowed(@Nonnull String worldName) {
        if (this.mode == RestrictionMode.DISABLED) {
            return true;
        }
        String lowerName = worldName.toLowerCase();
        return !this.blacklistedWorlds.contains(lowerName);
    }

    public boolean isPluginAllowedInWorld(@Nonnull String worldName) {
        return this.isWorldAllowed(worldName);
    }

    public boolean isPluginAllowedInWorld(@Nonnull World world) {
        return this.isWorldAllowed(world);
    }

    public boolean isPlayerInAllowedWorld(@Nonnull Player player) {
        return this.isWorldAllowed(player.getWorld());
    }

    public boolean canUsePlugin(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canUseLobbyItems(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canStartDuel(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canJoinQueue(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canJoinFFA(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canUseCommands(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canShowScoreboard(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public boolean canSpectate(@Nonnull Player player) {
        return this.isPlayerInAllowedWorld(player);
    }

    public void sendBlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.blocked-world"));
    }

    public void sendCommandBlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.command-blocked"));
    }

    public void sendLobbyItemsBlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.lobby-items-blocked"));
    }

    public void sendQueueBlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.queue-blocked"));
    }

    public void sendDuelBlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.duel-blocked"));
    }

    public void sendFFABlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.ffa-blocked"));
    }

    public void sendSpectateBlockedMessage(@Nonnull Player player) {
        LanguageManager lang = this.plugin.getLanguageManager();
        player.sendMessage(lang.getComponent(player, "world-restriction.spectate-blocked"));
    }

    public void addToBlacklist(@Nonnull String worldName) {
        this.blacklistedWorlds.add(worldName.toLowerCase());
        this.saveToConfig();
    }

    public void removeFromBlacklist(@Nonnull String worldName) {
        this.blacklistedWorlds.remove(worldName.toLowerCase());
        this.saveToConfig();
    }

    public void setMode(@Nonnull RestrictionMode mode) {
        if (mode == RestrictionMode.WHITELIST) {
            this.plugin.getLogger().warning("WHITELIST mode is no longer supported. Use BLACKLIST instead.");
            return;
        }
        this.mode = mode;
        this.plugin.getConfig().set("world-restrictions.mode", (Object)mode.name());
        this.plugin.saveConfig();
        this.plugin.getLogger().info("[WorldRestrictionManager] Mode changed to: " + String.valueOf((Object)mode));
    }

    private void saveToConfig() {
        this.plugin.getConfig().set("world-restrictions.blacklist", List.copyOf(this.blacklistedWorlds));
        this.plugin.saveConfig();
    }

    public RestrictionMode getMode() {
        return this.mode;
    }

    public Set<String> getBlacklistedWorlds() {
        return new HashSet<String>(this.blacklistedWorlds);
    }

    public boolean isBlacklisted(@Nonnull String worldName) {
        return this.blacklistedWorlds.contains(worldName.toLowerCase());
    }

    public void reload() {
        this.loadConfiguration();
        this.plugin.getLogger().info("[WorldRestrictionManager] Reloaded successfully!");
    }

    public static enum RestrictionMode {
        DISABLED,
        BLACKLIST,
        WHITELIST;

    }
}

