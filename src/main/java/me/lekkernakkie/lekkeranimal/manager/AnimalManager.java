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
    private final Map<UUID, AnimalData> pendingAnimals = new ConcurrentHashMap<>();

    public boolean isSupported(Entity entity) {
        return entity instanceof Animals;
    }

    public boolean isBonded(Entity entity) {
        return entity != null && bondedAnimals.containsKey(entity.getUniqueId());
    }

    public boolean isKnownAnimal(Entity entity) {
        if (entity == null) {
            return false;
        }

        UUID uuid = entity.getUniqueId();
        return bondedAnimals.containsKey(uuid) || pendingAnimals.containsKey(uuid);
    }

    public AnimalData getAnimalData(Entity entity) {
        if (entity == null) {
            return null;
        }

        AnimalData data = bondedAnimals.get(entity.getUniqueId());
        if (data != null) {
            return data;
        }

        return pendingAnimals.get(entity.getUniqueId());
    }

    public AnimalData getAnimalData(UUID entityUuid) {
        if (entityUuid == null) {
            return null;
        }

        AnimalData data = bondedAnimals.get(entityUuid);
        if (data != null) {
            return data;
        }

        return pendingAnimals.get(entityUuid);
    }

    public void registerAnimal(AnimalData data) {
        if (data == null) {
            return;
        }

        pendingAnimals.remove(data.getEntityUuid());
        bondedAnimals.put(data.getEntityUuid(), data);
    }

    public void registerPendingAnimal(AnimalData data) {
        if (data == null) {
            return;
        }

        if (!bondedAnimals.containsKey(data.getEntityUuid())) {
            pendingAnimals.put(data.getEntityUuid(), data);
        }
    }

    public boolean tryPromotePending(Entity entity) {
        if (entity == null) {
            return false;
        }

        AnimalData pending = pendingAnimals.remove(entity.getUniqueId());
        if (pending == null) {
            return false;
        }

        bondedAnimals.put(entity.getUniqueId(), pending);
        return true;
    }

    public void unregisterAnimal(UUID entityUuid) {
        if (entityUuid == null) {
            return;
        }

        bondedAnimals.remove(entityUuid);
        pendingAnimals.remove(entityUuid);
    }

    public Collection<AnimalData> getAllBondedAnimals() {
        return bondedAnimals.values();
    }

    public Collection<AnimalData> getAllPendingAnimals() {
        return pendingAnimals.values();
    }

    public int getBondedCount() {
        return bondedAnimals.size();
    }

    public int getPendingCount() {
        return pendingAnimals.size();
    }
}