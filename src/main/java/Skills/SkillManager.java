package Skills;

import PlayerData.PlayerData;
import Quests.QuestObjectiveType;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;


public class SkillManager {

    private final CarcerWorldCore plugin;

    public SkillManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public int getSkillLevel(Player player, SkillType type) {
        return plugin.getPlayerDataManager()
                .getPlayerData(player)
                .getSkillLevel(type);
    }

    public boolean upgradeSkill(Player player, SkillType type) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        int currentLevel = data.getSkillLevel(type);

        if (currentLevel >= type.getMaxLevel()) {
            player.sendMessage(color(
                    "&c&lSKILLS &7&l| &fThis skill is already max level."
            ));

            return false;
        }

        int cost = type.getPointCost();

        if (data.getSkillPoints() < cost) {
            player.sendMessage(color(
                    "&c&lSKILLS &7&l| &fYou need &c"
                            + cost
                            + " &fskill point"
                            + (cost == 1 ? "" : "s")
                            + "."
            ));

            return false;
        }

        data.removeSkillPoints(cost);
        data.addSkillLevel(type, 1);

        if (plugin.getQuestManager() != null) plugin.getQuestManager().handleProgress(player, QuestObjectiveType.SPEND_SKILL_POINTS, cost);

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());

        if (type == SkillType.HEALTH) {
            updatePlayerHealth(player);
        }

        if (plugin.getWeaponManager() != null) {
            plugin.getWeaponManager()
                    .giveOrUpdateWeapon(player);
        }

        player.sendMessage(color(
                "&a&lSKILLS &7&l| &fUpgraded "
                        + type.getDisplayName()
                        + " &fto level &a"
                        + data.getSkillLevel(type)
                        + "&f."
        ));

        return true;
    }

    // ================================
    // STRENGTH
    // +3% damage per level
    // ================================

    public double applyStrength(Player player, double damage) {
        int level = getSkillLevel(
                player,
                SkillType.STRENGTH
        );

        double multiplier = 1.0 + (level * 0.03);

        return damage * multiplier;
    }

    public double getStrengthPercent(Player player) {
        return getSkillLevel(
                player,
                SkillType.STRENGTH
        ) * 3.0;
    }

    // ================================
    // KNOWLEDGE
    // +5% EXP per level
    // ================================

    public long applyKnowledge(Player player, long exp) {
        int level = getSkillLevel(
                player,
                SkillType.KNOWLEDGE
        );

        double multiplier = 1.0 + (level * 0.05);

        return Math.round(exp * multiplier);
    }

    public double getKnowledgePercent(Player player) {
        return getSkillLevel(
                player,
                SkillType.KNOWLEDGE
        ) * 5.0;
    }

    // ================================
    // HEALTH
    // +1 health point per level
    // ================================

    public void updatePlayerHealth(Player player) {
        int level = getSkillLevel(
                player,
                SkillType.HEALTH
        );

        double baseHealth = 20.0;
        double bonusHealth = level;

        AttributeInstance maxHealth =
                player.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth == null) {
            return;
        }

        double newMaxHealth =
                baseHealth + bonusHealth;

        maxHealth.setBaseValue(newMaxHealth);

        if (player.getHealth() > newMaxHealth) {
            player.setHealth(newMaxHealth);
        }
    }

    public double getHealthBonus(Player player) {
        return getSkillLevel(
                player,
                SkillType.HEALTH
        );
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }
}
