/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.kit;

public enum KitType {
    ADMIN_STANDARD("Admin Kit", "\u00a76", true),
    PLAYER_CUSTOM("Custom Layout", "\u00a7a", false),
    EVENT_TEMPORARY("Event Kit", "\u00a7d", false),
    FFA_SPECIFIC("FFA Kit", "\u00a7c", true),
    SPECIAL_MODE("Special Kit", "\u00a7b", true);

    private final String displayName;
    private final String colorCode;
    private final boolean showInMenu;

    private KitType(String displayName, String colorCode, boolean showInMenu) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.showInMenu = showInMenu;
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

    public boolean shouldShowInMenu() {
        return this.showInMenu;
    }

    public boolean isAdminKit() {
        return this == ADMIN_STANDARD || this == FFA_SPECIFIC || this == SPECIAL_MODE;
    }

    public boolean allowsCustomLayout() {
        return this == ADMIN_STANDARD;
    }

    public boolean isPlayerOwned() {
        return this == PLAYER_CUSTOM;
    }

    public boolean shouldPersist() {
        return this != EVENT_TEMPORARY;
    }

    public boolean canUseInQueue() {
        return this == ADMIN_STANDARD || this == PLAYER_CUSTOM;
    }

    public boolean isEditable() {
        return this == PLAYER_CUSTOM;
    }

    public KitType getBaseType() {
        if (this == PLAYER_CUSTOM) {
            return ADMIN_STANDARD;
        }
        return this;
    }
}

