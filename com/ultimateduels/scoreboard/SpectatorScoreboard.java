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

public class SpectatorScoreboard {
    private final UltimateDuels plugin;

    public SpectatorScoreboard(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void update(ScoreboardBuilder board, Player spectator, DuelMatch match) {
        String title = this.plugin.getConfigManager().getConfig().getString("scoreboard.spectator.title", "&7&lSPECTATING");
        board.updateTitle(title);
        List configLines = this.plugin.getConfigManager().getConfig().getStringList("scoreboard.spectator.lines");
        ArrayList<String> lines = new ArrayList<String>();
        if (configLines.isEmpty()) {
            this.buildDefaultSpectatorScoreboard(lines, spectator, match);
        } else {
            for (String line : configLines) {
                lines.add(this.replacePlaceholders(line, spectator, match));
            }
        }
        board.updateLines(lines);
    }

    private void buildDefaultSpectatorScoreboard(List<String> lines, Player spectator, DuelMatch match) {
        lines.add("&7&m                    ");
        lines.add("");
        lines.add("&fKit: &a" + match.getKitName());
        lines.add("&fArena: &e" + match.getArena().getName());
        lines.add("");
        String stateDisplay = this.getStateDisplay(match.getState());
        lines.add("&7State: " + stateDisplay);
        lines.add("");
        if (match.isTeamMatch()) {
            String health;
            String status;
            boolean alive;
            Player p;
            lines.add("&a&lTeam 1:");
            for (DuelParticipant participant : match.getTeam1()) {
                p = Bukkit.getPlayer((UUID)participant.getUuid());
                if (p == null) continue;
                alive = participant.isAlive();
                status = alive ? "&a\u2764" : "&c\u2717";
                health = alive ? " " + this.formatHealth(p) : "";
                lines.add("  " + status + " &f" + p.getName() + health);
            }
            lines.add("");
            lines.add("&c&lTeam 2:");
            for (DuelParticipant participant : match.getTeam2()) {
                p = Bukkit.getPlayer((UUID)participant.getUuid());
                if (p == null) continue;
                alive = participant.isAlive();
                status = alive ? "&c\u2764" : "&7\u2717";
                health = alive ? " " + this.formatHealth(p) : "";
                lines.add("  " + status + " &f" + p.getName() + health);
            }
        } else {
            List<DuelParticipant> allParticipants = match.getAllParticipants();
            if (allParticipants.size() >= 2) {
                DuelParticipant p1 = allParticipants.get(0);
                DuelParticipant p2 = allParticipants.get(1);
                Player player1 = Bukkit.getPlayer((UUID)p1.getUuid());
                Player player2 = Bukkit.getPlayer((UUID)p2.getUuid());
                lines.add("&a&lPlayer 1:");
                if (player1 != null && player1.isOnline()) {
                    lines.add("  &f" + player1.getName());
                    lines.add("  &7Health: " + this.formatHealth(player1));
                    lines.add("  &7Ping: &f" + player1.getPing() + "ms");
                } else {
                    lines.add("  &cDisconnected");
                }
                lines.add("");
                lines.add("&c&lPlayer 2:");
                if (player2 != null && player2.isOnline()) {
                    lines.add("  &f" + player2.getName());
                    lines.add("  &7Health: " + this.formatHealth(player2));
                    lines.add("  &7Ping: &f" + player2.getPing() + "ms");
                } else {
                    lines.add("  &cDisconnected");
                }
            }
        }
        lines.add("");
        if (match.getTotalRounds() > 1) {
            int team1Score = match.getTeamScore(1);
            int team2Score = match.getTeamScore(2);
            lines.add("&e&lScore: &a" + team1Score + " &7- &c" + team2Score);
            lines.add("&7Round: &f" + match.getCurrentRound() + "/" + match.getTotalRounds());
            lines.add("");
        }
        long duration = match.getMatchDuration();
        lines.add("&7Duration: &f" + this.formatDuration(duration));
        lines.add("");
        int spectatorCount = this.getSpectatorCount(match);
        lines.add("&7Spectators: &f" + spectatorCount);
        lines.add("");
        lines.add("&8[Compass] Teleport");
        lines.add("&8[Red Dye] Leave");
        lines.add("");
        lines.add("&7&m                    ");
        lines.add("&e" + this.plugin.getConfigManager().getWebsiteUrl());
    }

    private String replacePlaceholders(String line, Player spectator, DuelMatch match) {
        List<DuelParticipant> participants = match.getAllParticipants();
        String player1Name = "N/A";
        String player2Name = "N/A";
        String player1Health = "N/A";
        String player2Health = "N/A";
        int player1Ping = 0;
        int player2Ping = 0;
        if (!participants.isEmpty()) {
            Player p2;
            Player p1;
            if (participants.size() >= 1 && (p1 = Bukkit.getPlayer((UUID)participants.get(0).getUuid())) != null) {
                player1Name = p1.getName();
                player1Health = this.formatHealth(p1);
                player1Ping = p1.getPing();
            }
            if (participants.size() >= 2 && (p2 = Bukkit.getPlayer((UUID)participants.get(1).getUuid())) != null) {
                player2Name = p2.getName();
                player2Health = this.formatHealth(p2);
                player2Ping = p2.getPing();
            }
        }
        String processed = line.replace("{spectator}", spectator.getName()).replace("{kit}", match.getKitName()).replace("{arena}", match.getArena().getName()).replace("{state}", this.getStateDisplay(match.getState())).replace("{player1}", player1Name).replace("{player2}", player2Name).replace("{player1_health}", player1Health).replace("{player2_health}", player2Health).replace("{player1_ping}", String.valueOf(player1Ping)).replace("{player2_ping}", String.valueOf(player2Ping)).replace("{team1_score}", String.valueOf(match.getTeamScore(1))).replace("{team2_score}", String.valueOf(match.getTeamScore(2))).replace("{round}", String.valueOf(match.getCurrentRound())).replace("{total_rounds}", String.valueOf(match.getTotalRounds())).replace("{duration}", this.formatDuration(match.getMatchDuration())).replace("{spectator_count}", String.valueOf(this.getSpectatorCount(match))).replace("{website}", this.plugin.getConfigManager().getWebsiteUrl());
        return processed;
    }

    private int getSpectatorCount(DuelMatch match) {
        if (this.plugin.getDuelManager() != null) {
            return this.plugin.getDuelManager().getMatchSpectators(match.getMatchId()).size();
        }
        return 0;
    }

    private String getStateDisplay(MatchState state) {
        if (state == null) {
            return "&7? Unknown";
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
            default -> "&7? " + state.name();
        };
    }

    private String formatHealth(Player player) {
        int maxHealth;
        int health = (int)Math.ceil(player.getHealth());
        double percentage = (double)health / (double)(maxHealth = (int)player.getMaxHealth());
        String color = percentage > 0.6 ? "&a" : (percentage > 0.3 ? "&e" : "&c");
        return color + health + "&7/&f" + maxHealth + " &c\u2764";
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%02d:%02d", minutes, seconds %= 60L);
    }
}

