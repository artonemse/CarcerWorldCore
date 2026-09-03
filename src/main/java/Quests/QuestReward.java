package Quests;

public class QuestReward {

    private final long souls;
    private final long gems;

    public QuestReward(long souls, long gems) {
        this.souls = souls;
        this.gems = gems;
    }

    public long getSouls() {
        return souls;
    }

    public long getGems() {
        return gems;
    }
}
