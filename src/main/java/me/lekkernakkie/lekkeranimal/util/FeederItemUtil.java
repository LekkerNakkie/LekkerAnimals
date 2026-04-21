package me.lekkernakkie.lekkeranimal.util;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public final class FeederItemUtil {

    private FeederItemUtil() {
    }

    public static ItemStack createFeederItem(LekkerAnimal plugin,
                                             UUID ownerUuid,
                                             String ownerName,
                                             FeederTier tier,
                                             List<ItemStack> storedFood,
                                             boolean hologramEnabled) {
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
        int storageSlots = tierSettings != null ? tierSettings.storageSlots() : 0;
        int foodAmount = countFood(storedFood);

        String hologramStatus = hologramEnabled ? "&aAAN" : "&cUIT";

        String displayName = settings.getItemName()
                .replace("{owner}", ownerName != null ? ownerName : "Unknown")
                .replace("{tier}", tier.name())
                .replace("{tier_display}", tierDisplay)
                .replace("{max_animals}", String.valueOf(maxAnimals))
                .replace("{radius}", trimDouble(radius))
                .replace("{storage_slots}", String.valueOf(storageSlots))
                .replace("{food_amount}", String.valueOf(foodAmount))
                .replace("{hologram_status}", hologramStatus);

        meta.setDisplayName(ColorUtil.colorize(displayName));

        List<String> lore = new ArrayList<>();
        for (String line : settings.getItemLore()) {
            lore.add(ColorUtil.colorize(
                    line.replace("{owner}", ownerName != null ? ownerName : "Unknown")
                            .replace("{tier}", tier.name())
                            .replace("{tier_display}", tierDisplay)
                            .replace("{max_animals}", String.valueOf(maxAnimals))
                            .replace("{radius}", trimDouble(radius))
                            .replace("{storage_slots}", String.valueOf(storageSlots))
                            .replace("{food_amount}", String.valueOf(foodAmount))
                            .replace("{hologram_status}", hologramStatus)
            ));
        }
        meta.setLore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS
        );

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
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_item_instance"),
                PersistentDataType.STRING,
                UUID.randomUUID().toString()
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_stored_food"),
                PersistentDataType.STRING,
                serializeItemList(storedFood)
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "feeder_hologram_enabled"),
                PersistentDataType.BYTE,
                hologramEnabled ? (byte) 1 : (byte) 0
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

    public static boolean isHologramEnabled(LekkerAnimal plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return true;
        }

        Byte raw = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "feeder_hologram_enabled"),
                PersistentDataType.BYTE
        );

        return raw == null || raw == (byte) 1;
    }

    public static List<ItemStack> getStoredFood(LekkerAnimal plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return new ArrayList<>();
        }

        String raw = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(plugin, "feeder_stored_food"),
                PersistentDataType.STRING
        );

        return deserializeItemList(raw);
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

    private static int countFood(List<ItemStack> storedFood) {
        int total = 0;
        if (storedFood == null) {
            return total;
        }

        for (ItemStack stack : storedFood) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            total += stack.getAmount();
        }
        return total;
    }

    private static String serializeItemList(List<ItemStack> items) {
        try {
            List<ItemStack> safeItems = new ArrayList<>();
            if (items != null) {
                for (ItemStack item : items) {
                    if (item == null || item.getType().isAir()) {
                        continue;
                    }
                    safeItems.add(item.clone());
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(safeItems.size());

            for (ItemStack item : safeItems) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception ex) {
            return "";
        }
    }

    private static List<ItemStack> deserializeItemList(String data) {
        List<ItemStack> result = new ArrayList<>();

        if (data == null || data.isBlank()) {
            return result;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();

            for (int i = 0; i < size; i++) {
                Object object = dataInput.readObject();
                if (object instanceof ItemStack itemStack && !itemStack.getType().isAir()) {
                    result.add(itemStack);
                }
            }

            dataInput.close();
        } catch (Exception ignored) {
        }

        return result;
    }

    private static String trimDouble(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }
}