from pathlib import Path
import re

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
    if marker in text and 'this.reloadConfig();' not in text[text.index(marker):text.index(marker)+450]:
        return text.replace(marker, marker + '\n        // Refresh JavaPlugin config as well as ConfigManager so runtime listeners see edits immediately.\n        this.reloadConfig();', 1)
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
    return text.replace(old, '''            // Do not teleport players during shutdown. Paper saves their real world/location/inventory.
''', 1) if old in text else text


def patch_join(text):
    pattern = re.compile(r'''            boolean inLobbyWorld = this\.plugin\.getLobbyManager\(\) != null && this\.plugin\.getLobbyManager\(\)\.isInLobbyWorld\(player\);.*?            if \(this\.shouldGiveLobbyItemsOnJoin\(\)\) \{\n                this\.plugin\.getLobbyManager\(\)\.giveHotbarItems\(player\);\n            \}\n''', re.S)
    replacement = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null
                    && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean firstJoin = !player.hasPlayedBefore();
            boolean firstJoinTeleport = firstJoin
                    && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);

            // Normal-world players are never reset or teleported by UltimateDuels.
            // This is critical when the server restarts before PlayerQuitEvent fires:
            // Paper restores the saved world, location, inventory and offhand state.
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
    new, count = pattern.subn(replacement, text, count=1)
    return new if count else text


def patch_prepare(text):
    if 'message-at-seconds:' in text:
        return text
    needle = "  match-start-clear-radius: 64\n  # Countdown location: action-bar, chat, both, or none."
    if needle in text:
        return text.replace(needle, "  match-start-clear-radius: 64\n  # Show the countdown only this many seconds before each clear. 0 disables it.\n  message-at-seconds: 3\n  # Countdown location: action-bar, chat, both, or none.", 1)
    return text


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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** One repeating cleanup loop per active match/FFA arena; never one timer per item. */
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

    /** Compatibility method: item cleanup is controlled by the match loop, not per drop. */
    public void track(Item item, Player source) { }

    /** Compatibility method retained for existing FFA code. */
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
        if (this.task != null && !this.task.isCancelled()) return;
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
            if (!isActive(scope)) {
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
            int remaining = (int)Math.ceil(Math.max(0L, nextClearAt - now) / 1000.0);
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
            if (min != null && min.getWorld() != null) {
                for (Entity entity : min.getWorld().getEntities()) {
                    if (entity instanceof Item item && scope.arena.isWithinBounds(item.getLocation())) item.remove();
                }
                return;
            }
        }
        int radius = Math.max(1, this.plugin.getConfig().getInt("item-clear.match-start-clear-radius", 64));
        double r2 = (double)radius * radius;
        for (Location center : centers(scope.arena)) {
            if (center == null || center.getWorld() == null) continue;
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity instanceof Item item && item.getLocation().distanceSquared(center) <= r2) item.remove();
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
        Component component = Component.text(template.replace("{time}", String.valueOf(seconds)).replace("{seconds}", String.valueOf(seconds)).replace('&', '\u00a7'));

        if (scope.duelMatchId != null && this.plugin.getDuelManager() != null) {
            DuelMatch match = this.plugin.getDuelManager().getMatch(scope.duelMatchId);
            if (match != null) {
                match.getAllParticipants().forEach(p -> send(Bukkit.getPlayer(p.getUuid()), component, display));
            }
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
}
'''

edit('com/ultimateduels/UltimateDuels.java', patch_reload)
edit('com/ultimateduels/UltimateDuels.java', patch_shutdown)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_join)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', patch_manager)
edit('scripts/prepare_patch_jar.py', patch_prepare)
