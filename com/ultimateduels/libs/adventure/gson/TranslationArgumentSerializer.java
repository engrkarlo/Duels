/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.ComponentLike
 *  net.kyori.adventure.text.TranslationArgument
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ultimateduels.libs.adventure.gson.SerializerFactory;
import java.io.IOException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslationArgument;

final class TranslationArgumentSerializer
extends TypeAdapter<TranslationArgument> {
    private final Gson gson;

    static TypeAdapter<TranslationArgument> create(Gson gson) {
        return new TranslationArgumentSerializer(gson).nullSafe();
    }

    private TranslationArgumentSerializer(Gson gson) {
        this.gson = gson;
    }

    public void write(JsonWriter out, TranslationArgument value) throws IOException {
        Object raw = value.value();
        if (raw instanceof Boolean) {
            out.value((Boolean)raw);
        } else if (raw instanceof Number) {
            out.value((Number)raw);
        } else if (raw instanceof Component) {
            this.gson.toJson(raw, SerializerFactory.COMPONENT_TYPE, out);
        } else {
            throw new IllegalStateException("Unable to serialize translatable argument of type " + raw.getClass() + ": " + raw);
        }
    }

    public TranslationArgument read(JsonReader in) throws IOException {
        switch (in.peek()) {
            case BOOLEAN: {
                return TranslationArgument.bool((boolean)in.nextBoolean());
            }
            case NUMBER: {
                return TranslationArgument.numeric((Number)((Number)this.gson.fromJson(in, Number.class)));
            }
        }
        return TranslationArgument.component((ComponentLike)((ComponentLike)this.gson.fromJson(in, SerializerFactory.COMPONENT_TYPE)));
    }
}

