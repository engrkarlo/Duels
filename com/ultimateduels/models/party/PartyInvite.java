/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.party;

import java.util.Objects;
import java.util.UUID;

public class PartyInvite {
    private final UUID inviteId = UUID.randomUUID();
    private final UUID partyId;
    private final UUID senderId;
    private final String senderName;
    private final UUID targetId;
    private final String targetName;
    private InviteStatus status;
    private final long createdAt;
    private final long expiresAt;
    private long respondedAt;
    private String message;
    private int partySize;
    private int maxPartySize;

    public PartyInvite(UUID partyId, UUID senderId, String senderName, UUID targetId, String targetName, int expirySeconds) {
        this.partyId = partyId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.status = InviteStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + (long)expirySeconds * 1000L;
    }

    public static PartyInvite create(UUID partyId, UUID senderId, String senderName, UUID targetId, String targetName, int expirySeconds, int currentSize, int maxSize) {
        PartyInvite invite = new PartyInvite(partyId, senderId, senderName, targetId, targetName, expirySeconds);
        invite.partySize = currentSize;
        invite.maxPartySize = maxSize;
        return invite;
    }

    public InviteStatus getStatus() {
        if (this.status == InviteStatus.PENDING && this.isExpired()) {
            this.status = InviteStatus.EXPIRED;
        }
        return this.status;
    }

    public boolean accept() {
        if (!this.canRespond()) {
            return false;
        }
        this.status = InviteStatus.ACCEPTED;
        this.respondedAt = System.currentTimeMillis();
        return true;
    }

    public boolean deny() {
        if (!this.canRespond()) {
            return false;
        }
        this.status = InviteStatus.DENIED;
        this.respondedAt = System.currentTimeMillis();
        return true;
    }

    public boolean cancel() {
        if (this.status != InviteStatus.PENDING) {
            return false;
        }
        this.status = InviteStatus.CANCELLED;
        this.respondedAt = System.currentTimeMillis();
        return true;
    }

    public void markPartyFull() {
        if (this.status == InviteStatus.PENDING) {
            this.status = InviteStatus.PARTY_FULL;
            this.respondedAt = System.currentTimeMillis();
        }
    }

    public void markPartyDisbanded() {
        if (this.status == InviteStatus.PENDING) {
            this.status = InviteStatus.PARTY_DISBANDED;
            this.respondedAt = System.currentTimeMillis();
        }
    }

    public boolean canRespond() {
        return this.getStatus().canRespond() && !this.isExpired();
    }

    public boolean isPending() {
        return this.getStatus() == InviteStatus.PENDING;
    }

    public boolean isAccepted() {
        return this.status == InviteStatus.ACCEPTED;
    }

    public boolean isDenied() {
        return this.status == InviteStatus.DENIED;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.expiresAt;
    }

    public boolean isValid() {
        return this.status == InviteStatus.PENDING && !this.isExpired();
    }

    public long getTimeRemaining() {
        long remaining = this.expiresAt - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public int getSecondsRemaining() {
        return (int)(this.getTimeRemaining() / 1000L);
    }

    public String getFormattedTimeRemaining() {
        int seconds = this.getSecondsRemaining();
        if (seconds >= 60) {
            int minutes = seconds / 60;
            return String.format("%dm %ds", minutes, seconds %= 60);
        }
        return seconds + "s";
    }

    public long getAge() {
        return System.currentTimeMillis() - this.createdAt;
    }

    public long getResponseTime() {
        if (this.respondedAt == 0L) {
            return -1L;
        }
        return this.respondedAt - this.createdAt;
    }

    public UUID getInviteId() {
        return this.inviteId;
    }

    public UUID getPartyId() {
        return this.partyId;
    }

    public UUID getSenderId() {
        return this.senderId;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public UUID getTargetId() {
        return this.targetId;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getExpiresAt() {
        return this.expiresAt;
    }

    public long getRespondedAt() {
        return this.respondedAt;
    }

    public String getMessage() {
        return this.message;
    }

    public int getPartySize() {
        return this.partySize;
    }

    public int getMaxPartySize() {
        return this.maxPartySize;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void updatePartyInfo(int currentSize, int maxSize) {
        this.partySize = currentSize;
        this.maxPartySize = maxSize;
    }

    public String getInviteMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u00a7e").append(this.senderName).append(" \u00a77has invited you to their party!");
        if (this.partySize > 0 && this.maxPartySize > 0) {
            sb.append("\n\u00a77Members: \u00a7f").append(this.partySize).append("/").append(this.maxPartySize);
        }
        if (this.message != null && !this.message.isEmpty()) {
            sb.append("\n\u00a77Message: \u00a7f").append(this.message);
        }
        sb.append("\n\u00a77Expires in: \u00a7e").append(this.getFormattedTimeRemaining());
        return sb.toString();
    }

    public String getShortDescription() {
        return String.format("Invite from %s (%s)", this.senderName, this.getFormattedTimeRemaining());
    }

    public boolean involves(UUID uuid) {
        return this.senderId.equals(uuid) || this.targetId.equals(uuid);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PartyInvite that = (PartyInvite)o;
        return Objects.equals(this.inviteId, that.inviteId);
    }

    public int hashCode() {
        return Objects.hash(this.inviteId);
    }

    public String toString() {
        return "PartyInvite{inviteId=" + String.valueOf(this.inviteId) + ", sender=" + this.senderName + ", target=" + this.targetName + ", status=" + String.valueOf((Object)this.status) + ", timeRemaining=" + this.getFormattedTimeRemaining() + "}";
    }

    public static enum InviteStatus {
        PENDING("Pending", "\u00a7e", true),
        ACCEPTED("Accepted", "\u00a7a", false),
        DENIED("Denied", "\u00a7c", false),
        EXPIRED("Expired", "\u00a77", false),
        CANCELLED("Cancelled", "\u00a74", false),
        PARTY_FULL("Party Full", "\u00a7c", false),
        PARTY_DISBANDED("Party Disbanded", "\u00a74", false);

        private final String displayName;
        private final String colorCode;
        private final boolean canRespond;

        private InviteStatus(String displayName, String colorCode, boolean canRespond) {
            this.displayName = displayName;
            this.colorCode = colorCode;
            this.canRespond = canRespond;
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

        public boolean canRespond() {
            return this.canRespond;
        }
    }
}

