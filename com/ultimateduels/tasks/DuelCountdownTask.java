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
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.jetbrains.annotations.NotNull
 */
package com.ultimateduels.tasks;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class DuelCountdownTask
extends BukkitRunnable {
    private final UltimateDuels plugin;
    private final DuelManager duelManager;
    private final DuelMatch match;
    private int countdown;
    private boolean cancelled;
    private static final int DEFAULT_COUNTDOWN_SECONDS = 3;
    private static final Sound COUNTDOWN_SOUND = Sound.BLOCK_NOTE_BLOCK_HAT;
    private static final Sound COUNTDOWN_FINAL_SOUND = Sound.BLOCK_NOTE_BLOCK_PLING;
    private static final Sound FIGHT_SOUND = Sound.ENTITY_ENDER_DRAGON_GROWL;
    private static final float COUNTDOWN_VOLUME = 1.0f;
    private static final float COUNTDOWN_PITCH = 1.0f;
    private static final float FINAL_COUNTDOWN_PITCH = 2.0f;
    private static final float FIGHT_VOLUME = 0.5f;
    private static final float FIGHT_PITCH = 1.5f;

    public DuelCountdownTask(@NotNull UltimateDuels plugin, @NotNull DuelManager duelManager, @NotNull DuelMatch match) {
        this(plugin, duelManager, match, 3);
    }

    public DuelCountdownTask(@NotNull UltimateDuels plugin, @NotNull DuelManager duelManager, @NotNull DuelMatch match, int countdownSeconds) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.match = match;
        this.countdown = Math.max(1, countdownSeconds);
        this.cancelled = false;
    }

    public void run() {
        if (this.shouldCancel()) {
            this.handleCancellation();
            return;
        }
        if (!this.validateParticipants()) {
            return;
        }
        if (this.countdown > 0) {
            this.displayCountdown();
            --this.countdown;
        } else {
            this.displayFightMessage();
            this.cancel();
            this.duelManager.onCountdownComplete(this.match);
        }
    }

    private boolean shouldCancel() {
        if (this.cancelled) {
            return true;
        }
        MatchState state = this.match.getState();
        return state == MatchState.CANCELLED || state == MatchState.COMPLETED;
    }

    private void handleCancellation() {
        this.cancel();
        this.plugin.getLogger().fine("Countdown cancelled for match: " + String.valueOf(this.match.getMatchId()));
    }

    private boolean validateParticipants() {
        for (DuelParticipant participant : this.match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player != null && player.isOnline()) continue;
            this.cancelled = true;
            this.cancel();
            String disconnectedName = participant.getPlayerName();
            this.duelManager.handleDisconnect(participant.getUuid());
            this.plugin.getLogger().info("Countdown cancelled - player disconnected: " + disconnectedName);
            return false;
        }
        return true;
    }

    private void displayCountdown() {
        Component titleComponent = this.createCountdownTitle();
        Component subtitleComponent = this.createCountdownSubtitle();
        Title title = Title.title((Component)titleComponent, (Component)subtitleComponent, (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1100L), (Duration)Duration.ZERO));
        Sound sound = this.countdown == 1 ? COUNTDOWN_FINAL_SOUND : COUNTDOWN_SOUND;
        float pitch = this.countdown == 1 ? 2.0f : 1.0f;
        for (DuelParticipant participant : this.match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null || !player.isOnline()) continue;
            player.showTitle(title);
            player.playSound(player.getLocation(), sound, 1.0f, pitch);
        }
        this.showToSpectators(title, null);
        this.plugin.getLogger().fine("Countdown: " + this.countdown + " for match: " + String.valueOf(this.match.getMatchId()));
    }

    private void displayFightMessage() {
        Component titleComponent = ((TextComponent)Component.text((String)"FIGHT!").color((TextColor)NamedTextColor.GREEN)).decoration(TextDecoration.BOLD, true);
        Component subtitleComponent = Component.text((String)"Good luck!").color((TextColor)NamedTextColor.GRAY);
        Title title = Title.title((Component)titleComponent, (Component)subtitleComponent, (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofMillis(1000L), (Duration)Duration.ofMillis(500L)));
        for (DuelParticipant participant : this.match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null || !player.isOnline()) continue;
            player.showTitle(title);
            player.playSound(player.getLocation(), FIGHT_SOUND, 0.5f, 1.5f);
        }
        this.showToSpectators(title, FIGHT_SOUND);
        this.plugin.getLogger().fine("Fight started for match: " + String.valueOf(this.match.getMatchId()));
    }

    private void showToSpectators(@NotNull Title title, Sound sound) {
        Set<UUID> spectators = this.duelManager.getMatchSpectators(this.match.getMatchId());
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            spectator.showTitle(title);
            if (sound == null) continue;
            spectator.playSound(spectator.getLocation(), sound, 0.5f, 1.5f);
        }
    }

    @NotNull
    private Component createCountdownTitle() {
        TextColor color = this.getCountdownColor();
        return ((TextComponent)Component.text((String)String.valueOf(this.countdown)).color(color)).decoration(TextDecoration.BOLD, true);
    }

    @NotNull
    private Component createCountdownSubtitle() {
        int currentRound = this.match.getCurrentRound();
        int totalRounds = this.match.getTotalRounds();
        if (totalRounds > 1) {
            int team1Wins = this.match.getTeamScore(1);
            int team2Wins = this.match.getTeamScore(2);
            return Component.text((String)("Round " + currentRound + " | Wins: " + team1Wins + " - " + team2Wins)).color((TextColor)NamedTextColor.GRAY);
        }
        return Component.text((String)this.match.getKitName()).color((TextColor)NamedTextColor.GRAY);
    }

    @NotNull
    private TextColor getCountdownColor() {
        return switch (this.countdown) {
            case 1 -> NamedTextColor.GREEN;
            case 2 -> NamedTextColor.GOLD;
            case 3 -> NamedTextColor.RED;
            default -> NamedTextColor.YELLOW;
        };
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelled) {
            try {
                this.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public int getCountdown() {
        return this.countdown;
    }

    @NotNull
    public DuelMatch getMatch() {
        return this.match;
    }
}

