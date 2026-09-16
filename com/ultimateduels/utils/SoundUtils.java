/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.SoundCategory
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.utils;

import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SoundUtils {
    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;
    private static final SoundCategory DEFAULT_CATEGORY = SoundCategory.MASTER;

    private SoundUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void playSound(@NotNull Player player, @NotNull Sound sound) {
        SoundUtils.playSound(player, sound, 1.0f, 1.0f);
    }

    public static void playSound(@NotNull Player player, @NotNull Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, DEFAULT_CATEGORY, volume, pitch);
    }

    public static void playSound(@NotNull Player player, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, category, volume, pitch);
    }

    public static void playSound(@NotNull Location location, @NotNull Sound sound) {
        SoundUtils.playSound(location, sound, 1.0f, 1.0f);
    }

    public static void playSound(@NotNull Location location, @NotNull Sound sound, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, DEFAULT_CATEGORY, volume, pitch);
        }
    }

    public static void playSound(@NotNull Collection<Player> players, @NotNull Sound sound) {
        for (Player player : players) {
            SoundUtils.playSound(player, sound);
        }
    }

    public static void playSoundToUuids(@NotNull Collection<UUID> uuids, @NotNull Sound sound, float volume, float pitch) {
        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            SoundUtils.playSound(player, sound, volume, pitch);
        }
    }

    public static void broadcastSound(@NotNull Sound sound) {
        SoundUtils.broadcastSound(sound, 1.0f, 1.0f);
    }

    public static void broadcastSound(@NotNull Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SoundUtils.playSound(player, sound, volume, pitch);
        }
    }

    public static void playCountdownTick(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
    }

    public static void playCountdownGo(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    public static void playMatchStart(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
    }

    public static void playRoundStart(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public static void playVictory(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public static void playDefeat(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
    }

    public static void playKill(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.2f);
    }

    public static void playDeath(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
    }

    public static void playRoundWin(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    public static void playRoundLose(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
    }

    public static void playGuiOpen(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }

    public static void playGuiClose(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.2f);
    }

    public static void playClick(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    public static void playSuccessClick(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }

    public static void playErrorClick(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    public static void playDenied(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.3f, 2.0f);
    }

    public static void playPageTurn(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 0.5f, 1.0f);
    }

    public static void playItemPickup(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
    }

    public static void playQueueJoin(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
    }

    public static void playQueueLeave(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
    }

    public static void playMatchFound(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }

    public static void playDuelRequest(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
    }

    public static void playDuelAccepted(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    public static void playDuelDeclined(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 0.5f);
    }

    public static void playPartyInvite(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.5f);
    }

    public static void playPartyJoin(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
    }

    public static void playPartyLeave(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_VILLAGER_HURT, 0.5f, 1.0f);
    }

    public static void playPartyDisbanded(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_IRON_GOLEM_DEATH, 0.3f, 1.5f);
    }

    public static void playCriticalHit(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }

    public static void playCombo(@NotNull Player player, int comboCount) {
        float pitch = Math.min(0.5f + (float)comboCount * 0.1f, 2.0f);
        SoundUtils.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, pitch);
    }

    public static void playStreak(@NotNull Player player, int streak) {
        if (streak == 3) {
            SoundUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
        } else if (streak == 5) {
            SoundUtils.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
        } else if (streak >= 10) {
            SoundUtils.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 2.0f);
        }
    }

    public static void playNotification(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
    }

    public static void playWarning(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
    }

    public static void playAlert(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
    }

    public static void playTimerTick(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_LEVER_CLICK, 0.3f, 1.5f);
    }

    public static void playTimerWarning(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
    }

    public static void playKitEquipped(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.0f, 1.0f);
    }

    public static void playKitSaved(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 1.2f);
    }

    public static void playKitLoaded(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_HORSE_ARMOR, 0.5f, 1.0f);
    }

    public static void playTeleport(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
    }

    public static void playSpawn(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.5f, 1.5f);
    }

    public static void playRespawn(@NotNull Player player) {
        SoundUtils.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
    }

    public static void playArenaReset(@NotNull Location location) {
        SoundUtils.playSound(location, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.3f, 1.5f);
    }

    public static void playCustomSound(@NotNull Player player, @NotNull String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf((String)soundName.toUpperCase());
            SoundUtils.playSound(player, sound, volume, pitch);
        }
        catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), soundName, DEFAULT_CATEGORY, volume, pitch);
        }
    }

    public static void playSoundFromConfig(@NotNull Player player, @Nullable String configString) {
        if (configString == null || configString.isEmpty()) {
            return;
        }
        String[] parts = configString.split(":");
        String soundName = parts[0];
        float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
        float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
        SoundUtils.playCustomSound(player, soundName, volume, pitch);
    }

    public static void playSoundSequence(@NotNull Player player, @NotNull Plugin plugin, SoundEntry ... sounds) {
        long currentDelay = 0L;
        for (SoundEntry entry : sounds) {
            long delay = currentDelay += entry.delayTicks();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    SoundUtils.playSound(player, entry.sound(), entry.volume(), entry.pitch());
                }
            }, delay);
        }
    }

    public static void playVictoryFanfare(@NotNull Player player, @NotNull Plugin plugin) {
        SoundUtils.playSoundSequence(player, plugin, new SoundEntry(Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f, 0L), new SoundEntry(Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.3f, 3L), new SoundEntry(Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f, 3L), new SoundEntry(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f, 5L));
    }

    public static void playDefeatSequence(@NotNull Player player, @NotNull Plugin plugin) {
        SoundUtils.playSoundSequence(player, plugin, new SoundEntry(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f, 0L), new SoundEntry(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.4f, 5L), new SoundEntry(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.3f, 5L));
    }

    public record SoundEntry(Sound sound, float volume, float pitch, long delayTicks) {
    }
}

