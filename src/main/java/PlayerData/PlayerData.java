package PlayerData;

import Enchantments.EnchantType;
import Skills.SkillType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;

    private int weaponLevel;
    private long weaponExp;
    private long mobKills;
    private int ascensions;
    private int skillPoints;

    private long souls;

    private final Map<SkillType, Integer> skills =
            new EnumMap<>(SkillType.class);

    private final Map<EnchantType, Integer> enchants =
            new EnumMap<>(EnchantType.class);

    public PlayerData(UUID uuid) {
        this.uuid = uuid;

        weaponLevel = 1;
        weaponExp = 0;
        mobKills = 0;
        ascensions = 0;
        skillPoints = 0;
        souls = 0;

        for (SkillType type : SkillType.values()) {
            skills.put(type, 0);
        }

        for (EnchantType type : EnchantType.values()) {
            enchants.put(type, 0);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    // ================================
    // WEAPON
    // ================================

    public int getWeaponLevel() {
        return weaponLevel;
    }

    public void setWeaponLevel(int weaponLevel) {
        this.weaponLevel = Math.max(1, weaponLevel);
    }

    public long getWeaponExp() {
        return weaponExp;
    }

    public void setWeaponExp(long weaponExp) {
        this.weaponExp = Math.max(0, weaponExp);
    }

    // ================================
    // MOB KILLS
    // ================================

    public long getMobKills() {
        return mobKills;
    }

    public void setMobKills(long mobKills) {
        this.mobKills = Math.max(0, mobKills);
    }

    public void addMobKill() {
        mobKills++;
    }

    // ================================
    // ASCENSION
    // ================================

    public int getAscensions() {
        return ascensions;
    }

    public void setAscensions(int ascensions) {
        this.ascensions = Math.max(0, ascensions);
    }

    public void addAscension() {
        ascensions++;
    }

    // ================================
    // SKILL POINTS
    // ================================

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = Math.max(0, skillPoints);
    }

    public void addSkillPoints(int amount) {
        skillPoints = Math.max(0, skillPoints + amount);
    }

    public boolean removeSkillPoints(int amount) {
        if (amount <= 0 || skillPoints < amount) {
            return false;
        }

        skillPoints -= amount;
        return true;
    }

    // ================================
    // SOULS
    // ================================

    public long getSouls() {
        return souls;
    }

    public void setSouls(long souls) {
        this.souls = Math.max(0, souls);
    }

    public void addSouls(long amount) {
        if (amount > 0) {
            souls += amount;
        }
    }

    public boolean removeSouls(long amount) {
        if (amount <= 0 || souls < amount) {
            return false;
        }

        souls -= amount;
        return true;
    }

    // ================================
    // SKILLS
    // ================================

    public int getSkillLevel(SkillType type) {
        return skills.getOrDefault(type, 0);
    }

    public void setSkillLevel(SkillType type, int level) {
        skills.put(
                type,
                Math.max(0, Math.min(level, type.getMaxLevel()))
        );
    }

    public void addSkillLevel(SkillType type, int amount) {
        setSkillLevel(
                type,
                getSkillLevel(type) + amount
        );
    }

    // ================================
    // ENCHANTS
    // ================================

    public int getEnchantLevel(EnchantType type) {
        return enchants.getOrDefault(type, 0);
    }

    public void setEnchantLevel(EnchantType type, int level) {
        enchants.put(
                type,
                Math.max(0, Math.min(level, type.getMaxLevel()))
        );
    }

    public void addEnchantLevel(EnchantType type, int amount) {
        setEnchantLevel(
                type,
                getEnchantLevel(type) + amount
        );
    }
}