/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSyntaxException
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ultimateduels.libs.adventure.gson.TextColorSerializer;
import java.io.IOException;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

final class TextColorWrapper {
    @Nullable
    final TextColor color;
    @Nullable
    final TextDecoration decoration;
    final boolean reset;

    TextColorWrapper(@Nullable TextColor color, @Nullable TextDecoration decoration, boolean reset) {
        this.color = color;
        this.decoration = decoration;
        this.reset = reset;
    }

    static final class Serializer
    extends TypeAdapter<TextColorWrapper> {
        static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        public void write(JsonWriter out, TextColorWrapper value) {
            throw new JsonSyntaxException("Cannot write TextColorWrapper instances");
        }

        public TextColorWrapper read(JsonReader in) throws IOException {
            boolean reset;
            String input = in.nextString();
            TextColor color = TextColorSerializer.fromString(input);
            TextDecoration decoration = (TextDecoration)TextDecoration.NAMES.value((Object)input);
            boolean bl = reset = decoration == null && input.equals("reset");
            if (color == null && decoration == null && !reset) {
                throw new JsonParseException("Don't know how to parse " + input + " at " + in.getPath());
            }
            return new TextColorWrapper(color, decoration, reset);
        }
    }
}

