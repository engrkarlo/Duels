/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.configuration.ConfigurationSection
 */
package com.ultimateduels.settings;

import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.configuration.ConfigurationSection;

public class PlayerSettings {
    private final UUID uuid;
    private boolean scoreboardEnabled = true;
    private boolean duelRequestsEnabled = true;
    private boolean partyInvitesEnabled = true;
    private boolean deathMessagesEnabled = true;
    private boolean spectatorNotifyEnabled = true;
    private boolean soundsEnabled = true;
    private boolean autoRequeueEnabled = false;
    private boolean showPingEnabled = true;
    private boolean allowSpectatorsEnabled = true;

    public PlayerSettings(@Nonnull UUID uuid) {
        this.uuid = uuid;
    }

    @Nonnull
    public static PlayerSettings fromConfig(@Nonnull UUID uuid, @Nonnull ConfigurationSection config) {
        PlayerSettings settings = new PlayerSettings(uuid);
        settings.scoreboardEnabled = config.getBoolean("scoreboard", true);
        settings.duelRequestsEnabled = config.getBoolean("duel-requests", true);
        settings.partyInvitesEnabled = config.getBoolean("party-invites", true);
        settings.deathMessagesEnabled = config.getBoolean("death-messages", true);
        settings.spectatorNotifyEnabled = config.getBoolean("spectator-notify", true);
        settings.soundsEnabled = config.getBoolean("sounds", true);
        settings.autoRequeueEnabled = config.getBoolean("auto-requeue", false);
        settings.showPingEnabled = config.getBoolean("show-ping", true);
        settings.allowSpectatorsEnabled = config.getBoolean("allow-spectators", true);
        return settings;
    }

    public void toConfig(@Nonnull ConfigurationSection config) {
        config.set("scoreboard", (Object)this.scoreboardEnabled);
        config.set("duel-requests", (Object)this.duelRequestsEnabled);
        config.set("party-invites", (Object)this.partyInvitesEnabled);
        config.set("death-messages", (Object)this.deathMessagesEnabled);
        config.set("spectator-notify", (Object)this.spectatorNotifyEnabled);
        config.set("sounds", (Object)this.soundsEnabled);
        config.set("auto-requeue", (Object)this.autoRequeueEnabled);
        config.set("show-ping", (Object)this.showPingEnabled);
        config.set("allow-spectators", (Object)this.allowSpectatorsEnabled);
    }

    @Nonnull
    public UUID getUuid() {
        return this.uuid;
    }

    public boolean isScoreboardEnabled() {
        return this.scoreboardEnabled;
    }

    public boolean isDuelRequestsEnabled() {
        return this.duelRequestsEnabled;
    }

    public boolean isPartyInvitesEnabled() {
        return this.partyInvitesEnabled;
    }

    public boolean isDeathMessagesEnabled() {
        return this.deathMessagesEnabled;
    }

    public boolean isSpectatorNotifyEnabled() {
        return this.spectatorNotifyEnabled;
    }

    public boolean isSoundsEnabled() {
        return this.soundsEnabled;
    }

    public boolean isAutoRequeueEnabled() {
        return this.autoRequeueEnabled;
    }

    public boolean isShowPingEnabled() {
        return this.showPingEnabled;
    }

    public boolean isAllowSpectatorsEnabled() {
        return this.allowSpectatorsEnabled;
    }

    public boolean isSpectatorsAllowed() {
        return this.allowSpectatorsEnabled;
    }

    public void setScoreboardEnabled(boolean enabled) {
        this.scoreboardEnabled = enabled;
    }

    public void setDuelRequestsEnabled(boolean enabled) {
        this.duelRequestsEnabled = enabled;
    }

    public void setPartyInvitesEnabled(boolean enabled) {
        this.partyInvitesEnabled = enabled;
    }

    public void setDeathMessagesEnabled(boolean enabled) {
        this.deathMessagesEnabled = enabled;
    }

    public void setSpectatorNotifyEnabled(boolean enabled) {
        this.spectatorNotifyEnabled = enabled;
    }

    public void setSoundsEnabled(boolean enabled) {
        this.soundsEnabled = enabled;
    }

    public void setAutoRequeueEnabled(boolean enabled) {
        this.autoRequeueEnabled = enabled;
    }

    public void setShowPingEnabled(boolean enabled) {
        this.showPingEnabled = enabled;
    }

    public void setAllowSpectatorsEnabled(boolean enabled) {
        this.allowSpectatorsEnabled = enabled;
    }

    public void setSpectatorsAllowed(boolean allowed) {
        this.allowSpectatorsEnabled = allowed;
    }

    public boolean toggleScoreboard() {
        this.scoreboardEnabled = !this.scoreboardEnabled;
        return this.scoreboardEnabled;
    }

    public boolean toggleDuelRequests() {
        this.duelRequestsEnabled = !this.duelRequestsEnabled;
        return this.duelRequestsEnabled;
    }

    public boolean togglePartyInvites() {
        this.partyInvitesEnabled = !this.partyInvitesEnabled;
        return this.partyInvitesEnabled;
    }

    public boolean toggleDeathMessages() {
        this.deathMessagesEnabled = !this.deathMessagesEnabled;
        return this.deathMessagesEnabled;
    }

    public boolean toggleSpectatorNotify() {
        this.spectatorNotifyEnabled = !this.spectatorNotifyEnabled;
        return this.spectatorNotifyEnabled;
    }

    public boolean toggleSounds() {
        this.soundsEnabled = !this.soundsEnabled;
        return this.soundsEnabled;
    }

    public boolean toggleAutoRequeue() {
        this.autoRequeueEnabled = !this.autoRequeueEnabled;
        return this.autoRequeueEnabled;
    }

    public boolean toggleShowPing() {
        this.showPingEnabled = !this.showPingEnabled;
        return this.showPingEnabled;
    }

    public boolean toggleAllowSpectators() {
        this.allowSpectatorsEnabled = !this.allowSpectatorsEnabled;
        return this.allowSpectatorsEnabled;
    }

    public void resetToDefaults() {
        this.scoreboardEnabled = true;
        this.duelRequestsEnabled = true;
        this.partyInvitesEnabled = true;
        this.deathMessagesEnabled = true;
        this.spectatorNotifyEnabled = true;
        this.soundsEnabled = true;
        this.autoRequeueEnabled = false;
        this.showPingEnabled = true;
        this.allowSpectatorsEnabled = true;
    }

    public String toString() {
        return "PlayerSettings{uuid=" + String.valueOf(this.uuid) + ", scoreboard=" + this.scoreboardEnabled + ", duelRequests=" + this.duelRequestsEnabled + ", partyInvites=" + this.partyInvitesEnabled + ", allowSpectators=" + this.allowSpectatorsEnabled + "}";
    }
}

