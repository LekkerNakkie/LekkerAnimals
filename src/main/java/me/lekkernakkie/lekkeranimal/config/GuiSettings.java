package me.lekkernakkie.lekkeranimal.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class GuiSettings {

    private final FileConfiguration config;

    public GuiSettings(FileConfiguration config) {
        this.config = config;
    }

    // ----------------------------------------------------------------
    // Existing animal info GUI
    // ----------------------------------------------------------------

    public boolean isAnimalInfoEnabled() {
        return config.getBoolean("gui.animal-info.enabled", true);
    }

    public String getAnimalInfoTitle() {
        return config.getString("gui.animal-info.title", "&bAnimal Info &8• &f{animal}");
    }

    public int getAnimalInfoRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.animal-info.rows", 3)));
    }

    public boolean isFillerEnabled() {
        return config.getBoolean("gui.animal-info.filler.enabled", true);
    }

    public String getFillerItemId() {
        return config.getString("gui.animal-info.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
    }

    public Material getFillerMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    public String getFillerName() {
        return config.getString("gui.animal-info.filler.name", " ");
    }

    public int getOwnerSlot() {
        return config.getInt("gui.animal-info.items.owner.slot", 10);
    }

    public String getOwnerItemId() {
        return config.getString("gui.animal-info.items.owner.material", "PLAYER_HEAD");
    }

    public Material getOwnerMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.items.owner.material", "PLAYER_HEAD"),
                Material.PLAYER_HEAD
        );
    }

    public String getOwnerName() {
        return config.getString("gui.animal-info.items.owner.name", "&bEigenaar");
    }

    public List<String> getOwnerLore() {
        return config.getStringList("gui.animal-info.items.owner.lore");
    }

    public int getHungerSlot() {
        return config.getInt("gui.animal-info.items.hunger.slot", 12);
    }

    public String getHungerItemId() {
        return config.getString("gui.animal-info.items.hunger.material", "GOLDEN_CARROT");
    }

    public Material getHungerMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.items.hunger.material", "GOLDEN_CARROT"),
                Material.GOLDEN_CARROT
        );
    }

    public String getHungerName() {
        return config.getString("gui.animal-info.items.hunger.name", "&bHonger");
    }

    public List<String> getHungerLore() {
        return config.getStringList("gui.animal-info.items.hunger.lore");
    }

    public int getLevelSlot() {
        return config.getInt("gui.animal-info.items.level.slot", 14);
    }

    public String getLevelItemId() {
        return config.getString("gui.animal-info.items.level.material", "EXPERIENCE_BOTTLE");
    }

    public Material getLevelMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.items.level.material", "EXPERIENCE_BOTTLE"),
                Material.EXPERIENCE_BOTTLE
        );
    }

    public String getLevelName() {
        return config.getString("gui.animal-info.items.level.name", "&bLevel &f{level}");
    }

    public List<String> getLevelLore() {
        return config.getStringList("gui.animal-info.items.level.lore");
    }

    public int getHarvestSlot() {
        return config.getInt("gui.animal-info.items.harvest.slot", 16);
    }

    public String getHarvestItemId() {
        return config.getString("gui.animal-info.items.harvest.material", "CHEST");
    }

    public Material getHarvestMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.items.harvest.material", "CHEST"),
                Material.CHEST
        );
    }

    public String getHarvestName() {
        return config.getString("gui.animal-info.items.harvest.name", "&bHarvest");
    }

    public List<String> getHarvestLore() {
        return config.getStringList("gui.animal-info.items.harvest.lore");
    }

    public int getCoOwnerManageSlot() {
        return config.getInt("gui.animal-info.items.co-owners.slot", 22);
    }

    public String getCoOwnerManageItemId() {
        return config.getString("gui.animal-info.items.co-owners.material", "NAME_TAG");
    }

    public Material getCoOwnerManageMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.items.co-owners.material", "NAME_TAG"),
                Material.NAME_TAG
        );
    }

    public String getCoOwnerManageName() {
        return config.getString("gui.animal-info.items.co-owners.name", "&bMede-eigenaars");
    }

    public List<String> getCoOwnerManageLore() {
        return config.getStringList("gui.animal-info.items.co-owners.lore");
    }

    public int getAnimalInfoBackSlot() {
        return config.getInt("gui.animal-info.items.back.slot", 18);
    }

    public String getAnimalInfoBackItemId() {
        return config.getString("gui.animal-info.items.back.material", "ARROW");
    }

    public Material getAnimalInfoBackMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info.items.back.material", "ARROW"),
                Material.ARROW
        );
    }

    public String getAnimalInfoBackName() {
        return config.getString("gui.animal-info.items.back.name", "&cTerug");
    }

    public List<String> getAnimalInfoBackLore() {
        return config.getStringList("gui.animal-info.items.back.lore");
    }

    // ----------------------------------------------------------------
    // Co-owner menu
    // ----------------------------------------------------------------

    public int getCoOwnerRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.co-owner-menu.rows", 3)));
    }

    public String getCoOwnerTitle() {
        return config.getString("gui.co-owner-menu.title", "&bMede-eigenaars");
    }

    public boolean isCoOwnerFillerEnabled() {
        return config.getBoolean("gui.co-owner-menu.filler.enabled", true);
    }

    public String getCoOwnerFillerItemId() {
        return config.getString("gui.co-owner-menu.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
    }

    public Material getCoOwnerFillerMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-menu.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    public String getCoOwnerFillerName() {
        return config.getString("gui.co-owner-menu.filler.name", " ");
    }

    public List<Integer> getCoOwnerSlots() {
        List<Integer> result = new ArrayList<>();
        List<String> raw = config.getStringList("gui.co-owner-menu.items.co-owner-slots");

        for (String value : raw) {
            try {
                result.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }

        if (result.isEmpty()) {
            result.add(11);
            result.add(13);
            result.add(15);
        }

        return result;
    }

    public String getEmptyCoOwnerItemId() {
        return config.getString("gui.co-owner-menu.items.empty-slot.material", "GRAY_DYE");
    }

    public Material getEmptyCoOwnerMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-menu.items.empty-slot.material", "GRAY_DYE"),
                Material.GRAY_DYE
        );
    }

    public String getEmptyCoOwnerName() {
        return config.getString("gui.co-owner-menu.items.empty-slot.name", "&7Leeg slot");
    }

    public List<String> getEmptyCoOwnerLore() {
        return config.getStringList("gui.co-owner-menu.items.empty-slot.lore");
    }

    public String getLockedCoOwnerItemId() {
        return config.getString("gui.co-owner-menu.items.locked-slot.material", "RED_STAINED_GLASS_PANE");
    }

    public Material getLockedCoOwnerMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-menu.items.locked-slot.material", "RED_STAINED_GLASS_PANE"),
                Material.RED_STAINED_GLASS_PANE
        );
    }

    public String getLockedCoOwnerName() {
        return config.getString("gui.co-owner-menu.items.locked-slot.name", "&cVergrendeld slot");
    }

    public List<String> getLockedCoOwnerLore() {
        return config.getStringList("gui.co-owner-menu.items.locked-slot.lore");
    }

    public String getCoOwnerHeadName() {
        return config.getString("gui.co-owner-menu.items.co-owner-head.name", "&b{player}");
    }

    public List<String> getCoOwnerHeadLore() {
        return config.getStringList("gui.co-owner-menu.items.co-owner-head.lore");
    }

    public int getCoOwnerAddSlot() {
        return config.getInt("gui.co-owner-menu.items.add.slot", 22);
    }

    public String getCoOwnerAddItemId() {
        return config.getString("gui.co-owner-menu.items.add.material", "LIME_WOOL");
    }

    public Material getCoOwnerAddMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-menu.items.add.material", "LIME_WOOL"),
                Material.LIME_WOOL
        );
    }

    public String getCoOwnerAddName() {
        return config.getString("gui.co-owner-menu.items.add.name", "&aVoeg mede-eigenaar toe");
    }

    public List<String> getCoOwnerAddLore() {
        return config.getStringList("gui.co-owner-menu.items.add.lore");
    }

    public int getCoOwnerBackSlot() {
        return config.getInt("gui.co-owner-menu.items.back.slot", 26);
    }

    public String getCoOwnerBackItemId() {
        return config.getString("gui.co-owner-menu.items.back.material", "BARRIER");
    }

    public Material getCoOwnerBackMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-menu.items.back.material", "BARRIER"),
                Material.BARRIER
        );
    }

    public String getCoOwnerBackName() {
        return config.getString("gui.co-owner-menu.items.back.name", "&cTerug");
    }

    public List<String> getCoOwnerBackLore() {
        return config.getStringList("gui.co-owner-menu.items.back.lore");
    }

    public int getCoOwnerToggleSlot() {
        return config.getInt("gui.co-owner-menu.items.toggle.slot", 24);
    }

    public String getCoOwnerToggleItemId() {
        return config.getString("gui.co-owner-menu.items.toggle.material", "REDSTONE");
    }

    public Material getCoOwnerToggleMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-menu.items.toggle.material", "REDSTONE"),
                Material.REDSTONE
        );
    }

    public String getCoOwnerToggleName() {
        return config.getString("gui.co-owner-menu.items.toggle.name", "&bCo-owner activiteit: {status}");
    }

    public List<String> getCoOwnerToggleLore() {
        return config.getStringList("gui.co-owner-menu.items.toggle.lore");
    }

    public String getCoOwnerToggleStatusEnabled() {
        return config.getString("gui.co-owner-menu.items.toggle.status.enabled", "&aAAN");
    }

    public String getCoOwnerToggleStatusDisabled() {
        return config.getString("gui.co-owner-menu.items.toggle.status.disabled", "&cUIT");
    }

    public int getCoOwnerRemoveConfirmRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.co-owner-remove-confirm.rows", 3)));
    }

    public String getCoOwnerRemoveConfirmTitle() {
        return config.getString("gui.co-owner-remove-confirm.title", "&cVerwijder {player}?");
    }

    public int getCoOwnerRemoveConfirmTargetSlot() {
        return config.getInt("gui.co-owner-remove-confirm.items.target.slot", 13);
    }

    public int getCoOwnerRemoveConfirmYesSlot() {
        return config.getInt("gui.co-owner-remove-confirm.items.yes.slot", 11);
    }

    public String getCoOwnerRemoveConfirmYesItemId() {
        return config.getString("gui.co-owner-remove-confirm.items.yes.material", "LIME_WOOL");
    }

    public Material getCoOwnerRemoveConfirmYesMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-remove-confirm.items.yes.material", "LIME_WOOL"),
                Material.LIME_WOOL
        );
    }

    public String getCoOwnerRemoveConfirmYesName() {
        return config.getString("gui.co-owner-remove-confirm.items.yes.name", "&aBevestigen");
    }

    public List<String> getCoOwnerRemoveConfirmYesLore() {
        return config.getStringList("gui.co-owner-remove-confirm.items.yes.lore");
    }

    public int getCoOwnerRemoveConfirmNoSlot() {
        return config.getInt("gui.co-owner-remove-confirm.items.no.slot", 15);
    }

    public String getCoOwnerRemoveConfirmNoItemId() {
        return config.getString("gui.co-owner-remove-confirm.items.no.material", "RED_WOOL");
    }

    public Material getCoOwnerRemoveConfirmNoMaterial() {
        return parseMaterial(
                config.getString("gui.co-owner-remove-confirm.items.no.material", "RED_WOOL"),
                Material.RED_WOOL
        );
    }

    public String getCoOwnerRemoveConfirmNoName() {
        return config.getString("gui.co-owner-remove-confirm.items.no.name", "&cAnnuleren");
    }

    public List<String> getCoOwnerRemoveConfirmNoLore() {
        return config.getStringList("gui.co-owner-remove-confirm.items.no.lore");
    }

    // ----------------------------------------------------------------
    // Animals Home GUI
    // ----------------------------------------------------------------

    public int getAnimalsHomeRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.animals-home.rows", 3)));
    }

    public String getAnimalsHomeTitle() {
        return config.getString("gui.animals-home.title", "&bAnimals");
    }

    public boolean isAnimalsHomeFillerEnabled() {
        return config.getBoolean("gui.animals-home.filler.enabled", true);
    }

    public String getAnimalsHomeFillerItemId() {
        return config.getString("gui.animals-home.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
    }

    public Material getAnimalsHomeFillerMaterial() {
        return parseMaterial(
                config.getString("gui.animals-home.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    public String getAnimalsHomeFillerName() {
        return config.getString("gui.animals-home.filler.name", " ");
    }

    public int getAnimalsHomeInfoSlot() {
        return config.getInt("gui.animals-home.items.info.slot", 11);
    }

    public String getAnimalsHomeInfoItemId() {
        return config.getString("gui.animals-home.items.info.material", "BOOK");
    }

    public Material getAnimalsHomeInfoMaterial() {
        return parseMaterial(
                config.getString("gui.animals-home.items.info.material", "BOOK"),
                Material.BOOK
        );
    }

    public String getAnimalsHomeInfoName() {
        return config.getString("gui.animals-home.items.info.name", "&bInformatie");
    }

    public List<String> getAnimalsHomeInfoLore() {
        return config.getStringList("gui.animals-home.items.info.lore");
    }

    public int getAnimalsHomeMyAnimalsSlot() {
        return config.getInt("gui.animals-home.items.my-animals.slot", 15);
    }

    public String getAnimalsHomeMyAnimalsItemId() {
        return config.getString("gui.animals-home.items.my-animals.material", "LEAD");
    }

    public Material getAnimalsHomeMyAnimalsMaterial() {
        return parseMaterial(
                config.getString("gui.animals-home.items.my-animals.material", "LEAD"),
                Material.LEAD
        );
    }

    public String getAnimalsHomeMyAnimalsName() {
        return config.getString("gui.animals-home.items.my-animals.name", "&bMijn Animals");
    }

    public List<String> getAnimalsHomeMyAnimalsLore() {
        return config.getStringList("gui.animals-home.items.my-animals.lore");
    }

    // ----------------------------------------------------------------
    // Animals Info List GUI
    // ----------------------------------------------------------------

    public int getAnimalsInfoListRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.animals-info-list.rows", 3)));
    }

    public String getAnimalsInfoListTitle() {
        return config.getString("gui.animals-info-list.title", "&bAnimals Informatie");
    }

    public boolean isAnimalsInfoListFillerEnabled() {
        return config.getBoolean("gui.animals-info-list.filler.enabled", true);
    }

    public String getAnimalsInfoListFillerItemId() {
        return config.getString("gui.animals-info-list.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
    }

    public Material getAnimalsInfoListFillerMaterial() {
        return parseMaterial(
                config.getString("gui.animals-info-list.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    public String getAnimalsInfoListFillerName() {
        return config.getString("gui.animals-info-list.filler.name", " ");
    }

    public List<Integer> getAnimalsInfoListAnimalSlots() {
        List<Integer> result = new ArrayList<>();
        List<String> raw = config.getStringList("gui.animals-info-list.items.animal-slots");

        for (String value : raw) {
            try {
                result.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }

        if (result.isEmpty()) {
            result.add(10);
            result.add(11);
            result.add(12);
            result.add(13);
            result.add(14);
            result.add(15);
            result.add(16);
        }

        return result;
    }

    public int getAnimalsInfoListBackSlot() {
        return config.getInt("gui.animals-info-list.items.back.slot", 22);
    }

    public String getAnimalsInfoListBackItemId() {
        return config.getString("gui.animals-info-list.items.back.material", "ARROW");
    }

    public Material getAnimalsInfoListBackMaterial() {
        return parseMaterial(
                config.getString("gui.animals-info-list.items.back.material", "ARROW"),
                Material.ARROW
        );
    }

    public String getAnimalsInfoListBackName() {
        return config.getString("gui.animals-info-list.items.back.name", "&cTerug");
    }

    public List<String> getAnimalsInfoListBackLore() {
        return config.getStringList("gui.animals-info-list.items.back.lore");
    }

    // ----------------------------------------------------------------
    // Animal Info Detail GUI
    // ----------------------------------------------------------------

    public int getAnimalInfoDetailRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.animal-info-detail.rows", 5)));
    }

    public String getAnimalInfoDetailTitle() {
        return config.getString("gui.animal-info-detail.title", "&bInfo &8• &f{animal}");
    }

    public boolean isAnimalInfoDetailFillerEnabled() {
        return config.getBoolean("gui.animal-info-detail.filler.enabled", true);
    }

    public String getAnimalInfoDetailFillerItemId() {
        return config.getString("gui.animal-info-detail.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
    }

    public Material getAnimalInfoDetailFillerMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info-detail.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    public String getAnimalInfoDetailFillerName() {
        return config.getString("gui.animal-info-detail.filler.name", " ");
    }

    public int getAnimalInfoDetailHeadSlot() {
        return config.getInt("gui.animal-info-detail.items.head.slot", 13);
    }

    public int getAnimalInfoDetailBondInfoSlot() {
        return config.getInt("gui.animal-info-detail.items.bond-info.slot", 20);
    }

    public String getAnimalInfoDetailBondInfoItemId() {
        return config.getString("gui.animal-info-detail.items.bond-info.material", "LEAD");
    }

    public Material getAnimalInfoDetailBondInfoMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info-detail.items.bond-info.material", "LEAD"),
                Material.LEAD
        );
    }

    public String getAnimalInfoDetailBondInfoName() {
        return config.getString("gui.animal-info-detail.items.bond-info.name", "&bBond Informatie");
    }

    public List<String> getAnimalInfoDetailBondInfoLore() {
        return config.getStringList("gui.animal-info-detail.items.bond-info.lore");
    }

    public int getAnimalInfoDetailFeedInfoSlot() {
        return config.getInt("gui.animal-info-detail.items.feed-info.slot", 22);
    }

    public String getAnimalInfoDetailFeedInfoItemId() {
        return config.getString("gui.animal-info-detail.items.feed-info.material", "GOLDEN_CARROT");
    }

    public Material getAnimalInfoDetailFeedInfoMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info-detail.items.feed-info.material", "GOLDEN_CARROT"),
                Material.GOLDEN_CARROT
        );
    }

    public String getAnimalInfoDetailFeedInfoName() {
        return config.getString("gui.animal-info-detail.items.feed-info.name", "&bFeed Items");
    }

    public List<String> getAnimalInfoDetailFeedInfoLore() {
        return config.getStringList("gui.animal-info-detail.items.feed-info.lore");
    }

    public int getAnimalInfoDetailLevelInfoSlot() {
        return config.getInt("gui.animal-info-detail.items.level-info.slot", 24);
    }

    public String getAnimalInfoDetailLevelInfoItemId() {
        return config.getString("gui.animal-info-detail.items.level-info.material", "EXPERIENCE_BOTTLE");
    }

    public Material getAnimalInfoDetailLevelInfoMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info-detail.items.level-info.material", "EXPERIENCE_BOTTLE"),
                Material.EXPERIENCE_BOTTLE
        );
    }

    public String getAnimalInfoDetailLevelInfoName() {
        return config.getString("gui.animal-info-detail.items.level-info.name", "&bLevel Kosten");
    }

    public List<String> getAnimalInfoDetailLevelInfoLore() {
        return config.getStringList("gui.animal-info-detail.items.level-info.lore");
    }

    public int getAnimalInfoDetailBackSlot() {
        return config.getInt("gui.animal-info-detail.items.back.slot", 36);
    }

    public String getAnimalInfoDetailBackItemId() {
        return config.getString("gui.animal-info-detail.items.back.material", "ARROW");
    }

    public Material getAnimalInfoDetailBackMaterial() {
        return parseMaterial(
                config.getString("gui.animal-info-detail.items.back.material", "ARROW"),
                Material.ARROW
        );
    }

    public String getAnimalInfoDetailBackName() {
        return config.getString("gui.animal-info-detail.items.back.name", "&cTerug");
    }

    public List<String> getAnimalInfoDetailBackLore() {
        return config.getStringList("gui.animal-info-detail.items.back.lore");
    }

    // ----------------------------------------------------------------
    // Animals List GUI
    // ----------------------------------------------------------------

    public int getAnimalsListRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.animals-list.rows", 6)));
    }

    public String getAnimalsListTitleMine() {
        return config.getString("gui.animals-list.title-mine", "&bJouw Dieren &8• &fPagina {page}");
    }

    public String getAnimalsListTitleCoOwner() {
        return config.getString("gui.animals-list.title-co-owner", "&bJouw Co-owner Dieren &8• &fPagina {page}");
    }

    public boolean isAnimalsListFillerEnabled() {
        return config.getBoolean("gui.animals-list.filler.enabled", true);
    }

    public String getAnimalsListFillerItemId() {
        return config.getString("gui.animals-list.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
    }

    public Material getAnimalsListFillerMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE
        );
    }

    public String getAnimalsListFillerName() {
        return config.getString("gui.animals-list.filler.name", " ");
    }

    public List<Integer> getAnimalsListAnimalSlots() {
        List<Integer> result = new ArrayList<>();
        List<String> raw = config.getStringList("gui.animals-list.items.animal-slots");

        for (String value : raw) {
            try {
                result.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }

        if (result.isEmpty()) {
            int[] defaults = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34,
                    37, 38, 39, 40, 41, 42, 43
            };
            for (int slot : defaults) {
                result.add(slot);
            }
        }

        return result;
    }

    public int getAnimalsListMineTabSlot() {
        return config.getInt("gui.animals-list.items.mine-tab.slot", 45);
    }

    public String getAnimalsListMineTabItemId() {
        return config.getString("gui.animals-list.items.mine-tab.material", "LEAD");
    }

    public Material getAnimalsListMineTabMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.items.mine-tab.material", "LEAD"),
                Material.LEAD
        );
    }

    public String getAnimalsListMineTabName() {
        return config.getString("gui.animals-list.items.mine-tab.name", "&bMijn dieren");
    }

    public List<String> getAnimalsListMineTabLore() {
        return config.getStringList("gui.animals-list.items.mine-tab.lore");
    }

    public int getAnimalsListCoOwnerTabSlot() {
        return config.getInt("gui.animals-list.items.co-owner-tab.slot", 46);
    }

    public String getAnimalsListCoOwnerTabItemId() {
        return config.getString("gui.animals-list.items.co-owner-tab.material", "NAME_TAG");
    }

    public Material getAnimalsListCoOwnerTabMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.items.co-owner-tab.material", "NAME_TAG"),
                Material.NAME_TAG
        );
    }

    public String getAnimalsListCoOwnerTabName() {
        return config.getString("gui.animals-list.items.co-owner-tab.name", "&dCo-owner dieren");
    }

    public List<String> getAnimalsListCoOwnerTabLore() {
        return config.getStringList("gui.animals-list.items.co-owner-tab.lore");
    }

    public int getAnimalsListPreviousPageSlot() {
        return config.getInt("gui.animals-list.items.previous-page.slot", 48);
    }

    public String getAnimalsListPreviousPageItemId() {
        return config.getString("gui.animals-list.items.previous-page.material", "ARROW");
    }

    public Material getAnimalsListPreviousPageMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.items.previous-page.material", "ARROW"),
                Material.ARROW
        );
    }

    public String getAnimalsListPreviousPageName() {
        return config.getString("gui.animals-list.items.previous-page.name", "&bVorige pagina");
    }

    public List<String> getAnimalsListPreviousPageLore() {
        return config.getStringList("gui.animals-list.items.previous-page.lore");
    }

    public int getAnimalsListInfoSlot() {
        return config.getInt("gui.animals-list.items.info.slot", 49);
    }

    public String getAnimalsListInfoItemId() {
        return config.getString("gui.animals-list.items.info.material", "BOOK");
    }

    public Material getAnimalsListInfoMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.items.info.material", "BOOK"),
                Material.BOOK
        );
    }

    public String getAnimalsListInfoNameMine() {
        return config.getString("gui.animals-list.items.info.name-mine", "&bMijn dieren");
    }

    public String getAnimalsListInfoNameCoOwner() {
        return config.getString("gui.animals-list.items.info.name-co-owner", "&dCo-owner dieren");
    }

    public List<String> getAnimalsListInfoLore() {
        return config.getStringList("gui.animals-list.items.info.lore");
    }

    public int getAnimalsListNextPageSlot() {
        return config.getInt("gui.animals-list.items.next-page.slot", 50);
    }

    public String getAnimalsListNextPageItemId() {
        return config.getString("gui.animals-list.items.next-page.material", "ARROW");
    }

    public Material getAnimalsListNextPageMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.items.next-page.material", "ARROW"),
                Material.ARROW
        );
    }

    public String getAnimalsListNextPageName() {
        return config.getString("gui.animals-list.items.next-page.name", "&bVolgende pagina");
    }

    public List<String> getAnimalsListNextPageLore() {
        return config.getStringList("gui.animals-list.items.next-page.lore");
    }

    public int getAnimalsListBackSlot() {
        return config.getInt("gui.animals-list.items.back.slot", 53);
    }

    public String getAnimalsListBackItemId() {
        return config.getString("gui.animals-list.items.back.material", "BARRIER");
    }

    public Material getAnimalsListBackMaterial() {
        return parseMaterial(
                config.getString("gui.animals-list.items.back.material", "BARRIER"),
                Material.BARRIER
        );
    }

    public String getAnimalsListBackName() {
        return config.getString("gui.animals-list.items.back.name", "&cTerug");
    }

    public List<String> getAnimalsListBackLore() {
        return config.getStringList("gui.animals-list.items.back.lore");
    }

    // ----------------------------------------------------------------
    // Bars
    // ----------------------------------------------------------------

    public int getHungerBarLength() {
        return Math.max(5, config.getInt("bars.hunger.length", 10));
    }

    public String getHungerBarFullColor() {
        return config.getString("bars.hunger.full-color", "&a");
    }

    public String getHungerBarEmptyColor() {
        return config.getString("bars.hunger.empty-color", "&7");
    }

    public String getHungerBarSymbol() {
        return config.getString("bars.hunger.symbol", "■");
    }

    public int getXpBarLength() {
        return Math.max(5, config.getInt("bars.xp.length", 10));
    }

    public String getXpBarFullColor() {
        return config.getString("bars.xp.full-color", "&b");
    }

    public String getXpBarEmptyColor() {
        return config.getString("bars.xp.empty-color", "&7");
    }

    public String getXpBarSymbol() {
        return config.getString("bars.xp.symbol", "■");
    }

    private Material parseMaterial(String input, Material fallback) {
        if (input == null) {
            return fallback;
        }

        try {
            return Material.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}