package me.lekkernakkie.lekkeranimal.config;

import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AnimalsSettings {

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
                continue;
            }

            String basePath = "animals." + key;

            boolean enabled = config.getBoolean(basePath + ".enabled", true);
            String displayName = config.getString(basePath + ".display-name", key);

            String bondItemName = config.getString(basePath + ".bonding.item", "WHEAT");
            int requiredAmount = config.getInt(basePath + ".bonding.required-amount", 1);

            int maxHunger = config.getInt(basePath + ".hunger.max", 100);
            int hungerDrain = config.getInt(basePath + ".hunger.drain", 1);

            int maxLevel = config.getInt(basePath + ".leveling.max-level", 25);

            Material bondItem;
            try {
                bondItem = Material.valueOf(bondItemName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                bondItem = Material.WHEAT;
            }

            Map<Material, FeedingReward> feedingRewards = loadFeedingRewards(basePath);
            Map<Integer, DirectLevelUpgrade> directUpgrades = loadDirectUpgrades(basePath);

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
                    directUpgrades
            );

            profiles.put(entityType, profile);
        }
    }

    private Map<Material, FeedingReward> loadFeedingRewards(String basePath) {
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
                continue;
            }

            String path = basePath + ".feeding." + materialKey;
            int hunger = config.getInt(path + ".hunger", 0);
            int xp = config.getInt(path + ".xp", 0);
            int bond = config.getInt(path + ".bond", 0);

            rewards.put(material, new FeedingReward(hunger, xp, bond));
        }

        return rewards;
    }

    private Map<Integer, DirectLevelUpgrade> loadDirectUpgrades(String basePath) {
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
                continue;
            }

            String path = basePath + ".leveling.direct-upgrades." + levelKey;
            String itemName = config.getString(path + ".item", "STONE");
            int amount = config.getInt(path + ".amount", 1);

            Material material;
            try {
                material = Material.valueOf(itemName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                continue;
            }

            upgrades.put(targetLevel, new DirectLevelUpgrade(material, amount));
        }

        return upgrades;
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
        return profiles.size();
    }
}