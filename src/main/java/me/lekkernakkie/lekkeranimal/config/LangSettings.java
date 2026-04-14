package me.lekkernakkie.lekkeranimal.config;

import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class LangSettings {

    private final FileConfiguration config;
    private final String prefix;

    public LangSettings(FileConfiguration config) {
        this.config = config;
        this.prefix = ColorUtil.colorize(config.getString("prefix", "&8[&bLekkerAnimal&8] &7"));
    }

    public String getRaw(String path) {
        return config.getString(path, path);
    }

    public String get(String path) {
        String value = config.getString(path, path);
        value = applyPrefix(value);
        return ColorUtil.colorize(value);
    }

    public String get(String path, Map<String, String> placeholders) {
        String value = config.getString(path, path);
        value = applyPrefix(value);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return ColorUtil.colorize(value);
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(get(path, placeholders));
    }

    public String getPrefix() {
        return prefix;
    }

    private String applyPrefix(String input) {
        return input.replace("{prefix}", prefix);
    }
}