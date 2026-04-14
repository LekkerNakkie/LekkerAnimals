package me.lekkernakkie.lekkeranimal.config;

import org.bukkit.configuration.file.FileConfiguration;

public class MainSettings {

    private final boolean debug;
    private final String languageFile;

    private final String databaseType;
    private final String sqliteFile;

    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final boolean mysqlSsl;
    private final int mysqlPoolSize;

    private final int autosaveIntervalSeconds;
    private final boolean saveOnDisable;

    private final boolean hungerEnabled;
    private final int hungerTaskIntervalSeconds;
    private final int hungerWarningThresholdPercent;
    private final boolean hungerKillOnZero;

    private final boolean levelingEnabled;
    private final int maxLevel;
    private final int baseXp;
    private final double xpScale;

    private final boolean bondEnabled;
    private final int maxBond;
    private final int startBond;
    private final int startLevel;

    public MainSettings(FileConfiguration config) {
        this.debug = config.getBoolean("debug", false);
        this.languageFile = config.getString("language-file", "lang_NL.yml");

        this.databaseType = config.getString("database.type", "SQLITE");
        this.sqliteFile = config.getString("database.sqlite.file", "data.db");

        this.mysqlHost = config.getString("database.mysql.host", "localhost");
        this.mysqlPort = config.getInt("database.mysql.port", 3306);
        this.mysqlDatabase = config.getString("database.mysql.database", "lekkeranimal");
        this.mysqlUsername = config.getString("database.mysql.username", "root");
        this.mysqlPassword = config.getString("database.mysql.password", "");
        this.mysqlSsl = config.getBoolean("database.mysql.ssl", false);
        this.mysqlPoolSize = config.getInt("database.mysql.pool-size", 10);

        this.autosaveIntervalSeconds = config.getInt("main.autosave-interval-seconds", 300);
        this.saveOnDisable = config.getBoolean("main.save-on-disable", true);

        this.hungerEnabled = config.getBoolean("hunger.enabled", true);
        this.hungerTaskIntervalSeconds = config.getInt("hunger.task-interval-seconds", 60);
        this.hungerWarningThresholdPercent = config.getInt("hunger.warning-threshold-percent", 25);
        this.hungerKillOnZero = config.getBoolean("hunger.kill-on-zero", true);

        this.levelingEnabled = config.getBoolean("leveling.enabled", true);
        this.maxLevel = config.getInt("leveling.max-level", 50);
        this.baseXp = config.getInt("leveling.base-xp", 100);
        this.xpScale = config.getDouble("leveling.xp-scale", 1.15D);

        this.bondEnabled = config.getBoolean("bond.enabled", true);
        this.maxBond = config.getInt("bond.max-bond", 100);
        this.startBond = config.getInt("bond.start-bond", 10);
        this.startLevel = config.getInt("bond.start-level", 1);
    }

    public boolean isDebug() {
        return debug;
    }

    public String getLanguageFile() {
        return languageFile;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getSqliteFile() {
        return sqliteFile;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public boolean isMysqlSsl() {
        return mysqlSsl;
    }

    public int getMysqlPoolSize() {
        return mysqlPoolSize;
    }

    public int getAutosaveIntervalSeconds() {
        return autosaveIntervalSeconds;
    }

    public boolean isSaveOnDisable() {
        return saveOnDisable;
    }

    public boolean isHungerEnabled() {
        return hungerEnabled;
    }

    public int getHungerTaskIntervalSeconds() {
        return hungerTaskIntervalSeconds;
    }

    public int getHungerWarningThresholdPercent() {
        return hungerWarningThresholdPercent;
    }

    public boolean isHungerKillOnZero() {
        return hungerKillOnZero;
    }

    public boolean isLevelingEnabled() {
        return levelingEnabled;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getBaseXp() {
        return baseXp;
    }

    public double getXpScale() {
        return xpScale;
    }

    public boolean isBondEnabled() {
        return bondEnabled;
    }

    public int getMaxBond() {
        return maxBond;
    }

    public int getStartBond() {
        return startBond;
    }

    public int getStartLevel() {
        return startLevel;
    }
}