package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnimalsCommand implements CommandExecutor {

    private final LekkerAnimal plugin;

    public AnimalsCommand(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("lekkeranimal.command.animals")) {
            player.sendMessage("§cNo permission.");
            return true;
        }

        plugin.getGuiManager().openAnimalsList(player, false, 0);
        return true;
    }
}