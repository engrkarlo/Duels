/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonToken
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.nbt.api.BinaryTagHolder
 *  net.kyori.adventure.text.event.HoverEvent$ShowItem
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ultimateduels.libs.adventure.gson.GsonDataComponentValue;
import com.ultimateduels.libs.adventure.gson.SerializerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.Nullable;

final class ShowItemSerializer
extends TypeAdapter<HoverEvent.ShowItem> {
    private static final String LEGACY_SHOW_ITEM_TAG = "tag";
    private final Gson gson;
    private final boolean emitDefaultQuantity;
    private final JSONOptions.ShowItemHoverDataMode itemDataMode;

    static TypeAdapter<HoverEvent.ShowItem> create(Gson gson, OptionState opt) {
        return new ShowItemSerializer(gson, opt.value(JSONOptions.EMIT_DEFAULT_ITEM_HOVER_QUANTITY), opt.value(JSONOptions.SHOW_ITEM_HOVER_DATA_MODE)).nullSafe();
    }

    private ShowItemSerializer(Gson gson, boolean emitDefaultQuantity, JSONOptions.ShowItemHoverDataMode itemDataMode) {
        this.gson = gson;
        this.emitDefaultQuantity = emitDefaultQuantity;
        this.itemDataMode = itemDataMode;
    }

    public HoverEvent.ShowItem read(JsonReader in) throws IOException {
        in.beginObject();
        Key key = null;
        int count = 1;
        BinaryTagHolder nbt = null;
        HashMap<Key, GsonDataComponentValue> dataComponents = null;
        while (in.hasNext()) {
            String fieldName = in.nextName();
            if (fieldName.equals("id")) {
                key = (Key)this.gson.fromJson(in, SerializerFactory.KEY_TYPE);
                continue;
            }
            if (fieldName.equals("count")) {
                count = in.nextInt();
                continue;
            }
            if (fieldName.equals(LEGACY_SHOW_ITEM_TAG)) {
                JsonToken token = in.peek();
                if (token == JsonToken.STRING || token == JsonToken.NUMBER) {
                    nbt = BinaryTagHolder.binaryTagHolder((String)in.nextString());
                    continue;
                }
                if (token == JsonToken.BOOLEAN) {
                    nbt = BinaryTagHolder.binaryTagHolder((String)String.valueOf(in.nextBoolean()));
                    continue;
                }
                if (token == JsonToken.NULL) {
                    in.nextNull();
                    continue;
                }
                throw new JsonParseException("Expected tag to be a string");
            }
            if (fieldName.equals("components")) {
                in.beginObject();
                while (in.peek() != JsonToken.END_OBJECT) {
                    Key id = Key.key((String)in.nextName());
                    JsonElement tree = (JsonElement)this.gson.fromJson(in, JsonElement.class);
                    if (dataComponents == null) {
                        dataComponents = new HashMap<Key, GsonDataComponentValue>();
                    }
                    dataComponents.put(id, GsonDataComponentValue.gsonDataComponentValue(tree));
                }
                in.endObject();
                continue;
            }
            in.skipValue();
        }
        if (key == null) {
            throw new JsonParseException("Not sure how to deserialize show_item hover event");
        }
        in.endObject();
        if (dataComponents != null) {
            return HoverEvent.ShowItem.showItem(key, (int)count, dataComponents);
        }
        return HoverEvent.ShowItem.showItem(key, (int)count, nbt);
    }

    public void write(JsonWriter out, HoverEvent.ShowItem value) throws IOException {
        Map dataComponents;
        out.beginObject();
        out.name("id");
        this.gson.toJson((Object)value.item(), SerializerFactory.KEY_TYPE, out);
        int count = value.count();
        if (count != 1 || this.emitDefaultQuantity) {
            out.name("count");
            out.value((long)count);
        }
        if (!(dataComponents = value.dataComponents()).isEmpty() && this.itemDataMode != JSONOptions.ShowItemHoverDataMode.EMIT_LEGACY_NBT) {
            out.name("components");
            out.beginObject();
            for (Map.Entry entry : value.dataComponentsAs(GsonDataComponentValue.class).entrySet()) {
                out.name(((Key)entry.getKey()).asString());
                this.gson.toJson(((GsonDataComponentValue)entry.getValue()).element(), out);
            }
            out.endObject();
        } else if (this.itemDataMode != JSONOptions.ShowItemHoverDataMode.EMIT_DATA_COMPONENTS) {
            ShowItemSerializer.maybeWriteLegacy(out, value);
        }
        out.endObject();
    }

    private static void maybeWriteLegacy(JsonWriter out, HoverEvent.ShowItem value) throws IOException {
        @Nullable BinaryTagHolder nbt = value.nbt();
        if (nbt != null) {
            out.name(LEGACY_SHOW_ITEM_TAG);
            out.value(nbt.string());
        }
    }
}

