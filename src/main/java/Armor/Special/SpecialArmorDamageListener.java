package Armor.Special;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class SpecialArmorDamageListener implements Listener {

    private final NamespacedKey blackthornFangKey;

    public SpecialArmorDamageListener(CarcerWorldCore plugin) {
        this.blackthornFangKey = new NamespacedKey(plugin, "blackthorn_fang");
    }

    @EventHandler
    public void onFangDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof EvokerFangs fang)) return;

        if (!fang.getPersistentDataContainer().has(blackthornFangKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);
    }
}
