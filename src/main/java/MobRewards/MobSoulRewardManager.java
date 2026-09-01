package MobRewards;

import Armor.Generic.ArmorStat;
import MobZones.MobType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.concurrent.ThreadLocalRandom;

public class MobSoulRewardManager {

    private final CarcerWorldCore plugin;
    private final NamespacedKey baseHealthKey;

    public MobSoulRewardManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.baseHealthKey = new NamespacedKey(plugin, "mob_base_health");
    }

    public void registerMob(LivingEntity mob, MobType mobType) {
        mob.getPersistentDataContainer().set(baseHealthKey, PersistentDataType.DOUBLE, mobType.getHealth());
    }

    public double getBaseHealth(LivingEntity mob) {
        Double health = mob.getPersistentDataContainer().get(baseHealthKey, PersistentDataType.DOUBLE);
        if (health != null) return health;

        return mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null ? mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue() : 20.0;
    }

    public long calculateBaseReward(LivingEntity mob) {
        double health = getBaseHealth(mob);

        int minimum = Math.max(2, (int) Math.floor(health / 5.0) - 2);
        int maximum = minimum + 3;

        return ThreadLocalRandom.current().nextLong(minimum, maximum + 1L);
    }

    public long calculateReward(Player player, LivingEntity mob) {
        double baseHealth = getBaseHealth(mob);
        long baseReward = calculateBaseReward(mob);

        int level = plugin.getMobScalingManager().getScalingLevel(mob);
        double levelMultiplier = 1.0 + ((level - 1) * 0.02);

        long afterLevel = Math.round(baseReward * levelMultiplier);

        double armorPercent = plugin.getArmorManager() == null ? 0.0 : plugin.getArmorManager().getModifierPercent(player, ArmorStat.SOUL_REWARD);
        long afterArmor = plugin.getArmorManager() == null ? afterLevel : plugin.getArmorManager().applySoulModifier(player, afterLevel);

        return Math.max(1, afterArmor);
    }
}