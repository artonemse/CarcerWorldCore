package Cosmetics.WeaponSkins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;

public class WeaponSkinGUI {

    public static final String TITLE = color("&8Weapon Skins");
    public static final int[] SKIN_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33
    };

    private final WeaponSkinManager manager;

    public WeaponSkinGUI(WeaponSkinManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);

        fill(inventory);

        WeaponSkin[] skins = WeaponSkin.values();

        for (int i = 0; i < skins.length && i < SKIN_SLOTS.length; i++) {
            inventory.setItem(SKIN_SLOTS[i], createSkinItem(player, skins[i]));
        }

        inventory.setItem(48, createRemoveItem(player));
        inventory.setItem(50, createBackItem());

        player.openInventory(inventory);
    }

    private ItemStack createSkinItem(Player player, WeaponSkin skin) {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color(skin.getDisplayName()));

        CustomModelDataComponent component = meta.getCustomModelDataComponent();
        component.setFloats(List.of((float) skin.getCustomModelData()));
        meta.setCustomModelDataComponent(component);

        List<String> lore = new ArrayList<>();
        lore.add("");

        if (manager.isSelected(player, skin)) {
            lore.add(color("&7&l| &fStatus: &aEquipped"));
            lore.add("");
            lore.add(color("&a&lCurrently Equipped"));
        } else if (manager.owns(player, skin)) {
            lore.add(color("&7&l| &fStatus: &aUnlocked"));
            lore.add("");
            lore.add(color("&e&lClick to Equip"));
        } else {
            lore.add(color("&7&l| &fStatus: &cLocked"));
            lore.add("");
            lore.add(color("&c&lNot Unlocked"));
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createRemoveItem(Player player) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&c&lRemove Weapon Skin"));

        List<String> lore = new ArrayList<>();
        WeaponSkin selected = manager.getSelected(player);

        if (selected == null) {
            lore.add(color("&7&l| &fNo weapon skin equipped."));
        } else {
            lore.add(color("&7&l| &fEquipped: " + selected.getDisplayName()));
            lore.add("");
            lore.add(color("&e&lClick to Remove"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&f&lBack"));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7&l| &fReturn to Cosmetics."));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private void fill(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}