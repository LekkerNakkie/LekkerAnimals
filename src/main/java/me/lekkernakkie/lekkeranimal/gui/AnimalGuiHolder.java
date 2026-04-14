package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class AnimalGuiHolder implements InventoryHolder {

    private final UUID entityUuid;

    public AnimalGuiHolder(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}