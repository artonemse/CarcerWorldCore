package org.carcercore.carcerWorldCore;

import Armor.*;
import Armor.Generic.GenericArmorGenerator;
import Armor.Salvage.SalvageGUI;
import Armor.Salvage.SalvageGUIListener;
import Armor.Salvage.SalvageManager;
import Ascension.AscensionGUI;
import Ascension.AscensionGUIListener;
import Ascension.AscensionManager;
import Commands.*;
import Commands.GemsCommand;
import Cosmetics.CosmeticsGUI;
import Cosmetics.CosmeticsGUIListener;
import Cosmetics.KillEffects.KillEffectGUI;
import Cosmetics.KillEffects.KillEffectGUIListener;
import Cosmetics.KillEffects.KillEffectListener;
import Cosmetics.KillEffects.KillEffectManager;
import Cosmetics.Trails.TrailGUI;
import Cosmetics.Trails.TrailGUIListener;
import Cosmetics.Trails.TrailManager;
import Currencies.GemManager;
import Currencies.ScrapManager;
import Currencies.SoulManager;
import Currencies.SoulsCommand;
import Enchantments.EnchantGUI;
import Enchantments.EnchantGUIListener;
import Enchantments.EnchantManager;
import Listeners.*;
import Locations.NamedLocationListener;
import Locations.NamedLocationManager;
import Locations.SafeZoneListener;
import MobScaling.MobHealthBarListener;
import MobScaling.MobHealthBarManager;
import MobScaling.MobScalingManager;
import MobScaling.NightMobSpawner;
import MobZones.MobZoneManager;
import PlayerData.PlayerDataListener;
import PlayerData.PlayerDataManager;
import Quests.QuestKillListener;
import Quests.QuestManager;
import Skills.SkillListener;
import Skills.SkillManager;
import Skills.SkillsGUI;
import Skills.SkillsGUIListener;
import Warps.WarpManager;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import Weapons.*;
import org.bukkit.plugin.java.JavaPlugin;
import MobRewards.MobSoulRewardListener;
import MobRewards.MobSoulRewardManager;
import NPCs.NPCInteractionListener;
import NPCs.NPCManager;
import Quests.QuestGUI;
import Quests.QuestGUIListener;
import Quests.QuestLocationListener;
import Quests.QuestPlayerListener;
import Quests.QuestsCommand;

public final class CarcerWorldCore extends JavaPlugin {

    private static CarcerWorldCore instance;
    private WarpManager warpManager;


    private QuestGUI questGUI;

    //Armor stuff
    private GenericArmorGenerator genericArmorGenerator;
    private ArmorManager armorManager;
    private CombatHealthBarManager combatHealthBarManager;

    //Mob Zone
    private MobZoneManager mobZoneManager;

    //Location Data
    private NamedLocationManager namedLocationManager;

    // Player Data
    private PlayerDataManager playerDataManager;

    // Currencies
    private SoulManager soulManager;
    private GemManager gemManager;

    // Skills
    private SkillManager skillManager;
    private SkillsGUI skillsGUI;

    // Enchantments
    private EnchantManager enchantManager;
    private EnchantGUI enchantGUI;

    // Weapons
    private WeaponManager weaponManager;
    private WeaponProgressionManager weaponProgressionManager;
    private WeaponMenu weaponMenu;

    // Ascension
    private AscensionManager ascensionManager;
    private AscensionGUI ascensionGUI;

    // Mobs
    private MobScalingManager mobScalingManager;
    private MobHealthBarManager mobHealthBarManager;
    private NightMobSpawner nightMobSpawner;
    private MobSoulRewardManager mobSoulRewardManager;

    // NPCs
    private NPCManager npcManager;
    // Quests
    private QuestManager questManager;

    // Cosmetics
    // Cosmetics
    private KillEffectManager killEffectManager;
    private KillEffectGUI killEffectGUI;
    private CosmeticsGUI cosmeticsGUI;

    private TrailManager trailManager;
    private TrailGUI trailGUI;

    private ScrapManager scrapManager;
    private SalvageManager salvageManager;
    private SalvageGUI salvageGUI;





