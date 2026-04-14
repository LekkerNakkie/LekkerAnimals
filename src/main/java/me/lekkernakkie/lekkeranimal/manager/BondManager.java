package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.AnimalsSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BondManager {

    private final LekkerAnimal plugin;
    private final AnimalManager animalManager;

    public BondManager(LekkerAnimal plugin, AnimalManager animalManager) {
        this.plugin = plugin;
        this.animalManager = animalManager;
    }

    public BondResult tryBond(Player player, Entity entity, ItemStack itemInHand) {
        AnimalsSettings settings = plugin.getConfigManager().getAnimalsSettings();
        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();

        if (!animalManager.isSupported(entity)) {
            return BondResult.NOT_SUPPORTED;
        }

        if (!settings.isSupported(entity.getType())) {
            return BondResult.NOT_SUPPORTED;
        }

        if (animalManager.isBonded(entity)) {
            AnimalData existing = animalManager.getAnimalData(entity);
            if (existing != null && existing.getOwnerUuid().equals(player.getUniqueId())) {
                return BondResult.ALREADY_YOURS;
            }
            return BondResult.ALREADY_OWNED;
        }

        AnimalProfile profile = settings.getProfile(entity.getType());
        if (profile == null) {
            return BondResult.NOT_SUPPORTED;
        }

        if (itemInHand == null || itemInHand.getType() != profile.getBondItem()) {
            return BondResult.WRONG_ITEM;
        }

        if (itemInHand.getAmount() < profile.getRequiredBondAmount()) {
            return BondResult.NOT_ENOUGH_ITEMS;
        }

        AnimalData data = new AnimalData(
                entity.getUniqueId(),
                player.getUniqueId(),
                player.getName(),
                entity.getType(),
                profile.getMaxHunger(),
                profile.getMaxHunger(),
                mainSettings.getStartLevel(),
                0,
                mainSettings.getStartBond()
        );

        data.syncLocation(entity);

        animalManager.registerAnimal(data);
        consumeItem(itemInHand, profile.getRequiredBondAmount());
        plugin.getDataManager().saveAnimal(data);

        return BondResult.SUCCESS;
    }

    private void consumeItem(ItemStack item, int amount) {
        int newAmount = item.getAmount() - amount;
        item.setAmount(Math.max(newAmount, 0));
    }

    public enum BondResult {
        SUCCESS,
        NOT_SUPPORTED,
        WRONG_ITEM,
        NOT_ENOUGH_ITEMS,
        ALREADY_OWNED,
        ALREADY_YOURS
    }
}