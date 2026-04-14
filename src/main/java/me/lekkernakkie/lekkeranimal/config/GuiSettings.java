package me.lekkernakkie.lekkeranimal.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

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
        return config.getInt("gui.animal-info.items.owner.slot", 11);
    }

    public Material getOwnerMaterial() {
        return parseMaterial(config.getString("gui.animal-info.items.owner.material", "PLAYER_HEAD"), Material.PLAYER_HEAD);
    }

    public String getOwnerName() {
        return config.getString("gui.animal-info.items.owner.name", "&bOwner");
    }

    public List<String> getOwnerLore() {
        return config.getStringList("gui.animal-info.items.owner.lore");
    }

    public int getHungerSlot() {
        return config.getInt("gui.animal-info.items.hunger.slot", 13);
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
        return config.getInt("gui.animal-info.items.level.slot", 15);
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
        return config.getInt("gui.animal-info.items.harvest.slot", 22);
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