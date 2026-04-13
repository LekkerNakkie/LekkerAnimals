package me.lekkernakkie.lekkeranimal;

import me.lekkernakkie.lekkeranimal.command.LekkerAnimalsCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LekkerAnimal extends JavaPlugin {

    private static LekkerAnimal instance;

    @Override
    public void onEnable() {
        instance = this;

        // Save default configs
        saveDefaultConfig();
        saveResource("animals.yml", false);
        saveResource("Lang_NL.yml", false);

        // Register command
        getCommand("lekkeranimals").setExecutor(new LekkerAnimalsCommand());

        getLogger().info("LekkerAnimal enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LekkerAnimal disabled!");
    }

    public static LekkerAnimal getInstance() {
        return instance;
    }
}