package Currencies;

import PlayerData.PlayerData;
import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class GemManager {

    private final CarcerWorldCore plugin;

    public GemManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public long getGems(Player player) {
        return plugin.getPlayerDataManager().getPlayerData(player).getGems();
    }

    public void setGems(Player player, long amount) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        data.setGems(amount);
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    public void addGems(Player player, long amount) {
        if (amount <= 0) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        data.addGems(amount);

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
    }

    public boolean removeGems(Player player, long amount) {
        if (amount <= 0) return false;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (!data.removeGems(amount)) return false;

        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        return true;
    }

    public boolean hasGems(Player player, long amount) {
        if (amount < 0) return false;
        return getGems(player) >= amount;
    }
}
