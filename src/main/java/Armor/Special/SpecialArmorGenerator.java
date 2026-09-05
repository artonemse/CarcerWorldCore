package Armor.Special;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;

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

        meta.setDisplayName(color(set.getDisplayName() + " " + slot.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(color("&2&lBLACKTHORN ARMOR"));
        lore.add(color("&7&l| &fHealth: &a+" + format(set.getPieceHealth()) + "%"));
        lore.add(color("&7&l| &fDamage: &a+" + format(set.getPieceDamage()) + "%"));
        lore.add(color("&7&l| &fDamage Reduction: &a+" + format(set.getPieceDamageReduction()) + "%"));
        lore.add("");
        lore.add(color("&2&lSET BONUS"));
        lore.add(color("&7&l| &f2 Pieces: &a+" + format(set.getTwoPieceDamage()) + "% Damage"));
        lore.add(color("&7&l| &f4 Pieces: &a+" + format(set.getFourPieceDamage()) + "% Damage"));
        lore.add(color("&7&l| &f          &a+" + format(set.getFourPieceDamageReduction()) + "% Damage Reduction"));
        lore.add("");
        lore.add(color("&5&lMAGIC ABILITY"));
        lore.add(color("&7&l| &fThornstorm"));
        lore.add(color("&7&l| &fDamage: &d250% Weapon Damage"));
        lore.add(color("&7&l| &fRadius: &d8 Blocks"));
        lore.add(color("&7&l| &fCooldown: &d30 Seconds"));
        lore.add("");
        lore.add(color("&d&lSneak + Right Click to Cast"));

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
