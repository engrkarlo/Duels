/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.builder.AbstractBuilder
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.KeybindComponent
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.TranslatableComponent
 *  net.kyori.adventure.text.flattener.ComponentFlattener
 *  net.kyori.adventure.text.serializer.ComponentSerializer
 *  net.kyori.adventure.util.Buildable
 *  net.kyori.adventure.util.Buildable$Builder
 *  org.jetbrains.annotations.ApiStatus$ScheduledForRemoval
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.plain;

import com.ultimateduels.libs.adventure.plain.PlainComponentSerializerImpl;
import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializer;
import java.util.function.Function;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
public class PlainComponentSerializer
implements ComponentSerializer<Component, TextComponent, String>,
Buildable<PlainComponentSerializer, Builder> {
    @Deprecated
    final PlainTextComponentSerializer serializer;

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    @NotNull
    public static PlainComponentSerializer plain() {
        return PlainComponentSerializerImpl.INSTANCE;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    public static @NotNull Builder builder() {
        return new PlainComponentSerializerImpl.BuilderImpl();
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    public PlainComponentSerializer() {
        this(PlainTextComponentSerializer.plainText());
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    public PlainComponentSerializer(@Nullable Function<KeybindComponent, String> keybind, @Nullable Function<TranslatableComponent, String> translatable) {
        this(PlainComponentSerializerImpl.createRealSerializerFromLegacyFunctions(keybind, translatable));
    }

    @Deprecated
    PlainComponentSerializer(@NotNull PlainTextComponentSerializer serializer) {
        this.serializer = serializer;
    }

    @NotNull
    public TextComponent deserialize(@NotNull String input) {
        return this.serializer.deserialize(input);
    }

    @NotNull
    public String serialize(@NotNull Component component) {
        return this.serializer.serialize(component);
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    public void serialize(@NotNull StringBuilder sb, @NotNull Component component) {
        this.serializer.serialize(sb, component);
    }

    public @NotNull Builder toBuilder() {
        return new PlainComponentSerializerImpl.BuilderImpl(this);
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    public static interface Builder
    extends AbstractBuilder<PlainComponentSerializer>,
    Buildable.Builder<PlainComponentSerializer> {
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
        @NotNull
        public Builder flattener(@NotNull ComponentFlattener var1);
    }
}

