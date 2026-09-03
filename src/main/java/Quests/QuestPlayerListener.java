package Quests;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class QuestPlayerListener implements Listener {

    private final CarcerWorldCore plugin;
    private final QuestManager questManager;

    public QuestPlayerListener(CarcerWorldCore plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> questManager.ensureMainQuests(event.getPlayer()), 1L);
    }
}
