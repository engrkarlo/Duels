/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.format.NamedTextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.Sound
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.ScoreboardManager
 *  org.bukkit.scoreboard.Team
 */
package com.ultimateduels.party;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.party.model.PartyInvite;
import com.ultimateduels.party.model.PartyResult;
import com.ultimateduels.party.model.PartyResultType;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.queue.model.PartyQueueType;
import com.ultimateduels.queue.model.QueueResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class PartyManager {
    private final UltimateDuels plugin;
    private final Map<UUID, Party> parties;
    private final Map<UUID, UUID> playerPartyMap;
    private final Map<UUID, PartyInvite> pendingInvites;
    private boolean partyEnabled;
    private int maxPartySize;
    private int inviteTimeoutSeconds;
    private boolean partyChatEnabled;
    private String partyChatPrefix;
    private BukkitTask cleanupTask;
    private long totalPartiesCreated;
    private long totalInvitesSent;
    private long lastCleanupTime;

    public PartyManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.parties = new ConcurrentHashMap<UUID, Party>();
        this.playerPartyMap = new ConcurrentHashMap<UUID, UUID>();
        this.pendingInvites = new ConcurrentHashMap<UUID, PartyInvite>();
        this.totalPartiesCreated = 0L;
        this.totalInvitesSent = 0L;
        this.lastCleanupTime = System.currentTimeMillis();
        this.loadSettings();
        this.startCleanupTask();
        plugin.getLogger().info("\u00a7a[PartyManager] Initialized successfully!");
    }

    private void loadSettings() {
        FileConfiguration config = this.plugin.getConfig();
        this.partyEnabled = config.getBoolean("party.enabled", true);
        this.maxPartySize = config.getInt("party.max-size", 10);
        this.inviteTimeoutSeconds = config.getInt("party.invite-timeout-seconds", 60);
        this.partyChatEnabled = config.getBoolean("party.chat.enabled", true);
        this.partyChatPrefix = config.getString("party.chat.prefix", "@");
    }

    private void startCleanupTask() {
        this.cleanupTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::runCleanup, 600L, 600L);
    }

    public PartyResult createParty(@Nonnull Player leader) {
        UUID leaderUUID = leader.getUniqueId();
        if (!this.partyEnabled) {
            return PartyResult.failure(PartyResultType.DISABLED, "\u00a7cParties are currently disabled!");
        }
        if (!leader.hasPermission("ultimateduels.party.create")) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cYou don't have permission to create a party! \u00a77You can still join parties when invited.");
        }
        if (this.isInParty(leaderUUID)) {
            return PartyResult.failure(PartyResultType.ALREADY_IN_PARTY, "\u00a7cYou are already in a party! Use \u00a7e/party leave \u00a7cto leave first.");
        }
        Party party = new Party(UUID.randomUUID(), leaderUUID, leader.getName());
        this.parties.put(party.getPartyId(), party);
        this.playerPartyMap.put(leaderUUID, party.getPartyId());
        ++this.totalPartiesCreated;
        leader.sendMessage("");
        leader.sendMessage("\u00a7a\u00a7lPARTY CREATED!");
        leader.sendMessage("\u00a77You are now the party leader.");
        leader.sendMessage("\u00a77Use \u00a7e/party invite <player> \u00a77to invite players.");
        leader.sendMessage("");
        leader.playSound(leader.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.plugin.getLogger().fine("Player " + leader.getName() + " created a party");
        return PartyResult.success("\u00a7aParty created successfully!");
    }

    public PartyResult disbandParty(@Nonnull Player leader) {
        UUID leaderUUID = leader.getUniqueId();
        Party party = this.getParty(leaderUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (!party.isLeader(leaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cOnly the party leader can disband the party!");
        }
        party.broadcast("\u00a7cThe party has been disbanded by the leader.");
        party.playSound(Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
        for (UUID memberUUID : party.getAllMembers()) {
            this.playerPartyMap.remove(memberUUID);
        }
        this.parties.remove(party.getPartyId());
        this.pendingInvites.entrySet().removeIf(entry -> ((PartyInvite)entry.getValue()).getPartyId().equals(party.getPartyId()));
        this.plugin.getLogger().fine("Party " + String.valueOf(party.getPartyId()) + " disbanded");
        return PartyResult.success("\u00a7aParty disbanded.");
    }

    public PartyResult invite(@Nonnull Player inviter, @Nonnull Player target) {
        UUID inviterUUID = inviter.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        if (inviter.equals((Object)target)) {
            return PartyResult.failure(PartyResultType.CANNOT_INVITE_SELF, "\u00a7cYou cannot invite yourself!");
        }
        Party party = this.getParty(inviterUUID);
        if (party == null) {
            if (!inviter.hasPermission("ultimateduels.party.create")) {
                return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cYou don't have permission to create a party!");
            }
            PartyResult createResult = this.createParty(inviter);
            if (!createResult.success()) {
                return createResult;
            }
            party = this.getParty(inviterUUID);
        }
        if (!party.isLeader(inviterUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cOnly the party leader can invite players!");
        }
        if (party.getSize() >= this.maxPartySize) {
            return PartyResult.failure(PartyResultType.PARTY_FULL, "\u00a7cYour party is full! (Max: " + this.maxPartySize + ")");
        }
        if (this.isInParty(targetUUID)) {
            return PartyResult.failure(PartyResultType.TARGET_IN_PARTY, "\u00a7c" + target.getName() + " is already in a party!");
        }
        PartyInvite existingInvite = this.pendingInvites.get(targetUUID);
        if (existingInvite != null && existingInvite.getPartyId().equals(party.getPartyId())) {
            return PartyResult.failure(PartyResultType.ALREADY_INVITED, "\u00a7cYou have already invited " + target.getName() + "!");
        }
        PartyInvite invite = new PartyInvite(party.getPartyId(), inviterUUID, targetUUID, System.currentTimeMillis());
        this.pendingInvites.put(targetUUID, invite);
        ++this.totalInvitesSent;
        inviter.sendMessage("\u00a7aYou invited \u00a7e" + target.getName() + " \u00a7ato your party.");
        inviter.playSound(inviter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        target.sendMessage("");
        target.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        target.sendMessage("\u00a7e\u00a7lPARTY INVITE");
        target.sendMessage("");
        target.sendMessage("\u00a77You have been invited to \u00a7a" + inviter.getName() + "'s \u00a77party!");
        target.sendMessage("");
        Component acceptButton = ((TextComponent)Component.text((String)"\u00a7a\u00a7l[ACCEPT]").clickEvent(ClickEvent.runCommand((String)"/party accept"))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)"\u00a7aClick to accept")));
        Component denyButton = ((TextComponent)Component.text((String)"\u00a7c\u00a7l[DENY]").clickEvent(ClickEvent.runCommand((String)"/party deny"))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)"\u00a7cClick to deny")));
        target.sendMessage(acceptButton.append((Component)Component.text((String)"  ")).append(denyButton));
        target.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        target.sendMessage("");
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        return PartyResult.success("\u00a7aInvite sent to " + target.getName() + "!");
    }

    public PartyResult acceptInvite(@Nonnull Player player) {
        UUID playerUUID = player.getUniqueId();
        PartyInvite invite = this.pendingInvites.remove(playerUUID);
        if (invite == null) {
            return PartyResult.failure(PartyResultType.NO_INVITE, "\u00a7cYou have no pending party invites!");
        }
        if (invite.isExpired((long)this.inviteTimeoutSeconds * 1000L)) {
            return PartyResult.failure(PartyResultType.INVITE_EXPIRED, "\u00a7cThis party invite has expired!");
        }
        Party party = this.parties.get(invite.getPartyId());
        if (party == null) {
            return PartyResult.failure(PartyResultType.PARTY_DISBANDED, "\u00a7cThis party no longer exists!");
        }
        if (party.getSize() >= this.maxPartySize) {
            return PartyResult.failure(PartyResultType.PARTY_FULL, "\u00a7cThis party is now full!");
        }
        if (this.isInParty(playerUUID)) {
            return PartyResult.failure(PartyResultType.ALREADY_IN_PARTY, "\u00a7cYou are already in a party!");
        }
        party.addMember(playerUUID, player.getName());
        this.playerPartyMap.put(playerUUID, party.getPartyId());
        party.broadcast("\u00a7a" + player.getName() + " \u00a77joined the party! \u00a78(\u00a7e" + party.getSize() + "/" + this.maxPartySize + "\u00a78)");
        party.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.plugin.getLogger().fine("Player " + player.getName() + " joined party " + String.valueOf(party.getPartyId()));
        return PartyResult.success("\u00a7aYou joined the party!");
    }

    public PartyResult denyInvite(@Nonnull Player player) {
        UUID playerUUID = player.getUniqueId();
        PartyInvite invite = this.pendingInvites.remove(playerUUID);
        if (invite == null) {
            return PartyResult.failure(PartyResultType.NO_INVITE, "\u00a7cYou have no pending party invites!");
        }
        Player inviter = Bukkit.getPlayer((UUID)invite.getInviterUUID());
        if (inviter != null && inviter.isOnline()) {
            inviter.sendMessage("\u00a7c" + player.getName() + " denied your party invite.");
        }
        player.sendMessage("\u00a7cYou denied the party invite.");
        return PartyResult.success("\u00a7cInvite denied.");
    }

    public PartyResult leaveParty(@Nonnull Player player) {
        UUID playerUUID = player.getUniqueId();
        Party party = this.getParty(playerUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (party.isLeader(playerUUID)) {
            if (party.getSize() > 1) {
                UUID newLeader = party.getMembers().stream().findFirst().orElse(null);
                if (newLeader != null) {
                    party.setLeader(newLeader);
                    Player newLeaderPlayer = Bukkit.getPlayer((UUID)newLeader);
                    String newLeaderName = newLeaderPlayer != null ? newLeaderPlayer.getName() : party.getMemberName(newLeader);
                    party.removeMember(playerUUID);
                    this.playerPartyMap.remove(playerUUID);
                    party.broadcast("\u00a7e" + player.getName() + " \u00a77left the party. \u00a7a" + newLeaderName + " \u00a77is now the leader.");
                    player.sendMessage("\u00a7aYou left the party.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                }
                return PartyResult.success("\u00a7aYou left the party.");
            }
            party.removeMember(playerUUID);
            this.playerPartyMap.remove(playerUUID);
            this.parties.remove(party.getPartyId());
            this.pendingInvites.entrySet().removeIf(entry -> ((PartyInvite)entry.getValue()).getPartyId().equals(party.getPartyId()));
            player.sendMessage("\u00a7aYou left the party. (Party disbanded)");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return PartyResult.success("\u00a7aYou left the party.");
        }
        party.removeMember(playerUUID);
        this.playerPartyMap.remove(playerUUID);
        party.broadcast("\u00a7e" + player.getName() + " \u00a77left the party.");
        player.sendMessage("\u00a7aYou left the party.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        return PartyResult.success("\u00a7aYou left the party.");
    }

    public PartyResult kick(@Nonnull Player leader, @Nonnull Player target) {
        UUID leaderUUID = leader.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        Party party = this.getParty(leaderUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (!party.isLeader(leaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cOnly the party leader can kick players!");
        }
        if (leaderUUID.equals(targetUUID)) {
            return PartyResult.failure(PartyResultType.CANNOT_KICK_SELF, "\u00a7cYou cannot kick yourself! Use \u00a7e/party disband \u00a7cor \u00a7e/party leave\u00a7c.");
        }
        if (!party.hasMember(targetUUID)) {
            return PartyResult.failure(PartyResultType.NOT_IN_YOUR_PARTY, "\u00a7c" + target.getName() + " is not in your party!");
        }
        party.removeMember(targetUUID);
        this.playerPartyMap.remove(targetUUID);
        party.broadcast("\u00a7c" + target.getName() + " \u00a77was kicked from the party.");
        target.sendMessage("\u00a7cYou have been kicked from the party.");
        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        return PartyResult.success("\u00a7aKicked " + target.getName() + " from the party.");
    }

    public PartyResult transferLeader(@Nonnull Player leader, @Nonnull Player newLeader) {
        UUID leaderUUID = leader.getUniqueId();
        UUID newLeaderUUID = newLeader.getUniqueId();
        Party party = this.getParty(leaderUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (!party.isLeader(leaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cYou are not the party leader!");
        }
        if (leaderUUID.equals(newLeaderUUID)) {
            return PartyResult.failure(PartyResultType.ALREADY_LEADER, "\u00a7cYou are already the party leader!");
        }
        if (!party.hasMember(newLeaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_IN_YOUR_PARTY, "\u00a7c" + newLeader.getName() + " is not in your party!");
        }
        party.setLeader(newLeaderUUID);
        party.broadcast("\u00a7a" + newLeader.getName() + " \u00a77is now the party leader!");
        party.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        return PartyResult.success("\u00a7aTransferred leadership to " + newLeader.getName() + ".");
    }

    public void sendPartyChat(@Nonnull Player sender, @Nonnull String message) {
        if (!this.partyChatEnabled) {
            sender.sendMessage("\u00a7cParty chat is disabled!");
            return;
        }
        Party party = this.getParty(sender.getUniqueId());
        if (party == null) {
            sender.sendMessage("\u00a7cYou are not in a party!");
            return;
        }
        String formatted = "\u00a7d\u00a7lPARTY \u00a78\u00bb \u00a77" + sender.getName() + "\u00a78: \u00a7f" + message;
        party.broadcast(formatted);
    }

    private void applyPartySplitTeams(@Nonnull List<UUID> team1, @Nonnull List<UUID> team2) {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm == null) {
            return;
        }
        Scoreboard board = sm.getMainScoreboard();
        Team oldT1 = board.getTeam("ud_split_t1");
        Team oldT2 = board.getTeam("ud_split_t2");
        if (oldT1 != null) {
            oldT1.unregister();
        }
        if (oldT2 != null) {
            oldT2.unregister();
        }
        Team t1 = board.registerNewTeam("ud_split_t1");
        t1.color(NamedTextColor.BLUE);
        t1.setAllowFriendlyFire(true);
        for (UUID uuid : team1) {
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null) continue;
            t1.addEntry(p.getName());
            p.sendMessage("\u00a79\u00a7lYou are on \u00a7b\u00a7lTeam Blue \u00a79\u00a7l(Team 1)");
        }
        Team t2 = board.registerNewTeam("ud_split_t2");
        t2.color(NamedTextColor.RED);
        t2.setAllowFriendlyFire(true);
        for (UUID uuid : team2) {
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null) continue;
            t2.addEntry(p.getName());
            p.sendMessage("\u00a7c\u00a7lYou are on \u00a74\u00a7lTeam Red \u00a7c\u00a7l(Team 2)");
        }
    }

    public void removePartySplitTeams() {
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm == null) {
            return;
        }
        Scoreboard board = sm.getMainScoreboard();
        Team t1 = board.getTeam("ud_split_t1");
        Team t2 = board.getTeam("ud_split_t2");
        if (t1 != null) {
            t1.unregister();
        }
        if (t2 != null) {
            t2.unregister();
        }
    }

    public boolean isPartyChatMessage(@Nonnull String message) {
        return this.partyChatEnabled && message.startsWith(this.partyChatPrefix);
    }

    @Nonnull
    public String getPartyChatPrefix() {
        return this.partyChatPrefix;
    }

    public boolean isInParty(@Nonnull UUID uuid) {
        return this.playerPartyMap.containsKey(uuid);
    }

    public boolean isInParty(@Nonnull Player player) {
        return this.isInParty(player.getUniqueId());
    }

    public boolean isPartyLeader(@Nonnull UUID uuid) {
        Party party = this.getParty(uuid);
        return party != null && party.isLeader(uuid);
    }

    public boolean isPartyLeader(@Nonnull Player player) {
        return this.isPartyLeader(player.getUniqueId());
    }

    @Nullable
    public Party getParty(@Nonnull UUID playerUUID) {
        UUID partyId = this.playerPartyMap.get(playerUUID);
        if (partyId == null) {
            return null;
        }
        return this.parties.get(partyId);
    }

    @Nullable
    public Party getParty(@Nonnull Player player) {
        return this.getParty(player.getUniqueId());
    }

    @Nullable
    public Party getPartyById(@Nonnull UUID partyId) {
        return this.parties.get(partyId);
    }

    @Nonnull
    public Collection<Party> getAllParties() {
        return Collections.unmodifiableCollection(this.parties.values());
    }

    public int getPartyCount() {
        return this.parties.size();
    }

    public int getTotalPlayersInParties() {
        return this.playerPartyMap.size();
    }

    public boolean hasPendingInvite(@Nonnull UUID uuid) {
        return this.pendingInvites.containsKey(uuid);
    }

    public boolean hasPendingInvite(@Nonnull Player player) {
        return this.hasPendingInvite(player.getUniqueId());
    }

    @Nullable
    public PartyInvite getPendingInvite(@Nonnull UUID uuid) {
        return this.pendingInvites.get(uuid);
    }

    @Nullable
    public PartyInvite getPendingInvite(@Nonnull Player player) {
        return this.getPendingInvite(player.getUniqueId());
    }

    public int getMaxPartySize() {
        return this.maxPartySize;
    }

    public boolean isPartyEnabled() {
        return this.partyEnabled;
    }

    public int getPendingInvitesCount() {
        return this.pendingInvites.size();
    }

    @Nonnull
    public List<Party> getPublicParties() {
        ArrayList<Party> publicParties = new ArrayList<Party>();
        for (Party party : this.parties.values()) {
            Player leader;
            if (!party.isPublic() || party.getSize() >= this.maxPartySize || (leader = Bukkit.getPlayer((UUID)party.getLeaderUUID())) == null || !leader.isOnline()) continue;
            publicParties.add(party);
        }
        return publicParties;
    }

    public PartyResult joinPublicParty(@Nonnull Player player, @Nonnull UUID partyId) {
        UUID playerUUID = player.getUniqueId();
        if (this.isInParty(playerUUID)) {
            return PartyResult.failure(PartyResultType.ALREADY_IN_PARTY, "\u00a7cYou are already in a party! Use \u00a7e/party leave \u00a7cfirst.");
        }
        Party party = this.parties.get(partyId);
        if (party == null) {
            return PartyResult.failure(PartyResultType.PARTY_DISBANDED, "\u00a7cThis party no longer exists!");
        }
        if (!party.isPublic()) {
            return PartyResult.failure(PartyResultType.NOT_PUBLIC, "\u00a7cThis party is not public! You need an invite.");
        }
        if (party.getSize() >= this.maxPartySize) {
            return PartyResult.failure(PartyResultType.PARTY_FULL, "\u00a7cThis party is full!");
        }
        party.addMember(playerUUID, player.getName());
        this.playerPartyMap.put(playerUUID, party.getPartyId());
        party.broadcast("\u00a7a" + player.getName() + " \u00a77joined the party! \u00a78(\u00a7e" + party.getSize() + "/" + this.maxPartySize + "\u00a78)");
        party.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.plugin.getLogger().fine("Player " + player.getName() + " joined public party " + String.valueOf(partyId));
        return PartyResult.success("\u00a7aYou joined " + party.getLeaderName() + "'s party!");
    }

    public void sendPartyInfo(@Nonnull Player player) {
        Party party = this.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("\u00a7cYou are not in a party!");
            return;
        }
        player.sendMessage("");
        player.sendMessage("\u00a7d\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac PARTY INFO \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
        player.sendMessage("\u00a77Leader: \u00a7a" + party.getLeaderName());
        player.sendMessage("\u00a77Members: \u00a7e" + party.getSize() + "/" + this.maxPartySize);
        player.sendMessage("");
        StringBuilder members = new StringBuilder("\u00a77");
        for (UUID memberUUID : party.getAllMembers()) {
            String status;
            Player member = Bukkit.getPlayer((UUID)memberUUID);
            String name = member != null ? member.getName() : "Offline";
            String string = status = member != null && member.isOnline() ? "\u00a7a" : "\u00a7c";
            if (party.isLeader(memberUUID)) {
                members.append("\u00a76\u2605 ");
            } else {
                members.append("\u00a77\u2022 ");
            }
            members.append(status).append(name).append("\u00a77, ");
        }
        if (members.length() > 2) {
            members.setLength(members.length() - 2);
        }
        player.sendMessage("\u00a77" + String.valueOf(members));
        player.sendMessage("");
        player.sendMessage("\u00a7d\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
    }

    public void listMembers(@Nonnull Player player) {
        Party party = this.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("\u00a7cYou are not in a party!");
            return;
        }
        this.sendPartyInfo(player);
    }

    public void runCleanup() {
        this.cleanupExpiredInvites();
        this.cleanupEmptyParties();
        this.lastCleanupTime = System.currentTimeMillis();
    }

    public void cleanupExpiredInvites() {
        long timeout = (long)this.inviteTimeoutSeconds * 1000L;
        int removed = 0;
        Iterator<Map.Entry<UUID, PartyInvite>> iterator = this.pendingInvites.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PartyInvite> entry = iterator.next();
            if (!entry.getValue().isExpired(timeout)) continue;
            Player target = Bukkit.getPlayer((UUID)entry.getKey());
            if (target != null && target.isOnline()) {
                target.sendMessage("\u00a77A party invite has expired.");
            }
            iterator.remove();
            ++removed;
        }
        if (removed > 0) {
            this.plugin.getLogger().fine("[PartyManager] Cleaned up " + removed + " expired invites");
        }
    }

    public void cleanupEmptyParties() {
        int partiesRemoved = 0;
        int membersRemoved = 0;
        Iterator<Map.Entry<UUID, Party>> iterator = this.parties.entrySet().iterator();
        while (iterator.hasNext()) {
            Party party = iterator.next().getValue();
            boolean anyInActiveMatch = party.getAllMembers().stream().anyMatch(memberId -> {
                DuelManager dm = this.plugin.getDuelManager();
                if (dm == null) {
                    return false;
                }
                UUID matchId = dm.getSpectatingMatchId((UUID)memberId);
                return dm.isInMatch((UUID)memberId) || matchId != null;
            });
            if (anyInActiveMatch) {
                this.plugin.debug("[PartyManager] Skipping cleanup for party " + String.valueOf(party.getPartyId()) + " - members in active match");
                continue;
            }
            ArrayList<UUID> toRemove = new ArrayList<UUID>();
            for (UUID memberUUID : party.getAllMembers()) {
                Player member = Bukkit.getPlayer((UUID)memberUUID);
                if (member != null && member.isOnline()) continue;
                toRemove.add(memberUUID);
            }
            for (UUID uuid : toRemove) {
                party.removeMember(uuid);
                this.playerPartyMap.remove(uuid);
                ++membersRemoved;
            }
            if (party.isEmpty()) {
                iterator.remove();
                ++partiesRemoved;
                continue;
            }
            if (Bukkit.getOfflinePlayer((UUID)party.getLeaderUUID()).isOnline() || party.getSize() <= 0) continue;
            UUID newLeader = party.getMembers().stream().findFirst().orElse(null);
            if (newLeader == null) {
                newLeader = party.getAllMembers().stream().findFirst().orElse(null);
            }
            if (newLeader == null) continue;
            party.setLeader(newLeader);
            party.broadcast("\u00a77The party leader went offline. \u00a7a" + party.getLeaderName() + " \u00a77is now the leader.");
        }
        if (partiesRemoved > 0 || membersRemoved > 0) {
            this.plugin.getLogger().fine("[PartyManager] Cleanup: removed " + partiesRemoved + " empty parties, " + membersRemoved + " offline members");
        }
    }

    public void handleDisconnect(@Nonnull UUID uuid) {
        Party party = this.getParty(uuid);
        if (party != null) {
            String name;
            Player player = Bukkit.getPlayer((UUID)uuid);
            String string = name = player != null ? player.getName() : party.getMemberName(uuid);
            if (party.isLeader(uuid)) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    Party currentParty;
                    Player reconnected = Bukkit.getPlayer((UUID)uuid);
                    if (!(reconnected != null && reconnected.isOnline() || (currentParty = this.parties.get(party.getPartyId())) == null)) {
                        boolean anyInMatch = currentParty.getAllMembers().stream().anyMatch(memberId -> {
                            DuelManager dm = this.plugin.getDuelManager();
                            return dm != null && dm.isInMatch((UUID)memberId);
                        });
                        if (anyInMatch) {
                            UUID newLeader = currentParty.getAllMembers().stream().filter(id -> !id.equals(uuid)).findFirst().orElse(null);
                            if (newLeader != null) {
                                currentParty.setLeader(newLeader);
                                currentParty.broadcast("\u00a77" + name + " \u00a77went offline. \u00a7a" + currentParty.getLeaderName() + " \u00a77is now the leader. Match continues!");
                            }
                            return;
                        }
                        if (currentParty.getSize() > 1) {
                            currentParty.removeMember(uuid);
                            this.playerPartyMap.remove(uuid);
                            UUID newLeader = currentParty.getMembers().stream().findFirst().orElse(null);
                            if (newLeader == null) {
                                newLeader = currentParty.getAllMembers().stream().findFirst().orElse(null);
                            }
                            if (newLeader != null) {
                                currentParty.setLeader(newLeader);
                                currentParty.broadcast("\u00a77" + name + " went offline. \u00a7a" + currentParty.getLeaderName() + " \u00a77is now the leader.");
                            }
                        } else {
                            this.parties.remove(currentParty.getPartyId());
                            this.playerPartyMap.remove(uuid);
                        }
                    }
                }, 600L);
            } else {
                party.broadcast("\u00a77" + name + " went offline.");
            }
        }
        this.pendingInvites.remove(uuid);
    }

    public void handleDisconnect(@Nonnull Player player) {
        this.handleDisconnect(player.getUniqueId());
    }

    public long getTotalPartiesCreated() {
        return this.totalPartiesCreated;
    }

    public long getTotalInvitesSent() {
        return this.totalInvitesSent;
    }

    public long getLastCleanupTime() {
        return this.lastCleanupTime;
    }

    public long getTimeSinceLastCleanup() {
        return System.currentTimeMillis() - this.lastCleanupTime;
    }

    @Nonnull
    public Map<String, Object> getStatistics() {
        HashMap<String, Object> stats = new HashMap<String, Object>();
        stats.put("activeParties", this.parties.size());
        stats.put("playersInParties", this.playerPartyMap.size());
        stats.put("pendingInvites", this.pendingInvites.size());
        stats.put("totalPartiesCreated", this.totalPartiesCreated);
        stats.put("totalInvitesSent", this.totalInvitesSent);
        stats.put("lastCleanupTime", this.lastCleanupTime);
        stats.put("maxPartySize", this.maxPartySize);
        stats.put("inviteTimeoutSeconds", this.inviteTimeoutSeconds);
        stats.put("partyEnabled", this.partyEnabled);
        return stats;
    }

    public void reload() {
        this.loadSettings();
        this.plugin.getLogger().info("\u00a7a[PartyManager] Reloaded successfully!");
    }

    public void shutdown() {
        if (this.cleanupTask != null) {
            try {
                this.cleanupTask.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
        for (Party party : new ArrayList<Party>(this.parties.values())) {
            party.broadcast("\u00a7cParties have been disabled due to server shutdown.");
        }
        this.parties.clear();
        this.playerPartyMap.clear();
        this.pendingInvites.clear();
        this.plugin.getLogger().info("\u00a7a[PartyManager] Shutdown complete");
    }

    public void save() {
        this.plugin.getLogger().fine("[PartyManager] Save called (no-op for in-memory parties)");
    }

    public PartyResult queuePartyVsParty(@Nonnull Player leader, @Nonnull String kitName) {
        UUID leaderUUID = leader.getUniqueId();
        Party party = this.getParty(leaderUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (!party.isLeader(leaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cOnly the party leader can queue!");
        }
        int partySize = party.getSize();
        if (partySize < 2) {
            return PartyResult.failure(PartyResultType.PARTY_TOO_SMALL, "\u00a7cYou need at least 2 players!");
        }
        if (!this.plugin.getKitManager().adminKitExists(kitName)) {
            return PartyResult.failure(PartyResultType.KIT_NOT_FOUND, "\u00a7cKit not found: " + kitName);
        }
        for (UUID memberUUID : party.getAllMembers()) {
            Player member = Bukkit.getPlayer((UUID)memberUUID);
            if (member == null || !member.isOnline()) {
                return PartyResult.failure(PartyResultType.MEMBER_OFFLINE, "\u00a7cAll party members must be online!");
            }
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(memberUUID)) {
                return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is in a match!");
            }
            if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(memberUUID)) {
                return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is already in queue!");
            }
            if (this.plugin.getFFAManager() == null || !this.plugin.getFFAManager().isInFFA(memberUUID)) continue;
            return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is in FFA!");
        }
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            return PartyResult.failure(PartyResultType.SYSTEM_DISABLED, "\u00a7cQueue system is unavailable!");
        }
        PartyQueueType queueType = PartyQueueType.PARTY_VS_PARTY;
        QueueResult result = queueManager.joinPartyQueue(party, kitName, queueType);
        if (!result.success()) {
            return PartyResult.failure(PartyResultType.SYSTEM_DISABLED, result.message());
        }
        party.broadcast("\u00a7aQueued for \u00a7eParty vs Party \u00a7ain \u00a7e" + kitName + "\u00a7a!");
        party.broadcast("\u00a77Waiting for an opponent party of similar size...");
        party.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        return PartyResult.success("\u00a7aParty queued for Party vs Party!");
    }

    public PartyResult queuePartySplit(@Nonnull Player leader, @Nonnull String kitName, int teamSize) {
        Player p;
        ArrayList<UUID> team2;
        ArrayList<UUID> team1;
        UUID leaderUUID = leader.getUniqueId();
        Party party = this.getParty(leaderUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (!party.isLeader(leaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cOnly the party leader can start split!");
        }
        int partySize = party.getSize();
        if (partySize < 2) {
            return PartyResult.failure(PartyResultType.PARTY_TOO_SMALL, "\u00a7cYou need at least \u00a7e2 \u00a7cplayers to do a party split!");
        }
        if (teamSize >= partySize || teamSize < 1) {
            return PartyResult.failure(PartyResultType.PARTY_TOO_SMALL, "\u00a7cInvalid team size! With \u00a7e" + partySize + " \u00a7cplayers, team size must be between 1 and " + (partySize - 1) + ".");
        }
        if (!this.plugin.getKitManager().adminKitExists(kitName)) {
            return PartyResult.failure(PartyResultType.KIT_NOT_FOUND, "\u00a7cKit not found: " + kitName);
        }
        ArrayList<UUID> onlineMembers = new ArrayList<UUID>();
        for (UUID memberUUID : party.getAllMembers()) {
            Player member = Bukkit.getPlayer((UUID)memberUUID);
            if (member == null || !member.isOnline()) {
                return PartyResult.failure(PartyResultType.MEMBER_OFFLINE, "\u00a7cAll members must be online!");
            }
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(memberUUID)) {
                return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is busy!");
            }
            onlineMembers.add(memberUUID);
        }
        Collections.shuffle(onlineMembers);
        if (new Random().nextBoolean()) {
            team1 = new ArrayList<UUID>(onlineMembers.subList(0, teamSize));
            team2 = new ArrayList<UUID>(onlineMembers.subList(teamSize, onlineMembers.size()));
        } else {
            int otherTeamSize = onlineMembers.size() - teamSize;
            team1 = new ArrayList(onlineMembers.subList(0, otherTeamSize));
            team2 = new ArrayList(onlineMembers.subList(otherTeamSize, onlineMembers.size()));
        }
        DuelArena arena = this.plugin.getArenaManager().allocateArena(null, kitName);
        if (arena == null) {
            return PartyResult.failure(PartyResultType.NO_ARENA, "\u00a7cNo arenas available!");
        }
        party.broadcast("\u00a7a\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac PARTY SPLIT \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        party.broadcast("");
        party.broadcast("\u00a7e\u00a7lTeam 1 \u00a77(" + team1.size() + "):");
        for (UUID uuid : team1) {
            p = Bukkit.getPlayer((UUID)uuid);
            party.broadcast("  \u00a7a\u2022 \u00a7f" + (p != null ? p.getName() : "Unknown"));
        }
        party.broadcast("");
        party.broadcast("\u00a7c\u00a7lTeam 2 \u00a77(" + team2.size() + "):");
        for (UUID uuid : team2) {
            p = Bukkit.getPlayer((UUID)uuid);
            party.broadcast("  \u00a7c\u2022 \u00a7f" + (p != null ? p.getName() : "Unknown"));
        }
        if (team1.size() != team2.size()) {
            party.broadcast("");
            party.broadcast("\u00a7e\u00a7l\u26a0 Uneven Match: \u00a76" + team1.size() + "v" + team2.size());
        }
        party.broadcast("");
        party.broadcast("\u00a77Kit: \u00a7e" + kitName);
        party.broadcast("\u00a77Starting in 3 seconds...");
        party.broadcast("\u00a7a\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        party.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        this.applyPartySplitTeams(team1, team2);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            DuelManager dm = this.plugin.getDuelManager();
            if (dm != null) {
                dm.startTeamMatch((List<UUID>)team1, (List<UUID>)team2, kitName, arena, 1, false, "Party Split");
            }
        }, 60L);
        return PartyResult.success("\u00a7aSplit started: \u00a7e" + team1.size() + "v" + team2.size());
    }

    public PartyResult joinPartyFFA(@Nonnull Player leader, @Nonnull String kitName) {
        UUID leaderUUID = leader.getUniqueId();
        Party party = this.getParty(leaderUUID);
        if (party == null) {
            return PartyResult.failure(PartyResultType.NOT_IN_PARTY, "\u00a7cYou are not in a party!");
        }
        if (!party.isLeader(leaderUUID)) {
            return PartyResult.failure(PartyResultType.NOT_LEADER, "\u00a7cOnly the party leader can start party FFA!");
        }
        int partySize = party.getSize();
        if (partySize < 2) {
            return PartyResult.failure(PartyResultType.PARTY_TOO_SMALL, "\u00a7cYou need at least 2 players for party FFA!");
        }
        if (!this.plugin.getKitManager().adminKitExists(kitName)) {
            return PartyResult.failure(PartyResultType.KIT_NOT_FOUND, "\u00a7cKit not found: " + kitName);
        }
        ArrayList<UUID> onlineMembers = new ArrayList<UUID>();
        for (UUID memberUUID : party.getAllMembers()) {
            Player member = Bukkit.getPlayer((UUID)memberUUID);
            if (member == null || !member.isOnline()) {
                return PartyResult.failure(PartyResultType.MEMBER_OFFLINE, "\u00a7cAll party members must be online!");
            }
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(memberUUID)) {
                return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is in a match!");
            }
            if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(memberUUID)) {
                return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is in queue!");
            }
            if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(memberUUID)) {
                return PartyResult.failure(PartyResultType.MEMBER_BUSY, "\u00a7c" + member.getName() + " is in FFA!");
            }
            onlineMembers.add(memberUUID);
        }
        DuelArena arena = this.plugin.getArenaManager().allocateArena(null, kitName);
        if (arena == null) {
            return PartyResult.failure(PartyResultType.NO_ARENA, "\u00a7cNo arenas available!");
        }
        party.broadcast("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac PARTY FFA \u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        party.broadcast("");
        party.broadcast("\u00a7e\u00a7lBattle Royale Mode");
        party.broadcast("\u00a77Everyone fights everyone!");
        party.broadcast("\u00a77Last player standing wins!");
        party.broadcast("");
        party.broadcast("\u00a77Players: \u00a7e" + onlineMembers.size());
        party.broadcast("\u00a77Kit: \u00a7e" + kitName);
        party.broadcast("\u00a77Starting in 3 seconds...");
        party.broadcast("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        party.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            DuelManager duelManager = this.plugin.getDuelManager();
            if (duelManager != null) {
                duelManager.startPartyFFAMatch(onlineMembers, kitName, arena);
            }
        }, 60L);
        return PartyResult.success("\u00a7aParty FFA started!");
    }
}

