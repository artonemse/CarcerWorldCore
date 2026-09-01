package MobScaling;

import PlayerData.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class MobScalingManager {

    private final CarcerWorldCore plugin;
    private final NamespacedKey scaledKey;
    private final NamespacedKey scalingLevelKey;

    private static final double ACTIVATION_RADIUS = 32.0;

    public MobScalingManager(CarcerWorldCore plugin) {
        this.plugin = plugin;

        this.scaledKey =
                new NamespacedKey(plugin, "mob_scaled");

        this.scalingLevelKey =
                new NamespacedKey(plugin, "mob_scaling_level");
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {

                            if (!(entity instanceof LivingEntity mob))
                                continue;

                            if (mob instanceof Player)
                                continue;

                            if (isScaled(mob))
                                continue;

                            Player nearbyPlayer =
                                    getHighestLevelNearbyPlayer(mob);

                            if (nearbyPlayer == null)
                                continue;

                            scaleMob(mob, nearbyPlayer);
                        }
                    }
                },
                20L,
                20L
        );
    }

    public void scaleMob(LivingEntity mob, Player player) {
        if (isScaled(mob)) return;

        PlayerData data =
                plugin.getPlayerDataManager()
                        .getPlayerData(player);

        int weaponLevel =
                data.getWeaponLevel();

        AttributeInstance maxHealthAttribute =
                mob.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealthAttribute == null) return;

        double baseHealth =
                maxHealthAttribute.getBaseValue();

        double multiplier =
                getHealthMultiplier(weaponLevel);

        double scaledHealth =
                baseHealth * multiplier;

        maxHealthAttribute.setBaseValue(scaledHealth);
        mob.setHealth(scaledHealth);

        mob.getPersistentDataContainer().set(
                scaledKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        mob.getPersistentDataContainer().set(
                scalingLevelKey,
                PersistentDataType.INTEGER,
                weaponLevel
        );

        plugin.getMobHealthBarManager()
                .updateHealthBar(mob);
    }

    private Player getHighestLevelNearbyPlayer(
            LivingEntity mob
    ) {
        Player highestPlayer = null;
        int highestLevel = -1;

        double radiusSquared =
                ACTIVATION_RADIUS * ACTIVATION_RADIUS;

        for (Player player : mob.getWorld().getPlayers()) {
            if (!player.isOnline()) continue;
            if (player.isDead()) continue;

            if (player.getLocation()
                    .distanceSquared(mob.getLocation())
                    > radiusSquared)
                continue;

            int level =
                    plugin.getPlayerDataManager()
                            .getPlayerData(player)
                            .getWeaponLevel();

            if (level > highestLevel) {
                highestLevel = level;
                highestPlayer = player;
            }
        }

        return highestPlayer;
    }

    public double getHealthMultiplier(int weaponLevel) {
        return 1.0 + ((weaponLevel - 1) * 0.25);
    }

    public boolean isScaled(LivingEntity mob) {
        return mob.getPersistentDataContainer().has(
                scaledKey,
                PersistentDataType.BYTE
        );
    }

    public int getScalingLevel(LivingEntity mob) {
        Integer level =
                mob.getPersistentDataContainer().get(
                        scalingLevelKey,
                        PersistentDataType.INTEGER
                );

        return level != null ? level : 1;
    }
}