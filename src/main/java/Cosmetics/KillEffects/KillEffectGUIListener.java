package Cosmetics.KillEffects;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class KillEffectGUIListener implements Listener {

    private final KillEffectManager killEffectManager;
    private final KillEffectGUI killEffectGUI;

    public KillEffectGUIListener(KillEffectManager killEffectManager, KillEffectGUI killEffectGUI) {
        this.killEffectManager = killEffectManager;
        this.killEffectGUI = killEffectGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(KillEffectGUI.TITLE)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);
        if (event.getRawSlot() == 18) {
            CarcerWorldCore.getInstance().getCosmeticsGUI().open(player);
            return;
        }
        KillEffectType type = switch (event.getRawSlot()) {

            case 10 -> KillEffectType.CHERRY_BLOSSOM;
            case 11 -> KillEffectType.WATER_PILLAR;
            case 12 -> KillEffectType.HELLFIRE_SKULL;
            case 13 -> KillEffectType.CASH_EXPLOSION;
            case 14 -> KillEffectType.VOID_RIFT;
            case 15 -> KillEffectType.BUTTERFLY_SWARM;
            case 16 -> KillEffectType.DIVINE_ASCENSION;
            case 22 -> KillEffectType.NONE;
            default -> null;
        };

        if (type == null) return;

        if (type == KillEffectType.NONE) {
            killEffectManager.selectEffect(player, type);
            killEffectGUI.open(player);
            return;
        }

        if (killEffectManager.ownsEffect(player, type)) {
            killEffectManager.selectEffect(player, type);
        } else {
            killEffectManager.purchaseEffect(player, type);
        }

        killEffectGUI.open(player);
    }
}
