from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    p = ROOT / rel
    if not p.exists():
        raise RuntimeError(f'Missing generated source: {rel}')
    old = p.read_text(encoding='utf-8')
    new = fn(old)
    if new != old:
        p.write_text(new, encoding='utf-8')
        print('patched', rel)
    else:
        print('already patched', rel)


def patch_command_listener(t):
    # ConfigManager is the live configuration used by /duels reload. JavaPlugin#getConfig()
    # is not the config instance reloaded by UltimateDuels' ConfigManager.
    t = t.replace('this.plugin.getConfig()', 'this.plugin.getConfigManager().getConfig()')
    return t


def patch_ffa_manager(t):
    # Keep one cleanup manager instance. The manager itself owns the repeating match loop.
    t = t.replace('new DroppedItemClearManager(this.plugin)', 'DroppedItemClearManager.get(this.plugin)')
    return t


def patch_drop_listener(t):
    t = t.replace('new DroppedItemClearManager(plugin)', 'DroppedItemClearManager.get(plugin)')
    # No per-item timer is allowed. track() is intentionally a compatibility no-op in v4.
    return t


def patch_ultimate_duels(t):
    # Never teleport all online players during plugin shutdown. Players still online during
    # a server restart must retain the world/location that Paper saves in playerdata.
    pattern = re.compile(
        r'(?m)^\s*if \(this\.lobbyManager != null\) \{\s*'
        r'this\.teleportAllPlayersToLobby\(\);\s*\}\s*'
    )
    new, count = pattern.subn('', t, count=1)
    if count:
        return new

    # Some decompiler variants put the call on one line.
    new = re.sub(r'(?m)^\s*this\.teleportAllPlayersToLobby\(\);\s*$', '', t, count=1)
    return new


