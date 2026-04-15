package me.lekkernakkie.lekkeranimal.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permissible;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private final boolean onlineOwnerActivityEnabled;

    private final boolean coOwnersEnabled;
    private final int coOwnersMaxPerAnimal;
    private final boolean coOwnersAllowFeed;
    private final boolean coOwnersAllowHarvest;
    private final boolean coOwnersAllowGuiAccess;
    private final boolean coOwnersAllowDirectUpgrades;

    private final boolean coOwnerGuiEnabled;
    private final boolean coOwnerChatInputEnabled;

    private final boolean customHeadsEnabled;
    private final boolean preservePlacedCustomHeads;
    private final String customHeadDefaultNameFormat;
    private final List<String> customHeadLore;
    private final Map<String, String> rarityDisplays;

    private final boolean interactionRangeEnabled;
    private final double feedAndLevelDistance;

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

        this.autosaveIntervalSeconds = Math.max(0, config.getInt("main.autosave-interval-seconds", 300));
        this.saveOnDisable = config.getBoolean("main.save-on-disable", true);

        this.hungerEnabled = config.getBoolean("hunger.enabled", true);
        this.hungerTaskIntervalSeconds = Math.max(1, config.getInt("hunger.task-interval-seconds", 60));
        this.hungerWarningThresholdPercent = Math.max(0, Math.min(100, config.getInt("hunger.warning-threshold-percent", 25)));
        this.hungerKillOnZero = config.getBoolean("hunger.kill-on-zero", true);

        this.levelingEnabled = config.getBoolean("leveling.enabled", true);
        this.maxLevel = Math.max(1, config.getInt("leveling.max-level", 50));
        this.baseXp = Math.max(1, config.getInt("leveling.base-xp", 100));
        this.xpScale = Math.max(1.0D, config.getDouble("leveling.xp-scale", 1.15D));

        this.bondEnabled = config.getBoolean("bond.enabled", true);
        this.maxBond = Math.max(1, config.getInt("bond.max-bond", 100));
        this.startBond = Math.max(0, config.getInt("bond.start-bond", 10));
        this.startLevel = Math.max(1, config.getInt("bond.start-level", 1));

        this.onlineOwnerActivityEnabled = config.getBoolean("modules.online-owner-activity.enabled", true);

        this.coOwnersEnabled = config.getBoolean("modules.co-owners.enabled", true);
        this.coOwnersMaxPerAnimal = Math.max(1, config.getInt("modules.co-owners.max-per-animal", 3));
        this.coOwnersAllowFeed = config.getBoolean("modules.co-owners.allow-feed", true);
        this.coOwnersAllowHarvest = config.getBoolean("modules.co-owners.allow-harvest", true);
        this.coOwnersAllowGuiAccess = config.getBoolean("modules.co-owners.allow-gui-access", true);
        this.coOwnersAllowDirectUpgrades = config.getBoolean("modules.co-owners.allow-direct-upgrades", false);

        this.coOwnerGuiEnabled = config.getBoolean("modules.co-owner-gui.enabled", true);
        this.coOwnerChatInputEnabled = config.getBoolean("modules.co-owner-gui.use-chat-input", true);

        this.customHeadsEnabled = config.getBoolean("custom-heads.enabled", true);
        this.preservePlacedCustomHeads = config.getBoolean("custom-heads.preserve-placed-heads", true);
        this.customHeadDefaultNameFormat = config.getString("custom-heads.default-name-format", "{rarity} {animal} Head");
        this.customHeadLore = config.getStringList("custom-heads.lore");

        Map<String, String> loadedRarities = new LinkedHashMap<>();
        ConfigurationSection raritySection = config.getConfigurationSection("custom-heads.rarities");
        if (raritySection != null) {
            for (String key : raritySection.getKeys(false)) {
                loadedRarities.put(key.toUpperCase(), raritySection.getString(key, key));
            }
        }
        this.rarityDisplays = loadedRarities;

        this.interactionRangeEnabled = config.getBoolean("interaction-range.enabled", true);
        this.feedAndLevelDistance = Math.max(0.0D, config.getDouble("interaction-range.feed-and-level-distance", 5.0D));
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

    public boolean isOnlineOwnerActivityEnabled() {
        return onlineOwnerActivityEnabled;
    }

    public boolean isCoOwnersEnabled() {
        return coOwnersEnabled;
    }

    public int getCoOwnersMaxPerAnimal() {
        return coOwnersMaxPerAnimal;
    }

    public int getEffectiveCoOwnersMax(Permissible permissible) {
        int configuredMax = Math.max(1, coOwnersMaxPerAnimal);
        int allowed = 1;

        if (permissible != null) {
            if (permissible.hasPermission("lekkeranimal.coowners.slots.3")) {
                allowed = 3;
            } else if (permissible.hasPermission("lekkeranimal.coowners.slots.2")) {
                allowed = 2;
            }

            if (permissible.hasPermission("lekkeranimal.admin")) {
                allowed = Math.max(allowed, configuredMax);
            }
        }

        return Math.max(1, Math.min(configuredMax, allowed));
    }

    public boolean isCoOwnersAllowFeed() {
        return coOwnersAllowFeed;
    }

    public boolean isCoOwnersAllowHarvest() {
        return coOwnersAllowHarvest;
    }

    public boolean isCoOwnersAllowGuiAccess() {
        return coOwnersAllowGuiAccess;
    }

    public boolean isCoOwnersAllowDirectUpgrades() {
        return coOwnersAllowDirectUpgrades;
    }

    public boolean isCoOwnerGuiEnabled() {
        return coOwnerGuiEnabled;
    }

    public boolean isCoOwnerChatInputEnabled() {
        return coOwnerChatInputEnabled;
    }

    public boolean isCustomHeadsEnabled() {
        return customHeadsEnabled;
    }

    public boolean isPreservePlacedCustomHeads() {
        return preservePlacedCustomHeads;
    }

    public String getCustomHeadDefaultNameFormat() {
        return customHeadDefaultNameFormat;
    }

    public List<String> getCustomHeadLore() {
        return customHeadLore;
    }

    public String getRarityDisplay(String rarityId) {
        if (rarityId == null || rarityId.isBlank()) {
            return rarityDisplays.getOrDefault("COMMON", "Common");
        }

        return rarityDisplays.getOrDefault(
                rarityId.toUpperCase(),
                rarityDisplays.getOrDefault("COMMON", rarityId)
        );
    }

    public Map<String, String> getRarityDisplays() {
        return Collections.unmodifiableMap(rarityDisplays);
    }

    public boolean isInteractionRangeEnabled() {
        return interactionRangeEnabled;
    }

    public double getFeedAndLevelDistance() {
        return feedAndLevelDistance;
    }
}