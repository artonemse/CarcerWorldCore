package MobZones;

public class MobZoneEntry {

    private final MobType mobType;
    private final int weight;

    public MobZoneEntry(MobType mobType, int weight) {
        this.mobType = mobType;
        this.weight = weight;
    }

    public MobType getMobType() {
        return mobType;
    }

    public int getWeight() {
        return weight;
    }
}