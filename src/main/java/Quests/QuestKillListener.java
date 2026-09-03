package Quests;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class QuestKillListener implements Listener {

    private final QuestManager questManager;

    public QuestKillListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster mob)) return;

        Player killer = mob.getKiller();
        if (killer == null) return;

        questManager.handleMobKill(killer, mob);
    }
}