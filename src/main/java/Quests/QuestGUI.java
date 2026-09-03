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

        List<Quest> quests = questManager.getActiveQuests(player);

        int totalPages = Math.max(1, (int) Math.ceil(quests.size() / 45.0));
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));

        pages.put(player.getUniqueId(), page);

        Inventory inventory = Bukkit.createInventory(null, 54, color("&8Active Quests &7(" + (page + 1) + "/" + totalPages + ")"));

        int start = page * 45;
        int end = Math.min(start + 45, quests.size());

        for (int index = start; index < end; index++) {
            Quest quest = quests.get(index);
            inventory.setItem(index - start, createQuestItem(player, quest));
        }

        if (quests.isEmpty()) inventory.setItem(22, createItem(Material.PAPER, "&f&lNo Active Quests", List.of("&7&l| &fYou currently have no active quests.")));

        if (page > 0) inventory.setItem(45, createItem(Material.ARROW, "&f&lPrevious Page", List.of("&7&l| &fView the previous page.")));
        inventory.setItem(49, createItem(Material.BARRIER, "&c&lClose", List.of("&7&l| &fClose the quest journal.")));
        if (page < totalPages - 1) inventory.setItem(53, createItem(Material.ARROW, "&f&lNext Page", List.of("&7&l| &fView the next page.")));

        player.openInventory(inventory);
    }

    private ItemStack createQuestItem(Player player, Quest quest) {
        Material material = quest.getType() == QuestType.MAIN ? Material.NETHER_STAR : Material.WRITABLE_BOOK;

        List<String> lore = new ArrayList<>();

        lore.add("&7&l| &fType: " + (quest.getType() == QuestType.MAIN ? "&6Main Quest" : "&bNPC Quest"));
        lore.add("");

        for (QuestObjective objective : quest.getObjectives()) lore.add(questManager.getObjectiveProgressLine(player, quest, objective));

        lore.add("");

        if (quest.getReward().getSouls() > 0) lore.add("&7&l| &fReward: &b" + format(quest.getReward().getSouls()) + " Souls");
        if (quest.getReward().getGems() > 0) lore.add("&7&l| &fReward: &d" + format(quest.getReward().getGems()) + " Gems");

        PlayerQuest playerQuest = questManager.getPlayerQuest(player, quest.getId());

        if (playerQuest != null && playerQuest.getState() == QuestState.READY_TO_TURN_IN) {
            lore.add("");
            lore.add("&7&l| &aReady to turn in!");
        }

        return createItem(material, "&f&l" + quest.getName(), lore);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

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
