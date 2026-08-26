package Enchantments;

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

public class EnchantGUI {

    public static final String TITLE =
            ChatColor.translateAlternateColorCodes(
                    '&',
                    "&8Weapon Enchants"
            );

    private final CarcerWorldCore plugin;
    private final EnchantManager enchantManager;

    public EnchantGUI(
            CarcerWorldCore plugin,
            EnchantManager enchantManager
    ) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    public void open(Player player) {
        Inventory inventory =
                Bukkit.createInventory(
                        null,
                        27,
                        TITLE
                );

        fill(inventory);

        inventory.setItem(
                10,
                createEnchantItem(
                        player,
                        EnchantType.SHARPNESS
                )
        );

        inventory.setItem(
                11,
                createEnchantItem(
                        player,
                        EnchantType.CRITICAL_STRIKE
                )
        );

        inventory.setItem(
                12,
                createEnchantItem(
                        player,
                        EnchantType.DOUBLE_STRIKE
                )
        );

        inventory.setItem(
                14,
                createEnchantItem(
                        player,
                        EnchantType.CLEAVE
                )
        );

        inventory.setItem(
                16,
                createEnchantItem(
                        player,
                        EnchantType.EXECUTE
                )
        );

        inventory.setItem(
                22,
                createSoulItem(player)
        );

        player.openInventory(inventory);
    }

    private ItemStack createEnchantItem(
            Player player,
            EnchantType type
    ) {
        ItemStack item =
                new ItemStack(type.getIcon());

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        int level = enchantManager
                .getEnchantLevel(player, type);

        meta.setDisplayName(
                color(type.getDisplayName())
        );

        List<String> lore = new ArrayList<>();

        switch (type) {

            case SHARPNESS -> {
                lore.add(color(
                        "&7&l| &fIncrease your weapon's base damage."
                ));

                lore.add(color(
                        "&7&l| &fBonus Per Level: &c+2 Damage"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Bonus: &c+"
                                + (level * 2)
                                + " Damage"
                ));
            }

            case CRITICAL_STRIKE -> {
                lore.add(color(
                        "&7&l| &fChance to deal a critical strike."
                ));

                lore.add(color(
                        "&7&l| &fCritical Damage: &6x3"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Chance: &6"
                                + (level * 2)
                                + "%"
                ));
            }

            case DOUBLE_STRIKE -> {
                lore.add(color(
                        "&7&l| &fChance to deal double damage."
                ));

                lore.add(color(
                        "&7&l| &fDamage Multiplier: &ex2"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Chance: &e"
                                + level
                                + "%"
                ));
            }

            case CLEAVE -> {
                lore.add(color(
                        "&7&l| &fChance to damage nearby enemies."
                ));

                lore.add(color(
                        "&7&l| &fCleave Damage: &550%"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Chance: &5"
                                + (level * 2)
                                + "%"
                ));
            }

            case EXECUTE -> {
                lore.add(color(
                        "&7&l| &fChance to instantly execute mobs"
                ));

                lore.add(color(
                        "&7&l| &fat or below &430% Health&f."
                ));

                lore.add(color(
                        "&7&l| &fCurrent Chance: &4"
                                + level
                                + "%"
                ));
            }
        }

        lore.add("");

        lore.add(color(
                "&7&l| &fLevel: &e"
                        + level
                        + "&7/"
                        + type.getMaxLevel()
        ));

        if (level >= type.getMaxLevel()) {

            lore.add("");
            lore.add(color(
                    "&a&lMAX LEVEL"
            ));

        } else {

            long cost = type.getUpgradeCost(level);

            lore.add(color(
                    "&7&l| &fUpgrade Cost: &b"
                            + format(cost)
                            + " Souls"
            ));

            lore.add("");
            lore.add(color(
                    "&e&lClick to Upgrade"
            ));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createSoulItem(Player player) {
        ItemStack item =
                new ItemStack(Material.SOUL_LANTERN);

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(
                color("&b&lSouls")
        );

        List<String> lore = new ArrayList<>();

        lore.add(color(
                "&7&l| &fCurrent Balance: &b"
                        + format(
                        plugin.getSoulManager()
                                .getSouls(player)
                )
        ));

        lore.add(color(
                "&7&l| &fUsed to upgrade weapon enchants."
        ));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private void fill(Inventory inventory) {
        ItemStack filler =
                new ItemStack(
                        Material.GRAY_STAINED_GLASS_PANE
                );

        ItemMeta meta = filler.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }

    private String format(long number) {
        return String.format("%,d", number);
    }
}