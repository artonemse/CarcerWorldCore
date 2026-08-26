package Weapons;

import PlayerData.PlayerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;


public class WeaponListener implements Listener {

    private final CarcerWorldCore plugin;
    private final WeaponManager weaponManager;
    private final WeaponMenu weaponMenu;
    private final NamespacedKey cleaveKey;

    public WeaponListener(CarcerWorldCore plugin, WeaponManager weaponManager, WeaponMenu weaponMenu) {
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.weaponMenu = weaponMenu;

        this.cleaveKey =
                new NamespacedKey(
                        plugin,
                        "cleave_damage"
                );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        weaponManager.giveOrUpdateWeapon(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity mob)) {
            return;
        }

        // Prevent cleave damage from recursively
        // triggering the enchant pipeline.
        if (mob.getPersistentDataContainer().has(
                cleaveKey,
                PersistentDataType.BYTE
        )) {
            mob.getPersistentDataContainer()
                    .remove(cleaveKey);

            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (mob instanceof Player) {
            return;
        }

        ItemStack item =
                player.getInventory()
                        .getItemInMainHand();

        if (!weaponManager.isCarcerWeapon(item)) {
            return;
        }

        int weaponLevel =
                plugin.getPlayerDataManager()
                        .getPlayerData(player)
                        .getWeaponLevel();

        // ================================
        // BASE DAMAGE
        // ================================

        double damage =
                weaponManager.getDamage(player);

        // ================================
        // SHARPNESS
        // ================================

        damage += plugin.getEnchantManager()
                .getSharpnessBonus(player);

        // ================================
        // STRENGTH
        // ================================

        damage = plugin.getSkillManager()
                .applyStrength(
                        player,
                        damage
                );

        // ================================
        // CRITICAL STRIKE
        // ================================

        if (plugin.getEnchantManager()
                .rollCriticalStrike(player)) {

            damage *= 3.0;
        }

        // ================================
        // DOUBLE STRIKE
        // ================================

        if (plugin.getEnchantManager()
                .rollDoubleStrike(player)) {

            damage *= 2.0;
        }

        // ================================
        // EXECUTE
        // ================================

        AttributeInstance maxHealthAttribute =
                mob.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (maxHealthAttribute != null) {

            double maxHealth =
                    maxHealthAttribute.getValue();

            double healthAfterHit =
                    Math.max(
                            0,
                            mob.getHealth() - damage
                    );

            double healthPercentAfterHit =
                    healthAfterHit / maxHealth;

            if (healthPercentAfterHit <= 0.30
                    && healthAfterHit > 0
                    && plugin.getEnchantManager()
                    .rollExecute(player)) {

                damage = mob.getHealth() + 1000;
            }
        }

        // Apply primary hit
        event.setDamage(damage);

        // ================================
        // CLEAVE
        // ================================

        if (plugin.getEnchantManager()
                .rollCleave(player)) {

            double cleaveDamage =
                    damage * 0.50;

            for (Entity nearby :
                    mob.getNearbyEntities(
                            3,
                            3,
                            3
                    )) {

                if (!(nearby
                        instanceof LivingEntity nearbyMob)) {
                    continue;
                }

                if (nearbyMob instanceof Player) {
                    continue;
                }

                if (nearbyMob.equals(mob)) {
                    continue;
                }

                nearbyMob.getPersistentDataContainer()
                        .set(
                                cleaveKey,
                                PersistentDataType.BYTE,
                                (byte) 1
                        );

                nearbyMob.damage(
                        cleaveDamage,
                        player
                );
            }
        }
    }

    @EventHandler
    public void onWeaponRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = player.getInventory()
                .getItemInMainHand();

        if (!weaponManager.isCarcerWeapon(item)) {
            return;
        }

        event.setCancelled(true);

        weaponMenu.open(player);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (weaponManager.isCarcerWeapon(
                event.getItemDrop().getItemStack()
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (weaponManager.isCarcerWeapon(current)
                || weaponManager.isCarcerWeapon(cursor)) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack cursor = event.getOldCursor();

        if (weaponManager.isCarcerWeapon(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreativeClick(InventoryCreativeEvent event) {
        ItemStack cursor = event.getCursor();

        if (weaponManager.isCarcerWeapon(cursor)) {
            event.setCancelled(true);
            event.setCursor(new ItemStack(Material.AIR));
        }
    }
}