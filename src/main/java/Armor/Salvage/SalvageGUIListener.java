package Armor.Salvage;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class SalvageGUIListener implements Listener {

    private final SalvageManager salvageManager;
    private final SalvageGUI salvageGUI;

    public SalvageGUIListener(SalvageManager salvageManager, SalvageGUI salvageGUI) {
        this.salvageManager = salvageManager;
        this.salvageGUI = salvageGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isSalvageGUI(event.getView().getTitle())) return;

        Inventory top = event.getView().getTopInventory();
        int rawSlot = event.getRawSlot();

        if (rawSlot == SalvageGUI.INPUT_SLOT) {
            event.setCancelled(false);
            player.getServer().getScheduler().runTaskLater(org.carcercore.carcerWorldCore.CarcerWorldCore.getInstance(), () -> salvageGUI.update(player), 1L);
            return;
        }

        if (rawSlot == SalvageGUI.SALVAGE_SLOT) {
            event.setCancelled(true);

            ItemStack armor = top.getItem(SalvageGUI.INPUT_SLOT);

            if (armor == null || armor.getType().isAir()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
                return;
            }

            if (!salvageManager.isSalvageable(armor)) {
                player.sendMessage(color("&c&lBLACKSMITH &7&l| &fOnly randomized armor can be salvaged."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
                return;
            }

            int value = salvageManager.getSalvageValue(armor);

            top.setItem(SalvageGUI.INPUT_SLOT, null);
            salvageManager.salvage(player, armor);

            player.sendMessage(color("&6&lBLACKSMITH &7&l| &fSalvaged armor for &e" + format(value) + " Scraps&f."));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.2f);

            salvageGUI.update(player);
            return;
        }

        if (rawSlot < top.getSize()) {
            event.setCancelled(true);
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!isSalvageGUI(event.getView().getTitle())) return;

        Inventory inventory = event.getInventory();
        ItemStack armor = inventory.getItem(SalvageGUI.INPUT_SLOT);

        if (armor == null || armor.getType().isAir()) return;

        inventory.setItem(SalvageGUI.INPUT_SLOT, null);

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(armor);

        for (ItemStack leftover : leftovers.values()) player.getWorld().dropItemNaturally(player.getLocation(), leftover);
    }

    private boolean isSalvageGUI(String title) {
        return SalvageGUI.TITLE.equalsIgnoreCase(ChatColor.stripColor(title));
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
