/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.queue.model.PartyQueueEntry;
import com.ultimateduels.queue.model.PartyQueueType;
import com.ultimateduels.queue.model.QueueEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class QueueMatchTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final QueueManager queueManager;
    private final DuelManager duelManager;
    private final Set<UUID> processingEntries;
    private int tickCounter;
    private static final int MATCH_INTERVAL = 5;
    private static final int ACTION_BAR_INTERVAL = 20;
    private static final int CLEANUP_INTERVAL = 100;
    private static final int STATS_UPDATE_INTERVAL = 40;

    public QueueMatchTask(@NotNull UltimateDuels plugin, @NotNull QueueManager queueManager) {
        this.plugin = plugin;
        this.queueManager = queueManager;
        this.duelManager = plugin.getDuelManager();
        this.processingEntries = ConcurrentHashMap.newKeySet();
        this.tickCounter = 0;
    }

    public void run() {
        ++this.tickCounter;
        if (this.tickCounter % 5 == 0) {
            this.processSoloQueues();
            this.processPartyQueues();
        }
        if (this.tickCounter % 20 == 0) {
            this.updateQueueActionBars();
        }
        if (this.tickCounter % 100 == 0) {
            this.queueManager.cleanupQueues();
        }
        if (this.tickCounter % 40 == 0) {
            this.queueManager.updateQueueStatistics();
        }
        if (this.tickCounter >= 6000) {
            this.tickCounter = 0;
        }
    }

    private void processSoloQueues() {
        for (String kitName : this.queueManager.getKitQueueNames()) {
            this.processSoloQueueForKit(kitName);
        }
    }

    private void processSoloQueueForKit(@NotNull String kitName) {
        Queue<QueueEntry> queue = this.queueManager.getKitQueue(kitName);
        if (queue == null || queue.size() < 2) {
            return;
        }
        ArrayList<QueueEntry> snapshot = new ArrayList<QueueEntry>(queue);
        snapshot.sort(Comparator.comparingLong(QueueEntry::getJoinTime));
        for (QueueEntry entry : snapshot) {
            if (!this.tryMatchEntry(entry)) continue;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean tryMatchEntry(@NotNull QueueEntry entry) {
        UUID uuid = entry.getPlayerUUID();
        if (this.processingEntries.contains(uuid)) {
            return false;
        }
        if (!this.isValidQueueEntry(entry)) {
            this.queueManager.removeFromQueue(uuid);
            return false;
        }
        this.processingEntries.add(uuid);
        try {
            boolean bl;
            QueueEntry match = this.queueManager.findMatch(entry);
            if (match == null) {
                boolean bl2 = false;
                return bl2;
            }
            UUID matchUuid = match.getPlayerUUID();
            if (this.processingEntries.contains(matchUuid) || !this.isValidQueueEntry(match)) {
                boolean bl3 = false;
                return bl3;
            }
            this.processingEntries.add(matchUuid);
            try {
                this.queueManager.processMatch(entry, match);
                bl = true;
                this.processingEntries.remove(matchUuid);
            }
            catch (Throwable throwable) {
                this.processingEntries.remove(matchUuid);
                throw throwable;
            }
            return bl;
        }
        finally {
            this.processingEntries.remove(uuid);
        }
    }

    private boolean isValidQueueEntry(@NotNull QueueEntry entry) {
        UUID uuid = entry.getPlayerUUID();
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player == null || !player.isOnline()) {
            return false;
        }
        if (!this.queueManager.isInQueue(uuid)) {
            return false;
        }
        return this.duelManager == null || !this.duelManager.isInMatch(uuid);
    }

    private void processPartyQueues() {
        Collection<PartyQueueEntry> partyQueues = this.queueManager.getAllPartyQueues();
        if (partyQueues.isEmpty()) {
            return;
        }
        List<PartyQueueEntry> pvpQueues = partyQueues.stream().filter(e -> e.getQueueType() == PartyQueueType.PARTY_VS_PARTY).sorted(Comparator.comparingLong(PartyQueueEntry::getJoinTime)).toList();
        if (pvpQueues.size() < 2) {
            return;
        }
        HashSet<UUID> processedParties = new HashSet<UUID>();
        for (PartyQueueEntry entry : pvpQueues) {
            UUID partyId = entry.getPartyId();
            if (processedParties.contains(partyId)) continue;
            if (!this.isValidPartyQueueEntry(entry)) {
                this.queueManager.removePartyFromQueue(partyId);
                continue;
            }
            PartyQueueEntry match = this.queueManager.findPartyMatch(entry);
            if (match == null || processedParties.contains(match.getPartyId())) continue;
            if (!this.isValidPartyQueueEntry(match)) {
                this.queueManager.removePartyFromQueue(match.getPartyId());
                continue;
            }
            processedParties.add(partyId);
            processedParties.add(match.getPartyId());
            this.queueManager.processPartyMatch(entry, match);
        }
    }

    private boolean isValidPartyQueueEntry(@NotNull PartyQueueEntry entry) {
        for (UUID memberId : entry.getMembers()) {
            Player member = Bukkit.getPlayer((UUID)memberId);
            if (member != null && member.isOnline()) continue;
            return false;
        }
        if (this.plugin.getPartyManager() != null) {
            return this.plugin.getPartyManager().getPartyById(entry.getPartyId()) != null;
        }
        return true;
    }

    private void updateQueueActionBars() {
        this.updateSoloQueueActionBars();
        this.updatePartyQueueActionBars();
    }

    private void updateSoloQueueActionBars() {
        for (String kitName : this.queueManager.getKitQueueNames()) {
            Queue<QueueEntry> queue = this.queueManager.getKitQueue(kitName);
            if (queue == null || queue.isEmpty()) continue;
            int queueSize = queue.size();
            int fightingCount = this.queueManager.getFightingCount(kitName);
            for (QueueEntry entry : queue) {
                Player player = Bukkit.getPlayer((UUID)entry.getPlayerUUID());
                if (player == null || !player.isOnline()) continue;
                long waitTime = System.currentTimeMillis() - entry.getJoinTime();
                String formattedTime = this.formatTime(waitTime);
                Component actionBar = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append((Component)Component.text((String)"Searching... ", (TextColor)NamedTextColor.GREEN))).append((Component)Component.text((String)"[", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)kitName, (TextColor)NamedTextColor.YELLOW))).append((Component)Component.text((String)"] ", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)"| ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text((String)"Queue: ", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((int)queueSize, (TextColor)NamedTextColor.YELLOW))).append((Component)Component.text((String)" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text((String)"Fighting: ", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((int)fightingCount, (TextColor)NamedTextColor.YELLOW))).append((Component)Component.text((String)" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text((String)"Time: ", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)formattedTime, (TextColor)NamedTextColor.YELLOW));
                player.sendActionBar(actionBar);
            }
        }
    }

    private void updatePartyQueueActionBars() {
        for (PartyQueueEntry entry : this.queueManager.getAllPartyQueues()) {
            long waitTime = System.currentTimeMillis() - entry.getJoinTime();
            String formattedTime = this.formatTime(waitTime);
            String queueTypeName = entry.getQueueType().getDisplayName();
            Component actionBar = ((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.empty().append((Component)Component.text((String)"Party Queue... ", (TextColor)NamedTextColor.LIGHT_PURPLE))).append((Component)Component.text((String)"[", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)entry.getKitName(), (TextColor)NamedTextColor.YELLOW))).append((Component)Component.text((String)"] ", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)"| ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text((String)queueTypeName, (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)" | ", (TextColor)NamedTextColor.DARK_GRAY))).append((Component)Component.text((String)"Time: ", (TextColor)NamedTextColor.GRAY))).append((Component)Component.text((String)formattedTime, (TextColor)NamedTextColor.YELLOW));
            for (UUID memberId : entry.getMembers()) {
                Player member = Bukkit.getPlayer((UUID)memberId);
                if (member == null || !member.isOnline()) continue;
                member.sendActionBar(actionBar);
            }
        }
    }

    @NotNull
    private String formatTime(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("0:%02d", seconds);
    }

    public void forceProcess() {
        this.processSoloQueues();
        this.processPartyQueues();
    }

    public void clearProcessing() {
        this.processingEntries.clear();
    }
}

