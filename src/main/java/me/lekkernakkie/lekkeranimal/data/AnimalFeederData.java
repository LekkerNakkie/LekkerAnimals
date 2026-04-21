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
    private boolean hologramEnabled;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private final List<ItemStack> storedFood = new ArrayList<>();

    public AnimalFeederData(UUID feederUuid,
                            UUID ownerUuid,
                            String ownerName,
                            FeederTier tier,
                            boolean hologramEnabled,
                            Location location,
                            List<ItemStack> storedFood) {
        this.feederUuid = feederUuid;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.tier = tier;
        this.hologramEnabled = hologramEnabled;
        this.worldName = location.getWorld() != null ? location.getWorld().getName() : null;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();

        if (storedFood != null) {
            for (ItemStack stack : storedFood) {
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                this.storedFood.add(stack.clone());
            }
        }
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
    }

    public FeederTier getTier() {
        return tier;
    }

    public void setTier(FeederTier tier) {
        this.tier = tier;
    }

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public void setHologramEnabled(boolean hologramEnabled) {
        this.hologramEnabled = hologramEnabled;
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

    public int getStoredFoodAmount() {
        int total = 0;
        for (ItemStack stack : storedFood) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            total += stack.getAmount();
        }
        return total;
    }
}