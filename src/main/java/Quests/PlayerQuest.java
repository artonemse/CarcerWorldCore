package Quests;

public class PlayerQuest {

    private final String questId;
    private QuestState state;
    private int progress;

    public PlayerQuest(String questId, QuestState state, int progress) {
        this.questId = questId;
        this.state = state;
        this.progress = progress;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void addProgress(int amount) {
        progress += amount;
    }
}
