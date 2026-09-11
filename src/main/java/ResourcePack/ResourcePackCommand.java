package ResourcePack;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourcePackCommand implements CommandExecutor, TabCompleter {

    private final CarcerWorldCore plugin;
    private final ResourcePackManager manager;
    private final AtomicBoolean updating = new AtomicBoolean(false);

    public ResourcePackCommand(CarcerWorldCore plugin, ResourcePackManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("carcer.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "rebuild" -> rebuild(sender);
            case "publish" -> publish(sender);
            case "reload" -> reload(sender);
            case "send" -> send(sender, args);
            case "info" -> info(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void rebuild(CommandSender sender) {
        if (!beginUpdate(sender)) return;

        sender.sendMessage("§7Rebuilding the CarcerWorld resource pack...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = manager.rebuildPack();

            Bukkit.getScheduler().runTask(plugin, () -> {
                updating.set(false);

                if (!success) {
                    sender.sendMessage("§cResource pack rebuild failed. Check console.");
                    return;
                }

                sender.sendMessage("§aResource pack rebuilt successfully.");
                sender.sendMessage("§7SHA-1: §f" + manager.getCurrentSHA1());
            });
        });
    }

    private void publish(CommandSender sender) {
        if (!beginUpdate(sender)) return;

        sender.sendMessage("§7Uploading the CarcerWorld resource pack to GitHub...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = manager.publishPack();

            Bukkit.getScheduler().runTask(plugin, () -> {
                updating.set(false);

                if (!success) {
                    sender.sendMessage("§cResource pack upload failed. Check console.");
                    return;
                }

                sender.sendMessage("§aResource pack uploaded successfully.");
                sender.sendMessage("§7URL: §f" + manager.getCurrentDownloadURL());
            });
        });
    }

    private void reload(CommandSender sender) {
        if (!beginUpdate(sender)) return;

        sender.sendMessage("§7Rebuilding and publishing the CarcerWorld resource pack...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = manager.rebuildAndPublish();

            Bukkit.getScheduler().runTask(plugin, () -> {
                updating.set(false);

                if (!success) {
                    sender.sendMessage("§cResource pack update failed. Check console.");
                    return;
                }

                sender.sendMessage("§aResource pack rebuilt and uploaded.");
                sender.sendMessage("§7SHA-1: §f" + manager.getCurrentSHA1());
                sender.sendMessage("§7Sending the updated pack to online players...");

                Bukkit.getScheduler().runTaskLater(plugin, manager::sendPackToEveryone, 40L);
            });
        });
    }

    private void send(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("all")) {
            manager.sendPackToEveryone();
            sender.sendMessage("§aResource pack sent to all online players.");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cConsole must use /carcerpack send all.");
            return;
        }

        manager.sendPack(player);
        sender.sendMessage("§aResource pack sent.");
    }

    private void info(CommandSender sender) {
        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§f§lCarcerWorld Resource Pack");
        sender.sendMessage("");
        sender.sendMessage("§7Base Pack: §f" + manager.getBasePack().exists());
        sender.sendMessage("§7ModelEngine Pack: §f" + manager.getModelEnginePack().exists());
        sender.sendMessage("§7Generated Pack: §f" + manager.getOutputPack().exists());
        sender.sendMessage("§7SHA-1: §f" + (manager.getCurrentSHA1() == null ? "None" : manager.getCurrentSHA1()));
        sender.sendMessage("§7URL: §f" + (manager.getCurrentDownloadURL() == null ? "None" : manager.getCurrentDownloadURL()));
        sender.sendMessage("§8§m--------------------------------");
    }

    private boolean beginUpdate(CommandSender sender) {
        if (!updating.compareAndSet(false, true)) {
            sender.sendMessage("§cA resource pack update is already running.");
            return false;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§f§lCarcerWorld Resource Pack");
        sender.sendMessage("§7/carcerpack rebuild §8- §fMerge the packs");
        sender.sendMessage("§7/carcerpack publish §8- §fUpload the generated pack");
        sender.sendMessage("§7/carcerpack reload §8- §fMerge, upload and send");
        sender.sendMessage("§7/carcerpack send §8- §fSend the pack to yourself");
        sender.sendMessage("§7/carcerpack send all §8- §fSend the pack to everyone");
        sender.sendMessage("§7/carcerpack info §8- §fView resource pack status");
        sender.sendMessage("§8§m--------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(List.of("rebuild", "publish", "reload", "send", "info"), args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("send")) return filter(List.of("all"), args[1]);

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        List<String> results = new ArrayList<>();

        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) results.add(option);
        }

        return results;
    }
}
