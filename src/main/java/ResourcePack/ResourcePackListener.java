package ResourcePack;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class ResourcePackListener implements Listener {

    private final CarcerWorldCore plugin;
    private final ResourcePackManager manager;

    public ResourcePackListener(CarcerWorldCore plugin, ResourcePackManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!event.getPlayer().isOnline()) return;

            manager.sendPack(event.getPlayer());
        }, 40L);
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED -> plugin.getLogger().info(event.getPlayer().getName() + " loaded the CarcerWorld resource pack.");
            case DECLINED -> plugin.getLogger().warning(event.getPlayer().getName() + " declined the CarcerWorld resource pack.");
            case FAILED_DOWNLOAD -> plugin.getLogger().warning(event.getPlayer().getName() + " failed to download the CarcerWorld resource pack.");
            default -> {
            }
        }
    }
}
