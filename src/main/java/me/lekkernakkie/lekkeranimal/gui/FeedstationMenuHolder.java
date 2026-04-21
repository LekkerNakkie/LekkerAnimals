package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class FeedstationMenuHolder implements InventoryHolder {

    private final UUID feederUuid;
    private final Screen screen;

    public FeedstationMenuHolder(UUID feederUuid, Screen screen) {
        this.feederUuid = feederUuid;
        this.screen = screen;
    }

    public UUID getFeederUuid() {
        return feederUuid;
    }

    public Screen getScreen() {
        return screen;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public enum Screen {
        MAIN,
        STORAGE
    }
}