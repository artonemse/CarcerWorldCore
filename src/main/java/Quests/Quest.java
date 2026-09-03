package Quests;

import java.util.List;

public class Quest {

    private final String id;
    private final String name;
    private final QuestType type;
    private final String npcId;
    private final String prerequisite;
    private final List<QuestObjective> objectives;
    private final QuestReward reward;
    private final List<String> startDialogue;
    private final List<String> activeDialogue;
    private final List<String> readyDialogue;
    private final List<String> completeDialogue;
    private final List<String> finishedDialogue;

    public Quest(String id, String name, QuestType type, String npcId, String prerequisite, List<QuestObjective> objectives, QuestReward reward, List<String> startDialogue, List<String> activeDialogue, List<String> readyDialogue, List<String> completeDialogue, List<String> finishedDialogue) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.npcId = npcId;
        this.prerequisite = prerequisite;
        this.objectives = objectives;
        this.reward = reward;
        this.startDialogue = startDialogue;
        this.activeDialogue = activeDialogue;
        this.readyDialogue = readyDialogue;
        this.completeDialogue = completeDialogue;
        this.finishedDialogue = finishedDialogue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public QuestType getType() {
        return type;
    }

    public String getNpcId() {
        return npcId;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public List<QuestObjective> getObjectives() {
        return objectives;
    }

    public QuestReward getReward() {
        return reward;
    }

    public List<String> getStartDialogue() {
        return startDialogue;
    }

    public List<String> getActiveDialogue() {
        return activeDialogue;
    }

    public List<String> getReadyDialogue() {
        return readyDialogue;
    }

    public List<String> getCompleteDialogue() {
        return completeDialogue;
    }

    public List<String> getFinishedDialogue() {
        return finishedDialogue;
    }
}