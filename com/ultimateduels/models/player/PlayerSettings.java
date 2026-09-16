/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettings {
    private final UUID uuid;
    private final Map<Setting, Boolean> settings;
    private String preferredKit;
    private String preferredArena;
    private int defaultRounds;
    private boolean dirty;

    public PlayerSettings(UUID uuid) {
        this.uuid = uuid;
        this.settings = new EnumMap<Setting, Boolean>(Setting.class);
        this.defaultRounds = 1;
        this.dirty = false;
        for (Setting setting : Setting.values()) {
            this.settings.put(setting, setting.getDefaultValue());
        }
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean getSetting(Setting setting) {
        return this.settings.getOrDefault((Object)setting, setting.getDefaultValue());
    }

    public void setSetting(Setting setting, boolean value) {
        this.settings.put(setting, value);
        this.markDirty();
    }

    public boolean toggleSetting(Setting setting) {
        boolean newValue = !this.getSetting(setting);
        this.setSetting(setting, newValue);
        return newValue;
    }

    public void resetSetting(Setting setting) {
        this.setSetting(setting, setting.getDefaultValue());
    }

    public void resetAll() {
        for (Setting setting : Setting.values()) {
            this.settings.put(setting, setting.getDefaultValue());
        }
        this.preferredKit = null;
        this.preferredArena = null;
        this.defaultRounds = 1;
        this.markDirty();
    }

    public boolean isScoreboardEnabled() {
        return this.getSetting(Setting.SCOREBOARD);
    }

    public boolean isDeathMessagesEnabled() {
        return this.getSetting(Setting.DEATH_MESSAGES);
    }

    public boolean isDuelRequestsEnabled() {
        return this.getSetting(Setting.DUEL_REQUESTS);
    }

    public boolean isSpectatorsAllowed() {
        return this.getSetting(Setting.SPECTATORS_ALLOWED);
    }

    public boolean isPartyInvitesEnabled() {
        return this.getSetting(Setting.PARTY_INVITES);
    }

    public boolean isSoundsEnabled() {
        return this.getSetting(Setting.SOUNDS);
    }

    public boolean isQueueNotificationsEnabled() {
        return this.getSetting(Setting.QUEUE_NOTIFICATIONS);
    }

    public boolean isAutoRequeueEnabled() {
        return this.getSetting(Setting.AUTO_REQUEUE);
    }

    public boolean isShowPingEnabled() {
        return this.getSetting(Setting.SHOW_PING);
    }

    public boolean isDuelBroadcastEnabled() {
        return this.getSetting(Setting.DUEL_BROADCAST);
    }

    public boolean isPrivateModeEnabled() {
        return this.getSetting(Setting.PRIVATE_MODE);
    }

    public boolean isHitParticlesEnabled() {
        return this.getSetting(Setting.HIT_PARTICLES);
    }

    public String getPreferredKit() {
        return this.preferredKit;
    }

    public void setPreferredKit(String kitName) {
        this.preferredKit = kitName;
        this.markDirty();
    }

    public String getPreferredArena() {
        return this.preferredArena;
    }

    public void setPreferredArena(String arenaName) {
        this.preferredArena = arenaName;
        this.markDirty();
    }

    public int getDefaultRounds() {
        return this.defaultRounds;
    }

    public void setDefaultRounds(int rounds) {
        this.defaultRounds = Math.max(1, Math.min(20, rounds));
        this.markDirty();
    }

    private void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public Map<String, Boolean> toMap() {
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        for (Map.Entry<Setting, Boolean> entry : this.settings.entrySet()) {
            map.put(entry.getKey().name(), entry.getValue());
        }
        return map;
    }

    public void fromMap(Map<String, Boolean> map) {
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            try {
                Setting setting = Setting.valueOf(entry.getKey());
                this.settings.put(setting, entry.getValue());
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
    }

    public String toString() {
        return "PlayerSettings{uuid=" + String.valueOf(this.uuid) + ", settings=" + this.settings.size() + ", preferredKit='" + this.preferredKit + "'}";
    }

    public static enum Setting {
        SCOREBOARD("Scoreboard", "Toggle the scoreboard display", true),
        DEATH_MESSAGES("Death Messages", "Toggle death messages in duels", true),
        DUEL_REQUESTS("Duel Requests", "Allow receiving duel requests", true),
        SPECTATORS_ALLOWED("Allow Spectators", "Allow others to spectate your duels", true),
        PARTY_INVITES("Party Invites", "Allow receiving party invites", true),
        SOUNDS("Sounds", "Toggle plugin sound effects", true),
        QUEUE_NOTIFICATIONS("Queue Notifications", "Show queue join/leave messages", true),
        AUTO_REQUEUE("Auto Requeue", "Automatically requeue after match", false),
        SHOW_PING("Show Ping", "Display ping on scoreboard", true),
        DUEL_BROADCAST("Duel Broadcasts", "See global duel result broadcasts", true),
        PRIVATE_MODE("Private Mode", "Hide from public spectate list", false),
        HIT_PARTICLES("Hit Particles", "Show particles when hitting players", true);

        private final String displayName;
        private final String description;
        private final boolean defaultValue;

        private Setting(String displayName, String description, boolean defaultValue) {
            this.displayName = displayName;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getDescription() {
            return this.description;
        }

        public boolean getDefaultValue() {
            return this.defaultValue;
        }
    }
}

