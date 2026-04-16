package me.lekkernakkie.lekkeranimal.config;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final LekkerAnimal plugin;

    private File animalsFile;
    private File langFile;
    private File guiFile;
    private File feedstationFile;

    private FileConfiguration animalsConfig;
    private FileConfiguration langConfig;
    private FileConfiguration guiConfig;
    private FileConfiguration feedstationConfig;

    private MainSettings mainSettings;
    private AnimalsSettings animalsSettings;
    private LangSettings langSettings;
    private GuiSettings guiSettings;
    private FeedstationSettings feedstationSettings;

    public ConfigManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        saveResourceIfNotExists("animals.yml");
        saveResourceIfNotExists("lang_NL.yml");
        saveResourceIfNotExists("Gui.yml");
        saveResourceIfNotExists("feedstation.yml");

        plugin.reloadConfig();

        animalsFile = new File(plugin.getDataFolder(), "animals.yml");
        langFile = new File(plugin.getDataFolder(), "lang_NL.yml");
        guiFile = new File(plugin.getDataFolder(), "Gui.yml");
        feedstationFile = new File(plugin.getDataFolder(), "feedstation.yml");

        animalsConfig = YamlConfiguration.loadConfiguration(animalsFile);
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        feedstationConfig = YamlConfiguration.loadConfiguration(feedstationFile);

        mainSettings = new MainSettings(plugin.getConfig());
        animalsSettings = new AnimalsSettings(animalsConfig);
        langSettings = new LangSettings(langConfig);
        guiSettings = new GuiSettings(guiConfig);
        feedstationSettings = new FeedstationSettings(feedstationConfig);
    }

    public void reloadAll() {
        loadAll();
    }

    private void saveResourceIfNotExists(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    public FileConfiguration getAnimalsConfig() {
        return animalsConfig;
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public FileConfiguration getFeedstationConfig() {
        return feedstationConfig;
    }

    public MainSettings getMainSettings() {
        return mainSettings;
    }

    public AnimalsSettings getAnimalsSettings() {
        return animalsSettings;
    }

    public LangSettings getLangSettings() {
        return langSettings;
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }

    public FeedstationSettings getFeedstationSettings() {
        return feedstationSettings;
    }
}