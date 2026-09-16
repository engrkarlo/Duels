/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.ultimateduels.cooldown;

import com.ultimateduels.UltimateDuels;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class CooldownManager {
    private final UltimateDuels plugin;
    private final Map<String, Map<String, Long>> cooldowns;
    private final Map<String, CooldownConfig> cooldownConfigs;
    private BukkitTask cleanupTask;
    public static final String DUEL_REQUEST = "duel_request";
    public static final String DUEL_ACCEPT = "duel_accept";
    public static final String QUEUE_JOIN = "queue_join";
    public static final String QUEUE_LEAVE = "queue_leave";
    public static final String PARTY_INVITE = "party_invite";
    public static final String PARTY_CREATE = "party_create";
    public static final String FFA_JOIN = "ffa_join";
    public static final String KIT_EDIT = "kit_edit";
    public static final String SPECTATE = "spectate";
    public static final String CHAT_MESSAGE = "chat_message";
    public static final String COMMAND_STATS = "command_stats";
    public static final String ENDER_PEARL = "ender_pearl";
    public static final String GOLDEN_APPLE = "golden_apple";
    public static final String TOTEM_USE = "totem_use";
    public static final String RESPAWN = "respawn";

    public CooldownManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<String, Map<String, Long>>();
        this.cooldownConfigs = new ConcurrentHashMap<String, CooldownConfig>();
        this.loadDefaultCooldowns();
        this.startCleanupTask();
        plugin.getLogger().info("\u00a7a[CooldownManager] Initialized successfully!");
    }

    private void loadDefaultCooldowns() {
        this.registerCooldown(DUEL_REQUEST, 3L, TimeUnit.SECONDS, "ultimateduels.bypass.duel_request");
        this.registerCooldown(DUEL_ACCEPT, 1L, TimeUnit.SECONDS, "ultimateduels.bypass.duel_accept");
        this.registerCooldown(QUEUE_JOIN, 2L, TimeUnit.SECONDS, "ultimateduels.bypass.queue_join");
        this.registerCooldown(QUEUE_LEAVE, 3L, TimeUnit.SECONDS, "ultimateduels.bypass.queue_leave");
        this.registerCooldown(PARTY_INVITE, 5L, TimeUnit.SECONDS, "ultimateduels.bypass.party_invite");
        this.registerCooldown(PARTY_CREATE, 10L, TimeUnit.SECONDS, "ultimateduels.bypass.party_create");
        this.registerCooldown(FFA_JOIN, 2L, TimeUnit.SECONDS, "ultimateduels.bypass.ffa_join");
        this.registerCooldown(KIT_EDIT, 1L, TimeUnit.SECONDS, "ultimateduels.bypass.kit_edit");
        this.registerCooldown(SPECTATE, 3L, TimeUnit.SECONDS, "ultimateduels.bypass.spectate");
        this.registerCooldown(CHAT_MESSAGE, 1L, TimeUnit.SECONDS, "ultimateduels.bypass.chat");
        this.registerCooldown(COMMAND_STATS, 5L, TimeUnit.SECONDS, "ultimateduels.bypass.stats");
        this.registerCooldown(ENDER_PEARL, 16L, TimeUnit.SECONDS, null);
        this.registerCooldown(GOLDEN_APPLE, 0L, TimeUnit.SECONDS, null);
        this.registerCooldown(TOTEM_USE, 0L, TimeUnit.SECONDS, null);
        this.registerCooldown(RESPAWN, 3L, TimeUnit.SECONDS, null);
    }

    private void startCleanupTask() {
        this.cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, this::cleanupExpiredCooldowns, 1200L, 1200L);
    }

    public void registerCooldown(@Nonnull String type, long duration, @Nonnull TimeUnit unit, @Nullable String bypassPermission) {
        this.cooldownConfigs.put(type.toLowerCase(), new CooldownConfig(type, unit.toMillis(duration), bypassPermission, false));
        this.cooldowns.putIfAbsent(type.toLowerCase(), new ConcurrentHashMap());
    }

    public void registerGlobalCooldown(@Nonnull String type, long duration, @Nonnull TimeUnit unit, @Nullable String bypassPermission) {
        this.cooldownConfigs.put(type.toLowerCase(), new CooldownConfig(type, unit.toMillis(duration), bypassPermission, true));
        this.cooldowns.putIfAbsent(type.toLowerCase(), new ConcurrentHashMap());
    }

    public void setCooldownDuration(@Nonnull String type, long duration, @Nonnull TimeUnit unit) {
        CooldownConfig config = this.cooldownConfigs.get(type.toLowerCase());
        if (config != null) {
            this.cooldownConfigs.put(type.toLowerCase(), new CooldownConfig(config.type(), unit.toMillis(duration), config.bypassPermission(), config.isGlobal()));
        }
    }

    public void setCooldown(@Nonnull UUID playerId, @Nonnull String type) {
        CooldownConfig config = this.cooldownConfigs.get(type.toLowerCase());
        if (config == null || config.duration() <= 0L) {
            return;
        }
        String key = config.isGlobal() ? "global" : playerId.toString();
        long expiry = System.currentTimeMillis() + config.duration();
        this.cooldowns.computeIfAbsent(type.toLowerCase(), k -> new ConcurrentHashMap()).put(key, expiry);
    }

    public void setCooldown(@Nonnull Player player, @Nonnull String type) {
        CooldownConfig config = this.cooldownConfigs.get(type.toLowerCase());
        if (config != null && config.bypassPermission() != null && player.hasPermission(config.bypassPermission())) {
            return;
        }
        this.setCooldown(player.getUniqueId(), type);
    }

    public void setCooldown(@Nonnull UUID playerId, @Nonnull String type, long duration, @Nonnull TimeUnit unit) {
        String key = playerId.toString();
        long expiry = System.currentTimeMillis() + unit.toMillis(duration);
        this.cooldowns.computeIfAbsent(type.toLowerCase(), k -> new ConcurrentHashMap()).put(key, expiry);
    }

    public void setCooldown(@Nonnull Player player, @Nonnull String type, long duration, @Nonnull TimeUnit unit) {
        this.setCooldown(player.getUniqueId(), type, duration, unit);
    }

    public boolean isOnCooldown(@Nonnull UUID playerId, @Nonnull String type) {
        CooldownConfig config = this.cooldownConfigs.get(type.toLowerCase());
        if (config == null) {
            return false;
        }
        String key = config.isGlobal() ? "global" : playerId.toString();
        Map<String, Long> typeCooldowns = this.cooldowns.get(type.toLowerCase());
        if (typeCooldowns == null) {
            return false;
        }
        Long expiry = typeCooldowns.get(key);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expiry) {
            typeCooldowns.remove(key);
            return false;
        }
        return true;
    }

    public boolean isOnCooldown(@Nonnull Player player, @Nonnull String type) {
        CooldownConfig config = this.cooldownConfigs.get(type.toLowerCase());
        if (config != null && config.bypassPermission() != null && player.hasPermission(config.bypassPermission())) {
            return false;
        }
        return this.isOnCooldown(player.getUniqueId(), type);
    }

    public long getRemainingCooldown(@Nonnull UUID playerId, @Nonnull String type) {
        CooldownConfig config = this.cooldownConfigs.get(type.toLowerCase());
        if (config == null) {
            return 0L;
        }
        String key = config.isGlobal() ? "global" : playerId.toString();
        Map<String, Long> typeCooldowns = this.cooldowns.get(type.toLowerCase());
        if (typeCooldowns == null) {
            return 0L;
        }
        Long expiry = typeCooldowns.get(key);
        if (expiry == null) {
            return 0L;
        }
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public long getRemainingCooldown(@Nonnull Player player, @Nonnull String type) {
        return this.getRemainingCooldown(player.getUniqueId(), type);
    }

    public double getRemainingCooldownSeconds(@Nonnull UUID playerId, @Nonnull String type) {
        return (double)this.getRemainingCooldown(playerId, type) / 1000.0;
    }

    public double getRemainingCooldownSeconds(@Nonnull Player player, @Nonnull String type) {
        return this.getRemainingCooldownSeconds(player.getUniqueId(), type);
    }

    @Nonnull
    public String getFormattedRemainingCooldown(@Nonnull UUID playerId, @Nonnull String type) {
        long remaining = this.getRemainingCooldown(playerId, type);
        return this.formatDuration(remaining);
    }

    @Nonnull
    public String getFormattedRemainingCooldown(@Nonnull Player player, @Nonnull String type) {
        return this.getFormattedRemainingCooldown(player.getUniqueId(), type);
    }

    public void removeCooldown(@Nonnull UUID playerId, @Nonnull String type) {
        Map<String, Long> typeCooldowns = this.cooldowns.get(type.toLowerCase());
        if (typeCooldowns != null) {
            typeCooldowns.remove(playerId.toString());
        }
    }

    public void removeCooldown(@Nonnull Player player, @Nonnull String type) {
        this.removeCooldown(player.getUniqueId(), type);
    }

    public void removeAllCooldowns(@Nonnull UUID playerId) {
        String key = playerId.toString();
        for (Map<String, Long> typeCooldowns : this.cooldowns.values()) {
            typeCooldowns.remove(key);
        }
    }

    public void removeAllCooldowns(@Nonnull Player player) {
        this.removeAllCooldowns(player.getUniqueId());
    }

    public void clearCooldownType(@Nonnull String type) {
        Map<String, Long> typeCooldowns = this.cooldowns.get(type.toLowerCase());
        if (typeCooldowns != null) {
            typeCooldowns.clear();
        }
    }

    public void clearAllCooldowns() {
        for (Map<String, Long> typeCooldowns : this.cooldowns.values()) {
            typeCooldowns.clear();
        }
    }

    public boolean checkAndApply(@Nonnull Player player, @Nonnull String type) {
        if (this.isOnCooldown(player, type)) {
            return false;
        }
        this.setCooldown(player, type);
        return true;
    }

    public boolean checkAndApplyWithMessage(@Nonnull Player player, @Nonnull String type, @Nonnull String message) {
        if (this.isOnCooldown(player, type)) {
            String formattedMessage = message.replace("{time}", this.getFormattedRemainingCooldown(player, type));
            player.sendMessage(formattedMessage);
            return false;
        }
        this.setCooldown(player, type);
        return true;
    }

    @Nonnull
    public CooldownResult checkCooldown(@Nonnull Player player, @Nonnull String type) {
        if (!this.isOnCooldown(player, type)) {
            return new CooldownResult(true, 0L, null);
        }
        long remaining = this.getRemainingCooldown(player, type);
        String formatted = this.getFormattedRemainingCooldown(player, type);
        return new CooldownResult(false, remaining, formatted);
    }

    @Nonnull
    public String formatDuration(long millis) {
        if (millis <= 0L) {
            return "0s";
        }
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        if (hours > 0L) {
            return String.format("%dh %dm %ds", hours, minutes % 60L, seconds % 60L);
        }
        if (minutes > 0L) {
            return String.format("%dm %ds", minutes, seconds % 60L);
        }
        if (seconds > 0L) {
            double secs = (double)millis / 1000.0;
            if (secs < 10.0) {
                return String.format("%.1fs", secs);
            }
            return String.format("%ds", seconds);
        }
        return String.format("%dms", millis);
    }

    public int cleanupExpired() {
        long now = System.currentTimeMillis();
        int removed = 0;
        for (Map<String, Long> typeCooldowns : this.cooldowns.values()) {
            Iterator<Map.Entry<String, Long>> iterator = typeCooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                if (now < entry.getValue()) continue;
                iterator.remove();
                ++removed;
            }
        }
        return removed;
    }

    private void cleanupExpiredCooldowns() {
        int removed = this.cleanupExpired();
        if (removed > 0) {
            this.plugin.getLogger().fine("[CooldownManager] Cleaned up " + removed + " expired cooldowns");
        }
    }

    @Nonnull
    public Map<String, Long> getActiveCooldowns(@Nonnull UUID playerId) {
        HashMap<String, Long> active = new HashMap<String, Long>();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Map<String, Long>> entry : this.cooldowns.entrySet()) {
            String key;
            String type = entry.getKey();
            CooldownConfig config = this.cooldownConfigs.get(type);
            String string = key = config != null && config.isGlobal() ? "global" : playerId.toString();
            Long expiry = entry.getValue().get(key);
            if (expiry == null || now >= expiry) continue;
            active.put(type, expiry - now);
        }
        return active;
    }

    @Nullable
    public CooldownConfig getCooldownConfig(@Nonnull String type) {
        return this.cooldownConfigs.get(type.toLowerCase());
    }

    @Nonnull
    public Set<String> getRegisteredCooldownTypes() {
        return Collections.unmodifiableSet(this.cooldownConfigs.keySet());
    }

    public int getTotalActiveCooldowns() {
        int total = 0;
        long now = System.currentTimeMillis();
        for (Map<String, Long> typeCooldowns : this.cooldowns.values()) {
            for (Long expiry : typeCooldowns.values()) {
                if (now >= expiry) continue;
                ++total;
            }
        }
        return total;
    }

    public int getActiveCooldownCount(@Nonnull String type) {
        Map<String, Long> typeCooldowns = this.cooldowns.get(type.toLowerCase());
        if (typeCooldowns == null) {
            return 0;
        }
        int count = 0;
        long now = System.currentTimeMillis();
        for (Long expiry : typeCooldowns.values()) {
            if (now >= expiry) continue;
            ++count;
        }
        return count;
    }

    public void reload() {
        this.plugin.getLogger().info("\u00a7a[CooldownManager] Reloaded successfully!");
    }

    public void shutdown() {
        if (this.cleanupTask != null) {
            this.cleanupTask.cancel();
            this.cleanupTask = null;
        }
        this.cooldowns.clear();
        this.cooldownConfigs.clear();
        this.plugin.getLogger().info("\u00a7a[CooldownManager] Shutdown complete");
    }

    public record CooldownConfig(String type, long duration, @Nullable String bypassPermission, boolean isGlobal) {
    }

    public record CooldownResult(boolean canProceed, long remainingMillis, @Nullable String formattedRemaining) {
        public double getRemainingSeconds() {
            return (double)this.remainingMillis / 1000.0;
        }
    }
}

