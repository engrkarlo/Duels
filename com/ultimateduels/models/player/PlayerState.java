/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.player;

public enum PlayerState {
    IN_LOBBY("In Lobby", "\u00a7a", true, false),
    IN_QUEUE("In Queue", "\u00a7e", false, false),
    IN_COUNTDOWN("Starting", "\u00a76", false, true),
    FIGHTING("Fighting", "\u00a7c", false, true),
    ROUND_ENDED("Round Ended", "\u00a77", false, true),
    SPECTATING("Spectating", "\u00a77", false, false),
    IN_FFA("In FFA", "\u00a7c", false, true),
    EDITING_KIT("Editing Kit", "\u00a7b", false, false),
    IN_MENU("In Menu", "\u00a7f", false, false),
    TELEPORTING("Teleporting", "\u00a7d", false, false),
    FROZEN("Frozen", "\u00a79", false, false),
    OFFLINE("Offline", "\u00a78", false, false);

    private final String displayName;
    private final String colorCode;
    private final boolean canQueue;
    private final boolean inCombat;

    private PlayerState(String displayName, String colorCode, boolean canQueue, boolean inCombat) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.canQueue = canQueue;
        this.inCombat = inCombat;
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

    public boolean canQueue() {
        return this.canQueue;
    }

    public boolean isInCombat() {
        return this.inCombat;
    }

    public boolean canReceiveDuelRequest() {
        return this == IN_LOBBY;
    }

    public boolean canSendDuelRequest() {
        return this == IN_LOBBY;
    }

    public boolean canJoinParty() {
        return this == IN_LOBBY || this == IN_QUEUE;
    }

    public boolean canSpectate() {
        return this == IN_LOBBY;
    }

    public boolean canOpenMenu() {
        return !this.inCombat && this != TELEPORTING && this != OFFLINE;
    }

    public boolean canChat() {
        return this != OFFLINE;
    }

    public boolean shouldLockInventory() {
        return this == IN_LOBBY || this == IN_QUEUE || this == SPECTATING;
    }

    public boolean canMove() {
        return this != IN_COUNTDOWN && this != FROZEN && this != TELEPORTING && this != ROUND_ENDED;
    }

    public boolean isBusy() {
        return this != IN_LOBBY;
    }

    public boolean isAvailable() {
        return this == IN_LOBBY;
    }

    public PlayerState[] getValidTransitions() {
        PlayerState[] playerStateArray;
        switch (this.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                PlayerState[] playerStateArray2 = new PlayerState[7];
                playerStateArray2[0] = IN_QUEUE;
                playerStateArray2[1] = IN_COUNTDOWN;
                playerStateArray2[2] = SPECTATING;
                playerStateArray2[3] = IN_FFA;
                playerStateArray2[4] = EDITING_KIT;
                playerStateArray2[5] = IN_MENU;
                playerStateArray = playerStateArray2;
                playerStateArray2[6] = OFFLINE;
                break;
            }
            case 1: {
                PlayerState[] playerStateArray3 = new PlayerState[3];
                playerStateArray3[0] = IN_LOBBY;
                playerStateArray3[1] = IN_COUNTDOWN;
                playerStateArray = playerStateArray3;
                playerStateArray3[2] = OFFLINE;
                break;
            }
            case 2: {
                PlayerState[] playerStateArray4 = new PlayerState[2];
                playerStateArray4[0] = FIGHTING;
                playerStateArray = playerStateArray4;
                playerStateArray4[1] = IN_LOBBY;
                break;
            }
            case 3: {
                PlayerState[] playerStateArray5 = new PlayerState[3];
                playerStateArray5[0] = ROUND_ENDED;
                playerStateArray5[1] = IN_LOBBY;
                playerStateArray = playerStateArray5;
                playerStateArray5[2] = SPECTATING;
                break;
            }
            case 4: {
                PlayerState[] playerStateArray6 = new PlayerState[3];
                playerStateArray6[0] = IN_COUNTDOWN;
                playerStateArray6[1] = IN_LOBBY;
                playerStateArray = playerStateArray6;
                playerStateArray6[2] = SPECTATING;
                break;
            }
            case 5: {
                PlayerState[] playerStateArray7 = new PlayerState[2];
                playerStateArray7[0] = IN_LOBBY;
                playerStateArray = playerStateArray7;
                playerStateArray7[1] = OFFLINE;
                break;
            }
            case 6: {
                PlayerState[] playerStateArray8 = new PlayerState[2];
                playerStateArray8[0] = IN_LOBBY;
                playerStateArray = playerStateArray8;
                playerStateArray8[1] = OFFLINE;
                break;
            }
            case 7: {
                PlayerState[] playerStateArray9 = new PlayerState[2];
                playerStateArray9[0] = IN_LOBBY;
                playerStateArray = playerStateArray9;
                playerStateArray9[1] = OFFLINE;
                break;
            }
            case 8: {
                PlayerState[] playerStateArray10 = new PlayerState[3];
                playerStateArray10[0] = IN_LOBBY;
                playerStateArray10[1] = IN_QUEUE;
                playerStateArray = playerStateArray10;
                playerStateArray10[2] = OFFLINE;
                break;
            }
            case 9: {
                PlayerState[] playerStateArray11 = new PlayerState[4];
                playerStateArray11[0] = IN_LOBBY;
                playerStateArray11[1] = FIGHTING;
                playerStateArray11[2] = IN_FFA;
                playerStateArray = playerStateArray11;
                playerStateArray11[3] = SPECTATING;
                break;
            }
            case 10: {
                PlayerState[] playerStateArray12 = new PlayerState[3];
                playerStateArray12[0] = IN_LOBBY;
                playerStateArray12[1] = FIGHTING;
                playerStateArray = playerStateArray12;
                playerStateArray12[2] = IN_COUNTDOWN;
                break;
            }
            case 11: {
                PlayerState[] playerStateArray13 = new PlayerState[1];
                playerStateArray = playerStateArray13;
                playerStateArray13[0] = IN_LOBBY;
            }
        }
        return playerStateArray;
    }

    public boolean canTransitionTo(PlayerState nextState) {
        if (nextState == this) {
            return true;
        }
        for (PlayerState valid : this.getValidTransitions()) {
            if (valid != nextState) continue;
            return true;
        }
        return false;
    }
}

