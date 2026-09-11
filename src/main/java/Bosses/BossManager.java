package Bosses;

import Bosses.Blackthorn.ThornboundWarden;
import Quests.PlayerQuest;
import Quests.QuestState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossManager {

    private final CarcerWorldCore plugin;
    private final Map<UUID, BossInstance> playerEncounters = new HashMap<>();
    private final Map<UUID, BossInstance> bossEntities = new HashMap<>();

    private File file;
    private FileConfiguration config;
    private BukkitTask detectionTask;

    public BossManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        setupFile();
    }

    public void start() {
        detectionTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkBossTriggers, 20L, 20L);
    }

    private void checkBossTriggers() {
        for (BossType type : BossType.values()) {
            Location spawn = getSpawn(type);

            if (spawn == null) continue;
            if (hasActiveBoss(type)) continue;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isOnline() || player.isDead()) continue;
                if (playerEncounters.containsKey(player.getUniqueId())) continue;
                if (!player.getWorld().equals(spawn.getWorld())) continue;
                if (player.getLocation().distanceSquared(spawn) > type.getTriggerRadius() * type.getTriggerRadius()) continue;
                if (!hasActiveQuest(player, type.getQuestId())) continue;

                startBoss(player, type, spawn);
                break;
            }
        }
    }

    private boolean hasActiveQuest(Player player, String questId) {
        if (questId == null || questId.isBlank()) return true;

        PlayerQuest quest = plugin.getQuestManager().getPlayerQuest(player, questId);

        return quest != null && quest.getState() == QuestState.ACTIVE;
    }

    public boolean startBoss(Player player, BossType type, Location center) {
        if (playerEncounters.containsKey(player.getUniqueId())) {
            player.sendMessage(color("&cYou are already in a boss encounter."));
            return false;
        }

        if (hasActiveBoss(type)) {
            player.sendMessage(color("&cThat boss encounter is currently occupied."));
            return false;
        }

        BossInstance instance = switch (type) {
            case THORNBOUND_WARDEN -> new ThornboundWarden(plugin, this, player, center);
        };

        playerEncounters.put(player.getUniqueId(), instance);

        instance.start();

        if (instance.getBossEntity() == null) {
            playerEncounters.remove(player.getUniqueId());
            return false;
        }

        bossEntities.put(instance.getBossEntity().getUniqueId(), instance);

        return true;
    }

    public void handleBossDeath(LivingEntity entity, Player killer) {
        BossInstance instance = bossEntities.get(entity.getUniqueId());

        if (instance == null) return;

        instance.defeated(killer);
    }

    public BossInstance getEncounter(Player player) {
        return playerEncounters.get(player.getUniqueId());
    }

    public BossInstance getEncounterByEntity(LivingEntity entity) {
        return bossEntities.get(entity.getUniqueId());
    }

    public boolean isBoss(LivingEntity entity) {
        return bossEntities.containsKey(entity.getUniqueId());
    }

    public boolean hasActiveBoss(BossType type) {
        for (BossInstance instance : playerEncounters.values()) {
            if (!instance.isFinished() && instance.getType() == type) return true;
        }

        return false;
    }

    public void stopEncounter(Player player, String message) {
        BossInstance instance = playerEncounters.get(player.getUniqueId());

        if (instance != null) instance.fail(message);
    }

    public void unregister(BossInstance instance) {
        playerEncounters.remove(instance.getOwnerId());

        LivingEntity boss = instance.getBossEntity();

        if (boss != null) bossEntities.remove(boss.getUniqueId());
    }

    public void setSpawn(BossType type, Location location) {
        String path = "spawns." + type.getId();

        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        saveFile();
    }

    public Location getSpawn(BossType type) {
        String path = "spawns." + type.getId();
        String worldName = config.getString(path + ".world");

        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);

        if (world == null) return null;

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void setupFile() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        file = new File(plugin.getDataFolder(), "bosses.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void shutdown() {
        if (detectionTask != null) detectionTask.cancel();

        for (BossInstance instance : playerEncounters.values().toArray(new BossInstance[0])) {
            instance.fail(null);
        }

        playerEncounters.clear();
        bossEntities.clear();
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
