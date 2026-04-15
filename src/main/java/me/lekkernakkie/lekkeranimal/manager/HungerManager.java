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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HungerManager {

    private static final long WARNING_COOLDOWN_MILLIS = 5L * 60L * 1000L;

    private final LekkerAnimal plugin;
    private final AnimalManager animalManager;
    private final Map<UUID, Long> lastWarningTimes = new ConcurrentHashMap<>();

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

        lastWarningTimes.clear();
    }

    private void tick() {
        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();
        LangSettings lang = plugin.getConfigManager().getLangSettings();
        long intervalMillis = Math.max(1000L, mainSettings.getHungerTaskIntervalSeconds() * 1000L);

        Collection<AnimalData> animals = animalManager.getAllBondedAnimals();

        for (AnimalData data : animals) {
            Entity entity = plugin.getServer().getEntity(data.getEntityUuid());

            if (entity == null) {
                continue;
            }

            if (!entity.isValid() || entity.isDead()) {
                animalManager.unregisterAnimal(data.getEntityUuid());
                lastWarningTimes.remove(data.getEntityUuid());
                plugin.getDataManager().deleteAnimal(data.getEntityUuid());
                continue;
            }

            AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
            if (profile == null || !profile.isEnabled()) {
                continue;
            }

            Player owner = plugin.getServer().getPlayer(data.getOwnerUuid());
            boolean ownerOnline = owner != null && owner.isOnline();

            boolean coOwnerOnline = false;
            if (!ownerOnline && mainSettings.isCoOwnersEnabled() && data.isCoOwnersKeepActive()) {
                for (UUID coOwnerUuid : data.getCoOwnerUuids()) {
                    Player coOwner = plugin.getServer().getPlayer(coOwnerUuid);
                    if (coOwner != null && coOwner.isOnline()) {
                        coOwnerOnline = true;
                        break;
                    }
                }
            }

            boolean animalActive = ownerOnline || coOwnerOnline;

            if (mainSettings.isOnlineOwnerActivityEnabled() && !animalActive) {
                plugin.getHologramManager().refresh(entity);
                continue;
            }

            boolean changed = false;

            int newHunger = Math.max(0, data.getHunger() - profile.getHungerDrain());
            if (newHunger != data.getHunger()) {
                data.setHunger(newHunger);
                data.setMaxHunger(profile.getMaxHunger());
                changed = true;
            }

            if (profile.isHarvestingEnabled()) {
                long cooldownMillis = profile.getHarvestCooldownSeconds() * 1000L;
                if (data.getHarvestProgressMillis() < cooldownMillis) {
                    long toAdd = Math.min(intervalMillis, cooldownMillis - data.getHarvestProgressMillis());
                    data.addHarvestProgressMillis(toAdd);
                    changed = true;
                }
            }

            if (changed) {
                plugin.getDataManager().saveAnimal(data);
            }

            plugin.getHologramManager().refresh(entity);

            int warningThreshold = (int) Math.ceil(profile.getMaxHunger() * (mainSettings.getHungerWarningThresholdPercent() / 100.0));

            if (data.getHunger() <= warningThreshold && data.getHunger() > 0 && ownerOnline) {
                long now = System.currentTimeMillis();
                long lastWarning = lastWarningTimes.getOrDefault(data.getEntityUuid(), 0L);

                if (now - lastWarning >= WARNING_COOLDOWN_MILLIS) {
                    lang.send(owner, "hunger.warning", Map.of(
                            "animal", profile.getDisplayName(),
                            "hunger", String.valueOf(data.getHunger()),
                            "max_hunger", String.valueOf(profile.getMaxHunger())
                    ));
                    lastWarningTimes.put(data.getEntityUuid(), now);
                }
            } else if (data.getHunger() > warningThreshold) {
                lastWarningTimes.remove(data.getEntityUuid());
            }

            if (data.getHunger() <= 0 && mainSettings.isHungerKillOnZero() && entity instanceof LivingEntity livingEntity) {
                if (ownerOnline) {
                    lang.send(owner, "hunger.starving-death", Map.of(
                            "animal", profile.getDisplayName()
                    ));
                }

                lastWarningTimes.remove(data.getEntityUuid());
                livingEntity.setHealth(0.0);
            }
        }
    }
}