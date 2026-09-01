package Warps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WarpManager {

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "warps.yml");

        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void setWarp(String name, Location location) {
        String path = "warps." + name.toLowerCase();

        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        save();
    }

    public void deleteWarp(String name) {
        config.set("warps." + name.toLowerCase(), null);
        save();
    }

    public boolean warpExists(String name) {
        return config.contains("warps." + name.toLowerCase());
    }

    public Location getWarp(String name) {
        String path = "warps." + name.toLowerCase();

        if (!config.contains(path)) return null;

        World world = Bukkit.getWorld(config.getString(path + ".world"));
        if (world == null) return null;

        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }

    public List<String> getWarps() {
        if (!config.isConfigurationSection("warps")) return new ArrayList<>();
        return new ArrayList<>(config.getConfigurationSection("warps").getKeys(false));
    }

    public void teleport(Player player, String name) {
        Location location = getWarp(name);

        if (location == null) {
            player.sendMessage(color("&c&lWARPS &7&l| &fThat warp does not exist."));
            return;
        }

        player.teleport(location);
        player.sendMessage(color("&a&lWARPS &7&l| &fWarped to &e" + name + "&f."));
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String color(String text) {
        return text.replace("&0", "§0").replace("&1", "§1")
                .replace("&2", "§2").replace("&3", "§3")
                .replace("&4", "§4").replace("&5", "§5")
                .replace("&6", "§6").replace("&7", "§7")
                .replace("&8", "§8").replace("&9", "§9")
                .replace("&a", "§a").replace("&b", "§b")
                .replace("&c", "§c").replace("&d", "§d")
                .replace("&e", "§e").replace("&f", "§f")
                .replace("&l", "§l").replace("&m", "§m")
                .replace("&n", "§n").replace("&o", "§o")
                .replace("&r", "§r");
    }
}
