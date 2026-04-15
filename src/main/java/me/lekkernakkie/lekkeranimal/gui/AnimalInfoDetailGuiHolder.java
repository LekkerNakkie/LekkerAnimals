package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AnimalInfoDetailGuiHolder implements InventoryHolder {

    private final String entityTypeName;

    public AnimalInfoDetailGuiHolder(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}