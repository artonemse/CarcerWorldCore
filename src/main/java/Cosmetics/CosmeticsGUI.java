package Cosmetics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class CosmeticsGUI {

    public static final String TITLE = color("&8Cosmetics");

    public static final class Menu implements InventoryHolder {
        private final UUID owner;
        private final Inventory inventory;

        public Menu(Player player) {
            owner = player.getUniqueId();
            inventory = Bukkit.createInventory(this, 45, TITLE);
        }

        public UUID getOwner() {
            return owner;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void open(Player player) {
        Menu menu = new Menu(player);
        Inventory inventory = menu.getInventory();

        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, filler);

        inventory.setItem(10, item(Material.FIREWORK_STAR, "&f&lKill Effects",
                "&7&l| &fCustomize enemy defeat effects.",
                "",
                "&eClick to Open"));

        inventory.setItem(12, item(Material.BLAZE_POWDER, "&f&lPlayer Trails",
                "&7&l| &fLeave cosmetic particles behind you.",
                "",
                "&eClick to Open"));

        inventory.setItem(14, item(Material.END_CRYSTAL, "&f&lAuras",
                "&7&l| &fSurround yourself with effects.",
                "",
                "&cComing Soon"));

        inventory.setItem(16, item(Material.NETHERITE_SWORD, "&f&lWeapon Skins",
                "&7&l| &fCustomize your progression weapon.",
                "",
                "&eClick to Open"));

        inventory.setItem(21, item(Material.WHITE_DYE, "&f&lChat Colors",
                "&7&l| &fStandard Minecraft chat colors.",
                "&7&l| &fPurchase using Scraps.",
                "",
                "&eClick to Open"));

        inventory.setItem(23, item(Material.AMETHYST_SHARD, "&f&lPremium Gradients",
                "&7&l| &fHex gradient chat colors.",
                "&7&l| &fGem purchases and earned rewards.",
                "",
                "&eClick to Open"));

        inventory.setItem(25, item(Material.NAME_TAG, "&f&lPlayer Tags",
                "&7&l| &fShow a title beside your name.",
                "&7&l| &fEarned through gameplay only.",
                "",
                "&eClick to Open"));

        inventory.setItem(40, item(Material.ARROW, "&f&lBack",
                "&7&l| &fReturn to the Weapon Menu."));

        player.openInventory(inventory);
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color(name));
        meta.setLore(Arrays.stream(lore).map(CosmeticsGUI::color).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}