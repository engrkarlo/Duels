/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.queue.model;

public enum QueueType {
    SOLO("Solo", "\u00a7a"),
    PARTY("Party", "\u00a7d"),
    RANKED("Ranked", "\u00a76"),
    CUSTOM("Custom", "\u00a7e");

    private final String displayName;
    private final String colorCode;

    private QueueType(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public String getFormattedName() {
        return this.colorCode + this.displayName;
    }
}

