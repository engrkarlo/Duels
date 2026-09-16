/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package net.kyori.adventure.text.serializer.json;

import net.kyori.option.Option;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.NotNull;

public final class JSONOptions {
    private static final int VERSION_INITIAL = 0;
    private static final int VERSION_1_16 = 2526;
    private static final int VERSION_1_20_3 = 3679;
    private static final int VERSION_1_20_5 = 3819;
    public static final Option<Boolean> EMIT_RGB = Option.booleanOption(JSONOptions.key("emit/rgb"), true);
    public static final Option<HoverEventValueMode> EMIT_HOVER_EVENT_TYPE = Option.enumOption(JSONOptions.key("emit/hover_value_mode"), HoverEventValueMode.class, HoverEventValueMode.MODERN_ONLY);
    public static final Option<Boolean> EMIT_COMPACT_TEXT_COMPONENT = Option.booleanOption(JSONOptions.key("emit/compact_text_component"), true);
    public static final Option<Boolean> EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY = Option.booleanOption(JSONOptions.key("emit/hover_show_entity_id_as_int_array"), true);
    public static final Option<Boolean> VALIDATE_STRICT_EVENTS = Option.booleanOption(JSONOptions.key("validate/strict_events"), true);
    public static final Option<Boolean> EMIT_DEFAULT_ITEM_HOVER_QUANTITY = Option.booleanOption(JSONOptions.key("emit/default_item_hover_quantity"), true);
    public static final Option<ShowItemHoverDataMode> SHOW_ITEM_HOVER_DATA_MODE = Option.enumOption(JSONOptions.key("emit/show_item_hover_data"), ShowItemHoverDataMode.class, ShowItemHoverDataMode.EMIT_EITHER);
    private static final OptionState.Versioned BY_DATA_VERSION = OptionState.versionedOptionState().version(0, b -> b.value(EMIT_HOVER_EVENT_TYPE, HoverEventValueMode.LEGACY_ONLY).value(EMIT_RGB, false).value(EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, false).value(VALIDATE_STRICT_EVENTS, false).value(EMIT_DEFAULT_ITEM_HOVER_QUANTITY, false).value(SHOW_ITEM_HOVER_DATA_MODE, ShowItemHoverDataMode.EMIT_LEGACY_NBT)).version(2526, b -> b.value(EMIT_HOVER_EVENT_TYPE, HoverEventValueMode.MODERN_ONLY).value(EMIT_RGB, true)).version(3679, b -> b.value(EMIT_COMPACT_TEXT_COMPONENT, true).value(EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, true).value(VALIDATE_STRICT_EVENTS, true)).version(3819, b -> b.value(EMIT_DEFAULT_ITEM_HOVER_QUANTITY, true).value(SHOW_ITEM_HOVER_DATA_MODE, ShowItemHoverDataMode.EMIT_DATA_COMPONENTS)).build();
    private static final OptionState MOST_COMPATIBLE = OptionState.optionState().value(EMIT_HOVER_EVENT_TYPE, HoverEventValueMode.BOTH).value(EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, false).value(EMIT_COMPACT_TEXT_COMPONENT, false).value(VALIDATE_STRICT_EVENTS, false).value(SHOW_ITEM_HOVER_DATA_MODE, ShowItemHoverDataMode.EMIT_EITHER).build();

    private JSONOptions() {
    }

    private static String key(String value) {
        return "adventure:json/" + value;
    }

    public static @NotNull OptionState.Versioned byDataVersion() {
        return BY_DATA_VERSION;
    }

    @NotNull
    public static OptionState compatibility() {
        return MOST_COMPATIBLE;
    }

    public static enum ShowItemHoverDataMode {
        EMIT_LEGACY_NBT,
        EMIT_DATA_COMPONENTS,
        EMIT_EITHER;

    }

    public static enum HoverEventValueMode {
        MODERN_ONLY,
        LEGACY_ONLY,
        BOTH;

    }
}

