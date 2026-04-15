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

        plugin.getConfigManager().getLangSettings().send(player, "co-owners.chat-enter-name");
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
        LangSettings lang = plugin.getConfigManager().getLangSettings();

        if (message.equalsIgnoreCase("cancel")) {
            pendingAddByPlayer.remove(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () ->
                    lang.send(player, "co-owners.chat-cancelled")
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
            lang.send(player, "co-owners.animal-missing");
            return;
        }

        AnimalData data = plugin.getAnimalManager().getAnimalData(entity);
        if (data == null) {
            lang.send(player, "co-owners.animal-missing");
            return;
        }

        if (!data.isOwner(player.getUniqueId())) {
            lang.send(player, "general.not-your-animal");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.isOnline() && !target.hasPlayedBefore()) {
            lang.send(player, "co-owners.player-not-found");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        UUID targetUuid = target.getUniqueId();
        if (targetUuid == null) {
            lang.send(player, "co-owners.player-not-found");
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        String resolvedName = target.getName() != null ? target.getName() : targetUuid.toString();

        if (data.isOwner(targetUuid)) {
            lang.send(player, "co-owners.already-owner", Map.of(
                    "player", resolvedName
            ));
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (data.isCoOwner(targetUuid)) {
            lang.send(player, "co-owners.already-co-owner", Map.of(
                    "player", resolvedName
            ));
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        int maxCoOwners = settings.getEffectiveCoOwnersMax(player);

        if (data.getCoOwnerCount() >= maxCoOwners) {
            lang.send(player, "co-owners.max-reached", Map.of(
                    "max", String.valueOf(maxCoOwners)
            ));
            plugin.getGuiManager().openCoOwnerMenu(player, entity);
            return;
        }

        if (data.addCoOwner(targetUuid, maxCoOwners)) {
            plugin.getDataManager().saveAnimal(data);
            lang.send(player, "co-owners.added", Map.of(
                    "player", resolvedName
            ));
        } else {
            lang.send(player, "co-owners.add-failed", Map.of(
                    "player", resolvedName
            ));
        }

        plugin.getGuiManager().openCoOwnerMenu(player, entity);
    }
}