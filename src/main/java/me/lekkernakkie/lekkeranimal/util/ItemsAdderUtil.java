package me.lekkernakkie.lekkeranimal.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ItemsAdderUtil {

    private ItemsAdderUtil() {
    }

    public static ItemStack createGuiItem(String idOrMaterial, Material fallback, String name, List<String> lore) {
        ItemStack item = createBaseItem(idOrMaterial, fallback);

        if (item == null || item.getType().isAir()) {
            item = new ItemStack(fallback);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.colorize(name));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ColorUtil.colorize(line));
        }

        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createBaseItem(String idOrMaterial, Material fallback) {
        if (idOrMaterial == null || idOrMaterial.isBlank()) {
            return new ItemStack(fallback);
        }

        if (idOrMaterial.contains(":")) {
            ItemStack custom = createItemsAdderItem(idOrMaterial);
            if (custom != null) {
                return custom;
            }
        }

        try {
            return new ItemStack(Material.valueOf(idOrMaterial.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return new ItemStack(fallback);
        }
    }

    private static ItemStack createItemsAdderItem(String namespacedId) {
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Method getInstanceMethod = customStackClass.getMethod("getInstance", String.class);
            Object customStack = getInstanceMethod.invoke(null, namespacedId);

            if (customStack == null) {
                return null;
            }

            Method getItemStackMethod = customStackClass.getMethod("getItemStack");
            Object item = getItemStackMethod.invoke(customStack);

            if (item instanceof ItemStack itemStack) {
                return itemStack.clone();
            }
        } catch (Throwable ignored) {
        }

        return null;
    }
}