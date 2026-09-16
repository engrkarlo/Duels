/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonPrimitive
 *  com.google.gson.JsonSyntaxException
 *  com.google.gson.TypeAdapter
 *  com.google.gson.stream.JsonReader
 *  com.google.gson.stream.JsonToken
 *  com.google.gson.stream.JsonWriter
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.ClickEvent$Action
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEvent$Action
 *  net.kyori.adventure.text.event.HoverEvent$ShowEntity
 *  net.kyori.adventure.text.event.HoverEvent$ShowItem
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.format.Style
 *  net.kyori.adventure.text.format.Style$Builder
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.text.format.TextDecoration$State
 *  net.kyori.adventure.util.Codec$Decoder
 *  net.kyori.adventure.util.Codec$Encoder
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ultimateduels.libs.adventure.gson.ComponentSerializerImpl;
import com.ultimateduels.libs.adventure.gson.GsonHacks;
import com.ultimateduels.libs.adventure.gson.SerializerFactory;
import com.ultimateduels.libs.adventure.gson.TextColorWrapper;
import java.io.IOException;
import java.util.EnumSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.util.Codec;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.Nullable;

final class StyleSerializer
extends TypeAdapter<Style> {
    private static final TextDecoration[] DECORATIONS = new TextDecoration[]{TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.UNDERLINED, TextDecoration.STRIKETHROUGH, TextDecoration.OBFUSCATED};
    private final LegacyHoverEventSerializer legacyHover;
    private final boolean emitLegacyHover;
    private final boolean emitModernHover;
    private final boolean strictEventValues;
    private final Gson gson;

    static TypeAdapter<Style> create(@Nullable LegacyHoverEventSerializer legacyHover, OptionState features, Gson gson) {
        JSONOptions.HoverEventValueMode hoverMode = features.value(JSONOptions.EMIT_HOVER_EVENT_TYPE);
        return new StyleSerializer(legacyHover, hoverMode == JSONOptions.HoverEventValueMode.LEGACY_ONLY || hoverMode == JSONOptions.HoverEventValueMode.BOTH, hoverMode == JSONOptions.HoverEventValueMode.MODERN_ONLY || hoverMode == JSONOptions.HoverEventValueMode.BOTH, features.value(JSONOptions.VALIDATE_STRICT_EVENTS), gson).nullSafe();
    }

    private StyleSerializer(@Nullable LegacyHoverEventSerializer legacyHover, boolean emitLegacyHover, boolean emitModernHover, boolean strictEventValues, Gson gson) {
        this.legacyHover = legacyHover;
        this.emitLegacyHover = emitLegacyHover;
        this.emitModernHover = emitModernHover;
        this.strictEventValues = strictEventValues;
        this.gson = gson;
    }

    public Style read(JsonReader in) throws IOException {
        in.beginObject();
        Style.Builder style = Style.style();
        while (in.hasNext()) {
            String fieldName = in.nextName();
            if (fieldName.equals("font")) {
                style.font((Key)this.gson.fromJson(in, SerializerFactory.KEY_TYPE));
                continue;
            }
            if (fieldName.equals("color")) {
                TextColorWrapper color = (TextColorWrapper)this.gson.fromJson(in, SerializerFactory.COLOR_WRAPPER_TYPE);
                if (color.color != null) {
                    style.color(color.color);
                    continue;
                }
                if (color.decoration == null) continue;
                style.decoration(color.decoration, TextDecoration.State.TRUE);
                continue;
            }
            if (TextDecoration.NAMES.keys().contains(fieldName)) {
                style.decoration((TextDecoration)TextDecoration.NAMES.value((Object)fieldName), GsonHacks.readBoolean(in));
                continue;
            }
            if (fieldName.equals("insertion")) {
                style.insertion(in.nextString());
                continue;
            }
            if (fieldName.equals("clickEvent")) {
                in.beginObject();
                ClickEvent.Action action = null;
                String value = null;
                while (in.hasNext()) {
                    String clickEventField = in.nextName();
                    if (clickEventField.equals("action")) {
                        action = (ClickEvent.Action)this.gson.fromJson(in, SerializerFactory.CLICK_ACTION_TYPE);
                        continue;
                    }
                    if (clickEventField.equals("value")) {
                        if (in.peek() == JsonToken.NULL && this.strictEventValues) {
                            throw ComponentSerializerImpl.notSureHowToDeserialize("value");
                        }
                        value = in.peek() == JsonToken.NULL ? null : in.nextString();
                        continue;
                    }
                    in.skipValue();
                }
                if (action != null && action.readable() && value != null) {
                    style.clickEvent(ClickEvent.clickEvent(action, value));
                }
                in.endObject();
                continue;
            }
            if (fieldName.equals("hoverEvent")) {
                Object value;
                HoverEvent.Action action;
                JsonPrimitive serializedAction;
                JsonObject hoverEventObject = (JsonObject)this.gson.fromJson(in, JsonObject.class);
                if (hoverEventObject == null || (serializedAction = hoverEventObject.getAsJsonPrimitive("action")) == null || !(action = (HoverEvent.Action)this.gson.fromJson((JsonElement)serializedAction, SerializerFactory.HOVER_ACTION_TYPE)).readable()) continue;
                Class actionType = action.type();
                if (hoverEventObject.has("contents")) {
                    @Nullable JsonElement rawValue = hoverEventObject.get("contents");
                    if (GsonHacks.isNullOrEmpty(rawValue)) {
                        if (this.strictEventValues) {
                            throw ComponentSerializerImpl.notSureHowToDeserialize(rawValue);
                        }
                        value = null;
                    } else {
                        value = SerializerFactory.COMPONENT_TYPE.isAssignableFrom(actionType) ? this.gson.fromJson(rawValue, SerializerFactory.COMPONENT_TYPE) : (SerializerFactory.SHOW_ITEM_TYPE.isAssignableFrom(actionType) ? this.gson.fromJson(rawValue, SerializerFactory.SHOW_ITEM_TYPE) : (SerializerFactory.SHOW_ENTITY_TYPE.isAssignableFrom(actionType) ? this.gson.fromJson(rawValue, SerializerFactory.SHOW_ENTITY_TYPE) : null));
                    }
                } else if (hoverEventObject.has("value")) {
                    JsonElement element = hoverEventObject.get("value");
                    if (GsonHacks.isNullOrEmpty(element)) {
                        if (this.strictEventValues) {
                            throw ComponentSerializerImpl.notSureHowToDeserialize(element);
                        }
                        value = null;
                    } else if (SerializerFactory.COMPONENT_TYPE.isAssignableFrom(actionType)) {
                        Component rawValue = (Component)this.gson.fromJson(element, SerializerFactory.COMPONENT_TYPE);
                        value = this.legacyHoverEventContents(action, rawValue);
                    } else {
                        value = SerializerFactory.STRING_TYPE.isAssignableFrom(actionType) ? this.gson.fromJson(element, SerializerFactory.STRING_TYPE) : null;
                    }
                } else {
                    if (this.strictEventValues) {
                        throw ComponentSerializerImpl.notSureHowToDeserialize(hoverEventObject);
                    }
                    value = null;
                }
                if (value == null) continue;
                style.hoverEvent((HoverEventSource)HoverEvent.hoverEvent((HoverEvent.Action)action, value));
                continue;
            }
            in.skipValue();
        }
        in.endObject();
        return style.build();
    }

    private Object legacyHoverEventContents(HoverEvent.Action<?> action, Component rawValue) {
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return rawValue;
        }
        if (this.legacyHover != null) {
            try {
                if (action == HoverEvent.Action.SHOW_ENTITY) {
                    return this.legacyHover.deserializeShowEntity(rawValue, this.decoder());
                }
                if (action == HoverEvent.Action.SHOW_ITEM) {
                    return this.legacyHover.deserializeShowItem(rawValue);
                }
            }
            catch (IOException ex) {
                throw new JsonParseException((Throwable)ex);
            }
        }
        throw new UnsupportedOperationException();
    }

    private Codec.Decoder<Component, String, JsonParseException> decoder() {
        return string -> (Component)this.gson.fromJson(string, SerializerFactory.COMPONENT_TYPE);
    }

    private Codec.Encoder<Component, String, JsonParseException> encoder() {
        return component -> this.gson.toJson(component, SerializerFactory.COMPONENT_TYPE);
    }

    public void write(JsonWriter out, Style value) throws IOException {
        Key font;
        HoverEvent hoverEvent;
        ClickEvent clickEvent;
        String insertion;
        out.beginObject();
        for (TextDecoration decoration : DECORATIONS) {
            TextDecoration.State state = value.decoration(decoration);
            if (state == TextDecoration.State.NOT_SET) continue;
            String name = (String)TextDecoration.NAMES.key((Object)decoration);
            assert (name != null);
            out.name(name);
            out.value(state == TextDecoration.State.TRUE);
        }
        @Nullable TextColor color = value.color();
        if (color != null) {
            out.name("color");
            this.gson.toJson((Object)color, SerializerFactory.COLOR_TYPE, out);
        }
        if ((insertion = value.insertion()) != null) {
            out.name("insertion");
            out.value(insertion);
        }
        if ((clickEvent = value.clickEvent()) != null) {
            out.name("clickEvent");
            out.beginObject();
            out.name("action");
            this.gson.toJson((Object)clickEvent.action(), SerializerFactory.CLICK_ACTION_TYPE, out);
            out.name("value");
            out.value(clickEvent.value());
            out.endObject();
        }
        if ((hoverEvent = value.hoverEvent()) != null && (this.emitModernHover && hoverEvent.action() != HoverEvent.Action.SHOW_ACHIEVEMENT || this.emitLegacyHover)) {
            out.name("hoverEvent");
            out.beginObject();
            out.name("action");
            HoverEvent.Action action = hoverEvent.action();
            this.gson.toJson((Object)action, SerializerFactory.HOVER_ACTION_TYPE, out);
            if (this.emitModernHover && action != HoverEvent.Action.SHOW_ACHIEVEMENT) {
                out.name("contents");
                if (action == HoverEvent.Action.SHOW_ITEM) {
                    this.gson.toJson(hoverEvent.value(), SerializerFactory.SHOW_ITEM_TYPE, out);
                } else if (action == HoverEvent.Action.SHOW_ENTITY) {
                    this.gson.toJson(hoverEvent.value(), SerializerFactory.SHOW_ENTITY_TYPE, out);
                } else if (action == HoverEvent.Action.SHOW_TEXT) {
                    this.gson.toJson(hoverEvent.value(), SerializerFactory.COMPONENT_TYPE, out);
                } else {
                    throw new JsonParseException("Don't know how to serialize " + hoverEvent.value());
                }
            }
            if (this.emitLegacyHover) {
                out.name("value");
                this.serializeLegacyHoverEvent(hoverEvent, out);
            }
            out.endObject();
        }
        if ((font = value.font()) != null) {
            out.name("font");
            this.gson.toJson((Object)font, SerializerFactory.KEY_TYPE, out);
        }
        out.endObject();
    }

    private void serializeLegacyHoverEvent(HoverEvent<?> hoverEvent, JsonWriter out) throws IOException {
        if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            this.gson.toJson(hoverEvent.value(), SerializerFactory.COMPONENT_TYPE, out);
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
            this.gson.toJson(hoverEvent.value(), String.class, out);
        } else if (this.legacyHover != null) {
            Component serialized = null;
            try {
                if (hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
                    serialized = this.legacyHover.serializeShowEntity((HoverEvent.ShowEntity)hoverEvent.value(), this.encoder());
                } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
                    serialized = this.legacyHover.serializeShowItem((HoverEvent.ShowItem)hoverEvent.value());
                }
            }
            catch (IOException ex) {
                throw new JsonSyntaxException((Throwable)ex);
            }
            if (serialized != null) {
                this.gson.toJson((Object)serialized, SerializerFactory.COMPONENT_TYPE, out);
            } else {
                out.nullValue();
            }
        } else {
            out.nullValue();
        }
    }

    static {
        EnumSet<TextDecoration> knownDecorations = EnumSet.allOf(TextDecoration.class);
        for (TextDecoration decoration : DECORATIONS) {
            knownDecorations.remove(decoration);
        }
        if (!knownDecorations.isEmpty()) {
            throw new IllegalStateException("Gson serializer is missing some text decorations: " + knownDecorations);
        }
    }
}

