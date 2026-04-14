package me.lekkernakkie.lekkeranimal.data;

public class FeedingReward {

    private final int hungerRestore;
    private final int xp;
    private final int bondGain;

    public FeedingReward(int hungerRestore, int xp, int bondGain) {
        this.hungerRestore = hungerRestore;
        this.xp = xp;
        this.bondGain = bondGain;
    }

    public int getHungerRestore() {
        return hungerRestore;
    }

    public int getXp() {
        return xp;
    }

    public int getBondGain() {
        return bondGain;
    }
}