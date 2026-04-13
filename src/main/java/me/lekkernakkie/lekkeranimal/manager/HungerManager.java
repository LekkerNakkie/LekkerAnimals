package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;

public class HungerManager {

    private final LekkerAnimal plugin;
    private final AnimalManager animalManager;
    private BukkitTask hungerTask;

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

        hungerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickHunger, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (hungerTask != null) {
            hungerTask.cancel();
            hungerTask = null;
        }
    }

    private void tickHunger() {
        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        for (AnimalData data : animalManager.getAllBondedAnimals()) {
            AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(data.getEntityType());
            if (profile == null) {
                continue;
            }

            int newHunger = Math.max(0, data.getHunger() - profile.getHungerDrain());
            data.setHunger(newHunger);

            int warningThreshold = (int) Math.ceil(profile.getMaxHunger() * (mainSettings.getHungerWarningThresholdPercent() / 100.0));
            if (newHunger > 0 && newHunger <= warningThreshold) {
                Player owner = Bukkit.getPlayer(data.getOwnerUuid());
                if (owner != null && owner.isOnline()) {
                    lang.send(owner, "hunger.warning", Map.of(
                            "animal", profile.getDisplayName(),
                            "hunger", String.valueOf(newHunger),
                            "max_hunger", String.valueOf(profile.getMaxHunger())
                    ));
                }
            }

            if (newHunger <= 0 && mainSettings.isHungerKillOnZero()) {
                killAnimal(data.getEntityUuid(), data.getOwnerUuid(), profile);
            }
        }
    }

    private void killAnimal(UUID entityUuid, UUID ownerUuid, AnimalProfile profile) {
        Entity entity = Bukkit.getEntity(entityUuid);
        if (entity != null && entity.isValid()) {
            entity.remove();
        }

        animalManager.unregisterAnimal(entityUuid);

        Player owner = Bukkit.getPlayer(ownerUuid);
        if (owner != null && owner.isOnline()) {
            plugin.getConfigManager().getLangSettings().send(owner, "hunger.starving-death", Map.of(
                    "animal", profile.getDisplayName()
            ));

            try {
                Sound sound = Sound.valueOf(plugin.getConfig().getString("sounds.death", "ENTITY_GENERIC_EXTINGUISH_FIRE"));
                owner.playSound(owner.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}