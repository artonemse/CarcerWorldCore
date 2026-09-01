package Commands;

import Currencies.GemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsCommand implements CommandExecutor {

    private final GemManager gemManager;

    public GemsCommand(GemManager gemManager) {
        this.gemManager = gemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // ================================
        // /gems
        // ================================

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fOnly players can check their balance."));
                return true;
            }

            player.sendMessage(color("&a&lGEMS &7&l| &fBalance: &a" + format(gemManager.getGems(player)) + " Gems"));
            return true;
        }

        // ================================
        // /gems pay <player> <amount>
        // ================================

        if (args[0].equalsIgnoreCase("pay")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fOnly players can pay Gems."));
                return true;
            }

            if (args.length != 3) {
                player.sendMessage(color("&c&lGEMS &7&l| &fUsage: /gems pay <player> <amount>"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                player.sendMessage(color("&c&lGEMS &7&l| &fThat player is not online."));
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(color("&c&lGEMS &7&l| &fYou cannot pay yourself."));
                return true;
            }

            Long amount = parseAmount(args[2]);

            if (amount == null || amount <= 0) {
                player.sendMessage(color("&c&lGEMS &7&l| &fPlease enter a valid amount."));
                return true;
            }

            if (!gemManager.hasGems(player, amount)) {
                player.sendMessage(color("&c&lGEMS &7&l| &fYou do not have enough Gems."));
                return true;
            }

            if (!gemManager.removeGems(player, amount)) {
                player.sendMessage(color("&c&lGEMS &7&l| &fUnable to complete the payment."));
                return true;
            }

            gemManager.addGems(target, amount);

            player.sendMessage(color("&a&lGEMS &7&l| &fYou paid &a" + target.getName() + " " + format(amount) + " Gems&f."));
            target.sendMessage(color("&a&lGEMS &7&l| &fYou received &a" + format(amount) + " Gems &ffrom &a" + player.getName() + "&f."));

            return true;
        }

        // ================================
        // ADMIN COMMANDS
        // ================================

        if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("take")) {

            if (!sender.hasPermission("carcerworld.admin.gems")) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fYou do not have permission to use this command."));
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fUsage: /gems give|set|take <player> <amount>"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fThat player is not online."));
                return true;
            }

            Long amount = parseAmount(args[2]);

            if (amount == null || amount < 0) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fPlease enter a valid amount."));
                return true;
            }

            // GIVE

            if (args[0].equalsIgnoreCase("give")) {
                if (amount == 0) {
                    sender.sendMessage(color("&c&lGEMS &7&l| &fAmount must be greater than 0."));
                    return true;
                }

                gemManager.addGems(target, amount);

                sender.sendMessage(color("&a&lGEMS &7&l| &fGave &a" + target.getName() + " " + format(amount) + " Gems&f."));
                target.sendMessage(color("&a&lGEMS &7&l| &fYou received &a" + format(amount) + " Gems&f."));

                return true;
            }

            // SET

            if (args[0].equalsIgnoreCase("set")) {
                gemManager.setGems(target, amount);

                sender.sendMessage(color("&a&lGEMS &7&l| &fSet &a" + target.getName() + "&f's Gems to &a" + format(amount) + "&f."));

                return true;
            }

            // TAKE

            if (amount == 0) {
                sender.sendMessage(color("&c&lGEMS &7&l| &fAmount must be greater than 0."));
                return true;
            }

            long current = gemManager.getGems(target);
            long removed = Math.min(current, amount);

            gemManager.setGems(target, current - removed);

            sender.sendMessage(color("&a&lGEMS &7&l| &fRemoved &a" + format(removed) + " Gems &ffrom &a" + target.getName() + "&f."));

            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&a&lGEMS"));
        sender.sendMessage(color("&7&l| &f/gems"));
        sender.sendMessage(color("&7&l| &f/gems pay <player> <amount>"));

        if (sender.hasPermission("carcerworld.admin.gems")) {
            sender.sendMessage(color("&7&l| &f/gems give <player> <amount>"));
            sender.sendMessage(color("&7&l| &f/gems set <player> <amount>"));
            sender.sendMessage(color("&7&l| &f/gems take <player> <amount>"));
        }
    }

    private Long parseAmount(String input) {
        try {
            return Long.parseLong(input.replace(",", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}