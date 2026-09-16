/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.stats;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.stats.KitStats;
import com.ultimateduels.stats.LeaderboardEntry;
import com.ultimateduels.stats.PlayerStats;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatsManager {
    private final UltimateDuels plugin;
    private final File statsFolder;
    private final Map<UUID, PlayerStats> statsCache;
    private final Set<UUID> dirtyStats;
    private final Object saveLock = new Object();

    public StatsManager(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.statsFolder = new File(plugin.getDataFolder(), "data/stats");
        this.statsCache = new ConcurrentHashMap<UUID, PlayerStats>();
        this.dirtyStats = ConcurrentHashMap.newKeySet();
        if (!this.statsFolder.exists()) {
            this.statsFolder.mkdirs();
        }
        plugin.getLogger().info("[StatsManager] Initialized successfully!");
    }

    @NotNull
    public PlayerStats getStats(@NotNull UUID playerUUID) {
        PlayerStats cached = this.statsCache.get(playerUUID);
        if (cached != null) {
            return cached;
        }
        PlayerStats stats = this.loadStats(playerUUID);
        if (stats == null) {
            stats = new PlayerStats(playerUUID);
            Player player = Bukkit.getPlayer((UUID)playerUUID);
            if (player != null) {
                stats.setPlayerName(player.getName());
            }
        }
        this.statsCache.put(playerUUID, stats);
        return stats;
    }

    @NotNull
    public PlayerStats getStats(@NotNull Player player) {
        PlayerStats stats = this.getStats(player.getUniqueId());
        stats.setPlayerName(player.getName());
        stats.updateLastSeen();
        return stats;
    }

    @Nullable
    private PlayerStats loadStats(@NotNull UUID playerUUID) {
        File file = this.getStatsFile(playerUUID);
        if (!file.exists()) {
            return null;
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
            PlayerStats stats = new PlayerStats(playerUUID);
            stats.setPlayerName(config.getString("name", ""));
            stats.setGlobalElo(config.getInt("globalElo", 1000));
            stats.setTotalWins(config.getInt("totalWins", 0));
            stats.setTotalLosses(config.getInt("totalLosses", 0));
            stats.setTotalKills(config.getInt("totalKills", 0));
            stats.setTotalDeaths(config.getInt("totalDeaths", 0));
            stats.setTotalMatchesPlayed(config.getInt("totalMatchesPlayed", 0));
            stats.setCurrentWinStreak(config.getInt("currentWinStreak", 0));
            stats.setBestWinStreak(config.getInt("bestWinStreak", 0));
            stats.setBestKillStreak(config.getInt("bestKillStreak", 0));
            stats.setTotalHeadshotKills(config.getInt("totalHeadshotKills", 0));
            stats.setFirstJoin(config.getLong("firstJoin", System.currentTimeMillis()));
            stats.setLastSeen(config.getLong("lastSeen", System.currentTimeMillis()));
            ConfigurationSection kitsSection = config.getConfigurationSection("kitStats");
            if (kitsSection != null) {
                for (String kitName : kitsSection.getKeys(false)) {
                    ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
                    if (kitSection == null) continue;
                    KitStats kitStats = new KitStats(kitName);
                    kitStats.setElo(kitSection.getInt("elo", 1000));
                    kitStats.setWins(kitSection.getInt("wins", 0));
                    kitStats.setLosses(kitSection.getInt("losses", 0));
                    kitStats.setKills(kitSection.getInt("kills", 0));
                    kitStats.setDeaths(kitSection.getInt("deaths", 0));
                    kitStats.setMatchesPlayed(kitSection.getInt("matchesPlayed", 0));
                    kitStats.setCurrentWinStreak(kitSection.getInt("currentWinStreak", 0));
                    kitStats.setBestWinStreak(kitSection.getInt("bestWinStreak", 0));
                    kitStats.setBestKillStreak(kitSection.getInt("bestKillStreak", 0));
                    stats.addKitStats(kitName, kitStats);
                }
            }
            this.plugin.debug("Loaded stats for " + String.valueOf(playerUUID));
            return stats;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load stats for " + String.valueOf(playerUUID), e);
            return null;
        }
    }

    public void markDirty(@NotNull UUID playerUUID) {
        this.dirtyStats.add(playerUUID);
    }

    public void saveStats(@NotNull UUID playerUUID) {
        PlayerStats stats = this.statsCache.get(playerUUID);
        if (stats == null) {
            return;
        }
        this.saveStatsToFile(stats);
        this.dirtyStats.remove(playerUUID);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveStatsSync(@NotNull UUID playerUUID) {
        PlayerStats stats = this.statsCache.get(playerUUID);
        if (stats == null) {
            return;
        }
        Object object = this.saveLock;
        synchronized (object) {
            this.saveStatsToFile(stats);
            this.dirtyStats.remove(playerUUID);
        }
    }

    private void saveStatsToFile(@NotNull PlayerStats stats) {
        File file = this.getStatsFile(stats.getPlayerUUID());
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.set("uuid", (Object)stats.getPlayerUUID().toString());
            config.set("name", (Object)stats.getPlayerName());
            config.set("globalElo", (Object)stats.getGlobalElo());
            config.set("totalWins", (Object)stats.getTotalWins());
            config.set("totalLosses", (Object)stats.getTotalLosses());
            config.set("totalKills", (Object)stats.getTotalKills());
            config.set("totalDeaths", (Object)stats.getTotalDeaths());
            config.set("totalMatchesPlayed", (Object)stats.getTotalMatchesPlayed());
            config.set("currentWinStreak", (Object)stats.getCurrentWinStreak());
            config.set("bestWinStreak", (Object)stats.getBestWinStreak());
            config.set("bestKillStreak", (Object)stats.getBestKillStreak());
            config.set("totalHeadshotKills", (Object)stats.getTotalHeadshotKills());
            config.set("firstJoin", (Object)stats.getFirstJoin());
            config.set("lastSeen", (Object)stats.getLastSeen());
            for (Map.Entry<String, KitStats> entry : stats.getAllKitStats().entrySet()) {
                String path = "kitStats." + entry.getKey();
                KitStats kitStats = entry.getValue();
                config.set(path + ".elo", (Object)kitStats.getElo());
                config.set(path + ".wins", (Object)kitStats.getWins());
                config.set(path + ".losses", (Object)kitStats.getLosses());
                config.set(path + ".kills", (Object)kitStats.getKills());
                config.set(path + ".deaths", (Object)kitStats.getDeaths());
                config.set(path + ".matchesPlayed", (Object)kitStats.getMatchesPlayed());
                config.set(path + ".currentWinStreak", (Object)kitStats.getCurrentWinStreak());
                config.set(path + ".bestWinStreak", (Object)kitStats.getBestWinStreak());
                config.set(path + ".bestKillStreak", (Object)kitStats.getBestKillStreak());
            }
            config.save(file);
            this.plugin.debug("Saved stats for " + String.valueOf(stats.getPlayerUUID()) + " (" + stats.getPlayerName() + ")");
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save stats for " + String.valueOf(stats.getPlayerUUID()), e);
        }
    }

    public void saveAllDirty() {
        if (this.dirtyStats.isEmpty()) {
            return;
        }
        this.plugin.getLogger().info("[StatsManager] Saving " + this.dirtyStats.size() + " dirty stats...");
        for (UUID uuid : new HashSet<UUID>(this.dirtyStats)) {
            this.saveStats(uuid);
        }
        this.plugin.getLogger().info("[StatsManager] All dirty stats saved!");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveAll() {
        this.plugin.getLogger().info("[StatsManager] Saving all " + this.statsCache.size() + " cached stats...");
        Object object = this.saveLock;
        synchronized (object) {
            for (PlayerStats stats : this.statsCache.values()) {
                this.saveStatsToFile(stats);
            }
        }
        this.dirtyStats.clear();
        this.plugin.getLogger().info("[StatsManager] All stats saved!");
    }

    public void unloadStats(@NotNull UUID playerUUID) {
        PlayerStats stats = this.statsCache.get(playerUUID);
        if (stats != null) {
            this.saveStatsToFile(stats);
            this.statsCache.remove(playerUUID);
            this.dirtyStats.remove(playerUUID);
            this.plugin.debug("Unloaded stats for " + String.valueOf(playerUUID));
        }
    }

    public void recordMatch(@NotNull UUID playerUUID, @NotNull String kitName, boolean won, int kills, int deaths) {
        PlayerStats stats = this.getStats(playerUUID);
        stats.recordMatch(kitName, won, kills, deaths);
        this.markDirty(playerUUID);
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> this.saveStats(playerUUID));
        this.plugin.debug("[StatsManager] Recorded match for " + stats.getPlayerName() + ": " + (won ? "WIN" : "LOSS") + ", K:" + kills + " D:" + deaths + " Kit:" + kitName + " KDR:" + stats.getFormattedKDR());
    }

    public void recordWin(@NotNull UUID playerUUID, @NotNull String kitName) {
        PlayerStats stats = this.getStats(playerUUID);
        stats.addWin();
        stats.getOrCreateKitStats(kitName).addWin();
        this.markDirty(playerUUID);
    }

    public void recordLoss(@NotNull UUID playerUUID, @NotNull String kitName) {
        PlayerStats stats = this.getStats(playerUUID);
        stats.addLoss();
        stats.getOrCreateKitStats(kitName).addLoss();
        this.markDirty(playerUUID);
    }

    public void addKills(@NotNull UUID playerUUID, @NotNull String kitName, int kills) {
        if (kills <= 0) {
            return;
        }
        PlayerStats stats = this.getStats(playerUUID);
        stats.addKills(kills);
        stats.getOrCreateKitStats(kitName).addKills(kills);
        this.markDirty(playerUUID);
    }

    public void addDeaths(@NotNull UUID playerUUID, @NotNull String kitName, int deaths) {
        if (deaths <= 0) {
            return;
        }
        PlayerStats stats = this.getStats(playerUUID);
        stats.addDeaths(deaths);
        stats.getOrCreateKitStats(kitName).addDeaths(deaths);
        this.markDirty(playerUUID);
    }

    public void addElo(@NotNull UUID playerUUID, int amount) {
        PlayerStats stats = this.getStats(playerUUID);
        stats.addElo(amount);
        this.markDirty(playerUUID);
    }

    public void addKitElo(@NotNull UUID playerUUID, @NotNull String kitName, int amount) {
        PlayerStats stats = this.getStats(playerUUID);
        stats.getOrCreateKitStats(kitName).addElo(amount);
        this.markDirty(playerUUID);
    }

    public int getElo(@NotNull UUID playerUUID) {
        return this.getStats(playerUUID).getGlobalElo();
    }

    public int getKitElo(@NotNull UUID playerUUID, @NotNull String kitName) {
        KitStats kitStats = this.getStats(playerUUID).getKitStats(kitName);
        return kitStats != null ? kitStats.getElo() : 1000;
    }

    public double getKDR(@NotNull UUID playerUUID) {
        return this.getStats(playerUUID).getKDR();
    }

    @NotNull
    public String getFormattedKDR(@NotNull UUID playerUUID) {
        return this.getStats(playerUUID).getFormattedKDR();
    }

    public double getKitKDR(@NotNull UUID playerUUID, @NotNull String kitName) {
        KitStats kitStats = this.getStats(playerUUID).getKitStats(kitName);
        return kitStats != null ? kitStats.getKDR() : 0.0;
    }

    public boolean hasStats(@NotNull UUID playerUUID) {
        return this.getStatsFile(playerUUID).exists() || this.statsCache.containsKey(playerUUID);
    }

    public int getPlayerRank(@NotNull UUID playerUUID, @NotNull String statType) {
        List<PlayerStats> allStats = this.getAllStats();
        Comparator<PlayerStats> comparator = this.getComparatorForStat(statType);
        allStats.sort(comparator);
        for (int i = 0; i < allStats.size(); ++i) {
            if (!allStats.get(i).getPlayerUUID().equals(playerUUID)) continue;
            return i + 1;
        }
        return 0;
    }

    private Comparator<PlayerStats> getComparatorForStat(@NotNull String statType) {
        return switch (statType.toLowerCase()) {
            case "wins" -> Comparator.comparingInt(PlayerStats::getTotalWins).reversed();
            case "kills" -> Comparator.comparingInt(PlayerStats::getTotalKills).reversed();
            case "deaths" -> Comparator.comparingInt(PlayerStats::getTotalDeaths).reversed();
            case "losses" -> Comparator.comparingInt(PlayerStats::getTotalLosses).reversed();
            case "elo" -> Comparator.comparingInt(PlayerStats::getGlobalElo).reversed();
            case "kdr", "kd" -> Comparator.comparingDouble(PlayerStats::getKDR).reversed();
            case "wlr", "wl" -> Comparator.comparingDouble(PlayerStats::getWinLossRatio).reversed();
            case "games", "matches" -> Comparator.comparingInt(PlayerStats::getTotalGames).reversed();
            case "winstreak", "streak" -> Comparator.comparingInt(PlayerStats::getBestWinStreak).reversed();
            case "killstreak" -> Comparator.comparingInt(PlayerStats::getBestKillStreak).reversed();
            case "winrate", "wr" -> Comparator.comparingDouble(PlayerStats::getWinRate).reversed();
            default -> Comparator.comparingInt(PlayerStats::getTotalWins).reversed();
        };
    }

    public int getPlayerWinsRank(@NotNull UUID playerUUID) {
        return this.getPlayerRank(playerUUID, "wins");
    }

    public int getPlayerEloRank(@NotNull UUID playerUUID) {
        return this.getPlayerRank(playerUUID, "elo");
    }

    public int getPlayerKDRRank(@NotNull UUID playerUUID) {
        return this.getPlayerRank(playerUUID, "kdr");
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByElo(int limit) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getGlobalElo).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByWins(int limit) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalWins).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByKDR(int limit) {
        return this.getAllStats().stream().filter(s -> s.getTotalGames() >= 5).sorted(Comparator.comparingDouble(PlayerStats::getKDR).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByKills(int limit) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalKills).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByWinStreak(int limit) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getBestWinStreak).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByKillStreak(int limit) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getBestKillStreak).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopPlayersByWinRate(int limit, int minGames) {
        return this.getAllStats().stream().filter(s -> s.getTotalGames() >= minGames).sorted(Comparator.comparingDouble(PlayerStats::getWinRate).reversed()).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByElo(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getGlobalElo).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByKills(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalKills).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByWins(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalWins).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByStreak(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getBestWinStreak).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByKD(int limit, int offset) {
        return this.getAllStats().stream().filter(s -> s.getTotalGames() >= 5).sorted(Comparator.comparingDouble(PlayerStats::getKDR).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByWinRate(int limit, int offset) {
        return this.getAllStats().stream().filter(s -> s.getTotalGames() >= 10).sorted(Comparator.comparingDouble(PlayerStats::getWinRate).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByDeaths(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalDeaths).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByLosses(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalLosses).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<PlayerStats> getTopByGamesPlayed(int limit, int offset) {
        return this.getAllStats().stream().sorted(Comparator.comparingInt(PlayerStats::getTotalGames).reversed()).skip(offset).limit(limit).collect(Collectors.toList());
    }

    @NotNull
    public List<LeaderboardEntry> getLeaderboard(@NotNull String statType, @Nullable String kitName, int limit) {
        List<PlayerStats> allStats = this.getAllStats();
        ArrayList<LeaderboardEntry> entries = new ArrayList<LeaderboardEntry>();
        switch (statType.toLowerCase()) {
            case "kills": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getTotalKills).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getTotalKills())));
                break;
            }
            case "wins": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getTotalWins).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getTotalWins())));
                break;
            }
            case "deaths": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getTotalDeaths).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getTotalDeaths())));
                break;
            }
            case "losses": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getTotalLosses).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getTotalLosses())));
                break;
            }
            case "elo": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getGlobalElo).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getGlobalElo())));
                break;
            }
            case "kdr": 
            case "kd": {
                allStats.stream().filter(s -> s.getTotalGames() >= 5).sorted(Comparator.comparingDouble(PlayerStats::getKDR).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getKDR())));
                break;
            }
            case "wlr": 
            case "wl": {
                allStats.stream().filter(s -> s.getTotalGames() >= 5).sorted(Comparator.comparingDouble(PlayerStats::getWinLossRatio).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getWinLossRatio())));
                break;
            }
            case "best_winstreak": 
            case "winstreak": 
            case "streak": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getBestWinStreak).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getBestWinStreak())));
                break;
            }
            case "best_killstreak": 
            case "killstreak": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getBestKillStreak).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getBestKillStreak())));
                break;
            }
            case "games_played": 
            case "matches": 
            case "games": {
                allStats.stream().sorted(Comparator.comparingInt(PlayerStats::getTotalGames).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getTotalGames())));
                break;
            }
            case "winrate": 
            case "wr": {
                allStats.stream().filter(s -> s.getTotalGames() >= 10).sorted(Comparator.comparingDouble(PlayerStats::getWinRate).reversed()).limit(limit).forEach(s -> entries.add(new LeaderboardEntry(s.getPlayerUUID(), s.getPlayerName(), s.getWinRate())));
                break;
            }
        }
        return entries;
    }

    @NotNull
    public List<PlayerStats> getAllStats() {
        ArrayList<PlayerStats> allStats = new ArrayList<PlayerStats>(this.statsCache.values());
        File[] files = this.statsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    PlayerStats stats;
                    String uuidStr = file.getName().replace(".yml", "");
                    UUID uuid = UUID.fromString(uuidStr);
                    if (this.statsCache.containsKey(uuid) || (stats = this.loadStats(uuid)) == null) continue;
                    allStats.add(stats);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
        }
        return allStats;
    }

    public int getTotalPlayersWithStats() {
        HashSet<UUID> allUUIDs = new HashSet<UUID>(this.statsCache.keySet());
        File[] files = this.statsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    String uuidStr = file.getName().replace(".yml", "");
                    UUID uuid = UUID.fromString(uuidStr);
                    allUUIDs.add(uuid);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
        }
        return allUUIDs.size();
    }

    @NotNull
    private File getStatsFile(@NotNull UUID playerUUID) {
        return new File(this.statsFolder, playerUUID.toString() + ".yml");
    }

    public int getCacheSize() {
        return this.statsCache.size();
    }

    public int getDirtyCount() {
        return this.dirtyStats.size();
    }

    public boolean isInitialized() {
        return this.statsFolder.exists() && this.statsFolder.isDirectory();
    }

    @NotNull
    public File getStatsFolder() {
        return this.statsFolder;
    }

    public void resetStats(@NotNull UUID playerUUID) {
        PlayerStats stats = new PlayerStats(playerUUID);
        Player player = Bukkit.getPlayer((UUID)playerUUID);
        if (player != null) {
            stats.setPlayerName(player.getName());
        }
        this.statsCache.put(playerUUID, stats);
        this.markDirty(playerUUID);
        this.saveStats(playerUUID);
        this.plugin.getLogger().info("[StatsManager] Reset stats for " + String.valueOf(playerUUID));
    }

    public void resetKitStats(@NotNull UUID playerUUID, @NotNull String kitName) {
        PlayerStats stats = this.getStats(playerUUID);
        stats.addKitStats(kitName, new KitStats(kitName));
        this.markDirty(playerUUID);
        this.saveStats(playerUUID);
        this.plugin.getLogger().info("[StatsManager] Reset kit stats for " + String.valueOf(playerUUID) + " - " + kitName);
    }

    public void shutdown() {
        this.plugin.getLogger().info("[StatsManager] Shutting down...");
        this.saveAll();
        this.statsCache.clear();
        this.dirtyStats.clear();
        this.plugin.getLogger().info("[StatsManager] Shutdown complete!");
    }

    public void reload() {
        this.plugin.getLogger().info("[StatsManager] Reloading...");
        this.saveAll();
        this.statsCache.clear();
        this.dirtyStats.clear();
        this.plugin.getLogger().info("[StatsManager] Reloaded!");
    }
}

