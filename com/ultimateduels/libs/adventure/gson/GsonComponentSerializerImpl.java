/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.TypeAdapterFactory
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.util.Services
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapterFactory;
import com.ultimateduels.libs.adventure.gson.ComponentSerializerImpl;
import com.ultimateduels.libs.adventure.gson.GsonComponentSerializer;
import com.ultimateduels.libs.adventure.gson.SerializerFactory;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.util.Services;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GsonComponentSerializerImpl
implements GsonComponentSerializer {
    private static final Optional<GsonComponentSerializer.Provider> SERVICE = Services.service(GsonComponentSerializer.Provider.class);
    static final Consumer<GsonComponentSerializer.Builder> BUILDER = SERVICE.map(GsonComponentSerializer.Provider::builder).orElseGet(() -> builder -> {});
    private final Gson serializer;
    private final UnaryOperator<GsonBuilder> populator;
    private final @Nullable LegacyHoverEventSerializer legacyHoverSerializer;
    private final OptionState flags;

    GsonComponentSerializerImpl(OptionState flags, @Nullable LegacyHoverEventSerializer legacyHoverSerializer) {
        this.flags = flags;
        this.legacyHoverSerializer = legacyHoverSerializer;
        this.populator = builder -> {
            builder.registerTypeAdapterFactory((TypeAdapterFactory)new SerializerFactory(flags, legacyHoverSerializer));
            return builder;
        };
        this.serializer = ((GsonBuilder)this.populator.apply(new GsonBuilder().disableHtmlEscaping())).create();
    }

    @Override
    @NotNull
    public Gson serializer() {
        return this.serializer;
    }

    @Override
    @NotNull
    public UnaryOperator<GsonBuilder> populator() {
        return this.populator;
    }

    @NotNull
    public Component deserialize(@NotNull String string) {
        Component component = (Component)this.serializer().fromJson(string, Component.class);
        if (component == null) {
            throw ComponentSerializerImpl.notSureHowToDeserialize(string);
        }
        return component;
    }

    @Nullable
    public Component deserializeOr(@Nullable String input, @Nullable Component fallback) {
        if (input == null) {
            return fallback;
        }
        Component component = (Component)this.serializer().fromJson(input, Component.class);
        if (component == null) {
            return fallback;
        }
        return component;
    }

    @NotNull
    public String serialize(@NotNull Component component) {
        return this.serializer().toJson((Object)component);
    }

    @Override
    @NotNull
    public Component deserializeFromTree(@NotNull JsonElement input) {
        Component component = (Component)this.serializer().fromJson(input, Component.class);
        if (component == null) {
            throw ComponentSerializerImpl.notSureHowToDeserialize(input);
        }
        return component;
    }

    @Override
    @NotNull
    public JsonElement serializeToTree(@NotNull Component component) {
        return this.serializer().toJsonTree((Object)component);
    }

    @NotNull
    public GsonComponentSerializer.Builder toBuilder() {
        return new BuilderImpl(this);
    }

    static /* synthetic */ Optional access$000() {
        return SERVICE;
    }

    static final class BuilderImpl
    implements GsonComponentSerializer.Builder {
        private OptionState flags = JSONOptions.byDataVersion();
        private @Nullable LegacyHoverEventSerializer legacyHoverSerializer;

        BuilderImpl() {
            BUILDER.accept(this);
        }

        BuilderImpl(GsonComponentSerializerImpl serializer) {
            this();
            this.flags = serializer.flags;
            this.legacyHoverSerializer = serializer.legacyHoverSerializer;
        }

        @Override
        @NotNull
        public GsonComponentSerializer.Builder options(@NotNull OptionState flags) {
            this.flags = Objects.requireNonNull(flags, "flags");
            return this;
        }

        @Override
        @NotNull
        public GsonComponentSerializer.Builder editOptions(@NotNull Consumer<OptionState.Builder> optionEditor) {
            OptionState.Builder builder = OptionState.optionState().values(this.flags);
            Objects.requireNonNull(optionEditor, "flagEditor").accept(builder);
            this.flags = builder.build();
            return this;
        }

        @Override
        @NotNull
        public GsonComponentSerializer.Builder legacyHoverEventSerializer(@Nullable LegacyHoverEventSerializer serializer) {
            this.legacyHoverSerializer = serializer;
            return this;
        }

        @Override
        @NotNull
        public GsonComponentSerializer build() {
            return new GsonComponentSerializerImpl(this.flags, this.legacyHoverSerializer);
        }
    }

    static final class Instances {
        static final GsonComponentSerializer INSTANCE = GsonComponentSerializerImpl.access$000().map(GsonComponentSerializer.Provider::gson).orElseGet(() -> new GsonComponentSerializerImpl(JSONOptions.byDataVersion(), null));
        static final GsonComponentSerializer LEGACY_INSTANCE = GsonComponentSerializerImpl.access$000().map(GsonComponentSerializer.Provider::gsonLegacy).orElseGet(() -> new GsonComponentSerializerImpl(JSONOptions.byDataVersion().at(2525), null));

        Instances() {
        }
    }
}

