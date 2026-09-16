/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.key.Key
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.kyori.adventure.key.Key;

final class KeySerializer
extends TypeAdapter<Key> {
    static final TypeAdapter<Key> INSTANCE = new KeySerializer().nullSafe();

    private KeySerializer() {
    }

    public void write(JsonWriter out, Key value) throws IOException {
        out.value(value.asString());
    }

    public Key read(JsonReader in) throws IOException {
        return Key.key((String)in.nextString());
    }
}

