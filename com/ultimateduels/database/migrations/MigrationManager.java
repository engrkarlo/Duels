/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.database.migrations;

import com.ultimateduels.database.DatabaseManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MigrationManager {
    private final DatabaseManager databaseManager;
    private final Logger logger;
    private final List<Migration> migrations;

    public MigrationManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.logger = Logger.getLogger("UltimateDuels-Migrations");
        this.migrations = this.initializeMigrations();
    }

    private List<Migration> initializeMigrations() {
        ArrayList<Migration> list = new ArrayList<Migration>();
        list.add(new Migration(){

            @Override
            public int getVersion() {
                return 1;
            }

            @Override
            public String getDescription() {
                return "Initial schema creation";
            }

            @Override
            public void migrate(Connection conn) throws SQLException {
                MigrationManager.this.logger.info("Initial schema already created.");
            }
        });
        list.add(new Migration(){

            @Override
            public int getVersion() {
                return 2;
            }

            @Override
            public String getDescription() {
                return "Add seasonal statistics columns";
            }

            @Override
            public void migrate(Connection conn) throws SQLException {
                String tableName = MigrationManager.this.databaseManager.getTable("player_stats");
                if (!MigrationManager.this.columnExists(conn, tableName, "season_kills")) {
                    MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN season_kills INT DEFAULT 0");
                    MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN season_deaths INT DEFAULT 0");
                    MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN season_wins INT DEFAULT 0");
                    MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN season_losses INT DEFAULT 0");
                    MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN season_elo INT DEFAULT 1000");
                    MigrationManager.this.logger.info("Added seasonal statistics columns.");
                }
            }
        });
        list.add(new Migration(){

            @Override
            public int getVersion() {
                return 3;
            }

            @Override
            public String getDescription() {
                return "Add match replay data support";
            }

            @Override
            public void migrate(Connection conn) throws SQLException {
                String tableName = MigrationManager.this.databaseManager.getTable("match_history");
                if (!MigrationManager.this.columnExists(conn, tableName, "replay_data")) {
                    if (MigrationManager.this.databaseManager.getConnectionPool().isMySQL()) {
                        MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN replay_data LONGBLOB");
                    } else {
                        MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN replay_data BLOB");
                    }
                    MigrationManager.this.executeSQL(conn, "ALTER TABLE " + tableName + " ADD COLUMN replay_available BOOLEAN DEFAULT FALSE");
                    MigrationManager.this.logger.info("Added match replay data support.");
                }
            }
        });
        list.add(new Migration(){

            @Override
            public int getVersion() {
                return 4;
            }

            @Override
            public String getDescription() {
                return "Add achievement tracking table";
            }

            @Override
            public void migrate(Connection conn) throws SQLException {
                String tableName = MigrationManager.this.databaseManager.getTablePrefix() + "player_achievements";
                String sql = MigrationManager.this.databaseManager.getConnectionPool().isMySQL() ? "CREATE TABLE IF NOT EXISTS %s (\n    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n    uuid VARCHAR(36) NOT NULL,\n    achievement_id VARCHAR(64) NOT NULL,\n    progress INT DEFAULT 0,\n    completed BOOLEAN DEFAULT FALSE,\n    completed_at TIMESTAMP NULL,\n    UNIQUE KEY unique_player_achievement (uuid, achievement_id),\n    INDEX idx_uuid (uuid)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci\n".formatted(tableName) : "CREATE TABLE IF NOT EXISTS %s (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    uuid TEXT NOT NULL,\n    achievement_id TEXT NOT NULL,\n    progress INTEGER DEFAULT 0,\n    completed INTEGER DEFAULT 0,\n    completed_at TIMESTAMP NULL,\n    UNIQUE(uuid, achievement_id)\n)\n".formatted(tableName);
                MigrationManager.this.executeSQL(conn, sql);
                MigrationManager.this.logger.info("Created achievement tracking table.");
            }
        });
        list.add(new Migration(){

            @Override
            public int getVersion() {
                return 5;
            }

            @Override
            public String getDescription() {
                return "Add performance indexes";
            }

            @Override
            public void migrate(Connection conn) throws SQLException {
                String statsTable = MigrationManager.this.databaseManager.getTable("player_stats");
                String historyTable = MigrationManager.this.databaseManager.getTable("match_history");
                try {
                    MigrationManager.this.executeSQL(conn, "CREATE INDEX IF NOT EXISTS idx_stats_elo_games ON " + statsTable + " (elo DESC, games_played DESC)");
                    MigrationManager.this.executeSQL(conn, "CREATE INDEX IF NOT EXISTS idx_history_players ON " + historyTable + " (winner_uuid, loser_uuid, started_at DESC)");
                }
                catch (SQLException e) {
                    MigrationManager.this.logger.fine("Some indexes may already exist: " + e.getMessage());
                }
                MigrationManager.this.logger.info("Added performance indexes.");
            }
        });
        list.add(new Migration(){

            @Override
            public int getVersion() {
                return 6;
            }

            @Override
            public String getDescription() {
                return "Add queue statistics tracking";
            }

            @Override
            public void migrate(Connection conn) throws SQLException {
                String tableName = MigrationManager.this.databaseManager.getTablePrefix() + "queue_stats";
                String sql = MigrationManager.this.databaseManager.getConnectionPool().isMySQL() ? "CREATE TABLE IF NOT EXISTS %s (\n    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n    date DATE NOT NULL,\n    hour TINYINT NOT NULL,\n    kit VARCHAR(64) NOT NULL,\n    queue_joins INT DEFAULT 0,\n    matches_started INT DEFAULT 0,\n    avg_queue_time_seconds INT DEFAULT 0,\n    peak_queue_size INT DEFAULT 0,\n    UNIQUE KEY unique_hourly_kit (date, hour, kit),\n    INDEX idx_date (date),\n    INDEX idx_kit (kit)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci\n".formatted(tableName) : "CREATE TABLE IF NOT EXISTS %s (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    date TEXT NOT NULL,\n    hour INTEGER NOT NULL,\n    kit TEXT NOT NULL,\n    queue_joins INTEGER DEFAULT 0,\n    matches_started INTEGER DEFAULT 0,\n    avg_queue_time_seconds INTEGER DEFAULT 0,\n    peak_queue_size INTEGER DEFAULT 0,\n    UNIQUE(date, hour, kit)\n)\n".formatted(tableName);
                MigrationManager.this.executeSQL(conn, sql);
                MigrationManager.this.logger.info("Created queue statistics table.");
            }
        });
        return list;
    }

    public boolean runMigrations() {
        this.logger.info("Checking for pending database migrations...");
        int currentVersion = this.getCurrentVersion();
        int targetVersion = this.getTargetVersion();
        if (currentVersion >= targetVersion) {
            this.logger.info("Database is up to date (version " + currentVersion + ")");
            return true;
        }
        this.logger.info("Migrating database from version " + currentVersion + " to " + targetVersion);
        boolean success = true;
        try (Connection conn = this.databaseManager.getConnection();){
            conn.setAutoCommit(false);
            for (Migration migration : this.migrations) {
                if (migration.getVersion() <= currentVersion) continue;
                this.logger.info("Running migration " + migration.getVersion() + ": " + migration.getDescription());
                try {
                    migration.migrate(conn);
                    this.recordMigration(conn, migration);
                    conn.commit();
                    this.logger.info("Migration " + migration.getVersion() + " completed successfully.");
                }
                catch (SQLException e) {
                    this.logger.log(Level.SEVERE, "Migration " + migration.getVersion() + " failed!", e);
                    conn.rollback();
                    success = false;
                    break;
                }
            }
            conn.setAutoCommit(true);
        }
        catch (SQLException e) {
            this.logger.log(Level.SEVERE, "Failed to run migrations", e);
            return false;
        }
        if (success) {
            this.logger.info("All migrations completed successfully!");
        }
        return success;
    }

    public int getCurrentVersion() {
        return this.databaseManager.getCurrentSchemaVersion();
    }

    public int getTargetVersion() {
        return this.migrations.isEmpty() ? 0 : this.migrations.stream().mapToInt(Migration::getVersion).max().orElse(0);
    }

    private void recordMigration(Connection conn, Migration migration) throws SQLException {
        String sql = "INSERT INTO " + this.databaseManager.getTablePrefix() + "schema_version (version, description) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql);){
            stmt.setInt(1, migration.getVersion());
            stmt.setString(2, migration.getDescription());
            stmt.executeUpdate();
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName);){
            boolean bl = rs.next();
            return bl;
        }
    }

    private void executeSQL(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();){
            stmt.execute(sql);
        }
    }

    public List<Map.Entry<Integer, String>> getMigrationHistory() {
        ArrayList<Map.Entry<Integer, String>> history = new ArrayList<Map.Entry<Integer, String>>();
        String sql = "SELECT version, description FROM " + this.databaseManager.getTablePrefix() + "schema_version ORDER BY version";
        try {
            List<Map.Entry> entries = this.databaseManager.executeQueryList(sql, rs -> {
                try {
                    return Map.entry(rs.getInt("version"), rs.getString("description"));
                }
                catch (SQLException e) {
                    return null;
                }
            }, new Object[0]);
            history.addAll(entries);
        }
        catch (SQLException e) {
            this.logger.log(Level.WARNING, "Failed to get migration history", e);
        }
        return history;
    }

    public boolean isMigrationApplied(int version) {
        return this.getCurrentVersion() >= version;
    }

    public List<Migration> getPendingMigrations() {
        int currentVersion = this.getCurrentVersion();
        ArrayList<Migration> pending = new ArrayList<Migration>();
        for (Migration migration : this.migrations) {
            if (migration.getVersion() <= currentVersion) continue;
            pending.add(migration);
        }
        return pending;
    }

    public void registerMigration(Migration migration) {
        int index = 0;
        for (int i = 0; i < this.migrations.size(); ++i) {
            if (this.migrations.get(i).getVersion() > migration.getVersion()) {
                index = i;
                break;
            }
            index = i + 1;
        }
        this.migrations.add(index, migration);
    }

    public static interface Migration {
        public int getVersion();

        public String getDescription();

        public void migrate(Connection var1) throws SQLException;
    }
}

