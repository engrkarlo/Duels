/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.party;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PartyRole {
    LEADER("Leader", "\u00a76", "\u2605", 100, true, true, true, true, true, true, true),
    MODERATOR("Moderator", "\u00a7b", "\u25c6", 50, true, true, false, false, true, false, false),
    MEMBER("Member", "\u00a7a", "\u25cf", 10, false, false, false, false, false, false, false),
    INVITED("Invited", "\u00a77", "\u25cb", 0, false, false, false, false, false, false, false);

    private final String displayName;
    private final String colorCode;
    private final String symbol;
    private final int priority;
    private final boolean canInvite;
    private final boolean canKick;
    private final boolean canPromote;
    private final boolean canDemote;
    private final boolean canStartMatch;
    private final boolean canChangeSettings;
    private final boolean canDisband;

    private PartyRole(String displayName, String colorCode, String symbol, int priority, boolean canInvite, boolean canKick, boolean canPromote, boolean canDemote, boolean canStartMatch, boolean canChangeSettings, boolean canDisband) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.symbol = symbol;
        this.priority = priority;
        this.canInvite = canInvite;
        this.canKick = canKick;
        this.canPromote = canPromote;
        this.canDemote = canDemote;
        this.canStartMatch = canStartMatch;
        this.canChangeSettings = canChangeSettings;
        this.canDisband = canDisband;
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

    public String getSymbol() {
        return this.symbol;
    }

    public String getColoredSymbol() {
        return this.colorCode + this.symbol;
    }

    public String getFormattedName() {
        return this.colorCode + this.symbol + " " + this.displayName;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean canInvite() {
        return this.canInvite;
    }

    public boolean canKick() {
        return this.canKick;
    }

    public boolean canPromote() {
        return this.canPromote;
    }

    public boolean canDemote() {
        return this.canDemote;
    }

    public boolean canStartMatch() {
        return this.canStartMatch;
    }

    public boolean canChangeSettings() {
        return this.canChangeSettings;
    }

    public boolean canDisband() {
        return this.canDisband;
    }

    public boolean isLeader() {
        return this == LEADER;
    }

    public boolean isModerator() {
        return this == LEADER || this == MODERATOR;
    }

    public boolean isMember() {
        return this != INVITED;
    }

    public boolean isInvited() {
        return this == INVITED;
    }

    public boolean hasHigherAuthority(PartyRole other) {
        return this.priority > other.priority;
    }

    public boolean hasEqualOrHigherAuthority(PartyRole other) {
        return this.priority >= other.priority;
    }

    public boolean canManage(PartyRole other) {
        return this.hasHigherAuthority(other) && other != LEADER;
    }

    public boolean canKick(PartyRole targetRole) {
        return this.canKick && this.hasHigherAuthority(targetRole);
    }

    public boolean canPromoteTo(PartyRole targetRole, PartyRole newRole) {
        if (!this.canPromote) {
            return false;
        }
        if (!this.hasHigherAuthority(targetRole)) {
            return false;
        }
        if (!this.hasHigherAuthority(newRole) && newRole != this) {
            return false;
        }
        return newRole.priority > targetRole.priority;
    }

    public boolean canDemoteTo(PartyRole targetRole, PartyRole newRole) {
        if (!this.canDemote) {
            return false;
        }
        if (!this.hasHigherAuthority(targetRole)) {
            return false;
        }
        return newRole.priority < targetRole.priority;
    }

    public PartyRole demote() {
        return switch (this.ordinal()) {
            case 0 -> MODERATOR;
            case 1 -> MEMBER;
            default -> MEMBER;
        };
    }

    public PartyRole promote() {
        return switch (this.ordinal()) {
            case 2 -> MODERATOR;
            case 1 -> LEADER;
            default -> this;
        };
    }

    public List<String> getPermissions() {
        ArrayList<String> perms = new ArrayList<String>();
        if (this.canInvite) {
            perms.add("invite");
        }
        if (this.canKick) {
            perms.add("kick");
        }
        if (this.canPromote) {
            perms.add("promote");
        }
        if (this.canDemote) {
            perms.add("demote");
        }
        if (this.canStartMatch) {
            perms.add("start_match");
        }
        if (this.canChangeSettings) {
            perms.add("change_settings");
        }
        if (this.canDisband) {
            perms.add("disband");
        }
        return perms;
    }

    public List<String> getGuiLore() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("");
        lore.add("\u00a77Permissions:");
        lore.add("  \u00a77Invite: " + (this.canInvite ? "\u00a7a\u2713" : "\u00a7c\u2717"));
        lore.add("  \u00a77Kick: " + (this.canKick ? "\u00a7a\u2713" : "\u00a7c\u2717"));
        lore.add("  \u00a77Promote: " + (this.canPromote ? "\u00a7a\u2713" : "\u00a7c\u2717"));
        lore.add("  \u00a77Start Match: " + (this.canStartMatch ? "\u00a7a\u2713" : "\u00a7c\u2717"));
        lore.add("  \u00a77Settings: " + (this.canChangeSettings ? "\u00a7a\u2713" : "\u00a7c\u2717"));
        return lore;
    }

    public static PartyRole[] getMemberRoles() {
        return new PartyRole[]{LEADER, MODERATOR, MEMBER};
    }

    public static PartyRole[] getInvitingRoles() {
        return (PartyRole[])Arrays.stream(PartyRole.values()).filter(PartyRole::canInvite).toArray(PartyRole[]::new);
    }

    public static PartyRole fromString(String value) {
        if (value == null || value.isEmpty()) {
            return MEMBER;
        }
        try {
            return PartyRole.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }

    public static PartyRole getDefaultRole() {
        return MEMBER;
    }
}

