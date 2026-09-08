package Armor.Special;

import Armor.Generic.ArmorStat;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpecialArmorGenerator {

    private final NamespacedKey specialArmorKey;
    private final NamespacedKey setKey;
    private final NamespacedKey slotKey;

    public SpecialArmorGenerator(CarcerWorldCore plugin) {
        specialArmorKey = new NamespacedKey(plugin, "special_armor");
        setKey = new NamespacedKey(plugin, "special_armor_set");
        slotKey = new NamespacedKey(plugin, "special_armor_slot");
    }

    public ItemStack createArmor(SpecialArmorSet set, SpecialArmorSlot slot) {
        ItemStack item = new ItemStack(slot.getMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        if (meta instanceof ArmorMeta armorMeta) {
            if (set == SpecialArmorSet.ROYAL_GUARD) armorMeta.setTrim(new ArmorTrim(TrimMaterial.GOLD, TrimPattern.SPIRE));
            if (set == SpecialArmorSet.GOBLIN_SLAYER) armorMeta.setTrim(new ArmorTrim(TrimMaterial.REDSTONE, TrimPattern.RIB));
        }

        meta.setDisplayName(color(set.getDisplayName() + " " + slot.getDisplayName()));

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add(color(set.getArmorTitle()));

        addStats(lore, set.getPieceStats(), set.getAccentColor());

        lore.add("");
        lore.add(color(set.getAccentColor() + "&lSET BONUS"));
        lore.add(color("&7&l| &f2 Pieces:"));

        addStats(lore, set.getTwoPieceStats(), set.getAccentColor());

        lore.add(color("&7&l| &f4 Pieces:"));

        addStats(lore, set.getFourPieceStats(), set.getAccentColor());

        lore.add("");

        addAbilityLore(lore, set);

        lore.add("");
        lore.add(color(set.getAccentColor() + "&lSneak + Right Click to Cast"));

        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer().set(specialArmorKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(setKey, PersistentDataType.STRING, set.getId());
        meta.getPersistentDataContainer().set(slotKey, PersistentDataType.STRING, slot.name());

        item.setItemMeta(meta);

        return item;
    }

    private void addAbilityLore(List<String> lore, SpecialArmorSet set) {
        lore.add(color(set.getAccentColor() + "&lMAGIC ABILITY"));
        lore.add(color("&7&l| &f" + set.getAbilityName()));

        switch (set) {
            case BLACKTHORN -> {
                lore.add(color("&7&l| &fDamage: &a50"));
                lore.add(color("&7&l| &fRadius: &a8 Blocks"));
                lore.add(color("&7&l| &fCooldown: &a30 Seconds"));
            }

            case GRAVEBORN -> {
                lore.add(color("&7&l| &fFallback Damage: &d50"));
                lore.add(color("&7&l| &fTargets: &dUp to 7"));
                lore.add(color("&7&l| &fExecute Threshold: &d75% Health"));
                lore.add(color("&7&l| &fHealing: &d30% Per Target"));
                lore.add(color("&7&l| &fMax Healing: &d40 Hearts"));
                lore.add(color("&7&l| &fRadius: &d8 Blocks"));
                lore.add(color("&7&l| &fCooldown: &d30 Seconds"));
            }

            case BLACKTIDE -> {
                lore.add(color("&7&l| &fPull Damage: &b15"));
                lore.add(color("&7&l| &fCollapse Damage: &b50"));
                lore.add(color("&7&l| &fTargets: &bUp to 10"));
                lore.add(color("&7&l| &fRadius: &b8 Blocks"));
                lore.add(color("&7&l| &fPull Duration: &b2 Seconds"));
                lore.add(color("&7&l| &fCooldown: &b30 Seconds"));
            }

            case ROYAL_GUARD -> {
                lore.add(color("&6&lGuardian of Judgment"));
                lore.add(color("&7&l| &fSlam Damage: &e25"));
                lore.add(color("&7&l| &fSlam Radius: &e7 Blocks"));
                lore.add(color(""));
                lore.add(color("&e&lGuardian of Restoration"));
                lore.add(color("&7&l| &fHealing: &e60% Maximum Health"));
                lore.add(color("&7&l| &fResistance I: &e60 Seconds"));
                lore.add(color("&7&l| &fCooldown: &e35 Seconds"));
            }

            case GOBLIN_SLAYER -> {
                lore.add(color("&2&lBlade Frenzy"));
                lore.add(color("&7&l| &fStrikes: &a5"));
                lore.add(color("&7&l| &fDamage Per Strike: &a15"));
                lore.add(color("&7&l| &fTotal Damage: &a75"));
                lore.add(color("&7&l| &fTargets: &aUp to 10"));
                lore.add(color("&7&l| &fRadius: &a6 Blocks"));
                lore.add(color("&7&l| &fCooldown: &a25 Seconds"));
            }
        }
    }

    private void addStats(List<String> lore, Map<ArmorStat, Double> stats, String accentColor) {
        for (Map.Entry<ArmorStat, Double> entry : stats.entrySet()) {
            lore.add(color("&7&l| &f" + getStatName(entry.getKey()) + ": " + accentColor + "+" + format(entry.getValue()) + "%"));
        }
    }

    private String getStatName(ArmorStat stat) {
        return switch (stat) {
            case HEALTH -> "Health";
            case DAMAGE -> "Damage";
            case SOUL_REWARD -> "Soul Reward";
            case WEAPON_XP -> "Weapon XP";
            case LOOT_FIND -> "Loot Find";
            case DAMAGE_REDUCTION -> "Damage Reduction";
            case MOVEMENT_SPEED -> "Movement Speed";
            case HEALING -> "Healing";
        };
    }

    public boolean isSpecialArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        Byte value = item.getItemMeta().getPersistentDataContainer().get(specialArmorKey, PersistentDataType.BYTE);

        return value != null && value == (byte) 1;
    }

    public SpecialArmorSet getSet(ItemStack item) {
        if (!isSpecialArmor(item)) return null;

        String id = item.getItemMeta().getPersistentDataContainer().get(setKey, PersistentDataType.STRING);

        return SpecialArmorSet.fromId(id);
    }

    public SpecialArmorSlot getSlot(ItemStack item) {
        if (!isSpecialArmor(item)) return null;

        String value = item.getItemMeta().getPersistentDataContainer().get(slotKey, PersistentDataType.STRING);

        if (value == null) return null;

        try {
            return SpecialArmorSlot.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String format(double value) {
        if (value == Math.floor(value)) return String.valueOf((long) value);

        return String.format("%.1f", value);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}