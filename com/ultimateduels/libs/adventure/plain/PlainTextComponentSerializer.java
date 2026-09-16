/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.builder.AbstractBuilder
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.flattener.ComponentFlattener
 *  net.kyori.adventure.text.serializer.ComponentSerializer
 *  net.kyori.adventure.util.Buildable
 *  net.kyori.adventure.util.Buildable$Builder
 *  net.kyori.adventure.util.PlatformAPI
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.libs.adventure.plain;

import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializerImpl;
import java.util.function.Consumer;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.util.Buildable;
import net.kyori.adventure.util.PlatformAPI;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface PlainTextComponentSerializer
extends ComponentSerializer<Component, TextComponent, String>,
Buildable<PlainTextComponentSerializer, Builder> {
    @NotNull
    public static PlainTextComponentSerializer plainText() {
        return PlainTextComponentSerializerImpl.Instances.INSTANCE;
    }

    public static @NotNull Builder builder() {
        return new PlainTextComponentSerializerImpl.BuilderImpl();
    }

    @NotNull
    default public TextComponent deserialize(@NotNull String input) {
        return Component.text((String)input);
    }

    @NotNull
    default public String serialize(@NotNull Component component) {
        StringBuilder sb = new StringBuilder();
        this.serialize(sb, component);
        return sb.toString();
    }

    public void serialize(@NotNull StringBuilder var1, @NotNull Component var2);

    @PlatformAPI
    @ApiStatus.Internal
    public static interface Provider {
        @PlatformAPI
        @ApiStatus.Internal
        @NotNull
        public PlainTextComponentSerializer plainTextSimple();

        @PlatformAPI
        @ApiStatus.Internal
        @NotNull
        public Consumer<Builder> plainText();
    }

    public static interface Builder
    extends AbstractBuilder<PlainTextComponentSerializer>,
    Buildable.Builder<PlainTextComponentSerializer> {
        @NotNull
        public Builder flattener(@NotNull ComponentFlattener var1);
    }
}

