package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.GuiSettings;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.data.FeedingReward;
import me.lekkernakkie.lekkeranimal.gui.AnimalGuiHolder;
import me.lekkernakkie.lekkeranimal.gui.AnimalInfoDetailGuiHolder;
import me.lekkernakkie.lekkeranimal.gui.AnimalsHomeGuiHolder;
import me.lekkernakkie.lekkeranimal.gui.AnimalsInfoListGuiHolder;
import me.lekkernakkie.lekkeranimal.gui.AnimalsListGuiHolder;
import me.lekkernakkie.lekkeranimal.manager.HarvestManager;
import me.lekkernakkie.lekkeranimal.manager.LevelManager;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiClickListener implements Listener {

    private final LekkerAnimal plugin;
    private final NamespacedKey animalsListEntityKey;
    private final NamespacedKey animalsInfoTypeKey;

    public GuiClickListener(LekkerAnimal plugin) {
        this.plugin = plugin;
        this.animalsListEntityKey = new NamespacedKey(plugin, "animals_list_entity");
        this.animalsInfoTypeKey = new NamespacedKey(plugin, "animals_info_type");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getInventory().getHolder() instanceof AnimalsHomeGuiHolder) {
            handleAnimalsHomeClick(event, player);
            return;
        }

        if (event.getInventory().getHolder() instanceof AnimalsInfoListGuiHolder) {
            handleAnimalsInfoListClick(event, player);
            return;
        }

        if (event.getInventory().getHolder() instanceof AnimalInfoDetailGuiHolder holder) {
            handleAnimalInfoDetailClick(event, player, holder);
            return;
        }

        if (event.getInventory().getHolder() instanceof AnimalsListGuiHolder holder) {
            handleAnimalsListClick(event, player, holder);
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

        MainSettings main = plugin.getConfigManager().getMainSettings();
        boolean allowed = data.isOwner(player.getUniqueId())
                || (main.isCoOwnersEnabled() && main.isCoOwnersAllowGuiAccess() && data.isCoOwner(player.getUniqueId()));

        if (!allowed) {
            player.closeInventory();
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null || !profile.isEnabled()) {
            player.closeInventory();
            return;
        }

        switch (holder.getScreenType()) {
            case MAIN -> handleMainMenuClick(event, player, entity, data, profile, holder);
            case CO_OWNERS -> handleCoOwnerMenuClick(event, player, entity, data);
            case REMOVE_CO_OWNER_CONFIRM -> handleRemoveConfirmClick(event, player, entity, data, holder);
        }
    }

    private void handleAnimalsHomeClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();

        if (rawSlot == 11) {
            plugin.getGuiManager().openAnimalsInfoList(player);
            return;
        }

        if (rawSlot == 15) {
            plugin.getGuiManager().openAnimalsList(player, false, 0);
        }
    }

    private void handleAnimalsInfoListClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();

        if (rawSlot == 22) {
            plugin.getGuiManager().openAnimalsHome(player);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        String typeName = clicked.getItemMeta().getPersistentDataContainer().get(
                animalsInfoTypeKey,
                PersistentDataType.STRING
        );

        if (typeName == null || typeName.isBlank()) {
            return;
        }

        try {
            EntityType entityType = EntityType.valueOf(typeName);
            plugin.getGuiManager().openAnimalInfoDetail(player, entityType);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void handleAnimalInfoDetailClick(InventoryClickEvent event, Player player, AnimalInfoDetailGuiHolder holder) {
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot == 36) {
            plugin.getGuiManager().openAnimalsInfoList(player);
        }
    }

    private void handleAnimalsListClick(InventoryClickEvent event, Player player, AnimalsListGuiHolder holder) {
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        boolean coOwnerView = holder.isCoOwnerView();
        int page = holder.getPage();

        if (rawSlot == 45 && coOwnerView) {
            plugin.getGuiManager().openAnimalsList(player, false, 0);
            return;
        }

        if (rawSlot == 46 && !coOwnerView) {
            plugin.getGuiManager().openAnimalsList(player, true, 0);
            return;
        }

        if (rawSlot == 48 && page > 0) {
            plugin.getGuiManager().openAnimalsList(player, coOwnerView, page - 1);
            return;
        }

        if (rawSlot == 50) {
            plugin.getGuiManager().openAnimalsList(player, coOwnerView, page + 1);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        String entityUuidRaw = clicked.getItemMeta().getPersistentDataContainer().get(
                animalsListEntityKey,
                PersistentDataType.STRING
        );

        if (entityUuidRaw == null || entityUuidRaw.isBlank()) {
            return;
        }

        UUID entityUuid;
        try {
            entityUuid = UUID.fromString(entityUuidRaw);
        } catch (IllegalArgumentException ex) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(entityUuid);
        if (entity == null || !entity.isValid() || entity.isDead()) {
            player.sendMessage("§cDit dier is momenteel niet geladen.");
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entityUuid);
        if (data == null) {
            player.sendMessage("§cKon dierdata niet vinden.");
            return;
        }

        MainSettings main = plugin.getConfigManager().getMainSettings();
        boolean allowed = data.isOwner(player.getUniqueId())
                || (main.isCoOwnersEnabled() && main.isCoOwnersAllowGuiAccess() && data.isCoOwner(player.getUniqueId()));

        if (!allowed) {
            player.sendMessage("§cJe hebt geen toegang tot dit dier.");
            return;
        }

        plugin.getGuiManager().openAnimalInfo(player, entity, true, coOwnerView, page);
    }

    private void handleMainMenuClick(InventoryClickEvent event,
                                     Player player,
                                     Entity entity,
                                     AnimalData data,
                                     AnimalProfile profile,
                                     AnimalGuiHolder holder) {
        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        MainSettings main = plugin.getConfigManager().getMainSettings();

        int rawSlot = event.getRawSlot();

        if (holder.isOpenedFromAnimalsMenu() && rawSlot == 18) {
            plugin.getGuiManager().openAnimalsList(
                    player,
                    holder.isAnimalsMenuCoOwnerView(),
                    holder.getAnimalsMenuPage()
            );
            return;
        }

        if (rawSlot == gui.getHungerSlot()) {
            if (data.isOwner(player.getUniqueId()) || (main.isCoOwnersEnabled() && main.isCoOwnersAllowFeed() && data.isCoOwner(player.getUniqueId()))) {
                handleFeedClick(player, entity, data, profile, holder);
            }
            return;
        }

        if (rawSlot == gui.getLevelSlot()) {
            if (data.isOwner(player.getUniqueId()) || (main.isCoOwnersEnabled() && main.isCoOwnersAllowDirectUpgrades() && data.isCoOwner(player.getUniqueId()))) {
                handleLevelClick(player, entity, data, profile, holder);
            }
            return;
        }

        if (rawSlot == gui.getHarvestSlot()) {
            if (data.isOwner(player.getUniqueId()) || (main.isCoOwnersEnabled() && main.isCoOwnersAllowHarvest() && data.isCoOwner(player.getUniqueId()))) {
                handleHarvestClick(player, entity, data, profile, holder);
            }
            return;
        }

        if (rawSlot == gui.getCoOwnerManageSlot() && data.isOwner(player.getUniqueId()) && main.isCoOwnersEnabled() && main.isCoOwnerGuiEnabled()) {
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
        }
    }

    private void handleCoOwnerMenuClick(InventoryClickEvent event, Player player, Entity entity, AnimalData data) {
        MainSettings main = plugin.getConfigManager().getMainSettings();
        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (!data.isOwner(player.getUniqueId()) || !main.isCoOwnersEnabled() || !main.isCoOwnerGuiEnabled()) {
            player.closeInventory();
            return;
        }

        int rawSlot = event.getRawSlot();

        if (rawSlot == gui.getCoOwnerBackSlot()) {
            plugin.getGuiManager().openAnimalInfo(player, entity);
            return;
        }

        if (rawSlot == gui.getCoOwnerAddSlot()) {
            int maxCoOwners = main.getEffectiveCoOwnersMax(player);

            if (data.getCoOwnerCount() >= maxCoOwners) {
                lang.send(player, "co-owners.max-reached", Map.of(
                        "max", String.valueOf(maxCoOwners)
                ));
                return;
            }

            plugin.getCoOwnerChatListener().startAddFlow(player, entity.getUniqueId());
            return;
        }

        if (rawSlot == gui.getCoOwnerToggleSlot()) {
            data.setCoOwnersKeepActive(!data.isCoOwnersKeepActive());
            plugin.getDataManager().saveAnimal(data);

            lang.send(player, "co-owners.toggle-updated", Map.of(
                    "status", data.isCoOwnersKeepActive()
                            ? gui.getCoOwnerToggleStatusEnabled()
                            : gui.getCoOwnerToggleStatusDisabled()
            ));

            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        List<UUID> coOwners = new ArrayList<>(data.getCoOwnerUuids());
        List<Integer> slots = gui.getCoOwnerSlots();
        int unlockedSlots = main.getEffectiveCoOwnersMax(player);

        for (int i = 0; i < slots.size(); i++) {
            if (rawSlot != slots.get(i)) {
                continue;
            }

            if (i >= unlockedSlots) {
                return;
            }

            if (i < coOwners.size()) {
                plugin.getGuiManager().openRemoveCoOwnerConfirm(player, entity, coOwners.get(i));
            }
            return;
        }
    }

    private void handleRemoveConfirmClick(InventoryClickEvent event,
                                          Player player,
                                          Entity entity,
                                          AnimalData data,
                                          AnimalGuiHolder holder) {
        MainSettings main = plugin.getConfigManager().getMainSettings();
        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (!data.isOwner(player.getUniqueId()) || !main.isCoOwnersEnabled() || !main.isCoOwnerGuiEnabled()) {
            player.closeInventory();
            return;
        }

        UUID targetUuid = holder.getTargetCoOwnerUuid();
        if (targetUuid == null) {
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        int rawSlot = event.getRawSlot();

        if (rawSlot == gui.getCoOwnerRemoveConfirmYesSlot()) {
            if (data.removeCoOwner(targetUuid)) {
                plugin.getDataManager().saveAnimal(data);

                String targetName = plugin.getServer().getOfflinePlayer(targetUuid).getName();
                if (targetName == null || targetName.isBlank()) {
                    targetName = targetUuid.toString();
                }

                lang.send(player, "co-owners.removed", Map.of(
                        "player", targetName
                ));
            }

            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (rawSlot == gui.getCoOwnerRemoveConfirmNoSlot()) {
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
        }
    }

    private void handleFeedClick(Player player,
                                 Entity entity,
                                 AnimalData data,
                                 AnimalProfile profile,
                                 AnimalGuiHolder holder) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();
        MainSettings main = plugin.getConfigManager().getMainSettings();

        if (!isWithinInteractionRange(player, entity, main)) {
            player.sendMessage(ColorUtil.colorize("&cJe bent te ver van dit dier verwijderd om het te voeden."));
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

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

        int newHunger = Math.min(profile.getMaxHunger(), data.getHunger() + reward.getHungerRestore());
        data.setHunger(newHunger);
        data.setMaxHunger(profile.getMaxHunger());

        int newBond = Math.min(main.getMaxBond(), data.getBond() + reward.getBondGain());
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
        plugin.getGuiManager().openAnimalInfo(
                player,
                entity,
                holder.isOpenedFromAnimalsMenu(),
                holder.isAnimalsMenuCoOwnerView(),
                holder.getAnimalsMenuPage()
        );
    }

    private void handleLevelClick(Player player,
                                  Entity entity,
                                  AnimalData data,
                                  AnimalProfile profile,
                                  AnimalGuiHolder holder) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();
        LevelManager levelManager = plugin.getLevelManager();
        MainSettings main = plugin.getConfigManager().getMainSettings();

        if (!isWithinInteractionRange(player, entity, main)) {
            player.sendMessage(ColorUtil.colorize("&cJe bent te ver van dit dier verwijderd om het te levelen."));
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

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

        int have = levelManager.countMaterial(player, upgrade.getItem());
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

        if (!levelManager.consumeMaterial(player, upgrade.getItem(), upgrade.getAmount())) {
            plugin.getGuiManager().refreshOpenAnimalGui(player, entity);
            return;
        }

        data.setLevel(targetLevel);
        data.setXp(0);

        plugin.getDataManager().saveAnimal(data);

        lang.send(player, "leveling.direct-upgrade-success", Map.of(
                "animal", profile.getDisplayName(),
                "level", String.valueOf(data.getLevel())
        ));

        plugin.getHologramManager().refresh(entity);
        plugin.getGuiManager().openAnimalInfo(
                player,
                entity,
                holder.isOpenedFromAnimalsMenu(),
                holder.isAnimalsMenuCoOwnerView(),
                holder.getAnimalsMenuPage()
        );
    }

    private void handleHarvestClick(Player player,
                                    Entity entity,
                                    AnimalData data,
                                    AnimalProfile profile,
                                    AnimalGuiHolder holder) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        HarvestManager.HarvestResult result = plugin.getHarvestManager().tryHarvest(player, entity, data, profile);

        switch (result) {
            case SUCCESS -> lang.send(player, "harvesting.success", Map.of(
                    "animal", profile.getDisplayName()
            ));
            case NOT_READY -> lang.send(player, "harvesting.not-ready", Map.of(
                    "time", plugin.getHarvestManager().formatTimeLeft(
                            plugin.getHarvestManager().getTimeLeftMillis(data, profile)
                    )
            ));
            case DISABLED -> lang.send(player, "harvesting.disabled");
            case NO_DROPS -> lang.send(player, "harvesting.no-drops");
        }

        plugin.getGuiManager().openAnimalInfo(
                player,
                entity,
                holder.isOpenedFromAnimalsMenu(),
                holder.isAnimalsMenuCoOwnerView(),
                holder.getAnimalsMenuPage()
        );
    }

    private boolean isWithinInteractionRange(Player player, Entity entity, MainSettings settings) {
        if (!settings.isInteractionRangeEnabled()) {
            return true;
        }

        if (player == null || entity == null || player.getWorld() != entity.getWorld()) {
            return false;
        }

        double maxDistance = settings.getFeedAndLevelDistance();
        double maxDistanceSquared = maxDistance * maxDistance;

        return player.getLocation().distanceSquared(entity.getLocation()) <= maxDistanceSquared;
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

    private void consume(ItemStack item, int amount) {
        int newAmount = item.getAmount() - amount;
        item.setAmount(Math.max(newAmount, 0));
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