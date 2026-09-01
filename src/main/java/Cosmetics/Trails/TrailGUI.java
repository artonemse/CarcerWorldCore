package Cosmetics.Trails;

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

public class TrailGUI {

    public static final String TITLE = color("&8Player Trails");

    private final CarcerWorldCore plugin;
    private final TrailManager trailManager;

    public TrailGUI(CarcerWorldCore plugin, TrailManager trailManager) {
        this.plugin = plugin;
        this.trailManager = trailManager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);

        fill(inventory);

        inventory.setItem(10, createTrailItem(player, TrailType.CHERRY_BLOSSOM));
        inventory.setItem(11, createTrailItem(player, TrailType.HELLFIRE));
        inventory.setItem(12, createTrailItem(player, TrailType.SOULMIST));
        inventory.setItem(13, createTrailItem(player, TrailType.VOIDWALKER));
        inventory.setItem(14, createTrailItem(player, TrailType.FROSTBOUND));
        inventory.setItem(15, createTrailItem(player, TrailType.DIVINE));
        inventory.setItem(16, createTrailItem(player, TrailType.ARCANE));

        inventory.setItem(18, createBackItem());
        inventory.setItem(22, createTrailItem(player, TrailType.NONE));

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

    private ItemStack createTrailItem(Player player, TrailType type) {
        ItemStack item = new ItemStack(type.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        TrailType selected = trailManager.getSelectedTrail(player);

        boolean owns = trailManager.ownsTrail(player, type);
        boolean isSelected = selected == type;

        meta.setDisplayName(color(type.getDisplayName()));

        List<String> lore = new ArrayList<>();

        addDescription(lore, type);

        if (type != TrailType.NONE) {
            lore.add("");
            lore.add(color(
                    "&7&l| &fCost: &a"
                            + format(type.getGemCost())
                            + " Gems"
            ));
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

    private void addDescription(List<String> lore, TrailType type) {
        switch (type) {

            case NONE ->
                    lore.add(color(
                            "&7&l| &fDisable your current Player Trail."
                    ));

            case CHERRY_BLOSSOM -> {
                lore.add(color("&7&l| &fA trail of cherry petals"));
                lore.add(color("&7&l| &fblooms behind your movement."));
            }

            case HELLFIRE -> {
                lore.add(color("&7&l| &fLeave a blazing path"));
                lore.add(color("&7&l| &fof hellfire behind you."));
            }

            case SOULMIST -> {
                lore.add(color("&7&l| &fGhostly soul energy"));
                lore.add(color("&7&l| &ffollows every step."));
            }

            case VOIDWALKER -> {
                lore.add(color("&7&l| &fTear through reality"));
                lore.add(color("&7&l| &fwith unstable void energy."));
            }

            case FROSTBOUND -> {
                lore.add(color("&7&l| &fLeave a frozen trail"));
                lore.add(color("&7&l| &fof drifting snowflakes."));
            }

            case DIVINE -> {
                lore.add(color("&7&l| &fRadiant divine energy"));
                lore.add(color("&7&l| &fshimmers in your wake."));
            }

            case ARCANE -> {
                lore.add(color("&7&l| &fAncient magical energy"));
                lore.add(color("&7&l| &fflows behind your movement."));
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

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, filler);
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}