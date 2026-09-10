package Armor.Special;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GoblinSlayerAbility {

    private static final double DAMAGE_PER_STRIKE = 15.0;
    private static final double RADIUS = 6.0;
    private static final int MAX_TARGETS = 10;
    private static final int TOTAL_STRIKES = 5;
    private static final int STRIKE_INTERVAL = 8;
    private static final int BLADE_DIRECTION_INTERVAL = 5;

    private static final Particle.DustOptions GREEN_DUST = new Particle.DustOptions(Color.fromRGB(60, 220, 80), 1.15f);
    private static final Particle.DustOptions BRIGHT_GREEN_DUST = new Particle.DustOptions(Color.fromRGB(150, 255, 120), 0.9f);

    private final CarcerWorldCore plugin;
    private final NamespacedKey bladeKey;
    private final NamespacedKey abilityDamageKey;
    private final Map<UUID, List<ArmorStand>> activeBlades = new HashMap<>();
    private final Random random = new Random();

    public GoblinSlayerAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.bladeKey = new NamespacedKey(plugin, "goblin_slayer_blade");
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");

        cleanupOrphanedBlades();
    }

    public void cast(Player player) {
        cleanupBlades(player);

        List<ArmorStand> blades = spawnBlades(player);

        activeBlades.put(player.getUniqueId(), blades);

        playActivation(player);
        startFrenzy(player, blades);
    }

    private void startFrenzy(Player player, List<ArmorStand> blades) {
        World castWorld = player.getWorld();
        List<Vector> currentOffsets = new ArrayList<>();
        List<Vector> targetOffsets = new ArrayList<>();

        for (ArmorStand blade : blades) {
            Vector current = randomBladeOffset();

            currentOffsets.add(current);
            targetOffsets.add(randomBladeOffset());

            blade.teleport(getBladeLocation(player, current));
            randomizeBladePose(blade);
        }

        new BukkitRunnable() {

            private int tick = 0;
            private int strikes = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || !player.getWorld().equals(castWorld)) {
                    cleanupBlades(player);
                    cancel();
                    return;
                }

                if (tick > 0 && tick % BLADE_DIRECTION_INTERVAL == 0) {
                    for (int i = 0; i < blades.size(); i++) {
                        targetOffsets.set(i, randomBladeOffset());

                        ArmorStand blade = blades.get(i);

                        if (blade.isValid()) randomizeBladePose(blade);
                    }
                }

                animateBlades(player, blades, currentOffsets, targetOffsets);

                if (tick % STRIKE_INTERVAL == 0 && strikes < TOTAL_STRIKES) {
                    strikes++;

                    if (strikes < TOTAL_STRIKES) {
                        performStrike(player, strikes);
                    } else {
                        performFinalStrike(player);
                    }
                }

                if (tick >= 40) {
                    dissolveBlades(player, blades);
                    activeBlades.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private List<ArmorStand> spawnBlades(Player player) {
        List<ArmorStand> blades = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Vector offset = randomBladeOffset();
            Location location = getBladeLocation(player, offset);
            ArmorStand blade = location.getWorld().spawn(location, ArmorStand.class);

            blade.setVisible(false);
            blade.setArms(true);
            blade.setBasePlate(false);
            blade.setGravity(false);
            blade.setInvulnerable(true);
            blade.setMarker(true);
            blade.setSilent(true);
            blade.setGlowing(true);
            blade.setCustomName(null);
            blade.setCustomNameVisible(false);

            EntityEquipment equipment = blade.getEquipment();

            equipment.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));

            randomizeBladePose(blade);

            blade.getPersistentDataContainer().set(bladeKey, PersistentDataType.BYTE, (byte) 1);

            blades.add(blade);
        }

        return blades;
    }

    private void animateBlades(Player player, List<ArmorStand> blades, List<Vector> currentOffsets, List<Vector> targetOffsets) {
        Vector forward = getHorizontalDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        for (int i = 0; i < blades.size(); i++) {
            ArmorStand blade = blades.get(i);

            if (!blade.isValid()) continue;

            Vector current = currentOffsets.get(i);
            Vector target = targetOffsets.get(i);
            Vector movement = target.clone().subtract(current);

            current.add(movement.clone().multiply(0.45));

            Location location = getBladeLocation(player, current);

            Vector worldMovement = right.clone().multiply(movement.getX());
            worldMovement.add(forward.clone().multiply(movement.getZ()));
            worldMovement.setY(movement.getY());

            if (worldMovement.lengthSquared() > 0.001) location.setDirection(worldMovement.normalize());

            blade.teleport(location);
            blade.setCustomName(null);
            blade.setCustomNameVisible(false);

            createBladeTrail(location);
        }
    }

    private Vector randomBladeOffset() {
        double side = -2.4 + (random.nextDouble() * 4.8);
        double height = -0.35 + (random.nextDouble() * 1.9);
        double forward = 2.5 + (random.nextDouble() * 2.5);

        return new Vector(side, height, forward);
    }

    private Location getBladeLocation(Player player, Vector offset) {
        Vector forward = getHorizontalDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        Location location = player.getLocation().clone().add(0, 1.0, 0);

        location.add(right.multiply(offset.getX()));
        location.add(0, offset.getY(), 0);
        location.add(forward.multiply(offset.getZ()));

        return location;
    }

    private void randomizeBladePose(ArmorStand blade) {
        double armX = Math.toRadians(235 + random.nextInt(90));
        double armY = Math.toRadians(-35 + random.nextInt(71));
        double armZ = Math.toRadians(-60 + random.nextInt(121));

        blade.setRightArmPose(new EulerAngle(armX, armY, armZ));
    }

    private void performStrike(Player player, int strike) {
        double slashAngle = Math.toRadians(-65 + (random.nextDouble() * 130.0));

        playSlash(player, slashAngle, false);
        damageTargets(player, false);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.15f, 0.85f + (strike * 0.08f));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.25f);
    }

    private void performFinalStrike(Player player) {
        double slashAngle = Math.toRadians(-45 + (random.nextDouble() * 90.0));
        Location center = getFrontEffectCenter(player, 3.0, 1.2);

        playSlash(player, slashAngle, true);
        playSlash(player, slashAngle + Math.toRadians(90), true);

        damageTargets(player, true);

        player.getWorld().spawnParticle(Particle.EXPLOSION, center, 2, 0.5, 0.4, 0.5, 0);
        player.getWorld().spawnParticle(Particle.DUST, center, 45, 1.4, 1.0, 1.4, 0.05, GREEN_DUST);
        player.getWorld().spawnParticle(Particle.CRIT, center, 35, 1.3, 0.8, 1.3, 0.15);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.55f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.1f, 1.45f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 0.8f, 1.25f);
    }

    private void playSlash(Player player, double slashAngle, boolean finalStrike) {
        Vector forward = getHorizontalDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        Location center = getFrontEffectCenter(player, finalStrike ? 3.2 : 2.8, finalStrike ? 1.4 : 1.2);

        double halfLength = finalStrike ? 2.8 : 2.1;
        int points = finalStrike ? 24 : 17;

        for (int i = 0; i < points; i++) {
            double progress = i / (double) (points - 1);
            double line = (progress * 2.0) - 1.0;
            double side = Math.cos(slashAngle) * line * halfLength;
            double height = Math.sin(slashAngle) * line * halfLength;
            double depth = Math.sin(progress * Math.PI) * 0.45;

            Location point = center.clone();

            point.add(right.clone().multiply(side));
            point.add(0, height, 0);
            point.add(forward.clone().multiply(depth));

            player.getWorld().spawnParticle(Particle.DUST, point, 2, 0.04, 0.04, 0.04, 0, BRIGHT_GREEN_DUST);

            if (i % 2 == 0) player.getWorld().spawnParticle(Particle.CRIT, point, 2, 0.08, 0.08, 0.08, 0.03);
            if (i % 4 == 0) player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, point, 1, 0, 0, 0, 0);
        }
    }

    private void damageTargets(Player player, boolean finalStrike) {
        List<LivingEntity> targets = getTargets(player);

        for (LivingEntity target : targets) {
            dealAbilityDamage(player, target, DAMAGE_PER_STRIKE);

            if (!finalStrike || target.isDead()) continue;

            Vector knockback = target.getLocation().toVector().subtract(player.getLocation().toVector());
            knockback.setY(0);

            if (knockback.lengthSquared() > 0) knockback.normalize();

            knockback.multiply(1.0);
            knockback.setY(0.4);

            target.setVelocity(knockback);
        }
    }

    private List<LivingEntity> getTargets(Player player) {
        List<LivingEntity> targets = new ArrayList<>();
        Location center = player.getLocation();

        for (Entity entity : player.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (!(target instanceof Monster)) continue;
            if (target.isDead()) continue;
            if (target.getLocation().distanceSquared(center) > RADIUS * RADIUS) continue;

            targets.add(target);
        }

        targets.sort(Comparator.comparingDouble(target -> target.getLocation().distanceSquared(center)));

        if (targets.size() > MAX_TARGETS) return new ArrayList<>(targets.subList(0, MAX_TARGETS));

        return targets;
    }

    private void dealAbilityDamage(Player player, LivingEntity target, double damage) {
        target.setNoDamageTicks(0);

        target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
        target.damage(damage, player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isValid()) target.getPersistentDataContainer().remove(abilityDamageKey);
        });
    }

    private void createBladeTrail(Location location) {
        Location center = location.clone().add(0, 0.8, 0);

        location.getWorld().spawnParticle(Particle.DUST, center, 2, 0.15, 0.25, 0.15, 0.01, GREEN_DUST);

        if (random.nextDouble() < 0.35) {
            location.getWorld().spawnParticle(Particle.CRIT, center, 1, 0.1, 0.15, 0.1, 0.02);
        }
    }

    private void playActivation(Player player) {
        Location center = getFrontEffectCenter(player, 2.5, 1.0);

        createActivationRing(player);

        player.getWorld().spawnParticle(Particle.DUST, center, 30, 1.3, 0.8, 1.3, 0.05, GREEN_DUST);
        player.getWorld().spawnParticle(Particle.CRIT, center, 20, 1.0, 0.7, 1.0, 0.10);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.1f, 0.6f);
    }

    private void createActivationRing(Player player) {
        Vector forward = getHorizontalDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();
        Location center = getFrontEffectCenter(player, 2.5, 0.3);

        for (int i = 0; i < 28; i++) {
            double angle = (Math.PI * 2.0 * i) / 28.0;
            double side = Math.cos(angle) * 1.8;
            double height = Math.sin(angle) * 1.8;

            Location point = center.clone();

            point.add(right.clone().multiply(side));
            point.add(0, height, 0);

            center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, GREEN_DUST);

            if (i % 4 == 0) center.getWorld().spawnParticle(Particle.CRIT, point, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    private Location getFrontEffectCenter(Player player, double forwardDistance, double height) {
        Vector forward = getHorizontalDirection(player);

        return player.getLocation().clone().add(0, height, 0).add(forward.multiply(forwardDistance));
    }

    private Vector getHorizontalDirection(Player player) {
        Vector direction = player.getLocation().getDirection().clone();
        direction.setY(0);

        if (direction.lengthSquared() <= 0) direction = new Vector(0, 0, 1);

        return direction.normalize();
    }

    private void dissolveBlades(Player player, List<ArmorStand> blades) {
        for (ArmorStand blade : blades) {
            if (!blade.isValid()) continue;

            Location location = blade.getLocation().clone().add(0, 0.8, 0);

            blade.getWorld().spawnParticle(Particle.DUST, location, 12, 0.35, 0.5, 0.35, 0.03, GREEN_DUST);
            blade.getWorld().spawnParticle(Particle.CRIT, location, 8, 0.3, 0.4, 0.3, 0.05);

            blade.remove();
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.6f);
    }

    private void cleanupBlades(Player player) {
        List<ArmorStand> blades = activeBlades.remove(player.getUniqueId());

        if (blades == null) return;

        for (ArmorStand blade : blades) {
            if (blade != null && blade.isValid()) blade.remove();
        }
    }

    private void cleanupOrphanedBlades() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand blade)) continue;
                if (!blade.getPersistentDataContainer().has(bladeKey, PersistentDataType.BYTE)) continue;

                blade.remove();
            }
        }
    }

    public void shutdown() {
        for (List<ArmorStand> blades : activeBlades.values()) {
            for (ArmorStand blade : blades) {
                if (blade != null && blade.isValid()) blade.remove();
            }
        }

        activeBlades.clear();
        cleanupOrphanedBlades();
    }
}