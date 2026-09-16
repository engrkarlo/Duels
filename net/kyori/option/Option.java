/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.kyori.option;

import net.kyori.option.OptionImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface Option<V> {
    public static Option<Boolean> booleanOption(String id, boolean defaultValue) {
        return OptionImpl.option(id, Boolean.class, defaultValue);
    }

    public static <E extends Enum<E>> Option<E> enumOption(String id, Class<E> enumClazz, E defaultValue) {
        return OptionImpl.option(id, enumClazz, defaultValue);
    }

    @NotNull
    public String id();

    @NotNull
    public Class<V> type();

    @Nullable
    public V defaultValue();
}

