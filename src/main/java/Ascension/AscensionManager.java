package Ascension;

import Quests.QuestObjectiveType;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;
import Enchantments.EnchantType;
import PlayerData.PlayerData;
import Skills.SkillType;

public class AscensionManager {

    private static final int REQUIRED_WEAPON_LEVEL = 100;

    private final CarcerWorldCore plugin;

    public AscensionManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public boolean canAscend(Player player) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        return data.getWeaponLevel() >= REQUIRED_WEAPON_LEVEL;
    }

    public int getAscensionLevel(Player player) {
        return plugin.getPlayerDataManager()
                .getPlayerData(player)
                .getAscensions();
    }

    public void ascend(Player player) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        if (!canAscend(player)) {
            player.sendMessage(color(
                    "&d&lASCENSION &7&l| &fYour weapon must reach level &d100 &fto ascend."
            ));
            return;
        }

        // Increase Ascension first
        data.addAscension();

        if (plugin.getQuestManager() != null) plugin.getQuestManager().handleStateProgress(player, QuestObjectiveType.REACH_ASCENSION, data.getAscensions());

        // Reset weapon progression
        data.setWeaponLevel(1);
        data.setWeaponExp(0);

        // Reset skill points
        data.setSkillPoints(0);

        // Reset all skills
        for (SkillType type : SkillType.values()) {
            data.setSkillLevel(type, 0);
        }

        // Reset all enchants
        for (EnchantType type : EnchantType.values()) {
            data.setEnchantLevel(type, 0);
        }

        // Save everything
        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());

        // Health skill may have changed
        plugin.getSkillManager()
                .updatePlayerHealth(player);

        // Physically upgrade/reset weapon
        plugin.getWeaponManager()
                .giveOrUpdateWeapon(player);

        int ascension = data.getAscensions();

        player.sendMessage(color(
                "&d&lASCENSION &7&l| &fYou have reached Ascension &d"
                        + ascension
                        + "&f."
        ));

        player.sendMessage(color(
                "&d&lASCENSION &7&l| &fYour weapon has evolved into &d"
                        + plugin.getWeaponManager()
                        .getAscensionWeaponName(ascension)
                        + "&f."
        ));
    }

    public double getPostNetheriteMultiplier(Player player) {
        int ascension = getAscensionLevel(player);

        if (ascension <= 5) {
            return 1.0;
        }

        int extraAscensions = ascension - 5;

        return 1.0 + (extraAscensions * 0.50);
    }

    public int getPostNetheriteBonusPercent(Player player) {
        int ascension = getAscensionLevel(player);

        if (ascension <= 5) {
            return 0;
        }

        return (ascension - 5) * 50;
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }
}
