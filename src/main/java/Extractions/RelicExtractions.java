package Extractions;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.*;
import java.util.logging.Level;

public final class RelicExtractions implements Listener, CommandExecutor, TabCompleter {

    private final CarcerWorldCore plugin;
    private final ExtractionManager manager;
    private BukkitTask task;

    private static final class Menu implements InventoryHolder {

        final UUID owner;
        final Inventory inventory;

        Menu(Player p) {
            owner = p.getUniqueId();
            inventory = Bukkit.createInventory(this, 27, "§8Veyr — Relic Extractions");
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private RelicExtractions(CarcerWorldCore plugin) throws Exception {
        this.plugin = plugin;
        manager = new ExtractionManager(plugin);
    }

    public static void install(CarcerWorldCore plugin) {
        try {
            PluginCommand command = Objects.requireNonNull(plugin.getCommand("extraction"), "Add extraction to plugin.yml first.");
            RelicExtractions system = new RelicExtractions(plugin);

            command.setExecutor(system);
            command.setTabCompleter(system);
            Bukkit.getPluginManager().registerEvents(system, plugin);

            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    system.manager.cleanStale(entity);
                }
            }

            system.task = Bukkit.getScheduler().runTaskTimer(plugin, system.manager::tick, 20L, 20L);
        } catch (Exception e) {
            throw new IllegalStateException("Could not install Relic Extractions", e);
        }
    }

    private static ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§f§l" + name);
        meta.setLore(Arrays.stream(lore).map(line -> "§7§l| §f" + line).toList());
        item.setItemMeta(meta);

