/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.utils;

import com.ultimateduels.utils.ColorUtils;
import com.ultimateduels.utils.PlayerUtils;
import com.ultimateduels.utils.SoundUtils;
import com.ultimateduels.utils.TimeUtils;
import java.time.Duration;
import java.util.Collection;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TitleUtils {
    private static final Duration DEFAULT_FADE_IN = Duration.ofMillis(250L);
    private static final Duration DEFAULT_STAY = Duration.ofSeconds(2L);
    private static final Duration DEFAULT_FADE_OUT = Duration.ofMillis(500L);
    public static final Title.Times TIMES_INSTANT = Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofSeconds(1L), (Duration)Duration.ofMillis(250L));
    public static final Title.Times TIMES_SHORT = Title.Times.times((Duration)Duration.ofMillis(100L), (Duration)Duration.ofSeconds(1L), (Duration)Duration.ofMillis(250L));
    public static final Title.Times TIMES_NORMAL = Title.Times.times((Duration)Duration.ofMillis(250L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L));
    public static final Title.Times TIMES_LONG = Title.Times.times((Duration)Duration.ofMillis(500L), (Duration)Duration.ofSeconds(4L), (Duration)Duration.ofMillis(1000L));
    public static final Title.Times TIMES_COUNTDOWN = Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(900L), (Duration)Duration.ofMillis(100L));

    private TitleUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void sendTitle(@NotNull Player player, @NotNull String title) {
        TitleUtils.sendTitle(player, title, "", TIMES_NORMAL);
    }

    public static void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle) {
        TitleUtils.sendTitle(player, title, subtitle, TIMES_NORMAL);
    }

    public static void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle, @NotNull Title.Times times) {
        Component titleComponent = ColorUtils.parse(title);
        Component subtitleComponent = ColorUtils.parse(subtitle);
        Title adventureTitle = Title.title((Component)titleComponent, (Component)subtitleComponent, (Title.Times)times);
        player.showTitle(adventureTitle);
    }

    public static void sendTitle(@NotNull Player player, @NotNull Component title, @NotNull Component subtitle, @NotNull Title.Times times) {
        player.showTitle(Title.title((Component)title, (Component)subtitle, (Title.Times)times));
    }

    public static void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Title.Times times = Title.Times.times((Duration)Duration.ofMillis((long)fadeInTicks * 50L), (Duration)Duration.ofMillis((long)stayTicks * 50L), (Duration)Duration.ofMillis((long)fadeOutTicks * 50L));
        TitleUtils.sendTitle(player, title, subtitle, times);
    }

    public static void sendSubtitle(@NotNull Player player, @NotNull String subtitle) {
        TitleUtils.sendTitle(player, "", subtitle, TIMES_NORMAL);
    }

    public static void sendSubtitle(@NotNull Player player, @NotNull String subtitle, @NotNull Title.Times times) {
        TitleUtils.sendTitle(player, "", subtitle, times);
    }

    public static void clearTitle(@NotNull Player player) {
        player.clearTitle();
    }

    public static void resetTitleTimes(@NotNull Player player) {
        player.resetTitle();
    }

    public static void sendTitle(@NotNull Collection<Player> players, @NotNull String title, @NotNull String subtitle, @NotNull Title.Times times) {
        for (Player player : players) {
            TitleUtils.sendTitle(player, title, subtitle, times);
        }
    }

    public static void sendTitleToUuids(@NotNull Collection<UUID> uuids, @NotNull String title, @NotNull String subtitle, @NotNull Title.Times times) {
        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            TitleUtils.sendTitle(player, title, subtitle, times);
        }
    }

    public static void broadcastTitle(@NotNull String title, @NotNull String subtitle) {
        TitleUtils.broadcastTitle(title, subtitle, TIMES_NORMAL);
    }

    public static void broadcastTitle(@NotNull String title, @NotNull String subtitle, @NotNull Title.Times times) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            TitleUtils.sendTitle(player, title, subtitle, times);
        }
    }

    public static void sendCountdown(@NotNull Player player, int number) {
        TitleUtils.sendTitle(player, (switch (number) {
            case 3 -> "&c";
            case 2 -> "&e";
            case 1 -> "&a";
            default -> "&f";
        }) + "&l" + number, "", TIMES_COUNTDOWN);
        SoundUtils.playCountdownTick(player);
    }

    public static void sendGo(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&a&lGO!", "&7Fight!", TIMES_SHORT);
        SoundUtils.playCountdownGo(player);
    }

    public static void sendFight(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&6&l\u2694 FIGHT! \u2694", "", TIMES_SHORT);
        SoundUtils.playMatchStart(player);
    }

    public static void sendVictory(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&a&lVICTORY!", "&7Congratulations!", TIMES_LONG);
        SoundUtils.playVictory(player);
    }

    public static void sendVictory(@NotNull Player player, @NotNull String opponentName) {
        TitleUtils.sendTitle(player, "&a&lVICTORY!", "&7You defeated &f" + opponentName, TIMES_LONG);
        SoundUtils.playVictory(player);
    }

    public static void sendDefeat(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&c&lDEFEAT!", "&7Better luck next time!", TIMES_LONG);
        SoundUtils.playDefeat(player);
    }

    public static void sendDefeat(@NotNull Player player, @NotNull String winnerName) {
        TitleUtils.sendTitle(player, "&c&lDEFEAT!", "&7You lost to &f" + winnerName, TIMES_LONG);
        SoundUtils.playDefeat(player);
    }

    public static void sendDraw(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&e&lDRAW!", "&7Neither player won", TIMES_LONG);
    }

    public static void sendRoundWin(@NotNull Player player, int currentScore, int opponentScore) {
        TitleUtils.sendTitle(player, "&a&lROUND WON!", "&7Score: &a" + currentScore + " &7- &c" + opponentScore, TIMES_NORMAL);
        SoundUtils.playRoundWin(player);
    }

    public static void sendRoundLoss(@NotNull Player player, int currentScore, int opponentScore) {
        TitleUtils.sendTitle(player, "&c&lROUND LOST!", "&7Score: &a" + currentScore + " &7- &c" + opponentScore, TIMES_NORMAL);
        SoundUtils.playRoundLose(player);
    }

    public static void sendNewRound(@NotNull Player player, int roundNumber, int totalRounds) {
        TitleUtils.sendTitle(player, "&6&lROUND " + roundNumber, "&7of " + totalRounds, TIMES_SHORT);
    }

    public static void sendMatchPoint(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&6&lMATCH POINT!", "&eWin this round to win the match!", TIMES_NORMAL);
    }

    public static void sendMatchFound(@NotNull Player player, @NotNull String opponentName) {
        TitleUtils.sendTitle(player, "&a&lMATCH FOUND!", "&7Opponent: &f" + opponentName, TIMES_NORMAL);
        SoundUtils.playMatchFound(player);
    }

    public static void sendTeleporting(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&b&lTELEPORTING...", "&7Prepare for battle!", TIMES_SHORT);
    }

    public static void sendWaiting(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&eWaiting...", "&7Opponent connecting", TIMES_SHORT);
    }

    public static void sendOpponentDisconnected(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&c&lOPPONENT LEFT!", "&7Match cancelled", TIMES_NORMAL);
    }

    public static void sendFFAJoin(@NotNull Player player, @NotNull String arenaName) {
        TitleUtils.sendTitle(player, "&6&lFFA", "&7" + arenaName + " &8- &cKill to survive!", TIMES_NORMAL);
    }

    public static void sendFFADeath(@NotNull Player player, @Nullable String killerName) {
        if (killerName != null) {
            TitleUtils.sendTitle(player, "&c&lYOU DIED!", "&7Killed by &f" + killerName, TIMES_SHORT);
        } else {
            TitleUtils.sendTitle(player, "&c&lYOU DIED!", "&7Returning to lobby...", TIMES_SHORT);
        }
    }

    public static void sendKillStreak(@NotNull Player player, int streak) {
        Object message = switch (streak) {
            case 3 -> "&e&lTRIPLE KILL!";
            case 5 -> "&6&lKILLING SPREE!";
            case 10 -> "&c&lUNSTOPPABLE!";
            case 15 -> "&4&lGODLIKE!";
            case 20 -> "&5&lLEGENDARY!";
            default -> "&a&l" + streak + " KILL STREAK!";
        };
        TitleUtils.sendTitle(player, (String)message, "", TIMES_SHORT);
        SoundUtils.playStreak(player, streak);
    }

    public static void sendPartyInvite(@NotNull Player player, @NotNull String inviterName) {
        TitleUtils.sendTitle(player, "&d&lPARTY INVITE", "&7From &f" + inviterName + " &7(/party accept)", TIMES_NORMAL);
    }

    public static void sendPartyDisbanded(@NotNull Player player) {
        TitleUtils.sendTitle(player, "&cParty Disbanded", "&7The party leader left", TIMES_SHORT);
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String message) {
        player.sendActionBar(ColorUtils.parse(message));
    }

    public static void sendActionBar(@NotNull Player player, @NotNull Component message) {
        player.sendActionBar(message);
    }

    public static void sendActionBar(@NotNull Collection<Player> players, @NotNull String message) {
        Component component = ColorUtils.parse(message);
        for (Player player : players) {
            player.sendActionBar(component);
        }
    }

    public static void broadcastActionBar(@NotNull String message) {
        Component component = ColorUtils.parse(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(component);
        }
    }

    public static void sendHealthActionBar(@NotNull Player player, @NotNull Player target) {
        double maxHealth;
        double health = target.getHealth();
        int percentage = (int)(health / (maxHealth = PlayerUtils.getMaxHealth(target)) * 100.0);
        String color = percentage > 66 ? "&a" : (percentage > 33 ? "&e" : "&c");
        String healthBar = TitleUtils.createHealthBar(health, maxHealth, 20);
        TitleUtils.sendActionBar(player, color + target.getName() + " " + healthBar + " " + String.format("%.1f", health) + "\u2764");
    }

    @NotNull
    private static String createHealthBar(double current, double max, int bars) {
        int filled = (int)Math.ceil(current / max * (double)bars);
        int empty = bars - filled;
        return "&a" + "\u258c".repeat(Math.max(0, filled)) + "&8" + "\u258c".repeat(Math.max(0, empty));
    }

    public static void sendComboActionBar(@NotNull Player player, int combo) {
        String color = combo >= 10 ? "&c&l" : (combo >= 5 ? "&6" : "&e");
        TitleUtils.sendActionBar(player, color + "\u2694 " + combo + " Hit Combo! \u2694");
    }

    public static void sendTimerActionBar(@NotNull Player player, long secondsRemaining) {
        String formatted = TimeUtils.formatMatchTime(secondsRemaining);
        String color = secondsRemaining <= 10L ? "&c" : "&f";
        TitleUtils.sendActionBar(player, color + "\u23f1 " + formatted);
    }

    public static void sendCountdownSequence(@NotNull Player player, @NotNull Plugin plugin, @NotNull Runnable onComplete) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                TitleUtils.sendCountdown(player, 3);
            }
        }, 0L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                TitleUtils.sendCountdown(player, 2);
            }
        }, 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                TitleUtils.sendCountdown(player, 1);
            }
        }, 40L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                TitleUtils.sendGo(player);
                onComplete.run();
            }
        }, 60L);
    }

    public static void sendCountdownSequence(@NotNull Collection<Player> players, @NotNull Plugin plugin, @NotNull Runnable onComplete) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : players) {
                if (!player.isOnline()) continue;
                TitleUtils.sendCountdown(player, 3);
            }
        }, 0L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : players) {
                if (!player.isOnline()) continue;
                TitleUtils.sendCountdown(player, 2);
            }
        }, 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : players) {
                if (!player.isOnline()) continue;
                TitleUtils.sendCountdown(player, 1);
            }
        }, 40L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : players) {
                if (!player.isOnline()) continue;
                TitleUtils.sendGo(player);
            }
            onComplete.run();
        }, 60L);
    }

    public static void sendRepeatingActionBar(@NotNull Player player, @NotNull String message, @NotNull Plugin plugin, int durationTicks, int intervalTicks) {
        Component component = ColorUtils.parse(message);
        for (int tick = 0; tick <= durationTicks; tick += intervalTicks) {
            int currentTick = tick;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendActionBar(component);
                }
            }, (long)currentTick);
        }
    }

    public static void sendPulsingTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle, @NotNull Plugin plugin, int pulseCount) {
        Title.Times pulseTime = Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofMillis(600L), (Duration)Duration.ofMillis(200L));
        for (int i = 0; i < pulseCount; ++i) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    TitleUtils.sendTitle(player, title, subtitle, pulseTime);
                }
            }, (long)i * 20L);
        }
    }

    @NotNull
    public static Title.Times times(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        return Title.Times.times((Duration)Duration.ofMillis((long)fadeInTicks * 50L), (Duration)Duration.ofMillis((long)stayTicks * 50L), (Duration)Duration.ofMillis((long)fadeOutTicks * 50L));
    }

    @NotNull
    public static Title.Times timesMs(long fadeInMs, long stayMs, long fadeOutMs) {
        return Title.Times.times((Duration)Duration.ofMillis(fadeInMs), (Duration)Duration.ofMillis(stayMs), (Duration)Duration.ofMillis(fadeOutMs));
    }

    @NotNull
    public static Title.Times timesSeconds(double fadeIn, double stay, double fadeOut) {
        return Title.Times.times((Duration)Duration.ofMillis((long)(fadeIn * 1000.0)), (Duration)Duration.ofMillis((long)(stay * 1000.0)), (Duration)Duration.ofMillis((long)(fadeOut * 1000.0)));
    }
}

