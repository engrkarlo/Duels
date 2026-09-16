/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.database;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.database.ConnectionPoolManager;
import com.ultimateduels.database.PlayerStatsDAO;
import com.ultimateduels.database.models.PlayerStats;
import com.ultimateduels.player.PlayerData;
import com.ultimateduels.settings.PlayerSettings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseManager {
    private final UltimateDuels plugin;
    private ConnectionPoolManager poolManager;
    private PlayerStatsDAO statsDAO;
    private String tablePrefix;
    private DatabaseType databaseType;
    private final Map<UUID, com.ultimateduels.stats.PlayerStats> statsCache;
    private final Map<UUID, PlayerSettings> settingsCache;
    private final Map<UUID, com.ultimateduels.stats.PlayerStats> pendingStatsUpdates;
    private final Object updateLock = new Object();
    private long totalQueries = 0L;
    private long failedQueries = 0L;
    private long cacheHits = 0L;
    private long cacheMisses = 0L;
    public static final String TABLE_PLAYER_STATS = "player_stats";
    public static final String TABLE_PLAYER_SETTINGS = "player_settings";
    public static final String TABLE_MATCH_HISTORY = "match_history";
    public static final String TABLE_KIT_STATS = "kit_stats";
    public static final String TABLE_PARTY_DATA = "party_data";

    public DatabaseManager(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.statsCache = new ConcurrentHashMap<UUID, com.ultimateduels.stats.PlayerStats>();
        this.settingsCache = new ConcurrentHashMap<UUID, PlayerSettings>();
        this.pendingStatsUpdates = new ConcurrentHashMap<UUID, com.ultimateduels.stats.PlayerStats>();
    }

    public boolean initialize() {
        this.plugin.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        this.plugin.getLogger().info("  Initializing Database System");
        this.plugin.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        ConfigurationSection dbSection = this.plugin.getConfig().getConfigurationSection("database");
        if (dbSection == null) {
            this.plugin.getLogger().severe("Database configuration section not found!");
            return false;
        }
        String typeString = dbSection.getString("type", "SQLITE").toUpperCase();
        try {
            this.databaseType = DatabaseType.valueOf(typeString);
        }
        catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("Invalid database type '" + typeString + "', falling back to SQLite");
            this.databaseType = DatabaseType.SQLITE;
        }
        this.tablePrefix = dbSection.getString("table-prefix", "ud_");
        this.plugin.getLogger().info("  Database Type: " + this.databaseType.getDisplayName());
        this.plugin.getLogger().info("  Table Prefix: " + this.tablePrefix);
        this.poolManager = new ConnectionPoolManager(this.plugin);
        if (!this.poolManager.initialize()) {
            this.plugin.getLogger().severe("Failed to initialize database connection pool!");
            return false;
        }
        this.plugin.getLogger().info("  \u2713 Connection pool initialized");
        if (!this.createDatabaseSchema()) {
            this.plugin.getLogger().severe("Failed to create database schema!");
            return false;
        }
        this.plugin.getLogger().info("  \u2713 Database schema created");
        this.statsDAO = new PlayerStatsDAO(this.plugin, this);
        this.plugin.getLogger().info("  \u2713 Data Access Objects initialized");
        this.startBatchUpdateTask();
        this.plugin.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        this.plugin.getLogger().info("  Database System Ready!");
        this.plugin.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        return true;
    }

    private boolean createDatabaseSchema() {
        if (this.databaseType.isNoSQL()) {
            return this.createMongoDBSchema();
        }
        return this.createSQLSchema();
    }

    private boolean createSQLSchema() {
        boolean bl;
        block14: {
            ArrayList<String> tables = new ArrayList<String>();
            tables.add(this.createPlayerStatsTable());
            tables.add(this.createPlayerSettingsTable());
            tables.add(this.createMatchHistoryTable());
            tables.add(this.createKitStatsTable());
            tables.add(this.createPartyDataTable());
            Connection conn = this.getConnection();
            try {
                for (String sql : tables) {
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    try {
                        stmt.executeUpdate();
                    }
                    finally {
                        if (stmt == null) continue;
                        stmt.close();
                    }
                }
                this.plugin.getLogger().info("  \u2713 Created " + tables.size() + " SQL tables");
                bl = true;
                if (conn == null) break block14;
            }
            catch (Throwable throwable) {
                try {
                    if (conn != null) {
                        try {
                            conn.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (SQLException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to create SQL schema", e);
                    return false;
                }
            }
            conn.close();
        }
        return bl;
    }

    private String createPlayerStatsTable() {
        String autoIncrement = this.getAutoIncrementSyntax();
        String textType = this.getTextType();
        String timestampDefault = this.getTimestampDefault();
        return "CREATE TABLE IF NOT EXISTS " + this.tablePrefix + "player_stats (id " + autoIncrement + " PRIMARY KEY, uuid VARCHAR(36) NOT NULL UNIQUE, player_name VARCHAR(16) NOT NULL, total_kills INT NOT NULL DEFAULT 0, total_deaths INT NOT NULL DEFAULT 0, total_wins INT NOT NULL DEFAULT 0, total_losses INT NOT NULL DEFAULT 0, global_elo INT NOT NULL DEFAULT 1000, current_win_streak INT NOT NULL DEFAULT 0, best_win_streak INT NOT NULL DEFAULT 0, total_matches INT NOT NULL DEFAULT 0, ranked_matches INT NOT NULL DEFAULT 0, unranked_matches INT NOT NULL DEFAULT 0, ffa_kills INT NOT NULL DEFAULT 0, ffa_deaths INT NOT NULL DEFAULT 0, playtime_seconds BIGINT NOT NULL DEFAULT 0, first_join " + timestampDefault + ", last_seen " + timestampDefault + ", kit_stats " + textType + ", CONSTRAINT unique_uuid UNIQUE (uuid))";
    }

    private String createPlayerSettingsTable() {
        String autoIncrement = this.getAutoIncrementSyntax();
        String timestampDefault = this.getTimestampDefault();
        return "CREATE TABLE IF NOT EXISTS " + this.tablePrefix + "player_settings (id " + autoIncrement + " PRIMARY KEY, uuid VARCHAR(36) NOT NULL UNIQUE, scoreboard_enabled BOOLEAN NOT NULL DEFAULT TRUE, duel_requests_enabled BOOLEAN NOT NULL DEFAULT TRUE, party_invites_enabled BOOLEAN NOT NULL DEFAULT TRUE, spectators_allowed BOOLEAN NOT NULL DEFAULT TRUE, death_messages_enabled BOOLEAN NOT NULL DEFAULT TRUE, sounds_enabled BOOLEAN NOT NULL DEFAULT TRUE, auto_requeue_enabled BOOLEAN NOT NULL DEFAULT FALSE, show_ping BOOLEAN NOT NULL DEFAULT TRUE, updated_at " + timestampDefault + ", CONSTRAINT unique_settings_uuid UNIQUE (uuid))";
    }

    private String createMatchHistoryTable() {
        String autoIncrement = this.getAutoIncrementSyntax();
        String textType = this.getTextType();
        String timestampDefault = this.getTimestampDefault();
        return "CREATE TABLE IF NOT EXISTS " + this.tablePrefix + "match_history (id " + autoIncrement + " PRIMARY KEY, match_id VARCHAR(36) NOT NULL, match_type VARCHAR(20) NOT NULL, kit_name VARCHAR(50) NOT NULL, arena_name VARCHAR(50) NOT NULL, winner_uuid VARCHAR(36), loser_uuid VARCHAR(36), winner_name VARCHAR(16), loser_name VARCHAR(16), rounds_won INT NOT NULL DEFAULT 0, rounds_lost INT NOT NULL DEFAULT 0, duration_seconds INT NOT NULL DEFAULT 0, elo_change INT NOT NULL DEFAULT 0, match_date " + timestampDefault + ", match_data " + textType + ")";
    }

    private String createKitStatsTable() {
        String autoIncrement = this.getAutoIncrementSyntax();
        return "CREATE TABLE IF NOT EXISTS " + this.tablePrefix + "kit_stats (id " + autoIncrement + " PRIMARY KEY, uuid VARCHAR(36) NOT NULL, kit_name VARCHAR(50) NOT NULL, kills INT NOT NULL DEFAULT 0, deaths INT NOT NULL DEFAULT 0, wins INT NOT NULL DEFAULT 0, losses INT NOT NULL DEFAULT 0, matches INT NOT NULL DEFAULT 0, elo INT NOT NULL DEFAULT 1000, best_win_streak INT NOT NULL DEFAULT 0, CONSTRAINT unique_player_kit UNIQUE (uuid, kit_name))";
    }

    private String createPartyDataTable() {
        String autoIncrement = this.getAutoIncrementSyntax();
        String textType = this.getTextType();
        String timestampDefault = this.getTimestampDefault();
        return "CREATE TABLE IF NOT EXISTS " + this.tablePrefix + "party_data (id " + autoIncrement + " PRIMARY KEY, party_id VARCHAR(36) NOT NULL UNIQUE, leader_uuid VARCHAR(36) NOT NULL, members " + textType + ", created_at " + timestampDefault + ", updated_at " + timestampDefault + ")";
    }

    private boolean createMongoDBSchema() {
        this.plugin.getLogger().info("  MongoDB schema creation - collections auto-created on first insert");
        this.plugin.getLogger().info("  \u2713 MongoDB ready");
        return true;
    }

    private String getAutoIncrementSyntax() {
        return switch (this.databaseType.ordinal()) {
            case 2 -> "SERIAL";
            case 4 -> "INTEGER";
            default -> "INT AUTO_INCREMENT";
        };
    }

    private String getTextType() {
        return switch (this.databaseType.ordinal()) {
            case 2, 4 -> "TEXT";
            default -> "LONGTEXT";
        };
    }

    private String getTimestampDefault() {
        return switch (this.databaseType.ordinal()) {
            case 4 -> "DATETIME DEFAULT CURRENT_TIMESTAMP";
            default -> "TIMESTAMP DEFAULT CURRENT_TIMESTAMP";
        };
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        ++this.totalQueries;
        return this.poolManager.getConnection();
    }

    public void closeConnection(@Nullable Connection connection) {
        if (connection != null) {
            this.poolManager.closeConnection(connection);
        }
    }

    public boolean isHealthy() {
        return this.poolManager != null && this.poolManager.isHealthy();
    }

    @NotNull
    public DatabaseType getDatabaseType() {
        return this.databaseType;
    }

    @NotNull
    public String getTablePrefix() {
        return this.tablePrefix;
    }

    @NotNull
    public ConnectionPoolManager getPoolManager() {
        return this.poolManager;
    }

    @NotNull
    public ConnectionPoolManager getConnectionPool() {
        return this.poolManager;
    }

    @NotNull
    public String getTable(@NotNull String tableName) {
        return this.tablePrefix + tableName;
    }

    /*
     * Loose catch block
     */
    public long executeInsert(@NotNull String sql, Object ... params) throws SQLException {
        try (Connection conn = this.getConnection();){
            long l;
            block23: {
                ResultSet rs;
                PreparedStatement stmt;
                block20: {
                    long l2;
                    block22: {
                        block21: {
                            stmt = conn.prepareStatement(sql, 1);
                            this.setParameters(stmt, params);
                            stmt.executeUpdate();
                            rs = stmt.getGeneratedKeys();
                            if (!rs.next()) break block20;
                            l2 = rs.getLong(1);
                            if (rs == null) break block21;
                            rs.close();
                        }
                        if (stmt == null) break block22;
                        stmt.close();
                    }
                    return l2;
                }
                try {
                    block24: {
                        if (rs != null) {
                            rs.close();
                        }
                        break block24;
                        {
                            catch (Throwable throwable) {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    }
                                    catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            }
                        }
                    }
                    l = -1L;
                    if (stmt == null) break block23;
                }
                catch (Throwable throwable) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (Throwable throwable3) {
                            throwable.addSuppressed(throwable3);
                        }
                    }
                    throw throwable;
                }
                stmt.close();
            }
            return l;
        }
    }

    public int executeUpdate(@NotNull String sql, Object ... params) throws SQLException {
        try (Connection conn = this.getConnection();){
            int n;
            block12: {
                PreparedStatement stmt = conn.prepareStatement(sql);
                try {
                    this.setParameters(stmt, params);
                    n = stmt.executeUpdate();
                    if (stmt == null) break block12;
                }
                catch (Throwable throwable) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                stmt.close();
            }
            return n;
        }
    }

    /*
     * Loose catch block
     */
    @NotNull
    public <T> Optional<T> executeQuerySingle(@NotNull String sql, @NotNull ResultSetMapper<T> mapper, Object ... params) throws SQLException {
        try (Connection conn = this.getConnection();){
            Optional optional;
            block23: {
                ResultSet rs;
                PreparedStatement stmt;
                block20: {
                    Optional<T> optional2;
                    block22: {
                        block21: {
                            stmt = conn.prepareStatement(sql);
                            this.setParameters(stmt, params);
                            rs = stmt.executeQuery();
                            if (!rs.next()) break block20;
                            optional2 = Optional.ofNullable(mapper.map(rs));
                            if (rs == null) break block21;
                            rs.close();
                        }
                        if (stmt == null) break block22;
                        stmt.close();
                    }
                    return optional2;
                }
                try {
                    block24: {
                        if (rs != null) {
                            rs.close();
                        }
                        break block24;
                        {
                            catch (Throwable throwable) {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    }
                                    catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            }
                        }
                    }
                    optional = Optional.empty();
                    if (stmt == null) break block23;
                }
                catch (Throwable throwable) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (Throwable throwable3) {
                            throwable.addSuppressed(throwable3);
                        }
                    }
                    throw throwable;
                }
                stmt.close();
            }
            return optional;
        }
    }

    @NotNull
    public <T> List<T> executeQueryList(@NotNull String sql, @NotNull ResultSetMapper<T> mapper, Object ... params) throws SQLException {
        ArrayList<T> results = new ArrayList<T>();
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);){
            this.setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery();){
                while (rs.next()) {
                    T item = mapper.map(rs);
                    if (item == null) continue;
                    results.add(item);
                }
            }
        }
        return results;
    }

    public int[] executeBatch(@NotNull String sql, @NotNull List<Object[]> batchParams) throws SQLException {
        try (Connection conn = this.getConnection();){
            Object object;
            block13: {
                PreparedStatement stmt = conn.prepareStatement(sql);
                try {
                    for (Object[] params : batchParams) {
                        this.setParameters(stmt, params);
                        stmt.addBatch();
                    }
                    object = stmt.executeBatch();
                    if (stmt == null) break block13;
                }
                catch (Throwable throwable) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                stmt.close();
            }
            return object;
        }
    }

    private void setParameters(@NotNull PreparedStatement stmt, Object ... params) throws SQLException {
        for (int i = 0; i < params.length; ++i) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    @NotNull
    public CompletableFuture<com.ultimateduels.stats.PlayerStats> loadPlayerStats(@NotNull UUID uuid) {
        if (this.statsCache.containsKey(uuid)) {
            ++this.cacheHits;
            return CompletableFuture.completedFuture(this.statsCache.get(uuid));
        }
        ++this.cacheMisses;
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerStats dbStats = this.statsDAO.findById(uuid).orElse(null);
                com.ultimateduels.stats.PlayerStats stats = dbStats != null ? this.convertFromDbModel(dbStats) : new com.ultimateduels.stats.PlayerStats(uuid);
                this.statsCache.put(uuid, stats);
                return stats;
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load stats for " + String.valueOf(uuid), e);
                ++this.failedQueries;
                return new com.ultimateduels.stats.PlayerStats(uuid);
            }
        }, this.plugin.getTaskScheduler().getAsyncExecutor());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NotNull
    public CompletableFuture<Boolean> savePlayerStats(@NotNull com.ultimateduels.stats.PlayerStats stats) {
        Object object = this.updateLock;
        synchronized (object) {
            this.pendingStatsUpdates.put(stats.getPlayerUUID(), stats);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean success = this.trySaveStats(stats);
                if (success) {
                    this.statsCache.put(stats.getPlayerUUID(), stats);
                } else {
                    ++this.failedQueries;
                }
                return success;
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save stats for " + String.valueOf(stats.getPlayerUUID()), e);
                ++this.failedQueries;
                return false;
            }
        }, this.plugin.getTaskScheduler().getAsyncExecutor());
    }

    @Nullable
    public com.ultimateduels.stats.PlayerStats getPlayerStatsFromCache(@NotNull UUID uuid) {
        return this.statsCache.get(uuid);
    }

    public void updateStatsCache(@NotNull com.ultimateduels.stats.PlayerStats stats) {
        this.statsCache.put(stats.getPlayerUUID(), stats);
    }

    public void removeStatsFromCache(@NotNull UUID uuid) {
        this.statsCache.remove(uuid);
    }

    @Nullable
    public PlayerData loadPlayerData(@NotNull UUID uuid) {
        return null;
    }

    public void savePlayerData(@NotNull PlayerData data) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.plugin.getLogger().fine("Saving player data for " + String.valueOf(data.getUuid()));
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + String.valueOf(data.getUuid()), e);
            }
        });
    }

    public void savePlayerDataAsync(@NotNull PlayerData data) {
        this.plugin.getTaskScheduler().runTaskAsync(() -> this.savePlayerData(data));
    }

    @NotNull
    private com.ultimateduels.stats.PlayerStats convertFromDbModel(@NotNull PlayerStats dbStats) {
        com.ultimateduels.stats.PlayerStats stats = new com.ultimateduels.stats.PlayerStats(dbStats.getUuid(), dbStats.getUsername());
        stats.setGlobalElo(dbStats.getElo());
        stats.setTotalWins(dbStats.getWins());
        stats.setTotalLosses(dbStats.getLosses());
        stats.setTotalKills(dbStats.getKills());
        stats.setTotalDeaths(dbStats.getDeaths());
        stats.setTotalMatchesPlayed(dbStats.getGamesPlayed());
        stats.setCurrentWinStreak(dbStats.getWinStreak());
        stats.setBestWinStreak(dbStats.getBestWinStreak());
        if (dbStats.getFirstJoin() != null) {
            stats.setFirstJoin(dbStats.getFirstJoin().getTime());
        }
        if (dbStats.getLastSeen() != null) {
            stats.setLastSeen(dbStats.getLastSeen().getTime());
        }
        return stats;
    }

    @NotNull
    private PlayerStats convertToDbModel(@NotNull com.ultimateduels.stats.PlayerStats stats) {
        return new PlayerStats.Builder(stats.getPlayerUUID()).username(stats.getPlayerName()).kills(stats.getTotalKills()).deaths(stats.getTotalDeaths()).wins(stats.getTotalWins()).losses(stats.getTotalLosses()).winStreak(stats.getCurrentWinStreak()).bestWinStreak(stats.getBestWinStreak()).elo(stats.getGlobalElo()).highestElo(stats.getGlobalElo()).gamesPlayed(stats.getTotalMatchesPlayed()).build();
    }

    private boolean trySaveStats(@NotNull com.ultimateduels.stats.PlayerStats stats) {
        try {
            PlayerStats dbStats = this.convertToDbModel(stats);
            if (this.statsDAO.exists(dbStats.getUuid())) {
                this.statsDAO.update(dbStats);
            } else {
                this.statsDAO.create(dbStats);
            }
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save stats for " + String.valueOf(stats.getPlayerUUID()), e);
            return false;
        }
    }

    @NotNull
    public CompletableFuture<PlayerSettings> loadPlayerSettings(@NotNull UUID uuid) {
        if (this.settingsCache.containsKey(uuid)) {
            ++this.cacheHits;
            return CompletableFuture.completedFuture(this.settingsCache.get(uuid));
        }
        ++this.cacheMisses;
        return CompletableFuture.supplyAsync(() -> {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }, this.plugin.getTaskScheduler().getAsyncExecutor());
    }

    @NotNull
    public CompletableFuture<Boolean> savePlayerSettings(@NotNull PlayerSettings settings) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = this.getConnection();){
                Boolean bl;
                block14: {
                    String sql = "INSERT INTO " + this.tablePrefix + "player_settings (uuid, scoreboard_enabled, duel_requests_enabled, party_invites_enabled, spectators_allowed, death_messages_enabled, sounds_enabled, auto_requeue_enabled, show_ping) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE scoreboard_enabled = VALUES(scoreboard_enabled), duel_requests_enabled = VALUES(duel_requests_enabled), party_invites_enabled = VALUES(party_invites_enabled), spectators_allowed = VALUES(spectators_allowed), death_messages_enabled = VALUES(death_messages_enabled), sounds_enabled = VALUES(sounds_enabled), auto_requeue_enabled = VALUES(auto_requeue_enabled), show_ping = VALUES(show_ping)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    try {
                        stmt.setString(1, settings.getUuid().toString());
                        stmt.setBoolean(2, settings.isScoreboardEnabled());
                        stmt.setBoolean(3, settings.isDuelRequestsEnabled());
                        stmt.setBoolean(4, settings.isPartyInvitesEnabled());
                        stmt.setBoolean(5, settings.isAllowSpectatorsEnabled());
                        stmt.setBoolean(6, settings.isDeathMessagesEnabled());
                        stmt.setBoolean(7, settings.isSoundsEnabled());
                        stmt.setBoolean(8, settings.isAutoRequeueEnabled());
                        stmt.setBoolean(9, settings.isShowPingEnabled());
                        stmt.executeUpdate();
                        this.settingsCache.put(settings.getUuid(), settings);
                        bl = true;
                        if (stmt == null) break block14;
                    }
                    catch (Throwable throwable) {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    stmt.close();
                }
                return bl;
            }
            catch (SQLException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to save settings for " + String.valueOf(settings.getUuid()), e);
                ++this.failedQueries;
                return false;
            }
        }, this.plugin.getTaskScheduler().getAsyncExecutor());
    }

    private void startBatchUpdateTask() {
        this.plugin.getTaskScheduler().runTaskTimerAsync(() -> {
            Object object = this.updateLock;
            synchronized (object) {
                if (this.pendingStatsUpdates.isEmpty()) {
                    return;
                }
                int saved = 0;
                int failed = 0;
                for (com.ultimateduels.stats.PlayerStats stats : new ArrayList<com.ultimateduels.stats.PlayerStats>(this.pendingStatsUpdates.values())) {
                    if (this.trySaveStats(stats)) {
                        ++saved;
                        continue;
                    }
                    ++failed;
                }
                this.pendingStatsUpdates.clear();
                if (saved > 0 || failed > 0) {
                    this.plugin.getLogger().info("Batch stats update: " + saved + " saved, " + failed + " failed");
                }
            }
        }, 6000L, 6000L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void forceSaveAll() {
        this.plugin.getLogger().info("Force saving all pending database updates...");
        Object object = this.updateLock;
        synchronized (object) {
            int saved = 0;
            int failed = 0;
            for (com.ultimateduels.stats.PlayerStats stats : this.pendingStatsUpdates.values()) {
                if (this.trySaveStats(stats)) {
                    ++saved;
                    continue;
                }
                ++failed;
            }
            for (com.ultimateduels.stats.PlayerStats stats : this.statsCache.values()) {
                if (this.pendingStatsUpdates.containsKey(stats.getPlayerUUID())) continue;
                if (this.trySaveStats(stats)) {
                    ++saved;
                    continue;
                }
                ++failed;
            }
            this.pendingStatsUpdates.clear();
            this.plugin.getLogger().info("Force save complete: " + saved + " saved, " + failed + " failed");
        }
    }

    @NotNull
    public Map<String, Object> getStatistics() {
        HashMap<String, Object> stats = new HashMap<String, Object>();
        stats.put("databaseType", this.databaseType.getDisplayName());
        stats.put("totalQueries", this.totalQueries);
        stats.put("failedQueries", this.failedQueries);
        stats.put("cacheHits", this.cacheHits);
        stats.put("cacheMisses", this.cacheMisses);
        stats.put("cacheSize", this.statsCache.size());
        stats.put("pendingUpdates", this.pendingStatsUpdates.size());
        if (this.poolManager != null) {
            ConnectionPoolManager.PoolStatistics poolStats = this.poolManager.getStatistics();
            stats.put("activeConnections", poolStats.getActiveConnections());
            stats.put("idleConnections", poolStats.getIdleConnections());
            stats.put("totalConnections", poolStats.getTotalConnections());
            stats.put("waitingThreads", poolStats.getWaitingThreads());
        }
        return stats;
    }

    public void clearCaches() {
        this.statsCache.clear();
        this.settingsCache.clear();
        this.plugin.getLogger().info("Database caches cleared");
    }

    public int getCurrentSchemaVersion() {
        try {
            String sql = "SELECT MAX(version) FROM " + this.tablePrefix + "schema_version";
            return this.executeQuerySingle(sql, rs -> {
                try {
                    return rs.getInt(1);
                }
                catch (SQLException e) {
                    return 0;
                }
            }, new Object[0]).orElse(0);
        }
        catch (SQLException e) {
            this.plugin.getLogger().fine("Schema version table not found, returning 0");
            return 0;
        }
    }

    public void shutdown() {
        this.plugin.getLogger().info("Shutting down database manager...");
        this.forceSaveAll();
        if (this.poolManager != null) {
            this.poolManager.shutdown();
        }
        this.statsCache.clear();
        this.settingsCache.clear();
        this.pendingStatsUpdates.clear();
        this.plugin.getLogger().info("Database manager shutdown complete");
    }

    public static enum DatabaseType {
        MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", 3306),
        MARIADB("MariaDB", "org.mariadb.jdbc.Driver", 3306),
        POSTGRESQL("PostgreSQL", "org.postgresql.Driver", 5432),
        MONGODB("MongoDB", "mongodb.MongoClient", 27017),
        SQLITE("SQLite", "org.sqlite.JDBC", 0);

        private final String displayName;
        private final String driverClass;
        private final int defaultPort;

        private DatabaseType(String displayName, String driverClass, int defaultPort) {
            this.displayName = displayName;
            this.driverClass = driverClass;
            this.defaultPort = defaultPort;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getDriverClass() {
            return this.driverClass;
        }

        public int getDefaultPort() {
            return this.defaultPort;
        }

        public boolean isSQL() {
            return this != MONGODB;
        }

        public boolean isNoSQL() {
            return this == MONGODB;
        }
    }

    @FunctionalInterface
    public static interface ResultSetMapper<T> {
        @Nullable
        public T map(@NotNull ResultSet var1) throws SQLException;
    }
}

