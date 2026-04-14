package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.GuiSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.DirectLevelUpgrade;
import me.lekkernakkie.lekkeranimal.gui.AnimalGuiHolder;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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

    public void openAnimalInfo(org.bukkit.entity.Player player, Entity entity) {
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

        Inventory inventory = Bukkit.createInventory(
                new AnimalGuiHolder(entity.getUniqueId()),
                gui.getAnimalInfoRows() * 9,
                title
        );

        if (gui.isFillerEnabled()) {
            ItemStack filler = createItem(gui.getFillerMaterial(), gui.getFillerName(), List.of());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
        }

        inventory.setItem(gui.getOwnerSlot(), createOwnerItem(data, profile, gui));
        inventory.setItem(gui.getHungerSlot(), createHungerItem(data, profile, gui));
        inventory.setItem(gui.getLevelSlot(), createLevelItem(data, profile, gui));

        player.openInventory(inventory);
    }

    public void refreshOpenAnimalGui(org.bukkit.entity.Player player, Entity entity) {
        if (player == null || entity == null) {
            return;
        }

        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof AnimalGuiHolder holder)) {
            return;
        }

        if (!holder.getEntityUuid().equals(entity.getUniqueId())) {
            return;
        }

        openAnimalInfo(player, entity);
    }

    private ItemStack createOwnerItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        ItemStack item = new ItemStack(gui.getOwnerMaterial());

        if (item.getType() == Material.PLAYER_HEAD && item.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(data.getOwnerUuid()));
            skullMeta.setDisplayName(ColorUtil.colorize(gui.getOwnerName()));
            skullMeta.setLore(colorizeList(applyPlaceholders(
                    gui.getOwnerLore(),
                    "{owner}", data.getOwnerName(),
                    "{animal_type}", profile.getEntityType().name(),
                    "{animal}", profile.getDisplayName()
            )));
            skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(skullMeta);
            return item;
        }

        return createItem(
                gui.getOwnerMaterial(),
                gui.getOwnerName(),
                applyPlaceholders(
                        gui.getOwnerLore(),
                        "{owner}", data.getOwnerName(),
                        "{animal_type}", profile.getEntityType().name(),
                        "{animal}", profile.getDisplayName()
                )
        );
    }

    private ItemStack createHungerItem(AnimalData data, AnimalProfile profile, GuiSettings gui) {
        int percent = profile.getMaxHunger() <= 0 ? 0 : (int) Math.round((data.getHunger() * 100.0) / profile.getMaxHunger());

        return createItem(
                gui.getHungerMaterial(),
                gui.getHungerName(),
                applyPlaceholders(
                        gui.getHungerLore(),
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
        int nextLevel = data.getLevel() + 1;
        DirectLevelUpgrade upgrade = profile.getDirectLevelUpgrade(nextLevel);

        String nextUpgradeItem;
        String nextUpgradeAmount;
        String nextUpgradeStatus;

        if (data.getLevel() >= profile.getMaxLevel()) {
            nextUpgradeItem = "MAX";
            nextUpgradeAmount = "0";
            nextUpgradeStatus = ColorUtil.colorize("&aMaximum level bereikt");
        } else if (upgrade == null) {
            nextUpgradeItem = "None";
            nextUpgradeAmount = "0";
            nextUpgradeStatus = ColorUtil.colorize("&cGeen directe upgrade ingesteld");
        } else {
            nextUpgradeItem = formatMaterial(upgrade.getItem());
            nextUpgradeAmount = String.valueOf(upgrade.getAmount());
            nextUpgradeStatus = ColorUtil.colorize("&eKlik om direct te upgraden");
        }

        return createItem(
                gui.getLevelMaterial(),
                gui.getLevelName(),
                applyPlaceholders(
                        gui.getLevelLore(),
                        "{level}", String.valueOf(data.getLevel()),
                        "{xp}", String.valueOf(data.getXp()),
                        "{required_xp}", String.valueOf(requiredXp),
                        "{xp_bar}", createBar(
                                data.getXp(),
                                requiredXp,
                                gui.getXpBarLength(),
                                gui.getXpBarFullColor(),
                                gui.getXpBarEmptyColor(),
                                gui.getXpBarSymbol()
                        ),
                        "{next_level}", String.valueOf(nextLevel),
                        "{next_upgrade_item}", nextUpgradeItem,
                        "{next_upgrade_amount}", nextUpgradeAmount,
                        "{next_upgrade_status}", nextUpgradeStatus
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