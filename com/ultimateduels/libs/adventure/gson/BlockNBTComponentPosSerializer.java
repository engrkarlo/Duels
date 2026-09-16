/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParseException
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.text.BlockNBTComponent$Pos
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.kyori.adventure.text.BlockNBTComponent;

final class BlockNBTComponentPosSerializer
extends TypeAdapter<BlockNBTComponent.Pos> {
    static final TypeAdapter<BlockNBTComponent.Pos> INSTANCE = new BlockNBTComponentPosSerializer().nullSafe();

    private BlockNBTComponentPosSerializer() {
    }

    public BlockNBTComponent.Pos read(JsonReader in) throws IOException {
        String string = in.nextString();
        try {
            return BlockNBTComponent.Pos.fromString((String)string);
        }
        catch (IllegalArgumentException ex) {
            throw new JsonParseException("Don't know how to turn " + string + " into a Position");
        }
    }

    public void write(JsonWriter out, BlockNBTComponent.Pos value) throws IOException {
        out.value(value.asString());
    }
}

