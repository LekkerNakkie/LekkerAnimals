package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.HarvestDrop;
import me.lekkernakkie.lekkeranimal.data.HarvestLevelProfile;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

public class AnimalHeadCommand implements CommandExecutor {

    private final LekkerAnimal plugin;

    public AnimalHeadCommand(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (!sender.hasPermission("lekkeranimal.command.animalhead")) {
            lang.send(sender, "general.no-permission");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("/animalhead <player> <animal> [amount]");
            sender.sendMessage("/animalhead <player> <animal> <rarity> [amount]");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            lang.send(sender, "heads.player-not-found");
            return true;
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            lang.send(sender, "heads.invalid-animal");
            return true;
        }

        AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(entityType);
        if (profile == null || !profile.isEnabled()) {
            lang.send(sender, "heads.invalid-animal");
            return true;
        }

        String rarity = "COMMON";
        int amount = 1;

        if (args.length >= 3) {
            if (isInteger(args[2])) {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } else {
                rarity = args[2].toUpperCase(Locale.ROOT);

                if (args.length >= 4) {
                    if (!isInteger(args[3])) {
                        lang.send(sender, "heads.invalid-amount");
                        return true;
                    }
                    amount = Math.max(1, Integer.parseInt(args[3]));
                }
            }
        }

        String headOwner = "";
        String headTexture = "";

        for (HarvestLevelProfile harvestLevelProfile : profile.getHarvestProfiles().values()) {
            for (HarvestDrop drop : harvestLevelProfile.getDrops()) {
                if (drop.getMaterial() != Material.PLAYER_HEAD) {
                    continue;
                }

                if (!drop.getRarity().equalsIgnoreCase(rarity)) {
                    continue;
                }

                headOwner = drop.getHeadOwner();
                headTexture = drop.getHeadTexture();
                break;
            }
        }

        ItemStack item = plugin.getHarvestManager().createPersistentHeadItem(
                rarity,
                entityType.name(),
                profile.getDisplayName(),
                headOwner,
                headTexture
        );
        item.setAmount(Math.min(64, amount));

        target.getInventory().addItem(item).values().forEach(leftover ->
                target.getWorld().dropItemNaturally(target.getLocation(), leftover)
        );

        lang.send(sender, "heads.given", Map.of(
                "player", target.getName(),
                "animal", profile.getDisplayName(),
                "rarity", rarity,
                "amount", String.valueOf(amount)
        ));

        if (!sender.equals(target)) {
            lang.send(target, "heads.received", Map.of(
                    "animal", profile.getDisplayName(),
                    "rarity", rarity,
                    "amount", String.valueOf(amount)
            ));
        }

        return true;
    }

    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}