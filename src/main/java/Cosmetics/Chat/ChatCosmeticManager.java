package Cosmetics.Chat;

import Quests.PlayerQuest;
import Quests.QuestState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class ChatCosmeticManager {

    public record Snapshot(String prefix, ChatCosmetic color) {}

    private final CarcerWorldCore plugin;
    private final File settingsFile;
    private final File dataFile;
    private final YamlConfiguration settings = new YamlConfiguration();
    private final YamlConfiguration data = new YamlConfiguration();
    private final Map<UUID, Snapshot> snapshots = new ConcurrentHashMap<>();

    public ChatCosmeticManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
        settingsFile = new File(plugin.getDataFolder(), "chat-cosmetics.yml");
        dataFile = new File(plugin.getDataFolder(), "chat-cosmetics-data.yml");

        try {
            Files.createDirectories(plugin.getDataFolder().toPath());

            if (settingsFile.isFile()) settings.load(settingsFile);
            if (dataFile.isFile()) data.load(dataFile);

            for (ChatCosmetic cosmetic : ChatCosmetic.values()) {
                String path = "cosmetics." + cosmetic.id();

                settings.addDefault(path + ".price", cosmetic.defaultPrice());
                settings.addDefault(path + ".requirement", cosmetic.defaultRequirement());
                settings.addDefault(path + ".quest", cosmetic.defaultQuest());
                settings.addDefault(path + ".mob-kills", cosmetic.defaultKills());
                settings.addDefault(path + ".ascension", cosmetic.defaultAscension());
            }

            settings.options().copyDefaults(true);
            settings.save(settingsFile);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load chat cosmetic files. Check their YAML syntax.", exception);
        }
    }

    private void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Chat cosmetic changes must run on the server thread.");
    }

    private String playerPath(Player player) {
        return "players." + player.getUniqueId();
    }

    public boolean owns(Player player, ChatCosmetic cosmetic) {
        requireMainThread();

        if (cosmetic == ChatCosmetic.WHITE) return true;

        return data.getStringList(playerPath(player) + ".owned").contains(cosmetic.id());
    }

    public long price(ChatCosmetic cosmetic) {
        if (cosmetic.kind() == ChatCosmetic.Kind.TAG) return -1;
        if (cosmetic == ChatCosmetic.WHITE) return 0;

        long configured = settings.getLong("cosmetics." + cosmetic.id() + ".price", cosmetic.defaultPrice());

        if (cosmetic.kind() == ChatCosmetic.Kind.BASIC) return Math.max(1, configured);

        return configured <= 0 ? -1 : configured;
    }

    public String requirement(ChatCosmetic cosmetic) {
        return settings.getString("cosmetics." + cosmetic.id() + ".requirement", cosmetic.defaultRequirement());
    }

    public ChatCosmetic selectedColor(Player player) {
        ChatCosmetic cosmetic = ChatCosmetic.fromId(data.getString(playerPath(player) + ".color", "white"));

        if (cosmetic == null || cosmetic.kind() == ChatCosmetic.Kind.TAG || !owns(player, cosmetic)) return ChatCosmetic.WHITE;

        return cosmetic;
    }

    public ChatCosmetic selectedTag(Player player) {
        ChatCosmetic cosmetic = ChatCosmetic.fromId(data.getString(playerPath(player) + ".tag", ""));

        if (cosmetic == null || cosmetic.kind() != ChatCosmetic.Kind.TAG || !owns(player, cosmetic)) return null;

        return cosmetic;
    }

    public boolean selected(Player player, ChatCosmetic cosmetic) {
        return cosmetic.kind() == ChatCosmetic.Kind.TAG
                ? selectedTag(player) == cosmetic
                : selectedColor(player) == cosmetic;
    }

    private void addOwnership(Player player, ChatCosmetic cosmetic) {
        String path = playerPath(player);
        Set<String> owned = new HashSet<>(data.getStringList(path + ".owned"));
        Set<String> blocked = new HashSet<>(data.getStringList(path + ".blocked"));

        owned.add(cosmetic.id());
        blocked.remove(cosmetic.id());

        data.set(path + ".owned", new ArrayList<>(owned));
        data.set(path + ".blocked", new ArrayList<>(blocked));
    }

    private void setSelection(Player player, ChatCosmetic cosmetic) {
        String key = cosmetic.kind() == ChatCosmetic.Kind.TAG ? ".tag" : ".color";
        data.set(playerPath(player) + key, cosmetic.id());
    }

    public String activate(Player player, ChatCosmetic cosmetic) {
        requireMainThread();

        if (owns(player, cosmetic)) {
            if (!commit(() -> setSelection(player, cosmetic))) return "§cCould not save your selection. Please contact staff.";

            publishSnapshot(player);
            return "§aEquipped §f" + cosmetic.displayName() + "§a.";
        }

        long cost = price(cosmetic);

        if (cost < 0) return "§7Earn this cosmetic: §f" + requirement(cosmetic);

        boolean basic = cosmetic.kind() == ChatCosmetic.Kind.BASIC;
        boolean paid = basic
                ? plugin.getScrapManager().removeScraps(player, cost)
                : plugin.getGemManager().removeGems(player, cost);

        if (!paid) {
            String currency = basic ? "Scraps" : "Gems";
            return "§cYou need §f" + String.format("%,d", cost) + " " + currency + "§c.";
        }

        boolean saved = commit(() -> {
            addOwnership(player, cosmetic);
            setSelection(player, cosmetic);
        });

        if (!saved) {
            if (basic) plugin.getScrapManager().addScraps(player, cost);
            else plugin.getGemManager().addGems(player, cost);

            return "§cCould not save the purchase. Your currency was refunded.";
        }

        publishSnapshot(player);
        return "§aPurchased and equipped §f" + cosmetic.displayName() + "§a.";
    }

    public boolean grant(Player player, ChatCosmetic cosmetic) {
        requireMainThread();

        if (owns(player, cosmetic)) return false;
        if (!commit(() -> addOwnership(player, cosmetic))) return false;

        publishSnapshot(player);
        player.sendMessage("§b§lCOSMETICS §7§l| §fUnlocked " + cosmetic.paint(cosmetic.displayName()) + "§f!");
        return true;
    }

    public boolean revoke(Player player, ChatCosmetic cosmetic) {
        requireMainThread();

        if (cosmetic == ChatCosmetic.WHITE) return false;

        boolean success = commit(() -> {
            String path = playerPath(player);
            Set<String> owned = new HashSet<>(data.getStringList(path + ".owned"));
            Set<String> blocked = new HashSet<>(data.getStringList(path + ".blocked"));

            owned.remove(cosmetic.id());
            blocked.add(cosmetic.id());

            data.set(path + ".owned", new ArrayList<>(owned));
            data.set(path + ".blocked", new ArrayList<>(blocked));

            if (cosmetic.id().equals(data.getString(path + ".color"))) data.set(path + ".color", "white");
            if (cosmetic.id().equals(data.getString(path + ".tag"))) data.set(path + ".tag", null);
        });

        if (success) publishSnapshot(player);
        return success;
    }

    public boolean resetSelection(Player player, boolean tag) {
        requireMainThread();

        boolean success = commit(() -> data.set(playerPath(player) + (tag ? ".tag" : ".color"), tag ? null : "white"));

        if (success) publishSnapshot(player);
        return success;
    }

    public void refresh(Player player) {
        requireMainThread();

        Set<String> blocked = new HashSet<>(data.getStringList(playerPath(player) + ".blocked"));

        for (ChatCosmetic cosmetic : ChatCosmetic.values()) {
            if (cosmetic.kind() == ChatCosmetic.Kind.BASIC) continue;
            if (owns(player, cosmetic) || blocked.contains(cosmetic.id())) continue;

            if (meetsRequirement(player, cosmetic)) grant(player, cosmetic);
        }

        publishSnapshot(player);
    }

    private boolean meetsRequirement(Player player, ChatCosmetic cosmetic) {
        String path = "cosmetics." + cosmetic.id();
        String quest = settings.getString(path + ".quest", "").trim();
        long kills = Math.max(0, settings.getLong(path + ".mob-kills"));
        int ascension = Math.max(0, settings.getInt(path + ".ascension"));

        // No condition means external reward only, not a free unlock.
        if (quest.isEmpty() && kills == 0 && ascension == 0) return false;

        var playerData = plugin.getPlayerDataManager().getPlayerData(player);

        if (playerData.getMobKills() < kills) return false;
        if (playerData.getAscensions() < ascension) return false;

        if (!quest.isEmpty()) {
            PlayerQuest progress = plugin.getQuestManager().getPlayerQuest(player, quest);
            if (progress == null || progress.getState() != QuestState.COMPLETED) return false;
        }

        return true;
    }

    private void publishSnapshot(Player player) {
        int ascension = plugin.getPlayerDataManager().getPlayerData(player).getAscensions();
        ChatCosmetic tag = selectedTag(player);

        String prefix = "§8[§5§lAscension " + ascension + "§8] ";

        if (tag != null) prefix += "§8[" + tag.paint(tag.displayName()) + "§8] ";

        prefix += "§f" + player.getName() + " §8» §r";

        snapshots.put(player.getUniqueId(), new Snapshot(prefix, selectedColor(player)));
    }

    public Snapshot snapshot(UUID playerId) {
        return snapshots.get(playerId);
    }

    public String preview(Player player) {
        publishSnapshot(player);
        Snapshot snapshot = snapshots.get(player.getUniqueId());
        return snapshot.prefix() + snapshot.color().paint("Ready for another adventure!");
    }

    public void forget(UUID playerId) {
        snapshots.remove(playerId);
    }

    private boolean commit(Runnable change) {
        requireMainThread();

        String previous = data.saveToString();

        try {
            change.run();
            saveAtomically();
            return true;
        } catch (Exception exception) {
            try {
                data.loadFromString(previous);
            } catch (Exception rollbackFailure) {
                exception.addSuppressed(rollbackFailure);
            }

            plugin.getLogger().log(Level.SEVERE, "Could not save chat cosmetics.", exception);
            return false;
        }
    }

    private void saveAtomically() throws IOException {
        Path temporary = Files.createTempFile(plugin.getDataFolder().toPath(), "chat-cosmetics-", ".tmp");

        try {
            Files.writeString(temporary, data.saveToString(), StandardCharsets.UTF_8);

            try {
                Files.move(temporary, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static String cleanMessage(String message) {
        String result = message.replaceAll("(?i)&#[0-9a-f]{6}", "");
        result = result.replaceAll("(?i)&[0-9a-fk-orx]", "");
        result = ChatColor.stripColor(result);
        return result.replaceAll("[§\\p{Cc}]", "");
    }
}
