package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnimalData {

    private long databaseId;

    private final UUID entityUuid;
    private final UUID ownerUuid;
    private final EntityType entityType;

    private String ownerName;
    private int hunger;
    private int maxHunger;
    private int level;
    private int xp;
    private int bond;

    private String worldName;
    private double x;
    private double y;
    private double z;

    private long createdAt;
    private long updatedAt;
    private long harvestProgressMillis;

    private final Set<UUID> coOwnerUuids = new LinkedHashSet<>();
    private boolean coOwnersKeepActive;

    private boolean dirty = true;

    public AnimalData(UUID entityUuid, UUID ownerUuid, EntityType entityType, int hunger, int level, int xp, int bond) {
        this(
                0L,
                entityUuid,
                ownerUuid,
                "",
                entityType,
                hunger,
                hunger,
                level,
                xp,
                bond,
                null,
                0.0,
                0.0,
                0.0,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L,
                "",
                false
        );
    }

    public AnimalData(UUID entityUuid,
                      UUID ownerUuid,
                      String ownerName,
                      EntityType entityType,
                      int hunger,
                      int maxHunger,
                      int level,
                      int xp,
                      int bond) {
        this(
                0L,
                entityUuid,
                ownerUuid,
                ownerName,
                entityType,
                hunger,
                maxHunger,
                level,
                xp,
                bond,
                null,
                0.0,
                0.0,
                0.0,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L,
                "",
                false
        );
    }

    public AnimalData(long databaseId,
                      UUID entityUuid,
                      UUID ownerUuid,
                      String ownerName,
                      EntityType entityType,
                      int hunger,
                      int maxHunger,
                      int level,
                      int xp,
                      int bond,
                      String worldName,
                      double x,
                      double y,
                      double z,
                      long createdAt,
                      long updatedAt,
                      long harvestProgressMillis,
                      String coOwnersSerialized) {
        this(
                databaseId,
                entityUuid,
                ownerUuid,
                ownerName,
                entityType,
                hunger,
                maxHunger,
                level,
                xp,
                bond,
                worldName,
                x,
                y,
                z,
                createdAt,
                updatedAt,
                harvestProgressMillis,
                coOwnersSerialized,
                false
        );
    }

    public AnimalData(long databaseId,
                      UUID entityUuid,
                      UUID ownerUuid,
                      String ownerName,
                      EntityType entityType,
                      int hunger,
                      int maxHunger,
                      int level,
                      int xp,
                      int bond,
                      String worldName,
                      double x,
                      double y,
                      double z,
                      long createdAt,
                      long updatedAt,
                      long harvestProgressMillis,
                      String coOwnersSerialized,
                      boolean coOwnersKeepActive) {
        this.databaseId = databaseId;
        this.entityUuid = entityUuid;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.entityType = entityType;
        this.hunger = hunger;
        this.maxHunger = maxHunger;
        this.level = level;
        this.xp = xp;
        this.bond = bond;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.harvestProgressMillis = harvestProgressMillis;
        this.coOwnersKeepActive = coOwnersKeepActive;
        applySerializedCoOwners(coOwnersSerialized);
    }

    public void syncLocation(Entity entity) {
        if (entity == null || !entity.isValid()) {
            return;
        }

        Location location = entity.getLocation();
        this.worldName = location.getWorld() != null ? location.getWorld().getName() : null;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public boolean isOwner(UUID uuid) {
        return uuid != null && ownerUuid.equals(uuid);
    }

    public boolean isCoOwner(UUID uuid) {
        return uuid != null && coOwnerUuids.contains(uuid);
    }

    public boolean canAccess(UUID uuid) {
        return isOwner(uuid) || isCoOwner(uuid);
    }

    public boolean addCoOwner(UUID uuid, int max) {
        if (uuid == null || isOwner(uuid) || coOwnerUuids.contains(uuid) || coOwnerUuids.size() >= max) {
            return false;
        }

        boolean added = coOwnerUuids.add(uuid);
        if (added) {
            this.updatedAt = System.currentTimeMillis();
            this.dirty = true;
        }
        return added;
    }

    public boolean removeCoOwner(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        boolean removed = coOwnerUuids.remove(uuid);
        if (removed) {
            this.updatedAt = System.currentTimeMillis();
            this.dirty = true;
        }
        return removed;
    }

    public int getCoOwnerCount() {
        return coOwnerUuids.size();
    }

    public Set<UUID> getCoOwnerUuids() {
        return new LinkedHashSet<>(coOwnerUuids);
    }

    public String getSerializedCoOwners() {
        return coOwnerUuids.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
    }

    public void applySerializedCoOwners(String input) {
        coOwnerUuids.clear();

        if (input == null || input.isBlank()) {
            return;
        }

        for (String part : input.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            try {
                UUID uuid = UUID.fromString(trimmed);
                if (!uuid.equals(ownerUuid)) {
                    coOwnerUuids.add(uuid);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public int getMaxHunger() {
        return maxHunger;
    }

    public void setMaxHunger(int maxHunger) {
        this.maxHunger = maxHunger;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public int getBond() {
        return bond;
    }

    public void setBond(int bond) {
        this.bond = bond;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getHarvestProgressMillis() {
        return harvestProgressMillis;
    }

    public void setHarvestProgressMillis(long harvestProgressMillis) {
        this.harvestProgressMillis = Math.max(0L, harvestProgressMillis);
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public void addHarvestProgressMillis(long amount) {
        if (amount <= 0L) {
            return;
        }

        this.harvestProgressMillis += amount;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public boolean isCoOwnersKeepActive() {
        return coOwnersKeepActive;
    }

    public void setCoOwnersKeepActive(boolean coOwnersKeepActive) {
        this.coOwnersKeepActive = coOwnersKeepActive;
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}