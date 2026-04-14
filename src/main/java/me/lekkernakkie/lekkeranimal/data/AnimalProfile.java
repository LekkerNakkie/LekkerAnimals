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
    private final int maxLevel;
    private final Map<Material, FeedingReward> feedingRewards;
    private final Map<Integer, DirectLevelUpgrade> directLevelUpgrades;

    public AnimalProfile(EntityType entityType,
                         boolean enabled,
                         String displayName,
                         Material bondItem,
                         int requiredBondAmount,
                         int maxHunger,
                         int hungerDrain,
                         int maxLevel,
                         Map<Material, FeedingReward> feedingRewards,
                         Map<Integer, DirectLevelUpgrade> directLevelUpgrades) {
        this.entityType = entityType;
        this.enabled = enabled;
        this.displayName = displayName;
        this.bondItem = bondItem;
        this.requiredBondAmount = requiredBondAmount;
        this.maxHunger = maxHunger;
        this.hungerDrain = hungerDrain;
        this.maxLevel = maxLevel;
        this.feedingRewards = feedingRewards;
        this.directLevelUpgrades = directLevelUpgrades;
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

    public int getMaxLevel() {
        return maxLevel;
    }

    public Map<Material, FeedingReward> getFeedingRewards() {
        return Collections.unmodifiableMap(feedingRewards);
    }

    public FeedingReward getFeedingReward(Material material) {
        return feedingRewards.get(material);
    }

    public Map<Integer, DirectLevelUpgrade> getDirectLevelUpgrades() {
        return Collections.unmodifiableMap(directLevelUpgrades);
    }

    public DirectLevelUpgrade getDirectLevelUpgrade(int targetLevel) {
        return directLevelUpgrades.get(targetLevel);
    }
}