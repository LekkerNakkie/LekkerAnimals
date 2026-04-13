package me.lekkernakkie.lekkeranimal.config;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final LekkerAnimal plugin;

    private File animalsFile;
    private File langFile;

    private FileConfiguration animalsConfig;
    private FileConfiguration langConfig;

    private MainSettings mainSettings;
    private AnimalsSettings animalsSettings;
    private LangSettings langSettings;

    public ConfigManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        saveResourceIfNotExists("animals.yml");
        saveResourceIfNotExists("Lang_NL.yml");

        plugin.reloadConfig();

        animalsFile = new File(plugin.getDataFolder(), "animals.yml");
        langFile = new File(plugin.getDataFolder(), "Lang_NL.yml");

        animalsConfig = YamlConfiguration.loadConfiguration(animalsFile);
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        mainSettings = new MainSettings(plugin.getConfig());
        animalsSettings = new AnimalsSettings(animalsConfig);
        langSettings = new LangSettings(langConfig);
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

    public MainSettings getMainSettings() {
        return mainSettings;
    }

    public AnimalsSettings getAnimalsSettings() {
        return animalsSettings;
    }

    public LangSettings getLangSettings() {
        return langSettings;
    }
}