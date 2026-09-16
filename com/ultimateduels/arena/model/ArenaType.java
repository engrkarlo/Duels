/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.arena.model;

public enum ArenaType {
    DUEL_1V1("1v1 Duel", "\u00a7a", 2, 2),
    DUEL_2V2("2v2 Duel", "\u00a7b", 4, 4),
    DUEL_3V3("3v3 Duel", "\u00a7d", 6, 6),
    PARTY("Party Duel", "\u00a7e", 2, 10),
    FFA("FFA", "\u00a7c", 2, 50),
    SUMO("Sumo", "\u00a76", 2, 2),
    BOXING("Boxing", "\u00a7f", 2, 2),
    BRIDGE("Bridge", "\u00a79", 2, 2),
    BUILD_UHC("BuildUHC", "\u00a74", 2, 2);

    private final String displayName;
    private final String colorCode;
    private final int minPlayers;
    private final int maxPlayers;

    private ArenaType(String displayName, String colorCode, int minPlayers, int maxPlayers) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
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

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isTeamBased() {
        return this == DUEL_2V2 || this == DUEL_3V3 || this == PARTY;
    }

    public boolean isFFA() {
        return this == FFA;
    }

    public boolean requiresRegeneration() {
        return this == BUILD_UHC || this == BRIDGE;
    }

    public int getRequiredSpawnPoints() {
        if (this.isFFA()) {
            return 2;
        }
        if (this.isTeamBased()) {
            return 2;
        }
        return 2;
    }
}

