package Enchantments;

import PlayerData.PlayerData;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.concurrent.ThreadLocalRandom;

public class EnchantManager {

    private final CarcerWorldCore plugin;

    public EnchantManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public int getEnchantLevel(
            Player player,
            EnchantType type
    ) {
        return plugin.getPlayerDataManager()
                .getPlayerData(player)
                .getEnchantLevel(type);
    }

    public boolean upgradeEnchant(
            Player player,
            EnchantType type
    ) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        int currentLevel = data.getEnchantLevel(type);

        if (currentLevel >= type.getMaxLevel()) {
            player.sendMessage(color(
                    "&c&lENCHANTS &7&l| &fThis enchant is already max level."
            ));

            return false;
        }

        long cost = type.getUpgradeCost(currentLevel);

        if (!plugin.getSoulManager().hasSouls(player, cost)) {
            player.sendMessage(color(
                    "&c&lENCHANTS &7&l| &fYou need &c"
                            + format(cost)
                            + " Souls &fto upgrade this enchant."
            ));

            return false;
        }

        if (!plugin.getSoulManager().removeSouls(player, cost)) {
            return false;
        }

        data.addEnchantLevel(type, 1);

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());

        plugin.getWeaponManager()
                .giveOrUpdateWeapon(player);

        player.sendMessage(color(
                "&a&lENCHANTS &7&l| &fUpgraded "
                        + type.getDisplayName()
                        + " &fto level &a"
                        + data.getEnchantLevel(type)
                        + "&f."
        ));

        return true;
    }

    // ================================
    // SHARPNESS
    // ================================

    public double getSharpnessBonus(Player player) {
        int level = getEnchantLevel(
                player,
                EnchantType.SHARPNESS
        );

        return level * 2.0;
    }

    // ================================
    // CRITICAL STRIKE
    // ================================

    public double getCriticalStrikeChance(Player player) {
        int level = getEnchantLevel(
                player,
                EnchantType.CRITICAL_STRIKE
        );

        return level * 0.02;
    }

    public boolean rollCriticalStrike(Player player) {
        return roll(getCriticalStrikeChance(player));
    }

    // ================================
    // DOUBLE STRIKE
    // ================================

    public double getDoubleStrikeChance(Player player) {
        int level = getEnchantLevel(
                player,
                EnchantType.DOUBLE_STRIKE
        );

        return level * 0.01;
    }

    public boolean rollDoubleStrike(Player player) {
        return roll(getDoubleStrikeChance(player));
    }

    // ================================
    // CLEAVE
    // ================================

    public double getCleaveChance(Player player) {
        int level = getEnchantLevel(
                player,
                EnchantType.CLEAVE
        );

        return level * 0.02;
    }

    public boolean rollCleave(Player player) {
        return roll(getCleaveChance(player));
    }

    // ================================
    // EXECUTE
    // ================================

    public double getExecuteChance(Player player) {
        int level = getEnchantLevel(
                player,
                EnchantType.EXECUTE
        );

        return level * 0.01;
    }

    public boolean rollExecute(Player player) {
        return roll(getExecuteChance(player));
    }

    // ================================
    // RANDOM
    // ================================

    private boolean roll(double chance) {
        if (chance <= 0) {
            return false;
        }

        return ThreadLocalRandom.current()
                .nextDouble() < chance;
    }

    private String color(String text) {
        return org.bukkit.ChatColor
                .translateAlternateColorCodes('&', text);
    }

    private String format(long number) {
        return String.format("%,d", number);
    }
}
