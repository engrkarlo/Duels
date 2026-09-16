/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Color
 *  org.bukkit.FireworkEffect
 *  org.bukkit.FireworkEffect$Type
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Firework
 *  org.bukkit.entity.Player
 *  org.bukkit.event.HandlerList
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.FireworkMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.RegisteredListener
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.ScoreboardManager
 *  org.bukkit.scoreboard.Team
 */
package com.ultimateduels.duel;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.config.LanguageManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.DuelRequest;
import com.ultimateduels.duel.model.DuelRequestResult;
import com.ultimateduels.duel.model.MatchResult;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.duel.model.MatchType;
import com.ultimateduels.duel.model.WinCondition;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.kit.model.DuelKit;
import com.ultimateduels.listeners.WorldChangeListener;
import com.ultimateduels.player.PlayerDataManager;
import com.ultimateduels.player.PlayerStateManager;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.stats.PlayerStats;
import com.ultimateduels.stats.StatsManager;
import com.ultimateduels.tasks.DuelCountdownTask;
import com.ultimateduels.visuals.HealthDisplayManager;
import com.ultimateduels.world.WorldRestrictionManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class DuelManager {
    private final UltimateDuels plugin;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private final PlayerStateManager playerStateManager;
    private final PlayerDataManager playerDataManager;
    private final com.ultimateduels.scoreboard.ScoreboardManager scoreboardManager;
    private final Map<UUID, DuelMatch> activeMatches;
    private final Map<UUID, UUID> playerMatchMap;
    private final Map<UUID, UUID> spectatorMatchMap;
    private final Map<UUID, Set<UUID>> matchSpectators;
    private final Map<UUID, DuelRequest> pendingRequests;
    private final Map<UUID, BukkitTask> matchTasks;
    private final List<MatchResult> matchHistory;
    private static final int MAX_HISTORY_SIZE = 100;
    private int defaultRounds;
    private boolean defaultBestOf;
    private int countdownSeconds;
    private int endCelebrationSeconds;
    private int requestTimeoutSeconds;
    private boolean allowSpectators;
    private int maxSpectatorsPerMatch;

    public DuelManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerStateManager = plugin.getPlayerStateManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.scoreboardManager = plugin.getScoreboardManager();
        this.activeMatches = new ConcurrentHashMap<UUID, DuelMatch>();
        this.playerMatchMap = new ConcurrentHashMap<UUID, UUID>();
        this.spectatorMatchMap = new ConcurrentHashMap<UUID, UUID>();
        this.matchSpectators = new ConcurrentHashMap<UUID, Set<UUID>>();
        this.pendingRequests = new ConcurrentHashMap<UUID, DuelRequest>();
        this.matchTasks = new ConcurrentHashMap<UUID, BukkitTask>();
        this.matchHistory = Collections.synchronizedList(new ArrayList());
        this.loadSettings();
        this.startCleanupTask();
        plugin.getLogger().info("\u00a7a[DuelManager] Initialized successfully!");
    }

    private void loadSettings() {
        FileConfiguration config = this.plugin.getConfig();
        this.defaultRounds = config.getInt("duel.default-rounds", 1);
        this.defaultBestOf = config.getBoolean("duel.default-best-of", true);
        this.countdownSeconds = config.getInt("duel.countdown-seconds", 3);
        this.endCelebrationSeconds = config.getInt("duel.end-celebration-seconds", 5);
        this.requestTimeoutSeconds = config.getInt("duel.request-timeout-seconds", 60);
        this.allowSpectators = config.getBoolean("duel.allow-spectators", true);
        this.maxSpectatorsPerMatch = config.getInt("duel.max-spectators-per-match", 20);
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            this.cleanupExpiredRequests();
            this.validateActiveMatches();
        }, 600L, 600L);
    }

    @Nullable
    private DuelManager getDuelManager() {
        return this.plugin.getDuelManager();
    }

    public DuelRequestResult sendRequest(@Nonnull Player sender, @Nonnull Player target, @Nonnull String kitName, @Nullable String arenaName, int rounds, @Nonnull WinCondition winCondition) {
        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        LanguageManager lang = this.plugin.getLanguageManager();
        WorldRestrictionManager worldRestriction = this.plugin.getWorldRestrictionManager();
        if (worldRestriction != null && !worldRestriction.canStartDuel(sender)) {
            worldRestriction.sendDuelBlockedMessage(sender);
            return new DuelRequestResult(false, lang.getParsed(sender, "world-restriction.duel-blocked"));
        }
        if (sender.equals((Object)target)) {
            return new DuelRequestResult(false, lang.getParsed(sender, "duel.cannot-duel-self"));
        }
        if (this.isInMatch(senderUUID)) {
            return new DuelRequestResult(false, lang.getParsed(sender, "duel.already-in-match"));
        }
        if (this.isInMatch(targetUUID)) {
            String msg = lang.getParsed(sender, "duel.target-in-match", "player", target.getName());
            return new DuelRequestResult(false, msg);
        }
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager != null) {
            if (queueManager.isInQueue(senderUUID)) {
                return new DuelRequestResult(false, lang.getParsed(sender, "duel.sender-in-queue"));
            }
            if (queueManager.isInQueue(targetUUID)) {
                String msg = lang.getParsed(sender, "duel.target-in-queue", "player", target.getName());
                return new DuelRequestResult(false, msg);
            }
        }
        if (!this.kitManager.adminKitExists(kitName)) {
            String msg = lang.getParsed(sender, "duel.kit-not-found", "kit", kitName);
            return new DuelRequestResult(false, msg);
        }
        DuelRequest existingRequest = this.pendingRequests.get(senderUUID);
        if (existingRequest != null && existingRequest.getSenderUUID().equals(targetUUID)) {
            return this.acceptRequest(sender, target);
        }
        rounds = Math.max(1, Math.min(rounds, 20));
        boolean bestOf = winCondition == WinCondition.BEST_OF || winCondition == WinCondition.FIRST_TO_WIN;
        DuelRequest request = new DuelRequest(senderUUID, targetUUID, kitName, arenaName, rounds, bestOf, System.currentTimeMillis());
        request.setWinCondition(winCondition);
        this.pendingRequests.put(targetUUID, request);
        String winConditionDisplay = winCondition.getFormattedString(rounds);
        sender.sendMessage("");
        sender.sendMessage(lang.getComponent(sender, "duel.request.sent-header"));
        sender.sendMessage(lang.getComponent(sender, "duel.request.sent-target", "target", target.getName()));
        sender.sendMessage(lang.getComponent(sender, "duel.request.sent-kit", "kit", kitName));
        sender.sendMessage(lang.getComponent(sender, "duel.request.sent-rounds", "rounds", winConditionDisplay));
        sender.sendMessage(lang.getComponent(sender, "duel.request.sent-waiting"));
        sender.sendMessage("");
        sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        target.sendMessage("");
        target.sendMessage(lang.getComponent(target, "duel.request.received-header"));
        target.sendMessage(lang.getComponent(target, "duel.request.received-title"));
        target.sendMessage("");
        target.sendMessage(lang.getComponent(target, "duel.request.received-from", "sender", sender.getName()));
        target.sendMessage(lang.getComponent(target, "duel.request.received-kit", "kit", kitName));
        target.sendMessage(lang.getComponent(target, "duel.request.received-rounds", "rounds", winConditionDisplay));
        target.sendMessage("");
        Component acceptBtn = ((TextComponent)((TextComponent)((TextComponent)Component.text((String)"  [ACCEPT]  ").color((TextColor)NamedTextColor.GREEN)).decorate(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand((String)"/accept"))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)"Click to accept the duel request!").color((TextColor)NamedTextColor.GREEN)));
        Component denyBtn = ((TextComponent)((TextComponent)((TextComponent)Component.text((String)"  [DENY]  ").color((TextColor)NamedTextColor.RED)).decorate(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand((String)"/deny"))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)"Click to deny the duel request!").color((TextColor)NamedTextColor.RED)));
        target.sendMessage(acceptBtn.append(denyBtn));
        target.sendMessage(lang.getComponent(target, "duel.request.received-footer"));
        target.sendMessage("");
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        String successMsg = lang.getParsed(sender, "duel.request.sent-success").replace("{target}", target.getName());
        return new DuelRequestResult(true, successMsg);
    }

    public DuelRequestResult sendRequest(@Nonnull Player sender, @Nonnull Player target, @Nonnull String kitName, @Nullable String arenaName, int rounds, boolean bestOf) {
        WinCondition condition = bestOf ? WinCondition.BEST_OF : WinCondition.PLAY_ALL;
        return this.sendRequest(sender, target, kitName, arenaName, rounds, condition);
    }

    public DuelRequestResult acceptRequest(@Nonnull Player accepter, @Nullable Player sender) {
        UUID accepterUUID = accepter.getUniqueId();
        LanguageManager lang = this.plugin.getLanguageManager();
        DuelRequest request = this.pendingRequests.remove(accepterUUID);
        if (request == null) {
            return new DuelRequestResult(false, lang.getParsed(accepter, "duel.accept.no-request"));
        }
        if (request.isExpired((long)this.requestTimeoutSeconds * 1000L)) {
            return new DuelRequestResult(false, lang.getParsed(accepter, "duel.accept.expired"));
        }
        Player requester = Bukkit.getPlayer((UUID)request.getSenderUUID());
        if (requester == null || !requester.isOnline()) {
            return new DuelRequestResult(false, lang.getParsed(accepter, "duel.accept.sender-offline"));
        }
        if (this.isInMatch(accepterUUID) || this.isInMatch(request.getSenderUUID())) {
            return new DuelRequestResult(false, lang.getParsed(accepter, "duel.accept.already-in-match"));
        }
        DuelArena arena = this.arenaManager.allocateArena(request.getArenaName(), request.getKitName());
        if (arena == null) {
            return new DuelRequestResult(false, lang.getParsed(accepter, "duel.accept.no-arenas"));
        }
        accepter.sendMessage(lang.getComponent(accepter, "duel.accept.accepted-you", "sender", requester.getName()));
        requester.sendMessage(lang.getComponent(requester, "duel.accept.accepted-them", "target", accepter.getName()));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            WinCondition wc = request.getWinCondition() != null ? request.getWinCondition() : (request.isBestOf() ? WinCondition.BEST_OF : WinCondition.PLAY_ALL);
            this.startMatch(requester, accepter, request.getKitName(), arena, request.getRounds(), wc);
        }, 20L);
        return new DuelRequestResult(true, lang.getParsed(accepter, "duel.accept.success"));
    }

    public DuelRequestResult denyRequest(@Nonnull Player denier) {
        UUID denierUUID = denier.getUniqueId();
        LanguageManager lang = this.plugin.getLanguageManager();
        DuelRequest request = this.pendingRequests.remove(denierUUID);
        if (request == null) {
            return new DuelRequestResult(false, lang.getParsed(denier, "duel.deny.no-request"));
        }
        Player requester = Bukkit.getPlayer((UUID)request.getSenderUUID());
        if (requester != null && requester.isOnline()) {
            String deniedThem = lang.getParsed(requester, "duel.deny.denied-them", "player", denier.getName());
            requester.sendMessage(deniedThem);
            requester.playSound(requester.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
        String deniedYou = lang.getParsed(denier, "duel.deny.denied-you");
        denier.sendMessage(deniedYou);
        return new DuelRequestResult(true, deniedYou);
    }

    public void startMatch(@Nonnull Player player1, @Nonnull Player player2, @Nonnull String kitName, @Nonnull DuelArena arena, int rounds, @Nonnull WinCondition winCondition) {
        UUID matchId = UUID.randomUUID();
        List<DuelParticipant> team1 = Collections.singletonList(new DuelParticipant(player1.getUniqueId(), player1.getName(), 1));
        List<DuelParticipant> team2 = Collections.singletonList(new DuelParticipant(player2.getUniqueId(), player2.getName(), 2));
        DuelMatch match = new DuelMatch(matchId, MatchType.DUEL_1V1, team1, team2, kitName, arena, rounds, winCondition);
        this.activeMatches.put(matchId, match);
        this.playerMatchMap.put(player1.getUniqueId(), matchId);
        this.playerMatchMap.put(player2.getUniqueId(), matchId);
        this.matchSpectators.put(matchId, ConcurrentHashMap.newKeySet());
        this.initializeMatch(match);
    }

    public void startMatch(@Nonnull Player player1, @Nonnull Player player2, @Nonnull String kitName, @Nonnull DuelArena arena, int rounds, boolean bestOf) {
        this.startMatch(player1, player2, kitName, arena, rounds, bestOf ? WinCondition.BEST_OF : WinCondition.PLAY_ALL);
    }

    public void startTeamMatch(@Nonnull List<UUID> team1Members, @Nonnull List<UUID> team2Members, @Nonnull String kitName, @Nonnull DuelArena arena, int rounds, @Nonnull WinCondition winCondition, @Nonnull String matchTypeStr) {
        UUID matchId = UUID.randomUUID();
        if (team1Members.isEmpty() || team2Members.isEmpty()) {
            this.plugin.getLogger().warning("[DuelManager] startTeamMatch called with empty team!");
            if (this.arenaManager != null) {
                this.arenaManager.releaseArena(arena.getName(), matchId);
            }
            return;
        }
        int maxTeamSize = Math.max(team1Members.size(), team2Members.size());
        MatchType matchType = switch (maxTeamSize) {
            case 1 -> MatchType.DUEL_1V1;
            case 2 -> MatchType.DUEL_2V2;
            case 3 -> MatchType.DUEL_3V3;
            default -> MatchType.PARTY;
        };
        ArrayList<DuelParticipant> team1 = new ArrayList<DuelParticipant>();
        for (UUID uUID : team1Members) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null || !player.isOnline()) continue;
            team1.add(new DuelParticipant(uUID, player.getName(), 1));
        }
        ArrayList<DuelParticipant> team2 = new ArrayList<DuelParticipant>();
        for (UUID uuid : team2Members) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            team2.add(new DuelParticipant(uuid, player.getName(), 2));
        }
        if (team1.isEmpty() || team2.isEmpty()) {
            this.plugin.getLogger().warning("[DuelManager] A team is empty after filtering offline players!");
            if (this.arenaManager != null) {
                this.arenaManager.releaseArena(arena.getName(), matchId);
            }
            for (UUID uuid : team1Members) {
                Player player = Bukkit.getPlayer((UUID)uuid);
                if (player == null) continue;
                player.sendMessage("\u00a7cMatch cancelled \u2014 a player went offline!");
            }
            for (UUID uuid : team2Members) {
                Player player = Bukkit.getPlayer((UUID)uuid);
                if (player == null) continue;
                player.sendMessage("\u00a7cMatch cancelled \u2014 a player went offline!");
            }
            return;
        }
        DuelMatch duelMatch = new DuelMatch(matchId, matchType, team1, team2, kitName, arena, rounds, winCondition);
        this.activeMatches.put(matchId, duelMatch);
        this.matchSpectators.put(matchId, ConcurrentHashMap.newKeySet());
        for (DuelParticipant duelParticipant : team1) {
            this.playerMatchMap.put(duelParticipant.getUuid(), matchId);
        }
        for (DuelParticipant duelParticipant : team2) {
            this.playerMatchMap.put(duelParticipant.getUuid(), matchId);
        }
        this.initializeMatch(duelMatch);
    }

    public void startTeamMatch(@Nonnull List<UUID> team1Members, @Nonnull List<UUID> team2Members, @Nonnull String kitName, @Nonnull DuelArena arena, int rounds, boolean bestOf, @Nonnull String matchTypeStr) {
        this.startTeamMatch(team1Members, team2Members, kitName, arena, rounds, bestOf ? WinCondition.BEST_OF : WinCondition.PLAY_ALL, matchTypeStr);
    }

    public void startPartyFFAMatch(@Nonnull List<UUID> playerUUIDs, @Nonnull String kitName, @Nonnull DuelArena arena) {
        Player player;
        UUID matchId = UUID.randomUUID();
        ArrayList<DuelParticipant> participants = new ArrayList<DuelParticipant>();
        for (UUID uuid : playerUUIDs) {
            player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) continue;
            participants.add(new DuelParticipant(uuid, player.getName(), 0));
        }
        if (participants.size() < 2) {
            for (UUID uuid : playerUUIDs) {
                player = Bukkit.getPlayer((UUID)uuid);
                if (player == null) continue;
                player.sendMessage("\u00a7cNot enough players for Party FFA!");
            }
            if (this.arenaManager != null) {
                this.arenaManager.releaseArena(arena.getName(), matchId);
            }
            return;
        }
        DuelMatch match = new DuelMatch(matchId, MatchType.PARTY_FFA, participants, new ArrayList<DuelParticipant>(), kitName, arena, 1, WinCondition.PLAY_ALL);
        this.activeMatches.put(matchId, match);
        this.matchSpectators.put(matchId, ConcurrentHashMap.newKeySet());
        for (DuelParticipant p : participants) {
            this.playerMatchMap.put(p.getUuid(), matchId);
        }
        this.initializePartyFFAMatch(match);
    }

    private void initializePartyFFAMatch(@Nonnull DuelMatch match) {
        match.setState(MatchState.STARTING);
        match.setStartTime(System.currentTimeMillis());
        DuelKit kit = this.kitManager.getAdminKit(match.getKitName());
        if (kit == null) {
            this.plugin.getLogger().warning("Kit not found: " + match.getKitName());
            this.cancelMatch(match, "Kit not found!");
            return;
        }
        Location spawn1 = match.getArena().getSpawnPoint1();
        Location spawn2 = match.getArena().getSpawnPoint2();
        if (spawn1 == null || spawn2 == null) {
            this.plugin.getLogger().severe("Arena " + match.getArena().getName() + " missing spawn points!");
            this.cancelMatch(match, "Arena spawn points not set!");
            return;
        }
        List<DuelParticipant> allParticipants = match.getAllParticipants();
        ArrayList<DuelParticipant> shuffled = new ArrayList<DuelParticipant>(allParticipants);
        Collections.shuffle(shuffled);
        int spawn1Count = 0;
        int spawn2Count = 0;
        for (int i = 0; i < shuffled.size(); ++i) {
            int offsetIndex;
            Location baseSpawn;
            DuelParticipant participant = (DuelParticipant)shuffled.get(i);
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) {
                this.plugin.getLogger().warning("Player is NULL: " + participant.getPlayerName());
                this.cancelMatch(match, "A player went offline!");
                return;
            }
            QueueManager queueManager = this.plugin.getQueueManager();
            if (queueManager != null) {
                queueManager.removeFromAllQueues(player.getUniqueId());
            }
            this.playerStateManager.saveState(player);
            this.playerStateManager.clearPlayer(player);
            if (i % 2 == 0) {
                baseSpawn = spawn1;
                offsetIndex = spawn1Count++;
            } else {
                baseSpawn = spawn2;
                offsetIndex = spawn2Count++;
            }
            Location spawn = this.getOffsetSpawn(baseSpawn, offsetIndex);
            boolean teleported = player.teleport(spawn);
            if (!teleported) {
                this.plugin.getLogger().severe("TELEPORT FAILED for " + player.getName());
                this.cancelMatch(match, "Failed to teleport player!");
                return;
            }
            this.kitManager.applyKit(player, kit, true);
            participant.setAlive(true);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            if (this.scoreboardManager != null) {
                this.scoreboardManager.refreshScoreboardType(player);
            }
            this.plugin.getLogger().fine("[DuelManager] FFA player " + player.getName() + " spawned at " + (i % 2 == 0 ? "spawn1" : "spawn2") + " offset " + offsetIndex);
        }
        match.getArena().incrementTotalMatches();
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            this.createHealthDisplaysForMatch(healthDisplayManager, match);
        }
        this.startCountdown(match);
    }

    public void forfeitMatch(@Nonnull Player player) {
        UUID playerId = player.getUniqueId();
        DuelMatch match = this.getMatch(playerId);
        LanguageManager lang = this.plugin.getLanguageManager();
        if (match == null) {
            player.sendMessage(lang.getComponent(player, "duel.forfeit.not-in-match"));
            return;
        }
        if (match.getState() != MatchState.IN_PROGRESS && match.getState() != MatchState.STARTING) {
            player.sendMessage(lang.getComponent(player, "duel.forfeit.cannot-forfeit"));
            return;
        }
        DuelParticipant participant = match.getParticipant(playerId);
        if (participant == null) {
            return;
        }
        participant.setAlive(false);
        player.setNoDamageTicks(0);
        player.setInvulnerable(false);
        player.setFireTicks(0);
        int losingTeam = participant.getTeamId();
        int winningTeam = losingTeam == 1 ? 2 : 1;
        String opponentName = "Unknown";
        List<DuelParticipant> opponents = match.getTeamParticipants(winningTeam);
        if (!opponents.isEmpty()) {
            opponentName = opponents.get(0).getPlayerName();
        }
        for (DuelParticipant p : match.getAllParticipants()) {
            Player pOnline = Bukkit.getPlayer((UUID)p.getUuid());
            if (pOnline == null) continue;
            if (pOnline.getUniqueId().equals(playerId)) {
                pOnline.sendMessage(lang.getComponent(pOnline, "duel.forfeit.self"));
                continue;
            }
            pOnline.sendMessage(lang.getComponent(pOnline, "duel.forfeit.opponent", "player", player.getName()));
        }
        for (UUID spectatorUUID : this.getMatchSpectators(match.getMatchId())) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorUUID);
            if (spectator == null) continue;
            spectator.sendMessage(lang.getComponent(spectator, "duel.forfeit.broadcast", "player", player.getName(), "opponent", opponentName));
        }
        this.handleMatchEnd(match, winningTeam);
        if (this.plugin.getStatsManager() != null) {
            this.plugin.getStatsManager().recordMatch(playerId, match.getKitName(), false, 0, 1);
        }
    }

    private void setupMatchTeams(@Nonnull DuelMatch match) {
        if (match.getMatchType() == MatchType.DUEL_1V1 || match.getMatchType() == MatchType.PARTY_FFA) {
            return;
        }
        boolean friendlyFireEnabled = this.plugin.getConfig().getBoolean("party.friendly-fire", false);
        String matchShort = match.getMatchId().toString().substring(0, 8);
        String team1Name = "ud_t1_" + matchShort;
        String team2Name = "ud_t2_" + matchShort;
        for (DuelParticipant viewerParticipant : match.getAllParticipants()) {
            Team existingT2;
            Scoreboard board;
            Player viewer = Bukkit.getPlayer((UUID)viewerParticipant.getUuid());
            if (viewer == null || !viewer.isOnline() || (board = viewer.getScoreboard()) == null) continue;
            Team existingT1 = board.getTeam(team1Name);
            if (existingT1 != null) {
                existingT1.unregister();
            }
            if ((existingT2 = board.getTeam(team2Name)) != null) {
                existingT2.unregister();
            }
            Team t1 = board.registerNewTeam(team1Name);
            Team t2 = board.registerNewTeam(team2Name);
            t1.setAllowFriendlyFire(friendlyFireEnabled);
            t2.setAllowFriendlyFire(friendlyFireEnabled);
            t1.setColor(ChatColor.BLUE);
            t2.setColor(ChatColor.RED);
            t1.setPrefix("\u00a79");
            t2.setPrefix("\u00a7c");
            for (DuelParticipant p : match.getTeam1()) {
                Player teammate = Bukkit.getPlayer((UUID)p.getUuid());
                if (teammate == null) continue;
                t1.addEntry(teammate.getName());
            }
            for (DuelParticipant p : match.getTeam2()) {
                Player enemy = Bukkit.getPlayer((UUID)p.getUuid());
                if (enemy == null) continue;
                t2.addEntry(enemy.getName());
            }
        }
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm != null) {
            Team mainT2;
            Scoreboard mainBoard = sm.getMainScoreboard();
            Team mainT1 = mainBoard.getTeam(team1Name);
            if (mainT1 != null) {
                mainT1.unregister();
            }
            if ((mainT2 = mainBoard.getTeam(team2Name)) != null) {
                mainT2.unregister();
            }
            mainT1 = mainBoard.registerNewTeam(team1Name);
            mainT2 = mainBoard.registerNewTeam(team2Name);
            mainT1.setAllowFriendlyFire(friendlyFireEnabled);
            mainT2.setAllowFriendlyFire(friendlyFireEnabled);
            mainT1.setColor(ChatColor.BLUE);
            mainT2.setColor(ChatColor.RED);
            mainT1.setPrefix("\u00a79");
            mainT2.setPrefix("\u00a7c");
            for (DuelParticipant p : match.getTeam1()) {
                Player teammate = Bukkit.getPlayer((UUID)p.getUuid());
                if (teammate == null) continue;
                mainT1.addEntry(teammate.getName());
            }
            for (DuelParticipant p : match.getTeam2()) {
                Player enemy = Bukkit.getPlayer((UUID)p.getUuid());
                if (enemy == null) continue;
                mainT2.addEntry(enemy.getName());
            }
        }
        this.plugin.debug("[DuelManager] Created team colors: \u00a79BLUE (" + match.getTeam1().size() + ") vs \u00a7cRED (" + match.getTeam2().size() + ")");
    }

    public void reapplyMatchTeams(@Nonnull Player player) {
        Team existingT2;
        DuelMatch match = this.getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        if (match.getMatchType() == MatchType.DUEL_1V1 || match.getMatchType() == MatchType.PARTY_FFA) {
            return;
        }
        boolean friendlyFireEnabled = this.plugin.getConfig().getBoolean("party.friendly-fire", false);
        String matchShort = match.getMatchId().toString().substring(0, 8);
        String team1Name = "ud_t1_" + matchShort;
        String team2Name = "ud_t2_" + matchShort;
        Scoreboard board = player.getScoreboard();
        if (board == null) {
            return;
        }
        Team existingT1 = board.getTeam(team1Name);
        if (existingT1 != null) {
            existingT1.unregister();
        }
        if ((existingT2 = board.getTeam(team2Name)) != null) {
            existingT2.unregister();
        }
        Team t1 = board.registerNewTeam(team1Name);
        Team t2 = board.registerNewTeam(team2Name);
        t1.setAllowFriendlyFire(friendlyFireEnabled);
        t2.setAllowFriendlyFire(friendlyFireEnabled);
        t1.setColor(ChatColor.BLUE);
        t2.setColor(ChatColor.RED);
        t1.setPrefix("\u00a79");
        t2.setPrefix("\u00a7c");
        for (DuelParticipant p : match.getTeam1()) {
            Player teammate = Bukkit.getPlayer((UUID)p.getUuid());
            if (teammate == null) continue;
            t1.addEntry(teammate.getName());
        }
        for (DuelParticipant p : match.getTeam2()) {
            Player enemy = Bukkit.getPlayer((UUID)p.getUuid());
            if (enemy == null) continue;
            t2.addEntry(enemy.getName());
        }
    }

    private void removeMatchTeams(@Nonnull DuelMatch match) {
        Team mainT2;
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        if (sm == null) {
            return;
        }
        String matchShort = match.getMatchId().toString().substring(0, 8);
        String team1Name = "ud_t1_" + matchShort;
        String team2Name = "ud_t2_" + matchShort;
        Scoreboard mainBoard = sm.getMainScoreboard();
        Team mainT1 = mainBoard.getTeam(team1Name);
        if (mainT1 != null) {
            mainT1.unregister();
        }
        if ((mainT2 = mainBoard.getTeam(team2Name)) != null) {
            mainT2.unregister();
        }
        for (DuelParticipant participant : match.getAllParticipants()) {
            Team t2;
            Scoreboard board;
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null || !player.isOnline() || (board = player.getScoreboard()) == null) continue;
            Team t1 = board.getTeam(team1Name);
            if (t1 != null) {
                t1.unregister();
            }
            if ((t2 = board.getTeam(team2Name)) == null) continue;
            t2.unregister();
        }
        this.plugin.debug("[DuelManager] Removed scoreboard teams for match " + matchShort);
    }

    private void initializeMatch(@Nonnull DuelMatch match) {
        match.setState(MatchState.STARTING);
        match.setStartTime(System.currentTimeMillis());
        for (DuelParticipant participant : match.getAllParticipants()) {
            this.plugin.getLobbyManager().forceRemoveFromLobbyTracking(participant.getUuid());
        }
        DuelKit kit = this.kitManager.getAdminKit(match.getKitName());
        if (kit == null) {
            this.plugin.getLogger().warning("[DuelManager] Kit not found: " + match.getKitName());
            this.cancelMatch(match, "Kit not found!");
            return;
        }
        Location spawn1 = match.getArena().getSpawnPoint1();
        Location spawn2 = match.getArena().getSpawnPoint2();
        if (spawn1 == null || spawn2 == null) {
            this.plugin.getLogger().severe("[DuelManager] Arena " + match.getArena().getName() + " is missing spawn points! spawn1=" + String.valueOf(spawn1) + " spawn2=" + String.valueOf(spawn2));
            this.cancelMatch(match, "Arena spawn points not configured!");
            return;
        }
        int team1Index = 0;
        int team2Index = 0;
        List<DuelParticipant> allParticipants = match.getAllParticipants();
        for (DuelParticipant participant : allParticipants) {
            Location spawn;
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null || !player.isOnline()) {
                this.plugin.getLogger().warning("[DuelManager] Player offline during init: " + participant.getPlayerName());
                this.cancelMatch(match, participant.getPlayerName() + " went offline!");
                return;
            }
            QueueManager queueManager = this.plugin.getQueueManager();
            if (queueManager != null) {
                queueManager.removeFromAllQueues(player.getUniqueId());
            }
            this.plugin.getLobbyManager().forceRemoveFromLobbyTracking(player.getUniqueId());
            this.playerStateManager.saveState(player);
            this.playerStateManager.clearPlayer(player);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            player.setFireTicks(0);
            player.setFallDistance(0.0f);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            if (participant.getTeamId() == 1) {
                spawn = this.getOffsetSpawn(spawn1, team1Index);
                ++team1Index;
            } else {
                spawn = this.getOffsetSpawn(spawn2, team2Index);
                ++team2Index;
            }
            boolean teleported = player.teleport(spawn);
            if (!teleported) {
                this.plugin.getLogger().severe("[DuelManager] Teleport failed for " + player.getName());
                this.cancelMatch(match, "Failed to teleport " + player.getName() + "!");
                return;
            }
            this.kitManager.applyKit(player, kit, true);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            participant.setAlive(true);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            if (this.scoreboardManager != null) {
                this.scoreboardManager.refreshScoreboardType(player);
            }
            this.plugin.getLogger().fine("[DuelManager] Initialized " + player.getName() + " team=" + participant.getTeamId() + " spawn=" + this.formatLocation(spawn));
        }
        match.getArena().incrementTotalMatches();
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            this.createHealthDisplaysForMatch(healthDisplayManager, match);
        }
        this.setupMatchTeams(match);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (DuelParticipant participant : match.getAllParticipants()) {
                Player p = Bukkit.getPlayer((UUID)participant.getUuid());
                if (p == null || !p.isOnline()) continue;
                this.reapplyMatchTeams(p);
            }
        }, 5L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (DuelParticipant participant : match.getAllParticipants()) {
                Player p = Bukkit.getPlayer((UUID)participant.getUuid());
                if (p == null || !p.isOnline()) continue;
                this.reapplyMatchTeams(p);
            }
        }, 20L);
        this.startCountdown(match);
    }

    private Location getOffsetSpawn(@Nonnull Location base, int index) {
        if (index == 0) {
            return base.clone();
        }
        Location offset = base.clone();
        int side = index % 2 == 1 ? 1 : -1;
        int amount = (index + 1) / 2;
        double yaw = Math.toRadians(base.getYaw());
        double rightX = Math.cos(yaw) * (double)amount * (double)side;
        double rightZ = Math.sin(yaw) * (double)amount * (double)side;
        offset.add(rightX, 0.0, rightZ);
        return offset;
    }

    private String formatLocation(@Nonnull Location loc) {
        return String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
    }

    private void createHealthDisplaysForMatch(@Nonnull HealthDisplayManager healthDisplayManager, @Nonnull DuelMatch match) {
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            healthDisplayManager.createDisplay(player);
        }
    }

    private void removeHealthDisplaysForMatch(@Nonnull HealthDisplayManager healthDisplayManager, @Nonnull DuelMatch match) {
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            healthDisplayManager.removeDisplay(player);
        }
    }

    private void startCountdown(@Nonnull DuelMatch match) {
        DuelCountdownTask countdownTask = new DuelCountdownTask(this.plugin, this, match, this.countdownSeconds);
        BukkitTask task = countdownTask.runTaskTimer((Plugin)this.plugin, 0L, 20L);
        this.matchTasks.put(match.getMatchId(), task);
    }

    public void onCountdownComplete(@Nonnull DuelMatch match) {
        match.setState(MatchState.IN_PROGRESS);
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null || !participant.isAlive()) continue;
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            int currentRound = match.getCurrentRound();
            int totalRounds = match.getTotalRounds();
            if (totalRounds > 1) {
                player.sendMessage("\u00a7a\u00a7lROUND " + currentRound + " \u00a7a- \u00a7eFIGHT!");
                continue;
            }
            player.sendMessage("\u00a7a\u00a7lFIGHT!");
        }
        this.plugin.getLogger().fine("Match " + String.valueOf(match.getMatchId()) + " Round " + match.getCurrentRound() + " started");
    }

    public void handleDeath(@Nonnull Player victim, @Nullable Player killer) {
        DuelParticipant killerParticipant;
        UUID victimUUID = victim.getUniqueId();
        UUID matchId = this.playerMatchMap.get(victimUUID);
        if (matchId == null) {
            return;
        }
        DuelMatch match = this.activeMatches.get(matchId);
        if (match == null || match.getState() != MatchState.IN_PROGRESS) {
            return;
        }
        DuelParticipant victimParticipant = match.getParticipant(victimUUID);
        if (victimParticipant == null || !victimParticipant.isAlive()) {
            return;
        }
        victim.setHealth(victim.getMaxHealth());
        victim.setFireTicks(0);
        victim.setFallDistance(0.0f);
        victimParticipant.setAlive(false);
        victimParticipant.incrementDeaths();
        if (killer != null && (killerParticipant = match.getParticipant(killer.getUniqueId())) != null) {
            killerParticipant.incrementKills();
        }
        if (match.getMatchType() == MatchType.PARTY_FFA) {
            this.handlePartyFFADeath(victim, killer, match, victimParticipant);
            return;
        }
        int victimTeam = victimParticipant.getTeamId();
        int otherTeam = victimTeam == 1 ? 2 : 1;
        boolean teamEliminated = match.getTeamParticipants(victimTeam).stream().noneMatch(DuelParticipant::isAlive);
        if (teamEliminated) {
            this.handleRoundEnd(match, otherTeam, victimTeam);
        } else {
            this.setPlayerSpectatorForRound(victim, match);
        }
    }

    private void handlePartyFFADeath(@Nonnull Player victim, @Nullable Player killer, @Nonnull DuelMatch match, @Nonnull DuelParticipant victimParticipant) {
        List<DuelParticipant> alive;
        String deathMessage;
        UUID victimUUID = victim.getUniqueId();
        victim.setGameMode(GameMode.SPECTATOR);
        Location spectatorSpawn = match.getArena().getSpectatorSpawn();
        if (spectatorSpawn == null) {
            spectatorSpawn = match.getArena().getCenter();
        }
        if (spectatorSpawn != null) {
            victim.teleport(spectatorSpawn);
        }
        victim.sendMessage("");
        victim.sendMessage("\u00a7c\u00a7lYOU DIED!");
        victim.sendMessage("\u00a77You are now spectating the match.");
        victim.sendMessage("\u00a77Use \u00a7e/unspectate \u00a77to return to lobby.");
        victim.sendMessage("");
        Title title = Title.title((Component)Component.text((String)"\u00a7c\u00a7lYOU DIED"), (Component)Component.text((String)"\u00a77Now spectating..."), (Title.Times)Title.Times.times((Duration)Duration.ZERO, (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
        victim.showTitle(title);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        if (killer != null) {
            DuelParticipant killerParticipant = match.getParticipant(killer.getUniqueId());
            int kills = killerParticipant != null ? killerParticipant.getKills() : 0;
            deathMessage = "\u00a7c" + victim.getName() + " \u00a77was killed by \u00a7a" + killer.getName() + " \u00a77(\u00a7e" + kills + " kills\u00a77)";
        } else {
            deathMessage = "\u00a7c" + victim.getName() + " \u00a77died";
        }
        for (DuelParticipant p : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)p.getUuid());
            if (player == null) continue;
            player.sendMessage(deathMessage);
        }
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.removeDisplay(victim);
        }
        if ((alive = match.getAliveParticipants()).size() == 1) {
            this.handlePartyFFAEnd(match, alive.get(0));
        } else if (alive.isEmpty()) {
            this.handlePartyFFAEnd(match, null);
        }
    }

    private void handlePartyFFAEnd(@Nonnull DuelMatch match, @Nullable DuelParticipant winner) {
        match.setState(MatchState.ENDING);
        match.setEndTime(System.currentTimeMillis());
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            this.removeHealthDisplaysForMatch(healthDisplayManager, match);
        }
        for (DuelParticipant p : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)p.getUuid());
            if (player == null) continue;
            player.sendMessage("");
            if (winner != null && p.getUuid().equals(winner.getUuid())) {
                player.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
                player.sendMessage("");
                player.sendMessage("         \u00a76\u00a7lVICTORY ROYALE!");
                player.sendMessage("");
                player.sendMessage("   \u00a77You are the last one standing!");
                player.sendMessage("");
                player.sendMessage("         \u00a7eKills: \u00a7a" + winner.getKills());
                player.sendMessage("         \u00a7eDeaths: \u00a7c" + winner.getDeaths());
                player.sendMessage("");
                player.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                this.spawnVictoryFirework(player.getLocation());
                if (this.plugin.getStatsManager() != null) {
                    this.plugin.getStatsManager().recordMatch(p.getUuid(), match.getKitName(), true, p.getKills(), p.getDeaths());
                }
            } else if (winner != null) {
                player.sendMessage("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
                player.sendMessage("");
                player.sendMessage("         \u00a7e\u00a7lPARTY FFA ENDED");
                player.sendMessage("");
                player.sendMessage("   \u00a77Winner: \u00a7a" + winner.getPlayerName());
                player.sendMessage("");
                player.sendMessage("   \u00a77Your Stats:");
                player.sendMessage("   \u00a7eKills: \u00a7a" + p.getKills());
                player.sendMessage("   \u00a7eDeaths: \u00a7c" + p.getDeaths());
                player.sendMessage("");
                player.sendMessage("\u00a7c\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
                if (this.plugin.getStatsManager() != null) {
                    this.plugin.getStatsManager().recordMatch(p.getUuid(), match.getKitName(), false, p.getKills(), p.getDeaths());
                }
            } else {
                player.sendMessage("\u00a7e\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
                player.sendMessage("");
                player.sendMessage("         \u00a7e\u00a7lPARTY FFA - DRAW!");
                player.sendMessage("");
                player.sendMessage("\u00a7e\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
            }
            player.sendMessage("");
            player.sendMessage("\u00a77Returning to lobby in \u00a7e5 \u00a77seconds...");
            player.sendMessage("");
        }
        if (winner != null) {
            match.setWinningTeam(1);
        }
        this.addToHistory(match);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.cleanupMatch(match), 100L);
    }

    private void handleRoundEnd(@Nonnull DuelMatch match, int winningTeam, int losingTeam) {
        Player player;
        match.setState(MatchState.ROUND_ENDING);
        match.addScore(winningTeam, 1);
        for (DuelParticipant participant : match.getTeamParticipants(winningTeam)) {
            player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            player.setHealth(player.getMaxHealth());
            player.setFireTicks(0);
            player.setFallDistance(0.0f);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
        }
        for (DuelParticipant participant : match.getTeamParticipants(losingTeam)) {
            player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            Location deathLoc = player.getLocation().clone();
            Player finalPlayer = player;
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
            player.setHealth(player.getMaxHealth());
            player.setFireTicks(0);
            player.setFallDistance(0.0f);
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (!finalPlayer.isOnline()) {
                    return;
                }
                finalPlayer.setGameMode(GameMode.SPECTATOR);
                Location specView = match.getArena().getSpectatorSpawn();
                if (specView == null) {
                    specView = match.getArena().getCenter();
                }
                if (specView == null) {
                    specView = deathLoc.clone().add(0.0, 5.0, 0.0);
                }
                finalPlayer.teleport(specView);
            }, 10L);
        }
        boolean matchOver = this.isMatchOver(match);
        if (matchOver) {
            int matchWinner = match.getTeamScore(1) >= match.getTeamScore(2) ? 1 : 2;
            this.handleMatchEnd(match, matchWinner);
        } else {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.handleMultiRoundContinue(match, winningTeam, losingTeam), 20L);
        }
    }

    private void freezeAllPlayers(@Nonnull DuelMatch match) {
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            player.setWalkSpeed(0.0f);
            player.setFlySpeed(0.0f);
        }
    }

    private void healAllPlayers(@Nonnull DuelMatch match) {
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setFireTicks(0);
            player.setFallDistance(0.0f);
        }
    }

    private void handleMultiRoundContinue(@Nonnull DuelMatch match, int winningTeam, int losingTeam) {
        int team1Wins = match.getTeamScore(1);
        int team2Wins = match.getTeamScore(2);
        String winsDisplay = team1Wins + " - " + team2Wins;
        int currentRound = match.getCurrentRound();
        LanguageManager lang = this.plugin.getLanguageManager();
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            int playerTeam = participant.getTeamId();
            int playerWins = match.getTeamScore(playerTeam);
            int opponentWins = match.getTeamScore(playerTeam == 1 ? 2 : 1);
            if (playerTeam == winningTeam) {
                player.sendMessage("\u00a7a\u00a7lROUND WON! \u00a77Wins: \u00a7aYou " + playerWins + " \u00a77- \u00a7cThem " + opponentWins);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                title = Title.title((Component)Component.text((String)"\u00a7a\u00a7lROUND WON!"), (Component)Component.text((String)("\u00a77Wins: \u00a7aYou " + playerWins + " \u00a77- \u00a7cThem " + opponentWins)), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(100L), (Duration)Duration.ofMillis(1500L), (Duration)Duration.ofMillis(200L)));
                player.showTitle(title);
            } else {
                player.sendMessage("\u00a7c\u00a7lROUND LOST! \u00a77Wins: \u00a7aYou " + playerWins + " \u00a77- \u00a7cThem " + opponentWins);
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0f, 1.0f);
                title = Title.title((Component)Component.text((String)"\u00a7c\u00a7lROUND LOST!"), (Component)Component.text((String)("\u00a77Wins: \u00a7aYou " + playerWins + " \u00a77- \u00a7cThem " + opponentWins)), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(100L), (Duration)Duration.ofMillis(1500L), (Duration)Duration.ofMillis(200L)));
                player.showTitle(title);
            }
            if (this.scoreboardManager == null) continue;
            this.scoreboardManager.updateScoreboard(player);
        }
        for (UUID spectatorUUID : this.getMatchSpectators(match.getMatchId())) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorUUID);
            if (spectator == null) continue;
            spectator.sendMessage("\u00a77Round " + currentRound + " ended! Wins: \u00a7e" + winsDisplay);
        }
        match.incrementRound();
        int nextRound = match.getCurrentRound();
        String arenaName = match.getArena().getName();
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            player.sendMessage("\u00a7e\u00a7lPreparing arena for Round " + nextRound + "...");
        }
        ((CompletableFuture)this.plugin.getSchematicManager().restoreArena(arenaName).thenAccept(success -> Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            for (DuelParticipant participant : match.getAllParticipants()) {
                Player player = Bukkit.getPlayer((UUID)participant.getUuid());
                if (player == null) continue;
                player.sendMessage("\u00a7a\u00a7lArena ready! Starting Round " + nextRound + "...");
            }
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.resetPlayersForNextRound(match), 40L);
        }))).exceptionally(throwable -> {
            this.plugin.getLogger().warning("Exception during arena regen: " + throwable.getMessage());
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                for (DuelParticipant participant : match.getAllParticipants()) {
                    Player player = Bukkit.getPlayer((UUID)participant.getUuid());
                    if (player == null) continue;
                    player.sendMessage("\u00a7e\u00a7lArena regeneration skipped - continuing match...");
                }
                this.resetPlayersForNextRound(match);
            });
            return null;
        });
    }

    private void resetPlayersForNextRound(@Nonnull DuelMatch match) {
        match.setState(MatchState.RESETTING);
        for (DuelParticipant participant : match.getAllParticipants()) {
            this.plugin.getLobbyManager().forceRemoveFromLobbyTracking(participant.getUuid());
        }
        DuelKit kit = this.kitManager.getAdminKit(match.getKitName());
        if (kit == null) {
            this.cancelMatch(match, "Kit not found!");
            return;
        }
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        int team1Index = 0;
        int team2Index = 0;
        for (DuelParticipant participant : match.getAllParticipants()) {
            Location spawn;
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setFireTicks(0);
            player.setFallDistance(0.0f);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            this.kitManager.applyKit(player, kit, true);
            if (participant.getTeamId() == 1) {
                spawn = this.getOffsetSpawn(match.getArena().getSpawnPoint1(), team1Index);
                ++team1Index;
            } else if (participant.getTeamId() == 2) {
                spawn = this.getOffsetSpawn(match.getArena().getSpawnPoint2(), team2Index);
                ++team2Index;
            } else if (new Random().nextBoolean()) {
                spawn = this.getOffsetSpawn(match.getArena().getSpawnPoint1(), team1Index);
                ++team1Index;
            } else {
                spawn = this.getOffsetSpawn(match.getArena().getSpawnPoint2(), team2Index);
                ++team2Index;
            }
            if (spawn != null) {
                player.teleport(spawn);
            }
            participant.setAlive(true);
            participant.resetRoundStats();
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            Player finalPlayer = player;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (finalPlayer.isOnline()) {
                    finalPlayer.setGameMode(GameMode.SURVIVAL);
                    finalPlayer.setAllowFlight(false);
                    finalPlayer.setFlying(false);
                    finalPlayer.setWalkSpeed(0.2f);
                    finalPlayer.setFlySpeed(0.1f);
                }
            }, 2L);
            if (healthDisplayManager == null) continue;
            healthDisplayManager.updateHealth(player);
        }
        if (this.arenaManager != null) {
            this.arenaManager.cleanupArenaItems(match.getArena());
        }
        this.startCountdown(match);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            for (DuelParticipant participant : match.getAllParticipants()) {
                Player p = Bukkit.getPlayer((UUID)participant.getUuid());
                if (p == null || !p.isOnline()) continue;
                this.reapplyMatchTeams(p);
            }
        }, 5L);
    }

    private boolean isMatchOver(@Nonnull DuelMatch match) {
        int score1 = match.getTeamScore(1);
        int score2 = match.getTeamScore(2);
        int totalRounds = match.getTotalRounds();
        if (totalRounds == 1) {
            return true;
        }
        return match.getWinCondition().isMatchOver(score1, score2, totalRounds, match.getCurrentRound());
    }

    private void setPlayerSpectatorForRound(@Nonnull Player player, @Nonnull DuelMatch match) {
        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setGameMode(GameMode.SPECTATOR);
        Location spectatorSpawn = match.getArena().getSpectatorSpawn();
        if (spectatorSpawn == null) {
            spectatorSpawn = match.getArena().getCenter();
        }
        if (spectatorSpawn != null) {
            player.teleport(spectatorSpawn);
        }
        player.sendMessage("\u00a77You are now spectating. Wait for the round to end...");
    }

    private void handleMatchEnd(@Nonnull DuelMatch match, int winningTeam) {
        Player player;
        match.setState(MatchState.ENDING);
        match.setEndTime(System.currentTimeMillis());
        match.setWinningTeam(winningTeam);
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player2 = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player2 == null) continue;
            player2.setWalkSpeed(0.2f);
            player2.setFlySpeed(0.1f);
        }
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            this.removeHealthDisplaysForMatch(healthDisplayManager, match);
        }
        List<DuelParticipant> winners = match.getTeamParticipants(winningTeam);
        List<DuelParticipant> losers = match.getTeamParticipants(winningTeam == 1 ? 2 : 1);
        for (DuelParticipant winner : winners) {
            player = Bukkit.getPlayer((UUID)winner.getUuid());
            if (player == null) continue;
            player.setHealth(player.getMaxHealth());
            player.setFireTicks(0);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
            this.showVictoryTitle(player);
            this.sendMatchEndMessage(player, match, true);
        }
        for (DuelParticipant loser : losers) {
            player = Bukkit.getPlayer((UUID)loser.getUuid());
            if (player == null) continue;
            player.setHealth(player.getMaxHealth());
            player.setFireTicks(0);
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            if (loser.isAlive()) {
                player.setGameMode(GameMode.SPECTATOR);
                Location specSpawn = match.getArena().getSpectatorSpawn();
                if (specSpawn == null) {
                    specSpawn = match.getArena().getCenter();
                }
                if (specSpawn != null) {
                    player.teleport(specSpawn);
                }
                this.showDefeatTitle(player);
                this.sendMatchEndMessage(player, match, false);
                player.sendMessage("\u00a77Returning to lobby in " + this.endCelebrationSeconds + " seconds...");
                continue;
            }
            if (player.getGameMode() == GameMode.SPECTATOR) {
                this.showDefeatTitle(player);
                this.sendMatchEndMessage(player, match, false);
                player.sendMessage("\u00a77Returning to lobby in " + this.endCelebrationSeconds + " seconds...");
                continue;
            }
            this.showDefeatTitle(player);
            this.sendMatchEndMessage(player, match, false);
            player.sendMessage("\u00a77You forfeited the match.");
        }
        for (UUID spectatorUUID : this.getMatchSpectators(match.getMatchId())) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorUUID);
            if (spectator == null) continue;
            String winnerNames = winners.stream().map(DuelParticipant::getPlayerName).collect(Collectors.joining(", "));
            spectator.sendMessage("\u00a7a\u00a7lMATCH OVER! \u00a77Winner: \u00a7e" + winnerNames);
        }
        this.updateMatchStatistics(match, winners, losers);
        this.addToHistory(match);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.cleanupMatch(match), (long)this.endCelebrationSeconds * 20L);
    }

    private void showVictoryTitle(@Nonnull Player player) {
        Title title = Title.title((Component)Component.text((String)"\u00a7a\u00a7lVICTORY!"), (Component)Component.text((String)"\u00a77You won the duel!"), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        this.spawnVictoryFirework(player.getLocation());
    }

    private void showDefeatTitle(@Nonnull Player player) {
        Title title = Title.title((Component)Component.text((String)"\u00a7c\u00a7lDEFEAT"), (Component)Component.text((String)"\u00a77Better luck next time!"), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofSeconds(2L), (Duration)Duration.ofMillis(500L)));
        player.showTitle(title);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
    }

    private void sendMatchEndMessage(@Nonnull Player player, @Nonnull DuelMatch match, boolean won) {
        String result = won ? "\u00a7a\u00a7lVICTORY" : "\u00a7c\u00a7lDEFEAT";
        String score = match.getTeamScore(1) + " - " + match.getTeamScore(2);
        long duration = match.getMatchDuration();
        String durationStr = this.formatDuration(duration);
        player.sendMessage("");
        player.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("  " + result);
        player.sendMessage("");
        player.sendMessage("  \u00a77Final Score: \u00a7e" + score);
        player.sendMessage("  \u00a77Duration: \u00a7e" + durationStr);
        player.sendMessage("  \u00a77Kit: \u00a7e" + match.getKitName());
        player.sendMessage("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        player.sendMessage("");
    }

    private void spawnVictoryFirework(@Nonnull Location location) {
        if (location.getWorld() == null) {
            return;
        }
        location.getWorld().spawn(location.clone().add(0.0, 1.0, 0.0), Firework.class, firework -> {
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withColor(new Color[]{Color.GREEN, Color.LIME}).withFade(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).trail(true).flicker(true).build());
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        });
    }

    private void updateMatchStatistics(@Nonnull DuelMatch match, @Nonnull List<DuelParticipant> winners, @Nonnull List<DuelParticipant> losers) {
        PlayerStats stats;
        UUID uuid;
        StatsManager statsManager = this.plugin.getStatsManager();
        if (statsManager == null) {
            this.plugin.getLogger().warning("StatsManager is null - cannot save stats!");
            return;
        }
        String kitName = match.getKitName();
        for (DuelParticipant winner : winners) {
            uuid = winner.getUuid();
            try {
                statsManager.recordMatch(uuid, kitName, true, winner.getKills(), winner.getDeaths());
                this.plugin.debug("\u2713 Recorded WIN for " + winner.getPlayerName() + " (K:" + winner.getKills() + " D:" + winner.getDeaths() + ")");
                stats = statsManager.getStats(uuid);
                this.plugin.debug("  Current stats: W:" + stats.getTotalWins() + " L:" + stats.getTotalLosses() + " K:" + stats.getTotalKills() + " D:" + stats.getTotalDeaths() + " ELO:" + stats.getGlobalElo());
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Failed to save stats for winner " + winner.getPlayerName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        for (DuelParticipant loser : losers) {
            uuid = loser.getUuid();
            try {
                statsManager.recordMatch(uuid, kitName, false, loser.getKills(), loser.getDeaths());
                this.plugin.debug("\u2713 Recorded LOSS for " + loser.getPlayerName() + " (K:" + loser.getKills() + " D:" + loser.getDeaths() + ")");
                stats = statsManager.getStats(uuid);
                this.plugin.debug("  Current stats: W:" + stats.getTotalWins() + " L:" + stats.getTotalLosses() + " K:" + stats.getTotalKills() + " D:" + stats.getTotalDeaths() + " ELO:" + stats.getGlobalElo());
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Failed to save stats for loser " + loser.getPlayerName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (winners.size() == 1 && losers.size() == 1) {
            try {
                UUID winnerUUID = winners.get(0).getUuid();
                UUID loserUUID = losers.get(0).getUuid();
                int winnerElo = statsManager.getElo(winnerUUID);
                int loserElo = statsManager.getElo(loserUUID);
                int[] eloChanges = this.calculateEloChange(winnerElo, loserElo);
                int winnerChange = eloChanges[0];
                int loserChange = eloChanges[1];
                statsManager.addElo(winnerUUID, winnerChange);
                statsManager.addElo(loserUUID, loserChange);
                statsManager.addKitElo(winnerUUID, kitName, winnerChange);
                statsManager.addKitElo(loserUUID, kitName, loserChange);
                this.plugin.debug("\u2713 Updated ELO: " + winners.get(0).getPlayerName() + " (+" + winnerChange + " \u2192 " + (winnerElo + winnerChange) + "), " + losers.get(0).getPlayerName() + " (" + loserChange + " \u2192 " + (loserElo + loserChange) + ")");
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to update ELO: " + e.getMessage());
                e.printStackTrace();
            }
        }
        for (DuelParticipant participant : match.getAllParticipants()) {
            statsManager.saveStats(participant.getUuid());
        }
    }

    private int[] calculateEloChange(int winnerElo, int loserElo) {
        int kFactor = 32;
        double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (double)(loserElo - winnerElo) / 400.0));
        double expectedLoser = 1.0 / (1.0 + Math.pow(10.0, (double)(winnerElo - loserElo) / 400.0));
        int winnerChange = (int)Math.round((double)kFactor * (1.0 - expectedWinner));
        int loserChange = (int)Math.round((double)kFactor * (0.0 - expectedLoser));
        if (winnerChange < 1) {
            winnerChange = 1;
        }
        if (loserChange > -1) {
            loserChange = -1;
        }
        return new int[]{winnerChange, loserChange};
    }

    private void cleanupMatch(@Nonnull DuelMatch match) {
        BukkitTask task;
        HealthDisplayManager healthDisplayManager;
        match.setState(MatchState.COMPLETED);
        this.removeMatchTeams(match);
        if (this.plugin.getPartyManager() != null) {
            this.plugin.getPartyManager().removePartySplitTeams();
        }
        if ((healthDisplayManager = this.plugin.getHealthDisplayManager()) != null) {
            this.removeHealthDisplaysForMatch(healthDisplayManager, match);
        }
        HashSet<UUID> spectatorUUIDs = new HashSet<UUID>(this.getMatchSpectators(match.getMatchId()));
        for (UUID spectatorUUID : spectatorUUIDs) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorUUID);
            if (spectator != null && spectator.isOnline()) {
                this.spectatorMatchMap.remove(spectatorUUID);
                Set<UUID> specs = this.matchSpectators.get(match.getMatchId());
                if (specs != null) {
                    specs.remove(spectatorUUID);
                }
                this.playerStateManager.restoreState(spectator);
                spectator.setWalkSpeed(0.2f);
                spectator.setFlySpeed(0.1f);
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (spectator.isOnline()) {
                        spectator.setAllowFlight(false);
                        spectator.setFlying(false);
                    }
                }, 5L);
                this.plugin.getLobbyManager().sendToLobby(spectator, true, false);
                spectator.sendMessage("\u00a7aThe match has ended. Returning you to the lobby.");
                continue;
            }
            this.spectatorMatchMap.remove(spectatorUUID);
        }
        for (DuelParticipant participant : match.getAllParticipants()) {
            this.playerMatchMap.remove(participant.getUuid());
        }
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player == null) continue;
            player.setNoDamageTicks(0);
            player.setInvulnerable(false);
            player.setGameMode(GameMode.SURVIVAL);
            WorldChangeListener worldListener = this.getWorldChangeListener();
            if (worldListener != null) {
                worldListener.markPlayerFinishingMatch(participant.getUuid());
            }
            this.plugin.getLobbyManager().sendToLobby(player, true, true);
            if (this.scoreboardManager == null) continue;
            this.scoreboardManager.refreshScoreboardType(player);
        }
        if (this.arenaManager != null) {
            this.arenaManager.cleanupArenaItems(match.getArena());
            this.arenaManager.releaseArena(match.getArena().getName(), match.getMatchId());
        }
        if ((task = this.matchTasks.remove(match.getMatchId())) != null) {
            task.cancel();
        }
        this.activeMatches.remove(match.getMatchId());
        this.matchSpectators.remove(match.getMatchId());
    }

    private WorldChangeListener getWorldChangeListener() {
        try {
            for (RegisteredListener listener : HandlerList.getRegisteredListeners((Plugin)this.plugin)) {
                if (!(listener.getListener() instanceof WorldChangeListener)) continue;
                return (WorldChangeListener)listener.getListener();
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to get WorldChangeListener: " + e.getMessage());
        }
        return null;
    }

    public void cancelMatch(@Nonnull DuelMatch match, @Nonnull String reason) {
        BukkitTask task;
        match.setState(MatchState.CANCELLED);
        this.removeMatchTeams(match);
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            this.removeHealthDisplaysForMatch(healthDisplayManager, match);
        }
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player player = Bukkit.getPlayer((UUID)participant.getUuid());
            if (player != null) {
                player.sendMessage("\u00a7c\u00a7lMATCH CANCELLED: \u00a77" + reason);
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SURVIVAL);
                }
                this.playerStateManager.restoreState(player);
                this.plugin.getLobbyManager().sendToLobby(player);
                if (this.scoreboardManager != null) {
                    this.scoreboardManager.refreshScoreboardType(player);
                }
            }
            this.playerMatchMap.remove(participant.getUuid());
        }
        if (this.arenaManager != null && match.getArena() != null) {
            this.arenaManager.releaseArena(match.getArena().getName(), match.getMatchId());
        }
        if ((task = this.matchTasks.remove(match.getMatchId())) != null) {
            task.cancel();
        }
        this.activeMatches.remove(match.getMatchId());
        this.matchSpectators.remove(match.getMatchId());
    }

    public boolean addSpectator(@Nonnull Player spectator, @Nonnull UUID matchId) {
        if (!this.allowSpectators) {
            spectator.sendMessage("\u00a7cSpectating is currently disabled!");
            return false;
        }
        DuelMatch match = this.activeMatches.get(matchId);
        if (match == null) {
            spectator.sendMessage("\u00a7cThis match no longer exists!");
            return false;
        }
        if (this.isInMatch(spectator.getUniqueId())) {
            spectator.sendMessage("\u00a7cYou cannot spectate while in a match!");
            return false;
        }
        Set<UUID> specs = this.matchSpectators.get(matchId);
        if (specs != null && specs.size() >= this.maxSpectatorsPerMatch) {
            spectator.sendMessage("\u00a7cThis match has too many spectators!");
            return false;
        }
        Location spectatorSpawn = match.getArena().getSpectatorSpawn();
        if (spectatorSpawn == null) {
            spectatorSpawn = match.getArena().getCenter();
        }
        if (spectatorSpawn == null && (spectatorSpawn = match.getArena().getSpawnPoint1()) != null) {
            spectatorSpawn = spectatorSpawn.clone().add(0.0, 10.0, 0.0);
        }
        if (spectatorSpawn == null || spectatorSpawn.getWorld() == null) {
            spectator.sendMessage("\u00a7cThis arena has no valid spectator location!");
            this.plugin.getLogger().warning("Arena " + match.getArena().getName() + " has NO spectator spawn, center, or spawn1 set!");
            return false;
        }
        QueueManager queueManager = this.plugin.getQueueManager();
        if (queueManager != null) {
            queueManager.removeFromAllQueues(spectator.getUniqueId());
        }
        this.playerStateManager.saveState(spectator);
        this.spectatorMatchMap.put(spectator.getUniqueId(), matchId);
        if (specs != null) {
            specs.add(spectator.getUniqueId());
        }
        spectator.getActivePotionEffects().forEach(effect -> spectator.removePotionEffect(effect.getType()));
        spectator.getInventory().clear();
        spectator.getInventory().setArmorContents(new ItemStack[4]);
        spectator.getInventory().setItemInOffHand(null);
        Location finalSpawn = spectatorSpawn.clone();
        UUID finalMatchId = matchId;
        spectator.setGameMode(GameMode.SURVIVAL);
        boolean teleported = spectator.teleport(finalSpawn);
        if (!teleported) {
            this.spectatorMatchMap.remove(spectator.getUniqueId());
            if (specs != null) {
                specs.remove(spectator.getUniqueId());
            }
            this.playerStateManager.restoreState(spectator);
            spectator.sendMessage("\u00a7cFailed to teleport to spectator location!");
            return false;
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!spectator.isOnline()) {
                return;
            }
            if (!this.spectatorMatchMap.containsKey(spectator.getUniqueId())) {
                return;
            }
            spectator.setGameMode(GameMode.SPECTATOR);
            spectator.setAllowFlight(true);
            spectator.setFlying(true);
            spectator.setWalkSpeed(0.2f);
            spectator.setFlySpeed(0.1f);
            spectator.getInventory().clear();
            this.plugin.debug("[Spectator] Set " + spectator.getName() + " to SPECTATOR (attempt 1)");
        }, 5L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!spectator.isOnline()) {
                return;
            }
            if (!this.spectatorMatchMap.containsKey(spectator.getUniqueId())) {
                return;
            }
            if (spectator.getGameMode() != GameMode.SPECTATOR) {
                this.plugin.getLogger().warning("[Spectator] GameMode was overridden for " + spectator.getName() + ", forcing SPECTATOR again!");
                spectator.setGameMode(GameMode.SPECTATOR);
            }
            spectator.setAllowFlight(true);
            spectator.setFlying(true);
            spectator.getInventory().clear();
            spectator.updateInventory();
            this.plugin.debug("[Spectator] Verified " + spectator.getName() + " is in SPECTATOR mode: " + String.valueOf(spectator.getGameMode()));
        }, 10L);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (!spectator.isOnline()) {
                return;
            }
            if (!this.spectatorMatchMap.containsKey(spectator.getUniqueId())) {
                return;
            }
            if (spectator.getGameMode() != GameMode.SPECTATOR) {
                this.plugin.getLogger().severe("[Spectator] CRITICAL: GameMode keeps getting overridden for " + spectator.getName() + "! Current: " + String.valueOf(spectator.getGameMode()));
                spectator.setGameMode(GameMode.SPECTATOR);
            }
        }, 20L);
        spectator.sendMessage("\u00a7aYou are now spectating the match!");
        spectator.sendMessage("\u00a77Use \u00a7e/spectate leave \u00a77or \u00a7e/unspectate \u00a77to stop spectating.");
        this.plugin.debug("[Spectator] " + spectator.getName() + " started spectating match " + String.valueOf(matchId));
        return true;
    }

    public boolean addSpectator(@Nonnull Player spectator, @Nonnull String target) {
        UUID matchId;
        Player targetPlayer = Bukkit.getPlayer((String)target);
        if (targetPlayer != null && (matchId = this.playerMatchMap.get(targetPlayer.getUniqueId())) != null) {
            return this.addSpectator(spectator, matchId);
        }
        for (DuelMatch match : this.activeMatches.values()) {
            if (!match.getArena().getName().equalsIgnoreCase(target)) continue;
            return this.addSpectator(spectator, match.getMatchId());
        }
        spectator.sendMessage("\u00a7cNo active match found for: " + target);
        return false;
    }

    public void removeSpectator(@Nonnull Player spectator) {
        Set<UUID> spectators;
        UUID spectatorUUID = spectator.getUniqueId();
        UUID matchId = this.spectatorMatchMap.remove(spectatorUUID);
        if (matchId != null && (spectators = this.matchSpectators.get(matchId)) != null) {
            spectators.remove(spectatorUUID);
        }
        this.playerStateManager.restoreState(spectator);
        spectator.setWalkSpeed(0.2f);
        spectator.setFlySpeed(0.1f);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (spectator.isOnline()) {
                spectator.setWalkSpeed(0.2f);
                spectator.setFlySpeed(0.1f);
                spectator.setAllowFlight(false);
                spectator.setFlying(false);
            }
        }, 5L);
        this.plugin.getLobbyManager().sendToLobby(spectator, true, false);
        spectator.sendMessage("\u00a7aYou are no longer spectating.");
        this.plugin.debug("Player " + spectator.getName() + " stopped spectating");
    }

    @Nonnull
    public Set<UUID> getMatchSpectators(@Nonnull UUID matchId) {
        Set<UUID> spectators = this.matchSpectators.get(matchId);
        return spectators != null ? new HashSet<UUID>(spectators) : Collections.emptySet();
    }

    @Nullable
    public UUID getSpectatingMatchId(@Nonnull UUID spectatorUUID) {
        return this.spectatorMatchMap.get(spectatorUUID);
    }

    public boolean isSpectating(@Nonnull UUID uuid) {
        return this.spectatorMatchMap.containsKey(uuid);
    }

    @Nonnull
    public List<DuelMatch> getSpectatableMatches() {
        return this.activeMatches.values().stream().filter(m -> m.getState() == MatchState.IN_PROGRESS).collect(Collectors.toList());
    }

    public boolean isInMatch(@Nonnull UUID uuid) {
        return this.playerMatchMap.containsKey(uuid);
    }

    public boolean isInDuel(@Nonnull Player player) {
        return this.isInMatch(player.getUniqueId());
    }

    public boolean isActiveParticipant(@Nonnull UUID uuid) {
        DuelMatch match = this.getMatch(uuid);
        if (match == null) {
            return false;
        }
        DuelParticipant participant = match.getParticipant(uuid);
        if (participant == null) {
            return false;
        }
        return participant.isAlive();
    }

    public void removeDeadSpectatorFromMatch(@Nonnull UUID uuid) {
        DuelMatch match = this.getMatch(uuid);
        if (match == null) {
            return;
        }
        DuelParticipant participant = match.getParticipant(uuid);
        if (participant == null) {
            return;
        }
        if (participant.isAlive()) {
            this.plugin.getLogger().warning("[DuelManager] removeDeadSpectatorFromMatch called on alive player!");
            return;
        }
        this.playerMatchMap.remove(uuid);
        HealthDisplayManager hdm = this.plugin.getHealthDisplayManager();
        if (hdm != null) {
            hdm.removeDisplay(uuid);
        }
        this.plugin.debug("[DuelManager] Dead spectator " + String.valueOf(uuid) + " left match voluntarily. Match continues.");
    }

    @Nullable
    public DuelMatch getMatch(@Nonnull UUID playerUUID) {
        UUID matchId = this.playerMatchMap.get(playerUUID);
        if (matchId == null) {
            return null;
        }
        return this.activeMatches.get(matchId);
    }

    @Nullable
    public DuelMatch getMatch(@Nonnull Player player) {
        return this.getMatch(player.getUniqueId());
    }

    @Nullable
    public DuelMatch getMatchById(@Nonnull UUID matchId) {
        return this.activeMatches.get(matchId);
    }

    @Nonnull
    public Collection<DuelMatch> getActiveMatches() {
        return Collections.unmodifiableCollection(this.activeMatches.values());
    }

    public int getActiveMatchCount() {
        return this.activeMatches.size();
    }

    public int getTotalPlayersInMatches() {
        return this.playerMatchMap.size();
    }

    public int getPlayersInMatchForKit(@Nonnull String kitName) {
        return (int)this.activeMatches.values().stream().filter(m -> m.getKitName().equalsIgnoreCase(kitName)).mapToLong(m -> m.getAllParticipants().size()).sum();
    }

    @Nullable
    public UUID getOpponent(@Nonnull UUID playerUUID) {
        DuelMatch match = this.getMatch(playerUUID);
        if (match == null) {
            return null;
        }
        return match.getOpponent(playerUUID);
    }

    public boolean hasPendingRequest(@Nonnull UUID targetUUID) {
        return this.pendingRequests.containsKey(targetUUID);
    }

    @Nullable
    public DuelRequest getPendingRequest(@Nonnull UUID targetUUID) {
        return this.pendingRequests.get(targetUUID);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addToHistory(@Nonnull DuelMatch match) {
        List<UUID> participantUUIDs = match.getAllParticipants().stream().map(DuelParticipant::getUuid).collect(Collectors.toList());
        MatchResult result = new MatchResult(match.getMatchId(), match.getMatchType(), match.getKitName(), match.getTeamScore(1), match.getTeamScore(2), match.getWinningTeam(), match.getMatchDuration(), match.getStartTime(), participantUUIDs);
        List<MatchResult> list = this.matchHistory;
        synchronized (list) {
            this.matchHistory.add(0, result);
            while (this.matchHistory.size() > 100) {
                this.matchHistory.remove(this.matchHistory.size() - 1);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nonnull
    public List<MatchResult> getMatchHistory(int limit) {
        List<MatchResult> list = this.matchHistory;
        synchronized (list) {
            return this.matchHistory.stream().limit(limit).collect(Collectors.toList());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nonnull
    public List<MatchResult> getPlayerMatchHistory(@Nonnull UUID playerUUID, int limit) {
        List<MatchResult> list = this.matchHistory;
        synchronized (list) {
            return this.matchHistory.stream().filter(r -> r.hadParticipant(playerUUID)).limit(limit).collect(Collectors.toList());
        }
    }

    private void cleanupExpiredRequests() {
        long now = System.currentTimeMillis();
        long timeout = (long)this.requestTimeoutSeconds * 1000L;
        Iterator<Map.Entry<UUID, DuelRequest>> iterator = this.pendingRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            Player sender;
            Map.Entry<UUID, DuelRequest> entry = iterator.next();
            if (!entry.getValue().isExpired(timeout)) continue;
            iterator.remove();
            Player target = Bukkit.getPlayer((UUID)entry.getKey());
            if (target != null) {
                target.sendMessage("\u00a77A duel request has expired.");
            }
            if ((sender = Bukkit.getPlayer((UUID)entry.getValue().getSenderUUID())) == null) continue;
            sender.sendMessage("\u00a77Your duel request has expired.");
        }
    }

    private void validateActiveMatches() {
        for (DuelMatch match : new ArrayList<DuelMatch>(this.activeMatches.values())) {
            Player player;
            boolean allAliveOnline = match.getAllParticipants().stream().filter(DuelParticipant::isAlive).allMatch(p -> {
                Player player = Bukkit.getPlayer((UUID)p.getUuid());
                return player != null && player.isOnline();
            });
            if (!allAliveOnline) {
                for (DuelParticipant p2 : match.getAllParticipants()) {
                    if (!p2.isAlive() || (player = Bukkit.getPlayer((UUID)p2.getUuid())) != null && player.isOnline()) continue;
                    this.handleDisconnect(p2.getUuid());
                    break;
                }
            }
            for (DuelParticipant p2 : match.getAllParticipants()) {
                if (p2.isAlive() || (player = Bukkit.getPlayer((UUID)p2.getUuid())) != null && player.isOnline() || !this.playerMatchMap.containsKey(p2.getUuid())) continue;
                this.playerMatchMap.remove(p2.getUuid());
                this.plugin.debug("[DuelManager] Cleaned up offline dead participant: " + p2.getPlayerName());
            }
        }
    }

    public void handleDisconnect(@Nonnull UUID playerUUID) {
        boolean teamStillAlive;
        DuelParticipant participant;
        if (this.isSpectating(playerUUID)) {
            this.spectatorMatchMap.remove(playerUUID);
            for (Set<UUID> spectators : this.matchSpectators.values()) {
                spectators.remove(playerUUID);
            }
            return;
        }
        DuelMatch match = this.getMatch(playerUUID);
        if (match == null) {
            return;
        }
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.removeDisplay(playerUUID);
        }
        if ((participant = match.getParticipant(playerUUID)) == null) {
            this.playerMatchMap.remove(playerUUID);
            return;
        }
        if (!participant.isAlive()) {
            this.playerMatchMap.remove(playerUUID);
            this.plugin.debug("[DuelManager] Dead participant " + participant.getPlayerName() + " disconnected. Match continues.");
            return;
        }
        participant.setAlive(false);
        int disconnectedTeam = participant.getTeamId();
        int otherTeam = disconnectedTeam == 1 ? 2 : 1;
        for (DuelParticipant p : match.getAllParticipants()) {
            Player onlinePlayer;
            if (p.getUuid().equals(playerUUID) || (onlinePlayer = Bukkit.getPlayer((UUID)p.getUuid())) == null) continue;
            onlinePlayer.sendMessage("\u00a7c" + participant.getPlayerName() + " disconnected!");
        }
        if (match.getMatchType() != MatchType.DUEL_1V1 && match.getMatchType() != MatchType.RANKED && (teamStillAlive = match.getTeamParticipants(disconnectedTeam).stream().anyMatch(DuelParticipant::isAlive))) {
            this.playerMatchMap.remove(playerUUID);
            this.plugin.debug("[DuelManager] Team player " + participant.getPlayerName() + " disconnected but team still has alive members. Match continues.");
            return;
        }
        this.handleMatchEnd(match, otherTeam);
    }

    public void handlePlayerLeave(@Nonnull Player player) {
        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();
        if (healthDisplayManager != null) {
            healthDisplayManager.removeDisplay(player);
        }
        this.handleDisconnect(player.getUniqueId());
    }

    @Nonnull
    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        if (minutes > 0L) {
            return String.format("%d:%02d", minutes, seconds % 60L);
        }
        return String.format("0:%02d", seconds);
    }

    public void reload() {
        this.loadSettings();
        this.plugin.getLogger().info("\u00a7a[DuelManager] Reloaded successfully!");
    }

    public void shutdown() {
        for (DuelMatch match : new ArrayList<DuelMatch>(this.activeMatches.values())) {
            this.cancelMatch(match, "Server shutting down");
        }
        this.pendingRequests.clear();
        for (BukkitTask task : this.matchTasks.values()) {
            task.cancel();
        }
        this.matchTasks.clear();
        this.plugin.getLogger().info("\u00a7a[DuelManager] Shutdown complete!");
    }
}

