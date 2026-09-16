/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.utils.MessageUtils;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveListener
implements Listener {
    private final UltimateDuels plugin;
    private static final int VOID_THRESHOLD = -64;

    public PlayerMoveListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        if (to.getY() < -64.0) {
            this.handleVoidDamage(player, event);
            return;
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            this.handleDuelMovement(player, event, from, to);
            return;
        }
        if (this.plugin.getFFAManager().isInFFAArena(player)) {
            this.handleFFAMovement(player, event, from, to);
            return;
        }
        if (this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            this.handleSpectatorMovement(player, event, to);
            return;
        }
    }

    private void handleVoidDamage(Player player, PlayerMoveEvent event) {
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager != null && duelManager.isInDuel(player)) {
            DuelMatch match = duelManager.getMatch(player.getUniqueId());
            if (match != null) {
                if (match.getState() == MatchState.IN_PROGRESS) {
                    event.setCancelled(true);
                    Location spawn = this.getSpawnPointForPlayer(match, player);
                    if (spawn != null) {
                        player.teleport(spawn);
                    }
                    duelManager.handleDeath(player, null);
                    MessageUtils.sendMessage(player, "&cYou fell into the void!");
                    return;
                }
                event.setCancelled(true);
                Location spawn = this.getSpawnPointForPlayer(match, player);
                if (spawn != null) {
                    player.teleport(spawn);
                }
                return;
            }
            return;
        }
        if (ffaManager != null && ffaManager.isInFFAArena(player)) {
            FFAArenaInstance arenaInstance;
            event.setCancelled(true);
            String arenaName = ffaManager.getPlayerArena(player.getUniqueId());
            if (arenaName != null && (arenaInstance = ffaManager.getArena(arenaName)) != null && arenaInstance.getArena() != null) {
                Location spawn = arenaInstance.getArena().getRandomSpawnPoint();
                if (spawn == null) {
                    spawn = arenaInstance.getArena().getSpawnPoint1();
                }
                if (spawn != null) {
                    player.teleport(spawn);
                }
            }
            ffaManager.handleDeath(player, null);
            return;
        }
        if (duelManager != null && duelManager.isSpectating(player.getUniqueId())) {
            event.setCancelled(true);
            Location lobby = this.plugin.getLobbyManager().getLobbySpawn();
            if (lobby != null) {
                player.teleport(lobby);
            }
            duelManager.removeSpectator(player);
            MessageUtils.sendMessage(player, "&cYou fell into the void while spectating!");
            return;
        }
        if (this.plugin.getLobbyManager().isInLobbyWorld(player)) {
            event.setCancelled(true);
            Location lobby = this.plugin.getLobbyManager().getLobbySpawn();
            if (lobby != null) {
                player.teleport(lobby);
                MessageUtils.sendMessage(player, "&cYou fell into the void!");
            }
            return;
        }
        if (this.plugin.getWorldRestrictionManager() != null && !this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(player.getWorld())) {
            this.plugin.debug("Void death in restricted world " + player.getWorld().getName() + " - allowing vanilla death for " + player.getName());
            return;
        }
        this.plugin.debug("Void death in non-managed world " + player.getWorld().getName() + " - allowing vanilla death for " + player.getName());
    }

    private void handleDuelMovement(Player player, PlayerMoveEvent event, Location from, Location to) {
        DuelArena arena;
        DuelManager duelManager = this.plugin.getDuelManager();
        DuelMatch match = duelManager.getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        MatchState state = match.getState();
        if (state == MatchState.STARTING && this.hasMovedPosition(from, to)) {
            Location newTo = from.clone();
            newTo.setYaw(to.getYaw());
            newTo.setPitch(to.getPitch());
            event.setTo(newTo);
            return;
        }
        if (state == MatchState.IN_PROGRESS && (arena = match.getArena()) != null && this.hasArenaBoundaries(arena) && !this.isWithinArenaBounds(arena, to)) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(player, "&cYou cannot leave the arena!");
        }
        if (state == MatchState.ROUND_ENDING && this.hasMovedPosition(from, to)) {
            Location newTo = from.clone();
            newTo.setYaw(to.getYaw());
            newTo.setPitch(to.getPitch());
            event.setTo(newTo);
        }
    }

    private void handleFFAMovement(Player player, PlayerMoveEvent event, Location from, Location to) {
        FFAManager ffaManager = this.plugin.getFFAManager();
        String arenaName = ffaManager.getPlayerArena(player.getUniqueId());
        if (arenaName == null) {
            return;
        }
        FFAArenaInstance arenaInstance = ffaManager.getArena(arenaName);
        if (arenaInstance == null) {
            return;
        }
        DuelArena arena = arenaInstance.getArena();
        if (arena == null) {
            return;
        }
        if (this.hasArenaBoundaries(arena) && !this.isWithinArenaBounds(arena, to)) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(player, "&cYou cannot leave the arena!");
        }
    }

    private void handleSpectatorMovement(Player player, PlayerMoveEvent event, Location to) {
        double maxZ;
        double minZ;
        double maxY;
        double minY;
        double maxX;
        double minX;
        DuelMatch deadParticipantMatch;
        UUID matchId = this.plugin.getDuelManager().getSpectatingMatchId(player.getUniqueId());
        if (matchId == null && (deadParticipantMatch = this.plugin.getDuelManager().getMatch(player.getUniqueId())) != null) {
            matchId = deadParticipantMatch.getMatchId();
        }
        if (matchId == null) {
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatchById(matchId);
        if (match == null || match.getArena() == null) {
            return;
        }
        Location corner1 = match.getArena().getCorner1();
        Location corner2 = match.getArena().getCorner2();
        if (corner1 != null && corner2 != null && corner1.getWorld() != null && to.getWorld() != null && corner1.getWorld().equals((Object)to.getWorld())) {
            double buffer = 10.0;
            minX = Math.min(corner1.getX(), corner2.getX()) - buffer;
            maxX = Math.max(corner1.getX(), corner2.getX()) + buffer;
            minY = Math.min(corner1.getY(), corner2.getY()) - 15.0;
            maxY = Math.max(corner1.getY(), corner2.getY()) + 30.0;
            minZ = Math.min(corner1.getZ(), corner2.getZ()) - buffer;
            maxZ = Math.max(corner1.getZ(), corner2.getZ()) + buffer;
        } else {
            Location spawn1 = match.getArena().getSpawnPoint1();
            Location spawn2 = match.getArena().getSpawnPoint2();
            if (spawn1 == null) {
                return;
            }
            double buffer = 40.0;
            double refX = spawn2 != null ? (spawn1.getX() + spawn2.getX()) / 2.0 : spawn1.getX();
            double refZ = spawn2 != null ? (spawn1.getZ() + spawn2.getZ()) / 2.0 : spawn1.getZ();
            double refY = spawn1.getY();
            minX = refX - buffer;
            maxX = refX + buffer;
            minY = refY - 20.0;
            maxY = refY + 50.0;
            minZ = refZ - buffer;
            maxZ = refZ + buffer;
        }
        if (to.getX() < minX || to.getX() > maxX || to.getY() < minY || to.getY() > maxY || to.getZ() < minZ || to.getZ() > maxZ) {
            event.setCancelled(true);
            player.sendActionBar((Component)Component.text((String)"\u00a7cYou cannot leave the arena while spectating!"));
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        DuelManager duelManager = this.plugin.getDuelManager();
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            FFAArenaInstance arenaInstance;
            String arenaName;
            DuelMatch match;
            if (duelManager.isInDuel(player) && (match = duelManager.getMatch(player.getUniqueId())) != null) {
                MatchState state = match.getState();
                if (state == MatchState.STARTING || state == MatchState.ROUND_ENDING || state == MatchState.RESETTING) {
                    event.setCancelled(true);
                    MessageUtils.sendMessage(player, "&cYou cannot use ender pearls right now!");
                    return;
                }
                Location to = event.getTo();
                DuelArena arena = match.getArena();
                if (to != null && arena != null && this.hasArenaBoundaries(arena) && !this.isWithinArenaBounds(arena, to)) {
                    event.setCancelled(true);
                    MessageUtils.sendMessage(player, "&cYou cannot pearl outside the arena!");
                    return;
                }
            }
            if (this.plugin.getFFAManager().isInFFAArena(player) && (arenaName = this.plugin.getFFAManager().getPlayerArena(player.getUniqueId())) != null && (arenaInstance = this.plugin.getFFAManager().getArena(arenaName)) != null) {
                DuelArena arena = arenaInstance.getArena();
                Location to = event.getTo();
                if (to != null && arena != null && this.hasArenaBoundaries(arena) && !this.isWithinArenaBounds(arena, to)) {
                    event.setCancelled(true);
                    MessageUtils.sendMessage(player, "&cYou cannot pearl outside the arena!");
                }
            }
        }
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT && (duelManager.isInDuel(player) || this.plugin.getFFAManager().isInFFAArena(player))) {
            event.setCancelled(true);
        }
        if (duelManager.isSpectating(player.getUniqueId()) && (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            event.setCancelled(true);
        }
    }

    private boolean hasMovedPosition(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    private Location getSpawnPointForPlayer(DuelMatch match, Player player) {
        if (match == null || match.getArena() == null) {
            return null;
        }
        DuelParticipant participant = match.getParticipant(player.getUniqueId());
        if (participant == null) {
            return null;
        }
        DuelArena arena = match.getArena();
        return participant.getTeamId() == 1 ? arena.getSpawnPoint1() : arena.getSpawnPoint2();
    }

    private boolean hasArenaBoundaries(DuelArena arena) {
        return false;
    }

    private boolean isWithinArenaBounds(DuelArena arena, Location location) {
        return true;
    }

    private boolean isWithinBounds(Location loc, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            return true;
        }
        if (loc.getWorld() == null || !loc.getWorld().equals((Object)pos1.getWorld())) {
            return false;
        }
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX && loc.getY() >= minY && loc.getY() <= maxY && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}

