/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  me.clip.placeholderapi.expansion.PlaceholderExpansion
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.hooks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.ArenaState;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.stats.KitStats;
import com.ultimateduels.stats.LeaderboardEntry;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.stats.StatsManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook
extends PlaceholderExpansion {
    private final UltimateDuels plugin;
    private final DecimalFormat decimalFormat;
    private final DecimalFormat kdFormat;
    private Map<String, List<LeaderboardEntry>> leaderboardCache;
    private long lastLeaderboardUpdate;
    private static final long LEADERBOARD_CACHE_DURATION = 60000L;

    public PlaceholderAPIHook(UltimateDuels plugin) {
        this.plugin = plugin;
        this.decimalFormat = new DecimalFormat("#,###");
        this.kdFormat = new DecimalFormat("0.00");
        this.leaderboardCache = new HashMap<String, List<LeaderboardEntry>>();
        this.lastLeaderboardUpdate = 0L;
    }

    @Nonnull
    public String getIdentifier() {
        return "ultimateduels";
    }

    @Nonnull
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    @Nonnull
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    @Nullable
    public String onRequest(OfflinePlayer offlinePlayer, @Nonnull String params) {
        String result = this.handleGlobalPlaceholder(params);
        if (result != null) {
            return result;
        }
        if (offlinePlayer == null) {
            return "";
        }
        return this.handlePlayerPlaceholder(offlinePlayer, params);
    }

    @Nullable
    private String handleGlobalPlaceholder(@Nonnull String params) {
        if (params.startsWith("queue_") && params.endsWith("_count")) {
            String kitName = this.extractMiddle(params, "queue_", "_count");
            return this.getQueueCount(kitName);
        }
        if (params.startsWith("fighting_") && params.endsWith("_count")) {
            String kitName = this.extractMiddle(params, "fighting_", "_count");
            return this.getFightingCount(kitName);
        }
        if (params.equals("queue_total")) {
            return this.getTotalQueueCount();
        }
        if (params.equals("fighting_total")) {
            return this.getTotalFightingCount();
        }
        if (params.equals("online_total")) {
            return this.getTotalOnline();
        }
        if (params.equals("arenas_total")) {
            return this.getArenasTotal();
        }
        if (params.equals("arenas_available")) {
            return this.getArenasAvailable();
        }
        if (params.equals("arenas_inuse")) {
            return this.getArenasInUse();
        }
        if (params.startsWith("ffa_") && params.endsWith("_count")) {
            String arenaName = this.extractMiddle(params, "ffa_", "_count");
            return this.getFFAPlayerCount(arenaName);
        }
        if (params.equals("ffa_total")) {
            return this.getTotalFFAPlayers();
        }
        if (params.equals("matches_active")) {
            return this.getActiveMatches();
        }
        if (params.startsWith("top_")) {
            return this.handleLeaderboardPlaceholder(params);
        }
        return null;
    }

    @Nullable
    private String handlePlayerPlaceholder(@Nonnull OfflinePlayer offlinePlayer, @Nonnull String params) {
        UUID uuid = offlinePlayer.getUniqueId();
        StatsManager statsManager = this.plugin.getStatsManager();
        PlayerStats stats = statsManager != null ? statsManager.getStats(uuid) : null;
        switch (params.toLowerCase()) {
            case "kills": {
                return stats != null ? this.decimalFormat.format(stats.getTotalKills()) : "0";
            }
            case "deaths": {
                return stats != null ? this.decimalFormat.format(stats.getTotalDeaths()) : "0";
            }
            case "wins": {
                return stats != null ? this.decimalFormat.format(stats.getTotalWins()) : "0";
            }
            case "losses": {
                return stats != null ? this.decimalFormat.format(stats.getTotalLosses()) : "0";
            }
            case "kd": 
            case "kdr": {
                return stats != null ? this.kdFormat.format(stats.getKDR()) : "0.00";
            }
            case "wl": 
            case "wlr": {
                return stats != null ? this.kdFormat.format(stats.getWinLossRatio()) : "0.00";
            }
            case "streak": 
            case "winstreak": {
                return stats != null ? this.decimalFormat.format(stats.getCurrentWinStreak()) : "0";
            }
            case "best_streak": 
            case "highest_streak": {
                return stats != null ? this.decimalFormat.format(stats.getBestWinStreak()) : "0";
            }
            case "matches": 
            case "games": {
                return stats != null ? this.decimalFormat.format(stats.getTotalGames()) : "0";
            }
            case "elo": 
            case "rating": {
                return stats != null ? this.decimalFormat.format(stats.getGlobalElo()) : "1000";
            }
            case "state": 
            case "status": {
                return this.getPlayerState(uuid);
            }
            case "in_queue": {
                return this.isInQueue(uuid) ? "true" : "false";
            }
            case "in_match": 
            case "in_duel": {
                return this.isInMatch(uuid) ? "true" : "false";
            }
            case "in_ffa": {
                return this.isInFFA(uuid) ? "true" : "false";
            }
            case "in_lobby": {
                return this.isInLobby(uuid) ? "true" : "false";
            }
            case "spectating": {
                return this.isSpectating(uuid) ? "true" : "false";
            }
            case "queue_kit": {
                return this.getQueuedKit(uuid);
            }
            case "queue_time": {
                return this.getQueueTime(uuid);
            }
            case "match_opponent": 
            case "opponent": {
                return this.getOpponentName(uuid);
            }
            case "match_kit": {
                return this.getMatchKit(uuid);
            }
            case "match_arena": {
                return this.getMatchArena(uuid);
            }
            case "match_score": {
                return this.getMatchScore(uuid);
            }
            case "match_round": {
                return this.getMatchRound(uuid);
            }
            case "match_time": {
                return this.getMatchTime(uuid);
            }
            case "party_size": {
                return this.getPartySize(uuid);
            }
            case "party_leader": {
                return this.getPartyLeader(uuid);
            }
            case "in_party": {
                return this.isInParty(uuid) ? "true" : "false";
            }
            case "is_party_leader": {
                return this.isPartyLeader(uuid) ? "true" : "false";
            }
        }
        return this.handleKitSpecificStats(uuid, params, stats);
    }

    @Nullable
    private String handleKitSpecificStats(@Nonnull UUID uuid, @Nonnull String params, @Nullable PlayerStats stats) {
        String[] parts = params.split("_");
        if (parts.length < 2) {
            return null;
        }
        String kitName = parts[0];
        String statType = parts[parts.length - 1];
        if (parts.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; ++i) {
                if (i > 0) {
                    sb.append("_");
                }
                sb.append(parts[i]);
            }
            kitName = sb.toString();
        }
        if (stats == null) {
            return "0";
        }
        KitStats kitStats = stats.getKitStats(kitName);
        if (kitStats == null) {
            return "0";
        }
        return switch (statType) {
            case "kills" -> this.decimalFormat.format(kitStats.getKills());
            case "deaths" -> this.decimalFormat.format(kitStats.getDeaths());
            case "wins" -> this.decimalFormat.format(kitStats.getWins());
            case "losses" -> this.decimalFormat.format(kitStats.getLosses());
            case "kd", "kdr" -> this.kdFormat.format(kitStats.getKDR());
            case "wl", "wlr" -> this.kdFormat.format(kitStats.getWinLossRatio());
            case "matches", "games" -> this.decimalFormat.format(kitStats.getTotalGames());
            case "elo" -> this.decimalFormat.format(kitStats.getElo());
            default -> null;
        };
    }

    @Nullable
    private String handleLeaderboardPlaceholder(@Nonnull String params) {
        int position;
        String[] parts = params.substring(4).split("_");
        if (parts.length < 3) {
            return null;
        }
        String statType = parts[0];
        try {
            position = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e) {
            return null;
        }
        String returnType = parts[2];
        if (position < 1 || position > 100) {
            return null;
        }
        this.updateLeaderboardCache();
        List<LeaderboardEntry> leaderboard = this.leaderboardCache.get(statType);
        if (leaderboard == null || position > leaderboard.size()) {
            return returnType.equals("name") ? "---" : "0";
        }
        LeaderboardEntry entry = leaderboard.get(position - 1);
        if (returnType.equals("name")) {
            return entry.getPlayerName() != null ? entry.getPlayerName() : "Unknown";
        }
        if (returnType.equals("value")) {
            if (entry.isDoubleValue()) {
                return this.kdFormat.format(entry.getDoubleValue());
            }
            return this.decimalFormat.format(entry.getValue());
        }
        return null;
    }

    private String getQueueCount(@Nonnull String kitName) {
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            return "0";
        }
        return String.valueOf(queueManager.getQueueSize(kitName));
    }

    private String getTotalQueueCount() {
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            return "0";
        }
        return String.valueOf(queueManager.getTotalQueueSize());
    }

    private String getFightingCount(@Nonnull String kitName) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "0";
        }
        return String.valueOf(duelManager.getPlayersInMatchForKit(kitName));
    }

    private String getTotalFightingCount() {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "0";
        }
        return String.valueOf(duelManager.getTotalPlayersInMatches());
    }

    private String getTotalOnline() {
        FFAManager ffaManager;
        DuelManager duelManager;
        int queue = 0;
        int fighting = 0;
        int ffa = 0;
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager != null) {
            queue = queueManager.getTotalQueueSize();
        }
        if ((duelManager = this.plugin.getDuelManager()) != null) {
            fighting = duelManager.getTotalPlayersInMatches();
        }
        if ((ffaManager = this.plugin.getFFAManager()) != null) {
            ffa = ffaManager.getTotalPlayers();
        }
        return String.valueOf(queue + fighting + ffa);
    }

    private String getArenasTotal() {
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            return "0";
        }
        return String.valueOf(arenaManager.getAllDuelArenas().size());
    }

    private String getArenasAvailable() {
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            return "0";
        }
        return String.valueOf(arenaManager.getAvailableArenaCount());
    }

    private String getArenasInUse() {
        ArenaManager arenaManager = this.plugin.getArenaManager();
        if (arenaManager == null) {
            return "0";
        }
        return String.valueOf(arenaManager.getArenasByState(ArenaState.IN_USE).size());
    }

    private String getFFAPlayerCount(@Nonnull String arenaName) {
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (ffaManager == null) {
            return "0";
        }
        return String.valueOf(ffaManager.getPlayerCount(arenaName));
    }

    private String getTotalFFAPlayers() {
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (ffaManager == null) {
            return "0";
        }
        return String.valueOf(ffaManager.getTotalPlayers());
    }

    private String getActiveMatches() {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "0";
        }
        return String.valueOf(duelManager.getActiveMatchCount());
    }

    private String getPlayerState(@Nonnull UUID uuid) {
        if (this.isInMatch(uuid)) {
            return "In Match";
        }
        if (this.isInQueue(uuid)) {
            return "Queuing";
        }
        if (this.isInFFA(uuid)) {
            return "In FFA";
        }
        if (this.isSpectating(uuid)) {
            return "Spectating";
        }
        if (this.isInLobby(uuid)) {
            return "In Lobby";
        }
        return "None";
    }

    private boolean isInQueue(@Nonnull UUID uuid) {
        QueueManager queueManager = this.plugin.getQueueManager();
        return queueManager != null && queueManager.isInQueue(uuid);
    }

    private boolean isInMatch(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        return duelManager != null && duelManager.isInMatch(uuid);
    }

    private boolean isInFFA(@Nonnull UUID uuid) {
        FFAManager ffaManager = this.plugin.getFFAManager();
        return ffaManager != null && ffaManager.isInFFA(uuid);
    }

    private boolean isInLobby(@Nonnull UUID uuid) {
        return this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobby(uuid);
    }

    private boolean isSpectating(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        return duelManager != null && duelManager.isSpectating(uuid);
    }

    private String getQueuedKit(@Nonnull UUID uuid) {
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            return "None";
        }
        String kit = queueManager.getQueuedKit(uuid);
        return kit != null ? kit : "None";
    }

    private String getQueueTime(@Nonnull UUID uuid) {
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager == null) {
            return "0:00";
        }
        long time = queueManager.getQueueTime(uuid);
        return this.formatTime(time);
    }

    private String getOpponentName(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "None";
        }
        DuelMatch match = duelManager.getMatch(uuid);
        if (match == null) {
            return "None";
        }
        UUID opponentUUID = match.getOpponent(uuid);
        if (opponentUUID == null) {
            return "None";
        }
        Player opponent = Bukkit.getPlayer((UUID)opponentUUID);
        return opponent != null ? opponent.getName() : "Unknown";
    }

    private String getMatchKit(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "None";
        }
        DuelMatch match = duelManager.getMatch(uuid);
        return match != null ? match.getKitName() : "None";
    }

    private String getMatchArena(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "None";
        }
        DuelMatch match = duelManager.getMatch(uuid);
        if (match == null || match.getArena() == null) {
            return "None";
        }
        return match.getArena().getDisplayName();
    }

    private String getMatchScore(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "0 - 0";
        }
        DuelMatch match = duelManager.getMatch(uuid);
        if (match == null) {
            return "0 - 0";
        }
        int score1 = match.getTeamScore(1);
        int score2 = match.getTeamScore(2);
        return score1 + " - " + score2;
    }

    private String getMatchRound(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "0";
        }
        DuelMatch match = duelManager.getMatch(uuid);
        return match != null ? String.valueOf(match.getCurrentRound()) : "0";
    }

    private String getMatchTime(@Nonnull UUID uuid) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return "0:00";
        }
        DuelMatch match = duelManager.getMatch(uuid);
        if (match == null) {
            return "0:00";
        }
        long duration = match.getMatchDuration();
        return this.formatTime(duration);
    }

    private boolean isInParty(@Nonnull UUID uuid) {
        PartyManager partyManager = this.plugin.getPartyManager();
        return partyManager != null && partyManager.isInParty(uuid);
    }

    private boolean isPartyLeader(@Nonnull UUID uuid) {
        PartyManager partyManager = this.plugin.getPartyManager();
        return partyManager != null && partyManager.isPartyLeader(uuid);
    }

    private String getPartySize(@Nonnull UUID uuid) {
        PartyManager partyManager = this.plugin.getPartyManager();
        if (partyManager == null) {
            return "0";
        }
        Party party = partyManager.getParty(uuid);
        return party != null ? String.valueOf(party.getSize()) : "0";
    }

    private String getPartyLeader(@Nonnull UUID uuid) {
        PartyManager partyManager = this.plugin.getPartyManager();
        if (partyManager == null) {
            return "None";
        }
        Party party = partyManager.getParty(uuid);
        if (party == null) {
            return "None";
        }
        Player leader = Bukkit.getPlayer((UUID)party.getLeaderUUID());
        return leader != null ? leader.getName() : "Unknown";
    }

    private void updateLeaderboardCache() {
        if (System.currentTimeMillis() - this.lastLeaderboardUpdate < 60000L) {
            return;
        }
        StatsManager statsManager = this.plugin.getStatsManager();
        if (statsManager == null) {
            return;
        }
        this.leaderboardCache.clear();
        this.leaderboardCache.put("kills", statsManager.getLeaderboard("kills", null, 100));
        this.leaderboardCache.put("wins", statsManager.getLeaderboard("wins", null, 100));
        this.leaderboardCache.put("deaths", statsManager.getLeaderboard("deaths", null, 100));
        this.leaderboardCache.put("streak", statsManager.getLeaderboard("best_winstreak", null, 100));
        this.leaderboardCache.put("elo", statsManager.getLeaderboard("elo", null, 100));
        this.leaderboardCache.put("matches", statsManager.getLeaderboard("games_played", null, 100));
        this.leaderboardCache.put("kdr", statsManager.getLeaderboard("kdr", null, 100));
        this.leaderboardCache.put("wlr", statsManager.getLeaderboard("wlr", null, 100));
        this.lastLeaderboardUpdate = System.currentTimeMillis();
    }

    @Nonnull
    private String extractMiddle(@Nonnull String str, @Nonnull String prefix, @Nonnull String suffix) {
        if (!str.startsWith(prefix) || !str.endsWith(suffix)) {
            return "";
        }
        return str.substring(prefix.length(), str.length() - suffix.length());
    }

    @Nonnull
    private String formatTime(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        if (hours > 0L) {
            return String.format("%d:%02d:%02d", hours, minutes % 60L, seconds % 60L);
        }
        return String.format("%d:%02d", minutes, seconds % 60L);
    }

    @Nonnull
    public List<String> getAvailablePlaceholders() {
        ArrayList<String> placeholders = new ArrayList<String>();
        placeholders.add("%ultimateduels_kills%");
        placeholders.add("%ultimateduels_deaths%");
        placeholders.add("%ultimateduels_wins%");
        placeholders.add("%ultimateduels_losses%");
        placeholders.add("%ultimateduels_kd%");
        placeholders.add("%ultimateduels_wl%");
        placeholders.add("%ultimateduels_streak%");
        placeholders.add("%ultimateduels_best_streak%");
        placeholders.add("%ultimateduels_matches%");
        placeholders.add("%ultimateduels_elo%");
        placeholders.add("%ultimateduels_state%");
        placeholders.add("%ultimateduels_in_queue%");
        placeholders.add("%ultimateduels_in_match%");
        placeholders.add("%ultimateduels_in_ffa%");
        placeholders.add("%ultimateduels_in_lobby%");
        placeholders.add("%ultimateduels_spectating%");
        placeholders.add("%ultimateduels_queue_kit%");
        placeholders.add("%ultimateduels_queue_time%");
        placeholders.add("%ultimateduels_queue_<kit>_count%");
        placeholders.add("%ultimateduels_queue_total%");
        placeholders.add("%ultimateduels_fighting_<kit>_count%");
        placeholders.add("%ultimateduels_fighting_total%");
        placeholders.add("%ultimateduels_online_total%");
        placeholders.add("%ultimateduels_opponent%");
        placeholders.add("%ultimateduels_match_kit%");
        placeholders.add("%ultimateduels_match_arena%");
        placeholders.add("%ultimateduels_match_score%");
        placeholders.add("%ultimateduels_match_round%");
        placeholders.add("%ultimateduels_match_time%");
        placeholders.add("%ultimateduels_in_party%");
        placeholders.add("%ultimateduels_is_party_leader%");
        placeholders.add("%ultimateduels_party_size%");
        placeholders.add("%ultimateduels_party_leader%");
        placeholders.add("%ultimateduels_arenas_total%");
        placeholders.add("%ultimateduels_arenas_available%");
        placeholders.add("%ultimateduels_arenas_inuse%");
        placeholders.add("%ultimateduels_matches_active%");
        placeholders.add("%ultimateduels_ffa_<arena>_count%");
        placeholders.add("%ultimateduels_ffa_total%");
        placeholders.add("%ultimateduels_<kit>_kills%");
        placeholders.add("%ultimateduels_<kit>_wins%");
        placeholders.add("%ultimateduels_<kit>_kd%");
        placeholders.add("%ultimateduels_<kit>_elo%");
        placeholders.add("%ultimateduels_top_kills_<pos>_name%");
        placeholders.add("%ultimateduels_top_kills_<pos>_value%");
        placeholders.add("%ultimateduels_top_wins_<pos>_name%");
        placeholders.add("%ultimateduels_top_wins_<pos>_value%");
        placeholders.add("%ultimateduels_top_elo_<pos>_name%");
        placeholders.add("%ultimateduels_top_elo_<pos>_value%");
        placeholders.add("%ultimateduels_top_streak_<pos>_name%");
        placeholders.add("%ultimateduels_top_streak_<pos>_value%");
        placeholders.add("%ultimateduels_top_kdr_<pos>_name%");
        placeholders.add("%ultimateduels_top_kdr_<pos>_value%");
        return placeholders;
    }
}

