package Armor;

import Armor.Generic.GenericArmorGenerator;
import Quests.QuestObjectiveType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.concurrent.ThreadLocalRandom;

public class ArmorDropListener implements Listener {

    /*
     * 0.015 = 1.5%
     */
    private static final double BASE_DROP_CHANCE =
            0.015;

    private final ArmorManager armorManager;
    private final GenericArmorGenerator generator;
    private final CarcerWorldCore plugin;

    public ArmorDropListener(CarcerWorldCore plugin, ArmorManager armorManager, GenericArmorGenerator generator) {
        this.plugin = plugin;
        this.armorManager = armorManager;
        this.generator = generator;
    }

    @EventHandler
    public void onMobDeath(
            EntityDeathEvent event
    ) {

        LivingEntity entity =
                event.getEntity();

        if (entity instanceof Player) {
            return;
        }

        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        double lootMultiplier = armorManager.getLootMultiplier(killer);
        double chance = BASE_DROP_CHANCE * lootMultiplier;
        chance = Math.min(1.0, chance);

        if (ThreadLocalRandom.current().nextDouble() > chance) {
            return;
        }

        ItemStack armor = generator.generateRandomArmor();
        entity.getWorld().dropItemNaturally(entity.getLocation(), armor);

        if (plugin.getQuestManager() != null) plugin.getQuestManager().handleProgress(killer, QuestObjectiveType.OBTAIN_ARMOR, 1);
    }
}
