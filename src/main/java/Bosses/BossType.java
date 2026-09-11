package Bosses;

public enum BossType {

    THORNBOUND_WARDEN(
            "thornbound_warden",
            "&2&lThe Thornbound Warden",
            "heart_of_blackthorn",
            1800.0,
            35.0,
            18.0
    );

    private final String id;
    private final String displayName;
    private final String questId;
    private final double maxHealth;
    private final double arenaRadius;
    private final double triggerRadius;

    BossType(String id, String displayName, String questId, double maxHealth, double arenaRadius, double triggerRadius) {
        this.id = id;
        this.displayName = displayName;
        this.questId = questId;
        this.maxHealth = maxHealth;
        this.arenaRadius = arenaRadius;
        this.triggerRadius = triggerRadius;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getQuestId() {
        return questId;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getArenaRadius() {
        return arenaRadius;
    }

    public double getTriggerRadius() {
        return triggerRadius;
    }

    public static BossType fromId(String id) {
        if (id == null) return null;

        for (BossType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }

        return null;
    }
}