/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.settings;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.settings.PlayerSettings;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SettingsManager {
    private final UltimateDuels plugin;
    private final Map<UUID, PlayerSettings> settingsCache;
    private final File settingsFolder;

    public SettingsManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.settingsCache = new ConcurrentHashMap<UUID, PlayerSettings>();
        this.settingsFolder = new File(plugin.getDataFolder(), "player-settings");
        if (!this.settingsFolder.exists()) {
            this.settingsFolder.mkdirs();
        }
        plugin.getLogger().info("[SettingsManager] Initialized successfully!");
    }

    @Nonnull
    public PlayerSettings getSettings(@Nonnull UUID uuid) {
        PlayerSettings cached = this.settingsCache.get(uuid);
        if (cached != null) {
            return cached;
        }
        PlayerSettings loaded = this.loadSettings(uuid);
        this.settingsCache.put(uuid, loaded);
        return loaded;
    }

    @Nonnull
    public PlayerSettings getSettings(@Nonnull Player player) {
        return this.getSettings(player.getUniqueId());
    }

    @Nonnull
    private PlayerSettings loadSettings(@Nonnull UUID uuid) {
        File file = this.getSettingsFile(uuid);
        if (!file.exists()) {
            return new PlayerSettings(uuid);
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
            return PlayerSettings.fromConfig(uuid, (ConfigurationSection)config);
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load settings for " + String.valueOf(uuid), e);
            return new PlayerSettings(uuid);
        }
    }

    public void saveSettings(@Nonnull UUID uuid) {
        PlayerSettings settings = this.settingsCache.get(uuid);
        if (settings == null) {
            return;
        }
        File file = this.getSettingsFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        settings.toConfig((ConfigurationSection)config);
        try {
            config.save(file);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save settings for " + String.valueOf(uuid), e);
        }
    }

    @Nonnull
    private File getSettingsFile(@Nonnull UUID uuid) {
        return new File(this.settingsFolder, uuid.toString() + ".yml");
    }

    public boolean toggleScoreboard(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleScoreboard();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleDuelRequests(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleDuelRequests();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean togglePartyInvites(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.togglePartyInvites();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleDeathMessages(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleDeathMessages();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleSpectatorNotify(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleSpectatorNotify();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleSounds(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleSounds();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleAutoRequeue(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleAutoRequeue();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleShowPing(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleShowPing();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean toggleAllowSpectators(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        boolean newValue = settings.toggleAllowSpectators();
        this.saveSettings(player.getUniqueId());
        return newValue;
    }

    public boolean hasScoreboardEnabled(@Nonnull Player player) {
        return this.getSettings(player).isScoreboardEnabled();
    }

    public boolean canReceiveDuelRequests(@Nonnull Player player) {
        return this.getSettings(player).isDuelRequestsEnabled();
    }

    public boolean canReceivePartyInvites(@Nonnull Player player) {
        return this.getSettings(player).isPartyInvitesEnabled();
    }

    public boolean showDeathMessages(@Nonnull Player player) {
        return this.getSettings(player).isDeathMessagesEnabled();
    }

    public boolean showSpectatorJoinMessages(@Nonnull Player player) {
        return this.getSettings(player).isSpectatorNotifyEnabled();
    }

    public boolean hasSoundsEnabled(@Nonnull Player player) {
        return this.getSettings(player).isSoundsEnabled();
    }

    public boolean hasAutoRequeue(@Nonnull Player player) {
        return this.getSettings(player).isAutoRequeueEnabled();
    }

    public boolean showPing(@Nonnull Player player) {
        return this.getSettings(player).isShowPingEnabled();
    }

    public boolean allowsSpectators(@Nonnull Player player) {
        return this.getSettings(player).isAllowSpectatorsEnabled();
    }

    public void setScoreboardEnabled(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setScoreboardEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setDuelRequestsEnabled(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setDuelRequestsEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setPartyInvitesEnabled(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setPartyInvitesEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setDeathMessagesEnabled(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setDeathMessagesEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setSpectatorNotifyEnabled(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setSpectatorNotifyEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setSoundsEnabled(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setSoundsEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setAutoRequeue(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setAutoRequeueEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setShowPing(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setShowPingEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void setAllowSpectators(@Nonnull Player player, boolean enabled) {
        this.getSettings(player).setAllowSpectatorsEnabled(enabled);
        this.saveSettings(player.getUniqueId());
    }

    public void resetToDefaults(@Nonnull Player player) {
        PlayerSettings settings = this.getSettings(player.getUniqueId());
        settings.resetToDefaults();
        this.saveSettings(player.getUniqueId());
    }

    public void saveAll() {
        for (UUID uuid : this.settingsCache.keySet()) {
            this.saveSettings(uuid);
        }
    }

    public void unloadSettings(@Nonnull UUID uuid) {
        PlayerSettings settings = this.settingsCache.get(uuid);
        if (settings != null) {
            this.saveSettings(uuid);
            this.settingsCache.remove(uuid);
        }
    }

    public void reload() {
        this.saveAll();
        this.settingsCache.clear();
        this.plugin.getLogger().info("[SettingsManager] Reloaded!");
    }

    public void shutdown() {
        this.saveAll();
        this.settingsCache.clear();
        this.plugin.getLogger().info("[SettingsManager] Shutdown complete!");
    }
}

