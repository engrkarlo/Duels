/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.duel;

import com.ultimateduels.models.duel.Duel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DuelResult {
    private final UUID matchId;
    private final UUID winnerId;
    private final UUID loserId;
    private final List<UUID> winnerTeam;
    private final List<UUID> loserTeam;
    private final int winnerScore;
    private final int loserScore;
    private final int roundsPlayed;
    private final int maxRounds;
    private final String kitId;
    private final String arenaId;
    private final long durationMs;
    private final Timestamp endedAt;
    private final UUID mvpId;
    private final boolean isDraw;
    private final Map<UUID, Duel.MatchStats> playerStats;
    private int winnerEloChange;
    private int loserEloChange;
    private int winnerNewElo;
    private int loserNewElo;
    private final Map<String, Object> metadata;

    public DuelResult(UUID matchId, UUID winnerId, UUID loserId, int winnerScore, int loserScore, int roundsPlayed, int maxRounds, String kitId, String arenaId, long durationMs, UUID mvpId, boolean isDraw, Map<UUID, Duel.MatchStats> playerStats) {
        this.matchId = matchId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.winnerTeam = new ArrayList<UUID>();
        this.loserTeam = new ArrayList<UUID>();
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
        this.roundsPlayed = roundsPlayed;
        this.maxRounds = maxRounds;
        this.kitId = kitId;
        this.arenaId = arenaId;
        this.durationMs = durationMs;
        this.endedAt = new Timestamp(System.currentTimeMillis());
        this.mvpId = mvpId;
        this.isDraw = isDraw;
        this.playerStats = playerStats != null ? new HashMap<UUID, Duel.MatchStats>(playerStats) : new HashMap();
        this.metadata = new HashMap<String, Object>();
    }

    public static DuelResult createTeamResult(UUID matchId, List<UUID> winnerTeam, List<UUID> loserTeam, int winnerScore, int loserScore, int roundsPlayed, int maxRounds, String kitId, String arenaId, long durationMs, UUID mvpId, boolean isDraw, Map<UUID, Duel.MatchStats> playerStats) {
        UUID winnerId = winnerTeam.isEmpty() ? null : winnerTeam.get(0);
        UUID loserId = loserTeam.isEmpty() ? null : loserTeam.get(0);
        DuelResult result = new DuelResult(matchId, winnerId, loserId, winnerScore, loserScore, roundsPlayed, maxRounds, kitId, arenaId, durationMs, mvpId, isDraw, playerStats);
        result.winnerTeam.addAll(winnerTeam);
        result.loserTeam.addAll(loserTeam);
        return result;
    }

    public UUID getMatchId() {
        return this.matchId;
    }

    public UUID getWinnerId() {
        return this.winnerId;
    }

    public UUID getLoserId() {
        return this.loserId;
    }

    public List<UUID> getWinnerTeam() {
        return Collections.unmodifiableList(this.winnerTeam);
    }

    public List<UUID> getLoserTeam() {
        return Collections.unmodifiableList(this.loserTeam);
    }

    public int getWinnerScore() {
        return this.winnerScore;
    }

    public int getLoserScore() {
        return this.loserScore;
    }

    public int getRoundsPlayed() {
        return this.roundsPlayed;
    }

    public int getMaxRounds() {
        return this.maxRounds;
    }

    public String getKitId() {
        return this.kitId;
    }

    public String getArenaId() {
        return this.arenaId;
    }

    public long getDurationMs() {
        return this.durationMs;
    }

    public Timestamp getEndedAt() {
        return this.endedAt;
    }

    public UUID getMvpId() {
        return this.mvpId;
    }

    public boolean isDraw() {
        return this.isDraw;
    }

    public Map<UUID, Duel.MatchStats> getPlayerStats() {
        return Collections.unmodifiableMap(this.playerStats);
    }

    public int getWinnerEloChange() {
        return this.winnerEloChange;
    }

    public int getLoserEloChange() {
        return this.loserEloChange;
    }

    public int getWinnerNewElo() {
        return this.winnerNewElo;
    }

    public int getLoserNewElo() {
        return this.loserNewElo;
    }

    public void setEloChanges(int winnerChange, int loserChange, int winnerNewElo, int loserNewElo) {
        this.winnerEloChange = winnerChange;
        this.loserEloChange = loserChange;
        this.winnerNewElo = winnerNewElo;
        this.loserNewElo = loserNewElo;
    }

    public String getFormattedWinnerEloChange() {
        return (this.winnerEloChange >= 0 ? "+" : "") + this.winnerEloChange;
    }

    public String getFormattedLoserEloChange() {
        return (this.loserEloChange >= 0 ? "+" : "") + this.loserEloChange;
    }

    public String getFormattedScore() {
        return this.winnerScore + " - " + this.loserScore;
    }

    public int getDurationSeconds() {
        return (int)(this.durationMs / 1000L);
    }

    public String getFormattedDuration() {
        int seconds = this.getDurationSeconds();
        int minutes = seconds / 60;
        return String.format("%d:%02d", minutes, seconds %= 60);
    }

    public boolean is1v1() {
        return this.winnerTeam.size() <= 1 && this.loserTeam.size() <= 1;
    }

    public boolean isTeamMatch() {
        return this.winnerTeam.size() > 1 || this.loserTeam.size() > 1;
    }

    public int getTotalKills() {
        return this.playerStats.values().stream().mapToInt(Duel.MatchStats::getKills).sum();
    }

    public double getTotalDamage() {
        return this.playerStats.values().stream().mapToDouble(Duel.MatchStats::getDamageDealt).sum();
    }

    public Duel.MatchStats getStatsFor(UUID uuid) {
        return this.playerStats.get(uuid);
    }

    public boolean involvedPlayer(UUID uuid) {
        return Objects.equals(this.winnerId, uuid) || Objects.equals(this.loserId, uuid) || this.winnerTeam.contains(uuid) || this.loserTeam.contains(uuid);
    }

    public boolean didPlayerWin(UUID uuid) {
        if (this.isDraw) {
            return false;
        }
        return Objects.equals(this.winnerId, uuid) || this.winnerTeam.contains(uuid);
    }

    public String getPlayerOutcome(UUID uuid) {
        if (!this.involvedPlayer(uuid)) {
            return "N/A";
        }
        if (this.isDraw) {
            return "DRAW";
        }
        return this.didPlayerWin(uuid) ? "WIN" : "LOSS";
    }

    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public <T> T getMetadata(String key) {
        return (T)this.metadata.get(key);
    }

    public <T> T getMetadata(String key, T defaultValue) {
        Object value = this.metadata.get(key);
        return (T)(value != null ? value : defaultValue);
    }

    public List<String> getSummaryLines(String winnerName, String loserName) {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        lines.add("\u00a7e\u00a7lDUEL RESULTS");
        lines.add("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        lines.add("");
        if (this.isDraw) {
            lines.add("\u00a77Result: \u00a7e\u00a7lDRAW");
        } else {
            lines.add("\u00a77Winner: \u00a7a" + winnerName);
            lines.add("\u00a77Loser: \u00a7c" + loserName);
        }
        lines.add("\u00a77Final Score: \u00a7e" + this.getFormattedScore());
        lines.add("");
        lines.add("\u00a77Kit: \u00a7b" + this.kitId);
        lines.add("\u00a77Arena: \u00a7d" + this.arenaId);
        lines.add("\u00a77Duration: \u00a7f" + this.getFormattedDuration());
        if (this.winnerEloChange != 0 || this.loserEloChange != 0) {
            lines.add("");
            lines.add("\u00a77ELO Changes:");
            if (!this.isDraw && winnerName != null) {
                lines.add("  \u00a7a" + winnerName + ": " + this.getFormattedWinnerEloChange() + " \u00a77(" + this.winnerNewElo + ")");
            }
            if (loserName != null) {
                lines.add("  \u00a7c" + loserName + ": " + this.getFormattedLoserEloChange() + " \u00a77(" + this.loserNewElo + ")");
            }
        }
        lines.add("");
        lines.add("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        return lines;
    }

    public String getShortSummary(String winnerName, String loserName) {
        if (this.isDraw) {
            return String.format("\u00a76%s \u00a77vs \u00a76%s \u00a7e(Draw) \u00a77- Kit: \u00a7b%s", winnerName, loserName, this.kitId);
        }
        return String.format("\u00a7a%s \u00a77defeated \u00a7c%s \u00a7e(%s) \u00a77- Kit: \u00a7b%s", winnerName, loserName, this.getFormattedScore(), this.kitId);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelResult that = (DuelResult)o;
        return Objects.equals(this.matchId, that.matchId);
    }

    public int hashCode() {
        return Objects.hash(this.matchId);
    }

    public String toString() {
        return "DuelResult{matchId=" + String.valueOf(this.matchId) + ", score=" + this.getFormattedScore() + ", kit=" + this.kitId + ", arena=" + this.arenaId + ", duration=" + this.getFormattedDuration() + ", isDraw=" + this.isDraw + "}";
    }
}

