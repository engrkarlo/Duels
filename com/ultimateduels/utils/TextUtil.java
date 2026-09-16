/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 */
package com.ultimateduels.utils;

import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern MINI_MESSAGE_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[\u00a7&][0-9a-fk-orA-FK-OR]");

    private TextUtil() {
    }

    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        try {
            boolean hasMiniMessage = MINI_MESSAGE_PATTERN.matcher(text).find();
            if (hasMiniMessage) {
                text = text.replace('&', '\u00a7');
                text = TextUtil.convertLegacyToMiniMessage(text);
                return MINI_MESSAGE.deserialize((Object)text);
            }
            return TextUtil.parseLegacy(text);
        }
        catch (Exception e) {
            try {
                return TextUtil.parseLegacy(text);
            }
            catch (Exception e2) {
                return Component.text((String)text);
            }
        }
    }

    public static Component parseLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        text = text.replace('&', '\u00a7');
        return LEGACY_SECTION.deserialize(text);
    }

    private static String convertLegacyToMiniMessage(String text) {
        text = text.replace("\u00a70", "<black>");
        text = text.replace("\u00a71", "<dark_blue>");
        text = text.replace("\u00a72", "<dark_green>");
        text = text.replace("\u00a73", "<dark_aqua>");
        text = text.replace("\u00a74", "<dark_red>");
        text = text.replace("\u00a75", "<dark_purple>");
        text = text.replace("\u00a76", "<gold>");
        text = text.replace("\u00a77", "<gray>");
        text = text.replace("\u00a78", "<dark_gray>");
        text = text.replace("\u00a79", "<blue>");
        text = text.replace("\u00a7a", "<green>");
        text = text.replace("\u00a7b", "<aqua>");
        text = text.replace("\u00a7c", "<red>");
        text = text.replace("\u00a7d", "<light_purple>");
        text = text.replace("\u00a7e", "<yellow>");
        text = text.replace("\u00a7f", "<white>");
        text = text.replace("\u00a7k", "<obfuscated>");
        text = text.replace("\u00a7l", "<bold>");
        text = text.replace("\u00a7m", "<strikethrough>");
        text = text.replace("\u00a7n", "<underlined>");
        text = text.replace("\u00a7o", "<italic>");
        text = text.replace("\u00a7r", "<reset>");
        return text;
    }

    public static String stripLegacyCodes(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\u00a7&][0-9a-fk-orA-FK-OR]", "");
    }

    public static String stripAllFormatting(String text) {
        if (text == null) {
            return "";
        }
        text = text.replaceAll("[\u00a7&][0-9a-fk-orA-FK-OR]", "");
        text = text.replaceAll("<[^>]+>", "");
        return text;
    }

    public static String toLegacy(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SECTION.serialize(component);
    }

    public static String toLegacyAmpersand(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_AMPERSAND.serialize(component);
    }

    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('&', '\u00a7');
    }

    public static MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }
}

