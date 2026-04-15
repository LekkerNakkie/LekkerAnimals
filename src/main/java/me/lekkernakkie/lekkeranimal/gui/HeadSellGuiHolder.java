package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class HeadSellGuiHolder implements InventoryHolder {

    private final UUID playerUuid;

    public HeadSellGuiHolder(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}