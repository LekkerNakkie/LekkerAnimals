package me.lekkernakkie.lekkeranimal.config;

import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import me.lekkernakkie.lekkeranimal.data.HarvestDrop;
import me.lekkernakkie.lekkeranimal.data.HarvestLevelProfile;
import me.lekkernakkie.lekkeranimal.data.HeadPriceRule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class AnimalsSettings {

    private static final Logger LOGGER = Logger.getLogger("LekkerAnimal");

    private final FileConfiguration config;
    private final Map<EntityType, AnimalProfile> profiles = new EnumMap<>(EntityType.class);

    public AnimalsSettings(FileConfiguration config) {
        this.config = config;

        ConfigurationSection animalsSection = config.getConfigurationSection("animals");
        if (animalsSection == null) {
            return;
        }

        for (String key : animalsSection.getKeys(false)) {
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException ex) {
                warn("Skipping unknown entity type in animals.yml: " + key);
                continue;
            }

            String basePath = "animals." + key;

            boolean enabled = config.getBoolean(basePath + ".enabled", true);
            String displayName = config.getString(basePath + ".display-name", key);

            String bondItemName = config.getString(basePath + ".bonding.item", "WHEAT");
            int requiredAmount = Math.max(1, config.getInt(basePath + ".bonding.required-amount", 1));

            int maxHunger = Math.max(1, config.getInt(basePath + ".hunger.max", 100));
            int hungerDrain = Math.max(0, config.getInt(basePath + ".hunger.drain", 1));
            int maxLevel = Math.max(1, config.getInt(basePath + ".leveling.max-level", 25));

            Material bondItem = parseMaterial(
                    bondItemName,
                    Material.WHEAT,
                    "Invalid bonding item for " + key + ": " + bondItemName + ". Falling back to WHEAT."
            );

            Map<Material, FeedingReward> feedingRewards = loadFeedingRewards(basePath, key);
            Map<Integer, DirectLevelUpgrade> directUpgrades = loadDirectUpgrades(basePath, key);

            boolean harvestingEnabled = config.getBoolean(basePath + ".harvesting.enabled", false);
            long harvestCooldownSeconds = Math.max(0L, config.getLong(basePath + ".harvesting.cooldown-seconds", 1800L));
            TreeMap<Integer, HarvestLevelProfile> harvestProfiles = loadHarvestProfiles(basePath, key);

            boolean headSellingEnabled = config.getBoolean(basePath + ".head-sell.enabled", false);
            HeadPriceRule defaultHeadPrice = loadPriceRule(basePath + ".head-sell.default-pricing");
            Map<String, HeadPriceRule> rarityHeadPrices = loadRarityPriceRules(basePath + ".head-sell.rarity-pricing");

            AnimalProfile profile = new AnimalProfile(
                    entityType,
                    enabled,
                    displayName,
                    bondItem,
                    requiredAmount,
                    maxHunger,
                    hungerDrain,
                    maxLevel,
                    feedingRewards,
                    directUpgrades,
                    harvestingEnabled,
                    harvestCooldownSeconds,
                    harvestProfiles,
                    headSellingEnabled,
                    defaultHeadPrice,
                    rarityHeadPrices
            );

            profiles.put(entityType, profile);
        }
    }

    private Map<Material, FeedingReward> loadFeedingRewards(String basePath, String animalKey) {
        Map<Material, FeedingReward> rewards = new EnumMap<>(Material.class);

        ConfigurationSection feedingSection = config.getConfigurationSection(basePath + ".feeding");
        if (feedingSection == null) {
            return rewards;
        }

        for (String materialKey : feedingSection.getKeys(false)) {
            Material material;
            try {
                material = Material.valueOf(materialKey.toUpperCase());
            } catch (IllegalArgumentException ex) {
                warn("Skipping invalid feeding material for " + animalKey + ": " + materialKey);
                continue;
            }

            String path = basePath + ".feeding." + materialKey;
            int hunger = Math.max(0, config.getInt(path + ".hunger", 0));
            int xp = Math.max(0, config.getInt(path + ".xp", 0));
            int bond = Math.max(0, config.getInt(path + ".bond", 0));

            rewards.put(material, new FeedingReward(hunger, xp, bond));
        }

        return rewards;
    }

    private Map<Integer, DirectLevelUpgrade> loadDirectUpgrades(String basePath, String animalKey) {
        Map<Integer, DirectLevelUpgrade> upgrades = new HashMap<>();

        ConfigurationSection upgradeSection = config.getConfigurationSection(basePath + ".leveling.direct-upgrades");
        if (upgradeSection == null) {
            return upgrades;
        }

        for (String levelKey : upgradeSection.getKeys(false)) {
            int targetLevel;
            try {
                targetLevel = Integer.parseInt(levelKey);
            } catch (NumberFormatException ex) {
                warn("Skipping invalid direct-upgrade level for " + animalKey + ": " + levelKey);
                continue;
            }

            if (targetLevel <= 1) {
                warn("Skipping invalid direct-upgrade target level for " + animalKey + ": " + targetLevel);
                continue;
            }

            String path = basePath + ".leveling.direct-upgrades." + levelKey;
            String itemName = config.getString(path + ".item", "STONE");
            int amount = Math.max(1, config.getInt(path + ".amount", 1));

            Material material;
            try {
                material = Material.valueOf(itemName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                warn("Skipping invalid direct-upgrade material for " + animalKey + " level " + targetLevel + ": " + itemName);
                continue;
            }

            upgrades.put(targetLevel, new DirectLevelUpgrade(material, amount));
        }

        return upgrades;
    }

    private TreeMap<Integer, HarvestLevelProfile> loadHarvestProfiles(String basePath, String animalKey) {
        TreeMap<Integer, HarvestLevelProfile> result = new TreeMap<>();

        ConfigurationSection levelsSection = config.getConfigurationSection(basePath + ".harvesting.levels");
        if (levelsSection == null) {
            return result;
        }

        for (String levelKey : levelsSection.getKeys(false)) {
            int level;
            try {
                level = Integer.parseInt(levelKey);
            } catch (NumberFormatException ex) {
                warn("Skipping invalid harvest level for " + animalKey + ": " + levelKey);
                continue;
            }

            if (level <= 0) {
                warn("Skipping invalid harvest level for " + animalKey + ": " + level);
                continue;
            }

            ConfigurationSection dropsSection = config.getConfigurationSection(
                    basePath + ".harvesting.levels." + levelKey + ".drops"
            );
            if (dropsSection == null) {
                continue;
            }

            List<HarvestDrop> drops = new ArrayList<>();

            for (String dropKey : dropsSection.getKeys(false)) {
                String path = basePath + ".harvesting.levels." + levelKey + ".drops." + dropKey;

                Material material;
                try {
                    material = Material.valueOf(dropKey.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    warn("Skipping invalid harvest material for " + animalKey + " level " + level + ": " + dropKey);
                    continue;
                }

                int amount = Math.max(1, config.getInt(path + ".amount", 1));
                double chance = Math.max(0.0D, Math.min(100.0D, config.getDouble(path + ".chance", 100.0D)));
                String displayName = config.getString(path + ".name", "");
                String headTexture = config.getString(path + ".head-texture", "");
                String headOwner = config.getString(path + ".head-owner", "");
                String rarity = config.getString(path + ".rarity", "COMMON");

                drops.add(new HarvestDrop(
                        material,
                        amount,
                        chance,
                        displayName,
                        headTexture,
                        headOwner,
                        rarity
                ));
            }

            result.put(level, new HarvestLevelProfile(level, drops));
        }

        return result;
    }

    private HeadPriceRule loadPriceRule(String path) {
        String typeRaw = config.getString(path + ".type", "FIXED");
        HeadPriceRule.Type type;

        try {
            type = HeadPriceRule.Type.valueOf(typeRaw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            type = HeadPriceRule.Type.FIXED;
        }

        double fixed = Math.max(0.0D, config.getDouble(path + ".fixed", 0.0D));
        double min = Math.max(0.0D, config.getDouble(path + ".min", 0.0D));
        double max = Math.max(0.0D, config.getDouble(path + ".max", 0.0D));

        HeadPriceRule rule = new HeadPriceRule(type, fixed, min, max);
        return rule.isConfigured() ? rule : null;
    }

    private Map<String, HeadPriceRule> loadRarityPriceRules(String path) {
        Map<String, HeadPriceRule> result = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) {
            return result;
        }

        for (String rarityKey : section.getKeys(false)) {
            HeadPriceRule rule = loadPriceRule(path + "." + rarityKey);
            if (rule != null && rule.isConfigured()) {
                result.put(rarityKey.toUpperCase(), rule);
            }
        }

        return result;
    }

    private Material parseMaterial(String input, Material fallback, String warning) {
        if (input == null || input.isBlank()) {
            warn(warning);
            return fallback;
        }

        try {
            return Material.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException ex) {
            warn(warning);
            return fallback;
        }
    }

    private void warn(String message) {
        LOGGER.warning("[AnimalsSettings] " + message);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isSupported(EntityType type) {
        AnimalProfile profile = profiles.get(type);
        return profile != null && profile.isEnabled();
    }

    public AnimalProfile getProfile(EntityType type) {
        return profiles.get(type);
    }

    public Map<EntityType, AnimalProfile> getProfiles() {
        return profiles;
    }

    public int getLoadedAmount() {
        int count = 0;
        for (AnimalProfile profile : profiles.values()) {
            if (profile.isEnabled()) {
                count++;
            }
        }
        return count;
    }
}