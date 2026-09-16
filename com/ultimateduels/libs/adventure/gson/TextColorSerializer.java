/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TextColorSerializer
extends TypeAdapter<TextColor> {
    static final TypeAdapter<TextColor> INSTANCE = new TextColorSerializer(false).nullSafe();
    static final TypeAdapter<TextColor> DOWNSAMPLE_COLOR = new TextColorSerializer(true).nullSafe();
    private final boolean downsampleColor;

    private TextColorSerializer(boolean downsampleColor) {
        this.downsampleColor = downsampleColor;
    }

    public void write(JsonWriter out, TextColor value) throws IOException {
        if (value instanceof NamedTextColor) {
            out.value((String)NamedTextColor.NAMES.key((Object)((NamedTextColor)value)));
        } else if (this.downsampleColor) {
            out.value((String)NamedTextColor.NAMES.key((Object)NamedTextColor.nearestTo((TextColor)value)));
        } else {
            out.value(TextColorSerializer.asUpperCaseHexString(value));
        }
    }

    private static String asUpperCaseHexString(TextColor color) {
        return String.format(Locale.ROOT, "%c%06X", Character.valueOf('#'), color.value());
    }

    @Nullable
    public TextColor read(JsonReader in) throws IOException {
        @Nullable TextColor color = TextColorSerializer.fromString(in.nextString());
        if (color == null) {
            return null;
        }
        return this.downsampleColor ? NamedTextColor.nearestTo((TextColor)color) : color;
    }

    @Nullable
    static TextColor fromString(@NotNull String value) {
        if (value.startsWith("#")) {
            return TextColor.fromHexString((String)value);
        }
        return (TextColor)NamedTextColor.NAMES.value((Object)value);
    }
}

