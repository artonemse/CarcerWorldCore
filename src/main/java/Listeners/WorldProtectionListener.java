package Listeners;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WorldProtectionListener implements Listener {

    private static final String PROTECTED_WORLD = "Seratari";
    private static final String ADMIN_PERMISSION = "carcer.admin";

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase(PROTECTED_WORLD)) return;
        if (event.getPlayer().hasPermission(ADMIN_PERMISSION)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase(PROTECTED_WORLD)) return;
        if (event.getPlayer().hasPermission(ADMIN_PERMISSION)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper)) return;
        if (!event.getEntity().getWorld().getName().equalsIgnoreCase(PROTECTED_WORLD)) return;

        event.blockList().clear();
    }
}