/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 */
package com.ultimateduels.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class LocationSerializer {
    private static final Logger LOGGER = Logger.getLogger("UltimateDuels-LocationSerializer");
    private static final String DELIMITER = ":";
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[^:]+:-?\\d+(\\.\\d+)?:-?\\d+(\\.\\d+)?:-?\\d+(\\.\\d+)?(:-?\\d+(\\.\\d+)?:-?\\d+(\\.\\d+)?)?$");

    private LocationSerializer() {
    }

    public static String toString(Location location) {
        if (location == null) {
            return null;
        }
        if (location.getWorld() == null) {
            return null;
        }
        return String.format("%s%s%.4f%s%.4f%s%.4f%s%.2f%s%.2f", location.getWorld().getName(), DELIMITER, location.getX(), DELIMITER, location.getY(), DELIMITER, location.getZ(), DELIMITER, Float.valueOf(location.getYaw()), DELIMITER, Float.valueOf(location.getPitch()));
    }

    public static String toStringSimple(Location location) {
        if (location == null) {
            return null;
        }
        if (location.getWorld() == null) {
            return null;
        }
        return String.format("%s%s%.4f%s%.4f%s%.4f", location.getWorld().getName(), DELIMITER, location.getX(), DELIMITER, location.getY(), DELIMITER, location.getZ());
    }

    public static String toBlockString(Location location) {
        if (location == null) {
            return null;
        }
        if (location.getWorld() == null) {
            return null;
        }
        return String.format("%s%s%d%s%d%s%d", location.getWorld().getName(), DELIMITER, location.getBlockX(), DELIMITER, location.getBlockY(), DELIMITER, location.getBlockZ());
    }

    public static Location fromString(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        String[] parts = string.split(DELIMITER);
        if (parts.length < 4) {
            return null;
        }
        try {
            World world = Bukkit.getWorld((String)parts[0]);
            if (world == null) {
                LOGGER.warning("World not found: " + parts[0]);
                return null;
            }
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = 0.0f;
            float pitch = 0.0f;
            if (parts.length >= 6) {
                yaw = Float.parseFloat(parts[4]);
                pitch = Float.parseFloat(parts[5]);
            }
            return new Location(world, x, y, z, yaw, pitch);
        }
        catch (NumberFormatException e) {
            LOGGER.warning("Invalid location format: " + string);
            return null;
        }
    }

    public static Optional<Location> fromStringSafe(String string) {
        return Optional.ofNullable(LocationSerializer.fromString(string));
    }

    public static boolean isValidFormat(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        return LOCATION_PATTERN.matcher(string).matches();
    }

    public static Map<String, Object> toMap(Location location) {
        if (location == null) {
            return null;
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        if (location.getWorld() != null) {
            map.put("world", location.getWorld().getName());
        }
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", Float.valueOf(location.getYaw()));
        map.put("pitch", Float.valueOf(location.getPitch()));
        return map;
    }

    public static Map<String, Object> toBlockMap(Location location) {
        if (location == null) {
            return null;
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        if (location.getWorld() != null) {
            map.put("world", location.getWorld().getName());
        }
        map.put("x", location.getBlockX());
        map.put("y", location.getBlockY());
        map.put("z", location.getBlockZ());
        return map;
    }

    public static Location fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            String worldName = (String)map.get("world");
            World world = worldName != null ? Bukkit.getWorld((String)worldName) : null;
            double x = ((Number)map.get("x")).doubleValue();
            double y = ((Number)map.get("y")).doubleValue();
            double z = ((Number)map.get("z")).doubleValue();
            float yaw = map.containsKey("yaw") ? ((Number)map.get("yaw")).floatValue() : 0.0f;
            float pitch = map.containsKey("pitch") ? ((Number)map.get("pitch")).floatValue() : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        }
        catch (Exception e) {
            LOGGER.warning("Invalid location map: " + e.getMessage());
            return null;
        }
    }

    public static void saveToConfig(ConfigurationSection section, Location location) {
        if (section == null || location == null) {
            return;
        }
        if (location.getWorld() != null) {
            section.set("world", (Object)location.getWorld().getName());
        }
        section.set("x", (Object)location.getX());
        section.set("y", (Object)location.getY());
        section.set("z", (Object)location.getZ());
        section.set("yaw", (Object)Float.valueOf(location.getYaw()));
        section.set("pitch", (Object)Float.valueOf(location.getPitch()));
    }

    public static void saveToConfig(ConfigurationSection section, String path, Location location) {
        if (section == null) {
            return;
        }
        ConfigurationSection locSection = section.createSection(path);
        LocationSerializer.saveToConfig(locSection, location);
    }

    public static Location loadFromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        try {
            String worldName = section.getString("world");
            World world = worldName != null ? Bukkit.getWorld((String)worldName) : null;
            double x = section.getDouble("x", 0.0);
            double y = section.getDouble("y", 64.0);
            double z = section.getDouble("z", 0.0);
            float yaw = (float)section.getDouble("yaw", 0.0);
            float pitch = (float)section.getDouble("pitch", 0.0);
            return new Location(world, x, y, z, yaw, pitch);
        }
        catch (Exception e) {
            LOGGER.warning("Failed to load location from config: " + e.getMessage());
            return null;
        }
    }

    public static Location loadFromConfig(ConfigurationSection section, String path) {
        if (section == null) {
            return null;
        }
        return LocationSerializer.loadFromConfig(section.getConfigurationSection(path));
    }

    public static Location loadFromConfig(ConfigurationSection section, World defaultWorld) {
        Location location = LocationSerializer.loadFromConfig(section);
        if (location != null && location.getWorld() == null) {
            location.setWorld(defaultWorld);
        }
        return location;
    }

    public static String toDatabaseString(Location location) {
        if (location == null) {
            return null;
        }
        if (location.getWorld() == null) {
            return null;
        }
        return String.format("%s;%.6f;%.6f;%.6f;%.4f;%.4f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), Float.valueOf(location.getYaw()), Float.valueOf(location.getPitch()));
    }

    public static Location fromDatabaseString(String dbString) {
        if (dbString == null || dbString.isEmpty()) {
            return null;
        }
        String[] parts = dbString.split(";");
        if (parts.length < 4) {
            return null;
        }
        try {
            World world = Bukkit.getWorld((String)parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length >= 6 ? Float.parseFloat(parts[5]) : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        }
        catch (NumberFormatException e) {
            LOGGER.warning("Invalid database location format: " + dbString);
            return null;
        }
    }

    public static Location center(Location location) {
        if (location == null) {
            return null;
        }
        return new Location(location.getWorld(), (double)location.getBlockX() + 0.5, location.getY(), (double)location.getBlockZ() + 0.5, location.getYaw(), location.getPitch());
    }

    public static Location toBlock(Location location) {
        if (location == null) {
            return null;
        }
        return new Location(location.getWorld(), (double)location.getBlockX(), (double)location.getBlockY(), (double)location.getBlockZ());
    }

    public static double distance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return -1.0;
        }
        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return -1.0;
        }
        if (!loc1.getWorld().equals((Object)loc2.getWorld())) {
            return -1.0;
        }
        return loc1.distance(loc2);
    }

    public static double distanceSquared(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return -1.0;
        }
        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return -1.0;
        }
        if (!loc1.getWorld().equals((Object)loc2.getWorld())) {
            return -1.0;
        }
        return loc1.distanceSquared(loc2);
    }

    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return false;
        }
        if (loc1.getWorld() == null || loc2.getWorld() == null) {
            return false;
        }
        if (!loc1.getWorld().equals((Object)loc2.getWorld())) {
            return false;
        }
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    public static boolean isWithinDistance(Location loc1, Location loc2, double distance) {
        double actualDistance = LocationSerializer.distance(loc1, loc2);
        return actualDistance >= 0.0 && actualDistance <= distance;
    }

    public static Location midpoint(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return null;
        }
        if (loc1.getWorld() == null || !loc1.getWorld().equals((Object)loc2.getWorld())) {
            return null;
        }
        return new Location(loc1.getWorld(), (loc1.getX() + loc2.getX()) / 2.0, (loc1.getY() + loc2.getY()) / 2.0, (loc1.getZ() + loc2.getZ()) / 2.0);
    }

    public static Location cloneSafe(Location location) {
        return location != null ? location.clone() : null;
    }

    public static boolean isValid(Location location) {
        if (location == null) {
            return false;
        }
        if (location.getWorld() == null) {
            return false;
        }
        return Double.isFinite(location.getX()) && Double.isFinite(location.getY()) && Double.isFinite(location.getZ());
    }

    public static String toDisplayString(Location location) {
        if (location == null) {
            return "null";
        }
        String world = location.getWorld() != null ? location.getWorld().getName() : "?";
        return String.format("%s @ %.1f, %.1f, %.1f", world, location.getX(), location.getY(), location.getZ());
    }

    public static String toDetailedDisplayString(Location location) {
        if (location == null) {
            return "null";
        }
        String world = location.getWorld() != null ? location.getWorld().getName() : "?";
        return String.format("%s @ X: %.2f, Y: %.2f, Z: %.2f (Yaw: %.1f, Pitch: %.1f)", world, location.getX(), location.getY(), location.getZ(), Float.valueOf(location.getYaw()), Float.valueOf(location.getPitch()));
    }

    public static List<String> toStringList(List<Location> locations) {
        if (locations == null) {
            return Collections.emptyList();
        }
        ArrayList<String> strings = new ArrayList<String>();
        for (Location location : locations) {
            String str = LocationSerializer.toString(location);
            if (str == null) continue;
            strings.add(str);
        }
        return strings;
    }

    public static List<Location> fromStringList(List<String> strings) {
        if (strings == null) {
            return Collections.emptyList();
        }
        ArrayList<Location> locations = new ArrayList<Location>();
        for (String string : strings) {
            Location location = LocationSerializer.fromString(string);
            if (location == null) continue;
            locations.add(location);
        }
        return locations;
    }
}

