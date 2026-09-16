/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.PlaceholderAPI
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 *  org.bukkit.scoreboard.Criteria
 *  org.bukkit.scoreboard.DisplaySlot
 *  org.bukkit.scoreboard.Objective
 *  org.bukkit.scoreboard.Score
 *  org.bukkit.scoreboard.Scoreboard
 */
package com.ultimateduels.scoreboard;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.ffa.model.FFAPlayerData;
import com.ultimateduels.stats.LeaderboardEntry;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.utils.MessageUtils;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardBuilder {
    private static final DecimalFormat KD_FORMAT = new DecimalFormat("#.##");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Pattern CURLY_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");
    private static final Pattern ANGLE_PATTERN = Pattern.compile("<([a-zA-Z0-9_]+)>");
    private static final Pattern PERCENT_PATTERN = Pattern.compile("%([a-zA-Z0-9_]+)%");
    private static final boolean PLACEHOLDER_API_ENABLED = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    private final Player player;
    private final Scoreboard scoreboard;
    private Objective objective;
    private String title;
    private final List<String> lines;
    private static final String[] COLOR_CODES = new String[]{"\u00a70", "\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75", "\u00a76", "\u00a77", "\u00a78", "\u00a79", "\u00a7a", "\u00a7b", "\u00a7c", "\u00a7d", "\u00a7e", "\u00a7f"};

    public ScoreboardBuilder(Player player) {
        this.player = player;
        this.lines = new ArrayList<String>();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("ultimateduels", Criteria.DUMMY, MessageUtils.colorize("&6&lUltimateDuels"));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(this.scoreboard);
    }

    public void updateTitle(String title) {
        this.title = title;
        String processed = this.applyPlaceholders(title, this.player);
        String displayTitle = this.parseMiniMessageToLegacy(processed);
        this.objective.setDisplayName(displayTitle);
    }

    public void updateLines(List<String> newLines) {
        for (String entry : this.scoreboard.getEntries()) {
            this.scoreboard.resetScores(entry);
        }
        this.lines.clear();
        this.lines.addAll(newLines);
        int score = newLines.size();
        for (int i = 0; i < newLines.size(); ++i) {
            String line = newLines.get(i);
            String entry = this.formatLine(line, i);
            Score scoreObj = this.objective.getScore(entry);
            scoreObj.setScore(score--);
        }
    }

    private String formatLine(String line, int index) {
        Object result;
        String processed = this.applyPlaceholders(line, this.player);
        String colorized = this.parseMiniMessageToLegacy(processed);
        Object prefix = "";
        if (index < COLOR_CODES.length) {
            prefix = COLOR_CODES[index] + String.valueOf(ChatColor.RESET);
        }
        if (((String)(result = (String)prefix + colorized)).length() > 40) {
            result = ((String)result).substring(0, 40);
        }
        return result;
    }

    private String parseMiniMessageToLegacy(String text) {
        try {
            if (text.contains("&")) {
                return MessageUtils.colorize(text);
            }
            UltimateDuels plugin = UltimateDuels.getInstance();
            if (plugin == null) {
                return MessageUtils.colorize(text);
            }
            Component component = plugin.getMiniMessage().deserialize((Object)text);
            return LegacyComponentSerializer.legacySection().serialize(component);
        }
        catch (Exception e) {
            return MessageUtils.colorize(text);
        }
    }

    public void setLine(int index, String line) {
        if (index >= 0 && index < this.lines.size()) {
            this.lines.set(index, line);
            this.updateLines(new ArrayList<String>(this.lines));
        }
    }

    public void addLine(String line) {
        this.lines.add(line);
        this.updateLines(new ArrayList<String>(this.lines));
    }

    public void removeLine(int index) {
        if (index >= 0 && index < this.lines.size()) {
            this.lines.remove(index);
            this.updateLines(new ArrayList<String>(this.lines));
        }
    }

    public void clearLines() {
        this.lines.clear();
        for (String entry : this.scoreboard.getEntries()) {
            this.scoreboard.resetScores(entry);
        }
    }

    public void delete() {
        if (this.player.isOnline()) {
            this.player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        if (this.objective != null) {
            this.objective.unregister();
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isValid() {
        return this.player.isOnline() && this.objective != null;
    }

    public int getLineCount() {
        return this.lines.size();
    }

    private String applyPlaceholders(String input, Player player) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (player == null) {
            return input;
        }
        Map<String, String> values = this.buildPlaceholders(player);
        String result = input;
        result = this.replacePattern(result, CURLY_PATTERN, values, true);
        result = this.replacePattern(result, ANGLE_PATTERN, values, false);
        result = this.replacePattern(result, PERCENT_PATTERN, values, false);
        if (PLACEHOLDER_API_ENABLED && result.contains("%")) {
            try {
                result = PlaceholderAPI.setPlaceholders((Player)player, (String)result);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return result;
    }

    private String replacePattern(String input, Pattern pattern, Map<String, String> values, boolean replaceUnknownWithEmpty) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = values.get(key);
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
                continue;
            }
            if (replaceUnknownWithEmpty) {
                matcher.appendReplacement(sb, "");
                continue;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Map<String, String> buildPlaceholders(Player player) {
        HashMap<String, String> v;
        block31: {
            v = new HashMap<String, String>();
            UltimateDuels plugin = UltimateDuels.getInstance();
            if (plugin == null) {
                return v;
            }
            UUID uuid = player.getUniqueId();
            v.put("player", player.getName());
            v.put("player_name", player.getName());
            v.put("name", player.getName());
            v.put("displayname", player.getDisplayName());
            v.put("world", player.getWorld().getName());
            v.put("ping", String.valueOf(player.getPing()));
            v.put("player_ping", String.valueOf(player.getPing()));
            v.put("health", String.valueOf((int)Math.ceil(player.getHealth())));
            v.put("max_health", String.valueOf((int)player.getMaxHealth()));
            v.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
            v.put("online_count", String.valueOf(Bukkit.getOnlinePlayers().size()));
            v.put("max_players", String.valueOf(Bukkit.getMaxPlayers()));
            v.put("server", Bukkit.getName());
            v.put("date", DATE_FORMAT.format(new Date()));
            v.put("time", TIME_FORMAT.format(new Date()));
            try {
                if (plugin.getConfigManager() != null) {
                    String web = plugin.getConfigManager().getWebsiteUrl();
                    v.put("website", web != null ? web : "");
                }
            }
            catch (Throwable ignored) {
                v.put("website", "");
            }
            try {
                if (plugin.getStatsManager() != null) {
                    PlayerStats s = plugin.getStatsManager().getStats(uuid);
                    v.put("kills", String.valueOf(s.getTotalKills()));
                    v.put("deaths", String.valueOf(s.getTotalDeaths()));
                    v.put("kd", s.getFormattedKDR());
                    v.put("kdr", s.getFormattedKDR());
                    v.put("wins", String.valueOf(s.getTotalWins()));
                    v.put("losses", String.valueOf(s.getTotalLosses()));
                    v.put("wlr", s.getFormattedWLR());
                    v.put("win_rate", String.format("%.1f", s.getWinRate()));
                    v.put("winrate", String.format("%.1f", s.getWinRate()));
                    v.put("streak", String.valueOf(s.getCurrentWinStreak()));
                    v.put("current_streak", String.valueOf(s.getCurrentWinStreak()));
                    v.put("win_streak", String.valueOf(s.getCurrentWinStreak()));
                    v.put("best_streak", String.valueOf(s.getBestWinStreak()));
                    v.put("best_winstreak", String.valueOf(s.getBestWinStreak()));
                    v.put("best_killstreak", String.valueOf(s.getBestKillStreak()));
                    v.put("elo", String.valueOf(s.getGlobalElo()));
                    v.put("games", String.valueOf(s.getTotalGames()));
                    v.put("games_played", String.valueOf(s.getTotalGames()));
                    v.put("matches_played", String.valueOf(s.getTotalMatchesPlayed()));
                }
            }
            catch (Throwable s) {
                // empty catch block
            }
            try {
                if (plugin.getQueueManager() != null) {
                    v.put("queue_count", String.valueOf(plugin.getQueueManager().getTotalQueueSize()));
                    v.put("in_queue", String.valueOf(plugin.getQueueManager().isInQueue(uuid)));
                }
            }
            catch (Throwable s) {
                // empty catch block
            }
            try {
                if (plugin.getDuelManager() != null) {
                    int matches = plugin.getDuelManager().getActiveMatches().size();
                    v.put("active_matches", String.valueOf(matches));
                    v.put("fighting_count", String.valueOf(matches * 2));
                }
            }
            catch (Throwable matches) {
                // empty catch block
            }
            try {
                if (plugin.getStatsManager() != null) {
                    for (int rank = 1; rank <= 10; ++rank) {
                        this.putTop(v, plugin, "wins", rank);
                        this.putTop(v, plugin, "kills", rank);
                        this.putTop(v, plugin, "elo", rank);
                        this.putTop(v, plugin, "kdr", rank);
                    }
                }
            }
            catch (Throwable rank) {
                // empty catch block
            }
            try {
                if (plugin.getFFAManager() != null && plugin.getFFAManager().isInFFA(uuid)) {
                    String arenaName = plugin.getFFAManager().getPlayerArena(uuid);
                    FFAArenaInstance arena = arenaName != null ? plugin.getFFAManager().getFFAArena(arenaName) : null;
                    FFAPlayerData data = plugin.getFFAManager().getPlayerData(uuid);
                    if (arena != null) {
                        v.put("arena", arena.getArenaName());
                        v.put("arena_name", arena.getArenaName());
                        v.put("kit", arena.getKitName());
                        v.put("kit_name", arena.getKitName());
                        v.put("players", String.valueOf(arena.getPlayerCount()));
                        v.put("player_count", String.valueOf(arena.getPlayerCount()));
                    }
                    int ffaKills = data != null ? data.getKills() : 0;
                    int ffaDeaths = data != null ? data.getDeaths() : 0;
                    int ffaStreak = data != null ? data.getKillstreak() : 0;
                    int bestStreak = data != null ? data.getHighestKillstreak() : 0;
                    long sessionTime = data != null ? data.getSessionDuration() : 0L;
                    v.put("ffa_kills", String.valueOf(ffaKills));
                    v.put("ffa_deaths", String.valueOf(ffaDeaths));
                    v.put("ffa_kd", ScoreboardBuilder.formatKD(ffaKills, ffaDeaths));
                    v.put("ffa_kdr", ScoreboardBuilder.formatKD(ffaKills, ffaDeaths));
                    v.put("ffa_streak", String.valueOf(ffaStreak));
                    v.put("ffa_killstreak", String.valueOf(ffaStreak));
                    v.put("ffa_best_streak", String.valueOf(bestStreak));
                    v.put("session_time", ScoreboardBuilder.formatDuration(sessionTime));
                    v.put("kills", String.valueOf(ffaKills));
                    v.put("deaths", String.valueOf(ffaDeaths));
                    v.put("kd", ScoreboardBuilder.formatKD(ffaKills, ffaDeaths));
                    v.put("kdr", ScoreboardBuilder.formatKD(ffaKills, ffaDeaths));
                    v.put("streak", String.valueOf(ffaStreak));
                    v.put("protected", plugin.getFFAManager().hasSpawnProtection(uuid) ? "&a\u2726 PROTECTED \u2726" : "");
                }
            }
            catch (Throwable arenaName) {
                // empty catch block
            }
            try {
                DuelMatch match;
                if (plugin.getDuelManager() != null && (match = plugin.getDuelManager().getMatch(player)) != null) {
                    Player oppPlayer;
                    DuelParticipant self = match.getParticipant(uuid);
                    UUID opponentUuid = match.getOpponent(uuid);
                    DuelParticipant opp = opponentUuid != null ? match.getParticipant(opponentUuid) : null;
                    Player player2 = oppPlayer = opponentUuid != null ? Bukkit.getPlayer((UUID)opponentUuid) : null;
                    String oppName = oppPlayer != null ? oppPlayer.getName() : (opp != null ? opp.getPlayerName() : "Unknown");
                    int oppPing = oppPlayer != null ? oppPlayer.getPing() : 0;
                    int selfTeam = self != null ? self.getTeamId() : 1;
                    int oppTeam = selfTeam == 1 ? 2 : 1;
                    int playerScore = match.getTeamScore(selfTeam);
                    int oppScore = match.getTeamScore(oppTeam);
                    v.put("opponent", oppName);
                    v.put("opponent_name", oppName);
                    v.put("opponent_ping", String.valueOf(oppPing));
                    v.put("player_score", String.valueOf(playerScore));
                    v.put("opponent_score", String.valueOf(oppScore));
                    v.put("score", match.getScoreString());
                    v.put("current_round", String.valueOf(match.getCurrentRound()));
                    v.put("round", String.valueOf(match.getCurrentRound()));
                    v.put("max_rounds", String.valueOf(match.getTotalRounds()));
                    v.put("total_rounds", String.valueOf(match.getTotalRounds()));
                    v.put("kit", match.getKitName());
                    v.put("kit_name", match.getKitName());
                    v.put("arena", match.getArena() != null ? match.getArena().getName() : "Unknown");
                    v.put("arena_name", match.getArena() != null ? match.getArena().getName() : "Unknown");
                    v.put("duration", ScoreboardBuilder.formatDuration(match.getMatchDuration()));
                    v.put("match_time", ScoreboardBuilder.formatDuration(match.getMatchDuration()));
                    v.put("round_time", ScoreboardBuilder.formatDuration(match.getRoundDuration()));
                    List<DuelParticipant> t1 = match.getTeam1();
                    List<DuelParticipant> t2 = match.getTeam2();
                    if (!t1.isEmpty()) {
                        v.put("player1", t1.get(0).getPlayerName());
                        v.put("player1_name", t1.get(0).getPlayerName());
                    }
                    if (!t2.isEmpty()) {
                        v.put("player2", t2.get(0).getPlayerName());
                        v.put("player2_name", t2.get(0).getPlayerName());
                    }
                    v.put("score1", String.valueOf(match.getTeamScore(1)));
                    v.put("score2", String.valueOf(match.getTeamScore(2)));
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                if (plugin.getDuelManager() == null || !plugin.getDuelManager().isSpectating(uuid)) break block31;
                for (DuelMatch match : plugin.getDuelManager().getActiveMatches()) {
                    if (!plugin.getDuelManager().getMatchSpectators(match.getMatchId()).contains(uuid)) continue;
                    List<DuelParticipant> t1 = match.getTeam1();
                    List<DuelParticipant> t2 = match.getTeam2();
                    if (!t1.isEmpty()) {
                        v.put("player1", t1.get(0).getPlayerName());
                        v.put("player1_name", t1.get(0).getPlayerName());
                    }
                    if (!t2.isEmpty()) {
                        v.put("player2", t2.get(0).getPlayerName());
                        v.put("player2_name", t2.get(0).getPlayerName());
                    }
                    v.put("score1", String.valueOf(match.getTeamScore(1)));
                    v.put("score2", String.valueOf(match.getTeamScore(2)));
                    v.put("score", match.getScoreString());
                    v.put("current_round", String.valueOf(match.getCurrentRound()));
                    v.put("round", String.valueOf(match.getCurrentRound()));
                    v.put("max_rounds", String.valueOf(match.getTotalRounds()));
                    v.put("total_rounds", String.valueOf(match.getTotalRounds()));
                    v.put("kit", match.getKitName());
                    v.put("kit_name", match.getKitName());
                    v.put("arena", match.getArena() != null ? match.getArena().getName() : "Unknown");
                    v.put("arena_name", match.getArena() != null ? match.getArena().getName() : "Unknown");
                    v.put("duration", ScoreboardBuilder.formatDuration(match.getMatchDuration()));
                    break;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return v;
    }

    private void putTop(Map<String, String> v, UltimateDuels plugin, String stat, int rank) {
        try {
            List<LeaderboardEntry> lb = plugin.getStatsManager().getLeaderboard(stat, null, rank);
            if (lb != null && lb.size() >= rank) {
                String name = lb.get(rank - 1).getPlayerName();
                String value = "kdr".equals(stat) ? String.format("%.2f", lb.get(rank - 1).getDoubleValue()) : String.valueOf(lb.get(rank - 1).getValue());
                v.put("top" + rank + "_name", name != null ? name : "---");
                v.put("top" + rank + "_" + stat, value);
                if (rank == 1 && "wins".equals(stat)) {
                    v.put("top" + rank + "_wins", value);
                }
            } else {
                v.put("top" + rank + "_name", "---");
                v.put("top" + rank + "_" + stat, "0");
            }
        }
        catch (Throwable t) {
            v.put("top" + rank + "_name", "---");
            v.put("top" + rank + "_" + stat, "0");
        }
    }

    private static String formatKD(int kills, int deaths) {
        if (deaths == 0) {
            return kills > 0 ? kills + ".00" : "0.00";
        }
        return KD_FORMAT.format((double)kills / (double)deaths);
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%02d:%02d", minutes, seconds %= 60L);
    }
}

