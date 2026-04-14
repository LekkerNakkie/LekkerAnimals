package me.lekkernakkie.lekkeranimal.database;

import me.lekkernakkie.lekkeranimal.data.AnimalData;
import org.bukkit.entity.EntityType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimalRepository {

    private final DatabaseManager databaseManager;

    public AnimalRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(AnimalData data) throws SQLException {
        if (data == null) {
            return;
        }

        int updated = updateByEntityUuid(data);
        if (updated == 0) {
            insert(data);
        }

        data.setDirty(false);
    }

    public void delete(UUID entityUuid) throws SQLException {
        if (entityUuid == null) {
            return;
        }

        String sql = "DELETE FROM bonded_animals WHERE entity_uuid = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entityUuid.toString());
            statement.executeUpdate();
        }
    }

    public List<AnimalData> loadAll() throws SQLException {
        List<AnimalData> animals = new ArrayList<>();

        String sql = """
                SELECT id, entity_uuid, owner_uuid, owner_name, animal_type, level, xp, bond, hunger, max_hunger,
                       world, x, y, z, created_at, updated_at, last_harvest_at
                FROM bonded_animals
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                try {
                    AnimalData data = new AnimalData(
                            rs.getLong("id"),
                            UUID.fromString(rs.getString("entity_uuid")),
                            UUID.fromString(rs.getString("owner_uuid")),
                            rs.getString("owner_name"),
                            EntityType.valueOf(rs.getString("animal_type").toUpperCase()),
                            rs.getInt("hunger"),
                            rs.getInt("max_hunger"),
                            rs.getInt("level"),
                            rs.getInt("xp"),
                            rs.getInt("bond"),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getLong("created_at"),
                            rs.getLong("updated_at"),
                            rs.getLong("last_harvest_at")
                    );
                    data.setDirty(false);
                    animals.add(data);
                } catch (Exception ignored) {
                }
            }
        }

        return animals;
    }

    private int updateByEntityUuid(AnimalData data) throws SQLException {
        String sql = """
                UPDATE bonded_animals
                SET owner_uuid = ?, owner_name = ?, animal_type = ?, level = ?, xp = ?, bond = ?, hunger = ?,
                    max_hunger = ?, world = ?, x = ?, y = ?, z = ?, updated_at = ?, last_harvest_at = ?
                WHERE entity_uuid = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, data.getOwnerUuid().toString());
            statement.setString(2, data.getOwnerName());
            statement.setString(3, data.getEntityType().name());
            statement.setInt(4, data.getLevel());
            statement.setInt(5, data.getXp());
            statement.setInt(6, data.getBond());
            statement.setInt(7, data.getHunger());
            statement.setInt(8, data.getMaxHunger());
            statement.setString(9, data.getWorldName());
            statement.setDouble(10, data.getX());
            statement.setDouble(11, data.getY());
            statement.setDouble(12, data.getZ());
            statement.setLong(13, System.currentTimeMillis());
            statement.setLong(14, data.getLastHarvestAt());
            statement.setString(15, data.getEntityUuid().toString());

            return statement.executeUpdate();
        }
    }

    private void insert(AnimalData data) throws SQLException {
        String sql = """
                INSERT INTO bonded_animals
                (entity_uuid, owner_uuid, owner_name, animal_type, level, xp, bond, hunger, max_hunger, world, x, y, z, created_at, updated_at, last_harvest_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        long now = System.currentTimeMillis();
        if (data.getCreatedAt() <= 0L) {
            data.setCreatedAt(now);
        }
        data.setUpdatedAt(now);

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, data.getEntityUuid().toString());
            statement.setString(2, data.getOwnerUuid().toString());
            statement.setString(3, data.getOwnerName());
            statement.setString(4, data.getEntityType().name());
            statement.setInt(5, data.getLevel());
            statement.setInt(6, data.getXp());
            statement.setInt(7, data.getBond());
            statement.setInt(8, data.getHunger());
            statement.setInt(9, data.getMaxHunger());
            statement.setString(10, data.getWorldName());
            statement.setDouble(11, data.getX());
            statement.setDouble(12, data.getY());
            statement.setDouble(13, data.getZ());
            statement.setLong(14, data.getCreatedAt());
            statement.setLong(15, data.getUpdatedAt());
            statement.setLong(16, data.getLastHarvestAt());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    data.setDatabaseId(keys.getLong(1));
                }
            }
        }
    }
}