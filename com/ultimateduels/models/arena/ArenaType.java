/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.arena;

public enum ArenaType {
    DUEL("Duel Arena", "\u00a76", 2, true, false),
    FFA("FFA Arena", "\u00a7c", -1, false, true),
    PARTY("Party Arena", "\u00a7d", 10, true, false),
    EVENT("Event Arena", "\u00a7b", -1, true, false),
    SUMO("Sumo Arena", "\u00a76", 2, true, false),
    BRIDGE("Bridge Arena", "\u00a79", 8, true, false),
    SPLEEF("Spleef Arena", "\u00a7f", 4, true, false),
    BOXING("Boxing Arena", "\u00a7c", 2, true, false);

    private final String displayName;
    private final String colorCode;
    private final int maxSpawns;
    private final boolean regenerateAfterMatch;
    private final boolean isContinuous;

    private ArenaType(String displayName, String colorCode, int maxSpawns, boolean regenerateAfterMatch, boolean isContinuous) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.maxSpawns = maxSpawns;
        this.regenerateAfterMatch = regenerateAfterMatch;
        this.isContinuous = isContinuous;
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

    public int getMaxSpawns() {
        return this.maxSpawns;
    }

    public boolean shouldRegenerateAfterMatch() {
        return this.regenerateAfterMatch;
    }

    public boolean isContinuous() {
        return this.isContinuous;
    }

    public boolean isStandardDuel() {
        return this == DUEL;
    }

    public boolean isFFA() {
        return this == FFA;
    }

    public boolean isTeamBased() {
        return this == PARTY || this == BRIDGE;
    }

    public boolean supportsVoidDeath() {
        return this == SUMO || this == SPLEEF || this == BRIDGE;
    }

    public boolean supportsBuilding() {
        return this == BRIDGE || this == SPLEEF;
    }

    public boolean usesHitCounter() {
        return this == BOXING;
    }

    public boolean hasGoals() {
        return this == BRIDGE;
    }

    public boolean isDamageDisabled() {
        return this == SUMO || this == BOXING;
    }

    public int getMinimumSpawns() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 4, 7 -> 2;
            case 1 -> 4;
            case 2, 5 -> 4;
            case 6 -> 2;
            case 3 -> 1;
        };
    }

    public boolean requiresSpectatorSpawn() {
        return this != FFA;
    }

    public int getDefaultCountdown() {
        return switch (this.ordinal()) {
            case 4, 7 -> 3;
            case 1 -> 0;
            case 5 -> 5;
            default -> 3;
        };
    }

    public String getStateMachineType() {
        return switch (this.ordinal()) {
            case 1 -> "ffa";
            case 5 -> "bridge";
            case 6 -> "spleef";
            case 4, 7 -> "special";
            default -> "standard";
        };
    }

    public boolean canBeQueued() {
        return this != EVENT && this != FFA;
    }

    public boolean supportsMultiRound() {
        return this == DUEL || this == SUMO || this == BOXING || this == PARTY;
    }

    public String getConfigSection() {
        return switch (this.ordinal()) {
            case 1 -> "ffa-arenas";
            default -> "arenas";
        };
    }

    public static ArenaType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return DUEL;
        }
        try {
            return ArenaType.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return DUEL;
        }
    }
}

