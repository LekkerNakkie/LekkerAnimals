package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;

public class HarvestDrop {

    private final Material material;
    private final int amount;
    private final double chance;
    private final String displayName;
    private final String headTexture;
    private final String headOwner;
    private final String rarity;

    public HarvestDrop(Material material,
                       int amount,
                       double chance,
                       String displayName,
                       String headTexture,
                       String headOwner,
                       String rarity) {
        this.material = material;
        this.amount = amount;
        this.chance = chance;
        this.displayName = displayName;
        this.headTexture = headTexture;
        this.headOwner = headOwner;
        this.rarity = rarity;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHeadTexture() {
        return headTexture;
    }

    public String getHeadOwner() {
        return headOwner;
    }

    public String getRarity() {
        return rarity;
    }

    public boolean hasHeadTexture() {
        return headTexture != null && !headTexture.isBlank();
    }

    public boolean hasHeadOwner() {
        return headOwner != null && !headOwner.isBlank();
    }

    public boolean hasRarity() {
        return rarity != null && !rarity.isBlank();
    }

    public boolean isGuaranteed() {
        return chance >= 100.0D;
    }
}