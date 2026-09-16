/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.gui.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelRequestResult;
import com.ultimateduels.duel.model.WinCondition;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.duel.DuelSettingsGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DuelRequestGUI
extends AbstractGUI {
    private final Player sender;
    private final Player target;
    private DuelKit selectedKit;
    private DuelArena selectedArena;
    private int rounds = 1;
    private WinCondition winCondition = WinCondition.BEST_OF;
    private int kitPage = 1;
    private static final int KITS_PER_PAGE = 7;

    public DuelRequestGUI(UltimateDuels plugin, Player sender, Player target) {
        super(plugin, "&6&lDuel " + target.getName(), 6);
        this.sender = sender;
        this.target = target;
        List<DuelKit> kits = plugin.getKitManager().getEnabledKits();
        if (!kits.isEmpty()) {
            this.selectedKit = kits.get(0);
        }
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        this.setItem(4, this.createTargetItem());
        this.fillRow(1, Material.GRAY_STAINED_GLASS_PANE);
        this.setupKitSelection();
        this.setItem(29, this.createArenaSelector(), event -> this.handleArenaClick(event.getPlayer(), event.getClickType()));
        this.setItem(31, this.createRoundsSelector(), event -> this.handleRoundsClick(event.getPlayer(), event.getClickType()));
        this.setItem(33, this.createWinConditionSelector(), event -> this.handleWinConditionClick(event.getPlayer()));
        this.fillRow(4, Material.GRAY_STAINED_GLASS_PANE);
        this.setItem(49, this.createSendButton(), event -> this.handleSendRequest(event.getPlayer()));
        this.setItem(45, GUIItem.cancel(event -> event.getPlayer().closeInventory()));
        this.setItem(53, this.createPreviewButton(), event -> this.handlePreviewKit(event.getPlayer()));
    }

    private void setupKitSelection() {
        int[] kitSlots;
        List<DuelKit> kits = this.plugin.getKitManager().getEnabledKits();
        int totalPages = (int)Math.ceil((double)kits.size() / 7.0);
        int startIndex = (this.kitPage - 1) * 7;
        int endIndex = Math.min(startIndex + 7, kits.size());
        for (int slot : kitSlots = new int[]{19, 20, 21, 22, 23, 24, 25}) {
            this.inventory.setItem(slot, null);
            this.items.remove(slot);
        }
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < kitSlots.length; ++i) {
            int slot;
            DuelKit kit = kits.get(i);
            slot = kitSlots[slotIndex++];
            boolean isSelected = this.selectedKit != null && this.selectedKit.getName().equals(kit.getName());
            this.setItem(slot, this.createKitItem(kit, isSelected), event -> {
                this.selectedKit = kit;
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                this.refresh(event.getPlayer());
            });
        }
        this.inventory.setItem(18, null);
        this.items.remove(18);
        this.inventory.setItem(26, null);
        this.items.remove(26);
        if (this.kitPage > 1) {
            this.setItem(18, GUIItem.previousPage(this.kitPage, event -> {
                --this.kitPage;
                this.refresh(event.getPlayer());
            }));
        }
        if (this.kitPage < totalPages) {
            this.setItem(26, GUIItem.nextPage(this.kitPage, totalPages, event -> {
                ++this.kitPage;
                this.refresh(event.getPlayer());
            }));
        }
    }

    private ItemStack createTargetItem() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Configure your duel request");
        lore.add("&7for &e" + this.target.getName());
        lore.add("");
        PlayerStats stats = this.plugin.getStatsManager().getStats(this.target.getUniqueId());
        if (stats != null) {
            lore.add("&7Their Stats:");
            lore.add("&7Elo: &f" + stats.getGlobalElo());
            lore.add("&7W/L: &a" + stats.getTotalWins() + "&7/&c" + stats.getTotalLosses());
            lore.add("");
        }
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(this.target.getName()).name("&6&lDueling: &e" + this.target.getName()).lore(lore).buildItem();
    }

    private ItemStack createKitItem(DuelKit kit, boolean selected) {
        String displayName = this.stripColors(kit.getDisplayName());
        String name = (selected ? "&a&l\u2713 " : "&e") + displayName;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (selected) {
            lore.add("&a&lSELECTED");
        } else {
            lore.add("&7Click to select");
        }
        lore.add("");
        Material icon = kit.getIcon() != null ? kit.getIcon() : Material.DIAMOND_SWORD;
        return new GUIItem.Builder(icon).name(name).lore(lore).glowing(selected).buildItem();
    }

    private ItemStack createArenaSelector() {
        String arenaName = this.selectedArena != null ? this.selectedArena.getDisplayName() : "Random";
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Current: &f" + arenaName);
        lore.add("");
        lore.add("&eLeft-click &7to cycle arenas");
        lore.add("&eRight-click &7for random");
        lore.add("");
        return this.createItem(Material.GRASS_BLOCK, "&6&lArena", lore);
    }

    private ItemStack createRoundsSelector() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Current: &f" + this.rounds + " round" + (this.rounds > 1 ? "s" : ""));
        lore.add("");
        lore.add("&eLeft-click &7to increase");
        lore.add("&eRight-click &7to decrease");
        lore.add("&eShift-click &7for advanced");
        lore.add("");
        return new GUIItem.Builder(Material.REPEATER).name("&6&lRounds").lore(lore).amount(Math.min(this.rounds, 64)).buildItem();
    }

    private ItemStack createWinConditionSelector() {
        Material icon = switch (this.winCondition) {
            case WinCondition.BEST_OF -> Material.GOLDEN_SWORD;
            case WinCondition.PLAY_ALL -> Material.IRON_SWORD;
            case WinCondition.FIRST_TO_WIN -> Material.DIAMOND_SWORD;
            default -> Material.WOODEN_SWORD;
        };
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Current: &f" + this.winCondition.getDisplayName());
        lore.add("");
        lore.add("&eBest Of: &7First to win majority");
        lore.add("&ePlay All: &7Play all rounds");
        lore.add("&eFirst to Win: &7First to X round wins");
        lore.add("");
        lore.add("&eClick to cycle");
        lore.add("");
        return this.createItem(icon, "&6&lWin Condition", lore);
    }

    private ItemStack createSendButton() {
        if (this.selectedKit == null) {
            return this.createItem(Material.GRAY_WOOL, "&c&lSelect a Kit", "", "&7You must select a kit first", "");
        }
        String kitDisplayName = this.stripColors(this.selectedKit.getDisplayName());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Kit: &e" + kitDisplayName);
        lore.add("&7Arena: &e" + (this.selectedArena != null ? this.selectedArena.getDisplayName() : "Random"));
        lore.add("&7Rounds: &e" + this.rounds);
        lore.add("&7Mode: &e" + this.winCondition.getDisplayName());
        lore.add("");
        lore.add("&a&lClick to send request!");
        lore.add("");
        return this.createItem(Material.LIME_WOOL, "&a&lSend Duel Request", lore);
    }

    private ItemStack createPreviewButton() {
        return this.createItem(Material.ENDER_EYE, "&b&lPreview Kit", "", "&7Click to preview the", "&7selected kit contents", "");
    }

    private String stripColors(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    private void handleArenaClick(Player player, ClickType clickType) {
        if (clickType.isRightClick()) {
            this.selectedArena = null;
        } else {
            List<DuelArena> arenas = this.plugin.getArenaManager().getEnabledArenas();
            if (arenas.isEmpty()) {
                this.selectedArena = null;
            } else if (this.selectedArena == null) {
                this.selectedArena = arenas.get(0);
            } else {
                int index = -1;
                for (int i = 0; i < arenas.size(); ++i) {
                    if (!arenas.get(i).getName().equals(this.selectedArena.getName())) continue;
                    index = i;
                    break;
                }
                this.selectedArena = (index = (index + 1) % (arenas.size() + 1)) < arenas.size() ? arenas.get(index) : null;
            }
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        this.refresh(player);
    }

    private void handleRoundsClick(Player player, ClickType clickType) {
        if (clickType.isShiftClick()) {
            new DuelSettingsGUI(this.plugin, this.sender, this.target, this).open(player);
            return;
        }
        if (clickType.isLeftClick()) {
            this.rounds = Math.min(this.rounds + 1, 20);
        } else if (clickType.isRightClick()) {
            this.rounds = Math.max(this.rounds - 1, 1);
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        this.refresh(player);
    }

    private void handleWinConditionClick(Player player) {
        this.winCondition = switch (this.winCondition) {
            case WinCondition.BEST_OF -> WinCondition.PLAY_ALL;
            case WinCondition.PLAY_ALL -> WinCondition.FIRST_TO_WIN;
            case WinCondition.FIRST_TO_WIN -> WinCondition.BEST_OF;
            default -> WinCondition.BEST_OF;
        };
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        this.refresh(player);
    }

    private void handleSendRequest(Player player) {
        if (this.selectedKit == null) {
            MessageUtils.sendMessage(player, "&cPlease select a kit first!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (!this.target.isOnline()) {
            MessageUtils.sendMessage(player, "&c" + this.target.getName() + " is no longer online!");
            player.closeInventory();
            return;
        }
        if (this.plugin.getDuelManager().isInMatch(this.target.getUniqueId())) {
            MessageUtils.sendMessage(player, "&c" + this.target.getName() + " is now in a duel!");
            player.closeInventory();
            return;
        }
        player.closeInventory();
        String arenaName = this.selectedArena != null ? this.selectedArena.getName() : null;
        DuelRequestResult result = this.plugin.getDuelManager().sendRequest(this.sender, this.target, this.selectedKit.getName(), arenaName, this.rounds, this.winCondition);
        if (!result.success()) {
            MessageUtils.sendMessage(player, result.message());
        }
    }

    private void handlePreviewKit(Player player) {
        if (this.selectedKit == null) {
            MessageUtils.sendMessage(player, "&cNo kit selected!");
            return;
        }
        MessageUtils.sendMessage(player, "&eKit preview coming soon!");
    }

    public DuelKit getSelectedKit() {
        return this.selectedKit;
    }

    public void setSelectedKit(DuelKit kit) {
        this.selectedKit = kit;
    }

    public DuelArena getSelectedArena() {
        return this.selectedArena;
    }

    public void setSelectedArena(DuelArena arena) {
        this.selectedArena = arena;
    }

    public int getRounds() {
        return this.rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = Math.max(1, Math.min(20, rounds));
    }

    public WinCondition getWinCondition() {
        return this.winCondition;
    }

    public void setWinCondition(WinCondition winCondition) {
        this.winCondition = winCondition;
    }

    public Player getTarget() {
        return this.target;
    }
}

