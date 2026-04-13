package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LekkerAnimalsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lekkeranimal.command.reload")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            LekkerAnimal.getInstance().reloadConfig();
            sender.sendMessage("§aConfig reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§bLekkerAnimal §7v1.0");
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8--------------------------");
        sender.sendMessage("§bLekkerAnimal Help");
        sender.sendMessage("§e/lekkeranimals");
        sender.sendMessage("§e/lekkeranimals reload");
        sender.sendMessage("§e/lekkeranimals info");
        sender.sendMessage("§8--------------------------");
    }
}