        return item;
    }

    private void open(Player p) {
        if (!manager.nearBroker(p)) {
            p.sendMessage("§cVisit a Relic Broker to open this menu.");
            return;
        }

        manager.openLobby(p);

        Menu menu = new Menu(p);

        for (int i = 0; i < 27; i++) menu.inventory.setItem(i, item(Material.GRAY_STAINED_GLASS_PANE, " "));

        ExtractionManager.Lobby l = manager.lobby(p);

        if (l != null) {
            int slot = 0;

            for (UUID id : l.members) {
                Player member = Bukkit.getPlayer(id);
                menu.inventory.setItem(slot++, item(Material.PLAYER_HEAD, member == null ? "Offline" : member.getName(), l.ready.contains(id) ? "§aReady" : "§eNot ready", id.equals(l.leader) ? "Party leader" : "Party member"));
            }
        }

        menu.inventory.setItem(11, item(Material.LIME_DYE, "Toggle Ready", "Everyone must be ready before departure."));
        menu.inventory.setItem(13, item(Material.ENDER_EYE, "Start Extraction", "Leader starts a party of 1–4 players.", "Two shared lives. Travel on foot.", "Second death or a disconnect ends the run."));
        menu.inventory.setItem(15, item(Material.BARRIER, "Leave / Abandon", "During a run, this ends it for everyone."));
        menu.inventory.setItem(22, item(Material.PAPER, "Party and Objective", manager.status(p), "Invite: /extraction invite <player>", "Accept: /extraction accept"));

        p.openInventory(menu.inventory);
    }

    @EventHandler
    public void onNPC(NPCRightClickEvent e) {
        if (!manager.broker(e.getNPC().getId())) return;

        e.setCancelled(true);

        if (!manager.recovering(e.getClicker())) open(e.getClicker());
    }

    @EventHandler
    public void onMenu(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Menu menu)) return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player p) || !menu.owner.equals(p.getUniqueId()) || manager.recovering(p)) return;
        if (e.isShiftClick() || (!e.isLeftClick() && !e.isRightClick())) return;

        int slot = e.getRawSlot();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!p.isOnline() || p.getOpenInventory().getTopInventory() != menu.inventory || manager.recovering(p)) return;

            switch (slot) {
                case 11 -> {
                    p.sendMessage("§e" + manager.ready(p));
                    open(p);
                }
                case 13 -> {
                    p.sendMessage("§e" + manager.start(p));
                    p.closeInventory();
                }
                case 15 -> {
                    manager.leave(p);
                    p.closeInventory();
                }
                case 22 -> p.sendMessage("§e" + manager.status(p));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventory(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && manager.recovering(p)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Menu || e.getWhoClicked() instanceof Player p && manager.recovering(p)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityClick(PlayerInteractEntityEvent e) {
        if (manager.recovering(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        if (!manager.isMarker(e.getRightClicked())) return;

        e.setCancelled(true);

        if (e.getHand() == EquipmentSlot.HAND) manager.pickup(e.getPlayer(), e.getRightClicked());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityClickAt(PlayerInteractAtEntityEvent e) {
        onEntityClick(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        if (manager.isMarker(e.getRightClicked())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (!manager.active(p)) return;

        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);

        manager.died(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        ExtractionRun r = manager.run(e.getPlayer());

        if (r != null && r.recovery.containsKey(e.getPlayer().getUniqueId())) e.setRespawnLocation(r.recovery.get(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e instanceof PlayerTeleportEvent || !manager.active(e.getPlayer()) || e.getTo() == null) return;

        if (manager.recovering(e.getPlayer()) && e.getFrom().distanceSquared(e.getTo()) > 0) {
            e.setTo(e.getFrom());
            return;
        }

        ExtractionRun r = manager.run(e.getPlayer());

        if (!ExtractionManager.near(e.getTo(), r.exit, 6)) manager.interruptChannel(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (manager.active(e.getPlayer()) && !manager.permitted(e.getPlayer(), e.getTo())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cTeleporting is disabled during an extraction. Use /extraction abandon to leave.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        onTeleport(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRecoveryCommand(PlayerCommandPreprocessEvent e) {
        if (!manager.recovering(e.getPlayer())) return;

        String command = e.getMessage().trim().toLowerCase(Locale.ROOT);

        if (!Set.of("/extraction status", "/extraction leave", "/extraction abandon").contains(command)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cWait for recovery, or use /extraction abandon.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (manager.isMarker(e.getEntity())) e.setCancelled(true);
        if (e.getEntity() instanceof Player p && manager.recovering(p)) e.setCancelled(true);

        if (e instanceof EntityDamageByEntityEvent hit) {
            Entity attacker = hit.getDamager();

            if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) attacker = shooter;
            if (attacker instanceof Player p && manager.recovering(p)) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageDone(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && e.getFinalDamage() > 0) manager.interruptChannel(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!manager.active(e.getPlayer())) return;

        if (manager.recovering(e.getPlayer()) || Set.of(Material.ENDER_PEARL, Material.CHORUS_FRUIT, Material.TRIDENT, Material.FIREWORK_ROCKET).contains(e.getMaterial())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsume(PlayerItemConsumeEvent e) {
        if (manager.active(e.getPlayer()) && (manager.recovering(e.getPlayer()) || e.getItem().getType() == Material.CHORUS_FRUIT)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlight(PlayerToggleFlightEvent e) {
        if (manager.active(e.getPlayer()) && e.isFlying()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGlide(EntityToggleGlideEvent e) {
        if (e.getEntity() instanceof Player p && manager.active(p) && e.isGliding()) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMount(EntityMountEvent e) {
        if (e.getEntity() instanceof Player p && manager.active(p)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMode(PlayerGameModeChangeEvent e) {
        if (manager.active(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent e) {
        if (manager.recovering(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && manager.recovering(p)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player p && manager.recovering(p)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player p && manager.recovering(p)) {
            e.setCancelled(true);
            e.setTarget(null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        manager.leave(e.getPlayer());
    }

    @EventHandler
    public void onChunk(ChunkLoadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) manager.cleanStale(entity);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin() == plugin) {
            if (task != null) task.cancel();
            manager.shutdown();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Use this command in game.");
            return true;
        }

        if (args.length == 0) {
            open(p);
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);

        try {
            switch (action) {
                case "invite" -> p.sendMessage("§e" + (args.length == 2 ? manager.invite(p, Bukkit.getPlayerExact(args[1])) : "Use /extraction invite <player>"));
                case "accept" -> p.sendMessage("§e" + manager.accept(p));
                case "status" -> p.sendMessage("§e" + manager.status(p));
                case "leave", "abandon" -> {
                    manager.leave(p);
                    p.sendMessage("§eYou left the extraction party.");
                }
                case "broker", "shrine", "exit" -> {
                    if (!p.hasPermission("carcer.admin")) {
                        p.sendMessage("§cYou do not have permission.");
                    } else if (args.length != 2) {
                        p.sendMessage("§e/extraction " + action + " <" + (action.equals("broker") ? "Citizens ID" : "location_id") + ">");
                    } else {
                        p.sendMessage("§e" + manager.configure(p, action, args[1]));
                    }
                }
                default -> p.sendMessage("§e/extraction [invite <player>|accept|status|leave|abandon]");
            }
        } catch (NumberFormatException e) {
            p.sendMessage("§cUse the numeric Citizens NPC ID.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Extraction command failed", e);
            p.sendMessage("§cCould not complete that command. Check the console.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> values = new ArrayList<>();

        if (args.length == 1) {
            values.addAll(List.of("invite", "accept", "status", "leave", "abandon"));

            if (sender.hasPermission("carcer.admin")) values.addAll(List.of("broker", "shrine", "exit"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            values.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }

        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);

        return values.stream().filter(v -> v.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
