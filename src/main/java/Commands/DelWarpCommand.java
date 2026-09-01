package Commands;

import Warps.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelWarpCommand implements CommandExecutor {

    private final WarpManager warpManager;

    public DelWarpCommand(WarpManager warpManager) {
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
            player.sendMessage(color("&cUsage: /delwarp <name>"));
            return true;
        }

        String name = args[0].toLowerCase();

        if (!warpManager.warpExists(name)) {
            player.sendMessage(color("&c&lWARPS &7&l| &fThat warp does not exist."));
            return true;
        }

        warpManager.deleteWarp(name);

        player.sendMessage(color(
                "&a&lWARPS &7&l| &fDeleted warp &e" + name + "&f."
        ));

        return true;
    }

    private String color(String text) {
        return text.replace('&', '§');
    }
}