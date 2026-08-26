package Skills;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SkillsGUIListener implements Listener {

    private final SkillManager skillManager;
    private final SkillsGUI skillsGUI;

    public SkillsGUIListener(
            SkillManager skillManager,
            SkillsGUI skillsGUI
    ) {
        this.skillManager = skillManager;
        this.skillsGUI = skillsGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView()
                .getTitle()
                .equals(SkillsGUI.TITLE)) {

            return;
        }

        if (!(event.getWhoClicked()
                instanceof Player player)) {

            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        SkillType type = switch (slot) {
            case 11 -> SkillType.STRENGTH;
            case 13 -> SkillType.HEALTH;
            case 15 -> SkillType.KNOWLEDGE;

            default -> null;
        };

        if (type == null) {
            return;
        }

        skillManager.upgradeSkill(
                player,
                type
        );

        skillsGUI.open(player);
    }
}
