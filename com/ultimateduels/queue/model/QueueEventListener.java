/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.queue.model;

import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.queue.model.PartyQueueEntry;
import com.ultimateduels.queue.model.QueueEntry;
import javax.annotation.Nonnull;

public interface QueueEventListener {
    default public void onQueueJoin(@Nonnull QueueEntry entry) {
    }

    default public void onQueueLeave(@Nonnull QueueEntry entry) {
    }

    default public void onMatchFound(@Nonnull QueueEntry entry1, @Nonnull QueueEntry entry2, @Nonnull DuelArena arena) {
    }

    default public void onPartyMatchFound(@Nonnull PartyQueueEntry entry1, @Nonnull PartyQueueEntry entry2, @Nonnull DuelArena arena) {
    }
}

