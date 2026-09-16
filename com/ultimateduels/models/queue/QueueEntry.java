/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.queue;

import com.ultimateduels.models.player.DuelPlayer;
import com.ultimateduels.models.player.PlayerStats;
import com.ultimateduels.models.queue.QueueType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class QueueEntry {
    private final UUID entryId = UUID.randomUUID();
    private final QueueType type;
    private final UUID primaryPlayerId;
    private final String primaryPlayerName;
    private final List<UUID> allPlayerIds;
    private final Map<UUID, String> playerNames;
    private final String kitId;
    private String preferredArenaId;
    private int preferredRounds;
    private UUID partyId;
    private int teamSize;
    private int averageElo;
    private int minElo;
    private int maxElo;
    private int eloRange;
    private final long queuedAt;
    private long lastMatchAttempt;
    private int matchAttempts;
    private QueueEntryStatus status;
    private UUID matchedWithId;
    private int priority;
    private boolean isPremium;
    private static final int DEFAULT_ELO = 1000;

    public QueueEntry(DuelPlayer player, String kitId) {
        this.type = QueueType.SOLO;
        this.primaryPlayerId = player.getUuid();
        this.primaryPlayerName = player.getName();
        this.allPlayerIds = new ArrayList<UUID>();
        this.playerNames = new HashMap<UUID, String>();
        this.kitId = kitId;
        this.preferredRounds = 1;
        this.teamSize = 1;
        this.queuedAt = System.currentTimeMillis();
        this.status = QueueEntryStatus.WAITING;
        this.priority = 0;
        this.eloRange = 200;
        this.allPlayerIds.add(player.getUuid());
        this.playerNames.put(player.getUuid(), player.getName());
        this.minElo = this.averageElo = this.getPlayerElo(player, kitId);
        this.maxElo = this.averageElo;
    }

    public QueueEntry(UUID partyId, List<DuelPlayer> players, String kitId, int teamSize) {
        this.type = QueueType.PARTY;
        this.partyId = partyId;
        this.primaryPlayerId = players.get(0).getUuid();
        this.primaryPlayerName = players.get(0).getName();
        this.allPlayerIds = new ArrayList<UUID>();
        this.playerNames = new HashMap<UUID, String>();
        this.kitId = kitId;
        this.preferredRounds = 1;
        this.teamSize = teamSize;
        this.queuedAt = System.currentTimeMillis();
        this.status = QueueEntryStatus.WAITING;
        this.priority = 0;
        this.eloRange = 300;
        int totalElo = 0;
        int minEloVal = Integer.MAX_VALUE;
        int maxEloVal = Integer.MIN_VALUE;
        for (DuelPlayer player : players) {
            this.allPlayerIds.add(player.getUuid());
            this.playerNames.put(player.getUuid(), player.getName());
            int playerElo = this.getPlayerElo(player, kitId);
            totalElo += playerElo;
            minEloVal = Math.min(minEloVal, playerElo);
            maxEloVal = Math.max(maxEloVal, playerElo);
        }
        this.averageElo = players.isEmpty() ? 1000 : totalElo / players.size();
        this.minElo = minEloVal == Integer.MAX_VALUE ? 1000 : minEloVal;
        this.maxElo = maxEloVal == Integer.MIN_VALUE ? 1000 : maxEloVal;
    }

    private int getPlayerElo(DuelPlayer player, String kitId) {
        if (player == null) {
            return 1000;
        }
        PlayerStats stats = player.getStats();
        if (stats == null) {
            return 1000;
        }
        try {
            if (kitId != null && !kitId.isEmpty()) {
                return stats.getElo(kitId);
            }
            return stats.getGlobalElo();
        }
        catch (Exception e) {
            return 1000;
        }
    }

    public QueueEntryStatus getStatus() {
        return this.status;
    }

    public void setStatus(QueueEntryStatus status) {
        this.status = status;
    }

    public void markMatching() {
        this.status = QueueEntryStatus.MATCHING;
        this.lastMatchAttempt = System.currentTimeMillis();
        ++this.matchAttempts;
    }

    public void markMatched(UUID otherEntryId) {
        this.status = QueueEntryStatus.MATCHED;
        this.matchedWithId = otherEntryId;
    }

    public void markCancelled() {
        this.status = QueueEntryStatus.CANCELLED;
    }

    public void markExpired() {
        this.status = QueueEntryStatus.EXPIRED;
    }

    public void resetToWaiting() {
        this.status = QueueEntryStatus.WAITING;
        this.matchedWithId = null;
    }

    public boolean isWaiting() {
        return this.status == QueueEntryStatus.WAITING;
    }

    public boolean isMatching() {
        return this.status == QueueEntryStatus.MATCHING;
    }

    public boolean isMatched() {
        return this.status == QueueEntryStatus.MATCHED;
    }

    public boolean isActive() {
        return this.status == QueueEntryStatus.WAITING || this.status == QueueEntryStatus.MATCHING;
    }

    public boolean canMatchWith(QueueEntry other) {
        if (other == null) {
            return false;
        }
        if (other.equals(this)) {
            return false;
        }
        if (!other.isActive()) {
            return false;
        }
        if (!this.kitId.equals(other.kitId)) {
            return false;
        }
        if (this.teamSize != other.teamSize) {
            return false;
        }
        if (!this.isEloCompatible(other)) {
            return false;
        }
        for (UUID playerId : this.allPlayerIds) {
            if (!other.allPlayerIds.contains(playerId)) continue;
            return false;
        }
        return true;
    }

    public boolean isEloCompatible(QueueEntry other) {
        int effectiveRange = this.getEffectiveEloRange();
        int otherEffectiveRange = other.getEffectiveEloRange();
        int range = Math.max(effectiveRange, otherEffectiveRange);
        return Math.abs(this.averageElo - other.averageElo) <= range;
    }

    public int getEffectiveEloRange() {
        long waitTime = this.getTimeInQueue() / 1000L;
        int expansion = (int)(waitTime / 30L * 50L);
        return Math.min(this.eloRange + expansion, 500);
    }

    public double getMatchQuality(QueueEntry other) {
        if (!this.canMatchWith(other)) {
            return 0.0;
        }
        double quality = 100.0;
        int eloDiff = Math.abs(this.averageElo - other.averageElo);
        quality -= (double)eloDiff / 10.0;
        long combinedWait = this.getTimeInQueue() + other.getTimeInQueue();
        quality += Math.min((double)combinedWait / 10000.0, 20.0);
        if (this.isPremium || other.isPremium) {
            quality += 10.0;
        }
        return Math.max(0.0, quality += (double)(this.priority + other.priority));
    }

    public long getTimeInQueue() {
        return System.currentTimeMillis() - this.queuedAt;
    }

    public int getSecondsInQueue() {
        return (int)(this.getTimeInQueue() / 1000L);
    }

    public String getFormattedQueueTime() {
        int seconds = this.getSecondsInQueue();
        int minutes = seconds / 60;
        return String.format("%d:%02d", minutes, seconds %= 60);
    }

    public boolean hasExceededMaxTime(int maxSeconds) {
        return this.getSecondsInQueue() > maxSeconds;
    }

    public UUID getEntryId() {
        return this.entryId;
    }

    public QueueType getType() {
        return this.type;
    }

    public UUID getPrimaryPlayerId() {
        return this.primaryPlayerId;
    }

    public String getPrimaryPlayerName() {
        return this.primaryPlayerName;
    }

    public List<UUID> getAllPlayerIds() {
        return Collections.unmodifiableList(this.allPlayerIds);
    }

    public Map<UUID, String> getPlayerNames() {
        return Collections.unmodifiableMap(this.playerNames);
    }

    public String getKitId() {
        return this.kitId;
    }

    public String getPreferredArenaId() {
        return this.preferredArenaId;
    }

    public int getPreferredRounds() {
        return this.preferredRounds;
    }

    public UUID getPartyId() {
        return this.partyId;
    }

    public int getTeamSize() {
        return this.teamSize;
    }

    public int getAverageElo() {
        return this.averageElo;
    }

    public int getMinElo() {
        return this.minElo;
    }

    public int getMaxElo() {
        return this.maxElo;
    }

    public int getEloRange() {
        return this.eloRange;
    }

    public long getQueuedAt() {
        return this.queuedAt;
    }

    public long getLastMatchAttempt() {
        return this.lastMatchAttempt;
    }

    public int getMatchAttempts() {
        return this.matchAttempts;
    }

    public UUID getMatchedWithId() {
        return this.matchedWithId;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean isPremium() {
        return this.isPremium;
    }

    public void setPreferredArenaId(String arenaId) {
        this.preferredArenaId = arenaId;
    }

    public void setPreferredRounds(int rounds) {
        this.preferredRounds = Math.max(1, Math.min(20, rounds));
    }

    public void setEloRange(int range) {
        this.eloRange = Math.max(50, Math.min(500, range));
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setPremium(boolean premium) {
        this.isPremium = premium;
    }

    public int getPlayerCount() {
        return this.allPlayerIds.size();
    }

    public boolean containsPlayer(UUID uuid) {
        return this.allPlayerIds.contains(uuid);
    }

    public boolean isSolo() {
        return this.type == QueueType.SOLO;
    }

    public boolean isParty() {
        return this.type == QueueType.PARTY;
    }

    public String getPlayerName(UUID uuid) {
        return this.playerNames.get(uuid);
    }

    public String getDescription() {
        if (this.isSolo()) {
            return this.primaryPlayerName + " (" + this.averageElo + " ELO)";
        }
        return this.primaryPlayerName + "'s Party (" + this.getPlayerCount() + " players, ~" + this.averageElo + " ELO)";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        QueueEntry that = (QueueEntry)o;
        return Objects.equals(this.entryId, that.entryId);
    }

    public int hashCode() {
        return Objects.hash(this.entryId);
    }

    public String toString() {
        return "QueueEntry{entryId=" + String.valueOf(this.entryId) + ", type=" + String.valueOf((Object)this.type) + ", kit=" + this.kitId + ", players=" + this.getPlayerCount() + ", elo=" + this.averageElo + ", status=" + String.valueOf((Object)this.status) + ", queueTime=" + this.getFormattedQueueTime() + "}";
    }

    public static enum QueueEntryStatus {
        WAITING("Waiting", "\u00a7e"),
        MATCHING("Matching", "\u00a76"),
        MATCHED("Matched", "\u00a7a"),
        CANCELLED("Cancelled", "\u00a7c"),
        EXPIRED("Expired", "\u00a77");

        private final String displayName;
        private final String colorCode;

        private QueueEntryStatus(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getColorCode() {
            return this.colorCode;
        }

        public String getColoredName() {
            return this.colorCode + this.displayName;
        }
    }
}

