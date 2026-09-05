package Commands;

import Enchantments.EnchantType;
import PlayerData.PlayerData;
import Skills.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class CarcerAdminCommand implements CommandExecutor {

    private final CarcerWorldCore plugin;

    public CarcerAdminCommand(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("carcerworld.admin")) {
            sender.sendMessage(color("&c&lADMIN &7&l| &fYou do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        // ================================
        // QUEST ADMIN
        // ================================

        if (action.equals("quest")) {
            handleQuestCommand(sender, args);
            return true;
        }

        // ================================
        // GIVE RANDOM ARMOR
        // ================================

        if (action.equals("givearmor")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fOnly players can use this command."));
                return true;
            }

            ItemStack armor = plugin.getGenericArmorGenerator().generateRandomArmor();
            player.getInventory().addItem(armor);

            sender.sendMessage(color("&a&lADMIN &7&l| &fGenerated a random generic armor piece."));
            return true;
        }
        if (action.equals("giveblackthorn")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fOnly players can use this command."));
                return true;
            }

            for (Armor.Special.SpecialArmorSlot slot : Armor.Special.SpecialArmorSlot.values()) {
                player.getInventory().addItem(plugin.getSpecialArmorGenerator().createArmor(Armor.Special.SpecialArmorSet.BLACKTHORN, slot));
            }

            sender.sendMessage(color("&2&lBLACKTHORN &7&l| &fYou received the complete Blackthorn armor set."));
            return true;
        }

        if (args.length < 3) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(color("&c&lADMIN &7&l| &fThat player is not online."));
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);

        // ================================
        // WEAPON LEVEL
        // ================================

        if (action.equals("level")) {
            Integer amount = parseInt(args[2]);

            if (amount == null || amount < 1 || amount > 100) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fWeapon level must be between &c1-100&f."));
                return true;
            }

            data.setWeaponLevel(amount);
            data.setWeaponExp(0);

            saveAndUpdate(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fSet &a" + target.getName() + "&f's weapon level to &a" + amount + "&f."));
            target.sendMessage(color("&a&lADMIN &7&l| &fYour weapon level was set to &a" + amount + "&f."));

            return true;
        }

        // ================================
        // WEAPON EXP
        // ================================

        if (action.equals("exp")) {
            Long amount = parseLong(args[2]);

            if (amount == null || amount < 0) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fPlease enter a valid EXP amount."));
                return true;
            }

            data.setWeaponExp(amount);

            saveAndUpdate(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fSet &a" + target.getName() + "&f's weapon EXP to &a" + format(amount) + "&f."));
            return true;
        }

        // ================================
        // SKILL POINTS
        // ================================

        if (action.equals("skillpoints")) {
            Integer amount = parseInt(args[2]);

            if (amount == null || amount < 0) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fPlease enter a valid skill point amount."));
                return true;
            }

            data.setSkillPoints(amount);

            saveAndUpdate(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fSet &a" + target.getName() + "&f's skill points to &a" + amount + "&f."));
            return true;
        }

        // ================================
        // ASCENSION
        // ================================

        if (action.equals("ascension")) {
            Integer amount = parseInt(args[2]);

            if (amount == null || amount < 0) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fPlease enter a valid Ascension level."));
                return true;
            }

            data.setAscensions(amount);

            saveAndUpdate(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fSet &a" + target.getName() + "&f's Ascension to &d" + amount + "&f."));
            target.sendMessage(color("&d&lASCENSION &7&l| &fYour Ascension level was set to &d" + amount + "&f."));

            return true;
        }

        // ================================
        // SKILL
        // ================================

        if (action.equals("skill")) {
            if (args.length < 4) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fUsage: /carcer skill <player> <skill> <level>"));
                return true;
            }

            SkillType type = getSkillType(args[2]);

            if (type == null) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fUnknown skill. Use: strength, health, knowledge."));
                return true;
            }

            Integer level = parseInt(args[3]);

            if (level == null || level < 0 || level > type.getMaxLevel()) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fLevel must be between &c0-" + type.getMaxLevel() + "&f."));
                return true;
            }

            data.setSkillLevel(type, level);

            plugin.getPlayerDataManager().savePlayerData(target.getUniqueId());
            plugin.getSkillManager().updatePlayerHealth(target);
            plugin.getWeaponManager().giveOrUpdateWeapon(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fSet &a" + target.getName() + "&f's " + type.getDisplayName() + " &fto level &a" + level + "&f."));
            return true;
        }

        // ================================
        // ENCHANT
        // ================================

        if (action.equals("enchant")) {
            if (args.length < 4) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fUsage: /carcer enchant <player> <enchant> <level>"));
                return true;
            }

            EnchantType type = getEnchantType(args[2]);

            if (type == null) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fUnknown enchant."));
                sender.sendMessage(color("&7&l| &fsharpness, critical, double, cleave, execute"));
                return true;
            }

            Integer level = parseInt(args[3]);

            if (level == null || level < 0 || level > type.getMaxLevel()) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fLevel must be between &c0-" + type.getMaxLevel() + "&f."));
                return true;
            }

            data.setEnchantLevel(type, level);

            saveAndUpdate(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fSet &a" + target.getName() + "&f's " + type.getDisplayName() + " &fto level &a" + level + "&f."));
            return true;
        }

        // ================================
        // RESET PLAYER
        // ================================

        if (action.equals("reset")) {
            if (!args[2].equalsIgnoreCase("confirm")) {
                sender.sendMessage(color("&c&lADMIN &7&l| &fThis completely resets the player's progression."));
                sender.sendMessage(color("&c&lADMIN &7&l| &fUse &c/carcer reset " + target.getName() + " confirm &fto continue."));
                return true;
            }

            resetPlayer(data);
            plugin.getQuestManager().resetAllQuests(target);

            plugin.getPlayerDataManager().savePlayerData(target.getUniqueId());
            plugin.getSkillManager().updatePlayerHealth(target);
            plugin.getWeaponManager().giveOrUpdateWeapon(target);

            sender.sendMessage(color("&a&lADMIN &7&l| &fCompletely reset &a" + target.getName() + "&f's progression."));
            target.sendMessage(color("&c&lPROGRESSION &7&l| &fYour progression has been reset."));

            return true;
        }

        sendHelp(sender);
        return true;
    }

    // ================================
    // QUEST ADMIN
    // ================================

    private void handleQuestCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendQuestHelp(sender);
            return;
        }

        String subAction = args[1].toLowerCase();

        if (subAction.equals("reload")) {
            plugin.getQuestManager().reloadQuests();
            sender.sendMessage(color("&a&lQUEST ADMIN &7&l| &fQuest definitions have been reloaded."));
            return;
        }


        if (subAction.equals("resetall")) {
            if (args.length < 3) {
                sender.sendMessage(color("&c&lQUEST ADMIN &7&l| &fUsage: /carcer quest resetall <player>"));
                return;
            }

            Player target = Bukkit.getPlayerExact(args[2]);

            if (target == null) {
                sender.sendMessage(color("&c&lQUEST ADMIN &7&l| &fThat player is not online."));
                return;
            }

            plugin.getQuestManager().resetAllQuests(target);

            sender.sendMessage(color("&a&lQUEST ADMIN &7&l| &fReset all quests for &a" + target.getName() + "&f."));
            target.sendMessage(color("&6&lQUESTS &7&l| &fYour quest progress has been reset."));
            return;
        }

        if (args.length < 4) {
            sendQuestHelp(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[2]);

        if (target == null) {
            sender.sendMessage(color("&c&lQUEST ADMIN &7&l| &fThat player is not online."));
            return;
        }

        String questId = args[3].toLowerCase();

        if (!plugin.getQuestManager().questExists(questId)) {
            sender.sendMessage(color("&c&lQUEST ADMIN &7&l| &fUnknown quest: &c" + questId));
            return;
        }

        if (subAction.equals("start")) {
            boolean success = plugin.getQuestManager().startQuestAdmin(target, questId);

            if (!success) {
                sender.sendMessage(color("&c&lQUEST ADMIN &7&l| &fThat quest is already active or completed for this player."));
                return;
            }

            sender.sendMessage(color("&a&lQUEST ADMIN &7&l| &fStarted &a" + questId + " &ffor &a" + target.getName() + "&f."));
            return;
        }

        if (subAction.equals("complete")) {
            boolean success = plugin.getQuestManager().completeQuestAdmin(target, questId);

            if (!success) {
                sender.sendMessage(color("&c&lQUEST ADMIN &7&l| &fThat quest is already completed or could not be completed."));
                return;
            }

            sender.sendMessage(color("&a&lQUEST ADMIN &7&l| &fCompleted &a" + questId + " &ffor &a" + target.getName() + "&f."));
            return;
        }

        if (subAction.equals("reset")) {
            plugin.getQuestManager().resetQuest(target, questId);

            sender.sendMessage(color("&a&lQUEST ADMIN &7&l| &fReset &a" + questId + " &ffor &a" + target.getName() + "&f."));
            target.sendMessage(color("&6&lQUESTS &7&l| &fQuest progress reset: &e" + questId));
            return;
        }

        sendQuestHelp(sender);
    }

    private void sendQuestHelp(CommandSender sender) {
        sender.sendMessage(color("&6&lQUEST ADMIN"));
        sender.sendMessage(color("&7&l| &f/carcer quest start <player> <quest>"));
        sender.sendMessage(color("&7&l| &f/carcer quest complete <player> <quest>"));
        sender.sendMessage(color("&7&l| &f/carcer quest reset <player> <quest>"));
        sender.sendMessage(color("&7&l| &f/carcer quest resetall <player>"));
        sender.sendMessage(color("&7&l| &f/carcer quest reload"));
    }

    // ================================
    // RESET PLAYER
    // ================================

    private void resetPlayer(PlayerData data) {
        data.setWeaponLevel(1);
        data.setWeaponExp(0);
        data.setMobKills(0);
        data.setAscensions(0);
        data.setSkillPoints(0);
        data.setSouls(0);

        for (SkillType type : SkillType.values()) data.setSkillLevel(type, 0);
        for (EnchantType type : EnchantType.values()) data.setEnchantLevel(type, 0);
    }

    // ================================
    // SAVE + UPDATE WEAPON
    // ================================

    private void saveAndUpdate(Player player) {
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        plugin.getWeaponManager().giveOrUpdateWeapon(player);
    }

    private SkillType getSkillType(String input) {
        return switch (input.toLowerCase()) {
            case "strength" -> SkillType.STRENGTH;
            case "health" -> SkillType.HEALTH;
            case "knowledge" -> SkillType.KNOWLEDGE;
            default -> null;
        };
    }

    private EnchantType getEnchantType(String input) {
        return switch (input.toLowerCase()) {
            case "sharpness" -> EnchantType.SHARPNESS;
            case "critical", "criticalstrike", "critical_strike" -> EnchantType.CRITICAL_STRIKE;
            case "double", "doublestrike", "double_strike" -> EnchantType.DOUBLE_STRIKE;
            case "cleave" -> EnchantType.CLEAVE;
            case "execute" -> EnchantType.EXECUTE;
            default -> null;
        };
    }



    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&a&lCARCER ADMIN"));
        sender.sendMessage(color("&7&l| &f/carcer givearmor"));
        sender.sendMessage(color("&7&l| &f/carcer level <player> <1-100>"));
        sender.sendMessage(color("&7&l| &f/carcer exp <player> <amount>"));
        sender.sendMessage(color("&7&l| &f/carcer skillpoints <player> <amount>"));
        sender.sendMessage(color("&7&l| &f/carcer ascension <player> <amount>"));
        sender.sendMessage(color("&7&l| &f/carcer skill <player> <skill> <level>"));
        sender.sendMessage(color("&7&l| &f/carcer enchant <player> <enchant> <level>"));
        sender.sendMessage(color("&7&l| &f/carcer quest"));
        sender.sendMessage(color("&7&l| &f/carcer giveblackthorn"));
        sender.sendMessage(color("&7&l| &f/carcer reset <player> confirm"));
    }

    private Integer parseInt(String input) {
        try {
            return Integer.parseInt(input.replace(",", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parseLong(String input) {
        try {
            return Long.parseLong(input.replace(",", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String format(long number) {
        return String.format("%,d", number);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}