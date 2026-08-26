package Weapons;

import Enchantments.EnchantType;
import PlayerData.PlayerData;
import Skills.SkillType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;
import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;

public class WeaponManager {

    private final CarcerWorldCore plugin;
    private final NamespacedKey weaponKey;

    public static String color(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String hex = message.substring(matcher.start(), matcher.end());
            StringBuilder replacement = new StringBuilder("§x");

            for (char c : hex.substring(1).toCharArray()) {
                replacement.append("§").append(c);
            }

            message = message.replace(hex, replacement.toString());
            matcher = pattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public double getAscensionBaseDamage(int ascension) {
        return switch (Math.min(ascension, 5)) {
            case 0 -> 5.0;
            case 1 -> 8.0;
            case 2 -> 12.0;
            case 3 -> 17.0;
            case 4 -> 23.0;
            default -> 30.0;
        };
    }

    public WeaponManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "carcer_weapon");
    }

    public ItemStack createWeapon(Player player) {

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        int level = data.getWeaponLevel();
        int ascension = data.getAscensions();
        long exp = data.getWeaponExp();
        long requiredExp = getExpRequired(level);
        long mobKills = data.getMobKills();

        // ================================
        // DAMAGE
        // ================================

        double baseDamage = getDamage(player);

        int sharpnessLevel =
                data.getEnchantLevel(EnchantType.SHARPNESS);

        double sharpnessBonus =
                sharpnessLevel * 2.0;

        int strengthLevel =
                data.getSkillLevel(SkillType.STRENGTH);

        double strengthPercent =
                strengthLevel * 3.0;

        double damageBeforeStrength =
                baseDamage + sharpnessBonus;

        double totalDamage =
                damageBeforeStrength
                        * (1.0 + (strengthPercent / 100.0));

        // ================================
        // CREATE WEAPON
        // ================================

        ItemStack item =
                new ItemStack(getWeaponMaterial(ascension));

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(color(
                "&6&l" + getAscensionWeaponName(ascension)
                        + " &7[&fLvl "
                        + level
                        + "&7]"
        ));

        List<String> lore = new ArrayList<>();

        // ================================
        // STATS
        // ================================

        lore.add("");

        lore.add(color(
                "&6&lWEAPON STATS"
        ));

        lore.add(color("&7&l| &fDamage &6" + format(totalDamage)));

        if (strengthLevel > 0) {
            lore.add(color("&7&l| &fStrength Bonus &6+" + format(strengthPercent) + "%"));
        }
        if (level >= 100) {
            lore.add(color("&7&l| &fEXP: &aMAX LEVEL"));
        } else {
            lore.add(color("&7&l| &fEXP &6" + formatLong(exp) + "&7/&6" + formatLong(requiredExp)));
        }
        lore.add(color("&7&l| &fMob Kills &6" + formatLong(mobKills)));
        if (ascension > 0) {
            lore.add(color("&7&l| &fAscension Level &6" + ascension));
        }
        if (ascension > 5) {
            int bonus = (ascension - 5) * 50;

            lore.add(color(
                    "&7&l| &fAscension Damage &6+"
                            + bonus
                            + "%"
            ));
        }

        // ================================
        // ENCHANTS
        // ================================

        int criticalStrike =
                data.getEnchantLevel(
                        EnchantType.CRITICAL_STRIKE
                );

        int doubleStrike =
                data.getEnchantLevel(
                        EnchantType.DOUBLE_STRIKE
                );

        int cleave =
                data.getEnchantLevel(
                        EnchantType.CLEAVE
                );

        int execute =
                data.getEnchantLevel(
                        EnchantType.EXECUTE
                );

        boolean hasEnchants =
                sharpnessLevel > 0
                        || criticalStrike > 0
                        || doubleStrike > 0
                        || cleave > 0
                        || execute > 0;

        if (hasEnchants) {

            lore.add("");

            lore.add(color(
                    "&c&lENCHANTS"
            ));

            if (sharpnessLevel > 0) {
                lore.add(color(
                        "&7&l| &fSharpness &c" + sharpnessLevel));
            }

            if (criticalStrike > 0) {
                lore.add(color("&7&l| &fCritical Strike &c" + criticalStrike));
            }

            if (doubleStrike > 0) {
                lore.add(color(
                        "&7&l| &fDouble Strike &c"
                                + doubleStrike
                ));
            }

            if (cleave > 0) {
                lore.add(color(
                        "&7&l| &fCleave &c"
                                + cleave
                ));
            }

            if (execute > 0) {
                lore.add(color(
                        "&7&l| &fExecute &c"
                                + execute
                ));
            }
        }


        // ================================
        // MENU
        // ================================

        lore.add("");

        lore.add(color(
                "&7&oRight-Click to open Weapon Menu"
        ));

        // ================================
        // ITEM SETTINGS
        // ================================

        meta.setLore(lore);

        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);

        meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ENCHANTS
        );

        meta.getPersistentDataContainer().set(
                weaponKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    public void giveOrUpdateWeapon(Player player) {
        ItemStack newWeapon = createWeapon(player);

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (isCarcerWeapon(item)) {
                player.getInventory().setItem(i, newWeapon);
                return;
            }
        }

        player.getInventory().setItem(0, newWeapon);
    }

    public boolean isCarcerWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        Byte value = meta.getPersistentDataContainer().get(
                weaponKey,
                PersistentDataType.BYTE
        );

        return value != null && value == (byte) 1;
    }

    public double getDamage(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        int ascension = data.getAscensions();

        double baseDamage = switch (Math.min(ascension, 5)) {
            case 0 -> 5.0;   // Wooden Sword
            case 1 -> 8.0;   // Copper Sword
            case 2 -> 12.0;  // Stone Sword
            case 3 -> 17.0;  // Iron Sword
            case 4 -> 23.0;  // Diamond Sword
            default -> 30.0; // Netherite Sword
        };

        if (ascension > 5) {
            int extraAscensions = ascension - 5;
            baseDamage *= 1.0 + (extraAscensions * 0.50);
        }

        return baseDamage;
    }

    public long getExpRequired(int level) {
        if (level >= 100) {
            return 0;
        }

        return 100L + (level * 25L);
    }

    public Material getWeaponMaterial(int ascension) {

        return switch (Math.min(ascension, 5)) {

            case 0 -> Material.WOODEN_SWORD;

            /*
             * Copper Sword compatibility.
             *
             * If your Paper/Minecraft version supports COPPER_SWORD,
             * this will use it.
             *
             * Otherwise it temporarily falls back to GOLDEN_SWORD.
             */
            case 1 -> {
                Material copper =
                        Material.matchMaterial("COPPER_SWORD");

                yield copper != null
                        ? copper
                        : Material.GOLDEN_SWORD;
            }

            case 2 -> Material.STONE_SWORD;

            case 3 -> Material.IRON_SWORD;

            case 4 -> Material.DIAMOND_SWORD;

            default -> Material.NETHERITE_SWORD;
        };
    }
    public String getAscensionWeaponName(int ascension) {
        return switch (Math.min(ascension, 5)) {
            case 0 -> color("#C58A55&lTraining Sword");
            case 1 -> color("#E77C4C&lRusted Sword");
            case 2 -> color("#A8A8A8&lOathbound Edge");
            case 3 -> color("#E4E4E4&lKnight's Vow");
            case 4 -> color("#55FFFF&lFrostveil Reckoner");
            default -> color("#8B6B8F&lEternity's Edge");
        };
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
