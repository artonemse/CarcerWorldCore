package Quests;

import java.util.HashMap;
import java.util.Map;

public class PlayerQuest {

    private final String questId;
    private QuestState state;
    private final Map<String, PlayerQuestObjective> objectives = new HashMap<>();

    public PlayerQuest(String questId, QuestState state) {
        this.questId = questId;
        this.state = state;
    }

    public String getQuestId() {
        return questId;
    }

    public QuestState getState() {
        return state;
    }

    public void setState(QuestState state) {
        this.state = state;
    }

    public Map<String, PlayerQuestObjective> getObjectives() {
        return objectives;
    }

    public PlayerQuestObjective getObjective(String objectiveId) {
        return objectives.computeIfAbsent(objectiveId.toLowerCase(), id -> new PlayerQuestObjective());
    }
}