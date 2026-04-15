package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class AnimalLoadListener implements Listener {

    private final LekkerAnimal plugin;
    private final AnimalManager animalManager;

    public AnimalLoadListener(LekkerAnimal plugin, AnimalManager animalManager) {
        this.plugin = plugin;
        this.animalManager = animalManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        restoreFromChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        for (Chunk chunk : event.getWorld().getLoadedChunks()) {
            restoreFromChunk(chunk);
        }
    }

    private void restoreFromChunk(Chunk chunk) {
        if (chunk == null) {
            return;
        }

        int restored = 0;

        for (Entity entity : chunk.getEntities()) {
            if (!animalManager.tryPromotePending(entity)) {
                continue;
            }

            AnimalData data = animalManager.getAnimalData(entity);
            if (data != null) {
                data.syncLocation(entity);
                plugin.getHologramManager().refresh(entity);
                restored++;
            }
        }

        if (restored > 0) {
            plugin.getLogger().info("Restored " + restored + " bonded animal(s) from chunk load.");
        }
    }
}