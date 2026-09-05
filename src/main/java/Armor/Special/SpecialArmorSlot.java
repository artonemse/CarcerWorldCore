package Armor.Special;

import org.bukkit.Material;

public enum SpecialArmorSlot {

    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS;

    public Material getMaterial() {
        return switch (this) {
            case HELMET -> Material.NETHERITE_HELMET;
            case CHESTPLATE -> Material.NETHERITE_CHESTPLATE;
            case LEGGINGS -> Material.NETHERITE_LEGGINGS;
            case BOOTS -> Material.NETHERITE_BOOTS;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case HELMET -> "Helmet";
            case CHESTPLATE -> "Chestplate";
            case LEGGINGS -> "Leggings";
            case BOOTS -> "Boots";
        };
    }
}