def rewrite_item_manager(_t):
    return r'''package com.ultimateduels.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Match-scoped item cleanup.
 *
 * Items are NOT given individual timers. One repeating cycle belongs to each active
 * duel/FFA match. A cycle clears the arena, then the next cycle is scheduled from
 * that same match scope. This prevents duplicate timers/messages and keeps normal
 * survival-world drops untouched.
 */
public final class DroppedItemClearManager {
    private static DroppedItemClearManager instance;

    private final UltimateDuels plugin;
    private final Map<String, Long> nextClearAt = new HashMap<>();
    private final Set<String> warnedCycles = new HashSet<>();
    private BukkitTask task;
    private int lastIntervalSeconds = -1;

    public DroppedItemClearManager(UltimateDuels plugin) {
        this.plugin = plugin;
        if (instance == null) {
            instance = this;
            ensureTask();
        }
    }

    public static DroppedItemClearManager get(UltimateDuels plugin) {
        if (instance == null || instance.plugin != plugin) {
            instance = new DroppedItemClearManager(plugin);
        } else {
            instance.ensureTask();
        }
        return instance;
    }

    /** Compatibility hook for existing death/drop code. No per-item timer is created. */
    public void track(Item item, Player source) {
        // Deliberately empty. Cleanup is match-scoped and handled by tick().
    }

    /** Compatibility hook retained for existing FFA join code. */
    public void clearArena(String arenaName) {
        if (this.plugin.getFFAManager() == null || arenaName == null) return;
        FFAArenaInstance instance = this.plugin.getFFAManager().getArena(arenaName);
        if (instance != null && instance.getArena() != null) {
            clearArena(instance.getArena());
        }
    }

    private void ensureTask() {
        if (this.task != null && !this.task.isCancelled()) return;
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (!this.plugin.getConfigManager().getConfig().getBoolean("item-clear.enabled", true)) {
            this.nextClearAt.clear();
            this.warnedCycles.clear();
            return;
        }

        int interval = this.plugin.getConfigManager().getConfig().getInt("item-clear.interval-seconds", -1);
        if (interval < 0) {
            // Backward-compatible with #72 configuration.
            interval = this.plugin.getConfigManager().getConfig().getInt("item-clear.delay-seconds", 30);
        }
        interval = Math.max(1, interval);

        int warning = this.plugin.getConfigManager().getConfig().getInt("item-clear.warning-seconds", 3);
        warning = Math.max(0, Math.min(warning, interval));

        long now = System.currentTimeMillis();
        if (interval != this.lastIntervalSeconds) {
            this.lastIntervalSeconds = interval;
            this.nextClearAt.replaceAll((key, value) -> now + interval * 1000L);
            this.warnedCycles.clear();
        }

        Set<String> activeScopes = new HashSet<>();

        if (this.plugin.getDuelManager() != null) {
            for (DuelMatch match : this.plugin.getDuelManager().getActiveMatches()) {
                if (match == null || match.getArena() == null) continue;
                String key = "duel:" + match.getMatchId();
                activeScopes.add(key);
                this.processScope(key, match.getArena(), match.getAllParticipants(), interval, warning, now);
            }
        }

        if (this.plugin.getFFAManager() != null) {
            for (FFAArenaInstance ffa : this.plugin.getFFAManager().getAllArenas()) {
                if (ffa == null || ffa.getArena() == null || ffa.getPlayerCount() <= 0) continue;
                String key = "ffa:" + ffa.getArenaName().toLowerCase();
                activeScopes.add(key);
                this.processScope(key, ffa.getArena(), ffa.getPlayers(), interval, warning, now);
            }
        }

        this.nextClearAt.keySet().removeIf(key -> !activeScopes.contains(key));
        this.warnedCycles.removeIf(key -> !activeScopes.contains(key));
    }

    private void processScope(String key, DuelArena arena, Object participants, int interval, int warning, long now) {
        Long next = this.nextClearAt.get(key);
        if (next == null) {
            // Match start: clear existing items immediately, then begin the repeating cycle.
            this.clearArena(arena);
            next = now + interval * 1000L;
            this.nextClearAt.put(key, next);
            this.warnedCycles.remove(key);
            return;
        }

        long remainingMillis = next - now;
        if (remainingMillis <= 0L) {
            this.clearArena(arena);
            next = now + interval * 1000L;
            this.nextClearAt.put(key, next);
            this.warnedCycles.remove(key);
            return;
        }

        int remaining = (int) Math.ceil(remainingMillis / 1000.0D);
        if (warning > 0 && remaining <= warning && !this.warnedCycles.contains(key)) {
            if (this.hasItems(arena)) {
                this.warnParticipants(participants, remaining);
            }
            this.warnedCycles.add(key);
        }
    }

    private boolean hasItems(DuelArena arena) {
        if (arena.hasBounds() && arena.getCenter() != null) {
            Location min = arena.getMinPoint();
            Location max = arena.getMaxPoint();
            if (min != null && max != null && min.getWorld() != null && min.getWorld() == max.getWorld()) {
                double x = Math.max(1.0D, Math.abs(max.getX() - min.getX()) / 2.0D + 2.0D);
                double y = Math.max(1.0D, Math.abs(max.getY() - min.getY()) / 2.0D + 2.0D);
                double z = Math.max(1.0D, Math.abs(max.getZ() - min.getZ()) / 2.0D + 2.0D);
                for (Entity entity : min.getWorld().getNearbyEntities(arena.getCenter(), x, y, z)) {
                    if (entity instanceof Item && arena.isWithinBounds(entity.getLocation())) return true;
                }
                return false;
            }
        }
        int radius = Math.max(1, this.plugin.getConfigManager().getConfig().getInt("item-clear.match-start-clear-radius", 64));
        for (Location spawn : arena.getSpawnPoints().stream().map(sp -> sp.getLocation()).toList()) {
            if (spawn == null || spawn.getWorld() == null) continue;
            for (Entity entity : spawn.getWorld().getNearbyEntities(spawn, radius, radius, radius)) {
                if (entity instanceof Item) return true;
            }
        }
        return false;
    }

    private void clearArena(DuelArena arena) {
        if (arena == null) return;
        if (arena.hasBounds()) {
            Location min = arena.getMinPoint();
            Location max = arena.getMaxPoint();
            Location center = arena.getCenter();
            if (min != null && max != null && center != null && min.getWorld() != null && min.getWorld() == max.getWorld()) {
                double x = Math.max(1.0D, Math.abs(max.getX() - min.getX()) / 2.0D + 2.0D);
                double y = Math.max(1.0D, Math.abs(max.getY() - min.getY()) / 2.0D + 2.0D);
                double z = Math.max(1.0D, Math.abs(max.getZ() - min.getZ()) / 2.0D + 2.0D);
                for (Entity entity : center.getWorld().getNearbyEntities(center, x, y, z)) {
                    if (entity instanceof Item && arena.isWithinBounds(entity.getLocation())) entity.remove();
                }
                return;
            }
        }

        int radius = Math.max(1, this.plugin.getConfigManager().getConfig().getInt("item-clear.match-start-clear-radius", 64));
        for (var spawnPoint : arena.getSpawnPoints()) {
            Location spawn = spawnPoint.getLocation();
            if (spawn == null || spawn.getWorld() == null) continue;
            for (Entity entity : spawn.getWorld().getNearbyEntities(spawn, radius, radius, radius)) {
                if (entity instanceof Item) entity.remove();
            }
        }
    }

    private void warnParticipants(Object participants, int seconds) {
        String display = this.plugin.getConfigManager().getConfig().getString("item-clear.display", "action-bar").toLowerCase();
        if ("none".equals(display)) return;

        String template = this.plugin.getConfigManager().getConfig().getString(
                "item-clear.message", "&eDropped items clear in &f{time}s");
        String message = template.replace("{time}", String.valueOf(seconds))
                .replace("{seconds}", String.valueOf(seconds));
        Component component = this.plugin.parseText(message);

        if (participants instanceof Set<?> set) {
            for (Object value : set) {
                if (value instanceof UUID uuid) this.send(uuid, component, display);
            }
        } else if (participants instanceof java.util.List<?> list) {
            for (Object value : list) {
                if (value instanceof com.ultimateduels.duel.model.DuelParticipant participant) {
                    this.send(participant.getUuid(), component, display);
                }
            }
        }
    }

    private void send(UUID uuid, Component component, String display) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) return;
        if ("chat".equals(display) || "both".equals(display)) player.sendMessage(component);
        if ("action-bar".equals(display) || "both".equals(display)) player.sendActionBar(component);
    }
}
'''


