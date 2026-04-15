package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.GuiSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.gui.AnimalGuiHolder;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import me.lekkernakkie.lekkeranimal.util.ItemsAdderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuiManager {

    private final LekkerAnimal plugin;

    public GuiManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void openAnimalInfo(org.bukkit.entity.Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            return;
        }

        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        MainSettings main = plugin.getConfigManager().getMainSettings();

        if (!gui.isAnimalInfoEnabled()) {
            return;
        }

        String title = ColorUtil.colorize(
                gui.getAnimalInfoTitle().replace("{animal}", profile.getDisplayName())
        );

        Inventory inventory = Bukkit.createInventory(
                new AnimalGuiHolder(entity.getUniqueId(), AnimalGuiHolder.ScreenType.MAIN),
                gui.getAnimalInfoRows() * 9,
                title
        );

        if (gui.isFillerEnabled()) {
            ItemStack filler = createItem(
                    gui.getFillerItemId(),
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    gui.getFillerName(),
                    List.of()
            );
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        inventory.setItem(gui.getOwnerSlot(), createOwnerItem(data, profile, gui));
        inventory.setItem(gui.getHungerSlot(), createHungerItem(data, profile, gui));
        inventory.setItem(gui.getLevelSlot(), createLevelItem(data, profile, gui));
        inventory.setItem(gui.getHarvestSlot(), createHarvestItem(data, profile, gui));

        if (main.isCoOwnersEnabled() && main.isCoOwnerGuiEnabled()) {
            inventory.setItem(gui.getCoOwnerManageSlot(), createCoOwnerManageItem(data, gui));
        }

        player.openInventory(inventory);
    }

    public void openCoOwnerMenu(org.bukkit.entity.Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            return;
        }

        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        MainSettings main = plugin.getConfigManager().getMainSettings();

        if (!main.isCoOwnersEnabled() || !main.isCoOwnerGuiEnabled()) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new AnimalGuiHolder(entity.getUniqueId(), AnimalGuiHolder.ScreenType.CO_OWNERS),
                gui.getCoOwnerRows() * 9,
                ColorUtil.colorize(gui.getCoOwnerTitle())
        );

        if (gui.isCoOwnerFillerEnabled()) {
            ItemStack filler = createItem(
                    gui.getCoOwnerFillerItemId(),
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    gui.getCoOwnerFillerName(),
                    List.of()
            );
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        List<UUID> coOwners = new ArrayList<>(data.getCoOwnerUuids());
        List<Integer> slots = gui.getCoOwnerSlots();
        int unlockedSlots = main.getEffectiveCoOwnersMax(player);

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);

            if (i >= unlockedSlots) {
                inventory.setItem(slot, createLockedCoOwnerItem(gui, i + 1));
                continue;
            }

            if (i < coOwners.size()) {
                inventory.setItem(slot, createCoOwnerHead(coOwners.get(i), gui));
            } else {
                inventory.setItem(slot, createItem(
                        gui.getEmptyCoOwnerItemId(),
                        Material.GRAY_DYE,
                        gui.getEmptyCoOwnerName(),
                        applyPlaceholders(
                                gui.getEmptyCoOwnerLore(),
                                "{slot}", String.valueOf(i + 1)
                        )
                ));
            }
        }

        inventory.setItem(gui.getCoOwnerAddSlot(), createItem(
                gui.getCoOwnerAddItemId(),
                Material.LIME_WOOL,
                gui.getCoOwnerAddName(),
                applyPlaceholders(
                        gui.getCoOwnerAddLore(),
                        "{current}", String.valueOf(data.getCoOwnerCount()),
                        "{max}", String.valueOf(unlockedSlots)
                )
        ));

        inventory.setItem(gui.getCoOwnerBackSlot(), createItem(
                gui.getCoOwnerBackItemId(),
                Material.BARRIER,
                gui.getCoOwnerBackName(),
                gui.getCoOwnerBackLore()
        ));

        inventory.setItem(gui.getCoOwnerToggleSlot(), createCoOwnerActiveToggleItem(data, gui));

        player.openInventory(inventory);
    }

    public void openRemoveCoOwnerConfirm(org.bukkit.entity.Player player, Entity entity, UUID targetCoOwnerUuid) {
        if (player == null || entity == null || targetCoOwnerUuid == null) {
            return;
        }

        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetCoOwnerUuid);
        String targetName = target.getName() != null ? target.getName() : targetCoOwnerUuid.toString();

        Inventory inventory = Bukkit.createInventory(
                new AnimalGuiHolder(entity.getUniqueId(), AnimalGuiHolder.ScreenType.REMOVE_CO_OWNER_CONFIRM, targetCoOwnerUuid),
                gui.getCoOwnerRemoveConfirmRows() * 9,
                ColorUtil.colorize(gui.getCoOwnerRemoveConfirmTitle().replace("{player}", targetName))
        );

        if (gui.isCoOwnerFillerEnabled()) {
            ItemStack filler = createItem(
                    gui.getCoOwnerFillerItemId(),
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    gui.getCoOwnerFillerName(),
                    List.of()
            );
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        inventory.setItem(gui.getCoOwnerRemoveConfirmTargetSlot(), createCoOwnerHead(targetCoOwnerUuid, gui));
        inventory.setItem(gui.getCoOwnerRemoveConfirmYesSlot(), createItem(
                gui.getCoOwnerRemoveConfirmYesItemId(),
                Material.LIME_WOOL,
                gui.getCoOwnerRemoveConfirmYesName(),
                applyPlaceholders(gui.getCoOwnerRemoveConfirmYesLore(), "{player}", targetName)
        ));
        inventory.setItem(gui.getCoOwnerRemoveConfirmNoSlot(), createItem(
                gui.getCoOwnerRemoveConfirmNoItemId(),
                Material.RED_WOOL,
                gui.getCoOwnerRemoveConfirmNoName(),
                applyPlaceholders(gui.getCoOwnerRemoveConfirmNoLore(), "{player}", targetName)
        ));

        player.openInventory(inventory);
    }

    public void refreshOpenAnimalGui(org.bukkit.entity.Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }

        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof AnimalGuiHolder holder)) {
            return;
        }

        if (!holder.getEntityUuid().equals(entity.getUniqueId())) {
            return;
        }

        switch (holder.getScreenType()) {
            case MAIN -> openAnimalInfo(player, entity);
            case CO_OWNERS -> openCoOwnerMenu(player, entity);
            case REMOVE_CO_OWNER_CONFIRM -> {
                if (holder.getTargetCoOwnerUuid() != null) {
                    openRemoveCoOwnerConfirm(player, entity, holder.getTargetCoOwnerUuid());
                }
            }
        }
    }

    private ItemStack createOwnerItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        String itemId = gui.getOwnerItemId();

        if ("PLAYER_HEAD".equalsIgnoreCase(itemId)) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);

            if (item.getItemMeta() instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getOwnerUuid()));
                skullMeta.setDisplayName(ColorUtil.colorize(gui.getOwnerName()));
                skullMeta.setLore(colorizeList(applyPlaceholders(
                        gui.getOwnerLore(),
                        "{owner}", data.getOwnerName(),
                        "{animal_type}", profile.getEntityType().name(),
                        "{animal}", profile.getDisplayName()
                )));
                skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(skullMeta);
            }

            return item;
        }

        return createItem(
                itemId,
                gui.getOwnerMaterial(),
                gui.getOwnerName(),
                applyPlaceholders(
                        gui.getOwnerLore(),
                        "{owner}", data.getOwnerName(),
                        "{animal_type}", profile.getEntityType().name(),
                        "{animal}", profile.getDisplayName()
                )
        );
    }

    private ItemStack createHungerItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        int percent = profile.getMaxHunger() <= 0
                ? 0
                : (int) Math.round((data.getHunger() * 100.0) / profile.getMaxHunger());

        return createItem(
                gui.getHungerItemId(),
                gui.getHungerMaterial(),
                gui.getHungerName(),
                applyPlaceholders(
                        gui.getHungerLore(),
                        "{hunger}", String.valueOf(data.getHunger()),
                        "{max_hunger}", String.valueOf(profile.getMaxHunger()),
                        "{hunger_percent}", String.valueOf(percent),
                        "{hunger_bar}", createHungerBar(
                                data.getHunger(),
                                profile.getMaxHunger(),
                                gui.getHungerBarLength(),
                                gui.getHungerBarEmptyColor(),
                                gui.getHungerBarSymbol()
                        )
                )
        );
    }

    private ItemStack createLevelItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        int requiredXp = plugin.getLevelManager().getRequiredXpForNextLevel(data.getLevel());
        int nextLevel = data.getLevel() + 1;
        DirectLevelUpgrade upgrade = profile.getDirectLevelUpgrade(nextLevel);

        String nextLevelDisplay;
        String nextUpgradeItem;
        String nextUpgradeAmount;
        String nextUpgradeStatus;

        if (data.getLevel() >= profile.getMaxLevel()) {
            nextLevelDisplay = "MAX";
            nextUpgradeItem = "-";
            nextUpgradeAmount = "-";
            nextUpgradeStatus = ColorUtil.colorize("&aMaximum level bereikt");
        } else if (upgrade == null) {
            nextLevelDisplay = String.valueOf(nextLevel);
            nextUpgradeItem = "-";
            nextUpgradeAmount = "-";
            nextUpgradeStatus = ColorUtil.colorize("&cGeen directe upgrade ingesteld");
        } else {
            nextLevelDisplay = String.valueOf(nextLevel);
            nextUpgradeItem = formatMaterial(upgrade.getItem());
            nextUpgradeAmount = String.valueOf(upgrade.getAmount());
            nextUpgradeStatus = ColorUtil.colorize("&eKlik om direct te upgraden");
        }

        return createItem(
                gui.getLevelItemId(),
                gui.getLevelMaterial(),
                gui.getLevelName().replace("{level}", String.valueOf(data.getLevel())),
                applyPlaceholders(
                        gui.getLevelLore(),
                        "{level}", String.valueOf(data.getLevel()),
                        "{xp}", String.valueOf(data.getXp()),
                        "{required_xp}", String.valueOf(requiredXp),
                        "{xp_bar}", createBar(
                                data.getXp(),
                                requiredXp,
                                gui.getXpBarLength(),
                                gui.getXpBarFullColor(),
                                gui.getXpBarEmptyColor(),
                                gui.getXpBarSymbol()
                        ),
                        "{next_level}", nextLevelDisplay,
                        "{next_upgrade_item}", nextUpgradeItem,
                        "{next_upgrade_amount}", nextUpgradeAmount,
                        "{next_upgrade_status}", nextUpgradeStatus
                )
        );
    }

    private ItemStack createHarvestItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        boolean ready = plugin.getHarvestManager().isReady(data, profile);

        String status = ready
                ? ColorUtil.colorize("&aKlaar om te harvesten")
                : ColorUtil.colorize("&eNog niet klaar");

        String timeLeft = ready
                ? ColorUtil.colorize("&aNu")
                : ColorUtil.colorize("&f" + plugin.getHarvestManager().formatTimeLeft(
                plugin.getHarvestManager().getTimeLeftMillis(data, profile)
        ));

        String preview = plugin.getHarvestManager().getPreview(profile, data.getLevel());

        List<String> lore = applyPlaceholders(
                gui.getHarvestLore(),
                "{harvest_status}", status,
                "{harvest_time_left}", timeLeft,
                "{harvest_progress_percent}", String.valueOf(plugin.getHarvestManager().getProgressPercent(data, profile))
        );

        lore = expandMultilinePlaceholder(lore, "{harvest_preview}", preview);

        return createItem(
                gui.getHarvestItemId(),
                gui.getHarvestMaterial(),
                gui.getHarvestName(),
                lore
        );
    }

    private ItemStack createCoOwnerManageItem(AnimalData data, GuiSettings gui) {
        String names = data.getCoOwnerUuids().stream()
                .map(Bukkit::getOfflinePlayer)
                .map(player -> player.getName() != null ? player.getName() : player.getUniqueId().toString())
                .collect(Collectors.joining(", "));

        if (names.isBlank()) {
            names = "Geen";
        }

        return createItem(
                gui.getCoOwnerManageItemId(),
                gui.getCoOwnerManageMaterial(),
                gui.getCoOwnerManageName(),
                applyPlaceholders(
                        gui.getCoOwnerManageLore(),
                        "{count}", String.valueOf(data.getCoOwnerCount()),
                        "{list}", names
                )
        );
    }

    private ItemStack createCoOwnerHead(UUID uuid, GuiSettings gui) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta baseMeta = item.getItemMeta();
        if (!(baseMeta instanceof SkullMeta meta)) {
            return item;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
        String name = target.getName() != null ? target.getName() : uuid.toString();

        meta.setOwningPlayer(target);
        meta.setDisplayName(ColorUtil.colorize(gui.getCoOwnerHeadName().replace("{player}", name)));
        meta.setLore(colorizeList(applyPlaceholders(gui.getCoOwnerHeadLore(), "{player}", name)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLockedCoOwnerItem(GuiSettings gui, int slotNumber) {
        return createItem(
                gui.getLockedCoOwnerItemId(),
                gui.getLockedCoOwnerMaterial(),
                gui.getLockedCoOwnerName(),
                applyPlaceholders(
                        gui.getLockedCoOwnerLore(),
                        "{slot}", String.valueOf(slotNumber)
                )
        );
    }

    private ItemStack createCoOwnerActiveToggleItem(AnimalData data, GuiSettings gui) {
        String status = data.isCoOwnersKeepActive()
                ? ColorUtil.colorize(gui.getCoOwnerToggleStatusEnabled())
                : ColorUtil.colorize(gui.getCoOwnerToggleStatusDisabled());

        return createItem(
                gui.getCoOwnerToggleItemId(),
                gui.getCoOwnerToggleMaterial(),
                gui.getCoOwnerToggleName().replace("{status}", status),
                applyPlaceholders(gui.getCoOwnerToggleLore(), "{status}", status)
        );
    }

    private ItemStack createItem(String idOrMaterial, Material fallback, String name, List<String> lore) {
        ItemStack item = ItemsAdderUtil.createGuiItem(idOrMaterial, fallback, name, lore);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material.name(), material, name, lore);
    }

    private List<String> applyPlaceholders(List<String> input, String... replacements) {
        List<String> result = new ArrayList<>();

        for (String line : input) {
            String value = line;
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                value = value.replace(replacements[i], replacements[i + 1]);
            }
            result.add(value);
        }

        return result;
    }

    private List<String> expandMultilinePlaceholder(List<String> input, String placeholder, String value) {
        List<String> result = new ArrayList<>();
        String[] split = value != null ? value.split("\n") : new String[0];

        for (String line : input) {
            if (!line.contains(placeholder)) {
                result.add(line);
                continue;
            }

            if (split.length == 0) {
                result.add(line.replace(placeholder, ""));
                continue;
            }

            for (String part : split) {
                result.add(line.replace(placeholder, part));
            }
        }

        return result;
    }

    private List<String> colorizeList(List<String> input) {
        List<String> result = new ArrayList<>();
        for (String line : input) {
            result.add(ColorUtil.colorize(line));
        }
        return result;
    }

    private String createHungerBar(int current, int max, int length, String emptyColor, String symbol) {
        if (max <= 0) {
            return ColorUtil.colorize(emptyColor + symbol.repeat(length));
        }

        double progress = Math.max(0.0, Math.min(1.0, current / (double) max));
        int filled = (int) Math.round(progress * length);

        String fullColor;
        if (progress <= 0.33) {
            fullColor = "&c";
        } else if (progress <= 0.66) {
            fullColor = "&6";
        } else {
            fullColor = "&a";
        }

        return ColorUtil.colorize(
                fullColor + symbol.repeat(filled) +
                        emptyColor + symbol.repeat(Math.max(0, length - filled))
        );
    }

    private String createBar(int current, int max, int length, String fullColor, String emptyColor, String symbol) {
        if (max <= 0) {
            return ColorUtil.colorize(emptyColor + symbol.repeat(length));
        }

        double progress = Math.max(0.0, Math.min(1.0, current / (double) max));
        int filled = (int) Math.round(progress * length);

        return ColorUtil.colorize(
                fullColor + symbol.repeat(filled) +
                        emptyColor + symbol.repeat(Math.max(0, length - filled))
        );
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