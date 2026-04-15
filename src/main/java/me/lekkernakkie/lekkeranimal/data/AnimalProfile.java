package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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

    private final boolean harvestingEnabled;
    private final long harvestCooldownSeconds;
    private final TreeMap<Integer, HarvestLevelProfile> harvestProfiles;

    private final boolean headSellingEnabled;
    private final HeadPriceRule defaultHeadPrice;
    private final Map<String, HeadPriceRule> rarityHeadPrices;

    public AnimalProfile(EntityType entityType,
                         boolean enabled,
                         String displayName,
                         Material bondItem,
                         int requiredBondAmount,
                         int maxHunger,
                         int hungerDrain,
                         int maxLevel,
                         Map<Material, FeedingReward> feedingRewards,
                         Map<Integer, DirectLevelUpgrade> directLevelUpgrades,
                         boolean harvestingEnabled,
                         long harvestCooldownSeconds,
                         TreeMap<Integer, HarvestLevelProfile> harvestProfiles,
                         boolean headSellingEnabled,
                         HeadPriceRule defaultHeadPrice,
                         Map<String, HeadPriceRule> rarityHeadPrices) {
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
        this.harvestingEnabled = harvestingEnabled;
        this.harvestCooldownSeconds = harvestCooldownSeconds;
        this.harvestProfiles = harvestProfiles;
        this.headSellingEnabled = headSellingEnabled;
        this.defaultHeadPrice = defaultHeadPrice;
        this.rarityHeadPrices = rarityHeadPrices;
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

    public boolean isHarvestingEnabled() {
        return harvestingEnabled;
    }

    public long getHarvestCooldownSeconds() {
        return harvestCooldownSeconds;
    }

    public Map<Integer, HarvestLevelProfile> getHarvestProfiles() {
        return Collections.unmodifiableMap(harvestProfiles);
    }

    public HarvestLevelProfile getHarvestProfileForLevel(int currentLevel) {
        Map.Entry<Integer, HarvestLevelProfile> entry = harvestProfiles.floorEntry(currentLevel);
        return entry != null ? entry.getValue() : null;
    }

    public boolean isHeadSellingEnabled() {
        return headSellingEnabled;
    }

    public HeadPriceRule getDefaultHeadPrice() {
        return defaultHeadPrice;
    }

    public Map<String, HeadPriceRule> getRarityHeadPrices() {
        return Collections.unmodifiableMap(rarityHeadPrices);
    }

    public HeadPriceRule getHeadPriceRule(String rarityId) {
        if (rarityId != null && !rarityId.isBlank()) {
            HeadPriceRule specific = rarityHeadPrices.get(rarityId.toUpperCase());
            if (specific != null && specific.isConfigured()) {
                return specific;
            }
        }

        return defaultHeadPrice != null && defaultHeadPrice.isConfigured()
                ? defaultHeadPrice
                : null;
    }
}