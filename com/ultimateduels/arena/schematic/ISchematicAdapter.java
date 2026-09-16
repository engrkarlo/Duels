/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Location
 */
package com.ultimateduels.arena.schematic;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.bukkit.Location;

public interface ISchematicAdapter {
    @Nonnull
    public CompletableFuture<Boolean> save(@Nonnull String var1, @Nonnull Location var2, @Nonnull Location var3);

    @Nonnull
    public CompletableFuture<Boolean> paste(@Nonnull String var1, @Nonnull Location var2);
}

