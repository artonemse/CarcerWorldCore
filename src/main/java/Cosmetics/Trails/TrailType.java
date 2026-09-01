package Cosmetics.Trails;

import org.bukkit.Material;
import org.bukkit.Particle;

public enum TrailType {

    NONE("none", "&7&lNone", Material.BARRIER, 0, null),

    CHERRY_BLOSSOM("cherry_blossom", "&d&lCherry Blossom Trail",
            Material.CHERRY_LEAVES, 750, Particle.CHERRY_LEAVES),

    HELLFIRE("hellfire", "&c&lHellfire Trail",
            Material.BLAZE_POWDER, 1000, Particle.FLAME),

    SOULMIST("soulmist", "&b&lSoulmist Trail",
            Material.SOUL_LANTERN, 1250, Particle.SOUL),

    VOIDWALKER("voidwalker", "&5&lVoidwalker Trail",
            Material.ENDER_EYE, 1500, Particle.PORTAL),

    FROSTBOUND("frostbound", "&b&lFrostbound Trail",
            Material.BLUE_ICE, 1000, Particle.SNOWFLAKE),

    DIVINE("divine", "&e&lDivine Trail",
            Material.END_ROD, 2000, Particle.END_ROD),

    ARCANE("arcane", "&d&lArcane Trail",
            Material.ENCHANTING_TABLE, 1750, Particle.ENCHANT);

    private final String id;
    private final String displayName;
    private final Material icon;
    private final long gemCost;
    private final Particle particle;

    TrailType(String id, String displayName, Material icon, long gemCost, Particle particle) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.gemCost = gemCost;
        this.particle = particle;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public long getGemCost() {
        return gemCost;
    }

    public Particle getParticle() {
        return particle;
    }

    public static TrailType fromId(String id) {
        if (id == null) return NONE;

        for (TrailType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) return type;
        }

        return NONE;
    }
}