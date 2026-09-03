package Quests;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestLoader {

    private final CarcerWorldCore plugin;

    public QuestLoader(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public Map<String, Quest> loadQuests() {
        File file = new File(plugin.getDataFolder(), "quest-definitions.yml");

        if (!file.exists()) plugin.saveResource("quest-definitions.yml", false);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection questsSection = config.getConfigurationSection("quests");
        Map<String, Quest> quests = new HashMap<>();

        if (questsSection == null) return quests;

        for (String questId : questsSection.getKeys(false)) {
            String path = "quests." + questId;

            try {
                String name = config.getString(path + ".name", questId);
                QuestType type = QuestType.valueOf(config.getString(path + ".type", "MAIN").toUpperCase());
                String npcId = config.getString(path + ".npc", "");
                String prerequisite = config.getString(path + ".prerequisite", "");

                List<QuestObjective> objectives = loadObjectives(config, path);
                QuestReward reward = new QuestReward(config.getLong(path + ".rewards.souls", 0), config.getLong(path + ".rewards.gems", 0));

                List<String> startDialogue = config.getStringList(path + ".dialogue.start");
                List<String> activeDialogue = config.getStringList(path + ".dialogue.active");
                List<String> readyDialogue = config.getStringList(path + ".dialogue.ready");
                List<String> completeDialogue = config.getStringList(path + ".dialogue.complete");
                List<String> finishedDialogue = config.getStringList(path + ".dialogue.finished");

                Quest quest = new Quest(questId, name, type, npcId, prerequisite, objectives, reward, startDialogue, activeDialogue, readyDialogue, completeDialogue, finishedDialogue);
                quests.put(questId.toLowerCase(), quest);
            } catch (Exception exception) {
                plugin.getLogger().warning("[CarcerWorldCore] Failed to load quest: " + questId);
                exception.printStackTrace();
            }
        }

        plugin.getLogger().info("[CarcerWorldCore] Loaded " + quests.size() + " quests.");
        return quests;
    }

    private List<QuestObjective> loadObjectives(FileConfiguration config, String questPath) {
        List<Map<?, ?>> objectiveMaps = config.getMapList(questPath + ".objectives");
        List<QuestObjective> objectives = new ArrayList<>();

        int index = 0;

        for (Map<?, ?> map : objectiveMaps) {
            String id = map.containsKey("id") ? String.valueOf(map.get("id")) : "objective_" + index;
            String typeString = map.containsKey("type") ? String.valueOf(map.get("type")) : "KILL_HOSTILE_MOBS";
            String description = map.containsKey("description") ? String.valueOf(map.get("description")) : "";
            String target = map.containsKey("target") ? String.valueOf(map.get("target")) : "";
            int amount = parseInt(map.get("amount"), 1);

            List<String> targets = new ArrayList<>();
            Object targetsObject = map.get("targets");

            if (targetsObject instanceof List<?> list) {
                for (Object value : list) targets.add(String.valueOf(value).toLowerCase());
            }

            QuestObjectiveType type = QuestObjectiveType.valueOf(typeString.toUpperCase());

            objectives.add(new QuestObjective(id, type, description, target.toLowerCase(), amount, Collections.unmodifiableList(targets)));
            index++;
        }

        return objectives;
    }

    private int parseInt(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception exception) {
            return fallback;
        }
    }
}
