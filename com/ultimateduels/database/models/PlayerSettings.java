/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.database.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class PlayerSettings {
    private final UUID uuid;
    private boolean scoreboardEnabled;
    private boolean deathMessagesEnabled;
    private boolean duelRequestsEnabled;
    private boolean spectatorsAllowed;
    private boolean partyInvitesEnabled;
    private boolean soundsEnabled;
    private boolean queueNotifications;
    private boolean autoRequeue;
    private boolean showPing;
    private String preferredKit;
    private String preferredArena;
    private Timestamp updatedAt;

    public PlayerSettings(UUID uuid) {
        this.uuid = uuid;
        this.scoreboardEnabled = true;
        this.deathMessagesEnabled = true;
        this.duelRequestsEnabled = true;
        this.spectatorsAllowed = true;
        this.partyInvitesEnabled = true;
        this.soundsEnabled = true;
        this.queueNotifications = true;
        this.autoRequeue = false;
        this.showPing = true;
        this.preferredKit = null;
        this.preferredArena = null;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public static PlayerSettings fromResultSet(ResultSet rs) throws SQLException {
        PlayerSettings settings = new PlayerSettings(UUID.fromString(rs.getString("uuid")));
        settings.scoreboardEnabled = rs.getBoolean("scoreboard_enabled");
        settings.deathMessagesEnabled = rs.getBoolean("death_messages_enabled");
        settings.duelRequestsEnabled = rs.getBoolean("duel_requests_enabled");
        settings.spectatorsAllowed = rs.getBoolean("spectators_allowed");
        settings.partyInvitesEnabled = rs.getBoolean("party_invites_enabled");
        settings.soundsEnabled = rs.getBoolean("sounds_enabled");
        settings.queueNotifications = rs.getBoolean("queue_notifications");
        settings.autoRequeue = rs.getBoolean("auto_requeue");
        settings.showPing = rs.getBoolean("show_ping");
        settings.preferredKit = rs.getString("preferred_kit");
        settings.preferredArena = rs.getString("preferred_arena");
        settings.updatedAt = rs.getTimestamp("updated_at");
        return settings;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean isScoreboardEnabled() {
        return this.scoreboardEnabled;
    }

    public boolean isDeathMessagesEnabled() {
        return this.deathMessagesEnabled;
    }

    public boolean isDuelRequestsEnabled() {
        return this.duelRequestsEnabled;
    }

    public boolean isSpectatorsAllowed() {
        return this.spectatorsAllowed;
    }

    public boolean isPartyInvitesEnabled() {
        return this.partyInvitesEnabled;
    }

    public boolean isSoundsEnabled() {
        return this.soundsEnabled;
    }

    public boolean isQueueNotifications() {
        return this.queueNotifications;
    }

    public boolean isAutoRequeue() {
        return this.autoRequeue;
    }

    public boolean isShowPing() {
        return this.showPing;
    }

    public String getPreferredKit() {
        return this.preferredKit;
    }

    public String getPreferredArena() {
        return this.preferredArena;
    }

    public Timestamp getUpdatedAt() {
        return this.updatedAt;
    }

    public void setScoreboardEnabled(boolean enabled) {
        this.scoreboardEnabled = enabled;
        this.touch();
    }

    public void setDeathMessagesEnabled(boolean enabled) {
        this.deathMessagesEnabled = enabled;
        this.touch();
    }

    public void setDuelRequestsEnabled(boolean enabled) {
        this.duelRequestsEnabled = enabled;
        this.touch();
    }

    public void setSpectatorsAllowed(boolean allowed) {
        this.spectatorsAllowed = allowed;
        this.touch();
    }

    public void setPartyInvitesEnabled(boolean enabled) {
        this.partyInvitesEnabled = enabled;
        this.touch();
    }

    public void setSoundsEnabled(boolean enabled) {
        this.soundsEnabled = enabled;
        this.touch();
    }

    public void setQueueNotifications(boolean enabled) {
        this.queueNotifications = enabled;
        this.touch();
    }

    public void setAutoRequeue(boolean enabled) {
        this.autoRequeue = enabled;
        this.touch();
    }

    public void setShowPing(boolean show) {
        this.showPing = show;
        this.touch();
    }

    public void setPreferredKit(String kit) {
        this.preferredKit = kit;
        this.touch();
    }

    public void setPreferredArena(String arena) {
        this.preferredArena = arena;
        this.touch();
    }

    private void touch() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public boolean toggle(String setting) {
        switch (setting.toLowerCase()) {
            case "scoreboard": {
                this.scoreboardEnabled = !this.scoreboardEnabled;
                this.touch();
                return this.scoreboardEnabled;
            }
            case "death_messages": {
                this.deathMessagesEnabled = !this.deathMessagesEnabled;
                this.touch();
                return this.deathMessagesEnabled;
            }
            case "duel_requests": {
                this.duelRequestsEnabled = !this.duelRequestsEnabled;
                this.touch();
                return this.duelRequestsEnabled;
            }
            case "spectators": {
                this.spectatorsAllowed = !this.spectatorsAllowed;
                this.touch();
                return this.spectatorsAllowed;
            }
            case "party_invites": {
                this.partyInvitesEnabled = !this.partyInvitesEnabled;
                this.touch();
                return this.partyInvitesEnabled;
            }
            case "sounds": {
                this.soundsEnabled = !this.soundsEnabled;
                this.touch();
                return this.soundsEnabled;
            }
            case "queue_notifications": {
                this.queueNotifications = !this.queueNotifications;
                this.touch();
                return this.queueNotifications;
            }
            case "auto_requeue": {
                this.autoRequeue = !this.autoRequeue;
                this.touch();
                return this.autoRequeue;
            }
            case "show_ping": {
                this.showPing = !this.showPing;
                this.touch();
                return this.showPing;
            }
        }
        throw new IllegalArgumentException("Unknown setting: " + setting);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PlayerSettings that = (PlayerSettings)o;
        return Objects.equals(this.uuid, that.uuid);
    }

    public int hashCode() {
        return Objects.hash(this.uuid);
    }
}

