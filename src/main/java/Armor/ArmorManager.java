package Armor;

import Armor.Generic.ArmorStat;
import Armor.Generic.GenericArmorData;
import Armor.Generic.GenericArmorGenerator;
import PlayerData.PlayerData;
import Skills.SkillType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class ArmorManager {

    private final CarcerWorldCore plugin;
    private final GenericArmorGenerator generator;

    public ArmorManager(
            CarcerWorldCore plugin,
            GenericArmorGenerator generator
    ) {
        this.plugin = plugin;
        this.generator = generator;
    }

    /*
     * Returns the final NET percentage.
     *
     * Example:
     *
     * +147 Health
     * +80 Health
     * -41 Health
     *
     * = +186%
     *
     * Duplicate debuffs:
     *
     * -41 Health
     * -68 Health
     *
     * Only -68 applies.
     */
    public double getModifierPercent(
            Player player,
            ArmorStat stat
    ) {

        double totalBuff = 0.0;
        double strongestDebuff = 0.0;

        for (ItemStack item :
                player.getInventory().getArmorContents()) {

            GenericArmorData data =
                    generator.getArmorData(item);

            if (data == null) continue;

            if (data.getBuffOne() == stat) {
                totalBuff +=
                        data.getBuffOneValue();
            }

            if (data.getBuffTwo() == stat) {
                totalBuff +=
                        data.getBuffTwoValue();
            }

            if (data.getDebuff() == stat) {

                strongestDebuff =
                        Math.max(
                                strongestDebuff,
                                data.getDebuffValue()
                        );
            }
        }

        return totalBuff
                - strongestDebuff;
    }

    /*
     * +72%  = 1.72x
     * +172% = 2.72x
     * -80%  = 0.20x
     */
    public double getMultiplier(
            Player player,
            ArmorStat stat
    ) {

        double percent =
                getModifierPercent(
                        player,
                        stat
                );

        double multiplier =
                1.0 + (percent / 100.0);

        return Math.max(
                0.20,
                multiplier
        );
    }

    public long applySoulModifier(
            Player player,
            long souls
    ) {

        double multiplier =
                getMultiplier(
                        player,
                        ArmorStat.SOUL_REWARD
                );

        return Math.max(
                0,
                Math.round(
                        souls * multiplier
                )
        );
    }

    public long applyWeaponExpModifier(
            Player player,
            long exp
    ) {

        double multiplier =
                getMultiplier(
                        player,
                        ArmorStat.WEAPON_XP
                );

        return Math.max(
                0,
                Math.round(
                        exp * multiplier
                )
        );
    }

    public double getLootMultiplier(
            Player player
    ) {
        return getMultiplier(
                player,
                ArmorStat.LOOT_FIND
        );
    }

    /*
     * Damage Reduction is special.
     *
     * +100% Damage Reduction does NOT mean immunity.
     *
     * Instead:
     *
     * +100% -> incoming damage / 2
     * +200% -> incoming damage / 3
     *
     * A debuff works the opposite direction:
     *
     * -50% -> 1.50x incoming damage
     * -80% -> 1.80x incoming damage
     */
    public double getIncomingDamageMultiplier(
            Player player
    ) {

        double percent =
                getModifierPercent(
                        player,
                        ArmorStat.DAMAGE_REDUCTION
                );

        if (percent >= 0) {

            return 1.0 /
                    (1.0 + (percent / 100.0));
        }

        return 1.0 +
                (Math.abs(percent) / 100.0);
    }

    public double calculateMaxHealth(
            Player player
    ) {

        PlayerData data =
                plugin.getPlayerDataManager()
                        .getPlayerData(player);

        int healthLevel =
                data.getSkillLevel(
                        SkillType.HEALTH
                );

        /*
         * Current Health skill:
         * +1 HP per level.
         */
        double baseHealth =
                20.0 + healthLevel;

        double armorMultiplier =
                getMultiplier(
                        player,
                        ArmorStat.HEALTH
                );

        double calculated =
                baseHealth * armorMultiplier;

        /*
         * No 41.3 max HP.
         *
         * 41.3 -> 41
         * 41.5 -> 42
         */
        return Math.max(
                1.0,
                Math.round(calculated)
        );
    }

    public void refreshPlayer(
            Player player
    ) {

        refreshHealth(player);
        refreshMovementSpeed(player);
        refreshWeapon(player);
    }

    public void refreshHealth(
            Player player
    ) {

        if (player.isDead()) return;

        double oldHealth =
                player.getHealth();

        double newMaxHealth =
                calculateMaxHealth(player);

        AttributeInstance maxHealth =
                player.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (maxHealth == null) {
            return;
        }

        maxHealth.setBaseValue(
                newMaxHealth
        );

        /*
         * ALWAYS show exactly 10 hearts.
         *
         * Underlying HP may be much higher.
         */
        player.setHealthScaled(true);
        player.setHealthScale(20.0);

        /*
         * Equipping armor NEVER heals.
         *
         * Removing armor only clamps health
         * if current HP is now above max HP.
         */
        double allowedHealth =
                Math.min(
                        oldHealth,
                        newMaxHealth
                );

        allowedHealth =
                Math.max(
                        0.0,
                        allowedHealth
                );

        if (!player.isDead()) {
            player.setHealth(
                    allowedHealth
            );
        }
    }

    public void refreshMovementSpeed(
            Player player
    ) {

        double multiplier =
                getMultiplier(
                        player,
                        ArmorStat.MOVEMENT_SPEED
                );

        double speed =
                0.20 * multiplier;

        /*
         * Bukkit walk speed must remain
         * between -1 and 1.
         */
        speed =
                Math.max(
                        0.01,
                        Math.min(
                                1.0,
                                speed
                        )
                );

        player.setWalkSpeed(
                (float) speed
        );
    }

    public void refreshWeapon(
            Player player
    ) {

        if (plugin.getWeaponManager() == null) {
            return;
        }

        plugin.getWeaponManager()
                .giveOrUpdateWeapon(player);
    }
}
