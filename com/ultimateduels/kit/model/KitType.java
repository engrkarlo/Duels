/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.kit.model;

public enum KitType {
    ADMIN("Admin", "\u00a7c"),
    PLAYER_CUSTOM("Custom", "\u00a7a"),
    TEMPLATE("Template", "\u00a7e"),
    TEMPORARY("Temporary", "\u00a77");

    private final String displayName;
    private final String colorCode;

    private KitType(String displayName, String colorCode) {
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

