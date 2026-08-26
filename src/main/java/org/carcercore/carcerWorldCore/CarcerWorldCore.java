package org.carcercore.carcerWorldCore;

import Ascension.AscensionGUI;
import Ascension.AscensionGUIListener;
import Ascension.AscensionManager;
import Commands.CarcerAdminCommand;
import Commands.ResetSeasonCommand;
import Currencies.SoulManager;
import Currencies.SoulsCommand;
import Enchantments.EnchantGUI;
import Enchantments.EnchantGUIListener;
import Enchantments.EnchantManager;
import PlayerData.PlayerDataListener;
import PlayerData.PlayerDataManager;
import Skills.SkillListener;
import Skills.SkillManager;
import Skills.SkillsGUI;
import Skills.SkillsGUIListener;
import Weapons.*;
import org.bukkit.plugin.java.JavaPlugin;


public final class CarcerWorldCore extends JavaPlugin {

    private static CarcerWorldCore instance;

    private PlayerDataManager playerDataManager;

    private SkillManager skillManager;
    private SkillsGUI skillsGUI;

    private WeaponManager weaponManager;
    private WeaponProgressionManager weaponProgressionManager;
    private WeaponMenu weaponMenu;

    private SoulManager soulManager;

    private EnchantManager enchantManager;
    private EnchantGUI enchantGUI;

    private AscensionManager ascensionManager;
    private AscensionGUI ascensionGUI;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("carcer").setExecutor(new CarcerAdminCommand(this));
        getCommand("resetseason").setExecutor(new ResetSeasonCommand(this));

        // ================================
        // PLAYER DATA
        // ================================

        playerDataManager = new PlayerDataManager(this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager), this);

        // ================================
        // SOULS
        // ================================

        soulManager = new SoulManager(this);
        getCommand("souls").setExecutor(new SoulsCommand(this));




        // ================================
        // ENCHANTMENTS
        // ================================

        enchantManager =
                new EnchantManager(this);

        enchantGUI =
                new EnchantGUI(
                        this,
                        enchantManager
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new EnchantGUIListener(
                                enchantManager,
                                enchantGUI
                        ),
                        this
                );

        // ================================
        // SKILLS
        // ================================

        skillManager = new SkillManager(this);

        skillsGUI = new SkillsGUI(
                this,
                skillManager
        );

        getServer().getPluginManager().registerEvents(
                new SkillListener(
                        this,
                        skillManager
                ),
                this
        );

        getServer().getPluginManager().registerEvents(
                new SkillsGUIListener(
                        skillManager,
                        skillsGUI
                ),
                this
        );

        // ================================
        // WEAPON SYSTEM
        // ================================

        weaponManager = new WeaponManager(this);
        weaponProgressionManager = new WeaponProgressionManager(this, weaponManager);

        weaponMenu = new WeaponMenu(this);


        // ================================
        // ASCENSION
        // ================================

        ascensionManager = new AscensionManager(this);
        ascensionGUI = new AscensionGUI(this, ascensionManager);

        getServer().getPluginManager().registerEvents(new AscensionGUIListener(this, ascensionManager, ascensionGUI), this);

        getServer().getPluginManager().registerEvents(new WeaponListener(this, weaponManager, weaponMenu), this);
        getServer().getPluginManager().registerEvents(new WeaponMenuListener(), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(this, weaponProgressionManager), this);
        getLogger().info("[CarcerWorldCore] has been enabled!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("[CarcerWorldCore] has been disabled!");
    }

    public static CarcerWorldCore getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public WeaponProgressionManager getWeaponProgressionManager() {
        return weaponProgressionManager;
    }

    public WeaponMenu getWeaponMenu() {
        return weaponMenu;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillsGUI getSkillsGUI() {
        return skillsGUI;
    }

    public SoulManager getSoulManager() {
        return soulManager;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public EnchantGUI getEnchantGUI() {
        return enchantGUI;
    }

    public AscensionManager getAscensionManager() {
        return ascensionManager;
    }

    public AscensionGUI getAscensionGUI() {
        return ascensionGUI;
    }
}
