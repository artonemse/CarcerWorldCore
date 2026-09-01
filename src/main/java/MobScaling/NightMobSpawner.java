package MobScaling;

import MobZones.MobType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class NightMobSpawner {

    private final JavaPlugin plugin;

    private static final int SPAWN_INTERVAL = 100; // 5 seconds
    private static final int MAX_MOBS = 25;
    private static final int MOB_CHECK_RADIUS = 50;

    private static final int MIN_DISTANCE = 15;
    private static final int MAX_DISTANCE = 35;

    private static final int UNDERGROUND_TOLERANCE = 5;

    public NightMobSpawner(JavaPlugin plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers())
                    trySpawnNearPlayer(player);
            }
        }.runTaskTimer(plugin, 60L, SPAWN_INTERVAL);
    }

    private void trySpawnNearPlayer(Player player) {
        World world = player.getWorld();

        if (!world.getName().equalsIgnoreCase("Seratari")) return;
        if (!isNight(world)) return;

        long nearbyMobs = player.getNearbyEntities(
                MOB_CHECK_RADIUS,
                MOB_CHECK_RADIUS,
                MOB_CHECK_RADIUS
        ).stream().filter(entity -> entity instanceof Monster).count();

        if (nearbyMobs >= MAX_MOBS) return;

        Location spawnLocation = findSpawnLocation(player);
        if (spawnLocation == null) return;

        MobType mobType =
                ((org.carcercore.carcerWorldCore.CarcerWorldCore) plugin)
                        .getMobZoneManager()
                        .getRandomMob(spawnLocation);

        if (mobType == null) return;

        LivingEntity mob = (LivingEntity) world.spawnEntity(
                spawnLocation,
                mobType.getEntityType()
        );

        AttributeInstance maxHealth =
                mob.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(mobType.getHealth());
            mob.setHealth(mobType.getHealth());
        }

        mob.setCustomName(mobType.getName());
        mob.setCustomNameVisible(false);
        ((org.carcercore.carcerWorldCore.CarcerWorldCore) plugin).getMobSoulRewardManager().registerMob(mob, mobType);
    }

    private Location findSpawnLocation(Player player) {
        if (isUnderground(player))
            return findUndergroundSpawnLocation(player);

        return findSurfaceSpawnLocation(player);
    }

    private boolean isUnderground(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        int surfaceY = world.getHighestBlockYAt(
                location.getBlockX(),
                location.getBlockZ(),
                HeightMap.MOTION_BLOCKING_NO_LEAVES
        );

        return location.getBlockY()
                < surfaceY - UNDERGROUND_TOLERANCE;
    }

    private Location findSurfaceSpawnLocation(Player player) {
        World world = player.getWorld();

        for (int attempt = 0; attempt < 20; attempt++) {
            Location randomLocation =
                    getRandomHorizontalLocation(player);

            int x = randomLocation.getBlockX();
            int z = randomLocation.getBlockZ();

            int groundY = world.getHighestBlockYAt(
                    x,
                    z,
                    HeightMap.MOTION_BLOCKING_NO_LEAVES
            );

            Block ground =
                    world.getBlockAt(x, groundY, z);

            Block feet =
                    world.getBlockAt(x, groundY + 1, z);

            Block head =
                    world.getBlockAt(x, groundY + 2, z);

            if (!isValidSpawnPosition(
                    ground,
                    feet,
                    head
            )) continue;

            return new Location(
                    world,
                    x + 0.5,
                    groundY + 1,
                    z + 0.5
            );
        }

        return null;
    }

    private Location findUndergroundSpawnLocation(Player player) {
        World world = player.getWorld();

        int playerY =
                player.getLocation().getBlockY();

        for (int attempt = 0; attempt < 20; attempt++) {
            Location randomLocation =
                    getRandomHorizontalLocation(player);

            int x = randomLocation.getBlockX();
            int z = randomLocation.getBlockZ();

            for (int offset = 8; offset >= -12; offset--) {
                int groundY = playerY + offset;

                if (groundY <= world.getMinHeight())
                    continue;

                if (groundY + 2 >= world.getMaxHeight())
                    continue;

                Block ground =
                        world.getBlockAt(x, groundY, z);

                Block feet =
                        world.getBlockAt(x, groundY + 1, z);

                Block head =
                        world.getBlockAt(x, groundY + 2, z);

                if (!isValidSpawnPosition(
                        ground,
                        feet,
                        head
                )) continue;

                return new Location(
                        world,
                        x + 0.5,
                        groundY + 1,
                        z + 0.5
                );
            }
        }

        return null;
    }

    private Location getRandomHorizontalLocation(Player player) {
        double angle =
                ThreadLocalRandom.current()
                        .nextDouble(0, Math.PI * 2);

        double distance =
                ThreadLocalRandom.current()
                        .nextDouble(
                                MIN_DISTANCE,
                                MAX_DISTANCE
                        );

        double x =
                player.getLocation().getX()
                        + Math.cos(angle) * distance;

        double z =
                player.getLocation().getZ()
                        + Math.sin(angle) * distance;

        return new Location(
                player.getWorld(),
                x,
                player.getLocation().getY(),
                z
        );
    }

    private boolean isValidSpawnPosition(
            Block ground,
            Block feet,
            Block head
    ) {
        if (!isValidGround(ground)) return false;

        if (!feet.isPassable()) return false;
        if (!head.isPassable()) return false;

        if (isLiquid(feet)) return false;
        if (isLiquid(head)) return false;

        return true;
    }

    private boolean isValidGround(Block block) {
        Material material = block.getType();

        if (!material.isSolid()) return false;

        if (material == Material.OAK_LEAVES) return false;
        if (material == Material.SPRUCE_LEAVES) return false;
        if (material == Material.BIRCH_LEAVES) return false;
        if (material == Material.JUNGLE_LEAVES) return false;
        if (material == Material.ACACIA_LEAVES) return false;
        if (material == Material.DARK_OAK_LEAVES) return false;
        if (material == Material.MANGROVE_LEAVES) return false;
        if (material == Material.CHERRY_LEAVES) return false;
        if (material == Material.AZALEA_LEAVES) return false;
        if (material == Material.FLOWERING_AZALEA_LEAVES) return false;

        if (material == Material.OAK_LOG) return false;
        if (material == Material.SPRUCE_LOG) return false;
        if (material == Material.BIRCH_LOG) return false;
        if (material == Material.JUNGLE_LOG) return false;
        if (material == Material.ACACIA_LOG) return false;
        if (material == Material.DARK_OAK_LOG) return false;
        if (material == Material.MANGROVE_LOG) return false;
        if (material == Material.CHERRY_LOG) return false;

        return !isLiquid(block);
    }

    private boolean isLiquid(Block block) {
        if (block.isLiquid()) return true;

        if (block.getBlockData()
                instanceof Waterlogged waterlogged)
            return waterlogged.isWaterlogged();

        return false;
    }

    private boolean isNight(World world) {
        long time = world.getTime();

        return time >= 13000
                && time <= 23000;
    }
}