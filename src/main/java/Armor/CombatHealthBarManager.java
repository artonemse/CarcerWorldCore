package Armor;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatHealthBarManager {

    private static final long COMBAT_TIME = 7000L;
    private static final int BAR_LENGTH = 20;

    private final Map<UUID, Long> combatTimes = new HashMap<>();
    private final BukkitTask task;

    public CombatHealthBarManager(CarcerWorldCore plugin) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 2L, 2L);
    }

    public void markCombat(Player player) {
        combatTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isInCombat(Player player) {
        Long lastCombat = combatTimes.get(player.getUniqueId());

        if (lastCombat == null) return false;

        return System.currentTimeMillis() - lastCombat <= COMBAT_TIME;
    }

    public void remove(Player player) {
        combatTimes.remove(player.getUniqueId());
        clear(player);
    }

    public void shutdown() {
        task.cancel();

        for (Player player : Bukkit.getOnlinePlayers()) {
            clear(player);
        }

        combatTimes.clear();
    }

    private void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!isInCombat(player)) {
                if (combatTimes.remove(player.getUniqueId()) != null) {
                    clear(player);
                }

                continue;
            }

            show(player);
        }
    }

    private void show(Player player) {
        double health = Math.max(0, player.getHealth());
        double maxHealth = Math.max(1, player.getMaxHealth());

        double ratio = Math.max(0, Math.min(1, health / maxHealth));

        int filled = (int) Math.round(ratio * BAR_LENGTH);
        int empty = BAR_LENGTH - filled;

        StringBuilder bar = new StringBuilder();

        bar.append("&c❤ &f")
                .append(Math.round(health))
                .append("&7/")
                .append(Math.round(maxHealth))
                .append(" ");

        bar.append("&c");

        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }

        bar.append("&8");

        for (int i = 0; i < empty; i++) {
            bar.append("░");
        }

        sendActionBar(player, color(bar.toString()));
    }

    private void clear(Player player) {
        sendActionBar(player, "");
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(message)
        );
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}