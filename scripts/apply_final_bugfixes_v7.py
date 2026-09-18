from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

def edit(rel, fn):
    p = ROOT / rel
    t = p.read_text(encoding='utf-8')
    n = fn(t)
    if n != t:
        p.write_text(n, encoding='utf-8')
        print('patched', rel)
    else:
        print('already patched', rel)

def reload_fix(t):
    marker = '    public boolean reload() {\n        this.getLogger().info("Reloading UltimateDuels...");'
    if marker in t and 'this.reloadConfig();' not in t[t.index(marker):t.index(marker)+400]:
        return t.replace(marker, marker + '\n        this.reloadConfig();', 1)
    return t

def shutdown_fix(t):
    old = '''            if (this.lobbyManager != null) {
                try {
                    this.teleportAllPlayersToLobby();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error teleporting players to lobby: " + e.getMessage());
                }
            }
'''
    return t.replace(old, '            // Do not teleport online players during shutdown; Paper saves their real playerdata.\n', 1) if old in t else t

def join_fix(t):
    # Replace the older runtime-fix join block that reset every player or teleported them.
    old = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean teleportToLobby = this.shouldTeleportToLobbyOnJoin()
                    && this.plugin.getLobbyManager() != null
                    && (lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn()) != null;
            if (teleportToLobby) {
                this.resetPlayerState(player);
                player.teleport(lobbySpawn);
                inLobbyWorld = true;
            } else if (inLobbyWorld) {
                this.resetPlayerState(player);
            }
            if (inLobbyWorld && this.shouldGiveLobbyItemsOnJoin()) {
                this.plugin.getLobbyManager().giveHotbarItems(player);
            }
'''
    new = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null
                    && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean firstJoinTeleport = !player.hasPlayedBefore()
                    && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);

            // Never reset/teleport a player who is returning to a normal world. This
            // covers a restart where PlayerQuitEvent never fired and preserves Paper's
            // saved world, exact location, inventory, armor and offhand.
            if (inLobbyWorld || firstJoinTeleport) {
                this.resetPlayerState(player);
                if (firstJoinTeleport || this.shouldTeleportToLobbyOnJoin()) {
                    Location targetLobby = this.plugin.getLobbyManager().getLobbySpawn();
                    if (targetLobby != null) player.teleport(targetLobby);
                }
                if (this.shouldGiveLobbyItemsOnJoin()) {
                    this.plugin.getLobbyManager().giveHotbarItems(player);
                }
            }
'''
    return t.replace(old, new, 1) if old in t else t

def prepare_fix(t):
    if 'message-at-seconds:' in t:
        return t
    return t.replace('  match-start-clear-radius: 64\n  # Countdown location:', '  match-start-clear-radius: 64\n  # Show the message only this many seconds before each clear. 0 disables it.\n  message-at-seconds: 3\n  # Countdown location:', 1)