def patch_config(_):
    p = ROOT / 'build' / 'base' / 'config.yml'
    if not p.exists():
        return
    text = p.read_text(encoding='utf-8')
    old = '''item-clear:\n  enabled: true\n  # Seconds before a tracked FFA item is removed. 0 removes immediately.\n  delay-seconds: 30\n  # Clear existing item entities when an FFA arena becomes active.\n  clear-on-match-start: true\n  # Radius around each FFA spawn point used for the match-start cleanup.\n  match-start-clear-radius: 64\n  # Countdown location: action-bar, chat, both, or none.\n  display: action-bar\n  # {time} and {seconds} are replaced with the remaining seconds.\n  message: '&eDropped items clear in &f{time}s'\n'''
    new = '''item-clear:\n  enabled: true\n  # Seconds between item cleanup cycles for each active duel/FFA match.\n  interval-seconds: 30\n  # Legacy name retained for older configs; interval-seconds takes priority.\n  delay-seconds: 30\n  # Clear existing item entities immediately when a match becomes active.\n  clear-on-match-start: true\n  # Warning is only shown this many seconds before a cleanup cycle.\n  warning-seconds: 3\n  # Fallback radius when an arena has no configured corner bounds.\n  match-start-clear-radius: 64\n  # Countdown location: action-bar, chat, both, or none.\n  display: action-bar\n  # {time} and {seconds} are replaced with the remaining seconds.\n  message: '&eDropped items clear in &f{time}s'\n'''
    if old in text:
        text = text.replace(old, new, 1)
    else:
        text = re.sub(r'(?ms)^item-clear:\n.*?(?=^\S|\Z)', new, text, count=1)
    p.write_text(text, encoding='utf-8')


edit('com/ultimateduels/listeners/CommandBlockListener.java', patch_command_listener)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
edit('com/ultimateduels/listeners/PlayerDropListener.java', patch_drop_listener)
edit('com/ultimateduels/UltimateDuels.java', patch_ultimate_duels)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', rewrite_item_manager)
patch_config(None)
print('v4 bug fixes prepared')
