/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Location
 */
package com.ultimateduels.arena.model;

import com.ultimateduels.arena.model.ArenaSpawnPoint;
import com.ultimateduels.arena.model.ArenaState;
import com.ultimateduels.arena.model.ArenaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;

public class DuelArena {
    private String name;
    private String displayName;
    private ArenaType arenaType;
    private ArenaState state;
    private boolean enabled;
    private Location spawnPoint1;
    private Location spawnPoint2;
    private Location spectatorSpawn;
    private List<ArenaSpawnPoint> spawnPoints;
    private Location corner1;
    private Location corner2;
    private String schematicName;
    private List<String> compatibleKits;
    private String linkedKit;
    private Set<String> assignedKits;
    private boolean useAssignedKits;
    private boolean allowBuilding;
    private boolean allowBreaking;
    private boolean allowBlockBreak;
    private boolean allowBlockPlace;
    private int buildHeightLimit;
    private int deathY;
    private boolean voidKills;
    private int totalMatches;
    private int regenerationCount;
    private long createdAt;
    private long lastUsed;

    public DuelArena(@Nonnull String name, @Nonnull ArenaType arenaType) {
        this.name = name;
        this.displayName = name;
        this.arenaType = arenaType;
        this.state = ArenaState.SETUP;
        this.enabled = false;
        this.spawnPoints = new ArrayList<ArenaSpawnPoint>();
        this.compatibleKits = new ArrayList<String>();
        this.assignedKits = new HashSet<String>();
        this.useAssignedKits = false;
        this.allowBuilding = false;
        this.allowBreaking = false;
        this.allowBlockBreak = false;
        this.allowBlockPlace = false;
        this.buildHeightLimit = 256;
        this.deathY = 0;
        this.voidKills = true;
        this.totalMatches = 0;
        this.regenerationCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastUsed = 0L;
    }

    @Nullable
    public Location getSpawnPoint1() {
        return this.spawnPoint1;
    }

    public void setSpawnPoint1(@Nullable Location spawnPoint1) {
        this.spawnPoint1 = spawnPoint1;
    }

    @Nullable
    public Location getSpawnPoint2() {
        return this.spawnPoint2;
    }

    public void setSpawnPoint2(@Nullable Location spawnPoint2) {
        this.spawnPoint2 = spawnPoint2;
    }

    @Nullable
    public Location getSpectatorSpawn() {
        return this.spectatorSpawn;
    }

