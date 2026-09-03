package Cosmetics.WeaponSkins;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class WeaponSkinGUIListener implements Listener {

    private final CarcerWorldCore plugin;
    private final WeaponSkinManager manager;
    private final WeaponSkinGUI gui;

    public WeaponSkinGUIListener(CarcerWorldCore plugin, WeaponSkinManager manager, WeaponSkinGUI gui) {
        this.plugin = plugin;
        this.manager = manager;
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(WeaponSkinGUI.TITLE)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        if (event.getRawSlot() == 50) {
            plugin.getCosmeticsGUI().open(player);
            return;
        }

        if (event.getRawSlot() == 48) {
            if (manager.getSelected(player) == null) return;

            manager.setSelected(player, null);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            gui.open(player);
            return;
        }

        WeaponSkin skin = getSkinFromSlot(event.getRawSlot());
        if (skin == null) return;

        if (!manager.owns(player, skin)) {
            player.sendMessage("§b§lWEAPON SKINS §7§l| §fYou have not unlocked this skin.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
            return;
        }

        manager.setSelected(player, skin);

        player.sendMessage("§b§lWEAPON SKINS §7§l| §fEquipped " + color(skin.getDisplayName()) + "§f.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);

        gui.open(player);
    }

    private WeaponSkin getSkinFromSlot(int slot) {
        WeaponSkin[] skins = WeaponSkin.values();

        for (int i = 0; i < WeaponSkinGUI.SKIN_SLOTS.length && i < skins.length; i++) {
            if (slot == WeaponSkinGUI.SKIN_SLOTS[i]) return skins[i];
        }

        return null;
    }

    private String color(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}