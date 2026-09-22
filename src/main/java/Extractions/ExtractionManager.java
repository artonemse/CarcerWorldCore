package Extractions;

import Cosmetics.Chat.ChatCosmetic;
import Cosmetics.Chat.ChatCosmetics;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;

public final class ExtractionManager {

    static final class Lobby {

        final UUID leader;
        final Set<UUID> members = new LinkedHashSet<>();
        final Set<UUID> ready = new HashSet<>();

        Lobby(UUID leader) {
            this.leader = leader;
            members.add(leader);
        }
    }

    private record Invitation(UUID leader, long expires) {}

    private final CarcerWorldCore plugin;
    private final File configFile;
    private final File statsFile;
    private final YamlConfiguration config;
    private final YamlConfiguration stats;

    private final Map<UUID, Lobby> lobbies = new HashMap<>();
    private final Map<UUID, Invitation> invitations = new HashMap<>();
    private final Map<UUID, ExtractionRun> players = new HashMap<>();
    private final Map<UUID, ExtractionRun> runs = new HashMap<>();
    private final Map<Chunk, Integer> tickets = new HashMap<>();
    private final Map<UUID, Location> permittedTeleports = new HashMap<>();

    final NamespacedKey markerKey;

    public ExtractionManager(CarcerWorldCore plugin) throws Exception {
        this.plugin = plugin;
        markerKey = new NamespacedKey(plugin, "extraction_marker");
        configFile = new File(plugin.getDataFolder(), "extractions.yml");
        statsFile = new File(plugin.getDataFolder(), "extraction-data.yml");
        config = new YamlConfiguration();
        stats = new YamlConfiguration();

        if (configFile.exists()) config.load(configFile);
        if (statsFile.exists()) stats.load(statsFile);

        config.addDefault("broker-ids", List.of());
        config.addDefault("duration-seconds", 1200);
        config.addDefault("recovery-seconds", 15);
        config.addDefault("channel-seconds", 20);
        config.addDefault("minimum-shrine-distance", 500);
        config.addDefault("minimum-exit-distance", 250);
        config.addDefault("maximum-active-runs", 12);
        config.addDefault("rewards.souls", 1000);
        config.addDefault("rewards.scraps", 100);
        config.addDefault("rewards.daily-gems", 25);

        config.options().copyDefaults(true);
        config.save(configFile);
    }

    ExtractionRun run(Player p) {
        return players.get(p.getUniqueId());
    }

    boolean active(Player p) {
        return run(p) != null;
    }

    boolean recovering(Player p) {
        return active(p) && run(p).recoveringUntil.containsKey(p.getUniqueId());
    }

    boolean broker(int id) {
        return config.getIntegerList("broker-ids").contains(id);
    }

    Lobby lobby(Player p) {
        return lobbies.get(p.getUniqueId());
    }

    boolean nearBroker(Player p) {
        for (int id : config.getIntegerList("broker-ids")) {
            var npc = CitizensAPI.getNPCRegistry().getById(id);

            if (npc != null && npc.isSpawned() && near(p.getLocation(), npc.getEntity().getLocation(), 8)) return true;
        }

        return false;
    }

    void openLobby(Player p) {
        if (!active(p) && lobby(p) == null) lobbies.put(p.getUniqueId(), new Lobby(p.getUniqueId()));
    }

    void say(Collection<UUID> ids, String message) {
        for (UUID id : ids) {
            Player p = Bukkit.getPlayer(id);

            if (p != null) p.sendMessage("§6§lEXTRACTION §7§l| §f" + message);
        }
    }

    public String invite(Player p, Player target) {
        if (active(p) || !nearBroker(p)) return "Visit a Relic Broker before inviting players.";

        openLobby(p);
        Lobby l = lobby(p);

        if (!l.leader.equals(p.getUniqueId())) return "Only the party leader can invite.";
        if (l.members.size() >= 4) return "The party is full.";
        if (target == null || target.equals(p) || active(target)) return "Choose another available online player.";
        if (l.members.contains(target.getUniqueId())) return "That player is already in your party.";

        invitations.put(target.getUniqueId(), new Invitation(l.leader, System.currentTimeMillis() + 60000));
        target.sendMessage("§e" + p.getName() + " invited you to an extraction. Use §f/extraction accept§e within 60 seconds.");

        return "Invitation sent to " + target.getName() + ".";
    }

