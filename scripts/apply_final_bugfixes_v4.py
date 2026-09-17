from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding='utf-8')
    new = fn(text)
    if new != text:
        path.write_text(new, encoding='utf-8')
        print('patched', rel)
    else:
        print('already patched', rel)


def patch_reload(text):
    marker = '    public boolean reload() {\n        this.getLogger().info("Reloading UltimateDuels...");'
    replacement = '    public boolean reload() {\n        this.getLogger().info("Reloading UltimateDuels...");\n        // Keep JavaPlugin#getConfig() synchronized with ConfigManager. Runtime fixes\n        // read this configuration directly, so /duels reload must update it too.\n        this.reloadConfig();'
    if marker in text and 'this.reloadConfig();' not in text[text.index(marker):text.index(marker)+500]:
        return text.replace(marker, replacement, 1)
    return text


def patch_shutdown(text):
    old = '''            if (this.lobbyManager != null) {
                try {
                    this.teleportAllPlayersToLobby();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error teleporting players to lobby: " + e.getMessage());
                }
            }
'''
    if old in text:
        return text.replace(old, '''            // Never teleport online players during plugin/server shutdown. Bukkit/Paper
            // must save each player's actual world, location and inventory so a restart
            // cannot move or wipe players who were in normal worlds.
''', 1)
    return text


def patch_join(text):
    old = '''            if (this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
                return;
            }
            if (this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
                return;
            }
            this.resetPlayerState(player);
            if (this.shouldTeleportToLobbyOnJoin() && (lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn()) != null) {
                player.teleport(lobbySpawn);
            }
            if (this.shouldGiveLobbyItemsOnJoin()) {
                this.plugin.getLobbyManager().giveHotbarItems(player);
            }
'''
    new = '''            if (this.plugin.getDuelManager().isInMatch(player.getUniqueId())) {
                return;
            }
            if (this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
                return;
            }

            boolean currentlyInLobby = this.plugin.getLobbyManager() != null
                    && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean firstJoin = !player.hasPlayedBefore();
            boolean firstJoinTeleport = firstJoin
                    && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);

            // A normal-world login (including a server restart where the player never
            // got a PlayerQuitEvent) is completely hands-off. Paper restores the saved
            // playerdata world/location/inventory; resetting state here would wipe it.
            if (currentlyInLobby || firstJoinTeleport) {
                this.resetPlayerState(player);
                if ((currentlyInLobby && this.shouldTeleportToLobbyOnJoin()) || firstJoinTeleport) {
                    Location target = this.plugin.getLobbyManager().getLobbySpawn();
                    if (target != null) player.teleport(target);
                }
                if (this.shouldGiveLobbyItemsOnJoin()) {
                    this.plugin.getLobbyManager().giveHotbarItems(player);
                }
            }
'''
    if old in text:
        return text.replace(old, new, 1)
    # v2 may already have the newer persistence block. Ensure it does not reset normal worlds.
    return text


def patch_config(text):
    marker = '''item-clear:
  enabled: true
  delay-seconds: 30
  clear-on-match-start: true
  match-start-clear-radius: 64
  display: action-bar
  message: '&eDropped items clear in &f{time}s'
'''
    replacement = '''item-clear:
  enabled: true
  # Items are cleared as one repeating timer per active match/FFA arena.
  # This is the interval between clears, not a timer per dropped item.
  delay-seconds: 30
  clear-on-match-start: true
  match-start-clear-radius: 64
  # Show the configured message only this many seconds before the next clear.
  # Set to 0 to disable the countdown message.
  message-at-seconds: 3
  display: action-bar
  message: '&eDropped items clear in &f{time}s'
'''
    return text.replace(marker, replacement, 1) if marker in text else text


