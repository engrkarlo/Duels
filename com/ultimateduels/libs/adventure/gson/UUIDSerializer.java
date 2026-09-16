/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonToken
 *  com.google.gson.stream.JsonWriter
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.UUID;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.option.OptionState;

final class UUIDSerializer
extends TypeAdapter<UUID> {
    private final boolean emitIntArray;

    static TypeAdapter<UUID> uuidSerializer(OptionState features) {
        return new UUIDSerializer(features.value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY)).nullSafe();
    }

    private UUIDSerializer(boolean emitIntArray) {
        this.emitIntArray = emitIntArray;
    }

    public void write(JsonWriter out, UUID value) throws IOException {
        if (this.emitIntArray) {
            int msb0 = (int)(value.getMostSignificantBits() >> 32);
            int msb1 = (int)(value.getMostSignificantBits() & 0xFFFFFFFFL);
            int lsb0 = (int)(value.getLeastSignificantBits() >> 32);
            int lsb1 = (int)(value.getLeastSignificantBits() & 0xFFFFFFFFL);
            out.beginArray().value((long)msb0).value((long)msb1).value((long)lsb0).value((long)lsb1).endArray();
        } else {
            out.value(value.toString());
        }
    }

    public UUID read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.BEGIN_ARRAY) {
            in.beginArray();
            int msb0 = in.nextInt();
            int msb1 = in.nextInt();
            int lsb0 = in.nextInt();
            int lsb1 = in.nextInt();
            in.endArray();
            return new UUID((long)msb0 << 32 | (long)msb1 & 0xFFFFFFFFL, (long)lsb0 << 32 | (long)lsb1 & 0xFFFFFFFFL);
        }
        return UUID.fromString(in.nextString());
    }
}

