package Bosses;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.UUID;

public class BossListener implements Listener {

    private final BossManager bossManager;
    private final NamespacedKey minionOwnerKey;

    public BossListener(CarcerWorldCore plugin, BossManager bossManager) {
        this.bossManager = bossManager;
        this.minionOwnerKey = new NamespacedKey(plugin, "boss_minion_owner");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity victim) {
            BossInstance instance = bossManager.getEncounterByEntity(victim);

            if (instance != null) {
                Player attacker = getResponsiblePlayer(event.getDamager());

                if (attacker == null || !attacker.getUniqueId().equals(instance.getOwnerId())) event.setCancelled(true);

                return;
            }

            UUID minionOwner = getMinionOwner(victim);

            if (minionOwner != null) {
                Player attacker = getResponsiblePlayer(event.getDamager());

                if (attacker == null || !attacker.getUniqueId().equals(minionOwner)) event.setCancelled(true);
            }
        }

        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof LivingEntity damager)) return;

        BossInstance bossInstance = bossManager.getEncounterByEntity(damager);

        if (bossInstance != null && !player.getUniqueId().equals(bossInstance.getOwnerId())) {
            event.setCancelled(true);
            return;
        }

        UUID minionOwner = getMinionOwner(damager);

        if (minionOwner != null && !player.getUniqueId().equals(minionOwner)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnvironmentalBossDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!bossManager.isBoss(entity)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        BossInstance instance = bossManager.getEncounterByEntity(entity);

        if (instance != null) {
            Player owner = instance.getOwner();

            if (owner == null) {
                event.setCancelled(true);
                return;
            }

            if (event.getTarget() == null || !event.getTarget().getUniqueId().equals(owner.getUniqueId())) event.setTarget(owner);

            return;
        }

        UUID minionOwner = getMinionOwner(entity);

        if (minionOwner == null) return;

        Player owner = org.bukkit.Bukkit.getPlayer(minionOwner);

        if (owner == null) {
            event.setCancelled(true);
            return;
        }

        if (event.getTarget() == null || !event.getTarget().getUniqueId().equals(owner.getUniqueId())) event.setTarget(owner);
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        BossInstance instance = bossManager.getEncounterByEntity(entity);

        if (instance == null) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        bossManager.handleBossDeath(entity, entity.getKiller());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        bossManager.stopEncounter(event.getEntity(), "&cThe encounter has been reset.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        bossManager.stopEncounter(event.getPlayer(), null);
    }

    private Player getResponsiblePlayer(Entity damager) {
        if (damager instanceof Player player) return player;

        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;

        return null;
    }

    private UUID getMinionOwner(LivingEntity entity) {
        String value = entity.getPersistentDataContainer().get(minionOwnerKey, PersistentDataType.STRING);

        if (value == null) return null;

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
