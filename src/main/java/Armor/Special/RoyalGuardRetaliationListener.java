package Armor.Special;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class RoyalGuardRetaliationListener implements Listener {

    private final RoyalGuardAbility royalGuardAbility;
    private final NamespacedKey abilityDamageKey;

    public RoyalGuardRetaliationListener(CarcerWorldCore plugin, RoyalGuardAbility royalGuardAbility) {
        this.royalGuardAbility = royalGuardAbility;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!royalGuardAbility.isRetaliationActive(player)) return;

        LivingEntity attacker = getHostileAttacker(event);

        if (attacker == null) return;
        if (attacker.isDead()) return;

        double reflectedDamage = event.getDamage();

        event.setCancelled(true);

        royalGuardAbility.playShieldImpact(player, attacker.getLocation());

        if (reflectedDamage <= 0) return;

        attacker.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
        attacker.damage(reflectedDamage, player);
    }

    private LivingEntity getHostileAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Monster monster) return monster;

        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Monster monster) return monster;
        }

        return null;
    }
}
