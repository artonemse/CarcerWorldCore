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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SanctumAbility {

    private static final double RADIUS = 8.0;
    private static final double PULSE_DAMAGE = 25.0;
    private static final int DURATION_TICKS = 160;
    private static final int PULSE_INTERVAL = 40;

    private static final Particle.DustOptions GOLD_DUST = new Particle.DustOptions(Color.fromRGB(255, 210, 60), 1.2f);
    private static final Particle.DustOptions HOLY_DUST = new Particle.DustOptions(Color.fromRGB(255, 245, 175), 0.9f);

    private final CarcerWorldCore plugin;
    private final NamespacedKey abilityDamageKey;
    private final Map<UUID, Domain> activeDomains = new HashMap<>();

    public SanctumAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
    }

    public void cast(Player player) {
        Location center = player.getLocation().clone();
        center.setPitch(0);

        Domain oldDomain = activeDomains.remove(player.getUniqueId());

        if (oldDomain != null) oldDomain.cancel();

        Domain domain = new Domain(player.getUniqueId(), center);

        activeDomains.put(player.getUniqueId(), domain);

        playActivation(center);
        domain.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean isInsideDomain(Player player, LivingEntity mob) {
        Domain domain = activeDomains.get(player.getUniqueId());

        if (domain == null) return false;
        if (!mob.getWorld().equals(domain.center.getWorld())) return false;

        return mob.getLocation().distanceSquared(domain.center) <= RADIUS * RADIUS;
    }

    public double applySoulBonus(Player player, LivingEntity mob, double reward) {
        if (!isInsideDomain(player, mob)) return reward;

        return reward * 1.50;
    }

    public double applyWeaponXPBonus(Player player, LivingEntity mob, double xp) {
        if (!isInsideDomain(player, mob)) return xp;

        return xp * 1.50;
    }

    private void pulse(Player player, Location center) {
        World world = center.getWorld();

        if (world == null) return;

        createPulseRing(center);

        world.spawnParticle(Particle.DUST, center.clone().add(0, 0.5, 0), 50, 2.2, 0.5, 2.2, 0.04, GOLD_DUST);
        world.spawnParticle(Particle.ENCHANT, center.clone().add(0, 0.8, 0), 35, 2.5, 0.8, 2.5, 0.15);
        world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.1f, 1.4f);
        world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.7f, 1.6f);

        for (Entity entity : world.getNearbyEntities(center, RADIUS, 4.0, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (!(target instanceof Monster)) continue;
            if (target.isDead()) continue;
            if (target.getLocation().distanceSquared(center) > RADIUS * RADIUS) continue;

            strikeTarget(player, target);
        }
    }

    private void strikeTarget(Player player, LivingEntity target) {
        Location location = target.getLocation().clone();
        World world = target.getWorld();

        for (double y = 0; y <= 3.5; y += 0.35) {
            Location point = location.clone().add(0, y, 0);

            world.spawnParticle(Particle.DUST, point, 2, 0.12, 0.05, 0.12, 0, HOLY_DUST);
        }

        world.spawnParticle(Particle.ENCHANT, location.clone().add(0, 1.0, 0), 18, 0.4, 0.8, 0.4, 0.12);
        world.spawnParticle(Particle.CRIT, location.clone().add(0, 1.0, 0), 10, 0.35, 0.5, 0.35, 0.08);

        target.setNoDamageTicks(0);
        target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);
        target.damage(PULSE_DAMAGE, player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isValid()) target.getPersistentDataContainer().remove(abilityDamageKey);
        });
    }

    private void playActivation(Location center) {
        World world = center.getWorld();

        if (world == null) return;

        world.spawnParticle(Particle.DUST, center.clone().add(0, 0.5, 0), 70, 2.0, 0.7, 2.0, 0.05, GOLD_DUST);
        world.spawnParticle(Particle.ENCHANT, center.clone().add(0, 1.0, 0), 60, 2.5, 1.0, 2.5, 0.2);

        createDomainRing(center);

        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 1.25f);
        world.playSound(center, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 0.8f, 1.6f);
    }

    private void createDomainRing(Location center) {
        World world = center.getWorld();

        if (world == null) return;

        for (int i = 0; i < 64; i++) {
            double angle = (Math.PI * 2.0 * i) / 64.0;
            double x = Math.cos(angle) * RADIUS;
            double z = Math.sin(angle) * RADIUS;

            Location point = center.clone().add(x, 0.15, z);

            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, GOLD_DUST);

            if (i % 4 == 0) world.spawnParticle(Particle.ENCHANT, point.clone().add(0, 0.25, 0), 2, 0.08, 0.1, 0.08, 0.02);
        }
    }

    private void createPulseRing(Location center) {
        World world = center.getWorld();

        if (world == null) return;

        for (double radius = 1.0; radius <= RADIUS; radius += 1.0) {
            final double currentRadius = radius;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (center.getWorld() == null) return;

                for (int i = 0; i < 28; i++) {
                    double angle = (Math.PI * 2.0 * i) / 28.0;
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;

                    Location point = center.clone().add(x, 0.15, z);

                    world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, HOLY_DUST);
                    world.spawnParticle(Particle.END_ROD, point.clone().add(0, 0.15, 0), 1, 0.02, 0.02, 0.02, 0);
                }
            }, Math.round((radius - 1.0) * 2.0));
        }
    }

    private void createAmbientEffect(Location center, int tick) {
        World world = center.getWorld();

        if (world == null) return;

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians((tick * 5) + (i * 60));
            double radius = 3.0 + ((i % 2) * 2.5);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 0.2 + ((tick + i * 4) % 20) * 0.05;

            Location point = center.clone().add(x, y, z);

            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, i % 2 == 0 ? GOLD_DUST : HOLY_DUST);
        }

        if (tick % 10 == 0) createDomainRing(center);
    }

    public void shutdown() {
        for (Domain domain : activeDomains.values()) domain.cancel();

        activeDomains.clear();
    }

    private class Domain extends BukkitRunnable {

        private final UUID playerId;
        private final Location center;
        private int tick = 0;

        private Domain(UUID playerId, Location center) {
            this.playerId = playerId;
            this.center = center;
        }

        @Override
        public void run() {
            Player player = Bukkit.getPlayer(playerId);

            if (player == null || !player.isOnline() || !player.getWorld().equals(center.getWorld())) {
                activeDomains.remove(playerId);
                cancel();
                return;
            }

            createAmbientEffect(center, tick);

            if (tick % PULSE_INTERVAL == 0) pulse(player, center);

            if (tick >= DURATION_TICKS) {
                finishDomain(center);
                activeDomains.remove(playerId);
                cancel();
                return;
            }

            tick += 2;
        }
    }

    private void finishDomain(Location center) {
        World world = center.getWorld();

        if (world == null) return;

        world.spawnParticle(Particle.DUST, center.clone().add(0, 0.5, 0), 60, 3.0, 0.7, 3.0, 0.05, HOLY_DUST);
        world.spawnParticle(Particle.ENCHANT, center.clone().add(0, 0.8, 0), 45, 3.0, 0.8, 3.0, 0.15);
        world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.5f);
    }
}
