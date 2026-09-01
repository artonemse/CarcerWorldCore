package Cosmetics.KillEffects;

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

public class KillEffectGUI {

    public static final String TITLE = color("&8Kill Effects");

    private final CarcerWorldCore plugin;
    private final KillEffectManager killEffectManager;

    public KillEffectGUI(CarcerWorldCore plugin, KillEffectManager killEffectManager) {
        this.plugin = plugin;
        this.killEffectManager = killEffectManager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);

        fill(inventory);

        inventory.setItem(10, createEffectItem(player, KillEffectType.CHERRY_BLOSSOM));
        inventory.setItem(11, createEffectItem(player, KillEffectType.WATER_PILLAR));
        inventory.setItem(12, createEffectItem(player, KillEffectType.HELLFIRE_SKULL));
        inventory.setItem(13, createEffectItem(player, KillEffectType.CASH_EXPLOSION));
        inventory.setItem(14, createEffectItem(player, KillEffectType.VOID_RIFT));
        inventory.setItem(15, createEffectItem(player, KillEffectType.BUTTERFLY_SWARM));
        inventory.setItem(16, createEffectItem(player, KillEffectType.DIVINE_ASCENSION));


        inventory.setItem(18, createBackItem());
        inventory.setItem(22, createEffectItem(player, KillEffectType.NONE));

        player.openInventory(inventory);
    }
    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color("&f&lBack"));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7&l| &fReturn to the Cosmetics Menu."));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createEffectItem(Player player, KillEffectType type) {
        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        KillEffectType selected = killEffectManager.getSelectedEffect(player);

        boolean owns = killEffectManager.ownsEffect(player, type);
        boolean isSelected = selected == type;

        meta.setDisplayName(color(type.getDisplayName()));

        List<String> lore = new ArrayList<>();

        addDescription(lore, type);

        if (type != KillEffectType.NONE) {
            lore.add("");
            lore.add(color("&7&l| &fCost: &a" + format(type.getGemCost()) + " Gems"));
        }

        lore.add("");

        if (isSelected) {
            lore.add(color("&a&lCurrently Selected"));
        } else if (owns) {
            lore.add(color("&e&lClick to Select"));
        } else {
            lore.add(color("&c&lLocked"));
            lore.add(color("&7&l| &fClick to Purchase"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private void addDescription(List<String> lore, KillEffectType type) {
        switch (type) {
            case NONE -> lore.add(color("&7&l| &fDisable your current Kill Effect."));

            case CHERRY_BLOSSOM -> {
                lore.add(color("&7&l| &fA burst of cherry petals"));
                lore.add(color("&7&l| &fblooms from defeated enemies."));
            }

            case WATER_PILLAR -> {
                lore.add(color("&7&l| &fLaunch a powerful pillar of water"));
                lore.add(color("&7&l| &ffrom your defeated enemy."));
            }

            case HELLFIRE_SKULL -> {
                lore.add(color("&7&l| &fSummon a geyser of hellfire"));
                lore.add(color("&7&l| &ffollowed by a demonic smoke skull."));
            }

            case CASH_EXPLOSION -> {
                lore.add(color("&7&l| &fExplode defeated enemies"));
                lore.add(color("&7&l| &finto a burst of wealth."));
            }

            case VOID_RIFT -> {
                lore.add(color("&7&l| &fRip open a temporary rift"));
                lore.add(color("&7&l| &finto the void."));
            }

            case BUTTERFLY_SWARM -> {
                lore.add(color("&7&l| &fRelease a graceful swarm"));
                lore.add(color("&7&l| &fof glowing butterflies."));
            }

            case DIVINE_ASCENSION -> {
                lore.add(color("&7&l| &fSummon a towering beam"));
                lore.add(color("&7&l| &fof divine energy."));
            }
        }
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

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