    @Override
    public void onEnable() {
        instance = this;

        // ================================
        // NPC SYSTEM
        // ================================
        npcManager = new NPCManager(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        // ================================
        // LOCATION DATA
        // ================================
        namedLocationManager = new NamedLocationManager(this);

        getServer().getPluginManager().registerEvents(new NamedLocationListener(namedLocationManager), this);
        getServer().getPluginManager().registerEvents(new SafeZoneListener(this), this);



        // ================================
        // PLAYER DATA
        // ================================
        playerDataManager = new PlayerDataManager(this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager), this);

        // ================================
        // CURRENCIES
        // ================================
        soulManager = new SoulManager(this);
        gemManager = new GemManager(this);
        scrapManager = new ScrapManager(this);


        getCommand("souls").setExecutor(new SoulsCommand(this));
        getCommand("gems").setExecutor(new GemsCommand(gemManager));
        getCommand("scraps").setExecutor(new ScrapsCommand(scrapManager));


        // ================================
        // QUEST SYSTEM
        // ================================
        questManager = new QuestManager(this);
        questGUI = new QuestGUI(questManager);

        getServer().getPluginManager().registerEvents(new NPCInteractionListener(npcManager, questManager), this);
        getServer().getPluginManager().registerEvents(new QuestKillListener(questManager), this);
        getServer().getPluginManager().registerEvents(new QuestPlayerListener(this, questManager), this);
        getServer().getPluginManager().registerEvents(new QuestLocationListener(questManager, namedLocationManager), this);
        getServer().getPluginManager().registerEvents(new QuestGUIListener(questGUI), this);

        getCommand("quests").setExecutor(new QuestsCommand(questGUI));
        // ================================
        // SKILLS
        // ================================
        skillManager = new SkillManager(this);
        skillsGUI = new SkillsGUI(this, skillManager);

        getServer().getPluginManager().registerEvents(new SkillListener(this, skillManager), this);
        getServer().getPluginManager().registerEvents(new SkillsGUIListener(skillManager, skillsGUI), this);

        // ================================
        // ENCHANTMENTS
        // ================================
        enchantManager = new EnchantManager(this);
        enchantGUI = new EnchantGUI(this, enchantManager);

        getServer().getPluginManager().registerEvents(new EnchantGUIListener(enchantManager, enchantGUI), this);

        // ================================
        // WEAPON SYSTEM
        // ================================
        weaponManager = new WeaponManager(this);
        weaponProgressionManager = new WeaponProgressionManager(this, weaponManager);
        weaponMenu = new WeaponMenu(this);

        getServer().getPluginManager().registerEvents(new WeaponListener(this, weaponManager, weaponMenu), this);
        getServer().getPluginManager().registerEvents(new WeaponMenuListener(), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(this, weaponProgressionManager), this);

        // ================================
        // ASCENSION
        // ================================
        ascensionManager = new AscensionManager(this);
        ascensionGUI = new AscensionGUI(this, ascensionManager);

        getServer().getPluginManager().registerEvents(new AscensionGUIListener(this, ascensionManager, ascensionGUI), this);


        // ================================
        // MOB SYSTEM
        // ================================
        mobSoulRewardManager = new MobSoulRewardManager(this);

        getServer().getPluginManager().registerEvents(new MobSoulRewardListener(this, mobSoulRewardManager), this);
        mobHealthBarManager = new MobHealthBarManager(this);
        mobScalingManager = new MobScalingManager(this);
        mobZoneManager = new MobZoneManager(this);
        nightMobSpawner = new NightMobSpawner(this);


        getServer().getPluginManager().registerEvents(new MobHealthBarListener(this, mobHealthBarManager), this);
        getServer().getPluginManager().registerEvents(new MobSpawnCancelListener(), this);
        getServer().getPluginManager().registerEvents(new MobDropListener(), this);

        mobScalingManager.start();
        nightMobSpawner.start();

        // ================================
        // COSMETICS
        // ================================
        cosmeticsGUI = new CosmeticsGUI();

        // Kill Effects
        killEffectManager = new KillEffectManager(this);
        killEffectGUI = new KillEffectGUI(this, killEffectManager);

        // Trails
        trailManager = new TrailManager(this);
        trailGUI = new TrailGUI(this, trailManager);

        // Main Cosmetics GUI
        getServer().getPluginManager().registerEvents(
                new CosmeticsGUIListener(this),
                this
        );

        // Kill Effects
        getServer().getPluginManager().registerEvents(
                new KillEffectGUIListener(killEffectManager, killEffectGUI),
                this
        );

        getServer().getPluginManager().registerEvents(
                new KillEffectListener(killEffectManager),
                this
        );

        // Trails
        getServer().getPluginManager().registerEvents(
                new TrailGUIListener(trailManager, trailGUI),
                this
        );

        // ================================
        // WORLD PROTECTION
        // ================================
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(), this);

        //ARMOR STUFF
        genericArmorGenerator = new GenericArmorGenerator(this);
        armorManager = new ArmorManager(this, genericArmorGenerator);

