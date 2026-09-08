package Quests;

import Armor.Special.SpecialArmorSet;
import Armor.Special.SpecialArmorSlot;

public class QuestReward {

    private final long souls;
    private final long gems;
    private final SpecialArmorSet specialArmorSet;
    private final SpecialArmorSlot specialArmorSlot;

    public QuestReward(long souls, long gems) {
        this(souls, gems, null, null);
    }

    public QuestReward(long souls, long gems, SpecialArmorSet specialArmorSet, SpecialArmorSlot specialArmorSlot) {
        this.souls = souls;
        this.gems = gems;
        this.specialArmorSet = specialArmorSet;
        this.specialArmorSlot = specialArmorSlot;
    }

    public long getSouls() {
        return souls;
    }

    public long getGems() {
        return gems;
    }

    public SpecialArmorSet getSpecialArmorSet() {
        return specialArmorSet;
    }

    public SpecialArmorSlot getSpecialArmorSlot() {
        return specialArmorSlot;
    }

    public boolean hasSpecialArmorReward() {
        return specialArmorSet != null && specialArmorSlot != null;
    }
}
