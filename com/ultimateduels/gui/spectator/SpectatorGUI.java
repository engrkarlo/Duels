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
package com.ultimateduels.gui.spectator;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SpectatorGUI
extends AbstractGUI {
    private final DuelManager duelManager;
    private int page = 1;
    private static final int DUELS_PER_PAGE = 21;
    private static final String SECTION = "spectator";

    public SpectatorGUI(UltimateDuels plugin) {
        super(plugin, GUIMessages.getTitle(null, SECTION, "&7&lSpectate a Duel"), 5);
        this.duelManager = plugin.getDuelManager();
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        ArrayList<DuelMatch> matches = new ArrayList<DuelMatch>(this.duelManager.getSpectatableMatches());
        int totalPages = Math.max(1, (int)Math.ceil((double)matches.size() / 21.0));
        int startIndex = (this.page - 1) * 21;
        int endIndex = Math.min(startIndex + 21, matches.size());
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        if (matches.isEmpty()) {
            String noMatchesTitle = GUIMessages.getText(null, SECTION, "no-matches-title", "&c&lNo Active Duels");
            String noMatchesLine1 = GUIMessages.getText(null, SECTION, "no-matches-line1", "&7There are no duels to");
            String noMatchesLine2 = GUIMessages.getText(null, SECTION, "no-matches-line2", "&7spectate right now.");
            String checkBack = GUIMessages.getText(null, SECTION, "check-back", "&7Check back later!");
            this.setItem(22, this.createItem(Material.BARRIER, noMatchesTitle, "", noMatchesLine1, noMatchesLine2, "", checkBack));
        } else {
            int slotIndex = 0;
            for (int i = startIndex; i < endIndex && slotIndex < slots.length; ++i) {
                DuelMatch match = (DuelMatch)matches.get(i);
                int slot = slots[slotIndex++];
                this.setItem(slot, this.createMatchItem(match), event -> this.handleMatchClick((AbstractGUI.GUIClickEvent)event, match));
            }
        }
        this.setItem(4, this.createInfoItem(matches.size()));
        if (this.page > 1) {
            this.setItem(36, GUIItem.previousPage(this.page, event -> {
                --this.page;
                this.refresh(event.getPlayer());
            }));
        }
        String refreshTitle = GUIMessages.getText(null, SECTION, "refresh-title", "&e&lRefresh");
        String refreshLine = GUIMessages.getText(null, SECTION, "refresh-line", "&7Refresh duel list");
        String clickToRefresh = GUIMessages.getText(null, SECTION, "click-to-refresh", "&eClick to refresh");
        this.setItem(39, new GUIItem.Builder(Material.SUNFLOWER).name(refreshTitle).lore("", refreshLine, "", clickToRefresh).onClick(event -> {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            this.refresh(event.getPlayer());
        }).build());
        if (this.page < totalPages) {
            int finalTotalPages = totalPages;
            this.setItem(44, GUIItem.nextPage(this.page, finalTotalPages, event -> {
                ++this.page;
                this.refresh(event.getPlayer());
            }));
        }
        this.setItem(40, GUIItem.closeButton());
    }

    private ItemStack createMatchItem(DuelMatch match) {
        String title;
        Material icon;
        boolean isTeamMatch;
        String teamDuelType = GUIMessages.getText(null, SECTION, "type-team-duel", "&7Type: &dTeam Duel");
        String teamSizeLabel = GUIMessages.getText(null, SECTION, "team-size-label", "&7Size: &f{size1}v{size2}");
        String type1v1 = GUIMessages.getText(null, SECTION, "type-1v1", "&7Type: &e1v1");
        String vsLabel = GUIMessages.getText(null, SECTION, "vs-label", "&7vs");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        boolean bl = isTeamMatch = match.getTeam1().size() > 1 || match.getTeam2().size() > 1;
        if (isTeamMatch) {
            lore.add(teamDuelType);
            lore.add(teamSizeLabel.replace("{size1}", String.valueOf(match.getTeam1().size())).replace("{size2}", String.valueOf(match.getTeam2().size())));
        } else {
            lore.add(type1v1);
            if (!match.getTeam1().isEmpty() && !match.getTeam2().isEmpty()) {
                String name1 = match.getTeam1().get(0).getPlayerName();
                String name2 = match.getTeam2().get(0).getPlayerName();
                lore.add("");
                lore.add("&a" + name1);
                lore.add(vsLabel);
                lore.add("&c" + name2);
            }
        }
        lore.add("");
        String kitLabel = GUIMessages.getText(null, SECTION, "match-kit-label", "&7Kit: &f{kit}").replace("{kit}", match.getKitName());
        String arenaLabel = GUIMessages.getText(null, SECTION, "match-arena-label", "&7Arena: &f{arena}").replace("{arena}", match.getArena().getDisplayName());
        lore.add(kitLabel);
        lore.add(arenaLabel);
        Object stateDisplay = switch (match.getState()) {
            case MatchState.STARTING -> GUIMessages.getText(null, SECTION, "state-starting", "&eStarting...");
            case MatchState.IN_PROGRESS -> GUIMessages.getText(null, SECTION, "state-fighting", "&cFighting");
            case MatchState.ROUND_ENDING -> GUIMessages.getText(null, SECTION, "state-round-ending", "&6Round Ending");
            case MatchState.RESETTING -> GUIMessages.getText(null, SECTION, "state-resetting", "&eResetting...");
            default -> "&7" + match.getState().name();
        };
        String statusLabel = GUIMessages.getText(null, SECTION, "status-label", "&7Status: {status}").replace("{status}", (CharSequence)stateDisplay);
        lore.add(statusLabel);
        if (match.getTotalRounds() > 1) {
            int score1 = match.getTeamScore(1);
            int score2 = match.getTeamScore(2);
            String scoreLabel = GUIMessages.getText(null, SECTION, "score-label", "&7Score: &a{score1} &7- &c{score2}").replace("{score1}", String.valueOf(score1)).replace("{score2}", String.valueOf(score2));
            String roundLabel = GUIMessages.getText(null, SECTION, "round-label", "&7Round: &f{current}/{total}").replace("{current}", String.valueOf(match.getCurrentRound())).replace("{total}", String.valueOf(match.getTotalRounds()));
            lore.add(scoreLabel);
            lore.add(roundLabel);
        }
        String durationLabel = GUIMessages.getText(null, SECTION, "duration-label", "&7Duration: &f{time}").replace("{time}", this.formatDuration(match.getMatchDuration()));
        lore.add(durationLabel);
        int spectatorCount = this.duelManager.getMatchSpectators(match.getMatchId()).size();
        String spectatorsLabel = GUIMessages.getText(null, SECTION, "spectators-label", "&7Spectators: &f{count}").replace("{count}", String.valueOf(spectatorCount));
        lore.add(spectatorsLabel);
        lore.add("");
        lore.add(GUIMessages.getText(null, SECTION, "click-to-spectate", "&aClick to spectate!"));
        Material material = icon = isTeamMatch ? Material.GOLDEN_SWORD : Material.IRON_SWORD;
        if (match.getState() == MatchState.IN_PROGRESS) {
            icon = Material.DIAMOND_SWORD;
        }
        if (isTeamMatch) {
            title = GUIMessages.getText(null, SECTION, "team-duel-title", "&e&lTeam Duel");
        } else {
            String vsTitle;
            String name1 = this.getFirstParticipantName(match);
            String name2 = this.getSecondParticipantName(match);
            title = vsTitle = GUIMessages.getText(null, SECTION, "vs-title", "&e&l{player1} vs {player2}").replace("{player1}", name1).replace("{player2}", name2);
        }
        return new GUIItem.Builder(icon).name(title).lore(lore).glowing(match.getState() == MatchState.IN_PROGRESS).buildItem();
    }

    private String getFirstParticipantName(DuelMatch match) {
        if (match.getTeam1().isEmpty()) {
            return "???";
        }
        return match.getTeam1().get(0).getPlayerName();
    }

    private String getSecondParticipantName(DuelMatch match) {
        if (match.getTeam2().isEmpty()) {
            return "???";
        }
        return match.getTeam2().get(0).getPlayerName();
    }

    private ItemStack createInfoItem(int matchCount) {
        int totalSpectating = 0;
        for (DuelMatch match : this.duelManager.getSpectatableMatches()) {
            totalSpectating += this.duelManager.getMatchSpectators(match.getMatchId()).size();
        }
        String infoTitle = GUIMessages.getText(null, SECTION, "info-title", "&7&lSpectate");
        String activeDuelsLabel = GUIMessages.getText(null, SECTION, "active-duels-label", "&7Active Duels: &f{count}").replace("{count}", String.valueOf(matchCount));
        String totalSpectatorsLabel = GUIMessages.getText(null, SECTION, "total-spectators-label", "&7Total Spectators: &f{count}").replace("{count}", String.valueOf(totalSpectating));
        String clickDuelLine1 = GUIMessages.getText(null, SECTION, "click-duel-line1", "&7Click a duel to start");
        String clickDuelLine2 = GUIMessages.getText(null, SECTION, "click-duel-line2", "&7spectating!");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(activeDuelsLabel);
        lore.add(totalSpectatorsLabel);
        lore.add("");
        lore.add(clickDuelLine1);
        lore.add(clickDuelLine2);
        lore.add("");
        return this.createItem(Material.ENDER_EYE, infoTitle, lore);
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%d:%02d", minutes, seconds %= 60L);
    }

    private void handleMatchClick(AbstractGUI.GUIClickEvent event, DuelMatch match) {
        Player player = event.getPlayer();
        if (match.getState() == MatchState.COMPLETED || match.getState() == MatchState.CANCELLED) {
            MessageUtils.sendMessage(player, "&cThis duel has already ended!");
            this.refresh(player);
            return;
        }
        if (this.duelManager.isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot spectate while in a duel!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou cannot spectate while in FFA!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot spectate while in queue!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        player.closeInventory();
        boolean success = this.duelManager.addSpectator(player, match.getMatchId());
        if (success) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else {
            MessageUtils.sendMessage(player, "&cFailed to start spectating!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

