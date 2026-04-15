package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class LekkerAnimalsCommand implements CommandExecutor {

    private final LekkerAnimal plugin;

    public LekkerAnimalsCommand(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, lang);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lekkeranimal.command.reload")) {
                lang.send(sender, "general.no-permission");
                return true;
            }

            plugin.reloadPluginState();
            lang.send(sender, "general.reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            lang.send(sender, "info.header");
            lang.send(sender, "info.title");
            lang.send(sender, "info.version", Map.of("version", plugin.getDescription().getVersion()));
            lang.send(sender, "info.animals-loaded", Map.of(
                    "amount", String.valueOf(plugin.getConfigManager().getAnimalsSettings().getLoadedAmount())
            ));
            lang.send(sender, "info.bonded-animals", Map.of(
                    "amount", String.valueOf(plugin.getAnimalManager().getBondedCount())
            ));
            lang.send(sender, "info.footer");
            return true;
        }

        sendHelp(sender, lang);
        return true;
    }

    private void sendHelp(CommandSender sender, LangSettings lang) {
        lang.send(sender, "help.header");
        lang.send(sender, "help.title");
        lang.send(sender, "help.line1");
        lang.send(sender, "help.line2");
        lang.send(sender, "help.line3");
        lang.send(sender, "help.footer");
    }
}