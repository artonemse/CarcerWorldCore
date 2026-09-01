package Armor;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ArmorCombatListener implements Listener {

    private final ArmorManager armorManager;
    private final CombatHealthBarManager healthBarManager;

    public ArmorCombatListener(
            ArmorManager armorManager,
            CombatHealthBarManager healthBarManager
    ) {
        this.armorManager = armorManager;
        this.healthBarManager = healthBarManager;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onDamage(
            EntityDamageByEntityEvent event
    ) {

        Player attacker =
                getPlayerAttacker(event);

        if (attacker != null) {
            healthBarManager.markCombat(
                    attacker
            );
        }

        if (!(event.getEntity()
                instanceof Player victim)) {
            return;
        }

        healthBarManager.markCombat(
                victim
        );

        double multiplier =
                armorManager
                        .getIncomingDamageMultiplier(
                                victim
                        );

        event.setDamage(
                event.getDamage()
                        * multiplier
        );
    }

    private Player getPlayerAttacker(
            EntityDamageByEntityEvent event
    ) {

        if (event.getDamager()
                instanceof Player player) {

            return player;
        }

        if (event.getDamager()
                instanceof Projectile projectile) {

            ProjectileSource shooter =
                    projectile.getShooter();

            if (shooter
                    instanceof Player player) {

                return player;
            }
        }

        return null;
    }
}
