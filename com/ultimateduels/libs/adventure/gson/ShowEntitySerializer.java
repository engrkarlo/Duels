/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonParseException
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.event.HoverEvent$ShowEntity
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ultimateduels.libs.adventure.gson.SerializerFactory;
import java.io.IOException;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;

final class ShowEntitySerializer
extends TypeAdapter<HoverEvent.ShowEntity> {
    private final Gson gson;

    static TypeAdapter<HoverEvent.ShowEntity> create(Gson gson) {
        return new ShowEntitySerializer(gson).nullSafe();
    }

    private ShowEntitySerializer(Gson gson) {
        this.gson = gson;
    }

    public HoverEvent.ShowEntity read(JsonReader in) throws IOException {
        in.beginObject();
        Key type = null;
        UUID id = null;
        Component name = null;
        while (in.hasNext()) {
            String fieldName = in.nextName();
            if (fieldName.equals("type")) {
                type = (Key)this.gson.fromJson(in, SerializerFactory.KEY_TYPE);
                continue;
            }
            if (fieldName.equals("id")) {
                id = (UUID)this.gson.fromJson(in, SerializerFactory.UUID_TYPE);
                continue;
            }
            if (fieldName.equals("name")) {
                name = (Component)this.gson.fromJson(in, SerializerFactory.COMPONENT_TYPE);
                continue;
            }
            in.skipValue();
        }
        if (type == null || id == null) {
            throw new JsonParseException("A show entity hover event needs type and id fields to be deserialized");
        }
        in.endObject();
        return HoverEvent.ShowEntity.showEntity(type, id, name);
    }

    public void write(JsonWriter out, HoverEvent.ShowEntity value) throws IOException {
        out.beginObject();
        out.name("type");
        this.gson.toJson((Object)value.type(), SerializerFactory.KEY_TYPE, out);
        out.name("id");
        this.gson.toJson((Object)value.id(), SerializerFactory.UUID_TYPE, out);
        @Nullable Component name = value.name();
        if (name != null) {
            out.name("name");
            this.gson.toJson((Object)name, SerializerFactory.COMPONENT_TYPE, out);
        }
        out.endObject();
    }
}

