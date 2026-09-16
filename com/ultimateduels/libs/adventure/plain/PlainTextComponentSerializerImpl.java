/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.flattener.ComponentFlattener
 *  net.kyori.adventure.text.flattener.ComponentFlattener$Builder
 *  net.kyori.adventure.util.Services
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.libs.adventure.plain;

import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializer;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.util.Services;
import org.jetbrains.annotations.NotNull;

final class PlainTextComponentSerializerImpl
implements PlainTextComponentSerializer {
    private static final ComponentFlattener DEFAULT_FLATTENER = (ComponentFlattener)((ComponentFlattener.Builder)ComponentFlattener.basic().toBuilder()).unknownMapper(component -> {
        throw new UnsupportedOperationException("Don't know how to turn " + component.getClass().getSimpleName() + " into a string");
    }).build();
    private static final Optional<PlainTextComponentSerializer.Provider> SERVICE = Services.service(PlainTextComponentSerializer.Provider.class);
    static final Consumer<PlainTextComponentSerializer.Builder> BUILDER = SERVICE.map(PlainTextComponentSerializer.Provider::plainText).orElseGet(() -> builder -> {});
    final ComponentFlattener flattener;

    PlainTextComponentSerializerImpl(ComponentFlattener flattener) {
        this.flattener = flattener;
    }

    @Override
    public void serialize(@NotNull StringBuilder sb, @NotNull Component component) {
        this.flattener.flatten(Objects.requireNonNull(component, "component"), sb::append);
    }

    @NotNull
    public PlainTextComponentSerializer.Builder toBuilder() {
        return new BuilderImpl(this);
    }

    static /* synthetic */ Optional access$000() {
        return SERVICE;
    }

    static final class BuilderImpl
    implements PlainTextComponentSerializer.Builder {
        private ComponentFlattener flattener = PlainTextComponentSerializerImpl.access$100();

        BuilderImpl() {
            BUILDER.accept(this);
        }

        BuilderImpl(PlainTextComponentSerializerImpl serializer) {
            this();
            this.flattener = serializer.flattener;
        }

        @Override
        public @NotNull PlainTextComponentSerializer.Builder flattener(@NotNull ComponentFlattener flattener) {
            this.flattener = Objects.requireNonNull(flattener, "flattener");
            return this;
        }

        @NotNull
        public PlainTextComponentSerializer build() {
            return new PlainTextComponentSerializerImpl(this.flattener);
        }
    }

    static final class Instances {
        static final PlainTextComponentSerializer INSTANCE = PlainTextComponentSerializerImpl.access$000().map(PlainTextComponentSerializer.Provider::plainTextSimple).orElseGet(() -> new PlainTextComponentSerializerImpl(DEFAULT_FLATTENER));

        Instances() {
        }
    }
}

