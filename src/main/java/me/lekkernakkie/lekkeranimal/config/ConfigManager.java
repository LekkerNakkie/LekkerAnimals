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

    private FileConfiguration animalsConfig;
    private FileConfiguration langConfig;
    private FileConfiguration guiConfig;

    private MainSettings mainSettings;
    private AnimalsSettings animalsSettings;
    private LangSettings langSettings;
    private GuiSettings guiSettings;

    public ConfigManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        saveResourceIfNotExists("animals.yml");
        saveResourceIfNotExists("lang_NL.yml");
        saveResourceIfNotExists("Gui.yml");

        plugin.reloadConfig();

        animalsFile = new File(plugin.getDataFolder(), "animals.yml");
        langFile = new File(plugin.getDataFolder(), "lang_NL.yml");
        guiFile = new File(plugin.getDataFolder(), "Gui.yml");

        animalsConfig = YamlConfiguration.loadConfiguration(animalsFile);
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);

        mainSettings = new MainSettings(plugin.getConfig());
        animalsSettings = new AnimalsSettings(plugin, animalsConfig);
        langSettings = new LangSettings(langConfig);
        guiSettings = new GuiSettings(guiConfig);
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
}