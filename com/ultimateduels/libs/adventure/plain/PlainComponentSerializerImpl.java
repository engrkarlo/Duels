/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.KeybindComponent
 *  net.kyori.adventure.text.TranslatableComponent
 *  net.kyori.adventure.text.flattener.ComponentFlattener
 *  net.kyori.adventure.text.flattener.ComponentFlattener$Builder
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.plain;

import com.ultimateduels.libs.adventure.plain.PlainComponentSerializer;
import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializer;
import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializerImpl;
import java.util.function.Function;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
final class PlainComponentSerializerImpl {
    @Deprecated
    static final PlainComponentSerializer INSTANCE = new PlainComponentSerializer();

    private PlainComponentSerializerImpl() {
    }

    @Deprecated
    static PlainTextComponentSerializer createRealSerializerFromLegacyFunctions(@Nullable Function<KeybindComponent, String> keybind, @Nullable Function<TranslatableComponent, String> translatable) {
        if (keybind == null && translatable == null) {
            return PlainTextComponentSerializer.plainText();
        }
        ComponentFlattener.Builder builder = (ComponentFlattener.Builder)ComponentFlattener.basic().toBuilder();
        if (keybind != null) {
            builder.mapper(KeybindComponent.class, keybind);
        }
        if (translatable != null) {
            builder.mapper(TranslatableComponent.class, translatable);
        }
        return (PlainTextComponentSerializer)PlainTextComponentSerializer.builder().flattener((ComponentFlattener)builder.build()).build();
    }

    @Deprecated
    static final class BuilderImpl
    implements PlainComponentSerializer.Builder {
        private final PlainTextComponentSerializer.Builder builder = PlainTextComponentSerializer.builder();

        @Deprecated
        BuilderImpl() {
        }

        @Deprecated
        BuilderImpl(PlainComponentSerializer serializer) {
            this.builder.flattener(((PlainTextComponentSerializerImpl)serializer.serializer).flattener);
        }

        @Override
        public @NotNull PlainComponentSerializer.Builder flattener(@NotNull ComponentFlattener flattener) {
            this.builder.flattener(flattener);
            return this;
        }

        @NotNull
        public PlainComponentSerializer build() {
            return new PlainComponentSerializer((PlainTextComponentSerializer)this.builder.build());
        }
    }
}

