package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiClickListener implements Listener {

    private final LekkerAnimal plugin;

    public GuiClickListener(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getRawSlot() == 24) {

            AnimalData data = plugin.getSelectedAnimal(player);
            if (data == null) return;

            if (!data.isOwner(player.getUniqueId())) return;

            data.setCoOwnersKeepActive(!data.isCoOwnersKeepActive());

            player.sendMessage("§7Co-owner activiteit: "
                    + (data.isCoOwnersKeepActive() ? "§aAAN" : "§cUIT"));

            plugin.getGuiManager().openCoOwnerMenu(player, data);
        }
    }
}