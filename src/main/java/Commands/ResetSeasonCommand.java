package Commands;

import PlayerData.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class ResetSeasonCommand implements CommandExecutor {

    private final CarcerWorldCore plugin;

    public ResetSeasonCommand(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("carcerworld.admin.resetseason")) {
            sender.sendMessage(color("&c&lSEASON RESET &7&l| &fYou do not have permission to use this command."));
            return true;
        }

        // /resetseason
        if (args.length == 0) {
            sender.sendMessage(color("&c&lSEASON RESET"));
            sender.sendMessage(color("&7&l| &fThis will permanently wipe ALL player progression."));
            sender.sendMessage(color("&7&l| &fWeapon levels and EXP"));
            sender.sendMessage(color("&7&l| &fMob kills"));
            sender.sendMessage(color("&7&l| &fSkill points and skills"));
            sender.sendMessage(color("&7&l| &fSouls"));
            sender.sendMessage(color("&7&l| &fWeapon enchants"));
            sender.sendMessage(color("&7&l| &fAscensions"));
            sender.sendMessage(color("&7&l| &fAll other saved player progression"));
            sender.sendMessage("");
            sender.sendMessage(color("&c&lWARNING &7&l| &fThis cannot be undone."));
            sender.sendMessage(color("&7&l| &fRun &c/resetseason confirm &fto continue."));
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("confirm")) {
            sender.sendMessage(color("&c&lSEASON RESET &7&l| &fUsage: /resetseason confirm"));
            return true;
        }

        // ================================
        // WIPE ALL SAVED PLAYER DATA
        // ================================

        plugin.getPlayerDataManager().clearAllData();

        // ================================
        // REINITIALIZE ONLINE PLAYERS
        // ================================

        for (Player player : Bukkit.getOnlinePlayers()) {

            // Creates brand-new default PlayerData
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

            // Reset health in case Health skill previously modified it
            plugin.getSkillManager().updatePlayerHealth(player);

            // Give/update fresh beginner weapon
            plugin.getWeaponManager().giveOrUpdateWeapon(player);

            player.sendMessage(color("&c&lSEASON RESET &7&l| &fYour progression has been completely reset."));
        }

        Bukkit.broadcastMessage(color("&c&lSEASON RESET &7&l| &fA new season has begun!"));

        sender.sendMessage(color("&a&lSEASON RESET &7&l| &fAll player data has been successfully wiped."));

        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