    public String accept(Player p) {
        Invitation invite = invitations.remove(p.getUniqueId());

        if (invite == null || invite.expires() < System.currentTimeMillis()) return "No valid invitation.";

        Lobby l = lobbies.get(invite.leader());

        if (active(p) || l == null || !l.leader.equals(invite.leader()) || l.members.size() >= 4) return "That party is unavailable.";

        Lobby old = lobby(p);

        if (old != null && (old.members.size() != 1 || !old.leader.equals(p.getUniqueId()))) return "Leave your current party first.";
        if (old != null) lobbies.remove(p.getUniqueId());

        l.members.add(p.getUniqueId());
        l.ready.clear();
        lobbies.put(p.getUniqueId(), l);

        say(l.members, p.getName() + " joined. Everyone must click Ready at a Broker.");
        return "Joined the party.";
    }

    public String ready(Player p) {
        if (active(p) || !nearBroker(p)) return "Visit a Broker to ready up.";

        openLobby(p);
        Lobby l = lobby(p);

        if (!l.ready.add(p.getUniqueId())) l.ready.remove(p.getUniqueId());

        say(l.members, p.getName() + (l.ready.contains(p.getUniqueId()) ? " is ready." : " is no longer ready."));
        return "Ready status updated.";
    }

    public void leave(Player p) {
        if (active(p)) {
            finish(run(p), false, p.getName() + " left. The expedition ended.");
            return;
        }

        invitations.remove(p.getUniqueId());
        Lobby l = lobby(p);

        if (l == null) return;

        if (l.leader.equals(p.getUniqueId())) {
            say(l.members, "The leader disbanded the party.");
            l.members.forEach(lobbies::remove);
            invitations.entrySet().removeIf(e -> e.getValue().leader().equals(l.leader));
        } else {
            l.members.remove(p.getUniqueId());
            l.ready.clear();
            lobbies.remove(p.getUniqueId());
            say(l.members, p.getName() + " left. Ready checks reset.");
        }
    }

    public String start(Player p) {
        Lobby l = lobby(p);

        if (active(p) || l == null || !l.leader.equals(p.getUniqueId())) return "Only a lobby leader can start.";
        if (!nearBroker(p)) return "Start at a Relic Broker.";
        if (!l.ready.containsAll(l.members)) return "Everyone must click Ready first, including you.";
        if (runs.size() >= Math.max(1, config.getInt("maximum-active-runs"))) return "All expedition slots are busy.";

        for (UUID id : l.members) {
            Player member = Bukkit.getPlayer(id);

            if (member == null || member.isDead() || active(member) || !near(member.getLocation(), p.getLocation(), 16)) return "Gather every party member within 16 blocks.";
            if (member.getGameMode() != GameMode.SURVIVAL && member.getGameMode() != GameMode.ADVENTURE) return "Everyone must be in Survival or Adventure mode.";
            if (member.isFlying() || member.isGliding() || member.isInsideVehicle() || !safe(member.getLocation())) return "Everyone must stand safely on the ground.";
        }

        Location origin = p.getLocation().clone();
        Location shrine = randomSurface(origin, 490, 510);

        if (shrine == null) return "No safe relic location was found 490–510 blocks away in generated terrain. Try again or use another Broker.";

        Location exit = randomSurface(shrine, 490, 510);

        if (exit == null) return "No safe extraction location was found. No run was started; try again.";

        BossBar bar = Bukkit.createBossBar("Relic Extraction", BarColor.PURPLE, BarStyle.SOLID);
        ExtractionRun r = new ExtractionRun(l.members, shrine, exit, System.currentTimeMillis() + Math.max(60, config.getInt("duration-seconds")) * 1000L, bar);

        runs.put(r.id, r);

        try {
            marker(r);
        } catch (Exception e) {
            removeMarker(r);
            runs.remove(r.id);
            bar.removeAll();
            plugin.getLogger().log(Level.SEVERE, "Could not create extraction marker", e);
            return "Could not create the relic. No expedition was started.";
        }

        for (UUID id : r.members) {
            Player member = Bukkit.getPlayer(id);

            players.put(id, r);
            r.safe.put(id, member.getLocation().clone());
            r.previousCompass.put(id, member.getCompassTarget().clone());
            bar.addPlayer(member);
            lobbies.remove(id);
        }

        invitations.entrySet().removeIf(e -> e.getValue().leader().equals(l.leader));

        say(r.members, "Find the relic at " + coords(r.shrine) + ". Right-click its floating marker to claim it. Two shared lives!");

        return "Expedition started. Use /extraction status for your target.";
    }

