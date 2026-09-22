package Extractions;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;

import java.util.*;

final class ExtractionRun {

    final UUID id = UUID.randomUUID();
    final Set<UUID> members;
    final Location shrine;
    final Location exit;
    final long deadline;
    final BossBar bar;

    final Map<UUID, Location> safe = new HashMap<>();
    final Map<UUID, Location> recovery = new HashMap<>();
    final Map<UUID, Long> recoveringUntil = new HashMap<>();
    final Map<UUID, Location> previousCompass = new HashMap<>();

    UUID carrier;
    ArmorStand marker;
    Chunk markerChunk;
    Location relic;
    int lives = 2;
    long channelStarted;

    ExtractionRun(Set<UUID> members, Location shrine, Location exit, long deadline, BossBar bar) {
        this.members = Set.copyOf(members);
        this.shrine = shrine.clone();
        this.relic = shrine.clone();
        this.exit = exit.clone();
        this.deadline = deadline;
        this.bar = bar;
    }

    Location target() {
        return carrier == null ? relic : exit;
    }
}
