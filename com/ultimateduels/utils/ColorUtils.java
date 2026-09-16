/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.ChatColor
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.utils;

import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ColorUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final MiniMessage MINI_MESSAGE_STRICT = MiniMessage.builder().strict(true).build();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_BRACKET_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    public static final TextColor PRIMARY = TextColor.fromHexString((String)"#FFB347");
    public static final TextColor SECONDARY = TextColor.fromHexString((String)"#7DD3FC");
    public static final TextColor SUCCESS = TextColor.fromHexString((String)"#4ADE80");
    public static final TextColor ERROR = TextColor.fromHexString((String)"#F87171");
    public static final TextColor WARNING = TextColor.fromHexString((String)"#FACC15");
    public static final TextColor INFO = TextColor.fromHexString((String)"#22D3EE");
    public static final TextColor MUTED = TextColor.fromHexString((String)"#9CA3AF");

    private ColorUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    @NotNull
    public static Component parse(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String processed = ColorUtils.colorize(message);
        return LEGACY_SECTION_SERIALIZER.deserialize(processed);
    }

    @NotNull
    public static Component parse(@Nullable String message, @NotNull Map<String, String> placeholders) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String processed = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processed = processed.replace("{" + entry.getKey() + "}", entry.getValue());
            processed = processed.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return ColorUtils.parse(processed);
    }

    @NotNull
    public static Component parse(@Nullable String message, @NotNull String key, @NotNull Component value) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        Component base = ColorUtils.parse(message.replace("{" + key + "}", "").replace("<" + key + ">", ""));
        return base.append(value);
    }

    @NotNull
    public static List<Component> parseList(@Nullable List<String> messages) {
        ArrayList<Component> components = new ArrayList<Component>();
        if (messages == null) {
            return components;
        }
        for (String message : messages) {
            components.add(ColorUtils.parse(message));
        }
        return components;
    }

    @NotNull
    public static String translateLegacy(@Nullable String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }

    @NotNull
    public static String translateHex(@Nullable String message) {
        if (message == null) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
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

    @NotNull
    public static String colorize(@Nullable String message) {
        if (message == null) {
            return "";
        }
        return ColorUtils.translateLegacy(ColorUtils.translateHex(message));
    }

    @NotNull
    public static List<String> colorize(@Nullable List<String> messages) {
        ArrayList<String> colored = new ArrayList<String>();
        if (messages == null) {
            return colored;
        }
        for (String message : messages) {
            colored.add(ColorUtils.colorize(message));
        }
        return colored;
    }

    @NotNull
    public static String toLegacy(@NotNull Component component) {
        return LEGACY_SECTION_SERIALIZER.serialize(component);
    }

    @NotNull
    public static String toPlain(@NotNull Component component) {
        return PLAIN_SERIALIZER.serialize(component);
    }

    @NotNull
    public static Component fromLegacy(@Nullable String legacy) {
        if (legacy == null || legacy.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(legacy);
    }

    @NotNull
    public static String stripColors(@Nullable String message) {
        if (message == null) {
            return "";
        }
        String stripped = ChatColor.stripColor((String)ColorUtils.colorize(message));
        return stripped != null ? stripped : "";
    }

    @NotNull
    public static Component text(@NotNull String text, @NotNull NamedTextColor color) {
        return Component.text((String)text, (TextColor)color);
    }

    @NotNull
    public static Component text(@NotNull String text, @NotNull String hexColor) {
        TextColor color = TextColor.fromHexString((String)hexColor);
        return color != null ? Component.text((String)text, (TextColor)color) : Component.text((String)text);
    }

    @NotNull
    public static Component bold(@NotNull String text, @NotNull NamedTextColor color) {
        return Component.text((String)text, (TextColor)color, (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD});
    }

    @NotNull
    public static Component italic(@NotNull String text, @NotNull NamedTextColor color) {
        return Component.text((String)text, (TextColor)color, (TextDecoration[])new TextDecoration[]{TextDecoration.ITALIC});
    }

    @NotNull
    public static Component combine(Component ... components) {
        TextComponent result = Component.empty();
        for (Component component : components) {
            result = result.append(component);
        }
        return result;
    }

    @NotNull
    public static Component prefix() {
        return ColorUtils.parse("&8[&6UltimateDuels&8] ");
    }

    @NotNull
    public static Component prefixed(@NotNull String message) {
        return ColorUtils.prefix().append(ColorUtils.parse(message));
    }

    @NotNull
    public static Component prefixed(@NotNull Component message) {
        return ColorUtils.prefix().append(message);
    }

    @NotNull
    public static Component success(@NotNull String message) {
        return ColorUtils.prefix().append(ColorUtils.parse("&a" + message));
    }

    @NotNull
    public static Component error(@NotNull String message) {
        return ColorUtils.prefix().append(ColorUtils.parse("&c" + message));
    }

    @NotNull
    public static Component warning(@NotNull String message) {
        return ColorUtils.prefix().append(ColorUtils.parse("&e" + message));
    }

    @NotNull
    public static Component info(@NotNull String message) {
        return ColorUtils.prefix().append(ColorUtils.parse("&b" + message));
    }

    @NotNull
    public static String scoreboardLine(@NotNull String text) {
        String processed = ColorUtils.colorize(text);
        if (processed.length() > 40) {
            processed = processed.substring(0, 40);
        }
        return processed;
    }

    @NotNull
    public static String centerText(@NotNull String text, int lineWidth) {
        String stripped = ColorUtils.stripColors(text);
        int padding = (lineWidth - stripped.length()) / 2;
        if (padding <= 0) {
            return text;
        }
        return " ".repeat(padding) + text;
    }

    @NotNull
    public static Component header(@NotNull String text) {
        return ColorUtils.parse("&7\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501 &6&l" + text + " &7\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
    }

    @NotNull
    public static Component separator() {
        return ColorUtils.parse("&8&m" + " ".repeat(50));
    }
}

