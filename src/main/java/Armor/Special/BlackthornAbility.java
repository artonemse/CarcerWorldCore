package Armor.Special;

import Armor.Generic.ArmorStat;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class BlackthornAbility {

    private static final double RADIUS = 8.0;
    private static final double DAMAGE_MULTIPLIER = 2.5;
    private static final long COOLDOWN = 30_000L;

    private final CarcerWorldCore plugin;
    private final NamespacedKey abilityDamageKey;
    private final NamespacedKey blackthornFangKey;

    public BlackthornAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.abilityDamageKey = new NamespacedKey(plugin, "armor_ability_damage");
        this.blackthornFangKey = new NamespacedKey(plugin, "blackthorn_fang");
    }

    public void cast(Player player) {
        Location center = player.getLocation().clone();

        player.getWorld().playSound(center, Sound.ENTITY_RAVAGER_ROAR, 1.3f, 0.55f);
        player.getWorld().playSound(center, Sound.BLOCK_ROOTED_DIRT_BREAK, 2.0f, 0.5f);

        createThornstorm(player, center);

        BukkitRunnable damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                damageEnemies(player, center);
            }
        };

        damageTask.runTaskLater(plugin, 12L);
    }

    private void createThornstorm(Player player, Location center) {
        new BukkitRunnable() {

            private double radius = 1.5;

            @Override
            public void run() {
                if (!player.isOnline() || radius > RADIUS) {
                    cancel();
                    return;
                }

                createThornRing(player, center, radius);

                radius += 1.3;
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void createThornRing(Player player, Location center, double radius) {
        int spikes = Math.max(8, (int) (radius * 5));

        for (int i = 0; i < spikes; i++) {
            double angle = (Math.PI * 2.0 * i) / spikes;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location location = center.clone().add(x, 0, z);
            location = findGround(location);

            EvokerFangs fang = player.getWorld().spawn(location, EvokerFangs.class);

            fang.getPersistentDataContainer().set(blackthornFangKey, PersistentDataType.BYTE, (byte) 1);

            player.getWorld().spawnParticle(Particle.COMPOSTER, location.clone().add(0, 0.3, 0), 4, 0.2, 0.25, 0.2, 0.02);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0, 0.5, 0), 2, 0.15, 0.3, 0.15, 0.01);
        }

        player.getWorld().playSound(center, Sound.BLOCK_ROOTED_DIRT_BREAK, 0.8f, 0.65f);
    }

    private Location findGround(Location location) {
        Location check = location.clone();

        for (int i = 0; i < 4; i++) {
            if (!check.getBlock().getType().isAir() && check.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return check.add(0, 1, 0);
            }

            check.subtract(0, 1, 0);
        }

        return location;
    }

    private void damageEnemies(Player player, Location center) {
        double damage = calculateAbilityDamage(player);

        for (Entity entity : player.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target instanceof Player) continue;
            if (target.equals(player)) continue;

            target.getPersistentDataContainer().set(abilityDamageKey, PersistentDataType.BYTE, (byte) 1);

            target.damage(damage, player);

            Vector direction = target.getLocation().toVector().subtract(center.toVector());

            if (direction.lengthSquared() > 0) {
                direction.normalize();
            }

            direction.multiply(0.9);
            direction.setY(0.5);

            target.setVelocity(direction);
        }

        player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.65f);
    }

    private double calculateAbilityDamage(Player player) {
        double damage = plugin.getWeaponManager().getDamage(player);

        damage += plugin.getEnchantManager().getSharpnessBonus(player);
        damage = plugin.getSkillManager().applyStrength(player, damage);

        if (plugin.getArmorManager() != null) {
            damage *= plugin.getArmorManager().getMultiplier(player, ArmorStat.DAMAGE);
        }

        return damage * DAMAGE_MULTIPLIER;
    }

    public NamespacedKey getAbilityDamageKey() {
        return abilityDamageKey;
    }

    public NamespacedKey getBlackthornFangKey() {
        return blackthornFangKey;
    }

    public long getCooldown() {
        return COOLDOWN;
    }
}