/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 *  org.jetbrains.annotations.NotNull
 */
package net.kyori.option;

import java.util.Map;
import java.util.function.Consumer;
import net.kyori.option.Option;
import net.kyori.option.OptionStateImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface OptionState {
    public static OptionState emptyOptionState() {
        return OptionStateImpl.EMPTY;
    }

    @NotNull
    public static Builder optionState() {
        return new OptionStateImpl.BuilderImpl();
    }

    @NotNull
    public static VersionedBuilder versionedOptionState() {
        return new OptionStateImpl.VersionedBuilderImpl();
    }

    public boolean has(@NotNull Option<?> var1);

    public <V> V value(@NotNull Option<V> var1);

    @ApiStatus.NonExtendable
    public static interface VersionedBuilder {
        @NotNull
        public VersionedBuilder version(int var1, @NotNull Consumer<Builder> var2);

        @NotNull
        public Versioned build();
    }

    @ApiStatus.NonExtendable
    public static interface Builder {
        @NotNull
        public <V> Builder value(@NotNull Option<V> var1, @NotNull V var2);

        @NotNull
        public Builder values(@NotNull OptionState var1);

        @NotNull
        public OptionState build();
    }

    @ApiStatus.NonExtendable
    public static interface Versioned
    extends OptionState {
        @NotNull
        public Map<Integer, OptionState> childStates();

        @NotNull
        public Versioned at(int var1);
    }
}

