package Locations;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NamedLocationManager {

    private final CarcerWorldCore plugin;
    private final List<NamedLocation> locations = new ArrayList<>();

    private File file;
    private FileConfiguration config;

    public NamedLocationManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        setup();
        loadLocations();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        file = new File(plugin.getDataFolder(), "locations.yml");

        if (!file.exists()) plugin.saveResource("locations.yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadLocations() {
        config = YamlConfiguration.loadConfiguration(file);
        locations.clear();

        ConfigurationSection section = config.getConfigurationSection("locations");

        if (section == null) {
            plugin.getLogger().info("[CarcerWorldCore] Loaded 0 named locations.");
            return;
        }

        for (String id : section.getKeys(false)) {
            String path = "locations." + id;

            String name = config.getString(path + ".name", id);
            String world = config.getString(path + ".world", "Seratari");
            String type = config.getString(path + ".type", "RADIUS");
            String subtitle = config.getString(path + ".subtitle", "&7Unknown Location");
            boolean safeZone = config.getBoolean(path + ".safe-zone", false);

            if (type.equalsIgnoreCase("BOX")) {
                double minX = config.getDouble(path + ".min-x");
                double maxX = config.getDouble(path + ".max-x");
                double minY = config.getDouble(path + ".min-y");
                double maxY = config.getDouble(path + ".max-y");
                double minZ = config.getDouble(path + ".min-z");
                double maxZ = config.getDouble(path + ".max-z");

                locations.add(new NamedLocation(id, name, world, minX, maxX, minY, maxY, minZ, maxZ, subtitle, safeZone));
                continue;
            }

            double x = config.getDouble(path + ".x");
            double z = config.getDouble(path + ".z");
            double radius = config.getDouble(path + ".radius", 100);

            locations.add(new NamedLocation(id, name, world, x, z, radius, subtitle, safeZone));
        }

        plugin.getLogger().info("[CarcerWorldCore] Loaded " + locations.size() + " named locations.");
    }

    public NamedLocation getLocation(Player player) {
        return getLocation(player.getLocation());
    }

    public NamedLocation getLocation(Location location) {
        for (NamedLocation namedLocation : locations) {
            if (namedLocation.contains(location)) return namedLocation;
        }

        return null;
    }

    public boolean isSafeZone(Location location) {
        NamedLocation namedLocation = getLocation(location);
        return namedLocation != null && namedLocation.isSafeZone();
    }

    public List<NamedLocation> getLocations() {
        return locations;
    }
}