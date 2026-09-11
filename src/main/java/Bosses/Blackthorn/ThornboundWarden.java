package Bosses.Blackthorn;

import Bosses.BossInstance;
import Bosses.BossManager;
import Bosses.BossType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.persistence.PersistentDataType;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

public class ThornboundWarden extends BossInstance {

    private final ThornboundWardenAbilities abilities;

    private int phase = 1;

    private int nextEruption = 60;
    private int nextCharge = 140;
    private int nextRootPrison = 180;
    private int nextSummon = 240;
    private int nextThornstorm = 220;
    private int nextShockwave = 180;

    public ThornboundWarden(CarcerWorldCore plugin, BossManager bossManager, Player owner, Location center) {
        super(plugin, bossManager, BossType.THORNBOUND_WARDEN, owner, center);
        this.abilities = new ThornboundWardenAbilities(plugin, this);
    }

    @Override
    protected LivingEntity spawnBoss() {
        Location spawn = center.clone().add(0, 0, 6);

        Warden warden = spawn.getWorld().spawn(spawn, Warden.class);

        warden.setCustomName(color("&2&lThe Thornbound Warden"));
        warden.setCustomNameVisible(true);
        warden.setPersistent(true);
        warden.setRemoveWhenFarAway(false);
        warden.setCanPickupItems(false);

        setAttribute(warden, Attribute.MAX_HEALTH, type.getMaxHealth());
        setAttribute(warden, Attribute.ATTACK_DAMAGE, 8.0);
        setAttribute(warden, Attribute.MOVEMENT_SPEED, 0.25);
        setAttribute(warden, Attribute.KNOCKBACK_RESISTANCE, 0.90);
        setAttribute(warden, Attribute.FOLLOW_RANGE, 45.0);

        warden.setHealth(type.getMaxHealth());

        NamespacedKey bossKey = new NamespacedKey(plugin, "boss_entity");
        NamespacedKey bossTypeKey = new NamespacedKey(plugin, "boss_type");
        NamespacedKey mobTypeKey = new NamespacedKey(plugin, "mob_type_id");
        NamespacedKey ignoreMobSystemsKey = new NamespacedKey(plugin, "boss_ignore_standard_mob_systems");
        NamespacedKey noRewardKey = new NamespacedKey(plugin, "boss_no_auto_rewards");

        warden.getPersistentDataContainer().set(bossKey, PersistentDataType.BYTE, (byte) 1);
        warden.getPersistentDataContainer().set(bossTypeKey, PersistentDataType.STRING, type.getId());
        warden.getPersistentDataContainer().set(mobTypeKey, PersistentDataType.STRING, type.getId());
        warden.getPersistentDataContainer().set(ignoreMobSystemsKey, PersistentDataType.BYTE, (byte) 1);
        warden.getPersistentDataContainer().set(noRewardKey, PersistentDataType.BYTE, (byte) 1);

        Player owner = getOwner();

        if (owner != null) warden.setAnger(owner, 150);

        return warden;
    }

