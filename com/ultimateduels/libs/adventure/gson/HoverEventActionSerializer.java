/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  net.kyori.adventure.text.event.HoverEvent$Action
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.TypeAdapter;
import com.ultimateduels.libs.adventure.gson.IndexedSerializer;
import net.kyori.adventure.text.event.HoverEvent;

final class HoverEventActionSerializer {
    static final TypeAdapter<HoverEvent.Action<?>> INSTANCE = IndexedSerializer.lenient("hover action", HoverEvent.Action.NAMES);

    private HoverEventActionSerializer() {
    }
}

