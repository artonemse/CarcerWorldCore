package Armor.Salvage;

import Armor.Generic.ArmorMaterialTier;
import Armor.Generic.GenericArmorData;
import Armor.Generic.GenericArmorGenerator;
import Currencies.ScrapManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SalvageManager {

    private final GenericArmorGenerator armorGenerator;
    private final ScrapManager scrapManager;

    public SalvageManager(GenericArmorGenerator armorGenerator, ScrapManager scrapManager) {
        this.armorGenerator = armorGenerator;
        this.scrapManager = scrapManager;
    }

    public boolean isSalvageable(ItemStack item) {
        return armorGenerator.isGenericArmor(item);
    }

    public int getSalvageValue(ItemStack item) {
        GenericArmorData data = armorGenerator.getArmorData(item);
        if (data == null) return 0;

        return getSalvageValue(data.getMaterialTier());
    }

    public int getSalvageValue(ArmorMaterialTier material) {
        return switch (material) {
            case LEATHER -> 12;
            case COPPER -> 10;
            case GOLD -> 8;
            case CHAINMAIL -> 6;
            case IRON -> 4;
            case DIAMOND -> 2;
        };
    }

    public boolean salvage(Player player, ItemStack item) {
        if (!isSalvageable(item)) return false;

        int value = getSalvageValue(item);
        if (value <= 0) return false;

        scrapManager.addScraps(player, value);
        return true;
    }
}
