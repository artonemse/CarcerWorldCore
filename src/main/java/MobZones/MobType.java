package MobZones;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

public class MobType {

    private final String id;
    private final EntityType entityType;
    private final String name;
    private final double health;

    public MobType(String id, EntityType entityType, String name, double health) {
        this.id = id;
        this.entityType = entityType;
        this.name = name;
        this.health = health;
    }

    public String getId() {
        return id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getName() {
        return color(name);
    }

    public double getHealth() {
        return health;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}