        combatHealthBarManager = new CombatHealthBarManager(this);
        getServer().getPluginManager().registerEvents(new ArmorListener(this, armorManager, genericArmorGenerator, combatHealthBarManager), this);
        getServer().getPluginManager().registerEvents(new ArmorCombatListener(armorManager, combatHealthBarManager), this);
        getServer().getPluginManager().registerEvents(new ArmorDropListener(this, armorManager, genericArmorGenerator), this);

        salvageManager = new SalvageManager(genericArmorGenerator, scrapManager);
        salvageGUI = new SalvageGUI(salvageManager, scrapManager);

        getServer().getPluginManager().registerEvents(new SalvageGUIListener(salvageManager, salvageGUI), this);

        // ================================
        // ADMIN COMMANDS
        // ================================
        warpManager = new WarpManager(this);
        getCommand("carcer").setExecutor(new CarcerAdminCommand(this));
        getCommand("resetseason").setExecutor(new ResetSeasonCommand(this));
        getCommand("setwarp").setExecutor(new SetWarpCommand(warpManager));
        getCommand("delwarp").setExecutor(new DelWarpCommand(warpManager));


        getCommand("warp").setExecutor(new WarpCommand(warpManager));
        getLogger().info("[CarcerWorldCore] has been enabled!");
    }


    @Override
    public void onDisable() {
        if (playerDataManager != null) playerDataManager.saveAll();
        if (trailManager != null) trailManager.save();
        if (combatHealthBarManager != null) {
            combatHealthBarManager.shutdown();
        }
        if (questManager != null) questManager.saveAll();

        getLogger().info("[CarcerWorldCore] has been disabled!");
    }



    public NPCManager getNPCManager() {
        return npcManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public QuestGUI getQuestGUI() {
        return questGUI;
    }

    public ScrapManager getScrapManager() {
        return scrapManager;
    }

    public SalvageManager getSalvageManager() {
        return salvageManager;
    }

    public SalvageGUI getSalvageGUI() {
        return salvageGUI;
    }

    // ================================
    // INSTANCE
    // ================================

    public static CarcerWorldCore getInstance() {
        return instance;
    }

    // ================================
    // PLAYER DATA
    // ================================

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    // ================================
    // CURRENCIES
    // ================================

    public SoulManager getSoulManager() {
        return soulManager;
    }

    public GemManager getGemManager() {
        return gemManager;
    }

    // ================================
    // SKILLS
    // ================================

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillsGUI getSkillsGUI() {
        return skillsGUI;
    }

    // ================================
    // ENCHANTMENTS
    // ================================

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public EnchantGUI getEnchantGUI() {
        return enchantGUI;
    }

    // ================================
    // WEAPONS
    // ================================

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public WeaponProgressionManager getWeaponProgressionManager() {
        return weaponProgressionManager;
    }

    public WeaponMenu getWeaponMenu() {
        return weaponMenu;
    }

    // ================================
    // ASCENSION
    // ================================

    public AscensionManager getAscensionManager() {
        return ascensionManager;
    }

    public AscensionGUI getAscensionGUI() {
        return ascensionGUI;
    }

    // ================================
    // MOBS
    // ================================
    public MobSoulRewardManager getMobSoulRewardManager() {
        return mobSoulRewardManager;
    }

    public MobScalingManager getMobScalingManager() {
        return mobScalingManager;
    }

    public MobHealthBarManager getMobHealthBarManager() {
        return mobHealthBarManager;
    }

    public MobZoneManager getMobZoneManager() {
        return mobZoneManager;
    }

    //ARMOR STUFF
    public GenericArmorGenerator getGenericArmorGenerator() {
        return genericArmorGenerator;
    }

    public ArmorManager getArmorManager() {
        return armorManager;
    }

    public CombatHealthBarManager getCombatHealthBarManager() {
        return combatHealthBarManager;
    }


    // ================================
    // COSMETICS
    // ================================

    public KillEffectManager getKillEffectManager() {
        return killEffectManager;
    }

    public KillEffectGUI getKillEffectGUI() {
        return killEffectGUI;
    }

    public CosmeticsGUI getCosmeticsGUI() {
        return cosmeticsGUI;
    }
    public TrailManager getTrailManager() {
        return trailManager;
    }
    public TrailGUI getTrailGUI() {
        return trailGUI;
    }


    public WarpManager getWarpManager() {
        return warpManager;
    }


    public NamedLocationManager getNamedLocationManager() {
        return namedLocationManager;
    }
}