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
    private final boolean openedFromAnimalsMenu;
    private final boolean animalsMenuCoOwnerView;
    private final int animalsMenuPage;

    public AnimalGuiHolder(UUID entityUuid) {
        this(entityUuid, ScreenType.MAIN, null, false, false, 0);
    }

    public AnimalGuiHolder(UUID entityUuid, ScreenType screenType) {
        this(entityUuid, screenType, null, false, false, 0);
    }

    public AnimalGuiHolder(UUID entityUuid, ScreenType screenType, UUID targetCoOwnerUuid) {
        this(entityUuid, screenType, targetCoOwnerUuid, false, false, 0);
    }

    public AnimalGuiHolder(UUID entityUuid,
                           ScreenType screenType,
                           UUID targetCoOwnerUuid,
                           boolean openedFromAnimalsMenu,
                           boolean animalsMenuCoOwnerView,
                           int animalsMenuPage) {
        this.entityUuid = entityUuid;
        this.screenType = screenType;
        this.targetCoOwnerUuid = targetCoOwnerUuid;
        this.openedFromAnimalsMenu = openedFromAnimalsMenu;
        this.animalsMenuCoOwnerView = animalsMenuCoOwnerView;
        this.animalsMenuPage = animalsMenuPage;
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

    public boolean isOpenedFromAnimalsMenu() {
        return openedFromAnimalsMenu;
    }

    public boolean isAnimalsMenuCoOwnerView() {
        return animalsMenuCoOwnerView;
    }

    public int getAnimalsMenuPage() {
        return animalsMenuPage;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}