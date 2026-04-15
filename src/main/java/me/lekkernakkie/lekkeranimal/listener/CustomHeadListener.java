package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.manager.HarvestManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

public class CustomHeadListener implements Listener {

    private final LekkerAnimal plugin;

    public CustomHeadListener(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().getMainSettings().isCustomHeadsEnabled()) {
            return;
        }

        if (!plugin.getConfigManager().getMainSettings().isPreservePlacedCustomHeads()) {
            return;
        }

        ItemStack item = event.getItemInHand();
        HarvestManager harvestManager = plugin.getHarvestManager();

        if (!harvestManager.isPluginCustomHead(item)) {
            return;
        }

        Block block = event.getBlockPlaced();
        if (!(block.getState() instanceof Skull skull)) {
            return;
        }

        if (!item.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer blockPdc = skull.getPersistentDataContainer();

        harvestManager.applyPersistentHeadData(
                skull,
                harvestManager.getStoredRarityFromMeta(itemMeta),
                harvestManager.getStoredAnimalNameFromMeta(itemMeta),
                harvestManager.getStoredHeadOwnerFromMeta(itemMeta),
                harvestManager.getStoredHeadTextureFromMeta(itemMeta)
        );

        skull.update(true, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().getMainSettings().isCustomHeadsEnabled()) {
            return;
        }

        if (!plugin.getConfigManager().getMainSettings().isPreservePlacedCustomHeads()) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
            return;
        }

        if (!(block.getState() instanceof Skull skull)) {
            return;
        }

        String rarity = plugin.getHarvestManager().getStoredRarity(skull);
        String animalName = plugin.getHarvestManager().getStoredAnimalName(skull);
        String headOwner = plugin.getHarvestManager().getStoredHeadOwner(skull);
        String headTexture = plugin.getHarvestManager().getStoredHeadTexture(skull);

        if (!skull.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "lekkeranimal_head"),
                org.bukkit.persistence.PersistentDataType.BYTE
        )) {
            return;
        }

        event.setDropItems(false);

        ItemStack item = plugin.getHarvestManager().createPersistentHeadItem(
                rarity,
                animalName,
                headOwner,
                headTexture
        );

        block.getWorld().dropItemNaturally(block.getLocation(), item);
    }
}