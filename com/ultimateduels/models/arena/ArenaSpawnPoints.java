/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.util.Vector
 */
package com.ultimateduels.models.arena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class ArenaSpawnPoints {
    private Location spawn1;
    private Location spawn2;
    private Location spectatorSpawn;
    private List<Location> team1Spawns = new ArrayList<Location>();
    private List<Location> team2Spawns = new ArrayList<Location>();
    private List<Location> ffaSpawns = new ArrayList<Location>();
    private Location waitingRoomSpawn;
    private float defaultYaw = 0.0f;
    private float defaultPitch = 0.0f;
    private Location centerPoint;
    private boolean centerManuallySet = false;

    public ArenaSpawnPoints() {
    }

    public ArenaSpawnPoints(Location spawn1, Location spawn2) {
        this();
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.calculateCenter();
    }

    public static ArenaSpawnPoints fromConfig(ConfigurationSection section, World world) {
        ConfigurationSection ffaSection;
        ConfigurationSection team2Section;
        ConfigurationSection team1Section;
        ConfigurationSection waitSection;
        ConfigurationSection specSection;
        ConfigurationSection spawn2Section;
        ArenaSpawnPoints spawns = new ArenaSpawnPoints();
        if (section == null || world == null) {
            return spawns;
        }
        ConfigurationSection spawn1Section = section.getConfigurationSection("spawn1");
        if (spawn1Section != null) {
            spawns.spawn1 = ArenaSpawnPoints.loadLocation(spawn1Section, world);
        }
        if ((spawn2Section = section.getConfigurationSection("spawn2")) != null) {
            spawns.spawn2 = ArenaSpawnPoints.loadLocation(spawn2Section, world);
        }
        if ((specSection = section.getConfigurationSection("spectator-spawn")) != null) {
            spawns.spectatorSpawn = ArenaSpawnPoints.loadLocation(specSection, world);
        }
        if ((waitSection = section.getConfigurationSection("waiting-room")) != null) {
            spawns.waitingRoomSpawn = ArenaSpawnPoints.loadLocation(waitSection, world);
        }
        if ((team1Section = section.getConfigurationSection("team1-spawns")) != null) {
            spawns.team1Spawns = ArenaSpawnPoints.loadLocationList(team1Section, world);
        }
        if ((team2Section = section.getConfigurationSection("team2-spawns")) != null) {
            spawns.team2Spawns = ArenaSpawnPoints.loadLocationList(team2Section, world);
        }
        if ((ffaSection = section.getConfigurationSection("spawns")) != null) {
            spawns.ffaSpawns = ArenaSpawnPoints.loadLocationList(ffaSection, world);
        }
        if (!spawns.centerManuallySet) {
            spawns.calculateCenter();
        }
        return spawns;
    }

    private static Location loadLocation(ConfigurationSection section, World world) {
        if (section == null) {
            return null;
        }
        double x = section.getDouble("x", 0.0);
        double y = section.getDouble("y", 64.0);
        double z = section.getDouble("z", 0.0);
        float yaw = (float)section.getDouble("yaw", 0.0);
        float pitch = (float)section.getDouble("pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static List<Location> loadLocationList(ConfigurationSection section, World world) {
        ArrayList<Location> locations = new ArrayList<Location>();
        if (section == null) {
            return locations;
        }
        for (String key : section.getKeys(false)) {
            Location loc;
            ConfigurationSection locSection = section.getConfigurationSection(key);
            if (locSection == null || (loc = ArenaSpawnPoints.loadLocation(locSection, world)) == null) continue;
            locations.add(loc);
        }
        return locations;
    }

    public Location getSpawn1() {
        return this.spawn1;
    }

    public void setSpawn1(Location location) {
        this.spawn1 = location;
        this.calculateCenter();
    }

    public Location getSpawn2() {
        return this.spawn2;
    }

    public void setSpawn2(Location location) {
        this.spawn2 = location;
        this.calculateCenter();
    }

    public Location getSpectatorSpawn() {
        return this.spectatorSpawn;
    }

    public void setSpectatorSpawn(Location location) {
        this.spectatorSpawn = location;
    }

    public Location getWaitingRoomSpawn() {
        return this.waitingRoomSpawn;
    }

    public void setWaitingRoomSpawn(Location location) {
        this.waitingRoomSpawn = location;
    }

    public List<Location> getTeam1Spawns() {
        return this.team1Spawns;
    }

    public List<Location> getTeam2Spawns() {
        return this.team2Spawns;
    }

    public void addTeam1Spawn(Location location) {
        this.team1Spawns.add(location);
    }

    public void addTeam2Spawn(Location location) {
        this.team2Spawns.add(location);
    }

    public void clearTeam1Spawns() {
        this.team1Spawns.clear();
    }

    public void clearTeam2Spawns() {
        this.team2Spawns.clear();
    }

    public Location getTeam1Spawn(int index) {
        if (!this.team1Spawns.isEmpty()) {
            return this.team1Spawns.get(index % this.team1Spawns.size());
        }
        return this.spawn1;
    }

    public Location getTeam2Spawn(int index) {
        if (!this.team2Spawns.isEmpty()) {
            return this.team2Spawns.get(index % this.team2Spawns.size());
        }
        return this.spawn2;
    }

    public List<Location> getFfaSpawns() {
        return this.ffaSpawns;
    }

    public void addFfaSpawn(Location location) {
        this.ffaSpawns.add(location);
        this.calculateCenter();
    }

    public Location removeFfaSpawn(int index) {
        if (index >= 0 && index < this.ffaSpawns.size()) {
            Location removed = this.ffaSpawns.remove(index);
            this.calculateCenter();
            return removed;
        }
        return null;
    }

    public void clearFfaSpawns() {
        this.ffaSpawns.clear();
    }

    public Location getRandomFfaSpawn() {
        if (this.ffaSpawns.isEmpty()) {
            return this.spawn1 != null ? this.spawn1 : this.spawn2;
        }
        return this.ffaSpawns.get(new Random().nextInt(this.ffaSpawns.size()));
    }

    public Location getRandomFfaSpawn(List<Location> avoidLocations, double minDistance) {
        if (this.ffaSpawns.isEmpty()) {
            return this.getRandomFfaSpawn();
        }
        if (avoidLocations == null || avoidLocations.isEmpty()) {
            return this.getRandomFfaSpawn();
        }
        ArrayList<Location> shuffled = new ArrayList<Location>(this.ffaSpawns);
        Collections.shuffle(shuffled);
        Location bestSpawn = (Location)shuffled.get(0);
        double bestMinDistance = 0.0;
        for (Location spawn : shuffled) {
            double minDist = Double.MAX_VALUE;
            for (Location avoid : avoidLocations) {
                double dist;
                if (!avoid.getWorld().equals((Object)spawn.getWorld()) || !((dist = spawn.distance(avoid)) < minDist)) continue;
                minDist = dist;
            }
            if (!(minDist > bestMinDistance)) continue;
            bestMinDistance = minDist;
            bestSpawn = spawn;
            if (!(minDist >= minDistance)) continue;
            return spawn;
        }
        return bestSpawn;
    }

    public int getFfaSpawnCount() {
        return this.ffaSpawns.size();
    }

    public Location getCenterPoint() {
        return this.centerPoint;
    }

    public void setCenterPoint(Location center) {
        this.centerPoint = center;
        this.centerManuallySet = true;
    }

    public void calculateCenter() {
        if (this.centerManuallySet) {
            return;
        }
        List<Location> allSpawns = this.getAllSpawns();
        if (allSpawns.isEmpty()) {
            this.centerPoint = null;
            return;
        }
        World world = allSpawns.get(0).getWorld();
        double totalX = 0.0;
        double totalY = 0.0;
        double totalZ = 0.0;
        for (Location loc : allSpawns) {
            totalX += loc.getX();
            totalY += loc.getY();
            totalZ += loc.getZ();
        }
        int count = allSpawns.size();
        this.centerPoint = new Location(world, totalX / (double)count, totalY / (double)count, totalZ / (double)count);
    }

    public List<Location> getAllSpawns() {
        ArrayList<Location> all = new ArrayList<Location>();
        if (this.spawn1 != null) {
            all.add(this.spawn1);
        }
        if (this.spawn2 != null) {
            all.add(this.spawn2);
        }
        all.addAll(this.team1Spawns);
        all.addAll(this.team2Spawns);
        all.addAll(this.ffaSpawns);
        return all;
    }

    public boolean isValid() {
        return this.spawn1 != null && this.spawn2 != null;
    }

    public boolean isValidForFFA(int minSpawns) {
        return this.ffaSpawns.size() >= minSpawns;
    }

    public boolean isValidForTeams(int teamSize) {
        boolean team1Valid = this.team1Spawns.size() >= teamSize || this.spawn1 != null;
        boolean team2Valid = this.team2Spawns.size() >= teamSize || this.spawn2 != null;
        return team1Valid && team2Valid;
    }

    public boolean hasSpectatorSpawn() {
        return this.spectatorSpawn != null;
    }

    public boolean isInSameWorld() {
        List<Location> all = this.getAllSpawns();
        if (all.isEmpty()) {
            return true;
        }
        World world = all.get(0).getWorld();
        for (Location loc : all) {
            if (loc.getWorld().equals((Object)world)) continue;
            return false;
        }
        return true;
    }

    public World getWorld() {
        if (this.spawn1 != null) {
            return this.spawn1.getWorld();
        }
        if (this.spawn2 != null) {
            return this.spawn2.getWorld();
        }
        if (!this.ffaSpawns.isEmpty()) {
            return this.ffaSpawns.get(0).getWorld();
        }
        return null;
    }

    public double getSpawnDistance() {
        if (this.spawn1 == null || this.spawn2 == null) {
            return -1.0;
        }
        if (!this.spawn1.getWorld().equals((Object)this.spawn2.getWorld())) {
            return -1.0;
        }
        return this.spawn1.distance(this.spawn2);
    }

    public double getMaxDimension() {
        List<Location> all = this.getAllSpawns();
        if (all.size() < 2) {
            return 0.0;
        }
        double maxDist = 0.0;
        for (int i = 0; i < all.size(); ++i) {
            for (int j = i + 1; j < all.size(); ++j) {
                double dist;
                Location loc1 = all.get(i);
                Location loc2 = all.get(j);
                if (!loc1.getWorld().equals((Object)loc2.getWorld()) || !((dist = loc1.distance(loc2)) > maxDist)) continue;
                maxDist = dist;
            }
        }
        return maxDist;
    }

    public void orientSpawnsToFaceEachOther() {
        if (this.spawn1 == null || this.spawn2 == null) {
            return;
        }
        if (!this.spawn1.getWorld().equals((Object)this.spawn2.getWorld())) {
            return;
        }
        Vector direction = this.spawn2.toVector().subtract(this.spawn1.toVector());
        Location oriented1 = this.spawn1.clone();
        oriented1.setDirection(direction);
        this.spawn1 = oriented1;
        Location oriented2 = this.spawn2.clone();
        oriented2.setDirection(direction.multiply(-1));
        this.spawn2 = oriented2;
    }

    public void orientSpawnsToFaceCenter() {
        if (this.centerPoint == null) {
            this.calculateCenter();
        }
        if (this.centerPoint == null) {
            return;
        }
        for (int i = 0; i < this.ffaSpawns.size(); ++i) {
            Location spawn = this.ffaSpawns.get(i);
            if (!spawn.getWorld().equals((Object)this.centerPoint.getWorld())) continue;
            Vector direction = this.centerPoint.toVector().subtract(spawn.toVector());
            Location oriented = spawn.clone();
            oriented.setDirection(direction);
            this.ffaSpawns.set(i, oriented);
        }
    }

    public static Map<String, Object> serializeLocation(Location location) {
        if (location == null) {
            return null;
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", Float.valueOf(location.getYaw()));
        map.put("pitch", Float.valueOf(location.getPitch()));
        return map;
    }

    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        if (this.spawn1 != null) {
            map.put("spawn1", ArenaSpawnPoints.serializeLocation(this.spawn1));
        }
        if (this.spawn2 != null) {
            map.put("spawn2", ArenaSpawnPoints.serializeLocation(this.spawn2));
        }
        if (this.spectatorSpawn != null) {
            map.put("spectator-spawn", ArenaSpawnPoints.serializeLocation(this.spectatorSpawn));
        }
        if (!this.ffaSpawns.isEmpty()) {
            LinkedHashMap<String, Map<String, Object>> ffaMap = new LinkedHashMap<String, Map<String, Object>>();
            for (int i = 0; i < this.ffaSpawns.size(); ++i) {
                ffaMap.put(String.valueOf(i + 1), ArenaSpawnPoints.serializeLocation(this.ffaSpawns.get(i)));
            }
            map.put("spawns", ffaMap);
        }
        return map;
    }

    public ArenaSpawnPoints copy() {
        ArenaSpawnPoints copy = new ArenaSpawnPoints();
        copy.spawn1 = this.spawn1 != null ? this.spawn1.clone() : null;
        copy.spawn2 = this.spawn2 != null ? this.spawn2.clone() : null;
        copy.spectatorSpawn = this.spectatorSpawn != null ? this.spectatorSpawn.clone() : null;
        copy.waitingRoomSpawn = this.waitingRoomSpawn != null ? this.waitingRoomSpawn.clone() : null;
        copy.centerPoint = this.centerPoint != null ? this.centerPoint.clone() : null;
        copy.centerManuallySet = this.centerManuallySet;
        for (Location loc : this.team1Spawns) {
            copy.team1Spawns.add(loc.clone());
        }
        for (Location loc : this.team2Spawns) {
            copy.team2Spawns.add(loc.clone());
        }
        for (Location loc : this.ffaSpawns) {
            copy.ffaSpawns.add(loc.clone());
        }
        return copy;
    }

    public String toString() {
        return "ArenaSpawnPoints{spawn1=" + (this.spawn1 != null) + ", spawn2=" + (this.spawn2 != null) + ", spectator=" + (this.spectatorSpawn != null) + ", team1Spawns=" + this.team1Spawns.size() + ", team2Spawns=" + this.team2Spawns.size() + ", ffaSpawns=" + this.ffaSpawns.size() + "}";
    }
}

