package Ascension;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class AscensionGUIListener implements Listener {

    private final CarcerWorldCore plugin;
    private final AscensionManager ascensionManager;
    private final AscensionGUI ascensionGUI;

    public AscensionGUIListener(CarcerWorldCore plugin, AscensionManager ascensionManager, AscensionGUI ascensionGUI) {
        this.plugin = plugin;
        this.ascensionManager = ascensionManager;
        this.ascensionGUI = ascensionGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.equals(AscensionGUI.ASCENSION_TITLE)) {
            event.setCancelled(true);
            handleAscensionMenu(player, event.getRawSlot());
            return;
        }

        if (title.equals(AscensionGUI.CONFIRM_TITLE)) {
            event.setCancelled(true);
            handleConfirmationMenu(player, event.getRawSlot());
        }
    }

    private void handleAscensionMenu(Player player, int slot) {

        // ASCEND
        if (slot == 13) {
            if (!ascensionManager.canAscend(player)) {
                player.sendMessage(color("&c&lASCENSION &7&l| &fYour weapon must reach level &c100 &fto ascend."));
                return;
            }

            ascensionGUI.openConfirmation(player);
            return;
        }

        // BACK
        if (slot == 22) {
            plugin.getWeaponMenu().open(player);
        }
    }

    private void handleConfirmationMenu(Player player, int slot) {

        // CONFIRM
        if (slot == 11) {
            if (!ascensionManager.canAscend(player)) {
                player.closeInventory();
                player.sendMessage(color("&c&lASCENSION &7&l| &fYou no longer meet the requirements to ascend."));
                return;
            }

            player.closeInventory();
            ascensionManager.ascend(player);
            return;
        }

        // CANCEL
        if (slot == 15) {
            ascensionGUI.open(player);
        }
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}
