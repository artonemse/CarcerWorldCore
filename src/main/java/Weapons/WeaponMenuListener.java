package Weapons;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class WeaponMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(WeaponMenu.TITLE)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        switch (slot) {

            case 10 -> {
                player.sendMessage(
                        "§6§lWEAPON §8> §fYour current weapon stats are shown in this menu."
                );
            }

            case 12 -> {
                player.closeInventory();

                CarcerWorldCore.getInstance()
                        .getSkillsGUI()
                        .open(player);
            }

            case 14 -> {
                player.closeInventory();

                CarcerWorldCore.getInstance()
                        .getEnchantGUI()
                        .open(player);
            }

            case 16 -> {
                player.closeInventory();
                CarcerWorldCore.getInstance().getAscensionGUI().open(player);
            }

            case 22 -> {
                player.closeInventory();

                player.sendMessage(
                        "§d§lCOSMETICS §8> §fCosmetics are coming soon."
                );
            }
        }
    }
}
