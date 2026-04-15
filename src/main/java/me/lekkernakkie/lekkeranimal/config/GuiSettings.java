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

    public Material getFillerMaterial() {
        return parseMaterial(config.getString("gui.animal-info.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    }

    public String getFillerName() {
        return config.getString("gui.animal-info.filler.name", " ");
    }

    public int getOwnerSlot() {
        return config.getInt("gui.animal-info.items.owner.slot", 10);
    }

    public Material getOwnerMaterial() {
        return parseMaterial(config.getString("gui.animal-info.items.owner.material", "PLAYER_HEAD"), Material.PLAYER_HEAD);
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

    public Material getHungerMaterial() {
        return parseMaterial(config.getString("gui.animal-info.items.hunger.material", "GOLDEN_CARROT"), Material.GOLDEN_CARROT);
    }

    public String getHungerName() {
        return config.getString("gui.animal-info.items.hunger.name", "&cHunger");
    }

    public List<String> getHungerLore() {
        return config.getStringList("gui.animal-info.items.hunger.lore");
    }

    public int getLevelSlot() {
        return config.getInt("gui.animal-info.items.level.slot", 14);
    }

    public Material getLevelMaterial() {
        return parseMaterial(config.getString("gui.animal-info.items.level.material", "EXPERIENCE_BOTTLE"), Material.EXPERIENCE_BOTTLE);
    }

    public String getLevelName() {
        return config.getString("gui.animal-info.items.level.name", "&eLevel");
    }

    public List<String> getLevelLore() {
        return config.getStringList("gui.animal-info.items.level.lore");
    }

    public int getHarvestSlot() {
        return config.getInt("gui.animal-info.items.harvest.slot", 16);
    }

    public Material getHarvestMaterial() {
        return parseMaterial(config.getString("gui.animal-info.items.harvest.material", "CHEST"), Material.CHEST);
    }

    public String getHarvestName() {
        return config.getString("gui.animal-info.items.harvest.name", "&6Harvest");
    }

    public List<String> getHarvestLore() {
        return config.getStringList("gui.animal-info.items.harvest.lore");
    }

    public int getCoOwnerManageSlot() {
        return config.getInt("gui.animal-info.items.co-owners.slot", 22);
    }

    public Material getCoOwnerManageMaterial() {
        return parseMaterial(config.getString("gui.animal-info.items.co-owners.material", "NAME_TAG"), Material.NAME_TAG);
    }

    public String getCoOwnerManageName() {
        return config.getString("gui.animal-info.items.co-owners.name", "&bMede-eigenaars");
    }

    public List<String> getCoOwnerManageLore() {
        return config.getStringList("gui.animal-info.items.co-owners.lore");
    }

    public int getCoOwnerRows() {
        return Math.max(1, Math.min(6, config.getInt("gui.co-owner-menu.rows", 3)));
    }

    public String getCoOwnerTitle() {
        return config.getString("gui.co-owner-menu.title", "&bMede-eigenaars");
    }

    public boolean isCoOwnerFillerEnabled() {
        return config.getBoolean("gui.co-owner-menu.filler.enabled", true);
    }

    public Material getCoOwnerFillerMaterial() {
        return parseMaterial(config.getString("gui.co-owner-menu.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    }

    public String getCoOwnerFillerName() {
        return config.getString("gui.co-owner-menu.filler.name", " ");
    }

    public List<Integer> getCoOwnerSlots() {
        List<Integer> result = new ArrayList<>();
        for (String value : config.getStringList("gui.co-owner-menu.items.co-owner-slots")) {
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

    public Material getEmptyCoOwnerMaterial() {
        return parseMaterial(config.getString("gui.co-owner-menu.items.empty-slot.material", "GRAY_DYE"), Material.GRAY_DYE);
    }

    public String getEmptyCoOwnerName() {
        return config.getString("gui.co-owner-menu.items.empty-slot.name", "&7Leeg slot");
    }

    public List<String> getEmptyCoOwnerLore() {
        return config.getStringList("gui.co-owner-menu.items.empty-slot.lore");
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

    public Material getCoOwnerAddMaterial() {
        return parseMaterial(config.getString("gui.co-owner-menu.items.add.material", "LIME_WOOL"), Material.LIME_WOOL);
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

    public Material getCoOwnerBackMaterial() {
        return parseMaterial(config.getString("gui.co-owner-menu.items.back.material", "BARRIER"), Material.BARRIER);
    }

    public String getCoOwnerBackName() {
        return config.getString("gui.co-owner-menu.items.back.name", "&cTerug");
    }

    public List<String> getCoOwnerBackLore() {
        return config.getStringList("gui.co-owner-menu.items.back.lore");
    }

    public int getCoOwnerRemoveConfirmRows() {
        return Math.max(1, Math.min(3, config.getInt("gui.co-owner-remove-confirm.rows", 3)));
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

    public Material getCoOwnerRemoveConfirmYesMaterial() {
        return parseMaterial(config.getString("gui.co-owner-remove-confirm.items.yes.material", "LIME_WOOL"), Material.LIME_WOOL);
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

    public Material getCoOwnerRemoveConfirmNoMaterial() {
        return parseMaterial(config.getString("gui.co-owner-remove-confirm.items.no.material", "RED_WOOL"), Material.RED_WOOL);
    }

    public String getCoOwnerRemoveConfirmNoName() {
        return config.getString("gui.co-owner-remove-confirm.items.no.name", "&cAnnuleren");
    }

    public List<String> getCoOwnerRemoveConfirmNoLore() {
        return config.getStringList("gui.co-owner-remove-confirm.items.no.lore");
    }

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