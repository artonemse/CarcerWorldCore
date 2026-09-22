package Cosmetics;

import Cosmetics.Chat.ChatCosmetic;
import Cosmetics.Chat.ChatCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class CosmeticsGUIListener implements Listener {

    private final CarcerWorldCore plugin;

    public CosmeticsGUIListener(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof CosmeticsGUI.Menu menu)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!menu.getOwner().equals(player.getUniqueId())) return;
        if (!event.isLeftClick() && !event.isRightClick()) return;
        if (event.isShiftClick()) return;

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= menu.getInventory().getSize()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getOpenInventory().getTopInventory() != menu.getInventory()) return;

            switch (slot) {
                case 10 -> plugin.getKillEffectGUI().open(player);
                case 12 -> plugin.getTrailGUI().open(player);
                case 14 -> {
                    player.closeInventory();
                    player.sendMessage("§5§lCOSMETICS §7§l| §fAuras are coming soon.");
                }
                case 16 -> plugin.getWeaponSkinGUI().open(player);
                case 20 -> ChatCosmetics.get().gui().open(player, ChatCosmetic.Kind.BASIC);
                case 22 -> ChatCosmetics.get().gui().open(player, ChatCosmetic.Kind.PREMIUM);
                case 24 -> ChatCosmetics.get().gui().open(player, ChatCosmetic.Kind.TAG);
                case 40 -> plugin.getWeaponMenu().open(player);
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CosmeticsGUI.Menu) event.setCancelled(true);
    }
}