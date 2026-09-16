/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.api;

import com.ultimateduels.api.UltimateDuelsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UltimateDuelsAPIProvider {
    private static UltimateDuelsAPI instance;

    private UltimateDuelsAPIProvider() {
    }

    @NotNull
    public static UltimateDuelsAPI get() {
        if (instance == null) {
            throw new IllegalStateException("UltimateDuels API is not loaded yet!");
        }
        return instance;
    }

    @Nullable
    public static UltimateDuelsAPI getOrNull() {
        return instance;
    }

    public static boolean isLoaded() {
        return instance != null;
    }

    static void register(@NotNull UltimateDuelsAPI api) {
        instance = api;
    }

    static void unregister() {
        instance = null;
    }
}

