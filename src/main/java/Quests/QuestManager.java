package Quests;

import NPCs.CarcerNPC;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestManager {

    private final CarcerWorldCore plugin;
    private final Map<String, Quest> quests = new HashMap<>();
    private final Map<UUID, Map<String, PlayerQuest>> playerQuests = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public QuestManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        setup();
        registerQuests();
        loadPlayerQuests();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        file = new File(plugin.getDataFolder(), "quests.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    private void registerQuests() {
        registerQuest(new Quest("clearing_the_road", "Clearing the Road", "warden_garrick", QuestObjectiveType.KILL_HOSTILE_MOBS, 5, 250));
    }

    private void registerQuest(Quest quest) {
        quests.put(quest.getId().toLowerCase(), quest);
    }

    public Quest getQuest(String id) {
        if (id == null) return null;
        return quests.get(id.toLowerCase());
    }

    public PlayerQuest getPlayerQuest(Player player, String questId) {
        Map<String, PlayerQuest> quests = playerQuests.get(player.getUniqueId());
        if (quests == null) return null;
        return quests.get(questId.toLowerCase());
    }

    public boolean handleNPCInteraction(Player player, CarcerNPC npc) {
        for (Quest quest : quests.values()) {
            if (!quest.getNpcId().equalsIgnoreCase(npc.getId())) continue;

            handleQuestInteraction(player, quest);
            return true;
        }

        return false;
    }

    private void handleQuestInteraction(Player player, Quest quest) {
        PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());

        if (playerQuest == null) {
            startQuest(player, quest);
            return;
        }

        if (playerQuest.getState() == QuestState.ACTIVE) {
            sendProgressDialogue(player, quest, playerQuest);
            return;
        }

        if (playerQuest.getState() == QuestState.READY_TO_TURN_IN) {
            completeQuest(player, quest, playerQuest);
            return;
        }

        if (playerQuest.getState() == QuestState.COMPLETED) sendCompletedDialogue(player);
    }

    private void startQuest(Player player, Quest quest) {
        PlayerQuest playerQuest = new PlayerQuest(quest.getId(), QuestState.ACTIVE, 0);

        playerQuests.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>()).put(quest.getId().toLowerCase(), playerQuest);

        player.sendMessage("");
        player.sendMessage(color("&8[&b&lWarden Garrick&8] &fCreatures have been gathering beyond the village."));
        player.sendMessage(color("&8[&b&lWarden Garrick&8] &fThin their numbers. Kill &c5 hostile mobs &fand report back."));
        player.sendMessage("");
        player.sendMessage(color("&6&lQUEST STARTED &8» &f" + quest.getName()));
        player.sendMessage(color("&7Hostile Mobs Killed: &f0&7/&f" + quest.getRequiredAmount()));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);

        savePlayer(player.getUniqueId());
    }

    private void sendProgressDialogue(Player player, Quest quest, PlayerQuest playerQuest) {
        player.sendMessage(color("&8[&b&lWarden Garrick&8] &fYou've dealt with &c" + playerQuest.getProgress() + "&f/&c" + quest.getRequiredAmount() + " &fof them. Keep moving."));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.0f);
    }

    private void sendCompletedDialogue(Player player) {
        player.sendMessage(color("&8[&b&lWarden Garrick&8] &fThe roads are quieter because of your work."));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.0f);
    }

    public void addKillProgress(Player player) {
        Map<String, PlayerQuest> activeQuests = playerQuests.get(player.getUniqueId());
        if (activeQuests == null) return;

        for (PlayerQuest playerQuest : activeQuests.values()) {
            if (playerQuest.getState() != QuestState.ACTIVE) continue;

            Quest quest = getQuest(playerQuest.getQuestId());
            if (quest == null) continue;
            if (quest.getObjectiveType() != QuestObjectiveType.KILL_HOSTILE_MOBS) continue;

            addProgress(player, quest, playerQuest);
        }
    }

    private void addProgress(Player player, Quest quest, PlayerQuest playerQuest) {
        playerQuest.addProgress(1);

        if (playerQuest.getProgress() > quest.getRequiredAmount()) playerQuest.setProgress(quest.getRequiredAmount());

        player.sendMessage(color("&6&lQUEST &8» &f" + quest.getName() + " &8- &e" + playerQuest.getProgress() + "&7/&e" + quest.getRequiredAmount()));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.4f);

        if (playerQuest.getProgress() >= quest.getRequiredAmount()) {
            playerQuest.setState(QuestState.READY_TO_TURN_IN);

            player.sendMessage("");
            player.sendMessage(color("&a&lQUEST OBJECTIVE COMPLETE"));
            player.sendMessage(color("&7Return to &bWarden Garrick &7to claim your reward."));
            player.sendMessage("");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
        }

        savePlayer(player.getUniqueId());
    }

    private void completeQuest(Player player, Quest quest, PlayerQuest playerQuest) {
        plugin.getSoulManager().addSouls(player, quest.getSoulReward());

        playerQuest.setState(QuestState.COMPLETED);

        player.sendMessage("");
        player.sendMessage(color("&8[&b&lWarden Garrick&8] &fGood work. That's five fewer threats near Blackthorn."));
        player.sendMessage("");
        player.sendMessage(color("&a&lQUEST COMPLETED &8» &f" + quest.getName()));
        player.sendMessage(color("&6&lREWARD &8» &b+" + quest.getSoulReward() + " Souls"));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);

        savePlayer(player.getUniqueId());
    }

    private void loadPlayerQuests() {
        playerQuests.clear();

        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String uuidString : playersSection.getKeys(false)) {
            UUID uuid;

            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            ConfigurationSection questSection = config.getConfigurationSection("players." + uuidString + ".quests");
            if (questSection == null) continue;

            Map<String, PlayerQuest> loadedQuests = new HashMap<>();

            for (String questId : questSection.getKeys(false)) {
                String path = "players." + uuidString + ".quests." + questId;

                String stateString = config.getString(path + ".state", QuestState.ACTIVE.name());
                int progress = config.getInt(path + ".progress", 0);

                QuestState state;

                try {
                    state = QuestState.valueOf(stateString);
                } catch (IllegalArgumentException exception) {
                    state = QuestState.ACTIVE;
                }

                loadedQuests.put(questId.toLowerCase(), new PlayerQuest(questId, state, progress));
            }

            playerQuests.put(uuid, loadedQuests);
        }
    }

    public void savePlayer(UUID uuid) {
        String basePath = "players." + uuid + ".quests";

        config.set(basePath, null);

        Map<String, PlayerQuest> quests = playerQuests.get(uuid);

        if (quests != null) {
            for (PlayerQuest playerQuest : quests.values()) {
                String path = basePath + "." + playerQuest.getQuestId();

                config.set(path + ".state", playerQuest.getState().name());
                config.set(path + ".progress", playerQuest.getProgress());
            }
        }

        saveFile();
    }

    public void saveAll() {
        for (UUID uuid : playerQuests.keySet()) savePlayer(uuid);
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
