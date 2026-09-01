package Cosmetics.Trails;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TrailManager {

    private final CarcerWorldCore plugin;

    private final File file;
    private final FileConfiguration config;

    private final Map<UUID, Set<TrailType>> unlockedTrails = new HashMap<>();
    private final Map<UUID, TrailType> selectedTrails = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public TrailManager(CarcerWorldCore plugin) {
        this.plugin = plugin;

        file = new File(plugin.getDataFolder(), "trails.yml");

        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        load();
        startTrailTask();
    }

    private void load() {
        unlockedTrails.clear();
        selectedTrails.clear();

        if (!config.isConfigurationSection("players")) return;

        for (String uuidString : config.getConfigurationSection("players").getKeys(false)) {

            UUID uuid;

            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                continue;
            }

            Set<TrailType> unlocked = new HashSet<>();

            for (String id : config.getStringList(
                    "players." + uuid + ".unlocked")) {

                TrailType type = TrailType.fromId(id);

                if (type != TrailType.NONE)
                    unlocked.add(type);
            }

            unlockedTrails.put(uuid, unlocked);

            String selectedId =
                    config.getString("players." + uuid + ".selected");

            TrailType selected = TrailType.fromId(selectedId);

            selectedTrails.put(uuid, selected);
        }
    }

    public void save() {

        config.set("players", null);

        for (UUID uuid : unlockedTrails.keySet()) {

            List<String> unlocked = new ArrayList<>();

            for (TrailType type : unlockedTrails.get(uuid))
                unlocked.add(type.getId());

            config.set(
                    "players." + uuid + ".unlocked",
                    unlocked
            );

            TrailType selected =
                    selectedTrails.getOrDefault(uuid, TrailType.NONE);

            config.set(
                    "players." + uuid + ".selected",
                    selected.getId()
            );
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean ownsTrail(Player player, TrailType type) {
        if (type == TrailType.NONE) return true;

        if (player.isOp()) return true;

        return unlockedTrails
                .getOrDefault(player.getUniqueId(), Collections.emptySet())
                .contains(type);
    }

    public void unlockTrail(Player player, TrailType type) {
        if (type == TrailType.NONE) return;

        unlockedTrails
                .computeIfAbsent(player.getUniqueId(), uuid -> new HashSet<>())
                .add(type);

        save();
    }

    public TrailType getSelectedTrail(Player player) {
        return selectedTrails.getOrDefault(
                player.getUniqueId(),
                TrailType.NONE
        );
    }

    public void selectTrail(Player player, TrailType type) {

        if (type == null)
            type = TrailType.NONE;

        if (type != TrailType.NONE && !ownsTrail(player, type))
            return;

        selectedTrails.put(player.getUniqueId(), type);

        save();

        player.sendMessage(
                color("&d&lCOSMETICS &8> &fSelected " +
                        type.getDisplayName() + "&f.")
        );
    }

    public void purchaseTrail(Player player, TrailType type) {
        if (type == TrailType.NONE) return;

        if (player.isOp()) {
            selectTrail(player, type);
            return;
        }

        if (ownsTrail(player, type)) {
            selectTrail(player, type);
            return;
        }

        long cost = type.getGemCost();

        if (!plugin.getGemManager().hasGems(player, cost)) {
            player.sendMessage(
                    color("&d&lCOSMETICS &8> &fYou need &a"
                            + format(cost)
                            + " Gems &fto purchase this.")
            );
            return;
        }

        plugin.getGemManager().removeGems(player,
                cost
        );

        unlockTrail(player, type);
        selectTrail(player, type);

        player.sendMessage(
                color("&d&lCOSMETICS &8> &fPurchased "
                        + type.getDisplayName()
                        + " &ffor &a"
                        + format(cost)
                        + " Gems&f.")
        );
    }

    private void startTrailTask() {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                TrailType type = getSelectedTrail(player);

                if (type == TrailType.NONE) {
                    lastLocations.put(
                            player.getUniqueId(),
                            player.getLocation().clone()
                    );
                    continue;
                }

                Location current = player.getLocation();
                Location previous = lastLocations.get(player.getUniqueId());

                lastLocations.put(
                        player.getUniqueId(),
                        current.clone()
                );

                if (previous == null) continue;
                if (previous.getWorld() != current.getWorld()) continue;

                double moved = previous.distanceSquared(current);

                if (moved < 0.0025) continue;

                Location particleLocation = current.clone().add(0, 0.2, 0);

                int count;
                double spread;

                switch (type) {

                    case CHERRY_BLOSSOM -> {
                        count = 8;
                        spread = 0.30;
                    }

                    case HELLFIRE -> {
                        count = 7;
                        spread = 0.22;
                    }

                    case SOULMIST -> {
                        count = 10;
                        spread = 0.35;
                    }

                    case VOIDWALKER -> {
                        count = 12;
                        spread = 0.30;
                    }

                    case FROSTBOUND -> {
                        count = 9;
                        spread = 0.28;
                    }

                    case DIVINE -> {
                        count = 7;
                        spread = 0.20;
                    }

                    case ARCANE -> {
                        count = 14;
                        spread = 0.35;
                    }

                    default -> {
                        count = 6;
                        spread = 0.25;
                    }
                }

                current.getWorld().spawnParticle(
                        type.getParticle(),
                        particleLocation,
                        count,
                        spread,
                        0.12,
                        spread,
                        0.01
                );
            }

        }, 2L, 2L);
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}