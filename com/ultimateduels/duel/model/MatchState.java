/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.duel.model;

public enum MatchState {
    PENDING("Pending", "\u00a7e", "\u23f3"),
    COUNTDOWN("Countdown", "\u00a76", "\u23f3"),
    STARTING("Starting", "\u00a76", "\u23f3"),
    IN_PROGRESS("In Progress", "\u00a7a", "\u2694"),
    ROUND_ENDING("Round Ending", "\u00a7b", "\u23f8"),
    RESETTING("Resetting", "\u00a7d", "\u27f3"),
    ENDING("Ending", "\u00a7c", "\u23f8"),
    ENDED("Ended", "\u00a77", "\u2713"),
    COMPLETED("Completed", "\u00a77", "\u2713"),
    CANCELLED("Cancelled", "\u00a74", "\u2717");

    private final String displayName;
    private final String colorCode;
    private final String icon;

    private MatchState(String displayName, String colorCode, String icon) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.icon = icon;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getFormattedName() {
        return this.colorCode + this.displayName;
    }

    public String getFormattedNameWithIcon() {
        return this.colorCode + this.icon + " " + this.displayName;
    }

    public boolean isFighting() {
        return this == IN_PROGRESS;
    }

    public boolean isStarting() {
        return this == COUNTDOWN || this == STARTING;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == ENDED;
    }

    public boolean isEnding() {
        return this == ENDING || this == ROUND_ENDING;
    }

    public boolean canCancel() {
        return this == PENDING || this == COUNTDOWN || this == STARTING || this == IN_PROGRESS || this == ROUND_ENDING;
    }

    public boolean canTakeDamage() {
        return this == IN_PROGRESS;
    }

    public boolean canMove() {
        return this != COUNTDOWN && this != STARTING && this != RESETTING;
    }

    public boolean isWaiting() {
        return this == PENDING;
    }

    public boolean isTransitioning() {
        return this == ROUND_ENDING || this == RESETTING || this == ENDING;
    }

    public MatchState getNextState() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> COUNTDOWN;
            case 1 -> STARTING;
            case 2 -> IN_PROGRESS;
            case 3 -> ROUND_ENDING;
            case 4 -> RESETTING;
            case 5 -> IN_PROGRESS;
            case 6 -> COMPLETED;
            case 7 -> COMPLETED;
            case 8 -> COMPLETED;
            case 9 -> CANCELLED;
        };
    }

    public static MatchState fromString(String name) {
        if (name == null || name.isEmpty()) {
            return PENDING;
        }
        try {
            return MatchState.valueOf(name.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            for (MatchState state : MatchState.values()) {
                if (!state.getDisplayName().equalsIgnoreCase(name)) continue;
                return state;
            }
            return PENDING;
        }
    }

    public boolean isBefore(MatchState other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isAfter(MatchState other) {
        return this.ordinal() > other.ordinal();
    }
}

