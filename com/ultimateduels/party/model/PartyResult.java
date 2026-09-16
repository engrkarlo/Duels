/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.party.model;

import com.ultimateduels.party.model.PartyResultType;
import javax.annotation.Nonnull;

public record PartyResult(boolean success, @Nonnull PartyResultType type, @Nonnull String message) {
    public static PartyResult success(@Nonnull String message) {
        return new PartyResult(true, PartyResultType.SUCCESS, message);
    }

    public static PartyResult failure(@Nonnull PartyResultType type, @Nonnull String message) {
        return new PartyResult(false, type, message);
    }
}

