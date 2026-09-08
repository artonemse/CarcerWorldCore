package Armor.Special;

import Armor.Generic.ArmorStat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpecialArmorManager {

    private final SpecialArmorGenerator generator;

    public SpecialArmorManager(SpecialArmorGenerator generator) {
        this.generator = generator;
    }

    public int getPieceCount(Player player, SpecialArmorSet set) {
        int pieces = 0;

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (generator.getSet(item) == set) pieces++;
        }

        return pieces;
    }

    public boolean hasFullSet(Player player, SpecialArmorSet set) {
        return getPieceCount(player, set) >= 4;
    }

    public SpecialArmorSet getFullSet(Player player) {
        for (SpecialArmorSet set : SpecialArmorSet.values()) {
            if (hasFullSet(player, set)) return set;
        }

        return null;
    }

    public double getModifierPercent(Player player, ArmorStat stat) {
        double total = 0.0;

        for (SpecialArmorSet set : SpecialArmorSet.values()) {
            int pieces = getPieceCount(player, set);

            if (pieces <= 0) continue;

            total += set.getPieceStat(stat) * pieces;

            if (pieces >= 2) total += set.getTwoPieceStat(stat);
            if (pieces >= 4) total += set.getFourPieceStat(stat);
        }

        return total;
    }
}