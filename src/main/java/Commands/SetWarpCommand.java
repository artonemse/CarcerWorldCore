package Commands;

import Warps.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {

    private final WarpManager warpManager;

    public SetWarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("carcer.admin")) {
            player.sendMessage(color("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(color("&cUsage: /setwarp <name>"));
            return true;
        }

        String name = args[0].toLowerCase();

        warpManager.setWarp(name, player.getLocation());

        player.sendMessage(color(
                "&a&lWARPS &7&l| &fSet warp &e" + name + "&f."
        ));

        return true;
    }

    private String color(String text) {
        return text.replace('&', '§');
    }
}