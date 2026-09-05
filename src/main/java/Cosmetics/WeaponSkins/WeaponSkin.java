package Cosmetics.WeaponSkins;

import java.util.Arrays;

public enum WeaponSkin {

    BROADSWORD_OF_ENTOMBED("broadsword_of_entombed", "&8&lBlackthorn Reckoner", 2801, 500),
    GEM_SWORD("gem_sword", "&b&lGravewarden", 2802, 750),
    SILVER_CLAYMORE("silver_claymore", "&f&lSteelstrike", 2803, 400),
    FLAMBERG("flamberg", "&6&lEmbermaw Edge", 2804, 900),
    VAMPIRIC_BLADE("vampiric_blade", "&4&lColdheart", 2805, 1100),
    UNDERGROUND_DWELLERS_SWORD("underground_dwellers_sword", "&7&lBlade of Arcane", 2806, 800),
    TWISTED_SWORD("twisted_sword", "&5&lAshbringer", 2807, 1200),
    GEODE_CLEAVER("geode_cleaver", "&d&lCalamity's Harbinger", 2808, 1750),
    BROADSWORD_OF_THE_FOREST("broadsword_of_the_forest", "&2&lEarthen Mightwielder", 2809, 950),
    DEMONIC_SWORD("demonic_sword", "&c&lThe Fading Light", 2810, 2000),

    ANGEL_SWORD("angel_sword", "&f&lAngel's Blessing", 2820, 2250),
    FAIRY_GREATSWORD("fairy_greatsword", "&d&lFairy Greatsword", 2821, 1000),
    ORC_SWORD("orc_sword", "&2&lBonecleaver", 2822, 650),
    RUBY_SABER("ruby_saber", "&c&lFireheart's Blaze", 2823, 1350),
    SOULREAVER_SWORD("soulreaver_sword", "&5&lSoulreaver Sword", 2824, 1800),
    SOLSTICE("solstice", "&6&lSunborn Nightlayer", 2825, 2500),
    SWORD_OF_THE_RAVEN("sword_of_the_raven", "&8&lRaven Stormbringer", 2826, 1500),
    TORRENTIAL_BLADE("torrential_blade", "&b&lTidecaller", 2827, 1400),
    TWILIGHTS_EDGE("twilights_edge", "&5&lTwilight's Edge", 2828, 1900),
    VERDANT_EDGE("verdant_edge", "&a&lEarthquake Blade", 2829, 1250);

    private final String id;
    private final String displayName;
    private final int customModelData;
    private final long scrapCost;

    WeaponSkin(String id, String displayName, int customModelData, long scrapCost) {
        this.id = id;
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.scrapCost = scrapCost;
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

    public long getScrapCost() {
        return scrapCost;
    }

    public static WeaponSkin fromId(String id) {
        if (id == null) return null;

        return Arrays.stream(values()).filter(skin -> skin.id.equalsIgnoreCase(id) || skin.name().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
}