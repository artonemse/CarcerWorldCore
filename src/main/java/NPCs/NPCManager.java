package NPCs;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCManager {

    private final CarcerWorldCore plugin;
    private final Map<String, CarcerNPC> npcsById = new HashMap<>();
    private final Map<Integer, CarcerNPC> npcsByCitizensId = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public NPCManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        setup();
        loadNPCs();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        file = new File(plugin.getDataFolder(), "npcs.yml");

        if (!file.exists()) plugin.saveResource("npcs.yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadNPCs() {
        config = YamlConfiguration.loadConfiguration(file);

        npcsById.clear();
        npcsByCitizensId.clear();

        ConfigurationSection section = config.getConfigurationSection("npcs");

        if (section == null) {
            plugin.getLogger().info("[CarcerWorldCore] Loaded 0 NPCs.");
            return;
        }

        for (String id : section.getKeys(false)) {
            String path = "npcs." + id;

            int citizensId = config.getInt(path + ".citizens-id", -1);
            String name = config.getString(path + ".name", id);
            List<String> dialogue = config.getStringList(path + ".dialogue");

            if (citizensId < 0) {
                plugin.getLogger().warning("[CarcerWorldCore] NPC " + id + " has no valid Citizens ID.");
                continue;
            }

            CarcerNPC npc = new CarcerNPC(id, citizensId, name, dialogue);

            npcsById.put(id.toLowerCase(), npc);
            npcsByCitizensId.put(citizensId, npc);
        }

        plugin.getLogger().info("[CarcerWorldCore] Loaded " + npcsById.size() + " NPCs.");
    }

    public CarcerNPC getNPC(String id) {
        if (id == null) return null;
        return npcsById.get(id.toLowerCase());
    }

    public CarcerNPC getNPC(int citizensId) {
        return npcsByCitizensId.get(citizensId);
    }

    public void startDialogue(Player player, CarcerNPC npc) {
        if (npc == null) return;
        if (npc.getDialogue().isEmpty()) return;

        new BukkitRunnable() {

            private int line = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (line >= npc.getDialogue().size()) {
                    cancel();
                    return;
                }

                String message = npc.getDialogue().get(line);

                player.sendMessage(color("&8[&b&l" + npc.getName() + "&8] &f" + message));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);

                line++;
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}