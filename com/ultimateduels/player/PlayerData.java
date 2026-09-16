/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PlayerData {
    private final UUID uuid;
    private int elo;
    private final Map<String, Integer> kitWins;
    private final Map<String, Integer> kitLosses;
    private final Map<String, Integer> kitKills;
    private final Map<String, Integer> kitDeaths;

    public PlayerData(@Nonnull UUID uuid) {
        this.uuid = uuid;
        this.elo = 1000;
        this.kitWins = new HashMap<String, Integer>();
        this.kitLosses = new HashMap<String, Integer>();
        this.kitKills = new HashMap<String, Integer>();
        this.kitDeaths = new HashMap<String, Integer>();
    }

    public int getElo() {
        return this.elo;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
    }

    public void addElo(int amount) {
        this.elo = Math.max(0, this.elo + amount);
    }

    public void removeElo(int amount) {
        this.elo = Math.max(0, this.elo - amount);
    }

    @Nonnull
    public UUID getUuid() {
        return this.uuid;
    }

    public void addWin(@Nonnull String kitName) {
        String key = kitName.toLowerCase();
        this.kitWins.put(key, this.kitWins.getOrDefault(key, 0) + 1);
    }

    public int getKitWins(@Nonnull String kitName) {
        return this.kitWins.getOrDefault(kitName.toLowerCase(), 0);
    }

    public int getTotalWins() {
        return this.kitWins.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addLoss(@Nonnull String kitName) {
        String key = kitName.toLowerCase();
        this.kitLosses.put(key, this.kitLosses.getOrDefault(key, 0) + 1);
    }

    public int getKitLosses(@Nonnull String kitName) {
        return this.kitLosses.getOrDefault(kitName.toLowerCase(), 0);
    }

    public int getTotalLosses() {
        return this.kitLosses.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addKills(@Nonnull String kitName, int kills) {
        String key = kitName.toLowerCase();
        this.kitKills.put(key, this.kitKills.getOrDefault(key, 0) + kills);
    }

    public int getKitKills(@Nonnull String kitName) {
        return this.kitKills.getOrDefault(kitName.toLowerCase(), 0);
    }

    public int getTotalKills() {
        return this.kitKills.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addDeaths(@Nonnull String kitName, int deaths) {
        String key = kitName.toLowerCase();
        this.kitDeaths.put(key, this.kitDeaths.getOrDefault(key, 0) + deaths);
    }

    public int getKitDeaths(@Nonnull String kitName) {
        return this.kitDeaths.getOrDefault(kitName.toLowerCase(), 0);
    }

    public int getTotalDeaths() {
        return this.kitDeaths.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getKitKDR(@Nonnull String kitName) {
        int kills = this.getKitKills(kitName);
        int deaths = this.getKitDeaths(kitName);
        return deaths == 0 ? (double)kills : (double)kills / (double)deaths;
    }

    public double getTotalKDR() {
        int kills = this.getTotalKills();
        int deaths = this.getTotalDeaths();
        return deaths == 0 ? (double)kills : (double)kills / (double)deaths;
    }

    public double getKitWinRate(@Nonnull String kitName) {
        int losses;
        int wins = this.getKitWins(kitName);
        int total = wins + (losses = this.getKitLosses(kitName));
        return total == 0 ? 0.0 : (double)wins / (double)total;
    }

    public double getTotalWinRate() {
        int losses;
        int wins = this.getTotalWins();
        int total = wins + (losses = this.getTotalLosses());
        return total == 0 ? 0.0 : (double)wins / (double)total;
    }

    @Nonnull
    public Map<String, Integer> getKitWinsMap() {
        return new HashMap<String, Integer>(this.kitWins);
    }

    @Nonnull
    public Map<String, Integer> getKitLossesMap() {
        return new HashMap<String, Integer>(this.kitLosses);
    }

    @Nonnull
    public Map<String, Integer> getKitKillsMap() {
        return new HashMap<String, Integer>(this.kitKills);
    }

    @Nonnull
    public Map<String, Integer> getKitDeathsMap() {
        return new HashMap<String, Integer>(this.kitDeaths);
    }

    public void loadKitWins(@Nonnull Map<String, Integer> wins) {
        this.kitWins.clear();
        this.kitWins.putAll(wins);
    }

    public void loadKitLosses(@Nonnull Map<String, Integer> losses) {
        this.kitLosses.clear();
        this.kitLosses.putAll(losses);
    }

    public void loadKitKills(@Nonnull Map<String, Integer> kills) {
        this.kitKills.clear();
        this.kitKills.putAll(kills);
    }

    public void loadKitDeaths(@Nonnull Map<String, Integer> deaths) {
        this.kitDeaths.clear();
        this.kitDeaths.putAll(deaths);
    }
}

