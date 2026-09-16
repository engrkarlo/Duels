/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 */
package com.ultimateduels.spectator;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class SpectatorState {
    private Location location;
    private ItemStack[] inventory;
    private ItemStack[] armor;
    private ItemStack offhand;
    private GameMode gameMode;
    private boolean flying;
    private boolean allowFlight;
    private double health;
    private int foodLevel;
    private float exp;
    private int level;
    private List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemStack[] getInventory() {
        return this.inventory;
    }

    public void setInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }

    public ItemStack[] getArmor() {
        return this.armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public ItemStack getOffhand() {
        return this.offhand;
    }

    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public boolean isAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

    public double getHealth() {
        return this.health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public float getExp() {
        return this.exp;
    }

    public void setExp(float exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }
}

