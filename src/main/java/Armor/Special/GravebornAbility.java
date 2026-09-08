package Armor.Special;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;

public class GravebornAbility {

    private static final double RADIUS = 7.0;
    private static final double ABILITY_DAMAGE = 35.0;
    private static final double HEAL_PERCENT = 0.30;
    private static final double MAX_HEALING = 40.0;
    private static final long COOLDOWN = 25_000L;

    private final CarcerWorldCore plugin;
    private final NamespacedKey abilityDamageKey;

    public GravebornAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
    }

    public void cast(Player player) {
        Location center = player.getLocation().clone();
        List<LivingEntity> targets = findTargets(player, center);

        player.getWorld().playSound(center, Sound.ENTITY_WITHER_AMBIENT, 0.8f, 1.4f);
        player.getWorld().playSound(center, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.5f, 1.6f);
        player.getWorld().spawnParticle(Particle.SOUL, center.clone().add(0, 1, 0), 30, 1.5, 1.0, 1.5, 0.04);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, center.clone().add(0, 1, 0), 20, 1.0, 0.8, 1.0, 0.02);

        if (targets.isEmpty()) {
            player.sendMessage("§5§lGRAVEBORN §7§l| §fNo souls were close enough to reap.");
            return;
        }

        double totalDamage = 0.0;

        for (LivingEntity target : targets) {
            target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
            target.damage(ABILITY_DAMAGE, player);
            createSoulTrail(target, player);
            totalDamage += ABILITY_DAMAGE;
        }

        double healing = Math.min(totalDamage * HEAL_PERCENT, MAX_HEALING);
        healPlayer(player, healing);

        player.getWorld().playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.5f);
    }

    private List<LivingEntity> findTargets(Player player, Location center) {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : player.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target instanceof Player) continue;
            if (target.equals(player)) continue;

            targets.add(target);
        }

        return targets;
    }

    private void createSoulTrail(LivingEntity target, Player player) {
        Location start = target.getLocation().clone().add(0, target.getHeight() * 0.5, 0);

        new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || tick >= 12) {
                    cancel();
                    return;
                }

                Location end = player.getLocation().clone().add(0, 1.0, 0);
                double progress = tick / 12.0;

                Vector direction = end.toVector().subtract(start.toVector());
                Location point = start.clone().add(direction.multiply(progress));

                player.getWorld().spawnParticle(Particle.SOUL, point, 2, 0.08, 0.08, 0.08, 0.01);
                player.getWorld().spawnParticle(Particle.SCULK_SOUL, point, 1, 0.05, 0.05, 0.05, 0.0);

                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void healPlayer(Player player, double amount) {
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double newHealth = Math.min(maxHealth, player.getHealth() + amount);

        player.setHealth(newHealth);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().clone().add(0, 1.2, 0), 8, 0.5, 0.6, 0.5, 0.02);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.6f);
    }

    public long getCooldown() {
        return COOLDOWN;
    }
}
