package Quests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestGUI {

    private static final int[] MAIN_SLOTS = {
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30,
            36, 37, 38, 39
    };

    private static final int[] NPC_SLOTS = {
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35,
            41, 42, 43, 44
    };

    private final QuestManager questManager;
    private final Map<UUID, Integer> pages = new HashMap<>();

    public QuestGUI(QuestManager questManager) {
        this.questManager = questManager;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int requestedPage) {
        questManager.ensureMainQuests(player);

        List<Quest> activeQuests = questManager.getActiveQuests(player);
        List<Quest> mainQuests = new ArrayList<>();
        List<Quest> npcQuests = new ArrayList<>();

        for (Quest quest : activeQuests) {
            if (quest.getType() == QuestType.MAIN) mainQuests.add(quest);
            if (quest.getType() == QuestType.NPC) npcQuests.add(quest);
        }

        int mainPages = Math.max(1, (int) Math.ceil(mainQuests.size() / (double) MAIN_SLOTS.length));
        int npcPages = Math.max(1, (int) Math.ceil(npcQuests.size() / (double) NPC_SLOTS.length));
        int totalPages = Math.max(mainPages, npcPages);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));

        pages.put(player.getUniqueId(), page);

        Inventory inventory = Bukkit.createInventory(null, 54, color("&8Quest Journal &7(" + (page + 1) + "/" + totalPages + ")"));

        fillBackground(inventory);
        createHeaders(inventory, mainQuests.size(), npcQuests.size(), page, totalPages);
        placeMainQuests(inventory, player, mainQuests, page);
        placeNPCQuests(inventory, player, npcQuests, page);
        createNavigation(inventory, page, totalPages);

        player.openInventory(inventory);
    }

    private void createHeaders(Inventory inventory, int mainCount, int npcCount, int page, int totalPages) {
        inventory.setItem(2, createItem(
                Material.NETHER_STAR,
                "&6&lMAIN QUESTS",
                List.of(
                        "&7&l| &fStory and progression objectives.",
                        "&7&l| &fActive: &e" + mainCount
                )
        ));

        inventory.setItem(4, createItem(
                Material.WRITABLE_BOOK,
                "&f&lQUEST JOURNAL",
                List.of(
                        "&7&l| &fTrack your current objectives.",
                        "&7&l| &fPage: &e" + (page + 1) + "&7/&e" + totalPages
                )
        ));

        inventory.setItem(6, createItem(
                Material.EMERALD,
                "&b&lNPC QUESTS",
                List.of(
                        "&7&l| &fQuests given by characters.",
                        "&7&l| &fActive: &e" + npcCount
                )
        ));
    }

    private void placeMainQuests(Inventory inventory, Player player, List<Quest> quests, int page) {
        int start = page * MAIN_SLOTS.length;
        int end = Math.min(start + MAIN_SLOTS.length, quests.size());

        if (start >= quests.size()) {
            inventory.setItem(20, createItem(
                    Material.PAPER,
                    "&7&lNo Main Quests",
                    List.of("&7&l| &fNo main quests on this page.")
            ));
            return;
        }

        int slotIndex = 0;

        for (int index = start; index < end; index++) {
            Quest quest = quests.get(index);
            inventory.setItem(MAIN_SLOTS[slotIndex], createQuestItem(player, quest));
            slotIndex++;
        }
    }

    private void placeNPCQuests(Inventory inventory, Player player, List<Quest> quests, int page) {
        int start = page * NPC_SLOTS.length;
        int end = Math.min(start + NPC_SLOTS.length, quests.size());

        if (start >= quests.size()) {
            inventory.setItem(24, createItem(
                    Material.PAPER,
                    "&7&lNo NPC Quests",
                    List.of("&7&l| &fNo NPC quests on this page.")
            ));
            return;
        }

        int slotIndex = 0;

        for (int index = start; index < end; index++) {
            Quest quest = quests.get(index);
            inventory.setItem(NPC_SLOTS[slotIndex], createQuestItem(player, quest));
            slotIndex++;
        }
    }

    private ItemStack createQuestItem(Player player, Quest quest) {
        PlayerQuest playerQuest = questManager.getPlayerQuest(player, quest.getId());
        boolean ready = playerQuest != null && playerQuest.getState() == QuestState.READY_TO_TURN_IN;

        Material material;

        if (ready) {
            material = Material.LIME_DYE;
        } else if (quest.getType() == QuestType.MAIN) {
            material = Material.NETHER_STAR;
        } else {
            material = Material.WRITABLE_BOOK;
        }

        List<String> lore = new ArrayList<>();

        if (quest.getType() == QuestType.MAIN) {
            lore.add("&7&l| &fType: &6Main Quest");
        } else {
            lore.add("&7&l| &fType: &bNPC Quest");
        }

        lore.add("");

        for (QuestObjective objective : quest.getObjectives()) {
            lore.add(questManager.getObjectiveProgressLine(player, quest, objective));
        }

        lore.add("");
        lore.add("&f&lREWARDS");

        boolean hasReward = false;

        if (quest.getReward().getSouls() > 0) {
            lore.add("&7&l| &b" + format(quest.getReward().getSouls()) + " Souls");
            hasReward = true;
        }

        if (quest.getReward().getGems() > 0) {
            lore.add("&7&l| &d" + format(quest.getReward().getGems()) + " Gems");
            hasReward = true;
        }

        if (quest.getReward().hasSpecialArmorReward()) {
            String armorName = quest.getReward().getSpecialArmorSet().getDisplayName() + " " + quest.getReward().getSpecialArmorSlot().getDisplayName();
            lore.add("&7&l| " + armorName);
            hasReward = true;
        }

        if (!hasReward) lore.add("&7&l| &fNone");

        if (ready) {
            lore.add("");
            lore.add("&a&lREADY TO TURN IN");
            lore.add("&7&l| &fReturn to the quest NPC.");
        }

        return createItem(material, "&f&l" + quest.getName(), lore);
    }

    private void createNavigation(Inventory inventory, int page, int totalPages) {
        if (page > 0) {
            inventory.setItem(45, createItem(
                    Material.ARROW,
                    "&f&lPrevious Page",
                    List.of("&7&l| &fGo to page &e" + page + "&f.")
            ));
        }

        inventory.setItem(49, createItem(
                Material.BARRIER,
                "&c&lClose",
                List.of("&7&l| &fClose the quest journal.")
        ));

        if (page < totalPages - 1) {
            inventory.setItem(53, createItem(
                    Material.ARROW,
                    "&f&lNext Page",
                    List.of("&7&l| &fGo to page &e" + (page + 2) + "&f.")
            ));
        }
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        ItemStack divider = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());

        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, filler);

        inventory.setItem(13, divider);
        inventory.setItem(22, divider);
        inventory.setItem(31, divider);
        inventory.setItem(40, divider);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color(name));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) coloredLore.add(color(line));

        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    public int getPage(Player player) {
        return pages.getOrDefault(player.getUniqueId(), 0);
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}