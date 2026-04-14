package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Map;

public class HungerManager {

    private final LekkerAnimal plugin;
    private final AnimalManager animalManager;

    private BukkitTask task;

    public HungerManager(LekkerAnimal plugin, AnimalManager animalManager) {
        this.plugin = plugin;
        this.animalManager = animalManager;
    }

    public void start() {
        stop();

        MainSettings settings = plugin.getConfigManager().getMainSettings();
        if (!settings.isHungerEnabled()) {
            return;
        }

        long intervalTicks = Math.max(20L, settings.getHungerTaskIntervalSeconds() * 20L);

        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        Collection<AnimalData> animals = animalManager.getAllBondedAnimals();

        for (AnimalData data : animals) {
            Entity entity = plugin.getServer().getEntity(data.getEntityUuid());

            if (entity == null || !entity.isValid() || entity.isDead()) {
                animalManager.unregisterAnimal(data.getEntityUuid());
                plugin.getDataManager().deleteAnimal(data.getEntityUuid());
                continue;
            }

            AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
            if (profile == null) {
                continue;
            }

            int newHunger = Math.max(0, data.getHunger() - profile.getHungerDrain());
            data.setHunger(newHunger);
            data.setMaxHunger(profile.getMaxHunger());

            plugin.getDataManager().saveAnimal(data);
            plugin.getHologramManager().refresh(entity);

            int warningThreshold = (int) Math.ceil(profile.getMaxHunger() * (mainSettings.getHungerWarningThresholdPercent() / 100.0));

            if (data.getHunger() <= warningThreshold && data.getHunger() > 0) {
                Player owner = plugin.getServer().getPlayer(data.getOwnerUuid());
                if (owner != null && owner.isOnline()) {
                    lang.send(owner, "hunger.warning", Map.of(
                            "animal", profile.getDisplayName(),
                            "hunger", String.valueOf(data.getHunger()),
                            "max_hunger", String.valueOf(profile.getMaxHunger())
                    ));
                }
            }

            if (data.getHunger() <= 0 && mainSettings.isHungerKillOnZero() && entity instanceof LivingEntity livingEntity) {
                Player owner = plugin.getServer().getPlayer(data.getOwnerUuid());
                if (owner != null && owner.isOnline()) {
                    lang.send(owner, "hunger.starving-death", Map.of(
                            "animal", profile.getDisplayName()
                    ));
                }

                livingEntity.setHealth(0.0);
            }
        }
    }
}