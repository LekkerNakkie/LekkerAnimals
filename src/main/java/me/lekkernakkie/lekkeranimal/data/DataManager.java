package me.lekkernakkie.lekkeranimal.data;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.database.AnimalRepository;
import me.lekkernakkie.lekkeranimal.database.DatabaseManager;
import me.lekkernakkie.lekkeranimal.manager.AnimalManager;
import org.bukkit.entity.Entity;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final LekkerAnimal plugin;
    private DatabaseManager databaseManager;
    private AnimalRepository animalRepository;

    public DataManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            this.databaseManager = new DatabaseManager(plugin);
            this.databaseManager.connect();
            this.animalRepository = new AnimalRepository(databaseManager);
            plugin.getLogger().info("Database connected successfully.");
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to initialize database.", ex);
        }
    }

    public void shutdown(AnimalManager animalManager) {
        if (plugin.getConfigManager().getMainSettings().isSaveOnDisable()) {
            saveAll(animalManager);
        }

        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public void loadAllIntoMemory(AnimalManager animalManager) {
        try {
            List<AnimalData> loadedAnimals = animalRepository.loadAll();

            int loaded = 0;
            int pending = 0;
            int removed = 0;

            for (AnimalData data : loadedAnimals) {
                Entity entity = plugin.getServer().getEntity(data.getEntityUuid());

                if (entity == null) {
                    animalManager.registerPendingAnimal(data);
                    pending++;
                    continue;
                }

                if (!entity.isValid() || entity.isDead()) {
                    animalRepository.delete(data.getEntityUuid());
                    removed++;
                    continue;
                }

                animalManager.registerAnimal(data);
                loaded++;
            }

            plugin.getLogger().info(
                    "Loaded " + loaded + " bonded animals from database. " +
                            "Pending " + pending + " unloaded animals. " +
                            "Removed " + removed + " invalid entries."
            );
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to load bonded animals from database: " + ex.getMessage());
        }
    }

    public void saveAnimal(AnimalData data) {
        if (data == null) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(data.getEntityUuid());
        if (entity != null && entity.isValid() && !entity.isDead()) {
            data.syncLocation(entity);
        }

        try {
            animalRepository.save(data);
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to save animal " + data.getEntityUuid() + ": " + ex.getMessage());
        }
    }

    public void deleteAnimal(UUID entityUuid) {
        if (entityUuid == null) {
            return;
        }

        try {
            animalRepository.delete(entityUuid);
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to delete animal " + entityUuid + ": " + ex.getMessage());
        }
    }

    public void saveAll(AnimalManager animalManager) {
        if (animalManager == null) {
            return;
        }

        for (AnimalData data : animalManager.getAllBondedAnimals()) {
            if (data.isDirty()) {
                saveAnimal(data);
            }
        }

        for (AnimalData data : animalManager.getAllPendingAnimals()) {
            if (data.isDirty()) {
                saveAnimal(data);
            }
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AnimalRepository getAnimalRepository() {
        return animalRepository;
    }

    public LekkerAnimal getPlugin() {
        return plugin;
    }
}