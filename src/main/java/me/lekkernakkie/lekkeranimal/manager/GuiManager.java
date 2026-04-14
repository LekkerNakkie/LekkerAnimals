package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.GuiSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    private final LekkerAnimal plugin;

    public GuiManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void openAnimalInfo(Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            return;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entity.getType());
        if (profile == null) {
            return;
        }

        GuiSettings gui = plugin.getConfigManager().getGuiSettings();
        if (!gui.isAnimalInfoEnabled()) {
            return;
        }

        String title = ColorUtil.colorize(
                gui.getAnimalInfoTitle().replace("{animal}", profile.getDisplayName())
        );

        Inventory inventory = Bukkit.createInventory(null, gui.getAnimalInfoRows() * 9, title);

        if (gui.isFillerEnabled()) {
            ItemStack filler = createItem(gui.getFillerMaterial(), gui.getFillerName(), List.of());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        inventory.setItem(gui.getOwnerSlot(), createOwnerItem(data, profile, gui));
        inventory.setItem(gui.getHungerSlot(), createHungerItem(data, profile, gui));
        inventory.setItem(gui.getLevelSlot(), createLevelItem(data, profile, gui));
        inventory.setItem(gui.getNextUpgradeSlot(), createNextUpgradeItem(data, profile, gui));

        player.openInventory(inventory);
    }

    private ItemStack createOwnerItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        ItemStack item = new ItemStack(gui.getOwnerMaterial());

        if (item.getType() == Material.PLAYER_HEAD && item.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getOwnerUuid()));
            skullMeta.setDisplayName(ColorUtil.colorize(gui.getOwnerName()));
            skullMeta.setLore(colorizeList(applyPlaceholders(gui.getOwnerLore(),
                    "{owner}", data.getOwnerName(),
                    "{animal_type}", profile.getEntityType().name(),
                    "{animal}", profile.getDisplayName()
            )));
            skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(skullMeta);
            return item;
        }

        return createItem(gui.getOwnerMaterial(), gui.getOwnerName(),
                applyPlaceholders(gui.getOwnerLore(),
                        "{owner}", data.getOwnerName(),
                        "{animal_type}", profile.getEntityType().name(),
                        "{animal}", profile.getDisplayName()
                ));
    }

    private ItemStack createHungerItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        int percent = profile.getMaxHunger() <= 0 ? 0 : (int) Math.round((data.getHunger() * 100.0) / profile.getMaxHunger());

        return createItem(
                gui.getHungerMaterial(),
                gui.getHungerName(),
                applyPlaceholders(gui.getHungerLore(),
                        "{hunger}", String.valueOf(data.getHunger()),
                        "{max_hunger}", String.valueOf(profile.getMaxHunger()),
                        "{hunger_percent}", String.valueOf(percent),
                        "{hunger_bar}", createBar(
                                data.getHunger(),
                                profile.getMaxHunger(),
                                gui.getHungerBarLength(),
                                gui.getHungerBarFullColor(),
                                gui.getHungerBarEmptyColor(),
                                gui.getHungerBarSymbol()
                        )
                )
        );
    }

    private ItemStack createLevelItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        int requiredXp = plugin.getLevelManager().getRequiredXpForNextLevel(data.getLevel());
        int xpLeft = Math.max(0, requiredXp - data.getXp());

        return createItem(
                gui.getLevelMaterial(),
                gui.getLevelName(),
                applyPlaceholders(gui.getLevelLore(),
                        "{level}", String.valueOf(data.getLevel()),
                        "{xp}", String.valueOf(data.getXp()),
                        "{required_xp}", String.valueOf(requiredXp),
                        "{xp_left}", String.valueOf(xpLeft),
                        "{xp_bar}", createBar(
                                data.getXp(),
                                requiredXp,
                                gui.getXpBarLength(),
                                gui.getXpBarFullColor(),
                                gui.getXpBarEmptyColor(),
                                gui.getXpBarSymbol()
                        )
                )
        );
    }

    private ItemStack createNextUpgradeItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        int nextLevel = data.getLevel() + 1;
        DirectLevelUpgrade upgrade = profile.getDirectLevelUpgrade(nextLevel);

        String itemName;
        String amount;
        String status;

        if (data.getLevel() >= profile.getMaxLevel()) {
            itemName = "MAX";
            amount = "0";
            status = ColorUtil.colorize("&aMaximum level bereikt");
        } else if (upgrade == null) {
            itemName = "None";
            amount = "0";
            status = ColorUtil.colorize("&cGeen directe upgrade ingesteld");
        } else {
            itemName = formatMaterial(upgrade.getItem());
            amount = String.valueOf(upgrade.getAmount());
            status = ColorUtil.colorize("&eGeef dit item om direct te levelen");
        }

        return createItem(
                gui.getNextUpgradeMaterial(),
                gui.getNextUpgradeName(),
                applyPlaceholders(gui.getNextUpgradeLore(),
                        "{next_level}", String.valueOf(nextLevel),
                        "{next_upgrade_item}", itemName,
                        "{next_upgrade_amount}", amount,
                        "{next_upgrade_status}", status
                )
        );
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.colorize(name));
        meta.setLore(colorizeList(lore));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> applyPlaceholders(List<String> input, String... replacements) {
        List<String> result = new ArrayList<>();

        for (String line : input) {
            String value = line;
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                value = value.replace(replacements[i], replacements[i + 1]);
            }
            result.add(value);
        }

        return result;
    }

    private List<String> colorizeList(List<String> input) {
        List<String> result = new ArrayList<>();
        for (String line : input) {
            result.add(ColorUtil.colorize(line));
        }
        return result;
    }

    private String createBar(int current, int max, int length, String fullColor, String emptyColor, String symbol) {
        if (max <= 0) {
            return ColorUtil.colorize(emptyColor + symbol.repeat(length));
        }

        double progress = Math.max(0.0, Math.min(1.0, current / (double) max));
        int filled = (int) Math.round(progress * length);

        return ColorUtil.colorize(fullColor + symbol.repeat(filled) + emptyColor + symbol.repeat(Math.max(0, length - filled)));
    }

    private String formatMaterial(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }

        return builder.toString().trim();
    }
}