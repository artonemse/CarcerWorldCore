package PlayerData;

import Enchantments.EnchantType;
import Skills.SkillType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final CarcerWorldCore plugin;

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public PlayerDataManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(
                plugin.getDataFolder(),
                "playerdata.yml"
        );

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe(
                        "Could not create playerdata.yml!"
                );

                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(
                uuid,
                this::loadPlayerData
        );
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    private PlayerData loadPlayerData(UUID uuid) {
        String path = "players." + uuid;

        PlayerData data = new PlayerData(uuid);

        data.setWeaponLevel(
                config.getInt(
                        path + ".weapon-level",
                        1
                )
        );

        data.setWeaponExp(
                config.getLong(
                        path + ".weapon-exp",
                        0
                )
        );

        data.setMobKills(
                config.getLong(
                        path + ".mob-kills",
                        0
                )
        );

        data.setAscensions(
                config.getInt(
                        path + ".ascensions",
                        0
                )
        );

        data.setSkillPoints(
                config.getInt(
                        path + ".skill-points",
                        0
                )
        );

        data.setSouls(
                config.getLong(
                        path + ".souls",
                        0
                )
        );

        for (EnchantType type : EnchantType.values()) {
            data.setEnchantLevel(
                    type,
                    config.getInt(
                            path + ".enchants." + type.getId(),
                            0
                    )
            );
        }

        for (SkillType type : SkillType.values()) {
            int level = config.getInt(
                    path + ".skills." + type.getId(),
                    0
            );

            data.setSkillLevel(type, level);
        }



        return data;
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);

        if (data == null) {
            return;
        }

        String path = "players." + uuid;

        config.set(
                path + ".weapon-level",
                data.getWeaponLevel()
        );

        config.set(
                path + ".weapon-exp",
                data.getWeaponExp()
        );

        config.set(
                path + ".mob-kills",
                data.getMobKills()
        );

        config.set(
                path + ".ascensions",
                data.getAscensions()
        );

        config.set(
                path + ".skill-points",
                data.getSkillPoints()
        );

        config.set(
                path + ".souls",
                data.getSouls()
        );

        for (EnchantType type : EnchantType.values()) {
            config.set(
                    path + ".enchants." + type.getId(),
                    data.getEnchantLevel(type)
            );
        }

        for (SkillType type : SkillType.values()) {
            config.set(
                    path + ".skills." + type.getId(),
                    data.getSkillLevel(type)
            );
        }

        saveFile();
    }

    public void clearAllData() {
        playerDataMap.clear();
        config.set("players", null);
        saveFile();
    }
    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            savePlayerData(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayerData(uuid);
        playerDataMap.remove(uuid);
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not save playerdata.yml!"
            );

            e.printStackTrace();
        }
    }
}