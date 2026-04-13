package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.Map;

public class AnimalProfile {

    private final EntityType entityType;
    private final boolean enabled;
    private final String displayName;
    private final Material bondItem;
    private final int requiredBondAmount;
    private final int maxHunger;
    private final int hungerDrain;
    private final Map<Material, FoodSettings> feedingFoods;

    public AnimalProfile(EntityType entityType,
                         boolean enabled,
                         String displayName,
                         Material bondItem,
                         int requiredBondAmount,
                         int maxHunger,
                         int hungerDrain,
                         Map<Material, FoodSettings> feedingFoods) {
        this.entityType = entityType;
        this.enabled = enabled;
        this.displayName = displayName;
        this.bondItem = bondItem;
        this.requiredBondAmount = requiredBondAmount;
        this.maxHunger = maxHunger;
        this.hungerDrain = hungerDrain;
        this.feedingFoods = feedingFoods;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getBondItem() {
        return bondItem;
    }

    public int getRequiredBondAmount() {
        return requiredBondAmount;
    }

    public int getMaxHunger() {
        return maxHunger;
    }

    public int getHungerDrain() {
        return hungerDrain;
    }

    public Map<Material, FoodSettings> getFeedingFoods() {
        return Collections.unmodifiableMap(feedingFoods);
    }

    public FoodSettings getFoodSettings(Material material) {
        return feedingFoods.get(material);
    }

    public static class FoodSettings {
        private final int hungerRestore;
        private final int xpGain;
        private final int bondGain;

        public FoodSettings(int hungerRestore, int xpGain, int bondGain) {
            this.hungerRestore = hungerRestore;
            this.xpGain = xpGain;
            this.bondGain = bondGain;
        }

        public int getHungerRestore() {
            return hungerRestore;
        }

        public int getXpGain() {
            return xpGain;
        }

        public int getBondGain() {
            return bondGain;
        }
    }
}