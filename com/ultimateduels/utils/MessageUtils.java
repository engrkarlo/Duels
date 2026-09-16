/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.utils;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.utils.TextUtil;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageUtils {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static UltimateDuels plugin;

    private MessageUtils() {
    }

    public static void init(@Nonnull UltimateDuels pluginInstance) {
        plugin = pluginInstance;
    }

    @Nonnull
    private static String getPrefix(@Nonnull Player player) {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getPrefix(player);
        }
        return "&8[&6UltimateDuels&8] ";
    }

    @Nonnull
    private static String getDefaultPrefix() {
        if (plugin != null && plugin.getLanguageManager() != null) {
            return plugin.getLanguageManager().getDefaultPrefix();
        }
        return "&8[&6UltimateDuels&8] ";
    }

    @Nonnull
    public static String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        message = MessageUtils.processHexColors(message);
        return message.replace('&', '\u00a7');
    }

    @Nonnull
    private static String processHexColors(@Nonnull String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00a7x");
            for (char c : hex.toCharArray()) {
                replacement.append('\u00a7').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @Nonnull
    public static String stripColor(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return message.replaceAll("(?i)[\u00a7&][0-9A-FK-ORX]", "");
    }

    @Nonnull
    public static Component toComponent(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(MessageUtils.colorize(message));
    }

    @Nonnull
    public static String toLegacy(@Nonnull Component component) {
        return SECTION_SERIALIZER.serialize(component);
    }

    public static void sendMessage(@Nonnull Player player, @Nonnull String message) {
        String prefix = MessageUtils.getPrefix(player);
        String fullMessage = (prefix + message).replace('&', '\u00a7');
        player.sendMessage(TextUtil.parseLegacy(fullMessage));
    }

    public static void sendMessage(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            MessageUtils.sendMessage(player, message);
        } else {
            String prefix = MessageUtils.getDefaultPrefix();
            sender.sendMessage((prefix + message).replace('&', '\u00a7'));
        }
    }

    public static void sendRaw(@Nonnull Player player, @Nonnull String message) {
        player.sendMessage(MessageUtils.colorize(message));
    }

    public static void sendRaw(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            MessageUtils.sendRaw(player, message);
        } else {
            sender.sendMessage(MessageUtils.colorize(message));
        }
    }

    public static void send(@Nonnull CommandSender sender, @Nonnull String message) {
        MessageUtils.sendMessage(sender, message);
    }

    public static void sendPrefixed(@Nonnull CommandSender sender, @Nonnull String message) {
        MessageUtils.sendMessage(sender, message);
    }

    public static void send(@Nonnull CommandSender sender, @Nonnull Component component) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            player.sendMessage(component);
        } else {
            sender.sendMessage(MessageUtils.toLegacy(component));
        }
    }

    public static void sendPrefixedMessage(@Nonnull Player player, @Nonnull String message) {
        MessageUtils.sendMessage(player, message);
    }

    public static void sendActionBar(@Nonnull Player player, @Nonnull String message) {
        player.sendActionBar(MessageUtils.toComponent(message));
    }

    public static void sendToAll(@Nonnull Collection<? extends Player> players, @Nonnull String message) {
        for (Player player : players) {
            MessageUtils.sendMessage(player, message);
        }
    }

    public static void sendPrefixedToAll(@Nonnull Collection<? extends Player> players, @Nonnull String message) {
        for (Player player : players) {
            MessageUtils.sendMessage(player, message);
        }
    }

    public static void broadcast(@Nonnull String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MessageUtils.sendMessage(player, message);
        }
        String prefix = MessageUtils.getDefaultPrefix();
        Bukkit.getConsoleSender().sendMessage((prefix + message).replace('&', '\u00a7'));
    }

    public static void broadcastPrefixed(@Nonnull String message) {
        MessageUtils.broadcast(message);
    }

    public static void broadcastWithPermission(@Nonnull String message, @Nonnull String permission) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(permission)) continue;
            MessageUtils.sendMessage(player, message);
        }
    }

    public static void sendError(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            MessageUtils.sendMessage(player, "&c" + message);
        } else {
            String prefix = MessageUtils.getDefaultPrefix();
            sender.sendMessage(prefix + "\u00a7c" + message);
        }
    }

    public static void sendSuccess(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            MessageUtils.sendMessage(player, "&a" + message);
        } else {
            String prefix = MessageUtils.getDefaultPrefix();
            sender.sendMessage(prefix + "\u00a7a" + message);
        }
    }

    public static void sendWarning(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            MessageUtils.sendMessage(player, "&e" + message);
        } else {
            String prefix = MessageUtils.getDefaultPrefix();
            sender.sendMessage(prefix + "\u00a7e" + message);
        }
    }

    public static void sendInfo(@Nonnull CommandSender sender, @Nonnull String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            MessageUtils.sendMessage(player, "&7" + message);
        } else {
            String prefix = MessageUtils.getDefaultPrefix();
            sender.sendMessage(prefix + "\u00a77" + message);
        }
    }

    @Nonnull
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    @Nonnull
    public static String formatDecimal(double number, int places) {
        return String.format("%,." + places + "f", number);
    }

    @Nonnull
    public static String formatTime(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        if (hours > 0L) {
            return String.format("%d:%02d:%02d", hours, minutes % 60L, seconds % 60L);
        }
        if (minutes > 0L) {
            return String.format("%d:%02d", minutes, seconds % 60L);
        }
        return String.format("0:%02d", seconds);
    }

    @Nonnull
    public static String formatTimeWords(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        StringBuilder sb = new StringBuilder();
        if (days > 0L) {
            sb.append(days).append(days == 1L ? " day " : " days ");
        }
        if (hours % 24L > 0L) {
            sb.append(hours % 24L).append(hours % 24L == 1L ? " hour " : " hours ");
        }
        if (minutes % 60L > 0L) {
            sb.append(minutes % 60L).append(minutes % 60L == 1L ? " minute " : " minutes ");
        }
        if (seconds % 60L > 0L || sb.length() == 0) {
            sb.append(seconds % 60L).append(seconds % 60L == 1L ? " second" : " seconds");
        }
        return sb.toString().trim();
    }

    @Nonnull
    public static String createProgressBar(double current, double max, int totalBars, char filledChar, char emptyChar, String filledColor, String emptyColor) {
        double percent = Math.min(1.0, Math.max(0.0, current / max));
        int filledBars = (int)((double)totalBars * percent);
        StringBuilder bar = new StringBuilder();
        bar.append(filledColor);
        for (int i = 0; i < totalBars; ++i) {
            if (i < filledBars) {
                bar.append(filledChar);
                continue;
            }
            if (i == filledBars) {
                bar.append(emptyColor);
            }
            bar.append(emptyChar);
        }
        return MessageUtils.colorize(bar.toString());
    }

    @Nonnull
    public static String createProgressBar(double current, double max, int totalBars) {
        return MessageUtils.createProgressBar(current, max, totalBars, '\u2588', '\u2591', "&a", "&7");
    }

    @Nonnull
    public static String centerMessage(@Nonnull String message) {
        int CENTER_PX = 154;
        String stripped = MessageUtils.stripColor(message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : stripped.toCharArray()) {
            if (c == '\u00a7') {
                previousCode = true;
                continue;
            }
            if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }
            DefaultFontInfo dfi = DefaultFontInfo.getDefaultFontInfo(c);
            messagePxSize += isBold ? dfi.getBoldLength() : dfi.getLength();
            ++messagePxSize;
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        StringBuilder sb = new StringBuilder();
        for (int compensated = 0; compensated < toCompensate; compensated += spaceLength) {
            sb.append(' ');
        }
        return String.valueOf(sb) + MessageUtils.colorize(message);
    }

    private static enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        HASH('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('\"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        private DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == SPACE) {
                return this.length;
            }
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dfi : DefaultFontInfo.values()) {
                if (dfi.getCharacter() != c) continue;
                return dfi;
            }
            return DEFAULT;
        }
    }
}

