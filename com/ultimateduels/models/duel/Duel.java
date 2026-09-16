/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.models.duel;

import com.ultimateduels.models.arena.Arena;
import com.ultimateduels.models.duel.DuelResult;
import com.ultimateduels.models.duel.DuelSettings;
import com.ultimateduels.models.duel.DuelState;
import com.ultimateduels.models.duel.WinCondition;
import com.ultimateduels.models.kit.Kit;
import com.ultimateduels.models.player.DuelPlayer;
import com.ultimateduels.models.player.PlayerState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Duel {
    private final UUID matchId = UUID.randomUUID();
    private final String duelId = this.matchId.toString().substring(0, 8);
    private final DuelSettings settings;
    private final Arena arena;
    private final Kit kit;
    private final List<DuelPlayer> team1;
    private final List<DuelPlayer> team2;
    private final Map<UUID, TeamSide> playerTeams;
    private final Set<UUID> spectators;
    private final Set<UUID> team1Players;
    private final Set<UUID> team2Players;
    private DuelState state;
    private long stateChangedAt;
    private int currentRound;
    private int team1Score;
    private int team2Score;
    private final List<RoundResult> roundHistory;
    private long matchStartTime;
    private long matchEndTime;
    private long roundStartTime;
    private long countdownStartTime;
    private final Map<UUID, MatchStats> playerMatchStats;
    private UUID lastKillerId;
    private UUID lastVictimId;
    private final Set<UUID> winners;
    private final Set<UUID> losers;
    private final Set<UUID> alivePlayers;
    private DuelResult result;
    private final Map<String, Object> metadata;

    public Duel(DuelSettings settings, Arena arena, Kit kit, List<DuelPlayer> team1, List<DuelPlayer> team2) {
        this.settings = settings;
        this.arena = arena;
        this.kit = kit;
        this.team1 = new ArrayList<DuelPlayer>(team1);
        this.team2 = new ArrayList<DuelPlayer>(team2);
        this.playerTeams = new ConcurrentHashMap<UUID, TeamSide>();
        this.spectators = ConcurrentHashMap.newKeySet();
        this.team1Players = ConcurrentHashMap.newKeySet();
        this.team2Players = ConcurrentHashMap.newKeySet();
        for (DuelPlayer player : team1) {
            this.playerTeams.put(player.getUuid(), TeamSide.TEAM_1);
            this.team1Players.add(player.getUuid());
        }
        for (DuelPlayer player : team2) {
            this.playerTeams.put(player.getUuid(), TeamSide.TEAM_2);
            this.team2Players.add(player.getUuid());
        }
        this.state = DuelState.INITIALIZING;
        this.stateChangedAt = System.currentTimeMillis();
        this.currentRound = 0;
        this.team1Score = 0;
        this.team2Score = 0;
        this.roundHistory = new ArrayList<RoundResult>();
        this.playerMatchStats = new ConcurrentHashMap<UUID, MatchStats>();
        for (DuelPlayer player : this.getAllParticipantsList()) {
            this.playerMatchStats.put(player.getUuid(), new MatchStats());
        }
        this.winners = ConcurrentHashMap.newKeySet();
        this.losers = ConcurrentHashMap.newKeySet();
        this.alivePlayers = ConcurrentHashMap.newKeySet();
        for (DuelPlayer player : this.getAllParticipantsList()) {
            this.alivePlayers.add(player.getUuid());
        }
        this.metadata = new ConcurrentHashMap<String, Object>();
    }

    public static Duel create1v1(DuelSettings settings, Arena arena, Kit kit, DuelPlayer player1, DuelPlayer player2) {
        return new Duel(settings, arena, kit, Collections.singletonList(player1), Collections.singletonList(player2));
    }

    public Set<UUID> getOpponents(UUID playerId) {
        HashSet<UUID> opponents = new HashSet<UUID>();
        if (this.isTeamDuel()) {
            int playerTeam = this.getTeam(playerId);
            for (UUID participantId : this.getAllParticipants()) {
                if (this.getTeam(participantId) == playerTeam) continue;
                opponents.add(participantId);
            }
        } else {
            for (UUID participantId : this.getAllParticipants()) {
                if (participantId.equals(playerId)) continue;
                opponents.add(participantId);
            }
        }
        return opponents;
    }

    public Set<UUID> getAllParticipants() {
        HashSet<UUID> all = new HashSet<UUID>();
        all.addAll(this.team1Players);
        all.addAll(this.team2Players);
        return all;
    }

    public boolean isTeamDuel() {
        return this.team1Players.size() > 1 || this.team2Players.size() > 1;
    }

    public int getTeam(UUID playerId) {
        if (this.team1Players.contains(playerId)) {
            return 1;
        }
        if (this.team2Players.contains(playerId)) {
            return 2;
        }
        return -1;
    }

    public Set<UUID> getAlivePlayers() {
        return Collections.unmodifiableSet(this.alivePlayers);
    }

    public void markPlayerDead(UUID playerId) {
        this.alivePlayers.remove(playerId);
    }

    public void markPlayerAlive(UUID playerId) {
        if (this.getAllParticipants().contains(playerId)) {
            this.alivePlayers.add(playerId);
        }
    }

    public void resetAlivePlayers() {
        this.alivePlayers.clear();
        this.alivePlayers.addAll(this.getAllParticipants());
    }

    public Set<UUID> getWinners() {
        return Collections.unmodifiableSet(this.winners);
    }

    public Set<UUID> getLosers() {
        return Collections.unmodifiableSet(this.losers);
    }

    public void setWinners(TeamSide winnerTeam) {
        this.winners.clear();
        this.losers.clear();
        if (winnerTeam == TeamSide.TEAM_1) {
            this.winners.addAll(this.team1Players);
            this.losers.addAll(this.team2Players);
        } else if (winnerTeam == TeamSide.TEAM_2) {
            this.winners.addAll(this.team2Players);
            this.losers.addAll(this.team1Players);
        }
    }

    public String getDuelId() {
        return this.duelId;
    }

    public DuelState getState() {
        return this.state;
    }

    public void setState(DuelState newState) {
        DuelState oldState = this.state;
        this.state = newState;
        this.stateChangedAt = System.currentTimeMillis();
        this.updatePlayerStates(newState);
        this.onStateChange(oldState, newState);
    }

    private void updatePlayerStates(DuelState duelState) {
        PlayerState playerState = switch (duelState) {
            case DuelState.INITIALIZING, DuelState.TELEPORTING -> PlayerState.TELEPORTING;
            case DuelState.COUNTDOWN -> PlayerState.IN_COUNTDOWN;
            case DuelState.FIGHTING -> PlayerState.FIGHTING;
            case DuelState.ROUND_ENDING -> PlayerState.ROUND_ENDED;
            case DuelState.ENDING, DuelState.ENDED -> PlayerState.IN_LOBBY;
            default -> PlayerState.FIGHTING;
        };
        for (DuelPlayer player : this.getAllParticipantsList()) {
            player.setState(playerState);
        }
    }

    private void onStateChange(DuelState oldState, DuelState newState) {
        switch (newState) {
            case COUNTDOWN: {
                this.countdownStartTime = System.currentTimeMillis();
                break;
            }
            case FIGHTING: {
                if (this.currentRound == 0) {
                    this.matchStartTime = System.currentTimeMillis();
                }
                this.roundStartTime = System.currentTimeMillis();
                ++this.currentRound;
                break;
            }
            case ENDING: {
                this.matchEndTime = System.currentTimeMillis();
            }
        }
    }

    public long getTimeInState() {
        return System.currentTimeMillis() - this.stateChangedAt;
    }

    public boolean isActive() {
        return this.state != DuelState.ENDED && this.state != DuelState.CANCELLED;
    }

    public boolean isFighting() {
        return this.state == DuelState.FIGHTING;
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public int getMaxRounds() {
        return this.settings.getRounds();
    }

    public int getTeam1Score() {
        return this.team1Score;
    }

    public int getTeam2Score() {
        return this.team2Score;
    }

    public void recordRoundWin(TeamSide winner, UUID mvpPlayer) {
        if (winner == TeamSide.TEAM_1) {
            ++this.team1Score;
            for (DuelPlayer duelPlayer : this.team1) {
                stats = this.playerMatchStats.get(duelPlayer.getUuid());
                if (stats == null) continue;
                stats.addRoundWon();
            }
        } else if (winner == TeamSide.TEAM_2) {
            ++this.team2Score;
            for (DuelPlayer duelPlayer : this.team2) {
                stats = this.playerMatchStats.get(duelPlayer.getUuid());
                if (stats == null) continue;
                stats.addRoundWon();
            }
        }
        HashMap<UUID, Integer> roundKills = new HashMap<UUID, Integer>();
        for (Map.Entry<UUID, MatchStats> entry : this.playerMatchStats.entrySet()) {
            roundKills.put(entry.getKey(), entry.getValue().getKills());
        }
        long l = System.currentTimeMillis() - this.roundStartTime;
        this.roundHistory.add(new RoundResult(this.currentRound, winner, mvpPlayer, l, roundKills));
    }

    public boolean isMatchOver() {
        WinCondition condition = this.settings.getWinCondition();
        int maxRounds = this.settings.getRounds();
        if (condition == WinCondition.BEST_OF) {
            int winsNeeded = maxRounds / 2 + 1;
            return this.team1Score >= winsNeeded || this.team2Score >= winsNeeded;
        }
        return this.currentRound >= maxRounds;
    }

    public TeamSide getWinningTeam() {
        if (this.team1Score > this.team2Score) {
            return TeamSide.TEAM_1;
        }
        if (this.team2Score > this.team1Score) {
            return TeamSide.TEAM_2;
        }
        return null;
    }

    public TeamSide getLosingTeam() {
        TeamSide winner = this.getWinningTeam();
        return winner != null ? winner.getOpposite() : null;
    }

    public boolean isDraw() {
        return this.isMatchOver() && this.team1Score == this.team2Score;
    }

    public String getFormattedScore() {
        return this.team1Score + " - " + this.team2Score;
    }

    public List<DuelPlayer> getAllParticipantsList() {
        ArrayList<DuelPlayer> all = new ArrayList<DuelPlayer>(this.team1);
        all.addAll(this.team2);
        return all;
    }

    public List<DuelPlayer> getTeam1() {
        return Collections.unmodifiableList(this.team1);
    }

    public List<DuelPlayer> getTeam2() {
        return Collections.unmodifiableList(this.team2);
    }

    public TeamSide getPlayerTeam(UUID uuid) {
        return this.playerTeams.get(uuid);
    }

    public List<DuelPlayer> getTeamPlayers(TeamSide team) {
        return team == TeamSide.TEAM_1 ? this.team1 : this.team2;
    }

    public List<DuelPlayer> getOpponentsList(UUID uuid) {
        TeamSide team = this.playerTeams.get(uuid);
        if (team == null) {
            return Collections.emptyList();
        }
        return team == TeamSide.TEAM_1 ? this.team2 : this.team1;
    }

    public DuelPlayer getOpponent(UUID uuid) {
        List<DuelPlayer> opponents = this.getOpponentsList(uuid);
        return opponents.isEmpty() ? null : opponents.get(0);
    }

    public DuelPlayer getParticipant(UUID uuid) {
        for (DuelPlayer player : this.team1) {
            if (!player.getUuid().equals(uuid)) continue;
            return player;
        }
        for (DuelPlayer player : this.team2) {
            if (!player.getUuid().equals(uuid)) continue;
            return player;
        }
        return null;
    }

    public boolean isParticipant(UUID uuid) {
        return this.playerTeams.containsKey(uuid) && this.playerTeams.get(uuid) != TeamSide.SPECTATOR;
    }

    public boolean is1v1() {
        return this.team1.size() == 1 && this.team2.size() == 1;
    }

    public List<DuelPlayer> getAlivePlayersOnTeam(TeamSide team) {
        return this.getTeamPlayers(team).stream().filter(p -> this.alivePlayers.contains(p.getUuid())).collect(Collectors.toList());
    }

    public boolean isTeamEliminated(TeamSide team) {
        return this.getAlivePlayersOnTeam(team).isEmpty();
    }

    public void addSpectator(UUID uuid) {
        this.spectators.add(uuid);
        this.playerTeams.put(uuid, TeamSide.SPECTATOR);
    }

    public void removeSpectator(UUID uuid) {
        this.spectators.remove(uuid);
        this.playerTeams.remove(uuid);
    }

    public Set<UUID> getSpectators() {
        return Collections.unmodifiableSet(this.spectators);
    }

    public int getSpectatorCount() {
        return this.spectators.size();
    }

    public boolean isSpectator(UUID uuid) {
        return this.spectators.contains(uuid);
    }

    public void recordKill(UUID killerId, UUID victimId) {
        this.lastKillerId = killerId;
        this.lastVictimId = victimId;
        MatchStats killerStats = this.playerMatchStats.get(killerId);
        MatchStats victimStats = this.playerMatchStats.get(victimId);
        if (killerStats != null) {
            killerStats.addKill();
        }
        if (victimStats != null) {
            victimStats.addDeath();
        }
        this.markPlayerDead(victimId);
        DuelPlayer killer = this.getParticipant(killerId);
        DuelPlayer victim = this.getParticipant(victimId);
        if (killer != null) {
            killer.getCombatData().addKill();
        }
        if (victim != null) {
            victim.getCombatData().addDeath();
            victim.setState(PlayerState.ROUND_ENDED);
        }
    }

    public void recordDamage(UUID dealerId, UUID receiverId, double damage) {
        MatchStats dealerStats = this.playerMatchStats.get(dealerId);
        MatchStats receiverStats = this.playerMatchStats.get(receiverId);
        if (dealerStats != null) {
            dealerStats.addDamageDealt(damage);
        }
        if (receiverStats != null) {
            receiverStats.addDamageTaken(damage);
        }
        DuelPlayer dealer = this.getParticipant(dealerId);
        DuelPlayer receiver = this.getParticipant(receiverId);
        if (dealer != null) {
            dealer.getCombatData().addDamageDealt(damage);
        }
        if (receiver != null) {
            receiver.getCombatData().addDamageTaken(damage);
            receiver.getCombatData().setLastDamager(dealerId);
        }
    }

    public MatchStats getMatchStats(UUID uuid) {
        return this.playerMatchStats.get(uuid);
    }

    public Map<UUID, MatchStats> getAllMatchStats() {
        return Collections.unmodifiableMap(this.playerMatchStats);
    }

    public UUID getMVP() {
        UUID mvp = null;
        int maxKills = -1;
        for (Map.Entry<UUID, MatchStats> entry : this.playerMatchStats.entrySet()) {
            if (entry.getValue().getKills() <= maxKills) continue;
            maxKills = entry.getValue().getKills();
            mvp = entry.getKey();
        }
        return mvp;
    }

    public void teleportToSpawns() {
        Location spawn;
        DuelPlayer player;
        int i;
        for (i = 0; i < this.team1.size(); ++i) {
            player = this.team1.get(i);
            spawn = this.arena.getSpawnPoints().getTeam1Spawn(i);
            if (spawn == null || !player.isOnline()) continue;
            player.teleport(spawn);
        }
        for (i = 0; i < this.team2.size(); ++i) {
            player = this.team2.get(i);
            spawn = this.arena.getSpawnPoints().getTeam2Spawn(i);
            if (spawn == null || !player.isOnline()) continue;
            player.teleport(spawn);
        }
    }

    public void teleportSpectator(Player specPlayer) {
        Location specSpawn = this.arena.getSpectatorSpawn();
        if (specSpawn == null) {
            specSpawn = this.arena.getSpawnPoints().getCenterPoint();
        }
        if (specSpawn != null) {
            specPlayer.teleport(specSpawn);
        }
    }

    public void applyKitToAll() {
        for (DuelPlayer player : this.getAllParticipantsList()) {
            if (!player.isOnline()) continue;
            player.clearInventory();
            player.clearEffects();
            player.heal();
            player.applyKit(this.kit, false);
        }
    }

    public void resetPlayerForRound(DuelPlayer player) {
        if (!player.isOnline()) {
            return;
        }
        player.heal();
        player.clearEffects();
        player.clearInventory();
        player.applyKit(this.kit, false);
        player.getCombatData().reset();
        player.setState(PlayerState.IN_COUNTDOWN);
        this.markPlayerAlive(player.getUuid());
    }

    public void resetAllForRound() {
        this.resetAlivePlayers();
        for (DuelPlayer player : this.getAllParticipantsList()) {
            this.resetPlayerForRound(player);
        }
    }

    public long getMatchDuration() {
        if (this.matchStartTime == 0L) {
            return 0L;
        }
        long endTime = this.matchEndTime > 0L ? this.matchEndTime : System.currentTimeMillis();
        return endTime - this.matchStartTime;
    }

    public long getRoundDuration() {
        if (this.roundStartTime == 0L) {
            return 0L;
        }
        return System.currentTimeMillis() - this.roundStartTime;
    }

    public String getFormattedDuration() {
        long duration = this.getMatchDuration() / 1000L;
        long minutes = duration / 60L;
        long seconds = duration % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public long getMatchStartTime() {
        return this.matchStartTime;
    }

    public long getCountdownStartTime() {
        return this.countdownStartTime;
    }

    public DuelResult createResult() {
        List losersList;
        TeamSide winningTeam = this.getWinningTeam();
        List winnersList = winningTeam != null ? this.getTeamPlayers(winningTeam) : Collections.emptyList();
        List<Object> list = losersList = winningTeam != null ? this.getTeamPlayers(winningTeam.getOpposite()) : Collections.emptyList();
        if (winningTeam != null) {
            this.setWinners(winningTeam);
        }
        this.result = new DuelResult(this.matchId, winnersList.isEmpty() ? null : ((DuelPlayer)winnersList.get(0)).getUuid(), losersList.isEmpty() ? null : ((DuelPlayer)losersList.get(0)).getUuid(), this.team1Score, this.team2Score, this.currentRound, this.settings.getRounds(), this.kit.getId(), this.arena.getId(), this.getMatchDuration(), this.getMVP(), this.isDraw(), new HashMap<UUID, MatchStats>(this.playerMatchStats));
        return this.result;
    }

    public DuelResult getResult() {
        return this.result;
    }

    public void end() {
        if (this.result == null) {
            this.createResult();
        }
        this.setState(DuelState.ENDED);
        this.matchEndTime = System.currentTimeMillis();
    }

    public void cancel(String reason) {
        this.setState(DuelState.CANCELLED);
        this.metadata.put("cancel_reason", reason);
        this.matchEndTime = System.currentTimeMillis();
    }

    public UUID getMatchId() {
        return this.matchId;
    }

    public DuelSettings getSettings() {
        return this.settings;
    }

    public Arena getArena() {
        return this.arena;
    }

    public Kit getKit() {
        return this.kit;
    }

    public List<RoundResult> getRoundHistory() {
        return Collections.unmodifiableList(this.roundHistory);
    }

    public UUID getLastKillerId() {
        return this.lastKillerId;
    }

    public UUID getLastVictimId() {
        return this.lastVictimId;
    }

    public Set<UUID> getTeam1Players() {
        return Collections.unmodifiableSet(this.team1Players);
    }

    public Set<UUID> getTeam2Players() {
        return Collections.unmodifiableSet(this.team2Players);
    }

    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public <T> T getMetadata(String key) {
        return (T)this.metadata.get(key);
    }

    public <T> T getMetadata(String key, T defaultValue) {
        Object value = this.metadata.get(key);
        return (T)(value != null ? value : defaultValue);
    }

    public void broadcast(String message) {
        for (DuelPlayer player : this.getAllParticipantsList()) {
            player.sendMessage(message);
        }
        for (UUID spectatorId : this.spectators) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null) continue;
            spectator.sendMessage(message);
        }
    }

    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (DuelPlayer player : this.getAllParticipantsList()) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void broadcastSound(Sound sound, float volume, float pitch) {
        for (DuelPlayer player : this.getAllParticipantsList()) {
            player.playSound(sound, volume, pitch);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Duel duel = (Duel)o;
        return Objects.equals(this.matchId, duel.matchId);
    }

    public int hashCode() {
        return Objects.hash(this.matchId);
    }

    public String toString() {
        return "Duel{matchId=" + String.valueOf(this.matchId) + ", duelId=" + this.duelId + ", state=" + String.valueOf((Object)this.state) + ", kit=" + this.kit.getId() + ", arena=" + this.arena.getId() + ", score=" + this.getFormattedScore() + ", round=" + this.currentRound + "/" + this.settings.getRounds() + "}";
    }

    public static enum TeamSide {
        TEAM_1("Team 1", "\u00a7a"),
        TEAM_2("Team 2", "\u00a7c"),
        SPECTATOR("Spectator", "\u00a77");

        private final String displayName;
        private final String colorCode;

        private TeamSide(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getColorCode() {
            return this.colorCode;
        }

        public String getColoredName() {
            return this.colorCode + this.displayName;
        }

        public TeamSide getOpposite() {
            return this == TEAM_1 ? TEAM_2 : (this == TEAM_2 ? TEAM_1 : SPECTATOR);
        }
    }

    public static class MatchStats {
        private int kills = 0;
        private int deaths = 0;
        private double damageDealt = 0.0;
        private double damageTaken = 0.0;
        private int arrowsShot = 0;
        private int arrowsHit = 0;
        private int potionsUsed = 0;
        private int goldenApplesEaten = 0;
        private int roundsWon = 0;

        public void addKill() {
            ++this.kills;
        }

        public void addDeath() {
            ++this.deaths;
        }

        public void addDamageDealt(double damage) {
            this.damageDealt += damage;
        }

        public void addDamageTaken(double damage) {
            this.damageTaken += damage;
        }

        public void addArrowShot() {
            ++this.arrowsShot;
        }

        public void addArrowHit() {
            ++this.arrowsHit;
        }

        public void addPotionUsed() {
            ++this.potionsUsed;
        }

        public void addGoldenAppleEaten() {
            ++this.goldenApplesEaten;
        }

        public void addRoundWon() {
            ++this.roundsWon;
        }

        public int getKills() {
            return this.kills;
        }

        public int getDeaths() {
            return this.deaths;
        }

        public double getDamageDealt() {
            return this.damageDealt;
        }

        public double getDamageTaken() {
            return this.damageTaken;
        }

        public int getArrowsShot() {
            return this.arrowsShot;
        }

        public int getArrowsHit() {
            return this.arrowsHit;
        }

        public int getPotionsUsed() {
            return this.potionsUsed;
        }

        public int getGoldenApplesEaten() {
            return this.goldenApplesEaten;
        }

        public int getRoundsWon() {
            return this.roundsWon;
        }

        public double getArrowAccuracy() {
            if (this.arrowsShot == 0) {
                return 0.0;
            }
            return (double)this.arrowsHit / (double)this.arrowsShot * 100.0;
        }

        public double getKDRatio() {
            if (this.deaths == 0) {
                return this.kills;
            }
            return (double)this.kills / (double)this.deaths;
        }
    }

    public static class RoundResult {
        private final int roundNumber;
        private final TeamSide winner;
        private final UUID mvpPlayerId;
        private final long durationMs;
        private final Map<UUID, Integer> kills;

        public RoundResult(int roundNumber, TeamSide winner, UUID mvpPlayerId, long durationMs, Map<UUID, Integer> kills) {
            this.roundNumber = roundNumber;
            this.winner = winner;
            this.mvpPlayerId = mvpPlayerId;
            this.durationMs = durationMs;
            this.kills = new HashMap<UUID, Integer>(kills);
        }

        public int getRoundNumber() {
            return this.roundNumber;
        }

        public TeamSide getWinner() {
            return this.winner;
        }

        public UUID getMvpPlayerId() {
            return this.mvpPlayerId;
        }

        public long getDurationMs() {
            return this.durationMs;
        }

        public Map<UUID, Integer> getKills() {
            return this.kills;
        }
    }
}

