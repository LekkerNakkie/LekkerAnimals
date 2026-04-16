package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.data.AnimalFeederData;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FeedstationManager {

    private final LekkerAnimal plugin;
    private final Map<UUID, AnimalFeederData> feeders = new ConcurrentHashMap<>();

    public FeedstationManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // feeding task/hologram task komt later
    }

    public void stop() {
        // tijdelijke basis
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
}