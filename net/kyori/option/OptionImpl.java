/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.kyori.option;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.option.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class OptionImpl<V>
implements Option<V> {
    private static final Set<String> KNOWN_KEYS = ConcurrentHashMap.newKeySet();
    private final String id;
    private final Class<V> type;
    @Nullable
    private final V defaultValue;

    OptionImpl(@NotNull String id, @NotNull Class<V> type, @Nullable V defaultValue) {
        this.id = id;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    static <T> Option<T> option(String id, Class<T> type, @Nullable T defaultValue) {
        if (!KNOWN_KEYS.add(id)) {
            throw new IllegalStateException("Key " + id + " has already been used. Option keys must be unique.");
        }
        return new OptionImpl<T>(Objects.requireNonNull(id, "id"), Objects.requireNonNull(type, "type"), defaultValue);
    }

    @Override
    @NotNull
    public String id() {
        return this.id;
    }

    @Override
    @NotNull
    public Class<V> type() {
        return this.type;
    }

    @Override
    @Nullable
    public V defaultValue() {
        return this.defaultValue;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        OptionImpl that = (OptionImpl)other;
        return Objects.equals(this.id, that.id) && Objects.equals(this.type, that.type);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.type);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{id=" + this.id + ",type=" + this.type + ",defaultValue=" + this.defaultValue + '}';
    }
}

