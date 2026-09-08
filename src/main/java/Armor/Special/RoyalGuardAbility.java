package Armor.Special;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoyalGuardAbility {

    private static final double SLAM_DAMAGE = 25.0;
    private static final double SLAM_RADIUS = 7.0;
    private static final double HEAL_PERCENT = 0.60;
    private static final int RESISTANCE_DURATION = 20 * 60;
    private static final long COOLDOWN = 35_000L;

    private static final Particle.DustOptions GOLD_DUST = new Particle.DustOptions(Color.fromRGB(255, 200, 40), 1.25f);
    private static final Particle.DustOptions BRIGHT_GOLD_DUST = new Particle.DustOptions(Color.fromRGB(255, 235, 120), 1.0f);

    private final CarcerWorldCore plugin;
    private final NamespacedKey apparitionKey;
    private final NamespacedKey abilityDamageKey;
    private final Map<UUID, List<ArmorStand>> activeGuardians = new HashMap<>();

    public RoyalGuardAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.apparitionKey = new NamespacedKey(plugin, "royal_guard_apparition");
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");

        cleanupOrphanedGuardians();
    }

    public void cast(Player player) {
        cleanupGuardians(player);

        Location impact = getImpactLocation(player);

        playOpeningEffect(player);
        startJudgmentSequence(player, impact);
    }

    private void startJudgmentSequence(Player player, Location impact) {
        Vector forward = getHorizontalDirection(player);
        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        Location start = impact.clone().add(forward.clone().multiply(9.0)).add(right.clone().multiply(2.5));
        Location exit = impact.clone().subtract(forward.clone().multiply(7.0)).subtract(right.clone().multiply(4.0));

        start = findGround(start);
        exit = findGround(exit);

        Location finalStart = start;
        Location finalExit = exit;

        ArmorStand guard = spawnJudgmentGuard(start, impact);

        trackGuardian(player, guard);
        createJudgmentArrival(start);

        new BukkitRunnable() {

            private int tick = 0;
            private boolean slammed = false;

            @Override
            public void run() {
                if (!isPlayerValid(player)) {
                    cleanupGuardians(player);
                    cancel();
                    return;
                }

                if (!guard.isValid()) {
                    cancel();
                    return;
                }

                if (tick <= 10) {
                    double progress = tick / 10.0;
                    Location location = interpolate(finalStart, impact, progress);

                    location = findGround(location);
                    faceLocation(location, impact);

                    guard.teleport(location);
                    guard.setRightArmPose(new EulerAngle(Math.toRadians(285), 0, Math.toRadians(10)));

                    createRushTrail(guard.getLocation());
                } else if (tick <= 18) {
                    double progress = (tick - 10) / 8.0;
                    double height = Math.sin(progress * Math.PI) * 1.6;

                    Location location = impact.clone().add(0, height, 0);

                    faceLocation(location, player.getLocation());

                    guard.teleport(location);

                    if (progress < 0.50) {
                        guard.setRightArmPose(new EulerAngle(Math.toRadians(220), 0, 0));
                    } else {
                        guard.setRightArmPose(new EulerAngle(Math.toRadians(25), 0, 0));
                    }

                    createLeapParticles(guard.getLocation());

                    if (tick == 18 && !slammed) {
                        slamGround(player, impact);
                        slammed = true;
                    }
                } else if (tick <= 22) {
                    guard.teleport(impact);
                    guard.setRightArmPose(new EulerAngle(Math.toRadians(25), 0, 0));
                } else if (tick <= 35) {
                    double progress = (tick - 22) / 13.0;
                    Location location = interpolate(impact, finalExit, progress);

                    location = findGround(location);
                    faceLocation(location, finalExit);

                    guard.teleport(location);
                    guard.setRightArmPose(new EulerAngle(Math.toRadians(285), 0, Math.toRadians(10)));

                    createRushTrail(guard.getLocation());
                } else {
                    dissolveGuardian(guard);
                    cancel();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> startRestorationSequence(player), 8L);
                    return;
                }

                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ArmorStand spawnJudgmentGuard(Location location, Location target) {
        faceLocation(location, target);

        ArmorStand guard = createGuardian(location);
        EntityEquipment equipment = guard.getEquipment();

        equipRoyalGuardArmor(equipment);
        equipment.setItemInMainHand(new ItemStack(Material.MACE));
        equipment.setItemInOffHand(new ItemStack(Material.SHIELD));

        guard.setRightArmPose(new EulerAngle(Math.toRadians(285), 0, Math.toRadians(10)));
        guard.setLeftArmPose(new EulerAngle(Math.toRadians(300), 0, Math.toRadians(-15)));

        return guard;
    }

    private void slamGround(Player player, Location impact) {
        Location center = impact.clone().add(0, 0.3, 0);

        impact.getWorld().spawnParticle(Particle.EXPLOSION, center, 3, 0.5, 0.2, 0.5, 0);
        impact.getWorld().spawnParticle(Particle.CLOUD, center, 50, 1.8, 0.25, 1.8, 0.10);
        impact.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 35, 1.5, 0.4, 1.5, 0.12);
        impact.getWorld().spawnParticle(Particle.DUST, center, 45, 1.5, 0.35, 1.5, 0.05, GOLD_DUST);
        impact.getWorld().spawnParticle(Particle.WAX_ON, center, 25, 1.4, 0.3, 1.4, 0.08);

        impact.getWorld().playSound(impact, Sound.BLOCK_ANVIL_LAND, 1.3f, 0.65f);
        impact.getWorld().playSound(impact, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.4f, 0.55f);
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);

        createExpandingShockwave(impact);

        for (Entity entity : impact.getWorld().getNearbyEntities(impact, SLAM_RADIUS, SLAM_RADIUS, SLAM_RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (!(target instanceof Monster)) continue;
            if (target.isDead()) continue;
            if (target.getLocation().distanceSquared(impact) > SLAM_RADIUS * SLAM_RADIUS) continue;

            dealAbilityDamage(player, target, SLAM_DAMAGE);

            if (target.isDead()) continue;

            Vector knockback = target.getLocation().toVector().subtract(impact.toVector());
            knockback.setY(0);

            if (knockback.lengthSquared() > 0) knockback.normalize();

            knockback.multiply(0.65);
            knockback.setY(0.35);

            target.setVelocity(knockback);
        }
    }

    private void createExpandingShockwave(Location center) {
        new BukkitRunnable() {

            private int ring = 0;

            @Override
            public void run() {
                double radius = 1.3 + (ring * 1.25);

                createGroundRing(center, radius, 30 + (ring * 5));

                ring++;

                if (ring >= 5) cancel();
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void createGroundRing(Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = center.clone().add(x, 0.15, z);

            center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, GOLD_DUST);

            if (i % 2 == 0) center.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0, 0.05, 0, 0);
            if (i % 4 == 0) center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point, 1, 0, 0.05, 0, 0);
        }
    }

    private void startRestorationSequence(Player player) {
        if (!isPlayerValid(player)) return;

        Location summonLocation = getRestorationLocation(player);
        ArmorStand guard = createGuardian(summonLocation);

        trackGuardian(player, guard);
        playRestorationSummon(player, summonLocation);

        new BukkitRunnable() {

            private int tick = 0;
            private boolean healed = false;

            @Override
            public void run() {
                if (!isPlayerValid(player)) {
                    cleanupGuardians(player);
                    cancel();
                    return;
                }

                if (!guard.isValid()) {
                    cancel();
                    return;
                }

                faceGuardTowardPlayer(guard, player);

                if (tick <= 20) {
                    materializeRestorationGuard(guard, tick);
                    createMaterializationEffect(guard, tick);
                }

                if (tick >= 20 && tick < 40) {
                    guard.setRightArmPose(new EulerAngle(Math.toRadians(210), 0, 0));
                    createHolyCharge(guard, tick);
                }

                if (tick == 40 && !healed) {
                    performRestoration(player, guard);
                    healed = true;
                }

                if (tick > 40 && tick <= 58) createHealingAfterglow(player, tick - 40);

                if (tick == 62) guard.getEquipment().setItemInMainHand(null);
                if (tick == 65) guard.getEquipment().setHelmet(null);
                if (tick == 68) guard.getEquipment().setChestplate(null);
                if (tick == 71) guard.getEquipment().setLeggings(null);

                if (tick >= 74) {
                    guard.getEquipment().setBoots(null);
                    dissolveGuardian(guard);
                    cleanupGuardians(player);
                    cancel();
                    return;
                }

                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void materializeRestorationGuard(ArmorStand guard, int tick) {
        EntityEquipment equipment = guard.getEquipment();

        if (tick == 0) equipment.setBoots(createRoyalGuardArmor(Material.NETHERITE_BOOTS));
        if (tick == 4) equipment.setLeggings(createRoyalGuardArmor(Material.NETHERITE_LEGGINGS));
        if (tick == 8) equipment.setChestplate(createRoyalGuardArmor(Material.NETHERITE_CHESTPLATE));
        if (tick == 12) equipment.setHelmet(createRoyalGuardArmor(Material.NETHERITE_HELMET));

        if (tick == 16) {
            equipment.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
            equipment.setItemInOffHand(new ItemStack(Material.SHIELD));
        }
    }

    private void performRestoration(Player player, ArmorStand guard) {
        playHealingBeam(guard, player);

        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealthAttribute != null) {
            double maxHealth = maxHealthAttribute.getValue();
            double healing = maxHealth * HEAL_PERCENT;

            player.setHealth(Math.min(maxHealth, player.getHealth() + healing));
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, RESISTANCE_DURATION, 0, false, true, true));

        Location center = player.getLocation().clone().add(0, 1.0, 0);

        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 55, 1.2, 1.0, 1.2, 0.12);
        player.getWorld().spawnParticle(Particle.DUST, center, 55, 1.1, 0.9, 1.1, 0.04, GOLD_DUST);
        player.getWorld().spawnParticle(Particle.HEART, center, 15, 0.8, 0.8, 0.8, 0.05);
        player.getWorld().spawnParticle(Particle.WAX_ON, center, 25, 1.0, 0.8, 1.0, 0.08);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.35f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.2f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.35f);

        createHealingWave(player);

        player.sendMessage("§6§lROYAL GUARD §7§l| §fThe Guardian of Restoration restored §e60% §fof your maximum health.");
        player.sendMessage("§6§lROYAL GUARD §7§l| §fYou received §eResistance I §ffor §e60 seconds§f.");
    }

    private void playHealingBeam(ArmorStand guard, Player player) {
        Location start = getSwordLocation(guard);
        Location end = player.getLocation().clone().add(0, 1.1, 0);

        Vector difference = end.toVector().subtract(start.toVector());
        double distance = difference.length();

        if (distance <= 0) return;

        Vector step = difference.normalize().multiply(0.22);
        Location point = start.clone();

        for (double traveled = 0; traveled <= distance; traveled += 0.22) {
            player.getWorld().spawnParticle(Particle.DUST, point, 2, 0.03, 0.03, 0.03, 0, BRIGHT_GOLD_DUST);
            player.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0.03, 0.03, 0.03, 0);

            point.add(step);
        }
    }

    private void createHealingWave(Player player) {
        Location center = player.getLocation().clone();

        new BukkitRunnable() {

            private int ring = 0;

            @Override
            public void run() {
                double radius = 0.8 + (ring * 0.65);
                int points = 20 + (ring * 5);

                for (int i = 0; i < points; i++) {
                    double angle = (Math.PI * 2.0 * i) / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location point = center.clone().add(x, 0.25, z);

                    center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, GOLD_DUST);

                    if (i % 2 == 0) center.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0, 0, 0, 0);
                }

                ring++;

                if (ring >= 6) cancel();
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void createMaterializationEffect(ArmorStand guard, int tick) {
        Location base = guard.getLocation().clone();
        double height = Math.min(2.2, tick * 0.11);

        for (int i = 0; i < 4; i++) {
            double angle = (tick * 0.38) + ((Math.PI * 2.0 * i) / 4.0);
            double radius = 0.7;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = base.clone().add(x, height, z);

            guard.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, GOLD_DUST);
            guard.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0, 0, 0, 0);
        }

        guard.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, base.clone().add(0, height, 0), 2, 0.3, 0.15, 0.3, 0.03);
    }

    private void createHolyCharge(ArmorStand guard, int tick) {
        Location sword = getSwordLocation(guard);
        double rotation = tick * 0.45;

        for (int i = 0; i < 6; i++) {
            double angle = rotation + ((Math.PI * 2.0 * i) / 6.0);
            double radius = 0.65;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle * 1.4) * 0.35;
            double z = Math.sin(angle) * radius;

            Location point = sword.clone().add(x, y, z);

            guard.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, BRIGHT_GOLD_DUST);
            guard.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0, 0, 0, 0);
        }

        if (tick % 5 == 0) {
            guard.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, sword, 4, 0.2, 0.2, 0.2, 0.03);
            guard.getWorld().playSound(guard.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.4f, 1.4f);
        }
    }

    private void createHealingAfterglow(Player player, int tick) {
        Location center = player.getLocation().clone().add(0, 0.8, 0);
        double angle = tick * 0.45;

        for (int i = 0; i < 5; i++) {
            double offset = angle + ((Math.PI * 2.0 * i) / 5.0);
            double radius = 0.9;
            double x = Math.cos(offset) * radius;
            double z = Math.sin(offset) * radius;
            double y = (tick * 0.055) + (i * 0.12);

            Location point = center.clone().add(x, y, z);

            player.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, BRIGHT_GOLD_DUST);
        }
    }

    private void playOpeningEffect(Player player) {
        Location center = player.getLocation().clone().add(0, 1.0, 0);

        player.getWorld().spawnParticle(Particle.DUST, center, 20, 0.8, 0.8, 0.8, 0.04, GOLD_DUST);
        player.getWorld().spawnParticle(Particle.WAX_ON, center, 15, 0.8, 0.7, 0.8, 0.05);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.35f);
    }

    private void createJudgmentArrival(Location location) {
        Location center = location.clone().add(0, 1.0, 0);

        location.getWorld().spawnParticle(Particle.DUST, center, 30, 0.6, 1.0, 0.6, 0.04, GOLD_DUST);
        location.getWorld().spawnParticle(Particle.WAX_ON, center, 20, 0.6, 1.0, 0.6, 0.06);

        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.9f, 0.8f);
    }

    private void createRushTrail(Location location) {
        Location center = location.clone().add(0, 1.0, 0);

        location.getWorld().spawnParticle(Particle.DUST, center, 3, 0.25, 0.5, 0.25, 0.01, GOLD_DUST);
        location.getWorld().spawnParticle(Particle.WAX_ON, center, 2, 0.25, 0.4, 0.25, 0.02);
        location.getWorld().spawnParticle(Particle.CLOUD, location.clone().add(0, 0.15, 0), 2, 0.25, 0.05, 0.25, 0.02);
    }

    private void createLeapParticles(Location location) {
        Location center = location.clone().add(0, 0.5, 0);

        location.getWorld().spawnParticle(Particle.DUST, center, 4, 0.25, 0.25, 0.25, 0.02, GOLD_DUST);
        location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 2, 0.2, 0.2, 0.2, 0.03);
    }

    private void playRestorationSummon(Player player, Location location) {
        createSummoningCircle(location);

        Location center = location.clone().add(0, 1.0, 0);

        location.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 30, 0.8, 1.0, 0.8, 0.07);
        location.getWorld().spawnParticle(Particle.DUST, center, 35, 0.7, 1.0, 0.7, 0.04, GOLD_DUST);

        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.1f, 1.45f);
        location.getWorld().playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 1.1f);
    }

    private void createSummoningCircle(Location center) {
        for (int ring = 0; ring < 3; ring++) {
            double radius = 0.8 + (ring * 0.5);
            int points = 20 + (ring * 6);

            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2.0 * i) / points;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                Location point = center.clone().add(x, 0.1, z);

                center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, GOLD_DUST);

                if (i % 3 == 0) center.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0, 0, 0, 0);
            }
        }
    }

    private ArmorStand createGuardian(Location location) {
        ArmorStand guard = location.getWorld().spawn(location, ArmorStand.class);

        guard.setVisible(false);
        guard.setArms(true);
        guard.setBasePlate(false);
        guard.setGravity(false);
        guard.setInvulnerable(true);
        guard.setMarker(true);
        guard.setSilent(true);
        guard.setGlowing(true);

        guard.getPersistentDataContainer().set(apparitionKey, PersistentDataType.BYTE, (byte) 1);

        return guard;
    }

    private void equipRoyalGuardArmor(EntityEquipment equipment) {
        equipment.setHelmet(createRoyalGuardArmor(Material.NETHERITE_HELMET));
        equipment.setChestplate(createRoyalGuardArmor(Material.NETHERITE_CHESTPLATE));
        equipment.setLeggings(createRoyalGuardArmor(Material.NETHERITE_LEGGINGS));
        equipment.setBoots(createRoyalGuardArmor(Material.NETHERITE_BOOTS));
    }

    private ItemStack createRoyalGuardArmor(Material material) {
        ItemStack item = new ItemStack(material);

        if (item.getItemMeta() instanceof ArmorMeta meta) {
            meta.setTrim(new ArmorTrim(TrimMaterial.GOLD, TrimPattern.SPIRE));
            item.setItemMeta(meta);
        }

        return item;
    }

    private void dealAbilityDamage(Player player, LivingEntity target, double damage) {
        target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
        target.damage(damage, player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isValid()) target.getPersistentDataContainer().remove(abilityDamageKey);
        });
    }

    private Location getImpactLocation(Player player) {
        Vector forward = getHorizontalDirection(player);
        Location impact = player.getLocation().clone().add(forward.multiply(1.8));

        return findGround(impact);
    }

    private Location getRestorationLocation(Player player) {
        Vector forward = getHorizontalDirection(player);
        Location location = player.getLocation().clone().subtract(forward.multiply(2.3));

        location = findGround(location);

        faceLocation(location, player.getLocation());

        return location;
    }

    private Vector getHorizontalDirection(Player player) {
        Vector direction = player.getLocation().getDirection().clone();
        direction.setY(0);

        if (direction.lengthSquared() <= 0) direction = new Vector(0, 0, 1);

        return direction.normalize();
    }

    private Location findGround(Location location) {
        Location check = location.clone().add(0, 3, 0);

        for (int i = 0; i < 9; i++) {
            Location below = check.clone().subtract(0, 1, 0);
            Location above = check.clone().add(0, 1, 0);

            if (check.getBlock().isPassable() && above.getBlock().isPassable() && below.getBlock().getType().isSolid()) {
                return check;
            }

            check.subtract(0, 1, 0);
        }

        return location;
    }

    private Location interpolate(Location start, Location end, double progress) {
        Vector difference = end.toVector().subtract(start.toVector());

        return start.clone().add(difference.multiply(progress));
    }

    private void faceLocation(Location location, Location target) {
        Vector direction = target.toVector().subtract(location.toVector());
        direction.setY(0);

        if (direction.lengthSquared() > 0) location.setDirection(direction);
    }

    private void faceGuardTowardPlayer(ArmorStand guard, Player player) {
        Location location = guard.getLocation();

        faceLocation(location, player.getLocation());

        guard.teleport(location);
    }

    private Location getSwordLocation(ArmorStand guard) {
        Location location = guard.getLocation().clone().add(0, 1.75, 0);
        Vector forward = location.getDirection().clone();
        forward.setY(0);

        if (forward.lengthSquared() <= 0) forward = new Vector(0, 0, 1);

        forward.normalize();

        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        location.add(forward.multiply(0.35));
        location.add(right.multiply(0.45));

        return location;
    }

    private void dissolveGuardian(ArmorStand guard) {
        if (!guard.isValid()) return;

        Location center = guard.getLocation().clone().add(0, 1.0, 0);

        guard.getWorld().spawnParticle(Particle.DUST, center, 30, 0.6, 1.0, 0.6, 0.05, GOLD_DUST);
        guard.getWorld().spawnParticle(Particle.WAX_ON, center, 25, 0.6, 1.0, 0.6, 0.06);
        guard.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 15, 0.5, 0.9, 0.5, 0.07);

        guard.getWorld().playSound(guard.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.4f);

        guard.remove();
    }

    private void trackGuardian(Player player, ArmorStand guard) {
        activeGuardians.computeIfAbsent(player.getUniqueId(), key -> new ArrayList<>()).add(guard);
    }

    private boolean isPlayerValid(Player player) {
        return player.isOnline() && !player.isDead();
    }

    private void cleanupGuardians(Player player) {
        List<ArmorStand> guardians = activeGuardians.remove(player.getUniqueId());

        if (guardians == null) return;

        for (ArmorStand guard : guardians) {
            if (guard != null && guard.isValid()) guard.remove();
        }
    }

    private void cleanupOrphanedGuardians() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand guard)) continue;
                if (!guard.getPersistentDataContainer().has(apparitionKey, PersistentDataType.BYTE)) continue;

                guard.remove();
            }
        }
    }

    public long getCooldown() {
        return COOLDOWN;
    }

    public void shutdown() {
        for (List<ArmorStand> guardians : activeGuardians.values()) {
            for (ArmorStand guard : guardians) {
                if (guard != null && guard.isValid()) guard.remove();
            }
        }

        activeGuardians.clear();
        cleanupOrphanedGuardians();
    }
}