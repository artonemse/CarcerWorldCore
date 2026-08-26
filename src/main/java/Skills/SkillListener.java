package Skills;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class SkillListener implements Listener {

    private final CarcerWorldCore plugin;
    private final SkillManager skillManager;

    public SkillListener(
            CarcerWorldCore plugin,
            SkillManager skillManager
    ) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        skillManager.updatePlayerHealth(
                event.getPlayer()
        );
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTask(
                plugin,
                () -> skillManager.updatePlayerHealth(
                        event.getPlayer()
                )
        );
    }
}