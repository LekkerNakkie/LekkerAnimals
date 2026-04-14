package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;

public class HarvestDrop {

    private final Material material;
    private final int amount;
    private final double chance;
    private final String displayName;

    public HarvestDrop(Material material, int amount, double chance, String displayName) {
        this.material = material;
        this.amount = amount;
        this.chance = chance;
        this.displayName = displayName;
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

    public boolean isGuaranteed() {
        return chance >= 100.0;
    }
}