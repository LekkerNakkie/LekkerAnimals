package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import me.lekkernakkie.lekkeranimal.gui.AnimalGuiHolder;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GuiClickListener implements Listener {

    private final LekkerAnimal plugin;

    public GuiClickListener(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof AnimalGuiHolder holder)) {
            return;
        }

        event.setCancelled(true);

        Entity entity = plugin.getServer().getEntity(holder.getEntityUuid());
        if (entity == null || !entity.isValid() || entity.isDead()) {
            player.closeInventory();
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            player.closeInventory();
            return;
        }

        if (!data.getOwnerUuid().equals(player.getUniqueId())) {
            player.closeInventory();
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            player.closeInventory();
            return;
        }

        int rawSlot = event.getRawSlot();
        int hungerSlot = plugin.getConfigManager().getGuiSettings().getHungerSlot();
        int levelSlot = plugin.getConfigManager().getGuiSettings().getLevelSlot();

        if (rawSlot == hungerSlot) {
            handleFeedClick(player, entity, data, profile);
            return;
        }

        if (rawSlot == levelSlot) {
            handleLevelClick(player, entity, data, profile);
        }
    }

    private void handleFeedClick(Player player, Entity entity, AnimalData data, AnimalProfile profile) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (data.getHunger() >= profile.getMaxHunger()) {
            lang.send(player, "feeding.full");
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        ItemStack foodStack = findFirstMatchingFood(player, profile);
        if (foodStack == null) {
            lang.send(player, "feeding.no-food-in-inventory");
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        FeedingReward reward = profile.getFeedingReward(foodStack.getType());
        if (reward == null) {
            lang.send(player, "feeding.invalid-food");
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();

        int newHunger = Math.min(profile.getMaxHunger(), data.getHunger() + reward.getHungerRestore());
        data.setHunger(newHunger);
        data.setMaxHunger(profile.getMaxHunger());

        int newBond = Math.min(mainSettings.getMaxBond(), data.getBond() + reward.getBondGain());
        data.setBond(newBond);

        int levelsGained = plugin.getLevelManager().addXp(data, profile, reward.getXp());

        consume(foodStack, 1);
        plugin.getDataManager().saveAnimal(data);

        lang.send(player, "feeding.success", Map.of(
                "animal", profile.getDisplayName(),
                "hunger", String.valueOf(data.getHunger()),
                "max_hunger", String.valueOf(profile.getMaxHunger()),
                "xp", String.valueOf(reward.getXp())
        ));

        if (levelsGained > 0) {
            lang.send(player, "leveling.level-up", Map.of(
                    "animal", profile.getDisplayName(),
                    "level", String.valueOf(data.getLevel())
            ));
        }

        plugin.getHologramManager().refresh(entity);
        plugin.getGuiManager().openAnimalInfo(player, entity);
    }

    private void handleLevelClick(Player player, Entity entity, AnimalData data, AnimalProfile profile) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (data.getLevel() >= profile.getMaxLevel()) {
            lang.send(player, "leveling.max-level");
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        int targetLevel = data.getLevel() + 1;
        DirectLevelUpgrade upgrade = profile.getDirectLevelUpgrade(targetLevel);

        if (upgrade == null) {
            lang.send(player, "leveling.direct-upgrade-no-config");
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        int have = countMaterial(player, upgrade.getItem());
        if (have < upgrade.getAmount()) {
            int missing = upgrade.getAmount() - have;

            lang.send(player, "leveling.direct-upgrade-not-enough", Map.of(
                    "item", formatMaterial(upgrade.getItem()),
                    "required", String.valueOf(upgrade.getAmount()),
                    "have", String.valueOf(have),
                    "missing", String.valueOf(missing),
                    "level", String.valueOf(targetLevel)
            ));
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        ItemStack upgradeStack = findFirstMatchingStack(player, upgrade.getItem(), upgrade.getAmount());
        if (upgradeStack == null) {
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        consume(upgradeStack, upgrade.getAmount());
        data.setLevel(targetLevel);
        data.setXp(0);

        plugin.getDataManager().saveAnimal(data);

        lang.send(player, "leveling.direct-upgrade-success", Map.of(
                "animal", profile.getDisplayName(),
                "level", String.valueOf(data.getLevel())
        ));

        plugin.getHologramManager().refresh(entity);
        plugin.getGuiManager().openAnimalInfo(player, entity);
    }

    private ItemStack findFirstMatchingFood(Player player, AnimalProfile profile) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            if (profile.getFeedingReward(item.getType()) != null && item.getAmount() > 0) {
                return item;
            }
        }

        return null;
    }

    private ItemStack findFirstMatchingStack(Player player, Material material, int amountNeeded) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            if (item.getType() == material && item.getAmount() >= amountNeeded) {
                return item;
            }
        }

        return null;
    }

    private void consume(ItemStack item, int amount) {
        int newAmount = item.getAmount() - amount;
        item.setAmount(Math.max(newAmount, 0));
    }

    private int countMaterial(Player player, Material material) {
        int amount = 0;

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }

            if (stack.getType() == material) {
                amount += stack.getAmount();
            }
        }

        return amount;
    }

    private String formatMaterial(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }

        return builder.toString().trim();
    }
}