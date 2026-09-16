/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.queue.model;

public enum QueueResultType {
    SUCCESS("Success"),
    QUEUE_DISABLED("Queue Disabled"),
    ALREADY_IN_QUEUE("Already In Queue"),
    NOT_IN_QUEUE("Not In Queue"),
    IN_MATCH("In Match"),
    IN_FFA("In FFA"),
    IN_PARTY("In Party"),
    INVALID_KIT("Invalid Kit"),
    NO_ARENAS("No Arenas Available"),
    ON_COOLDOWN("On Cooldown"),
    MEMBER_IN_QUEUE("Party Member In Queue"),
    MEMBER_IN_MATCH("Party Member In Match"),
    PARTY_TOO_SMALL("Party Too Small"),
    PARTY_TOO_LARGE("Party Too Large"),
    WORLD_RESTRICTED("World Restricted"),
    INTERNAL_ERROR("Internal Error");

    private final String displayName;

    private QueueResultType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

