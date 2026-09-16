/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.api;

import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelRequest;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.stats.PlayerStats;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UltimateDuelsAPI {
    @Nullable
    public DuelMatch getPlayerDuel(@NotNull Player var1);

    @Nullable
    public DuelMatch getPlayerDuel(@NotNull UUID var1);

    public boolean isInDuel(@NotNull Player var1);

    public boolean isInDuel(@NotNull UUID var1);

    @NotNull
    public Collection<DuelMatch> getActiveDuels();

    public int getActiveDuelCount();

    public @NotNull CompletableFuture<@Nullable DuelMatch> startDuel(@NotNull Player var1, @NotNull Player var2, @NotNull String var3, @Nullable String var4, int var5, boolean var6);

    public void endDuel(@NotNull DuelMatch var1, @Nullable UUID var2);

    @Nullable
    public DuelRequest sendDuelRequest(@NotNull Player var1, @NotNull Player var2, @NotNull String var3, @Nullable String var4, int var5, boolean var6);

    @NotNull
    public Collection<DuelRequest> getPendingRequests(@NotNull Player var1);

    public boolean canDuel(@NotNull Player var1);

    @Nullable
    public String getCannotDuelReason(@NotNull Player var1);

    public boolean addSpectator(@NotNull Player var1, @NotNull DuelMatch var2);

    public void removeSpectator(@NotNull Player var1);

    public boolean isSpectating(@NotNull Player var1);

    @Nullable
    public DuelMatch getSpectatedDuel(@NotNull Player var1);

    @NotNull
    public Collection<UUID> getSpectators(@NotNull DuelMatch var1);

    public boolean addToQueue(@NotNull Player var1, @NotNull String var2);

    public boolean removeFromQueue(@NotNull Player var1);

    public boolean isInQueue(@NotNull Player var1);

    @Nullable
    public String getQueuedKit(@NotNull Player var1);

    public int getQueueSize(@NotNull String var1);

    public int getFightingCount(@NotNull String var1);

    @NotNull
    public Map<UUID, String> getAllQueuedPlayers();

    @Nullable
    public DuelKit getKit(@NotNull String var1);

    @NotNull
    public Collection<DuelKit> getAllKits();

    @NotNull
    public Collection<DuelKit> getEnabledKits();

    public boolean kitExists(@NotNull String var1);

    @Nullable
    public DuelKit createKit(@NotNull String var1, @NotNull Player var2);

    public boolean deleteKit(@NotNull String var1);

    public void applyKit(@NotNull Player var1, @NotNull DuelKit var2);

    @Nullable
    public DuelKit getPlayerCustomKit(@NotNull Player var1, @NotNull String var2);

    public boolean hasCustomKit(@NotNull Player var1, @NotNull String var2);

    @Nullable
    public DuelArena getArena(@NotNull String var1);

    @NotNull
    public Collection<DuelArena> getAllArenas();

    @NotNull
    public Collection<DuelArena> getAvailableArenas();

    public boolean arenaExists(@NotNull String var1);

    public boolean isArenaAvailable(@NotNull DuelArena var1);

    @Nullable
    public DuelArena getRandomAvailableArena();

    @NotNull
    public CompletableFuture<Boolean> regenerateArena(@NotNull DuelArena var1);

    @Nullable
    public FFAArenaInstance getFFAArena(@NotNull String var1);

    @NotNull
    public Collection<FFAArenaInstance> getAllFFAArenas();

    public boolean isInFFA(@NotNull Player var1);

    @Nullable
    public FFAArenaInstance getPlayerFFAArena(@NotNull Player var1);

    public boolean joinFFA(@NotNull Player var1, @NotNull String var2);

    public void leaveFFA(@NotNull Player var1);

    public int getFFAPlayerCount(@NotNull String var1);

    @NotNull
    public Collection<UUID> getFFAPlayers(@NotNull String var1);

    @Nullable
    public Party getPlayerParty(@NotNull Player var1);

    @Nullable
    public Party getPlayerParty(@NotNull UUID var1);

    public boolean isInParty(@NotNull Player var1);

    public boolean isPartyLeader(@NotNull Player var1);

    @Nullable
    public Party createParty(@NotNull Player var1);

    public void disbandParty(@NotNull Party var1);

    @NotNull
    public Collection<Party> getAllParties();

    public boolean sendPartyInvite(@NotNull Party var1, @NotNull Player var2);

    @NotNull
    public PlayerStats getPlayerStats(@NotNull Player var1);

    @NotNull
    public CompletableFuture<PlayerStats> getPlayerStatsAsync(@NotNull UUID var1);

    @NotNull
    public CompletableFuture<List<PlayerStats>> getTopPlayersByKills(int var1);

    @NotNull
    public CompletableFuture<List<PlayerStats>> getTopPlayersByWins(int var1);

    @NotNull
    public CompletableFuture<List<PlayerStats>> getTopPlayersByWinStreak(int var1);

    public void addKills(@NotNull UUID var1, @NotNull String var2, int var3);

    public void addDeaths(@NotNull UUID var1, @NotNull String var2, int var3);

    public void addWin(@NotNull UUID var1, @NotNull String var2);

    public void addLoss(@NotNull UUID var1, @NotNull String var2);

    public boolean isDuelRequestsEnabled(@NotNull Player var1);

    public void setDuelRequestsEnabled(@NotNull Player var1, boolean var2);

    public boolean isScoreboardEnabled(@NotNull Player var1);

    public void setScoreboardEnabled(@NotNull Player var1, boolean var2);

    public boolean isSpectatorsAllowed(@NotNull Player var1);

    public void setSpectatorsAllowed(@NotNull Player var1, boolean var2);

    public void teleportToLobby(@NotNull Player var1);

    public boolean isInLobby(@NotNull Player var1);

    public void giveLobbyItems(@NotNull Player var1);

    public void sendToLobby(@NotNull Player var1);

    public int getPlayerPing(@NotNull Player var1);

    @NotNull
    public String formatDuration(long var1);

    @NotNull
    public String getVersion();

    public boolean isFeatureEnabled(@NotNull String var1);

    public void registerDuelStartListener(@NotNull DuelEventListener var1);

    public void registerDuelEndListener(@NotNull DuelEventListener var1);

    public void unregisterDuelListener(@NotNull DuelEventListener var1);

    @FunctionalInterface
    public static interface DuelEventListener {
        public void onDuelEvent(@NotNull DuelMatch var1);
    }
}

