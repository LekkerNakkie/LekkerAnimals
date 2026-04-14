package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.HarvestDrop;
import me.lekkernakkie.lekkeranimal.data.HarvestLevelProfile;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HarvestManager {

    private final LekkerAnimal plugin;

    public HarvestManager(LekkerAnimal plugin) {
        this.plugin = plugin;
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

            ItemStack item = createDrop(drop);
            giveOrDrop(player, item);
        }

        data.setLastHarvestAt(System.currentTimeMillis());
        plugin.getDataManager().saveAnimal(data);

        return HarvestResult.SUCCESS;
    }

    public boolean isReady(AnimalData data, AnimalProfile profile) {
        if (!profile.isHarvestingEnabled()) {
            return false;
        }

        long cooldownMillis = profile.getHarvestCooldownSeconds() * 1000L;
        long now = System.currentTimeMillis();

        return data.getLastHarvestAt() <= 0L || now >= data.getLastHarvestAt() + cooldownMillis;
    }

    public long getTimeLeftMillis(AnimalData data, AnimalProfile profile) {
        if (!profile.isHarvestingEnabled()) {
            return 0L;
        }

        long cooldownMillis = profile.getHarvestCooldownSeconds() * 1000L;
        long readyAt = data.getLastHarvestAt() + cooldownMillis;
        return Math.max(0L, readyAt - System.currentTimeMillis());
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
            String name = drop.getDisplayName() != null && !drop.getDisplayName().isBlank()
                    ? ColorUtil.colorize(drop.getDisplayName())
                    : formatMaterial(drop.getMaterial());

            if (drop.isGuaranteed()) {
                parts.add(drop.getAmount() + "x " + name);
            } else {
                parts.add((int) drop.getChance() + "% " + drop.getAmount() + "x " + name);
            }
        }

        return String.join(", ", parts);
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

    private ItemStack createDrop(HarvestDrop drop) {
        ItemStack item = new ItemStack(drop.getMaterial(), Math.max(1, drop.getAmount()));

        if (drop.getDisplayName() != null && !drop.getDisplayName().isBlank()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtil.colorize(drop.getDisplayName()));
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    private void giveOrDrop(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover)
        );
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