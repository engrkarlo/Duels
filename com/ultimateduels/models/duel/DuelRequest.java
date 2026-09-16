/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.duel;

import com.ultimateduels.models.duel.DuelSettings;
import java.util.Objects;
import java.util.UUID;

public class DuelRequest {
    private final UUID requestId = UUID.randomUUID();
    private final UUID senderId;
    private final UUID targetId;
    private final String senderName;
    private final String targetName;
    private final DuelSettings settings;
    private RequestStatus status;
    private final long createdAt;
    private final long expiresAt;
    private long respondedAt;
    private UUID senderPartyId;
    private UUID targetPartyId;
    private boolean isPartyDuel;

    public DuelRequest(UUID senderId, String senderName, UUID targetId, String targetName, DuelSettings settings, int expirySeconds) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.settings = settings;
        this.status = RequestStatus.PENDING;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + (long)expirySeconds * 1000L;
        this.isPartyDuel = false;
    }

    public static DuelRequest createPartyRequest(UUID senderId, String senderName, UUID targetId, String targetName, DuelSettings settings, int expirySeconds, UUID senderPartyId, UUID targetPartyId) {
        DuelRequest request = new DuelRequest(senderId, senderName, targetId, targetName, settings, expirySeconds);
        request.senderPartyId = senderPartyId;
        request.targetPartyId = targetPartyId;
        request.isPartyDuel = true;
        return request;
    }

    public RequestStatus getStatus() {
        if (this.status == RequestStatus.PENDING && this.isExpired()) {
            this.status = RequestStatus.EXPIRED;
        }
        return this.status;
    }

    public boolean accept() {
        if (this.status != RequestStatus.PENDING || this.isExpired()) {
            return false;
        }
        this.status = RequestStatus.ACCEPTED;
        this.respondedAt = System.currentTimeMillis();
        return true;
    }

    public boolean deny() {
        if (this.status != RequestStatus.PENDING) {
            return false;
        }
        this.status = RequestStatus.DENIED;
        this.respondedAt = System.currentTimeMillis();
        return true;
    }

    public boolean cancel() {
        if (this.status != RequestStatus.PENDING) {
            return false;
        }
        this.status = RequestStatus.CANCELLED;
        this.respondedAt = System.currentTimeMillis();
        return true;
    }

    public void markExpired() {
        if (this.status == RequestStatus.PENDING) {
            this.status = RequestStatus.EXPIRED;
        }
    }

    public boolean isPending() {
        return this.getStatus() == RequestStatus.PENDING;
    }

    public boolean isAccepted() {
        return this.status == RequestStatus.ACCEPTED;
    }

    public boolean isDenied() {
        return this.status == RequestStatus.DENIED;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.expiresAt;
    }

    public boolean isValid() {
        return this.status == RequestStatus.PENDING && !this.isExpired();
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

    public UUID getRequestId() {
        return this.requestId;
    }

    public UUID getSenderId() {
        return this.senderId;
    }

    public UUID getTargetId() {
        return this.targetId;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public DuelSettings getSettings() {
        return this.settings;
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

    public UUID getSenderPartyId() {
        return this.senderPartyId;
    }

    public UUID getTargetPartyId() {
        return this.targetPartyId;
    }

    public boolean isPartyDuel() {
        return this.isPartyDuel;
    }

    public boolean involves(UUID uuid) {
        return this.senderId.equals(uuid) || this.targetId.equals(uuid);
    }

    public UUID getOtherPlayer(UUID uuid) {
        if (this.senderId.equals(uuid)) {
            return this.targetId;
        }
        if (this.targetId.equals(uuid)) {
            return this.senderId;
        }
        return null;
    }

    public String getKitName() {
        return this.settings.getKitId();
    }

    public String getArenaName() {
        String arenaId = this.settings.getArenaId();
        return arenaId != null ? arenaId : "Random";
    }

    public String getSummary() {
        return String.format("Kit: %s | Arena: %s | Rounds: %d (%s)", this.getKitName(), this.getArenaName(), this.settings.getRounds(), this.settings.getWinCondition().getDisplayName());
    }

    public DuelRequest withSettings(DuelSettings newSettings, int expirySeconds) {
        return new DuelRequest(this.senderId, this.senderName, this.targetId, this.targetName, newSettings, expirySeconds);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelRequest that = (DuelRequest)o;
        return Objects.equals(this.requestId, that.requestId);
    }

    public int hashCode() {
        return Objects.hash(this.requestId);
    }

    public String toString() {
        return "DuelRequest{requestId=" + String.valueOf(this.requestId) + ", sender=" + this.senderName + ", target=" + this.targetName + ", status=" + String.valueOf((Object)this.status) + ", kit=" + this.getKitName() + ", timeRemaining=" + this.getFormattedTimeRemaining() + "}";
    }

    public static enum RequestStatus {
        PENDING("Pending", "\u00a7e"),
        ACCEPTED("Accepted", "\u00a7a"),
        DENIED("Denied", "\u00a7c"),
        EXPIRED("Expired", "\u00a77"),
        CANCELLED("Cancelled", "\u00a74");

        private final String displayName;
        private final String colorCode;

        private RequestStatus(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
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
    }
}

