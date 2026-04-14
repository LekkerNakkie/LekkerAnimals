package me.lekkernakkie.lekkeranimal;

import me.lekkernakkie.lekkeranimal.command.LekkerAnimalsCommand;
import me.lekkernakkie.lekkeranimal.config.ConfigManager;
import me.lekkernakkie.lekkeranimal.data.DataManager;
import me.lekkernakkie.lekkeranimal.listener.AnimalDeathListener;
import me.lekkernakkie.lekkeranimal.listener.AnimalInteractListener;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import me.lekkernakkie.lekkeranimal.manager.BondManager;
import me.lekkernakkie.lekkeranimal.manager.GuiManager;
import me.lekkernakkie.lekkeranimal.manager.HologramManager;
import me.lekkernakkie.lekkeranimal.manager.HungerManager;
import me.lekkernakkie.lekkeranimal.manager.LevelManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LekkerAnimal extends JavaPlugin {

    private static LekkerAnimal instance;

    private ConfigManager configManager;
    private DataManager dataManager;

    private AnimalManager animalManager;
    private BondManager bondManager;
    private HungerManager hungerManager;
    private LevelManager levelManager;
    private HologramManager hologramManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.dataManager = new DataManager(this);
        this.dataManager.initialize();

        this.animalManager = new AnimalManager();
        this.levelManager = new LevelManager(this);
        this.bondManager = new BondManager(this, animalManager);
        this.hungerManager = new HungerManager(this, animalManager);
        this.hologramManager = new HologramManager(this, animalManager);
        this.guiManager = new GuiManager(this);

        this.dataManager.loadAllIntoMemory(animalManager);

        if (getCommand("lekkeranimals") != null) {
            getCommand("lekkeranimals").setExecutor(new LekkerAnimalsCommand(this));
        }

        getServer().getPluginManager().registerEvents(
                new AnimalInteractListener(this, bondManager, animalManager, levelManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new AnimalDeathListener(animalManager),
                this
        );

        hungerManager.start();
        hologramManager.start();

        getLogger().info("LekkerAnimal enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (hungerManager != null) {
            hungerManager.stop();
        }

        if (hologramManager != null) {
            hologramManager.stop();
        }

        if (dataManager != null) {
            dataManager.shutdown(animalManager);
        }

        getLogger().info("LekkerAnimal disabled.");
    }

    public static LekkerAnimal getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public AnimalManager getAnimalManager() {
        return animalManager;
    }

    public BondManager getBondManager() {
        return bondManager;
    }

    public HungerManager getHungerManager() {
        return hungerManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}