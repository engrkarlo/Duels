/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.player.PlayerStateManager;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundResetTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final DuelManager duelManager;
    private final KitManager kitManager;
    private final PlayerStateManager playerStateManager;
    private final ArenaManager arenaManager;
    private final DuelMatch match;
    private static final float DEFAULT_WALK_SPEED = 0.2f;
    private static final float FROZEN_SPEED = 0.0f;
    private static final double SPAWN_CENTER_OFFSET = 0.5;

    public RoundResetTask(@NotNull UltimateDuels plugin, @NotNull DuelManager duelManager, @NotNull DuelMatch match) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.kitManager = plugin.getKitManager();
        this.playerStateManager = plugin.getPlayerStateManager();
        this.arenaManager = plugin.getArenaManager();
        this.match = match;
    }

    public void run() {
        try {
            this.executeReset();
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Error during round reset for match " + String.valueOf(this.match.getMatchId()) + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeReset() {
        String kitName = this.match.getKitName();
        DuelKit kit = this.kitManager.getAdminKit(kitName);
        if (kit == null) {
            this.plugin.getLogger().warning("Kit not found during round reset: " + kitName);
            this.duelManager.cancelMatch(this.match, "Kit not found!");
            return;
        }
        DuelArena arena = this.match.getArena();
        for (DuelParticipant participant : this.match.getAllParticipants()) {
            this.resetParticipant(participant, kit, arena);
        }
        this.cleanupArena(arena);
        this.notifySpectators();
        this.plugin.getLogger().fine("Round " + this.match.getCurrentRound() + " reset complete for match " + String.valueOf(this.match.getMatchId()));
    }

    private void resetParticipant(@NotNull DuelParticipant participant, @NotNull DuelKit kit, @NotNull DuelArena arena) {
        Player player = Bukkit.getPlayer((UUID)participant.getUuid());
        if (player == null || !player.isOnline()) {
            this.plugin.getLogger().warning("Player offline during round reset: " + participant.getPlayerName());
            return;
        }
        this.resetGamemode(player);
        this.resetPlayerState(player);
        this.applyKit(player, kit);
        this.teleportToSpawn(player, participant, arena);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.2f);
        participant.setAlive(true);
        participant.resetRoundStats();
        this.showResetTitle(player);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    private void resetGamemode(@NotNull Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    private void resetPlayerState(@NotNull Player player) {
        if (this.playerStateManager != null) {
            this.playerStateManager.resetForRound(player);
        } else {
            this.manualResetPlayerState(player);
        }
    }

    private void manualResetPlayerState(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setNoDamageTicks(0);
        player.setExp(0.0f);
        player.setLevel(0);
    }

    private void applyKit(@NotNull Player player, @NotNull DuelKit kit) {
        if (this.kitManager != null) {
            this.kitManager.applyKit(player, kit, true);
        } else {
            this.plugin.getLogger().warning("KitManager not available for round reset!");
        }
    }

    private void teleportToSpawn(@NotNull Player player, @NotNull DuelParticipant participant, @NotNull DuelArena arena) {
        Location spawn = this.getSpawnLocation(participant, arena);
        if (spawn != null) {
            Location centeredSpawn = this.centerLocation(spawn);
            player.teleport(centeredSpawn);
        } else {
            this.plugin.getLogger().warning("No spawn location for team " + participant.getTeamId() + " in arena " + arena.getName());
        }
    }

    @Nullable
    private Location getSpawnLocation(@NotNull DuelParticipant participant, @NotNull DuelArena arena) {
        int teamId = participant.getTeamId();
        if (teamId == 1) {
            return arena.getSpawnPoint1();
        }
        if (teamId == 2) {
            return arena.getSpawnPoint2();
        }
        return null;
    }

    @NotNull
    private Location centerLocation(@NotNull Location location) {
        Location centered = location.clone();
        centered.setX(Math.floor(location.getX()) + 0.5);
        centered.setZ(Math.floor(location.getZ()) + 0.5);
        return centered;
    }

    private void freezePlayer(@NotNull Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.2f);
    }

    public static void unfreezePlayer(@NotNull Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.2f);
    }

    private void showResetTitle(@NotNull Player player) {
        String winsText;
        int currentRound = this.match.getCurrentRound();
        int team1Wins = this.match.getTeamScore(1);
        int team2Wins = this.match.getTeamScore(2);
        DuelParticipant participant = this.match.getParticipant(player.getUniqueId());
        if (participant != null) {
            int playerTeam = participant.getTeamId();
            int myWins = this.match.getTeamScore(playerTeam);
            int theirWins = this.match.getTeamScore(playerTeam == 1 ? 2 : 1);
            winsText = "Wins: You " + myWins + " - Them " + theirWins;
        } else {
            winsText = "Wins: " + team1Wins + " - " + team2Wins;
        }
        Component titleComponent = ((TextComponent)Component.text((String)("ROUND " + currentRound)).color((TextColor)NamedTextColor.YELLOW)).decoration(TextDecoration.BOLD, true);
        Component subtitleComponent = Component.text((String)winsText).color((TextColor)NamedTextColor.GRAY);
        Title title = Title.title((Component)titleComponent, (Component)subtitleComponent, (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1500L), (Duration)Duration.ofMillis(300L)));
        player.showTitle(title);
    }

    private void cleanupArena(@NotNull DuelArena arena) {
        if (this.arenaManager != null) {
            this.arenaManager.cleanupArenaItems(arena);
        }
    }

    private void notifySpectators() {
        Set<UUID> spectators = this.duelManager.getMatchSpectators(this.match.getMatchId());
        Component message = Component.text((String)("Round " + this.match.getCurrentRound() + " starting...")).color((TextColor)NamedTextColor.GRAY);
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            spectator.sendMessage(message);
        }
    }

    @NotNull
    public DuelMatch getMatch() {
        return this.match;
    }
}

