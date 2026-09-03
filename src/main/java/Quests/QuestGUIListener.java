package Quests;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class QuestGUIListener implements Listener {

    private final QuestGUI questGUI;

    public QuestGUIListener(QuestGUI questGUI) {
        this.questGUI = questGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Active Quests")) return;

        event.setCancelled(true);

        if (event.getRawSlot() == 45) questGUI.open(player, questGUI.getPage(player) - 1);
        if (event.getRawSlot() == 49) player.closeInventory();
        if (event.getRawSlot() == 53) questGUI.open(player, questGUI.getPage(player) + 1);
    }
}
