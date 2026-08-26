package Enchantments;

import org.bukkit.Material;

public enum EnchantType {

    SHARPNESS(
            "sharpness",
            "&c&lSharpness",
            Material.IRON_SWORD,
            25,
            2.0,
            250
    ),

    CRITICAL_STRIKE(
            "critical_strike",
            "&6&lCritical Strike",
            Material.NETHER_STAR,
            10,
            2.0,
            500
    ),

    DOUBLE_STRIKE(
            "double_strike",
            "&e&lDouble Strike",
            Material.GOLDEN_SWORD,
            10,
            1.0,
            750
    ),

    CLEAVE(
            "cleave",
            "&5&lCleave",
            Material.DIAMOND_AXE,
            10,
            2.0,
            750
    ),

    EXECUTE(
            "execute",
            "&4&lExecute",
            Material.NETHERITE_AXE,
            10,
            1.0,
            1000
    );

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int maxLevel;
    private final double valuePerLevel;
    private final long baseCost;

    EnchantType(
            String id,
            String displayName,
            Material icon,
            int maxLevel,
            double valuePerLevel,
            long baseCost
    ) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.valuePerLevel = valuePerLevel;
        this.baseCost = baseCost;
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

    public double getValuePerLevel() {
        return valuePerLevel;
    }

    public long getBaseCost() {
        return baseCost;
    }

    public long getUpgradeCost(int currentLevel) {
        return baseCost * (currentLevel + 1L);
    }
}
