package Quests;

public class Quest {

    private final String id;
    private final String name;
    private final String npcId;
    private final QuestObjectiveType objectiveType;
    private final int requiredAmount;
    private final long soulReward;

    public Quest(String id, String name, String npcId, QuestObjectiveType objectiveType, int requiredAmount, long soulReward) {
        this.id = id;
        this.name = name;
        this.npcId = npcId;
        this.objectiveType = objectiveType;
        this.requiredAmount = requiredAmount;
        this.soulReward = soulReward;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNpcId() {
        return npcId;
    }

    public QuestObjectiveType getObjectiveType() {
        return objectiveType;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public long getSoulReward() {
        return soulReward;
    }
}
