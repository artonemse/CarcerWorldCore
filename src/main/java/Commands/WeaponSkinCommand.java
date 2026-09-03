package Commands;

import Cosmetics.WeaponSkins.WeaponSkin;
import Cosmetics.WeaponSkins.WeaponSkinManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WeaponSkinCommand implements CommandExecutor {

    private final WeaponSkinManager manager;

    public WeaponSkinCommand(WeaponSkinManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("carcer.admin")) {
            sender.sendMessage(color("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(color("&b&lWEAPON SKINS"));

            for (WeaponSkin skin : WeaponSkin.values()) sender.sendMessage(color("&7- &f" + skin.getId() + " &7(" + skin.getCustomModelData() + ")"));

            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(color("&cUsage: /weaponskin give <player> <skin>"));
            sender.sendMessage(color("&cUsage: /weaponskin remove <player> <skin>"));
            sender.sendMessage(color("&cUsage: /weaponskin list"));
            return true;
        }

        Player target = sender.getServer().getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(color("&cThat player is not online."));
            return true;
        }

        WeaponSkin skin = WeaponSkin.fromId(args[2]);

        if (skin == null) {
            sender.sendMessage(color("&cUnknown weapon skin. Use /weaponskin list."));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            manager.unlock(target, skin);

            sender.sendMessage(color("&b&lWEAPON SKINS &7&l| &fUnlocked " + skin.getDisplayName() + " &ffor &b" + target.getName() + "&f."));
            target.sendMessage(color("&b&lWEAPON SKINS &7&l| &fYou unlocked " + skin.getDisplayName() + "&f."));
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            manager.remove(target, skin);

            sender.sendMessage(color("&b&lWEAPON SKINS &7&l| &fRemoved " + skin.getDisplayName() + " &ffrom &b" + target.getName() + "&f."));
            target.sendMessage(color("&b&lWEAPON SKINS &7&l| &fYou no longer own " + skin.getDisplayName() + "&f."));
            return true;
        }

        sender.sendMessage(color("&cUsage: /weaponskin give <player> <skin>"));
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