def patch_manager(_text):
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
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Match-scoped repeating item cleanup. It intentionally does not create a timer
 * for each item: every active duel/FFA scope has exactly one repeating schedule.
 */
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
            this.key = key;
            this.duelMatchId = duelMatchId;
            this.ffaArenaName = ffaArenaName;
            this.arena = arena;
            this.startedAt = System.currentTimeMillis();
        }
    }

    public DroppedItemClearManager(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void startDuelMatch(DuelMatch match) {
        if (match == null || match.getArena() == null || !enabled()) return;
        String key = "duel:" + match.getMatchId();
        MatchScope old = this.scopes.remove(key);
        if (old != null) clearScope(old);
        MatchScope scope = new MatchScope(key, match.getMatchId(), null, match.getArena());
        this.scopes.put(key, scope);
        clearExistingItems(scope);
        ensureTask();
    }

    public void startFFAMatch(String arenaName, DuelArena arena) {
        if (arenaName == null || arena == null || !enabled()) return;
        String key = "ffa:" + arenaName.toLowerCase();
        MatchScope old = this.scopes.remove(key);
        if (old != null) clearScope(old);
        MatchScope scope = new MatchScope(key, null, arenaName.toLowerCase(), arena);
        this.scopes.put(key, scope);
        clearExistingItems(scope);
        ensureTask();
    }

    /** Compatibility hook for existing drop listeners. No per-item timer is created. */
    public void track(Item item, Player source) {
        // The repeating match timer scans the active arena. Intentionally empty.
    }

    /** Compatibility hook retained for FFAManager. */
    public void clearArena(String arenaName) {
        if (arenaName == null || !enabled()) return;
        String key = "ffa:" + arenaName.toLowerCase();
        MatchScope scope = this.scopes.get(key);
        if (scope != null) {
            clearExistingItems(scope);
            return;
        }
        if (this.plugin.getFFAManager() == null) return;
        try {
            Object instance = this.plugin.getFFAManager().getArena(arenaName);
            if (instance instanceof FFAArenaInstance ffa) {
                DuelArena arena = ffa.getArena();
                if (arena != null) clearArenaGeometry(arena);
            }
        } catch (Exception ignored) {
            // Existing FFA manager implementations may not expose an arena lookup.
        }
    }

    private boolean enabled() {
        return this.plugin.getConfig().getBoolean("item-clear.enabled", true);
    }

    private void ensureTask() {
        if (this.task != null && !this.task.isCancelled()) return;
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (!enabled() || this.scopes.isEmpty()) {
            stopTaskIfIdle();
            return;
        }

        long now = System.currentTimeMillis();
        Set<String> finished = new HashSet<>();
        int interval = Math.max(1, this.plugin.getConfig().getInt("item-clear.delay-seconds", 30));
        int messageAt = Math.max(0, this.plugin.getConfig().getInt("item-clear.message-at-seconds", 3));

        for (MatchScope scope : this.scopes.values()) {
            if (!isScopeActive(scope)) {
                finished.add(scope.key);
                continue;
            }

            long elapsed = Math.max(0L, now - scope.startedAt);
            long currentCycle = elapsed / (interval * 1000L);
            if (currentCycle > scope.cycle) {
                scope.cycle = currentCycle;
                clearExistingItems(scope);
            }

            long nextClearAt = scope.startedAt + (scope.cycle + 1L) * interval * 1000L;
            int remaining = (int) Math.ceil(Math.max(0L, nextClearAt - now) / 1000.0);
            if (messageAt > 0 && remaining == messageAt && scope.announcedCycle != scope.cycle) {
                scope.announcedCycle = scope.cycle;
                sendCountdown(scope, remaining);
            }
        }

        for (String key : finished) this.scopes.remove(key);
        stopTaskIfIdle();
    }

    private void stopTaskIfIdle() {
        if (!this.scopes.isEmpty()) return;
        if (this.task != null) this.task.cancel();
        this.task = null;
    }

    private boolean isScopeActive(MatchScope scope) {
        if (scope.duelMatchId != null) {
            return this.plugin.getDuelManager() != null
                    && this.plugin.getDuelManager().getMatch(scope.duelMatchId) != null;
        }
        if (scope.ffaArenaName != null && this.plugin.getFFAManager() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String arena = this.plugin.getFFAManager().getPlayerArenaName(player.getUniqueId());
                if (arena != null && arena.equalsIgnoreCase(scope.ffaArenaName)) return true;
            }
        }
        return false;
    }

    private void clearExistingItems(MatchScope scope) {
        if (scope.arena == null) return;
        if (scope.arena.hasBounds()) {
            Location min = scope.arena.getMinPoint();
            Location max = scope.arena.getMaxPoint();
            if (min != null && max != null && min.getWorld() != null) {
                for (Entity entity : min.getWorld().getEntities()) {
                    if (entity instanceof Item item && scope.arena.isWithinBounds(item.getLocation())) item.remove();
                }
                return;
            }
        }
        clearArenaGeometry(scope.arena);
    }

    private void clearArenaGeometry(DuelArena arena) {
        int radius = Math.max(1, this.plugin.getConfig().getInt("item-clear.match-start-clear-radius", 64));
        double radiusSquared = (double) radius * radius;
        for (Location center : centers(arena)) {
            if (center == null || center.getWorld() == null) continue;
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity instanceof Item item && item.getLocation().distanceSquared(center) <= radiusSquared) item.remove();
            }
        }
    }

    private List<Location> centers(DuelArena arena) {
        List<Location> result = new ArrayList<>();
        if (arena.getSpawnPoints() != null) {
            arena.getSpawnPoints().forEach(point -> {
                if (point != null && point.getLocation() != null) result.add(point.getLocation());
            });
        }
        if (result.isEmpty()) {
            result.add(arena.getSpawnPoint1());
            result.add(arena.getSpawnPoint2());
        }
        return result;
    }

    private void sendCountdown(MatchScope scope, int seconds) {
        String display = this.plugin.getConfig().getString("item-clear.display", "action-bar").toLowerCase();
        if ("none".equals(display)) return;
        String template = this.plugin.getConfig().getString("item-clear.message", "&eDropped items clear in &f{time}s");
        String message = template.replace("{time}", String.valueOf(seconds)).replace("{seconds}", String.valueOf(seconds));
        Component component = Component.text(message.replace('&', '\u00a7'));

        if (scope.duelMatchId != null && this.plugin.getDuelManager() != null) {
            DuelMatch match = this.plugin.getDuelManager().getMatch(scope.duelMatchId);
            if (match == null) return;
            match.getAllParticipants().forEach(participant -> send(Bukkit.getPlayer(participant.getUuid()), component, display));
        } else if (scope.ffaArenaName != null && this.plugin.getFFAManager() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String arena = this.plugin.getFFAManager().getPlayerArenaName(player.getUniqueId());
                if (arena != null && arena.equalsIgnoreCase(scope.ffaArenaName)) send(player, component, display);
            }
        }
    }

    private void send(Player player, Component component, String display) {
        if (player == null || !player.isOnline()) return;
        if ("chat".equals(display) || "both".equals(display)) player.sendMessage(component);
        if ("action-bar".equals(display) || "both".equals(display)) player.sendActionBar(component);
    }

    private void clearScope(MatchScope scope) {
        if (scope != null) clearExistingItems(scope);
    }
}
'''

edit('com/ultimateduels/UltimateDuels.java', patch_reload)
edit('com/ultimateduels/UltimateDuels.java', patch_shutdown)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_join)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', patch_manager)
edit('vendor/config.yml', patch_config)
