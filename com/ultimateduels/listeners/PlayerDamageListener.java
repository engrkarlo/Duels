/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.AreaEffectCloud
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.TNTPrimed
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 *  org.bukkit.event.entity.ProjectileHitEvent
 *  org.bukkit.projectiles.ProjectileSource
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.duel.model.MatchType;
import com.ultimateduels.ffa.FFAManager;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerDamageListener
implements Listener {
    private final UltimateDuels plugin;
    private final Map<UUID, Long> friendlyFireMessageCooldown = new ConcurrentHashMap<UUID, Long>();
    private static final long FRIENDLY_FIRE_MSG_COOLDOWN_MS = 2000L;

    public PlayerDamageListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager != null && duelManager.isSpectating(victim.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (duelManager != null && duelManager.isInMatch(victim.getUniqueId())) {
            this.handleDuelEnvironmentalDamage(event, victim, duelManager);
            return;
        }
        if (ffaManager != null && ffaManager.isInFFAArena(victim)) {
            this.handleFFAEnvironmentalDamage(event, victim, ffaManager);
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(victim.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (this.isInLobby(victim) && this.plugin.getConfigManager().getLobbySettings().preventDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        boolean victimInFFA;
        boolean victimInDuel;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Player attacker = this.getPlayerAttacker(event);
        if (attacker == null) {
            return;
        }
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager != null && duelManager.isSpectating(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        boolean attackerInDuel = duelManager != null && duelManager.isInMatch(attacker.getUniqueId());
        boolean bl = victimInDuel = duelManager != null && duelManager.isInMatch(victim.getUniqueId());
        if (attackerInDuel || victimInDuel) {
            if (attackerInDuel && victimInDuel) {
                this.handleDuelPvP(event, attacker, victim, duelManager);
            } else {
                event.setCancelled(true);
            }
            return;
        }
        boolean attackerInFFA = ffaManager != null && ffaManager.isInFFAArena(attacker);
        boolean bl2 = victimInFFA = ffaManager != null && ffaManager.isInFFAArena(victim);
        if (attackerInFFA || victimInFFA) {
            if (attackerInFFA && victimInFFA) {
                this.handleFFAPvP(event, attacker, victim, ffaManager);
            } else {
                event.setCancelled(true);
            }
            return;
        }
        if (this.plugin.getQueueManager() != null && (this.plugin.getQueueManager().isInQueue(attacker.getUniqueId()) || this.plugin.getQueueManager().isInQueue(victim.getUniqueId()))) {
            event.setCancelled(true);
            return;
        }
        if ((this.isInLobby(attacker) || this.isInLobby(victim)) && this.plugin.getConfigManager().getLobbySettings().preventDamage()) {
            event.setCancelled(true);
        }
    }

    private void handleDuelEnvironmentalDamage(EntityDamageEvent event, Player victim, DuelManager duelManager) {
        DuelMatch match = duelManager.getMatch(victim.getUniqueId());
        if (match == null) {
            return;
        }
        if (!this.isActiveFightingState(match)) {
            event.setCancelled(true);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(9999.0);
        }
    }

    private void handleDuelPvP(EntityDamageByEntityEvent event, Player attacker, Player victim, DuelManager duelManager) {
        DuelMatch attackerMatch = duelManager.getMatch(attacker.getUniqueId());
        DuelMatch victimMatch = duelManager.getMatch(victim.getUniqueId());
        if (attackerMatch == null || victimMatch == null || !attackerMatch.getMatchId().equals(victimMatch.getMatchId())) {
            event.setCancelled(true);
            return;
        }
        DuelMatch match = attackerMatch;
        if (!this.isActiveFightingState(match)) {
            event.setCancelled(true);
            return;
        }
        DuelParticipant attackerP = match.getParticipant(attacker.getUniqueId());
        DuelParticipant victimP = match.getParticipant(victim.getUniqueId());
        if (attackerP == null || victimP == null) {
            event.setCancelled(true);
            return;
        }
        if (!victimP.isAlive()) {
            event.setCancelled(true);
            return;
        }
        if (attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        if (match.getMatchType() == MatchType.PARTY_FFA) {
            return;
        }
        int attackerTeam = attackerP.getTeamId();
        int victimTeam = victimP.getTeamId();
        if (attackerTeam != 0 && victimTeam != 0 && attackerTeam == victimTeam) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (this.isExplosionOrAreaDamage(cause)) {
                return;
            }
            boolean friendlyFireEnabled = this.getFriendlyFireSetting();
            if (!friendlyFireEnabled) {
                event.setCancelled(true);
                this.sendFriendlyFireMessage(attacker);
                return;
            }
        }
    }

    private boolean isExplosionOrAreaDamage(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;
    }

    private void handleFFAEnvironmentalDamage(EntityDamageEvent event, Player victim, FFAManager ffaManager) {
        if (ffaManager.hasSpawnProtection(victim.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(9999.0);
        }
    }

    private void handleFFAPvP(EntityDamageByEntityEvent event, Player attacker, Player victim, FFAManager ffaManager) {
        if (attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        String attackerArena = ffaManager.getPlayerArena(attacker.getUniqueId());
        String victimArena = ffaManager.getPlayerArena(victim.getUniqueId());
        if (attackerArena == null || !attackerArena.equals(victimArena)) {
            event.setCancelled(true);
            return;
        }
        if (ffaManager.hasSpawnProtection(victim.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage("\u00a7cThis player has spawn protection!");
            return;
        }
        ffaManager.handleDamage(attacker, victim);
    }

    private boolean isActiveFightingState(DuelMatch match) {
        MatchState state = match.getState();
        return state == MatchState.IN_PROGRESS;
    }

    private Player getPlayerAttacker(EntityDamageByEntityEvent event) {
        TNTPrimed tnt;
        Player player;
        AreaEffectCloud cloud;
        ProjectileSource projectileSource;
        Projectile projectile;
        ProjectileSource source;
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            Player player2 = (Player)damager;
            return player2;
        }
        if (damager instanceof Projectile && (source = (projectile = (Projectile)damager).getShooter()) instanceof Player) {
            Player player3 = (Player)source;
            return player3;
        }
        if (damager instanceof AreaEffectCloud && (projectileSource = (cloud = (AreaEffectCloud)damager).getSource()) instanceof Player) {
            player = (Player)projectileSource;
            return player;
        }
        if (damager instanceof TNTPrimed && (projectileSource = (tnt = (TNTPrimed)damager).getSource()) instanceof Player) {
            player = (Player)projectileSource;
            return player;
        }
        return null;
    }

    private boolean getFriendlyFireSetting() {
        try {
            return this.plugin.getConfig().getBoolean("party.friendly-fire", false);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("[PlayerDamageListener] Failed reading party.friendly-fire. Defaulting to FALSE.");
            return false;
        }
    }

    private void sendFriendlyFireMessage(Player attacker) {
        long now = System.currentTimeMillis();
        Long lastSent = this.friendlyFireMessageCooldown.get(attacker.getUniqueId());
        if (lastSent == null || now - lastSent >= 2000L) {
            attacker.sendMessage("\u00a7c\u2694 You cannot attack your own teammate!");
            this.friendlyFireMessageCooldown.put(attacker.getUniqueId(), now);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)projectile;
        ProjectileSource projectileSource = arrow.getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player shooter = (Player)projectileSource;
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager != null && duelManager.isSpectating(shooter.getUniqueId())) {
            event.getEntity().remove();
        }
    }

    private boolean isInLobby(Player player) {
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager != null && duelManager.isInMatch(player.getUniqueId())) {
            return false;
        }
        if (ffaManager != null && ffaManager.isInFFAArena(player)) {
            return false;
        }
        if (duelManager != null && duelManager.isSpectating(player.getUniqueId())) {
            return false;
        }
        Location lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn();
        if (lobbySpawn == null) {
            return true;
        }
        return player.getWorld().equals((Object)lobbySpawn.getWorld());
    }
}

