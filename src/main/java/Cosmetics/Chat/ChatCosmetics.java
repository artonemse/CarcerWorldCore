package Cosmetics.Chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitTask;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class ChatCosmetics implements Listener, CommandExecutor, TabCompleter {

    private static ChatCosmetics instance;

    private final CarcerWorldCore plugin;
    private final ChatCosmeticManager manager;
    private final ChatCosmeticGUI gui;
    private BukkitTask refreshTask;

    private ChatCosmetics(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.manager = new ChatCosmeticManager(plugin);
        this.gui = new ChatCosmeticGUI(plugin, manager);
    }

    public static void install(CarcerWorldCore plugin) {
        if (instance != null) throw new IllegalStateException("Chat cosmetics are already installed.");

        PluginCommand command = plugin.getCommand("chatcosmetic");

        if (command == null) throw new IllegalStateException("Add chatcosmetic to plugin.yml first.");

        ChatCosmetics system = new ChatCosmetics(plugin);
        instance = system;

        command.setExecutor(system);
        command.setTabCompleter(system);

        plugin.getServer().getPluginManager().registerEvents(system, plugin);
        plugin.getServer().getPluginManager().registerEvents(system.gui, plugin);

        for (Player player : Bukkit.getOnlinePlayers()) system.refreshSafely(player);

        system.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) system.refreshSafely(player);
        }, 20L, 20L);
    }

    public static ChatCosmetics get() {
        if (instance == null) throw new IllegalStateException("Chat cosmetics are not installed.");
        return instance;
    }

    public ChatCosmeticManager manager() {
        return manager;
    }

    public ChatCosmeticGUI gui() {
        return gui;
    }

    private void refreshSafely(Player player) {
        try {
            manager.refresh(player);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not refresh chat cosmetics for " + player.getName(), exception);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (event.getPlayer().isOnline()) refreshSafely(event.getPlayer());
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.forget(event.getPlayer().getUniqueId());
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        ChatCosmeticManager.Snapshot snapshot = manager.snapshot(event.getPlayer().getUniqueId());

        String prefix = snapshot == null
                ? "§f" + event.getPlayer().getName() + " §8» §r"
                : snapshot.prefix();

        ChatCosmetic color = snapshot == null ? ChatCosmetic.WHITE : snapshot.color();
        String message = ChatCosmeticManager.cleanMessage(event.getMessage());

        // User text remains a format argument, so percent signs are safe.
        event.setMessage(color.paint(message));
        event.setFormat(prefix.replace("%", "%%") + "%2$s");
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin() != plugin) return;

        if (refreshTask != null) refreshTask.cancel();
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) gui.open(player, ChatCosmetic.Kind.BASIC);
            else help(sender);

            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);

        if (List.of("colors", "gradients", "tags").contains(action)) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cUse this menu command in game.");
                return true;
            }

            ChatCosmetic.Kind kind = switch (action) {
                case "gradients" -> ChatCosmetic.Kind.PREMIUM;
                case "tags" -> ChatCosmetic.Kind.TAG;
                default -> ChatCosmetic.Kind.BASIC;
            };

            gui.open(player, kind);
            return true;
        }

        if (!sender.hasPermission("carcer.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (action.equals("list")) {
            for (ChatCosmetic cosmetic : ChatCosmetic.values()) {
                sender.sendMessage("§f" + cosmetic.id() + " §7— " + cosmetic.displayName());
            }
            return true;
        }

        if ((!action.equals("grant") && !action.equals("revoke")) || args.length != 3) {
            help(sender);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        ChatCosmetic cosmetic = ChatCosmetic.fromId(args[2]);

        if (target == null) {
            sender.sendMessage("§cThat player must be online.");
            return true;
        }

        if (cosmetic == null) {
            sender.sendMessage("§cUnknown cosmetic. Use /chatcosmetic list.");
            return true;
        }

        boolean success = action.equals("grant")
                ? manager.grant(target, cosmetic)
                : manager.revoke(target, cosmetic);

        sender.sendMessage(success
                ? "§aUpdated " + cosmetic.id() + " for " + target.getName() + "."
                : "§eNo change was made. It may already be owned, be the default white color, or have failed to save.");

        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage("§f/chatcosmetic colors");
        sender.sendMessage("§f/chatcosmetic gradients");
        sender.sendMessage("§f/chatcosmetic tags");

        if (sender.hasPermission("carcer.admin")) {
            sender.sendMessage("§f/chatcosmetic list");
            sender.sendMessage("§f/chatcosmetic grant <player> <id>");
            sender.sendMessage("§f/chatcosmetic revoke <player> <id>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(sender.hasPermission("carcer.admin")
                    ? List.of("colors", "gradients", "tags", "grant", "revoke", "list")
                    : List.of("colors", "gradients", "tags"), args[0]);
        }

        if (!sender.hasPermission("carcer.admin")) return List.of();
        if (!args[0].equalsIgnoreCase("grant") && !args[0].equalsIgnoreCase("revoke")) return List.of();

        if (args.length == 2) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }

        if (args.length == 3) {
            return filter(Arrays.stream(ChatCosmetic.values()).map(ChatCosmetic::id).toList(), args[2]);
        }

        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
