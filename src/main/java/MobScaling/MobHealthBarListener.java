package MobScaling;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class MobHealthBarListener implements Listener {

    private final CarcerWorldCore plugin;
    private final MobHealthBarManager healthBarManager;

    public MobHealthBarListener(CarcerWorldCore plugin, MobHealthBarManager healthBarManager) {
        this.plugin = plugin;
        this.healthBarManager = healthBarManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (mob instanceof Player) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!mob.isValid() || mob.isDead()) return;
            healthBarManager.updateHealthBar(mob);
        });
    }
}
