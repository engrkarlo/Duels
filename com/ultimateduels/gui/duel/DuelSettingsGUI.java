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
import com.ultimateduels.arena.model.ArenaState;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.WinCondition;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.gui.duel.DuelRequestGUI;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DuelSettingsGUI
extends AbstractGUI {
    private final Player sender;
    private final Player target;
    private final DuelRequestGUI parentGUI;
    private static final String SECTION = "duel-settings";
    private DuelKit selectedKit;
    private DuelArena selectedArena;
    private int rounds;
    private WinCondition winCondition;
    private SelectionMode mode = SelectionMode.KIT;
    private int page = 1;
    private static final int ITEMS_PER_PAGE = 21;

    public DuelSettingsGUI(UltimateDuels plugin, Player sender, Player target) {
        this(plugin, sender, target, null);
    }

    public DuelSettingsGUI(UltimateDuels plugin, Player sender, Player target, DuelRequestGUI parentGUI) {
        super(plugin, GUIMessages.getTitle(sender, SECTION, "&6&lDuel Settings"), 6);
        this.sender = sender;
        this.target = target;
        this.parentGUI = parentGUI;
        if (parentGUI != null) {
            this.selectedKit = parentGUI.getSelectedKit();
            this.selectedArena = parentGUI.getSelectedArena();
            this.rounds = parentGUI.getRounds();
            this.winCondition = parentGUI.getWinCondition();
        } else {
            List<DuelKit> kits = this.getEnabledKits();
            this.selectedKit = kits.isEmpty() ? null : kits.get(0);
            this.selectedArena = null;
            this.rounds = 1;
            this.winCondition = WinCondition.BEST_OF;
        }
    }

    private List<DuelKit> getEnabledKits() {
        return new ArrayList<DuelKit>(this.plugin.getKitManager().getAllAdminKits());
    }

    private List<DuelArena> getEnabledArenas() {
        return this.plugin.getArenaManager().getAllDuelArenas().stream().filter(DuelArena::isEnabled).collect(Collectors.toList());
    }

    private boolean isArenaInUse(DuelArena arena) {
        return arena.getState() == ArenaState.IN_USE;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.BLACK_STAINED_GLASS_PANE);
        this.setupModeTabs();
        for (int i = 9; i < 18; ++i) {
            this.inventory.setItem(i, this.createFiller(Material.GRAY_STAINED_GLASS_PANE));
        }
        switch (this.mode.ordinal()) {
            case 0: {
                this.setupKitSelection();
                break;
            }
            case 1: {
                this.setupArenaSelection();
                break;
            }
            case 2: {
                this.setupRoundsSelection();
                break;
            }
            case 3: {
                this.setupWinConditionSelection();
            }
        }
        this.fillRow(5, Material.GRAY_STAINED_GLASS_PANE);
        this.setItem(48, this.createSettingsSummary());
        this.setItem(49, this.createConfirmButton());
        this.setItem(45, GUIItem.backButton(event -> {
            if (this.parentGUI != null) {
                this.parentGUI.open(event.getPlayer());
            } else {
                event.getPlayer().closeInventory();
            }
        }));
        this.setItem(53, this.createPageInfo());
    }

    private void setupModeTabs() {
        int slot = 1;
        for (SelectionMode m : SelectionMode.values()) {
            boolean selected = m == this.mode;
            String tabName = GUIMessages.getText(this.sender, SECTION, "tab-" + m.name().toLowerCase(), (selected ? "&a&l" : "&e") + m.getDisplayName());
            String selectedText = GUIMessages.getText(this.sender, SECTION, "selected", "&a&lSELECTED");
            String clickToConfigText = GUIMessages.getText(this.sender, SECTION, "click-to-config", "&7Click to configure");
            ItemStack item = new GUIItem.Builder(m.getIcon()).name(tabName).lore("", selected ? selectedText : clickToConfigText, "").glowing(selected).buildItem();
            SelectionMode finalMode = m;
            this.setItem(slot, item, event -> {
                this.mode = finalMode;
                this.page = 1;
                this.refresh(event.getPlayer());
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            });
            slot += 2;
        }
    }

    private void setupKitSelection() {
        List<DuelKit> kits = this.getEnabledKits();
        int totalPages = (int)Math.ceil((double)kits.size() / 21.0);
        int startIndex = (this.page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, kits.size());
        int[] slots = this.getContentSlots();
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
            DuelKit kit = kits.get(i);
            int slot = slots[slotIndex++];
            boolean selected = this.selectedKit != null && this.selectedKit.getName().equals(kit.getName());
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("");
            String description = kit.getDescription();
            if (description != null && !description.isEmpty()) {
                lore.add("&7" + this.stripColors(description));
                lore.add("");
            }
            String selectedText = GUIMessages.getText(this.sender, SECTION, "kit-selected", "&a&l\u2713 SELECTED");
            String clickSelectText = GUIMessages.getText(this.sender, SECTION, "click-to-select", "&eClick to select");
            lore.add(selected ? selectedText : clickSelectText);
            Material iconMaterial = kit.getIcon() != null ? kit.getIcon() : Material.DIAMOND_SWORD;
            ItemStack item = new GUIItem.Builder(iconMaterial).name((selected ? "&a&l" : "&e") + this.stripColors(kit.getDisplayName())).lore(lore).glowing(selected).buildItem();
            this.setItem(slot, item, event -> {
                this.selectedKit = kit;
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                this.refresh(event.getPlayer());
            });
        }
        this.setupPagination(totalPages);
    }

    private void setupArenaSelection() {
        ArrayList<DuelArena> arenas = new ArrayList<DuelArena>();
        arenas.add(null);
        arenas.addAll(this.getEnabledArenas());
        int totalPages = (int)Math.ceil((double)arenas.size() / 21.0);
        int startIndex = (this.page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, arenas.size());
        int[] slots = this.getContentSlots();
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
            DuelArena arena = (DuelArena)arenas.get(i);
            int slot = slots[slotIndex++];
            boolean selected = arena == null && this.selectedArena == null || arena != null && this.selectedArena != null && arena.getName().equals(this.selectedArena.getName());
            String name = arena == null ? GUIMessages.getText(this.sender, SECTION, "random-arena", "Random Arena") : arena.getDisplayName();
            Material icon = arena == null ? Material.ENDER_EYE : Material.GRASS_BLOCK;
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("");
            if (arena == null) {
                lore.add(GUIMessages.getText(this.sender, SECTION, "random-arena-line1", "&7A random arena will be"));
                lore.add(GUIMessages.getText(this.sender, SECTION, "random-arena-line2", "&7selected for the duel"));
            } else {
                String typeText = GUIMessages.getText(this.sender, SECTION, "arena-type", "&7Type: &f{type}").replace("{type}", arena.getArenaType().name());
                lore.add(typeText);
                if (this.isArenaInUse(arena)) {
                    lore.add(GUIMessages.getText(this.sender, SECTION, "arena-in-use", "&c\u26a0 Currently in use"));
                }
            }
            lore.add("");
            String selectedText = GUIMessages.getText(this.sender, SECTION, "arena-selected", "&a&l\u2713 SELECTED");
            String clickSelectText = GUIMessages.getText(this.sender, SECTION, "click-to-select", "&eClick to select");
            lore.add(selected ? selectedText : clickSelectText);
            ItemStack item = new GUIItem.Builder(icon).name((selected ? "&a&l" : "&e") + name).lore(lore).glowing(selected).buildItem();
            this.setItem(slot, item, event -> {
                this.selectedArena = arena;
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                this.refresh(event.getPlayer());
            });
        }
        this.setupPagination(totalPages);
    }

    private void setupRoundsSelection() {
        int[] slots = this.getContentSlots();
        for (int i = 0; i < Math.min(20, slots.length); ++i) {
            boolean selected;
            int roundNum = i + 1;
            int slot = slots[i];
            boolean bl = selected = roundNum == this.rounds;
            Material material = roundNum == 1 ? Material.WOODEN_SWORD : (roundNum <= 3 ? Material.STONE_SWORD : (roundNum <= 5 ? Material.IRON_SWORD : (roundNum <= 10 ? Material.GOLDEN_SWORD : Material.DIAMOND_SWORD)));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add("");
            if (roundNum == 1) {
                lore.add(GUIMessages.getText(this.sender, SECTION, "single-round", "&7Single round duel"));
            } else if (this.winCondition == WinCondition.BEST_OF) {
                int toWin = roundNum / 2 + 1;
                String bestOfText = GUIMessages.getText(this.sender, SECTION, "first-to-wins", "&7First to &f{count} &7wins").replace("{count}", String.valueOf(toWin));
                lore.add(bestOfText);
            } else if (this.winCondition == WinCondition.FIRST_TO_WIN) {
                lore.add("&7First to &f" + roundNum + " &7round wins");
                lore.add("&7wins the match");
            } else {
                String playAllText = GUIMessages.getText(this.sender, SECTION, "play-all-rounds", "&7Play all &f{count} &7rounds").replace("{count}", String.valueOf(roundNum));
                lore.add(playAllText);
            }
            lore.add("");
            String selectedText = GUIMessages.getText(this.sender, SECTION, "rounds-selected", "&a&l\u2713 SELECTED");
            String clickSelectText = GUIMessages.getText(this.sender, SECTION, "click-to-select", "&eClick to select");
            lore.add(selected ? selectedText : clickSelectText);
            ItemStack item = new GUIItem.Builder(material).name((selected ? "&a&l" : "&e") + roundNum + " Round" + (roundNum > 1 ? "s" : "")).lore(lore).amount(roundNum).glowing(selected).buildItem();
            this.setItem(slot, item, event -> {
                this.rounds = roundNum;
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                this.refresh(event.getPlayer());
            });
        }
    }

    private void setupWinConditionSelection() {
        boolean bestOfSelected = this.winCondition == WinCondition.BEST_OF;
        boolean playAllSelected = this.winCondition == WinCondition.PLAY_ALL;
        boolean firstToWinSelected = this.winCondition == WinCondition.FIRST_TO_WIN;
        String selectedText = GUIMessages.getText(this.sender, SECTION, "win-condition-selected", "&a&l\u2713 SELECTED");
        String clickSelectText = GUIMessages.getText(this.sender, SECTION, "click-to-select", "&eClick to select");
        String bestOfTitle = GUIMessages.getText(this.sender, SECTION, "best-of-title", "Best Of");
        String bestOfLine1 = GUIMessages.getText(this.sender, SECTION, "best-of-line1", "&7First player to win");
        String bestOfLine2 = GUIMessages.getText(this.sender, SECTION, "best-of-line2", "&7the majority of rounds");
        String bestOfLine3 = GUIMessages.getText(this.sender, SECTION, "best-of-line3", "&7wins the match.");
        String bestOfExample = GUIMessages.getText(this.sender, SECTION, "best-of-example", "&7Example: Best of 5 =");
        String bestOfExVal = GUIMessages.getText(this.sender, SECTION, "best-of-example-value", "&7First to 3 wins");
        this.setItem(20, new GUIItem.Builder(Material.GOLDEN_SWORD).name((bestOfSelected ? "&a&l" : "&e") + bestOfTitle).lore("", bestOfLine1, bestOfLine2, bestOfLine3, "", bestOfExample, bestOfExVal, "", bestOfSelected ? selectedText : clickSelectText).glowing(bestOfSelected).buildItem(), event -> {
            this.winCondition = WinCondition.BEST_OF;
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        });
        String playAllTitle = GUIMessages.getText(this.sender, SECTION, "play-all-title", "Play All Rounds");
        String playAllLine1 = GUIMessages.getText(this.sender, SECTION, "play-all-line1", "&7Play every round");
        String playAllLine2 = GUIMessages.getText(this.sender, SECTION, "play-all-line2", "&7regardless of score.");
        String playAllLine3 = GUIMessages.getText(this.sender, SECTION, "play-all-line3", "&7Winner is determined");
        String playAllLine4 = GUIMessages.getText(this.sender, SECTION, "play-all-line4", "&7by total rounds won.");
        this.setItem(22, new GUIItem.Builder(Material.IRON_SWORD).name((playAllSelected ? "&a&l" : "&e") + playAllTitle).lore("", playAllLine1, playAllLine2, "", playAllLine3, playAllLine4, "", playAllSelected ? selectedText : clickSelectText).glowing(playAllSelected).buildItem(), event -> {
            this.winCondition = WinCondition.PLAY_ALL;
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        });
        String ftwTitle = GUIMessages.getText(this.sender, SECTION, "first-to-win-title", "First to Win");
        String ftwLine1 = GUIMessages.getText(this.sender, SECTION, "first-to-win-line1", "&7First player to win");
        String ftwLine2 = GUIMessages.getText(this.sender, SECTION, "first-to-win-line2", "&e" + this.rounds + " rounds &7wins!");
        String ftwLine3 = GUIMessages.getText(this.sender, SECTION, "first-to-win-line3", "");
        String ftwLine4 = GUIMessages.getText(this.sender, SECTION, "first-to-win-line4", "&7Match continues until target.");
        this.setItem(24, new GUIItem.Builder(Material.DIAMOND_SWORD).name((firstToWinSelected ? "&a&l" : "&e") + ftwTitle).lore("", ftwLine1, ftwLine2, "", ftwLine4, "", firstToWinSelected ? selectedText : clickSelectText).glowing(firstToWinSelected).buildItem(), event -> {
            this.winCondition = WinCondition.FIRST_TO_WIN;
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        });
        String winCondTitle = GUIMessages.getText(this.sender, SECTION, "win-conditions-title", "&6&lWin Conditions");
        String winCondLine1 = GUIMessages.getText(this.sender, SECTION, "win-conditions-line1", "&7Choose how the winner");
        String winCondLine2 = GUIMessages.getText(this.sender, SECTION, "win-conditions-line2", "&7of the duel is determined.");
        this.setItem(13, this.createItem(Material.BOOK, winCondTitle, "", winCondLine1, winCondLine2, ""));
    }

    private int[] getContentSlots() {
        return new int[]{19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    }

    private void setupPagination(int totalPages) {
        if (this.page > 1) {
            this.setItem(18, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        if (this.page < totalPages) {
            this.setItem(26, GUIItem.nextPage(this.page, totalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
    }

    private ItemStack createSettingsSummary() {
        String kitName = this.selectedKit != null ? this.stripColors(this.selectedKit.getDisplayName()) : GUIMessages.getText(this.sender, SECTION, "not-selected", "&cNot selected");
        String arenaName = this.selectedArena != null ? this.selectedArena.getDisplayName() : GUIMessages.getText(this.sender, SECTION, "random", "Random");
        String titleText = GUIMessages.getText(this.sender, SECTION, "current-settings-title", "&6&lCurrent Settings");
        String kitLabel = GUIMessages.getText(this.sender, SECTION, "kit-label", "&7Kit: &f{kit}").replace("{kit}", kitName);
        String arenaLabel = GUIMessages.getText(this.sender, SECTION, "arena-label", "&7Arena: &f{arena}").replace("{arena}", arenaName);
        String roundsLabel = GUIMessages.getText(this.sender, SECTION, "rounds-label", "&7Rounds: &f{rounds}").replace("{rounds}", String.valueOf(this.rounds));
        String modeLabel = GUIMessages.getText(this.sender, SECTION, "mode-label", "&7Mode: &f{mode}").replace("{mode}", this.winCondition.getDisplayName());
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(kitLabel);
        lore.add(arenaLabel);
        lore.add(roundsLabel);
        lore.add(modeLabel);
        lore.add("");
        return this.createItem(Material.PAPER, titleText, lore);
    }

    private ItemStack createConfirmButton() {
        if (this.selectedKit == null) {
            String selectKitText = GUIMessages.getText(this.sender, SECTION, "select-kit-first", "&c&lSelect a Kit First");
            String mustSelectLine1 = GUIMessages.getText(this.sender, SECTION, "must-select-kit-line1", "&7You must select a kit");
            String mustSelectLine2 = GUIMessages.getText(this.sender, SECTION, "must-select-kit-line2", "&7before confirming");
            return this.createItem(Material.GRAY_WOOL, selectKitText, "", mustSelectLine1, mustSelectLine2, "");
        }
        String confirmText = GUIMessages.getText(this.sender, SECTION, "confirm-settings", "&a&lConfirm Settings");
        String clickToApply = GUIMessages.getText(this.sender, SECTION, "click-to-apply", "&7Click to apply these settings");
        return this.createItem(Material.LIME_WOOL, confirmText, "", clickToApply, "");
    }

    private ItemStack createPageInfo() {
        String pageText = GUIMessages.getText(this.sender, SECTION, "page-indicator", "&7Page {page}").replace("{page}", String.valueOf(this.page));
        String showingText = GUIMessages.getText(this.sender, SECTION, "showing-page", "&7Showing page {page}").replace("{page}", String.valueOf(this.page));
        return this.createItem(Material.MAP, pageText, "", showingText, "");
    }

    private String stripColors(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
        if (slot == 49 && this.selectedKit != null) {
            if (this.parentGUI != null) {
                this.parentGUI.setSelectedKit(this.selectedKit);
                this.parentGUI.setSelectedArena(this.selectedArena);
                this.parentGUI.setRounds(this.rounds);
                this.parentGUI.setWinCondition(this.winCondition);
                this.parentGUI.open(player);
            } else {
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aSettings configured!");
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        }
    }

    public DuelKit getSelectedKit() {
        return this.selectedKit;
    }

    public DuelArena getSelectedArena() {
        return this.selectedArena;
    }

    public int getRounds() {
        return this.rounds;
    }

    public WinCondition getWinCondition() {
        return this.winCondition;
    }

    public static enum SelectionMode {
        KIT("Select Kit", Material.DIAMOND_SWORD),
        ARENA("Select Arena", Material.GRASS_BLOCK),
        ROUNDS("Select Rounds", Material.REPEATER),
        WIN_CONDITION("Win Condition", Material.GOLDEN_SWORD);

        private final String displayName;
        private final Material icon;

        private SelectionMode(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Material getIcon() {
            return this.icon;
        }
    }
}

