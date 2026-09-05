package Armor.Special;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecialArmorAbilityListener implements Listener {

    private final CarcerWorldCore plugin;
    private final SpecialArmorManager specialArmorManager;
    private final BlackthornAbility blackthornAbility;
    private final Map<UUID, Long> blackthornCooldowns = new HashMap<>();

    public SpecialArmorAbilityListener(CarcerWorldCore plugin, SpecialArmorManager specialArmorManager, BlackthornAbility blackthornAbility) {
        this.plugin = plugin;
        this.specialArmorManager = specialArmorManager;
        this.blackthornAbility = blackthornAbility;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (!player.isSneaking()) return;
        if (!plugin.getWeaponManager().isCarcerWeapon(player.getInventory().getItemInMainHand())) return;
        if (!specialArmorManager.hasFullSet(player, SpecialArmorSet.BLACKTHORN)) return;

        event.setCancelled(true);

        long remaining = getRemainingCooldown(player);

        if (remaining > 0) {
            player.sendMessage("§2§lBLACKTHORN §7§l| §fThornstorm is on cooldown for §a" + remaining + "s§f.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 0.8f);
            return;
        }

        blackthornCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage("§2§lBLACKTHORN §7§l| §fYou cast §a§lThornstorm§f!");
        blackthornAbility.cast(player);
    }

    private long getRemainingCooldown(Player player) {
        Long lastUsed = blackthornCooldowns.get(player.getUniqueId());
        if (lastUsed == null) return 0;

        long elapsed = System.currentTimeMillis() - lastUsed;
        long remaining = blackthornAbility.getCooldown() - elapsed;

        if (remaining <= 0) {
            blackthornCooldowns.remove(player.getUniqueId());
            return 0;
        }

        return (long) Math.ceil(remaining / 1000.0);
    }
}
