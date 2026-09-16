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
import com.ultimateduels.duel.model.WinCondition;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.duel.DuelRequestGUI;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class RoundSelectorGUI
extends AbstractGUI {
    private final DuelRequestGUI parentGUI;
    private int selectedRounds;
    private WinCondition winCondition;

    public RoundSelectorGUI(UltimateDuels plugin, DuelRequestGUI parentGUI) {
        super(plugin, "&6&lSelect Rounds", 5);
        this.parentGUI = parentGUI;
        this.selectedRounds = parentGUI.getRounds();
        this.winCondition = parentGUI.getWinCondition();
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33};
        for (int i = 0; i < 20 && i < slots.length; ++i) {
            int roundNum = i + 1;
            int slot = slots[i];
            boolean selected = roundNum == this.selectedRounds;
            this.setItem(slot, this.createRoundItem(roundNum, selected), event -> {
                this.selectedRounds = roundNum;
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f + (float)roundNum * 0.05f);
                this.refresh(event.getPlayer());
            });
        }
        this.setItem(37, this.createQuickSelect(1, "Single"));
        this.setItem(38, this.createQuickSelect(3, "Best of 3"));
        this.setItem(39, this.createQuickSelect(5, "Best of 5"));
        this.setItem(40, this.createQuickSelect(7, "Best of 7"));
        this.setItem(41, this.createQuickSelect(10, "10 Rounds"));
        this.setItem(42, this.createQuickSelect(20, "20 Rounds"));
        this.setItem(34, this.createWinConditionToggle());
        this.setItem(4, this.createSelectionInfo());
        this.setItem(43, this.createConfirmButton());
        this.setItem(36, GUIItem.backButton(event -> this.parentGUI.open(event.getPlayer())));
    }

    private ItemStack createRoundItem(int roundNum, boolean selected) {
        Material material = this.getMaterialForRound(roundNum);
        String color = selected ? "&a&l" : "&e";
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (roundNum == 1) {
            lore.add("&7Single round");
        } else if (this.winCondition == WinCondition.BEST_OF) {
            int toWin = roundNum / 2 + 1;
            lore.add("&7First to &f" + toWin + " &7wins");
        } else if (this.winCondition == WinCondition.FIRST_TO_WIN) {
            lore.add("&7First to &f" + roundNum + " &7wins");
            lore.add("&7wins the match");
        } else {
            lore.add("&7Play all &f" + roundNum + " &7rounds");
        }
        lore.add("");
        lore.add(selected ? "&a\u2713 Selected" : "&7Click to select");
        return new GUIItem.Builder(material).name(color + roundNum + " Round" + (roundNum > 1 ? "s" : "")).amount(Math.min(roundNum, 64)).lore(lore).glowing(selected).buildItem();
    }

    private Material getMaterialForRound(int round) {
        if (round == 1) {
            return Material.WOODEN_SWORD;
        }
        if (round <= 3) {
            return Material.STONE_SWORD;
        }
        if (round <= 5) {
            return Material.IRON_SWORD;
        }
        if (round <= 7) {
            return Material.GOLDEN_SWORD;
        }
        if (round <= 10) {
            return Material.DIAMOND_SWORD;
        }
        return Material.NETHERITE_SWORD;
    }

    private GUIItem createQuickSelect(int rounds, String name) {
        boolean selected = this.selectedRounds == rounds;
        return new GUIItem.Builder(selected ? Material.LIME_DYE : Material.GRAY_DYE).name((selected ? "&a" : "&7") + name).lore("", "&eClick for " + rounds + " round" + (rounds > 1 ? "s" : "")).onClick(event -> {
            this.selectedRounds = rounds;
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            this.refresh(event.getPlayer());
        }).build();
    }

    private GUIItem createWinConditionToggle() {
        Material icon;
        String conditionName = switch (this.winCondition) {
            case WinCondition.BEST_OF -> {
                icon = Material.GOLDEN_SWORD;
                yield "Best Of";
            }
            case WinCondition.PLAY_ALL -> {
                icon = Material.IRON_SWORD;
                yield "Play All";
            }
            case WinCondition.FIRST_TO_WIN -> {
                icon = Material.DIAMOND_SWORD;
                yield "First to Win";
            }
            default -> {
                icon = Material.WOODEN_SWORD;
                yield this.winCondition.getDisplayName();
            }
        };
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Current: &f" + conditionName);
        lore.add("");
        lore.add("&eBest Of: &7First to majority");
        lore.add("&ePlay All: &7Complete all rounds");
        lore.add("&eFirst to Win: &7First to X round wins");
        lore.add("");
        lore.add("&eClick to cycle");
        return new GUIItem.Builder(icon).name("&6Win Condition").lore(lore).onClick(event -> {
            this.winCondition = switch (this.winCondition) {
                case WinCondition.BEST_OF -> WinCondition.PLAY_ALL;
                case WinCondition.PLAY_ALL -> WinCondition.FIRST_TO_WIN;
                case WinCondition.FIRST_TO_WIN -> WinCondition.BEST_OF;
                default -> WinCondition.BEST_OF;
            };
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            this.refresh(event.getPlayer());
        }).build();
    }

    private ItemStack createSelectionInfo() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Rounds: &f" + this.selectedRounds);
        lore.add("&7Mode: &f" + this.winCondition.getDisplayName());
        lore.add("");
        if (this.selectedRounds > 1) {
            if (this.winCondition == WinCondition.BEST_OF) {
                int toWin = this.selectedRounds / 2 + 1;
                lore.add("&7First to &a" + toWin + " &7wins");
            } else if (this.winCondition == WinCondition.FIRST_TO_WIN) {
                lore.add("&7Match ends when first");
                lore.add("&7round is won");
                lore.add("&7(max &a" + this.selectedRounds + " &7rounds)");
            } else {
                lore.add("&7All &a" + this.selectedRounds + " &7rounds played");
            }
        } else {
            lore.add("&7Single round match");
        }
        return this.createItem(Material.PAPER, "&6&lRound Configuration", lore);
    }

    private GUIItem createConfirmButton() {
        return new GUIItem.Builder(Material.LIME_WOOL).name("&a&lConfirm Selection").lore("", "&7Rounds: &f" + this.selectedRounds, "&7Mode: &f" + this.winCondition.getDisplayName(), "", "&aClick to confirm").onClick(event -> {
            this.parentGUI.setRounds(this.selectedRounds);
            this.parentGUI.setWinCondition(this.winCondition);
            this.parentGUI.open(event.getPlayer());
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        }).build();
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

