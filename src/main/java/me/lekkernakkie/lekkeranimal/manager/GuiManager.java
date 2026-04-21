package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.GuiSettings;
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
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import me.lekkernakkie.lekkeranimal.util.ItemsAdderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuiManager {

    private static final int ANIMALS_LIST_ROWS = 6;
    private static final int ANIMALS_LIST_PAGE_SIZE = 28;

    private static final int[] ANIMALS_INFO_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] ANIMALS_LIST_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final Comparator<AnimalData> ANIMALS_LIST_COMPARATOR = Comparator
            .comparing((AnimalData data) -> data.getWorldName() == null ? "" : data.getWorldName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(data -> data.getEntityType().name(), String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(AnimalData::getLevel)
            .reversed();

    private final LekkerAnimal plugin;
    private final NamespacedKey animalsListEntityKey;
    private final NamespacedKey animalsInfoTypeKey;
    private final Map<EntityType, ItemStack> cachedAnimalHeads = new EnumMap<>(EntityType.class);

    public GuiManager(LekkerAnimal plugin) {
        this.plugin = plugin;
        this.animalsListEntityKey = new NamespacedKey(plugin, "animals_list_entity");
        this.animalsInfoTypeKey = new NamespacedKey(plugin, "animals_info_type");
    }

    public void openAnimalsHome(Player player) {
        if (player == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new AnimalsHomeGuiHolder(),
                27,
                ColorUtil.colorize("&bAnimals")
        );

        ItemStack filler = createSimpleItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                " ",
                List.of()
        );

        fillInventory(inventory, filler);

        inventory.setItem(11, createSimpleItem(
                Material.BOOK,
                "&bInformatie",
                List.of(
                        "&7Bekijk per dier:",
                        "&7- Bond food",
                        "&7- Feed items",
                        "&7- Max level",
                        "&7- Level costs"
                )
        ));

        inventory.setItem(15, createSimpleItem(
                Material.LEAD,
                "&bMijn Animals",
                List.of(
                        "&7Bekijk jouw dieren",
                        "&7en je co-owner dieren"
                )
        ));

        player.openInventory(inventory);
    }

    public void openAnimalsInfoList(Player player) {
        if (player == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new AnimalsInfoListGuiHolder(),
                27,
                ColorUtil.colorize("&bAnimals Informatie")
        );

        ItemStack filler = createSimpleItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                " ",
                List.of()
        );

        fillInventory(inventory, filler);

        List<AnimalProfile> profiles = plugin.getConfigManager().getAnimalsSettings().getProfiles().values().stream()
                .filter(AnimalProfile::isEnabled)
                .sorted(Comparator.comparing(AnimalProfile::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        int index = 0;
        for (AnimalProfile profile : profiles) {
            if (index >= ANIMALS_INFO_SLOTS.length) {
                break;
            }

            inventory.setItem(ANIMALS_INFO_SLOTS[index++], createAnimalInfoMenuItem(profile));
        }

        inventory.setItem(22, createSimpleItem(
                Material.ARROW,
                "&cTerug",
                List.of("&7Terug naar het hoofdmenu")
        ));

        player.openInventory(inventory);
    }

    public void openAnimalInfoDetail(Player player, EntityType entityType) {
        if (player == null || entityType == null) {
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entityType);
        if (profile == null || !profile.isEnabled()) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new AnimalInfoDetailGuiHolder(entityType.name()),
                45,
                ColorUtil.colorize("&bInfo &8• &f" + profile.getDisplayName())
        );

        ItemStack filler = createSimpleItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                " ",
                List.of()
        );

        fillInventory(inventory, filler);

        inventory.setItem(13, createAnimalDisplayHead(profile));

        List<String> bondLore = new ArrayList<>();
        bondLore.add("&7Item: &f" + formatMaterial(profile.getBondItem()));
        bondLore.add("&7Hoeveelheid: &f" + profile.getRequiredBondAmount());
        bondLore.add("&7Max level: &f" + profile.getMaxLevel());

        inventory.setItem(20, createSimpleItem(
                Material.LEAD,
                "&bBond Informatie",
                bondLore
        ));

        List<String> feedLore = new ArrayList<>();
        if (profile.getFeedingRewards().isEmpty()) {
            feedLore.add("&7Geen feed items ingesteld.");
        } else {
            for (Map.Entry<Material, FeedingReward> entry : profile.getFeedingRewards().entrySet()) {
                FeedingReward reward = entry.getValue();
                feedLore.add("&f" + formatMaterial(entry.getKey())
                        + " &8- &7Hunger: &f" + reward.getHungerRestore()
                        + " &8| &7XP: &f" + reward.getXp()
                        + " &8| &7Bond: &f" + reward.getBondGain());
            }
        }

        inventory.setItem(22, createSimpleItem(
                Material.GOLDEN_CARROT,
                "&bFeed Items",
                feedLore
        ));

        List<String> levelLore = new ArrayList<>();
        levelLore.add("&7Max level: &f" + profile.getMaxLevel());
        levelLore.add("");

        if (profile.getDirectLevelUpgrades().isEmpty()) {
            levelLore.add("&7Geen directe upgrades ingesteld.");
        } else {
            List<Integer> levels = new ArrayList<>(profile.getDirectLevelUpgrades().keySet());
            levels.sort(Integer::compareTo);

            for (int level : levels) {
                DirectLevelUpgrade upgrade = profile.getDirectLevelUpgrade(level);
                if (upgrade == null) {
                    continue;
                }

                levelLore.add("&7Level &f" + level + " &8→ &f"
                        + formatMaterial(upgrade.getItem()) + " x" + upgrade.getAmount());
            }
        }

        inventory.setItem(24, createSimpleItem(
                Material.EXPERIENCE_BOTTLE,
                "&bLevel Kosten",
                levelLore
        ));

        inventory.setItem(36, createSimpleItem(
                Material.ARROW,
                "&cTerug",
                List.of("&7Terug naar diereninformatie")
        ));

        player.openInventory(inventory);
    }

    public void openAnimalInfo(Player player, Entity entity) {
        openAnimalInfo(player, entity, false, false, 0);
    }

    public void openAnimalInfo(Player player,
                               Entity entity,
                               boolean openedFromAnimalsMenu,
                               boolean animalsMenuCoOwnerView,
                               int animalsMenuPage) {
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
                new AnimalGuiHolder(
                        entity.getUniqueId(),
                        AnimalGuiHolder.ScreenType.MAIN,
                        null,
                        openedFromAnimalsMenu,
                        animalsMenuCoOwnerView,
                        animalsMenuPage
                ),
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
            fillInventory(inventory, filler);
        }

        inventory.setItem(gui.getOwnerSlot(), createOwnerItem(data, profile, gui));
        inventory.setItem(gui.getHungerSlot(), createHungerItem(data, profile, gui));
        inventory.setItem(gui.getLevelSlot(), createLevelItem(data, profile, gui));
        inventory.setItem(gui.getHarvestSlot(), createHarvestItem(data, profile, gui));

        if (main.isCoOwnersEnabled() && main.isCoOwnerGuiEnabled()) {
            inventory.setItem(gui.getCoOwnerManageSlot(), createCoOwnerManageItem(data, gui));
        }

        if (openedFromAnimalsMenu) {
            inventory.setItem(18, createSimpleItem(
                    Material.ARROW,
                    "&cTerug",
                    List.of("&7Terug naar jouw dieren")
            ));
        }

        player.openInventory(inventory);
    }

    public void openAnimalsList(Player player, boolean coOwnerView, int page) {
        if (player == null) {
            return;
        }

        List<AnimalData> source = collectAnimalsForPlayer(player, coOwnerView);
        source.sort(ANIMALS_LIST_COMPARATOR);

        int maxPage = Math.max(0, (source.size() - 1) / ANIMALS_LIST_PAGE_SIZE);
        int currentPage = Math.max(0, Math.min(page, maxPage));

        String title = coOwnerView
                ? "&bJouw Co-owner Dieren &8• &fPagina " + (currentPage + 1)
                : "&bJouw Dieren &8• &fPagina " + (currentPage + 1);

        Inventory inventory = Bukkit.createInventory(
                new AnimalsListGuiHolder(coOwnerView, currentPage),
                ANIMALS_LIST_ROWS * 9,
                ColorUtil.colorize(title)
        );

        ItemStack filler = createSimpleItem(
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                " ",
                List.of()
        );
        fillInventory(inventory, filler);

        inventory.setItem(45, createTabItem(!coOwnerView, "&bMijn dieren", Material.LEAD));
        inventory.setItem(46, createTabItem(coOwnerView, "&dCo-owner dieren", Material.NAME_TAG));

        if (currentPage > 0) {
            inventory.setItem(48, createSimpleItem(
                    Material.ARROW,
                    "&bVorige pagina",
                    List.of("&7Ga naar de vorige pagina")
            ));
        }

        if (currentPage < maxPage) {
            inventory.setItem(50, createSimpleItem(
                    Material.ARROW,
                    "&bVolgende pagina",
                    List.of("&7Ga naar de volgende pagina")
            ));
        }

        inventory.setItem(49, createSimpleItem(
                Material.BOOK,
                coOwnerView ? "&dCo-owner dieren" : "&bMijn dieren",
                List.of(
                        "&7Totaal: &f" + source.size(),
                        "&7Pagina: &f" + (currentPage + 1) + "&7/&f" + (maxPage + 1)
                )
        ));

        int start = currentPage * ANIMALS_LIST_PAGE_SIZE;
        int end = Math.min(source.size(), start + ANIMALS_LIST_PAGE_SIZE);

        int slotIndex = 0;
        for (int i = start; i < end; i++) {
            AnimalData data = source.get(i);
            inventory.setItem(ANIMALS_LIST_SLOTS[slotIndex++], createAnimalListItem(data, coOwnerView));
        }

        player.openInventory(inventory);
    }

    public void openCoOwnerMenu(Player player, Entity entity) {
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
            fillInventory(inventory, filler);
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

    public void openRemoveCoOwnerConfirm(Player player, Entity entity, UUID targetCoOwnerUuid) {
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
            fillInventory(inventory, filler);
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

    public void refreshOpenAnimalGui(Player player, Entity entity) {
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
            case MAIN -> openAnimalInfo(
                    player,
                    entity,
                    holder.isOpenedFromAnimalsMenu(),
                    holder.isAnimalsMenuCoOwnerView(),
                    holder.getAnimalsMenuPage()
            );
            case CO_OWNERS -> openCoOwnerMenu(player, entity);
            case REMOVE_CO_OWNER_CONFIRM -> {
                if (holder.getTargetCoOwnerUuid() != null) {
                    openRemoveCoOwnerConfirm(player, entity, holder.getTargetCoOwnerUuid());
                }
            }
        }
    }

    private List<AnimalData> collectAnimalsForPlayer(Player player, boolean coOwnerView) {
        List<AnimalData> source = new ArrayList<>();

        for (AnimalData data : plugin.getAnimalManager().getAllBondedAnimals()) {
            if (coOwnerView) {
                if (data.isCoOwner(player.getUniqueId())) {
                    source.add(data);
                }
            } else {
                if (data.isOwner(player.getUniqueId())) {
                    source.add(data);
                }
            }
        }

        for (AnimalData data : plugin.getAnimalManager().getAllPendingAnimals()) {
            if (coOwnerView) {
                if (data.isCoOwner(player.getUniqueId())) {
                    source.add(data);
                }
            } else {
                if (data.isOwner(player.getUniqueId())) {
                    source.add(data);
                }
            }
        }

        return source;
    }

    private ItemStack createAnimalInfoMenuItem(AnimalProfile profile) {
        ItemStack item = getCachedAnimalHead(profile.getEntityType()).clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize("&b" + profile.getDisplayName()));
            meta.setLore(colorizeList(List.of(
                    "&7Klik om alle info te bekijken"
            )));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(
                    animalsInfoTypeKey,
                    PersistentDataType.STRING,
                    profile.getEntityType().name()
            );
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAnimalDisplayHead(AnimalProfile profile) {
        ItemStack item = getCachedAnimalHead(profile.getEntityType()).clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize("&b" + profile.getDisplayName()));
            meta.setLore(List.of());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAnimalListItem(AnimalData data, boolean coOwnerView) {
        Entity entity = plugin.getServer().getEntity(data.getEntityUuid());
        boolean loaded = entity != null && entity.isValid() && !entity.isDead();

        String ownerName = data.getOwnerName() != null && !data.getOwnerName().isBlank()
                ? data.getOwnerName()
                : "Unknown";
        String world = data.getWorldName() != null ? data.getWorldName() : "Onbekend";
        String coords = (int) Math.floor(data.getX()) + ", " + (int) Math.floor(data.getY()) + ", " + (int) Math.floor(data.getZ());
        String animalDisplay = formatMaterialFromEntityType(data.getEntityType().name());
        String displayName = (coOwnerView ? "&d" : "&b") + animalDisplay;

        List<String> lore = new ArrayList<>();
        lore.add("&7Type: &f" + animalDisplay);
        lore.add("&7Rol: " + (coOwnerView ? "&dCo-owner" : "&bEigenaar"));
        lore.add("&7Level: &f" + data.getLevel());
        lore.add("&7Honger: &f" + data.getHunger() + "&7/&f" + data.getMaxHunger());
        lore.add("&7Wereld: &f" + world);
        lore.add("&7Locatie: &f" + coords);
        lore.add("&7Status: " + (loaded ? "&aGeladen" : "&cNiet geladen"));
        lore.add("&7Owner: &f" + ownerName);
        lore.add("");
        lore.add(loaded ? "&aKlik om te openen" : "&cDit dier is momenteel niet geladen");

        ItemStack item = getCachedAnimalHead(data.getEntityType()).clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(displayName));
            meta.setLore(colorizeList(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(
                    animalsListEntityKey,
                    PersistentDataType.STRING,
                    data.getEntityUuid().toString()
            );
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack getCachedAnimalHead(EntityType entityType) {
        ItemStack cached = cachedAnimalHeads.get(entityType);
        if (cached != null) {
            return cached;
        }

        String headOwner = getHeadOwnerForType(entityType);
        String animalName = formatMaterialFromEntityType(entityType.name());

        ItemStack built;
        if (!headOwner.isBlank()) {
            built = plugin.getHarvestManager().createPersistentHeadItem(
                    "COMMON",
                    entityType.name(),
                    animalName,
                    headOwner,
                    ""
            );
        } else {
            built = new ItemStack(getFallbackEntityIcon(entityType));
        }

        ItemMeta meta = built.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            built.setItemMeta(meta);
        }

        cachedAnimalHeads.put(entityType, built);
        return built;
    }

    private String getHeadOwnerForType(EntityType entityType) {
        if (entityType == null) {
            return "";
        }

        return switch (entityType) {
            case COW -> "MHF_Cow";
            case PIG -> "MHF_Pig";
            case CHICKEN -> "MHF_Chicken";
            case SHEEP -> "MHF_Sheep";
            case MOOSHROOM -> "MHF_MushroomCow";
            default -> "";
        };
    }

    private Material getFallbackEntityIcon(EntityType entityType) {
        if (entityType == null) {
            return Material.WHEAT;
        }

        try {
            return Material.valueOf(entityType.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException ignored) {
        }

        return switch (entityType) {
            case COW, MOOSHROOM -> Material.HAY_BLOCK;
            case PIG -> Material.CARROT;
            case CHICKEN -> Material.EGG;
            case SHEEP -> Material.WHITE_WOOL;
            default -> Material.WHEAT;
        };
    }

    private ItemStack createTabItem(boolean selected, String name, Material material) {
        List<String> lore = new ArrayList<>();
        lore.add(selected ? "&aGeselecteerd" : "&7Klik om te openen");
        return createSimpleItem(material, name, lore);
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

    private ItemStack createSimpleItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.colorize(name));
        meta.setLore(colorizeList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillInventory(Inventory inventory, ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item);
        }
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

    private String formatMaterialFromEntityType(String entityTypeName) {
        String[] parts = entityTypeName.toLowerCase().split("_");
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