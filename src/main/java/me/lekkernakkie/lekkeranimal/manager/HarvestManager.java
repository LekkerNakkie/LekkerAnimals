package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.HarvestDrop;
import me.lekkernakkie.lekkeranimal.data.HarvestLevelProfile;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HarvestManager {

    public static final String DEFAULT_RARITY_ID = "COMMON";

    private final LekkerAnimal plugin;

    private final NamespacedKey customHeadKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey animalNameKey;
    private final NamespacedKey headOwnerKey;
    private final NamespacedKey headTextureKey;

    public HarvestManager(LekkerAnimal plugin) {
        this.plugin = plugin;
        this.customHeadKey = new NamespacedKey(plugin, "lekkeranimal_head");
        this.rarityKey = new NamespacedKey(plugin, "lekkeranimal_head_rarity");
        this.animalNameKey = new NamespacedKey(plugin, "lekkeranimal_head_animal_name");
        this.headOwnerKey = new NamespacedKey(plugin, "lekkeranimal_head_owner");
        this.headTextureKey = new NamespacedKey(plugin, "lekkeranimal_head_texture");
    }

    public HarvestResult tryHarvest(Player player, Entity entity, AnimalData data, AnimalProfile profile) {
        if (!profile.isHarvestingEnabled()) {
            return HarvestResult.DISABLED;
        }

        HarvestLevelProfile levelProfile = profile.getHarvestProfileForLevel(data.getLevel());
        if (levelProfile == null || levelProfile.getDrops().isEmpty()) {
            return HarvestResult.NO_DROPS;
        }

        if (!isReady(data, profile)) {
            return HarvestResult.NOT_READY;
        }

        for (HarvestDrop drop : levelProfile.getDrops()) {
            if (!roll(drop.getChance())) {
                continue;
            }

            ItemStack item = createDrop(drop, entity, profile);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            giveOrDrop(player, item);
        }

        data.setHarvestProgressMillis(0L);
        plugin.getDataManager().saveAnimal(data);

        return HarvestResult.SUCCESS;
    }

    public boolean isReady(AnimalData data, AnimalProfile profile) {
        if (!profile.isHarvestingEnabled()) {
            return false;
        }

        long cooldownMillis = profile.getHarvestCooldownSeconds() * 1000L;
        return data.getHarvestProgressMillis() >= cooldownMillis;
    }

    public long getTimeLeftMillis(AnimalData data, AnimalProfile profile) {
        if (!profile.isHarvestingEnabled()) {
            return 0L;
        }

        long cooldownMillis = profile.getHarvestCooldownSeconds() * 1000L;
        return Math.max(0L, cooldownMillis - data.getHarvestProgressMillis());
    }

    public int getProgressPercent(AnimalData data, AnimalProfile profile) {
        if (!profile.isHarvestingEnabled()) {
            return 0;
        }

        long cooldownMillis = profile.getHarvestCooldownSeconds() * 1000L;
        if (cooldownMillis <= 0L) {
            return 100;
        }

        double progress = Math.min(1.0D, data.getHarvestProgressMillis() / (double) cooldownMillis);
        return (int) Math.round(progress * 100.0D);
    }

    public String formatTimeLeft(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public String getPreview(AnimalProfile profile, int level) {
        HarvestLevelProfile harvestLevelProfile = profile.getHarvestProfileForLevel(level);
        if (harvestLevelProfile == null || harvestLevelProfile.getDrops().isEmpty()) {
            return ColorUtil.colorize("&cGeen drops");
        }

        List<String> parts = new ArrayList<>();

        for (HarvestDrop drop : harvestLevelProfile.getDrops()) {
            String name;

            if (drop.getMaterial() == Material.PLAYER_HEAD) {
                name = ColorUtil.colorize(buildHeadDisplayName(
                        normalizeRarity(drop.getRarity()),
                        profile.getDisplayName()
                ));
            } else if (drop.getDisplayName() != null && !drop.getDisplayName().isBlank()) {
                name = ColorUtil.colorize(drop.getDisplayName());
            } else {
                name = formatMaterial(drop.getMaterial());
            }

            if (drop.isGuaranteed()) {
                parts.add(drop.getAmount() + "x " + name);
            } else {
                parts.add((int) drop.getChance() + "% " + drop.getAmount() + "x " + name);
            }
        }

        return String.join(", ", parts);
    }

    public boolean isPluginCustomHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(customHeadKey, PersistentDataType.BYTE);
    }

    public String getStoredRarity(ItemMeta meta) {
        if (meta == null) {
            return DEFAULT_RARITY_ID;
        }

        return normalizeRarity(
                meta.getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING)
        );
    }

    public String getStoredAnimalName(ItemMeta meta) {
        if (meta == null) {
            return "";
        }

        String animalName = meta.getPersistentDataContainer().get(animalNameKey, PersistentDataType.STRING);
        return animalName != null ? animalName : "";
    }

    public String getStoredHeadOwner(ItemMeta meta) {
        if (meta == null) {
            return "";
        }

        String owner = meta.getPersistentDataContainer().get(headOwnerKey, PersistentDataType.STRING);
        return owner != null ? owner : "";
    }

    public String getStoredHeadTexture(ItemMeta meta) {
        if (meta == null) {
            return "";
        }

        String texture = meta.getPersistentDataContainer().get(headTextureKey, PersistentDataType.STRING);
        return texture != null ? texture : "";
    }

    public String getStoredRarityFromMeta(ItemMeta meta) {
        return getStoredRarity(meta);
    }

    public String getStoredAnimalNameFromMeta(ItemMeta meta) {
        return getStoredAnimalName(meta);
    }

    public String getStoredHeadOwnerFromMeta(ItemMeta meta) {
        return getStoredHeadOwner(meta);
    }

    public String getStoredHeadTextureFromMeta(ItemMeta meta) {
        return getStoredHeadTexture(meta);
    }

    public String getStoredRarity(Skull skull) {
        if (skull == null) {
            return DEFAULT_RARITY_ID;
        }

        return normalizeRarity(
                skull.getPersistentDataContainer().get(rarityKey, PersistentDataType.STRING)
        );
    }

    public String getStoredAnimalName(Skull skull) {
        if (skull == null) {
            return "";
        }

        String animalName = skull.getPersistentDataContainer().get(animalNameKey, PersistentDataType.STRING);
        return animalName != null ? animalName : "";
    }

    public String getStoredHeadOwner(Skull skull) {
        if (skull == null) {
            return "";
        }

        String owner = skull.getPersistentDataContainer().get(headOwnerKey, PersistentDataType.STRING);
        return owner != null ? owner : "";
    }

    public String getStoredHeadTexture(Skull skull) {
        if (skull == null) {
            return "";
        }

        String texture = skull.getPersistentDataContainer().get(headTextureKey, PersistentDataType.STRING);
        return texture != null ? texture : "";
    }

    public void applyPersistentHeadData(ItemMeta meta, String rarityId, String animalName, String headOwner, String headTexture) {
        if (meta == null) {
            return;
        }

        meta.getPersistentDataContainer().set(customHeadKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, normalizeRarity(rarityId));
        meta.getPersistentDataContainer().set(animalNameKey, PersistentDataType.STRING, animalName != null ? animalName : "");

        if (headOwner != null && !headOwner.isBlank()) {
            meta.getPersistentDataContainer().set(headOwnerKey, PersistentDataType.STRING, headOwner);
        } else {
            meta.getPersistentDataContainer().remove(headOwnerKey);
        }

        if (headTexture != null && !headTexture.isBlank()) {
            meta.getPersistentDataContainer().set(headTextureKey, PersistentDataType.STRING, headTexture);
        } else {
            meta.getPersistentDataContainer().remove(headTextureKey);
        }
    }

    public void applyPersistentHeadData(Skull skull, String rarityId, String animalName, String headOwner, String headTexture) {
        if (skull == null) {
            return;
        }

        PersistentDataContainer pdc = skull.getPersistentDataContainer();

        pdc.set(customHeadKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(rarityKey, PersistentDataType.STRING, normalizeRarity(rarityId));
        pdc.set(animalNameKey, PersistentDataType.STRING, animalName != null ? animalName : "");

        if (headOwner != null && !headOwner.isBlank()) {
            pdc.set(headOwnerKey, PersistentDataType.STRING, headOwner);
        } else {
            pdc.remove(headOwnerKey);
        }

        if (headTexture != null && !headTexture.isBlank()) {
            pdc.set(headTextureKey, PersistentDataType.STRING, headTexture);
        } else {
            pdc.remove(headTextureKey);
        }
    }

    public ItemStack createPersistentHeadItem(String rarityId, String animalName, String headOwner, String headTexture) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return item;
        }

        if (headOwner != null && !headOwner.isBlank()) {
            try {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(headOwner));
            } catch (Throwable throwable) {
                plugin.getLogger().warning("Failed to apply head owner '" + headOwner + "': " + throwable.getMessage());
            }
        }

        String normalizedRarity = normalizeRarity(rarityId);
        String displayName = buildHeadDisplayName(normalizedRarity, animalName);

        meta.setDisplayName(ColorUtil.colorize(displayName));
        meta.setLore(colorizeLore(buildHeadLore(normalizedRarity, animalName, displayName)));
        applyPersistentHeadData(meta, normalizedRarity, animalName, headOwner, headTexture);

        item.setItemMeta(meta);
        return item;
    }

    private boolean roll(double chance) {
        if (chance >= 100.0D) {
            return true;
        }
        if (chance <= 0.0D) {
            return false;
        }
        return ThreadLocalRandom.current().nextDouble(100.0D) < chance;
    }

    private ItemStack createDrop(HarvestDrop drop, Entity entity, AnimalProfile profile) {
        Material material = drop.getMaterial();

        if (material == Material.WHITE_WOOL && entity instanceof Sheep sheep) {
            material = getWoolFromColor(sheep.getColor());
        }

        if (material == Material.PLAYER_HEAD && plugin.getConfigManager().getMainSettings().isCustomHeadsEnabled()) {
            return createPersistentHeadItem(
                    normalizeRarity(drop.getRarity()),
                    profile.getDisplayName(),
                    drop.getHeadOwner(),
                    drop.getHeadTexture()
            );
        }

        ItemStack item = new ItemStack(material, Math.max(1, drop.getAmount()));

        if (drop.getDisplayName() != null && !drop.getDisplayName().isBlank()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtil.colorize(drop.getDisplayName()));
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    private Material getWoolFromColor(org.bukkit.DyeColor color) {
        return switch (color) {
            case BLACK -> Material.BLACK_WOOL;
            case BLUE -> Material.BLUE_WOOL;
            case BROWN -> Material.BROWN_WOOL;
            case CYAN -> Material.CYAN_WOOL;
            case GRAY -> Material.GRAY_WOOL;
            case GREEN -> Material.GREEN_WOOL;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_WOOL;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_WOOL;
            case LIME -> Material.LIME_WOOL;
            case MAGENTA -> Material.MAGENTA_WOOL;
            case ORANGE -> Material.ORANGE_WOOL;
            case PINK -> Material.PINK_WOOL;
            case PURPLE -> Material.PURPLE_WOOL;
            case RED -> Material.RED_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case WHITE -> Material.WHITE_WOOL;
        };
    }

    private void giveOrDrop(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
    }

    private String buildHeadDisplayName(String rarityId, String animalName) {
        MainSettings settings = plugin.getConfigManager().getMainSettings();
        String rarityDisplay = settings.getRarityDisplay(rarityId);

        return settings.getCustomHeadDefaultNameFormat()
                .replace("{rarity}", rarityDisplay)
                .replace("{rarity_id}", rarityId)
                .replace("{animal}", animalName != null ? animalName : "");
    }

    private List<String> buildHeadLore(String rarityId, String animalName, String displayName) {
        MainSettings settings = plugin.getConfigManager().getMainSettings();
        String rarityDisplay = settings.getRarityDisplay(rarityId);

        List<String> output = new ArrayList<>();
        for (String line : settings.getCustomHeadLore()) {
            output.add(
                    line.replace("{rarity}", rarityDisplay)
                            .replace("{rarity_id}", rarityId)
                            .replace("{animal}", animalName != null ? animalName : "")
                            .replace("{display_name}", displayName)
            );
        }
        return output;
    }

    private List<String> colorizeLore(List<String> input) {
        List<String> output = new ArrayList<>();
        for (String line : input) {
            output.add(ColorUtil.colorize(line));
        }
        return output;
    }

    private String normalizeRarity(String rarityId) {
        if (rarityId == null || rarityId.isBlank()) {
            return DEFAULT_RARITY_ID;
        }
        return rarityId.toUpperCase();
    }

    private String formatMaterial(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }

        return builder.toString().trim();
    }

    public enum HarvestResult {
        SUCCESS,
        NOT_READY,
        DISABLED,
        NO_DROPS
    }
}