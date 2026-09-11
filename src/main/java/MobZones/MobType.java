package MobZones;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

public class MobType {

    private final String id;
    private final EntityType entityType;
    private final String name;
    private final double health;
    private final String modelId;

    public MobType(String id, EntityType entityType, String name, double health, String modelId) {
        this.id = id;
        this.entityType = entityType;
        this.name = name;
        this.health = health;
        this.modelId = modelId;
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

    public String getModelId() {
        return modelId;
    }

    public boolean hasModel() {
        return modelId != null && !modelId.isBlank();
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}