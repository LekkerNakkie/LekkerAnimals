package me.lekkernakkie.lekkeranimal.config;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeedstationSettings {

    private final boolean enabled;

    private final Material itemMaterial;
    private final String itemName;
    private final List<String> itemLore;

    private final boolean hologramEnabled;
    private final List<String> hologramLines;
    private final double hologramOffsetY;
    private final long hologramRefreshTicks;
    private final double hologramVisibleRadiusBlocks;

    private final String guiTitle;
    private final int guiRows;

    private final boolean fillerEnabled;
    private final Material fillerMaterial;
    private final String fillerName;

    private final int infoSlot;
    private final int openStorageSlot;
    private final int showRadiusSlot;
    private final int toggleHologramSlot;
    private final int upgradeSlot;

    private final Material infoMaterial;
    private final String infoName;
    private final List<String> infoLore;

    private final Material storageButtonMaterial;
    private final String storageButtonName;
    private final List<String> storageButtonLore;

    private final Material radiusButtonMaterial;
    private final String radiusButtonName;
    private final List<String> radiusButtonLore;

    private final Material hologramEnabledMaterial;
    private final Material hologramDisabledMaterial;
    private final String hologramButtonName;
    private final List<String> hologramEnabledLore;
    private final List<String> hologramDisabledLore;

    private final String storageGuiTitle;
    private final Material blockedSlotMaterial;
    private final String blockedSlotName;
    private final List<String> blockedSlotLore;

    private final long radiusPreviewDurationTicks;
    private final Particle radiusPreviewParticle;
    private final int radiusPreviewCountPerPoint;
    private final int radiusPreviewPoints;
    private final double radiusPreviewYOffset;

    private final String upgradeEnabledName;
    private final String upgradeDisabledName;
    private final List<String> upgradeEnabledLore;
    private final List<String> upgradeDisabledLore;

    private final boolean upgradesEnabled;
    private final Map<FeederTier, FeederTier> upgradeChain;
    private final Map<FeederTier, UpgradeCost> upgradeCosts;

    private final long taskTickInterval;

    private final boolean onlyWhenHungry;
    private final boolean consumeOneItemPerFeed;
    private final boolean skipFullAnimals;
    private final boolean prioritizeLowestHunger;

    private final boolean walkingAiEnabled;
    private final long walkingAiRepathIntervalTicks;
    private final double walkingAiHungerThresholdPercent;
    private final double walkingAiRandomTargetRadius;
    private final double walkingAiMinDistanceToStart;

    private final ParticleSection idleParticles;
    private final ParticleSection feedingParticles;
    private final ParticleSection attractionParticles;

    private final boolean soundsEnabled;
    private final Sound openSound;
    private final Sound depositSound;
    private final Sound feedSound;
    private final Sound upgradeSound;

    private final Map<FeederTier, TierSettings> tiers;

    public FeedstationSettings(FileConfiguration config) {
        this.enabled = config.getBoolean("enabled", true);

        this.itemMaterial = parseMaterial(config.getString("item.material"), Material.CAULDRON);
        this.itemName = config.getString("item.name", "&b{tier_display} &7Voederbak");
        this.itemLore = new ArrayList<>(config.getStringList("item.lore"));

        this.hologramEnabled = config.getBoolean("hologram.enabled", true);
        this.hologramLines = new ArrayList<>(config.getStringList("hologram.lines"));
        this.hologramOffsetY = config.getDouble("hologram.offset-y", 1.4D);
        this.hologramRefreshTicks = Math.max(5L, config.getLong("hologram.refresh-ticks", 20L));
        this.hologramVisibleRadiusBlocks = Math.max(1.0D, config.getDouble("hologram.visible-radius-blocks", 16.0D));

        this.guiTitle = config.getString("gui.title", "&bVoederbak");
        this.guiRows = Math.max(1, Math.min(6, config.getInt("gui.rows", 3)));

        this.fillerEnabled = config.getBoolean("gui.filler.enabled", true);
        this.fillerMaterial = parseMaterial(config.getString("gui.filler.material"), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.fillerName = config.getString("gui.filler.name", " ");

        this.infoSlot = config.getInt("gui.slots.info", 10);
        this.openStorageSlot = config.getInt("gui.slots.open-storage", 12);
        this.showRadiusSlot = config.getInt("gui.slots.show-radius", 14);
        this.toggleHologramSlot = config.getInt("gui.slots.toggle-hologram", 16);
        this.upgradeSlot = config.getInt("gui.slots.upgrade", 22);

        this.infoMaterial = parseMaterial(config.getString("gui.info-item.material"), Material.CAULDRON);
        this.infoName = config.getString("gui.info-item.name", "&bFeedstation Info");
        this.infoLore = new ArrayList<>(config.getStringList("gui.info-item.lore"));

        this.storageButtonMaterial = parseMaterial(config.getString("gui.storage-button.material"), Material.CHEST);
        this.storageButtonName = config.getString("gui.storage-button.name", "&bOpen inhoud");
        this.storageButtonLore = new ArrayList<>(config.getStringList("gui.storage-button.lore"));

        this.radiusButtonMaterial = parseMaterial(config.getString("gui.radius-button.material"), Material.BLAZE_POWDER);
        this.radiusButtonName = config.getString("gui.radius-button.name", "&bToon radius");
        this.radiusButtonLore = new ArrayList<>(config.getStringList("gui.radius-button.lore"));

        this.hologramEnabledMaterial = parseMaterial(config.getString("gui.hologram-button.enabled-material"), Material.LIME_DYE);
        this.hologramDisabledMaterial = parseMaterial(config.getString("gui.hologram-button.disabled-material"), Material.GRAY_DYE);
        this.hologramButtonName = config.getString("gui.hologram-button.name", "&bHologram");
        this.hologramEnabledLore = new ArrayList<>(config.getStringList("gui.hologram-button.enabled-lore"));
        this.hologramDisabledLore = new ArrayList<>(config.getStringList("gui.hologram-button.disabled-lore"));

        this.storageGuiTitle = config.getString("storage-gui.title", "&bFeedstation Inhoud");
        this.blockedSlotMaterial = parseMaterial(config.getString("storage-gui.blocked-slot-item.material"), Material.RED_STAINED_GLASS_PANE);
        this.blockedSlotName = config.getString("storage-gui.blocked-slot-item.name", "&cBlocked slot");
        this.blockedSlotLore = new ArrayList<>(config.getStringList("storage-gui.blocked-slot-item.lore"));

        this.radiusPreviewDurationTicks = Math.max(20L, config.getLong("radius-preview.duration-ticks", 100L));
        this.radiusPreviewParticle = parseParticle(config.getString("radius-preview.particle"), Particle.END_ROD);
        this.radiusPreviewCountPerPoint = Math.max(1, config.getInt("radius-preview.count-per-point", 1));
        this.radiusPreviewPoints = Math.max(8, config.getInt("radius-preview.points", 80));
        this.radiusPreviewYOffset = config.getDouble("radius-preview.y-offset", 0.15D);

        this.upgradeEnabledName = config.getString("gui.upgrade-item.enabled-name", "&aUpgrade");
        this.upgradeDisabledName = config.getString("gui.upgrade-item.disabled-name", "&cDisabled");
        this.upgradeEnabledLore = new ArrayList<>(config.getStringList("gui.upgrade-item.enabled-lore"));
        this.upgradeDisabledLore = new ArrayList<>(config.getStringList("gui.upgrade-item.disabled-lore"));

        this.upgradesEnabled = config.getBoolean("upgrades.enabled", true);
        this.upgradeChain = loadUpgradeChain(config.getConfigurationSection("upgrades.chain"));
        this.upgradeCosts = loadUpgradeCosts(config.getConfigurationSection("upgrades.costs"));

        this.taskTickInterval = Math.max(20L, config.getLong("task.tick-interval", 40L));

        this.onlyWhenHungry = config.getBoolean("feeding.only-when-hungry", true);
        this.consumeOneItemPerFeed = config.getBoolean("feeding.consume-one-item-per-feed", true);
        this.skipFullAnimals = config.getBoolean("feeding.skip-full-animals", true);
        this.prioritizeLowestHunger = config.getBoolean("feeding.prioritize-lowest-hunger", true);

        this.walkingAiEnabled = config.getBoolean("walking-ai.enabled", true);
        this.walkingAiRepathIntervalTicks = Math.max(10L, config.getLong("walking-ai.repath-interval-ticks", 60L));
        this.walkingAiHungerThresholdPercent = clampPercent(config.getDouble("walking-ai.hunger-threshold-percent", 85.0D));
        this.walkingAiRandomTargetRadius = Math.max(0.0D, config.getDouble("walking-ai.random-target-radius", 1.2D));
        this.walkingAiMinDistanceToStart = Math.max(0.5D, config.getDouble("walking-ai.min-distance-to-start", 2.0D));

        this.idleParticles = loadParticleSection(config.getConfigurationSection("particles.idle"), Particle.HAPPY_VILLAGER);
        this.feedingParticles = loadParticleSection(config.getConfigurationSection("particles.feeding"), Particle.HEART);
        this.attractionParticles = loadParticleSection(config.getConfigurationSection("particles.attraction"), Particle.END_ROD);

        this.soundsEnabled = config.getBoolean("sounds.enabled", true);
        this.openSound = parseSound(config.getString("sounds.open"), Sound.BLOCK_CHEST_OPEN);
        this.depositSound = parseSound(config.getString("sounds.deposit"), Sound.ENTITY_ITEM_PICKUP);
        this.feedSound = parseSound(config.getString("sounds.feed"), Sound.ENTITY_GENERIC_EAT);
        this.upgradeSound = parseSound(config.getString("sounds.upgrade"), Sound.ENTITY_PLAYER_LEVELUP);

        this.tiers = loadTiers(config.getConfigurationSection("tiers"));
    }

    private Map<FeederTier, TierSettings> loadTiers(ConfigurationSection section) {
        Map<FeederTier, TierSettings> result = new EnumMap<>(FeederTier.class);

        for (FeederTier tier : FeederTier.values()) {
            ConfigurationSection tierSection = section != null ? section.getConfigurationSection(tier.name()) : null;

            if (tierSection == null) {
                result.put(tier, new TierSettings(
                        tier,
                        tier.getDefaultDisplay(),
                        tier.getDefaultMaxAnimals(),
                        tier.getDefaultRadius(),
                        tier.getDefaultAttractionRange(),
                        tier.getDefaultAttractionSpeed(),
                        tier.getDefaultFeedIntervalSeconds(),
                        9,
                        2.0D
                ));
                continue;
            }

            result.put(tier, new TierSettings(
                    tier,
                    tierSection.getString("display", tier.getDefaultDisplay()),
                    Math.max(1, tierSection.getInt("max-animals", tier.getDefaultMaxAnimals())),
                    Math.max(1.0D, tierSection.getDouble("radius", tier.getDefaultRadius())),
                    Math.max(1.0D, tierSection.getDouble("attraction-range", tier.getDefaultAttractionRange())),
                    Math.max(0.01D, tierSection.getDouble("attraction-speed", tier.getDefaultAttractionSpeed())),
                    Math.max(1, tierSection.getInt("feed-interval-seconds", tier.getDefaultFeedIntervalSeconds())),
                    clampStorageSlots(tierSection.getInt("storage-slots", 9)),
                    Math.max(0.5D, tierSection.getDouble("eat-distance", 2.0D))
            ));
        }

        return result;
    }

    private int clampStorageSlots(int slots) {
        return Math.max(1, Math.min(54, slots));
    }

    private double clampPercent(double value) {
        return Math.max(0.0D, Math.min(100.0D, value));
    }

    private Map<FeederTier, FeederTier> loadUpgradeChain(ConfigurationSection section) {
        Map<FeederTier, FeederTier> result = new EnumMap<>(FeederTier.class);

        if (section == null) {
            return result;
        }

        for (String key : section.getKeys(false)) {
            FeederTier from = FeederTier.fromString(key);
            FeederTier to = FeederTier.fromString(section.getString(key));

            if (from != null && to != null) {
                result.put(from, to);
            }
        }

        return result;
    }

    private Map<FeederTier, UpgradeCost> loadUpgradeCosts(ConfigurationSection section) {
        Map<FeederTier, UpgradeCost> result = new EnumMap<>(FeederTier.class);

        if (section == null) {
            return result;
        }

        for (String key : section.getKeys(false)) {
            FeederTier tier = FeederTier.fromString(key);
            if (tier == null) {
                continue;
            }

            ConfigurationSection costSection = section.getConfigurationSection(key);
            if (costSection == null) {
                continue;
            }

            result.put(tier, new UpgradeCost(
                    parseMaterial(costSection.getString("material"), Material.IRON_INGOT),
                    Math.max(1, costSection.getInt("amount", 1))
            ));
        }

        return result;
    }

    private ParticleSection loadParticleSection(ConfigurationSection section, Particle fallbackParticle) {
        if (section == null) {
            return new ParticleSection(false, fallbackParticle, 1, 0.1D, 0.1D, 0.1D, 0.0D);
        }

        return new ParticleSection(
                section.getBoolean("enabled", true),
                parseParticle(section.getString("particle"), fallbackParticle),
                Math.max(0, section.getInt("count", 1)),
                Math.max(0.0D, section.getDouble("spread-x", 0.1D)),
                Math.max(0.0D, section.getDouble("spread-y", 0.1D)),
                Math.max(0.0D, section.getDouble("spread-z", 0.1D)),
                Math.max(0.0D, section.getDouble("extra", 0.0D))
        );
    }

    private Material parseMaterial(String input, Material fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }

        try {
            return Material.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private Sound parseSound(String input, Sound fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }

        try {
            return Sound.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private Particle parseParticle(String input, Particle fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }

        try {
            return Particle.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public String getItemName() {
        return itemName;
    }

    public List<String> getItemLore() {
        return Collections.unmodifiableList(itemLore);
    }

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public List<String> getHologramLines() {
        return Collections.unmodifiableList(hologramLines);
    }

    public double getHologramOffsetY() {
        return hologramOffsetY;
    }

    public long getHologramRefreshTicks() {
        return hologramRefreshTicks;
    }

    public double getHologramVisibleRadiusBlocks() {
        return hologramVisibleRadiusBlocks;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiRows() {
        return guiRows;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public String getFillerName() {
        return fillerName;
    }

    public int getInfoSlot() {
        return infoSlot;
    }

    public int getOpenStorageSlot() {
        return openStorageSlot;
    }

    public int getShowRadiusSlot() {
        return showRadiusSlot;
    }

    public int getToggleHologramSlot() {
        return toggleHologramSlot;
    }

    public int getUpgradeSlot() {
        return upgradeSlot;
    }

    public Material getInfoMaterial() {
        return infoMaterial;
    }

    public String getInfoName() {
        return infoName;
    }

    public List<String> getInfoLore() {
        return Collections.unmodifiableList(infoLore);
    }

    public Material getStorageButtonMaterial() {
        return storageButtonMaterial;
    }

    public String getStorageButtonName() {
        return storageButtonName;
    }

    public List<String> getStorageButtonLore() {
        return Collections.unmodifiableList(storageButtonLore);
    }

    public Material getRadiusButtonMaterial() {
        return radiusButtonMaterial;
    }

    public String getRadiusButtonName() {
        return radiusButtonName;
    }

    public List<String> getRadiusButtonLore() {
        return Collections.unmodifiableList(radiusButtonLore);
    }

    public Material getHologramEnabledMaterial() {
        return hologramEnabledMaterial;
    }

    public Material getHologramDisabledMaterial() {
        return hologramDisabledMaterial;
    }

    public String getHologramButtonName() {
        return hologramButtonName;
    }

    public List<String> getHologramEnabledLore() {
        return Collections.unmodifiableList(hologramEnabledLore);
    }

    public List<String> getHologramDisabledLore() {
        return Collections.unmodifiableList(hologramDisabledLore);
    }

    public String getStorageGuiTitle() {
        return storageGuiTitle;
    }

    public Material getBlockedSlotMaterial() {
        return blockedSlotMaterial;
    }

    public String getBlockedSlotName() {
        return blockedSlotName;
    }

    public List<String> getBlockedSlotLore() {
        return Collections.unmodifiableList(blockedSlotLore);
    }

    public long getRadiusPreviewDurationTicks() {
        return radiusPreviewDurationTicks;
    }

    public Particle getRadiusPreviewParticle() {
        return radiusPreviewParticle;
    }

    public int getRadiusPreviewCountPerPoint() {
        return radiusPreviewCountPerPoint;
    }

    public int getRadiusPreviewPoints() {
        return radiusPreviewPoints;
    }

    public double getRadiusPreviewYOffset() {
        return radiusPreviewYOffset;
    }

    public String getUpgradeEnabledName() {
        return upgradeEnabledName;
    }

    public String getUpgradeDisabledName() {
        return upgradeDisabledName;
    }

    public List<String> getUpgradeEnabledLore() {
        return Collections.unmodifiableList(upgradeEnabledLore);
    }

    public List<String> getUpgradeDisabledLore() {
        return Collections.unmodifiableList(upgradeDisabledLore);
    }

    public boolean isUpgradesEnabled() {
        return upgradesEnabled;
    }

    public FeederTier getNextTier(FeederTier currentTier) {
        return upgradeChain.get(currentTier);
    }

    public UpgradeCost getUpgradeCost(FeederTier targetTier) {
        return upgradeCosts.get(targetTier);
    }

    public long getTaskTickInterval() {
        return taskTickInterval;
    }

    public boolean isOnlyWhenHungry() {
        return onlyWhenHungry;
    }

    public boolean isConsumeOneItemPerFeed() {
        return consumeOneItemPerFeed;
    }

    public boolean isSkipFullAnimals() {
        return skipFullAnimals;
    }

    public boolean isPrioritizeLowestHunger() {
        return prioritizeLowestHunger;
    }

    public boolean isWalkingAiEnabled() {
        return walkingAiEnabled;
    }

    public long getWalkingAiRepathIntervalTicks() {
        return walkingAiRepathIntervalTicks;
    }

    public double getWalkingAiHungerThresholdPercent() {
        return walkingAiHungerThresholdPercent;
    }

    public double getWalkingAiRandomTargetRadius() {
        return walkingAiRandomTargetRadius;
    }

    public double getWalkingAiMinDistanceToStart() {
        return walkingAiMinDistanceToStart;
    }

    public ParticleSection getIdleParticles() {
        return idleParticles;
    }

    public ParticleSection getFeedingParticles() {
        return feedingParticles;
    }

    public ParticleSection getAttractionParticles() {
        return attractionParticles;
    }

    public boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public Sound getOpenSound() {
        return openSound;
    }

    public Sound getDepositSound() {
        return depositSound;
    }

    public Sound getFeedSound() {
        return feedSound;
    }

    public Sound getUpgradeSound() {
        return upgradeSound;
    }

    public Map<FeederTier, TierSettings> getTiers() {
        return Collections.unmodifiableMap(tiers);
    }

    public TierSettings getTierSettings(FeederTier tier) {
        return tiers.get(tier);
    }

    public record TierSettings(
            FeederTier tier,
            String display,
            int maxAnimals,
            double radius,
            double attractionRange,
            double attractionSpeed,
            int feedIntervalSeconds,
            int storageSlots,
            double eatDistance
    ) {}

    public record UpgradeCost(
            Material material,
            int amount
    ) {}

    public record ParticleSection(
            boolean enabled,
            Particle particle,
            int count,
            double spreadX,
            double spreadY,
            double spreadZ,
            double extra
    ) {}
}