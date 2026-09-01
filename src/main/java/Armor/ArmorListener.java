package Armor;

import Armor.Generic.ArmorStat;
import Armor.Generic.GenericArmorGenerator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class ArmorListener implements Listener {

    private final CarcerWorldCore plugin;
    private final ArmorManager armorManager;
    private final GenericArmorGenerator generator;
    private final CombatHealthBarManager healthBarManager;

    public ArmorListener(
            CarcerWorldCore plugin,
            ArmorManager armorManager,
            GenericArmorGenerator generator,
            CombatHealthBarManager healthBarManager
    ) {
        this.plugin = plugin;
        this.armorManager = armorManager;
        this.generator = generator;
        this.healthBarManager = healthBarManager;
    }

    @EventHandler
    public void onJoin(
            PlayerJoinEvent event
    ) {

        scheduleRefresh(
                event.getPlayer(),
                2L
        );
    }

    @EventHandler
    public void onRespawn(
            PlayerRespawnEvent event
    ) {

        scheduleRefresh(
                event.getPlayer(),
                2L
        );
    }

    @EventHandler
    public void onWorldChange(
            PlayerChangedWorldEvent event
    ) {

        scheduleRefresh(
                event.getPlayer(),
                1L
        );
    }

    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        /*
         * Wait until AFTER Bukkit finishes
         * changing the inventory.
         */
        scheduleRefresh(
                player,
                1L
        );
    }

    @EventHandler
    public void onInventoryDrag(
            InventoryDragEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        scheduleRefresh(
                player,
                1L
        );
    }

    @EventHandler
    public void onInteract(
            PlayerInteractEvent event
    ) {

        if (
                !generator.isGenericArmor(
                        event.getItem()
                )
        ) {
            return;
        }

        scheduleRefresh(
                event.getPlayer(),
                1L
        );
    }

    @EventHandler
    public void onHealing(
            EntityRegainHealthEvent event
    ) {

        if (!(event.getEntity()
                instanceof Player player)) {
            return;
        }

        double multiplier =
                armorManager.getMultiplier(
                        player,
                        ArmorStat.HEALING
                );

        event.setAmount(
                event.getAmount()
                        * multiplier
        );
    }

    @EventHandler
    public void onQuit(
            PlayerQuitEvent event
    ) {

        healthBarManager.remove(
                event.getPlayer()
        );
    }

    private void scheduleRefresh(
            Player player,
            long delay
    ) {

        Bukkit.getScheduler()
                .runTaskLater(
                        plugin,
                        () ->
                                armorManager.refreshPlayer(
                                        player
                                ),
                        delay
                );
    }
}
