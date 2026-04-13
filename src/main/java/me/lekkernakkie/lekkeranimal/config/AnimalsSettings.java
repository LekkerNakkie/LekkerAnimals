package me.lekkernakkie.lekkeranimal.config;

import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AnimalsSettings {

    private final Map<EntityType, AnimalProfile> profiles = new EnumMap<>(EntityType.class);

    public AnimalsSettings(FileConfiguration config) {
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

            Material bondItem;
            try {
                bondItem = Material.valueOf(bondItemName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                bondItem = Material.WHEAT;
            }

            Map<Material, AnimalProfile.FoodSettings> feedingFoods = new HashMap<>();
            ConfigurationSection feedingSection = config.getConfigurationSection(basePath + ".feeding");
            if (feedingSection != null) {
                for (String foodKey : feedingSection.getKeys(false)) {
                    Material material;
                    try {
                        material = Material.valueOf(foodKey.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }

                    String foodPath = basePath + ".feeding." + foodKey;
                    int hungerRestore = config.getInt(foodPath + ".hunger", 0);
                    int xpGain = config.getInt(foodPath + ".xp", 0);
                    int bondGain = config.getInt(foodPath + ".bond", 0);

                    feedingFoods.put(material, new AnimalProfile.FoodSettings(hungerRestore, xpGain, bondGain));
                }
            }

            AnimalProfile profile = new AnimalProfile(
                    entityType,
                    enabled,
                    displayName,
                    bondItem,
                    requiredAmount,
                    maxHunger,
                    hungerDrain,
                    feedingFoods
            );

            profiles.put(entityType, profile);
        }
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