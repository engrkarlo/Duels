/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  org.bukkit.Bukkit
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.party.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Party {
    private final UUID partyId;
    private UUID leaderUUID;
    private String leaderName;
    private final Set<UUID> members;
    private final Map<UUID, String> memberNames;
    private final long createdAt;
    private boolean isPublic;

    public Party(@Nonnull UUID partyId, @Nonnull UUID leaderUUID, @Nonnull String leaderName) {
        this.partyId = partyId;
        this.leaderUUID = leaderUUID;
        this.leaderName = leaderName;
        this.members = ConcurrentHashMap.newKeySet();
        this.memberNames = new ConcurrentHashMap<UUID, String>();
        this.createdAt = System.currentTimeMillis();
        this.isPublic = false;
        this.memberNames.put(leaderUUID, leaderName);
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean togglePublic() {
        this.isPublic = !this.isPublic;
        return this.isPublic;
    }

    public void addMember(@Nonnull UUID uuid, @Nonnull String name) {
        this.members.add(uuid);
        this.memberNames.put(uuid, name);
    }

    public void removeMember(@Nonnull UUID uuid) {
        this.members.remove(uuid);
        this.memberNames.remove(uuid);
    }

    public boolean hasMember(@Nonnull UUID uuid) {
        return this.leaderUUID.equals(uuid) || this.members.contains(uuid);
    }

    public boolean isLeader(@Nonnull UUID uuid) {
        return this.leaderUUID.equals(uuid);
    }

    @Nonnull
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    @Nonnull
    public Set<UUID> getAllMembers() {
        HashSet<UUID> all = new HashSet<UUID>(this.members);
        all.add(this.leaderUUID);
        return all;
    }

    @Nonnull
    public List<Player> getOnlineMembers() {
        ArrayList<Player> online = new ArrayList<Player>();
        for (UUID uuid : this.getAllMembers()) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            online.add(player);
        }
        return online;
    }

    public int getOnlineMemberCount() {
        return this.getOnlineMembers().size();
    }

    public int getSize() {
        return this.members.size() + 1;
    }

    public boolean isEmpty() {
        return this.members.isEmpty() && Bukkit.getPlayer((UUID)this.leaderUUID) == null;
    }

    public boolean isFull(int maxSize) {
        return this.getSize() >= maxSize;
    }

    public void setLeader(@Nonnull UUID newLeaderUUID) {
        if (!this.leaderUUID.equals(newLeaderUUID)) {
            this.members.add(this.leaderUUID);
            this.members.remove(newLeaderUUID);
        }
        this.leaderUUID = newLeaderUUID;
        String name = this.memberNames.get(newLeaderUUID);
        this.leaderName = name != null ? name : "Unknown";
    }

    public Player getLeader() {
        return Bukkit.getPlayer((UUID)this.leaderUUID);
    }

    public boolean isLeaderOnline() {
        Player leader = this.getLeader();
        return leader != null && leader.isOnline();
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

    public void sendMessage(@Nonnull String message) {
        this.broadcast(message);
    }

    public void sendTitle(@Nonnull String title, @Nonnull String subtitle, int fadeIn, int stay, int fadeOut) {
        for (UUID uuid : this.getAllMembers()) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
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
    public String getLeaderName() {
        return this.leaderName;
    }

    @Nonnull
    public String getMemberName(@Nonnull UUID uuid) {
        return this.memberNames.getOrDefault(uuid, "Unknown");
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public long getAge() {
        return System.currentTimeMillis() - this.createdAt;
    }

    @Nonnull
    public String getStatusString() {
        return this.isPublic ? "Public" : "Invite Only";
    }

    @Nonnull
    public String getFormattedMemberList() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u00a76\u2605 ").append(this.leaderName);
        for (UUID memberUUID : this.members) {
            sb.append("\u00a77, \u00a7f").append(this.memberNames.getOrDefault(memberUUID, "Unknown"));
        }
        return sb.toString();
    }

    public boolean canJoin(@Nonnull UUID playerUUID, int maxSize) {
        if (this.hasMember(playerUUID)) {
            return false;
        }
        return !this.isFull(maxSize);
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
        return "Party{id=" + this.partyId.toString().substring(0, 8) + ", leader=" + this.leaderName + ", size=" + this.getSize() + ", public=" + this.isPublic + "}";
    }
}

