/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.potion.PotionEffect
 */
package com.ultimateduels.models.ffa;

import com.ultimateduels.arena.model.ArenaSpawnPoint;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.models.ffa.FFASession;
import com.ultimateduels.models.player.DuelPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class FFAArena {
    private final String arenaId;
    private String displayName;
    private List<String> description;
    private final DuelArena baseArena;
    private final DuelKit kit;
    private final String worldName;
    private final List<ArenaSpawnPoint> spawnPoints;
    private final Map<UUID, FFASession> activeSessions;
    private final Set<UUID> activePlayers;
    private final Set<UUID> recentlyDied;
    private boolean enabled;
    private int maxPlayers;
    private int spawnProtectionSeconds;
    private boolean healOnKill;
    private int healAmount;
    private boolean rekitOnKill;
    private boolean dropItemsOnDeath;
    private int totalKills;
    private int totalDeaths;
    private long totalPlaytime;
    private int peakPlayerCount;
    private int currentPlayerCount;
    private final long createdAt;
    private long lastRegeneration;
    private final Map<UUID, Integer> sessionKillLeaderboard;

    public FFAArena(String arenaId, DuelArena baseArena, DuelKit kit) {
        this.arenaId = arenaId;
        this.displayName = arenaId;
        this.description = new ArrayList<String>();
        this.baseArena = baseArena;
        this.kit = kit;
        this.worldName = baseArena.getName();
        this.spawnPoints = new ArrayList<ArenaSpawnPoint>();
        if (baseArena.getSpawnPoints() != null) {
            this.spawnPoints.addAll(baseArena.getSpawnPoints());
        }
        this.activeSessions = new ConcurrentHashMap<UUID, FFASession>();
        this.activePlayers = ConcurrentHashMap.newKeySet();
        this.recentlyDied = ConcurrentHashMap.newKeySet();
        this.enabled = true;
        this.maxPlayers = 50;
        this.spawnProtectionSeconds = 3;
        this.healOnKill = true;
        this.healAmount = 4;
        this.rekitOnKill = false;
        this.dropItemsOnDeath = false;
        this.createdAt = System.currentTimeMillis();
        this.sessionKillLeaderboard = new ConcurrentHashMap<UUID, Integer>();
    }

    public FFASession addPlayer(DuelPlayer player) {
        if (this.isFull()) {
            return null;
        }
        FFASession session = new FFASession(player, this);
        this.activeSessions.put(player.getUuid(), session);
        this.activePlayers.add(player.getUuid());
        this.currentPlayerCount = this.activePlayers.size();
        if (this.currentPlayerCount > this.peakPlayerCount) {
            this.peakPlayerCount = this.currentPlayerCount;
        }
        return session;
    }

    public void addPlayer(Player player) {
        if (this.isFull()) {
            return;
        }
        this.activePlayers.add(player.getUniqueId());
        this.currentPlayerCount = this.activePlayers.size();
        if (this.currentPlayerCount > this.peakPlayerCount) {
            this.peakPlayerCount = this.currentPlayerCount;
        }
    }

    public FFASession removePlayer(UUID uuid) {
        FFASession session = this.activeSessions.remove(uuid);
        this.activePlayers.remove(uuid);
        this.recentlyDied.remove(uuid);
        this.sessionKillLeaderboard.remove(uuid);
        this.currentPlayerCount = this.activePlayers.size();
        if (session != null) {
            this.totalPlaytime += session.getSessionDuration();
        }
        return session;
    }

    public void removePlayer(Player player) {
        this.removePlayer(player.getUniqueId());
    }

    public FFASession getSession(UUID uuid) {
        return this.activeSessions.get(uuid);
    }

    public boolean hasPlayer(UUID uuid) {
        return this.activePlayers.contains(uuid);
    }

    public boolean containsPlayer(Player player) {
        return this.activePlayers.contains(player.getUniqueId());
    }

    public Collection<FFASession> getActiveSessions() {
        return Collections.unmodifiableCollection(this.activeSessions.values());
    }

    public Set<UUID> getPlayerUuids() {
        return Collections.unmodifiableSet(this.activePlayers);
    }

    public Set<Player> getPlayers() {
        HashSet<Player> players = new HashSet<Player>();
        for (UUID playerId : this.activePlayers) {
            Player player = Bukkit.getPlayer((UUID)playerId);
            if (player == null || !player.isOnline()) continue;
            players.add(player);
        }
        return players;
    }

    public List<Player> getPlayerList() {
        ArrayList<Player> players = new ArrayList<Player>();
        for (UUID playerId : this.activePlayers) {
            Player player = Bukkit.getPlayer((UUID)playerId);
            if (player == null || !player.isOnline()) continue;
            players.add(player);
        }
        return players;
    }

    public int getPlayerCount() {
        return this.activePlayers.size();
    }

    public boolean isFull() {
        return this.maxPlayers > 0 && this.currentPlayerCount >= this.maxPlayers;
    }

    public boolean isEmpty() {
        return this.activePlayers.isEmpty();
    }

    public void recordKill(UUID killerId, UUID victimId) {
        ++this.totalKills;
        ++this.totalDeaths;
        FFASession killerSession = this.activeSessions.get(killerId);
        FFASession victimSession = this.activeSessions.get(victimId);
        if (killerSession != null) {
            killerSession.addKill();
            this.sessionKillLeaderboard.merge(killerId, 1, Integer::sum);
        }
        if (victimSession != null) {
            victimSession.addDeath();
        }
        this.recentlyDied.add(victimId);
    }

    public void recordKill(Player killer, Player victim) {
        this.recordKill(killer.getUniqueId(), victim.getUniqueId());
    }

    public void recordDeath(UUID victimId) {
        ++this.totalDeaths;
        FFASession victimSession = this.activeSessions.get(victimId);
        if (victimSession != null) {
            victimSession.addDeath();
        }
        this.recentlyDied.add(victimId);
    }

    public void recordDeath(Player victim) {
        this.recordDeath(victim.getUniqueId());
    }

    public boolean hasRecentlyDied(UUID uuid) {
        return this.recentlyDied.contains(uuid);
    }

    public void clearRecentlyDied(UUID uuid) {
        this.recentlyDied.remove(uuid);
    }

    public boolean hasSpawnProtection(UUID uuid) {
        if (this.spawnProtectionSeconds <= 0) {
            return false;
        }
        FFASession session = this.activeSessions.get(uuid);
        if (session == null) {
            return false;
        }
        return session.hasSpawnProtection(this.spawnProtectionSeconds);
    }

    public boolean hasSpawnProtection(Player player) {
        return this.hasSpawnProtection(player.getUniqueId());
    }

    public Location getRandomSpawn() {
        if (this.spawnPoints.isEmpty()) {
            return this.baseArena.getSpawnPoint1();
        }
        Random random = new Random();
        int index = random.nextInt(this.spawnPoints.size());
        return this.spawnPoints.get(index).getLocation();
    }

    public Location getSafeSpawn(double minDistance) {
        if (this.spawnPoints.isEmpty()) {
            return this.getRandomSpawn();
        }
        List playerLocations = this.getPlayers().stream().map(OfflinePlayer::getLocation).collect(Collectors.toList());
        for (ArenaSpawnPoint spawn : this.spawnPoints) {
            Location spawnLoc = spawn.getLocation();
            boolean safe = true;
            for (Location playerLoc : playerLocations) {
                if (!spawnLoc.getWorld().equals((Object)playerLoc.getWorld()) || !(spawnLoc.distance(playerLoc) < minDistance)) continue;
                safe = false;
                break;
            }
            if (!safe) continue;
            return spawnLoc;
        }
        return this.getRandomSpawn();
    }

    private void applyKit(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        if (this.kit.getArmorContents() != null) {
            inv.setArmorContents(this.kit.getArmorContents());
        }
        if (this.kit.getOffhand() != null) {
            inv.setItemInOffHand(this.kit.getOffhand().clone());
        }
        if (this.kit.getInventoryContents() != null) {
            for (int i = 0; i < this.kit.getInventoryContents().length && i < 36; ++i) {
                if (this.kit.getInventoryContents()[i] == null) continue;
                inv.setItem(i, this.kit.getInventoryContents()[i].clone());
            }
        }
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        for (PotionEffect effect2 : this.kit.getEffects()) {
            player.addPotionEffect(effect2);
        }
        player.updateInventory();
    }

    public Location spawnPlayer(DuelPlayer player) {
        Location spawn = this.getSafeSpawn(10.0);
        if (spawn != null && player.isOnline()) {
            FFASession session;
            player.teleport(spawn);
            player.heal();
            player.clearEffects();
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer != null) {
                this.applyKit(bukkitPlayer);
            }
            if ((session = this.activeSessions.get(player.getUuid())) != null) {
                session.recordSpawn();
            }
        }
        return spawn;
    }

    public Location spawnPlayer(Player player) {
        Location spawn = this.getSafeSpawn(10.0);
        if (spawn != null) {
            player.teleport(spawn);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            this.applyKit(player);
            FFASession session = this.activeSessions.get(player.getUniqueId());
            if (session != null) {
                session.recordSpawn();
            }
        }
        return spawn;
    }

    public Location respawnPlayer(DuelPlayer player) {
        this.clearRecentlyDied(player.getUuid());
        return this.spawnPlayer(player);
    }

    public Location respawnPlayer(Player player) {
        this.clearRecentlyDied(player.getUniqueId());
        return this.spawnPlayer(player);
    }

    public List<Map.Entry<UUID, Integer>> getLeaderboard(int limit) {
        return this.sessionKillLeaderboard.entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).limit(limit).collect(Collectors.toList());
    }

    public int getPlayerRank(UUID uuid) {
        List sorted = this.sessionKillLeaderboard.entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).toList();
        for (int i = 0; i < sorted.size(); ++i) {
            if (!((UUID)sorted.get(i).getKey()).equals(uuid)) continue;
            return i + 1;
        }
        return -1;
    }

    public UUID getKillLeader() {
        return this.sessionKillLeaderboard.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    public Player getKillLeaderPlayer() {
        UUID leaderId = this.getKillLeader();
        return leaderId != null ? Bukkit.getPlayer((UUID)leaderId) : null;
    }

    public void markRegenerated() {
        this.lastRegeneration = System.currentTimeMillis();
    }

    public long getTimeSinceRegeneration() {
        if (this.lastRegeneration == 0L) {
            return -1L;
        }
        return System.currentTimeMillis() - this.lastRegeneration;
    }

    public boolean needsRegeneration(long intervalMs) {
        if (intervalMs <= 0L) {
            return false;
        }
        return this.getTimeSinceRegeneration() >= intervalMs;
    }

    public String getArenaId() {
        return this.arenaId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getDescription() {
        return this.description;
    }

    public DuelArena getBaseArena() {
        return this.baseArena;
    }

    public DuelKit getKit() {
        return this.kit;
    }

    public String getKitId() {
        return this.kit.getName();
    }

    public String getWorldName() {
        return this.worldName;
    }

    public List<ArenaSpawnPoint> getSpawnPoints() {
        return this.spawnPoints;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getSpawnProtectionSeconds() {
        return this.spawnProtectionSeconds;
    }

    public boolean isHealOnKill() {
        return this.healOnKill;
    }

    public int getHealAmount() {
        return this.healAmount;
    }

    public boolean isRekitOnKill() {
        return this.rekitOnKill;
    }

    public boolean isDropItemsOnDeath() {
        return this.dropItemsOnDeath;
    }

    public int getTotalKills() {
        return this.totalKills;
    }

    public int getTotalDeaths() {
        return this.totalDeaths;
    }

    public long getTotalPlaytime() {
        return this.totalPlaytime;
    }

    public int getPeakPlayerCount() {
        return this.peakPlayerCount;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getLastRegeneration() {
        return this.lastRegeneration;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setSpawnProtectionSeconds(int seconds) {
        this.spawnProtectionSeconds = seconds;
    }

    public void setHealOnKill(boolean healOnKill) {
        this.healOnKill = healOnKill;
    }

    public void setHealAmount(int healAmount) {
        this.healAmount = healAmount;
    }

    public void setRekitOnKill(boolean rekitOnKill) {
        this.rekitOnKill = rekitOnKill;
    }

    public void setDropItemsOnDeath(boolean dropItemsOnDeath) {
        this.dropItemsOnDeath = dropItemsOnDeath;
    }

    public void broadcast(String message) {
        for (Player player : this.getPlayers()) {
            player.sendMessage(message);
        }
    }

    public void broadcastKill(String killerName, String victimName) {
        this.broadcast("\u00a7c" + victimName + " \u00a77was killed by \u00a7a" + killerName);
    }

    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : this.getPlayers()) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void broadcastSound(Sound sound, float volume, float pitch) {
        for (Player player : this.getPlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FFAArena ffaArena = (FFAArena)o;
        return Objects.equals(this.arenaId, ffaArena.arenaId);
    }

    public int hashCode() {
        return Objects.hash(this.arenaId);
    }

    public String toString() {
        return "FFAArena{arenaId='" + this.arenaId + "', kit=" + this.kit.getName() + ", players=" + this.getPlayerCount() + "/" + this.maxPlayers + ", enabled=" + this.enabled + "}";
    }
}

