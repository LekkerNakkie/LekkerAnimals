package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class FeedstationMenuHolder implements InventoryHolder {

    private final UUID feederUuid;

    public FeedstationMenuHolder(UUID feederUuid) {
        this.feederUuid = feederUuid;
    }

    public UUID getFeederUuid() {
        return feederUuid;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}