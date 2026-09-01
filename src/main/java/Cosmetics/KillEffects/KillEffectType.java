package Cosmetics.KillEffects;

import org.bukkit.Material;

public enum KillEffectType {

    NONE("none", "&7&lNone", Material.BARRIER, 0, null),

    CHERRY_BLOSSOM("cherry_blossom", "&d&lCherry Blossom", Material.CHERRY_LEAVES, 500, "carcerworld.killeffect.cherry_blossom"),

    WATER_PILLAR("water_pillar", "&b&lWater Pillar", Material.WATER_BUCKET, 1200, "carcerworld.killeffect.water_pillar"),

    HELLFIRE_SKULL("hellfire_skull", "&4&lHellfire Skull", Material.WITHER_SKELETON_SKULL, 5000, "carcerworld.killeffect.hellfire_skull"),

    CASH_EXPLOSION("cash_explosion", "&a&lCash Explosion", Material.EMERALD, 2000, "carcerworld.killeffect.cash_explosion"),

    VOID_RIFT("void_rift", "&5&lVoid Rift", Material.ENDER_EYE, 4000, "carcerworld.killeffect.void_rift"),

    BUTTERFLY_SWARM("butterfly_swarm", "&d&lButterfly Swarm", Material.PINK_DYE, 3000, "carcerworld.killeffect.butterfly_swarm"),

    DIVINE_ASCENSION("divine_ascension", "&e&lDivine Ascension", Material.END_ROD, 6000, "carcerworld.killeffect.divine_ascension");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final long gemCost;
    private final String permission;

    KillEffectType(String id, String displayName, Material icon, long gemCost, String permission) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.gemCost = gemCost;
        this.permission = permission;
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

    public String getPermission() {
        return permission;
    }

    public static KillEffectType fromId(String id) {
        if (id == null) return NONE;

        for (KillEffectType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) return type;
        }

        return NONE;
    }
}
