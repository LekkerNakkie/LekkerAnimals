package me.lekkernakkie.lekkeranimal.command;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.FeederTier;
import me.lekkernakkie.lekkeranimal.config.FeedstationSettings;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import me.lekkernakkie.lekkeranimal.data.HarvestDrop;
import me.lekkernakkie.lekkeranimal.data.HarvestLevelProfile;
import me.lekkernakkie.lekkeranimal.util.ColorUtil;
import me.lekkernakkie.lekkeranimal.util.FeederItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnimalsCommand implements TabExecutor {

    private static final List<String> ROOT_SUBCOMMANDS = List.of(
            "help",
            "headshop",
            "feeders",
            "head",
            "reload",
            "info"
    );

    private static final List<String> HEAD_RARITIES = List.of(
            "COMMON",
            "UNCOMMON",
            "RARE",
            "ULTRA",
            "LEGENDARY"
    );

    private final LekkerAnimal plugin;

    public AnimalsCommand(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sendHelp(sender, label);
                return true;
            }

            if (!player.hasPermission("lekkeranimal.command.animals")) {
                lang.send(player, "general.no-permission");
                return true;
            }

            plugin.getGuiManager().openAnimalsHome(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "help" -> {
                sendHelp(sender, label);
                return true;
            }

            case "headshop" -> {
                return handleHeadShop(sender, args, lang, label);
            }

            case "feeders" -> {
                return handleFeeders(sender, args, lang, label);
            }

            case "head" -> {
                return handleHead(sender, args, lang, label);
            }

            case "reload" -> {
                if (!sender.hasPermission("lekkeranimal.command.reload")) {
                    lang.send(sender, "general.no-permission");
                    return true;
                }

                plugin.reloadPluginState();
                lang.send(sender, "general.reload");
                return true;
            }

            case "info" -> {
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

            default -> {
                sendHelp(sender, label);
                return true;
            }
        }
    }

    private boolean handleHeadShop(CommandSender sender, String[] args, LangSettings lang, String label) {
        if (!plugin.hasEconomy()) {
            lang.send(sender, "heads.economy-missing");
            return true;
        }

        Player target;

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§eGebruik: /" + label + " headshop <player>");
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

            target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                lang.send(sender, "heads.player-not-found");
                return true;
            }
        }

        plugin.getHeadSellManager().openSellMenu(target);

        if (!sender.equals(target)) {
            lang.send(sender, "heads.sell-opened-for", Map.of("player", target.getName()));
        }

        return true;
    }

    private boolean handleFeeders(CommandSender sender, String[] args, LangSettings lang, String label) {
        FeedstationSettings settings = plugin.getConfigManager().getFeedstationSettings();

        if (!sender.hasPermission("lekkeranimal.command.animalfeeder")) {
            lang.send(sender, "feedstation.no-permission");
            return true;
        }

        if (settings == null || !settings.isEnabled()) {
            sender.sendMessage("§cFeedstations are disabled in feedstation.yml");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§eGebruik: /" + label + " feeders <player> <tier> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            lang.send(sender, "feedstation.invalid-player");
            return true;
        }

        FeederTier tier = FeederTier.fromString(args[2]);
        if (tier == null) {
            lang.send(sender, "feedstation.invalid-tier");
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
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
            target.getInventory().addItem(feederItem).values().forEach(leftover ->
                    target.getWorld().dropItemNaturally(target.getLocation(), leftover)
            );
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

    private boolean handleHead(CommandSender sender, String[] args, LangSettings lang, String label) {
        if (!sender.hasPermission("lekkeranimal.command.animalhead")) {
            lang.send(sender, "general.no-permission");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§eGebruik: /" + label + " head <player> <animal> [amount]");
            sender.sendMessage("§eGebruik: /" + label + " head <player> <animal> <rarity> [amount]");
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            lang.send(sender, "heads.player-not-found");
            return true;
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(args[2].toUpperCase(Locale.ROOT));
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

        if (args.length >= 4) {
            if (isInteger(args[3])) {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } else {
                rarity = args[3].toUpperCase(Locale.ROOT);

                if (args.length >= 5) {
                    if (!isInteger(args[4])) {
                        lang.send(sender, "heads.invalid-amount");
                        return true;
                    }
                    amount = Math.max(1, Integer.parseInt(args[4]));
                }
            }
        }

        String headOwner = "";
        String headTexture = "";
        HarvestDrop fallbackHeadDrop = null;

        for (HarvestLevelProfile harvestLevelProfile : profile.getHarvestProfiles().values()) {
            for (HarvestDrop drop : harvestLevelProfile.getDrops()) {
                if (drop.getMaterial() != Material.PLAYER_HEAD) {
                    continue;
                }

                if (fallbackHeadDrop == null) {
                    fallbackHeadDrop = drop;
                }

                if (!drop.getRarity().equalsIgnoreCase(rarity)) {
                    continue;
                }

                headOwner = drop.getHeadOwner();
                headTexture = drop.getHeadTexture();
                fallbackHeadDrop = drop;
                break;
            }

            if (!headOwner.isBlank() || !headTexture.isBlank()) {
                break;
            }
        }

        if ((headOwner.isBlank() && headTexture.isBlank()) && fallbackHeadDrop != null) {
            headOwner = fallbackHeadDrop.getHeadOwner();
            headTexture = fallbackHeadDrop.getHeadTexture();
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

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtil.colorize("&b&lAnimals Commands"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " &8- &fOpen het basis menu"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " help &8- &fToon dit help bericht"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " headshop [player] &8- &fOpen de head shop"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " feeders <player> <tier> [amount] &8- &fGeef feederbakken"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " head <player> <animal> [amount]"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " head <player> <animal> <rarity> [amount]"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " reload &8- &fReload de plugin"));
        sender.sendMessage(ColorUtil.colorize("&7/" + label + " info &8- &fPlugin informatie"));
        sender.sendMessage(ColorUtil.colorize("&8&m----------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return partialMatch(args[0], ROOT_SUBCOMMANDS);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "headshop" -> {
                if (args.length == 2) {
                    return partialMatch(args[1], getOnlinePlayerNames());
                }
            }

            case "feeders" -> {
                if (args.length == 2) {
                    return partialMatch(args[1], getOnlinePlayerNames());
                }
                if (args.length == 3) {
                    List<String> tiers = new ArrayList<>();
                    for (FeederTier tier : FeederTier.values()) {
                        tiers.add(tier.name());
                    }
                    return partialMatch(args[2], tiers);
                }
                if (args.length == 4) {
                    return partialMatch(args[3], List.of("1", "2", "5", "10", "16", "32", "64"));
                }
            }

            case "head" -> {
                if (args.length == 2) {
                    return partialMatch(args[1], getOnlinePlayerNames());
                }
                if (args.length == 3) {
                    return partialMatch(args[2], getEnabledAnimalTypes());
                }
                if (args.length == 4) {
                    List<String> values = new ArrayList<>(HEAD_RARITIES);
                    values.addAll(List.of("1", "2", "5", "10", "16", "32", "64"));
                    return partialMatch(args[3], values);
                }
                if (args.length == 5) {
                    return partialMatch(args[4], List.of("1", "2", "5", "10", "16", "32", "64"));
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> getEnabledAnimalTypes() {
        List<String> animals = new ArrayList<>();
        for (AnimalProfile profile : plugin.getConfigManager().getAnimalsSettings().getProfiles().values()) {
            if (profile != null && profile.isEnabled()) {
                animals.add(profile.getEntityType().name());
            }
        }
        animals.sort(String.CASE_INSENSITIVE_ORDER);
        return animals;
    }

    private List<String> partialMatch(String input, List<String> options) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();

        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }

        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
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