/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.api;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.api.UltimateDuelsAPI;
import com.ultimateduels.api.UltimateDuelsAPIProvider;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelRequest;
import com.ultimateduels.duel.model.DuelRequestResult;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.party.model.Party;
import com.ultimateduels.party.model.PartyResult;
import com.ultimateduels.stats.PlayerStats;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UltimateDuelsAPIImpl
implements UltimateDuelsAPI {
    private final UltimateDuels plugin;
    private final List<UltimateDuelsAPI.DuelEventListener> startListeners = new CopyOnWriteArrayList<UltimateDuelsAPI.DuelEventListener>();
    private final List<UltimateDuelsAPI.DuelEventListener> endListeners = new CopyOnWriteArrayList<UltimateDuelsAPI.DuelEventListener>();

    public UltimateDuelsAPIImpl(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        UltimateDuelsAPIProvider.register(this);
    }

    @Override
    @Nullable
    public DuelMatch getPlayerDuel(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getDuelManager().getMatch(player);
    }

    @Override
    @Nullable
    public DuelMatch getPlayerDuel(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return this.plugin.getDuelManager().getMatch(uuid);
    }

    @Override
    public boolean isInDuel(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getDuelManager().isInDuel(player);
    }

    @Override
    public boolean isInDuel(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return this.plugin.getDuelManager().isInMatch(uuid);
    }

    @Override
    @NotNull
    public Collection<DuelMatch> getActiveDuels() {
        return this.plugin.getDuelManager().getActiveMatches();
    }

    @Override
    public int getActiveDuelCount() {
        return this.plugin.getDuelManager().getActiveMatchCount();
    }

    @Override
    public @NotNull CompletableFuture<@Nullable DuelMatch> startDuel(@NotNull Player player1, @NotNull Player player2, @NotNull String kitName, @Nullable String arenaName, int rounds, boolean bestOf) {
        Objects.requireNonNull(player1, "Player1 cannot be null");
        Objects.requireNonNull(player2, "Player2 cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void endDuel(@NotNull DuelMatch duel, @Nullable UUID winner) {
        Objects.requireNonNull(duel, "Duel cannot be null");
        this.plugin.getDuelManager().cancelMatch(duel, "Ended via API");
    }

    @Override
    @Nullable
    public DuelRequest sendDuelRequest(@NotNull Player sender, @NotNull Player target, @NotNull String kitName, @Nullable String arenaName, int rounds, boolean bestOf) {
        Objects.requireNonNull(sender, "Sender cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        DuelRequestResult result = this.plugin.getDuelManager().sendRequest(sender, target, kitName, arenaName, rounds, bestOf);
        if (result.success()) {
            return this.plugin.getDuelManager().getPendingRequest(target.getUniqueId());
        }
        return null;
    }

    @Override
    @NotNull
    public Collection<DuelRequest> getPendingRequests(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        DuelRequest request = this.plugin.getDuelManager().getPendingRequest(player.getUniqueId());
        if (request != null) {
            return Collections.singletonList(request);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canDuel(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        UUID uuid = player.getUniqueId();
        if (this.plugin.getDuelManager().isInMatch(uuid)) {
            return false;
        }
        if (this.plugin.getQueueManager().isInQueue(uuid)) {
            return false;
        }
        if (this.plugin.getFFAManager().isInFFA(uuid)) {
            return false;
        }
        return this.plugin.getLobbyManager().isInLobby(player);
    }

    @Override
    @Nullable
    public String getCannotDuelReason(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        UUID uuid = player.getUniqueId();
        if (this.plugin.getDuelManager().isInMatch(uuid)) {
            return "Already in a duel";
        }
        if (this.plugin.getQueueManager().isInQueue(uuid)) {
            return "Already in queue";
        }
        if (this.plugin.getFFAManager().isInFFA(uuid)) {
            return "Currently in FFA";
        }
        if (!this.plugin.getLobbyManager().isInLobby(player)) {
            return "Not in the duel lobby";
        }
        return null;
    }

    @Override
    public boolean addSpectator(@NotNull Player spectator, @NotNull DuelMatch duel) {
        Objects.requireNonNull(spectator, "Spectator cannot be null");
        Objects.requireNonNull(duel, "Duel cannot be null");
        return this.plugin.getDuelManager().addSpectator(spectator, duel.getMatchId());
    }

    @Override
    public void removeSpectator(@NotNull Player spectator) {
        Objects.requireNonNull(spectator, "Spectator cannot be null");
        this.plugin.getDuelManager().removeSpectator(spectator);
    }

    @Override
    public boolean isSpectating(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getDuelManager().isSpectating(player.getUniqueId());
    }

    @Override
    @Nullable
    public DuelMatch getSpectatedDuel(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        for (DuelMatch match : this.plugin.getDuelManager().getActiveMatches()) {
            if (!this.plugin.getDuelManager().getMatchSpectators(match.getMatchId()).contains(player.getUniqueId())) continue;
            return match;
        }
        return null;
    }

    @Override
    @NotNull
    public Collection<UUID> getSpectators(@NotNull DuelMatch duel) {
        Objects.requireNonNull(duel, "Duel cannot be null");
        return this.plugin.getDuelManager().getMatchSpectators(duel.getMatchId());
    }

    @Override
    public boolean addToQueue(@NotNull Player player, @NotNull String kitName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        if (!this.canDuel(player)) {
            return false;
        }
        return false;
    }

    @Override
    public boolean removeFromQueue(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getQueueManager().removeFromAllQueues(player.getUniqueId());
        return true;
    }

    @Override
    public boolean isInQueue(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getQueueManager().isInQueue(player.getUniqueId());
    }

    @Override
    @Nullable
    public String getQueuedKit(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getQueueManager().getQueuedKit(player.getUniqueId());
    }

    @Override
    public int getQueueSize(@NotNull String kitName) {
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        return this.plugin.getQueueManager().getQueueSize(kitName);
    }

    @Override
    public int getFightingCount(@NotNull String kitName) {
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        return this.plugin.getDuelManager().getPlayersInMatchForKit(kitName);
    }

    @Override
    @NotNull
    public Map<UUID, String> getAllQueuedPlayers() {
        return Collections.emptyMap();
    }

    @Override
    @Nullable
    public DuelKit getKit(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return this.plugin.getKitManager().getAdminKit(name);
    }

    @Override
    @NotNull
    public Collection<DuelKit> getAllKits() {
        return this.plugin.getKitManager().getAllAdminKits();
    }

    @Override
    @NotNull
    public Collection<DuelKit> getEnabledKits() {
        return this.plugin.getKitManager().getEnabledKits();
    }

    @Override
    public boolean kitExists(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return this.plugin.getKitManager().adminKitExists(name);
    }

    @Override
    @Nullable
    public DuelKit createKit(@NotNull String name, @NotNull Player player) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        if (this.plugin.getKitManager().createAdminKitFromPlayer(player, name)) {
            return this.plugin.getKitManager().getAdminKit(name);
        }
        return null;
    }

    @Override
    public boolean deleteKit(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return this.plugin.getKitManager().deleteAdminKit(name);
    }

    @Override
    public void applyKit(@NotNull Player player, @NotNull DuelKit kit) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(kit, "Kit cannot be null");
        this.plugin.getKitManager().applyKit(player, kit, true);
    }

    @Override
    @Nullable
    public DuelKit getPlayerCustomKit(@NotNull Player player, @NotNull String baseKitName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(baseKitName, "Base kit name cannot be null");
        return this.plugin.getKitManager().getPlayerKit(player.getUniqueId(), baseKitName);
    }

    @Override
    public boolean hasCustomKit(@NotNull Player player, @NotNull String baseKitName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(baseKitName, "Base kit name cannot be null");
        return this.plugin.getKitManager().playerKitExists(player.getUniqueId(), baseKitName);
    }

    @Override
    @Nullable
    public DuelArena getArena(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return null;
    }

    @Override
    @NotNull
    public Collection<DuelArena> getAllArenas() {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public Collection<DuelArena> getAvailableArenas() {
        return Collections.emptyList();
    }

    @Override
    public boolean arenaExists(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return this.plugin.getArenaManager().arenaExists(name);
    }

    @Override
    public boolean isArenaAvailable(@NotNull DuelArena arena) {
        Objects.requireNonNull(arena, "Arena cannot be null");
        return false;
    }

    @Override
    @Nullable
    public DuelArena getRandomAvailableArena() {
        return null;
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> regenerateArena(@NotNull DuelArena arena) {
        Objects.requireNonNull(arena, "Arena cannot be null");
        return CompletableFuture.completedFuture(false);
    }

    @Override
    @Nullable
    public FFAArenaInstance getFFAArena(@NotNull String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return null;
    }

    @Override
    @NotNull
    public Collection<FFAArenaInstance> getAllFFAArenas() {
        return Collections.emptyList();
    }

    @Override
    public boolean isInFFA(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getFFAManager().isInFFA(player.getUniqueId());
    }

    @Override
    @Nullable
    public FFAArenaInstance getPlayerFFAArena(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return null;
    }

    @Override
    public boolean joinFFA(@NotNull Player player, @NotNull String arenaName) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(arenaName, "Arena name cannot be null");
        return false;
    }

    @Override
    public void leaveFFA(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
    }

    @Override
    public int getFFAPlayerCount(@NotNull String arenaName) {
        Objects.requireNonNull(arenaName, "Arena name cannot be null");
        return 0;
    }

    @Override
    @NotNull
    public Collection<UUID> getFFAPlayers(@NotNull String arenaName) {
        Objects.requireNonNull(arenaName, "Arena name cannot be null");
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public Party getPlayerParty(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getPartyManager().getParty(player.getUniqueId());
    }

    @Override
    @Nullable
    public Party getPlayerParty(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return this.plugin.getPartyManager().getParty(uuid);
    }

    @Override
    public boolean isInParty(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getPartyManager().isInParty(player.getUniqueId());
    }

    @Override
    public boolean isPartyLeader(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getPartyManager().isPartyLeader(player.getUniqueId());
    }

    @Override
    @Nullable
    public Party createParty(@NotNull Player leader) {
        Objects.requireNonNull(leader, "Leader cannot be null");
        if (this.isInParty(leader)) {
            return null;
        }
        PartyResult result = this.plugin.getPartyManager().createParty(leader);
        if (result.success()) {
            return this.plugin.getPartyManager().getParty(leader.getUniqueId());
        }
        return null;
    }

    @Override
    public void disbandParty(@NotNull Party party) {
        Objects.requireNonNull(party, "Party cannot be null");
        Player leader = Bukkit.getPlayer((UUID)party.getLeaderUUID());
        if (leader != null) {
            this.plugin.getPartyManager().disbandParty(leader);
        }
    }

    @Override
    @NotNull
    public Collection<Party> getAllParties() {
        return this.plugin.getPartyManager().getAllParties();
    }

    @Override
    public boolean sendPartyInvite(@NotNull Party party, @NotNull Player target) {
        Objects.requireNonNull(party, "Party cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");
        if (this.isInParty(target)) {
            return false;
        }
        Player leader = Bukkit.getPlayer((UUID)party.getLeaderUUID());
        if (leader == null) {
            return false;
        }
        PartyResult result = this.plugin.getPartyManager().invite(leader, target);
        return result.success();
    }

    @Override
    @NotNull
    public PlayerStats getPlayerStats(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getStatsManager().getStats(player.getUniqueId());
    }

    @Override
    @NotNull
    public CompletableFuture<PlayerStats> getPlayerStatsAsync(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return CompletableFuture.supplyAsync(() -> this.plugin.getStatsManager().getStats(uuid));
    }

    @Override
    @NotNull
    public CompletableFuture<List<PlayerStats>> getTopPlayersByKills(int limit) {
        this.validateLimit(limit);
        return CompletableFuture.completedFuture(this.plugin.getStatsManager().getTopPlayersByKills(limit));
    }

    @Override
    @NotNull
    public CompletableFuture<List<PlayerStats>> getTopPlayersByWins(int limit) {
        this.validateLimit(limit);
        return CompletableFuture.completedFuture(this.plugin.getStatsManager().getTopPlayersByWins(limit));
    }

    @Override
    @NotNull
    public CompletableFuture<List<PlayerStats>> getTopPlayersByWinStreak(int limit) {
        this.validateLimit(limit);
        return CompletableFuture.completedFuture(this.plugin.getStatsManager().getTopPlayersByWinStreak(limit));
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
    }

    @Override
    public void addKills(@NotNull UUID uuid, @NotNull String kitName, int amount) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.plugin.getStatsManager().addKills(uuid, kitName, amount);
    }

    @Override
    public void addDeaths(@NotNull UUID uuid, @NotNull String kitName, int amount) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.plugin.getStatsManager().addDeaths(uuid, kitName, amount);
    }

    @Override
    public void addWin(@NotNull UUID uuid, @NotNull String kitName) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        this.plugin.getStatsManager().recordWin(uuid, kitName);
    }

    @Override
    public void addLoss(@NotNull UUID uuid, @NotNull String kitName) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        Objects.requireNonNull(kitName, "Kit name cannot be null");
        this.plugin.getStatsManager().recordLoss(uuid, kitName);
    }

    @Override
    public boolean isDuelRequestsEnabled(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getSettingsManager().canReceiveDuelRequests(player);
    }

    @Override
    public void setDuelRequestsEnabled(@NotNull Player player, boolean enabled) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getSettingsManager().setDuelRequestsEnabled(player, enabled);
    }

    @Override
    public boolean isScoreboardEnabled(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getSettingsManager().hasScoreboardEnabled(player);
    }

    @Override
    public void setScoreboardEnabled(@NotNull Player player, boolean enabled) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getSettingsManager().setScoreboardEnabled(player, enabled);
    }

    @Override
    public boolean isSpectatorsAllowed(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getSettingsManager().allowsSpectators(player);
    }

    @Override
    public void setSpectatorsAllowed(@NotNull Player player, boolean allowed) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getSettingsManager().setAllowSpectators(player, allowed);
    }

    @Override
    public void teleportToLobby(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getLobbyManager().teleportToLobby(player);
    }

    @Override
    public boolean isInLobby(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.plugin.getLobbyManager().isInLobby(player);
    }

    @Override
    public void giveLobbyItems(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getLobbyManager().giveHotbarItems(player);
    }

    @Override
    public void sendToLobby(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.plugin.getLobbyManager().sendToLobby(player);
    }

    @Override
    public int getPlayerPing(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return player.getPing();
    }

    @Override
    @NotNull
    public String formatDuration(long ticks) {
        long seconds = ticks / 20L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (minutes > 0L) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return String.format("%ds", seconds);
    }

    @Override
    @NotNull
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean isFeatureEnabled(@NotNull String feature) {
        Objects.requireNonNull(feature, "Feature cannot be null");
        return this.plugin.getConfig().getBoolean("features." + feature, true);
    }

    @Override
    public void registerDuelStartListener(@NotNull UltimateDuelsAPI.DuelEventListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        this.startListeners.add(listener);
    }

    @Override
    public void registerDuelEndListener(@NotNull UltimateDuelsAPI.DuelEventListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        this.endListeners.add(listener);
    }

    @Override
    public void unregisterDuelListener(@NotNull UltimateDuelsAPI.DuelEventListener listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        this.startListeners.remove(listener);
        this.endListeners.remove(listener);
    }

    public void notifyDuelStart(@NotNull DuelMatch duel) {
        for (UltimateDuelsAPI.DuelEventListener listener : this.startListeners) {
            try {
                listener.onDuelEvent(duel);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Error in duel start listener: " + e.getMessage());
            }
        }
    }

    public void notifyDuelEnd(@NotNull DuelMatch duel) {
        for (UltimateDuelsAPI.DuelEventListener listener : this.endListeners) {
            try {
                listener.onDuelEvent(duel);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Error in duel end listener: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        this.startListeners.clear();
        this.endListeners.clear();
        UltimateDuelsAPIProvider.unregister();
    }
}

