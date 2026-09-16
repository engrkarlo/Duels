/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.visuals;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.libs.adventure.gson.GsonComponentSerializer;
import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class HealthPacketSender {
    private static boolean initialized = false;
    private static boolean available = false;
    private static UltimateDuels plugin;
    private static Constructor<?> ctorSpawnPacket;
    private static Constructor<?> ctorRemovePacket;
    private static Constructor<?> ctorMetaPacket;
    private static Object streamCodecTeleport;
    private static Method streamCodecDecode;
    private static Class<?> friendlyByteBufClass;
    private static Constructor<?> ctorFriendlyByteBuf;
    private static Class<?> byteBufClass;
    private static Method unpooledBufferMethod;
    private static Constructor<?> ctorEntityDataAccessor;
    private static Method methodDataValueCreate;
    private static Object serByte;
    private static Object serInt;
    private static Object serFloat;
    private static Object serComponent;
    private static Object entityTypeTextDisplay;
    private static Object vec3Zero;
    private static Class<?> entityTypeClass;
    private static Method getHandleMethod;
    private static Field connectionField;
    private static Method sendMethod;
    private static Method paperAdventureAsVanilla;
    private static Method fbWriteVarInt;
    private static Method fbWriteDouble;
    private static Method fbWriteByte;
    private static Method fbWriteBoolean;
    private static final int IDX_INTERPOLATION_DELAY = 8;
    private static final int IDX_TRANSFORM_DURATION = 9;
    private static final int IDX_BILLBOARD = 15;
    private static final int IDX_VIEW_RANGE = 17;
    private static final int IDX_TEXT = 23;
    private static final int IDX_LINE_WIDTH = 24;
    private static final int IDX_BG_COLOR = 25;
    private static final int IDX_TEXT_OPACITY = 26;
    private static final int IDX_SHADOW_FLAGS = 27;
    private static final byte BILLBOARD_FIXED = 0;
    private static final byte BILLBOARD_VERTICAL = 1;
    private static final byte BILLBOARD_HORIZONTAL = 2;
    private static final byte BILLBOARD_CENTER = 3;

    private HealthPacketSender() {
    }

    public static void init(UltimateDuels pluginInstance) {
        if (initialized) {
            return;
        }
        initialized = true;
        plugin = pluginInstance;
        try {
            HealthPacketSender.initReflection();
            available = true;
            plugin.getLogger().info("[HealthPacketSender] Initialized successfully (pure reflection, no NMS compile dependency)");
        }
        catch (Exception e) {
            available = false;
            plugin.getLogger().log(Level.WARNING, "[HealthPacketSender] Failed to initialize \u2014 health displays disabled. Error: " + e.getMessage(), e);
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    private static void initReflection() throws Exception {
        String cbPkg = HealthPacketSender.getCraftBukkitPackage();
        Class<?> craftPlayerClass = Class.forName(cbPkg + ".entity.CraftPlayer");
        getHandleMethod = craftPlayerClass.getMethod("getHandle", new Class[0]);
        Class<?> entityPlayerClass = getHandleMethod.getReturnType();
        connectionField = HealthPacketSender.findFieldByTypeName(entityPlayerClass, "ServerGamePacketListener");
        if (connectionField == null) {
            connectionField = HealthPacketSender.findFieldByTypeName(entityPlayerClass, "ServerCommonPacketListener");
        }
        if (connectionField == null) {
            for (Field field : HealthPacketSender.getAllFields(entityPlayerClass)) {
                int hasSend = 0;
                for (Method method : field.getType().getMethods()) {
                    if (!method.getName().equals("send") || method.getParameterCount() != 1) continue;
                    hasSend = 1;
                    break;
                }
                if (hasSend == 0) continue;
                connectionField = field;
                break;
            }
        }
        if (connectionField == null) {
            throw new Exception("Could not locate connection field on " + entityPlayerClass.getName());
        }
        connectionField.setAccessible(true);
        Class<?> connectionClass = connectionField.getType();
        sendMethod = null;
        for (Method m : connectionClass.getMethods()) {
            if (!m.getName().equals("send") || m.getParameterCount() != 1) continue;
            sendMethod = m;
            break;
        }
        if (sendMethod == null) {
            throw new Exception("Could not find send(Packet) on " + connectionClass.getName());
        }
        sendMethod.setAccessible(true);
        Class<?> clazz = Class.forName("net.minecraft.world.phys.Vec3");
        vec3Zero = getStaticField(clazz, "ZERO");
        entityTypeClass = Class.forName("net.minecraft.world.entity.EntityType");
        entityTypeTextDisplay = getTextDisplayEntityType(entityTypeClass);
        Class<?> addEntityPacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
        ctorSpawnPacket = addEntityPacketClass.getDeclaredConstructor(Integer.TYPE, UUID.class, Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE, entityTypeClass, Integer.TYPE, clazz, Double.TYPE);
        ctorSpawnPacket.setAccessible(true);
        Class<?> removePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
        ctorRemovePacket = removePacketClass.getDeclaredConstructor(int[].class);
        ctorRemovePacket.setAccessible(true);
        Class<?> metaPacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
        ctorMetaPacket = metaPacketClass.getDeclaredConstructor(Integer.TYPE, List.class);
        ctorMetaPacket.setAccessible(true);
        Class<?> dataValueClass = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataValue");
        methodDataValueCreate = null;
        for (Method m : dataValueClass.getMethods()) {
            if (!m.getName().equals("create") || m.getParameterCount() != 2) continue;
            methodDataValueCreate = m;
            break;
        }
        if (methodDataValueCreate == null) {
            throw new Exception("Could not find SynchedEntityData.DataValue.create()");
        }
        Class<?> clazz2 = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");
        Class<?> entityDataAccessorClass = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
        ctorEntityDataAccessor = entityDataAccessorClass.getDeclaredConstructor(Integer.TYPE, clazz2);
        ctorEntityDataAccessor.setAccessible(true);
        Class<?> serializersClass = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        serByte = getStaticField(serializersClass, "BYTE");
        serInt = getStaticField(serializersClass, "INT");
        serFloat = getStaticField(serializersClass, "FLOAT");
        serComponent = getStaticField(serializersClass, "COMPONENT");
        Class<?> paperAdventureClass = Class.forName("io.papermc.paper.adventure.PaperAdventure");
        paperAdventureAsVanilla = paperAdventureClass.getMethod("asVanilla", Component.class);
        paperAdventureAsVanilla.setAccessible(true);
        byteBufClass = Class.forName("io.netty.buffer.ByteBuf");
        friendlyByteBufClass = Class.forName("net.minecraft.network.FriendlyByteBuf");
        ctorFriendlyByteBuf = friendlyByteBufClass.getDeclaredConstructor(byteBufClass);
        ctorFriendlyByteBuf.setAccessible(true);
        Class<?> unpooledClass = Class.forName("io.netty.buffer.Unpooled");
        unpooledBufferMethod = unpooledClass.getMethod("buffer", new Class[0]);
        fbWriteVarInt = friendlyByteBufClass.getMethod("writeVarInt", Integer.TYPE);
        fbWriteDouble = friendlyByteBufClass.getMethod("writeDouble", Double.TYPE);
        fbWriteByte = friendlyByteBufClass.getMethod("writeByte", Integer.TYPE);
        fbWriteBoolean = friendlyByteBufClass.getMethod("writeBoolean", Boolean.TYPE);
        Class<?> teleportClass = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
        streamCodecTeleport = getStaticField(teleportClass, "STREAM_CODEC");
        for (Method method : streamCodecTeleport.getClass().getMethods()) {
            if (!method.getName().equals("decode") || method.getParameterCount() != 1) continue;
            streamCodecDecode = method;
            break;
        }
        if (streamCodecDecode == null) {
            for (GenericDeclaration genericDeclaration : streamCodecTeleport.getClass().getInterfaces()) {
                for (Method m : ((Class)genericDeclaration).getMethods()) {
                    if (!m.getName().equals("decode") || m.getParameterCount() != 1) continue;
                    streamCodecDecode = m;
                    break;
                }
                if (streamCodecDecode != null) break;
            }
        }
        if (streamCodecDecode == null) {
            throw new Exception("Could not find STREAM_CODEC.decode() for teleport packet");
        }
        streamCodecDecode.setAccessible(true);
        plugin.getLogger().info("[HealthPacketSender] All reflection handles acquired");
    }

    public static void sendSpawn(Player viewer, int entityId, UUID entityUUID, Location loc, Component text, float viewRange, float scale) {
        if (!available) {
            return;
        }
        try {
            HealthPacketSender.rawSend(viewer, HealthPacketSender.buildSpawnPacket(entityId, entityUUID, loc));
            HealthPacketSender.rawSend(viewer, HealthPacketSender.buildMetaPacket(entityId, text, viewRange));
            HealthPacketSender.rawSend(viewer, HealthPacketSender.buildTeleportPacket(entityId, loc));
        }
        catch (Exception e) {
            HealthPacketSender.debug("sendSpawn failed for " + viewer.getName() + ": " + e.getMessage());
        }
    }

    public static void sendMetadata(Player viewer, int entityId, Component text, float viewRange, float scale) {
        if (!available) {
            return;
        }
        try {
            HealthPacketSender.rawSend(viewer, HealthPacketSender.buildMetaPacket(entityId, text, viewRange));
        }
        catch (Exception e) {
            HealthPacketSender.debug("sendMetadata failed: " + e.getMessage());
        }
    }

    public static void sendTeleport(Player viewer, int entityId, Location loc) {
        if (!available) {
            return;
        }
        try {
            HealthPacketSender.rawSend(viewer, HealthPacketSender.buildTeleportPacket(entityId, loc));
        }
        catch (Exception e) {
            HealthPacketSender.debug("sendTeleport failed: " + e.getMessage());
        }
    }

    public static void sendDestroy(Player viewer, int entityId) {
        if (!available) {
            return;
        }
        try {
            HealthPacketSender.rawSend(viewer, HealthPacketSender.buildDestroyPacket(entityId));
        }
        catch (Exception e) {
            HealthPacketSender.debug("sendDestroy failed: " + e.getMessage());
        }
    }

    private static Object buildSpawnPacket(int entityId, UUID uuid, Location loc) throws Exception {
        return ctorSpawnPacket.newInstance(entityId, uuid, loc.getX(), loc.getY(), loc.getZ(), Float.valueOf(0.0f), Float.valueOf(0.0f), entityTypeTextDisplay, 0, vec3Zero, 0.0);
    }

    private static Object buildMetaPacket(int entityId, Component text, float viewRange) throws Exception {
        Object nmsText = HealthPacketSender.toNmsComponent(text);
        ArrayList<Object> values = new ArrayList<Object>();
        values.add(HealthPacketSender.mkDataValue(23, serComponent, nmsText));
        values.add(HealthPacketSender.mkDataValue(15, serByte, (byte)1));
        values.add(HealthPacketSender.mkDataValue(17, serFloat, Float.valueOf(viewRange)));
        values.add(HealthPacketSender.mkDataValue(25, serInt, 0));
        values.add(HealthPacketSender.mkDataValue(26, serByte, (byte)-1));
        values.add(HealthPacketSender.mkDataValue(24, serInt, 200));
        values.add(HealthPacketSender.mkDataValue(27, serByte, (byte)0));
        values.add(HealthPacketSender.mkDataValue(8, serInt, 0));
        values.add(HealthPacketSender.mkDataValue(9, serInt, 0));
        return ctorMetaPacket.newInstance(entityId, values);
    }

    private static Object buildTeleportPacket(int entityId, Location loc) throws Exception {
        Object rawBuf = unpooledBufferMethod.invoke(null, new Object[0]);
        Object fb = ctorFriendlyByteBuf.newInstance(rawBuf);
        fbWriteVarInt.invoke(fb, entityId);
        fbWriteDouble.invoke(fb, loc.getX());
        fbWriteDouble.invoke(fb, loc.getY());
        fbWriteDouble.invoke(fb, loc.getZ());
        fbWriteByte.invoke(fb, 0);
        fbWriteByte.invoke(fb, 0);
        fbWriteBoolean.invoke(fb, false);
        Object packet = streamCodecDecode.invoke(streamCodecTeleport, fb);
        Method release = byteBufClass.getMethod("release", new Class[0]);
        release.invoke(rawBuf, new Object[0]);
        return packet;
    }

    private static Object buildDestroyPacket(int entityId) throws Exception {
        return ctorRemovePacket.newInstance(new Object[]{new int[]{entityId}});
    }

    private static Object mkDataValue(int index, Object serializer, Object value) throws Exception {
        Object accessor = ctorEntityDataAccessor.newInstance(index, serializer);
        return methodDataValueCreate.invoke(null, accessor, value);
    }

    private static Object toNmsComponent(Component component) throws Exception {
        try {
            return paperAdventureAsVanilla.invoke(null, component);
        }
        catch (Exception e) {
            HealthPacketSender.debug("PaperAdventure.asVanilla failed, trying JSON fallback: " + e.getMessage());
            String json = (String)GsonComponentSerializer.gson().serialize(component);
            Class<?> nmsComponentClass = Class.forName("net.minecraft.network.chat.Component");
            Class<?> serializerClass = Class.forName("net.minecraft.network.chat.Component$Serializer");
            for (Method m : serializerClass.getMethods()) {
                if (!m.getName().equals("fromJson") && !m.getName().equals("fromJsonLenient") || m.getParameterCount() != 1 || m.getParameterTypes()[0] != String.class) continue;
                m.setAccessible(true);
                return m.invoke(null, json);
            }
            String plain = PlainTextComponentSerializer.plainText().serialize(component);
            Method literal = nmsComponentClass.getMethod("literal", String.class);
            return literal.invoke(null, plain);
        }
    }

    private static void rawSend(Player player, Object packet) throws Exception {
        Object entityPlayer = getHandleMethod.invoke((Object)player, new Object[0]);
        Object connection = connectionField.get(entityPlayer);
        sendMethod.invoke(connection, packet);
    }

    private static String getCraftBukkitPackage() {
        try {
            Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            return "org.bukkit.craftbukkit";
        }
        catch (ClassNotFoundException classNotFoundException) {
            String serverPkg = Bukkit.getServer().getClass().getPackage().getName();
            return serverPkg.replace(".CraftServer", "").replace("CraftServer", "org.bukkit.craftbukkit");
        }
    }

    private static Object getTextDisplayEntityType(Class<?> type) throws Exception {
        try {
            return getStaticField(type, "TEXT_DISPLAY");
        } catch (NoSuchFieldException ignored) {
            Method byString = type.getMethod("byString", String.class);
            Object result = byString.invoke(null, "minecraft:text_display");
            if (result instanceof java.util.Optional) {
                java.util.Optional<?> optional = (java.util.Optional<?>) result;
                if (optional.isPresent()) return optional.get();
            }
            throw new NoSuchFieldException("Could not resolve minecraft:text_display from EntityType registry");
        }
    }

    private static Object getStaticField(Class<?> type, String name) throws Exception {
        try {
            Field field = type.getField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException ignored) {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        }
    }

    private static Field findFieldByTypeName(Class<?> clazz, String typeNameContains) {
        for (Field f : HealthPacketSender.getAllFields(clazz)) {
            if (!f.getType().getName().contains(typeNameContains)) continue;
            f.setAccessible(true);
            return f;
        }
        return null;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<Field>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field f : current.getDeclaredFields()) {
                fields.add(f);
            }
        }
        return fields;
    }

    private static void debug(String msg) {
        if (plugin != null) {
            plugin.debug("[HealthPacketSender] " + msg);
        }
    }
}

