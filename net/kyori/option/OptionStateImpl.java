/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.kyori.option;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import net.kyori.option.Option;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class OptionStateImpl
implements OptionState {
    static final OptionState EMPTY = new OptionStateImpl(new IdentityHashMap());
    private final IdentityHashMap<Option<?>, Object> values;

    OptionStateImpl(IdentityHashMap<Option<?>, Object> values) {
        this.values = new IdentityHashMap(values);
    }

    @Override
    public boolean has(@NotNull Option<?> option) {
        return this.values.containsKey(Objects.requireNonNull(option, "flag"));
    }

    @Override
    public <V> V value(@NotNull Option<V> option) {
        V value = option.type().cast(this.values.get(Objects.requireNonNull(option, "flag")));
        return value == null ? option.defaultValue() : value;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        OptionStateImpl that = (OptionStateImpl)other;
        return Objects.equals(this.values, that.values);
    }

    public int hashCode() {
        return Objects.hash(this.values);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{values=" + this.values + '}';
    }

    static final class VersionedBuilderImpl
    implements OptionState.VersionedBuilder {
        private final Map<Integer, BuilderImpl> builders = new TreeMap<Integer, BuilderImpl>();

        VersionedBuilderImpl() {
        }

        @Override
        public @NotNull OptionState.Versioned build() {
            if (this.builders.isEmpty()) {
                return new VersionedImpl(Collections.emptySortedMap(), 0, OptionState.emptyOptionState());
            }
            TreeMap<Integer, OptionState> built = new TreeMap<Integer, OptionState>();
            for (Map.Entry<Integer, BuilderImpl> entry : this.builders.entrySet()) {
                built.put(entry.getKey(), entry.getValue().build());
            }
            return new VersionedImpl(built, (Integer)built.lastKey(), VersionedImpl.flattened(built, (Integer)built.lastKey()));
        }

        @Override
        @NotNull
        public OptionState.VersionedBuilder version(int version, @NotNull Consumer<OptionState.Builder> versionBuilder) {
            Objects.requireNonNull(versionBuilder, "versionBuilder").accept(this.builders.computeIfAbsent(version, $ -> new BuilderImpl()));
            return this;
        }
    }

    static final class BuilderImpl
    implements OptionState.Builder {
        private final IdentityHashMap<Option<?>, Object> values = new IdentityHashMap();

        BuilderImpl() {
        }

        @Override
        @NotNull
        public OptionState build() {
            if (this.values.isEmpty()) {
                return EMPTY;
            }
            return new OptionStateImpl(this.values);
        }

        @Override
        @NotNull
        public <V> OptionState.Builder value(@NotNull Option<V> option, @NotNull V value) {
            this.values.put(Objects.requireNonNull(option, "flag"), Objects.requireNonNull(value, "value"));
            return this;
        }

        @Override
        @NotNull
        public OptionState.Builder values(@NotNull OptionState existing) {
            if (existing instanceof OptionStateImpl) {
                this.values.putAll(((OptionStateImpl)existing).values);
            } else if (existing instanceof VersionedImpl) {
                this.values.putAll(((OptionStateImpl)((VersionedImpl)existing).filtered).values);
            } else {
                throw new IllegalArgumentException("existing set " + existing + " is of an unknown implementation type");
            }
            return this;
        }
    }

    static final class VersionedImpl
    implements OptionState.Versioned {
        private final SortedMap<Integer, OptionState> sets;
        private final int targetVersion;
        private final OptionState filtered;

        VersionedImpl(SortedMap<Integer, OptionState> sets, int targetVersion, OptionState filtered) {
            this.sets = sets;
            this.targetVersion = targetVersion;
            this.filtered = filtered;
        }

        @Override
        public boolean has(@NotNull Option<?> option) {
            return this.filtered.has(option);
        }

        @Override
        public <V> V value(@NotNull Option<V> option) {
            return this.filtered.value(option);
        }

        @Override
        @NotNull
        public Map<Integer, OptionState> childStates() {
            return Collections.unmodifiableSortedMap(this.sets.headMap(this.targetVersion + 1));
        }

        @Override
        @NotNull
        public OptionState.Versioned at(int version) {
            return new VersionedImpl(this.sets, version, VersionedImpl.flattened(this.sets, version));
        }

        public static OptionState flattened(SortedMap<Integer, OptionState> versions, int targetVersion) {
            SortedMap<Integer, OptionState> applicable = versions.headMap(targetVersion + 1);
            OptionState.Builder builder = OptionState.optionState();
            for (OptionState child : applicable.values()) {
                builder.values(child);
            }
            return builder.build();
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || this.getClass() != other.getClass()) {
                return false;
            }
            VersionedImpl that = (VersionedImpl)other;
            return this.targetVersion == that.targetVersion && Objects.equals(this.sets, that.sets) && Objects.equals(this.filtered, that.filtered);
        }

        public int hashCode() {
            return Objects.hash(this.sets, this.targetVersion, this.filtered);
        }

        public String toString() {
            return this.getClass().getSimpleName() + "{sets=" + this.sets + ", targetVersion=" + this.targetVersion + ", filtered=" + this.filtered + '}';
        }
    }
}

