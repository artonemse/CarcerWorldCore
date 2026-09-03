package Quests;

import java.util.HashSet;
import java.util.Set;

public class PlayerQuestObjective {

    private int progress;
    private final Set<String> completedTargets = new HashSet<>();

    public PlayerQuestObjective() {
        this.progress = 0;
    }

    public PlayerQuestObjective(int progress, Set<String> completedTargets) {
        this.progress = progress;
        this.completedTargets.addAll(completedTargets);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void addProgress(int amount) {
        progress += amount;
    }

    public Set<String> getCompletedTargets() {
        return completedTargets;
    }

    public boolean addCompletedTarget(String target) {
        return completedTargets.add(target.toLowerCase());
    }
}
