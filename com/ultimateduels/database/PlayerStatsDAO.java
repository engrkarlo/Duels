/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.database;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.database.DatabaseManager;
import com.ultimateduels.database.dao.DAO;
import com.ultimateduels.database.models.PlayerStats;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public class PlayerStatsDAO
implements DAO<PlayerStats, UUID> {
    private final UltimateDuels plugin;
    private final DatabaseManager databaseManager;
    private final String tableName;
    private final Map<UUID, CachedStats> statsCache = new ConcurrentHashMap<UUID, CachedStats>();
    private static final long CACHE_EXPIRY_MS = TimeUnit.MINUTES.toMillis(5L);
    private final Map<UUID, PlayerStats> pendingUpdates = new ConcurrentHashMap<UUID, PlayerStats>();
    private final Map<String, List<PlayerStats>> leaderboardCache = new ConcurrentHashMap<String, List<PlayerStats>>();
    private long lastLeaderboardUpdate = 0L;
    private static final long LEADERBOARD_CACHE_MS = TimeUnit.MINUTES.toMillis(1L);

    public PlayerStatsDAO(UltimateDuels plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.tableName = databaseManager.getTable("player_stats");
        this.startCacheCleanupTask();
        this.startBatchSaveTask();
    }

    @Override
    public PlayerStats create(PlayerStats stats) throws SQLException {
        String sql = "INSERT INTO %s (uuid, username, kills, deaths, wins, losses, win_streak,\n    best_win_streak, elo, highest_elo, games_played, total_damage_dealt,\n    total_damage_taken, arrows_shot, arrows_hit, potions_used,\n    golden_apples_eaten, pearls_thrown, total_playtime, ffa_kills,\n    ffa_deaths, first_join, last_seen)\nVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n".formatted(this.tableName);
        long id = this.databaseManager.executeInsert(sql, stats.getUuid().toString(), stats.getUsername(), stats.getKills(), stats.getDeaths(), stats.getWins(), stats.getLosses(), stats.getWinStreak(), stats.getBestWinStreak(), stats.getElo(), stats.getHighestElo(), stats.getGamesPlayed(), stats.getTotalDamageDealt(), stats.getTotalDamageTaken(), stats.getArrowsShot(), stats.getArrowsHit(), stats.getPotionsUsed(), stats.getGoldenApplesEaten(), stats.getPearlsThrown(), stats.getTotalPlaytime(), stats.getFfaKills(), stats.getFfaDeaths(), stats.getFirstJoin(), stats.getLastSeen());
        PlayerStats created = this.findById(stats.getUuid()).orElse(stats);
        this.cacheStats(created);
        return created;
    }

    @Override
    public Optional<PlayerStats> findById(UUID uuid) throws SQLException {
        CachedStats cached = this.statsCache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            return Optional.of(cached.stats);
        }
        String sql = "SELECT * FROM %s WHERE uuid = ?".formatted(this.tableName);
        Optional<PlayerStats> result = this.databaseManager.executeQuerySingle(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Error parsing player stats", e);
                return null;
            }
        }, uuid.toString());
        result.ifPresent(this::cacheStats);
        return result;
    }

    @Override
    public List<PlayerStats> findAll() throws SQLException {
        String sql = "SELECT * FROM %s".formatted(this.tableName);
        return this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Error parsing player stats", e);
                return null;
            }
        }, new Object[0]);
    }

    @Override
    public PlayerStats update(PlayerStats stats) throws SQLException {
        String sql = "UPDATE %s SET\n    username = ?, kills = ?, deaths = ?, wins = ?, losses = ?,\n    win_streak = ?, best_win_streak = ?, elo = ?, highest_elo = ?,\n    games_played = ?, total_damage_dealt = ?, total_damage_taken = ?,\n    arrows_shot = ?, arrows_hit = ?, potions_used = ?,\n    golden_apples_eaten = ?, pearls_thrown = ?, total_playtime = ?,\n    ffa_kills = ?, ffa_deaths = ?, last_seen = ?\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, stats.getUsername(), stats.getKills(), stats.getDeaths(), stats.getWins(), stats.getLosses(), stats.getWinStreak(), stats.getBestWinStreak(), stats.getElo(), stats.getHighestElo(), stats.getGamesPlayed(), stats.getTotalDamageDealt(), stats.getTotalDamageTaken(), stats.getArrowsShot(), stats.getArrowsHit(), stats.getPotionsUsed(), stats.getGoldenApplesEaten(), stats.getPearlsThrown(), stats.getTotalPlaytime(), stats.getFfaKills(), stats.getFfaDeaths(), new Timestamp(System.currentTimeMillis()), stats.getUuid().toString());
        this.cacheStats(stats);
        return stats;
    }

    @Override
    public boolean delete(UUID uuid) throws SQLException {
        String sql = "DELETE FROM %s WHERE uuid = ?".formatted(this.tableName);
        int affected = this.databaseManager.executeUpdate(sql, uuid.toString());
        this.statsCache.remove(uuid);
        this.pendingUpdates.remove(uuid);
        return affected > 0;
    }

    @Override
    public boolean exists(UUID uuid) throws SQLException {
        if (this.statsCache.containsKey(uuid)) {
            return true;
        }
        String sql = "SELECT 1 FROM %s WHERE uuid = ? LIMIT 1".formatted(this.tableName);
        return this.databaseManager.executeQuerySingle(sql, rs -> true, uuid.toString()).isPresent();
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM %s".formatted(this.tableName);
        return this.databaseManager.executeQuerySingle(sql, rs -> {
            try {
                return rs.getLong(1);
            }
            catch (SQLException e) {
                return 0L;
            }
        }, new Object[0]).orElse(0L);
    }

    public Optional<PlayerStats> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM %s WHERE LOWER(username) = LOWER(?) LIMIT 1".formatted(this.tableName);
        return this.databaseManager.executeQuerySingle(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, username);
    }

    public PlayerStats getOrCreate(UUID uuid, String username) throws SQLException {
        Optional<PlayerStats> existing = this.findById(uuid);
        if (existing.isPresent()) {
            PlayerStats stats = existing.get();
            if (!stats.getUsername().equals(username)) {
                stats.setUsername(username);
                this.update(stats);
            }
            return stats;
        }
        PlayerStats newStats = PlayerStats.createNew(uuid, username);
        return this.create(newStats);
    }

    public CompletableFuture<PlayerStats> getOrCreateAsync(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.getOrCreate(uuid, username);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Map<UUID, PlayerStats> findByIds(Collection<UUID> uuids) throws SQLException {
        if (uuids.isEmpty()) {
            return Collections.emptyMap();
        }
        HashMap<UUID, PlayerStats> result = new HashMap<UUID, PlayerStats>();
        HashSet<UUID> uncached = new HashSet<UUID>();
        for (UUID uuid : uuids) {
            CachedStats cached = this.statsCache.get(uuid);
            if (cached != null && !cached.isExpired()) {
                result.put(uuid, cached.stats);
                continue;
            }
            uncached.add(uuid);
        }
        if (uncached.isEmpty()) {
            return result;
        }
        String placeholders = String.join((CharSequence)",", Collections.nCopies(uncached.size(), "?"));
        String sql = "SELECT * FROM %s WHERE uuid IN (%s)".formatted(this.tableName, placeholders);
        Object[] params = uncached.stream().map(UUID::toString).toArray();
        List<PlayerStats> found = this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, params);
        for (PlayerStats stats : found) {
            result.put(stats.getUuid(), stats);
            this.cacheStats(stats);
        }
        return result;
    }

    public List<PlayerStats> getLeaderboard(LeaderboardType type, int limit) throws SQLException {
        List<PlayerStats> cached;
        String cacheKey = type.getKey() + "_" + limit;
        if (System.currentTimeMillis() - this.lastLeaderboardUpdate < LEADERBOARD_CACHE_MS && (cached = this.leaderboardCache.get(cacheKey)) != null) {
            return cached;
        }
        String sql = "SELECT * FROM %s ORDER BY %s LIMIT ?".formatted(this.tableName, type.getOrderBy());
        List<PlayerStats> result = this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, limit);
        this.leaderboardCache.put(cacheKey, result);
        this.lastLeaderboardUpdate = System.currentTimeMillis();
        return result;
    }

    public int getPlayerRank(UUID uuid, LeaderboardType type) throws SQLException {
        String sql = this.databaseManager.getConnectionPool().isMySQL() ? "SELECT ranking FROM (\n    SELECT uuid, ROW_NUMBER() OVER (ORDER BY %s) as ranking\n    FROM %s\n) ranked\nWHERE uuid = ?\n".formatted(type.getOrderBy(), this.tableName) : "SELECT COUNT(*) + 1 FROM %s\nWHERE %s > (SELECT %s FROM %s WHERE uuid = ?)\n".formatted(this.tableName, type.getOrderBy().replace(" DESC", ""), type.getOrderBy().replace(" DESC", ""), this.tableName);
        return this.databaseManager.executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt(1);
            }
            catch (SQLException e) {
                return -1;
            }
        }, uuid.toString()).orElse(-1);
    }

    public List<PlayerStats> getLeaderboardPage(LeaderboardType type, int page, int pageSize) throws SQLException {
        int offset = page * pageSize;
        String sql = "SELECT * FROM %s ORDER BY %s LIMIT ? OFFSET ?".formatted(this.tableName, type.getOrderBy());
        return this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, pageSize, offset);
    }

    public void recordWin(UUID uuid, int eloChange) throws SQLException {
        String sql = "UPDATE %s SET\n    wins = wins + 1,\n    games_played = games_played + 1,\n    win_streak = win_streak + 1,\n    best_win_streak = GREATEST(best_win_streak, win_streak + 1),\n    elo = GREATEST(0, elo + ?),\n    highest_elo = GREATEST(highest_elo, GREATEST(0, elo + ?)),\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, eloChange, eloChange, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void recordLoss(UUID uuid, int eloChange) throws SQLException {
        String sql = "UPDATE %s SET\n    losses = losses + 1,\n    games_played = games_played + 1,\n    win_streak = 0,\n    elo = GREATEST(0, elo + ?),\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, eloChange, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void recordKill(UUID uuid, boolean isFfa) throws SQLException {
        String sql = isFfa ? "UPDATE %s SET\n    ffa_kills = ffa_kills + 1,\n    kills = kills + 1,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName) : "UPDATE %s SET\n    kills = kills + 1,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void recordDeath(UUID uuid, boolean isFfa) throws SQLException {
        String sql = isFfa ? "UPDATE %s SET\n    ffa_deaths = ffa_deaths + 1,\n    deaths = deaths + 1,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName) : "UPDATE %s SET\n    deaths = deaths + 1,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void updateCombatStats(UUID uuid, double damageDealt, double damageTaken, int arrowsShot, int arrowsHit, int potionsUsed, int gapplesEaten, int pearlsThrown) throws SQLException {
        String sql = "UPDATE %s SET\n    total_damage_dealt = total_damage_dealt + ?,\n    total_damage_taken = total_damage_taken + ?,\n    arrows_shot = arrows_shot + ?,\n    arrows_hit = arrows_hit + ?,\n    potions_used = potions_used + ?,\n    golden_apples_eaten = golden_apples_eaten + ?,\n    pearls_thrown = pearls_thrown + ?,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, damageDealt, damageTaken, arrowsShot, arrowsHit, potionsUsed, gapplesEaten, pearlsThrown, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void addPlaytime(UUID uuid, long secondsPlayed) throws SQLException {
        String sql = "UPDATE %s SET\n    total_playtime = total_playtime + ?,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, secondsPlayed, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void resetStats(UUID uuid) throws SQLException {
        String sql = "UPDATE %s SET\n    kills = 0, deaths = 0, wins = 0, losses = 0,\n    win_streak = 0, best_win_streak = 0,\n    elo = 1000, highest_elo = 1000, games_played = 0,\n    total_damage_dealt = 0, total_damage_taken = 0,\n    arrows_shot = 0, arrows_hit = 0, potions_used = 0,\n    golden_apples_eaten = 0, pearls_thrown = 0,\n    total_playtime = 0, ffa_kills = 0, ffa_deaths = 0,\n    last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        this.databaseManager.executeUpdate(sql, uuid.toString());
        this.invalidateCache(uuid);
    }

    public void queueUpdate(PlayerStats stats) {
        this.pendingUpdates.put(stats.getUuid(), stats);
        this.cacheStats(stats);
    }

    public int savePendingUpdates() {
        if (this.pendingUpdates.isEmpty()) {
            return 0;
        }
        HashMap<UUID, PlayerStats> toSave = new HashMap<UUID, PlayerStats>(this.pendingUpdates);
        this.pendingUpdates.clear();
        int saved = 0;
        for (PlayerStats stats : toSave.values()) {
            try {
                this.update(stats);
                ++saved;
            }
            catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save stats for " + String.valueOf(stats.getUuid()), e);
                this.pendingUpdates.put(stats.getUuid(), stats);
            }
        }
        if (saved > 0 && this.plugin.getConfig().getBoolean("general.debug")) {
            this.plugin.getLogger().info("Batch saved " + saved + " player stats");
        }
        return saved;
    }

    public void batchUpdate(List<PlayerStats> statsList) throws SQLException {
        if (statsList.isEmpty()) {
            return;
        }
        String sql = "UPDATE %s SET\n    username = ?, kills = ?, deaths = ?, wins = ?, losses = ?,\n    win_streak = ?, best_win_streak = ?, elo = ?, highest_elo = ?,\n    games_played = ?, last_seen = CURRENT_TIMESTAMP\nWHERE uuid = ?\n".formatted(this.tableName);
        ArrayList<Object[]> batchParams = new ArrayList<Object[]>();
        for (PlayerStats stats : statsList) {
            batchParams.add(new Object[]{stats.getUsername(), stats.getKills(), stats.getDeaths(), stats.getWins(), stats.getLosses(), stats.getWinStreak(), stats.getBestWinStreak(), stats.getElo(), stats.getHighestElo(), stats.getGamesPlayed(), stats.getUuid().toString()});
        }
        this.databaseManager.executeBatch(sql, batchParams);
        for (PlayerStats stats : statsList) {
            this.cacheStats(stats);
        }
    }

    public List<PlayerStats> searchByUsername(String query, int limit) throws SQLException {
        String sql = "SELECT * FROM %s WHERE LOWER(username) LIKE LOWER(?) ORDER BY username LIMIT ?".formatted(this.tableName);
        return this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, "%" + query + "%", limit);
    }

    public List<PlayerStats> findByEloRange(int minElo, int maxElo, int limit) throws SQLException {
        String sql = "SELECT * FROM %s WHERE elo BETWEEN ? AND ? ORDER BY elo DESC LIMIT ?".formatted(this.tableName);
        return this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, minElo, maxElo, limit);
    }

    public List<PlayerStats> findActivePlayersByGames(int minGames, int limit) throws SQLException {
        String sql = "SELECT * FROM %s WHERE games_played >= ? ORDER BY games_played DESC LIMIT ?".formatted(this.tableName);
        return this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, minGames, limit);
    }

    public List<PlayerStats> findRecentlyActive(int withinDays, int limit) throws SQLException {
        String sql = this.databaseManager.getConnectionPool().isMySQL() ? "SELECT * FROM %s\nWHERE last_seen >= DATE_SUB(NOW(), INTERVAL ? DAY)\nORDER BY last_seen DESC LIMIT ?\n".formatted(this.tableName) : "SELECT * FROM %s\nWHERE last_seen >= datetime('now', '-' || ? || ' days')\nORDER BY last_seen DESC LIMIT ?\n".formatted(this.tableName);
        return this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return PlayerStats.fromResultSet(rs);
            }
            catch (SQLException e) {
                return null;
            }
        }, withinDays, limit);
    }

    public Map<String, Object> getGlobalStats() throws SQLException {
        String sql = "SELECT\n    COUNT(*) as total_players,\n    SUM(kills) as total_kills,\n    SUM(deaths) as total_deaths,\n    SUM(wins) as total_wins,\n    SUM(games_played) as total_games,\n    AVG(elo) as average_elo,\n    MAX(elo) as highest_elo,\n    MAX(best_win_streak) as highest_streak,\n    SUM(total_playtime) as total_playtime\nFROM %s\n".formatted(this.tableName);
        return this.databaseManager.executeQuerySingle(sql, rs -> {
            try {
                LinkedHashMap<String, Number> stats = new LinkedHashMap<String, Number>();
                stats.put("total_players", rs.getLong("total_players"));
                stats.put("total_kills", rs.getLong("total_kills"));
                stats.put("total_deaths", rs.getLong("total_deaths"));
                stats.put("total_wins", rs.getLong("total_wins"));
                stats.put("total_games", rs.getLong("total_games"));
                stats.put("average_elo", Math.round(rs.getDouble("average_elo")));
                stats.put("highest_elo", rs.getInt("highest_elo"));
                stats.put("highest_streak", rs.getInt("highest_streak"));
                stats.put("total_playtime_hours", rs.getLong("total_playtime") / 3600L);
                return stats;
            }
            catch (SQLException e) {
                return null;
            }
        }, new Object[0]).orElse(Collections.emptyMap());
    }

    public Map<String, Integer> getEloDistribution() throws SQLException {
        String sql = "SELECT\n    CASE\n        WHEN elo >= 2000 THEN 'Grandmaster'\n        WHEN elo >= 1800 THEN 'Master'\n        WHEN elo >= 1600 THEN 'Diamond'\n        WHEN elo >= 1400 THEN 'Platinum'\n        WHEN elo >= 1200 THEN 'Gold'\n        WHEN elo >= 1000 THEN 'Silver'\n        WHEN elo >= 800 THEN 'Bronze'\n        ELSE 'Unranked'\n    END as bracket,\n    COUNT(*) as count\nFROM %s\nGROUP BY bracket\nORDER BY MIN(elo) DESC\n".formatted(this.tableName);
        LinkedHashMap<String, Integer> distribution = new LinkedHashMap<String, Integer>();
        List<Map.Entry> entries = this.databaseManager.executeQueryList(sql, rs -> {
            try {
                return Map.entry(rs.getString("bracket"), rs.getInt("count"));
            }
            catch (SQLException e) {
                return null;
            }
        }, new Object[0]);
        for (Map.Entry entry : entries) {
            if (entry == null) continue;
            distribution.put((String)entry.getKey(), (Integer)entry.getValue());
        }
        return distribution;
    }

    private void cacheStats(PlayerStats stats) {
        this.statsCache.put(stats.getUuid(), new CachedStats(stats));
    }

    public void invalidateCache(UUID uuid) {
        this.statsCache.remove(uuid);
    }

    public void clearCache() {
        this.statsCache.clear();
        this.leaderboardCache.clear();
        this.lastLeaderboardUpdate = 0L;
    }

    public int getCacheSize() {
        return this.statsCache.size();
    }

    public Optional<PlayerStats> getCached(UUID uuid) {
        CachedStats cached = this.statsCache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            return Optional.of(cached.stats);
        }
        return Optional.empty();
    }

    private void startCacheCleanupTask() {
        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, () -> {
            long now = System.currentTimeMillis();
            int removed = 0;
            Iterator<Map.Entry<UUID, CachedStats>> iterator = this.statsCache.entrySet().iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().getValue().isExpired()) continue;
                iterator.remove();
                ++removed;
            }
            if (removed > 0 && this.plugin.getConfig().getBoolean("general.debug")) {
                this.plugin.getLogger().info("Cleaned " + removed + " expired cache entries");
            }
        }, 6000L, 6000L);
    }

    private void startBatchSaveTask() {
        int intervalTicks = this.plugin.getConfig().getInt("general.auto-save-interval", 5) * 20 * 60;
        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)this.plugin, () -> {
            int saved = this.savePendingUpdates();
            if (saved > 0) {
                this.plugin.getLogger().info("Auto-saved " + saved + " player stats");
            }
        }, (long)intervalTicks, (long)intervalTicks);
    }

    public void shutdown() {
        this.plugin.getLogger().info("Shutting down PlayerStatsDAO...");
        int saved = this.savePendingUpdates();
        this.plugin.getLogger().info("Saved " + saved + " pending player stats");
        this.clearCache();
        this.plugin.getLogger().info("PlayerStatsDAO shut down successfully.");
    }

    private static class CachedStats {
        final PlayerStats stats;
        final long cachedAt;

        CachedStats(PlayerStats stats) {
            this.stats = stats;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - this.cachedAt > CACHE_EXPIRY_MS;
        }
    }

    public static enum LeaderboardType {
        KILLS("kills", "kills DESC"),
        DEATHS("deaths", "deaths DESC"),
        WINS("wins", "wins DESC"),
        LOSSES("losses", "losses DESC"),
        ELO("elo", "elo DESC"),
        WIN_STREAK("win_streak", "best_win_streak DESC"),
        KD_RATIO("kd", "CASE WHEN deaths = 0 THEN kills ELSE (kills * 1.0 / deaths) END DESC"),
        WIN_RATE("win_rate", "CASE WHEN games_played = 0 THEN 0 ELSE (wins * 1.0 / games_played) END DESC"),
        GAMES_PLAYED("games_played", "games_played DESC"),
        FFA_KILLS("ffa_kills", "ffa_kills DESC"),
        PLAYTIME("playtime", "total_playtime DESC");

        private final String key;
        private final String orderBy;

        private LeaderboardType(String key, String orderBy) {
            this.key = key;
            this.orderBy = orderBy;
        }

        public String getKey() {
            return this.key;
        }

        public String getOrderBy() {
            return this.orderBy;
        }
    }
}

