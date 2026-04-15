package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.GuiSettings;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import me.lekkernakkie.lekkeranimal.manager.BondManager;
import me.lekkernakkie.lekkeranimal.manager.LevelManager;
import me.lekkernakkie.lekkeranimal.util.ItemUtil;
import org.bukkit.Material;
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

    public AnimalInteractListener(LekkerAnimal plugin,
                                  BondManager bondManager,
                                  AnimalManager animalManager,
                                  LevelManager levelManager) {
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
        LangSettings lang = plugin.getConfigManager().getLangSettings();
        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();

        if (!animalManager.isSupported(entity)) {
            return;
        }

        if (player.isSneaking() && animalManager.isBonded(entity)) {
            AnimalData data = animalManager.getAnimalData(entity);

            boolean canOpen = data != null && (
                    data.isOwner(player.getUniqueId())
                            || (mainSettings.isCoOwnersEnabled() && mainSettings.isCoOwnersAllowGuiAccess() && data.isCoOwner(player.getUniqueId()))
            );

            if (canOpen) {
                GuiSettings guiSettings = plugin.getConfigManager().getGuiSettings();
                if (guiSettings.isAnimalInfoEnabled()) {
                    plugin.getGuiManager().openAnimalInfo(player, entity);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isEmpty(item)) {
            return;
        }

        if (!animalManager.isBonded(entity)) {
            handleBonding(player, entity, item, lang, event);
            return;
        }

        AnimalData data = animalManager.getAnimalData(entity);
        if (data == null) {
            return;
        }

        boolean isOwner = data.isOwner(player.getUniqueId());
        boolean isCoOwner = mainSettings.isCoOwnersEnabled() && data.isCoOwner(player.getUniqueId());

        if (!isOwner && !isCoOwner) {
            lang.send(player, "general.not-your-animal");
            event.setCancelled(true);
            return;
        }

        if (isOwner && !player.getName().equalsIgnoreCase(data.getOwnerName())) {
            data.setOwnerName(player.getName());
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            return;
        }

        boolean canDirectUpgrade = isOwner || (isCoOwner && mainSettings.isCoOwnersAllowDirectUpgrades());
        boolean canFeed = isOwner || (isCoOwner && mainSettings.isCoOwnersAllowFeed());

        if (canDirectUpgrade && tryDirectUpgrade(player, entity, item, data, profile, lang, event)) {
            return;
        }

        if (canFeed) {
            tryFeeding(player, entity, item, data, profile, lang, event);
        }
    }

    private void handleBonding(Player player,
                               Entity entity,
                               ItemStack item,
                               LangSettings lang,
                               PlayerInteractEntityEvent event) {
        BondManager.BondResult result = bondManager.tryBond(player, entity, item);

        switch (result) {
            case SUCCESS -> {
                lang.send(player, "bonding.success");
                plugin.getHologramManager().refresh(entity);
                event.setCancelled(true);
            }
            case NOT_ENOUGH_ITEMS -> {
                lang.send(player, "bonding.not-enough-items");
                event.setCancelled(true);
            }
            case ALREADY_OWNED -> {
                lang.send(player, "general.already-owned");
                event.setCancelled(true);
            }
            case ALREADY_YOURS -> {
                lang.send(player, "bonding.already-yours");
                event.setCancelled(true);
            }
            case WRONG_ITEM, NOT_SUPPORTED -> {
            }
        }
    }

    private boolean tryDirectUpgrade(Player player,
                                     Entity entity,
                                     ItemStack item,
                                     AnimalData data,
                                     AnimalProfile profile,
                                     LangSettings lang,
                                     PlayerInteractEntityEvent event) {

        if (data.getLevel() >= profile.getMaxLevel()) {
            return false;
        }

        int targetLevel = data.getLevel() + 1;
        DirectLevelUpgrade nextUpgrade = profile.getDirectLevelUpgrade(targetLevel);

        if (nextUpgrade == null) {
            return false;
        }

        if (item.getType() != nextUpgrade.getItem()) {
            return false;
        }

        LevelManager.DirectUpgradeResult result = levelManager.tryDirectUpgrade(player, entity, item);

        switch (result) {
            case SUCCESS -> {
                plugin.getDataManager().saveAnimal(data);
                lang.send(player, "leveling.direct-upgrade-success", Map.of(
                        "animal", profile.getDisplayName(),
                        "level", String.valueOf(data.getLevel())
                ));
                plugin.getHologramManager().refresh(entity);
                event.setCancelled(true);
                return true;
            }
            case MAX_LEVEL -> {
                lang.send(player, "leveling.max-level");
                event.setCancelled(true);
                return true;
            }
            case NO_CONFIG -> {
                lang.send(player, "leveling.direct-upgrade-no-config");
                event.setCancelled(true);
                return true;
            }
            case WRONG_ITEM -> {
                lang.send(player, "leveling.direct-upgrade-wrong-item");
                event.setCancelled(true);
                return true;
            }
            case NOT_ENOUGH_ITEMS -> {
                int have = countMaterial(player, nextUpgrade.getItem());
                int required = nextUpgrade.getAmount();
                int missing = Math.max(0, required - have);

                lang.send(player, "leveling.direct-upgrade-not-enough", Map.of(
                        "item", formatMaterial(nextUpgrade.getItem()),
                        "required", String.valueOf(required),
                        "have", String.valueOf(have),
                        "missing", String.valueOf(missing),
                        "level", String.valueOf(targetLevel)
                ));
                event.setCancelled(true);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void tryFeeding(Player player,
                            Entity entity,
                            ItemStack item,
                            AnimalData data,
                            AnimalProfile profile,
                            LangSettings lang,
                            PlayerInteractEntityEvent event) {

        FeedingReward reward = profile.getFeedingReward(item.getType());
        if (reward == null) {
            return;
        }

        if (data.getHunger() >= profile.getMaxHunger()) {
            lang.send(player, "feeding.full");
            event.setCancelled(true);
            return;
        }

        MainSettings mainSettings = plugin.getConfigManager().getMainSettings();

        int newHunger = Math.min(profile.getMaxHunger(), data.getHunger() + reward.getHungerRestore());
        data.setHunger(newHunger);
        data.setMaxHunger(profile.getMaxHunger());

        int newBond = Math.min(mainSettings.getMaxBond(), data.getBond() + reward.getBondGain());
        data.setBond(newBond);

        int levelsGained = levelManager.addXp(data, profile, reward.getXp());

        consumeOne(item);
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
        event.setCancelled(true);
    }

    private void consumeOne(ItemStack item) {
        int newAmount = item.getAmount() - 1;
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