    private Location randomSurface(Location center, double minimum, double maximum) {
        World world = center.getWorld();

        if (world == null || world.getEnvironment() != World.Environment.NORMAL) return null;

        Random random = new Random();
        double minimumSquared = minimum * minimum;
        double maximumSquared = maximum * maximum;

        for (int attempt = 0; attempt < 48; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = Math.sqrt(minimumSquared + random.nextDouble() * (maximumSquared - minimumSquared));

            int x = (int) Math.floor(center.getX() + Math.cos(angle) * radius);
            int z = (int) Math.floor(center.getZ() + Math.sin(angle) * radius);

            double dx = x + 0.5 - center.getX();
            double dz = z + 0.5 - center.getZ();
            double actualSquared = dx * dx + dz * dz;

            // Recheck after centering the location on a block.
            if (actualSquared < minimumSquared || actualSquared > maximumSquared) continue;

            Location borderCheck = new Location(world, x + 0.5, center.getY(), z + 0.5);

            if (!world.getWorldBorder().isInside(borderCheck)) continue;

            // Safety checks inspect adjacent blocks, so verify their chunks too.
            // Do not generate new terrain while searching for a destination.
            boolean generated = true;

            for (int cx = (x - 1) >> 4; cx <= (x + 1) >> 4; cx++) {
                for (int cz = (z - 1) >> 4; cz <= (z + 1) >> 4; cz++) {
                    if (!world.isChunkGenerated(cx, cz)) generated = false;
                }
            }

            if (!generated) continue;

            int groundY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            Material ground = world.getBlockAt(x, groundY, z).getType();

            if (!ground.isSolid()) continue;
            if (Tag.LEAVES.isTagged(ground) || Tag.LOGS.isTagged(ground)) continue;

            Location candidate = new Location(world, x + 0.5, groundY + 1, z + 0.5);

            // Reuses the existing clearance, floor, border, and hazard checks.
            if (!safe(candidate)) continue;

            // Avoid placing this objective directly beside another expedition.
            boolean occupied = runs.values().stream().anyMatch(r -> near(candidate, r.target(), 16) || near(candidate, r.exit, 16));

            if (!occupied) return candidate;
        }

        return null;
    }

    private List<Location> points(String section) {
        ConfigurationSection s = config.getConfigurationSection(section);

        if (s == null) return List.of();

        List<Location> result = new ArrayList<>();

        for (String id : s.getKeys(false)) {
            Location at = s.getLocation(id);
            if (at != null && at.getWorld() != null) result.add(at);
        }

        return result;
    }

    public String configure(Player p, String action, String value) throws IOException {
        if (!runs.isEmpty()) return "Wait for active expeditions to finish before changing locations.";

        if (action.equals("broker")) {
            int id = Integer.parseInt(value);

            if (CitizensAPI.getNPCRegistry().getById(id) == null) return "No Citizens NPC has that ID.";

            List<Integer> ids = new ArrayList<>(config.getIntegerList("broker-ids"));

            if (!ids.contains(id)) ids.add(id);
            config.set("broker-ids", ids);
        } else {
            if (!value.matches("[a-z0-9_-]{1,40}")) return "Use a lowercase location ID with letters, numbers, underscores or hyphens.";

            Location at = p.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);

            if (!safe(at)) return "Stand on clear, solid ground first.";

            config.set((action.equals("shrine") ? "shrines." : "exits.") + value, at);
        }

