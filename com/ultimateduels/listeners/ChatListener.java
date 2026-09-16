/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.event.player.AsyncChatEvent
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.plugin.Plugin
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.ffa.model.FFAPlayerData;
import com.ultimateduels.libs.adventure.plain.PlainTextComponentSerializer;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.player.PlayerDataManager;
import com.ultimateduels.utils.MessageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

public class ChatListener
implements Listener {
    private final UltimateDuels plugin;
    private final PartyManager partyManager;
    private final Map<UUID, Long> chatCooldowns;
    private static final long CHAT_COOLDOWN_MS = 1000L;
    private static final String PARTY_CHAT_PREFIX = "@p ";
    private static final String PARTY_CHAT_PREFIX_ALT = "@party ";
    private static final String TEAM_CHAT_PREFIX = "@t ";
    private static final String TEAM_CHAT_PREFIX_ALT = "@team ";
    private static final String SPEC_CHAT_PREFIX = "@s ";
    private static final String SPEC_CHAT_PREFIX_ALT = "@spec ";
    private static final Set<String> ALLOWED_DUEL_COMMANDS = Set.of("/msg", "/tell", "/whisper", "/w", "/r", "/reply", "/party", "/p", "/settings", "/ping");
    private static final Set<String> BLOCKED_DUEL_COMMANDS = Set.of("/spawn", "/home", "/tp", "/tpa", "/tpahere", "/back", "/warp", "/hub", "/lobby", "/server", "/leave");

    public ChatListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.partyManager = plugin.getPartyManager();
        this.chatCooldowns = new ConcurrentHashMap<UUID, Long>();
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.startsWith(PARTY_CHAT_PREFIX) || lowerMessage.startsWith(PARTY_CHAT_PREFIX_ALT)) {
            event.setCancelled(true);
            this.handlePartyChat(player, message);
            return;
        }
        if (lowerMessage.startsWith(TEAM_CHAT_PREFIX) || lowerMessage.startsWith(TEAM_CHAT_PREFIX_ALT)) {
            event.setCancelled(true);
            this.handleTeamChat(player, message);
            return;
        }
        if (lowerMessage.startsWith(SPEC_CHAT_PREFIX) || lowerMessage.startsWith(SPEC_CHAT_PREFIX_ALT)) {
            event.setCancelled(true);
            this.handleSpectatorChat(player, message);
            return;
        }
        if (this.isOnCooldown(player)) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "&cPlease wait before sending another message!");
            return;
        }
        this.applyCooldown(player);
        if (this.plugin.getDuelManager().isInDuel(player)) {
            this.handleDuelChat(event, player, message);
            return;
        }
        if (this.plugin.getFFAManager().isInFFAArena(player)) {
            this.handleFFAChat(event, player, message);
            return;
        }
        if (this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            this.handleSpectatingChat(event, player, message);
            return;
        }
        this.handleLobbyChat(event, player, message);
    }

    private void handlePartyChat(Player player, String message) {
        if (!this.partyManager.isInParty(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou are not in a party!");
            return;
        }
        String chatMessage = message;
        if (chatMessage.toLowerCase().startsWith(PARTY_CHAT_PREFIX)) {
            chatMessage = chatMessage.substring(PARTY_CHAT_PREFIX.length());
        } else if (chatMessage.toLowerCase().startsWith(PARTY_CHAT_PREFIX_ALT)) {
            chatMessage = chatMessage.substring(PARTY_CHAT_PREFIX_ALT.length());
        }
        if (chatMessage.trim().isEmpty()) {
            MessageUtils.sendMessage(player, "&cPlease enter a message!");
            return;
        }
        String finalMessage = chatMessage;
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.partyManager.sendPartyChat(player, finalMessage));
    }

    private void handleTeamChat(Player player, String message) {
        boolean isTeamDuel;
        if (!this.plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou are not in a duel!");
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatch(player.getUniqueId());
        if (match == null) {
            MessageUtils.sendMessage(player, "&cTeam chat is only available in party duels!");
            return;
        }
        boolean bl = isTeamDuel = match.getTeam1().size() > 1 || match.getTeam2().size() > 1;
        if (!isTeamDuel) {
            MessageUtils.sendMessage(player, "&cTeam chat is only available in party duels!");
            return;
        }
        String chatMessage = message;
        if (chatMessage.toLowerCase().startsWith(TEAM_CHAT_PREFIX)) {
            chatMessage = chatMessage.substring(TEAM_CHAT_PREFIX.length());
        } else if (chatMessage.toLowerCase().startsWith(TEAM_CHAT_PREFIX_ALT)) {
            chatMessage = chatMessage.substring(TEAM_CHAT_PREFIX_ALT.length());
        }
        if (chatMessage.trim().isEmpty()) {
            return;
        }
        UUID playerUUID = player.getUniqueId();
        boolean isTeam1 = match.getTeam1().stream().anyMatch(p -> p.getUuid().equals(playerUUID));
        List<DuelParticipant> team = isTeam1 ? match.getTeam1() : match.getTeam2();
        String formattedMessage = "&3[Team] &f" + player.getName() + "&7: &f" + chatMessage;
        for (DuelParticipant teamMate : team) {
            Player teamMatePlayer = Bukkit.getPlayer((UUID)teamMate.getUuid());
            if (teamMatePlayer == null || !teamMatePlayer.isOnline()) continue;
            MessageUtils.sendMessage(teamMatePlayer, formattedMessage);
        }
    }

    private void handleSpectatorChat(Player player, String message) {
        if (!this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "&cYou are not spectating!");
            return;
        }
        String chatMessage = message;
        if (chatMessage.toLowerCase().startsWith(SPEC_CHAT_PREFIX)) {
            chatMessage = chatMessage.substring(SPEC_CHAT_PREFIX.length());
        } else if (chatMessage.toLowerCase().startsWith(SPEC_CHAT_PREFIX_ALT)) {
            chatMessage = chatMessage.substring(SPEC_CHAT_PREFIX_ALT.length());
        }
        if (chatMessage.trim().isEmpty()) {
            return;
        }
        UUID matchId = this.findSpectatedMatchId(player.getUniqueId());
        if (matchId == null) {
            return;
        }
        String formattedMessage = "&8[Spec] &7" + player.getName() + "&8: &7" + chatMessage;
        Set<UUID> spectators = this.plugin.getDuelManager().getMatchSpectators(matchId);
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            MessageUtils.sendMessage(spectator, formattedMessage);
        }
    }

    private UUID findSpectatedMatchId(UUID spectatorUUID) {
        for (DuelMatch match : this.plugin.getDuelManager().getActiveMatches()) {
            if (!this.plugin.getDuelManager().getMatchSpectators(match.getMatchId()).contains(spectatorUUID)) continue;
            return match.getMatchId();
        }
        return null;
    }

    private void handleDuelChat(AsyncChatEvent event, Player player, String message) {
        DuelMatch match = this.plugin.getDuelManager().getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        event.setCancelled(true);
        String formattedMessage = this.formatDuelMessage(player, message, match);
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player participantPlayer = Bukkit.getPlayer((UUID)participant.getUuid());
            if (participantPlayer == null || !participantPlayer.isOnline()) continue;
            MessageUtils.sendMessage(participantPlayer, formattedMessage);
        }
        Set<UUID> spectators = this.plugin.getDuelManager().getMatchSpectators(match.getMatchId());
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            MessageUtils.sendMessage(spectator, formattedMessage);
        }
    }

    private void handleFFAChat(AsyncChatEvent event, Player player, String message) {
        String arenaName = this.plugin.getFFAManager().getPlayerArena(player.getUniqueId());
        if (arenaName == null) {
            return;
        }
        event.setCancelled(true);
        FFAPlayerData playerData = this.plugin.getFFAManager().getPlayerData(player.getUniqueId());
        int streak = playerData != null ? playerData.getKillstreak() : 0;
        Object streakPrefix = streak >= 5 ? "&6[" + streak + "\ud83d\udd25] " : "";
        String formattedMessage = (String)streakPrefix + "&f" + player.getName() + "&7: &f" + message;
        FFAArenaInstance arena = this.plugin.getFFAManager().getArena(arenaName);
        if (arena != null) {
            for (UUID playerId : arena.getPlayers()) {
                Player arenaPlayer = Bukkit.getPlayer((UUID)playerId);
                if (arenaPlayer == null || !arenaPlayer.isOnline()) continue;
                MessageUtils.sendMessage(arenaPlayer, formattedMessage);
            }
        }
    }

    private void handleSpectatingChat(AsyncChatEvent event, Player player, String message) {
        event.setCancelled(true);
        UUID matchId = this.findSpectatedMatchId(player.getUniqueId());
        if (matchId == null) {
            return;
        }
        String formattedMessage = "&8[Spec] &7" + player.getName() + "&8: &7" + message;
        Set<UUID> spectators = this.plugin.getDuelManager().getMatchSpectators(matchId);
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorId);
            if (spectator == null || !spectator.isOnline()) continue;
            MessageUtils.sendMessage(spectator, formattedMessage);
        }
    }

    private void handleLobbyChat(AsyncChatEvent event, Player player, String message) {
        String rankPrefix = this.getRankPrefix(player);
        if (!rankPrefix.isEmpty()) {
            TextComponent formattedMessage = Component.text((String)MessageUtils.colorize(rankPrefix + player.getName() + "&7: &f" + message));
            event.message((Component)formattedMessage);
        }
    }

    private String formatDuelMessage(Player player, String message, DuelMatch match) {
        boolean isTeamDuel;
        String teamColor = "&f";
        boolean bl = isTeamDuel = match.getTeam1().size() > 1 || match.getTeam2().size() > 1;
        if (isTeamDuel) {
            boolean isTeam1 = match.getTeam1().stream().anyMatch(p -> p.getUuid().equals(player.getUniqueId()));
            teamColor = isTeam1 ? "&c" : "&9";
        }
        return teamColor + player.getName() + "&7: &f" + message;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String getRankPrefix(Player player) {
        try {
            PlayerDataManager playerData = this.plugin.getPlayerDataManager();
            if (playerData == null) return "";
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "";
    }

    private String getRankPrefixByElo(int elo) {
        if (elo >= 2000) {
            return "&4[Champion] ";
        }
        if (elo >= 1800) {
            return "&c[Master] ";
        }
        if (elo >= 1600) {
            return "&b[Diamond] ";
        }
        if (elo >= 1400) {
            return "&d[Platinum] ";
        }
        if (elo >= 1200) {
            return "&6[Gold] ";
        }
        if (elo >= 1000) {
            return "&7[Silver] ";
        }
        if (elo >= 800) {
            return "&8[Bronze] ";
        }
        return "";
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        FFAPlayerData playerData;
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase().split(" ")[0];
        if (this.plugin.getDuelManager().isInDuel(player)) {
            if (ALLOWED_DUEL_COMMANDS.stream().anyMatch(command::startsWith)) {
                return;
            }
            if (BLOCKED_DUEL_COMMANDS.stream().anyMatch(command::startsWith)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, "&cYou cannot use this command during a duel!");
                return;
            }
            if (command.startsWith("/duel") || command.startsWith("/arena") || command.startsWith("/kit") || command.startsWith("/stats") || command.startsWith("/spectate") || command.startsWith("/duels")) {
                return;
            }
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "&cYou cannot use commands during a duel!");
            return;
        }
        if (this.plugin.getFFAManager().isInFFAArena(player) && (playerData = this.plugin.getFFAManager().getPlayerData(player.getUniqueId())) != null && !playerData.isSpawnProtected()) {
            if (BLOCKED_DUEL_COMMANDS.stream().anyMatch(command::startsWith)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, "&cYou cannot use this command in FFA!");
                return;
            }
        }
        if (!(!this.plugin.getDuelManager().isSpectating(player.getUniqueId()) || command.startsWith("/spectate") || command.startsWith("/spec") || command.startsWith("/msg") || command.startsWith("/r") || command.startsWith("/settings") || command.startsWith("/ping"))) {
            if (BLOCKED_DUEL_COMMANDS.stream().anyMatch(command::startsWith)) {
                event.setCancelled(true);
                MessageUtils.sendMessage(player, "&cYou cannot use this command while spectating!");
            }
        }
    }

    private boolean isOnCooldown(Player player) {
        Long lastChat = this.chatCooldowns.get(player.getUniqueId());
        if (lastChat == null) {
            return false;
        }
        return System.currentTimeMillis() - lastChat < 1000L;
    }

    private void applyCooldown(Player player) {
        this.chatCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void cleanupCooldowns() {
        this.chatCooldowns.entrySet().removeIf(entry -> Bukkit.getPlayer((UUID)((UUID)entry.getKey())) == null);
    }
}

