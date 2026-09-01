package Armor.Generic;

import org.bukkit.inventory.EquipmentSlot;

public class GenericArmorData {

    private final ArmorMaterialTier materialTier;
    private final EquipmentSlot slot;

    private final ArmorStat buffOne;
    private final double buffOneValue;

    private final ArmorStat buffTwo;
    private final double buffTwoValue;

    private final ArmorStat debuff;
    private final double debuffValue;

    public GenericArmorData(
            ArmorMaterialTier materialTier,
            EquipmentSlot slot,
            ArmorStat buffOne,
            double buffOneValue,
            ArmorStat buffTwo,
            double buffTwoValue,
            ArmorStat debuff,
            double debuffValue
    ) {
        this.materialTier = materialTier;
        this.slot = slot;

        this.buffOne = buffOne;
        this.buffOneValue = buffOneValue;

        this.buffTwo = buffTwo;
        this.buffTwoValue = buffTwoValue;

        this.debuff = debuff;
        this.debuffValue = debuffValue;
    }

    public ArmorMaterialTier getMaterialTier() {
        return materialTier;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public ArmorStat getBuffOne() {
        return buffOne;
    }

    public double getBuffOneValue() {
        return buffOneValue;
    }

    public ArmorStat getBuffTwo() {
        return buffTwo;
    }

    public double getBuffTwoValue() {
        return buffTwoValue;
    }

    public ArmorStat getDebuff() {
        return debuff;
    }

    public double getDebuffValue() {
        return debuffValue;
    }
}
