package Currencies;

import PlayerData.PlayerData;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class ScrapManager {

    private final CarcerWorldCore plugin;

    public ScrapManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public long getScraps(Player player) {
        return plugin.getPlayerDataManager().getPlayerData(player).getScraps();
    }

    public void addScraps(Player player, long amount) {
        if (amount <= 0) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        data.addScraps(amount);

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    public boolean removeScraps(Player player, long amount) {
        if (amount <= 0) return false;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (!data.removeScraps(amount)) return false;

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        return true;
    }

    public boolean hasScraps(Player player, long amount) {
        return getScraps(player) >= amount;
    }

    public void setScraps(Player player, long amount) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        data.setScraps(amount);

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }
}
