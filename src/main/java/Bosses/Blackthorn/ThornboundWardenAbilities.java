package Bosses.Blackthorn;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ThornboundWardenAbilities {

    private static final Particle.DustOptions GREEN_DUST = new Particle.DustOptions(Color.fromRGB(60, 170, 55), 1.25f);
    private static final Particle.DustOptions DARK_GREEN_DUST = new Particle.DustOptions(Color.fromRGB(30, 90, 35), 1.5f);
    private static final Particle.DustOptions WARNING_DUST = new Particle.DustOptions(Color.fromRGB(255, 55, 35), 1.55f);
    private static final Particle.DustOptions ORANGE_WARNING = new Particle.DustOptions(Color.fromRGB(255, 150, 25), 1.35f);
    private static final Particle.DustOptions VOID_DUST = new Particle.DustOptions(Color.fromRGB(90, 25, 150), 1.45f);
    private static final Particle.DustOptions BRIGHT_VOID_DUST = new Particle.DustOptions(Color.fromRGB(180, 50, 255), 1.75f);

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

        Location playerLocation = player.getLocation().clone();

        List<Location> eruptions = new ArrayList<>();

        eruptions.add(playerLocation.clone());
        eruptions.add(playerLocation.clone().add(3.0, 0, 0));
        eruptions.add(playerLocation.clone().add(-3.0, 0, 0));

        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 0.8f);

        for (Location location : eruptions) animateEruptionWarning(location);

        instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Location location : eruptions) erupt(location);
        }, 36L));
    }

    private void animateEruptionWarning(Location center) {
        BukkitRunnable warning = new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                if (tick > 32) {
                    cancel();
                    return;
                }

                double progress = tick / 32.0;
                double radius = 2.2;

                createRing(center, radius, WARNING_DUST, 38);

                for (double inner = 0.4; inner <= radius * progress; inner += 0.4) {
                    createRing(center, inner, ORANGE_WARNING, 18);
                }

                World world = center.getWorld();

                if (world != null) {
                    world.spawnParticle(Particle.COMPOSTER, center.clone().add(0, 0.15, 0), 4, 0.7, 0.05, 0.7, 0.02);

                    if (tick % 6 == 0) {
                        world.spawnParticle(Particle.DUST, center.clone().add(0, 0.25, 0), 12, 0.9, 0.05, 0.9, 0.02, WARNING_DUST);
                        world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.45f, 0.7f + ((float) progress));
                    }
                }

                tick += 2;
            }
        };

        instance.trackTask(warning.runTaskTimer(plugin, 0L, 2L));
    }

    private void erupt(Location location) {
        World world = location.getWorld();

        if (world == null) return;

        for (double y = 0; y <= 4.0; y += 0.35) {
            double spread = 0.35 + (y * 0.13);

            world.spawnParticle(Particle.COMPOSTER, location.clone().add(0, y, 0), 6, spread, 0.08, spread, 0.05);
            world.spawnParticle(Particle.DUST, location.clone().add(0, y, 0), 5, spread, 0.05, spread, 0.03, GREEN_DUST);
        }

        world.spawnParticle(Particle.BLOCK, location.clone().add(0, 0.3, 0), 35, 1.4, 0.4, 1.4, 0.25, Material.MOSS_BLOCK.createBlockData());
        world.playSound(location, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 0.7f);

        Player player = instance.getOwner();

        if (player == null || !player.getWorld().equals(world)) return;

        if (player.getLocation().distanceSquared(location) <= 5.0) {
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

        animateChargeWarning(start, direction, 15.0);

        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.25f);

        instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!boss.isValid() || boss.isDead()) return;

            boss.setVelocity(direction.clone().multiply(1.75).setY(0.12));

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

                    Location location = boss.getLocation().clone().add(0, 1, 0);

                    boss.getWorld().spawnParticle(Particle.SCULK_SOUL, location, 10, 0.7, 0.8, 0.7, 0.03);
                    boss.getWorld().spawnParticle(Particle.DUST, location, 14, 0.7, 0.5, 0.7, 0.03, DARK_GREEN_DUST);
                    boss.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 4, 0.5, 0.4, 0.5, 0.02);

                    if (!hit[0] && owner.getLocation().distanceSquared(boss.getLocation()) <= 6.25) {
                        hit[0] = true;

                        instance.damageOwner(9.0);

                        Vector knockback = direction.clone().multiply(1.4).setY(0.55);

                        owner.setVelocity(knockback);
                    }

                    if (tick >= 11) {
                        cancel();
                        return;
                    }

                    tick++;
                }
            };

            instance.trackTask(chargeTask.runTaskTimer(plugin, 0L, 1L));
        }, 34L));
    }

    private void animateChargeWarning(Location start, Vector direction, double length) {
        BukkitRunnable warning = new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                if (tick > 30) {
                    cancel();
                    return;
                }

                World world = start.getWorld();

                if (world == null) {
                    cancel();
                    return;
                }

                double progress = tick / 30.0;

                for (double distance = 0; distance <= length; distance += 0.45) {
                    Location point = start.clone().add(direction.clone().multiply(distance)).add(0, 0.12, 0);

                    world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, WARNING_DUST);

                    Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

                    world.spawnParticle(Particle.DUST, point.clone().add(right.clone().multiply(1.4)), 1, 0, 0, 0, 0, ORANGE_WARNING);
                    world.spawnParticle(Particle.DUST, point.clone().add(right.clone().multiply(-1.4)), 1, 0, 0, 0, 0, ORANGE_WARNING);
                }

                if (tick % 6 == 0) {
                    double markerDistance = length * progress;
                    Location marker = start.clone().add(direction.clone().multiply(markerDistance));

                    world.spawnParticle(Particle.FLAME, marker.clone().add(0, 0.25, 0), 15, 0.35, 0.1, 0.35, 0.03);
                    world.playSound(start, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.55f, 0.7f + ((float) progress));
                }

                tick += 2;
            }
        };

        instance.trackTask(warning.runTaskTimer(plugin, 0L, 2L));
    }

    public void rootPrison() {
        Player player = instance.getOwner();

        if (player == null) return;

        Location prisonCenter = player.getLocation().clone();
        World world = prisonCenter.getWorld();

        if (world == null) return;

        player.playSound(player.getLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 1.0f, 0.7f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.75f, 0.8f);

        BukkitRunnable task = new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                Player owner = instance.getOwner();

                if (owner == null) {
                    cancel();
                    return;
                }

                double progress = tick / 50.0;
                double radius = Math.max(1.2, 5.0 - (progress * 3.8));

                createVerticalRing(prisonCenter, radius, DARK_GREEN_DUST);
                createRing(prisonCenter, radius, WARNING_DUST, 42);

                for (int i = 0; i < 12; i++) {
                    double angle = (Math.PI * 2.0 * i) / 12.0;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location root = prisonCenter.clone().add(x, 0, z);

                    world.spawnParticle(Particle.COMPOSTER, root.clone().add(0, 0.8, 0), 5, 0.12, 0.8, 0.12, 0.03);
                }

                if (tick % 10 == 0) world.playSound(prisonCenter, Sound.BLOCK_ROOTED_DIRT_BREAK, 0.65f, 0.7f + ((float) progress));

                if (tick >= 50) {
                    if (owner.getWorld().equals(world) && owner.getLocation().distanceSquared(prisonCenter) <= 20.25) {
                        instance.damageOwner(7.0);
                        owner.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1, false, false, true));

                        world.spawnParticle(Particle.COMPOSTER, owner.getLocation(), 50, 0.9, 1.3, 0.9, 0.15);
                        world.spawnParticle(Particle.DUST, owner.getLocation(), 35, 0.8, 1.0, 0.8, 0.05, DARK_GREEN_DUST);
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

            world.spawnParticle(Particle.REVERSE_PORTAL, spawn.clone().add(0, 1, 0), 35, 0.6, 1.0, 0.6, 0.08);
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

        EntityEquipment equipment = mob.getEquipment();

        if (equipment != null) {
            equipment.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
            equipment.setHelmetDropChance(0.0f);
        }

        mob.getPersistentDataContainer().set(minionOwnerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
        mob.getPersistentDataContainer().set(ignoreMobSystemsKey, PersistentDataType.BYTE, (byte) 1);
        mob.getPersistentDataContainer().set(noRewardKey, PersistentDataType.BYTE, (byte) 1);
    }

    public void thornstorm() {
        Player player = instance.getOwner();

        if (player == null) return;

        List<Location> strikes = new ArrayList<>();

        strikes.add(player.getLocation().clone());

        for (int i = 0; i < 7; i++) {
            double angle = Math.toRadians(i * (360.0 / 7.0));
            double radius = 3.5 + ThreadLocalRandom.current().nextDouble(3.0);

            strikes.add(player.getLocation().clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
        }

        for (Location strike : strikes) animateThornstormWarning(strike);

        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.0f, 0.7f);

        for (int i = 0; i < strikes.size(); i++) {
            Location strike = strikes.get(i);

            instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> thornstormStrike(strike), 40L + (i * 5L)));
        }
    }

    private void animateThornstormWarning(Location center) {
        BukkitRunnable task = new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                if (tick > 36) {
                    cancel();
                    return;
                }

                World world = center.getWorld();

                if (world == null) {
                    cancel();
                    return;
                }

                double progress = tick / 36.0;

                createRing(center, 2.2, WARNING_DUST, 36);

                double pillarHeight = 0.5 + (progress * 4.5);

                world.spawnParticle(Particle.DUST, center.clone().add(0, pillarHeight, 0), 8, 0.12, 0.3, 0.12, 0.02, ORANGE_WARNING);
                world.spawnParticle(Particle.COMPOSTER, center.clone().add(0, pillarHeight / 2.0, 0), 6, 0.25, pillarHeight / 2.0, 0.25, 0.02);

                if (tick % 8 == 0) world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 0.45f, 0.7f + ((float) progress));

                tick += 2;
            }
        };

        instance.trackTask(task.runTaskTimer(plugin, 0L, 2L));
    }

    private void thornstormStrike(Location location) {
        World world = location.getWorld();

        if (world == null) return;

        for (double y = 0; y <= 5.0; y += 0.25) {
            world.spawnParticle(Particle.COMPOSTER, location.clone().add(0, y, 0), 7, 0.6, 0.05, 0.6, 0.08);
            world.spawnParticle(Particle.DUST, location.clone().add(0, y, 0), 5, 0.45, 0.05, 0.45, 0.04, DARK_GREEN_DUST);
        }

        world.spawnParticle(Particle.BLOCK, location.clone().add(0, 0.4, 0), 45, 1.4, 0.5, 1.4, 0.3, Material.MOSS_BLOCK.createBlockData());
        world.playSound(location, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.9f, 0.9f);

        Player player = instance.getOwner();

        if (player == null || !player.getWorld().equals(world)) return;

        if (player.getLocation().distanceSquared(location) <= 5.0) instance.damageOwner(8.0);
    }

    public void shockwave() {
        LivingEntity boss = instance.getBossEntity();

        if (boss == null) return;

        Location center = boss.getLocation().clone();

        boss.getWorld().spawnParticle(Particle.SCULK_SOUL, center.clone().add(0, 1, 0), 35, 0.8, 1.0, 0.8, 0.05);
        boss.getWorld().playSound(center, Sound.ENTITY_WARDEN_SONIC_CHARGE, 0.8f, 0.65f);

        for (int ring = 1; ring <= 5; ring++) {
            final double radius = ring * 2.1;

            instance.trackTask(plugin.getServer().getScheduler().runTaskLater(plugin, () -> createShockwaveRing(center, radius), ring * 7L));
        }
    }

    private void createShockwaveRing(Location center, double radius) {
        World world = center.getWorld();

        if (world == null) return;

        createRing(center, radius, GREEN_DUST, 55);
        createRing(center.clone().add(0, 0.25, 0), radius, ORANGE_WARNING, 45);

        for (int i = 0; i < 18; i++) {
            double angle = (Math.PI * 2.0 * i) / 18.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = center.clone().add(x, 0.5, z);

            world.spawnParticle(Particle.SCULK_SOUL, point, 2, 0.05, 0.25, 0.05, 0.01);
        }

        world.playSound(center, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.65f, 0.65f + ((float) radius / 20.0f));

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

    public void playDeathAnimation(Location location) {
        Location center = location.clone().add(0, 1.2, 0);
        World world = center.getWorld();

        if (world == null) return;

        world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.4f, 0.55f);
        world.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.35f);

        BukkitRunnable animation = new BukkitRunnable() {

            private int tick = 0;

            @Override
            public void run() {
                if (tick <= 30) {
                    playVoidImplosionFrame(center, tick);
                }

                if (tick == 32) {
                    world.strikeLightningEffect(location);

                    world.spawnParticle(Particle.FLASH, center, 3, 0.2, 0.2, 0.2, 0);
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 2, 0, 0, 0, 0);
                    world.spawnParticle(Particle.REVERSE_PORTAL, center, 300, 1.4, 1.4, 1.4, 0.45);
                    world.spawnParticle(Particle.PORTAL, center, 220, 2.0, 1.5, 2.0, 0.35);
                    world.spawnParticle(Particle.WITCH, center, 120, 1.8, 1.5, 1.8, 0.15);
                    world.spawnParticle(Particle.SCULK_SOUL, center, 100, 1.5, 1.8, 1.5, 0.12);
                    world.spawnParticle(Particle.LARGE_SMOKE, center, 100, 1.3, 1.3, 1.3, 0.08);

                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.6f, 0.55f);
                    world.playSound(center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.65f);
                }

                if (tick >= 34 && tick <= 60) {
                    double progress = (tick - 34) / 26.0;
                    double radius = 1.0 + (progress * 9.0);

                    createRing(center, radius, BRIGHT_VOID_DUST, 70);
                    createRing(center.clone().add(0, 0.4, 0), radius * 0.82, VOID_DUST, 55);

                    for (int i = 0; i < 14; i++) {
                        double angle = (Math.PI * 2.0 * i) / 14.0;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location point = center.clone().add(x, ThreadLocalRandom.current().nextDouble(-0.4, 1.2), z);

                        world.spawnParticle(Particle.REVERSE_PORTAL, point, 3, 0.12, 0.12, 0.12, 0.05);
                        world.spawnParticle(Particle.SCULK_SOUL, point, 1, 0.03, 0.12, 0.03, 0.01);
                    }
                }

                if (tick >= 38 && tick <= 72) {
                    playVoidVortexFrame(center, tick);
                }

                if (tick == 72) {
                    world.spawnParticle(Particle.FLASH, center.clone().add(0, 2, 0), 2, 0, 0, 0, 0);
                    world.spawnParticle(Particle.SCULK_SOUL, center.clone().add(0, 2, 0), 120, 3.0, 3.0, 3.0, 0.12);
                    world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 2, 0), 180, 2.5, 2.5, 2.5, 0.22);

                    world.playSound(center, Sound.ENTITY_WARDEN_DEATH, 1.2f, 0.45f);
                    world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.6f);
                }

                if (tick >= 78) {
                    cancel();
                    return;
                }

                tick += 2;
            }
        };

        instance.trackTask(animation.runTaskTimer(plugin, 0L, 2L));
    }

    private void playVoidImplosionFrame(Location center, int tick) {
        World world = center.getWorld();

        if (world == null) return;

        double progress = tick / 30.0;
        double radius = 7.0 - (progress * 6.0);

        for (int i = 0; i < 42; i++) {
            double angle = (Math.PI * 2.0 * i) / 42.0;
            double y = ThreadLocalRandom.current().nextDouble(-1.0, 3.5);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = center.clone().add(x, y, z);

            Vector direction = center.toVector().subtract(point.toVector()).normalize();

            world.spawnParticle(Particle.REVERSE_PORTAL, point, 1, direction.getX() * 0.25, direction.getY() * 0.25, direction.getZ() * 0.25, 0.1);
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, VOID_DUST);
        }

        createRing(center, radius, BRIGHT_VOID_DUST, 60);

        if (tick % 6 == 0) {
            world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.6f, 0.6f + ((float) progress));
        }
    }

    private void playVoidVortexFrame(Location center, int tick) {
        World world = center.getWorld();

        if (world == null) return;

        double time = (tick - 38) * 0.28;

        for (int i = 0; i < 28; i++) {
            double angle = time + (i * 0.48);
            double radius = 3.2 - ((i / 28.0) * 1.8);
            double y = (i / 28.0) * 6.0;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location point = center.clone().add(x, y, z);

            world.spawnParticle(Particle.REVERSE_PORTAL, point, 2, 0.06, 0.06, 0.06, 0.03);
            world.spawnParticle(Particle.WITCH, point, 1, 0.04, 0.04, 0.04, 0.01);

            if (i % 4 == 0) world.spawnParticle(Particle.SCULK_SOUL, point, 1, 0.03, 0.03, 0.03, 0.01);
        }
    }

    private void createVerticalRing(Location center, double radius, Particle.DustOptions dust) {
        World world = center.getWorld();

        if (world == null) return;

        for (int i = 0; i < 30; i++) {
            double angle = (Math.PI * 2.0 * i) / 30.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            for (double y = 0.2; y <= 2.8; y += 0.65) {
                world.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 1, 0, 0, 0, 0, dust);
            }
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