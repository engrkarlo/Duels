/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.queue.model;

public enum PartyQueueType {
    PARTY_VS_PARTY("Party vs Party", "\u00a7dPvP", 2, 10),
    SPLIT_2V2("2v2 Split", "\u00a7b2v2", 4, 4),
    SPLIT_3V3("3v3 Split", "\u00a7e3v3", 6, 6),
    SPLIT_4V4("4v4 Split", "\u00a764v4", 8, 8),
    SPLIT_5V5("5v5 Split", "\u00a7c5v5", 10, 10);

    private final String displayName;
    private final String shortName;
    private final int requiredPlayers;
    private final int maxPlayers;

    private PartyQueueType(String displayName, String shortName, int requiredPlayers, int maxPlayers) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.requiredPlayers = requiredPlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public int getRequiredPlayers() {
        return this.requiredPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isValidSize(int partySize) {
        return partySize >= this.requiredPlayers && partySize <= this.maxPlayers;
    }

    public boolean isSplitMode() {
        return this != PARTY_VS_PARTY;
    }

    public int getTeamSize() {
        return this.requiredPlayers / 2;
    }

    public static PartyQueueType getSplitTypeForSize(int partySize) {
        return switch (partySize) {
            case 4 -> SPLIT_2V2;
            case 6 -> SPLIT_3V3;
            case 8 -> SPLIT_4V4;
            case 10 -> SPLIT_5V5;
            default -> null;
        };
    }
}

