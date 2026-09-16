/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.ffa.model;

import javax.annotation.Nonnull;

public record FFAInfo(@Nonnull String arenaName, @Nonnull String kitName, int playerCount, boolean enabled) {
    @Nonnull
    public String getPlayersLore() {
        return "\u00a77Players: \u00a7e" + this.playerCount;
    }

    @Nonnull
    public String getKitLore() {
        return "\u00a77Kit: \u00a7e" + this.kitName;
    }

    @Nonnull
    public String getStatusLore() {
        return this.enabled ? "\u00a7aOnline" : "\u00a7cOffline";
    }
}

