package MobZones;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobZone {

    private final String id;
    private final String world;
    private final double x;
    private final double z;
    private final double radius;
    private final List<MobZoneEntry> mobs;

    public MobZone(String id, String world, double x, double z, double radius, List<MobZoneEntry> mobs) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
        this.radius = radius;
        this.mobs = new ArrayList<>(mobs);
    }

    public String getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public double getRadius() {
        return radius;
    }

    public List<MobZoneEntry> getMobs() {
        return mobs;
    }

    public boolean contains(Location location) {
        if (location.getWorld() == null) return false;
        if (!location.getWorld().getName().equalsIgnoreCase(world)) return false;

        double dx = location.getX() - x;
        double dz = location.getZ() - z;

        return (dx * dx) + (dz * dz) <= radius * radius;
    }

    public MobType getRandomMob() {
        if (mobs.isEmpty()) return null;

        int totalWeight = 0;

        for (MobZoneEntry entry : mobs)
            totalWeight += entry.getWeight();

        if (totalWeight <= 0) return null;

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;

        for (MobZoneEntry entry : mobs) {
            current += entry.getWeight();

            if (roll < current)
                return entry.getMobType();
        }

        return mobs.getFirst().getMobType();
    }
}
