/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  net.kyori.adventure.text.event.DataComponentValue
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.ultimateduels.libs.adventure.gson.GsonDataComponentValueImpl;
import java.util.Objects;
import net.kyori.adventure.text.event.DataComponentValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface GsonDataComponentValue
extends DataComponentValue {
    public static GsonDataComponentValue gsonDataComponentValue(@NotNull JsonElement data) {
        if (data instanceof JsonNull) {
            return GsonDataComponentValueImpl.RemovedGsonComponentValueImpl.INSTANCE;
        }
        return new GsonDataComponentValueImpl(Objects.requireNonNull(data, "data"));
    }

    @NotNull
    public JsonElement element();
}

