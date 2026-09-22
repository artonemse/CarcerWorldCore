package Cosmetics.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.carcercore.carcerWorldCore.CarcerWorldCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class ChatCosmeticGUI implements Listener {

    private final CarcerWorldCore plugin;
    private final ChatCosmeticManager manager;

    private static final class Menu implements InventoryHolder {
        private final UUID owner;
        private final ChatCosmetic.Kind kind;
        private final List<ChatCosmetic> entries;
        private final Inventory inventory;

        private Menu(Player player, ChatCosmetic.Kind kind) {
            this.owner = player.getUniqueId();
            this.kind = kind;
            this.entries = Arrays.stream(ChatCosmetic.values()).filter(cosmetic -> cosmetic.kind() == kind).toList();
            this.inventory = Bukkit.createInventory(this, 54, title(kind));
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public ChatCosmeticGUI(CarcerWorldCore plugin, ChatCosmeticManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player player, ChatCosmetic.Kind kind) {
        manager.refresh(player);

        Menu menu = new Menu(player, kind);
        Inventory inventory = menu.getInventory();

        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());

        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, filler);

        for (int index = 0; index < menu.entries.size() && index < 45; index++) {
            inventory.setItem(index, cosmeticItem(player, menu.entries.get(index)));
        }

        inventory.setItem(45, item(Material.WHITE_DYE, "&f&lStandard Colors", List.of("&7&l| &fPurchased with Scraps.")));
        inventory.setItem(46, item(Material.AMETHYST_SHARD, "&f&lPremium Gradients", List.of("&7&l| &fGem purchases and earned rewards.")));
        inventory.setItem(47, item(Material.NAME_TAG, "&f&lPlayer Tags", List.of("&7&l| &fEarned through gameplay only.")));

        inventory.setItem(49, item(Material.PAPER, "&f&lYour Chat Preview", List.of(
                manager.preview(player),
                "",
                "&7&l| &fScraps: &e" + String.format("%,d", plugin.getScrapManager().getScraps(player)),
                "&7&l| &fGems: &d" + String.format("%,d", plugin.getGemManager().getGems(player)),
                "",
                "&7&l| &fClick to preview in chat."
        )));

        inventory.setItem(50, item(Material.BARRIER, kind == ChatCosmetic.Kind.TAG ? "&f&lRemove Tag" : "&f&lReset to White", List.of("&7&l| &fKeep all your unlocks.")));
        inventory.setItem(53, item(Material.ARROW, "&f&lBack", List.of("&7&l| &fReturn to Cosmetics.")));

        player.openInventory(inventory);
    }

    private ItemStack cosmeticItem(Player player, ChatCosmetic cosmetic) {
        Material material = switch (cosmetic.kind()) {
            case BASIC -> Material.WHITE_DYE;
            case PREMIUM -> Material.AMETHYST_SHARD;
            case TAG -> Material.NAME_TAG;
        };

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7§l| §fPreview: " + cosmetic.paint(cosmetic.kind() == ChatCosmetic.Kind.TAG ? "[" + cosmetic.displayName() + "]" : "Your message looks like this."));
        lore.add("");

        if (manager.selected(player, cosmetic)) {
            lore.add("&7&l| &fStatus: &aEquipped");
        } else if (manager.owns(player, cosmetic)) {
            lore.add("&7&l| &fStatus: &aUnlocked");
            lore.add("&7&l| &fClick to equip.");
        } else {
            long price = manager.price(cosmetic);
            lore.add("&7&l| &fStatus: &cLocked");

            if (price >= 0) {
                String currency = cosmetic.kind() == ChatCosmetic.Kind.BASIC ? "Scraps" : "Gems";
                lore.add("&7&l| &fCost: &e" + String.format("%,d", price) + " " + currency);
                lore.add("&7&l| &fClick to purchase and equip.");
            } else {
                lore.add("&7&l| &fEarn-only reward.");
            }

            if (cosmetic.kind() != ChatCosmetic.Kind.BASIC) {
                lore.add("");
                lore.add("&7&l| &f" + manager.requirement(cosmetic));
            }
        }

        return item(material, "&f&l" + cosmetic.displayName(), lore);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!menu.owner.equals(player.getUniqueId())) return;

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= menu.inventory.getSize()) return;
        if (!event.isLeftClick() && !event.isRightClick()) return;
        if (event.isShiftClick()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getOpenInventory().getTopInventory() != menu.inventory) return;

            switch (slot) {
                case 45 -> open(player, ChatCosmetic.Kind.BASIC);
                case 46 -> open(player, ChatCosmetic.Kind.PREMIUM);
                case 47 -> open(player, ChatCosmetic.Kind.TAG);
                case 49 -> player.sendMessage(manager.preview(player));
                case 50 -> {
                    boolean success = manager.resetSelection(player, menu.kind == ChatCosmetic.Kind.TAG);
                    player.sendMessage(success ? "§aChat selection reset." : "§cCould not save your selection.");
                    open(player, menu.kind);
                }
                case 53 -> plugin.getCosmeticsGUI().open(player);
                default -> {
                    if (slot >= menu.entries.size() || slot >= 45) return;

                    player.sendMessage(manager.activate(player, menu.entries.get(slot)));
                    open(player, menu.kind);
                }
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }

    private static String title(ChatCosmetic.Kind kind) {
        return switch (kind) {
            case BASIC -> "§8Chat Colors";
            case PREMIUM -> "§8Premium Gradients";
            case TAG -> "§8Player Tags";
        };
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color(name));
        meta.setLore(lore.stream().map(ChatCosmeticGUI::color).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
