package Armor.Special;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TidecallerAbility implements Listener {

    private static final double DAMAGE = 50.0;
    private static final double HIT_RADIUS = 2.2;
    private static final double DASH_SPEED = 1.65;
    private static final int DASH_TICKS = 8;
    private static final long FALL_PROTECTION_TICKS = 80L;

    private static final Particle.DustOptions WATER_DUST = new Particle.DustOptions(Color.fromRGB(40, 190, 255), 1.15f);
    private static final Particle.DustOptions LIGHT_WATER_DUST = new Particle.DustOptions(Color.fromRGB(130, 235, 255), 0.9f);

    private final CarcerWorldCore plugin;
    private final NamespacedKey abilityDamageKey;
    private final Set<UUID> fallProtected = new HashSet<>();
    private final Set<UUID> activeDashes = new HashSet<>();

    public TidecallerAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
    }

    public void cast(Player player) {
        if (activeDashes.contains(player.getUniqueId())) return;

        Vector direction = player.getLocation().getDirection().clone();

        direction.setY(Math.max(0.08, direction.getY() * 0.25));

        if (direction.lengthSquared() <= 0) direction = new Vector(0, 0.08, 1);

        direction.normalize();

        activeDashes.add(player.getUniqueId());
        fallProtected.add(player.getUniqueId());

        playStartEffect(player);
        startDash(player, direction);

        Bukkit.getScheduler().runTaskLater(plugin, () -> fallProtected.remove(player.getUniqueId()), FALL_PROTECTION_TICKS);
    }

    private void startDash(Player player, Vector direction) {
        World castWorld = player.getWorld();
        Set<UUID> hitTargets = new HashSet<>();

        new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || !player.getWorld().equals(castWorld)) {
                    activeDashes.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                Vector velocity = direction.clone().multiply(DASH_SPEED);

                if (tick == 0) velocity.setY(Math.max(0.20, velocity.getY()));

                player.setVelocity(velocity);

                createDashTrail(player);
                damageNearbyEnemies(player, hitTargets, direction);

                if (tick >= DASH_TICKS) {
                    activeDashes.remove(player.getUniqueId());
                    createImpactWave(player);
                    cancel();
                    return;
                }

                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void damageNearbyEnemies(Player player, Set<UUID> hitTargets, Vector dashDirection) {
        Location center = player.getLocation().clone().add(0, 0.8, 0);

        for (Entity entity : player.getWorld().getNearbyEntities(center, HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (!(target instanceof Monster)) continue;
            if (target.isDead()) continue;
            if (hitTargets.contains(target.getUniqueId())) continue;

            hitTargets.add(target.getUniqueId());

            dealAbilityDamage(player, target, DAMAGE);
            launchTarget(player, target, dashDirection);

            Location hit = target.getLocation().clone().add(0, 1.0, 0);

            player.getWorld().spawnParticle(Particle.SPLASH, hit, 25, 0.5, 0.7, 0.5, 0.20);
            player.getWorld().spawnParticle(Particle.BUBBLE_POP, hit, 18, 0.5, 0.6, 0.5, 0.15);
            player.getWorld().spawnParticle(Particle.DUST, hit, 15, 0.4, 0.5, 0.4, 0.03, LIGHT_WATER_DUST);

            player.getWorld().playSound(hit, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.8f, 1.25f);
        }
    }

    private void launchTarget(Player player, LivingEntity target, Vector dashDirection) {
        Vector horizontalDash = dashDirection.clone();
        horizontalDash.setY(0);

        if (horizontalDash.lengthSquared() <= 0) horizontalDash = new Vector(0, 0, 1);

        horizontalDash.normalize();

        Vector side = new Vector(-horizontalDash.getZ(), 0, horizontalDash.getX());

        Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());

        if (side.dot(toTarget) < 0) side.multiply(-1);

        Vector launch = side.multiply(0.85);
        launch.add(horizontalDash.clone().multiply(0.25));
        launch.setY(0.55);

        target.setVelocity(launch);
    }

    private void createDashTrail(Player player) {
        Location center = player.getLocation().clone().add(0, 0.8, 0);

        player.getWorld().spawnParticle(Particle.SPLASH, center, 18, 0.55, 0.75, 0.55, 0.18);
        player.getWorld().spawnParticle(Particle.BUBBLE_POP, center, 10, 0.45, 0.6, 0.45, 0.12);
        player.getWorld().spawnParticle(Particle.DUST, center, 12, 0.5, 0.7, 0.5, 0.03, WATER_DUST);

        createWaterRing(center);
    }

    private void createWaterRing(Location center) {
        Vector direction = center.getDirection().clone();

        direction.setY(0);

        if (direction.lengthSquared() <= 0) direction = new Vector(0, 0, 1);

        direction.normalize();

        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2.0 * i) / 12.0;
            double side = Math.cos(angle) * 1.0;
            double height = Math.sin(angle) * 1.0;

            Location point = center.clone();

            point.add(right.clone().multiply(side));
            point.add(0, height, 0);

            center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, LIGHT_WATER_DUST);

            if (i % 3 == 0) center.getWorld().spawnParticle(Particle.SPLASH, point, 2, 0.05, 0.05, 0.05, 0.05);
        }
    }

    private void playStartEffect(Player player) {
        Location center = player.getLocation().clone().add(0, 1.0, 0);

        player.getWorld().spawnParticle(Particle.SPLASH, center, 45, 1.0, 1.1, 1.0, 0.25);
        player.getWorld().spawnParticle(Particle.BUBBLE_POP, center, 30, 0.8, 1.0, 0.8, 0.15);
        player.getWorld().spawnParticle(Particle.DUST, center, 30, 0.9, 1.0, 0.9, 0.04, WATER_DUST);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.2f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 1.15f);
    }

    private void createImpactWave(Player player) {
        Location center = player.getLocation().clone().add(0, 0.5, 0);

        player.getWorld().spawnParticle(Particle.SPLASH, center, 80, 2.0, 0.8, 2.0, 0.35);
        player.getWorld().spawnParticle(Particle.BUBBLE_POP, center, 45, 1.8, 0.7, 1.8, 0.25);
        player.getWorld().spawnParticle(Particle.DUST, center, 45, 1.7, 0.6, 1.7, 0.05, WATER_DUST);

        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * 1.5;

            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2.0 * i) / 24.0;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                Location point = center.clone().add(x, 0.15, z);

                player.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, LIGHT_WATER_DUST);

                if (i % 3 == 0) player.getWorld().spawnParticle(Particle.SPLASH, point, 2, 0.05, 0.1, 0.05, 0.05);
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.4f, 0.85f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.2f, 0.75f);
    }

    private void dealAbilityDamage(Player player, LivingEntity target, double damage) {
        target.setNoDamageTicks(0);

        target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
        target.damage(damage, player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isValid()) target.getPersistentDataContainer().remove(abilityDamageKey);
        });
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!fallProtected.contains(player.getUniqueId())) return;

        event.setCancelled(true);
        fallProtected.remove(player.getUniqueId());
    }

    public void shutdown() {
        activeDashes.clear();
        fallProtected.clear();
    }
}
