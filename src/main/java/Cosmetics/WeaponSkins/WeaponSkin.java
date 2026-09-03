package Cosmetics.WeaponSkins;

import java.util.Arrays;

public enum WeaponSkin {

    BROADSWORD_OF_ENTOMBED("broadsword_of_entombed", "&8&lBlackthorn Reckoner", 2801),
    GEM_SWORD("gem_sword", "&b&lGravewarden", 2802),
    SILVER_CLAYMORE("silver_claymore", "&f&lSteelstrike", 2803),
    FLAMBERG("flamberg", "&6&lEmbermaw Edge", 2804),
    VAMPIRIC_BLADE("vampiric_blade", "&4&lColdheart", 2805),
    UNDERGROUND_DWELLERS_SWORD("underground_dwellers_sword", "&7&lBlade of Arcane", 2806),
    TWISTED_SWORD("twisted_sword", "&5&lAshbringer", 2807),
    GEODE_CLEAVER("geode_cleaver", "&d&lCalamity's Harbinger", 2808),
    BROADSWORD_OF_THE_FOREST("broadsword_of_the_forest", "&2&lEarthen Mightwielder", 2809),
    DEMONIC_SWORD("demonic_sword", "&c&lThe Fading Light", 2810),
    ANGEL_SWORD("angel_sword", "&f&lAngel's Blessing", 2820),
    FAIRY_GREATSWORD("fairy_greatsword", "&d&lFairy Greatsword", 2821),
    ORC_SWORD("orc_sword", "&2&lBonecleaver", 2822),
    RUBY_SABER("ruby_saber", "&c&lFireheart's Blaze", 2823),
    SOULREAVER_SWORD("soulreaver_sword", "&5&lSoulreaver Sword", 2824),
    SOLSTICE("solstice", "&6&lSunborn Nightlayer", 2825),
    SWORD_OF_THE_RAVEN("sword_of_the_raven", "&8&lRaven Stormbringer", 2826),
    TORRENTIAL_BLADE("torrential_blade", "&b&lTidecaller", 2827),
    TWILIGHTS_EDGE("twilights_edge", "&5&lTwilight's Edge", 2828),
    VERDANT_EDGE("verdant_edge", "&a&lEarthquake Blade", 2829);

    private final String id;
    private final String displayName;
    private final int customModelData;

    WeaponSkin(String id, String displayName, int customModelData) {
        this.id = id;
        this.displayName = displayName;
        this.customModelData = customModelData;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public static WeaponSkin fromId(String id) {
        if (id == null) return null;

        return Arrays.stream(values()).filter(skin -> skin.id.equalsIgnoreCase(id) || skin.name().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
}
