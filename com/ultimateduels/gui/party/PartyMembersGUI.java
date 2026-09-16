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
import com.ultimateduels.gui.party.PartyInviteGUI;
import com.ultimateduels.gui.party.PartyMainGUI;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.stats.StatsManager;
import com.ultimateduels.utils.MessageUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PartyMembersGUI
extends AbstractGUI {
    private final PartyManager partyManager;
    private final StatsManager statsManager;
    private Player currentPlayer;
    private UUID partyLeaderUUID;
    private String partyLeaderName;
    private Set<UUID> partyAllMembers;
    private Set<UUID> partyMembersOnly;
    private int partySize;
    private boolean partyIsOpen;

    public PartyMembersGUI(UltimateDuels plugin) {
        super(plugin, "&5&lParty Members", 4);
        this.partyManager = plugin.getPartyManager();
        this.statsManager = plugin.getStatsManager();
    }

    @Override
    public void open(Player player) {
        this.currentPlayer = player;
        super.open(player);
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.PURPLE_STAINED_GLASS_PANE);
        if (!this.partyManager.isInParty(this.currentPlayer.getUniqueId())) {
            this.setItem(13, this.createItem(Material.BARRIER, "&c&lNot in a Party", "", "&7You are not in a party.", ""));
            this.setItem(31, GUIItem.closeButton());
            return;
        }
        if (!this.loadPartyData()) {
            this.setItem(13, this.createItem(Material.BARRIER, "&c&lError Loading Party", "", "&7Could not load party data.", ""));
            this.setItem(31, GUIItem.closeButton());
            return;
        }
        boolean isLeader = this.partyManager.isPartyLeader(this.currentPlayer.getUniqueId());
        this.displayMembers(isLeader);
        this.setItem(4, this.createPartyStatsItem());
        this.setItem(27, GUIItem.backButton(event -> new PartyMainGUI(this.plugin).open(event.getPlayer())));
        this.setItem(31, new GUIItem.Builder(Material.EMERALD).name("&a&lInvite More").lore("", "&7Invite more players", "&7to the party.", "", "&eClick to invite").onClick(event -> new PartyInviteGUI(this.plugin).open(event.getPlayer())).build());
        this.setItem(35, GUIItem.closeButton());
    }

    private boolean loadPartyData() {
        try {
            Method method;
            Method method2;
            Party party = this.partyManager.getParty(this.currentPlayer.getUniqueId());
            if (party == null) {
                return false;
            }
            Class<?> partyClass = party.getClass();
            try {
                method2 = partyClass.getMethod("getLeaderUUID", new Class[0]);
                this.partyLeaderUUID = (UUID)method2.invoke((Object)party, new Object[0]);
            }
            catch (NoSuchMethodException e) {
                try {
                    method = partyClass.getMethod("getLeaderId", new Class[0]);
                    this.partyLeaderUUID = (UUID)method.invoke((Object)party, new Object[0]);
                }
                catch (NoSuchMethodException e2) {
                    return false;
                }
            }
            try {
                method2 = partyClass.getMethod("getLeaderName", new Class[0]);
                this.partyLeaderName = (String)method2.invoke((Object)party, new Object[0]);
            }
            catch (NoSuchMethodException e) {
                Player leader = Bukkit.getPlayer((UUID)this.partyLeaderUUID);
                this.partyLeaderName = leader != null ? leader.getName() : "Unknown";
            }
            try {
                Set members;
                method2 = partyClass.getMethod("getAllMembers", new Class[0]);
                this.partyAllMembers = members = (Set)method2.invoke((Object)party, new Object[0]);
            }
            catch (NoSuchMethodException e) {
                return false;
            }
            try {
                method2 = partyClass.getMethod("getMembers", new Class[0]);
                Object result = method2.invoke((Object)party, new Object[0]);
                if (result instanceof Set) {
                    Set members;
                    this.partyMembersOnly = members = (Set)result;
                } else {
                    this.partyMembersOnly = new HashSet<UUID>(this.partyAllMembers);
                    this.partyMembersOnly.remove(this.partyLeaderUUID);
                }
            }
            catch (NoSuchMethodException e) {
                this.partyMembersOnly = new HashSet<UUID>(this.partyAllMembers);
                this.partyMembersOnly.remove(this.partyLeaderUUID);
            }
            try {
                method2 = partyClass.getMethod("getSize", new Class[0]);
                this.partySize = (Integer)method2.invoke((Object)party, new Object[0]);
            }
            catch (NoSuchMethodException e) {
                this.partySize = this.partyAllMembers.size();
            }
            try {
                method2 = partyClass.getMethod("isOpen", new Class[0]);
                this.partyIsOpen = (Boolean)method2.invoke((Object)party, new Object[0]);
            }
            catch (NoSuchMethodException e) {
                try {
                    method = partyClass.getMethod("isPublic", new Class[0]);
                    this.partyIsOpen = (Boolean)method.invoke((Object)party, new Object[0]);
                }
                catch (NoSuchMethodException e2) {
                    this.partyIsOpen = false;
                }
            }
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to load party data: " + e.getMessage());
            return false;
        }
    }

    private void displayMembers(boolean isLeader) {
        ArrayList<UUID> allMembers = new ArrayList<UUID>();
        allMembers.add(this.partyLeaderUUID);
        for (UUID memberId : this.partyMembersOnly) {
            if (memberId.equals(this.partyLeaderUUID)) continue;
            allMembers.add(memberId);
        }
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int slotIndex = 0;
        for (UUID memberId : allMembers) {
            if (slotIndex >= slots.length) break;
            int slot = slots[slotIndex++];
            boolean isMemberLeader = memberId.equals(this.partyLeaderUUID);
            boolean isSelf = memberId.equals(this.currentPlayer.getUniqueId());
            this.setItem(slot, this.createMemberItem(memberId, isMemberLeader, isSelf, isLeader), event -> this.handleMemberClick((AbstractGUI.GUIClickEvent)event, memberId, isMemberLeader, isLeader));
        }
    }

    private ItemStack createMemberItem(UUID memberId, boolean isMemberLeader, boolean isSelf, boolean viewerIsLeader) {
        String memberName = this.getMemberName(memberId);
        Player member = Bukkit.getPlayer((UUID)memberId);
        boolean online = member != null && member.isOnline();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        if (isMemberLeader) {
            lore.add("&6\u2605 &eParty Leader");
        } else {
            lore.add("&7Member");
        }
        lore.add("");
        lore.add("&7Status: " + (online ? "&aOnline" : "&cOffline"));
        if (member != null && online) {
            PlayerStats stats = this.getPlayerStats(memberId);
            if (stats != null) {
                lore.add("&7Elo: &f" + stats.getGlobalElo());
            }
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(memberId)) {
                lore.add("&7Activity: &cIn Duel");
            } else if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(memberId)) {
                lore.add("&7Activity: &6In FFA");
            } else if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(memberId)) {
                lore.add("&7Activity: &eIn Queue");
            }
        }
        lore.add("");
        if (isSelf) {
            lore.add("&7This is you!");
        } else if (viewerIsLeader && !isMemberLeader) {
            lore.add("&eLeft-click: &7Promote to leader");
            lore.add("&cRight-click: &7Kick from party");
        } else if (viewerIsLeader && isMemberLeader) {
            lore.add("&7You are the leader");
        }
        String displayName = (isMemberLeader ? "&6\u2605 " : "") + (online ? "&a" : "&7") + memberName;
        if (isSelf) {
            displayName = displayName + " &e(You)";
        }
        return new GUIItem.Builder(Material.PLAYER_HEAD).skullOwner(memberName).name(displayName).lore(lore).glowing(isMemberLeader).buildItem();
    }

    private String getMemberName(UUID memberId) {
        if (memberId.equals(this.partyLeaderUUID)) {
            return this.partyLeaderName;
        }
        Player player = Bukkit.getPlayer((UUID)memberId);
        if (player != null) {
            return player.getName();
        }
        return Bukkit.getOfflinePlayer((UUID)memberId).getName();
    }

    private ItemStack createPartyStatsItem() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("&7Leader: &f" + this.partyLeaderName);
        lore.add("&7Members: &f" + this.partySize + "/" + this.partyManager.getMaxPartySize());
        lore.add("&7Status: " + (this.partyIsOpen ? "&aOpen" : "&cInvite Only"));
        lore.add("");
        int online = 0;
        for (UUID memberId : this.partyAllMembers) {
            Player p = Bukkit.getPlayer((UUID)memberId);
            if (p == null || !p.isOnline()) continue;
            ++online;
        }
        lore.add("&7Online: &a" + online + "&7/&f" + this.partySize);
        int totalElo = 0;
        int count = 0;
        for (UUID memberId : this.partyAllMembers) {
            PlayerStats stats = this.getPlayerStats(memberId);
            if (stats == null) continue;
            totalElo += stats.getGlobalElo();
            ++count;
        }
        if (count > 0) {
            lore.add("&7Avg Elo: &f" + totalElo / count);
        }
        lore.add("");
        return this.createItem(Material.TOTEM_OF_UNDYING, "&5&lParty Stats", lore);
    }

    private PlayerStats getPlayerStats(UUID uuid) {
        if (this.statsManager == null) {
            return null;
        }
        return this.statsManager.getStats(uuid);
    }

    private void handleMemberClick(AbstractGUI.GUIClickEvent event, UUID memberId, boolean isMemberLeader, boolean viewerIsLeader) {
        Player player = event.getPlayer();
        if (memberId.equals(player.getUniqueId()) || !viewerIsLeader || isMemberLeader) {
            return;
        }
        Player target = Bukkit.getPlayer((UUID)memberId);
        if (target == null || !target.isOnline()) {
            MessageUtils.sendMessage(player, "&cThis player is offline!");
            return;
        }
        if (event.isLeftClick()) {
            this.partyManager.transferLeader(player, target);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.closeInventory();
        } else if (event.isRightClick()) {
            this.partyManager.kick(player, target);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            this.refresh(player);
        }
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }
}

