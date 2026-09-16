/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.boss.BarColor
 */
package com.ultimateduels.models.duel;

import org.bukkit.boss.BarColor;

public enum DuelState {
    INITIALIZING("Initializing", "\u00a77", false, false, false),
    TELEPORTING("Teleporting", "\u00a7d", false, false, false),
    COUNTDOWN("Starting", "\u00a76", false, true, true),
    FIGHTING("Fighting", "\u00a7c", true, true, false),
    ROUND_ENDING("Round Ending", "\u00a7e", false, true, true),
    RESETTING("Resetting", "\u00a7b", false, true, true),
    ENDING("Ending", "\u00a7a", false, true, true),
    ENDED("Ended", "\u00a77", false, false, false),
    CANCELLED("Cancelled", "\u00a74", false, false, false),
    PAUSED("Paused", "\u00a79", false, true, true),
    WAITING_RECONNECT("Waiting", "\u00a7e", false, true, true);

    private final String displayName;
    private final String colorCode;
    private final boolean combatEnabled;
    private final boolean playersInArena;
    private final boolean playersFrozen;

    private DuelState(String displayName, String colorCode, boolean combatEnabled, boolean playersInArena, boolean playersFrozen) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.combatEnabled = combatEnabled;
        this.playersInArena = playersInArena;
        this.playersFrozen = playersFrozen;
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

    public boolean isCombatEnabled() {
        return this.combatEnabled;
    }

    public boolean arePlayersInArena() {
        return this.playersInArena;
    }

    public boolean arePlayersFrozen() {
        return this.playersFrozen;
    }

    public boolean isActive() {
        return this != ENDED && this != CANCELLED && this != INITIALIZING;
    }

    public boolean isTerminal() {
        return this == ENDED || this == CANCELLED;
    }

    public boolean canBeCancelled() {
        return !this.isTerminal();
    }

    public boolean canSpectatorsJoin() {
        return this == COUNTDOWN || this == FIGHTING || this == ROUND_ENDING || this == PAUSED;
    }

    public boolean canPlayerLeave() {
        return this == FIGHTING || this == COUNTDOWN || this == PAUSED;
    }

    public boolean allowsMovement() {
        return !this.playersFrozen && this != TELEPORTING;
    }

    public boolean allowsItemUsage() {
        return this == FIGHTING;
    }

    public boolean isInventoryLocked() {
        return this != FIGHTING;
    }

    public DuelState getNextState() {
        return switch (this.ordinal()) {
            case 0 -> TELEPORTING;
            case 1 -> COUNTDOWN;
            case 2 -> FIGHTING;
            case 3 -> ROUND_ENDING;
            case 4 -> RESETTING;
            case 5 -> COUNTDOWN;
            case 6 -> ENDED;
            case 9 -> FIGHTING;
            case 10 -> FIGHTING;
            default -> null;
        };
    }

    public DuelState getEndState() {
        return ENDING;
    }

    public boolean canTransitionTo(DuelState nextState) {
        if (nextState == this) {
            return true;
        }
        if (this.isTerminal()) {
            return false;
        }
        if (nextState == CANCELLED) {
            return this.canBeCancelled();
        }
        if (nextState == PAUSED) {
            return this == FIGHTING || this == COUNTDOWN;
        }
        if (nextState == WAITING_RECONNECT) {
            return this == FIGHTING;
        }
        return nextState == this.getNextState() || this == ROUND_ENDING && nextState == ENDING || this == RESETTING && nextState == ENDING;
    }

    public int getEstimatedDuration() {
        return switch (this.ordinal()) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> -1;
            case 4 -> 2;
            case 5 -> 2;
            case 6 -> 3;
            default -> -1;
        };
    }

    public String getScoreboardStatus() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "\u00a77Setting up...";
            case 1 -> "\u00a7dTeleporting...";
            case 2 -> "\u00a76Starting...";
            case 3 -> "\u00a7c\u00a7lFIGHT!";
            case 4 -> "\u00a7eRound Over";
            case 5 -> "\u00a7bNext Round...";
            case 6 -> "\u00a7aMatch Complete";
            case 7 -> "\u00a77Match Ended";
            case 8 -> "\u00a74Cancelled";
            case 9 -> "\u00a79PAUSED";
            case 10 -> "\u00a7eWaiting...";
        };
    }

    public BarColor getBossBarColor() {
        return switch (this.ordinal()) {
            case 3 -> BarColor.RED;
            case 2 -> BarColor.YELLOW;
            case 6 -> BarColor.GREEN;
            case 9 -> BarColor.BLUE;
            default -> BarColor.WHITE;
        };
    }

    public static DuelState fromString(String value) {
        if (value == null || value.isEmpty()) {
            return INITIALIZING;
        }
        try {
            return DuelState.valueOf(value.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            return INITIALIZING;
        }
    }

    public static DuelState[] getActiveStates() {
        return new DuelState[]{TELEPORTING, COUNTDOWN, FIGHTING, ROUND_ENDING, RESETTING, ENDING};
    }

    public static DuelState[] getCombatTrackingStates() {
        return new DuelState[]{FIGHTING};
    }
}

