/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.ffa.model;

import com.ultimateduels.arena.model.DuelArena;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FFAArenaInstance {
    private final String arenaName;
    private final DuelArena arena;
    private final String kitName;
    private final Set<UUID> players;
    private boolean enabled;
    private int totalKills;
    private int totalDeaths;
    private int peakPlayerCount;
    private int regenerationCount;
    private long lastRegenerationTime;
    private int maxPlayers = 50;
    @Nullable
    private Integer spawnProtectionSeconds = null;
    @Nullable
    private Boolean announceKills = null;
    @Nullable
    private Integer minPlayersForStats = null;
    @Nullable
    private Boolean arenaHealOnKill = null;
    @Nullable
    private Integer arenaHealAmount = null;
    @Nullable
    private Boolean arenaRekitOnKill = null;
    @Nullable
    private Boolean arenaClearEffectsOnKill = null;
    @Nullable
    private Boolean arenaGiveGoldenApple = null;
    @Nullable
    private Integer arenaGoldenApplesAmount = null;

    public FFAArenaInstance(@Nonnull String arenaName, @Nonnull DuelArena arena, @Nonnull String kitName) {
        this.arenaName = arenaName;
        this.arena = arena;
        this.kitName = kitName;
        this.players = ConcurrentHashMap.newKeySet();
        this.enabled = true;
    }

    public void addPlayer(@Nonnull UUID uuid) {
        this.players.add(uuid);
        this.updatePeakCount();
    }

    public void removePlayer(@Nonnull UUID uuid) {
        this.players.remove(uuid);
    }

    public boolean hasPlayer(@Nonnull UUID uuid) {
        return this.players.contains(uuid);
    }

    @Nonnull
    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(this.players);
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    private void updatePeakCount() {
        if (this.players.size() > this.peakPlayerCount) {
            this.peakPlayerCount = this.players.size();
        }
    }

    public void markRegenerated() {
        ++this.regenerationCount;
        this.lastRegenerationTime = System.currentTimeMillis();
    }

    public int getRegenerationCount() {
        return this.regenerationCount;
    }

    public long getLastRegenerationTime() {
        return this.lastRegenerationTime;
    }

    public boolean hasBeenRegenerated() {
        return this.regenerationCount > 0;
    }

    public long getTimeSinceLastRegeneration() {
        if (this.lastRegenerationTime == 0L) {
            return -1L;
        }
        return System.currentTimeMillis() - this.lastRegenerationTime;
    }

    public void broadcast(@Nonnull String message) {
        for (UUID uuid : this.players) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            player.sendMessage(message);
        }
    }

    public void broadcastPrefixed(@Nonnull String message) {
        this.broadcast("\u00a7c\u00a7lFFA \u00a78\u00bb \u00a77" + message);
    }

    public void setKillRewardSettings(boolean healOnKill, int healAmount, boolean rekitOnKill, boolean clearEffectsOnKill, boolean giveGoldenApple, int goldenApplesAmount) {
        this.arenaHealOnKill = healOnKill;
        this.arenaHealAmount = healAmount;
        this.arenaRekitOnKill = rekitOnKill;
        this.arenaClearEffectsOnKill = clearEffectsOnKill;
        this.arenaGiveGoldenApple = giveGoldenApple;
        this.arenaGoldenApplesAmount = goldenApplesAmount;
    }

    public void incrementTotalKills() {
        ++this.totalKills;
    }

    public void incrementTotalDeaths() {
        ++this.totalDeaths;
    }

    public void resetSessionStats() {
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.peakPlayerCount = this.players.size();
    }

    public void resetAllStats() {
        this.resetSessionStats();
        this.regenerationCount = 0;
        this.lastRegenerationTime = 0L;
    }

    @Nonnull
    public String getArenaName() {
        return this.arenaName;
    }

    @Nonnull
    public DuelArena getArena() {
        return this.arena;
    }

    @Nonnull
    public String getKitName() {
        return this.kitName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTotalKills() {
        return this.totalKills;
    }

    public int getTotalDeaths() {
        return this.totalDeaths;
    }

    public int getPeakPlayerCount() {
        return this.peakPlayerCount;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Nullable
    public Integer getSpawnProtectionSeconds() {
        return this.spawnProtectionSeconds;
    }

    public void setSpawnProtectionSeconds(int seconds) {
        this.spawnProtectionSeconds = seconds;
    }

    @Nullable
    public Boolean getAnnounceKills() {
        return this.announceKills;
    }

    public void setAnnounceKills(boolean announceKills) {
        this.announceKills = announceKills;
    }

    @Nullable
    public Integer getMinPlayersForStats() {
        return this.minPlayersForStats;
    }

    public void setMinPlayersForStats(int min) {
        this.minPlayersForStats = min;
    }

    @Nullable
    public Boolean getArenaHealOnKill() {
        return this.arenaHealOnKill;
    }

    @Nullable
    public Integer getArenaHealAmount() {
        return this.arenaHealAmount;
    }

    @Nullable
    public Boolean getArenaRekitOnKill() {
        return this.arenaRekitOnKill;
    }

    @Nullable
    public Boolean getArenaClearEffectsOnKill() {
        return this.arenaClearEffectsOnKill;
    }

    @Nullable
    public Boolean getArenaGiveGoldenApple() {
        return this.arenaGiveGoldenApple;
    }

    @Nullable
    public Integer getArenaGoldenApplesAmount() {
        return this.arenaGoldenApplesAmount;
    }

    public String toString() {
        return "FFAArenaInstance{name=" + this.arenaName + ", kit=" + this.kitName + ", players=" + this.players.size() + ", maxPlayers=" + this.maxPlayers + ", enabled=" + this.enabled + ", regenerations=" + this.regenerationCount + "}";
    }
}

