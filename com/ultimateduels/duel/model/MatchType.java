/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.duel.model;

public enum MatchType {
    DUEL_1V1("1v1", "\u00a7a", 2),
    DUEL_2V2("2v2", "\u00a7b", 4),
    DUEL_3V3("3v3", "\u00a7d", 6),
    PARTY("Party", "\u00a7e", 20),
    RANKED("Ranked", "\u00a76", 2),
    PARTY_FFA("Party FFA", "\u00a7c", 20);

    private final String displayName;
    private final String colorCode;
    private final int maxPlayers;

    private MatchType(String displayName, String colorCode, int maxPlayers) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.maxPlayers = maxPlayers;
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

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isTeamMatch() {
        return this != DUEL_1V1 && this != RANKED;
    }
}

