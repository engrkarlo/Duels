/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.duel.model;

public enum WinCondition {
    BEST_OF("Best Of", "First to win majority"){

        @Override
        public int getWinsNeeded(int totalRounds) {
            return totalRounds / 2 + 1;
        }

        @Override
        public boolean isMatchOver(int score1, int score2, int totalRounds, int currentRound) {
            int winsNeeded = this.getWinsNeeded(totalRounds);
            return score1 >= winsNeeded || score2 >= winsNeeded;
        }
    }
    ,
    PLAY_ALL("Play All", "Play all rounds, most wins"){

        @Override
        public int getWinsNeeded(int totalRounds) {
            return totalRounds / 2 + 1;
        }

        @Override
        public boolean isMatchOver(int score1, int score2, int totalRounds, int currentRound) {
            int roundsPlayed = score1 + score2;
            return roundsPlayed >= totalRounds;
        }
    }
    ,
    FIRST_TO_WIN("First to Win", "First to reach target wins"){

        @Override
        public int getWinsNeeded(int totalRounds) {
            return totalRounds;
        }

        @Override
        public boolean isMatchOver(int score1, int score2, int totalRounds, int currentRound) {
            return score1 >= totalRounds || score2 >= totalRounds;
        }
    }
    ,
    FIRST_TO_KILLS("First To", "First to X kills wins"){

        @Override
        public int getWinsNeeded(int totalRounds) {
            return totalRounds;
        }

        @Override
        public boolean isMatchOver(int score1, int score2, int totalRounds, int currentRound) {
            return score1 >= totalRounds || score2 >= totalRounds;
        }
    };

    private final String displayName;
    private final String description;

    private WinCondition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public abstract int getWinsNeeded(int var1);

    public abstract boolean isMatchOver(int var1, int var2, int var3, int var4);

    public String getFormattedString(int totalRounds) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "Best of " + totalRounds;
            case 1 -> "Play All " + totalRounds;
            case 2 -> "First to " + totalRounds + " Wins";
            case 3 -> "First to " + totalRounds + " Kills";
        };
    }
}

