package Cosmetics;

import Cosmetics.Trails.TrailGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class CosmeticsGUIListener implements Listener {

    private final CarcerWorldCore plugin;

    public CosmeticsGUIListener(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(CosmeticsGUI.TITLE)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        switch (event.getRawSlot()) {
            case 10 -> plugin.getKillEffectGUI().open(player);

            case 12 -> plugin.getTrailGUI().open(player);

            case 14 -> {
                player.closeInventory();
                player.sendMessage("§5§lCOSMETICS §7§l| §fAuras are coming soon.");
            }

            case 16 -> {
                player.closeInventory();
                player.sendMessage("§b§lCOSMETICS §7§l| §fWeapon Skins are coming soon.");
            }

            case 22 -> plugin.getWeaponMenu().open(player);
        }
    }
}
