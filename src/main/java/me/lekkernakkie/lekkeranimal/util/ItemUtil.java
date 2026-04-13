package me.lekkernakkie.lekkeranimal.util;

import org.bukkit.inventory.ItemStack;

public final class ItemUtil {

    private ItemUtil() {
    }

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() <= 0;
    }
}