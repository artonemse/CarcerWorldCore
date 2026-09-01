package Cosmetics.KillEffects;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillEffectListener implements Listener {

    private final KillEffectManager killEffectManager;

    public KillEffectListener(KillEffectManager killEffectManager) {
        this.killEffectManager = killEffectManager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        killEffectManager.playKillEffect(killer, event.getEntity().getLocation());
    }
}
