/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.ffa.model;

import com.ultimateduels.ffa.model.FFAResultType;
import javax.annotation.Nonnull;

public record FFAResult(boolean success, @Nonnull FFAResultType type, @Nonnull String message) {
    public static FFAResult success(@Nonnull String message) {
        return new FFAResult(true, FFAResultType.SUCCESS, message);
    }

    public static FFAResult failure(@Nonnull FFAResultType type, @Nonnull String message) {
        return new FFAResult(false, type, message);
    }
}

