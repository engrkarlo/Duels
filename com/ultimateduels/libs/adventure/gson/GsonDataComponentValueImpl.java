/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  net.kyori.adventure.internal.Internals
 *  net.kyori.adventure.text.event.DataComponentValue$Removed
 *  net.kyori.examination.Examinable
 *  net.kyori.examination.ExaminableProperty
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.ultimateduels.libs.adventure.gson.GsonDataComponentValue;
import java.util.Objects;
import java.util.stream.Stream;
import net.kyori.adventure.internal.Internals;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GsonDataComponentValueImpl
implements GsonDataComponentValue {
    private final JsonElement element;

    GsonDataComponentValueImpl(@NotNull JsonElement element) {
        this.element = element;
    }

    @Override
    @NotNull
    public JsonElement element() {
        return this.element;
    }

    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of((String)"element", (Object)this.element));
    }

    public String toString() {
        return Internals.toString((Examinable)this);
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        GsonDataComponentValueImpl that = (GsonDataComponentValueImpl)other;
        return Objects.equals(this.element, that.element);
    }

    public int hashCode() {
        return Objects.hashCode(this.element);
    }

    static final class RemovedGsonComponentValueImpl
    extends GsonDataComponentValueImpl
    implements DataComponentValue.Removed {
        static final RemovedGsonComponentValueImpl INSTANCE = new RemovedGsonComponentValueImpl();

        private RemovedGsonComponentValueImpl() {
            super((JsonElement)JsonNull.INSTANCE);
        }
    }
}

