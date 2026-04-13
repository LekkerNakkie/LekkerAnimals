package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public class HologramManager {

    private final LekkerAnimal plugin;
    private final AnimalManager animalManager;

    private BukkitTask task;

    public HologramManager(LekkerAnimal plugin, AnimalManager animalManager) {
        this.plugin = plugin;
        this.animalManager = animalManager;
    }

    public void start() {
        stop();

        FileConfiguration cfg = plugin.getConfigManager().getAnimalsConfig();
        if (!cfg.getBoolean("hologram.enabled", true)) {
            return;
        }

        long interval = Math.max(1L, cfg.getLong("hologram.update-interval-ticks", 10L));

        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        clearAllNames();
    }

    public void remove(Entity entity) {
        if (entity == null || !entity.isValid()) {
            return;
        }

        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }

    public void refresh(Entity entity) {
        if (entity == null || !entity.isValid() || entity.isDead()) {
            return;
        }

        AnimalData data = animalManager.getAnimalData(entity);
        if (data == null) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }

        FileConfiguration cfg = plugin.getConfigManager().getAnimalsConfig();

        if (!cfg.getBoolean("hologram.enabled", true)) {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
            return;
        }

        if (isPlayerNearby(entity, cfg.getDouble("hologram.show-distance", 12.0))) {
            entity.setCustomName(buildName(data, profile, cfg));
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomNameVisible(false);
        }
    }

    private void tick() {
        FileConfiguration cfg = plugin.getConfigManager().getAnimalsConfig();
        if (!cfg.getBoolean("hologram.enabled", true)) {
            clearAllNames();
            return;
        }

        Collection<AnimalData> all = animalManager.getAllBondedAnimals();
        for (AnimalData data : all) {
            Entity entity = plugin.getServer().getEntity(data.getEntityUuid());

            if (entity == null || !entity.isValid() || entity.isDead()) {
                continue;
            }

            AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
            if (profile == null) {
                entity.setCustomName(null);
                entity.setCustomNameVisible(false);
                continue;
            }

            if (isPlayerNearby(entity, cfg.getDouble("hologram.show-distance", 12.0))) {
                entity.setCustomName(buildName(data, profile, cfg));
                entity.setCustomNameVisible(true);
            } else {
                entity.setCustomNameVisible(false);
            }
        }
    }

    private boolean isPlayerNearby(Entity entity, double radius) {
        double radiusSquared = radius * radius;

        for (Player player : entity.getWorld().getPlayers()) {
            if (player.isDead()) {
                continue;
            }

            if (player.getLocation().distanceSquared(entity.getLocation()) <= radiusSquared) {
                return true;
            }
        }

        return false;
    }

    private String buildName(AnimalData data, AnimalProfile profile, FileConfiguration cfg) {
        String format = cfg.getString(
                "hologram.format",
                "&b{animal} &8• &7Lvl &b{level} &8• &c❤ &b{hunger}&8/&7{max_hunger}"
        );

        return ColorUtil.colorize(
                format.replace("{animal}", profile.getDisplayName())
                        .replace("{level}", String.valueOf(data.getLevel()))
                        .replace("{hunger}", String.valueOf(data.getHunger()))
                        .replace("{max_hunger}", String.valueOf(profile.getMaxHunger()))
        );
    }

    private void clearAllNames() {
        for (AnimalData data : animalManager.getAllBondedAnimals()) {
            Entity entity = plugin.getServer().getEntity(data.getEntityUuid());
            if (entity != null && entity.isValid()) {
                entity.setCustomName(null);
                entity.setCustomNameVisible(false);
            }
        }
    }
}