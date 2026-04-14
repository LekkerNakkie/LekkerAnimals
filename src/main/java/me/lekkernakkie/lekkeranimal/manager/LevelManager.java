package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LevelManager {

    private final LekkerAnimal plugin;

    public LevelManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public int addXp(AnimalData data, AnimalProfile profile, int amount) {
        if (amount <= 0 || data == null || profile == null) {
            return 0;
        }

        if (data.getLevel() >= profile.getMaxLevel()) {
            data.setXp(0);
            return 0;
        }

        data.setXp(data.getXp() + amount);

        int levelsGained = 0;

        while (data.getLevel() < profile.getMaxLevel()) {
            int requiredXp = getRequiredXpForNextLevel(data.getLevel());

            if (data.getXp() < requiredXp) {
                break;
            }

            data.setXp(data.getXp() - requiredXp);
            data.setLevel(data.getLevel() + 1);
            levelsGained++;
        }

        if (data.getLevel() >= profile.getMaxLevel()) {
            data.setXp(0);
        }

        return levelsGained;
    }

    public DirectUpgradeResult tryDirectUpgrade(Player player, Entity entity, ItemStack itemInHand) {
        if (player == null || entity == null || itemInHand == null) {
            return DirectUpgradeResult.NO_CONFIG;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            return DirectUpgradeResult.NO_CONFIG;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            return DirectUpgradeResult.NO_CONFIG;
        }

        if (data.getLevel() >= profile.getMaxLevel()) {
            return DirectUpgradeResult.MAX_LEVEL;
        }

        int targetLevel = data.getLevel() + 1;
        DirectLevelUpgrade upgrade = profile.getDirectLevelUpgrade(targetLevel);

        if (upgrade == null) {
            return DirectUpgradeResult.NO_CONFIG;
        }

        if (itemInHand.getType() != upgrade.getItem()) {
            return DirectUpgradeResult.WRONG_ITEM;
        }

        if (itemInHand.getAmount() < upgrade.getAmount()) {
            return DirectUpgradeResult.NOT_ENOUGH_ITEMS;
        }

        consume(itemInHand, upgrade.getAmount());

        data.setLevel(targetLevel);
        data.setXp(0);

        return DirectUpgradeResult.SUCCESS;
    }

    public int getRequiredXpForNextLevel(int currentLevel) {
        MainSettings settings = plugin.getConfigManager().getMainSettings();
        double base = settings.getBaseXp();
        double scale = settings.getXpScale();

        return (int) Math.max(1, Math.round(base * Math.pow(scale, Math.max(0, currentLevel - 1))));
    }

    private void consume(ItemStack item, int amount) {
        int newAmount = item.getAmount() - amount;
        item.setAmount(Math.max(newAmount, 0));
    }

    public enum DirectUpgradeResult {
        SUCCESS,
        MAX_LEVEL,
        NO_CONFIG,
        WRONG_ITEM,
        NOT_ENOUGH_ITEMS
    }
}