package Listeners;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawnCancelListener implements Listener {

    @EventHandler
    public void onNaturalMobSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Phantom) {
            event.setCancelled(true);
            return;
        }

        if (!(event.getEntity() instanceof Monster)) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        event.setCancelled(true);
    }
}