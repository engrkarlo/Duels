/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.TypeAdapter
 *  com.google.gson.TypeAdapterFactory
 *  com.google.gson.reflect.TypeToken
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.BlockNBTComponent$Pos
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TranslationArgument
 *  net.kyori.adventure.text.event.ClickEvent$Action
 *  net.kyori.adventure.text.event.HoverEvent$Action
 *  net.kyori.adventure.text.event.HoverEvent$ShowEntity
 *  net.kyori.adventure.text.event.HoverEvent$ShowItem
 *  net.kyori.adventure.text.format.Style
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.ultimateduels.libs.adventure.gson.BlockNBTComponentPosSerializer;
import com.ultimateduels.libs.adventure.gson.ClickEventActionSerializer;
import com.ultimateduels.libs.adventure.gson.ComponentSerializerImpl;
import com.ultimateduels.libs.adventure.gson.HoverEventActionSerializer;
import com.ultimateduels.libs.adventure.gson.KeySerializer;
import com.ultimateduels.libs.adventure.gson.ShowEntitySerializer;
import com.ultimateduels.libs.adventure.gson.ShowItemSerializer;
import com.ultimateduels.libs.adventure.gson.StyleSerializer;
import com.ultimateduels.libs.adventure.gson.TextColorSerializer;
import com.ultimateduels.libs.adventure.gson.TextColorWrapper;
import com.ultimateduels.libs.adventure.gson.TextDecorationSerializer;
import com.ultimateduels.libs.adventure.gson.TranslationArgumentSerializer;
import com.ultimateduels.libs.adventure.gson.UUIDSerializer;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.Nullable;

final class SerializerFactory
implements TypeAdapterFactory {
    static final Class<Key> KEY_TYPE = Key.class;
    static final Class<Component> COMPONENT_TYPE = Component.class;
    static final Class<Style> STYLE_TYPE = Style.class;
    static final Class<ClickEvent.Action> CLICK_ACTION_TYPE = ClickEvent.Action.class;
    static final Class<HoverEvent.Action> HOVER_ACTION_TYPE = HoverEvent.Action.class;
    static final Class<HoverEvent.ShowItem> SHOW_ITEM_TYPE = HoverEvent.ShowItem.class;
    static final Class<HoverEvent.ShowEntity> SHOW_ENTITY_TYPE = HoverEvent.ShowEntity.class;
    static final Class<String> STRING_TYPE = String.class;
    static final Class<TextColorWrapper> COLOR_WRAPPER_TYPE = TextColorWrapper.class;
    static final Class<TextColor> COLOR_TYPE = TextColor.class;
    static final Class<TextDecoration> TEXT_DECORATION_TYPE = TextDecoration.class;
    static final Class<BlockNBTComponent.Pos> BLOCK_NBT_POS_TYPE = BlockNBTComponent.Pos.class;
    static final Class<UUID> UUID_TYPE = UUID.class;
    static final Class<TranslationArgument> TRANSLATION_ARGUMENT_TYPE = TranslationArgument.class;
    private final OptionState features;
    private final LegacyHoverEventSerializer legacyHoverSerializer;

    SerializerFactory(OptionState features, @Nullable LegacyHoverEventSerializer legacyHoverSerializer) {
        this.features = features;
        this.legacyHoverSerializer = legacyHoverSerializer;
    }

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class rawType = type.getRawType();
        if (COMPONENT_TYPE.isAssignableFrom(rawType)) {
            return ComponentSerializerImpl.create(this.features, gson);
        }
        if (KEY_TYPE.isAssignableFrom(rawType)) {
            return KeySerializer.INSTANCE;
        }
        if (STYLE_TYPE.isAssignableFrom(rawType)) {
            return StyleSerializer.create(this.legacyHoverSerializer, this.features, gson);
        }
        if (CLICK_ACTION_TYPE.isAssignableFrom(rawType)) {
            return ClickEventActionSerializer.INSTANCE;
        }
        if (HOVER_ACTION_TYPE.isAssignableFrom(rawType)) {
            return HoverEventActionSerializer.INSTANCE;
        }
        if (SHOW_ITEM_TYPE.isAssignableFrom(rawType)) {
            return ShowItemSerializer.create(gson, this.features);
        }
        if (SHOW_ENTITY_TYPE.isAssignableFrom(rawType)) {
            return ShowEntitySerializer.create(gson);
        }
        if (COLOR_WRAPPER_TYPE.isAssignableFrom(rawType)) {
            return TextColorWrapper.Serializer.INSTANCE;
        }
        if (COLOR_TYPE.isAssignableFrom(rawType)) {
            return this.features.value(JSONOptions.EMIT_RGB) != false ? TextColorSerializer.INSTANCE : TextColorSerializer.DOWNSAMPLE_COLOR;
        }
        if (TEXT_DECORATION_TYPE.isAssignableFrom(rawType)) {
            return TextDecorationSerializer.INSTANCE;
        }
        if (BLOCK_NBT_POS_TYPE.isAssignableFrom(rawType)) {
            return BlockNBTComponentPosSerializer.INSTANCE;
        }
        if (UUID_TYPE.isAssignableFrom(rawType)) {
            return UUIDSerializer.uuidSerializer(this.features);
        }
        if (TRANSLATION_ARGUMENT_TYPE.isAssignableFrom(rawType)) {
            return TranslationArgumentSerializer.create(gson);
        }
        return null;
    }
}

