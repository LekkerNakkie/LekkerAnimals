package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalFeederData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import me.lekkernakkie.lekkeranimal.gui.FeedstationMenuHolder;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FeedstationManager {

    private final LekkerAnimal plugin;
    private final Map<UUID, AnimalFeederData> feeders = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastFeedTimes = new ConcurrentHashMap<>();
    private final Map<UUID, List<ArmorStand>> holograms = new ConcurrentHashMap<>();

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

        long interval = Math.max(20L, settings.getTaskTickInterval());
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                if (stand != null && stand.isValid()) {
                    stand.remove();
                }
            }
        }

        holograms.clear();
        lastFeedTimes.clear();
    }

    public void registerFeeder(AnimalFeederData data) {
        if (data == null) {
            return;
        }

        feeders.put(data.getFeederUuid(), data);
        refreshHologram(data);
    }

    public void unregisterFeeder(UUID feederUuid) {
        if (feederUuid == null) {
            return;
        }

        feeders.remove(feederUuid);
        lastFeedTimes.remove(feederUuid);

        List<ArmorStand> stands = holograms.remove(feederUuid);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                if (stand != null && stand.isValid()) {
                    stand.remove();
                }
            }
        }
    }

    public AnimalFeederData getFeeder(UUID feederUuid) {
        return feeders.get(feederUuid);
    }

    public Collection<AnimalFeederData> getAllFeeders() {
        return feeders.values();
    }

    public AnimalFeederData getFeederAt(Block block) {
        if (block == null || block.getWorld() == null) {
            return null;
        }

        Location location = block.getLocation();

        for (AnimalFeederData feeder : feeders.values()) {
            if (!block.getWorld().getName().equalsIgnoreCase(feeder.getWorldName())) {
                continue;
            }

            Location feederLocation = feeder.getLocation(block.getWorld());
            if (feederLocation.getBlockX() == location.getBlockX()
                    && feederLocation.getBlockY() == location.getBlockY()
                    && feederLocation.getBlockZ() == location.getBlockZ()) {
                return feeder;
            }
        }

        return null;
    }

    public void openMainMenu(Player player, AnimalFeederData feeder) {
        if (player == null || feeder == null) {
            return;
        }

        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());
        String tierDisplay = tierSettings != null ? tierSettings.display() : feeder.getTier().name();

        String title = settings.getGuiTitle()
                .replace("{tier_display}", tierDisplay)
                .replace("{owner}", feeder.getOwnerName() != null ? feeder.getOwnerName() : "Unknown");

        Inventory inventory = Bukkit.createInventory(
                new FeedstationMenuHolder(feeder.getFeederUuid(), FeedstationMenuHolder.Screen.MAIN),
                settings.getGuiRows() * 9,
                ColorUtil.colorize(title)
        );

        if (settings.isFillerEnabled()) {
            ItemStack filler = createItem(
                    settings.getFillerMaterial(),
                    settings.getFillerName(),
                    List.of()
            );

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        if (tierSettings != null) {
            inventory.setItem(settings.getInfoSlot(), createInfoItem(feeder, tierSettings));
            inventory.setItem(settings.getOpenStorageSlot(), createStorageButton(feeder, tierSettings));
            inventory.setItem(settings.getShowRadiusSlot(), createRadiusButton(tierSettings));
            inventory.setItem(settings.getToggleHologramSlot(), createHologramButton(feeder));
        }

        if (settings.isUpgradesEnabled()) {
            inventory.setItem(settings.getUpgradeSlot(), createUpgradeItem(feeder));
        }

        player.openInventory(inventory);

        if (settings.isSoundsEnabled()) {
            player.playSound(player.getLocation(), settings.getOpenSound(), 1.0F, 1.0F);
        }
    }

    public void openStorageMenu(Player player, AnimalFeederData feeder) {
        if (player == null || feeder == null) {
            return;
        }

        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());
        int allowedSlots = tierSettings != null ? tierSettings.storageSlots() : 9;
        int rows = Math.max(1, Math.min(6, (int) Math.ceil(allowedSlots / 9.0D)));

        String title = settings.getStorageGuiTitle()
                .replace("{tier_display}", tierSettings != null ? tierSettings.display() : feeder.getTier().name())
                .replace("{owner}", feeder.getOwnerName() != null ? feeder.getOwnerName() : "Unknown");

        Inventory inventory = Bukkit.createInventory(
                new FeedstationMenuHolder(feeder.getFeederUuid(), FeedstationMenuHolder.Screen.STORAGE),
                rows * 9,
                ColorUtil.colorize(title)
        );

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < allowedSlots) {
                if (i < feeder.getStoredFood().size()) {
                    ItemStack stored = feeder.getStoredFood().get(i);
                    if (stored != null && !stored.getType().isAir()) {
                        inventory.setItem(i, stored.clone());
                    }
                }
            } else {
                inventory.setItem(i, createBlockedSlotItem());
            }
        }

        player.openInventory(inventory);

        if (settings.isSoundsEnabled()) {
            player.playSound(player.getLocation(), settings.getOpenSound(), 1.0F, 1.0F);
        }
    }

    public void saveStorageMenu(Inventory inventory, AnimalFeederData feeder) {
        if (inventory == null || feeder == null) {
            return;
        }

        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());
        int allowedSlots = tierSettings != null ? tierSettings.storageSlots() : inventory.getSize();

        feeder.getStoredFood().clear();

        for (int slot = 0; slot < Math.min(allowedSlots, inventory.getSize()); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            feeder.getStoredFood().add(item.clone());
        }

        refreshHologram(feeder);
    }

    public boolean isStorageScreen(FeedstationMenuHolder holder) {
        return holder != null && holder.getScreen() == FeedstationMenuHolder.Screen.STORAGE;
    }

    public boolean isStorageSlot(AnimalFeederData feeder, int slot) {
        if (feeder == null || slot < 0) {
            return false;
        }

        FeedstationSettings.TierSettings tierSettings = plugin.getConfigManager().getFeedstationSettings().getTierSettings(feeder.getTier());
        int allowedSlots = tierSettings != null ? tierSettings.storageSlots() : 0;
        return slot < allowedSlots;
    }

    public boolean isPotentialAnimalFood(Material material) {
        if (material == null || material.isAir()) {
            return false;
        }

        for (AnimalProfile profile : plugin.getConfigManager().getAnimalsSettings().getProfiles().values()) {
            if (profile == null || !profile.isEnabled()) {
                continue;
            }

            if (profile.getFeedingReward(material) != null) {
                return true;
            }
        }

        return false;
    }

    public void showRadiusPreview(Player player, AnimalFeederData feeder) {
        if (player == null || feeder == null) {
            return;
        }

        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());
        if (tierSettings == null) {
            return;
        }

        World world = plugin.getServer().getWorld(feeder.getWorldName());
        if (world == null) {
            return;
        }

        Location center = feeder.getLocation(world).clone().add(0.5D, settings.getRadiusPreviewYOffset(), 0.5D);
        double radius = tierSettings.radius();
        int points = settings.getRadiusPreviewPoints();
        Particle particle = settings.getRadiusPreviewParticle();
        int count = settings.getRadiusPreviewCountPerPoint();

        BukkitTask previewTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2D * i) / points;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                Location point = center.clone().add(x, 0.0D, z);

                player.spawnParticle(
                        particle,
                        point,
                        count,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }, 0L, 5L);

        plugin.getServer().getScheduler().runTaskLater(plugin, previewTask::cancel, settings.getRadiusPreviewDurationTicks());
    }

    public boolean tryUpgrade(Player player, AnimalFeederData feeder) {
        if (player == null || feeder == null) {
            return false;
        }

        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        if (!settings.isUpgradesEnabled()) {
            plugin.getConfigManager().getLangSettings().send(player, "feedstation.upgrades-disabled");
            return false;
        }

        FeederTier nextTier = settings.getNextTier(feeder.getTier());
        if (nextTier == null) {
            plugin.getConfigManager().getLangSettings().send(player, "feedstation.already-max-tier");
            return false;
        }

        FeedstationSettings.UpgradeCost cost = settings.getUpgradeCost(nextTier);
        if (cost == null) {
            plugin.getConfigManager().getLangSettings().send(player, "feedstation.upgrades-disabled");
            return false;
        }

        if (!hasEnough(player.getInventory(), cost.material(), cost.amount())) {
            plugin.getConfigManager().getLangSettings().send(player, "feedstation.not-enough-upgrade-items");
            return false;
        }

        removeItems(player.getInventory(), cost.material(), cost.amount());

        feeder.setTier(nextTier);
        refreshHologram(feeder);

        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(nextTier);
        plugin.getConfigManager().getLangSettings().send(player, "feedstation.upgrade-success", Map.of(
                "tier_display", tierSettings != null ? tierSettings.display() : nextTier.name()
        ));

        if (settings.isSoundsEnabled()) {
            player.playSound(player.getLocation(), settings.getUpgradeSound(), 1.0F, 1.0F);
        }

        return true;
    }

    public void dropStoredFood(World world, Location location, AnimalFeederData feeder) {
        if (world == null || location == null || feeder == null) {
            return;
        }

        for (ItemStack stack : feeder.getStoredFood()) {
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
                continue;
            }

            world.dropItemNaturally(location.clone().add(0.5D, 0.5D, 0.5D), stack.clone());
        }

        feeder.getStoredFood().clear();
    }

    private boolean hasEnough(PlayerInventory inventory, Material material, int amount) {
        int found = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() != material) {
                continue;
            }

            found += item.getAmount();
            if (found >= amount) {
                return true;
            }
        }

        return false;
    }

    private void removeItems(PlayerInventory inventory, Material material, int amount) {
        int remaining = amount;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() != material) {
                continue;
            }

            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                inventory.setItem(slot, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                inventory.setItem(slot, item);
                return;
            }

            if (remaining <= 0) {
                return;
            }
        }
    }

    private void tick() {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        for (AnimalFeederData feeder : new ArrayList<>(feeders.values())) {
            refreshHologram(feeder);
            spawnIdleParticles(feeder);
            processFeeder(feeder);
        }
    }

    private void processFeeder(AnimalFeederData feeder) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());

        if (tierSettings == null) {
            return;
        }

        World world = plugin.getServer().getWorld(feeder.getWorldName());
        if (world == null) {
            return;
        }

        Location feederLocation = feeder.getLocation(world);
        Location eatingLocation = feederLocation.clone().add(0.5D, 0.0D, 0.5D);
        long now = System.currentTimeMillis();
        long intervalMillis = tierSettings.feedIntervalSeconds() * 1000L;
        long lastFeed = lastFeedTimes.getOrDefault(feeder.getFeederUuid(), 0L);

        List<FeederAnimalTarget> targets = collectTargets(feeder, world, feederLocation, tierSettings.radius());

        if (targets.isEmpty()) {
            return;
        }

        applyAttraction(eatingLocation, tierSettings, targets);

        if (now - lastFeed < intervalMillis) {
            return;
        }

        int fedCount = 0;
        int maxAnimals = tierSettings.maxAnimals();

        for (FeederAnimalTarget target : targets) {
            if (fedCount >= maxAnimals) {
                break;
            }

            AnimalData data = target.data();
            AnimalProfile profile = target.profile();
            LivingEntity entity = target.entity();

            if (settings.isSkipFullAnimals() && data.getHunger() >= profile.getMaxHunger()) {
                continue;
            }

            if (settings.isOnlyWhenHungry() && data.getHunger() >= profile.getMaxHunger()) {
                continue;
            }

            double distanceToFeeder = entity.getLocation().distance(eatingLocation);
            if (distanceToFeeder > tierSettings.eatDistance()) {
                continue;
            }

            ItemStack matchingFood = findMatchingFood(feeder, profile);
            if (matchingFood == null) {
                continue;
            }

            FeedingReward reward = profile.getFeedingReward(matchingFood.getType());
            if (reward == null) {
                continue;
            }

            int newHunger = Math.min(profile.getMaxHunger(), data.getHunger() + reward.getHungerRestore());
            data.setHunger(newHunger);
            data.setMaxHunger(profile.getMaxHunger());

            int newBond = Math.min(
                    plugin.getConfigManager().getMainSettings().getMaxBond(),
                    data.getBond() + reward.getBondGain()
            );
            data.setBond(newBond);

            plugin.getLevelManager().addXp(data, profile, reward.getXp());
            plugin.getDataManager().saveAnimal(data);

            if (settings.isConsumeOneItemPerFeed()) {
                matchingFood.setAmount(matchingFood.getAmount() - 1);
                feeder.getStoredFood().removeIf(stack -> stack == null || stack.getType().isAir() || stack.getAmount() <= 0);
            }

            plugin.getHologramManager().refresh(entity);
            spawnFeedingParticles(entity.getLocation().add(0.0D, 0.8D, 0.0D));

            if (settings.isSoundsEnabled()) {
                entity.getWorld().playSound(
                        entity.getLocation(),
                        settings.getFeedSound(),
                        0.9F,
                        1.1F
                );
            }

            fedCount++;
        }

        if (fedCount > 0) {
            lastFeedTimes.put(feeder.getFeederUuid(), now);
            refreshHologram(feeder);
        }
    }

    private List<FeederAnimalTarget> collectTargets(AnimalFeederData feeder,
                                                    World world,
                                                    Location feederLocation,
                                                    double radius) {
        List<FeederAnimalTarget> targets = new ArrayList<>();
        double radiusSquared = radius * radius;

        for (AnimalData data : plugin.getAnimalManager().getAllBondedAnimals()) {
            if (!data.getOwnerUuid().equals(feeder.getOwnerUuid())) {
                continue;
            }

            Entity entity = plugin.getServer().getEntity(data.getEntityUuid());
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (!livingEntity.isValid() || livingEntity.isDead()) {
                continue;
            }

            if (!livingEntity.getWorld().getName().equalsIgnoreCase(world.getName())) {
                continue;
            }

            if (livingEntity.getLocation().distanceSquared(feederLocation) > radiusSquared) {
                continue;
            }

            AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
            if (profile == null || !profile.isEnabled()) {
                continue;
            }

            targets.add(new FeederAnimalTarget(livingEntity, data, profile));
        }

        if (plugin.getConfigManager().getFeedstationSettings().isPrioritizeLowestHunger()) {
            targets.sort(Comparator.comparingInt(target -> target.data().getHunger()));
        }

        return targets;
    }

    private void applyAttraction(Location feederLocation,
                                 FeedstationSettings.TierSettings tierSettings,
                                 List<FeederAnimalTarget> targets) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

        for (FeederAnimalTarget target : targets) {
            LivingEntity entity = target.entity();

            if (!(entity instanceof Mob mob)) {
                continue;
            }

            double distance = entity.getLocation().distance(feederLocation);
            if (distance <= Math.max(1.2D, tierSettings.eatDistance())) {
                continue;
            }

            if (distance > tierSettings.attractionRange()) {
                continue;
            }

            Location walkTarget = feederLocation.clone();

            boolean moved = mob.getPathfinder().moveTo(walkTarget, tierSettings.attractionSpeed());

            if (moved) {
                FeedstationSettings.ParticleSection particleSection = settings.getAttractionParticles();
                if (particleSection.enabled()) {
                    entity.getWorld().spawnParticle(
                            particleSection.particle(),
                            entity.getLocation().add(0.0D, 0.5D, 0.0D),
                            particleSection.count(),
                            particleSection.spreadX(),
                            particleSection.spreadY(),
                            particleSection.spreadZ(),
                            particleSection.extra()
                    );
                }
            }
        }
    }

    private ItemStack findMatchingFood(AnimalFeederData feeder, AnimalProfile profile) {
        for (ItemStack stack : feeder.getStoredFood()) {
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
                continue;
            }

            if (profile.getFeedingReward(stack.getType()) != null) {
                return stack;
            }
        }

        return null;
    }

    private void spawnIdleParticles(AnimalFeederData feeder) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.ParticleSection particleSection = settings.getIdleParticles();

        if (!particleSection.enabled()) {
            return;
        }

        World world = plugin.getServer().getWorld(feeder.getWorldName());
        if (world == null) {
            return;
        }

        Location location = feeder.getLocation(world).add(0.5D, 1.0D, 0.5D);
        world.spawnParticle(
                particleSection.particle(),
                location,
                particleSection.count(),
                particleSection.spreadX(),
                particleSection.spreadY(),
                particleSection.spreadZ(),
                particleSection.extra()
        );
    }

    private void spawnFeedingParticles(Location location) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeedstationSettings.ParticleSection particleSection = settings.getFeedingParticles();

        if (!particleSection.enabled() || location.getWorld() == null) {
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

    private void refreshHologram(AnimalFeederData feeder) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

        if (!settings.isHologramEnabled() || !feeder.isHologramEnabled()) {
            unregisterHologram(feeder.getFeederUuid());
            return;
        }

        World world = plugin.getServer().getWorld(feeder.getWorldName());
        if (world == null) {
            return;
        }

        FeedstationSettings.TierSettings tierSettings = settings.getTierSettings(feeder.getTier());
        if (tierSettings == null) {
            return;
        }

        Location base = feeder.getLocation(world).clone().add(0.5D, settings.getHologramOffsetY(), 0.5D);
        String tierDisplay = tierSettings.display();
        int nearbyAnimals = collectTargets(feeder, world, feeder.getLocation(world), tierSettings.radius()).size();
        int maxAnimals = tierSettings.maxAnimals();

        List<String> renderedLines = new ArrayList<>();
        for (String line : settings.getHologramLines()) {
            renderedLines.add(ColorUtil.colorize(
                    line.replace("{owner}", feeder.getOwnerName() != null ? feeder.getOwnerName() : "Unknown")
                            .replace("{tier_display}", tierDisplay)
                            .replace("{nearby_animals}", String.valueOf(nearbyAnimals))
                            .replace("{max_animals}", String.valueOf(maxAnimals))
                            .replace("{food_amount}", String.valueOf(feeder.getStoredFoodAmount()))
            ));
        }

        List<ArmorStand> stands = holograms.computeIfAbsent(feeder.getFeederUuid(), key -> new ArrayList<>());

        while (stands.size() > renderedLines.size()) {
            ArmorStand removed = stands.remove(stands.size() - 1);
            if (removed != null && removed.isValid()) {
                removed.remove();
            }
        }

        double lineOffset = 0.25D;

        for (int i = 0; i < renderedLines.size(); i++) {
            Location lineLocation = base.clone().add(0.0D, (renderedLines.size() - 1 - i) * lineOffset, 0.0D);
            ArmorStand stand;

            if (i < stands.size()) {
                stand = stands.get(i);
                if (stand == null || !stand.isValid()) {
                    stand = createHologramStand(lineLocation, renderedLines.get(i));
                    stands.set(i, stand);
                } else {
                    stand.teleport(lineLocation);
                    stand.setCustomName(renderedLines.get(i));
                }
            } else {
                stand = createHologramStand(lineLocation, renderedLines.get(i));
                stands.add(stand);
            }
        }
    }

    private ArmorStand createHologramStand(Location location, String line) {
        return location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCanPickupItems(false);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(line);
            stand.setSilent(true);
            stand.setInvulnerable(true);
        });
    }

    private void unregisterHologram(UUID feederUuid) {
        List<ArmorStand> stands = holograms.remove(feederUuid);
        if (stands == null) {
            return;
        }

        for (ArmorStand stand : stands) {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
    }

    private ItemStack createInfoItem(AnimalFeederData feeder, FeedstationSettings.TierSettings tierSettings) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        World world = plugin.getServer().getWorld(feeder.getWorldName());
        int nearbyAnimals = world != null
                ? collectTargets(feeder, world, feeder.getLocation(world), tierSettings.radius()).size()
                : 0;

        String hologramStatus = feeder.isHologramEnabled() ? "&aAAN" : "&cUIT";

        List<String> lore = new ArrayList<>();
        for (String line : settings.getInfoLore()) {
            lore.add(ColorUtil.colorize(
                    line.replace("{owner}", feeder.getOwnerName() != null ? feeder.getOwnerName() : "Unknown")
                            .replace("{tier_display}", tierSettings.display())
                            .replace("{radius}", trimDouble(tierSettings.radius()))
                            .replace("{eat_distance}", trimDouble(tierSettings.eatDistance()))
                            .replace("{max_animals}", String.valueOf(tierSettings.maxAnimals()))
                            .replace("{nearby_animals}", String.valueOf(nearbyAnimals))
                            .replace("{food_amount}", String.valueOf(feeder.getStoredFoodAmount()))
                            .replace("{storage_slots}", String.valueOf(tierSettings.storageSlots()))
                            .replace("{hologram_status}", hologramStatus)
            ));
        }

        return createItem(
                settings.getInfoMaterial(),
                settings.getInfoName().replace("{tier_display}", tierSettings.display()),
                lore
        );
    }

    private ItemStack createStorageButton(AnimalFeederData feeder, FeedstationSettings.TierSettings tierSettings) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

        List<String> lore = new ArrayList<>();
        for (String line : settings.getStorageButtonLore()) {
            lore.add(ColorUtil.colorize(
                    line.replace("{food_amount}", String.valueOf(feeder.getStoredFoodAmount()))
                            .replace("{storage_slots}", String.valueOf(tierSettings.storageSlots()))
            ));
        }

        return createItem(
                settings.getStorageButtonMaterial(),
                settings.getStorageButtonName(),
                lore
        );
    }

    private ItemStack createRadiusButton(FeedstationSettings.TierSettings tierSettings) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

        List<String> lore = new ArrayList<>();
        for (String line : settings.getRadiusButtonLore()) {
            lore.add(ColorUtil.colorize(
                    line.replace("{radius}", trimDouble(tierSettings.radius()))
            ));
        }

        return createItem(
                settings.getRadiusButtonMaterial(),
                settings.getRadiusButtonName(),
                lore
        );
    }

    private ItemStack createHologramButton(AnimalFeederData feeder) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        boolean enabled = feeder.isHologramEnabled();

        return createItem(
                enabled ? settings.getHologramEnabledMaterial() : settings.getHologramDisabledMaterial(),
                settings.getHologramButtonName(),
                enabled ? settings.getHologramEnabledLore() : settings.getHologramDisabledLore()
        );
    }

    private ItemStack createUpgradeItem(AnimalFeederData feeder) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        FeederTier nextTier = settings.getNextTier(feeder.getTier());

        if (nextTier == null) {
            return createItem(
                    Material.BARRIER,
                    settings.getUpgradeDisabledName(),
                    settings.getUpgradeDisabledLore()
            );
        }

        FeedstationSettings.UpgradeCost cost = settings.getUpgradeCost(nextTier);
        FeedstationSettings.TierSettings nextTierSettings = settings.getTierSettings(nextTier);

        String nextTierDisplay = nextTierSettings != null ? nextTierSettings.display() : nextTier.name();
        String materialName = cost != null ? formatMaterial(cost.material()) : "-";
        String amount = cost != null ? String.valueOf(cost.amount()) : "-";

        List<String> lore = new ArrayList<>();
        for (String line : settings.getUpgradeEnabledLore()) {
            lore.add(ColorUtil.colorize(
                    line.replace("{next_tier_display}", nextTierDisplay)
                            .replace("{upgrade_item}", materialName)
                            .replace("{upgrade_amount}", amount)
            ));
        }

        return createItem(
                Material.ANVIL,
                settings.getUpgradeEnabledName().replace("{next_tier_display}", nextTierDisplay),
                lore
        );
    }

    private ItemStack createBlockedSlotItem() {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();
        return createItem(
                settings.getBlockedSlotMaterial(),
                settings.getBlockedSlotName(),
                settings.getBlockedSlotLore()
        );
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
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
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private String trimDouble(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    private String formatMaterial(Material material) {
        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
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

    private record FeederAnimalTarget(LivingEntity entity, AnimalData data, AnimalProfile profile) {
    }
}