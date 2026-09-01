package MobScaling;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class MobHealthBarManager {

    private final CarcerWorldCore plugin;
    private final NamespacedKey mobNameKey;

    public MobHealthBarManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.mobNameKey = new NamespacedKey(plugin, "mob_name");
    }

    public void updateHealthBar(LivingEntity mob) {
        if (mob instanceof Player) return;

        AttributeInstance maxHealthAttribute = mob.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) return;

        int level = plugin.getMobScalingManager().getScalingLevel(mob);
        double health = Math.max(0, mob.getHealth());
        String mobName = getMobName(mob);

        mob.setCustomName(color("&7Level &f" + level + " &7" + mobName + " &8[&c" + formatHealth(health) + "❤&8]"));
        mob.setCustomNameVisible(true);
    }

    private String getMobName(LivingEntity mob) {
        String storedName = mob.getPersistentDataContainer().get(mobNameKey, PersistentDataType.STRING);
        if (storedName != null) return storedName;

        String customName = mob.getCustomName();

        if (customName != null && !customName.isBlank()) {
            String stripped = ChatColor.stripColor(customName);

            if (stripped != null && !stripped.startsWith("Level ")) {
                mob.getPersistentDataContainer().set(mobNameKey, PersistentDataType.STRING, customName);
                return customName;
            }
        }

        String name = formatEntityName(mob);
        mob.getPersistentDataContainer().set(mobNameKey, PersistentDataType.STRING, name);

        return name;
    }

    private String formatEntityName(LivingEntity mob) {
        String name = mob.getType().name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();

        for (String word : name.split(" ")) {
            if (word.isEmpty()) continue;
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return result.toString().trim();
    }

    private String formatHealth(double health) {
        if (health == Math.floor(health)) return String.valueOf((long) health);
        return String.format("%.1f", health);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}