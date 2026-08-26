package Weapons;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class MobKillListener implements Listener {

    private static final long EXP_PER_MOB = 10;

    private final CarcerWorldCore plugin;
    private final WeaponProgressionManager progressionManager;

    public MobKillListener(
            CarcerWorldCore plugin,
            WeaponProgressionManager progressionManager
    ) {
        this.plugin = plugin;
        this.progressionManager = progressionManager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            return;
        }

        progressionManager.addMobKill(killer);
        progressionManager.addWeaponExp(killer, EXP_PER_MOB);
    }
}
