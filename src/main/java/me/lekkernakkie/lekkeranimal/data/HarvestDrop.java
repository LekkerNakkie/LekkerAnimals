package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;

public class HarvestDrop {

    private final Material material;
    private final int amount;
    private final double chance;
    private final String displayName;
    private final String headTexture;
    private final String headOwner;

    public HarvestDrop(Material material, int amount, double chance, String displayName, String headTexture, String headOwner) {
        this.material = material;
        this.amount = amount;
        this.chance = chance;
        this.displayName = displayName;
        this.headTexture = headTexture;
        this.headOwner = headOwner;
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

    public boolean hasHeadTexture() {
        return headTexture != null && !headTexture.isBlank();
    }

    public boolean hasHeadOwner() {
        return headOwner != null && !headOwner.isBlank();
    }

    public boolean isGuaranteed() {
        return chance >= 100.0D;
    }
}