/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.ultimateduels.visuals;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.tasks.HealthDisplayUpdateTask;
import com.ultimateduels.visuals.HealthDisplay;
import com.ultimateduels.visuals.HealthDisplayConfig;
import com.ultimateduels.visuals.HealthDisplayType;
import com.ultimateduels.visuals.HealthPacketSender;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class HealthDisplayManager {
    private final UltimateDuels plugin;
    private final HealthDisplayConfig config;
    private final Map<UUID, HealthDisplay> activeDisplays = new ConcurrentHashMap<UUID, HealthDisplay>();
    private BukkitTask updateTask;
    private DuelManager duelManager;
    private FFAManager ffaManager;

    public HealthDisplayManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.config = this.loadConfig();
    }

    public void initialize() {
        this.duelManager = this.plugin.getDuelManager();
        this.ffaManager = this.plugin.getFFAManager();
        if (!this.config.isEnabled()) {
            this.plugin.getLogger().info("[HealthDisplayManager] Disabled in config");
            return;
        }
        if (!HealthPacketSender.isAvailable()) {
            this.plugin.getLogger().warning("[HealthDisplayManager] HealthPacketSender unavailable \u2014 health displays disabled. Check startup logs for NMS errors.");
            return;
        }
        this.startUpdateTask();
        this.plugin.getLogger().info("[HealthDisplayManager] Initialized (packet-based, no server entities)");
        this.plugin.getLogger().info("  Show in Duels:      " + this.config.isShowInDuels());
        this.plugin.getLogger().info("  Show in FFA:        " + this.config.isShowInFFA());
        this.plugin.getLogger().info("  Show to Spectators: " + this.config.isShowToSpectators());
        this.plugin.getLogger().info("  Update interval:    " + this.config.getUpdateIntervalTicks() + " ticks");
        this.plugin.getLogger().info("  Format:             " + this.config.getFormat());
    }

    public void shutdown() {
        this.cancelUpdateTask();
        for (HealthDisplay display : this.activeDisplays.values()) {
            display.remove();
        }
        this.activeDisplays.clear();
        this.plugin.getLogger().info("[HealthDisplayManager] Shutdown complete");
    }

    public void reload() {
        this.cancelUpdateTask();
        for (HealthDisplay display : this.activeDisplays.values()) {
            display.remove();
        }
        this.activeDisplays.clear();
        this.duelManager = this.plugin.getDuelManager();
        this.ffaManager = this.plugin.getFFAManager();
        if (this.config.isEnabled() && HealthPacketSender.isAvailable()) {
            this.startUpdateTask();
            this.plugin.getLogger().info("[HealthDisplayManager] Reloaded");
        }
    }

    public void createDisplay(@Nonnull Player player) {
        if (!this.config.isEnabled() || !this.config.isShowInDuels()) {
            return;
        }
        if (!HealthPacketSender.isAvailable()) {
            return;
        }
        this.spawnDisplay(player);
    }

    public void createDisplayForFFA(@Nonnull Player player) {
        if (!this.config.isEnabled() || !this.config.isShowInFFA()) {
            return;
        }
        if (!HealthPacketSender.isAvailable()) {
            return;
        }
        this.spawnDisplay(player);
    }

    public void removeDisplay(@Nonnull Player player) {
        this.removeDisplay(player.getUniqueId());
    }

    public void removeDisplay(@Nonnull UUID uuid) {
        HealthDisplay display = this.activeDisplays.remove(uuid);
        if (display != null) {
            display.remove();
        }
    }

    public void removeDisplayForFFA(@Nonnull Player player) {
        this.removeDisplay(player.getUniqueId());
    }

    public void updateHealth(@Nonnull Player player) {
        HealthDisplay display = this.activeDisplays.get(player.getUniqueId());
        if (display != null && display.isActive()) {
            display.updateHealth(player.getHealth(), player.getMaxHealth());
        }
    }

    public void updateHealth(@Nonnull Player player, double currentHealth, double maxHealth) {
        HealthDisplay display = this.activeDisplays.get(player.getUniqueId());
        if (display != null && display.isActive()) {
            display.updateHealth(currentHealth, maxHealth);
        }
    }

    public void createDisplaysForMatch(@Nonnull DuelMatch match) {
        if (!this.config.isEnabled() || !this.config.isShowInDuels()) {
            return;
        }
        if (!HealthPacketSender.isAvailable()) {
            return;
        }
        for (DuelParticipant p : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)p.getUuid());
            if (player == null || !player.isOnline()) continue;
            this.spawnDisplay(player);
        }
    }

    public void removeDisplaysForMatch(@Nonnull DuelMatch match) {
        for (DuelParticipant p : match.getAllParticipants()) {
            this.removeDisplay(p.getUuid());
        }
    }

    public void handleSpectatorJoin(@Nonnull Player spectator, @Nonnull DuelMatch match) {
        if (!this.config.isShowToSpectators() || !this.config.isShowInDuels()) {
            return;
        }
        if (!HealthPacketSender.isAvailable()) {
            return;
        }
        for (DuelParticipant p : match.getAllParticipants()) {
            HealthDisplay display = this.activeDisplays.get(p.getUuid());
            if (display == null) continue;
            display.addViewer(spectator);
        }
    }

    public void handleSpectatorLeave(@Nonnull Player spectator) {
        for (HealthDisplay display : this.activeDisplays.values()) {
            display.removeViewer(spectator);
        }
    }

    public void updateAllPositions() {
        if (!this.config.isEnabled() || !HealthPacketSender.isAvailable()) {
            if (!this.activeDisplays.isEmpty()) {
                for (HealthDisplay d : this.activeDisplays.values()) {
                    d.remove();
                }
                this.activeDisplays.clear();
            }
            return;
        }
        Iterator<Map.Entry<UUID, HealthDisplay>> it = this.activeDisplays.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, HealthDisplay> entry = it.next();
            UUID uuid = entry.getKey();
            HealthDisplay display = entry.getValue();
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) {
                display.remove();
                it.remove();
                continue;
            }
            if (!this.isPlayerInCombat(player)) {
                display.remove();
                it.remove();
                continue;
            }
            if (!this.isContextAllowed(player)) {
                display.remove();
                it.remove();
                continue;
            }
            if (display.shouldFade()) {
                display.remove();
                it.remove();
                continue;
            }
            display.updateHealth(player.getHealth(), player.getMaxHealth());
            display.updatePosition();
            this.syncViewers(player, display);
        }
    }

    private void spawnDisplay(@Nonnull Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (!HealthPacketSender.isAvailable()) {
            return;
        }
        HealthDisplay old = this.activeDisplays.remove(player.getUniqueId());
        if (old != null) {
            old.remove();
        }
        HealthDisplay display = new HealthDisplay(player, this.config);
        display.spawn();
        this.activeDisplays.put(player.getUniqueId(), display);
        this.syncViewers(player, display);
    }

    private void syncViewers(@Nonnull Player player, @Nonnull HealthDisplay display) {
        FFAArenaInstance arena;
        String arenaName;
        DuelMatch match;
        UUID uuid = player.getUniqueId();
        HashSet<Player> viewers = new HashSet<Player>();
        if (this.duelManager != null && this.duelManager.isInMatch(uuid) && this.config.isShowInDuels() && (match = this.duelManager.getMatch(uuid)) != null) {
            for (DuelParticipant other : match.getAllParticipants()) {
                Player op;
                if (other.getUuid().equals(uuid) || (op = Bukkit.getPlayer((UUID)other.getUuid())) == null || !op.isOnline()) continue;
                viewers.add(op);
            }
            if (this.config.isShowToSpectators()) {
                for (UUID specId : this.duelManager.getMatchSpectators(match.getMatchId())) {
                    Player spec = Bukkit.getPlayer((UUID)specId);
                    if (spec == null || !spec.isOnline()) continue;
                    viewers.add(spec);
                }
            }
        }
        if (this.ffaManager != null && this.ffaManager.isInFFA(uuid) && this.config.isShowInFFA() && (arenaName = this.ffaManager.getPlayerArena(uuid)) != null && (arena = this.ffaManager.getFFAArena(arenaName)) != null) {
            for (UUID otherId : arena.getPlayers()) {
                Player op;
                if (otherId.equals(uuid) || (op = Bukkit.getPlayer((UUID)otherId)) == null || !op.isOnline()) continue;
                viewers.add(op);
            }
        }
        display.updateViewers(viewers);
    }

    private boolean isPlayerInCombat(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        return this.duelManager != null && this.duelManager.isInMatch(uuid) || this.ffaManager != null && this.ffaManager.isInFFA(uuid);
    }

    private boolean isContextAllowed(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        if (this.duelManager != null && this.duelManager.isInMatch(uuid)) {
            return this.config.isShowInDuels();
        }
        if (this.ffaManager != null && this.ffaManager.isInFFA(uuid)) {
            return this.config.isShowInFFA();
        }
        return false;
    }

    private void startUpdateTask() {
        this.cancelUpdateTask();
        int interval = Math.max(1, this.config.getUpdateIntervalTicks());
        this.updateTask = new HealthDisplayUpdateTask(this).runTaskTimer((Plugin)this.plugin, 1L, interval);
    }

    private void cancelUpdateTask() {
        if (this.updateTask != null) {
            try {
                this.updateTask.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
            this.updateTask = null;
        }
    }

    private HealthDisplayConfig loadConfig() {
        HealthDisplayType type;
        FileConfiguration cfg = this.plugin.getConfig();
        String typeStr = cfg.getString("health-display.type", "TEXT_DISPLAY");
        try {
            type = HealthDisplayType.valueOf(typeStr.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            type = HealthDisplayType.TEXT_DISPLAY;
        }
        return HealthDisplayConfig.builder().displayType(type).enabled(cfg.getBoolean("health-display.enabled", true)).showInDuels(cfg.getBoolean("health-display.show-in-duels", true)).showInFFA(cfg.getBoolean("health-display.show-in-ffa", true)).showToSpectators(cfg.getBoolean("health-display.show-to-spectators", true)).format(cfg.getString("health-display.format", "\u2764 {current}/{max}")).heightOffset(cfg.getDouble("health-display.height-offset", 2.3)).viewRange(cfg.getDouble("health-display.view-range", 32.0)).scale((float)cfg.getDouble("health-display.scale", 1.0)).smoothInterpolation(cfg.getBoolean("health-display.smooth-interpolation", true)).interpolationDuration(cfg.getInt("health-display.interpolation-duration", 3)).updateIntervalTicks(cfg.getInt("health-display.update-interval-ticks", 1)).build();
    }

    public boolean hasDisplay(@Nonnull Player player) {
        return this.activeDisplays.containsKey(player.getUniqueId());
    }

    @Nullable
    public HealthDisplay getDisplay(@Nonnull Player player) {
        return this.activeDisplays.get(player.getUniqueId());
    }

    @Nonnull
    public Collection<HealthDisplay> getAllDisplays() {
        return Collections.unmodifiableCollection(this.activeDisplays.values());
    }

    @Nonnull
    public HealthDisplayConfig getConfig() {
        return this.config;
    }

    @Nonnull
    public Map<String, Object> getStatistics() {
        LinkedHashMap<String, Object> stats = new LinkedHashMap<String, Object>();
        stats.put("activeDisplays", this.activeDisplays.size());
        stats.put("displayType", "PACKET_BASED");
        stats.put("packetSender", HealthPacketSender.isAvailable() ? "OK" : "UNAVAILABLE");
        stats.put("enabled", this.config.isEnabled());
        stats.put("showInDuels", this.config.isShowInDuels());
        stats.put("showInFFA", this.config.isShowInFFA());
        stats.put("showToSpectators", this.config.isShowToSpectators());
        stats.put("format", this.config.getFormat());
        stats.put("updateInterval", this.config.getUpdateIntervalTicks() + " ticks");
        return stats;
    }
}

