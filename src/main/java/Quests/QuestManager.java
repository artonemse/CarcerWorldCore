package Quests;

import NPCs.CarcerNPC;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QuestManager {

    private final CarcerWorldCore plugin;
    private final Map<String, Quest> quests;
    private final Map<UUID, Map<String, PlayerQuest>> playerQuests = new HashMap<>();

    private File file;
    private FileConfiguration config;

    public QuestManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.quests = new QuestLoader(plugin).loadQuests();

        setup();
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

    public Quest getQuest(String questId) {
        if (questId == null) return null;
        return quests.get(questId.toLowerCase());
    }

    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests.values());
    }

    public PlayerQuest getPlayerQuest(Player player, String questId) {
        Map<String, PlayerQuest> playerMap = playerQuests.get(player.getUniqueId());
        if (playerMap == null) return null;
        return playerMap.get(questId.toLowerCase());
    }

    public List<Quest> getActiveQuests(Player player) {
        List<Quest> active = new ArrayList<>();
        Map<String, PlayerQuest> playerMap = playerQuests.get(player.getUniqueId());

        if (playerMap == null) return active;

        for (PlayerQuest playerQuest : playerMap.values()) {
            if (playerQuest.getState() == QuestState.COMPLETED) continue;

            Quest quest = getQuest(playerQuest.getQuestId());
            if (quest != null) active.add(quest);
        }

        active.sort(Comparator.comparing(Quest::getType).thenComparing(Quest::getName));
        return active;
    }

    public void ensureMainQuests(Player player) {
        boolean changed;

        do {
            changed = false;

            for (Quest quest : quests.values()) {
                if (quest.getType() != QuestType.MAIN) continue;
                if (getPlayerQuest(player, quest.getId()) != null) continue;
                if (!hasPrerequisite(player, quest)) continue;

                activateQuest(player, quest, false);
                changed = true;
            }
        } while (changed);
    }

    private boolean hasPrerequisite(Player player, Quest quest) {
        String prerequisite = quest.getPrerequisite();

        if (prerequisite == null || prerequisite.isBlank()) return true;

        PlayerQuest prerequisiteQuest = getPlayerQuest(player, prerequisite);
        return prerequisiteQuest != null && prerequisiteQuest.getState() == QuestState.COMPLETED;
    }

    private void activateQuest(Player player, Quest quest, boolean announce) {
        PlayerQuest playerQuest = new PlayerQuest(quest.getId(), QuestState.ACTIVE);

        for (QuestObjective objective : quest.getObjectives()) {
            playerQuest.getObjective(objective.getId());
        }

        syncStateObjectives(player, quest, playerQuest);

        playerQuests.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>()).put(quest.getId().toLowerCase(), playerQuest);

        if (isQuestComplete(quest, playerQuest)) {
            if (quest.getType() == QuestType.MAIN) {
                completeQuest(player, quest, playerQuest);
            } else {
                playerQuest.setState(QuestState.READY_TO_TURN_IN);
                savePlayer(player.getUniqueId());
            }

            return;
        }

        if (announce) {
            sendDialogue(player, quest, quest.getStartDialogue(), playerQuest);

            player.sendMessage("");
            player.sendMessage(color("&6&lQUEST STARTED &8» &f" + quest.getName()));
            player.sendMessage("");

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        }

        savePlayer(player.getUniqueId());
    }

    public boolean handleNPCInteraction(Player player, CarcerNPC npc) {
        Quest readyQuest = findNPCQuest(player, npc, QuestState.READY_TO_TURN_IN);

        if (readyQuest != null) {
            PlayerQuest playerQuest = getPlayerQuest(player, readyQuest.getId());
            sendDialogue(player, readyQuest, readyQuest.getReadyDialogue(), playerQuest);
            completeQuest(player, readyQuest, playerQuest);
            return true;
        }

        Quest activeQuest = findNPCQuest(player, npc, QuestState.ACTIVE);

        if (activeQuest != null) {
            PlayerQuest playerQuest = getPlayerQuest(player, activeQuest.getId());
            sendDialogue(player, activeQuest, activeQuest.getActiveDialogue(), playerQuest);
            return true;
        }

        Quest availableQuest = findAvailableNPCQuest(player, npc);

        if (availableQuest != null) {
            activateQuest(player, availableQuest, true);
            return true;
        }

        Quest finishedQuest = findNPCQuest(player, npc, QuestState.COMPLETED);

        if (finishedQuest != null && !finishedQuest.getFinishedDialogue().isEmpty()) {
            sendDialogue(player, finishedQuest, finishedQuest.getFinishedDialogue(), getPlayerQuest(player, finishedQuest.getId()));
            return true;
        }

        return false;
    }

    private Quest findNPCQuest(Player player, CarcerNPC npc, QuestState state) {
        for (Quest quest : quests.values()) {
            if (quest.getType() != QuestType.NPC) continue;
            if (!quest.getNpcId().equalsIgnoreCase(npc.getId())) continue;

            PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());

            if (playerQuest != null && playerQuest.getState() == state) return quest;
        }

        return null;
    }

    private Quest findAvailableNPCQuest(Player player, CarcerNPC npc) {
        for (Quest quest : quests.values()) {
            if (quest.getType() != QuestType.NPC) continue;
            if (!quest.getNpcId().equalsIgnoreCase(npc.getId())) continue;
            if (getPlayerQuest(player, quest.getId()) != null) continue;
            if (!hasPrerequisite(player, quest)) continue;

            return quest;
        }

        return null;
    }

    public void handleMobKill(Player player, LivingEntity mob) {
        if (!(mob instanceof Monster)) return;

        ensureMainQuests(player);

        for (Quest quest : getActiveQuests(player)) {
            PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());
            if (playerQuest == null || playerQuest.getState() != QuestState.ACTIVE) continue;

            boolean changed = false;

            for (QuestObjective objective : quest.getObjectives()) {
                PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());

                if (isObjectiveComplete(objective, progress)) continue;

                switch (objective.getType()) {
                    case KILL_HOSTILE_MOBS -> {
                        progress.addProgress(1);
                        changed = true;
                    }

                    case KILL_SPECIFIC_MOB -> {
                        if (!matchesMob(mob, objective.getTarget())) continue;

                        progress.addProgress(1);
                        changed = true;
                    }

                    case KILL_UNIQUE_MOBS -> {
                        String mobId = getMobIdentifier(mob);

                        if (!objective.getTargets().isEmpty() && !objective.getTargets().contains(mobId.toLowerCase())) continue;

                        if (progress.addCompletedTarget(mobId)) {
                            progress.setProgress(progress.getCompletedTargets().size());
                            changed = true;
                        }
                    }

                    default -> {
                    }
                }

                clampProgress(objective, progress);
            }

            if (changed) handleQuestProgress(player, quest, playerQuest);
        }
    }

    public void handleLocationVisit(Player player, String locationId) {
        if (locationId == null || locationId.isBlank()) return;

        ensureMainQuests(player);

        for (Quest quest : getActiveQuests(player)) {
            PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());
            if (playerQuest == null || playerQuest.getState() != QuestState.ACTIVE) continue;

            boolean changed = false;

            for (QuestObjective objective : quest.getObjectives()) {
                PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());

                if (isObjectiveComplete(objective, progress)) continue;

                switch (objective.getType()) {
                    case VISIT_LOCATION -> {
                        if (!objective.getTarget().equalsIgnoreCase(locationId)) continue;

                        progress.setProgress(1);
                        changed = true;
                    }

                    case VISIT_UNIQUE_LOCATIONS -> {
                        String normalized = locationId.toLowerCase();

                        if (!objective.getTargets().isEmpty() && !objective.getTargets().contains(normalized)) continue;

                        if (progress.addCompletedTarget(normalized)) {
                            progress.setProgress(progress.getCompletedTargets().size());
                            changed = true;
                        }
                    }

                    default -> {
                    }
                }

                clampProgress(objective, progress);
            }

            if (changed) handleQuestProgress(player, quest, playerQuest);
        }
    }

    private void handleQuestProgress(Player player, Quest quest, PlayerQuest playerQuest) {
        if (!isQuestComplete(quest, playerQuest)) {
            savePlayer(player.getUniqueId());
            return;
        }

        if (quest.getType() == QuestType.MAIN) {
            completeQuest(player, quest, playerQuest);
            return;
        }

        playerQuest.setState(QuestState.READY_TO_TURN_IN);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);

        savePlayer(player.getUniqueId());
    }

    private void syncStateObjectives(Player player, Quest quest, PlayerQuest playerQuest) {
        for (QuestObjective objective : quest.getObjectives()) {
            PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());

            switch (objective.getType()) {
                case REACH_WEAPON_LEVEL -> {
                    int level = plugin.getPlayerDataManager().getPlayerData(player).getWeaponLevel();
                    progress.setProgress(Math.min(level, objective.getEffectiveRequiredAmount()));
                }

                case REACH_ASCENSION -> {
                    int ascension = plugin.getPlayerDataManager().getPlayerData(player).getAscensions();
                    progress.setProgress(Math.min(ascension, objective.getEffectiveRequiredAmount()));
                }

                default -> {
                }
            }
        }
    }

    private void completeQuest(Player player, Quest quest, PlayerQuest playerQuest) {
        if (playerQuest.getState() == QuestState.COMPLETED) return;

        playerQuest.setState(QuestState.COMPLETED);

        giveRewards(player, quest);

        if (quest.getType() == QuestType.NPC) sendDialogue(player, quest, quest.getCompleteDialogue(), playerQuest);

        player.sendMessage("");
        player.sendMessage(color("&a&lQUEST COMPLETED &8» &f" + quest.getName()));

        if (quest.getReward().getSouls() > 0) player.sendMessage(color("&6&lREWARD &8» &b+" + format(quest.getReward().getSouls()) + " Souls"));
        if (quest.getReward().getGems() > 0) player.sendMessage(color("&6&lREWARD &8» &d+" + format(quest.getReward().getGems()) + " Gems"));

        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);

        savePlayer(player.getUniqueId());
        ensureMainQuests(player);
    }

    private void giveRewards(Player player, Quest quest) {
        QuestReward reward = quest.getReward();

        if (reward.getSouls() > 0) plugin.getSoulManager().addSouls(player, reward.getSouls());
        if (reward.getGems() > 0) plugin.getGemManager().addGems(player, reward.getGems());
    }

    private boolean isQuestComplete(Quest quest, PlayerQuest playerQuest) {
        if (quest.getObjectives().isEmpty()) return false;

        for (QuestObjective objective : quest.getObjectives()) {
            if (!isObjectiveComplete(objective, playerQuest.getObjective(objective.getId()))) return false;
        }

        return true;
    }

    private boolean isObjectiveComplete(QuestObjective objective, PlayerQuestObjective progress) {
        return progress.getProgress() >= objective.getEffectiveRequiredAmount();
    }

    private void clampProgress(QuestObjective objective, PlayerQuestObjective progress) {
        int required = objective.getEffectiveRequiredAmount();
        if (progress.getProgress() > required) progress.setProgress(required);
    }

    private boolean matchesMob(LivingEntity mob, String target) {
        if (target == null || target.isBlank()) return false;

        String mobId = getMobIdentifier(mob);

        if (mobId.equalsIgnoreCase(target)) return true;
        return mob.getType().name().equalsIgnoreCase(target);
    }

    private String getMobIdentifier(LivingEntity mob) {
        if (plugin.getMobSoulRewardManager() != null) {
            String mobId = plugin.getMobSoulRewardManager().getMobId(mob);
            if (mobId != null && !mobId.isBlank()) return mobId;
        }

        return mob.getType().name().toLowerCase();
    }

    private void sendObjectiveProgress(Player player, Quest quest, PlayerQuest playerQuest) {
        for (QuestObjective objective : quest.getObjectives()) {
            PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());
            player.sendMessage(color("&7" + objective.getDescription() + ": &f" + format(progress.getProgress()) + "&7/&f" + format(objective.getEffectiveRequiredAmount())));
        }
    }

    private void sendDialogue(Player player, Quest quest, List<String> dialogue, PlayerQuest playerQuest) {
        for (String line : dialogue) player.sendMessage(color(applyPlaceholders(line, quest, playerQuest)));

        if (!dialogue.isEmpty()) player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.0f);
    }

    private String applyPlaceholders(String text, Quest quest, PlayerQuest playerQuest) {
        String result = text.replace("%quest%", quest.getName());

        if (!quest.getObjectives().isEmpty()) {
            QuestObjective objective = quest.getObjectives().getFirst();
            PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());

            result = result.replace("%progress%", format(progress.getProgress()));
            result = result.replace("%required%", format(objective.getEffectiveRequiredAmount()));
        }

        return result;
    }

    public void handleProgress(Player player, QuestObjectiveType type, int amount) {
        if (amount <= 0) return;

        ensureMainQuests(player);

        for (Quest quest : getActiveQuests(player)) {
            PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());
            if (playerQuest == null || playerQuest.getState() != QuestState.ACTIVE) continue;

            boolean changed = false;

            for (QuestObjective objective : quest.getObjectives()) {
                if (objective.getType() != type) continue;

                PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());
                if (isObjectiveComplete(objective, progress)) continue;

                progress.addProgress(amount);
                clampProgress(objective, progress);
                changed = true;
            }

            if (changed) handleQuestProgress(player, quest, playerQuest);
        }
    }

    public void handleStateProgress(Player player, QuestObjectiveType type, int value) {
        if (value < 0) return;

        ensureMainQuests(player);

        for (Quest quest : getActiveQuests(player)) {
            PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());
            if (playerQuest == null || playerQuest.getState() != QuestState.ACTIVE) continue;

            boolean changed = false;

            for (QuestObjective objective : quest.getObjectives()) {
                if (objective.getType() != type) continue;

                PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());
                if (isObjectiveComplete(objective, progress)) continue;

                int newProgress = Math.min(value, objective.getEffectiveRequiredAmount());

                if (newProgress > progress.getProgress()) {
                    progress.setProgress(newProgress);
                    changed = true;
                }
            }

            if (changed) handleQuestProgress(player, quest, playerQuest);
        }
    }

    public String getObjectiveProgressLine(Player player, Quest quest, QuestObjective objective) {
        PlayerQuest playerQuest = getPlayerQuest(player, quest.getId());
        if (playerQuest == null) return "";

        PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());

        return "&7&l| &f" + objective.getDescription() + ": &e" + format(progress.getProgress()) + "&7/&e" + format(objective.getEffectiveRequiredAmount());
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

                QuestState state;

                try {
                    state = QuestState.valueOf(stateString);
                } catch (IllegalArgumentException exception) {
                    state = QuestState.ACTIVE;
                }

                PlayerQuest playerQuest = new PlayerQuest(questId, state);
                ConfigurationSection objectivesSection = config.getConfigurationSection(path + ".objectives");

                if (objectivesSection != null) {
                    for (String objectiveId : objectivesSection.getKeys(false)) {
                        String objectivePath = path + ".objectives." + objectiveId;
                        int progress = config.getInt(objectivePath + ".progress", 0);
                        Set<String> completedTargets = new HashSet<>(config.getStringList(objectivePath + ".completed-targets"));

                        playerQuest.getObjectives().put(objectiveId.toLowerCase(), new PlayerQuestObjective(progress, completedTargets));
                    }
                } else {
                    Quest quest = getQuest(questId);

                    if (quest != null && !quest.getObjectives().isEmpty()) {
                        int oldProgress = config.getInt(path + ".progress", 0);
                        playerQuest.getObjective(quest.getObjectives().getFirst().getId()).setProgress(oldProgress);
                    }
                }

                loadedQuests.put(questId.toLowerCase(), playerQuest);
            }

            playerQuests.put(uuid, loadedQuests);
        }
    }

    public void savePlayer(UUID uuid) {
        String basePath = "players." + uuid + ".quests";

        config.set(basePath, null);

        Map<String, PlayerQuest> playerMap = playerQuests.get(uuid);

        if (playerMap != null) {
            for (PlayerQuest playerQuest : playerMap.values()) {
                String questPath = basePath + "." + playerQuest.getQuestId();

                config.set(questPath + ".state", playerQuest.getState().name());

                for (Map.Entry<String, PlayerQuestObjective> entry : playerQuest.getObjectives().entrySet()) {
                    String objectivePath = questPath + ".objectives." + entry.getKey();
                    PlayerQuestObjective objective = entry.getValue();

                    config.set(objectivePath + ".progress", objective.getProgress());
                    config.set(objectivePath + ".completed-targets", new ArrayList<>(objective.getCompletedTargets()));
                }
            }
        }

        saveFile();
    }

    public boolean startQuestAdmin(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) return false;
        if (getPlayerQuest(player, questId) != null) return false;

        activateQuest(player, quest, true);
        return true;
    }

    public boolean completeQuestAdmin(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) return false;

        PlayerQuest playerQuest = getPlayerQuest(player, questId);

        if (playerQuest == null) {
            activateQuest(player, quest, false);
            playerQuest = getPlayerQuest(player, questId);
        }

        if (playerQuest == null) return false;
        if (playerQuest.getState() == QuestState.COMPLETED) return false;

        for (QuestObjective objective : quest.getObjectives()) {
            PlayerQuestObjective progress = playerQuest.getObjective(objective.getId());
            progress.setProgress(objective.getEffectiveRequiredAmount());

            if (!objective.getTargets().isEmpty()) {
                progress.getCompletedTargets().clear();
                progress.getCompletedTargets().addAll(objective.getTargets());
            }
        }

        completeQuest(player, quest, playerQuest);
        return true;
    }

    public boolean resetQuest(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) return false;

        Map<String, PlayerQuest> playerMap = playerQuests.get(player.getUniqueId());

        if (playerMap != null) playerMap.remove(questId.toLowerCase());

        savePlayer(player.getUniqueId());

        if (quest.getType() == QuestType.MAIN) ensureMainQuests(player);

        return true;
    }

    public void resetAllQuests(Player player) {
        playerQuests.remove(player.getUniqueId());
        savePlayer(player.getUniqueId());
        ensureMainQuests(player);
    }

    public void clearAllQuestData() {
        playerQuests.clear();
        config.set("players", null);
        saveFile();
    }

    public void reloadQuests() {
        quests.clear();
        quests.putAll(new QuestLoader(plugin).loadQuests());

        for (Player player : plugin.getServer().getOnlinePlayers()) ensureMainQuests(player);
    }

    public boolean questExists(String questId) {
        return getQuest(questId) != null;
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

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}