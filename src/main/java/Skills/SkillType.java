package Skills;

import org.bukkit.Material;

public enum SkillType {

    STRENGTH(
            "strength",
            "&c&lStrength",
            Material.BLAZE_POWDER,
            25,
            1
    ),

    HEALTH(
            "health",
            "&a&lHealth",
            Material.GOLDEN_APPLE,
            20,
            3
    ),

    KNOWLEDGE(
            "knowledge",
            "&b&lKnowledge",
            Material.EXPERIENCE_BOTTLE,
            20,
            1
    );

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int maxLevel;
    private final int pointCost;

    SkillType(
            String id,
            String displayName,
            Material icon,
            int maxLevel,
            int pointCost
    ) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.pointCost = pointCost;
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

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getPointCost() {
        return pointCost;
    }
}
