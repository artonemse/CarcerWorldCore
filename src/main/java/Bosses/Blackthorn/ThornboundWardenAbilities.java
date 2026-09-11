package Bosses.Blackthorn;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ThornboundWardenAbilities {

    private static final Particle.DustOptions GREEN_DUST = new Particle.DustOptions(Color.fromRGB(60, 170, 55), 1.25f);
    private static final Particle.DustOptions DARK_GREEN_DUST = new Particle.DustOptions(Color.fromRGB(35, 95, 30), 1.4f);
    private static final Particle.DustOptions WARNING_DUST = new Particle.DustOptions(Color.fromRGB(200, 55, 40), 1.1f);

    private final CarcerWorldCore plugin;
    private final ThornboundWarden instance;

    private final NamespacedKey minionOwnerKey;
    private final NamespacedKey ignoreMobSystemsKey;
    private final NamespacedKey noRewardKey;

    public ThornboundWardenAbilities(CarcerWorldCore plugin, ThornboundWarden instance) {
        this.plugin = plugin;
        this.instance = instance;
        this.minionOwnerKey = new NamespacedKey(plugin, "boss_minion_owner");
        this.ignoreMobSystemsKey = new NamespacedKey(plugin, "boss_ignore_standard_mob_systems");
        this.noRewardKey = new NamespacedKey(plugin, "boss_no_auto_rewards");
    }

    public void thornEruption() {
        Player player = instance.getOwner();

        if (player == null) return;

        List<Location> eruptions = new ArrayList<>();

        eruptions.add(player.getLocation().clone());
        eruptions.add(player.getLocation().clone().add(2.5, 0, 0));
        eruptions.add(player.getLocation().clone().add(-2.5, 0, 0));

        for (Location location : eruptions) createWarningCircle(location, 2.0);

        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 0.8f, 1.2f);

        instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Location location : eruptions) erupt(location);
        }, 24L));
    }

    private void erupt(Location location) {
        World world = location.getWorld();

        if (world == null) return;

        world.spawnParticle(Particle.COMPOSTER, location.clone().add(0, 1.0, 0), 45, 1.3, 1.7, 1.3, 0.15);
        world.spawnParticle(Particle.DUST, location.clone().add(0, 0.3, 0), 35, 1.0, 0.4, 1.0, 0.05, GREEN_DUST);
        world.playSound(location, Sound.ENTITY_RAVAGER_ATTACK, 0.8f, 0.75f);

        Player player = instance.getOwner();

        if (player == null || !player.getWorld().equals(world)) return;

        if (player.getLocation().distanceSquared(location) <= 4.0) {
            instance.damageOwner(7.0);

            Vector knockback = player.getLocation().toVector().subtract(location.toVector());

            if (knockback.lengthSquared() > 0) {
                knockback.normalize().multiply(0.8).setY(0.45);
                player.setVelocity(knockback);
            }
        }
    }

    public void wardenCharge() {
        Player player = instance.getOwner();
        LivingEntity boss = instance.getBossEntity();

        if (player == null || boss == null) return;

        Location start = boss.getLocation().clone();
        Location target = player.getLocation().clone();
        Vector direction = target.toVector().subtract(start.toVector());

        direction.setY(0);

        if (direction.lengthSquared() <= 0) return;

        direction.normalize();

        createChargeLine(start, direction, 14.0);

        player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.35f);

        instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) return;

            boss.setVelocity(direction.clone().multiply(1.8).setY(0.15));

            final boolean[] hit = {false};

            BukkitRunnable chargeTask = new BukkitRunnable() {

                private int tick = 0;

                @Override
                public void run() {
                    Player owner = instance.getOwner();

                    if (owner == null || !boss.isValid() || boss.isDead()) {
                        cancel();
                        return;
                    }

                    boss.getWorld().spawnParticle(Particle.DUST, boss.getLocation().clone().add(0, 0.4, 0), 12, 0.6, 0.2, 0.6, 0.02, GREEN_DUST);

                    if (!hit[0] && owner.getLocation().distanceSquared(boss.getLocation()) <= 6.25) {
                        hit[0] = true;

                        instance.damageOwner(9.0);

                        Vector knockback = direction.clone().multiply(1.4).setY(0.55);

                        owner.setVelocity(knockback);
                    }

                    if (tick >= 10) {
                        cancel();
                        return;
                    }

                    tick++;
                }
            };

            instance.trackTask(chargeTask.runTaskTimer(plugin, 0L, 1L));
        }, 20L));
    }

    public void rootPrison() {
        Player player = instance.getOwner();

        if (player == null) return;

        Location prisonCenter = player.getLocation().clone();
        World world = prisonCenter.getWorld();

        if (world == null) return;

        player.playSound(player.getLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 1.0f, 0.7f);

        BukkitRunnable task = new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                Player owner = instance.getOwner();

                if (owner == null) {
                    cancel();
                    return;
                }

                double radius = Math.max(1.0, 4.5 - (tick * 0.09));

                createRing(prisonCenter, radius, DARK_GREEN_DUST, 30);

                if (tick >= 40) {
                    if (owner.getWorld().equals(world) && owner.getLocation().distanceSquared(prisonCenter) <= 20.25) {
                        instance.damageOwner(7.0);
                        owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1, false, false, true));

                        world.spawnParticle(Particle.COMPOSTER, owner.getLocation(), 40, 0.8, 1.2, 0.8, 0.15);
                    }

                    cancel();
                    return;
                }

                tick += 2;
            }
        };

        instance.trackTask(task.runTaskTimer(plugin, 0L, 2L));
    }

    public void summonCorrupted() {
        Player owner = instance.getOwner();

        if (owner == null) return;

        World world = instance.getCenter().getWorld();

        if (world == null) return;

        int amount = ThreadLocalRandom.current().nextInt(3, 6);

        owner.sendMessage(color("&2&lWARDEN &8» &aCorrupted creatures answer the Warden's call!"));

        for (int i = 0; i < amount; i++) {
            double angle = (Math.PI * 2.0 * i) / amount;
            double radius = 5.0;
            Location spawn = instance.getBossEntity().getLocation().clone().add(Math.cos(angle) * radius, 0.3, Math.sin(angle) * radius);

            Mob mob;

            if (i % 3 == 0) {
                Skeleton skeleton = world.spawn(spawn, Skeleton.class);
                mob = skeleton;
            } else {
                Zombie zombie = world.spawn(spawn, Zombie.class);
                mob = zombie;
            }

            configureMinion(mob, owner);
            instance.trackEntity(mob);

            world.spawnParticle(Particle.COMPOSTER, spawn.clone().add(0, 1, 0), 25, 0.6, 1.0, 0.6, 0.1);
        }
    }

    private void configureMinion(Mob mob, Player owner) {
        mob.setCustomName(color("&2Corrupted Servant"));
        mob.setCustomNameVisible(true);
        mob.setPersistent(true);
        mob.setRemoveWhenFarAway(false);
        mob.setTarget(owner);

        AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance damage = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);

        if (health != null) {
            health.setBaseValue(35.0);
            mob.setHealth(35.0);
        }

        if (damage != null) damage.setBaseValue(4.0);
        if (speed != null) speed.setBaseValue(0.25);

        mob.getPersistentDataContainer().set(minionOwnerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
        mob.getPersistentDataContainer().set(ignoreMobSystemsKey, PersistentDataType.BYTE, (byte) 1);
        mob.getPersistentDataContainer().set(noRewardKey, PersistentDataType.BYTE, (byte) 1);
    }

    public void thornstorm() {
        Player player = instance.getOwner();

        if (player == null) return;

        World world = player.getWorld();
        List<Location> strikes = new ArrayList<>();

        strikes.add(player.getLocation().clone());

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60.0);
            double radius = 3.5 + ThreadLocalRandom.current().nextDouble(2.5);

            strikes.add(player.getLocation().clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
        }

        for (Location strike : strikes) createWarningCircle(strike, 2.0);

        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 0.7f);

        for (int i = 0; i < strikes.size(); i++) {
            Location strike = strikes.get(i);

            instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> thornstormStrike(strike), 28L + (i * 4L)));
        }
    }

    private void thornstormStrike(Location location) {
        World world = location.getWorld();

        if (world == null) return;

        world.spawnParticle(Particle.COMPOSTER, location.clone().add(0, 1, 0), 55, 1.5, 2.0, 1.5, 0.18);
        world.spawnParticle(Particle.DUST, location.clone().add(0, 0.5, 0), 30, 1.1, 0.6, 1.1, 0.05, DARK_GREEN_DUST);
        world.playSound(location, Sound.ENTITY_RAVAGER_ATTACK, 0.8f, 1.1f);

        Player player = instance.getOwner();

        if (player == null || !player.getWorld().equals(world)) return;

        if (player.getLocation().distanceSquared(location) <= 4.0) instance.damageOwner(8.0);
    }

    public void shockwave() {
        LivingEntity boss = instance.getBossEntity();

        if (boss == null) return;

        Location center = boss.getLocation().clone();

        for (int ring = 1; ring <= 4; ring++) {
            final double radius = ring * 2.25;

            instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> createShockwaveRing(center, radius), ring * 6L));
        }
    }

    private void createShockwaveRing(Location center, double radius) {
        World world = center.getWorld();

        if (world == null) return;

        createRing(center, radius, GREEN_DUST, 40);

        world.playSound(center, Sound.ENTITY_RAVAGER_ATTACK, 0.65f, 0.65f + ((float) radius / 20.0f));

        Player player = instance.getOwner();

        if (player == null || !player.getWorld().equals(world)) return;

        double horizontalDistance = horizontalDistance(player.getLocation(), center);
        double heightDifference = Math.abs(player.getLocation().getY() - center.getY());

        if (Math.abs(horizontalDistance - radius) <= 1.25 && heightDifference <= 1.0) {
            instance.damageOwner(8.0);

            Vector knockback = player.getLocation().toVector().subtract(center.toVector());

            knockback.setY(0);

            if (knockback.lengthSquared() > 0) {
                knockback.normalize().multiply(0.9).setY(0.4);
                player.setVelocity(knockback);
            }
        }
    }

    private void createWarningCircle(Location center, double radius) {
        World world = center.getWorld();

        if (world == null) return;

        for (int i = 0; i < 28; i++) {
            double angle = (Math.PI * 2.0 * i) / 28.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            world.spawnParticle(Particle.DUST, center.clone().add(x, 0.12, z), 1, 0, 0, 0, 0, WARNING_DUST);
        }
    }

    private void createChargeLine(Location start, Vector direction, double length) {
        World world = start.getWorld();

        if (world == null) return;

        for (double distance = 0; distance <= length; distance += 0.5) {
            Location point = start.clone().add(direction.clone().multiply(distance));

            world.spawnParticle(Particle.DUST, point.clone().add(0, 0.15, 0), 1, 0, 0, 0, 0, WARNING_DUST);
        }
    }

    private void createRing(Location center, double radius, Particle.DustOptions dust, int points) {
        World world = center.getWorld();

        if (world == null) return;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            world.spawnParticle(Particle.DUST, center.clone().add(x, 0.15, z), 1, 0, 0, 0, 0, dust);
        }
    }

    private double horizontalDistance(Location first, Location second) {
        double x = first.getX() - second.getX();
        double z = first.getZ() - second.getZ();

        return Math.sqrt((x * x) + (z * z));
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}
