package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HarvestDrop {

    private final Material material;
    private final int guaranteedAmount;
    private final List<BonusRoll> bonusRolls;
    private final String displayName;
    private final String headTexture;
    private final String headOwner;
    private final String rarity;

    public HarvestDrop(Material material,
                       int guaranteedAmount,
                       List<BonusRoll> bonusRolls,
                       String displayName,
                       String headTexture,
                       String headOwner,
                       String rarity) {
        this.material = material;
        this.guaranteedAmount = Math.max(0, guaranteedAmount);
        this.bonusRolls = bonusRolls != null ? new ArrayList<>(bonusRolls) : new ArrayList<>();
        this.displayName = displayName != null ? displayName : "";
        this.headTexture = headTexture != null ? headTexture : "";
        this.headOwner = headOwner != null ? headOwner : "";
        this.rarity = rarity != null && !rarity.isBlank() ? rarity : "COMMON";
    }

    public Material getMaterial() {
        return material;
    }

    public int getGuaranteedAmount() {
        return guaranteedAmount;
    }

    public List<BonusRoll> getBonusRolls() {
        return Collections.unmodifiableList(bonusRolls);
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
        return !headTexture.isBlank();
    }

    public boolean hasHeadOwner() {
        return !headOwner.isBlank();
    }

    public boolean hasRarity() {
        return !rarity.isBlank();
    }

    public int getMinimumAmount() {
        return guaranteedAmount;
    }

    public int getMaximumAmount() {
        int total = guaranteedAmount;
        for (BonusRoll roll : bonusRolls) {
            total += Math.max(0, roll.getAmount());
        }
        return total;
    }

    public boolean isGuaranteed() {
        return guaranteedAmount > 0 && bonusRolls.isEmpty();
    }
}