    @Override
    protected void onStart() {
        Player owner = getOwner();

        if (owner == null) return;

        owner.sendMessage("");
        owner.sendMessage(color("&2&lTHE THORNBOUND WARDEN"));
        owner.sendMessage(color("&7&l| &fThe guardian of the corrupted grove awakens."));
        owner.sendMessage("");

        owner.playSound(owner.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.4f, 0.65f);
        owner.playSound(owner.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.75f);

        Location location = bossEntity.getLocation().clone().add(0, 1, 0);

        center.getWorld().spawnParticle(Particle.SCULK_SOUL, location, 45, 1.6, 1.8, 1.6, 0.03);
        center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 70, 1.5, 1.5, 1.5, 0.08);
        center.getWorld().spawnParticle(Particle.COMPOSTER, location, 60, 1.8, 1.5, 1.8, 0.12);
    }

    @Override
    protected void tick(int ageTicks) {
        Player owner = getOwner();

        if (owner == null || bossEntity == null) return;

        if (bossEntity instanceof Warden warden) warden.setAnger(owner, 150);

        updatePhase();

        if (ageTicks >= nextEruption) {
            abilities.thornEruption();
            nextEruption = ageTicks + getEruptionInterval();
        }

        if (ageTicks >= nextCharge) {
            abilities.wardenCharge();
            nextCharge = ageTicks + getChargeInterval();
        }

        if (phase >= 2 && ageTicks >= nextRootPrison) {
            abilities.rootPrison();
            nextRootPrison = ageTicks + getRootInterval();
        }

        if (phase >= 2 && ageTicks >= nextSummon) {
            abilities.summonCorrupted();
            nextSummon = ageTicks + 240;
        }

        if (phase >= 3 && ageTicks >= nextThornstorm) {
            abilities.thornstorm();
            nextThornstorm = ageTicks + 190;
        }

        if (phase >= 3 && ageTicks >= nextShockwave) {
            abilities.shockwave();
            nextShockwave = ageTicks + 150;
        }
    }

    private void updatePhase() {
        AttributeInstance maxHealthAttribute = bossEntity.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealthAttribute == null) return;

        double healthPercent = bossEntity.getHealth() / maxHealthAttribute.getValue();

        if (phase == 1 && healthPercent <= 0.70) {
            phase = 2;
            enterPhaseTwo();
        }

        if (phase == 2 && healthPercent <= 0.35) {
            phase = 3;
            enterPhaseThree();
        }
    }

    private void enterPhaseTwo() {
        Player owner = getOwner();

        if (owner != null) {
            owner.sendMessage(color("&2&lWARDEN &8» &aThe corruption spreads through the arena!"));
            owner.playSound(owner.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.2f, 0.85f);
        }

        Location location = bossEntity.getLocation().clone().add(0, 1, 0);

        bossEntity.getWorld().spawnParticle(Particle.SCULK_SOUL, location, 70, 2.5, 2.0, 2.5, 0.05);
        bossEntity.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 90, 2.5, 2.0, 2.5, 0.10);
        bossEntity.getWorld().spawnParticle(Particle.COMPOSTER, location, 100, 3.0, 1.5, 3.0, 0.15);

        abilities.shockwave();
    }

    private void enterPhaseThree() {
        Player owner = getOwner();

        if (owner != null) {
            owner.sendMessage(color("&4&lENRAGED &8» &cThe Thornbound Warden unleashes the heart of the grove!"));
            owner.playSound(owner.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.5f, 1.2f);
        }

        setAttribute(bossEntity, Attribute.MOVEMENT_SPEED, 0.30);
        setAttribute(bossEntity, Attribute.ATTACK_DAMAGE, 10.0);

        Location location = bossEntity.getLocation().clone().add(0, 1, 0);

        bossEntity.getWorld().spawnParticle(Particle.SCULK_SOUL, location, 120, 3.5, 2.5, 3.5, 0.08);
        bossEntity.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 140, 4.0, 2.5, 4.0, 0.14);
        bossEntity.getWorld().spawnParticle(Particle.COMPOSTER, location, 140, 4.0, 2.0, 4.0, 0.2);

        abilities.thornstorm();
        abilities.shockwave();
    }

    @Override
    protected void onDefeated(Player killer) {
        Location location = bossEntity.getLocation().clone();

        location.getWorld().strikeLightningEffect(location);
        location.getWorld().strikeLightningEffect(location.clone().add(2, 0, 1));
        location.getWorld().strikeLightningEffect(location.clone().add(-2, 0, -1));

        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.65f);
        location.getWorld().playSound(location, Sound.ENTITY_WARDEN_DEATH, 1.2f, 0.65f);

        abilities.playDeathAnimation(location);

        if (killer != null) {
            killer.sendMessage("");
            killer.sendMessage(color("&a&lTHE THORNBOUND WARDEN HAS FALLEN"));
            killer.sendMessage(color("&7&l| &fThe corruption around Blackthorn begins to weaken."));
            killer.sendMessage("");
        }
    }

    private int getEruptionInterval() {
        if (phase == 3) return 70;
        if (phase == 2) return 90;

        return 110;
    }

    private int getChargeInterval() {
        return phase == 3 ? 130 : 160;
    }

    private int getRootInterval() {
        return phase == 3 ? 130 : 170;
    }

    private void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance instance = entity.getAttribute(attribute);

        if (instance != null) instance.setBaseValue(value);
    }

    public int getPhase() {
        return phase;
    }
}