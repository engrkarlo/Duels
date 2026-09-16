/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.gui.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIItem;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.gui.party.PartyBrowseGUI;
import com.ultimateduels.gui.party.PartyInviteGUI;
import com.ultimateduels.gui.party.PartyKitSelectorGUI;
import com.ultimateduels.gui.party.PartyMembersGUI;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyMainGUI
extends AbstractGUI {
    private final PartyManager partyManager;
    private Player currentPlayer;
    private static final String SECTION = "party-main";

    public PartyMainGUI(UltimateDuels plugin) {
        super(plugin, GUIMessages.getTitle(null, SECTION, "&5&lParty Menu"), 5);
        this.partyManager = plugin.getPartyManager();
    }

    @Override
    public void open(Player player) {
        this.currentPlayer = player;
        super.open(player);
    }

    private Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.PURPLE_STAINED_GLASS_PANE);
        Party party = this.partyManager.getParty(this.getCurrentPlayer().getUniqueId());
        if (party == null) {
            this.setupNoPartyView();
        } else {
            this.setupPartyView(party);
        }
        this.setItem(40, GUIItem.closeButton());
    }

    private void setupNoPartyView() {
        Player player = this.getCurrentPlayer();
        String createTitle = GUIMessages.getText(player, SECTION, "create-party-title", "&a&lCreate Party");
        String createLine1 = GUIMessages.getText(player, SECTION, "create-party-line1", "&7Create a new party");
        String createLine2 = GUIMessages.getText(player, SECTION, "create-party-line2", "&7and become the leader.");
        String createLine3 = GUIMessages.getText(player, SECTION, "create-party-line3", "&7You can then invite");
        String createLine4 = GUIMessages.getText(player, SECTION, "create-party-line4", "&7friends to join!");
        String clickToCreate = GUIMessages.getText(player, SECTION, "click-to-create", "&aClick to create");
        this.setItem(11, new GUIItem.Builder(Material.LIME_WOOL).name(createTitle).lore("", createLine1, createLine2, "", createLine3, createLine4, "", clickToCreate).onClick(event -> {
            this.partyManager.createParty(event.getPlayer());
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            this.refresh(event.getPlayer());
        }).build());
        boolean hasInvite = this.partyManager.hasPendingInvite(player.getUniqueId());
        String acceptTitle = hasInvite ? GUIMessages.getText(player, SECTION, "accept-invite-title", "&a&lAccept Invite") : GUIMessages.getText(player, SECTION, "no-pending-invite-title", "&7&lNo Pending Invite");
        String inviteStatus = hasInvite ? GUIMessages.getText(player, SECTION, "has-pending-invite", "&7You have a pending invite!") : GUIMessages.getText(player, SECTION, "no-invites", "&7No party invites.");
        String clickToAccept = GUIMessages.getText(player, SECTION, "click-to-accept", "&aClick to accept");
        this.setItem(13, new GUIItem.Builder(hasInvite ? Material.LIME_DYE : Material.GRAY_DYE).name(acceptTitle).lore("", inviteStatus, "", hasInvite ? clickToAccept : "").glowing(hasInvite).onClick(event -> {
            if (hasInvite) {
                this.partyManager.acceptInvite(event.getPlayer());
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                this.refresh(event.getPlayer());
            }
        }).build());
        String browseTitle = GUIMessages.getText(player, SECTION, "browse-parties-title", "&e&lBrowse Parties");
        String browseLine1 = GUIMessages.getText(player, SECTION, "browse-parties-line1", "&7View open parties");
        String browseLine2 = GUIMessages.getText(player, SECTION, "browse-parties-line2", "&7that you can join.");
        String clickToBrowse = GUIMessages.getText(player, SECTION, "click-to-browse", "&eClick to browse");
        this.setItem(15, new GUIItem.Builder(Material.SPYGLASS).name(browseTitle).lore("", browseLine1, browseLine2, "", clickToBrowse).onClick(event -> new PartyBrowseGUI(this.plugin).open(event.getPlayer())).build());
        this.setItem(22, this.createNoPartyInfoItem());
    }

    private void setupPartyView(Party party) {
        Player player = this.getCurrentPlayer();
        boolean isLeader = party.isLeader(player.getUniqueId());
        this.setItem(4, this.createPartyInfoItem(party));
        String viewMembersTitle = GUIMessages.getText(player, SECTION, "view-members-title", "&e&lView Members");
        String viewMembersLine1 = GUIMessages.getText(player, SECTION, "view-members-line1", "&7See all party members");
        String viewMembersLine2 = GUIMessages.getText(player, SECTION, "view-members-line2", "&7and their status.");
        String membersCountLabel = GUIMessages.getText(player, SECTION, "members-count-label", "&7Members: &f{count}/{max}").replace("{count}", String.valueOf(party.getSize())).replace("{max}", String.valueOf(this.partyManager.getMaxPartySize()));
        String clickToView = GUIMessages.getText(player, SECTION, "click-to-view", "&eClick to view");
        this.setItem(10, new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(party.getLeaderName()).name(viewMembersTitle).lore("", viewMembersLine1, viewMembersLine2, "", membersCountLabel, "", clickToView).onClick(event -> new PartyMembersGUI(this.plugin).open(event.getPlayer())).build());
        String inviteTitle = GUIMessages.getText(player, SECTION, "invite-player-title", "&a&lInvite Player");
        String inviteLine1 = GUIMessages.getText(player, SECTION, "invite-player-line1", "&7Invite a player to");
        String inviteLine2 = GUIMessages.getText(player, SECTION, "invite-player-line2", "&7your party.");
        String clickToInvite = GUIMessages.getText(player, SECTION, "click-to-invite", "&aClick to invite");
        String onlyLeaderInvite = GUIMessages.getText(player, SECTION, "only-leader-can-invite", "&cOnly leader can invite");
        this.setItem(12, new GUIItem.Builder(Material.WRITABLE_BOOK).name(inviteTitle).lore("", inviteLine1, inviteLine2, "", isLeader ? clickToInvite : onlyLeaderInvite).onClick(event -> {
            if (isLeader) {
                new PartyInviteGUI(this.plugin).open(event.getPlayer());
            } else {
                String errorMsg = GUIMessages.getText(event.getPlayer(), SECTION, "only-leader-invite-error", "&cOnly the party leader can invite players!");
                MessageUtils.sendMessage(event.getPlayer(), errorMsg);
            }
        }).build());
        if (isLeader) {
            String settingsTitle = GUIMessages.getText(player, SECTION, "party-settings-title", "&6&lParty Settings");
            String settingsLine = GUIMessages.getText(player, SECTION, "party-settings-line", "&7Configure party settings.");
            String publicLabel = GUIMessages.getText(player, SECTION, "public-label", "&7Public: {status}").replace("{status}", party.isPublic() ? GUIMessages.getText(player, SECTION, "yes", "&aYes") : GUIMessages.getText(player, SECTION, "no", "&cNo"));
            String clickToToggle = GUIMessages.getText(player, SECTION, "click-to-toggle", "&eClick to toggle");
            this.setItem(14, new GUIItem.Builder(Material.COMPARATOR).name(settingsTitle).lore("", settingsLine, "", publicLabel, "", clickToToggle).onClick(event -> {
                party.setPublic(!party.isPublic());
                String newStatus = party.isPublic() ? GUIMessages.getText(event.getPlayer(), SECTION, "now-public", "&apublic") : GUIMessages.getText(event.getPlayer(), SECTION, "now-private", "&cprivate");
                String broadcastMsg = GUIMessages.getText(event.getPlayer(), SECTION, "party-now-status", "&eParty is now {status}&e!").replace("{status}", newStatus);
                this.broadcastToParty(party, broadcastMsg);
                this.refresh(event.getPlayer());
            }).build());
        }
        String pvpTitle = GUIMessages.getText(player, SECTION, "party-vs-party-title", "&c&lParty vs Party");
        String pvpLine1 = GUIMessages.getText(player, SECTION, "party-vs-party-line1", "&7Queue against another");
        String pvpLine2 = GUIMessages.getText(player, SECTION, "party-vs-party-line2", "&7party for a team fight!");
        String minSizeLabel = GUIMessages.getText(player, SECTION, "min-size-label", "&7Min size: &f2 players");
        String clickToQueue = GUIMessages.getText(player, SECTION, "click-to-queue", "&eClick to queue");
        String needMembers = GUIMessages.getText(player, SECTION, "need-2-members", "&cNeed 2+ members");
        this.setItem(16, new GUIItem.Builder(Material.IRON_SWORD).name(pvpTitle).lore("", pvpLine1, pvpLine2, "", minSizeLabel, "", party.getSize() >= 2 ? clickToQueue : needMembers).onClick(event -> {
            if (party.getSize() < 2) {
                String errorMsg = GUIMessages.getText(event.getPlayer(), SECTION, "need-2-members-error", "&cYou need at least 2 party members!");
                MessageUtils.sendMessage(event.getPlayer(), errorMsg);
                return;
            }
            if (!isLeader) {
                String errorMsg = GUIMessages.getText(event.getPlayer(), SECTION, "only-leader-queue-error", "&cOnly the party leader can start queue!");
                MessageUtils.sendMessage(event.getPlayer(), errorMsg);
                return;
            }
            new PartyKitSelectorGUI(this.plugin, PartyKitSelectorGUI.PartyMode.PARTY_VS_PARTY).open(event.getPlayer());
        }).build());
        String splitTitle = GUIMessages.getText(player, SECTION, "party-split-title", "&6&lParty Split");
        String splitLine1 = GUIMessages.getText(player, SECTION, "party-split-line1", "&7Split your party into");
        String splitLine2 = GUIMessages.getText(player, SECTION, "party-split-line2", "&7two teams for a scrim!");
        this.setItem(28, new GUIItem.Builder(Material.GOLDEN_SWORD).name(splitTitle).lore("", splitLine1, splitLine2, "", minSizeLabel, "", party.getSize() >= 2 ? clickToQueue : needMembers).onClick(event -> {
            if (party.getSize() < 2) {
                String errorMsg = GUIMessages.getText(event.getPlayer(), SECTION, "need-2-members-split-error", "&cYou need at least 2 party members!");
                MessageUtils.sendMessage(event.getPlayer(), errorMsg);
                return;
            }
            if (!isLeader) {
                String errorMsg = GUIMessages.getText(event.getPlayer(), SECTION, "only-leader-split-error", "&cOnly the party leader can start party split!");
                MessageUtils.sendMessage(event.getPlayer(), errorMsg);
                return;
            }
            new PartyKitSelectorGUI(this.plugin, PartyKitSelectorGUI.PartyMode.PARTY_SPLIT).open(event.getPlayer());
        }).build());
        if (isLeader) {
            String warpTitle = GUIMessages.getText(player, SECTION, "warp-members-title", "&b&lWarp Members");
            String warpLine1 = GUIMessages.getText(player, SECTION, "warp-members-line1", "&7Teleport all party");
            String warpLine2 = GUIMessages.getText(player, SECTION, "warp-members-line2", "&7members to you.");
            String clickToWarp = GUIMessages.getText(player, SECTION, "click-to-warp", "&eClick to warp");
            this.setItem(30, new GUIItem.Builder(Material.ENDER_PEARL).name(warpTitle).lore("", warpLine1, warpLine2, "", clickToWarp).onClick(event -> {
                event.getPlayer().performCommand("party warp");
                event.getPlayer().closeInventory();
            }).build());
        }
        String chatTitle = GUIMessages.getText(player, SECTION, "party-chat-title", "&d&lParty Chat");
        String chatLine1 = GUIMessages.getText(player, SECTION, "party-chat-line1", "&7Use &f@p <message> &7to");
        String chatLine2 = GUIMessages.getText(player, SECTION, "party-chat-line2", "&7chat with your party.");
        String chatLine3 = GUIMessages.getText(player, SECTION, "party-chat-line3", "&7Or use &f/party chat <msg>");
        this.setItem(32, new GUIItem.Builder(Material.OAK_SIGN).name(chatTitle).lore("", chatLine1, chatLine2, "", chatLine3).buildItem());
        String leaveTitle = GUIMessages.getText(player, SECTION, "leave-party-title", "&c&lLeave Party");
        String leaveInfo = isLeader && party.getSize() > 1 ? GUIMessages.getText(player, SECTION, "leadership-transfer", "&7Leadership will transfer") : (isLeader ? GUIMessages.getText(player, SECTION, "party-disband", "&7Party will be disbanded") : GUIMessages.getText(player, SECTION, "leave-party-desc", "&7Leave the party"));
        String clickToLeave = GUIMessages.getText(player, SECTION, "click-to-leave", "&cClick to leave");
        this.setItem(34, new GUIItem.Builder(Material.RED_WOOL).name(leaveTitle).lore("", leaveInfo, "", clickToLeave).onClick(event -> {
            event.getPlayer().closeInventory();
            event.getPlayer().performCommand("party leave");
        }).build());
        if (isLeader) {
            String disbandTitle = GUIMessages.getText(player, SECTION, "disband-party-title", "&4&lDisband Party");
            String disbandLine1 = GUIMessages.getText(player, SECTION, "disband-party-line1", "&cDisband the entire party.");
            String disbandLine2 = GUIMessages.getText(player, SECTION, "disband-party-line2", "&cAll members will be removed.");
            String clickToDisband = GUIMessages.getText(player, SECTION, "click-to-disband", "&4Click to disband");
            this.setItem(36, new GUIItem.Builder(Material.TNT).name(disbandTitle).lore("", disbandLine1, disbandLine2, "", clickToDisband).onClick(event -> {
                this.partyManager.disbandParty(event.getPlayer());
                event.getPlayer().closeInventory();
            }).build());
        }
    }

    private ItemStack createPartyInfoItem(Party party) {
        Player player = this.getCurrentPlayer();
        String infoTitle = GUIMessages.getText(player, SECTION, "party-info-title", "&5&lParty Info");
        String leaderLabel = GUIMessages.getText(player, SECTION, "leader-label", "&7Leader: &f{leader}").replace("{leader}", party.getLeaderName());
        String membersLabel = GUIMessages.getText(player, SECTION, "members-label", "&7Members: &f{count}/{max}").replace("{count}", String.valueOf(party.getSize())).replace("{max}", String.valueOf(this.partyManager.getMaxPartySize()));
        String statusLabel = GUIMessages.getText(player, SECTION, "status-label", "&7Status: {status}").replace("{status}", party.isPublic() ? GUIMessages.getText(player, SECTION, "public", "&aPublic") : GUIMessages.getText(player, SECTION, "invite-only", "&cInvite Only"));
        String membersListLabel = GUIMessages.getText(player, SECTION, "members-list-label", "&7Members:");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add(leaderLabel);
        lore.add(membersLabel);
        lore.add(statusLabel);
        lore.add("");
        lore.add(membersListLabel);
        Player leader = Bukkit.getPlayer((UUID)party.getLeaderUUID());
        String leaderStatus = leader != null && leader.isOnline() ? "&a\u25cf" : "&c\u25cf";
        String leaderTag = GUIMessages.getText(player, SECTION, "leader-tag", "&7(Leader)");
        lore.add(leaderStatus + " &e\u2605 " + party.getLeaderName() + " " + leaderTag);
        for (UUID memberId : party.getAllMembers()) {
            if (memberId.equals(party.getLeaderUUID())) continue;
            Player member = Bukkit.getPlayer((UUID)memberId);
            String status = member != null && member.isOnline() ? "&a\u25cf" : "&c\u25cf";
            String memberName = party.getMemberName(memberId);
            lore.add(status + " &f" + memberName);
        }
        lore.add("");
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(party.getLeaderName()).name(infoTitle).lore(lore).buildItem();
    }

    private ItemStack createNoPartyInfoItem() {
        Player player = this.getCurrentPlayer();
        String noPartyTitle = GUIMessages.getText(player, SECTION, "no-party-title", "&5&lNo Party");
        String noPartyLine1 = GUIMessages.getText(player, SECTION, "no-party-line1", "&7You are not in a party.");
        String createToLabel = GUIMessages.getText(player, SECTION, "create-to-label", "&7Create a party to:");
        String feature1 = GUIMessages.getText(player, SECTION, "feature-invite", "&a\u2022 Invite friends");
        String feature2 = GUIMessages.getText(player, SECTION, "feature-queue", "&a\u2022 Queue for Party vs Party");
        String feature3 = GUIMessages.getText(player, SECTION, "feature-scrims", "&a\u2022 Have internal scrims");
        String feature4 = GUIMessages.getText(player, SECTION, "feature-chat", "&a\u2022 Chat privately");
        return new GUIItem.Builder(Material.PAPER).name(noPartyTitle).lore("", noPartyLine1, "", createToLabel, feature1, feature2, feature3, feature4, "").buildItem();
    }

    private void broadcastToParty(Party party, String message) {
        for (UUID memberId : party.getAllMembers()) {
            Player member = Bukkit.getPlayer((UUID)memberId);
            if (member == null || !member.isOnline()) continue;
            MessageUtils.sendMessage(member, message);
        }
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

