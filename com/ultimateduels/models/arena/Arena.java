/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 */
package com.ultimateduels.models.arena;

import com.ultimateduels.models.arena.ArenaSpawnPoints;
import com.ultimateduels.models.arena.ArenaState;
import com.ultimateduels.models.arena.ArenaType;
import com.ultimateduels.models.kit.Kit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Arena {
    private final String id;
    private String displayName;
    private List<String> description;
    private ArenaType type;
    private ArenaState state;
    private long stateChangedAt;
    private String worldName;
    private ArenaSpawnPoints spawnPoints;
    private Location center;
    private Material iconMaterial;
    private boolean iconGlow;
    private boolean enabled;
    private int weight;
    private int maxTeamSize;
    private boolean boundariesEnabled;
    private Location boundaryMin;
    private Location boundaryMax;
    private boolean voidDeathEnabled;
    private int voidDeathYLevel;
    private String schematicFile;
    private Location schematicOrigin;
    private boolean schematicIgnoreAir;
    private Set<String> compatibleKits;
    private Set<String> incompatibleKits;
    private Map<String, Object> settings;
    private ArenaBoundingBox goalTeam1;
    private ArenaBoundingBox goalTeam2;
    private UUID currentMatchId;
    private long matchStartedAt;
    private int totalMatchesPlayed;
    private long totalPlaytime;

    public Arena(String id) {
        this.id = id.toLowerCase();
        this.displayName = id;
        this.description = new ArrayList<String>();
        this.type = ArenaType.DUEL;
        this.state = ArenaState.AVAILABLE;
        this.stateChangedAt = System.currentTimeMillis();
        this.enabled = true;
        this.weight = 1;
        this.maxTeamSize = 5;
        this.boundariesEnabled = false;
        this.voidDeathEnabled = false;
        this.voidDeathYLevel = 0;
        this.schematicIgnoreAir = false;
        this.compatibleKits = new HashSet<String>();
        this.incompatibleKits = new HashSet<String>();
        this.settings = new HashMap<String, Object>();
        this.spawnPoints = new ArenaSpawnPoints();
    }

    public static Arena fromConfig(String id, ConfigurationSection section, World world) {
        ConfigurationSection goalsSection;
        ConfigurationSection schemSection;
        ConfigurationSection voidSection;
        ConfigurationSection boundSection;
        Arena arena = new Arena(id);
        arena.displayName = section.getString("display-name", id);
        arena.description = section.getStringList("description");
        arena.worldName = section.getString("world", world != null ? world.getName() : "world");
        arena.enabled = section.getBoolean("enabled", true);
        arena.weight = section.getInt("weight", 1);
        arena.maxTeamSize = section.getInt("max-team-size", 5);
        String iconName = section.getString("icon.material", "STONE");
        arena.iconMaterial = Material.getMaterial((String)iconName.toUpperCase());
        arena.iconGlow = section.getBoolean("icon.glow", false);
        String typeStr = section.getString("type", "DUEL");
        try {
            arena.type = ArenaType.valueOf(typeStr.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            arena.type = ArenaType.DUEL;
        }
        if (world != null) {
            arena.spawnPoints = ArenaSpawnPoints.fromConfig(section, world);
            ConfigurationSection centerSection = section.getConfigurationSection("center");
            if (centerSection != null) {
                arena.center = Arena.loadLocation(centerSection, world);
            }
        }
        if ((boundSection = section.getConfigurationSection("boundaries")) != null) {
            arena.boundariesEnabled = boundSection.getBoolean("enabled", false);
            if (arena.boundariesEnabled && world != null) {
                ConfigurationSection minSection = boundSection.getConfigurationSection("min");
                ConfigurationSection maxSection = boundSection.getConfigurationSection("max");
                if (minSection != null && maxSection != null) {
                    arena.boundaryMin = Arena.loadLocation(minSection, world);
                    arena.boundaryMax = Arena.loadLocation(maxSection, world);
                }
            }
        }
        if ((voidSection = section.getConfigurationSection("void-death")) != null) {
            arena.voidDeathEnabled = voidSection.getBoolean("enabled", false);
            arena.voidDeathYLevel = voidSection.getInt("y-level", 0);
        }
        if ((schemSection = section.getConfigurationSection("schematic")) != null) {
            arena.schematicFile = schemSection.getString("file");
            arena.schematicIgnoreAir = schemSection.getBoolean("ignore-air", false);
            ConfigurationSection originSection = schemSection.getConfigurationSection("origin");
            if (originSection != null && world != null) {
                arena.schematicOrigin = Arena.loadLocation(originSection, world);
            }
        }
        arena.compatibleKits = new HashSet<String>(section.getStringList("compatible-kits"));
        arena.incompatibleKits = new HashSet<String>(section.getStringList("incompatible-kits"));
        ConfigurationSection settingsSection = section.getConfigurationSection("settings");
        if (settingsSection != null) {
            for (String key : settingsSection.getKeys(false)) {
                arena.settings.put(key, settingsSection.get(key));
            }
        }
        if ((goalsSection = section.getConfigurationSection("goals")) != null && world != null) {
            Location max;
            Location min;
            ConfigurationSection team1 = goalsSection.getConfigurationSection("team1");
            ConfigurationSection team2 = goalsSection.getConfigurationSection("team2");
            if (team1 != null) {
                min = Arena.loadLocation(team1.getConfigurationSection("min"), world);
                max = Arena.loadLocation(team1.getConfigurationSection("max"), world);
                if (min != null && max != null) {
                    arena.goalTeam1 = new ArenaBoundingBox(min, max);
                }
            }
            if (team2 != null) {
                min = Arena.loadLocation(team2.getConfigurationSection("min"), world);
                max = Arena.loadLocation(team2.getConfigurationSection("max"), world);
                if (min != null && max != null) {
                    arena.goalTeam2 = new ArenaBoundingBox(min, max);
                }
            }
        }
        return arena;
    }

    private static Location loadLocation(ConfigurationSection section, World world) {
        if (section == null || world == null) {
            return null;
        }
        double x = section.getDouble("x", 0.0);
        double y = section.getDouble("y", 64.0);
        double z = section.getDouble("z", 0.0);
        float yaw = (float)section.getDouble("yaw", 0.0);
        float pitch = (float)section.getDouble("pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public ArenaState getState() {
        return this.state;
    }

    public void setState(ArenaState state) {
        this.state = state;
        this.stateChangedAt = System.currentTimeMillis();
    }

    public boolean isAvailable() {
        return this.enabled && this.state == ArenaState.AVAILABLE;
    }

    public boolean isInUse() {
        return this.state == ArenaState.IN_USE;
    }

    public boolean isRegenerating() {
        return this.state == ArenaState.REGENERATING;
    }

    public void markInUse(UUID matchId) {
        this.state = ArenaState.IN_USE;
        this.currentMatchId = matchId;
        this.matchStartedAt = System.currentTimeMillis();
        this.stateChangedAt = System.currentTimeMillis();
    }

    public void markRegenerating() {
        this.setState(ArenaState.REGENERATING);
        this.currentMatchId = null;
    }

    public void markAvailable() {
        this.setState(ArenaState.AVAILABLE);
        this.currentMatchId = null;
        if (this.matchStartedAt > 0L) {
            ++this.totalMatchesPlayed;
            this.totalPlaytime += System.currentTimeMillis() - this.matchStartedAt;
            this.matchStartedAt = 0L;
        }
    }

    public boolean isKitCompatible(String kitId) {
        if (this.incompatibleKits.contains(kitId)) {
            return false;
        }
        return this.compatibleKits.isEmpty() || this.compatibleKits.contains(kitId);
    }

    public boolean isKitCompatible(Kit kit) {
        return this.isKitCompatible(kit.getId());
    }

    public void addCompatibleKit(String kitId) {
        this.compatibleKits.add(kitId);
        this.incompatibleKits.remove(kitId);
    }

    public void addIncompatibleKit(String kitId) {
        this.incompatibleKits.add(kitId);
        this.compatibleKits.remove(kitId);
    }

    public boolean isWithinBounds(Location location) {
        if (!this.boundariesEnabled || this.boundaryMin == null || this.boundaryMax == null) {
            return true;
        }
        if (!location.getWorld().getName().equals(this.worldName)) {
            return false;
        }
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return x >= this.boundaryMin.getX() && x <= this.boundaryMax.getX() && y >= this.boundaryMin.getY() && y <= this.boundaryMax.getY() && z >= this.boundaryMin.getZ() && z <= this.boundaryMax.getZ();
    }

    public boolean isInVoid(Location location) {
        if (!this.voidDeathEnabled) {
            return false;
        }
        return location.getY() <= (double)this.voidDeathYLevel;
    }

    public boolean isInGoal1(Location location) {
        return this.goalTeam1 != null && this.goalTeam1.contains(location);
    }

    public boolean isInGoal2(Location location) {
        return this.goalTeam2 != null && this.goalTeam2.contains(location);
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getDescription() {
        return this.description;
    }

    public ArenaType getType() {
        return this.type;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public ArenaSpawnPoints getSpawnPoints() {
        return this.spawnPoints;
    }

    public Location getCenter() {
        return this.center;
    }

    public Material getIconMaterial() {
        return this.iconMaterial;
    }

    public boolean isIconGlow() {
        return this.iconGlow;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMaxTeamSize() {
        return this.maxTeamSize;
    }

    public boolean isBoundariesEnabled() {
        return this.boundariesEnabled;
    }

    public Location getBoundaryMin() {
        return this.boundaryMin;
    }

    public Location getBoundaryMax() {
        return this.boundaryMax;
    }

    public boolean isVoidDeathEnabled() {
        return this.voidDeathEnabled;
    }

    public int getVoidDeathYLevel() {
        return this.voidDeathYLevel;
    }

    public String getSchematicFile() {
        return this.schematicFile;
    }

    public Location getSchematicOrigin() {
        return this.schematicOrigin;
    }

    public boolean isSchematicIgnoreAir() {
        return this.schematicIgnoreAir;
    }

    public Set<String> getCompatibleKits() {
        return this.compatibleKits;
    }

    public Set<String> getIncompatibleKits() {
        return this.incompatibleKits;
    }

    public Map<String, Object> getSettings() {
        return this.settings;
    }

    public ArenaBoundingBox getGoalTeam1() {
        return this.goalTeam1;
    }

    public ArenaBoundingBox getGoalTeam2() {
        return this.goalTeam2;
    }

    public UUID getCurrentMatchId() {
        return this.currentMatchId;
    }

    public long getMatchStartedAt() {
        return this.matchStartedAt;
    }

    public int getTotalMatchesPlayed() {
        return this.totalMatchesPlayed;
    }

    public long getTotalPlaytime() {
        return this.totalPlaytime;
    }

    public long getStateChangedAt() {
        return this.stateChangedAt;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public void setType(ArenaType type) {
        this.type = type;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public void setSpawnPoints(ArenaSpawnPoints spawnPoints) {
        this.spawnPoints = spawnPoints;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public void setIconMaterial(Material material) {
        this.iconMaterial = material;
    }

    public void setIconGlow(boolean glow) {
        this.iconGlow = glow;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setWeight(int weight) {
        this.weight = Math.max(1, weight);
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = Math.max(1, maxTeamSize);
    }

    public void setBoundariesEnabled(boolean enabled) {
        this.boundariesEnabled = enabled;
    }

    public void setBoundaryMin(Location min) {
        this.boundaryMin = min;
    }

    public void setBoundaryMax(Location max) {
        this.boundaryMax = max;
    }

    public void setVoidDeathEnabled(boolean enabled) {
        this.voidDeathEnabled = enabled;
    }

    public void setVoidDeathYLevel(int yLevel) {
        this.voidDeathYLevel = yLevel;
    }

    public void setSchematicFile(String file) {
        this.schematicFile = file;
    }

    public void setSchematicOrigin(Location origin) {
        this.schematicOrigin = origin;
    }

    public void setSchematicIgnoreAir(boolean ignoreAir) {
        this.schematicIgnoreAir = ignoreAir;
    }

    public <T> T getSetting(String key, T defaultValue) {
        Object value = this.settings.get(key);
        return (T)(value != null ? value : defaultValue);
    }

    public void setSetting(String key, Object value) {
        this.settings.put(key, value);
    }

    public boolean isBuildingAllowed() {
        return this.getSetting("allow-building", false);
    }

    public boolean isNaturalRegeneration() {
        return this.getSetting("natural-regeneration", true);
    }

    public Location getSpawn1() {
        return this.spawnPoints != null ? this.spawnPoints.getSpawn1() : null;
    }

    public Location getSpawn2() {
        return this.spawnPoints != null ? this.spawnPoints.getSpawn2() : null;
    }

    public Location getSpectatorSpawn() {
        return this.spawnPoints != null ? this.spawnPoints.getSpectatorSpawn() : null;
    }

    public void setSpawn1(Location location) {
        if (this.spawnPoints == null) {
            this.spawnPoints = new ArenaSpawnPoints();
        }
        this.spawnPoints.setSpawn1(location);
    }

    public void setSpawn2(Location location) {
        if (this.spawnPoints == null) {
            this.spawnPoints = new ArenaSpawnPoints();
        }
        this.spawnPoints.setSpawn2(location);
    }

    public boolean isFullyConfigured() {
        return this.spawnPoints != null && this.spawnPoints.isValid() && this.worldName != null && !this.worldName.isEmpty();
    }

    public long getTimeSinceStateChange() {
        return System.currentTimeMillis() - this.stateChangedAt;
    }

    public long getAverageMatchDuration() {
        if (this.totalMatchesPlayed == 0) {
            return 0L;
        }
        return this.totalPlaytime / (long)this.totalMatchesPlayed;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Arena arena = (Arena)o;
        return Objects.equals(this.id, arena.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "Arena{id='" + this.id + "', displayName='" + this.displayName + "', type=" + String.valueOf((Object)this.type) + ", state=" + String.valueOf((Object)this.state) + ", enabled=" + this.enabled + "}";
    }

    public static class ArenaBoundingBox {
        private final Location min;
        private final Location max;

        public ArenaBoundingBox(Location min, Location max) {
            this.min = min;
            this.max = max;
        }

        public Location getMin() {
            return this.min;
        }

        public Location getMax() {
            return this.max;
        }

        public boolean contains(Location loc) {
            if (loc == null || this.min == null || this.max == null) {
                return false;
            }
            if (!loc.getWorld().equals((Object)this.min.getWorld())) {
                return false;
            }
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            return x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY() && z >= this.min.getZ() && z <= this.max.getZ();
        }

        public Location getCenter() {
            return new Location(this.min.getWorld(), (this.min.getX() + this.max.getX()) / 2.0, (this.min.getY() + this.max.getY()) / 2.0, (this.min.getZ() + this.max.getZ()) / 2.0);
        }
    }
}