    public void setSpectatorSpawn(@Nullable Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    @Nonnull
    public List<ArenaSpawnPoint> getSpawnPoints() {
        return this.spawnPoints;
    }

    public void setSpawnPoints(@Nonnull List<ArenaSpawnPoint> spawnPoints) {
        this.spawnPoints = spawnPoints;
    }

    public void addSpawnPoint(@Nonnull ArenaSpawnPoint spawnPoint) {
        this.spawnPoints.add(spawnPoint);
    }

    @Nullable
    public Location getRandomSpawnPoint() {
        if (this.spawnPoints.isEmpty()) {
            return null;
        }
        int index = new Random().nextInt(this.spawnPoints.size());
        return this.spawnPoints.get(index).getLocation();
    }

    @Nullable
    public Location getSpawnPoint(int index) {
        if (index < 0 || index >= this.spawnPoints.size()) {
            return null;
        }
        return this.spawnPoints.get(index).getLocation();
    }

    @Nullable
    public Location getCorner1() {
        return this.corner1;
    }

    public void setCorner1(@Nullable Location corner1) {
        this.corner1 = corner1;
    }

    @Nullable
    public Location getCorner2() {
        return this.corner2;
    }

    public void setCorner2(@Nullable Location corner2) {
        this.corner2 = corner2;
    }

    public boolean hasBounds() {
        return this.corner1 != null && this.corner2 != null && this.corner1.getWorld() != null && this.corner1.getWorld().equals((Object)this.corner2.getWorld());
    }

    @Nullable
    public Location getMinPoint() {
        if (!this.hasBounds()) {
            return null;
        }
        return new Location(this.corner1.getWorld(), Math.min(this.corner1.getX(), this.corner2.getX()), Math.min(this.corner1.getY(), this.corner2.getY()), Math.min(this.corner1.getZ(), this.corner2.getZ()));
    }

    @Nullable
    public Location getMaxPoint() {
        if (!this.hasBounds()) {
            return null;
        }
        return new Location(this.corner1.getWorld(), Math.max(this.corner1.getX(), this.corner2.getX()), Math.max(this.corner1.getY(), this.corner2.getY()), Math.max(this.corner1.getZ(), this.corner2.getZ()));
    }

    public boolean isWithinBounds(@Nonnull Location loc) {
        if (!this.hasBounds()) {
            return false;
        }
        if (!loc.getWorld().equals((Object)this.corner1.getWorld())) {
            return false;
        }
        Location min = this.getMinPoint();
        Location max = this.getMaxPoint();
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() && loc.getY() >= min.getY() && loc.getY() <= max.getY() && loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }

    public int getSize() {
        if (!this.hasBounds()) {
            return 0;
        }
        Location min = this.getMinPoint();
        Location max = this.getMaxPoint();
        int x = (int)(max.getX() - min.getX()) + 1;
        int y = (int)(max.getY() - min.getY()) + 1;
        int z = (int)(max.getZ() - min.getZ()) + 1;
        return x * y * z;
    }

    @Nullable
    public Location getCenter() {
        if (!this.hasBounds()) {
            return null;
        }
        return new Location(this.corner1.getWorld(), (this.corner1.getX() + this.corner2.getX()) / 2.0, (this.corner1.getY() + this.corner2.getY()) / 2.0, (this.corner1.getZ() + this.corner2.getZ()) / 2.0);
    }

    @Nonnull
    public List<String> getCompatibleKits() {
        return this.compatibleKits;
    }

    public void setCompatibleKits(@Nonnull List<String> compatibleKits) {
        this.compatibleKits = new ArrayList<String>(compatibleKits.stream().map(String::toLowerCase).toList());
    }

    public void addCompatibleKit(@Nonnull String kitName) {
        String key = kitName.toLowerCase();
        if (!this.compatibleKits.contains(key)) {
            this.compatibleKits.add(key);
        }
    }

    public void removeCompatibleKit(@Nonnull String kitName) {
        this.compatibleKits.remove(kitName.toLowerCase());
    }

    public boolean isKitCompatible(@Nonnull String kitName) {
        if (this.compatibleKits.isEmpty()) {
            return true;
        }
        return this.compatibleKits.contains(kitName.toLowerCase());
    }

    @Nullable
    public String getLinkedKit() {
        return this.linkedKit;
    }

    public void setLinkedKit(@Nullable String linkedKit) {
        this.linkedKit = linkedKit;
    }

    public boolean canUseKit(@Nonnull String kitName) {
        if (this.useAssignedKits) {
            if (this.assignedKits.isEmpty()) {
                return false;
            }
            return this.assignedKits.stream().anyMatch(k -> k.equalsIgnoreCase(kitName));
        }
        return this.isKitCompatible(kitName);
    }

    public void assignKit(@Nonnull String kitName) {
        this.assignedKits.add(kitName.toLowerCase());
    }

    public void unassignKit(@Nonnull String kitName) {
        this.assignedKits.remove(kitName.toLowerCase());
    }

    public void clearAssignedKits() {
        this.assignedKits.clear();
    }

    public void setUseAssignedKits(boolean use) {
        this.useAssignedKits = use;
    }

    @Nonnull
    public Set<String> getAssignedKits() {
        return new HashSet<String>(this.assignedKits);
    }

    public void setAssignedKits(@Nonnull Set<String> kits) {
        this.assignedKits = new HashSet<String>();
        for (String kit : kits) {
            this.assignedKits.add(kit.toLowerCase());
        }
    }

    public boolean isUsingAssignedKits() {
        return this.useAssignedKits;
    }

    public boolean hasAssignedKits() {
        return !this.assignedKits.isEmpty();
    }

    public boolean isAllowBlockBreak() {
        return this.allowBlockBreak;
    }

    public void setAllowBlockBreak(boolean allow) {
        this.allowBlockBreak = allow;
    }

    public boolean isAllowBlockPlace() {
        return this.allowBlockPlace;
    }

    public void setAllowBlockPlace(boolean allow) {
        this.allowBlockPlace = allow;
    }

    public boolean isFullyConfigured() {
        if (this.arenaType == ArenaType.FFA) {
            boolean allSpawnsValid;
            boolean hasKit;
            boolean hasSpawns = !this.spawnPoints.isEmpty();
            boolean bl = hasKit = this.linkedKit != null;
            if (hasSpawns && !(allSpawnsValid = this.spawnPoints.stream().allMatch(sp -> sp.getLocation() != null && sp.getLocation().getWorld() != null))) {
                hasSpawns = false;
            }
            return hasSpawns && hasKit;
        }
        boolean hasSpawn1 = this.spawnPoint1 != null && this.spawnPoint1.getWorld() != null;
        boolean hasSpawn2 = this.spawnPoint2 != null && this.spawnPoint2.getWorld() != null;
        return hasSpawn1 && hasSpawn2;
    }

    @Nonnull
    public String getConfigurationStatus() {
        ArrayList<String> missing = new ArrayList<String>();
        if (this.arenaType == ArenaType.FFA) {
            if (this.spawnPoints.isEmpty()) {
                missing.add("FFA Spawn Points (need at least 1)");
            }
            if (this.linkedKit == null) {
                missing.add("Linked Kit");
            }
        } else {
            if (this.spawnPoint1 == null) {
                missing.add("Spawn Point 1");
            }
            if (this.spawnPoint2 == null) {
                missing.add("Spawn Point 2");
            }
        }
        if (missing.isEmpty()) {
            return "\u00a7aFully Configured";
        }
        return "\u00a7cMissing: " + String.join((CharSequence)", ", missing);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(@Nonnull String displayName) {
        this.displayName = displayName;
    }

    @Nonnull
    public ArenaType getArenaType() {
        return this.arenaType;
    }

    public void setArenaType(@Nonnull ArenaType arenaType) {
        this.arenaType = arenaType;
    }

    @Nonnull
    public ArenaState getState() {
        return this.state;
    }

    public void setState(@Nonnull ArenaState state) {
        this.state = state;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    public String getSchematicName() {
        return this.schematicName;
    }

    public void setSchematicName(@Nullable String schematicName) {
        this.schematicName = schematicName;
    }

    public boolean isAllowBuilding() {
        return this.allowBuilding;
    }

    public void setAllowBuilding(boolean allowBuilding) {
        this.allowBuilding = allowBuilding;
    }

    public boolean isAllowBreaking() {
        return this.allowBreaking;
    }

    public void setAllowBreaking(boolean allowBreaking) {
        this.allowBreaking = allowBreaking;
    }

    public int getBuildHeightLimit() {
        return this.buildHeightLimit;
    }

    public void setBuildHeightLimit(int buildHeightLimit) {
        this.buildHeightLimit = buildHeightLimit;
    }

    public int getDeathY() {
        return this.deathY;
    }

    public void setDeathY(int deathY) {
        this.deathY = deathY;
    }

    public boolean isVoidKills() {
        return this.voidKills;
    }

    public void setVoidKills(boolean voidKills) {
        this.voidKills = voidKills;
    }

    public int getTotalMatches() {
        return this.totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public void incrementTotalMatches() {
        ++this.totalMatches;
        this.lastUsed = System.currentTimeMillis();
    }

    public int getRegenerationCount() {
        return this.regenerationCount;
    }

    public void setRegenerationCount(int regenerationCount) {
        this.regenerationCount = regenerationCount;
    }

    public void incrementRegenerationCount() {
        ++this.regenerationCount;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public boolean isAvailable() {
        return this.enabled && this.state == ArenaState.AVAILABLE && this.isFullyConfigured();
    }

    public boolean isInUse() {
        return this.state == ArenaState.IN_USE;
    }

    @Nonnull
    public List<String> getFormattedLore() {
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("\u00a77Type: \u00a7f" + this.arenaType.getDisplayName());
        lore.add("\u00a77State: " + this.state.getColoredName());
        lore.add("");
        lore.add(this.getConfigurationStatus());
        if (this.hasBounds()) {
            lore.add("\u00a77Size: \u00a7f" + this.getSize() + " blocks");
        }
        if (this.arenaType == ArenaType.FFA) {
            lore.add("\u00a77Spawn Points: \u00a7f" + this.spawnPoints.size());
            if (this.linkedKit != null) {
                lore.add("\u00a77Linked Kit: \u00a7f" + this.linkedKit);
            } else {
                lore.add("\u00a77Linked Kit: \u00a7cNot Set");
            }
        } else if (this.useAssignedKits && !this.assignedKits.isEmpty()) {
            lore.add("\u00a77Assigned Kits: \u00a7e" + String.join((CharSequence)", ", this.assignedKits));
        } else if (!this.compatibleKits.isEmpty()) {
            lore.add("\u00a77Compatible Kits: \u00a7f" + String.join((CharSequence)", ", this.compatibleKits));
        } else {
            lore.add("\u00a77Compatible Kits: \u00a7aAll");
        }
        lore.add("");
        lore.add("\u00a77Block Break: " + (this.allowBlockBreak ? "\u00a7aAllowed" : "\u00a7cBlocked"));
        lore.add("\u00a77Block Place: " + (this.allowBlockPlace ? "\u00a7aAllowed" : "\u00a7cBlocked"));
        lore.add("");
        lore.add("\u00a77Total Matches: \u00a7f" + this.totalMatches);
        lore.add("\u00a77Regenerations: \u00a7f" + this.regenerationCount);
        return lore;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelArena arena = (DuelArena)o;
        return Objects.equals(this.name.toLowerCase(), arena.name.toLowerCase());
    }

    public int hashCode() {
        return Objects.hash(this.name.toLowerCase());
    }

    public String toString() {
        return "DuelArena{name='" + this.name + "', type=" + String.valueOf((Object)this.arenaType) + ", state=" + String.valueOf((Object)this.state) + ", enabled=" + this.enabled + ", configured=" + this.isFullyConfigured() + ", useAssignedKits=" + this.useAssignedKits + ", allowBlockBreak=" + this.allowBlockBreak + ", allowBlockPlace=" + this.allowBlockPlace + "}";
    }
}

