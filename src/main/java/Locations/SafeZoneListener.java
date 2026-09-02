package Locations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class SafeZoneListener implements Listener {

    private final CarcerWorldCore plugin;

    public SafeZoneListener(CarcerWorldCore plugin) {
        this.plugin = plugin;
        startMobCheck();
    }

    @EventHandler
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (!(event.getTarget() instanceof Player player)) return;
        if (!plugin.getNamedLocationManager().isSafeZone(player.getLocation())) return;

        event.setCancelled(true);
        event.setTarget(null);
    }

    private void startMobCheck() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Monster monster : world.getEntitiesByClass(Monster.class)) {
                    if (!plugin.getNamedLocationManager().isSafeZone(monster.getLocation())) continue;
                    monster.remove();
                }
            }
        }, 20L, 10L);
    }
}
