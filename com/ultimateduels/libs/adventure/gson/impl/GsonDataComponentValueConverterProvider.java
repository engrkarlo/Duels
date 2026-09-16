/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  net.kyori.adventure.key.Key
 *  net.kyori.adventure.text.event.DataComponentValue$Removed
 *  net.kyori.adventure.text.event.DataComponentValueConverterRegistry$Conversion
 *  net.kyori.adventure.text.event.DataComponentValueConverterRegistry$Provider
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.libs.adventure.gson.impl;

import com.google.auto.service.AutoService;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.ultimateduels.libs.adventure.gson.GsonDataComponentValue;
import java.util.Collections;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@AutoService(value={DataComponentValueConverterRegistry.Provider.class})
@ApiStatus.Internal
public final class GsonDataComponentValueConverterProvider
implements DataComponentValueConverterRegistry.Provider {
    private static final Key ID = Key.key((String)"adventure", (String)"serializer/gson");

    @NotNull
    public Key id() {
        return ID;
    }

    @NotNull
    public Iterable<DataComponentValueConverterRegistry.Conversion<?, ?>> conversions() {
        return Collections.singletonList(DataComponentValueConverterRegistry.Conversion.convert(DataComponentValue.Removed.class, GsonDataComponentValue.class, (k, removed) -> GsonDataComponentValue.gsonDataComponentValue((JsonElement)JsonNull.INSTANCE)));
    }
}

