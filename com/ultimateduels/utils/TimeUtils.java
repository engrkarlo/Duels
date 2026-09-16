/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class TimeUtils {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATETIME_SHORT = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?", 2);
    private static final String[] TIME_UNITS = new String[]{"d", "h", "m", "s"};
    private static final long[] TIME_DIVISORS = new long[]{86400L, 3600L, 60L, 1L};

    private TimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    @NotNull
    public static String formatDuration(long seconds) {
        if (seconds <= 0L) {
            return "0s";
        }
        StringBuilder result = new StringBuilder();
        long days = seconds / 86400L;
        long hours = (seconds %= 86400L) / 3600L;
        long minutes = (seconds %= 3600L) / 60L;
        seconds %= 60L;
        if (days > 0L) {
            result.append(days).append("d ");
        }
        if (hours > 0L) {
            result.append(hours).append("h ");
        }
        if (minutes > 0L) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0L || result.length() == 0) {
            result.append(seconds).append("s");
        }
        return result.toString().trim();
    }

    @NotNull
    public static String formatDurationMillis(long millis) {
        return TimeUtils.formatDuration(millis / 1000L);
    }

    @NotNull
    public static String formatDuration(@NotNull Duration duration) {
        return TimeUtils.formatDuration(duration.getSeconds());
    }

    @NotNull
    public static String formatDurationFull(long seconds) {
        if (seconds <= 0L) {
            return "0 seconds";
        }
        StringBuilder result = new StringBuilder();
        long days = seconds / 86400L;
        long hours = (seconds %= 86400L) / 3600L;
        long minutes = (seconds %= 3600L) / 60L;
        seconds %= 60L;
        if (days > 0L) {
            result.append(days).append(days == 1L ? " day " : " days ");
        }
        if (hours > 0L) {
            result.append(hours).append(hours == 1L ? " hour " : " hours ");
        }
        if (minutes > 0L) {
            result.append(minutes).append(minutes == 1L ? " minute " : " minutes ");
        }
        if (seconds > 0L || result.length() == 0) {
            result.append(seconds).append(seconds == 1L ? " second" : " seconds");
        }
        return result.toString().trim();
    }

    @NotNull
    public static String formatDurationCompact(long seconds) {
        if (seconds < 0L) {
            seconds = 0L;
        }
        long hours = seconds / 3600L;
        long minutes = (seconds %= 3600L) / 60L;
        seconds %= 60L;
        if (hours > 0L) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    @NotNull
    public static String formatCountdown(long seconds) {
        if (seconds <= 0L) {
            return "0";
        }
        if (seconds < 60L) {
            return String.valueOf(seconds);
        }
        long minutes = seconds / 60L;
        return String.format("%d:%02d", minutes, seconds %= 60L);
    }

    @NotNull
    public static String formatMatchTime(long seconds) {
        if (seconds < 0L) {
            seconds = 0L;
        }
        long hours = seconds / 3600L;
        long minutes = seconds % 3600L / 60L;
        long secs = seconds % 60L;
        if (hours > 0L) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }

    public static long parseDuration(@NotNull String input) {
        if (input.isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(input);
        }
        catch (NumberFormatException numberFormatException) {
            Matcher matcher = DURATION_PATTERN.matcher(input.toLowerCase().replace(" ", ""));
            if (!matcher.matches()) {
                return -1L;
            }
            long total = 0L;
            boolean hasValue = false;
            if (matcher.group(1) != null) {
                total += Long.parseLong(matcher.group(1)) * 86400L;
                hasValue = true;
            }
            if (matcher.group(2) != null) {
                total += Long.parseLong(matcher.group(2)) * 3600L;
                hasValue = true;
            }
            if (matcher.group(3) != null) {
                total += Long.parseLong(matcher.group(3)) * 60L;
                hasValue = true;
            }
            if (matcher.group(4) != null) {
                total += Long.parseLong(matcher.group(4));
                hasValue = true;
            }
            return hasValue ? total : -1L;
        }
    }

    @NotNull
    public static Duration parseDurationObject(@NotNull String input) {
        long seconds = TimeUtils.parseDuration(input);
        return seconds > 0L ? Duration.ofSeconds(seconds) : Duration.ZERO;
    }

    @NotNull
    public static String getTimeAgo(long timestamp) {
        long years;
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 0L) {
            return "in the future";
        }
        long seconds = diff / 1000L;
        if (seconds < 5L) {
            return "just now";
        }
        if (seconds < 60L) {
            return seconds + " seconds ago";
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return minutes + (minutes == 1L ? " minute ago" : " minutes ago");
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + (hours == 1L ? " hour ago" : " hours ago");
        }
        long days = hours / 24L;
        if (days < 7L) {
            return days + (days == 1L ? " day ago" : " days ago");
        }
        long weeks = days / 7L;
        if (weeks < 4L) {
            return weeks + (weeks == 1L ? " week ago" : " weeks ago");
        }
        long months = days / 30L;
        if (months < 12L) {
            return months + (months == 1L ? " month ago" : " months ago");
        }
        return years + ((years = days / 365L) == 1L ? " year ago" : " years ago");
    }

    @NotNull
    public static String getTimeAgo(@NotNull Instant instant) {
        return TimeUtils.getTimeAgo(instant.toEpochMilli());
    }

    @NotNull
    public static String getTimeAgoCompact(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 0L) {
            return "?";
        }
        long seconds = diff / 1000L;
        if (seconds < 60L) {
            return seconds + "s";
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return minutes + "m";
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + "h";
        }
        long days = hours / 24L;
        if (days < 7L) {
            return days + "d";
        }
        long weeks = days / 7L;
        if (weeks < 4L) {
            return weeks + "w";
        }
        long months = days / 30L;
        return months + "mo";
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    @NotNull
    public static Instant nowInstant() {
        return Instant.now();
    }

    @NotNull
    public static String formatTimestamp(long timestamp) {
        return TimeUtils.formatTimestamp(timestamp, DATETIME_FORMAT);
    }

    @NotNull
    public static String formatTimestamp(long timestamp, @NotNull DateTimeFormatter formatter) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).format(formatter);
    }

    public static long getFutureTimestamp(long amount, @NotNull TimeUnit unit) {
        return System.currentTimeMillis() + unit.toMillis(amount);
    }

    public static boolean hasPassed(long timestamp) {
        return System.currentTimeMillis() >= timestamp;
    }

    public static long getRemainingSeconds(long futureTimestamp) {
        long remaining = futureTimestamp - System.currentTimeMillis();
        return remaining > 0L ? remaining / 1000L : 0L;
    }

    @NotNull
    public static String getRemainingFormatted(long futureTimestamp) {
        return TimeUtils.formatDuration(TimeUtils.getRemainingSeconds(futureTimestamp));
    }

    public static boolean isCooldownExpired(long lastAction, long cooldownSeconds) {
        return System.currentTimeMillis() >= lastAction + cooldownSeconds * 1000L;
    }

    public static long getCooldownRemaining(long lastAction, long cooldownSeconds) {
        long endTime = lastAction + cooldownSeconds * 1000L;
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0L ? remaining / 1000L : 0L;
    }

    @NotNull
    public static String getCooldownRemainingFormatted(long lastAction, long cooldownSeconds) {
        return TimeUtils.formatDuration(TimeUtils.getCooldownRemaining(lastAction, cooldownSeconds));
    }

    public static long secondsToTicks(double seconds) {
        return Math.round(seconds * 20.0);
    }

    public static double ticksToSeconds(long ticks) {
        return (double)ticks / 20.0;
    }

    public static long millisToTicks(long millis) {
        return millis / 50L;
    }

    public static long ticksToMillis(long ticks) {
        return ticks * 50L;
    }

    public static long getElapsedSeconds(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000L;
    }

    @NotNull
    public static String getElapsedFormatted(long startTime) {
        return TimeUtils.formatMatchTime(TimeUtils.getElapsedSeconds(startTime));
    }

    @NotNull
    public static String stopwatchDisplay(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        long minutes = elapsed / 1000L / 60L;
        long seconds = elapsed / 1000L % 60L;
        long millis = elapsed % 1000L / 10L;
        return String.format("%02d:%02d.%02d", minutes, seconds, millis);
    }

    public static boolean isToday(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        return dateTime.toLocalDate().equals(now.toLocalDate());
    }

    public static boolean isWithinHours(long timestamp, int hours) {
        return System.currentTimeMillis() - timestamp <= TimeUnit.HOURS.toMillis(hours);
    }

    public static long getStartOfToday() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

