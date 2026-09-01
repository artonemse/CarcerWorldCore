package Cosmetics.Trails;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class TrailGUIListener implements Listener {

    private final TrailManager trailManager;
    private final TrailGUI trailGUI;

    public TrailGUIListener(TrailManager trailManager, TrailGUI trailGUI) {
        this.trailManager = trailManager;
        this.trailGUI = trailGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TrailGUI.TITLE)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Stops ALL item movement inside the Trails GUI
        event.setCancelled(true);

        if (event.getRawSlot() == 18) {
            CarcerWorldCore.getInstance().getCosmeticsGUI().open(player);
            return;
        }

        TrailType type = switch (event.getRawSlot()) {
            case 10 -> TrailType.CHERRY_BLOSSOM;
            case 11 -> TrailType.HELLFIRE;
            case 12 -> TrailType.SOULMIST;
            case 13 -> TrailType.VOIDWALKER;
            case 14 -> TrailType.FROSTBOUND;
            case 15 -> TrailType.DIVINE;
            case 16 -> TrailType.ARCANE;
            case 22 -> TrailType.NONE;
            default -> null;
        };

        if (type == null) return;

        if (type == TrailType.NONE) {
            trailManager.selectTrail(player, type);
            trailGUI.open(player);
            return;
        }

        if (trailManager.ownsTrail(player, type)) {
            trailManager.selectTrail(player, type);
        } else {
            trailManager.purchaseTrail(player, type);
        }

        trailGUI.open(player);
    }
}