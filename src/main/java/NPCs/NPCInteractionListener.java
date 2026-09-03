package NPCs;

import Quests.QuestManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCInteractionListener implements Listener {

    private final NPCManager npcManager;
    private final QuestManager questManager;

    public NPCInteractionListener(NPCManager npcManager, QuestManager questManager) {
        this.npcManager = npcManager;
        this.questManager = questManager;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int citizensId = event.getNPC().getId();

        CarcerNPC npc = npcManager.getNPC(citizensId);
        if (npc == null) return;

        if (questManager.handleNPCInteraction(player, npc)) return;

        npcManager.startDialogue(player, npc);
    }
}
