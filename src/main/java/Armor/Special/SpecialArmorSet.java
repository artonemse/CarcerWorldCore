package Armor.Special;

import Armor.Generic.ArmorStat;

import java.util.EnumMap;
import java.util.Map;

public enum SpecialArmorSet {

    BLACKTHORN(
            "blackthorn",
            "&2&lBlackthorn",
            "&2&lBLACKTHORN ARMOR",
            "&a",
            "Thornstorm",
            50.0,
            8.0,
            30,
            stats(ArmorStat.HEALTH, 15.0, ArmorStat.DAMAGE, 10.0, ArmorStat.DAMAGE_REDUCTION, 8.0),
            stats(ArmorStat.DAMAGE, 20.0),
            stats(ArmorStat.DAMAGE, 40.0, ArmorStat.DAMAGE_REDUCTION, 20.0)
    ),

    GRAVEBORN(
            "graveborn",
            "&5&lGraveborn",
            "&5&lGRAVEBORN ARMOR",
            "&d",
            "Soul Reap",
            50.0,
            8.0,
            30,
            stats(ArmorStat.HEALTH, 10.0, ArmorStat.DAMAGE_REDUCTION, 10.0),
            stats(ArmorStat.DAMAGE_REDUCTION, 20.0),
            stats(ArmorStat.DAMAGE_REDUCTION, 35.0, ArmorStat.DAMAGE, 25.0)
    ),

    BLACKTIDE(
            "blacktide",
            "&3&lBlacktide",
            "&3&lBLACKTIDE ARMOR",
            "&b",
            "Abyssal Maelstrom",
            65.0,
            8.0,
            30,
            stats(ArmorStat.WEAPON_XP, 10.0, ArmorStat.DAMAGE, 5.0),
            stats(ArmorStat.WEAPON_XP, 20.0),
            stats(ArmorStat.WEAPON_XP, 35.0, ArmorStat.DAMAGE, 30.0)
    ),

    ROYAL_GUARD(
            "royal_guard",
            "&6&lRoyal Guard",
            "&6&lROYAL GUARD ARMOR",
            "&e",
            "Divine Guard",
            25.0,
            7.0,
            35,
            stats(ArmorStat.HEALTH, 15.0, ArmorStat.DAMAGE_REDUCTION, 8.0),
            stats(ArmorStat.HEALTH, 25.0),
            stats(ArmorStat.HEALTH, 50.0, ArmorStat.DAMAGE_REDUCTION, 30.0)
    ),

    GOBLIN_SLAYER(
            "goblin_slayer",
            "&2&lGoblin Slayer",
            "&2&lGOBLIN SLAYER ARMOR",
            "&a",
            "Blade Frenzy",
            150.0,
            6.0,
            25,
            stats(ArmorStat.DAMAGE, 10.0),
            stats(ArmorStat.DAMAGE, 25.0),
            stats(ArmorStat.DAMAGE, 50.0, ArmorStat.LOOT_FIND, 25.0)
    ),

    TIDECALLER(
            "tidecaller",
            "&3&lTidecaller",
            "&3&lTIDECALLER ARMOR",
            "&b",
            "Riptide",
            50.0,
            3.0,
            20,
            stats(ArmorStat.MOVEMENT_SPEED, 5.0),
            stats(ArmorStat.MOVEMENT_SPEED, 15.0),
            stats(ArmorStat.MOVEMENT_SPEED, 30.0, ArmorStat.DAMAGE, 30.0)
    );

    private final String id;
    private final String displayName;
    private final String armorTitle;
    private final String accentColor;
    private final String abilityName;
    private final double abilityDamage;
    private final double abilityRadius;
    private final int abilityCooldown;
    private final Map<ArmorStat, Double> pieceStats;
    private final Map<ArmorStat, Double> twoPieceStats;
    private final Map<ArmorStat, Double> fourPieceStats;

    SpecialArmorSet(String id, String displayName, String armorTitle, String accentColor, String abilityName, double abilityDamage, double abilityRadius, int abilityCooldown, Map<ArmorStat, Double> pieceStats, Map<ArmorStat, Double> twoPieceStats, Map<ArmorStat, Double> fourPieceStats) {
        this.id = id;
        this.displayName = displayName;
        this.armorTitle = armorTitle;
        this.accentColor = accentColor;
        this.abilityName = abilityName;
        this.abilityDamage = abilityDamage;
        this.abilityRadius = abilityRadius;
        this.abilityCooldown = abilityCooldown;
        this.pieceStats = pieceStats;
        this.twoPieceStats = twoPieceStats;
        this.fourPieceStats = fourPieceStats;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getArmorTitle() {
        return armorTitle;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public double getAbilityDamage() {
        return abilityDamage;
    }

    public double getAbilityRadius() {
        return abilityRadius;
    }

    public int getAbilityCooldown() {
        return abilityCooldown;
    }

    public double getPieceStat(ArmorStat stat) {
        return pieceStats.getOrDefault(stat, 0.0);
    }

    public double getTwoPieceStat(ArmorStat stat) {
        return twoPieceStats.getOrDefault(stat, 0.0);
    }

    public double getFourPieceStat(ArmorStat stat) {
        return fourPieceStats.getOrDefault(stat, 0.0);
    }

    public Map<ArmorStat, Double> getPieceStats() {
        return pieceStats;
    }

    public Map<ArmorStat, Double> getTwoPieceStats() {
        return twoPieceStats;
    }

    public Map<ArmorStat, Double> getFourPieceStats() {
        return fourPieceStats;
    }

    public static SpecialArmorSet fromId(String id) {
        if (id == null) return null;

        for (SpecialArmorSet set : values()) {
            if (set.id.equalsIgnoreCase(id)) return set;
        }

        return null;
    }

    private static Map<ArmorStat, Double> stats(Object... values) {
        Map<ArmorStat, Double> stats = new EnumMap<>(ArmorStat.class);

        for (int i = 0; i < values.length; i += 2) stats.put((ArmorStat) values[i], (Double) values[i + 1]);

        return stats;
    }
}