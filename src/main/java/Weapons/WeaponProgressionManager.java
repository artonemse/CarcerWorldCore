package Weapons;

import PlayerData.PlayerData;
import Quests.QuestObjectiveType;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;


public class WeaponProgressionManager {

    private static final int MAX_WEAPON_LEVEL = 100;
    private static final int SKILL_POINTS_PER_LEVEL = 3;

    private final CarcerWorldCore plugin;
    private final WeaponManager weaponManager;

    public WeaponProgressionManager(
            CarcerWorldCore plugin,
            WeaponManager weaponManager
    ) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
    }

    public void addWeaponExp(Player player, long amount) {
        if (amount <= 0) {
            return;
        }
        amount = plugin.getSkillManager()
                .applyKnowledge(
                        player,
                        amount
                );

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        int level = data.getWeaponLevel();

        if (level >= MAX_WEAPON_LEVEL) {
            data.setWeaponExp(0);
            return;
        }

        long exp = data.getWeaponExp() + amount;

        boolean leveledUp = false;

        while (level < MAX_WEAPON_LEVEL) {
            long required = weaponManager.getExpRequired(level);

            if (exp < required) {
                break;
            }

            exp -= required;
            level++;

            data.setWeaponLevel(level);
            data.addSkillPoints(SKILL_POINTS_PER_LEVEL);
            if (plugin.getQuestManager() != null) plugin.getQuestManager().handleStateProgress(player, QuestObjectiveType.REACH_WEAPON_LEVEL, level);

            leveledUp = true;

            player.sendMessage(
                    "§6§lWEAPON §8> §fYour weapon reached level §6"
                            + level
                            + "§f!"
            );

            player.sendMessage(
                    "§6§lSKILLS §8> §fYou received §63 Skill Points§f!"
            );
        }

        if (level >= MAX_WEAPON_LEVEL) {
            exp = 0;

            if (leveledUp) {
                player.sendMessage(
                        "§d§lASCENSION §8> §fYour weapon has reached level §d100§f!"
                );
            }
        }

        data.setWeaponExp(exp);

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());

        weaponManager.giveOrUpdateWeapon(player);
    }

    public void addMobKill(Player player) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        data.addMobKill();

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());
    }

    public int getMaxWeaponLevel() {
        return MAX_WEAPON_LEVEL;
    }
}