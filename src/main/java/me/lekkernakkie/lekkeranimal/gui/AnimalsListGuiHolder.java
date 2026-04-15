package me.lekkernakkie.lekkeranimal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AnimalsListGuiHolder implements InventoryHolder {

    private final boolean coOwnerView;
    private final int page;

    public AnimalsListGuiHolder(boolean coOwnerView, int page) {
        this.coOwnerView = coOwnerView;
        this.page = page;
    }

    public boolean isCoOwnerView() {
        return coOwnerView;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}