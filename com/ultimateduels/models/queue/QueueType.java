/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.queue;

import java.util.ArrayList;
import java.util.List;

public enum QueueType {
    SOLO("Solo", "\u00a7a", 1, 1, true, "Queue alone for 1v1 matches"),
    PARTY("Party", "\u00a7d", 2, 10, true, "Queue with your party"),
    PARTY_SPLIT("Party Split", "\u00a76", 4, 10, false, "Split your party for internal scrims"),
    RANKED_SOLO("Ranked", "\u00a7c", 1, 1, true, "Competitive solo queue"),
    RANKED_PARTY("Ranked Party", "\u00a74", 2, 5, true, "Competitive party queue"),
    EVENT("Event", "\u00a7b", 1, 20, true, "Special event queue"),
    PRACTICE("Practice", "\u00a77", 1, 1, true, "Casual practice matches");

    private final String displayName;
    private final String colorCode;
    private final int minPlayers;
    private final int maxPlayers;
    private final boolean requiresMatching;
    private final String description;

    private QueueType(String displayName, String colorCode, int minPlayers, int maxPlayers, boolean requiresMatching, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.requiresMatching = requiresMatching;
        this.description = description;
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

    public String getDescription() {
        return this.description;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean isValidPlayerCount(int count) {
        return count >= this.minPlayers && count <= this.maxPlayers;
    }

    public boolean isSolo() {
        return this == SOLO || this == RANKED_SOLO || this == PRACTICE;
    }

    public boolean isParty() {
        return this == PARTY || this == RANKED_PARTY || this == PARTY_SPLIT;
    }

    public boolean isRanked() {
        return this == RANKED_SOLO || this == RANKED_PARTY;
    }

    public boolean recordsStats() {
        return this != PRACTICE;
    }

    public boolean calculatesElo() {
        return this.isRanked();
    }

    public boolean requiresMatching() {
        return this.requiresMatching;
    }

    public boolean isInternal() {
        return this == PARTY_SPLIT;
    }

    public boolean canMatchWith(QueueType other) {
        if (this == other) {
            return true;
        }
        if (this.isSolo() && other.isSolo()) {
            return this.isRanked() == other.isRanked();
        }
        if (this.isParty() && other.isParty()) {
            if (this.isRanked() != other.isRanked()) {
                return false;
            }
            return this != PARTY_SPLIT && other != PARTY_SPLIT;
        }
        return false;
    }

    public int getBaseEloRange() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1, 6 -> 200;
            case 3 -> 150;
            case 4 -> 200;
            case 2 -> 0;
            case 5 -> 300;
        };
    }

    public int getEloExpansionRate() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 3 -> 25;
            case 4 -> 30;
            case 0, 1 -> 50;
            case 5, 6 -> 75;
            case 2 -> 0;
        };
    }

    public int getMaxQueueTime() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 3, 4 -> 300;
            case 0, 1 -> 0;
            case 6 -> 180;
            case 2 -> 0;
            case 5 -> 120;
        };
    }

    public String getGuiMaterial() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "IRON_SWORD";
            case 1 -> "GOLDEN_SWORD";
            case 2 -> "STONE_SWORD";
            case 3 -> "DIAMOND_SWORD";
            case 4 -> "NETHERITE_SWORD";
            case 5 -> "NETHER_STAR";
            case 6 -> "WOODEN_SWORD";
        };
    }

    public List<String> getGuiLore() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("\u00a77" + this.description);
        lore.add("");
        lore.add("\u00a77Players: \u00a7e" + this.minPlayers + (String)(this.maxPlayers > this.minPlayers ? "-" + this.maxPlayers : ""));
        if (this.isRanked()) {
            lore.add("\u00a77ELO: \u00a7aTracked");
        }
        if (this.recordsStats()) {
            lore.add("\u00a77Stats: \u00a7aRecorded");
        } else {
            lore.add("\u00a77Stats: \u00a7cNot Recorded");
        }
        return lore;
    }

    public static QueueType[] getSoloTypes() {
        return new QueueType[]{SOLO, RANKED_SOLO, PRACTICE};
    }

    public static QueueType[] getPartyTypes() {
        return new QueueType[]{PARTY, RANKED_PARTY, PARTY_SPLIT};
    }

    public static QueueType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return SOLO;
        }
        try {
            return QueueType.valueOf(value.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            return SOLO;
        }
    }
}

