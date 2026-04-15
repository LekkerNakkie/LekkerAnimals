package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.gui.HeadSellGuiHolder;
import me.lekkernakkie.lekkeranimal.manager.HeadSellManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class HeadSellListener implements Listener {

    private final LekkerAnimal plugin;

    public HeadSellListener(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof HeadSellGuiHolder)) {
            return;
        }

        HeadSellManager manager = plugin.getHeadSellManager();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0) {
            return;
        }

        if (rawSlot < top.getSize()) {
            if (manager.isSellSlot(rawSlot)) {
                handleSellSlotClick(player, top, rawSlot);
                manager.updateInfoItem(top);
                return;
            }

            if (rawSlot == manager.getConfirmSlot()) {
                HeadSellManager.SaleResult result = manager.sellAll(player, top);

                if (result.soldHeads() <= 0) {
                    lang.send(player, "heads.sell-none");
                    return;
                }

                lang.send(player, "heads.sell-success", Map.of(
                        "amount", String.valueOf(result.soldHeads()),
                        "money", manager.formatMoney(result.total())
                ));
                player.closeInventory();
                return;
            }

            if (rawSlot == manager.getCloseSlot()) {
                player.closeInventory();
                return;
            }

            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }

        if (event.isShiftClick()) {
            int moved = manager.moveToSellInventory(top, current.clone());
            if (moved <= 0) {
                if (current.getType() == Material.PLAYER_HEAD) {
                    lang.send(player, "heads.not-sellable");
                }
                return;
            }

            if (moved >= current.getAmount()) {
                event.getClickedInventory().setItem(event.getSlot(), null);
            } else {
                current.setAmount(current.getAmount() - moved);
            }

            manager.updateInfoItem(top);
        }
    }

    private void handleSellSlotClick(Player player, Inventory top, int slot) {
        HeadSellManager manager = plugin.getHeadSellManager();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        ItemStack current = top.getItem(slot);
        ItemStack cursor = player.getItemOnCursor();

        boolean currentEmpty = current == null || current.getType().isAir();
        boolean cursorEmpty = cursor == null || cursor.getType().isAir();

        if (cursorEmpty && !currentEmpty) {
            player.setItemOnCursor(current);
            top.setItem(slot, null);
            return;
        }

        if (!cursorEmpty) {
            if (!manager.isSellableHead(cursor)) {
                lang.send(player, "heads.not-sellable");
                return;
            }

            if (currentEmpty) {
                top.setItem(slot, cursor.clone());
                player.setItemOnCursor(null);
                return;
            }

            if (current.isSimilar(cursor)) {
                int maxStack = Math.min(current.getMaxStackSize(), 64);
                int free = maxStack - current.getAmount();
                if (free <= 0) {
                    return;
                }

                int moved = Math.min(free, cursor.getAmount());
                current.setAmount(current.getAmount() + moved);

                if (moved >= cursor.getAmount()) {
                    player.setItemOnCursor(null);
                } else {
                    cursor.setAmount(cursor.getAmount() - moved);
                    player.setItemOnCursor(cursor);
                }
                return;
            }

            if (manager.isSellableHead(current)) {
                top.setItem(slot, cursor.clone());
                player.setItemOnCursor(current);
            }
            return;
        }

        if (eventIsShiftPickup(player)) {
            player.getInventory().addItem(current).values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover)
            );
            top.setItem(slot, null);
        }
    }

    private boolean eventIsShiftPickup(Player player) {
        ClickType click = player.getOpenInventory().getCursor() == null ? ClickType.LEFT : ClickType.LEFT;
        return click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT || click == ClickType.LEFT;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof HeadSellGuiHolder)) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= top.getSize()) {
                continue;
            }

            if (!plugin.getHeadSellManager().isSellSlot(rawSlot)) {
                event.setCancelled(true);
                return;
            }

            ItemStack oldCursor = event.getOldCursor();
            if (oldCursor == null || oldCursor.getType().isAir()) {
                continue;
            }

            if (!plugin.getHeadSellManager().isSellableHead(oldCursor)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory top = event.getInventory();
        if (!(top.getHolder() instanceof HeadSellGuiHolder)) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        plugin.getHeadSellManager().returnItems(player, top);
    }
}