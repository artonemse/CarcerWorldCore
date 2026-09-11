package MobRewards;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class MobSoulRewardListener implements Listener {

    private final CarcerWorldCore plugin;
    private final MobSoulRewardManager rewardManager;
    private final NamespacedKey noAutoRewardKey;

    public MobSoulRewardListener(CarcerWorldCore plugin, MobSoulRewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.noAutoRewardKey = new NamespacedKey(plugin, "boss_no_auto_rewards");
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();

        if (!(mob instanceof Monster)) return;
        if (mob.getPersistentDataContainer().has(noAutoRewardKey, PersistentDataType.BYTE)) return;

        Player killer = mob.getKiller();

        if (killer == null) return;

        long reward = rewardManager.calculateReward(killer, mob);

        plugin.getSoulManager().addEarnedSouls(killer, reward);
    }
}