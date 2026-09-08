package Armor.Special;

import org.bukkit.Bukkit;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RoyalGuardAbility {

    private static final double HEAL_PERCENT = 0.60;
    private static final int RESISTANCE_DURATION = 20 * 60;
    private static final int RETALIATION_DURATION = 20 * 6;
    private static final long COOLDOWN = 45_000L;

    private final CarcerWorldCore plugin;
    private final NamespacedKey apparitionKey;
    private final Set<UUID> retaliationPlayers = new HashSet<>();
    private final Map<UUID, List<ArmorStand>> activeGuardians = new HashMap<>();

    public RoyalGuardAbility(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.apparitionKey = new NamespacedKey(plugin, "royal_guard_apparition");

        cleanupOrphanedGuardians();
    }

    public void cast(Player player) {
        cleanupGuardians(player);
        retaliationPlayers.remove(player.getUniqueId());

        Location restorationLocation = getGuardianLocation(player, -1.8);
        Location retributionLocation = getGuardianLocation(player, 1.8);

        ArmorStand restorationGuard = spawnRestorationGuard(restorationLocation);
        ArmorStand retributionGuard = spawnRetributionGuard(retributionLocation);

        List<ArmorStand> guardians = new ArrayList<>();

        guardians.add(restorationGuard);
        guardians.add(retributionGuard);

        activeGuardians.put(player.getUniqueId(), guardians);

        playSummonEffect(player);
        startSequence(player, restorationGuard, retributionGuard);
    }

    private void startSequence(Player player, ArmorStand restorationGuard, ArmorStand retributionGuard) {
        World castWorld = player.getWorld();

        new BukkitRunnable() {

            private int ticks = 0;
            private boolean restorationActivated = false;
            private boolean retaliationActivated = false;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || !player.getWorld().equals(castWorld)) {
                    retaliationPlayers.remove(player.getUniqueId());
                    cleanupGuardians(player);
                    cancel();
                    return;
                }

                updateGuardianPositions(player, restorationGuard, retributionGuard);

                if (restorationGuard.isValid()) createGuardianAura(restorationGuard, true);
                if (retributionGuard.isValid()) createGuardianAura(retributionGuard, false);

                if (!restorationActivated && ticks >= 10) {
                    activateRestoration(player, restorationGuard);
                    restorationActivated = true;
                }

                if (restorationGuard.isValid() && ticks >= 30) fadeGuardian(restorationGuard);

                if (!retaliationActivated && ticks >= 20) {
                    activateRetaliation(player);
                    retaliationActivated = true;
                }

                if (retaliationActivated && ticks < 20 + RETALIATION_DURATION) {
                    createShieldWalls(player, ticks - 20);
                }

                if (ticks >= 20 + RETALIATION_DURATION) {
                    finishRetaliation(player, retributionGuard);
                    cleanupGuardians(player);
                    cancel();
                    return;
                }

                ticks += 2;
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    private ArmorStand spawnRestorationGuard(Location location) {
        ArmorStand guard = createGuardian(location, "§e§lGuardian of Restoration");
        EntityEquipment equipment = guard.getEquipment();

        equipment.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        equipment.setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
        equipment.setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        equipment.setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        equipment.setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));

        guard.setRightArmPose(new EulerAngle(Math.toRadians(300), 0, Math.toRadians(10)));

        return guard;
    }

    private ArmorStand spawnRetributionGuard(Location location) {
        ArmorStand guard = createGuardian(location, "§6§lGuardian of Retribution");
        EntityEquipment equipment = guard.getEquipment();

        equipment.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        equipment.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        equipment.setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
        equipment.setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        equipment.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        equipment.setItemInOffHand(new ItemStack(Material.SHIELD));

        guard.setRightArmPose(new EulerAngle(Math.toRadians(330), 0, Math.toRadians(10)));
        guard.setLeftArmPose(new EulerAngle(Math.toRadians(300), 0, Math.toRadians(-15)));

        return guard;
    }

    private ArmorStand createGuardian(Location location, String name) {
        ArmorStand guard = location.getWorld().spawn(location, ArmorStand.class);

        guard.setVisible(false);
        guard.setArms(true);
        guard.setBasePlate(false);
        guard.setGravity(false);
        guard.setInvulnerable(true);
        guard.setMarker(true);
        guard.setSilent(true);
        guard.setGlowing(true);
        guard.setCustomName(name);
        guard.setCustomNameVisible(true);

        guard.getPersistentDataContainer().set(apparitionKey, PersistentDataType.BYTE, (byte) 1);

        return guard;
    }

    private void activateRestoration(Player player, ArmorStand guard) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealthAttribute != null) {
            double maxHealth = maxHealthAttribute.getValue();
            double healing = maxHealth * HEAL_PERCENT;

            player.setHealth(Math.min(maxHealth, player.getHealth() + healing));
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, RESISTANCE_DURATION, 0, false, true, true));

        Location center = player.getLocation().clone().add(0, 1.0, 0);

        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 60, 1.2, 1.0, 1.2, 0.15);
        player.getWorld().spawnParticle(Particle.END_ROD, center, 35, 1.0, 0.8, 1.0, 0.05);
        player.getWorld().spawnParticle(Particle.HEART, center, 15, 0.8, 0.8, 0.8, 0.05);

        if (guard.isValid()) {
            Location guardCenter = guard.getLocation().clone().add(0, 1.2, 0);

            guard.getWorld().spawnParticle(Particle.END_ROD, guardCenter, 30, 0.5, 1.0, 0.5, 0.05);
            guard.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, guardCenter, 25, 0.5, 0.8, 0.5, 0.08);
        }

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.35f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.4f);

        player.sendMessage("§6§lROYAL GUARD §7§l| §fThe Guardian of Restoration has restored your vitality.");
    }

    private void activateRetaliation(Player player) {
        retaliationPlayers.add(player.getUniqueId());

        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().clone().add(0, 1.0, 0), 40, 1.5, 1.0, 1.5, 0.05);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().clone().add(0, 1.0, 0), 30, 1.3, 1.0, 1.3, 0.08);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 0.75f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.6f);

        player.sendMessage("§6§lROYAL GUARD §7§l| §fRetaliation is active for §e6 seconds§f.");
    }

    private void finishRetaliation(Player player, ArmorStand guard) {
        retaliationPlayers.remove(player.getUniqueId());

        Location center = player.getLocation().clone().add(0, 1.0, 0);

        player.getWorld().spawnParticle(Particle.END_ROD, center, 40, 1.8, 1.2, 1.8, 0.05);
        player.getWorld().spawnParticle(Particle.WAX_ON, center, 30, 1.5, 1.0, 1.5, 0.08);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);

        if (guard.isValid()) fadeGuardian(guard);

        player.sendMessage("§6§lROYAL GUARD §7§l| §fRetaliation has ended.");
    }

    private void createShieldWalls(Player player, int ticks) {
        Location center = player.getLocation().clone();
        double rotation = Math.toRadians(player.getLocation().getYaw()) + (ticks * 0.055);

        for (int i = 0; i < 4; i++) {
            double angle = rotation + ((Math.PI * 2.0 * i) / 4.0);

            createShieldWall(center, angle);
        }
    }

    private void createShieldWall(Location playerCenter, double angle) {
        double radius = 2.2;

        Vector radial = new Vector(Math.cos(angle), 0, Math.sin(angle));
        Vector tangent = new Vector(-Math.sin(angle), 0, Math.cos(angle));

        Location wallCenter = playerCenter.clone().add(radial.clone().multiply(radius));

        for (double y = 0.25; y <= 2.35; y += 0.45) {
            for (double width = -0.8; width <= 0.8; width += 0.4) {
                Location point = wallCenter.clone().add(tangent.clone().multiply(width)).add(0, y, 0);

                playerCenter.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);

                boolean edge = y <= 0.3 || y >= 2.2 || Math.abs(width) >= 0.75;

                if (edge) playerCenter.getWorld().spawnParticle(Particle.WAX_ON, point, 1, 0, 0, 0, 0);
            }
        }
    }

    public void playShieldImpact(Player player, Location attackerLocation) {
        if (!player.getWorld().equals(attackerLocation.getWorld())) return;

        Location playerCenter = player.getLocation().clone().add(0, 1.1, 0);
        Vector direction = attackerLocation.toVector().subtract(playerCenter.toVector());
        direction.setY(0);

        if (direction.lengthSquared() <= 0) direction = player.getLocation().getDirection().clone();

        direction.normalize();

        Location impact = playerCenter.clone().add(direction.multiply(1.8));

        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, impact, 25, 0.4, 0.5, 0.4, 0.12);
        player.getWorld().spawnParticle(Particle.END_ROD, impact, 18, 0.35, 0.45, 0.35, 0.06);
        player.getWorld().spawnParticle(Particle.WAX_ON, impact, 15, 0.35, 0.45, 0.35, 0.08);

        player.getWorld().playSound(impact, Sound.ITEM_SHIELD_BLOCK, 1.3f, 0.8f);
    }

    private void updateGuardianPositions(Player player, ArmorStand restorationGuard, ArmorStand retributionGuard) {
        if (restorationGuard.isValid()) restorationGuard.teleport(getGuardianLocation(player, -1.8));
        if (retributionGuard.isValid()) retributionGuard.teleport(getGuardianLocation(player, 1.8));
    }

    private Location getGuardianLocation(Player player, double side) {
        Location location = player.getLocation().clone();

        Vector forward = location.getDirection().clone();
        forward.setY(0);

        if (forward.lengthSquared() <= 0) forward = new Vector(0, 0, 1);

        forward.normalize();

        Vector right = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        location.add(right.multiply(side));
        location.subtract(forward.multiply(0.5));
        location.setYaw(player.getLocation().getYaw());

        return location;
    }

    private void createGuardianAura(ArmorStand guard, boolean restoration) {
        Location center = guard.getLocation().clone().add(0, 1.0, 0);

        guard.getWorld().spawnParticle(Particle.END_ROD, center, 2, 0.35, 0.8, 0.35, 0.02);

        if (restoration) {
            guard.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center, 1, 0.25, 0.6, 0.25, 0.01);
        } else {
            guard.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 1, 0.25, 0.6, 0.25, 0.02);
        }
    }

    private void playSummonEffect(Player player) {
        Location center = player.getLocation().clone().add(0, 1.0, 0);

        createCircle(center, 2.3, 24);

        player.getWorld().spawnParticle(Particle.END_ROD, center, 35, 1.8, 1.0, 1.8, 0.04);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 25, 1.4, 0.8, 1.4, 0.08);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 1.25f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.2f, 0.8f);
    }

    private void createCircle(Location center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
        }
    }

    private void fadeGuardian(ArmorStand guard) {
        if (!guard.isValid()) return;

        Location center = guard.getLocation().clone().add(0, 1.0, 0);

        guard.getWorld().spawnParticle(Particle.END_ROD, center, 20, 0.5, 0.8, 0.5, 0.05);
        guard.getWorld().spawnParticle(Particle.WAX_ON, center, 20, 0.5, 0.8, 0.5, 0.06);

        guard.remove();
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

    public boolean isRetaliationActive(Player player) {
        return retaliationPlayers.contains(player.getUniqueId());
    }

    public long getCooldown() {
        return COOLDOWN;
    }

    public void shutdown() {
        retaliationPlayers.clear();

        for (List<ArmorStand> guardians : activeGuardians.values()) {
            for (ArmorStand guard : guardians) {
                if (guard != null && guard.isValid()) guard.remove();
            }
        }

        activeGuardians.clear();
        cleanupOrphanedGuardians();
    }
}
