package me.lekkernakkie.lekkeranimal.util;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FeederItemUtil {

    private FeederItemUtil() {
    }

    public static ItemStack createFeederItem(LekkerAnimal plugin,
                                             UUID ownerUuid,
                                             String ownerName,
                                             FeederTier tier) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(tier);

        Material material = settings.getItemMaterial();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        String tierDisplay = tierSettings != null ? tierSettings.display() : tier.name();
        int maxAnimals = tierSettings != null ? tierSettings.maxAnimals() : 0;
        double radius = tierSettings != null ? tierSettings.radius() : 0.0D;

        String displayName = settings.getItemName()
                .replace("{owner}", ownerName != null ? ownerName : "Unknown")
                .replace("{tier}", tier.name())
                .replace("{tier_display}", tierDisplay)
                .replace("{max_animals}", String.valueOf(maxAnimals))
                .replace("{radius}", trimDouble(radius));

        meta.setDisplayName(ColorUtil.colorize(displayName));

        List<String> lore = new ArrayList<>();
        for (String line : settings.getItemLore()) {
            lore.add(ColorUtil.colorize(
                    line.replace("{owner}", ownerName != null ? ownerName : "Unknown")
                            .replace("{tier}", tier.name())
                            .replace("{tier_display}", tierDisplay)
                            .replace("{max_animals}", String.valueOf(maxAnimals))
                            .replace("{radius}", trimDouble(radius))
            ));
        }
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_item"),
                PersistentDataType.BYTE,
                (byte) 1
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_tier"),
                PersistentDataType.STRING,
                tier.name()
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_owner_uuid"),
                PersistentDataType.STRING,
                ownerUuid.toString()
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_owner_name"),
                PersistentDataType.STRING,
                ownerName != null ? ownerName : ""
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isFeederItem(LekkerAnimal plugin, ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        Byte marker = meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "feeder_item"),
                PersistentDataType.BYTE
        );

        return marker != null && marker == (byte) 1;
    }

    public static FeederTier getTier(LekkerAnimal plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        String raw = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "feeder_tier"),
                PersistentDataType.STRING
        );

        return FeederTier.fromString(raw);
    }

    public static UUID getOwnerUuid(LekkerAnimal plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        String raw = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "feeder_owner_uuid"),
                PersistentDataType.STRING
        );

        try {
            return raw != null ? UUID.fromString(raw) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static String getOwnerName(LekkerAnimal plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return "";
        }

        String raw = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "feeder_owner_name"),
                PersistentDataType.STRING
        );

        return raw != null ? raw : "";
    }

    public static String resolveOwnerName(LekkerAnimal plugin, UUID ownerUuid, String fallback) {
        if (ownerUuid == null) {
            return fallback != null ? fallback : "";
        }

        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(ownerUuid);
        if (offlinePlayer.getName() != null && !offlinePlayer.getName().isBlank()) {
            return offlinePlayer.getName();
        }

        return fallback != null ? fallback : "";
    }

    private static String trimDouble(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }
}