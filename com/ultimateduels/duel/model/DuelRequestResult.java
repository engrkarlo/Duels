/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.duel.model;

import javax.annotation.Nonnull;

public record DuelRequestResult(boolean success, @Nonnull String message) {
    public static DuelRequestResult success(@Nonnull String message) {
        return new DuelRequestResult(true, message);
    }

    public static DuelRequestResult failure(@Nonnull String message) {
        return new DuelRequestResult(false, message);
    }
}

