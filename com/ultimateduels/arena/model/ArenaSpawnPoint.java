/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Location
 */
package com.ultimateduels.arena.model;

import javax.annotation.Nonnull;
import org.bukkit.Location;

public class ArenaSpawnPoint {
    private Location location;
    private int index;
    private SpawnPointType type;
    private int teamId;

    public ArenaSpawnPoint(@Nonnull Location location, int index) {
        this.location = location;
        this.index = index;
        this.type = SpawnPointType.PLAYER;
        this.teamId = 0;
    }

    public ArenaSpawnPoint(@Nonnull Location location, int index, @Nonnull SpawnPointType type) {
        this.location = location;
        this.index = index;
        this.type = type;
        this.teamId = type == SpawnPointType.TEAM_1 ? 1 : (type == SpawnPointType.TEAM_2 ? 2 : 0);
    }

    @Nonnull
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(@Nonnull Location location) {
        this.location = location;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Nonnull
    public SpawnPointType getType() {
        return this.type;
    }

    public void setType(@Nonnull SpawnPointType type) {
        this.type = type;
    }

    public int getTeamId() {
        return this.teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    @Nonnull
    public Location getLocationFacing(@Nonnull Location center) {
        Location loc = this.location.clone();
        double dx = center.getX() - loc.getX();
        double dz = center.getZ() - loc.getZ();
        float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        loc.setYaw(yaw);
        loc.setPitch(0.0f);
        return loc;
    }

    public String toString() {
        return "ArenaSpawnPoint{index=" + this.index + ", type=" + String.valueOf((Object)this.type) + ", teamId=" + this.teamId + ", location=" + (this.location != null ? String.format("%.1f, %.1f, %.1f", this.location.getX(), this.location.getY(), this.location.getZ()) : "null") + "}";
    }

    public static enum SpawnPointType {
        PLAYER,
        TEAM_1,
        TEAM_2,
        SPECTATOR,
        RESPAWN,
        FFA;

    }
}

