package me.lekkernakkie.lekkeranimal.listener;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoOwnerChatListener implements Listener {

    private final LekkerAnimal plugin;
    private final Map<UUID, UUID> pendingAddByPlayer = new ConcurrentHashMap<>();

    public CoOwnerChatListener(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void startAddFlow(Player player, UUID entityUuid) {
        if (player == null || entityUuid == null) {
            return;
        }

        pendingAddByPlayer.put(player.getUniqueId(), entityUuid);
        player.closeInventory();
        player.sendMessage("§8[§bLekkerAnimal§8] §7Typ de naam van de speler in de chat. Typ §ccancel §7om te stoppen.");
    }

    public boolean isWaiting(UUID playerUuid) {
        return pendingAddByPlayer.containsKey(playerUuid);
    }

    public void cancel(UUID playerUuid) {
        pendingAddByPlayer.remove(playerUuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID entityUuid = pendingAddByPlayer.get(player.getUniqueId());
        if (entityUuid == null) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage().trim();
        if (message.equalsIgnoreCase("cancel")) {
            pendingAddByPlayer.remove(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage("§8[§bLekkerAnimal§8] §7Toevoegen van mede-eigenaar geannuleerd.")
            );
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> handleAdd(player, entityUuid, message));
    }

    private void handleAdd(Player player, UUID entityUuid, String targetName) {
        pendingAddByPlayer.remove(player.getUniqueId());

        LangSettings lang = plugin.getConfigManager().getLangSettings();
        MainSettings settings = plugin.getConfigManager().getMainSettings();

        if (!settings.isCoOwnersEnabled() || !settings.isCoOwnerGuiEnabled() || !settings.isCoOwnerChatInputEnabled()) {
            return;
        }

        Entity entity = plugin.getServer().getEntity(entityUuid);
        if (entity == null || !entity.isValid() || entity.isDead()) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Dit dier bestaat niet meer.");
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Geen dierdata gevonden.");
            return;
        }

        if (!data.isOwner(player.getUniqueId())) {
            lang.send(player, "general.not-your-animal");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.isOnline() && !target.hasPlayedBefore()) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Speler niet gevonden.");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        UUID targetUuid = target.getUniqueId();
        if (targetUuid == null) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Speler niet gevonden.");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (data.isOwner(targetUuid)) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Deze speler is al de eigenaar.");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (data.isCoOwner(targetUuid)) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Deze speler is al mede-eigenaar.");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (data.getCoOwnerCount() >= settings.getCoOwnersMaxPerAnimal()) {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Je hebt al het maximum aantal mede-eigenaars bereikt.");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (data.addCoOwner(targetUuid, settings.getCoOwnersMaxPerAnimal())) {
            plugin.getDataManager().saveAnimal(data);
            player.sendMessage("§8[§bLekkerAnimal§8] §7" + (target.getName() != null ? target.getName() : targetUuid) + " §7werd toegevoegd als mede-eigenaar.");
        } else {
            player.sendMessage("§8[§bLekkerAnimal§8] §7Toevoegen mislukt.");
        }

        plugin.getGuiManager().openCoOwnerMenu(player, entity);
    }
}