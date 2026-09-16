/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.ffa.model;

public enum FFAResultType {
    SUCCESS("Success"),
    DISABLED("FFA Disabled"),
    ALREADY_IN_FFA("Already In FFA"),
    NOT_IN_FFA("Not In FFA"),
    IN_MATCH("In Match"),
    IN_QUEUE("In Queue"),
    INVALID_ARENA("Invalid Arena"),
    ARENA_DISABLED("Arena Disabled"),
    NO_ARENA("No Arena Available"),
    INVALID_KIT("Invalid Kit"),
    ON_COOLDOWN("On Cooldown"),
    WORLD_RESTRICTED("World Restricted"),
    INTERNAL_ERROR("Internal Error"),
    TELEPORT_FAILED("Teleport Failed"),
    NO_SPAWN_POINTS("No spawn points");

    private final String displayName;

    private FFAResultType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

