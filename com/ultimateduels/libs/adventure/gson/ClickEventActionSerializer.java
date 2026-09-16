/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  net.kyori.adventure.text.event.ClickEvent$Action
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.TypeAdapter;
import com.ultimateduels.libs.adventure.gson.IndexedSerializer;
import net.kyori.adventure.text.event.ClickEvent;

final class ClickEventActionSerializer {
    static final TypeAdapter<ClickEvent.Action> INSTANCE = IndexedSerializer.lenient("click action", ClickEvent.Action.NAMES);

    private ClickEventActionSerializer() {
    }
}