        config.save(configFile);
        return "Saved. Future expeditions can use this " + action + ".";
    }

    static boolean near(Location a, Location b, double distance) {
        return a != null && b != null && a.getWorld().equals(b.getWorld()) && a.distanceSquared(b) <= distance * distance;
    }

    private static boolean far(Location a, Location b, double distance) {
        if (!a.getWorld().equals(b.getWorld())) return false;

        double x = a.getX() - b.getX();
        double z = a.getZ() - b.getZ();

        return x * x + z * z >= distance * distance;
    }

    static boolean safe(Location at) {
        World w = at.getWorld();

        if (w == null || !w.getWorldBorder().isInside(at) || at.getY() <= w.getMinHeight() || at.getY() + 2 >= w.getMaxHeight()) return false;

        Block floor = at.clone().subtract(0, 1, 0).getBlock();

        if (!floor.getType().isSolid() || !floor.getCollisionShape().getBoundingBoxes().stream().anyMatch(box -> box.getMaxY() >= 1)) return false;
        if (!at.getBlock().getType().isAir() || !at.clone().add(0, 1, 0).getBlock().getType().isAir()) return false;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    String type = at.getBlock().getRelative(x, y, z).getType().name();

                    if (Set.of("LAVA", "FIRE", "SOUL_FIRE", "CACTUS", "MAGMA_BLOCK", "CAMPFIRE", "SOUL_CAMPFIRE", "SWEET_BERRY_BUSH", "POWDER_SNOW", "WITHER_ROSE").contains(type)) return false;
                }
            }
        }

        return true;
    }

    private Location safeNear(Location center) {
        for (int radius = 0; radius <= 5; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;

                    for (int dy : new int[]{0, 1, -1, 2, -2}) {
                        Location at = center.getBlock().getLocation().add(x + 0.5, dy, z + 0.5);

                        if (safe(at) && at.getWorld().getNearbyEntities(at, 3, 2, 3).stream().noneMatch(e -> e instanceof Monster)) return at;
                    }
                }
            }
        }

        return null;
    }

    private void marker(ExtractionRun r) {
        removeMarker(r);

        Location at = r.target();
        Chunk chunk = at.getChunk();

        if (!tickets.containsKey(chunk) && !chunk.addPluginChunkTicket(plugin)) throw new IllegalStateException("Could not hold marker chunk.");

        tickets.merge(chunk, 1, Integer::sum);
        r.markerChunk = chunk;

        r.marker = at.getWorld().spawn(at, ArmorStand.class, stand -> {
            stand.setVisibleByDefault(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setCollidable(false);
            stand.setPersistent(false);
            stand.setCustomName(r.carrier == null ? "§d§lLost Relic §7[Right-click]" : "§6§lExtraction Point");
            stand.setCustomNameVisible(true);
            stand.getEquipment().setHelmet(new ItemStack(r.carrier == null ? Material.ENDER_EYE : Material.BEACON));
            stand.getPersistentDataContainer().set(markerKey, PersistentDataType.STRING, r.id.toString());
        });

        for (UUID id : r.members) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.showEntity(plugin, r.marker);
        }
    }

    private void removeMarker(ExtractionRun r) {
        if (r.marker != null) r.marker.remove();
        r.marker = null;

        if (r.markerChunk != null) {
            Chunk c = r.markerChunk;
            int left = tickets.getOrDefault(c, 1) - 1;

            if (left <= 0) {
                tickets.remove(c);
                c.removePluginChunkTicket(plugin);
            } else {
                tickets.put(c, left);
            }

            r.markerChunk = null;
        }
    }

    boolean isMarker(Entity e) {
        return e.getPersistentDataContainer().has(markerKey, PersistentDataType.STRING);
    }

    void cleanStale(Entity entity) {
        String id = entity.getPersistentDataContainer().get(markerKey, PersistentDataType.STRING);

        if (id != null && runs.values().stream().noneMatch(r -> r.marker != null && r.marker.getUniqueId().equals(entity.getUniqueId()))) entity.remove();
    }

    void pickup(Player p, Entity entity) {
        ExtractionRun r = run(p);

        if (r == null || recovering(p) || p.isDead() || r.carrier != null || r.marker == null || !r.marker.equals(entity) || !near(p.getLocation(), r.relic, 3)) return;

        r.carrier = p.getUniqueId();
        r.channelStarted = 0;

        try {
            marker(r);
        } catch (Exception e) {
            finish(r, false, "The marker could not be created. Contact staff.");
            return;
        }

        say(r.members, p.getName() + " carries the cursed relic! Extract at " + coords(r.exit) + ". Everyone must stand within 6 blocks.");
    }

    void died(Player p) {
        ExtractionRun r = run(p);

        if (r == null) return;

        r.channelStarted = 0;

        if (--r.lives <= 0) {
            finish(r, false, "Both shared lives were lost.");
            return;
        }

        Location recovery = safeNear(p.getLocation());

        if (recovery == null) recovery = safeNear(r.safe.get(p.getUniqueId()));

        if (recovery == null) {
            finish(r, false, "No safe recovery position remains.");
            return;
        }

        r.recovery.put(p.getUniqueId(), recovery);
        r.recoveringUntil.put(p.getUniqueId(), System.currentTimeMillis() + Math.max(1, config.getInt("recovery-seconds")) * 1000L);

        if (p.getUniqueId().equals(r.carrier)) {
            r.carrier = null;
            r.relic = recovery.clone();

            try {
                marker(r);
            } catch (Exception e) {
                finish(r, false, "Could not place the dropped relic. Contact staff.");
                return;
            }

            say(r.members, "The relic dropped at " + coords(r.relic) + ". Recover it!");
        }

        say(r.members, p.getName() + " fell. One shared life remains.");

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (p.isOnline() && p.isDead() && run(p) == r) p.spigot().respawn();
        });
    }

    boolean permitted(Player p, Location destination) {
        Location expected = permittedTeleports.get(p.getUniqueId());
        return expected != null && near(expected, destination, 0.01);
    }

    private boolean recoverTeleport(Player p, Location at) {
        permittedTeleports.put(p.getUniqueId(), at.clone());

        try {
            return p.teleport(at);
        } finally {
            permittedTeleports.remove(p.getUniqueId());
        }
    }

    public String status(Player p) {
        ExtractionRun r = run(p);

        if (r == null) {
            Lobby l = lobby(p);
            return l == null ? "Visit Veyr to create a party." : "Party: " + l.members.size() + "/4 | Ready: " + l.ready.size() + "/" + l.members.size();
        }

        return (r.carrier == null ? "Recover relic: " : "Extract: ") + coords(r.target()) + " | Lives: " + r.lives + " | Time: " + Math.max(0, (r.deadline - System.currentTimeMillis()) / 1000) + "s";
    }

    private static String coords(Location at) {
        return at.getWorld().getName() + " " + at.getBlockX() + ", " + at.getBlockY() + ", " + at.getBlockZ();
    }

    public void tick() {
        long now = System.currentTimeMillis();
        invitations.values().removeIf(i -> i.expires() < now);

        for (ExtractionRun r : List.copyOf(runs.values())) {
            try {
                tick(r, now);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Extraction " + r.id + " failed", e);
                finish(r, false, "An extraction error occurred. Staff have been notified.");
            }
        }
    }

    private void tick(ExtractionRun r, long now) {
        if (now >= r.deadline) {
            finish(r, false, "Time expired.");
            return;
        }

        boolean allAtExit = r.carrier != null;

        for (UUID id : r.members) {
            Player p = Bukkit.getPlayer(id);

            if (p == null || !p.getWorld().equals(r.shrine.getWorld())) {
                finish(r, false, "A party member disconnected or left the world.");
                return;
            }

            if (p.isFlying()) p.setFlying(false);
            if (p.isGliding()) p.setGliding(false);
            if (p.isInsideVehicle()) p.leaveVehicle();

            if (r.recoveringUntil.containsKey(id)) {
                allAtExit = false;

                if (p.isDead()) {
                    p.spigot().respawn();
                    continue;
                }

                long until = r.recoveringUntil.get(id);

                if (now < until) {
                    p.sendTitle("§cRecovering", "§f" + ((until - now + 999) / 1000) + " seconds", 0, 25, 0);
                } else {
                    Location target = null;

                    for (UUID mateId : r.members) {
                        Player mate = Bukkit.getPlayer(mateId);

                        if (!mateId.equals(id) && mate != null && !mate.isDead() && !recovering(mate)) {
                            target = safeNear(mate.getLocation());
                            if (target != null) break;
                        }
                    }

                    if (target == null) target = safeNear(r.recovery.get(id));

                    if (target == null) {
                        finish(r, false, "No safe recovery position remains.");
                        return;
                    }

                    if (!recoverTeleport(p, target)) {
                        finish(r, false, "Another plugin blocked recovery.");
                        return;
                    }

                    r.recoveringUntil.remove(id);
                    r.recovery.remove(id);
                    p.setNoDamageTicks(60);
                    p.sendTitle("§aRecovered", "§fOne shared life remains", 0, 40, 10);
                }
            } else if (!p.isDead() && safe(p.getLocation())) {
                r.safe.put(id, p.getLocation().clone());
            }

            if (p.isDead() || recovering(p) || !near(p.getLocation(), r.exit, 6)) allAtExit = false;

            p.setCompassTarget(r.target());

            if (near(p.getLocation(), r.target(), 48)) p.spawnParticle(Particle.END_ROD, r.target().clone().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.01);

            if (id.equals(r.carrier) && !p.isDead()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, true, false, true));
                if (now % 10000 < 2000) p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, true, false, true));
            }
        }

        if (r.marker == null || !r.marker.isValid()) marker(r);

        if (allAtExit) {
            if (r.channelStarted == 0) r.channelStarted = now;

            long duration = Math.max(1, config.getInt("channel-seconds")) * 1000L;
            double progress = Math.min(1, (double) (now - r.channelStarted) / duration);

            r.bar.setTitle("Extracting — stay together: " + ((duration - (now - r.channelStarted) + 999) / 1000) + "s");
            r.bar.setColor(BarColor.GREEN);
            r.bar.setProgress(progress);

            if (progress >= 1) finish(r, true, "Relic extracted!");
        } else {
            r.channelStarted = 0;
            r.bar.setColor(BarColor.PURPLE);
            r.bar.setTitle((r.carrier == null ? "Relic " : "Exit ") + coords(r.target()) + " | Lives " + r.lives + " | " + ((r.deadline - now) / 1000) + "s");
            r.bar.setProgress(Math.min(1, (double) (r.deadline - now) / (Math.max(60, config.getInt("duration-seconds")) * 1000L)));
        }
    }

    void interruptChannel(Player p) {
        ExtractionRun r = run(p);
        if (r != null) r.channelStarted = 0;
    }

    private void reward(Player p) throws Exception {
        String path = "players." + p.getUniqueId();
        String day = LocalDate.now(ZoneOffset.UTC).toString();
        boolean daily = !day.equals(stats.getString(path + ".last-daily"));
        int wins = stats.getInt(path + ".wins") + 1;
        String before = stats.saveToString();

        try {
            stats.set(path + ".wins", wins);
            stats.set(path + ".last-daily", day);
            saveStats();
        } catch (Exception e) {
            stats.loadFromString(before);
            throw e;
        }

        plugin.getSoulManager().addEarnedSouls(p, Math.max(0, config.getLong("rewards.souls")));
        plugin.getScrapManager().addScraps(p, Math.max(0, config.getLong("rewards.scraps")));

        if (daily) plugin.getGemManager().addGems(p, Math.max(0, config.getLong("rewards.daily-gems")));
        if (wins == 10) ChatCosmetics.get().manager().grant(p, ChatCosmetic.TAG_RELIC_HUNTER);

        p.sendMessage("§aRewarded " + config.getLong("rewards.souls") + " Souls and " + config.getLong("rewards.scraps") + " Scraps" + (daily ? " + " + config.getLong("rewards.daily-gems") + " daily Gems" : "") + ". Successful extractions: " + wins);
    }

    private void saveStats() throws IOException {
        Path temp = Files.createTempFile(plugin.getDataFolder().toPath(), "extraction-", ".tmp");

        try {
            Files.writeString(temp, stats.saveToString(), StandardCharsets.UTF_8);

            try {
                Files.move(temp, statsFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, statsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    void finish(ExtractionRun r, boolean success, String reason) {
        if (runs.remove(r.id) == null) return;

        removeMarker(r);
        r.bar.removeAll();

        for (UUID id : r.members) {
            players.remove(id);
            Player p = Bukkit.getPlayer(id);

            if (p == null) continue;

            Location compass = r.previousCompass.get(id);
            if (compass != null && compass.getWorld() != null) p.setCompassTarget(compass);

            p.resetTitle();

            if (success) {
                try {
                    reward(p);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Review extraction reward for " + id + " run " + r.id, e);
                    p.sendMessage("§cYour reward needs staff review. Run: " + r.id);
                }
            }
        }

        say(r.members, reason);
    }

    public void shutdown() {
        for (ExtractionRun r : List.copyOf(runs.values())) finish(r, false, "Expedition cancelled: server stopping.");

        lobbies.clear();
        invitations.clear();
        permittedTeleports.clear();
    }
}
