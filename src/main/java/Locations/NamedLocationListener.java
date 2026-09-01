package Locations;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NamedLocationListener implements Listener {

    private final NamedLocationManager locationManager;
    private final Map<UUID, String> currentLocations = new HashMap<>();

    public NamedLocationListener(NamedLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        NamedLocation location = locationManager.getLocation(player);

        String previousLocation = currentLocations.get(player.getUniqueId());

        if (location == null) {
            if (previousLocation != null) currentLocations.remove(player.getUniqueId());
            return;
        }

        if (location.getId().equalsIgnoreCase(previousLocation)) return;

        currentLocations.put(player.getUniqueId(), location.getId());

        player.sendTitle(
                color("&6&l" + location.getName()),
                color(location.getSubtitle()),
                10,
                50,
                15
        );
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
