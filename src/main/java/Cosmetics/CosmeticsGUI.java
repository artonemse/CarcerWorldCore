package Cosmetics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsGUI {

    public static final String TITLE = color("&8Cosmetics");

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);

        fill(inventory);

        inventory.setItem(10, createKillEffectsItem());
        inventory.setItem(12, createTrailsItem());
        inventory.setItem(14, createAurasItem());
        inventory.setItem(16, createWeaponSkinsItem());
        inventory.setItem(22, createBackItem());

        player.openInventory(inventory);
    }

    private ItemStack createKillEffectsItem() {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&d&lKill Effects"));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7&l| &fCustomize what happens"));
        lore.add(color("&7&l| &fwhen you defeat an enemy."));
        lore.add("");
        lore.add(color("&d&lClick to Open"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createTrailsItem() {
        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&6&lPlayer Trails"));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7&l| &fLeave cosmetic particles"));
        lore.add(color("&7&l| &fbehind you while moving."));
        lore.add("");
        lore.add(color("&6&lClick to Open"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createAurasItem() {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&5&lAuras"));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7&l| &fSurround yourself with"));
        lore.add(color("&7&l| &funique cosmetic effects."));
        lore.add("");
        lore.add(color("&c&lComing Soon"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createWeaponSkinsItem() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&b&lWeapon Skins"));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7&l| &fChange the appearance"));
        lore.add(color("&7&l| &fof your progression weapon."));
        lore.add("");
        lore.add(color("&c&lComing Soon"));

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
        lore.add(color("&7&l| &fReturn to the Weapon Menu."));

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
