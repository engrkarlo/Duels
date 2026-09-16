/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.Sound
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.queue;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.cooldown.CooldownManager;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.player.PlayerData;
import com.ultimateduels.player.PlayerDataManager;
import com.ultimateduels.queue.model.PartyQueueEntry;
import com.ultimateduels.queue.model.PartyQueueType;
import com.ultimateduels.queue.model.QueueEntry;
import com.ultimateduels.queue.model.QueueEventListener;
import com.ultimateduels.queue.model.QueueInfo;
import com.ultimateduels.queue.model.QueueResult;
import com.ultimateduels.queue.model.QueueResultType;
import com.ultimateduels.queue.model.QueueStatistics;
import com.ultimateduels.queue.model.QueueType;
import com.ultimateduels.tasks.QueueMatchTask;
import com.ultimateduels.world.WorldRestrictionManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class QueueManager {
    private final UltimateDuels plugin;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private final PartyManager partyManager;
    private final PlayerDataManager playerDataManager;
    private final CooldownManager cooldownManager;
    private final Map<String, Queue<QueueEntry>> kitQueues;
    private final Map<UUID, QueueEntry> playerQueueMap;
    private final Map<UUID, String> playerKitMap;
    private final Map<UUID, PartyQueueEntry> partyQueues;
    private final Map<String, QueueStatistics> queueStats;
    private QueueMatchTask matchTask;
    private boolean queueEnabled;
    private int maxQueueTime;
    private int matchCheckInterval;
    private boolean eloMatchingEnabled;
    private int eloRangeStart;
    private int eloRangeExpansion;
    private int eloRangeMax;
    private boolean pingMatchingEnabled;
    private int maxPingDifference;
    private final List<QueueEventListener> eventListeners;

    public QueueManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.partyManager = plugin.getPartyManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.kitQueues = new ConcurrentHashMap<String, Queue<QueueEntry>>();
        this.playerQueueMap = new ConcurrentHashMap<UUID, QueueEntry>();
        this.playerKitMap = new ConcurrentHashMap<UUID, String>();
        this.partyQueues = new ConcurrentHashMap<UUID, PartyQueueEntry>();
        this.queueStats = new ConcurrentHashMap<String, QueueStatistics>();
        this.eventListeners = new ArrayList<QueueEventListener>();
        this.loadSettings();
        this.initializeQueues();
        this.startMatchTask();
        plugin.getLogger().info("\u00a7a[QueueManager] Initialized with " + this.kitQueues.size() + " kit queues");
    }

    private void loadSettings() {
        FileConfiguration config = this.plugin.getConfig();
        this.queueEnabled = config.getBoolean("queue.enabled", true);
        this.maxQueueTime = config.getInt("queue.max-queue-time", 600);
        this.matchCheckInterval = config.getInt("queue.match-check-interval", 20);
        this.eloMatchingEnabled = config.getBoolean("queue.elo-matching.enabled", true);
        this.eloRangeStart = config.getInt("queue.elo-matching.range-start", 100);
        this.eloRangeExpansion = config.getInt("queue.elo-matching.range-expansion", 50);
        this.eloRangeMax = config.getInt("queue.elo-matching.range-max", 500);
        this.pingMatchingEnabled = config.getBoolean("queue.ping-matching.enabled", false);
        this.maxPingDifference = config.getInt("queue.ping-matching.max-difference", 100);
    }

    private void initializeQueues() {
        for (String kitName : this.kitManager.getAdminKitNames()) {
            String key = kitName.toLowerCase();
            this.kitQueues.put(key, new ConcurrentLinkedQueue());
            this.queueStats.put(key, new QueueStatistics(kitName));
        }
    }

    private void startMatchTask() {
        if (this.matchTask != null) {
            this.stopMatchTask();
        }
        this.matchTask = new QueueMatchTask(this.plugin, this);
        this.matchTask.runTaskTimer((Plugin)this.plugin, this.matchCheckInterval, this.matchCheckInterval);
        this.plugin.getLogger().info("\u00a7a[QueueManager] Match task started (interval: " + this.matchCheckInterval + " ticks)");
    }

    private void stopMatchTask() {
        if (this.matchTask != null) {
            try {
                this.matchTask.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
            this.matchTask = null;
        }
    }

    @Nullable
    private DuelManager getDuelManager() {
        return this.plugin.getDuelManager();
    }

    public QueueResult joinQueue(@Nonnull Player player, @Nonnull String kitName) {
        Party party;
        UUID uuid = player.getUniqueId();
        String kit = kitName.toLowerCase();
        WorldRestrictionManager worldRestriction = this.plugin.getWorldRestrictionManager();
        if (worldRestriction != null && !worldRestriction.canJoinQueue(player)) {
            worldRestriction.sendQueueBlockedMessage(player);
            return new QueueResult(false, QueueResultType.WORLD_RESTRICTED, this.plugin.getLanguageManager().getRaw(player, "world-restriction.queue-blocked"));
        }
        if (!this.queueEnabled) {
            return new QueueResult(false, QueueResultType.QUEUE_DISABLED, "\u00a7cThe queue system is currently disabled.");
        }
        if (this.isInQueue(uuid)) {
            return new QueueResult(false, QueueResultType.ALREADY_IN_QUEUE, "\u00a7cYou are already in a queue! Use \u00a7e/leavequeue \u00a7cto leave first.");
        }
        DuelManager duelManager = this.getDuelManager();
        if (duelManager != null && duelManager.isInMatch(uuid)) {
            return new QueueResult(false, QueueResultType.IN_MATCH, "\u00a7cYou cannot join the queue while in a match!");
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(uuid)) {
            return new QueueResult(false, QueueResultType.IN_FFA, "\u00a7cYou cannot join the queue while in FFA!");
        }
        if (!this.kitManager.adminKitExists(kitName)) {
            return new QueueResult(false, QueueResultType.INVALID_KIT, "\u00a7cThe kit \u00a7e" + kitName + " \u00a7cdoes not exist!");
        }
        if (this.partyManager != null && this.partyManager.isInParty(uuid) && (party = this.partyManager.getParty(uuid)) != null && !party.isLeader(uuid)) {
            return new QueueResult(false, QueueResultType.IN_PARTY, "\u00a7cOnly the party leader can queue for matches!");
        }
        if (this.cooldownManager != null && this.cooldownManager.isOnCooldown(player, "queue_join")) {
            String remaining = this.cooldownManager.getFormattedRemainingCooldown(player, "queue_join");
            return new QueueResult(false, QueueResultType.ON_COOLDOWN, "\u00a7cYou must wait \u00a7e" + remaining + " \u00a7cbefore queuing again!");
        }
        if (this.arenaManager != null && this.arenaManager.getAvailableArenaCount() == 0) {
            return new QueueResult(false, QueueResultType.NO_ARENAS, "\u00a7cNo arenas are currently available. Please try again later.");
        }
        int elo = this.getPlayerElo(uuid);
        QueueEntry entry = new QueueEntry(uuid, player.getName(), kit, System.currentTimeMillis(), elo, player.getPing(), QueueType.SOLO);
        Queue<QueueEntry> queue = this.kitQueues.get(kit);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<QueueEntry>();
            this.kitQueues.put(kit, queue);
        }
        queue.add(entry);
        this.playerQueueMap.put(uuid, entry);
        this.playerKitMap.put(uuid, kit);
        if (this.cooldownManager != null) {
            this.cooldownManager.setCooldown(player, "queue_join");
        }
        QueueStatistics stats = this.queueStats.computeIfAbsent(kit, QueueStatistics::new);
        stats.incrementTotalQueues();
        this.fireQueueJoinEvent(entry);
        this.sendQueueJoinMessage(player, kitName, queue.size());
        this.plugin.getLogger().fine("Player " + player.getName() + " joined queue for " + kitName);
        return new QueueResult(true, QueueResultType.SUCCESS, "\u00a7aYou have joined the \u00a7e" + kitName + " \u00a7aqueue!");
    }

    private int getPlayerElo(@Nonnull UUID uuid) {
        if (this.playerDataManager == null) {
            return 1000;
        }
        PlayerData data = this.playerDataManager.getPlayerData(uuid);
        if (data != null) {
            return data.getElo();
        }
        return 1000;
    }

    public QueueResult leaveQueue(@Nonnull Player player) {
        return this.leaveQueue(player.getUniqueId(), true);
    }

    public QueueResult leaveQueue(@Nonnull UUID uuid, boolean notify) {
        if (!this.isInQueue(uuid)) {
            return new QueueResult(false, QueueResultType.NOT_IN_QUEUE, "\u00a7cYou are not in any queue!");
        }
        QueueEntry entry = this.playerQueueMap.remove(uuid);
        String kit = this.playerKitMap.remove(uuid);
        if (entry != null && kit != null) {
            Player player;
            Queue<QueueEntry> queue = this.kitQueues.get(kit);
            if (queue != null) {
                queue.remove(entry);
            }
            if ((player = Bukkit.getPlayer((UUID)uuid)) != null && this.cooldownManager != null) {
                this.cooldownManager.setCooldown(player, "queue_leave");
            }
            this.fireQueueLeaveEvent(entry);
            if (notify && player != null) {
                player.sendMessage("\u00a7aYou have left the \u00a7e" + kit + " \u00a7aqueue.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            this.plugin.getLogger().fine("Player " + entry.getPlayerName() + " left queue for " + kit);
        }
        return new QueueResult(true, QueueResultType.SUCCESS, "\u00a7aYou have left the queue.");
    }

    public void removeFromAllQueues(@Nonnull UUID uuid) {
        this.leaveQueue(uuid, false);
        this.partyQueues.entrySet().removeIf(entry -> {
            if (((PartyQueueEntry)entry.getValue()).getMembers().contains(uuid)) {
                for (UUID memberId : ((PartyQueueEntry)entry.getValue()).getMembers()) {
                    this.playerKitMap.remove(memberId);
                    this.playerQueueMap.remove(memberId);
                }
                return true;
            }
            return false;
        });
    }

    public void removeFromQueue(@Nonnull UUID uuid) {
        this.leaveQueue(uuid, false);
    }

    public void removePartyFromQueue(@Nonnull UUID partyId) {
        PartyQueueEntry entry = this.partyQueues.remove(partyId);
        if (entry != null) {
            for (UUID memberId : entry.getMembers()) {
                this.playerKitMap.remove(memberId);
                this.playerQueueMap.remove(memberId);
            }
        }
    }

    public QueueResult joinPartyQueue(@Nonnull Party party, @Nonnull String kitName, @Nonnull PartyQueueType queueType) {
        WorldRestrictionManager worldRestriction;
        UUID leaderId = party.getLeaderUUID();
        String kit = kitName.toLowerCase();
        Player leader = Bukkit.getPlayer((UUID)leaderId);
        if (leader != null && (worldRestriction = this.plugin.getWorldRestrictionManager()) != null && !worldRestriction.canJoinQueue(leader)) {
            worldRestriction.sendQueueBlockedMessage(leader);
            return new QueueResult(false, QueueResultType.WORLD_RESTRICTED, this.plugin.getLanguageManager().getRaw(leader, "world-restriction.queue-blocked"));
        }
        if (!this.queueEnabled) {
            return new QueueResult(false, QueueResultType.QUEUE_DISABLED, "\u00a7cThe queue system is currently disabled.");
        }
        if (!this.kitManager.adminKitExists(kitName)) {
            return new QueueResult(false, QueueResultType.INVALID_KIT, "\u00a7cThe kit \u00a7e" + kitName + " \u00a7cdoes not exist!");
        }
        DuelManager duelManager = this.getDuelManager();
        for (UUID memberId : party.getAllMembers()) {
            if (this.isInQueue(memberId)) {
                Player member = Bukkit.getPlayer((UUID)memberId);
                String name = member != null ? member.getName() : "A party member";
                return new QueueResult(false, QueueResultType.MEMBER_IN_QUEUE, "\u00a7c" + name + " is already in a queue!");
            }
            if (duelManager == null || !duelManager.isInMatch(memberId)) continue;
            Player member = Bukkit.getPlayer((UUID)memberId);
            String name = member != null ? member.getName() : "A party member";
            return new QueueResult(false, QueueResultType.MEMBER_IN_MATCH, "\u00a7c" + name + " is currently in a match!");
        }
        int size = party.getSize();
        switch (queueType) {
            case PARTY_VS_PARTY: {
                if (size >= 2) break;
                return new QueueResult(false, QueueResultType.PARTY_TOO_SMALL, "\u00a7cYou need at least 2 party members for Party vs Party!");
            }
            case SPLIT_2V2: {
                if (size < 4) {
                    return new QueueResult(false, QueueResultType.PARTY_TOO_SMALL, "\u00a7cYou need exactly 4 party members for 2v2 Split!");
                }
                if (size <= 4) break;
                return new QueueResult(false, QueueResultType.PARTY_TOO_LARGE, "\u00a7cYou need exactly 4 party members for 2v2 Split!");
            }
            case SPLIT_3V3: {
                if (size < 6) {
                    return new QueueResult(false, QueueResultType.PARTY_TOO_SMALL, "\u00a7cYou need exactly 6 party members for 3v3 Split!");
                }
                if (size <= 6) break;
                return new QueueResult(false, QueueResultType.PARTY_TOO_LARGE, "\u00a7cYou need exactly 6 party members for 3v3 Split!");
            }
        }
        int totalElo = 0;
        int avgPing = 0;
        for (UUID memberId : party.getAllMembers()) {
            totalElo += this.getPlayerElo(memberId);
            Player member = Bukkit.getPlayer((UUID)memberId);
            if (member == null) continue;
            avgPing += member.getPing();
        }
        int avgElo = totalElo / size;
        PartyQueueEntry entry = new PartyQueueEntry(party.getPartyId(), party.getLeaderUUID(), new ArrayList<UUID>(party.getAllMembers()), kit, queueType, System.currentTimeMillis(), avgElo, avgPing /= size);
        this.partyQueues.put(party.getPartyId(), entry);
        for (UUID memberId : party.getAllMembers()) {
            this.playerKitMap.put(memberId, kit);
            Player memberPlayer = Bukkit.getPlayer((UUID)memberId);
            if (memberPlayer == null) continue;
            QueueEntry memberEntry = new QueueEntry(memberId, memberPlayer.getName(), kit, System.currentTimeMillis(), this.getPlayerElo(memberId), memberPlayer.getPing(), QueueType.PARTY);
            this.playerQueueMap.put(memberId, memberEntry);
        }
        String queueTypeName = queueType.getDisplayName();
        party.broadcast("\u00a7aYour party has joined the \u00a7e" + kitName + " \u00a7a" + queueTypeName + " queue!");
        party.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        if (queueType == PartyQueueType.SPLIT_2V2 || queueType == PartyQueueType.SPLIT_3V3) {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.startPartySplitMatch(entry), 40L);
        }
        this.plugin.getLogger().info("Party " + String.valueOf(party.getPartyId()) + " joined " + queueTypeName + " queue for " + kitName);
        return new QueueResult(true, QueueResultType.SUCCESS, "\u00a7aYour party has joined the queue!");
    }

    public QueueResult leavePartyQueue(@Nonnull Party party) {
        UUID partyId = party.getPartyId();
        PartyQueueEntry entry = this.partyQueues.remove(partyId);
        if (entry == null) {
            return new QueueResult(false, QueueResultType.NOT_IN_QUEUE, "\u00a7cYour party is not in any queue!");
        }
        for (UUID memberId : entry.getMembers()) {
            this.playerKitMap.remove(memberId);
        }
        party.broadcast("\u00a7aYour party has left the queue.");
        party.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        return new QueueResult(true, QueueResultType.SUCCESS, "\u00a7aYour party has left the queue.");
    }

    private void startPartySplitMatch(@Nonnull PartyQueueEntry entry) {
        this.partyQueues.remove(entry.getPartyId());
        List<UUID> members = entry.getMembers();
        int teamSize = members.size() / 2;
        ArrayList<UUID> shuffled = new ArrayList<UUID>(members);
        Collections.shuffle(shuffled);
        List<UUID> team1 = shuffled.subList(0, teamSize);
        List<UUID> team2 = shuffled.subList(teamSize, members.size());
        DuelArena arena = this.arenaManager.allocateArena(null, entry.getKitName());
        if (arena == null) {
            for (UUID memberId : members) {
                Player player = Bukkit.getPlayer((UUID)memberId);
                if (player != null) {
                    player.sendMessage("\u00a7cNo arenas available! Please try again later.");
                }
                this.playerKitMap.remove(memberId);
            }
            return;
        }
        DuelManager duelManager = this.getDuelManager();
        if (duelManager != null) {
            duelManager.startTeamMatch(team1, team2, entry.getKitName(), arena, 1, false, "party_split");
        }
        for (UUID memberId : members) {
            this.playerKitMap.remove(memberId);
        }
    }

    public boolean isInQueue(@Nonnull UUID uuid) {
        if (this.playerQueueMap.containsKey(uuid)) {
            return true;
        }
        for (PartyQueueEntry entry : this.partyQueues.values()) {
            if (!entry.getMembers().contains(uuid)) continue;
            return true;
        }
        return false;
    }

    public boolean isInQueue(@Nonnull Player player) {
        return this.isInQueue(player.getUniqueId());
    }

    public boolean isInQueue(@Nonnull UUID uuid, @Nonnull String kitName) {
        String kit = this.playerKitMap.get(uuid);
        return kit != null && kit.equalsIgnoreCase(kitName);
    }

    public boolean isInQueue(@Nonnull Player player, @Nonnull String kitName) {
        return this.isInQueue(player.getUniqueId(), kitName);
    }

    @Nullable
    public String getQueuedKit(@Nonnull UUID uuid) {
        return this.playerKitMap.get(uuid);
    }

    @Nullable
    public String getQueuedKit(@Nonnull Player player) {
        return this.getQueuedKit(player.getUniqueId());
    }

    @Nullable
    public QueueEntry getQueueEntry(@Nonnull UUID uuid) {
        return this.playerQueueMap.get(uuid);
    }

    @Nullable
    public QueueEntry getQueueEntry(@Nonnull Player player) {
        return this.getQueueEntry(player.getUniqueId());
    }

    public long getQueueTime(@Nonnull UUID uuid) {
        QueueEntry entry = this.playerQueueMap.get(uuid);
        if (entry != null) {
            return System.currentTimeMillis() - entry.getJoinTime();
        }
        for (PartyQueueEntry partyEntry : this.partyQueues.values()) {
            if (!partyEntry.getMembers().contains(uuid)) continue;
            return System.currentTimeMillis() - partyEntry.getJoinTime();
        }
        return 0L;
    }

    public long getQueueTime(@Nonnull Player player) {
        return this.getQueueTime(player.getUniqueId());
    }

    public int getQueueSize(@Nonnull String kitName) {
        Queue<QueueEntry> queue = this.kitQueues.get(kitName.toLowerCase());
        return queue != null ? queue.size() : 0;
    }

    public int getTotalQueueSize() {
        int total = 0;
        for (Queue<QueueEntry> queue : this.kitQueues.values()) {
            total += queue.size();
        }
        for (PartyQueueEntry entry : this.partyQueues.values()) {
            total += entry.getMembers().size();
        }
        return total;
    }

    @Nonnull
    public Queue<QueueEntry> getKitQueue(@Nonnull String kitName) {
        return this.kitQueues.getOrDefault(kitName.toLowerCase(), new ConcurrentLinkedQueue());
    }

    @Nonnull
    public Set<String> getKitQueueNames() {
        return Collections.unmodifiableSet(this.kitQueues.keySet());
    }

    @Nonnull
    public List<PartyQueueEntry> getPartyQueues(@Nonnull String kitName, @Nonnull PartyQueueType type) {
        return this.partyQueues.values().stream().filter(e -> e.getKitName().equalsIgnoreCase(kitName) && e.getQueueType() == type).collect(Collectors.toList());
    }

    @Nonnull
    public Collection<PartyQueueEntry> getAllPartyQueues() {
        return Collections.unmodifiableCollection(this.partyQueues.values());
    }

    public int getFightingCount(@Nonnull String kitName) {
        DuelManager duelManager = this.getDuelManager();
        if (duelManager != null) {
            return duelManager.getPlayersInMatchForKit(kitName.toLowerCase());
        }
        return 0;
    }

    public void updateQueueStatistics() {
        for (Map.Entry<String, Queue<QueueEntry>> entry : this.kitQueues.entrySet()) {
            QueueStatistics stats = this.queueStats.get(entry.getKey());
            if (stats == null) continue;
            stats.updateCurrentQueueSize(entry.getValue().size());
        }
    }

    @Nullable
    public QueueStatistics getQueueStats(@Nonnull String kitName) {
        return this.queueStats.get(kitName.toLowerCase());
    }

    @Nonnull
    public Map<String, QueueStatistics> getAllQueueStats() {
        return Collections.unmodifiableMap(this.queueStats);
    }

    @Nonnull
    public QueueInfo getQueueInfo(@Nonnull String kitName) {
        String kit = kitName.toLowerCase();
        int inQueue = this.getQueueSize(kit);
        int fighting = this.getFightingCount(kit);
        return new QueueInfo(kitName, inQueue, fighting);
    }

    @Nonnull
    public List<QueueInfo> getAllQueueInfo() {
        ArrayList<QueueInfo> infos = new ArrayList<QueueInfo>();
        for (String kitName : this.kitManager.getAdminKitNames()) {
            infos.add(this.getQueueInfo(kitName));
        }
        return infos;
    }

    @Nullable
    public QueueEntry findMatch(@Nonnull QueueEntry entry) {
        Queue<QueueEntry> queue = this.kitQueues.get(entry.getKitName());
        if (queue == null || queue.size() < 2) {
            return null;
        }
        long waitTime = System.currentTimeMillis() - entry.getJoinTime();
        int eloRange = this.calculateEloRange(waitTime);
        QueueEntry bestMatch = null;
        int bestScore = Integer.MAX_VALUE;
        DuelManager duelManager = this.getDuelManager();
        for (QueueEntry potential : queue) {
            int score;
            Player potentialPlayer;
            if (potential.getPlayerUUID().equals(entry.getPlayerUUID()) || (potentialPlayer = Bukkit.getPlayer((UUID)potential.getPlayerUUID())) == null || !potentialPlayer.isOnline() || duelManager != null && duelManager.isInMatch(potential.getPlayerUUID()) || (score = this.calculateMatchScore(entry, potential, eloRange)) < 0 || score >= bestScore) continue;
            bestScore = score;
            bestMatch = potential;
        }
        return bestMatch;
    }

    private int calculateEloRange(long waitTimeMs) {
        if (!this.eloMatchingEnabled) {
            return Integer.MAX_VALUE;
        }
        long waitSeconds = waitTimeMs / 1000L;
        int expansions = (int)(waitSeconds / 30L);
        int range = this.eloRangeStart + expansions * this.eloRangeExpansion;
        return Math.min(range, this.eloRangeMax);
    }

    private int calculateMatchScore(@Nonnull QueueEntry entry1, @Nonnull QueueEntry entry2, int eloRange) {
        int score = 0;
        if (this.eloMatchingEnabled) {
            long otherWaitTime;
            int otherRange;
            int eloDiff = Math.abs(entry1.getElo() - entry2.getElo());
            if (eloDiff > eloRange && eloDiff > (otherRange = this.calculateEloRange(otherWaitTime = System.currentTimeMillis() - entry2.getJoinTime()))) {
                return -1;
            }
            score += eloDiff;
        }
        if (this.pingMatchingEnabled) {
            int pingDiff = Math.abs(entry1.getPing() - entry2.getPing());
            if (pingDiff > this.maxPingDifference) {
                return -1;
            }
            score += pingDiff;
        }
        long waitDiff = Math.abs(entry1.getJoinTime() - entry2.getJoinTime());
        return Math.max(0, score -= (int)(waitDiff / 1000L));
    }

    @Nullable
    public PartyQueueEntry findPartyMatch(@Nonnull PartyQueueEntry entry) {
        List<PartyQueueEntry> potentials = this.getPartyQueues(entry.getKitName(), PartyQueueType.PARTY_VS_PARTY);
        if (potentials.size() < 2) {
            return null;
        }
        long waitTime = System.currentTimeMillis() - entry.getJoinTime();
        int eloRange = this.calculateEloRange(waitTime);
        PartyQueueEntry bestMatch = null;
        int bestScore = Integer.MAX_VALUE;
        for (PartyQueueEntry potential : potentials) {
            int score;
            long otherWaitTime;
            int otherRange;
            if (potential.getPartyId().equals(entry.getPartyId())) continue;
            int sizeDiff = Math.abs(entry.getMembers().size() - potential.getMembers().size());
            int eloDiff = Math.abs(entry.getAverageElo() - potential.getAverageElo());
            if (this.eloMatchingEnabled && eloDiff > eloRange && eloDiff > (otherRange = this.calculateEloRange(otherWaitTime = System.currentTimeMillis() - potential.getJoinTime())) || (score = eloDiff + sizeDiff * 10) >= bestScore) continue;
            bestScore = score;
            bestMatch = potential;
        }
        return bestMatch;
    }

    public void processMatch(@Nonnull QueueEntry entry1, @Nonnull QueueEntry entry2) {
        DuelManager duelManager;
        this.removeFromQueue(entry1);
        this.removeFromQueue(entry2);
        Player player1 = Bukkit.getPlayer((UUID)entry1.getPlayerUUID());
        Player player2 = Bukkit.getPlayer((UUID)entry2.getPlayerUUID());
        if (player1 == null || player2 == null) {
            if (player1 != null) {
                this.joinQueue(player1, entry1.getKitName());
            }
            if (player2 != null) {
                this.joinQueue(player2, entry2.getKitName());
            }
            return;
        }
        DuelArena arena = this.arenaManager.allocateArena(null, entry1.getKitName());
        if (arena == null) {
            player1.sendMessage("\u00a7cNo arenas available! You have been re-queued.");
            player2.sendMessage("\u00a7cNo arenas available! You have been re-queued.");
            this.joinQueue(player1, entry1.getKitName());
            this.joinQueue(player2, entry2.getKitName());
            return;
        }
        this.sendMatchFoundNotification(player1, player2, entry1.getKitName());
        this.sendMatchFoundNotification(player2, player1, entry1.getKitName());
        long waitTime1 = System.currentTimeMillis() - entry1.getJoinTime();
        long waitTime2 = System.currentTimeMillis() - entry2.getJoinTime();
        QueueStatistics stats = this.queueStats.get(entry1.getKitName());
        if (stats != null) {
            stats.recordMatch(waitTime1);
            stats.recordMatch(waitTime2);
        }
        if ((duelManager = this.getDuelManager()) != null) {
            try {
                duelManager.startMatch(player1, player2, entry1.getKitName(), arena, 1, false);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to start match from queue", e);
                this.arenaManager.releaseArena(arena.getName(), null);
                player1.sendMessage("\u00a7cFailed to start match! Please try again.");
                player2.sendMessage("\u00a7cFailed to start match! Please try again.");
                return;
            }
        } else {
            this.plugin.getLogger().severe("DuelManager is null - cannot start match!");
            this.arenaManager.releaseArena(arena.getName(), null);
            player1.sendMessage("\u00a7cMatch system error! Please contact an administrator.");
            player2.sendMessage("\u00a7cMatch system error! Please contact an administrator.");
            return;
        }
        this.fireMatchFoundEvent(entry1, entry2, arena);
    }

    private void removeFromQueue(@Nonnull QueueEntry entry) {
        this.playerQueueMap.remove(entry.getPlayerUUID());
        this.playerKitMap.remove(entry.getPlayerUUID());
        Queue<QueueEntry> queue = this.kitQueues.get(entry.getKitName());
        if (queue != null) {
            queue.remove(entry);
        }
    }

    public void processPartyMatch(@Nonnull PartyQueueEntry entry1, @Nonnull PartyQueueEntry entry2) {
        Player p;
        this.partyQueues.remove(entry1.getPartyId());
        this.partyQueues.remove(entry2.getPartyId());
        for (UUID memberId : entry1.getMembers()) {
            this.playerKitMap.remove(memberId);
            this.playerQueueMap.remove(memberId);
        }
        for (UUID memberId : entry2.getMembers()) {
            this.playerKitMap.remove(memberId);
            this.playerQueueMap.remove(memberId);
        }
        ArrayList<UUID> team1 = new ArrayList<UUID>();
        ArrayList<UUID> team2 = new ArrayList<UUID>();
        for (UUID uUID : entry1.getMembers()) {
            p = Bukkit.getPlayer((UUID)uUID);
            if (p == null || !p.isOnline()) continue;
            team1.add(uUID);
        }
        for (UUID uUID : entry2.getMembers()) {
            p = Bukkit.getPlayer((UUID)uUID);
            if (p == null || !p.isOnline()) continue;
            team2.add(uUID);
        }
        if (team1.isEmpty() || team2.isEmpty()) {
            this.plugin.getLogger().warning("[QueueManager] Party match cancelled \u2014 a team has no online players after validation.");
            this.notifyPartyNoArena(entry1);
            this.notifyPartyNoArena(entry2);
            return;
        }
        DuelArena arena = this.arenaManager.allocateArena(null, entry1.getKitName());
        if (arena == null) {
            this.notifyPartyNoArena(entry1);
            this.notifyPartyNoArena(entry2);
            return;
        }
        if (team1.size() != team2.size()) {
            for (UUID memberId : team1) {
                Player p2 = Bukkit.getPlayer((UUID)memberId);
                if (p2 == null) continue;
                p2.sendMessage("\u00a7e\u00a7l\u26a0 Uneven Match: \u00a76" + team1.size() + "v" + team2.size());
            }
            for (UUID memberId : team2) {
                Player p2 = Bukkit.getPlayer((UUID)memberId);
                if (p2 == null) continue;
                p2.sendMessage("\u00a7e\u00a7l\u26a0 Uneven Match: \u00a76" + team1.size() + "v" + team2.size());
            }
        }
        this.notifyPartyMatchFound(entry1, entry2);
        this.notifyPartyMatchFound(entry2, entry1);
        DuelManager duelManager = this.getDuelManager();
        if (duelManager != null) {
            duelManager.startTeamMatch(team1, team2, entry1.getKitName(), arena, 1, false, "party_vs_party");
        } else {
            this.plugin.getLogger().severe("[QueueManager] DuelManager null in processPartyMatch!");
            this.arenaManager.releaseArena(arena.getName(), null);
        }
    }

    private void sendQueueJoinMessage(@Nonnull Player player, @Nonnull String kitName, int queueSize) {
        player.sendMessage("");
        player.sendMessage("\u00a7a\u00a7lQUEUE JOINED!");
        player.sendMessage("\u00a77Kit: \u00a7e" + kitName);
        player.sendMessage("\u00a77Players in queue: \u00a7e" + queueSize);
        player.sendMessage("\u00a77Use \u00a7e/leavequeue \u00a77to leave the queue.");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        player.sendActionBar((Component)Component.text((String)("\u00a7aSearching for opponent... \u00a77(\u00a7e" + kitName + "\u00a77)")));
    }

    private void sendMatchFoundNotification(@Nonnull Player player, @Nonnull Player opponent, @Nonnull String kitName) {
        Title title = Title.title((Component)Component.text((String)"\u00a7a\u00a7lMATCH FOUND!"), (Component)Component.text((String)("\u00a77vs \u00a7e" + opponent.getName())), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage("");
        player.sendMessage("\u00a7a\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("\u00a7a\u00a7lMATCH FOUND!");
        player.sendMessage("");
        player.sendMessage("\u00a77Opponent: \u00a7e" + opponent.getName());
        player.sendMessage("\u00a77Kit: \u00a7e" + kitName);
        player.sendMessage("\u00a77Starting countdown...");
        player.sendMessage("\u00a7a\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
    }

    private void notifyPartyMatchFound(@Nonnull PartyQueueEntry entry, @Nonnull PartyQueueEntry opponent) {
        for (UUID memberId : entry.getMembers()) {
            Player player = Bukkit.getPlayer((UUID)memberId);
            if (player == null) continue;
            Title title = Title.title((Component)Component.text((String)"\u00a7a\u00a7lMATCH FOUND!"), (Component)Component.text((String)"\u00a77Party vs Party"), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            player.sendMessage("\u00a7a\u00a7lMATCH FOUND! \u00a77Starting countdown...");
        }
    }

    private void notifyPartyNoArena(@Nonnull PartyQueueEntry entry) {
        for (UUID memberId : entry.getMembers()) {
            Player player = Bukkit.getPlayer((UUID)memberId);
            if (player == null) continue;
            player.sendMessage("\u00a7cNo arenas available! Please try again later.");
        }
    }

    public void addListener(@Nonnull QueueEventListener listener) {
        this.eventListeners.add(listener);
    }

    public void removeListener(@Nonnull QueueEventListener listener) {
        this.eventListeners.remove(listener);
    }

    private void fireQueueJoinEvent(@Nonnull QueueEntry entry) {
        for (QueueEventListener listener : this.eventListeners) {
            try {
                listener.onQueueJoin(entry);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Error in queue join listener", e);
            }
        }
    }

    private void fireQueueLeaveEvent(@Nonnull QueueEntry entry) {
        for (QueueEventListener listener : this.eventListeners) {
            try {
                listener.onQueueLeave(entry);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Error in queue leave listener", e);
            }
        }
    }

    private void fireMatchFoundEvent(@Nonnull QueueEntry entry1, @Nonnull QueueEntry entry2, @Nonnull DuelArena arena) {
        for (QueueEventListener listener : this.eventListeners) {
            try {
                listener.onMatchFound(entry1, entry2, arena);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.WARNING, "Error in match found listener", e);
            }
        }
    }

    public void cleanupQueues() {
        long now = System.currentTimeMillis();
        long maxTime = (long)this.maxQueueTime * 1000L;
        int removed = 0;
        for (Queue<QueueEntry> queue : this.kitQueues.values()) {
            Iterator iterator = queue.iterator();
            while (iterator.hasNext()) {
                QueueEntry entry = (QueueEntry)iterator.next();
                Player player = Bukkit.getPlayer((UUID)entry.getPlayerUUID());
                if (player == null || !player.isOnline()) {
                    iterator.remove();
                    this.playerQueueMap.remove(entry.getPlayerUUID());
                    this.playerKitMap.remove(entry.getPlayerUUID());
                    ++removed;
                    continue;
                }
                if (this.maxQueueTime <= 0 || now - entry.getJoinTime() <= maxTime) continue;
                iterator.remove();
                this.playerQueueMap.remove(entry.getPlayerUUID());
                this.playerKitMap.remove(entry.getPlayerUUID());
                ++removed;
                player.sendMessage("\u00a7cYou have been removed from the queue due to timeout.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
        }
        Iterator<PartyQueueEntry> partyIterator = this.partyQueues.values().iterator();
        while (partyIterator.hasNext()) {
            PartyQueueEntry entry = partyIterator.next();
            boolean allOnline = entry.getMembers().stream().allMatch(uuid -> {
                Player p = Bukkit.getPlayer((UUID)uuid);
                return p != null && p.isOnline();
            });
            if (!allOnline) {
                partyIterator.remove();
                for (UUID memberId : entry.getMembers()) {
                    this.playerKitMap.remove(memberId);
                }
                ++removed;
            }
            if (this.maxQueueTime <= 0 || now - entry.getJoinTime() <= maxTime) continue;
            partyIterator.remove();
            for (UUID memberId : entry.getMembers()) {
                this.playerKitMap.remove(memberId);
                Player player = Bukkit.getPlayer((UUID)memberId);
                if (player == null) continue;
                player.sendMessage("\u00a7cYour party has been removed from the queue due to timeout.");
            }
            ++removed;
        }
        if (removed > 0) {
            this.plugin.getLogger().fine("[QueueManager] Cleaned up " + removed + " stale queue entries");
        }
    }

    public boolean isQueueEnabled() {
        return this.queueEnabled;
    }

    public void setQueueEnabled(boolean enabled) {
        this.queueEnabled = enabled;
    }

    public boolean isEloMatchingEnabled() {
        return this.eloMatchingEnabled;
    }

    public boolean isPingMatchingEnabled() {
        return this.pingMatchingEnabled;
    }

    @Nonnull
    public UltimateDuels getPlugin() {
        return this.plugin;
    }

    @Nullable
    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    @Nullable
    public QueueMatchTask getMatchTask() {
        return this.matchTask;
    }

    public void reload() {
        this.loadSettings();
        this.stopMatchTask();
        this.startMatchTask();
        this.plugin.getLogger().info("\u00a7a[QueueManager] Reloaded successfully!");
    }

    public void shutdown() {
        this.stopMatchTask();
        for (UUID uuid : new ArrayList<UUID>(this.playerQueueMap.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            player.sendMessage("\u00a7cYou have been removed from the queue due to server shutdown.");
        }
        this.kitQueues.clear();
        this.playerQueueMap.clear();
        this.playerKitMap.clear();
        this.partyQueues.clear();
        this.plugin.getLogger().info("\u00a7a[QueueManager] Shutdown complete");
    }
}

