/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.arena;

public enum ArenaState {
    AVAILABLE("Available", "\u00a7a", true, false),
    IN_USE("In Use", "\u00a7c", false, true),
    REGENERATING("Regenerating", "\u00a7e", false, false),
    DISABLED("Disabled", "\u00a77", false, false),
    COUNTDOWN("Starting", "\u00a76", false, true),
    SETUP_INCOMPLETE("Setup Incomplete", "\u00a74", false, false),
    MAINTENANCE("Maintenance", "\u00a75", false, false),
    RESERVED("Reserved", "\u00a79", false, false),
    COOLDOWN("Cooldown", "\u00a77", false, false),
    WORLD_UNLOADED("World Unloaded", "\u00a78", false, false);

    private final String displayName;
    private final String colorCode;
    private final boolean canAcceptMatch;
    private final boolean hasActivePlayers;

    private ArenaState(String displayName, String colorCode, boolean canAcceptMatch, boolean hasActivePlayers) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.canAcceptMatch = canAcceptMatch;
        this.hasActivePlayers = hasActivePlayers;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public String getColoredName() {
        return this.colorCode + this.displayName;
    }

    public boolean canAcceptMatch() {
        return this.canAcceptMatch;
    }

    public boolean hasActivePlayers() {
        return this.hasActivePlayers;
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isOccupied() {
        return this == IN_USE || this == COUNTDOWN;
    }

    public boolean isTemporarilyUnavailable() {
        return this == REGENERATING || this == COOLDOWN || this == RESERVED;
    }

    public boolean isPermanentlyUnavailable() {
        return this == DISABLED || this == SETUP_INCOMPLETE || this == MAINTENANCE || this == WORLD_UNLOADED;
    }

    public boolean isVisibleInGui() {
        return this != WORLD_UNLOADED && this != SETUP_INCOMPLETE;
    }

    public boolean canTransitionTo(ArenaState nextState) {
        if (nextState == this) {
            return true;
        }
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (nextState == IN_USE || nextState == COUNTDOWN || nextState == RESERVED || nextState == DISABLED || nextState == MAINTENANCE) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (nextState == COOLDOWN || nextState == REGENERATING || nextState == AVAILABLE) {
                    yield true;
                }
                yield false;
            }
            case 2 -> {
                if (nextState == AVAILABLE || nextState == DISABLED) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (nextState == AVAILABLE || nextState == MAINTENANCE) {
                    yield true;
                }
                yield false;
            }
            case 4 -> {
                if (nextState == IN_USE || nextState == AVAILABLE) {
                    yield true;
                }
                yield false;
            }
            case 5 -> {
                if (nextState == AVAILABLE || nextState == DISABLED) {
                    yield true;
                }
                yield false;
            }
            case 6 -> {
                if (nextState == AVAILABLE || nextState == DISABLED) {
                    yield true;
                }
                yield false;
            }
            case 7 -> {
                if (nextState == COUNTDOWN || nextState == AVAILABLE) {
                    yield true;
                }
                yield false;
            }
            case 8 -> {
                if (nextState == REGENERATING || nextState == AVAILABLE) {
                    yield true;
                }
                yield false;
            }
            case 9 -> nextState == AVAILABLE || nextState == DISABLED;
        };
    }

    public ArenaState getNextMatchFlowState() {
        return switch (this.ordinal()) {
            case 0 -> COUNTDOWN;
            case 4 -> IN_USE;
            case 1 -> COOLDOWN;
            case 8 -> REGENERATING;
            case 2 -> AVAILABLE;
            default -> null;
        };
    }

    public String getIconMaterial() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "LIME_WOOL";
            case 1 -> "RED_WOOL";
            case 2 -> "YELLOW_WOOL";
            case 3 -> "GRAY_WOOL";
            case 4 -> "ORANGE_WOOL";
            case 5 -> "BLACK_WOOL";
            case 6 -> "PURPLE_WOOL";
            case 7 -> "BLUE_WOOL";
            case 8 -> "LIGHT_GRAY_WOOL";
            case 9 -> "BEDROCK";
        };
    }

    public String getDescription() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Ready for matches";
            case 1 -> "Match in progress";
            case 2 -> "Arena resetting...";
            case 3 -> "Disabled by admin";
            case 4 -> "Match starting soon";
            case 5 -> "Configuration needed";
            case 6 -> "Under maintenance";
            case 7 -> "Reserved for match";
            case 8 -> "Cooling down";
            case 9 -> "World not loaded";
        };
    }

    public int getEstimatedWaitTime() {
        return switch (this.ordinal()) {
            case 2 -> 5;
            case 8 -> 2;
            case 4 -> 3;
            default -> -1;
        };
    }

    public boolean autoTransitions() {
        return this == COOLDOWN || this == REGENERATING;
    }

    public static ArenaState fromString(String value) {
        if (value == null || value.isEmpty()) {
            return AVAILABLE;
        }
        try {
            return ArenaState.valueOf(value.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            return AVAILABLE;
        }
    }

    public static ArenaState[] getUsableStates() {
        return new ArenaState[]{AVAILABLE};
    }

    public static ArenaState[] getBusyStates() {
        return new ArenaState[]{IN_USE, COUNTDOWN, REGENERATING, COOLDOWN};
    }

    public static ArenaState[] getAttentionStates() {
        return new ArenaState[]{SETUP_INCOMPLETE, WORLD_UNLOADED};
    }
}

