package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.util.FeederItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class AnimalFeederCommand implements CommandExecutor {

    private final LekkerAnimal plugin;

    public AnimalFeederCommand(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

        if (!sender.hasPermission("lekkeranimal.command.animalfeeder")) {
            lang.send(sender, "feedstation.no-permission");
            return true;
        }

        if (settings == null || !settings.isEnabled()) {
            sender.sendMessage("§cFeedstations are disabled in feedstation.yml");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eGebruik: /animalfeeder <player> <tier> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            lang.send(sender, "feedstation.invalid-player");
            return true;
        }

        FeederTier tier = FeederTier.fromString(args[1]);
        if (tier == null) {
            lang.send(sender, "feedstation.invalid-tier");
            return true;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException ignored) {
                amount = 1;
            }
        }

        for (int i = 0; i < amount; i++) {
            ItemStack feederItem = FeederItemUtil.createFeederItem(
                    plugin,
                    target.getUniqueId(),
                    target.getName(),
                    tier,
                    List.of(),
                    true
            );
            target.getInventory().addItem(feederItem);
        }

        String tierDisplay = settings.getTierSettings(tier) != null
                ? settings.getTierSettings(tier).display()
                : tier.name();

        lang.send(sender, "feedstation.gave-feeder", Map.of(
                "player", target.getName(),
                "amount", String.valueOf(amount),
                "tier_display", tierDisplay
        ));

        if (!sender.equals(target)) {
            lang.send(target, "feedstation.received-feeder", Map.of(
                    "amount", String.valueOf(amount),
                    "tier_display", tierDisplay
            ));
        }

        return true;
    }
}