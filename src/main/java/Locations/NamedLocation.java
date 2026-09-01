package Locations;

public class NamedLocation {

    private final String id;
    private final String name;
    private final String world;
    private final LocationShape shape;
    private final String subtitle;

    private final double x;
    private final double z;
    private final double radius;

    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;
    private final double minZ;
    private final double maxZ;

    public NamedLocation(String id, String name, String world, double x, double z, double radius, String subtitle) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.shape = LocationShape.RADIUS;
        this.subtitle = subtitle;

        this.x = x;
        this.z = z;
        this.radius = radius;

        this.minX = 0;
        this.maxX = 0;
        this.minY = 0;
        this.maxY = 0;
        this.minZ = 0;
        this.maxZ = 0;
    }

    public NamedLocation(String id, String name, String world, double minX, double maxX, double minY, double maxY, double minZ, double maxZ, String subtitle) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.shape = LocationShape.BOX;
        this.subtitle = subtitle;

        this.x = 0;
        this.z = 0;
        this.radius = 0;

        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public LocationShape getShape() {
        return shape;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean contains(String worldName, double playerX, double playerY, double playerZ) {
        if (!world.equalsIgnoreCase(worldName)) return false;

        if (shape == LocationShape.RADIUS) {
            double dx = playerX - x;
            double dz = playerZ - z;

            return (dx * dx) + (dz * dz) <= radius * radius;
        }

        return playerX >= minX && playerX <= maxX
                && playerY >= minY && playerY <= maxY
                && playerZ >= minZ && playerZ <= maxZ;
    }

    public enum LocationShape {
        RADIUS,
        BOX
    }
}