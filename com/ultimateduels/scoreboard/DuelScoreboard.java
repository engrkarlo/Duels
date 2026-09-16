/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.scoreboard;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.scoreboard.ScoreboardBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelScoreboard {
    private final UltimateDuels plugin;

    public DuelScoreboard(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void update(ScoreboardBuilder board, Player player, DuelMatch match) {
        String title = this.plugin.getConfigManager().getConfig().getString("scoreboard.duel.title", "&c&lDUEL");
        if (title.equals("&c&lDUEL") && match.isTeamMatch()) {
            title = "&c&lPARTY DUEL";
        }
        board.updateTitle(title);
        List configLines = this.plugin.getConfigManager().getConfig().getStringList("scoreboard.duel.lines");
        ArrayList<String> lines = new ArrayList<String>();
        if (configLines.isEmpty()) {
            this.buildDefaultDuelScoreboard(lines, player, match);
        } else {
            for (String line : configLines) {
                lines.add(this.replacePlaceholders(line, player, match));
            }
        }
        board.updateLines(lines);
    }

    private void buildDefaultDuelScoreboard(List<String> lines, Player player, DuelMatch match) {
        lines.add("&7&m                    ");
        lines.add("");
        lines.add("&fKit: &a" + match.getKitName());
        lines.add("&fArena: &e" + match.getArena().getName());
        lines.add("");
        DuelParticipant participant = match.getParticipant(player.getUniqueId());
        if (participant == null) {
            return;
        }
        int playerTeamId = participant.getTeamId();
        int opponentTeamId = playerTeamId == 1 ? 2 : 1;
        String stateDisplay = this.getStateDisplay(match.getState());
        lines.add("&7State: " + stateDisplay);
        lines.add("");
        if (match.isTeamMatch()) {
            String status;
            boolean alive;
            lines.add(this.plugin.colorize("&a&lYour Team:"));
            for (DuelParticipant teamMate : match.getTeamParticipants(playerTeamId)) {
                Player teamMatePlayer = Bukkit.getPlayer((UUID)teamMate.getUuid());
                if (teamMatePlayer == null) continue;
                alive = teamMate.isAlive();
                status = alive ? "&a\u2764" : "&c\u2717";
                String healthDisplay = alive ? this.formatHealth(teamMatePlayer) : "";
                lines.add("  " + status + " &f" + teamMatePlayer.getName() + " " + healthDisplay);
            }
            lines.add("");
            lines.add(this.plugin.colorize("&c&lEnemy Team:"));
            for (DuelParticipant enemy : match.getTeamParticipants(opponentTeamId)) {
                Player enemyPlayer = Bukkit.getPlayer((UUID)enemy.getUuid());
                if (enemyPlayer == null) continue;
                alive = enemy.isAlive();
                status = alive ? "&c\u2764" : "&7\u2717";
                lines.add("  " + status + " &f" + enemyPlayer.getName());
            }
        } else {
            lines.add(this.plugin.colorize("&a&lYou:"));
            lines.add("  &f" + player.getName());
            lines.add("  &7Ping: &f" + this.getPing(player) + "ms");
            lines.add("");
            UUID opponentId = match.getOpponent(player.getUniqueId());
            Player opponent = opponentId != null ? Bukkit.getPlayer((UUID)opponentId) : null;
            lines.add(this.plugin.colorize("&c&lOpponent:"));
            if (opponent != null && opponent.isOnline()) {
                lines.add("  &f" + opponent.getName());
                lines.add("  &7Ping: &f" + this.getPing(opponent) + "ms");
            } else {
                lines.add("  &cDisconnected");
            }
        }
        lines.add("");
        if (match.getTotalRounds() > 1) {
            int playerWins = match.getTeamScore(playerTeamId);
            int opponentWins = match.getTeamScore(opponentTeamId);
            lines.add("&e&lRound: &f" + match.getCurrentRound());
            lines.add("");
            lines.add(this.plugin.colorize("&e&lWins:"));
            lines.add("  &aYou: &f" + playerWins);
            lines.add("  &cThem: &f" + opponentWins);
            lines.add("");
        }
        long duration = match.getMatchDuration();
        lines.add("&7Duration: &f" + this.formatDuration(duration));
        lines.add("");
        lines.add("&7&m                    ");
        lines.add("&e" + this.plugin.getConfigManager().getWebsiteUrl());
    }

    private int getWinsNeeded(int totalRounds) {
        return totalRounds / 2 + 1;
    }

    private String replacePlaceholders(String line, Player player, DuelMatch match) {
        DuelParticipant participant = match.getParticipant(player.getUniqueId());
        if (participant == null) {
            return line;
        }
        int playerTeamId = participant.getTeamId();
        int opponentTeamId = playerTeamId == 1 ? 2 : 1;
        UUID opponentId = match.getOpponent(player.getUniqueId());
        Player opponent = opponentId != null ? Bukkit.getPlayer((UUID)opponentId) : null;
        String opponentName = opponent != null ? opponent.getName() : "Unknown";
        int opponentPing = opponent != null ? opponent.getPing() : 0;
        int playerWins = match.getTeamScore(playerTeamId);
        int opponentWins = match.getTeamScore(opponentTeamId);
        return line.replace("{player}", player.getName()).replace("{player_name}", player.getName()).replace("{opponent}", opponentName).replace("{opponent_name}", opponentName).replace("{kit}", match.getKitName()).replace("{kit_name}", match.getKitName()).replace("{arena}", match.getArena().getName()).replace("{arena_name}", match.getArena().getName()).replace("{ping}", String.valueOf(this.getPing(player))).replace("{player_ping}", String.valueOf(this.getPing(player))).replace("{opponent_ping}", String.valueOf(opponentPing)).replace("{duration}", this.formatDuration(match.getMatchDuration())).replace("{state}", this.getStateDisplay(match.getState())).replace("{round}", String.valueOf(match.getCurrentRound())).replace("{current_round}", String.valueOf(match.getCurrentRound())).replace("{total_rounds}", String.valueOf(match.getTotalRounds())).replace("{max_rounds}", String.valueOf(match.getTotalRounds())).replace("{round_display}", "Round " + match.getCurrentRound()).replace("{player_score}", String.valueOf(playerWins)).replace("{opponent_score}", String.valueOf(opponentWins)).replace("{player_wins}", String.valueOf(playerWins)).replace("{opponent_wins}", String.valueOf(opponentWins)).replace("{wins_display}", "You: " + playerWins + " - Them: " + opponentWins).replace("{score}", playerWins + " - " + opponentWins).replace("{health}", this.formatHealth(player)).replace("{opponent_health}", opponent != null ? this.formatHealth(opponent) : "N/A").replace("{website}", this.plugin.getConfigManager().getWebsiteUrl());
    }

    private String getStateDisplay(MatchState state) {
        if (state == null) {
            return "&7Unknown";
        }
        return switch (state) {
            case MatchState.PENDING -> "&7\u23f3 Pending";
            case MatchState.STARTING -> "&e\u23f3 Starting";
            case MatchState.IN_PROGRESS -> "&c\u2694 Fighting";
            case MatchState.ROUND_ENDING -> "&6\u23f8 Round End";
            case MatchState.RESETTING -> "&d\u27f3 Resetting";
            case MatchState.ENDING -> "&6\u23f8 Ending";
            case MatchState.COMPLETED -> "&a\u2713 Completed";
            case MatchState.CANCELLED -> "&c\u2717 Cancelled";
            default -> "&7" + state.name();
        };
    }

    private int getPing(Player player) {
        return player.getPing();
    }

    private String formatHealth(Player player) {
        int maxHealth;
        int health = (int)Math.ceil(player.getHealth());
        double percentage = (double)health / (double)(maxHealth = (int)player.getMaxHealth());
        String color = percentage > 0.6 ? "&a" : (percentage > 0.3 ? "&e" : "&c");
        return color + health + "\u2764";
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%02d:%02d", minutes, seconds %= 60L);
    }
}

