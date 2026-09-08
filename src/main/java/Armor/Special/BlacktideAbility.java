package Armor.Special;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlacktideAbility {

    private static final double CAST_DISTANCE = 6.0;
    private static final double RADIUS = 8.0;
    private static final int MAX_TARGETS = 10;
    private static final double PULL_DAMAGE = 15.0;
    private static final double COLLAPSE_DAMAGE = 50.0;
    private static final int PULL_DURATION = 40;
    private static final long COOLDOWN = 30_000L;

    private final CarcerWorldCore plugin;
    private final NamespacedKey abilityDamageKey;

    public BlacktideAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
    }

    public void cast(Player player) {
        Location center = getVortexCenter(player);
        List<LivingEntity> targets = findTargets(center);

        createOpeningEffect(center);

        center.getWorld().playSound(center, Sound.BLOCK_CONDUIT_ACTIVATE, 1.3f, 0.55f);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.65f);

        for (LivingEntity target : targets) dealAbilityDamage(player, target, PULL_DAMAGE);

        startVortex(player, center, targets);
    }

    private void startVortex(Player player, Location center, List<LivingEntity> targets) {
        new BukkitRunnable() {

            private int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                if (!player.getWorld().equals(center.getWorld())) {
                    cancel();
                    return;
                }

                createVortexEffect(center, ticks);
                pullTargets(center, targets, ticks);

                ticks += 2;

                if (ticks >= PULL_DURATION) {
                    collapse(player, center, targets);
                    cancel();
                }
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    private List<LivingEntity> findTargets(Location center) {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (!(target instanceof Monster)) continue;
            if (target.isDead()) continue;

            targets.add(target);
        }

        targets.sort(Comparator.comparingDouble(target -> target.getLocation().distanceSquared(center)));

        if (targets.size() > MAX_TARGETS) return new ArrayList<>(targets.subList(0, MAX_TARGETS));

        return targets;
    }

    private void pullTargets(Location center, List<LivingEntity> targets, int ticks) {
        double progress = Math.min(1.0, ticks / (double) PULL_DURATION);
        double pullStrength = 0.30 + (progress * 0.35);
        double spiralStrength = 0.28 * (1.0 - progress);

        for (LivingEntity target : targets) {
            if (!target.isValid() || target.isDead()) continue;
            if (!target.getWorld().equals(center.getWorld())) continue;

            Vector towardCenter = center.toVector().subtract(target.getLocation().toVector());
            Vector horizontal = new Vector(towardCenter.getX(), 0, towardCenter.getZ());

            if (horizontal.lengthSquared() <= 0.20) {
                target.setVelocity(new Vector(0, 0.08, 0));
                continue;
            }

            horizontal.normalize();

            Vector spiral = new Vector(-horizontal.getZ(), 0, horizontal.getX()).multiply(spiralStrength);
            Vector velocity = horizontal.multiply(pullStrength).add(spiral);

            velocity.setY(0.08 + (progress * 0.06));

            target.setVelocity(velocity);

            Location mobCenter = target.getLocation().clone().add(0, target.getHeight() * 0.5, 0);

            target.getWorld().spawnParticle(Particle.PORTAL, mobCenter, 3, 0.15, 0.25, 0.15, 0.04);
            target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation().clone().add(0, 0.2, 0), 3, 0.2, 0.1, 0.2, 0.03);
        }
    }

    private void collapse(Player player, Location center, List<LivingEntity> targets) {
        center.getWorld().spawnParticle(Particle.EXPLOSION, center.clone().add(0, 0.6, 0), 4, 0.8, 0.5, 0.8, 0);
        center.getWorld().spawnParticle(Particle.SPLASH, center.clone().add(0, 0.5, 0), 90, 2.5, 1.0, 2.5, 0.15);
        center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0, 0.7, 0), 80, 2.0, 1.0, 2.0, 0.30);
        center.getWorld().spawnParticle(Particle.SCULK_SOUL, center.clone().add(0, 0.8, 0), 35, 1.5, 0.8, 1.5, 0.05);
        center.getWorld().spawnParticle(Particle.NAUTILUS, center.clone().add(0, 0.6, 0), 40, 1.8, 0.8, 1.8, 0.08);
        center.getWorld().spawnParticle(Particle.LARGE_SMOKE, center.clone().add(0, 0.5, 0), 30, 1.4, 0.6, 1.4, 0.04);

        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.3f, 0.55f);
        center.getWorld().playSound(center, Sound.BLOCK_CONDUIT_ACTIVATE, 1.2f, 1.35f);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1.2f, 0.5f);

        for (LivingEntity target : targets) {
            if (!target.isValid() || target.isDead()) continue;
            if (!target.getWorld().equals(center.getWorld())) continue;
            if (target.getLocation().distanceSquared(center) > Math.pow(RADIUS + 2.0, 2)) continue;

            dealAbilityDamage(player, target, COLLAPSE_DAMAGE);

            if (target.isDead()) continue;

            Vector knockback = target.getLocation().toVector().subtract(center.toVector());
            knockback.setY(0);

            if (knockback.lengthSquared() > 0) knockback.normalize();

            knockback.multiply(0.35);
            knockback.setY(0.55);

            target.setVelocity(knockback);
        }
    }

    private void createOpeningEffect(Location center) {
        center.getWorld().spawnParticle(Particle.SPLASH, center.clone().add(0, 0.3, 0), 45, 2.0, 0.4, 2.0, 0.12);
        center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0, 0.5, 0), 40, 1.6, 0.5, 1.6, 0.25);
        center.getWorld().spawnParticle(Particle.SCULK_SOUL, center.clone().add(0, 0.5, 0), 15, 1.0, 0.3, 1.0, 0.03);
        center.getWorld().spawnParticle(Particle.LARGE_SMOKE, center.clone().add(0, 0.4, 0), 20, 1.0, 0.3, 1.0, 0.03);
    }

    private void createVortexEffect(Location center, int ticks) {
        double rotation = ticks * 0.22;

        createRing(center.clone().add(0, 0.15, 0), 3.8, 28, rotation, Particle.SPLASH);
        createRing(center.clone().add(0, 0.30, 0), 3.0, 24, -rotation * 1.25, Particle.PORTAL);
        createRing(center.clone().add(0, 0.45, 0), 2.1, 18, rotation * 1.55, Particle.NAUTILUS);
        createRing(center.clone().add(0, 0.60, 0), 1.2, 12, -rotation * 2.0, Particle.SCULK_SOUL);

        center.getWorld().spawnParticle(Particle.BUBBLE_POP, center.clone().add(0, 0.4, 0), 10, 0.8, 0.3, 0.8, 0.05);
        center.getWorld().spawnParticle(Particle.LARGE_SMOKE, center.clone().add(0, 0.4, 0), 4, 0.4, 0.2, 0.4, 0.02);

        if (ticks % 8 == 0) center.getWorld().playSound(center, Sound.BLOCK_CONDUIT_AMBIENT, 0.5f, 0.55f);
    }

    private void createRing(Location center, double radius, int particleCount, double rotation, Particle particle) {
        for (int i = 0; i < particleCount; i++) {
            double angle = ((Math.PI * 2.0 * i) / particleCount) + rotation;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            center.getWorld().spawnParticle(particle, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
        }
    }

    private void dealAbilityDamage(Player player, LivingEntity target, double damage) {
        target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
        target.damage(damage, player);
    }

    private Location getVortexCenter(Player player) {
        Vector direction = player.getLocation().getDirection().clone();
        direction.setY(0);

        if (direction.lengthSquared() <= 0) direction = new Vector(0, 0, 1);

        direction.normalize();

        Location center = player.getLocation().clone().add(direction.multiply(CAST_DISTANCE));

        return findGround(center);
    }

    private Location findGround(Location location) {
        Location check = location.clone().add(0, 4, 0);

        for (int i = 0; i < 12; i++) {
            Location below = check.clone().subtract(0, 1, 0);

            if (check.getBlock().isPassable() && below.getBlock().getType().isSolid()) {
                return check.add(0, 0.1, 0);
            }

            check.subtract(0, 1, 0);
        }

        return location;
    }

    public long getCooldown() {
        return COOLDOWN;
    }
}
