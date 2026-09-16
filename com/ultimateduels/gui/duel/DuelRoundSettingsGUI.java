/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.gui.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelRequestResult;
import com.ultimateduels.duel.model.WinCondition;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.duel.DuelRequestGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class DuelRoundSettingsGUI
extends AbstractGUI {
    private final Player sender;
    private final Player target;
    private final String kitName;
    private final DuelKit kit;
    private int selectedRounds = 1;
    private WinCondition selectedWinCondition = WinCondition.FIRST_TO_WIN;
    private static final int KIT_INFO_SLOT = 4;
    private static final int ROUNDS_1_SLOT = 19;
    private static final int ROUNDS_3_SLOT = 20;
    private static final int ROUNDS_5_SLOT = 21;
    private static final int ROUNDS_7_SLOT = 23;
    private static final int ROUNDS_9_SLOT = 24;
    private static final int ROUNDS_CUSTOM_SLOT = 25;
    private static final int MODE_BEST_OF_SLOT = 29;
    private static final int MODE_PLAY_ALL_SLOT = 31;
    private static final int MODE_FIRST_TO_WIN_SLOT = 33;
    private static final int CANCEL_SLOT = 45;
    private static final int SEND_SLOT = 49;
    private static final int BACK_SLOT = 53;

    public DuelRoundSettingsGUI(UltimateDuels plugin, Player sender, Player target, String kitName) {
        super(plugin, "&6&lDuel Settings &8- &e" + target.getName(), 6);
        this.sender = sender;
        this.target = target;
        this.kitName = kitName;
        this.kit = plugin.getKitManager().getAdminKit(kitName);
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        this.fillRow(1, Material.GRAY_STAINED_GLASS_PANE);
        this.fillRow(4, Material.GRAY_STAINED_GLASS_PANE);
        this.setItem(4, this.createKitInfoItem());
        this.inventory.setItem(10, this.createSectionLabel("&e&lSelect Rounds"));
        this.inventory.setItem(16, this.createSectionLabel("&b&lWin Condition"));
        this.setItem(19, this.createRoundButton(1), e -> this.selectRounds(e.getPlayer(), 1));
        this.setItem(20, this.createRoundButton(3), e -> this.selectRounds(e.getPlayer(), 3));
        this.setItem(21, this.createRoundButton(5), e -> this.selectRounds(e.getPlayer(), 5));
        this.setItem(23, this.createRoundButton(7), e -> this.selectRounds(e.getPlayer(), 7));
        this.setItem(24, this.createRoundButton(9), e -> this.selectRounds(e.getPlayer(), 9));
        this.setItem(25, this.createCustomRoundsButton());
        this.setItem(29, this.createBestOfButton(), e -> this.selectMode(e.getPlayer(), WinCondition.BEST_OF));
        this.setItem(31, this.createPlayAllButton(), e -> this.selectMode(e.getPlayer(), WinCondition.PLAY_ALL));
        this.setItem(33, this.createFirstToWinButton(), e -> this.selectMode(e.getPlayer(), WinCondition.FIRST_TO_WIN));
        this.setItem(45, GUIItem.cancel(event -> event.getPlayer().closeInventory()));
        this.setItem(49, this.createSendButton(), e -> this.sendRequest(e.getPlayer()));
        this.setItem(53, this.createGoBackButton(), e -> this.goBack(e.getPlayer()));
    }

    private ItemStack createKitInfoItem() {
        Material icon = this.kit != null && this.kit.getIcon() != null ? this.kit.getIcon() : Material.DIAMOND_SWORD;
        String displayName = this.kit != null ? this.kit.getDisplayName() : this.kitName;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Opponent: &e" + this.target.getName());
        lore.add("&7Kit: &a" + this.stripColors(displayName));
        lore.add("");
        lore.add("&7Configure your duel settings below!");
        lore.add("");
        return new GUIItem.Builder(icon).name("&6&lDuel Configuration").lore(lore).glowing(true).buildItem();
    }

    private ItemStack createSectionLabel(String name) {
        return new GUIItem.Builder(Material.YELLOW_STAINED_GLASS_PANE).name(name).buildItem();
    }

    private ItemStack createRoundButton(int rounds) {
        boolean selected = this.selectedRounds == rounds;
        Material material = selected ? Material.LIME_CONCRETE : Material.WHITE_CONCRETE;
        String prefix = selected ? "&a&l\u2713 " : "&e";
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (selected) {
            lore.add("&a&lSELECTED");
        } else {
            lore.add("&7Click to select");
        }
        lore.add("");
        if (this.selectedWinCondition == WinCondition.BEST_OF) {
            int winsNeeded = rounds / 2 + 1;
            lore.add("&7First to &e" + winsNeeded + " &7wins");
        } else if (this.selectedWinCondition == WinCondition.FIRST_TO_WIN) {
            lore.add("&7First to &e" + rounds + " &7round wins");
            lore.add("&7wins the match");
        } else {
            lore.add("&7Play all &e" + rounds + " &7rounds");
        }
        lore.add("");
        return new GUIItem.Builder(material).name(prefix + rounds + " Round" + (rounds > 1 ? "s" : "")).lore(lore).amount(rounds).glowing(selected).buildItem();
    }

    private ItemStack createCustomRoundsButton() {
        boolean isCustom = this.selectedRounds != 1 && this.selectedRounds != 3 && this.selectedRounds != 5 && this.selectedRounds != 7 && this.selectedRounds != 9;
        Material material = isCustom ? Material.LIME_CONCRETE : Material.ORANGE_CONCRETE;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (isCustom) {
            lore.add("&aCurrent: &e" + this.selectedRounds + " &7rounds");
        }
        lore.add("&7Left-click: &e+1 round");
        lore.add("&7Right-click: &e-1 round");
        lore.add("&7Shift-click: &e\u00b15 rounds");
        lore.add("");
        lore.add("&7Range: &e1-20 rounds");
        lore.add("");
        return new GUIItem.Builder(material).name("&6&lCustom Rounds").lore(lore).amount(isCustom ? Math.min(this.selectedRounds, 64) : 1).glowing(isCustom).buildItem();
    }

    private ItemStack createBestOfButton() {
        boolean selected = this.selectedWinCondition == WinCondition.BEST_OF;
        Material material = selected ? Material.GOLDEN_SWORD : Material.WOODEN_SWORD;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(selected ? "&a&lSELECTED" : "&7Click to select");
        lore.add("");
        lore.add("&7First player to win the");
        lore.add("&7majority of rounds wins!");
        lore.add("");
        if (this.selectedRounds > 1) {
            int winsNeeded = this.selectedRounds / 2 + 1;
            lore.add("&eNeed &f" + winsNeeded + " &ewins to win match");
        }
        lore.add("");
        return new GUIItem.Builder(material).name((selected ? "&a&l\u2713 " : "&e") + "Best Of").lore(lore).glowing(selected).buildItem();
    }

    private ItemStack createPlayAllButton() {
        boolean selected = this.selectedWinCondition == WinCondition.PLAY_ALL;
        Material material = selected ? Material.IRON_SWORD : Material.WOODEN_SWORD;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(selected ? "&a&lSELECTED" : "&7Click to select");
        lore.add("");
        lore.add("&7Play all rounds regardless");
        lore.add("&7of the score!");
        lore.add("");
        lore.add("&eAll &f" + this.selectedRounds + " &erounds will be played");
        lore.add("");
        return new GUIItem.Builder(material).name((selected ? "&a&l\u2713 " : "&e") + "Play All").lore(lore).glowing(selected).buildItem();
    }

    private ItemStack createFirstToWinButton() {
        boolean selected = this.selectedWinCondition == WinCondition.FIRST_TO_WIN;
        Material material = selected ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(selected ? "&a&lSELECTED" : "&7Click to select");
        lore.add("");
        lore.add("&7First player to win");
        lore.add("&e" + this.selectedRounds + " rounds &7wins the match!");
        lore.add("");
        lore.add("&7No maximum round limit.");
        lore.add("&7Match continues until someone");
        lore.add("&7reaches &e" + this.selectedRounds + " &7wins.");
        lore.add("");
        return new GUIItem.Builder(material).name((selected ? "&a&l\u2713 " : "&e") + "First to " + this.selectedRounds).lore(lore).glowing(selected).buildItem();
    }

    private ItemStack createSendButton() {
        String modeName = this.selectedWinCondition.getFormattedString(this.selectedRounds);
        String kitDisplayName = this.kit != null ? this.stripColors(this.kit.getDisplayName()) : this.kitName;
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Opponent: &e" + this.target.getName());
        lore.add("&7Kit: &e" + kitDisplayName);
        lore.add("&7Rounds: &e" + this.selectedRounds);
        lore.add("&7Mode: &e" + modeName);
        lore.add("");
        lore.add("&a&lClick to send request!");
        lore.add("");
        return new GUIItem.Builder(Material.LIME_WOOL).name("&a&lSend Duel Request").lore(lore).glowing(true).buildItem();
    }

    private ItemStack createGoBackButton() {
        return new GUIItem.Builder(Material.ARROW).name("&e&lBack").lore("", "&7Return to full duel GUI", "").buildItem();
    }

    private String stripColors(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    @Override
    protected boolean onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
        if (slot == 25) {
            this.handleCustomRounds(player, clickType);
            return true;
        }
        return true;
    }

    private void selectRounds(Player player, int rounds) {
        this.selectedRounds = rounds;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        this.refresh(player);
    }

    private void handleCustomRounds(Player player, ClickType clickType) {
        int change;
        int n = change = clickType.isShiftClick() ? 5 : 1;
        if (clickType.isLeftClick()) {
            this.selectedRounds = Math.min(this.selectedRounds + change, 20);
        } else if (clickType.isRightClick()) {
            this.selectedRounds = Math.max(this.selectedRounds - change, 1);
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        this.refresh(player);
    }

    private void selectMode(Player player, WinCondition winCondition) {
        this.selectedWinCondition = winCondition;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        this.refresh(player);
    }

    private void sendRequest(Player player) {
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
        DuelRequestResult result = this.plugin.getDuelManager().sendRequest(this.sender, this.target, this.kitName, null, this.selectedRounds, this.selectedWinCondition);
        if (!result.success()) {
            MessageUtils.sendMessage(player, result.message());
        }
    }

    private void goBack(Player player) {
        player.closeInventory();
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> new DuelRequestGUI(this.plugin, this.sender, this.target).open(player));
    }

    public int getSelectedRounds() {
        return this.selectedRounds;
    }

    public WinCondition getSelectedWinCondition() {
        return this.selectedWinCondition;
    }

    public boolean isBestOf() {
        return this.selectedWinCondition == WinCondition.BEST_OF;
    }
}

