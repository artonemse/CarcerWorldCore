package Quests;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class QuestGUIListener implements Listener {

    private final QuestGUI questGUI;

    public QuestGUIListener(QuestGUI questGUI) {
        this.questGUI = questGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Quest Journal")) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == 45) questGUI.open(player, questGUI.getPage(player) - 1);
        if (slot == 49) player.closeInventory();
        if (slot == 53) questGUI.open(player, questGUI.getPage(player) + 1);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Quest Journal")) return;

        event.setCancelled(true);
    }
}