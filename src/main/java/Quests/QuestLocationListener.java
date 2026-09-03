package Quests;

import Locations.NamedLocation;
import Locations.NamedLocationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestLocationListener implements Listener {

    private final QuestManager questManager;
    private final NamedLocationManager locationManager;
    private final Map<UUID, String> currentLocations = new HashMap<>();

    public QuestLocationListener(QuestManager questManager, NamedLocationManager locationManager) {
        this.questManager = questManager;
        this.locationManager = locationManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        NamedLocation location = locationManager.getLocation(player);
        String previous = currentLocations.get(player.getUniqueId());

        if (location == null) {
            currentLocations.remove(player.getUniqueId());
            return;
        }

        if (location.getId().equalsIgnoreCase(previous)) return;

        currentLocations.put(player.getUniqueId(), location.getId());
        questManager.handleLocationVisit(player, location.getId());
    }
}
