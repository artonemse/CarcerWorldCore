package Commands;

import Warps.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(color("&6&lWARPS &7&l| &fAvailable warps: &e"
                    + String.join("&7, &e", warpManager.getWarps())));
            return true;
        }

        warpManager.teleport(player, args[0]);
        return true;
    }

    private String color(String text) {
        return text.replace('&', '§');
    }
}
