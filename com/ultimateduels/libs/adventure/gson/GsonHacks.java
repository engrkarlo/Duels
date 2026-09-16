/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonToken
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

final class GsonHacks {
    private GsonHacks() {
    }

    static boolean isNullOrEmpty(@Nullable JsonElement element) {
        return element == null || element.isJsonNull() || element.isJsonArray() && element.getAsJsonArray().size() == 0 || element.isJsonObject() && element.getAsJsonObject().entrySet().isEmpty();
    }

    static boolean readBoolean(JsonReader in) throws IOException {
        JsonToken peek = in.peek();
        if (peek == JsonToken.BOOLEAN) {
            return in.nextBoolean();
        }
        if (peek == JsonToken.STRING || peek == JsonToken.NUMBER) {
            return Boolean.parseBoolean(in.nextString());
        }
        throw new JsonParseException("Token of type " + peek + " cannot be interpreted as a boolean");
    }

    static String readString(JsonReader in) throws IOException {
        JsonToken peek = in.peek();
        if (peek == JsonToken.STRING || peek == JsonToken.NUMBER) {
            return in.nextString();
        }
        if (peek == JsonToken.BOOLEAN) {
            return String.valueOf(in.nextBoolean());
        }
        throw new JsonParseException("Token of type " + peek + " cannot be interpreted as a string");
    }
}

