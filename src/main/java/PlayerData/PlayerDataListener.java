package PlayerData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener {

    private final PlayerDataManager dataManager;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        dataManager.getPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dataManager.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
