package me.lekkernakkie.lekkeranimal.config;

import java.util.Locale;

public enum FeederTier {
    COMMON("&fCommon", 2, 8.0D, 12.0D, 0.22D, 20),
    UNCOMMON("&aUncommon", 5, 10.0D, 14.0D, 0.24D, 18),
    RARE("&dRare", 7, 12.0D, 16.0D, 0.26D, 16),
    ULTRA("&5Ultra", 10, 14.0D, 18.0D, 0.28D, 14),
    LEGENDARY("&6Legendary", 15, 16.0D, 20.0D, 0.30D, 12);

    private final String defaultDisplay;
    private final int defaultMaxAnimals;
    private final double defaultRadius;
    private final double defaultAttractionRange;
    private final double defaultAttractionSpeed;
    private final int defaultFeedIntervalSeconds;

    FeederTier(String defaultDisplay,
               int defaultMaxAnimals,
               double defaultRadius,
               double defaultAttractionRange,
               double defaultAttractionSpeed,
               int defaultFeedIntervalSeconds) {
        this.defaultDisplay = defaultDisplay;
        this.defaultMaxAnimals = defaultMaxAnimals;
        this.defaultRadius = defaultRadius;
        this.defaultAttractionRange = defaultAttractionRange;
        this.defaultAttractionSpeed = defaultAttractionSpeed;
        this.defaultFeedIntervalSeconds = defaultFeedIntervalSeconds;
    }

    public static FeederTier fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            return valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public String getDefaultDisplay() {
        return defaultDisplay;
    }

    public int getDefaultMaxAnimals() {
        return defaultMaxAnimals;
    }

    public double getDefaultRadius() {
        return defaultRadius;
    }

    public double getDefaultAttractionRange() {
        return defaultAttractionRange;
    }

    public double getDefaultAttractionSpeed() {
        return defaultAttractionSpeed;
    }

    public int getDefaultFeedIntervalSeconds() {
        return defaultFeedIntervalSeconds;
    }
}