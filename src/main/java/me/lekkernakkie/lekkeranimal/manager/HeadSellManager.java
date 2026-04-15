package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.HeadPriceRule;
import me.lekkernakkie.lekkeranimal.gui.HeadSellGuiHolder;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class HeadSellManager {

    private final LekkerAnimal plugin;
    private final DecimalFormat moneyFormat = new DecimalFormat("0.00");

    public HeadSellManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void openSellMenu(Player player) {
        if (player == null) {
            return;
        }

        FileConfiguration gui = plugin.getConfigManager().getGuiConfig();
        int rows = Math.max(1, Math.min(6, gui.getInt("gui.head-sell.rows", 3)));
        String title = ColorUtil.colorize(gui.getString("gui.head-sell.title", "&bAnimal Head Sell"));

        Inventory inventory = Bukkit.createInventory(
                new HeadSellGuiHolder(player.getUniqueId()),
                rows * 9,
                title
        );

        fillStaticItems(inventory);
        updateInfoItem(inventory);
        player.openInventory(inventory);
    }

    public boolean isSellInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof HeadSellGuiHolder;
    }

    public boolean isSellSlot(int slot) {
        return getSellSlots().contains(slot);
    }

    public int getConfirmSlot() {
        return plugin.getConfigManager().getGuiConfig().getInt("gui.head-sell.items.confirm.slot", 24);
    }

    public int getInfoSlot() {
        return plugin.getConfigManager().getGuiConfig().getInt("gui.head-sell.items.info.slot", 22);
    }

    public int getCloseSlot() {
        return plugin.getConfigManager().getGuiConfig().getInt("gui.head-sell.items.close.slot", 26);
    }

    public List<Integer> getSellSlots() {
        List<Integer> result = new ArrayList<>();
        for (String value : plugin.getConfigManager().getGuiConfig().getStringList("gui.head-sell.sell-slots")) {
            try {
                result.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }

        if (result.isEmpty()) {
            for (int i = 0; i <= 17; i++) {
                result.add(i);
            }
        }

        return result;
    }

    public void fillStaticItems(Inventory inventory) {
        FileConfiguration gui = plugin.getConfigManager().getGuiConfig();
        boolean fillerEnabled = gui.getBoolean("gui.head-sell.filler.enabled", true);

        if (fillerEnabled) {
            ItemStack filler = createItem(
                    parseMaterial(gui.getString("gui.head-sell.filler.material", "LIGHT_BLUE_STAINED_GLASS_PANE"), Material.LIGHT_BLUE_STAINED_GLASS_PANE),
                    gui.getString("gui.head-sell.filler.name", " "),
                    List.of()
            );

            for (int i = 0; i < inventory.getSize(); i++) {
                if (!isSellSlot(i)) {
                    inventory.setItem(i, filler);
                }
            }
        }

        inventory.setItem(getConfirmSlot(), createItem(
                parseMaterial(gui.getString("gui.head-sell.items.confirm.material", "LIME_WOOL"), Material.LIME_WOOL),
                gui.getString("gui.head-sell.items.confirm.name", "&aBevestig verkoop"),
                gui.getStringList("gui.head-sell.items.confirm.lore")
        ));

        inventory.setItem(getCloseSlot(), createItem(
                parseMaterial(gui.getString("gui.head-sell.items.close.material", "BARRIER"), Material.BARRIER),
                gui.getString("gui.head-sell.items.close.name", "&cSluiten"),
                gui.getStringList("gui.head-sell.items.close.lore")
        ));
    }

    public void updateInfoItem(Inventory inventory) {
        FileConfiguration gui = plugin.getConfigManager().getGuiConfig();
        SellPreview preview = buildPreview(inventory);

        List<String> lore = new ArrayList<>();
        for (String line : gui.getStringList("gui.head-sell.items.info.lore")) {
            lore.add(
                    line.replace("{heads}", String.valueOf(preview.totalHeads()))
                            .replace("{min_total}", formatMoney(preview.minTotal()))
                            .replace("{max_total}", formatMoney(preview.maxTotal()))
            );
        }

        if (!preview.breakdown().isEmpty()) {
            lore.add("&7");
            lore.add("&bWaarden:");
            for (String line : preview.breakdown()) {
                lore.add(line);
            }
        }

        inventory.setItem(getInfoSlot(), createItem(
                parseMaterial(gui.getString("gui.head-sell.items.info.material", "PAPER"), Material.PAPER),
                gui.getString("gui.head-sell.items.info.name", "&bVerkoop overzicht"),
                lore
        ));
    }

    public boolean isSellableHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) {
            return false;
        }

        if (!plugin.getHarvestManager().isPluginCustomHead(item)) {
            return false;
        }

        return resolvePriceRule(item) != null;
    }

    public int moveToSellInventory(Inventory inventory, ItemStack stack) {
        if (inventory == null || stack == null || stack.getType().isAir() || !isSellableHead(stack)) {
            return 0;
        }

        int originalAmount = stack.getAmount();
        int remaining = stack.getAmount();

        for (int slot : getSellSlots()) {
            if (remaining <= 0) {
                break;
            }

            ItemStack existing = inventory.getItem(slot);

            if (existing == null || existing.getType().isAir()) {
                ItemStack clone = stack.clone();
                clone.setAmount(remaining);
                inventory.setItem(slot, clone);
                remaining = 0;
                break;
            }

            if (!existing.isSimilar(stack)) {
                continue;
            }

            int maxStack = Math.min(existing.getMaxStackSize(), 64);
            int free = maxStack - existing.getAmount();
            if (free <= 0) {
                continue;
            }

            int moved = Math.min(free, remaining);
            existing.setAmount(existing.getAmount() + moved);
            remaining -= moved;
        }

        updateInfoItem(inventory);
        return originalAmount - remaining;
    }

    public SellPreview buildPreview(Inventory inventory) {
        int totalHeads = 0;
        double min = 0.0D;
        double max = 0.0D;
        Map<String, Integer> counters = new LinkedHashMap<>();

        for (int slot : getSellSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (!isSellableHead(item)) {
                continue;
            }

            HeadPriceRule rule = resolvePriceRule(item);
            if (rule == null) {
                continue;
            }

            int amount = item.getAmount();
            totalHeads += amount;
            min += rule.getMinPossible() * amount;
            max += rule.getMaxPossible() * amount;

            ItemMeta meta = item.getItemMeta();
            String animalName = plugin.getHarvestManager().getStoredAnimalName(meta);
            String rarity = plugin.getHarvestManager().getStoredRarity(meta);
            String key = animalName + " - " + rarity;

            counters.put(key, counters.getOrDefault(key, 0) + amount);
        }

        List<String> breakdown = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counters.entrySet()) {
            breakdown.add("&7- &f" + entry.getKey() + "&7: &b" + entry.getValue());
        }

        return new SellPreview(totalHeads, min, max, breakdown);
    }

    public SaleResult sellAll(Player player, Inventory inventory) {
        if (player == null || inventory == null || !plugin.hasEconomy()) {
            return new SaleResult(0, 0.0D);
        }

        int soldHeads = 0;
        double total = 0.0D;

        for (int slot : getSellSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (!isSellableHead(item)) {
                continue;
            }

            HeadPriceRule rule = resolvePriceRule(item);
            if (rule == null) {
                continue;
            }

            int amount = item.getAmount();
            soldHeads += amount;

            for (int i = 0; i < amount; i++) {
                total += rule.rollPrice();
            }

            inventory.setItem(slot, null);
        }

        updateInfoItem(inventory);

        if (soldHeads > 0 && total > 0.0D) {
            plugin.getEconomy().depositPlayer(player, total);
        }

        return new SaleResult(soldHeads, total);
    }

    public void returnItems(Player player, Inventory inventory) {
        if (player == null || inventory == null) {
            return;
        }

        for (int slot : getSellSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            inventory.setItem(slot, null);
            player.getInventory().addItem(item).values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover)
            );
        }
    }

    private HeadPriceRule resolvePriceRule(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        String rarity = plugin.getHarvestManager().getStoredRarity(meta);
        AnimalProfile profile = resolveProfile(meta);

        if (profile == null || !profile.isHeadSellingEnabled()) {
            return null;
        }

        return profile.getHeadPriceRule(rarity);
    }

    private AnimalProfile resolveProfile(ItemMeta meta) {
        String animalTypeKey = plugin.getHarvestManager().getStoredAnimalTypeKey(meta);

        if (animalTypeKey != null && !animalTypeKey.isBlank()) {
            try {
                EntityType type = EntityType.valueOf(animalTypeKey.toUpperCase());
                return plugin.getConfigManager().getAnimalsSettings().getProfile(type);
            } catch (IllegalArgumentException ignored) {
            }
        }

        String storedAnimalName = plugin.getHarvestManager().getStoredAnimalName(meta);
        if (storedAnimalName == null || storedAnimalName.isBlank()) {
            return null;
        }

        for (AnimalProfile profile : plugin.getConfigManager().getAnimalsSettings().getProfiles().values()) {
            if (profile.getDisplayName().equalsIgnoreCase(storedAnimalName)) {
                return profile;
            }
        }

        return null;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ColorUtil.colorize(name));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ColorUtil.colorize(line));
        }

        meta.setLore(coloredLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private Material parseMaterial(String input, Material fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }

        try {
            return Material.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public String formatMoney(double amount) {
        return "$" + moneyFormat.format(amount);
    }

    public record SellPreview(int totalHeads, double minTotal, double maxTotal, List<String> breakdown) {
    }

    public record SaleResult(int soldHeads, double total) {
    }
}