package me.lekkernakkie.lekkeranimal.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.MainSettings;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final LekkerAnimal plugin;
    private final MainSettings settings;

    private HikariDataSource dataSource;
    private DatabaseType databaseType;

    public DatabaseManager(LekkerAnimal plugin) {
        this.plugin = plugin;
        this.settings = plugin.getConfigManager().getMainSettings();
    }

    public void connect() throws SQLException {
        this.databaseType = DatabaseType.fromString(settings.getDatabaseType());

        HikariConfig hikariConfig = new HikariConfig();

        if (databaseType == DatabaseType.SQLITE) {
            File dbFile = new File(plugin.getDataFolder(), settings.getSqliteFile());
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setPoolName("LekkerAnimals-SQLite");
        } else {
            String jdbcUrl = "jdbc:mysql://"
                    + settings.getMysqlHost() + ":"
                    + settings.getMysqlPort() + "/"
                    + settings.getMysqlDatabase()
                    + "?useSSL=" + settings.isMysqlSsl()
                    + "&serverTimezone=UTC"
                    + "&characterEncoding=utf8";

            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariConfig.setUsername(settings.getMysqlUsername());
            hikariConfig.setPassword(settings.getMysqlPassword());
            hikariConfig.setMaximumPoolSize(Math.max(2, settings.getMysqlPoolSize()));
            hikariConfig.setPoolName("LekkerAnimals-MySQL");
        }

        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setValidationTimeout(5000);
        hikariConfig.setLeakDetectionThreshold(0);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool is not initialized.");
        }
        return dataSource.getConnection();
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void createTables() throws SQLException {
        String createTableSql;

        if (databaseType == DatabaseType.MYSQL) {
            createTableSql = """
                    CREATE TABLE IF NOT EXISTS bonded_animals (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        entity_uuid VARCHAR(36) NOT NULL UNIQUE,
                        owner_uuid VARCHAR(36) NOT NULL,
                        owner_name VARCHAR(16) NOT NULL,
                        animal_type VARCHAR(32) NOT NULL,
                        level INT NOT NULL,
                        xp INT NOT NULL,
                        bond INT NOT NULL,
                        hunger INT NOT NULL,
                        max_hunger INT NOT NULL,
                        world VARCHAR(64),
                        x DOUBLE,
                        y DOUBLE,
                        z DOUBLE,
                        created_at BIGINT NOT NULL,
                        updated_at BIGINT NOT NULL
                    )
                    """;
        } else {
            createTableSql = """
                    CREATE TABLE IF NOT EXISTS bonded_animals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        entity_uuid TEXT NOT NULL UNIQUE,
                        owner_uuid TEXT NOT NULL,
                        owner_name TEXT NOT NULL,
                        animal_type TEXT NOT NULL,
                        level INTEGER NOT NULL,
                        xp INTEGER NOT NULL,
                        bond INTEGER NOT NULL,
                        hunger INTEGER NOT NULL,
                        max_hunger INTEGER NOT NULL,
                        world TEXT,
                        x REAL,
                        y REAL,
                        z REAL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """;
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSql);
        }
    }
}