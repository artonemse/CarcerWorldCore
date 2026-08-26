package Weapons;

import PlayerData.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;

public class WeaponMenu {

    public static final String TITLE = "§8Weapon Menu";

    private final CarcerWorldCore plugin;

    public WeaponMenu(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);

        fill(inventory);

        inventory.setItem(10, createStatsItem(player));
        inventory.setItem(12, createSkillsItem(player));
        inventory.setItem(14, createEnchantsItem());
        inventory.setItem(16, createAscensionItem(player));
        inventory.setItem(22, createCosmeticsItem());

        player.openInventory(inventory);
    }

    private ItemStack createStatsItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        int level = data.getWeaponLevel();
        long exp = data.getWeaponExp();
        long kills = data.getMobKills();

        double damage = plugin.getWeaponManager().getDamage(player);

        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName("§6§lWeapon Stats");

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§7§l| §fView your current weapon");
        lore.add("§7§l| §fprogression and statistics.");
        lore.add("");
        lore.add("§7§l| §fLevel §6" + level);
        lore.add("§7§l| §fDamage §6" + format(damage));
        lore.add("§7§l| §fEXP §6" + formatLong(exp));
        lore.add("§7§l| §fMob Kills §6" + formatLong(kills));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createSkillsItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName("§b§lSkills");

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§7Spend skill points to improve");
        lore.add("§7your permanent abilities.");
        lore.add("");
        lore.add("§bSkill Points: §f" + data.getSkillPoints());
        lore.add("");
        lore.add("§eClick to open.");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createEnchantsItem() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName("§c§lWeapon Enchants");

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§7Upgrade your weapon with");
        lore.add("§7powerful custom enchantments.");
        lore.add("");
        lore.add("§eClick to open.");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createAscensionItem(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        int ascensions = data.getAscensions();
        int weaponLevel = data.getWeaponLevel();

        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName("§d§lAscension");

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§7Reset your weapon progression");
        lore.add("§7to gain permanent bonuses.");
        lore.add("");
        lore.add("§dAscension Level: §f" + ascensions);
        lore.add("§dWeapon Level: §f" + weaponLevel + "/100");
        lore.add("");

        if (weaponLevel >= 100) {
            lore.add("§aReady to Ascend!");
        } else {
            lore.add("§cRequires Weapon Level 100");
        }

        lore.add("");
        lore.add("§eClick to open.");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createCosmeticsItem() {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName("§d§lCosmetics");

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§7Customize your progression");
        lore.add("§7with cosmetic effects.");
        lore.add("");
        lore.add("§eClick to open.");

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

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private String format(double number) {
        if (number == Math.floor(number)) {
            return String.valueOf((long) number);
        }

        return String.format("%.1f", number);
    }

    private String formatLong(long number) {
        return String.format("%,d", number);
    }
}
