package Commands;

import Currencies.ScrapManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScrapsCommand implements CommandExecutor {

    private final ScrapManager scrapManager;

    public ScrapsCommand(ScrapManager scrapManager) {
        this.scrapManager = scrapManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /scraps
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only.");
                return true;
            }

            player.sendMessage(color("&6&lSCRAPS &8» &fYou have &e" + format(scrapManager.getScraps(player)) + " Scraps&f."));
            return true;
        }

        // /scraps give <player> <amount>
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("carcer.admin")) {
                sender.sendMessage(color("&cYou do not have permission to use this command."));
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage(color("&cUsage: /scraps give <player> <amount>"));
                return true;
            }

            Player target = sender.getServer().getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage(color("&cThat player is not online."));
                return true;
            }

            long amount;

            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(color("&cAmount must be a valid number."));
                return true;
            }

            if (amount <= 0) {
                sender.sendMessage(color("&cAmount must be greater than 0."));
                return true;
            }

            scrapManager.addScraps(target, amount);

            sender.sendMessage(color("&6&lSCRAPS &8» &fGave &e" + format(amount) + " Scraps &fto &e" + target.getName() + "&f."));
            target.sendMessage(color("&6&lSCRAPS &8» &fYou received &e" + format(amount) + " Scraps&f."));

            return true;
        }

        sender.sendMessage(color("&cUsage: /scraps"));
        return true;
    }

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
