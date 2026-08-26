package Currencies;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class SoulsCommand implements CommandExecutor {

    private final CarcerWorldCore plugin;
    private final SoulManager soulManager;

    private static final String ADMIN_PERMISSION =
            "carcerworld.admin.souls";

    public SoulsCommand(CarcerWorldCore plugin) {
        this.plugin = plugin;
        this.soulManager = plugin.getSoulManager();
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        // ================================
        // /SOULS
        // ================================

        if (args.length == 0) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage(color(
                        "&c&lSOULS &7&l| &fOnly players can check their balance."
                ));
                return true;
            }

            long balance = soulManager.getSouls(player);

            player.sendMessage(color(
                    "&b&lSOULS &7&l| &fBalance: &b"
                            + format(balance)
                            + " Souls"
            ));

            return true;
        }

        // ================================
        // /SOULS PAY <PLAYER> <AMOUNT>
        // ================================

        if (args[0].equalsIgnoreCase("pay")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage(color(
                        "&c&lSOULS &7&l| &fOnly players can pay Souls."
                ));
                return true;
            }

            if (args.length != 3) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fUsage: /souls pay <player> <amount>"
                ));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fThat player is not online."
                ));
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fYou cannot pay yourself."
                ));
                return true;
            }

            Long amount = parseAmount(args[2]);

            if (amount == null || amount <= 0) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fPlease enter a valid amount."
                ));
                return true;
            }

            long playerBalance = soulManager.getSouls(player);

            if (playerBalance < amount) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fYou do not have enough Souls."
                ));
                return true;
            }

            long targetBalance = soulManager.getSouls(target);

            // Prevent long overflow
            if (Long.MAX_VALUE - targetBalance < amount) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fThat payment is too large."
                ));
                return true;
            }

            /*
             * Remove first.
             *
             * removeSouls() returns false if something
             * changed and the player can no longer afford it.
             */
            if (!soulManager.removeSouls(player, amount)) {
                player.sendMessage(color(
                        "&c&lSOULS &7&l| &fYou do not have enough Souls."
                ));
                return true;
            }

            soulManager.addSouls(target, amount);

            player.sendMessage(color(
                    "&b&lSOULS &7&l| &fYou paid &b"
                            + target.getName()
                            + " "
                            + format(amount)
                            + " Souls&f."
            ));

            target.sendMessage(color(
                    "&b&lSOULS &7&l| &fYou received &b"
                            + format(amount)
                            + " Souls &ffrom &b"
                            + player.getName()
                            + "&f."
            ));

            return true;
        }

        // ================================
        // ADMIN COMMANDS
        // ================================

        if (args[0].equalsIgnoreCase("give")
                || args[0].equalsIgnoreCase("set")
                || args[0].equalsIgnoreCase("take")) {

            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(color(
                        "&c&lSOULS &7&l| &fYou do not have permission to do this."
                ));
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage(color(
                        "&c&lSOULS &7&l| &fUsage: /souls "
                                + args[0].toLowerCase()
                                + " <player> <amount>"
                ));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                sender.sendMessage(color(
                        "&c&lSOULS &7&l| &fThat player is not online."
                ));
                return true;
            }

            Long amount = parseAmount(args[2]);

            if (amount == null || amount < 0) {
                sender.sendMessage(color(
                        "&c&lSOULS &7&l| &fPlease enter a valid amount."
                ));
                return true;
            }

            // ================================
            // GIVE
            // ================================

            if (args[0].equalsIgnoreCase("give")) {

                if (amount == 0) {
                    sender.sendMessage(color(
                            "&c&lSOULS &7&l| &fAmount must be greater than 0."
                    ));
                    return true;
                }

                long currentBalance = soulManager.getSouls(target);

                if (Long.MAX_VALUE - currentBalance < amount) {
                    sender.sendMessage(color(
                            "&c&lSOULS &7&l| &fThat amount is too large."
                    ));
                    return true;
                }

                soulManager.addSouls(target, amount);

                sender.sendMessage(color(
                        "&a&lSOULS &7&l| &fGave &b"
                                + format(amount)
                                + " Souls &fto &b"
                                + target.getName()
                                + "&f."
                ));

                target.sendMessage(color(
                        "&b&lSOULS &7&l| &fYou received &b"
                                + format(amount)
                                + " Souls&f."
                ));

                return true;
            }

            // ================================
            // SET
            // ================================

            if (args[0].equalsIgnoreCase("set")) {

                soulManager.setSouls(target, amount);

                sender.sendMessage(color(
                        "&a&lSOULS &7&l| &fSet &b"
                                + target.getName()
                                + "&f's balance to &b"
                                + format(amount)
                                + " Souls&f."
                ));

                target.sendMessage(color(
                        "&b&lSOULS &7&l| &fYour balance was set to &b"
                                + format(amount)
                                + " Souls&f."
                ));

                return true;
            }

            // ================================
            // TAKE
            // ================================

            if (args[0].equalsIgnoreCase("take")) {

                if (amount == 0) {
                    sender.sendMessage(color(
                            "&c&lSOULS &7&l| &fAmount must be greater than 0."
                    ));
                    return true;
                }

                long currentBalance = soulManager.getSouls(target);

                long amountToTake = Math.min(
                        amount,
                        currentBalance
                );

                soulManager.removeSouls(
                        target,
                        amountToTake
                );

                sender.sendMessage(color(
                        "&a&lSOULS &7&l| &fTook &b"
                                + format(amountToTake)
                                + " Souls &ffrom &b"
                                + target.getName()
                                + "&f."
                ));

                target.sendMessage(color(
                        "&b&lSOULS &7&l| &f"
                                + format(amountToTake)
                                + " Souls &fwere removed from your balance."
                ));

                return true;
            }
        }

        // ================================
        // UNKNOWN COMMAND
        // ================================

        sender.sendMessage(color(
                "&b&lSOULS &7&l| &f/souls"
        ));

        sender.sendMessage(color(
                "&b&lSOULS &7&l| &f/souls pay <player> <amount>"
        ));

        if (sender.hasPermission(ADMIN_PERMISSION)) {

            sender.sendMessage(color(
                    "&b&lSOULS &7&l| &f/souls give <player> <amount>"
            ));

            sender.sendMessage(color(
                    "&b&lSOULS &7&l| &f/souls set <player> <amount>"
            ));

            sender.sendMessage(color(
                    "&b&lSOULS &7&l| &f/souls take <player> <amount>"
            ));
        }

        return true;
    }

    // ================================
    // PARSE AMOUNT
    // ================================

    private Long parseAmount(String input) {

        try {

            /*
             * Allows players to enter:
             *
             * 100000
             * 100,000
             */

            String cleaned = input.replace(",", "");

            return Long.parseLong(cleaned);

        } catch (NumberFormatException exception) {

            return null;
        }
    }

    // ================================
    // FORMAT
    // ================================

    private String format(long amount) {
        return String.format("%,d", amount);
    }

    // ================================
    // COLOR
    // ================================

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }
}
