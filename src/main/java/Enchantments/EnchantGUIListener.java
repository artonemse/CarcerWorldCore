package Enchantments;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EnchantGUIListener implements Listener {

    private final EnchantManager enchantManager;
    private final EnchantGUI enchantGUI;

    public EnchantGUIListener(
            EnchantManager enchantManager,
            EnchantGUI enchantGUI
    ) {
        this.enchantManager = enchantManager;
        this.enchantGUI = enchantGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView()
                .getTitle()
                .equals(EnchantGUI.TITLE)) {

            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);

        EnchantType type = switch (event.getRawSlot()) {

            case 10 -> EnchantType.SHARPNESS;

            case 11 ->
                    EnchantType.CRITICAL_STRIKE;

            case 12 ->
                    EnchantType.DOUBLE_STRIKE;

            case 14 ->
                    EnchantType.CLEAVE;

            case 16 ->
                    EnchantType.EXECUTE;

            default -> null;
        };

        if (type == null) {
            return;
        }

        enchantManager.upgradeEnchant(
                player,
                type
        );

        enchantGUI.open(player);
    }
}
