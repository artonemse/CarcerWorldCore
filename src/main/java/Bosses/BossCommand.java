package Bosses;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BossCommand implements CommandExecutor, TabCompleter {

    private final BossManager bossManager;

    public BossCommand(BossManager bossManager) {
        this.bossManager = bossManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("carcer.admin")) {
            player.sendMessage(color("&cYou do not have permission."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("setspawn")) {
            if (args.length < 2) {
                player.sendMessage(color("&cUsage: /boss setspawn <boss>"));
                return true;
            }

            BossType type = BossType.fromId(args[1]);

            if (type == null) {
                player.sendMessage(color("&cUnknown boss."));
                return true;
            }

            bossManager.setSpawn(type, player.getLocation());

            player.sendMessage(color("&aBoss arena location set for &f" + type.getId() + "&a."));
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (args.length < 2) {
                player.sendMessage(color("&cUsage: /boss start <boss>"));
                return true;
            }

            BossType type = BossType.fromId(args[1]);

            if (type == null) {
                player.sendMessage(color("&cUnknown boss."));
                return true;
            }

            Location spawn = bossManager.getSpawn(type);

            if (spawn == null) spawn = player.getLocation();

            bossManager.startBoss(player, type, spawn);
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            bossManager.stopEncounter(player, "&cBoss encounter stopped.");
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(color("&2&lBOSS COMMANDS"));
        player.sendMessage(color("&7/boss setspawn <boss>"));
        player.sendMessage(color("&7/boss start <boss>"));
        player.sendMessage(color("&7/boss stop"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            results.add("setspawn");
            results.add("start");
            results.add("stop");
            return results;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("start"))) {
            for (BossType type : BossType.values()) results.add(type.getId());
        }

        return results;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
