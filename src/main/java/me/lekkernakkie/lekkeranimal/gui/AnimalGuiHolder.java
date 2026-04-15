package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class AnimalGuiHolder implements InventoryHolder {

    public enum ScreenType {
        MAIN,
        CO_OWNERS,
        REMOVE_CO_OWNER_CONFIRM
    }

    private final UUID entityUuid;
    private final ScreenType screenType;
    private final UUID targetCoOwnerUuid;

    public AnimalGuiHolder(UUID entityUuid) {
        this(entityUuid, ScreenType.MAIN, null);
    }

    public AnimalGuiHolder(UUID entityUuid, ScreenType screenType) {
        this(entityUuid, screenType, null);
    }

    public AnimalGuiHolder(UUID entityUuid, ScreenType screenType, UUID targetCoOwnerUuid) {
        this.entityUuid = entityUuid;
        this.screenType = screenType;
        this.targetCoOwnerUuid = targetCoOwnerUuid;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public UUID getTargetCoOwnerUuid() {
        return targetCoOwnerUuid;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}