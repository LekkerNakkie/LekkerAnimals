package me.lekkernakkie.lekkeranimal.data;

import org.bukkit.entity.EntityType;

import java.util.UUID;

public class AnimalData {

    private final UUID entityUuid;
    private final UUID ownerUuid;
    private final EntityType entityType;

    private int hunger;
    private int level;
    private int xp;
    private int bond;

    public AnimalData(UUID entityUuid, UUID ownerUuid, EntityType entityType, int hunger, int level, int xp, int bond) {
        this.entityUuid = entityUuid;
        this.ownerUuid = ownerUuid;
        this.entityType = entityType;
        this.hunger = hunger;
        this.level = level;
        this.xp = xp;
        this.bond = bond;
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

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = Math.max(0, hunger);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    public int getBond() {
        return bond;
    }

    public void setBond(int bond) {
        this.bond = Math.max(0, bond);
    }
}