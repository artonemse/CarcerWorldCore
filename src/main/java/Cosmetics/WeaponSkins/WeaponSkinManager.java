package Cosmetics.WeaponSkins;

import org.bukkit.entity.Player;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class WeaponSkinManager {

    private final CarcerWorldCore plugin;

    public WeaponSkinManager(CarcerWorldCore plugin) {
        this.plugin = plugin;
    }

    public boolean owns(Player player, WeaponSkin skin) {
        return plugin.getPlayerDataManager().ownsWeaponSkin(player.getUniqueId(), skin.getId());
    }

    public void unlock(Player player, WeaponSkin skin) {
        plugin.getPlayerDataManager().setWeaponSkinOwned(player.getUniqueId(), skin.getId(), true);
    }

    public void remove(Player player, WeaponSkin skin) {
        plugin.getPlayerDataManager().setWeaponSkinOwned(player.getUniqueId(), skin.getId(), false);

        if (getSelected(player) == skin) setSelected(player, null);
    }

    public WeaponSkin getSelected(Player player) {
        String id = plugin.getPlayerDataManager().getSelectedWeaponSkin(player.getUniqueId());

        if (id == null || id.equalsIgnoreCase("none")) return null;

        return WeaponSkin.fromId(id);
    }

    public void setSelected(Player player, WeaponSkin skin) {
        String id = skin == null ? "none" : skin.getId();

        plugin.getPlayerDataManager().setSelectedWeaponSkin(player.getUniqueId(), id);
        plugin.getWeaponManager().giveOrUpdateWeapon(player);
    }

    public boolean isSelected(Player player, WeaponSkin skin) {
        return getSelected(player) == skin;
    }
}
