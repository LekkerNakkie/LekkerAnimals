package me.lekkernakkie.lekkeranimal.data;

import me.lekkernakkie.lekkeranimal.config.FeederTier;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimalFeederData {

    private long databaseId;
    private final UUID feederUuid;
    private final UUID ownerUuid;
    private String ownerName;
    private FeederTier tier;

    private String worldName;
    private double x;
    private double y;
    private double z;

    private final List<ItemStack> storedFood;
    private long createdAt;
    private long updatedAt;
    private long lastFeedAt;
    private boolean dirty = true;

    public AnimalFeederData(UUID feederUuid,
                            UUID ownerUuid,
                            String ownerName,
                            FeederTier tier,
                            Location location,
                            List<ItemStack> storedFood) {
        this(
                0L,
                feederUuid,
                ownerUuid,
                ownerName,
                tier,
                location.getWorld() != null ? location.getWorld().getName() : null,
                location.getX(),
                location.getY(),
                location.getZ(),
                storedFood,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                0L
        );
    }

    public AnimalFeederData(long databaseId,
                            UUID feederUuid,
                            UUID ownerUuid,
                            String ownerName,
                            FeederTier tier,
                            String worldName,
                            double x,
                            double y,
                            double z,
                            List<ItemStack> storedFood,
                            long createdAt,
                            long updatedAt,
                            long lastFeedAt) {
        this.databaseId = databaseId;
        this.feederUuid = feederUuid;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.tier = tier;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.storedFood = new ArrayList<>(storedFood != null ? storedFood : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastFeedAt = lastFeedAt;
    }

    public Location toLocation(World world) {
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    public void markDirty() {
        this.updatedAt = System.currentTimeMillis();
        this.dirty = true;
    }

    public int getTotalStoredFoodAmount() {
        int total = 0;
        for (ItemStack stack : storedFood) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            total += stack.getAmount();
        }
        return total;
    }

    public long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    public UUID getFeederUuid() {
        return feederUuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        markDirty();
    }

    public FeederTier getTier() {
        return tier;
    }

    public void setTier(FeederTier tier) {
        this.tier = tier;
        markDirty();
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
        markDirty();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
        markDirty();
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        markDirty();
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
        markDirty();
    }

    public List<ItemStack> getStoredFood() {
        return storedFood;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public long getLastFeedAt() {
        return lastFeedAt;
    }

    public void setLastFeedAt(long lastFeedAt) {
        this.lastFeedAt = lastFeedAt;
        markDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}