package MobRewards;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class MobSoulRewardListener implements Listener {

    private final CarcerWorldCore plugin;
    private final MobSoulRewardManager rewardManager;

    public MobSoulRewardListener(
            CarcerWorldCore plugin,
            MobSoulRewardManager rewardManager
    ) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {

        LivingEntity mob = event.getEntity();

        if (!(mob instanceof Monster)) return;

        Player killer = mob.getKiller();
        if (killer == null) return;

        long reward =
                rewardManager.calculateReward(killer, mob);

        plugin.getSoulManager()
                .addSouls(killer, reward);
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}