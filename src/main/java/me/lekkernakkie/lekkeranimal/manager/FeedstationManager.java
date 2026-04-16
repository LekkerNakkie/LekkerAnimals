package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalFeederData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FeedstationManager {

    private final LekkerAnimal plugin;
    private final Map<UUID, AnimalFeederData> feeders = new ConcurrentHashMap<>();
    private BukkitTask task;

    public FeedstationManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();

        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        this.task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::tick,
                settings.getTaskTickInterval(),
                settings.getTaskTickInterval()
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void registerFeeder(AnimalFeederData data) {
        if (data == null) {
            return;
        }
        feeders.put(data.getFeederUuid(), data);
    }

    public void unregisterFeeder(UUID feederUuid) {
        if (feederUuid == null) {
            return;
        }
        feeders.remove(feederUuid);
    }

    public Collection<AnimalFeederData> getAllFeeders() {
        return feeders.values();
    }

    public AnimalFeederData getFeeder(UUID feederUuid) {
        return feeders.get(feederUuid);
    }

    private void tick() {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();

        for (AnimalFeederData feeder : feeders.values()) {
            FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());
            if (tierSettings == null) {
                continue;
            }

            World world = plugin.getServer().getWorld(feeder.getWorldName());
            Location feederLocation = feeder.toLocation(world);
            if (world == null || feederLocation == null) {
                continue;
            }

            spawnIdleParticles(feederLocation, settings);

            List<Entity> nearbyEntities = world.getNearbyEntities(
                    feederLocation,
                    tierSettings.radius(),
                    tierSettings.radius(),
                    tierSettings.radius()
            ).stream().toList();

            List<AnimalCandidate> candidates = nearbyEntities.stream()
                    .filter(entity -> entity instanceof Animals)
                    .map(entity -> toCandidate(entity))
                    .filter(Objects::nonNull)
                    .filter(candidate -> candidate.data().isOwner(feeder.getOwnerUuid()))
                    .collect(Collectors.toList());

            if (settings.isPrioritizeLowestHunger()) {
                candidates.sort(Comparator.comparingInt(candidate -> candidate.data().getHunger()));
            }

            if (candidates.size() > tierSettings.maxAnimals()) {
                candidates = candidates.subList(0, tierSettings.maxAnimals());
            }

            attractAnimals(feederLocation, tierSettings, settings, candidates);

            long intervalMillis = tierSettings.feedIntervalSeconds() * 1000L;
            if (now - feeder.getLastFeedAt() < intervalMillis) {
                continue;
            }

            boolean fedSomething = feedNearbyAnimals(feeder, feederLocation, settings, tierSettings, candidates);
            if (fedSomething) {
                feeder.setLastFeedAt(now);
            }
        }
    }

    private AnimalCandidate toCandidate(Entity entity) {
        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            return null;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null || !profile.isEnabled()) {
            return null;
        }

        return new AnimalCandidate(entity, data, profile);
    }

    private void attractAnimals(Location feederLocation,
                                FeedstationSettings.TierSettings tierSettings,
                                FeedstationSettings settings,
                                List<AnimalCandidate> candidates) {
        for (AnimalCandidate candidate : candidates) {
            Entity entity = candidate.entity();
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            double distance = entity.getLocation().distance(feederLocation);
            if (distance > tierSettings.attractionRange() || distance < 1.75D) {
                continue;
            }

            Vector direction = feederLocation.toVector().subtract(entity.getLocation().toVector());
            if (direction.lengthSquared() <= 0.0001D) {
                continue;
            }

            direction.normalize().multiply(tierSettings.attractionSpeed());
            Vector currentVelocity = livingEntity.getVelocity();
            livingEntity.setVelocity(new Vector(
                    direction.getX(),
                    Math.max(currentVelocity.getY(), 0.05D),
                    direction.getZ()
            ));

            spawnParticle(feederLocation.clone().add(0.0D, 0.6D, 0.0D), settings.getAttractionParticles());
        }
    }

    private boolean feedNearbyAnimals(AnimalFeederData feeder,
                                      Location feederLocation,
                                      FeedstationSettings settings,
                                      FeedstationSettings.TierSettings tierSettings,
                                      List<AnimalCandidate> candidates) {
        boolean fedSomething = false;
        int fedCount = 0;

        for (AnimalCandidate candidate : candidates) {
            if (fedCount >= tierSettings.maxAnimals()) {
                break;
            }

            AnimalData data = candidate.data();
            AnimalProfile profile = candidate.profile();

            if (settings.isSkipFullAnimals() && data.getHunger() >= profile.getMaxHunger()) {
                continue;
            }

            if (settings.isOnlyWhenHungry() && data.getHunger() >= profile.getMaxHunger()) {
                continue;
            }

            StoredFoodMatch match = findMatchingFood(feeder, profile);
            if (match == null) {
                continue;
            }

            FeedingReward reward = profile.getFeedingReward(match.stack().getType());
            if (reward == null) {
                continue;
            }

            int newHunger = Math.min(profile.getMaxHunger(), data.getHunger() + reward.getHungerRestore());
            data.setHunger(newHunger);
            data.setMaxHunger(profile.getMaxHunger());

            int newBond = Math.min(plugin.getConfigManager().getMainSettings().getMaxBond(), data.getBond() + reward.getBondGain());
            data.setBond(newBond);

            int levelsGained = plugin.getLevelManager().addXp(data, profile, reward.getXp());
            plugin.getDataManager().saveAnimal(data);

            if (settings.isConsumeOneItemPerFeed()) {
                consumeOne(match.stack());
                cleanupFood(feeder);
            }

            plugin.getHologramManager().refresh(candidate.entity());

            spawnParticle(candidate.entity().getLocation().clone().add(0.0D, 0.8D, 0.0D), settings.getFeedingParticles());

            if (settings.isSoundsEnabled()) {
                candidate.entity().getWorld().playSound(candidate.entity().getLocation(), settings.getFeedSound(), 0.8F, 1.1F);
            }

            if (levelsGained > 0) {
                plugin.getHologramManager().refresh(candidate.entity());
            }

            fedSomething = true;
            fedCount++;
        }

        return fedSomething;
    }

    private StoredFoodMatch findMatchingFood(AnimalFeederData feeder, AnimalProfile profile) {
        for (ItemStack stack : feeder.getStoredFood()) {
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
                continue;
            }

            if (profile.getFeedingReward(stack.getType()) != null) {
                return new StoredFoodMatch(stack);
            }
        }
        return null;
    }

    private void cleanupFood(AnimalFeederData feeder) {
        feeder.getStoredFood().removeIf(stack -> stack == null || stack.getType().isAir() || stack.getAmount() <= 0);
        feeder.markDirty();
    }

    private void consumeOne(ItemStack stack) {
        stack.setAmount(Math.max(0, stack.getAmount() - 1));
    }

    private void spawnIdleParticles(Location location, FeedstationSettings settings) {
        spawnParticle(location.clone().add(0.5D, 0.9D, 0.5D), settings.getIdleParticles());
    }

    private void spawnParticle(Location location, FeedstationSettings.ParticleSection particleSection) {
        if (particleSection == null || !particleSection.enabled() || location.getWorld() == null) {
            return;
        }

        location.getWorld().spawnParticle(
                particleSection.particle(),
                location,
                particleSection.count(),
                particleSection.spreadX(),
                particleSection.spreadY(),
                particleSection.spreadZ(),
                particleSection.extra()
        );
    }

    private record AnimalCandidate(Entity entity, AnimalData data, AnimalProfile profile) {}
    private record StoredFoodMatch(ItemStack stack) {}
}