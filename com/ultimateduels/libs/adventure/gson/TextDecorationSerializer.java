/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.TypeAdapter
 *  net.kyori.adventure.text.format.TextDecoration
 */
package com.ultimateduels.libs.adventure.gson;

import com.google.gson.TypeAdapter;
import com.ultimateduels.libs.adventure.gson.IndexedSerializer;
import net.kyori.adventure.text.format.TextDecoration;

final class TextDecorationSerializer {
    static final TypeAdapter<TextDecoration> INSTANCE = IndexedSerializer.strict("text decoration", TextDecoration.NAMES);

    private TextDecorationSerializer() {
    }
}