def manager_fix(_t):
    return r'''package com.ultimateduels.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelMatch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** One repeating cleanup loop per active duel/FFA scope; never one timer per item. */
public final class DroppedItemClearManager {
    private final UltimateDuels plugin;
    private final Map<String, MatchScope> scopes = new ConcurrentHashMap<>();
    private BukkitTask task;

    private static final class MatchScope {
        private final String key;
        private final UUID duelMatchId;
        private final String ffaArenaName;
        private final DuelArena arena;
        private final long startedAt;
        private long cycle;
        private long announcedCycle = -1L;
        private MatchScope(String key, UUID duelMatchId, String ffaArenaName, DuelArena arena) {
            this.key = key; this.duelMatchId = duelMatchId; this.ffaArenaName = ffaArenaName;
            this.arena = arena; this.startedAt = System.currentTimeMillis();
        }
    }

    public DroppedItemClearManager(UltimateDuels plugin) { this.plugin = plugin; }

    public void startDuelMatch(DuelMatch match) {
        if (match == null || match.getArena() == null || !enabled()) return;
        String key = "duel:" + match.getMatchId();
        MatchScope old = this.scopes.remove(key);
        if (old != null) clearExistingItems(old);
        MatchScope scope = new MatchScope(key, match.getMatchId(), null, match.getArena());
        this.scopes.put(key, scope);
        clearExistingItems(scope);
        ensureTask();
    }

    public void startFFAMatch(String arenaName, DuelArena arena) {
        if (arenaName == null || arena == null || !enabled()) return;
        String key = "ffa:" + arenaName.toLowerCase();
        MatchScope old = this.scopes.remove(key);
        if (old != null) clearExistingItems(old);
        MatchScope scope = new MatchScope(key, null, arenaName.toLowerCase(), arena);
        this.scopes.put(key, scope);
        clearExistingItems(scope);
        ensureTask();
    }

    public void track(Item item, Player source) { }

    public void clearArena(String arenaName) {
        if (arenaName == null) return;
        MatchScope scope = this.scopes.get("ffa:" + arenaName.toLowerCase());
        if (scope != null) clearExistingItems(scope);
    }

    private boolean enabled() {
        return this.plugin.getConfig().getBoolean("item-clear.enabled", true)
                && this.plugin.getConfig().getBoolean("item-clear.clear-on-match-start", true);
    }

    private void ensureTask() {
        if (this.task == null || this.task.isCancelled())
            this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (!enabled() || this.scopes.isEmpty()) {
            if (this.scopes.isEmpty() && this.task != null) this.task.cancel();
            if (this.scopes.isEmpty()) this.task = null;
            return;
        }
        long now = System.currentTimeMillis();
        int interval = Math.max(1, this.plugin.getConfig().getInt("item-clear.delay-seconds", 30));
        int messageAt = Math.max(0, this.plugin.getConfig().getInt("item-clear.message-at-seconds", 3));
        Set<String> finished = new HashSet<>();
        for (MatchScope scope : this.scopes.values()) {
            if (!isActive(scope)) { finished.add(scope.key); continue; }
            long elapsed = Math.max(0L, now - scope.startedAt);
            long currentCycle = elapsed / (interval * 1000L);
            if (currentCycle > scope.cycle) {
                scope.cycle = currentCycle;
                clearExistingItems(scope);
            }
            long nextClear = scope.startedAt + (scope.cycle + 1L) * interval * 1000L;
            int remaining = (int)Math.ceil(Math.max(0L, nextClear - now) / 1000.0);
            if (messageAt > 0 && remaining == messageAt && scope.announcedCycle != scope.cycle) {
                scope.announcedCycle = scope.cycle;
                sendCountdown(scope, remaining);
            }
        }
        for (String key : finished) this.scopes.remove(key);
        if (this.scopes.isEmpty()) {
            if (this.task != null) this.task.cancel();
            this.task = null;
        }
    }

    private boolean isActive(MatchScope scope) {
        if (scope.duelMatchId != null)
            return this.plugin.getDuelManager() != null && this.plugin.getDuelManager().getMatch(scope.duelMatchId) != null;
        if (scope.ffaArenaName != null && this.plugin.getFFAManager() != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                String a = this.plugin.getFFAManager().getPlayerArenaName(p.getUniqueId());
                if (a != null && a.equalsIgnoreCase(scope.ffaArenaName)) return true;
            }
        }
        return false;
    }

    private void clearExistingItems(MatchScope scope) {
        DuelArena arena = scope.arena;
        if (arena == null) return;
        if (arena.hasBounds()) {
            Location min = arena.getMinPoint();
            if (min != null && min.getWorld() != null) {
                for (Entity e : min.getWorld().getEntities())
                    if (e instanceof Item item && arena.isWithinBounds(item.getLocation())) item.remove();
                return;
            }
        }
        int radius = Math.max(1, this.plugin.getConfig().getInt("item-clear.match-start-clear-radius", 64));
        double r2 = (double)radius * radius;
        for (Location center : centers(arena)) {
            if (center == null || center.getWorld() == null) continue;
            for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius))
                if (e instanceof Item item && item.getLocation().distanceSquared(center) <= r2) item.remove();
        }
    }

    private List<Location> centers(DuelArena arena) {
        List<Location> result = new ArrayList<>();
        if (arena.getSpawnPoints() != null) arena.getSpawnPoints().forEach(p -> { if (p != null && p.getLocation() != null) result.add(p.getLocation()); });
        if (result.isEmpty()) { result.add(arena.getSpawnPoint1()); result.add(arena.getSpawnPoint2()); }
        return result;
    }

    private void sendCountdown(MatchScope scope, int seconds) {
        String display = this.plugin.getConfig().getString("item-clear.display", "action-bar").toLowerCase();
        if ("none".equals(display)) return;
        String template = this.plugin.getConfig().getString("item-clear.message", "&eDropped items clear in &f{time}s");
        Component c = Component.text(template.replace("{time}", String.valueOf(seconds)).replace("{seconds}", String.valueOf(seconds)).replace('&', '\u00a7'));
        if (scope.duelMatchId != null && this.plugin.getDuelManager() != null) {
            DuelMatch m = this.plugin.getDuelManager().getMatch(scope.duelMatchId);
            if (m != null) m.getAllParticipants().forEach(p -> send(Bukkit.getPlayer(p.getUuid()), c, display));
        } else if (scope.ffaArenaName != null && this.plugin.getFFAManager() != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                String a = this.plugin.getFFAManager().getPlayerArenaName(p.getUniqueId());
                if (a != null && a.equalsIgnoreCase(scope.ffaArenaName)) send(p, c, display);
            }
        }
    }

    private void send(Player p, Component c, String display) {
        if (p == null || !p.isOnline()) return;
        if ("chat".equals(display) || "both".equals(display)) p.sendMessage(c);
        if ("action-bar".equals(display) || "both".equals(display)) p.sendActionBar(c);
    }
}
'''

edit('com/ultimateduels/UltimateDuels.java', reload_fix)
edit('com/ultimateduels/UltimateDuels.java', shutdown_fix)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', join_fix)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', manager_fix)
edit('scripts/prepare_patch_jar.py', prepare_fix)
print('final v7 fixes applied')
