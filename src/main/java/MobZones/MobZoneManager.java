package MobZones;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MobZoneManager {

    private final JavaPlugin plugin;

    private File file;
    private FileConfiguration config;

    private final Map<String, MobType> mobTypes = new HashMap<>();
    private final List<MobZone> zones = new ArrayList<>();
    private final List<MobZoneEntry> defaultMobs = new ArrayList<>();

    public MobZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;

        createFile();
        load();
    }

    private void createFile() {
        file = new File(plugin.getDataFolder(), "mob-zones.yml");

        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
            return;
        }

        config = new YamlConfiguration();

        config.set("mobs.wandering_dead.entity", "ZOMBIE");
        config.set("mobs.wandering_dead.name", "&2Wandering Dead");
        config.set("mobs.wandering_dead.health", 20);

        config.set("mobs.forsaken_archer.entity", "SKELETON");
        config.set("mobs.forsaken_archer.name", "&7Forsaken Archer");
        config.set("mobs.forsaken_archer.health", 20);

        config.set("mobs.night_crawler.entity", "SPIDER");
        config.set("mobs.night_crawler.name", "&8Night Crawler");
        config.set("mobs.night_crawler.health", 16);

        config.set("mobs.goblin.entity", "ZOMBIE");
        config.set("mobs.goblin.name", "&2Goblin");
        config.set("mobs.goblin.health", 20);

        config.set("mobs.goblin_brute.entity", "HUSK");
        config.set("mobs.goblin_brute.name", "&6Goblin Brute");
        config.set("mobs.goblin_brute.health", 40);

        config.set("mobs.goblin_archer.entity", "SKELETON");
        config.set("mobs.goblin_archer.name", "&aGoblin Archer");
        config.set("mobs.goblin_archer.health", 18);

        config.set("default-mobs.wandering_dead", 45);
        config.set("default-mobs.forsaken_archer", 35);
        config.set("default-mobs.night_crawler", 20);

        config.set("zones.goblin_depths.world", "Seratari");
        config.set("zones.goblin_depths.x", 657);
        config.set("zones.goblin_depths.z", -551);
        config.set("zones.goblin_depths.radius", 100);

        config.set("zones.goblin_depths.mobs.goblin", 60);
        config.set("zones.goblin_depths.mobs.goblin_brute", 25);
        config.set("zones.goblin_depths.mobs.goblin_archer", 15);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create mob-zones.yml");
            e.printStackTrace();
        }
    }

    public void load() {
        config = YamlConfiguration.loadConfiguration(file);

        mobTypes.clear();
        zones.clear();
        defaultMobs.clear();

        loadMobTypes();
        loadDefaultMobs();
        loadZones();

        plugin.getLogger().info(
                "Loaded " + mobTypes.size() + " mob types, "
                        + zones.size() + " mob zones and "
                        + defaultMobs.size() + " default mobs."
        );
    }

    private void loadMobTypes() {
        ConfigurationSection section = config.getConfigurationSection("mobs");

        if (section == null) {
            plugin.getLogger().warning("No mobs section found in mob-zones.yml");
            return;
        }

        for (String id : section.getKeys(false)) {
            String path = "mobs." + id;

            String entityName = config.getString(path + ".entity");
            String name = config.getString(path + ".name", id);
            double health = config.getDouble(path + ".health", 20.0);

            if (entityName == null) {
                plugin.getLogger().warning("Mob " + id + " has no entity type.");
                continue;
            }

            EntityType entityType;

            try {
                entityType = EntityType.valueOf(entityName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(
                        "Invalid entity type '" + entityName + "' for mob " + id
                );
                continue;
            }

            if (!entityType.isAlive()) {
                plugin.getLogger().warning(
                        "Entity type " + entityName + " is not a LivingEntity."
                );
                continue;
            }

            if (health <= 0) {
                plugin.getLogger().warning(
                        "Mob " + id + " has invalid health. Using 20."
                );
                health = 20;
            }

            mobTypes.put(
                    id,
                    new MobType(id, entityType, name, health)
            );
        }
    }

    private void loadDefaultMobs() {
        ConfigurationSection section =
                config.getConfigurationSection("default-mobs");

        if (section == null) {
            plugin.getLogger().warning(
                    "No default-mobs section found in mob-zones.yml"
            );
            return;
        }

        for (String mobId : section.getKeys(false)) {
            MobType mobType = mobTypes.get(mobId);

            if (mobType == null) {
                plugin.getLogger().warning(
                        "Unknown default mob: " + mobId
                );
                continue;
            }

            int weight = section.getInt(mobId);

            if (weight <= 0) continue;

            defaultMobs.add(
                    new MobZoneEntry(mobType, weight)
            );
        }
    }

    private void loadZones() {
        ConfigurationSection section =
                config.getConfigurationSection("zones");

        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String path = "zones." + id;

            String world = config.getString(path + ".world");
            double x = config.getDouble(path + ".x");
            double z = config.getDouble(path + ".z");
            double radius = config.getDouble(path + ".radius");

            if (world == null || radius <= 0) {
                plugin.getLogger().warning(
                        "Invalid mob zone: " + id
                );
                continue;
            }

            ConfigurationSection mobSection =
                    config.getConfigurationSection(path + ".mobs");

            if (mobSection == null) {
                plugin.getLogger().warning(
                        "Mob zone " + id + " has no mobs."
                );
                continue;
            }

            List<MobZoneEntry> entries = new ArrayList<>();

            for (String mobId : mobSection.getKeys(false)) {
                MobType mobType = mobTypes.get(mobId);

                if (mobType == null) {
                    plugin.getLogger().warning(
                            "Unknown mob '" + mobId
                                    + "' in zone " + id
                    );
                    continue;
                }

                int weight = mobSection.getInt(mobId);

                if (weight <= 0) continue;

                entries.add(
                        new MobZoneEntry(mobType, weight)
                );
            }

            if (entries.isEmpty()) continue;

            zones.add(
                    new MobZone(
                            id,
                            world,
                            x,
                            z,
                            radius,
                            entries
                    )
            );
        }
    }

    public MobZone getZone(Location location) {
        MobZone bestZone = null;

        for (MobZone zone : zones) {
            if (!zone.contains(location)) continue;

            if (bestZone == null
                    || zone.getRadius() < bestZone.getRadius())
                bestZone = zone;
        }

        return bestZone;
    }

    public MobType getRandomMob(Location location) {
        MobZone zone = getZone(location);

        if (zone != null)
            return zone.getRandomMob();

        return getRandomDefaultMob();
    }

    private MobType getRandomDefaultMob() {
        if (defaultMobs.isEmpty()) return null;

        int totalWeight = 0;

        for (MobZoneEntry entry : defaultMobs)
            totalWeight += entry.getWeight();

        if (totalWeight <= 0) return null;

        int random = ThreadLocalRandom.current()
                .nextInt(totalWeight);

        int current = 0;

        for (MobZoneEntry entry : defaultMobs) {
            current += entry.getWeight();

            if (random < current)
                return entry.getMobType();
        }

        return defaultMobs.get(
                defaultMobs.size() - 1
        ).getMobType();
    }

    public Map<String, MobType> getMobTypes() {
        return mobTypes;
    }

    public List<MobZone> getZones() {
        return zones;
    }

    public void reload() {
        load();
    }
}