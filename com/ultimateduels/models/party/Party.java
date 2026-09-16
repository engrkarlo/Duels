/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Bukkit
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.models.party;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Party {
    private final UUID partyId;
    private UUID leaderUUID;
    private String leaderName;
    private final Map<UUID, String> members;
    private boolean isPublic;

    public Party(UUID partyId, UUID leaderUUID, String leaderName) {
        this.partyId = partyId;
        this.leaderUUID = leaderUUID;
        this.leaderName = leaderName;
        this.members = new ConcurrentHashMap<UUID, String>();
        this.isPublic = false;
    }

    @Nonnull
    public UUID getPartyId() {
        return this.partyId;
    }

    @Nonnull
    public UUID getLeaderUUID() {
        return this.leaderUUID;
    }

    @Nonnull
    public UUID getLeaderId() {
        return this.leaderUUID;
    }

    @Nonnull
    public String getLeaderName() {
        return this.leaderName;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isOpen() {
        return this.isPublic;
    }

    public void setOpen(boolean open) {
        this.isPublic = open;
    }

    public void addMember(@Nonnull UUID uuid, @Nonnull String name) {
        if (!uuid.equals(this.leaderUUID)) {
            this.members.put(uuid, name);
        }
    }

    public void removeMember(@Nonnull UUID uuid) {
        this.members.remove(uuid);
    }

    public boolean hasMember(@Nonnull UUID uuid) {
        return this.members.containsKey(uuid) || uuid.equals(this.leaderUUID);
    }

    public boolean isLeader(@Nonnull UUID uuid) {
        return this.leaderUUID.equals(uuid);
    }

    @Nonnull
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(this.members.keySet());
    }

    @Nonnull
    public Set<UUID> getAllMembers() {
        HashSet<UUID> all = new HashSet<UUID>(this.members.keySet());
        all.add(this.leaderUUID);
        return Collections.unmodifiableSet(all);
    }

    @Nullable
    public String getMemberName(@Nonnull UUID uuid) {
        if (uuid.equals(this.leaderUUID)) {
            return this.leaderName;
        }
        return this.members.get(uuid);
    }

    public int getSize() {
        return this.members.size() + 1;
    }

    public boolean isEmpty() {
        return this.members.isEmpty();
    }

    public void setLeader(@Nonnull UUID newLeaderUUID) {
        String newLeaderName;
        if (!this.members.containsKey(this.leaderUUID)) {
            this.members.put(this.leaderUUID, this.leaderName);
        }
        if ((newLeaderName = this.members.get(newLeaderUUID)) == null) {
            Player player = Bukkit.getPlayer((UUID)newLeaderUUID);
            newLeaderName = player != null ? player.getName() : "Unknown";
        }
        this.members.remove(newLeaderUUID);
        this.leaderUUID = newLeaderUUID;
        this.leaderName = newLeaderName;
    }

    public void broadcast(@Nonnull String message) {
        for (UUID uuid : this.getAllMembers()) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            player.sendMessage(message);
        }
    }

    public void playSound(@Nonnull Sound sound, float volume, float pitch) {
        for (UUID uuid : this.getAllMembers()) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Party party = (Party)o;
        return Objects.equals(this.partyId, party.partyId);
    }

    public int hashCode() {
        return Objects.hash(this.partyId);
    }

    public String toString() {
        return "Party{partyId=" + String.valueOf(this.partyId) + ", leader=" + this.leaderName + ", size=" + this.getSize() + ", public=" + this.isPublic + "}";
    }
}

