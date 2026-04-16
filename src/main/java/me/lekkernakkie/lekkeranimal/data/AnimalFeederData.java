package me.lekkernakkie.lekkeranimal.data;

import me.lekkernakkie.lekkeranimal.config.FeederTier;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimalFeederData {

    private final UUID feederUuid;
    private final UUID ownerUuid;
    private String ownerName;
    private FeederTier tier;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private final List<ItemStack> storedFood = new ArrayList<>();

    public AnimalFeederData(UUID feederUuid,
                            UUID ownerUuid,
                            String ownerName,
                            FeederTier tier,
                            Location location) {
        this.feederUuid = feederUuid;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.tier = tier;
        this.worldName = location.getWorld() != null ? location.getWorld().getName() : null;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
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

    public FeederTier getTier() {
        return tier;
    }

    public void setTier(FeederTier tier) {
        this.tier = tier;
    }

    public String getWorldName() {
        return worldName;
    }

    public List<ItemStack> getStoredFood() {
        return storedFood;
    }

    public Location getLocation(World world) {
        return new Location(world, x, y, z);
    }
}