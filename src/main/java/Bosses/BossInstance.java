package Bosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BossInstance {

    protected final CarcerWorldCore plugin;
    protected final BossManager bossManager;
    protected final BossType type;
    protected final UUID ownerId;
    protected final Location center;

    protected LivingEntity bossEntity;
    protected BossBar bossBar;

    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<UUID> trackedEntities = new ArrayList<>();

    private BukkitTask heartbeatTask;
    private int ageTicks = 0;
    private boolean finished = false;

    protected BossInstance(CarcerWorldCore plugin, BossManager bossManager, BossType type, Player owner, Location center) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.type = type;
        this.ownerId = owner.getUniqueId();
        this.center = center.clone();
    }

    public final void start() {
        Player owner = getOwner();

        if (owner == null) return;

        bossEntity = spawnBoss();

        if (bossEntity == null) return;

        bossBar = Bukkit.createBossBar(color(type.getDisplayName()), BarColor.GREEN, BarStyle.SEGMENTED_10);
        bossBar.setVisible(true);
        bossBar.addPlayer(owner);

        onStart();

        heartbeatTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (finished) return;

            Player player = getOwner();

            if (player == null || !player.isOnline()) {
                fail(null);
                return;
            }

            if (player.isDead()) {
                fail("&cYou were defeated by " + type.getDisplayName() + "&c.");
                return;
            }

            if (bossEntity == null || !bossEntity.isValid() || bossEntity.isDead()) return;

            if (!player.getWorld().equals(center.getWorld())) {
                fail("&cYou left the boss encounter.");
                return;
            }

            if (player.getLocation().distanceSquared(center) > type.getArenaRadius() * type.getArenaRadius()) {
                fail("&cYou left the boss arena.");
                return;
            }

            if (bossEntity.getLocation().distanceSquared(center) > type.getArenaRadius() * type.getArenaRadius()) {
                bossEntity.teleport(center.clone().add(0, 0, 5));
            }

            updateBossBar();
            tick(ageTicks);

            ageTicks++;
        }, 0L, 1L);

        trackTask(heartbeatTask);
    }

    protected abstract LivingEntity spawnBoss();

    protected abstract void tick(int ageTicks);

    protected void onStart() {
    }

    protected void onDefeated(Player killer) {
    }

    protected void onCleanup() {
    }

    public final void defeated(Player killer) {
        if (finished) return;

        finished = true;

        onDefeated(killer);
        cleanup(false);
    }

    public final void fail(String message) {
        if (finished) return;

        finished = true;

        Player owner = getOwner();

        if (owner != null && message != null) owner.sendMessage(color(message));

        cleanup(true);
    }

    private void cleanup(boolean removeBoss) {
        onCleanup();

        for (BukkitTask task : new ArrayList<>(tasks)) {
            if (task != null && !task.isCancelled()) task.cancel();
        }

        tasks.clear();

        for (UUID entityId : trackedEntities) {
            Entity entity = Bukkit.getEntity(entityId);

            if (entity != null && entity.isValid()) entity.remove();
        }

        trackedEntities.clear();

        if (removeBoss && bossEntity != null && bossEntity.isValid() && !bossEntity.isDead()) bossEntity.remove();

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }

        bossManager.unregister(this);
    }

    private void updateBossBar() {
        if (bossBar == null || bossEntity == null) return;

        AttributeInstance attribute = bossEntity.getAttribute(Attribute.MAX_HEALTH);

        if (attribute == null) return;

        double maxHealth = attribute.getValue();
        double health = Math.max(0.0, bossEntity.getHealth());
        double progress = maxHealth <= 0 ? 0 : health / maxHealth;

        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
    }

    public void damageOwner(double damage) {
        Player owner = getOwner();

        if (owner == null || owner.isDead()) return;

        owner.damage(damage, bossEntity);
    }

    public Player getOwner() {
        return Bukkit.getPlayer(ownerId);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LivingEntity getBossEntity() {
        return bossEntity;
    }

    public Location getCenter() {
        return center.clone();
    }

    public BossType getType() {
        return type;
    }

    public boolean isFinished() {
        return finished;
    }

    public void trackTask(BukkitTask task) {
        if (task != null) tasks.add(task);
    }

    public void trackEntity(Entity entity) {
        if (entity != null) trackedEntities.add(entity.getUniqueId());
    }

    protected String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
