/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.duel;

import com.ultimateduels.models.duel.WinCondition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DuelSettings {
    private String kitId;
    private String arenaId;
    private int rounds = 1;
    private WinCondition winCondition = WinCondition.BEST_OF;
    private MatchType matchType = MatchType.DUEL_1V1;
    private boolean allowSpectators = true;
    private boolean showDeathMessages = true;
    private boolean recordStats = true;
    private boolean calculateElo = true;
    private int teamSize = 1;
    private Map<String, Object> customRules = new HashMap<String, Object>();
    private String presetName;

    public DuelSettings() {
    }

    public DuelSettings(String kitId) {
        this();
        this.kitId = kitId;
    }

    public DuelSettings(String kitId, String arenaId) {
        this(kitId);
        this.arenaId = arenaId;
    }

    public DuelSettings(String kitId, String arenaId, int rounds, WinCondition winCondition) {
        this(kitId, arenaId);
        this.rounds = DuelSettings.validateRounds(rounds);
        this.winCondition = winCondition;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKitId() {
        return this.kitId;
    }

    public String getArenaId() {
        return this.arenaId;
    }

    public int getRounds() {
        return this.rounds;
    }

    public WinCondition getWinCondition() {
        return this.winCondition;
    }

    public MatchType getMatchType() {
        return this.matchType;
    }

    public boolean isAllowSpectators() {
        return this.allowSpectators;
    }

    public boolean isShowDeathMessages() {
        return this.showDeathMessages;
    }

    public boolean isRecordStats() {
        return this.recordStats;
    }

    public boolean isCalculateElo() {
        return this.calculateElo;
    }

    public int getTeamSize() {
        return this.teamSize;
    }

    public Map<String, Object> getCustomRules() {
        return this.customRules;
    }

    public String getPresetName() {
        return this.presetName;
    }

    public void setKitId(String kitId) {
        this.kitId = kitId;
    }

    public void setArenaId(String arenaId) {
        this.arenaId = arenaId;
    }

    public void setRounds(int rounds) {
        this.rounds = DuelSettings.validateRounds(rounds);
    }

    public void setWinCondition(WinCondition winCondition) {
        this.winCondition = winCondition;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public void setAllowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
    }

    public void setShowDeathMessages(boolean showDeathMessages) {
        this.showDeathMessages = showDeathMessages;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }

    public void setCalculateElo(boolean calculateElo) {
        this.calculateElo = calculateElo;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = Math.max(1, teamSize);
    }

    public void setCustomRule(String key, Object value) {
        this.customRules.put(key, value);
    }

    public <T> T getCustomRule(String key, T defaultValue) {
        Object value = this.customRules.get(key);
        return (T)(value != null ? value : defaultValue);
    }

    public boolean hasCustomRule(String key) {
        return this.customRules.containsKey(key);
    }

    public void removeCustomRule(String key) {
        this.customRules.remove(key);
    }

    private static int validateRounds(int rounds) {
        return Math.max(1, Math.min(20, rounds));
    }

    public boolean isValid() {
        return this.kitId != null && !this.kitId.isEmpty() && this.rounds >= 1;
    }

    public List<String> getValidationErrors() {
        ArrayList<String> errors = new ArrayList<String>();
        if (this.kitId == null || this.kitId.isEmpty()) {
            errors.add("Kit must be selected");
        }
        if (this.rounds < 1) {
            errors.add("Rounds must be at least 1");
        }
        if (this.rounds > 20) {
            errors.add("Rounds cannot exceed 20");
        }
        if (this.winCondition == null) {
            errors.add("Win condition must be set");
        }
        if (this.teamSize < 1) {
            errors.add("Team size must be at least 1");
        }
        return errors;
    }

    public int getRoundsToWin() {
        if (this.winCondition == WinCondition.BEST_OF) {
            return this.rounds / 2 + 1;
        }
        return -1;
    }

    public boolean isMultiRound() {
        return this.rounds > 1;
    }

    public boolean isRandomArena() {
        return this.arenaId == null || this.arenaId.isEmpty() || this.arenaId.equalsIgnoreCase("random");
    }

    public boolean isRanked() {
        return this.recordStats && this.calculateElo;
    }

    public int getTotalPlayersRequired() {
        return switch (this.matchType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 2;
            case 1 -> this.teamSize * 2;
            case 2 -> this.teamSize * 2;
            case 3 -> -1;
        };
    }

    public static DuelSettings quickPlay(String kitId) {
        return new DuelSettings(kitId, null, 1, WinCondition.BEST_OF);
    }

    public static DuelSettings competitive(String kitId) {
        return DuelSettings.builder().kit(kitId).rounds(3).winCondition(WinCondition.BEST_OF).recordStats(true).calculateElo(true).build();
    }

    public static DuelSettings tournament(String kitId, String arenaId) {
        return DuelSettings.builder().kit(kitId).arena(arenaId).rounds(5).winCondition(WinCondition.BEST_OF).recordStats(true).calculateElo(false).allowSpectators(true).build();
    }

    public static DuelSettings practice(String kitId) {
        return DuelSettings.builder().kit(kitId).rounds(1).winCondition(WinCondition.BEST_OF).recordStats(false).calculateElo(false).build();
    }

    public DuelSettings copy() {
        DuelSettings copy = new DuelSettings();
        copy.kitId = this.kitId;
        copy.arenaId = this.arenaId;
        copy.rounds = this.rounds;
        copy.winCondition = this.winCondition;
        copy.matchType = this.matchType;
        copy.allowSpectators = this.allowSpectators;
        copy.showDeathMessages = this.showDeathMessages;
        copy.recordStats = this.recordStats;
        copy.calculateElo = this.calculateElo;
        copy.teamSize = this.teamSize;
        copy.customRules = new HashMap<String, Object>(this.customRules);
        copy.presetName = this.presetName;
        return copy;
    }

    public DuelSettings withKit(String newKitId) {
        DuelSettings copy = this.copy();
        copy.kitId = newKitId;
        return copy;
    }

    public DuelSettings withArena(String newArenaId) {
        DuelSettings copy = this.copy();
        copy.arenaId = newArenaId;
        return copy;
    }

    public DuelSettings withRounds(int newRounds) {
        DuelSettings copy = this.copy();
        copy.rounds = DuelSettings.validateRounds(newRounds);
        return copy;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelSettings that = (DuelSettings)o;
        return this.rounds == that.rounds && Objects.equals(this.kitId, that.kitId) && Objects.equals(this.arenaId, that.arenaId) && this.winCondition == that.winCondition;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.kitId, this.arenaId, this.rounds, this.winCondition});
    }

    public String toString() {
        return "DuelSettings{kit='" + this.kitId + "', arena='" + (this.arenaId != null ? this.arenaId : "Random") + "', rounds=" + this.rounds + ", condition=" + String.valueOf((Object)this.winCondition) + ", type=" + String.valueOf((Object)this.matchType) + "}";
    }

    public static enum MatchType {
        DUEL_1V1("1v1 Duel", 1),
        PARTY_VS_PARTY("Party vs Party", -1),
        PARTY_SPLIT("Party Split", -1),
        FFA("Free For All", -1);

        private final String displayName;
        private final int fixedTeamSize;

        private MatchType(String displayName, int fixedTeamSize) {
            this.displayName = displayName;
            this.fixedTeamSize = fixedTeamSize;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public int getFixedTeamSize() {
            return this.fixedTeamSize;
        }

        public boolean hasFixedTeamSize() {
            return this.fixedTeamSize > 0;
        }
    }

    public static class Builder {
        private final DuelSettings settings = new DuelSettings();

        public Builder kit(String kitId) {
            this.settings.kitId = kitId;
            return this;
        }

        public Builder arena(String arenaId) {
            this.settings.arenaId = arenaId;
            return this;
        }

        public Builder rounds(int rounds) {
            this.settings.rounds = DuelSettings.validateRounds(rounds);
            return this;
        }

        public Builder winCondition(WinCondition condition) {
            this.settings.winCondition = condition;
            return this;
        }

        public Builder matchType(MatchType type) {
            this.settings.matchType = type;
            return this;
        }

        public Builder allowSpectators(boolean allow) {
            this.settings.allowSpectators = allow;
            return this;
        }

        public Builder showDeathMessages(boolean show) {
            this.settings.showDeathMessages = show;
            return this;
        }

        public Builder recordStats(boolean record) {
            this.settings.recordStats = record;
            return this;
        }

        public Builder calculateElo(boolean calculate) {
            this.settings.calculateElo = calculate;
            return this;
        }

        public Builder teamSize(int size) {
            this.settings.teamSize = Math.max(1, size);
            return this;
        }

        public Builder customRule(String key, Object value) {
            this.settings.customRules.put(key, value);
            return this;
        }

        public Builder preset(String presetName) {
            this.settings.presetName = presetName;
            return this;
        }

        public DuelSettings build() {
            return this.settings;
        }
    }
}

