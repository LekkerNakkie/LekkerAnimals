package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalFeederData;
import me.lekkernakkie.lekkeranimal.gui.FeedstationMenuHolder;
import me.lekkernakkie.lekkeranimal.util.FeederItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedstationBlockListener implements Listener {

    private final LekkerAnimal plugin;

    public FeedstationBlockListener(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!FeederItemUtil.isFeederItem(plugin, item)) {
            return;
        }

        Block block = event.getBlockPlaced();
        if (block.getType() != plugin.getConfigManager().getFeedstationSettings().getItemMaterial()) {
            return;
        }

        Player player = event.getPlayer();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        FeederTier tier = FeederItemUtil.getTier(plugin, item);
        UUID ownerUuid = FeederItemUtil.getOwnerUuid(plugin, item);
        String ownerName = FeederItemUtil.getOwnerName(plugin, item);
        boolean hologramEnabled = FeederItemUtil.isHologramEnabled(plugin, item);
        List<ItemStack> storedFood = FeederItemUtil.getStoredFood(plugin, item);

        if (tier == null || ownerUuid == null) {
            event.setCancelled(true);
            return;
        }

        AnimalFeederData data = new AnimalFeederData(
                UUID.randomUUID(),
                ownerUuid,
                ownerName,
                tier,
                hologramEnabled,
                block.getLocation(),
                storedFood
        );

        plugin.getFeedstationManager().registerFeeder(data);

        FeedstationSettings.TierSettings tierSettings = plugin.getConfigManager()
                .getFeedstationSettings()
                .getTierSettings(tier);

        lang.send(player, "feedstation.feeder-created", Map.of(
                "tier_display", tierSettings != null ? tierSettings.display() : tier.name()
        ));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        AnimalFeederData feeder = plugin.getFeedstationManager().getFeederAt(block);
        if (feeder == null) {
            return;
        }

        event.setDropItems(false);
        event.setExpToDrop(0);

        Player player = event.getPlayer();
        block.setType(Material.AIR);

        plugin.getFeedstationManager().dropStoredFood(block.getWorld(), block.getLocation(), feeder);

        ItemStack item = FeederItemUtil.createFeederItem(
                plugin,
                feeder.getOwnerUuid(),
                feeder.getOwnerName(),
                feeder.getTier(),
                List.of(),
                feeder.isHologramEnabled()
        );

        if (player.getGameMode() != GameMode.CREATIVE) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.5D, 0.5D), item);
        }

        plugin.getFeedstationManager().unregisterFeeder(feeder.getFeederUuid());

        FeedstationSettings.TierSettings tierSettings = plugin.getConfigManager()
                .getFeedstationSettings()
                .getTierSettings(feeder.getTier());

        plugin.getConfigManager().getLangSettings().send(player, "feedstation.feeder-removed", Map.of(
                "tier_display", tierSettings != null ? tierSettings.display() : feeder.getTier().name()
        ));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        AnimalFeederData feeder = plugin.getFeedstationManager().getFeederAt(block);
        if (feeder == null) {
            return;
        }

        event.setCancelled(true);
        plugin.getFeedstationManager().openMainMenu(event.getPlayer(), feeder);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof FeedstationMenuHolder holder)) {
            return;
        }

        AnimalFeederData feeder = plugin.getFeedstationManager().getFeeder(holder.getFeederUuid());
        if (feeder == null) {
            event.setCancelled(true);
            return;
        }

        Inventory top = event.getView().getTopInventory();
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }

        if (holder.getScreen() == FeedstationMenuHolder.Screen.MAIN) {
            event.setCancelled(true);

            if (!clicked.equals(top)) {
                return;
            }

            FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

            if (event.getSlot() == settings.getOpenStorageSlot()) {
                plugin.getFeedstationManager().openStorageMenu((Player) event.getWhoClicked(), feeder);
                return;
            }

            if (event.getSlot() == settings.getShowRadiusSlot()) {
                plugin.getFeedstationManager().showRadiusPreview((Player) event.getWhoClicked(), feeder);
                plugin.getConfigManager().getLangSettings().send((Player) event.getWhoClicked(), "feedstation.radius-shown");
                return;
            }

            if (event.getSlot() == settings.getToggleHologramSlot()) {
                feeder.setHologramEnabled(!feeder.isHologramEnabled());
                plugin.getFeedstationManager().openMainMenu((Player) event.getWhoClicked(), feeder);

                if (feeder.isHologramEnabled()) {
                    plugin.getConfigManager().getLangSettings().send((Player) event.getWhoClicked(), "feedstation.hologram-enabled");
                } else {
                    plugin.getConfigManager().getLangSettings().send((Player) event.getWhoClicked(), "feedstation.hologram-disabled");
                }
                return;
            }

            if (event.getSlot() == settings.getUpgradeSlot()) {
                plugin.getFeedstationManager().tryUpgrade((Player) event.getWhoClicked(), feeder);
                plugin.getFeedstationManager().openMainMenu((Player) event.getWhoClicked(), feeder);
            }

            return;
        }

        if (holder.getScreen() != FeedstationMenuHolder.Screen.STORAGE) {
            return;
        }

        if (clicked.equals(top)) {
            if (!plugin.getFeedstationManager().isStorageSlot(feeder, event.getSlot())) {
                event.setCancelled(true);
                return;
            }

            ItemStack cursor = event.getCursor();
            if (FeederItemUtil.isFeederItem(plugin, cursor)) {
                event.setCancelled(true);
                return;
            }

            if (cursor != null && !cursor.getType().isAir() && !plugin.getFeedstationManager().isPotentialAnimalFood(cursor.getType())) {
                event.setCancelled(true);
                plugin.getConfigManager().getLangSettings().send((Player) event.getWhoClicked(), "feedstation.invalid-food-item");
            }
            return;
        }

        if (event.isShiftClick()) {
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType().isAir()) {
                return;
            }

            if (FeederItemUtil.isFeederItem(plugin, current)) {
                event.setCancelled(true);
                return;
            }

            if (!plugin.getFeedstationManager().isPotentialAnimalFood(current.getType())) {
                event.setCancelled(true);
                plugin.getConfigManager().getLangSettings().send((Player) event.getWhoClicked(), "feedstation.invalid-food-item");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof FeedstationMenuHolder holder)) {
            return;
        }

        if (holder.getScreen() != FeedstationMenuHolder.Screen.STORAGE) {
            event.setCancelled(true);
            return;
        }

        AnimalFeederData feeder = plugin.getFeedstationManager().getFeeder(holder.getFeederUuid());
        if (feeder == null) {
            event.setCancelled(true);
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= event.getView().getTopInventory().getSize()) {
                continue;
            }

            if (!plugin.getFeedstationManager().isStorageSlot(feeder, rawSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        ItemStack oldCursor = event.getOldCursor();
        if (FeederItemUtil.isFeederItem(plugin, oldCursor)) {
            event.setCancelled(true);
            return;
        }

        if (oldCursor != null && !oldCursor.getType().isAir() && !plugin.getFeedstationManager().isPotentialAnimalFood(oldCursor.getType())) {
            event.setCancelled(true);
            plugin.getConfigManager().getLangSettings().send((Player) event.getWhoClicked(), "feedstation.invalid-food-item");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof FeedstationMenuHolder holder)) {
            return;
        }

        if (holder.getScreen() != FeedstationMenuHolder.Screen.STORAGE) {
            return;
        }

        AnimalFeederData feeder = plugin.getFeedstationManager().getFeeder(holder.getFeederUuid());
        if (feeder == null) {
            return;
        }

        plugin.getFeedstationManager().saveStorageMenu(event.getInventory(), feeder);
    }
}