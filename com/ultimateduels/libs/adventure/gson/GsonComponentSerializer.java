/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  net.kyori.adventure.builder.AbstractBuilder
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.util.Buildable
 *  net.kyori.adventure.util.Buildable$Builder
 *  net.kyori.adventure.util.PlatformAPI
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ultimateduels.libs.adventure.gson.GsonComponentSerializerImpl;
import com.ultimateduels.libs.adventure.gson.LegacyHoverEventSerializer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.util.Buildable;
import net.kyori.adventure.util.PlatformAPI;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GsonComponentSerializer
extends JSONComponentSerializer,
Buildable<GsonComponentSerializer, Builder> {
    @NotNull
    public static GsonComponentSerializer gson() {
        return GsonComponentSerializerImpl.Instances.INSTANCE;
    }

    @NotNull
    public static GsonComponentSerializer colorDownsamplingGson() {
        return GsonComponentSerializerImpl.Instances.LEGACY_INSTANCE;
    }

    public static Builder builder() {
        return new GsonComponentSerializerImpl.BuilderImpl();
    }

    @NotNull
    public Gson serializer();

    @NotNull
    public UnaryOperator<GsonBuilder> populator();

    @NotNull
    public Component deserializeFromTree(@NotNull JsonElement var1);

    @NotNull
    public JsonElement serializeToTree(@NotNull Component var1);

    @PlatformAPI
    @ApiStatus.Internal
    public static interface Provider {
        @PlatformAPI
        @ApiStatus.Internal
        @NotNull
        public GsonComponentSerializer gson();

        @PlatformAPI
        @ApiStatus.Internal
        @NotNull
        public GsonComponentSerializer gsonLegacy();

        @PlatformAPI
        @ApiStatus.Internal
        @NotNull
        public Consumer<Builder> builder();
    }

    public static interface Builder
    extends AbstractBuilder<GsonComponentSerializer>,
    Buildable.Builder<GsonComponentSerializer>,
    JSONComponentSerializer.Builder {
        @Override
        @NotNull
        public Builder options(@NotNull OptionState var1);

        @Override
        @NotNull
        public Builder editOptions(@NotNull Consumer<OptionState.Builder> var1);

        @Override
        @NotNull
        default public Builder downsampleColors() {
            return this.editOptions(features -> features.value(JSONOptions.EMIT_RGB, false));
        }

        @Deprecated
        @NotNull
        default public Builder legacyHoverEventSerializer(@Nullable LegacyHoverEventSerializer serializer) {
            return this.legacyHoverEventSerializer((net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer)serializer);
        }

        @Override
        @NotNull
        public Builder legacyHoverEventSerializer(@Nullable net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer var1);

        @Override
        @Deprecated
        @NotNull
        default public Builder emitLegacyHoverEvent() {
            return this.editOptions(b -> b.value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.BOTH));
        }

        @Override
        @NotNull
        public GsonComponentSerializer build();
    }
}

