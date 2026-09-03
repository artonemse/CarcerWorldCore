package Armor.Salvage;

import Currencies.ScrapManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SalvageGUI {

    public static final String TITLE = "Blacksmith Salvage";
    public static final int INPUT_SLOT = 13;
    public static final int SALVAGE_SLOT = 22;
    public static final int BALANCE_SLOT = 4;

    private final SalvageManager salvageManager;
    private final ScrapManager scrapManager;

    public SalvageGUI(SalvageManager salvageManager, ScrapManager scrapManager) {
        this.salvageManager = salvageManager;
        this.scrapManager = scrapManager;
    }

    public void open(Player player) {
        Inventory inventory = player.getServer().createInventory(null, 27, color("&8" + TITLE));

        fill(inventory);

        inventory.setItem(BALANCE_SLOT, createBalanceItem(player));
        inventory.setItem(INPUT_SLOT, null);
        inventory.setItem(SALVAGE_SLOT, createSalvageButton(null));

        player.openInventory(inventory);
    }

    public void update(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();

        inventory.setItem(BALANCE_SLOT, createBalanceItem(player));
        inventory.setItem(SALVAGE_SLOT, createSalvageButton(inventory.getItem(INPUT_SLOT)));
    }

    public ItemStack createSalvageButton(ItemStack armor) {
        if (armor == null || armor.getType().isAir()) {
            return createItem(
                    Material.ANVIL,
                    "&f&lSalvage Armor",
                    "",
                    "&7&l| &fPlace randomized armor",
                    "&7&l| &fin the slot above.",
                    "",
                    "&cNo armor selected."
            );
        }

        if (!salvageManager.isSalvageable(armor)) {
            return createItem(
                    Material.BARRIER,
                    "&c&lCannot Salvage",
                    "",
                    "&7&l| &fThis item cannot",
                    "&7&l| &fbe salvaged.",
                    "",
                    "&cOnly randomized armor is accepted."
            );
        }

        int value = salvageManager.getSalvageValue(armor);

        return createItem(
                Material.ANVIL,
                "&a&lSalvage Armor",
                "",
                "&7&l| &fSalvage Value: &e" + format(value) + " Scraps",
                "",
                "&eClick to salvage this armor."
        );
    }

    private ItemStack createBalanceItem(Player player) {
        return createItem(
                Material.PAPER,
                "&f&lScraps",
                "",
                "&7&l| &fBalance: &e" + format(scrapManager.getScraps(player)),
                "",
                "&7Unused armor can be salvaged",
                "&7into Scraps at the Blacksmith."
        );
    }

    private void fill(Inventory inventory) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, filler);
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color(name));

        List<String> lore = new ArrayList<>();
        for (String line : loreLines) lore.add(color(line));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
