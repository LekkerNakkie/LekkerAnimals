package me.lekkernakkie.lekkeranimal.data;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;

public class DataManager {

    private final LekkerAnimal plugin;

    public DataManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public LekkerAnimal getPlugin() {
        return plugin;
    }
}