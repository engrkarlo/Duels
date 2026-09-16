/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.queue.model;

import com.ultimateduels.queue.model.QueueResultType;
import javax.annotation.Nonnull;

public record QueueResult(boolean success, @Nonnull QueueResultType type, @Nonnull String message) {
    public static QueueResult success(@Nonnull String message) {
        return new QueueResult(true, QueueResultType.SUCCESS, message);
    }

    public static QueueResult failure(@Nonnull QueueResultType type, @Nonnull String message) {
        return new QueueResult(false, type, message);
    }
}

