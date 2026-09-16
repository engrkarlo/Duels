/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.ConfigurationSection
 */
package com.ultimateduels.database;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.libs.hikari.HikariConfig;
import com.ultimateduels.libs.hikari.HikariDataSource;
import com.ultimateduels.libs.hikari.pool.HikariPool;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;

public class ConnectionPoolManager {
    private final UltimateDuels plugin;
    private HikariDataSource dataSource;
    private DatabaseType databaseType;
    private final AtomicLong totalConnectionsCreated = new AtomicLong(0L);
    private final AtomicLong totalConnectionsClosed = new AtomicLong(0L);
    private volatile long lastHealthCheck = 0L;
    private volatile boolean isRecovering = false;
    private final Object recoveryLock = new Object();

    public ConnectionPoolManager(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        ConfigurationSection dbSection = this.plugin.getConfig().getConfigurationSection("database");
        if (dbSection == null) {
            this.plugin.getLogger().severe("Database configuration section not found!");
            return false;
        }
        String type = dbSection.getString("type", "SQLITE").toUpperCase();
        try {
            this.databaseType = DatabaseType.valueOf(type);
        }
        catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("Invalid database type '" + type + "', defaulting to SQLite");
            this.databaseType = DatabaseType.SQLITE;
        }
        this.plugin.getLogger().info("Initializing " + this.databaseType.getDisplayName() + " connection pool...");
        try {
            switch (this.databaseType.ordinal()) {
                case 0: {
                    if (!this.initializeMySQL(dbSection.getConfigurationSection("mysql"))) {
                        this.plugin.getLogger().warning("MySQL initialization failed, falling back to SQLite");
                        this.databaseType = DatabaseType.SQLITE;
                        return this.initializeSQLite(dbSection.getConfigurationSection("sqlite"));
                    }
                    return true;
                }
            }
            return this.initializeSQLite(dbSection.getConfigurationSection("sqlite"));
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to initialize database connection pool", e);
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean initializeMySQL(ConfigurationSection mysqlSection) {
        if (mysqlSection == null) {
            this.plugin.getLogger().severe("MySQL configuration section not found!");
            return false;
        }
        String host = mysqlSection.getString("host", "localhost");
        int port = mysqlSection.getInt("port", 3306);
        String database = mysqlSection.getString("database", "ultimateduels");
        String username = mysqlSection.getString("username", "root");
        String password = mysqlSection.getString("password", "");
        ConfigurationSection poolSection = mysqlSection.getConfigurationSection("pool");
        int maxPoolSize = poolSection != null ? poolSection.getInt("maximum-pool-size", 10) : 10;
        int minIdle = poolSection != null ? poolSection.getInt("minimum-idle", 5) : 5;
        long connectionTimeout = poolSection != null ? poolSection.getLong("connection-timeout", 30000L) : 30000L;
        long idleTimeout = poolSection != null ? poolSection.getLong("idle-timeout", 600000L) : 600000L;
        long maxLifetime = poolSection != null ? poolSection.getLong("max-lifetime", 1800000L) : 1800000L;
        boolean useSSL = mysqlSection.getBoolean("use-ssl", false);
        boolean verifyServerCert = mysqlSection.getBoolean("verify-server-certificate", false);
        HikariConfig config = new HikariConfig();
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&verifyServerCertificate=%b&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true", host, port, database, useSSL, verifyServerCert);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setPoolName("UltimateDuels-MySQL-Pool");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.setConnectionTestQuery("SELECT 1");
        if (this.plugin.getConfig().getBoolean("general.debug", false)) {
            config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(60L));
        }
        try {
            this.dataSource = new HikariDataSource(config);
            try (Connection conn = this.dataSource.getConnection();){
                if (!conn.isValid(5)) return false;
                this.plugin.getLogger().info("Successfully connected to MySQL database!");
                this.plugin.getLogger().info("  Host: " + host + ":" + port);
                this.plugin.getLogger().info("  Database: " + database);
                this.plugin.getLogger().info("  Pool Size: " + maxPoolSize);
                boolean bl = true;
                return bl;
            }
        }
        catch (HikariPool.PoolInitializationException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to initialize MySQL connection pool", e);
            return false;
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to validate MySQL connection", e);
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean initializeSQLite(ConfigurationSection sqliteSection) {
        String fileName = sqliteSection != null ? sqliteSection.getString("file", "data/ultimateduels.db") : "data/ultimateduels.db";
        File dataFolder = this.plugin.getDataFolder();
        if (!this.ensureDirectoryExists(dataFolder)) {
            this.plugin.getLogger().severe("Failed to create plugin data folder: " + dataFolder.getAbsolutePath());
            return false;
        }
        File dbFile = new File(dataFolder, fileName);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !this.ensureDirectoryExists(parentDir)) {
            this.plugin.getLogger().severe("Failed to create database directory: " + parentDir.getAbsolutePath());
            return false;
        }
        if (parentDir != null && !parentDir.canWrite()) {
            this.plugin.getLogger().severe("Database directory is not writable: " + parentDir.getAbsolutePath());
            return false;
        }
        this.plugin.getLogger().info("Database file path: " + dbFile.getAbsolutePath());
        this.plugin.getLogger().info("Database directory exists: " + (parentDir != null && parentDir.exists()));
        this.plugin.getLogger().info("Database directory writable: " + (parentDir != null && parentDir.canWrite()));
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000L);
        config.setIdleTimeout(600000L);
        config.setMaxLifetime(1800000L);
        config.setPoolName("UltimateDuels-SQLite-Pool");
        config.setConnectionTestQuery("SELECT 1");
        config.setConnectionInitSql("PRAGMA foreign_keys = ON; PRAGMA journal_mode = WAL; PRAGMA synchronous = NORMAL; PRAGMA cache_size = 10000; PRAGMA temp_store = MEMORY;");
        config.setAutoCommit(true);
        if (this.plugin.getConfig().getBoolean("general.debug", false)) {
            config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(60L));
        }
        try {
            this.dataSource = new HikariDataSource(config);
            try (Connection conn = this.dataSource.getConnection();){
                if (conn.isValid(5)) {
                    this.plugin.getLogger().info("Successfully connected to SQLite database!");
                    this.plugin.getLogger().info("  File: " + dbFile.getAbsolutePath());
                    this.plugin.getLogger().info("  File exists: " + dbFile.exists());
                    boolean bl2 = true;
                    return bl2;
                }
                this.plugin.getLogger().severe("SQLite connection validation failed!");
                boolean bl = false;
                return bl;
            }
        }
        catch (HikariPool.PoolInitializationException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to initialize SQLite connection pool", e);
            this.plugin.getLogger().severe("Database file path was: " + dbFile.getAbsolutePath());
            this.plugin.getLogger().severe("Directory exists: " + (parentDir != null && parentDir.exists()));
            this.plugin.getLogger().severe("Directory writable: " + (parentDir != null && parentDir.canWrite()));
            return false;
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to validate SQLite connection", e);
            return false;
        }
    }

    private boolean ensureDirectoryExists(File directory) {
        if (directory == null) {
            return false;
        }
        if (directory.exists()) {
            if (directory.isDirectory()) {
                return true;
            }
            this.plugin.getLogger().severe("Path exists but is not a directory: " + directory.getAbsolutePath());
            return false;
        }
        this.plugin.getLogger().info("Creating directory: " + directory.getAbsolutePath());
        try {
            if (directory.mkdirs()) {
                this.plugin.getLogger().info("Successfully created directory: " + directory.getAbsolutePath());
                return true;
            }
            if (directory.exists() && directory.isDirectory()) {
                this.plugin.getLogger().info("Directory now exists: " + directory.getAbsolutePath());
                return true;
            }
            this.plugin.getLogger().severe("Failed to create directory: " + directory.getAbsolutePath());
            File parent = directory.getParentFile();
            if (parent != null) {
                this.plugin.getLogger().severe("  Parent exists: " + parent.exists());
                this.plugin.getLogger().severe("  Parent is directory: " + parent.isDirectory());
                this.plugin.getLogger().severe("  Parent writable: " + parent.canWrite());
                this.plugin.getLogger().severe("  Parent path: " + parent.getAbsolutePath());
            }
            return false;
        }
        catch (SecurityException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Security exception creating directory: " + directory.getAbsolutePath(), e);
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean ensureConnection() {
        if (this.dataSource == null) return this.attemptRecovery();
        if (this.dataSource.isClosed()) return this.attemptRecovery();
        try (Connection conn = this.dataSource.getConnection();){
            if (!conn.isValid(3)) return this.attemptRecovery();
            boolean bl = true;
            return bl;
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
        return this.attemptRecovery();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean attemptRecovery() {
        Object object = this.recoveryLock;
        synchronized (object) {
            if (this.isRecovering) {
                try {
                    this.recoveryLock.wait(30000L);
                    return this.dataSource != null && !this.dataSource.isClosed();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            this.isRecovering = true;
        }
        try {
            this.plugin.getLogger().warning("Database connection lost, attempting recovery...");
            if (this.dataSource != null) {
                try {
                    this.dataSource.close();
                }
                catch (Exception e) {
                    this.plugin.getLogger().log(Level.WARNING, "Error closing existing data source during recovery", e);
                }
                this.dataSource = null;
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean success = this.initialize();
            if (success) {
                this.plugin.getLogger().info("Database connection recovered successfully!");
            } else {
                this.plugin.getLogger().severe("Failed to recover database connection!");
            }
            boolean bl = success;
            return bl;
        }
        finally {
            Object object2 = this.recoveryLock;
            synchronized (object2) {
                this.isRecovering = false;
                this.recoveryLock.notifyAll();
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            try {
                Connection conn = this.dataSource.getConnection();
                this.totalConnectionsCreated.incrementAndGet();
                return conn;
            }
            catch (SQLException e) {
                String message = e.getMessage();
                if (message != null && (message.contains("does not exist") || message.contains("Connection is not available"))) {
                    this.plugin.getLogger().warning("Database connection error, attempting recovery: " + message);
                    if (this.attemptRecovery()) {
                        Connection conn = this.dataSource.getConnection();
                        this.totalConnectionsCreated.incrementAndGet();
                        return conn;
                    }
                }
                throw e;
            }
        }
        if (this.attemptRecovery()) {
            Connection conn = this.dataSource.getConnection();
            this.totalConnectionsCreated.incrementAndGet();
            return conn;
        }
        throw new SQLException("DataSource is not available and recovery failed");
    }

    public Connection getConnection(int timeoutSeconds) throws SQLException {
        Connection conn = this.getConnection();
        try {
            conn.setNetworkTimeout(Runnable::run, timeoutSeconds * 1000);
        }
        catch (SQLException e) {
            this.plugin.getLogger().fine("setNetworkTimeout not supported: " + e.getMessage());
        }
        return conn;
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    this.totalConnectionsClosed.incrementAndGet();
                }
            }
            catch (SQLException e) {
                this.plugin.getLogger().log(Level.WARNING, "Error closing connection", e);
            }
        }
    }

    public boolean isHealthy() {
        boolean bl;
        block9: {
            if (this.dataSource == null || this.dataSource.isClosed()) {
                return false;
            }
            Connection conn = this.dataSource.getConnection();
            try {
                boolean valid = conn.isValid(5);
                this.lastHealthCheck = System.currentTimeMillis();
                bl = valid;
                if (conn == null) break block9;
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
                    this.plugin.getLogger().log(Level.WARNING, "Database health check failed", e);
                    return false;
                }
            }
            conn.close();
        }
        return bl;
    }

    public PoolStatistics getStatistics() {
        if (this.dataSource == null || this.dataSource.isClosed()) {
            return new PoolStatistics(0, 0, 0, 0, this.totalConnectionsCreated.get(), this.totalConnectionsClosed.get());
        }
        try {
            return new PoolStatistics(this.dataSource.getHikariPoolMXBean().getActiveConnections(), this.dataSource.getHikariPoolMXBean().getIdleConnections(), this.dataSource.getHikariPoolMXBean().getTotalConnections(), this.dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(), this.totalConnectionsCreated.get(), this.totalConnectionsClosed.get());
        }
        catch (Exception e) {
            return new PoolStatistics(0, 0, 0, 0, this.totalConnectionsCreated.get(), this.totalConnectionsClosed.get());
        }
    }

    public DatabaseType getDatabaseType() {
        return this.databaseType;
    }

    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isMySQL() {
        return this.databaseType == DatabaseType.MYSQL;
    }

    public boolean isSQLite() {
        return this.databaseType == DatabaseType.SQLITE;
    }

    public long getLastHealthCheck() {
        return this.lastHealthCheck;
    }

    public boolean isAvailable() {
        return this.dataSource != null && !this.dataSource.isClosed();
    }

    public void shutdown() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.plugin.getLogger().info("Shutting down database connection pool...");
            try {
                PoolStatistics stats = this.getStatistics();
                this.plugin.getLogger().info("Final pool statistics: " + stats.toString());
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Could not get final pool statistics: " + e.getMessage());
            }
            try {
                this.dataSource.close();
                this.plugin.getLogger().info("Database connection pool shut down successfully.");
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Error shutting down connection pool", e);
            }
        }
    }

    public void evictIdleConnections() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            try {
                this.dataSource.getHikariPoolMXBean().softEvictConnections();
                this.plugin.getLogger().info("Evicted idle connections from pool.");
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Could not evict idle connections: " + e.getMessage());
            }
        }
    }

    public void suspend() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            try {
                this.dataSource.getHikariPoolMXBean().suspendPool();
                this.plugin.getLogger().warning("Connection pool suspended!");
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Could not suspend pool: " + e.getMessage());
            }
        }
    }

    public void resume() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            try {
                this.dataSource.getHikariPoolMXBean().resumePool();
                this.plugin.getLogger().info("Connection pool resumed.");
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Could not resume pool: " + e.getMessage());
            }
        }
    }

    public static enum DatabaseType {
        MYSQL("MySQL"),
        SQLITE("SQLite");

        private final String displayName;

        private DatabaseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }

    public static class PoolStatistics {
        private final int activeConnections;
        private final int idleConnections;
        private final int totalConnections;
        private final int waitingThreads;
        private final long totalCreated;
        private final long totalClosed;

        public PoolStatistics(int active, int idle, int total, int waiting, long created, long closed) {
            this.activeConnections = active;
            this.idleConnections = idle;
            this.totalConnections = total;
            this.waitingThreads = waiting;
            this.totalCreated = created;
            this.totalClosed = closed;
        }

        public int getActiveConnections() {
            return this.activeConnections;
        }

        public int getIdleConnections() {
            return this.idleConnections;
        }

        public int getTotalConnections() {
            return this.totalConnections;
        }

        public int getWaitingThreads() {
            return this.waitingThreads;
        }

        public long getTotalCreated() {
            return this.totalCreated;
        }

        public long getTotalClosed() {
            return this.totalClosed;
        }

        public String toString() {
            return String.format("PoolStats[active=%d, idle=%d, total=%d, waiting=%d]", this.activeConnections, this.idleConnections, this.totalConnections, this.waitingThreads);
        }
    }
}

