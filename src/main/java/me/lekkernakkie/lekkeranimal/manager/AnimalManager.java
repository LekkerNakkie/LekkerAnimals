package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.data.AnimalData;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnimalManager {

    private final Map<UUID, AnimalData> bondedAnimals = new ConcurrentHashMap<>();

    public boolean isSupported(Entity entity) {
        return entity instanceof Animals;
    }

    public boolean isBonded(Entity entity) {
        return bondedAnimals.containsKey(entity.getUniqueId());
    }

    public AnimalData getAnimalData(Entity entity) {
        return bondedAnimals.get(entity.getUniqueId());
    }

    public AnimalData getAnimalData(UUID entityUuid) {
        return bondedAnimals.get(entityUuid);
    }

    public void registerAnimal(AnimalData data) {
        bondedAnimals.put(data.getEntityUuid(), data);
    }

    public void unregisterAnimal(UUID entityUuid) {
        bondedAnimals.remove(entityUuid);
    }

    public Collection<AnimalData> getAllBondedAnimals() {
        return bondedAnimals.values();
    }

    public int getBondedCount() {
        return bondedAnimals.size();
    }
}