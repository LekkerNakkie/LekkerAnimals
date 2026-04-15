package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnimalHeadSellCommand implements CommandExecutor {

    private final LekkerAnimal plugin;

    public AnimalHeadSellCommand(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (!plugin.hasEconomy()) {
            lang.send(sender, "heads.economy-missing");
            return true;
        }

        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("/animalheadsell <player>");
                return true;
            }

            if (!player.hasPermission("lekkeranimal.command.animalheadsell")) {
                lang.send(player, "general.no-permission");
                return true;
            }

            target = player;
        } else {
            if (!sender.hasPermission("lekkeranimal.command.animalheadsell.others")) {
                lang.send(sender, "general.no-permission");
                return true;
            }

            target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                lang.send(sender, "heads.player-not-found");
                return true;
            }
        }

        plugin.getHeadSellManager().openSellMenu(target);

        if (!sender.equals(target)) {
            lang.send(sender, "heads.sell-opened-for", java.util.Map.of("player", target.getName()));
        }

        return true;
    }
}