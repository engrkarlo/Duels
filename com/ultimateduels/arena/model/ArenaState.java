/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.arena.model;

public enum ArenaState {
    SETUP("Setup", "\u00a7e", "\u2699"),
    AVAILABLE("Available", "\u00a7a", "\u2714"),
    IN_USE("In Use", "\u00a7c", "\u2694"),
    REGENERATING("Regenerating", "\u00a76", "\u27f3"),
    DISABLED("Disabled", "\u00a77", "\u2716"),
    ERROR("Error", "\u00a74", "\u26a0");

    private final String displayName;
    private final String colorCode;
    private final String symbol;

    private ArenaState(String displayName, String colorCode, String symbol) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getColoredName() {
        return this.colorCode + this.displayName;
    }

    public String getFormattedStatus() {
        return this.colorCode + this.symbol + " " + this.displayName;
    }

    public boolean canAcceptMatch() {
        return this == AVAILABLE;
    }

    public boolean canModify() {
        return this == SETUP || this == DISABLED;
    }
}

