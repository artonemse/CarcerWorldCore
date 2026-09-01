package Armor.Generic;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GenericArmorGenerator {

    private final NamespacedKey armorKey;
    private final NamespacedKey materialKey;
    private final NamespacedKey slotKey;

    private final NamespacedKey buffOneKey;
    private final NamespacedKey buffOneValueKey;

    private final NamespacedKey buffTwoKey;
    private final NamespacedKey buffTwoValueKey;

    private final NamespacedKey debuffKey;
    private final NamespacedKey debuffValueKey;

    private final ArmorNameGenerator nameGenerator =
            new ArmorNameGenerator();

    public GenericArmorGenerator(CarcerWorldCore plugin) {

        armorKey =
                new NamespacedKey(plugin, "generic_armor");

        materialKey =
                new NamespacedKey(plugin, "armor_material");

        slotKey =
                new NamespacedKey(plugin, "armor_slot");

        buffOneKey =
                new NamespacedKey(plugin, "armor_buff_1");

        buffOneValueKey =
                new NamespacedKey(plugin, "armor_buff_1_value");

        buffTwoKey =
                new NamespacedKey(plugin, "armor_buff_2");

        buffTwoValueKey =
                new NamespacedKey(plugin, "armor_buff_2_value");

        debuffKey =
                new NamespacedKey(plugin, "armor_debuff");

        debuffValueKey =
                new NamespacedKey(plugin, "armor_debuff_value");
    }

    public ItemStack generateRandomArmor() {

        ArmorMaterialTier material =
                randomMaterial();

        EquipmentSlot slot =
                randomSlot();

        List<ArmorStat> stats =
                new ArrayList<>(
                        Arrays.asList(ArmorStat.values())
                );

        Collections.shuffle(stats);

        ArmorStat buffOne = stats.get(0);
        ArmorStat buffTwo = stats.get(1);
        ArmorStat debuff = stats.get(2);

        double buffOneValue =
                material.rollBuff();

        double buffTwoValue =
                material.rollBuff();

        double debuffValue =
                Math.min(80.0, material.rollDebuff());

        return createArmor(
                material,
                slot,
                buffOne,
                buffOneValue,
                buffTwo,
                buffTwoValue,
                debuff,
                debuffValue
        );
    }

    public ItemStack createArmor(
            ArmorMaterialTier material,
            EquipmentSlot slot,
            ArmorStat buffOne,
            double buffOneValue,
            ArmorStat buffTwo,
            double buffTwoValue,
            ArmorStat debuff,
            double debuffValue
    ) {

        ItemStack item =
                material.createBaseItem(slot);

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return item;
        }

        if (
                material == ArmorMaterialTier.COPPER
                        && meta instanceof LeatherArmorMeta leatherMeta
        ) {
            leatherMeta.setColor(
                    Color.fromRGB(184, 115, 51)
            );
        }

        String name =
                nameGenerator.generate(slot);

        meta.setDisplayName(
                color("&6&l" + name)
        );

        List<String> lore =
                new ArrayList<>();

        lore.add("");

        lore.add(color(
                "&7&l| &f"
                        + buffOne.getDisplayName()
                        + ": &a+"
                        + format(buffOneValue)
                        + "%"
        ));

        lore.add(color(
                "&7&l| &f"
                        + buffTwo.getDisplayName()
                        + ": &a+"
                        + format(buffTwoValue)
                        + "%"
        ));

        lore.add(color(
                "&7&l| &f"
                        + debuff.getDisplayName()
                        + ": &c-"
                        + format(debuffValue)
                        + "%"
        ));

        lore.add("");

        lore.add(color(
                "&8" + material.getDisplayName() + " Armor"
        ));

        meta.setLore(lore);

        meta.setUnbreakable(true);

        meta.setEnchantmentGlintOverride(true);

        meta.addItemFlags(
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ATTRIBUTES
        );

        PersistentDataContainer pdc =
                meta.getPersistentDataContainer();

        pdc.set(
                armorKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        pdc.set(
                materialKey,
                PersistentDataType.STRING,
                material.name()
        );

        pdc.set(
                slotKey,
                PersistentDataType.STRING,
                slot.name()
        );

        pdc.set(
                buffOneKey,
                PersistentDataType.STRING,
                buffOne.name()
        );

        pdc.set(
                buffOneValueKey,
                PersistentDataType.DOUBLE,
                buffOneValue
        );

        pdc.set(
                buffTwoKey,
                PersistentDataType.STRING,
                buffTwo.name()
        );

        pdc.set(
                buffTwoValueKey,
                PersistentDataType.DOUBLE,
                buffTwoValue
        );

        pdc.set(
                debuffKey,
                PersistentDataType.STRING,
                debuff.name()
        );

        pdc.set(
                debuffValueKey,
                PersistentDataType.DOUBLE,
                debuffValue
        );

        item.setItemMeta(meta);

        return item;
    }

    public boolean isGenericArmor(ItemStack item) {

        if (item == null) return false;

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) return false;

        Byte value =
                meta.getPersistentDataContainer().get(
                        armorKey,
                        PersistentDataType.BYTE
                );

        return value != null
                && value == (byte) 1;
    }

    public GenericArmorData getArmorData(ItemStack item) {

        if (!isGenericArmor(item)) {
            return null;
        }

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc =
                meta.getPersistentDataContainer();

        String materialName =
                pdc.get(
                        materialKey,
                        PersistentDataType.STRING
                );

        String slotName =
                pdc.get(
                        slotKey,
                        PersistentDataType.STRING
                );

        String buffOneName =
                pdc.get(
                        buffOneKey,
                        PersistentDataType.STRING
                );

        String buffTwoName =
                pdc.get(
                        buffTwoKey,
                        PersistentDataType.STRING
                );

        String debuffName =
                pdc.get(
                        debuffKey,
                        PersistentDataType.STRING
                );

        Double buffOneValue =
                pdc.get(
                        buffOneValueKey,
                        PersistentDataType.DOUBLE
                );

        Double buffTwoValue =
                pdc.get(
                        buffTwoValueKey,
                        PersistentDataType.DOUBLE
                );

        Double debuffValue =
                pdc.get(
                        debuffValueKey,
                        PersistentDataType.DOUBLE
                );

        if (
                materialName == null
                        || slotName == null
                        || buffOneName == null
                        || buffTwoName == null
                        || debuffName == null
                        || buffOneValue == null
                        || buffTwoValue == null
                        || debuffValue == null
        ) {
            return null;
        }

        try {

            return new GenericArmorData(

                    ArmorMaterialTier.valueOf(
                            materialName
                    ),

                    EquipmentSlot.valueOf(
                            slotName
                    ),

                    ArmorStat.valueOf(
                            buffOneName
                    ),

                    buffOneValue,

                    ArmorStat.valueOf(
                            buffTwoName
                    ),

                    buffTwoValue,

                    ArmorStat.valueOf(
                            debuffName
                    ),

                    debuffValue
            );

        } catch (IllegalArgumentException exception) {

            return null;
        }
    }

    private ArmorMaterialTier randomMaterial() {

        ArmorMaterialTier[] values =
                ArmorMaterialTier.values();

        return values[
                ThreadLocalRandom.current()
                        .nextInt(values.length)
                ];
    }

    private EquipmentSlot randomSlot() {

        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        return slots[
                ThreadLocalRandom.current()
                        .nextInt(slots.length)
                ];
    }

    private String format(double amount) {

        if (amount == Math.floor(amount)) {
            return String.valueOf(
                    (long) amount
            );
        }

        return String.format(
                "%.1f",
                amount
        );
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }
}
