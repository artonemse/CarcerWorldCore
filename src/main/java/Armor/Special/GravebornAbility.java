package Armor.Special;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GravebornAbility {

    private static final double RADIUS = 8.0;
    private static final int MAX_TARGETS = 7;
    private static final double EXECUTE_THRESHOLD = 0.75;
    private static final double NORMAL_DAMAGE = 50.0;
    private static final double HEAL_PERCENT = 0.30;
    private static final double MAX_HEALING = 80.0;
    private static final double RISE_HEIGHT = 5.0;
    private static final long COOLDOWN = 30_000L;

    private final CarcerWorldCore plugin;
    private final NamespacedKey abilityDamageKey;

    public GravebornAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
    }

    public void cast(Player player) {
        Location origin = player.getLocation().clone();

        player.getWorld().playSound(origin, Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.35f);
        player.getWorld().playSound(origin, Sound.BLOCK_SOUL_SAND_BREAK, 1.2f, 0.6f);
        player.getWorld().spawnParticle(Particle.SOUL, origin.clone().add(0, 1, 0), 30, 1.0, 0.5, 1.0, 0.04);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, origin.clone().add(0, 0.3, 0), 20, 0.7, 0.2, 0.7, 0.02);

        risePlayer(player, origin);
    }

    private void risePlayer(Player player, Location origin) {
        new BukkitRunnable() {

            private double risen = 0.0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                if (risen >= RISE_HEIGHT) {
                    player.setVelocity(new Vector(0, 0, 0));
                    performSoulReap(player, origin);
                    cancel();
                    return;
                }

                Location next = player.getLocation().clone().add(0, 0.5, 0);

                if (!canRiseTo(next)) {
                    player.setVelocity(new Vector(0, 0, 0));
                    performSoulReap(player, origin);
                    cancel();
                    return;
                }

                player.teleport(next);
                player.setVelocity(new Vector(0, 0, 0));

                createRiseParticles(player);

                risen += 0.5;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean canRiseTo(Location location) {
        return location.getBlock().isPassable() && location.clone().add(0, 1, 0).getBlock().isPassable();
    }

    private void createRiseParticles(Player player) {
        Location center = player.getLocation().clone().add(0, 0.6, 0);
        double time = System.currentTimeMillis() / 120.0;

        for (int i = 0; i < 4; i++) {
            double angle = time + (Math.PI * 2.0 * i / 4.0);
            double x = Math.cos(angle) * 0.8;
            double z = Math.sin(angle) * 0.8;

            Location particleLocation = center.clone().add(x, 0, z);

            player.getWorld().spawnParticle(Particle.SOUL, particleLocation, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.SCULK_SOUL, particleLocation, 1, 0, 0, 0, 0);
        }

        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 2, 0.3, 0.1, 0.3, 0.01);
    }

    private void performSoulReap(Player player, Location origin) {
        List<LivingEntity> nearbyTargets = getNearbyTargets(player, origin);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.4f, 0.7f);
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation().clone().add(0, 1, 0), 25, 1.5, 0.8, 1.5, 0.02);

        List<LivingEntity> executableTargets = new ArrayList<>();

        for (LivingEntity target : nearbyTargets) {
            if (isExecutable(target)) executableTargets.add(target);
        }

        if (!executableTargets.isEmpty()) {
            executeTargets(player, executableTargets);
        } else {
            damageTargets(player, nearbyTargets);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false, true));
    }

    private List<LivingEntity> getNearbyTargets(Player player, Location origin) {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : origin.getWorld().getNearbyEntities(origin, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (!(target instanceof Monster)) continue;
            if (target.isDead()) continue;
            if (target.getLocation().getY() > player.getLocation().getY() + 1.0) continue;

            targets.add(target);
        }

        targets.sort(Comparator.comparingDouble(target -> target.getLocation().distanceSquared(origin)));

        if (targets.size() > MAX_TARGETS) return new ArrayList<>(targets.subList(0, MAX_TARGETS));

        return targets;
    }

    private boolean isExecutable(LivingEntity target) {
        AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) return false;

        double maxHealth = maxHealthAttribute.getValue();
        if (maxHealth <= 0) return false;

        double healthPercent = target.getHealth() / maxHealth;

        return healthPercent <= EXECUTE_THRESHOLD;
    }

    private void executeTargets(Player player, List<LivingEntity> targets) {
        int executed = 0;

        for (LivingEntity target : targets) {
            if (executed >= MAX_TARGETS) break;
            if (target.isDead()) continue;

            Location soulLocation = target.getLocation().clone().add(0, target.getHeight() * 0.6, 0);

            createExecutionEffect(target);

            target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
            target.damage(target.getHealth() + 1000.0, player);

            createConsumedSoul(player, soulLocation);

            executed++;
        }

        double healing = Math.min(executed * NORMAL_DAMAGE * HEAL_PERCENT, MAX_HEALING);

        healPlayer(player, healing);

        player.sendMessage("§5§lGRAVEBORN §7§l| §fSoul Reap executed §d" + executed + " §fenemies.");
    }

    private void damageTargets(Player player, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            player.sendMessage("§5§lGRAVEBORN §7§l| §fThere are no souls nearby to reap.");
            return;
        }

        int hits = 0;

        for (LivingEntity target : targets) {
            if (hits >= MAX_TARGETS) break;
            if (target.isDead()) continue;

            target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
            target.damage(NORMAL_DAMAGE, player);

            createHarvestHitEffect(target);

            hits++;
        }

        double healing = Math.min(hits * NORMAL_DAMAGE * HEAL_PERCENT, MAX_HEALING);

        healPlayer(player, healing);

        player.sendMessage("§5§lGRAVEBORN §7§l| §fSoul Reap struck §d" + hits + " §fenemies.");
    }

    private void createExecutionEffect(LivingEntity target) {
        Location location = target.getLocation().clone().add(0, target.getHeight() * 0.5, 0);

        target.getWorld().spawnParticle(Particle.SOUL, location, 25, 0.5, 0.7, 0.5, 0.05);
        target.getWorld().spawnParticle(Particle.SCULK_SOUL, location, 15, 0.4, 0.6, 0.4, 0.02);
        target.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 10, 0.35, 0.5, 0.35, 0.01);
        target.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 15, 0.4, 0.5, 0.4, 0.02);

        target.getWorld().playSound(location, Sound.ENTITY_WITHER_HURT, 0.8f, 0.6f);
    }

    private void createHarvestHitEffect(LivingEntity target) {
        Location location = target.getLocation().clone().add(0, target.getHeight() * 0.5, 0);

        target.getWorld().spawnParticle(Particle.SOUL, location, 8, 0.3, 0.4, 0.3, 0.03);
        target.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 6, 0.25, 0.3, 0.25, 0.01);

        target.getWorld().playSound(location, Sound.BLOCK_SOUL_SAND_BREAK, 0.6f, 0.8f);
    }

    private void createConsumedSoul(Player player, Location start) {
        new BukkitRunnable() {

            private int tick = 0;
            private static final int TRAVEL_TICKS = 15;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                if (!player.getWorld().equals(start.getWorld())) {
                    cancel();
                    return;
                }

                Location swordLocation = getSwordLocation(player);
                double progress = Math.min(1.0, tick / (double) TRAVEL_TICKS);
                double easedProgress = 1.0 - Math.pow(1.0 - progress, 2.0);

                Vector path = swordLocation.toVector().subtract(start.toVector());
                Location point = start.clone().add(path.multiply(easedProgress));

                double spiralAngle = progress * Math.PI * 5.0;
                double spiralRadius = 0.35 * (1.0 - progress);

                point.add(Math.cos(spiralAngle) * spiralRadius, Math.sin(spiralAngle * 1.3) * spiralRadius, Math.sin(spiralAngle) * spiralRadius);

                player.getWorld().spawnParticle(Particle.SOUL, point, 3, 0.06, 0.06, 0.06, 0.01);
                player.getWorld().spawnParticle(Particle.SCULK_SOUL, point, 1, 0.03, 0.03, 0.03, 0);

                if (tick >= TRAVEL_TICKS) {
                    consumeSoul(player, swordLocation);
                    cancel();
                    return;
                }

                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Location getSwordLocation(Player player) {
        Location eye = player.getEyeLocation();
        Vector forward = eye.getDirection().normalize();
        Vector right = new Vector(-forward.getZ(), 0, forward.getX());

        if (right.lengthSquared() > 0) right.normalize();

        double side = player.getMainHand() == MainHand.LEFT ? -0.45 : 0.45;

        Location swordLocation = eye.clone();
        swordLocation.add(forward.clone().multiply(0.65));
        swordLocation.add(right.multiply(side));
        swordLocation.add(0, -0.35, 0);

        return swordLocation;
    }

    private void consumeSoul(Player player, Location swordLocation) {
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, swordLocation, 10, 0.15, 0.15, 0.15, 0.02);
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, swordLocation, 6, 0.12, 0.12, 0.12, 0.01);

        player.getWorld().playSound(swordLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 0.5f);
        player.getWorld().playSound(swordLocation, Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.7f, 0.7f);
    }

    private void healPlayer(Player player, double amount) {
        if (amount <= 0) return;

        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) return;

        double maxHealth = maxHealthAttribute.getValue();
        double actualHealing = Math.min(amount, maxHealth - player.getHealth());

        if (actualHealing <= 0) return;

        player.setHealth(Math.min(maxHealth, player.getHealth() + actualHealing));

        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().clone().add(0, 1.2, 0), 10, 0.5, 0.7, 0.5, 0.02);
        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().clone().add(0, 1.0, 0), 15, 0.6, 0.8, 0.6, 0.03);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 0.7f);
    }

    public long getCooldown() {
        return COOLDOWN;
    }
}