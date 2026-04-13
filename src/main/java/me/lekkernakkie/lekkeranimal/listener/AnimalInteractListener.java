package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import me.lekkernakkie.lekkeranimal.manager.BondManager;
import me.lekkernakkie.lekkeranimal.manager.LevelManager;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AnimalInteractListener implements Listener {

    private final LekkerAnimal plugin;
    private final BondManager bondManager;
    private final AnimalManager animalManager;
    private final LevelManager levelManager;

    public AnimalInteractListener(LekkerAnimal plugin, BondManager bondManager, AnimalManager animalManager, LevelManager levelManager) {
        this.plugin = plugin;
        this.bondManager = bondManager;
        this.animalManager = animalManager;
        this.levelManager = levelManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (animalManager.isBonded(entity)) {
            handleFeeding(player, entity, item, lang, event);
            return;
        }

        BondManager.BondResult result = bondManager.tryBond(player, entity, item);

        switch (result) {
            case SUCCESS -> {
                lang.send(player, "bonding.success");
                event.setCancelled(true);
            }
            case WRONG_ITEM -> {
            }
            case NOT_ENOUGH_ITEMS -> lang.send(player, "bonding.not-enough-items");
            case ALREADY_OWNED -> lang.send(player, "general.already-owned");
            case ALREADY_YOURS -> lang.send(player, "bonding.already-yours");
            case NOT_SUPPORTED -> {
            }
        }
    }

    private void handleFeeding(Player player, Entity entity, ItemStack item, LangSettings lang, PlayerInteractEntityEvent event) {
        AnimalData data = animalManager.getAnimalData(entity);
        if (data == null) {
            return;
        }

        if (!data.getOwnerUuid().equals(player.getUniqueId())) {
            lang.send(player, "general.not-your-animal");
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            lang.send(player, "general.animal-not-supported");
            return;
        }

        if (item == null || item.getType().isAir()) {
            return;
        }

        AnimalProfile.FoodSettings food = profile.getFoodSettings(item.getType());
        if (food == null) {
            return;
        }

        if (data.getHunger() >= profile.getMaxHunger()) {
            lang.send(player, "feeding.full");
            event.setCancelled(true);
            return;
        }

        data.setHunger(Math.min(profile.getMaxHunger(), data.getHunger() + food.getHungerRestore()));
        data.setBond(Math.min(plugin.getConfigManager().getMainSettings().getMaxBond(), data.getBond() + food.getBondGain()));
        levelManager.addXp(data, food.getXpGain());

        int newAmount = item.getAmount() - 1;
        item.setAmount(Math.max(newAmount, 0));

        lang.send(player, "feeding.success", Map.of(
                "animal", profile.getDisplayName(),
                "hunger", String.valueOf(data.getHunger()),
                "max_hunger", String.valueOf(profile.getMaxHunger())
        ));

        try {
            Sound sound = Sound.valueOf(plugin.getConfig().getString("sounds.feed", "ENTITY_PLAYER_LEVELUP"));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }

        event.setCancelled(true);
    }
}