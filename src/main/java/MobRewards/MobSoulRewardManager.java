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
    private final NamespacedKey mobIdKey;

    public MobSoulRewardManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.baseHealthKey = new NamespacedKey(plugin, "mob_base_health");
        this.mobIdKey = new NamespacedKey(plugin, "mob_type_id");
    }

    public void registerMob(LivingEntity mob, MobType mobType) {
        mob.getPersistentDataContainer().set(baseHealthKey, PersistentDataType.DOUBLE, mobType.getHealth());
        mob.getPersistentDataContainer().set(mobIdKey, PersistentDataType.STRING, mobType.getId().toLowerCase());
    }

    public double getBaseHealth(LivingEntity mob) {
        Double health = mob.getPersistentDataContainer().get(baseHealthKey, PersistentDataType.DOUBLE);
        if (health != null) return health;

        return mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null ? mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue() : 20.0;
    }

    public String getMobId(LivingEntity mob) {
        return mob.getPersistentDataContainer().get(mobIdKey, PersistentDataType.STRING);
    }

    public long calculateBaseReward(LivingEntity mob) {
        double health = getBaseHealth(mob);

        int minimum = Math.max(2, (int) Math.floor(health / 5.0) - 2);
        int maximum = minimum + 3;

        return ThreadLocalRandom.current().nextLong(minimum, maximum + 1L);
    }

    public long calculateReward(Player player, LivingEntity mob) {
        long baseReward = calculateBaseReward(mob);

        int level = plugin.getMobScalingManager().getScalingLevel(mob);
        double levelMultiplier = 1.0 + ((level - 1) * 0.02);

        long reward = Math.round(baseReward * levelMultiplier);

        if (plugin.getArmorManager() != null) reward = plugin.getArmorManager().applySoulModifier(player, reward);

        return Math.max(1, reward);
    }
}