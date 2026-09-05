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

    public double getModifierPercent(Player player, ArmorStat stat) {
        double total = 0.0;

        for (SpecialArmorSet set : SpecialArmorSet.values()) {
            int pieces = getPieceCount(player, set);

            if (pieces <= 0) continue;

            if (stat == ArmorStat.HEALTH) total += set.getPieceHealth() * pieces;
            if (stat == ArmorStat.DAMAGE) total += set.getPieceDamage() * pieces;
            if (stat == ArmorStat.DAMAGE_REDUCTION) total += set.getPieceDamageReduction() * pieces;

            if (pieces >= 2 && stat == ArmorStat.DAMAGE) total += set.getTwoPieceDamage();

            if (pieces >= 4) {
                if (stat == ArmorStat.DAMAGE) total += set.getFourPieceDamage();
                if (stat == ArmorStat.DAMAGE_REDUCTION) total += set.getFourPieceDamageReduction();
            }
        }

        return total;
    }
}
