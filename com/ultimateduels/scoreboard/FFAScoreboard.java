/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.scoreboard;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.ffa.model.FFAPlayerData;
import com.ultimateduels.scoreboard.ScoreboardBuilder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FFAScoreboard {
    private final UltimateDuels plugin;
    private final DecimalFormat kdFormat = new DecimalFormat("#.##");

    public FFAScoreboard(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void update(ScoreboardBuilder board, Player player, FFAArenaInstance arena) {
        UUID uuid = player.getUniqueId();
        String title = this.plugin.getConfigManager().getConfig().getString("scoreboard.ffa.title", "&c&lFFA");
        board.updateTitle(title);
        List configLines = this.plugin.getConfigManager().getConfig().getStringList("scoreboard.ffa.lines");
        FFAPlayerData data = this.plugin.getFFAManager().getPlayerData(uuid);
        ArrayList<String> lines = new ArrayList<String>();
        if (configLines.isEmpty()) {
            this.buildDefaultFFAScoreboard(lines, player, arena, data);
        } else {
            for (String line : configLines) {
                lines.add(this.replacePlaceholders(line, player, arena, data));
            }
        }
        board.updateLines(lines);
    }

    private void buildDefaultFFAScoreboard(List<String> lines, Player player, FFAArenaInstance arena, FFAPlayerData data) {
        UUID uuid = player.getUniqueId();
        lines.add("&7&m                    ");
        lines.add("");
        lines.add("&fKit: &a" + arena.getKitName());
        lines.add("&fPlayers: &e" + arena.getPlayerCount());
        lines.add("");
        lines.add("&e&lSession Stats:");
        if (data != null) {
            lines.add("  &7Kills: &a" + data.getKills());
            lines.add("  &7Deaths: &c" + data.getDeaths());
            lines.add("  &7K/D: &f" + this.getKDRatio(data.getKills(), data.getDeaths()));
        } else {
            lines.add("  &7Kills: &a0");
            lines.add("  &7Deaths: &c0");
            lines.add("  &7K/D: &f0.00");
        }
        lines.add("");
        int streak = data != null ? data.getKillstreak() : 0;
        String streakColor = this.getStreakColor(streak);
        lines.add("&7Current Streak: " + streakColor + streak);
        if (streak >= 5) {
            lines.add(this.getStreakBonus(streak));
        }
        lines.add("");
        if (this.plugin.getFFAManager().hasSpawnProtection(uuid)) {
            lines.add("&a&l\u2726 PROTECTED \u2726");
            lines.add("");
        }
        lines.add("&a&lYou:");
        lines.add("  &7Health: " + this.formatHealth(player));
        lines.add("  &7Ping: &f" + player.getPing() + "ms");
        lines.add("");
        if (data != null) {
            lines.add("&7Time: &f" + this.formatDuration(data.getSessionDuration()));
        }
        lines.add("");
        lines.add("&7&m                    ");
        lines.add("&e" + this.plugin.getConfigManager().getWebsiteUrl());
    }

    private String replacePlaceholders(String line, Player player, FFAArenaInstance arena, FFAPlayerData data) {
        UUID uuid = player.getUniqueId();
        int kills = data != null ? data.getKills() : 0;
        int deaths = data != null ? data.getDeaths() : 0;
        int streak = data != null ? data.getKillstreak() : 0;
        long sessionTime = data != null ? data.getSessionDuration() : 0L;
        String processed = line.replace("{player}", player.getName()).replace("{arena}", arena.getArenaName()).replace("{kit}", arena.getKitName()).replace("{players}", String.valueOf(arena.getPlayerCount())).replace("{kills}", String.valueOf(kills)).replace("{deaths}", String.valueOf(deaths)).replace("{kd}", this.getKDRatio(kills, deaths)).replace("{streak}", String.valueOf(streak)).replace("{streak_color}", this.getStreakColor(streak)).replace("{streak_bonus}", this.getStreakBonus(streak)).replace("{health}", this.formatHealth(player)).replace("{ping}", String.valueOf(player.getPing())).replace("{session_time}", this.formatDuration(sessionTime)).replace("{protected}", this.plugin.getFFAManager().hasSpawnProtection(uuid) ? "&a\u2726 PROTECTED \u2726" : "").replace("{website}", this.plugin.getConfigManager().getWebsiteUrl());
        return processed;
    }

    private String getKDRatio(int kills, int deaths) {
        if (deaths == 0) {
            return kills > 0 ? kills + ".00" : "0.00";
        }
        double kd = (double)kills / (double)deaths;
        return this.kdFormat.format(kd);
    }

    private String getStreakColor(int streak) {
        if (streak >= 15) {
            return "&4&l";
        }
        if (streak >= 10) {
            return "&c&l";
        }
        if (streak >= 5) {
            return "&6";
        }
        if (streak >= 3) {
            return "&e";
        }
        return "&f";
    }

    private String getStreakBonus(int streak) {
        if (streak >= 15) {
            return "&4&l\u2605 GODLIKE \u2605";
        }
        if (streak >= 10) {
            return "&c&l\u2605 UNSTOPPABLE \u2605";
        }
        if (streak >= 5) {
            return "&6\u2605 ON FIRE \u2605";
        }
        return "";
    }

    private String formatHealth(Player player) {
        int maxHealth;
        int health = (int)Math.ceil(player.getHealth());
        double percentage = (double)health / (double)(maxHealth = (int)player.getMaxHealth());
        String color = percentage > 0.6 ? "&a" : (percentage > 0.3 ? "&e" : "&c");
        int bars = 10;
        int filledBars = (int)(percentage * (double)bars);
        StringBuilder healthBar = new StringBuilder();
        healthBar.append(color);
        for (int i = 0; i < bars; ++i) {
            if (i < filledBars) {
                healthBar.append("\u2588");
                continue;
            }
            healthBar.append("&7\u2591");
        }
        return String.valueOf(healthBar) + " " + color + health + "&7/" + maxHealth;
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        return String.format("%02d:%02d", minutes, seconds %= 60L);
    }
}

