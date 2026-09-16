/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.util.Codec$Decoder
 *  net.kyori.adventure.util.Codec$Encoder
 *  org.jetbrains.annotations.NotNull
 */
package net.kyori.adventure.text.serializer.json;

import java.io.IOException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.NotNull;

public interface LegacyHoverEventSerializer {
    public // Could not load outer class - annotation placement on inner may be incorrect
     @NotNull HoverEvent.ShowItem deserializeShowItem(@NotNull Component var1) throws IOException;

    @NotNull
    public Component serializeShowItem(// Could not load outer class - annotation placement on inner may be incorrect
     @NotNull HoverEvent.ShowItem var1) throws IOException;

    public // Could not load outer class - annotation placement on inner may be incorrect
     @NotNull HoverEvent.ShowEntity deserializeShowEntity(@NotNull Component var1, Codec.Decoder<Component, String, ? extends RuntimeException> var2) throws IOException;

    @NotNull
    public Component serializeShowEntity(// Could not load outer class - annotation placement on inner may be incorrect
     @NotNull HoverEvent.ShowEntity var1, Codec.Encoder<Component, String, ? extends RuntimeException> var2) throws IOException;
}

