package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;

public class DirectLevelUpgrade {

    private final Material item;
    private final int amount;

    public DirectLevelUpgrade(Material item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public Material getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }
}