/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.entity.AreaEffectCloud
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageCause
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.projectiles.ProjectileSource
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.config.ConfigManager;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.utils.MessageUtils;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener
implements Listener {
    private final UltimateDuels plugin;
    private final DuelManager duelManager;
    private final FFAManager ffaManager;

    public PlayerDeathListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
        this.ffaManager = plugin.getFFAManager();
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageEvent event) {
        boolean inFFA;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player victim = (Player)entity;
        if (this.plugin.getWorldRestrictionManager() != null && !this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(victim.getWorld())) {
            return;
        }
        boolean inDuel = this.duelManager != null && this.duelManager.isInDuel(victim);
        boolean bl = inFFA = this.ffaManager != null && this.ffaManager.isInFFAArena(victim);
        if (!inDuel && !inFFA) {
            return;
        }
        double finalHealth = victim.getHealth() - event.getFinalDamage();
        if (finalHealth > 0.0) {
            return;
        }
        if (this.hasTotemOfUndying(victim)) {
            this.plugin.getLogger().fine("[DEATH] " + victim.getName() + " has Totem of Undying");
            return;
        }
        Player killer = this.extractKiller(event);
        if (inFFA) {
            boolean dropItems = this.ffaManager != null && this.ffaManager.shouldDropItemsOnDeath();
            if (dropItems) {
                return;
            }
            event.setCancelled(true);
            Player finalKiller = killer;
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (!victim.isOnline()) {
                    return;
                }
                victim.setHealth(victim.getMaxHealth());
                victim.setFireTicks(0);
                victim.setFallDistance(0.0f);
                victim.setNoDamageTicks(20);
                if (this.ffaManager.isInFFAArena(victim)) {
                    this.ffaManager.handleDeath(victim, finalKiller);
                }
            }, 1L);
            return;
        }
        final Player finalKiller = killer;
        EntityDamageEvent finalKillEvent = event;
        final String deathMessage = this.buildDeathMessage(victim, finalKiller, finalKillEvent);
        new BukkitRunnable(){

            public void run() {
                if (!victim.isOnline()) {
                    return;
                }
                if (victim.isDead()) {
                    victim.spigot().respawn();
                }
                Bukkit.getScheduler().runTaskLater((Plugin)PlayerDeathListener.this.plugin, () -> {
                    if (!victim.isOnline()) {
                        return;
                    }
                    victim.setGameMode(GameMode.SURVIVAL);
                    victim.setAllowFlight(false);
                    victim.setFlying(false);
                    victim.setHealth(victim.getMaxHealth());
                    victim.setFireTicks(0);
                    victim.setFallDistance(0.0f);
                    victim.setNoDamageTicks(0);
                    PlayerDeathListener.this.broadcastToMatch(victim, deathMessage);
                    PlayerDeathListener.this.duelManager.handleDeath(victim, finalKiller);
                }, 2L);
            }
        }.runTaskLater((Plugin)this.plugin, 20L);
    }

    private Player extractKiller(EntityDamageEvent event) {
        AreaEffectCloud cloud;
        ProjectileSource projectileSource;
        Projectile projectile;
        ProjectileSource source;
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return null;
        }
        EntityDamageByEntityEvent byEntity = (EntityDamageByEntityEvent)event;
        Entity damager = byEntity.getDamager();
        if (damager instanceof Player) {
            Player player = (Player)damager;
            return player;
        }
        if (damager instanceof Projectile && (source = (projectile = (Projectile)damager).getShooter()) instanceof Player) {
            Player player = (Player)source;
            return player;
        }
        if (damager instanceof AreaEffectCloud && (projectileSource = (cloud = (AreaEffectCloud)damager).getSource()) instanceof Player) {
            Player player = (Player)projectileSource;
            return player;
        }
        return null;
    }

    private boolean hasTotemOfUndying(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return mainHand.getType() == Material.TOTEM_OF_UNDYING || offHand.getType() == Material.TOTEM_OF_UNDYING;
    }

    private String buildDeathMessage(Player victim, Player killer, EntityDamageEvent event) {
        Projectile proj;
        EntityDamageByEntityEvent byEntity;
        Entity damager;
        String victimName = victim.getName();
        if (killer != null) {
            String killerName = killer.getName();
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            String weaponName = this.getWeaponName(weapon);
            if (weaponName != null) {
                return "&c" + victimName + " &7was slain by &a" + killerName + " &7using &f" + weaponName;
            }
            return "&c" + victimName + " &7was slain by &a" + killerName;
        }
        if (event instanceof EntityDamageByEntityEvent && (damager = (byEntity = (EntityDamageByEntityEvent)event).getDamager()) instanceof Projectile && (proj = (Projectile)damager).getShooter() == null) {
            return "&c" + victimName + " &7was blown away";
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        return switch (cause) {
            case EntityDamageEvent.DamageCause.VOID -> "&c" + victimName + " &7fell into the void";
            case EntityDamageEvent.DamageCause.FALL -> "&c" + victimName + " &7fell to their death";
            case EntityDamageEvent.DamageCause.FIRE, EntityDamageEvent.DamageCause.FIRE_TICK, EntityDamageEvent.DamageCause.LAVA -> "&c" + victimName + " &7burned to death";
            case EntityDamageEvent.DamageCause.DROWNING -> "&c" + victimName + " &7drowned";
            case EntityDamageEvent.DamageCause.SUFFOCATION -> "&c" + victimName + " &7suffocated";
            case EntityDamageEvent.DamageCause.STARVATION -> "&c" + victimName + " &7starved to death";
            case EntityDamageEvent.DamageCause.POISON -> "&c" + victimName + " &7was poisoned";
            case EntityDamageEvent.DamageCause.WITHER -> "&c" + victimName + " &7withered away";
            case EntityDamageEvent.DamageCause.LIGHTNING -> "&c" + victimName + " &7was struck by lightning";
            case EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION -> "&c" + victimName + " &7was blown up";
            case EntityDamageEvent.DamageCause.PROJECTILE -> "&c" + victimName + " &7was shot";
            default -> "&c" + victimName + " &7died";
        };
    }

    private void broadcastToMatch(Player victim, String message) {
        if (this.duelManager == null) {
            return;
        }
        DuelMatch match = this.duelManager.getMatch(victim.getUniqueId());
        if (match == null) {
            return;
        }
        for (DuelParticipant participant : match.getAllParticipants()) {
            Player p = Bukkit.getPlayer((UUID)participant.getUuid());
            if (p == null) continue;
            MessageUtils.sendMessage(p, message);
        }
        for (UUID spectatorUUID : this.duelManager.getMatchSpectators(match.getMatchId())) {
            Player spectator = Bukkit.getPlayer((UUID)spectatorUUID);
            if (spectator == null) continue;
            MessageUtils.sendMessage(spectator, message);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (this.plugin.getWorldRestrictionManager() != null && !this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(victim.getWorld())) {
            return;
        }
        if (this.duelManager != null && this.duelManager.isInDuel(victim)) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            return;
        }
        if (this.ffaManager != null && this.ffaManager.isInFFAArena(victim)) {
            event.setDeathMessage(null);
            event.setDroppedExp(0);
            boolean dropItems = this.ffaManager.shouldDropItemsOnDeath();
            event.setKeepInventory(!dropItems);
            event.setKeepLevel(true);
            if (!dropItems) {
                event.getDrops().clear();
            }
            final Player finalKiller = killer;
            new BukkitRunnable(){

                public void run() {
                    if (!victim.isOnline()) {
                        return;
                    }
                    if (victim.isDead()) {
                        victim.spigot().respawn();
                    }
                    Bukkit.getScheduler().runTaskLater((Plugin)PlayerDeathListener.this.plugin, () -> {
                        if (!victim.isOnline()) {
                            return;
                        }
                        victim.setHealth(victim.getMaxHealth());
                        victim.setFireTicks(0);
                        victim.setFallDistance(0.0f);
                        victim.setNoDamageTicks(20);
                        if (PlayerDeathListener.this.ffaManager.isInFFAArena(victim)) {
                            PlayerDeathListener.this.ffaManager.handleDeath(victim, finalKiller);
                        }
                    }, 2L);
                }
            }.runTaskLater((Plugin)this.plugin, 1L);
            return;
        }
        if (this.duelManager != null && this.duelManager.isSpectating(victim.getUniqueId())) {
            event.setCancelled(true);
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
            new BukkitRunnable(){

                public void run() {
                    if (victim.isOnline() && victim.isDead()) {
                        victim.spigot().respawn();
                    }
                    Bukkit.getScheduler().runTaskLater((Plugin)PlayerDeathListener.this.plugin, () -> {
                        if (victim.isOnline()) {
                            PlayerDeathListener.this.duelManager.removeSpectator(victim);
                        }
                    }, 2L);
                }
            }.runTaskLater((Plugin)this.plugin, 1L);
            return;
        }
        if (this.isInLobbyWorld(victim)) {
            this.handleLobbyDeath(event, victim);
        }
    }

    private void handleFFADeath(PlayerDeathEvent event, Player victim, Player killer) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        victim.setHealth(victim.getMaxHealth());
        victim.setFireTicks(0);
        victim.setFallDistance(0.0f);
        if (this.ffaManager != null) {
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                if (victim.isOnline()) {
                    this.ffaManager.handleDeath(victim, killer);
                }
            }, 1L);
        }
    }

    private void handleLobbyDeath(PlayerDeathEvent event, Player victim) {
        ConfigManager.LobbySettings lobbySettings = this.plugin.getConfigManager().getLobbySettings();
        if (lobbySettings.preventDamage()) {
            event.setDeathMessage(null);
        }
        if (!lobbySettings.clearInventory()) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
        }
    }

    private String getWeaponName(ItemStack weapon) {
        if (weapon == null || weapon.getType().isAir()) {
            return null;
        }
        if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
            return weapon.getItemMeta().getDisplayName();
        }
        String name = weapon.getType().name().toLowerCase().replace("_", " ");
        return this.capitalize(name);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (this.plugin.getWorldRestrictionManager() != null) {
            String worldName = player.getWorld().getName();
            if (!this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(worldName)) {
                this.plugin.debug("Player " + player.getName() + " respawning in restricted world: " + worldName + " - not interfering");
                return;
            }
        }
        if (this.duelManager != null && this.duelManager.isInDuel(player)) {
            DuelMatch match = this.duelManager.getMatch(uuid);
            if (match != null) {
                DuelParticipant participant;
                Location spectatorSpawn = match.getArena().getSpectatorSpawn();
                if (spectatorSpawn == null) {
                    spectatorSpawn = match.getArena().getCenter();
                }
                if (spectatorSpawn == null && (participant = match.getParticipant(uuid)) != null) {
                    Location location = spectatorSpawn = participant.getTeamId() == 1 ? match.getArena().getSpawnPoint1() : match.getArena().getSpawnPoint2();
                }
                if (spectatorSpawn != null) {
                    event.setRespawnLocation(spectatorSpawn);
                }
                Player finalPlayer = player;
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
                    if (finalPlayer.isOnline()) {
                        finalPlayer.setGameMode(GameMode.SURVIVAL);
                        finalPlayer.setAllowFlight(false);
                        finalPlayer.setFlying(false);
                    }
                }, 1L);
            }
            return;
        }
        if (this.ffaManager != null && this.ffaManager.isInFFAArena(player)) {
            FFAArenaInstance arena;
            String arenaName = this.ffaManager.getPlayerArena(uuid);
            if (arenaName != null && (arena = this.ffaManager.getArena(arenaName)) != null) {
                Location spawn = arena.getArena().getRandomSpawnPoint();
                if (spawn == null) {
                    spawn = arena.getArena().getSpawnPoint1();
                }
                if (spawn != null) {
                    event.setRespawnLocation(spawn);
                }
            }
            return;
        }
        Location lobby = this.plugin.getLobbyManager().getLobbySpawn();
        if (lobby != null && lobby.getWorld() != null) {
            boolean lobbyManagesWorld;
            boolean inLobbyWorld = player.getWorld().equals((Object)lobby.getWorld());
            boolean bl = lobbyManagesWorld = this.plugin.getConfigManager().getLobbySettings().enabled() && this.plugin.getLobbyManager().isLobbyWorld(player.getWorld());
            if (inLobbyWorld || lobbyManagesWorld) {
                event.setRespawnLocation(lobby);
                new BukkitRunnable(){

                    public void run() {
                        if (player.isOnline()) {
                            boolean inFFA;
                            boolean inDuel = PlayerDeathListener.this.duelManager != null && PlayerDeathListener.this.duelManager.isInDuel(player);
                            boolean bl = inFFA = PlayerDeathListener.this.ffaManager != null && PlayerDeathListener.this.ffaManager.isInFFAArena(player);
                            if (!inDuel && !inFFA) {
                                PlayerDeathListener.this.plugin.getLobbyManager().giveHotbarItems(player);
                                if (PlayerDeathListener.this.plugin.getScoreboardManager() != null) {
                                    PlayerDeathListener.this.plugin.getScoreboardManager().refreshScoreboardType(player);
                                }
                            }
                        }
                    }
                }.runTaskLater((Plugin)this.plugin, 1L);
            }
        }
    }

    private boolean isInLobbyWorld(Player player) {
        Location lobby = this.plugin.getLobbyManager().getLobbySpawn();
        if (lobby == null || lobby.getWorld() == null) {
            return false;
        }
        return player.getWorld().equals((Object)lobby.getWorld());
    }
}

