/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.util.Vector
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class MathUtils {
    private static final Random RANDOM = ThreadLocalRandom.current();
    private static final DecimalFormat DF_0 = new DecimalFormat("#");
    private static final DecimalFormat DF_1 = new DecimalFormat("#.#");
    private static final DecimalFormat DF_2 = new DecimalFormat("#.##");
    private static final DecimalFormat DF_3 = new DecimalFormat("#.###");
    private static final DecimalFormat DF_PERCENT = new DecimalFormat("#.#%");
    private static final DecimalFormat DF_COMMA = new DecimalFormat("#,###");

    private MathUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static double calculateKD(int kills, int deaths) {
        if (deaths == 0) {
            return kills > 0 ? (double)kills : 0.0;
        }
        return (double)kills / (double)deaths;
    }

    public static double calculateKD(int kills, int deaths, int decimalPlaces) {
        return MathUtils.round(MathUtils.calculateKD(kills, deaths), decimalPlaces);
    }

    @NotNull
    public static String formatKD(int kills, int deaths) {
        double kd = MathUtils.calculateKD(kills, deaths);
        return MathUtils.formatKD(kd);
    }

    @NotNull
    public static String formatKD(double kd) {
        if (kd >= 100.0) {
            return DF_0.format(kd);
        }
        if (kd >= 10.0) {
            return DF_1.format(kd);
        }
        return DF_2.format(kd);
    }

    @NotNull
    public static String getKDColor(double kd) {
        if (kd >= 5.0) {
            return "&5";
        }
        if (kd >= 3.0) {
            return "&6";
        }
        if (kd >= 2.0) {
            return "&a";
        }
        if (kd >= 1.0) {
            return "&e";
        }
        if (kd >= 0.5) {
            return "&7";
        }
        return "&c";
    }

    @NotNull
    public static String getColoredKD(int kills, int deaths) {
        double kd = MathUtils.calculateKD(kills, deaths);
        return MathUtils.getKDColor(kd) + MathUtils.formatKD(kd);
    }

    public static double calculateWinRate(int wins, int totalGames) {
        if (totalGames == 0) {
            return 0.0;
        }
        return (double)wins / (double)totalGames * 100.0;
    }

    public static double calculateWinRate(int wins, int totalGames, int decimalPlaces) {
        return MathUtils.round(MathUtils.calculateWinRate(wins, totalGames), decimalPlaces);
    }

    @NotNull
    public static String formatWinRate(int wins, int totalGames) {
        double rate = MathUtils.calculateWinRate(wins, totalGames);
        return DF_1.format(rate) + "%";
    }

    @NotNull
    public static String getWinRateColor(double rate) {
        if (rate >= 80.0) {
            return "&5";
        }
        if (rate >= 65.0) {
            return "&6";
        }
        if (rate >= 50.0) {
            return "&a";
        }
        if (rate >= 35.0) {
            return "&e";
        }
        return "&c";
    }

    public static int calculateEloChange(int playerRating, int opponentRating, double won, int kFactor) {
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (double)(opponentRating - playerRating) / 400.0));
        return (int)Math.round((double)kFactor * (won - expectedScore));
    }

    public static int calculateNewElo(int playerRating, int opponentRating, boolean won, int kFactor) {
        int change = MathUtils.calculateEloChange(playerRating, opponentRating, won ? 1.0 : 0.0, kFactor);
        return Math.max(0, playerRating + change);
    }

    @NotNull
    public static String getEloTier(int rating) {
        if (rating >= 2400) {
            return "Grandmaster";
        }
        if (rating >= 2000) {
            return "Master";
        }
        if (rating >= 1800) {
            return "Diamond";
        }
        if (rating >= 1600) {
            return "Platinum";
        }
        if (rating >= 1400) {
            return "Gold";
        }
        if (rating >= 1200) {
            return "Silver";
        }
        if (rating >= 1000) {
            return "Bronze";
        }
        return "Unranked";
    }

    @NotNull
    public static String getEloTierColor(int rating) {
        if (rating >= 2400) {
            return "&4";
        }
        if (rating >= 2000) {
            return "&d";
        }
        if (rating >= 1800) {
            return "&b";
        }
        if (rating >= 1600) {
            return "&3";
        }
        if (rating >= 1400) {
            return "&6";
        }
        if (rating >= 1200) {
            return "&7";
        }
        if (rating >= 1000) {
            return "&8";
        }
        return "&f";
    }

    public static double getStreakMultiplier(int streak) {
        if (streak <= 0) {
            return 1.0;
        }
        if (streak <= 3) {
            return 1.0 + (double)streak * 0.1;
        }
        if (streak <= 5) {
            return 1.3 + (double)(streak - 3) * 0.15;
        }
        if (streak <= 10) {
            return 1.6 + (double)(streak - 5) * 0.1;
        }
        return 2.1 + (double)(streak - 10) * 0.05;
    }

    @NotNull
    public static String getStreakTitle(int streak) {
        if (streak >= 20) {
            return "GODLIKE";
        }
        if (streak >= 15) {
            return "UNSTOPPABLE";
        }
        if (streak >= 10) {
            return "DOMINATING";
        }
        if (streak >= 7) {
            return "RAMPAGE";
        }
        if (streak >= 5) {
            return "KILLING SPREE";
        }
        if (streak >= 3) {
            return "ON FIRE";
        }
        return "";
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("Decimal places must be >= 0");
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @NotNull
    public static String formatWithCommas(long number) {
        return DF_COMMA.format(number);
    }

    @NotNull
    public static String formatWithCommas(int number) {
        return DF_COMMA.format(number);
    }

    @NotNull
    public static String formatCompact(long number) {
        if (number < 1000L) {
            return String.valueOf(number);
        }
        if (number < 1000000L) {
            return DF_1.format((double)number / 1000.0) + "K";
        }
        if (number < 1000000000L) {
            return DF_1.format((double)number / 1000000.0) + "M";
        }
        return DF_1.format((double)number / 1.0E9) + "B";
    }

    @NotNull
    public static String formatPercent(double value) {
        return DF_1.format(value * 100.0) + "%";
    }

    @NotNull
    public static String formatDecimal(double value, int places) {
        return switch (places) {
            case 0 -> DF_0.format(value);
            case 1 -> DF_1.format(value);
            case 2 -> DF_2.format(value);
            case 3 -> DF_3.format(value);
            default -> String.format("%." + places + "f", value);
        };
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static double map(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    public static int randomInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    public static boolean chance(double percentage) {
        return RANDOM.nextDouble() * 100.0 < percentage;
    }

    public static boolean probability(double probability) {
        return RANDOM.nextDouble() < probability;
    }

    @NotNull
    public static <T> T randomElement(@NotNull List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }
        return list.get(RANDOM.nextInt(list.size()));
    }

    @NotNull
    public static <T> T randomElement(@NotNull T[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }
        return array[RANDOM.nextInt(array.length)];
    }

    public static double average(int ... values) {
        if (values.length == 0) {
            return 0.0;
        }
        long sum = 0L;
        for (int value : values) {
            sum += (long)value;
        }
        return (double)sum / (double)values.length;
    }

    public static double average(double ... values) {
        if (values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / (double)values.length;
    }

    public static double average(@NotNull List<? extends Number> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Number number : values) {
            sum += number.doubleValue();
        }
        return sum / (double)values.size();
    }

    public static double median(int ... values) {
        if (values.length == 0) {
            return 0.0;
        }
        int[] sorted = (int[])values.clone();
        Arrays.sort(sorted);
        int middle = sorted.length / 2;
        if (sorted.length % 2 == 0) {
            return (double)(sorted[middle - 1] + sorted[middle]) / 2.0;
        }
        return sorted[middle];
    }

    public static double standardDeviation(double ... values) {
        if (values.length == 0) {
            return 0.0;
        }
        double avg = MathUtils.average(values);
        double sumSquaredDiff = 0.0;
        for (double value : values) {
            sumSquaredDiff += Math.pow(value - avg, 2.0);
        }
        return Math.sqrt(sumSquaredDiff / (double)values.length);
    }

    public static double distance(@NotNull Location loc1, @NotNull Location loc2) {
        return loc1.distance(loc2);
    }

    public static double distanceSquared(@NotNull Location loc1, @NotNull Location loc2) {
        return loc1.distanceSquared(loc2);
    }

    public static double horizontalDistance(@NotNull Location loc1, @NotNull Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double horizontalDistanceSquared(@NotNull Location loc1, @NotNull Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return dx * dx + dz * dz;
    }

    public static boolean isWithinRadius(@NotNull Location center, @NotNull Location point, double radius) {
        return center.distanceSquared(point) <= radius * radius;
    }

    public static boolean isWithinHorizontalRadius(@NotNull Location center, @NotNull Location point, double radius) {
        return MathUtils.horizontalDistanceSquared(center, point) <= radius * radius;
    }

    @NotNull
    public static Vector getDirection(@NotNull Location from, @NotNull Location to) {
        return to.toVector().subtract(from.toVector()).normalize();
    }

    public static float getYaw(@NotNull Location from, @NotNull Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float)Math.toDegrees(Math.atan2(-dx, dz));
    }

    public static float getPitch(@NotNull Location from, @NotNull Location to) {
        double distance = MathUtils.horizontalDistance(from, to);
        double dy = to.getY() - from.getY();
        return (float)Math.toDegrees(-Math.atan2(dy, distance));
    }

    public static double angleBetween(@NotNull Vector v1, @NotNull Vector v2) {
        double dot = v1.dot(v2);
        double lengths = v1.length() * v2.length();
        if (lengths == 0.0) {
            return 0.0;
        }
        return Math.toDegrees(Math.acos(MathUtils.clamp(dot / lengths, -1.0, 1.0)));
    }

    public static double percentage(double value, double total) {
        if (total == 0.0) {
            return 0.0;
        }
        return value / total * 100.0;
    }

    public static double progress(double current, double max) {
        if (max == 0.0) {
            return 0.0;
        }
        return MathUtils.clamp(current / max, 0.0, 1.0);
    }

    public static double valueAtPercent(double min, double max, double percent) {
        return min + (max - min) * (percent / 100.0);
    }

    @NotNull
    public static String progressBar(double progress, int length, char filled, char empty) {
        progress = MathUtils.clamp(progress, 0.0, 1.0);
        int filledLength = (int)Math.round(progress * (double)length);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            bar.append(i < filledLength ? filled : empty);
        }
        return bar.toString();
    }

    @NotNull
    public static String coloredProgressBar(double progress, int length) {
        progress = MathUtils.clamp(progress, 0.0, 1.0);
        int filledLength = (int)Math.round(progress * (double)length);
        String color = progress > 0.66 ? "&a" : (progress > 0.33 ? "&e" : "&c");
        return color + "\u258c".repeat(Math.max(0, filledLength)) + "&8" + "\u258c".repeat(Math.max(0, length - filledLength));
    }

    @NotNull
    public static String getOrdinalSuffix(int number) {
        if (number >= 11 && number <= 13) {
            return number + "th";
        }
        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }

    @NotNull
    public static String getRankColor(int rank) {
        return switch (rank) {
            case 1 -> "&6";
            case 2 -> "&7";
            case 3 -> "&4";
            default -> rank <= 10 ? "&e" : (rank <= 25 ? "&a" : (rank <= 50 ? "&b" : "&f"));
        };
    }

    @NotNull
    public static String getRankDisplay(int rank) {
        String prefix = switch (rank) {
            case 1 -> "\ud83e\udd47 ";
            case 2 -> "\ud83e\udd48 ";
            case 3 -> "\ud83e\udd49 ";
            default -> "#";
        };
        return MathUtils.getRankColor(rank) + prefix + String.valueOf(rank > 3 ? Integer.valueOf(rank) : "");
    }
}

