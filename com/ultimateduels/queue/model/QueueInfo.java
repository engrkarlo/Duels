/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.queue.model;

import javax.annotation.Nonnull;

public record QueueInfo(@Nonnull String kitName, int inQueue, int fighting) {
    public int getTotal() {
        return this.inQueue + this.fighting;
    }

    @Nonnull
    public String getQueueLore() {
        return "\u00a77In Queue: \u00a7e" + this.inQueue;
    }

    @Nonnull
    public String getFightingLore() {
        return "\u00a77Fighting: \u00a7c" + this.fighting;
    }
}

