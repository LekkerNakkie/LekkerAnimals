package me.lekkernakkie.lekkeranimal.data;

public class BonusRoll {

    private final int amount;
    private final double chance;

    public BonusRoll(int amount, double chance) {
        this.amount = Math.max(0, amount);
        this.chance = Math.max(0.0D, Math.min(100.0D, chance));
    }

    public int getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }
}