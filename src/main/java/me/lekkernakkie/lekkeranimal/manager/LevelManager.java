package me.lekkernakkie.lekkeranimal.manager;

import me.lekkernakkie.lekkeranimal.LekkerAnimal;
import me.lekkernakkie.lekkeranimal.config.LangSettings;
import me.lekkernakkie.lekkeranimal.config.MainSettings;
import me.lekkernakkie.lekkeranimal.data.AnimalData;
import me.lekkernakkie.lekkeranimal.data.AnimalProfile;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;

public class LevelManager {

    private final LekkerAnimal plugin;

    public LevelManager(LekkerAnimal plugin) {
        this.plugin = plugin;
    }

    public void addXp(AnimalData data, int amount) {
        MainSettings settings = plugin.getConfigManager().getMainSettings();
        if (!settings.isLevelingEnabled() || amount <= 0) {
            return;
        }

        data.setXp(data.getXp() + amount);

        while (data.getLevel() < settings.getMaxLevel() && data.getXp() >= getRequiredXpForNextLevel(data.getLevel(), settings)) {
            data.setXp(data.getXp() - getRequiredXpForNextLevel(data.getLevel(), settings));
            data.setLevel(data.getLevel() + 1);

            Player owner = Bukkit.getPlayer(data.getOwnerUuid());
            AnimalProfile profile = plugin.getConfigManager().getAnimalsSettings().getProfile(data.getEntityType());
            LangSettings lang = plugin.getConfigManager().getLangSettings();

            if (owner != null && owner.isOnline() && profile != null) {
                lang.send(owner, "leveling.level-up", Map.of(
                        "animal", profile.getDisplayName(),
                        "level", String.valueOf(data.getLevel())
                ));

                try {
                    Sound sound = Sound.valueOf(plugin.getConfig().getString("sounds.levelup", "ENTITY_EXPERIENCE_ORB_PICKUP"));
                    owner.playSound(owner.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public int getRequiredXpForNextLevel(int currentLevel, MainSettings settings) {
        return (int) Math.ceil(settings.getBaseXp() * Math.pow(settings.getXpScale(), Math.max(0, currentLevel - 1)));
    }
}