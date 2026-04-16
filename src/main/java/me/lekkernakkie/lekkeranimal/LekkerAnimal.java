package me.lekkernakkie.lekkeranimal;

import me.lekkernakkie.lekkeranimal.command.AnimalFeederCommand;
import me.lekkernakkie.lekkeranimal.command.AnimalHeadCommand;
import me.lekkernakkie.lekkeranimal.command.AnimalHeadSellCommand;
import me.lekkernakkie.lekkeranimal.command.AnimalsCommand;
import me.lekkernakkie.lekkeranimal.command.LekkerAnimalsCommand;
import me.lekkernakkie.lekkeranimal.config.ConfigManager;
import me.lekkernakkie.lekkeranimal.data.DataManager;
import me.lekkernakkie.lekkeranimal.listener.AnimalDeathListener;
import me.lekkernakkie.lekkeranimal.listener.AnimalInteractListener;
import me.lekkernakkie.lekkeranimal.listener.AnimalLoadListener;
import me.lekkernakkie.lekkeranimal.listener.CoOwnerChatListener;
import me.lekkernakkie.lekkeranimal.listener.CustomHeadListener;
import me.lekkernakkie.lekkeranimal.listener.GuiClickListener;
import me.lekkernakkie.lekkeranimal.listener.HeadSellListener;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import me.lekkernakkie.lekkeranimal.manager.BondManager;
import me.lekkernakkie.lekkeranimal.manager.FeedstationManager;
import me.lekkernakkie.lekkeranimal.manager.GuiManager;
import me.lekkernakkie.lekkeranimal.manager.HarvestManager;
import me.lekkernakkie.lekkeranimal.manager.HeadSellManager;
import me.lekkernakkie.lekkeranimal.manager.HologramManager;
import me.lekkernakkie.lekkeranimal.manager.HungerManager;
import me.lekkernakkie.lekkeranimal.manager.LevelManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
    private HarvestManager harvestManager;
    private HeadSellManager headSellManager;
    private FeedstationManager feedstationManager;
    private CoOwnerChatListener coOwnerChatListener;

    private Economy economy;
    private BukkitTask autosaveTask;

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
        this.harvestManager = new HarvestManager(this);
        this.headSellManager = new HeadSellManager(this);
        this.feedstationManager = new FeedstationManager(this);
        this.coOwnerChatListener = new CoOwnerChatListener(this);

        this.dataManager.loadAllIntoMemory(animalManager);

        setupEconomy();

        if (getCommand("lekkeranimals") != null) {
            getCommand("lekkeranimals").setExecutor(new LekkerAnimalsCommand(this));
        }

        if (getCommand("animals") != null) {
            getCommand("animals").setExecutor(new AnimalsCommand(this));
        }

        if (getCommand("animalhead") != null) {
            getCommand("animalhead").setExecutor(new AnimalHeadCommand(this));
        }

        if (getCommand("animalheadsell") != null) {
            getCommand("animalheadsell").setExecutor(new AnimalHeadSellCommand(this));
        }

        if (getCommand("animalfeeder") != null) {
            getCommand("animalfeeder").setExecutor(new AnimalFeederCommand(this));
        }

        getServer().getPluginManager().registerEvents(
                new AnimalInteractListener(this, bondManager, animalManager, levelManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new AnimalDeathListener(animalManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new AnimalLoadListener(this, animalManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new GuiClickListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new HeadSellListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                coOwnerChatListener,
                this
        );

        getServer().getPluginManager().registerEvents(
                new CustomHeadListener(this),
                this
        );

        startRuntimeTasks();
        getLogger().info("LekkerAnimal enabled successfully.");
    }

    @Override
    public void onDisable() {
        stopRuntimeTasks();

        if (dataManager != null) {
            dataManager.shutdown(animalManager);
        }

        getLogger().info("LekkerAnimal disabled.");
    }

    public void reloadPluginState() {
        stopRuntimeTasks();

        if (configManager != null) {
            configManager.reloadAll();
        }

        setupEconomy();
        startRuntimeTasks();
        refreshLoadedAnimalDisplays();
    }

    private void startRuntimeTasks() {
        if (hungerManager != null) {
            hungerManager.start();
        }

        if (hologramManager != null) {
            hologramManager.start();
        }

        if (feedstationManager != null) {
            feedstationManager.start();
        }

        startAutosaveTask();
    }

    private void stopRuntimeTasks() {
        stopAutosaveTask();

        if (hungerManager != null) {
            hungerManager.stop();
        }

        if (hologramManager != null) {
            hologramManager.stop();
        }

        if (feedstationManager != null) {
            feedstationManager.stop();
        }
    }

    private void startAutosaveTask() {
        stopAutosaveTask();

        if (configManager == null || dataManager == null || animalManager == null) {
            return;
        }

        int intervalSeconds = configManager.getMainSettings().getAutosaveIntervalSeconds();
        if (intervalSeconds <= 0) {
            return;
        }

        long intervalTicks = Math.max(20L, intervalSeconds * 20L);
        this.autosaveTask = getServer().getScheduler().runTaskTimer(
                this,
                () -> dataManager.saveAll(animalManager),
                intervalTicks,
                intervalTicks
        );
    }

    private void stopAutosaveTask() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
    }

    private void refreshLoadedAnimalDisplays() {
        if (animalManager == null || hologramManager == null) {
            return;
        }

        for (var data : animalManager.getAllBondedAnimals()) {
            Entity entity = getServer().getEntity(data.getEntityUuid());
            if (entity != null && entity.isValid() && !entity.isDead()) {
                hologramManager.refresh(entity);
            }
        }
    }

    private void setupEconomy() {
        this.economy = null;

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found. Animal head selling will be disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> registration =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (registration == null) {
            getLogger().warning("No economy provider found. Animal head selling will be disabled.");
            return;
        }

        this.economy = registration.getProvider();
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
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

    public HarvestManager getHarvestManager() {
        return harvestManager;
    }

    public HeadSellManager getHeadSellManager() {
        return headSellManager;
    }

    public FeedstationManager getFeedstationManager() {
        return feedstationManager;
    }

    public CoOwnerChatListener getCoOwnerChatListener() {
        return coOwnerChatListener;
    }
}