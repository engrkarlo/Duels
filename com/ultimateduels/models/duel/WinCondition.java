/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.duel;

import java.util.ArrayList;
import java.util.List;

public enum WinCondition {
    BEST_OF("Best Of", "First to win majority", "\u00a76", true, "First to %d wins"),
    PLAY_ALL("Play All", "Complete all rounds", "\u00a7b", false, "Most wins after %d rounds"),
    FIRST_BLOOD("First Blood", "First kill wins", "\u00a7c", true, "First to kill wins"),
    ELIMINATION("Elimination", "Last team standing", "\u00a74", true, "Eliminate all enemies"),
    POINTS("Points", "First to target score", "\u00a7e", true, "First to %d points"),
    TIME_LIMIT("Time Limit", "Best score when time ends", "\u00a77", false, "Most kills in %d seconds"),
    HIT_COUNTER("Hit Counter", "First to land target hits", "\u00a7d", true, "First to %d hits");

    private final String displayName;
    private final String description;
    private final String colorCode;
    private final boolean canEndEarly;
    private final String targetFormat;

    private WinCondition(String displayName, String description, String colorCode, boolean canEndEarly, String targetFormat) {
        this.displayName = displayName;
        this.description = description;
        this.colorCode = colorCode;
        this.canEndEarly = canEndEarly;
        this.targetFormat = targetFormat;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getColorCode() {
        return this.colorCode;
    }

    public String getColoredName() {
        return this.colorCode + this.displayName;
    }

    public boolean canEndEarly() {
        return this.canEndEarly;
    }

    public String getFormattedTarget(int target) {
        return String.format(this.targetFormat, target);
    }

    public int calculateWinsNeeded(int totalRounds) {
        if (this == BEST_OF) {
            return totalRounds / 2 + 1;
        }
        return -1;
    }

    public boolean isMatchOver(int team1Score, int team2Score, int currentRound, int maxRounds) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                int winsNeeded = this.calculateWinsNeeded(maxRounds);
                if (team1Score >= winsNeeded || team2Score >= winsNeeded) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (currentRound >= maxRounds) {
                    yield true;
                }
                yield false;
            }
            case 2 -> {
                if (team1Score > 0 || team2Score > 0) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (team1Score > 0 || team2Score > 0) {
                    yield true;
                }
                yield false;
            }
            case 4, 6 -> {
                if (team1Score >= maxRounds || team2Score >= maxRounds) {
                    yield true;
                }
                yield false;
            }
            case 5 -> false;
        };
    }

    public int getWinner(int team1Score, int team2Score) {
        if (team1Score > team2Score) {
            return 1;
        }
        if (team2Score > team1Score) {
            return 2;
        }
        return 0;
    }

    public boolean canDraw() {
        return this == PLAY_ALL || this == TIME_LIMIT;
    }

    public boolean isRoundBased() {
        return this == BEST_OF || this == PLAY_ALL || this == ELIMINATION;
    }

    public boolean isPointBased() {
        return this == POINTS || this == HIT_COUNTER;
    }

    public String getScoreboardDisplay(int maxRounds) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Best of " + maxRounds;
            case 1 -> "Play All (" + maxRounds + ")";
            case 2 -> "First Blood";
            case 3 -> "Elimination";
            case 4 -> "First to " + maxRounds;
            case 5 -> maxRounds + "s Time Limit";
            case 6 -> "First to " + maxRounds + " hits";
        };
    }

    public String getGuiMaterial() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "GOLDEN_SWORD";
            case 1 -> "CLOCK";
            case 2 -> "REDSTONE";
            case 3 -> "WITHER_SKELETON_SKULL";
            case 4 -> "EXPERIENCE_BOTTLE";
            case 5 -> "CLOCK";
            case 6 -> "STICK";
        };
    }

    public List<String> getGuiLore() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("\u00a77" + this.description);
        lore.add("");
        switch (this.ordinal()) {
            case 0: {
                lore.add("\u00a7eExamples:");
                lore.add("\u00a77  Best of 3: First to 2");
                lore.add("\u00a77  Best of 5: First to 3");
                break;
            }
            case 1: {
                lore.add("\u00a77All rounds are played.");
                lore.add("\u00a77Can result in a draw.");
                break;
            }
            case 2: {
                lore.add("\u00a77Quick, decisive matches!");
                break;
            }
            case 3: {
                lore.add("\u00a77Team fights only.");
                break;
            }
            case 4: {
                lore.add("\u00a77Reach target score to win.");
                break;
            }
            case 5: {
                lore.add("\u00a77Be efficient with your time!");
                break;
            }
            case 6: {
                lore.add("\u00a77Boxing-style combat.");
            }
        }
        return lore;
    }

    public WinCondition next() {
        return switch (this.ordinal()) {
            case 0 -> PLAY_ALL;
            case 1 -> BEST_OF;
            default -> BEST_OF;
        };
    }

    public static WinCondition[] getCommonConditions() {
        return new WinCondition[]{BEST_OF, PLAY_ALL};
    }

    public static WinCondition[] getTeamConditions() {
        return new WinCondition[]{BEST_OF, PLAY_ALL, ELIMINATION};
    }

    public static WinCondition fromString(String value) {
        if (value == null || value.isEmpty()) {
            return BEST_OF;
        }
        try {
            return WinCondition.valueOf(value.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            for (WinCondition condition : WinCondition.values()) {
                if (!condition.displayName.equalsIgnoreCase(value)) continue;
                return condition;
            }
            return BEST_OF;
        }
    }
}

