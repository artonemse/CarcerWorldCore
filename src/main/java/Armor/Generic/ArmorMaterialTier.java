package Armor.Generic;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public enum ArmorMaterialTier {

    LEATHER(
            "Leather",
            150,
            200,
            50,
            80
    ),

    COPPER(
            "Copper",
            130,
            175,
            40,
            70
    ),

    GOLD(
            "Gold",
            110,
            150,
            30,
            60
    ),

    CHAINMAIL(
            "Chainmail",
            90,
            130,
            25,
            50
    ),

    IRON(
            "Iron",
            70,
            115,
            15,
            40
    ),

    DIAMOND(
            "Diamond",
            50,
            100,
            10,
            30
    );

    private final String displayName;
    private final int minBuff;
    private final int maxBuff;
    private final int minDebuff;
    private final int maxDebuff;

    ArmorMaterialTier(String displayName, int minBuff, int maxBuff, int minDebuff, int maxDebuff) {
        this.displayName = displayName;
        this.minBuff = minBuff;
        this.maxBuff = maxBuff;
        this.minDebuff = minDebuff;
        this.maxDebuff = maxDebuff;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double rollBuff() {
        return ThreadLocalRandom.current().nextInt(minBuff, maxBuff + 1);
    }

    public double rollDebuff() {
        return ThreadLocalRandom.current().nextInt(minDebuff, maxDebuff + 1);
    }

    public ItemStack createBaseItem(EquipmentSlot slot) {
        Material material = switch (this) {
            case LEATHER -> switch (slot) {
                case HEAD -> Material.LEATHER_HELMET;
                case CHEST -> Material.LEATHER_CHESTPLATE;
                case LEGS -> Material.LEATHER_LEGGINGS;
                case FEET -> Material.LEATHER_BOOTS;
                default -> Material.LEATHER_CHESTPLATE;
            };

            case COPPER -> switch (slot) {
                case HEAD -> Material.COPPER_HELMET;
                case CHEST -> Material.COPPER_CHESTPLATE;
                case LEGS -> Material.COPPER_LEGGINGS;
                case FEET -> Material.COPPER_BOOTS;
                default -> Material.COPPER_CHESTPLATE;
            };

            case GOLD -> switch (slot) {
                case HEAD -> Material.GOLDEN_HELMET;
                case CHEST -> Material.GOLDEN_CHESTPLATE;
                case LEGS -> Material.GOLDEN_LEGGINGS;
                case FEET -> Material.GOLDEN_BOOTS;
                default -> Material.GOLDEN_CHESTPLATE;
            };

            case CHAINMAIL -> switch (slot) {
                case HEAD -> Material.CHAINMAIL_HELMET;
                case CHEST -> Material.CHAINMAIL_CHESTPLATE;
                case LEGS -> Material.CHAINMAIL_LEGGINGS;
                case FEET -> Material.CHAINMAIL_BOOTS;
                default -> Material.CHAINMAIL_CHESTPLATE;
            };

            case IRON -> switch (slot) {
                case HEAD -> Material.IRON_HELMET;
                case CHEST -> Material.IRON_CHESTPLATE;
                case LEGS -> Material.IRON_LEGGINGS;
                case FEET -> Material.IRON_BOOTS;
                default -> Material.IRON_CHESTPLATE;
            };

            case DIAMOND -> switch (slot) {
                case HEAD -> Material.DIAMOND_HELMET;
                case CHEST -> Material.DIAMOND_CHESTPLATE;
                case LEGS -> Material.DIAMOND_LEGGINGS;
                case FEET -> Material.DIAMOND_BOOTS;
                default -> Material.DIAMOND_CHESTPLATE;
            };
        };

        return new ItemStack(material);
    }
}