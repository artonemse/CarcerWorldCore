package ResourcePack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;

public class ResourcePackCommand implements CommandExecutor, TabCompleter {

    private final CarcerWorldCore plugin;
    private final ResourcePackManager manager;
    private final AtomicBoolean updating = new AtomicBoolean(false);

    public ResourcePackCommand(
            CarcerWorldCore plugin,
            ResourcePackManager manager
    ) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("carcer.admin")) {
            sender.sendMessage("§cYou do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "rebuild" -> runUpdate(
                    sender,
                    "Rebuilding the resource pack...",
                    manager::rebuildPack,
                    () -> {
                        sender.sendMessage(
                                "§aPack rebuilt with "
                                        + manager.getSkinIds().size()
                                        + " imported weapon skin(s)."
                        );
                        sender.sendMessage(
                                "§7Run /carcerpack publish, then /carcerpack send all."
                        );
                    }
            );

            case "publish" -> runUpdate(
                    sender,
                    "Publishing the resource pack...",
                    manager::publishPack,
                    () -> sender.sendMessage(
                            "§aPack published. Run /carcerpack send all."
                    )
            );

            case "reload" -> runUpdate(
                    sender,
                    "Rebuilding and publishing the resource pack...",
                    manager::rebuildAndPublish,
                    () -> {
                        sender.sendMessage(
                                "§aPack rebuilt and published. Sending to players..."
                        );

                        Bukkit.getScheduler().runTaskLater(
                                plugin, manager::sendPackToEveryone, 40L
                        );
                    }
            );

            case "send" -> send(sender, args);
            case "info" -> info(sender);
            case "skins" -> skins(sender);
            case "testskin" -> changeSkin(sender, args, false);
            case "applyskin" -> changeSkin(sender, args, true);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void runUpdate(
            CommandSender sender,
            String message,
            BooleanSupplier operation,
            Runnable onSuccess
    ) {
        if (!updating.compareAndSet(false, true)) {
            sender.sendMessage("§cA resource pack update is already running.");
            return;
        }

        sender.sendMessage("§7" + message);

        try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean result;

                try {
                    result = operation.getAsBoolean();
                } catch (Exception e) {
                    plugin.getLogger().log(
                            Level.SEVERE, "Resource pack operation failed.", e
                    );
                    result = false;
                }

                boolean success = result;

                try {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        updating.set(false);

                        if (!success) {
                            sender.sendMessage(
                                    "§cResource pack operation failed. Check console."
                            );
                            return;
                        }

                        onSuccess.run();
                    });
                } catch (RuntimeException e) {
                    updating.set(false);

                    plugin.getLogger().log(
                            Level.WARNING,
                            "Could not deliver resource pack command result.",
                            e
                    );
                }
            });
        } catch (RuntimeException e) {
            updating.set(false);
            sender.sendMessage("§cCould not start the resource pack update.");

            plugin.getLogger().log(
                    Level.SEVERE, "Could not schedule resource pack update.", e
            );
        }
    }

    private void send(CommandSender sender, String[] args) {
        if (!manager.hasPublishedPack()) {
            sender.sendMessage(
                    "§cPublish a pack first with /carcerpack publish or reload."
            );
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
            manager.sendPackToEveryone();
            sender.sendMessage("§aSent the published pack to all online players.");
            return;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /carcerpack send [all]");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cConsole must use /carcerpack send all.");
            return;
        }

        manager.sendPack(player);
        sender.sendMessage("§aSent the published resource pack.");
    }

    private void info(CommandSender sender) {
        sender.sendMessage("§6CarcerWorld Resource Pack");
        sender.sendMessage(
                "§7Base pack: §f" + manager.getBasePack().isFile()
        );
        sender.sendMessage(
                "§7ModelEngine pack: §f" + manager.getModelEnginePack().isFile()
        );
        sender.sendMessage(
                "§7Generated pack: §f" + manager.getOutputPack().isFile()
        );
        sender.sendMessage(
                "§7Skin folder: §f" + manager.getWeaponSkinsFolder()
        );
        sender.sendMessage(
                "§7Imported skins: §f" + manager.getSkinIds().size()
        );
        sender.sendMessage(
                "§7Generated SHA-1: §f" + manager.getCurrentSHA1()
        );
        sender.sendMessage(
                "§7Published URL: §f" + manager.getCurrentDownloadURL()
        );
    }

    private void skins(CommandSender sender) {
        if (manager.getSkinIds().isEmpty()) {
            sender.sendMessage(
                    "§7No imported skins. Add a skin folder and rebuild."
            );
            return;
        }

        sender.sendMessage("§6Imported weapon skins:");

        manager.getSkinIds().stream()
                .sorted()
                .forEach(id -> sender.sendMessage(
                        "§7- §f" + id
                                + " §8(" + WeaponSkinImporter.NAMESPACE
                                + ":" + id + ")"
                ));
    }

    @SuppressWarnings("deprecation")
    private void changeSkin(
            CommandSender sender,
            String[] args,
            boolean applyToHeldItem
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cUse this command in game.");
            return;
        }

        String subcommand = applyToHeldItem ? "applyskin" : "testskin";

        if (args.length != 2) {
            sender.sendMessage(
                    "§cUsage: /carcerpack " + subcommand + " <skin>"
            );
            return;
        }

        String id = args[1].toLowerCase(Locale.ROOT);

        if (!manager.hasSkin(id)) {
            sender.sendMessage(
                    "§cUnknown imported skin. Run /carcerpack skins."
            );
            return;
        }

        ItemStack item;

        if (applyToHeldItem) {
            ItemStack held = player.getInventory().getItemInMainHand();

            if (!held.getType().name().endsWith("_SWORD")) {
                sender.sendMessage("§cHold a sword in your main hand.");
                return;
            }

            item = held.clone();
        } else {
            if (player.getInventory().firstEmpty() == -1) {
                sender.sendMessage("§cMake an empty inventory slot first.");
                return;
            }

            item = new ItemStack(Material.NETHERITE_SWORD);
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            sender.sendMessage("§cThis item cannot have a skin.");
            return;
        }

        meta.setItemModel(
                new NamespacedKey(WeaponSkinImporter.NAMESPACE, id)
        );

        if (!applyToHeldItem) {
            meta.setDisplayName("§c" + id.replace('_', ' ') + " §7[Test]");
        }

        item.setItemMeta(meta);

        if (applyToHeldItem) {
            player.getInventory().setItemInMainHand(item);
            player.sendMessage("§aApplied skin: §f" + id);
        } else {
            player.getInventory().addItem(item);
            player.sendMessage("§aGiven a test sword using: §f" + id);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6CarcerWorld Resource Pack");
        sender.sendMessage("§7/carcerpack rebuild §8- §fMerge packs and skins");
        sender.sendMessage("§7/carcerpack publish §8- §fUpload the generated pack");
        sender.sendMessage("§7/carcerpack reload §8- §fRebuild, upload and send");
        sender.sendMessage("§7/carcerpack send [all] §8- §fSend the published pack");
        sender.sendMessage("§7/carcerpack info §8- §fShow pack status");
        sender.sendMessage("§7/carcerpack skins §8- §fList imported skins");
        sender.sendMessage("§7/carcerpack testskin <skin> §8- §fGive a test sword");
        sender.sendMessage("§7/carcerpack applyskin <skin> §8- §fSkin held sword");
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission("carcer.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(
                    List.of(
                            "rebuild", "publish", "reload", "send",
                            "info", "skins", "testskin", "applyskin"
                    ),
                    args[0]
            );
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("send")) {
                return filter(List.of("all"), args[1]);
            }

            if (args[0].equalsIgnoreCase("testskin")
                    || args[0].equalsIgnoreCase("applyskin")) {

                return filter(
                        manager.getSkinIds().stream().sorted().toList(),
                        args[1]
                );
            }
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);

        return options.stream()
                .filter(option ->
                        option.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}