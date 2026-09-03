package Quests;

import java.util.List;

public class QuestObjective {

    private final String id;
    private final QuestObjectiveType type;
    private final String description;
    private final String target;
    private final int requiredAmount;
    private final List<String> targets;

    public QuestObjective(String id, QuestObjectiveType type, String description, String target, int requiredAmount, List<String> targets) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.target = target;
        this.requiredAmount = requiredAmount;
        this.targets = targets;
    }

    public String getId() {
        return id;
    }

    public QuestObjectiveType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getTarget() {
        return target;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public List<String> getTargets() {
        return targets;
    }

    public int getEffectiveRequiredAmount() {
        if (!targets.isEmpty()) return targets.size();
        return requiredAmount;
    }
}
