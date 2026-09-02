package NPCs;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCInteractionListener implements Listener {

    private final NPCManager npcManager;

    public NPCInteractionListener(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int citizensId = event.getNPC().getId();


        CarcerNPC npc = npcManager.getNPC(citizensId);

        npcManager.startDialogue(player, npc);
    }
}
