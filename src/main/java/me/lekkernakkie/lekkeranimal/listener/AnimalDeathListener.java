package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class AnimalDeathListener implements Listener {

    private final AnimalManager animalManager;

    public AnimalDeathListener(AnimalManager animalManager) {
        this.animalManager = animalManager;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (animalManager.isBonded(entity)) {
            animalManager.unregisterAnimal(entity.getUniqueId());

            if (LekkerAnimal.getInstance().getHologramManager() != null) {
                LekkerAnimal.getInstance().getHologramManager().remove(entity);
            }
        }
    }
}