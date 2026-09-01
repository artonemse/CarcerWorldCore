package Armor.Generic;

import org.bukkit.inventory.EquipmentSlot;

import java.util.concurrent.ThreadLocalRandom;

public class ArmorNameGenerator {

    private static final String[] PREFIXES = {
            "Forsaken",
            "Ashen",
            "Forgotten",
            "Dreadwoven",
            "Ancient",
            "Lost",
            "Wandering",
            "Oathbound",
            "Fallen",
            "Cursed",
            "Eternal",
            "Hollow"
    };

    private static final String[] TITLES = {
            "The Lost Soul",
            "Destiny",
            "Reckoning",
            "The Nomad",
            "The Last King",
            "The Wanderer",
            "The Forsaken",
            "Eternity",
            "Dusk",
            "The Fallen",
            "The Exiled",
            "Fate"
    };

    public String generate(EquipmentSlot slot) {

        String piece = getPieceName(slot);

        int style = ThreadLocalRandom.current().nextInt(3);

        return switch (style) {

            case 0 ->
                    piece + " of " + random(TITLES);

            case 1 ->
                    random(PREFIXES) + " " + piece;

            default ->
                    random(TITLES) + "'s " + piece;
        };
    }

    private String getPieceName(EquipmentSlot slot) {

        String[] names = switch (slot) {

            case HEAD -> new String[]{
                    "Helmet",
                    "Helm",
                    "Hood",
                    "Crown"
            };

            case CHEST -> new String[]{
                    "Chestplate",
                    "Tunic",
                    "Cuirass",
                    "Mantle"
            };

            case LEGS -> new String[]{
                    "Leggings",
                    "Greaves",
                    "Pants",
                    "Leg Guards"
            };

            case FEET -> new String[]{
                    "Boots",
                    "Sandals",
                    "Walkers"
            };

            default -> new String[]{
                    "Armor"
            };
        };

        return random(names);
    }

    private String random(String[] values) {
        return values[
                ThreadLocalRandom.current()
                        .nextInt(values.length)
                ];
    }
}
