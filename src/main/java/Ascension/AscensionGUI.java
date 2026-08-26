package Ascension;

import PlayerData.PlayerData;
import Weapons.WeaponManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;

public class AscensionGUI {

    public static final String ASCENSION_TITLE = color("&8Ascension");
    public static final String CONFIRM_TITLE = color("&8Confirm Ascension");

    private final CarcerWorldCore plugin;
    private final AscensionManager ascensionManager;
    private final WeaponManager weaponManager;

    public AscensionGUI(CarcerWorldCore plugin, AscensionManager ascensionManager) {
        this.plugin = plugin;
        this.ascensionManager = ascensionManager;
        this.weaponManager = plugin.getWeaponManager();
    }

    // ================================
    // MAIN ASCENSION MENU
    // ================================

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ASCENSION_TITLE);

        fill(inventory);

        inventory.setItem(11, createCurrentWeaponItem(player));
        inventory.setItem(13, createAscensionItem(player));
        inventory.setItem(15, createNextWeaponItem(player));
        inventory.setItem(22, createBackItem());

        player.openInventory(inventory);
    }

    // ================================
    // CONFIRMATION MENU
    // ================================

    public void openConfirmation(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, CONFIRM_TITLE);

        fill(inventory);

        inventory.setItem(11, createConfirmItem(player));
        inventory.setItem(13, createWarningItem());
        inventory.setItem(15, createCancelItem());

        player.openInventory(inventory);
    }

    // ================================
    // CURRENT WEAPON
    // ================================

    private ItemStack createCurrentWeaponItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        int ascension = data.getAscensions();
        int weaponLevel = data.getWeaponLevel();

        ItemStack item = new ItemStack(weaponManager.getWeaponMaterial(ascension));
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&f&lCurrent Weapon"));

        List<String> lore = new ArrayList<>();

        lore.add(color("&7&l| &fWeapon: " + weaponManager.getAscensionWeaponName(ascension)));
        lore.add(color("&7&l| &fWeapon Level: &e" + weaponLevel + "&7/&e100"));
        lore.add(color("&7&l| &fAscension: &d" + ascension));
        lore.add(color("&7&l| &fBase Damage: &c" + format(weaponManager.getDamage(player))));

        if (ascension > 5) {
            int bonus = (ascension - 5) * 50;
            lore.add(color("&7&l| &fAscension Bonus: &c+" + bonus + "%"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // ================================
    // ASCENSION BUTTON
    // ================================

    private ItemStack createAscensionItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        int ascension = data.getAscensions();
        int weaponLevel = data.getWeaponLevel();

        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&d&lAscension"));

        List<String> lore = new ArrayList<>();

        lore.add(color("&7&l| &fCurrent Ascension: &d" + ascension));
        lore.add(color("&7&l| &fNext Ascension: &d" + (ascension + 1)));
        lore.add("");

        if (ascension < 5) {
            lore.add(color("&7&l| &fAscending will evolve your weapon."));
            lore.add(color("&7&l| &fNext Weapon: " + weaponManager.getAscensionWeaponName(ascension + 1)));
        } else {
            lore.add(color("&7&l| &fAscending grants another &c+50%"));
            lore.add(color("&7&l| &fbase weapon damage."));
        }

        lore.add("");
        lore.add(color("&c&lRESETS"));
        lore.add(color("&7&l| &fWeapon Level"));
        lore.add(color("&7&l| &fWeapon EXP"));
        lore.add(color("&7&l| &fSkill Points"));
        lore.add(color("&7&l| &fSkills"));
        lore.add(color("&7&l| &fWeapon Enchants"));
        lore.add("");
        lore.add(color("&a&lKEEPS"));
        lore.add(color("&7&l| &fSouls"));
        lore.add(color("&7&l| &fMob Kills"));
        lore.add(color("&7&l| &fAscension Level"));
        lore.add("");

        if (weaponLevel >= 100) {
            lore.add(color("&a&lClick to Ascend"));
        } else {
            lore.add(color("&c&lRequires Weapon Level 100"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // ================================
    // NEXT WEAPON
    // ================================

    private ItemStack createNextWeaponItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        int currentAscension = data.getAscensions();
        int nextAscension = currentAscension + 1;

        ItemStack item = new ItemStack(weaponManager.getWeaponMaterial(nextAscension));
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&f&lNext Ascension"));

        List<String> lore = new ArrayList<>();

        lore.add(color("&7&l| &fAscension: &d" + nextAscension));
        lore.add(color("&7&l| &fWeapon: " + weaponManager.getAscensionWeaponName(nextAscension)));

        if (nextAscension <= 5) {
            lore.add(color("&7&l| &fBase Damage: &c" + format(weaponManager.getAscensionBaseDamage(nextAscension))));
        } else {
            int bonus = (nextAscension - 5) * 50;
            double damage = 30.0 * (1.0 + ((nextAscension - 5) * 0.50));

            lore.add(color("&7&l| &fBase Damage: &c" + format(damage)));
            lore.add(color("&7&l| &fAscension Bonus: &c+" + bonus + "%"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // ================================
    // CONFIRM
    // ================================

    private ItemStack createConfirmItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&a&lConfirm Ascension"));

        List<String> lore = new ArrayList<>();

        lore.add(color("&7&l| &fAscend to level &d" + (data.getAscensions() + 1) + "&f."));
        lore.add(color("&7&l| &fYour progression listed"));
        lore.add(color("&7&l| &fabove will be reset."));
        lore.add("");
        lore.add(color("&a&lClick to Confirm"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // ================================
    // WARNING
    // ================================

    private ItemStack createWarningItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&c&lWarning"));

        List<String> lore = new ArrayList<>();

        lore.add(color("&7&l| &fAscending cannot be undone."));
        lore.add(color("&7&l| &fYour Weapon Level, EXP,"));
        lore.add(color("&7&l| &fSkills and Enchants will reset."));
        lore.add("");
        lore.add(color("&7&l| &fSouls and Mob Kills are kept."));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // ================================
    // CANCEL
    // ================================

    private ItemStack createCancelItem() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&c&lCancel"));

        List<String> lore = new ArrayList<>();

        lore.add(color("&7&l| &fReturn to the Ascension Menu."));
        lore.add("");
        lore.add(color("&c&lClick to Cancel"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    // ================================
    // BACK
    // ================================

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

    // ================================
    // FILLER
    // ================================

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

    private String format(double number) {
        if (number == Math.floor(number)) return String.valueOf((long) number);
        return String.format("%.1f", number);
    }
}
