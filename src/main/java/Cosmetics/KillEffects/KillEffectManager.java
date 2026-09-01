package Cosmetics.KillEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class KillEffectManager {

    private final CarcerWorldCore plugin;

    public KillEffectManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    // ================================
    // OWNERSHIP
    // ================================

    public boolean ownsEffect(Player player, KillEffectType type) {
        if (type == KillEffectType.NONE) return true;

        if (plugin.getPlayerDataManager().ownsKillEffect(player.getUniqueId(), type.getId())) return true;

        return type.getPermission() != null && player.hasPermission(type.getPermission());
    }

    // ================================
    // SELECTED EFFECT
    // ================================

    public KillEffectType getSelectedEffect(Player player) {
        String id = plugin.getPlayerDataManager().getSelectedKillEffect(player.getUniqueId());
        KillEffectType type = KillEffectType.fromId(id);

        if (!ownsEffect(player, type)) return KillEffectType.NONE;

        return type;
    }

    public void selectEffect(Player player, KillEffectType type) {
        if (!ownsEffect(player, type)) {
            player.sendMessage(color("&c&lCOSMETICS &7&l| &fYou do not own this Kill Effect."));
            return;
        }

        plugin.getPlayerDataManager().setSelectedKillEffect(player.getUniqueId(), type.getId());

        if (type == KillEffectType.NONE) {
            player.sendMessage(color("&7&lCOSMETICS &7&l| &fYour Kill Effect has been disabled."));
            return;
        }

        player.sendMessage(color("&a&lCOSMETICS &7&l| &fSelected " + type.getDisplayName() + "&f."));
    }

    // ================================
    // PURCHASE
    // ================================

    public boolean purchaseEffect(Player player, KillEffectType type) {
        if (type == KillEffectType.NONE) return false;

        if (ownsEffect(player, type)) {
            selectEffect(player, type);
            return true;
        }

        long cost = type.getGemCost();

        if (!plugin.getGemManager().hasGems(player, cost)) {
            player.sendMessage(color("&c&lCOSMETICS &7&l| &fYou need &c" + format(cost) + " Gems &fto purchase this effect."));
            return false;
        }

        if (!plugin.getGemManager().removeGems(player, cost)) return false;

        plugin.getPlayerDataManager().setKillEffectOwned(player.getUniqueId(), type.getId(), true);
        plugin.getPlayerDataManager().setSelectedKillEffect(player.getUniqueId(), type.getId());

        player.sendMessage(color("&a&lCOSMETICS &7&l| &fPurchased " + type.getDisplayName() + " &ffor &a" + format(cost) + " Gems&f."));
        return true;
    }

    // ================================
    // PLAY EFFECT
    // ================================

    public void playKillEffect(Player player, Location location) {
        KillEffectType type = getSelectedEffect(player);

        switch (type) {
            case CHERRY_BLOSSOM -> playCherryBlossom(player, location);
            case WATER_PILLAR -> playWaterPillar(player, location);
            case HELLFIRE_SKULL -> playHellfireSkull(player, location);
            case CASH_EXPLOSION -> playCashExplosion(player, location);
            case VOID_RIFT -> playVoidRift(player, location);
            case BUTTERFLY_SWARM -> playButterflySwarm(player, location);
            case DIVINE_ASCENSION -> playDivineAscension(player, location);
            default -> {
            }
        }
    }

    // ================================
    // CHERRY BLOSSOM
    // ================================

    private void playCherryBlossom(Player player, Location location) {
        Location center = location.clone().add(0, 1, 0);

        player.spawnParticle(Particle.CHERRY_LEAVES, center, 50, 0.8, 0.8, 0.8, 0.02);
        player.spawnParticle(Particle.END_ROD, center, 8, 0.4, 0.5, 0.4, 0.01);

        player.playSound(center, Sound.BLOCK_CHERRY_LEAVES_BREAK, 1.0f, 1.4f);
    }

    // ================================
    // WATER PILLAR
    // ================================

    private void playWaterPillar(Player player, Location location) {
        Location base = location.clone();

        for (double y = 0; y <= 4.0; y += 0.10) {
            Location center = base.clone().add(0, y, 0);

            player.spawnParticle(Particle.FALLING_WATER, center, 12, 0.08, 0.02, 0.08, 0);
            player.spawnParticle(Particle.BUBBLE_POP, center, 4, 0.05, 0.02, 0.05, 0);

            if (((int) (y * 10)) % 3 == 0) player.spawnParticle(Particle.SPLASH, center, 3, 0.12, 0.02, 0.12, 0);
        }

        Location top = base.clone().add(0, 4.0, 0);

        player.spawnParticle(Particle.SPLASH, top, 40, 0.4, 0.1, 0.4, 0);
        player.playSound(base, Sound.ENTITY_PLAYER_SPLASH, 1.0f, 0.8f);
        player.playSound(base, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0f, 1.2f);
    }

    // ================================
    // HELLFIRE SKULL
    // ================================

    private void playHellfireSkull(Player player, Location location) {
        Location base = location.clone();

        for (double y = 0; y <= 4.5; y += 0.12) {
            Location center = base.clone().add(0, y, 0);

            player.spawnParticle(Particle.FLAME, center, 10, 0.10, 0.02, 0.10, 0);
            player.spawnParticle(Particle.LARGE_SMOKE, center, 3, 0.05, 0.02, 0.05, 0);
        }

        Location top = base.clone().add(0, 4.5, 0);

        player.spawnParticle(Particle.FLAME, top, 50, 0.6, 0.3, 0.6, 0);
        player.spawnParticle(Particle.LARGE_SMOKE, top, 25, 0.4, 0.2, 0.4, 0);

        player.playSound(base, Sound.ITEM_FIRECHARGE_USE, 1.2f, 0.7f);

        Location skullLocation = base.clone().add(0, 1.8, 0);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            spawnSmokeSkull(player, skullLocation);
        }, 8L);
    }

    private void spawnSmokeSkull(Player player, Location center) {
        double[][] points = {
                {-0.7, 0.9},
                {-0.35, 1.05},
                {0.0, 1.15},
                {0.35, 1.05},
                {0.7, 0.9},

                {-0.8, 0.45},
                {0.8, 0.45},

                {-0.7, 0.0},
                {0.7, 0.0},

                {-0.5, -0.35},
                {0.5, -0.35},

                {-0.28, 0.45},
                {0.28, 0.45},

                {0.0, 0.15},

                {-0.35, -0.75},
                {0.35, -0.75}
        };

        Vector facing = player.getEyeLocation().toVector().subtract(center.toVector()).normalize();
        Vector right = new Vector(-facing.getZ(), 0, facing.getX()).normalize();
        Vector up = new Vector(0, 1, 0);

        for (double[] point : points) {
            Vector offset = right.clone().multiply(point[0]).add(up.clone().multiply(point[1]));
            Location particleLocation = center.clone().add(offset);

            player.spawnParticle(Particle.LARGE_SMOKE, particleLocation, 6, 0.03, 0.03, 0.03, 0);
        }

        Location leftEye = center.clone().add(right.clone().multiply(-0.28)).add(up.clone().multiply(0.45));
        Location rightEye = center.clone().add(right.clone().multiply(0.28)).add(up.clone().multiply(0.45));

        player.spawnParticle(Particle.SOUL_FIRE_FLAME, leftEye, 5, 0.02, 0.02, 0.02, 0);
        player.spawnParticle(Particle.SOUL_FIRE_FLAME, rightEye, 5, 0.02, 0.02, 0.02, 0);

        player.playSound(center, Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.8f);
    }

    // ================================
    // CASH EXPLOSION
    // ================================

    private void playCashExplosion(Player player, Location location) {
        Location center = location.clone().add(0, 1, 0);

        player.spawnParticle(Particle.HAPPY_VILLAGER, center, 45, 0.9, 0.8, 0.9, 0.05);
        player.spawnParticle(Particle.END_ROD, center, 25, 0.7, 0.7, 0.7, 0.05);

        player.playSound(center, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.7f);
        player.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.8f);
    }

    // ================================
    // VOID RIFT
    // ================================

    private void playVoidRift(Player player, Location location) {
        Location center = location.clone().add(0, 1, 0);

        player.spawnParticle(Particle.REVERSE_PORTAL, center, 100, 0.8, 0.8, 0.8, 0.20);
        player.spawnParticle(Particle.PORTAL, center, 60, 0.6, 0.6, 0.6, 0.10);
        player.spawnParticle(Particle.WITCH, center, 30, 0.5, 0.5, 0.5, 0.03);

        player.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.4f);
        player.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8f, 1.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            player.spawnParticle(Particle.REVERSE_PORTAL, center, 120, 0.35, 0.35, 0.35, 0.35);
            player.spawnParticle(Particle.LARGE_SMOKE, center, 30, 0.25, 0.25, 0.25, 0.02);
        }, 6L);
    }

    // ================================
    // BUTTERFLY SWARM
    // ================================

    private void playButterflySwarm(Player player, Location location) {
        Location base = location.clone().add(0, 0.8, 0);

        for (int i = 0; i < 45; i++) {
            double angle = i * 0.45;
            double radius = 0.25 + (i * 0.025);
            double y = i * 0.045;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLocation = base.clone().add(x, y, z);

            player.spawnParticle(Particle.CHERRY_LEAVES, particleLocation, 2, 0.05, 0.05, 0.05, 0.01);

            if (i % 3 == 0) player.spawnParticle(Particle.END_ROD, particleLocation, 1, 0.02, 0.02, 0.02, 0.01);
        }

        player.playSound(base, Sound.BLOCK_CHERRY_LEAVES_BREAK, 1.0f, 1.7f);
        player.playSound(base, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 0.7f, 1.8f);
    }

    // ================================
    // DIVINE ASCENSION
    // ================================

    private void playDivineAscension(Player player, Location location) {
        Location base = location.clone();

        for (double y = 0; y <= 4.5; y += 0.15) {
            Location center = base.clone().add(0, y, 0);

            player.spawnParticle(Particle.END_ROD, center, 8, 0.12, 0.02, 0.12, 0.01);
            player.spawnParticle(Particle.ENCHANT, center, 6, 0.18, 0.02, 0.18, 0.01);
        }

        Location top = base.clone().add(0, 4.5, 0);

        player.spawnParticle(Particle.END_ROD, top, 90, 0.8, 0.3, 0.8, 0.05);
        player.spawnParticle(Particle.ENCHANT, top, 45, 0.6, 0.2, 0.6, 0.05);

        player.playSound(base, Sound.ITEM_TOTEM_USE, 0.8f, 1.4f);
        player.playSound(base, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.6f);
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
