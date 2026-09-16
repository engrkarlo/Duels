/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.utils;

import com.ultimateduels.utils.ColorUtils;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerUtils {
    private PlayerUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void resetPlayer(@NotNull Player player) {
        PlayerUtils.clearInventory(player);
        PlayerUtils.resetHealth(player);
        PlayerUtils.resetFood(player);
        PlayerUtils.clearPotionEffects(player);
        player.setExp(0.0f);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setVelocity(player.getVelocity().zero());
        player.setArrowsInBody(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setExhaustion(0.0f);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.updateInventory();
    }

    public static void resetForRound(@NotNull Player player) {
        PlayerUtils.clearInventory(player);
        PlayerUtils.resetHealth(player);
        PlayerUtils.resetFood(player);
        PlayerUtils.clearNegativeEffects(player);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setVelocity(player.getVelocity().zero());
        player.setArrowsInBody(0);
        player.updateInventory();
    }

    public static void quickReset(@NotNull Player player) {
        PlayerUtils.resetHealth(player);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setVelocity(player.getVelocity().zero());
    }

    public static void resetHealth(@NotNull Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        } else {
            player.setHealth(20.0);
        }
        player.setAbsorptionAmount(0.0);
    }

    public static void setHealthSafe(@NotNull Player player, double health) {
        double maxHealth = PlayerUtils.getMaxHealth(player);
        player.setHealth(Math.min(Math.max(0.0, health), maxHealth));
    }

    public static double getMaxHealth(@NotNull Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return attribute != null ? attribute.getValue() : 20.0;
    }

    public static void heal(@NotNull Player player, double amount) {
        double newHealth = Math.min(player.getHealth() + amount, PlayerUtils.getMaxHealth(player));
        player.setHealth(newHealth);
    }

    public static double getHearts(@NotNull Player player) {
        return player.getHealth() / 2.0;
    }

    public static int getHealthPercent(@NotNull Player player) {
        double maxHealth = PlayerUtils.getMaxHealth(player);
        return (int)Math.round(player.getHealth() / maxHealth * 100.0);
    }

    @NotNull
    public static String formatHealth(@NotNull Player player) {
        return String.format("%.1f\u2764", player.getHealth());
    }

    public static void resetFood(@NotNull Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
    }

    public static void setFoodSafe(@NotNull Player player, int food) {
        player.setFoodLevel(Math.min(Math.max(0, food), 20));
    }

    public static void clearInventory(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(new ItemStack[4]);
        inventory.setItemInOffHand(null);
        player.updateInventory();
    }

    public static void clearMainInventory(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 36; ++i) {
            inventory.setItem(i, null);
        }
        player.updateInventory();
    }

    public static void clearArmor(@NotNull Player player) {
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.updateInventory();
    }

    public static void giveItems(@NotNull Player player, ItemStack ... items) {
        for (ItemStack item : items) {
            if (item == null) continue;
            player.getInventory().addItem(new ItemStack[]{item}).values().forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));
        }
        player.updateInventory();
    }

    public static boolean hasInventorySpace(@NotNull Player player) {
        return player.getInventory().firstEmpty() != -1;
    }

    public static boolean hasInventorySpace(@NotNull Player player, int slots) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && !item.getType().isAir() || ++emptySlots < slots) continue;
            return true;
        }
        return false;
    }

    public static int countEmptySlots(@NotNull Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && !item.getType().isAir()) continue;
            ++count;
        }
        return count;
    }

    public static void clearPotionEffects(@NotNull Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public static void clearNegativeEffects(@NotNull Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (!PlayerUtils.isNegativeEffect(type)) continue;
            player.removePotionEffect(type);
        }
    }

    private static boolean isNegativeEffect(@NotNull PotionEffectType type) {
        return type.equals(PotionEffectType.POISON) || type.equals(PotionEffectType.WITHER) || type.equals(PotionEffectType.SLOWNESS) || type.equals(PotionEffectType.MINING_FATIGUE) || type.equals(PotionEffectType.INSTANT_DAMAGE) || type.equals(PotionEffectType.NAUSEA) || type.equals(PotionEffectType.BLINDNESS) || type.equals(PotionEffectType.HUNGER) || type.equals(PotionEffectType.WEAKNESS) || type.equals(PotionEffectType.LEVITATION) || type.equals(PotionEffectType.UNLUCK) || type.equals(PotionEffectType.BAD_OMEN) || type.equals(PotionEffectType.DARKNESS) || type.equals(PotionEffectType.INFESTED) || type.equals(PotionEffectType.OOZING) || type.equals(PotionEffectType.WEAVING) || type.equals(PotionEffectType.WIND_CHARGED);
    }

    public static void applyEffect(@NotNull Player player, @NotNull PotionEffectType type, int durationTicks, int amplifier, boolean ambient, boolean particles, boolean icon) {
        player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, ambient, particles, icon));
    }

    public static void applyEffect(@NotNull Player player, @NotNull PotionEffectType type, int durationTicks, int amplifier) {
        PlayerUtils.applyEffect(player, type, durationTicks, amplifier, true, false, true);
    }

    public static void applyPermanentEffect(@NotNull Player player, @NotNull PotionEffectType type, int amplifier) {
        PlayerUtils.applyEffect(player, type, -1, amplifier, true, false, false);
    }

    public static void teleport(@NotNull Player player, @NotNull Location location) {
        location.getChunk().load();
        player.teleportAsync(location);
    }

    public static void teleport(@NotNull Player player, @NotNull Location location, @NotNull Consumer<Boolean> callback) {
        location.getChunk().load();
        player.teleportAsync(location).thenAccept(callback);
    }

    public static void teleportCentered(@NotNull Player player, @NotNull Location location) {
        Location centered = location.clone();
        centered.setX((double)location.getBlockX() + 0.5);
        centered.setZ((double)location.getBlockZ() + 0.5);
        PlayerUtils.teleport(player, centered);
    }

    public static void teleportSafe(@NotNull Player player, @NotNull Location location) {
        Location safe = PlayerUtils.findSafeLocation(location);
        PlayerUtils.teleport(player, safe != null ? safe : location);
    }

    @Nullable
    public static Location findSafeLocation(@NotNull Location location) {
        Location check = location.clone();
        if (PlayerUtils.isSafeLocation(check)) {
            return check;
        }
        for (int y = 0; y < 5; ++y) {
            check.add(0.0, 1.0, 0.0);
            if (!PlayerUtils.isSafeLocation(check)) continue;
            return check;
        }
        check = location.clone();
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z) {
                for (int y = -2; y <= 2; ++y) {
                    Location test = check.clone().add((double)x, (double)y, (double)z);
                    if (!PlayerUtils.isSafeLocation(test)) continue;
                    return test;
                }
            }
        }
        return null;
    }

    public static boolean isSafeLocation(@NotNull Location location) {
        return location.getBlock().isPassable() && location.clone().add(0.0, 1.0, 0.0).getBlock().isPassable() && !location.clone().subtract(0.0, 1.0, 0.0).getBlock().isPassable();
    }

    @NotNull
    public static Optional<Player> getPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(Bukkit.getPlayer((UUID)uuid));
    }

    @NotNull
    public static Optional<Player> getPlayer(@NotNull String name) {
        return Optional.ofNullable(Bukkit.getPlayerExact((String)name));
    }

    public static boolean isOnline(@NotNull UUID uuid) {
        return Bukkit.getPlayer((UUID)uuid) != null;
    }

    @NotNull
    public static Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    public static void ifOnline(@NotNull UUID uuid, @NotNull Consumer<Player> action) {
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player != null) {
            action.accept(player);
        }
    }

    public static void setSpectator(@NotNull Player player) {
        player.setGameMode(GameMode.SPECTATOR);
    }

    public static void setSurvival(@NotNull Player player) {
        player.setGameMode(GameMode.SURVIVAL);
    }

    public static boolean isSpectator(@NotNull Player player) {
        return player.getGameMode() == GameMode.SPECTATOR;
    }

    public static void setSpectatorTarget(@NotNull Player spectator, @NotNull Player target) {
        if (spectator.getGameMode() == GameMode.SPECTATOR) {
            spectator.setSpectatorTarget((Entity)target);
        }
    }

    public static void hidePlayer(@NotNull Player player, @NotNull Player toHide, @NotNull Plugin plugin) {
        player.hidePlayer(plugin, toHide);
    }

    public static void showPlayer(@NotNull Player player, @NotNull Player toShow, @NotNull Plugin plugin) {
        player.showPlayer(plugin, toShow);
    }

    public static void hideFromAll(@NotNull Player player, @NotNull Plugin plugin) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals((Object)player)) continue;
            online.hidePlayer(plugin, player);
        }
    }

    public static void showToAll(@NotNull Player player, @NotNull Plugin plugin) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals((Object)player)) continue;
            online.showPlayer(plugin, player);
        }
    }

    public static int getPing(@NotNull Player player) {
        return player.getPing();
    }

    @NotNull
    public static String formatPing(@NotNull Player player) {
        int ping = player.getPing();
        String color = ping < 50 ? "<green>" : (ping < 100 ? "<yellow>" : (ping < 200 ? "<gold>" : "<red>"));
        return color + ping + "ms";
    }

    @Nullable
    public static String getClientBrand(@NotNull Player player) {
        return player.getClientBrandName();
    }

    public static void sendMessage(@NotNull Player player, @NotNull Component message) {
        player.sendMessage(message);
    }

    public static void sendMessage(@NotNull Player player, @NotNull String message) {
        player.sendMessage(ColorUtils.parse(message));
    }

    public static void sendPrefixedMessage(@NotNull Player player, @NotNull String message) {
        player.sendMessage(ColorUtils.prefixed(message));
    }

    public static void sendActionBar(@NotNull Player player, @NotNull Component message) {
        player.sendActionBar(message);
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String message) {
        player.sendActionBar(ColorUtils.parse(message));
    }

    public static void broadcast(@NotNull Component message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public static void broadcast(@NotNull Component message, @NotNull Collection<UUID> playerUuids) {
        for (UUID uuid : playerUuids) {
            PlayerUtils.ifOnline(uuid, player -> player.sendMessage(message));
        }
    }
}

