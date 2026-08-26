package Currencies;

import PlayerData.PlayerData;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;


public class SoulManager {

    private final CarcerWorldCore plugin;

    public SoulManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public long getSouls(Player player) {
        return plugin.getPlayerDataManager()
                .getPlayerData(player)
                .getSouls();
    }

    public void addSouls(Player player, long amount) {
        if (amount <= 0) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        data.addSouls(amount);

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());
    }

    public boolean removeSouls(Player player, long amount) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        if (!data.removeSouls(amount)) {
            return false;
        }

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());

        return true;
    }

    public boolean hasSouls(Player player, long amount) {
        return getSouls(player) >= amount;
    }

    public void setSouls(Player player, long amount) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayerData(player);

        data.setSouls(amount);

        plugin.getPlayerDataManager()
                .savePlayerData(player.getUniqueId());
    }
}
