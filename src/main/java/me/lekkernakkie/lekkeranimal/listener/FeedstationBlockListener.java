package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalFeederData;
import me.lekkernakkie.lekkeranimal.util.FeederItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

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
        if (block.getType() != Material.CAULDRON) {
            return;
        }

        Player player = event.getPlayer();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        FeederTier tier = FeederItemUtil.getTier(plugin, item);
        UUID ownerUuid = FeederItemUtil.getOwnerUuid(plugin, item);
        String ownerName = FeederItemUtil.getOwnerName(plugin, item);

        if (tier == null || ownerUuid == null) {
            event.setCancelled(true);
            return;
        }

        AnimalFeederData data = new AnimalFeederData(
                UUID.randomUUID(),
                ownerUuid,
                ownerName,
                tier,
                block.getLocation()
        );

        plugin.getFeedstationManager().registerFeeder(data);

        lang.send(player, "feedstation.feeder-created", Map.of(
                "tier_display", plugin.getConfigManager().getFeedstationSettings().getTierSettings(tier).display()
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

        ItemStack item = FeederItemUtil.createFeederItem(
                plugin,
                feeder.getOwnerUuid(),
                feeder.getOwnerName(),
                feeder.getTier()
        );

        if (player.getGameMode() == GameMode.CREATIVE) {
            // in creative niet dubbel spawnen
        } else {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.5D, 0.5D), item);
        }

        plugin.getFeedstationManager().unregisterFeeder(feeder.getFeederUuid());

        plugin.getConfigManager().getLangSettings().send(player, "feedstation.feeder-removed", Map.of(
                "tier_display", plugin.getConfigManager().getFeedstationSettings().getTierSettings(feeder.getTier()).display()
        ));
    }
}