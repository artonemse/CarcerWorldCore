package Skills;

import PlayerData.PlayerData;
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

public class SkillsGUI {

    public static final String TITLE =
            ChatColor.translateAlternateColorCodes(
                    '&',
                    "&8Skills"
            );

    private final CarcerWorldCore plugin;
    private final SkillManager skillManager;

    public SkillsGUI(
            CarcerWorldCore plugin,
            SkillManager skillManager
    ) {
        this.plugin = plugin;
        this.skillManager = skillManager;
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
                11,
                createSkillItem(
                        player,
                        SkillType.STRENGTH
                )
        );

        inventory.setItem(
                13,
                createSkillItem(
                        player,
                        SkillType.HEALTH
                )
        );

        inventory.setItem(
                15,
                createSkillItem(
                        player,
                        SkillType.KNOWLEDGE
                )
        );

        inventory.setItem(
                22,
                createPointsItem(player)
        );

        player.openInventory(inventory);
    }

    private ItemStack createSkillItem(
            Player player,
            SkillType type
    ) {
        ItemStack item =
                new ItemStack(type.getIcon());

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        int level =
                skillManager.getSkillLevel(
                        player,
                        type
                );

        meta.setDisplayName(
                color(type.getDisplayName())
        );

        List<String> lore = new ArrayList<>();

        switch (type) {

            case STRENGTH -> {
                lore.add(color(
                        "&7&l| &fIncrease your weapon damage."
                ));

                lore.add(color(
                        "&7&l| &fBonus per Level: &c+3%"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Bonus: &c+"
                                + format(
                                level * 3.0
                        )
                                + "%"
                ));
            }

            case HEALTH -> {
                lore.add(color(
                        "&7&l| &fIncrease your maximum health."
                ));

                lore.add(color(
                        "&7&l| &fBonus per Level: &a+1 Health"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Bonus: &a+"
                                + level
                                + " Health"
                ));
            }

            case KNOWLEDGE -> {
                lore.add(color(
                        "&7&l| &fGain additional weapon EXP."
                ));

                lore.add(color(
                        "&7&l| &fBonus per Level: &b+5%"
                ));

                lore.add(color(
                        "&7&l| &fCurrent Bonus: &b+"
                                + format(
                                level * 5.0
                        )
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

        lore.add(color(
                "&7&l| &fCost: &e"
                        + type.getPointCost()
                        + " Skill Point"
                        + (type.getPointCost() == 1 ? "" : "s")
        ));

        lore.add("");

        if (level >= type.getMaxLevel()) {
            lore.add(color(
                    "&a&lMAX LEVEL"
            ));
        } else {
            lore.add(color(
                    "&e&lClick to Upgrade"
            ));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createPointsItem(Player player) {
        PlayerData data = plugin
                .getPlayerDataManager()
                .getPlayerData(player);

        ItemStack item =
                new ItemStack(Material.SUNFLOWER);

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(
                color("&e&lSkill Points")
        );

        List<String> lore = new ArrayList<>();

        lore.add(color(
                "&7&l| &fAvailable Points: &e"
                        + data.getSkillPoints()
        ));

        lore.add(color(
                "&7&l| &fEarn &e3 &fpoints each weapon level."
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

    private String format(double number) {
        if (number == Math.floor(number)) {
            return String.valueOf(
                    (long) number
            );
        }

        return String.format(
                "%.1f",
                number
        );
    }
}