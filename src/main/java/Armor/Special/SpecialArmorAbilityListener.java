package Armor.Special;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecialArmorAbilityListener implements Listener {

    private final CarcerWorldCore plugin;
    private final SpecialArmorManager specialArmorManager;
    private final BlackthornAbility blackthornAbility;
    private final GravebornAbility gravebornAbility;
    private final BlacktideAbility blacktideAbility;
    private final RoyalGuardAbility royalGuardAbility;
    private final GoblinSlayerAbility goblinSlayerAbility;
    private final TidecallerAbility tidecallerAbility;
    private final SanctumAbility sanctumAbility;
    private final Map<SpecialArmorSet, Map<UUID, Long>> cooldowns = new EnumMap<>(SpecialArmorSet.class);

    public SpecialArmorAbilityListener(CarcerWorldCore plugin, SpecialArmorManager specialArmorManager, BlackthornAbility blackthornAbility, GravebornAbility gravebornAbility, BlacktideAbility blacktideAbility, RoyalGuardAbility royalGuardAbility, GoblinSlayerAbility goblinSlayerAbility, TidecallerAbility tidecallerAbility, SanctumAbility sanctumAbility) {
        this.plugin = plugin;
        this.specialArmorManager = specialArmorManager;
        this.blackthornAbility = blackthornAbility;
        this.gravebornAbility = gravebornAbility;
        this.blacktideAbility = blacktideAbility;
        this.royalGuardAbility = royalGuardAbility;
        this.goblinSlayerAbility = goblinSlayerAbility;
        this.tidecallerAbility = tidecallerAbility;
        this.sanctumAbility = sanctumAbility;

        for (SpecialArmorSet set : SpecialArmorSet.values()) cooldowns.put(set, new HashMap<>());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (!player.isSneaking()) return;
        if (!plugin.getWeaponManager().isCarcerWeapon(player.getInventory().getItemInMainHand())) return;

        SpecialArmorSet set = specialArmorManager.getFullSet(player);

        if (set == null) return;

        event.setCancelled(true);

        long remaining = getRemainingCooldown(player, set);

        if (remaining > 0) {
            player.sendMessage(color(set.getDisplayName() + " &7&l| &f" + set.getAbilityName() + " is on cooldown for " + set.getAccentColor() + remaining + "s&f."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 0.8f);
            return;
        }

        cooldowns.get(set).put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(color(set.getDisplayName() + " &7&l| &fYou cast " + set.getAccentColor() + "&l" + set.getAbilityName() + "&f!"));

        castAbility(player, set);
    }

    private void castAbility(Player player, SpecialArmorSet set) {
        switch (set) {
            case BLACKTHORN -> blackthornAbility.cast(player);
            case GRAVEBORN -> gravebornAbility.cast(player);
            case BLACKTIDE -> blacktideAbility.cast(player);
            case ROYAL_GUARD -> royalGuardAbility.cast(player);
            case GOBLIN_SLAYER -> goblinSlayerAbility.cast(player);
            case TIDECALLER -> tidecallerAbility.cast(player);
            case SANCTUM -> sanctumAbility.cast(player);
        }
    }

    private long getRemainingCooldown(Player player, SpecialArmorSet set) {
        Long lastUsed = cooldowns.get(set).get(player.getUniqueId());

        if (lastUsed == null) return 0;

        long elapsed = System.currentTimeMillis() - lastUsed;
        long cooldown = set.getAbilityCooldown() * 1000L;
        long remaining = cooldown - elapsed;

        if (remaining <= 0) {
            cooldowns.get(set).remove(player.getUniqueId());
            return 0;
        }

        return (long) Math.ceil(remaining / 1000.0);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}