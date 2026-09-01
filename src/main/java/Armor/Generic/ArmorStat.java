package Armor.Generic;

public enum ArmorStat {

    HEALTH("Health"),
    DAMAGE("Damage"),
    SOUL_REWARD("Soul Reward"),
    WEAPON_XP("Weapon XP"),
    LOOT_FIND("Loot Find"),
    DAMAGE_REDUCTION("Damage Reduction"),
    MOVEMENT_SPEED("Movement Speed"),
    HEALING("Healing");

    private final String displayName;

    ArmorStat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
