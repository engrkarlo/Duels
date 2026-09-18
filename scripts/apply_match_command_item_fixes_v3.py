from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    new = fn(text)
    if new != text:
        path.write_text(new, encoding="utf-8")
        print("patched", rel)
    else:
        print("already patched", rel)


def patch_command_listener(text):
    # Keep world command blocking and state command blocking separate.
    # State blocking prevents escape commands during queue/duel/FFA without
    # affecting normal lobby use of commands such as /spawn.
    start = text.index("    @EventHandler")
    method_start = text.index("    public void onCommand(", start)
    body_end = text.index("\n    }", method_start) + len("\n    }")
    new_method = '''    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String root = normalizeCommandRoot(event.getMessage());
        if (root.isEmpty()) return;

        if (this.plugin.getConfig().getBoolean("command-blocking.enabled", false)
                && isConfiguredWorld(player)
                && isConfiguredCommand(root)) {
            event.setCancelled(true);
            String message = this.plugin.getConfig().getString(
                    "command-blocking.message", "&cYou cannot use that command in this world.");
            player.sendMessage(message.replace('&', '\\u00a7'));
            return;
        }

        if (!this.plugin.getConfig().getBoolean("state-command-blocking.enabled", true)) return;

        String state = getBlockedState(player);
        if (state == null) return;

        String path = "state-command-blocking." + state;
        if (!this.plugin.getConfig().getBoolean(path + ".enabled", true)) return;

        List<String> configured = this.plugin.getConfig().getStringList(path + ".commands");
        if (configured.isEmpty() && "spawn".equals(root)) {
            configured = List.of("spawn");
        }

        if (!containsCommand(configured, root)) return;

        event.setCancelled(true);
        String message = this.plugin.getConfig().getString(
                "state-command-blocking.message",
                "&cYou cannot use that command while you are in a match or queue.");
        player.sendMessage(message.replace('&', '\\u00a7'));
        this.plugin.debug("Blocked state command /" + root + " from " + player.getName()
                + " (state=" + state + ")");
    }

    private String getBlockedState(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(uuid)) return "queue";
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(uuid)) return "duel";
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(uuid)) return "ffa";
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(uuid)) return "spectating";
        return null;
    }

    private boolean isConfiguredWorld(Player player) {
        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty()) worlds = this.plugin.getConfig().getStringList("command-blocker.worlds");
        return worlds.stream().anyMatch(w -> w != null && w.trim().equalsIgnoreCase(player.getWorld().getName()));
    }

    private boolean isConfiguredCommand(String root) {
        List<String> configured = this.plugin.getConfig().getStringList("command-blocking.commands");
        if (configured.isEmpty()) configured = this.plugin.getConfig().getStringList("command-blocker.commands");
        return containsCommand(configured, root);
    }

    private boolean containsCommand(List<String> configured, String root) {
        Set<String> blocked = new HashSet<>();
        for (String value : configured) {
            if (value == null) continue;
            value = normalizeCommandRoot(value);
            if (!value.isEmpty()) blocked.add(value);
        }
        return blocked.contains(root);
    }

    private String normalizeCommandRoot(String command) {
        String value = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) value = value.substring(1);
        if (value.isEmpty()) return "";
        int space = value.indexOf(' ');
        if (space >= 0) value = value.substring(0, space);
        int colon = value.indexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) value = value.substring(colon + 1);
        return value;
    }'''
    return text[:method_start] + new_method + text[body_end:]

def patch_listener_registration(text):
    marker = '        pm.registerEvents((Listener)new GUIListener(), (Plugin)this);'
    registration = '        pm.registerEvents((Listener)new com.ultimateduels.listeners.CommandBlockListener(this), (Plugin)this);'
    if registration in text:
        return text
    if marker not in text:
        raise RuntimeError("GUIListener registration marker not found")
    return text.replace(marker, marker + "\n" + registration, 1)


def patch_duel_manager(text):
    # Start a dedicated item-clear scope for every DuelManager match. The scope is
    # keyed by the match UUID, so simultaneous arenas never share a cleanup timer.
    marker = '''    private void initializeMatch(@Nonnull DuelMatch match) {
        match.setState(MatchState.STARTING);'''
    replacement = '''    private void initializeMatch(@Nonnull DuelMatch match) {
        match.setState(MatchState.STARTING);
        if (this.plugin.getDroppedItemClearManager() != null) {
            this.plugin.getDroppedItemClearManager().startDuelMatch(match);
        }'''
    if marker in text:
        text = text.replace(marker, replacement, 1)
    elif 'startDuelMatch(match)' not in text:
        raise RuntimeError("initializeMatch marker not found")

    marker = '''    private void initializePartyFFAMatch(@Nonnull DuelMatch match) {
        match.setState(MatchState.STARTING);'''
    replacement = '''    private void initializePartyFFAMatch(@Nonnull DuelMatch match) {
        match.setState(MatchState.STARTING);
        if (this.plugin.getDroppedItemClearManager() != null) {
            this.plugin.getDroppedItemClearManager().startDuelMatch(match);
        }'''
    if marker in text:
        text = text.replace(marker, replacement, 1)
    return text


def patch_ffa_manager(text):
    # The manager is created by the plugin after FFAManager, so use the plugin-level
    # getter and start an FFA scope when the first player enters an empty arena.
    marker = '''        FFAPlayerData playerData = new FFAPlayerData(uuid, player.getName(), arenaName);'''
    replacement = '''        if (arena.getPlayerCount() == 0 && this.plugin.getDroppedItemClearManager() != null
                && this.plugin.getConfig().getBoolean("item-clear.clear-on-match-start", true)) {
            this.plugin.getDroppedItemClearManager().startFFAMatch(arenaName, arena.getArena());
        }
        FFAPlayerData playerData = new FFAPlayerData(uuid, player.getName(), arenaName);'''
    if marker in text and 'startFFAMatch(arenaName, arena.getArena())' not in text:
        text = text.replace(marker, replacement, 1)
    return text


def patch_player_drop_listener(text):
    # Keep intentional drops associated with the current match when possible. The
    # manager also discovers drops by arena scan, so this is an immediate association.
    marker = '        Item droppedItem = event.getItemDrop();'
    if marker not in text:
        return text
    old = '''        Item droppedItem = event.getItemDrop();
        this.ffaDroppedItems.add(droppedItem.getUniqueId());
        this.droppedItemClearManager.track(droppedItem, player);'''
    new = '''        Item droppedItem = event.getItemDrop();
        this.ffaDroppedItems.add(droppedItem.getUniqueId());
        this.droppedItemClearManager.track(droppedItem, player);'''
    return text.replace(old, new, 1)


def rewrite_manager(_text):
    return r'''package com.ultimateduels.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Per-match item cleanup.
 *
 * A cleanup scope is created when a duel/party match or standalone FFA arena
 * becomes active. Existing item entities inside that match's arena are removed
 * immediately. New item entities inside the same arena are then removed after
 * the configured delay. No survival-world item is touched unless it is inside
 * an active match scope.
 */
public final class DroppedItemClearManager {
    private final UltimateDuels plugin;
    private final Map<String, MatchScope> scopes = new ConcurrentHashMap<>();
    private BukkitTask task;

    private static final class TrackedItem {
        private final long expiresAt;
        private TrackedItem(long expiresAt) { this.expiresAt = expiresAt; }
    }

    private static final class MatchScope {
        private final String key;
        private final UUID duelMatchId;
        private final String ffaArenaName;
        private final DuelArena arena;
        private final long startedAt;
        private final Map<UUID, TrackedItem> items = new ConcurrentHashMap<>();

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
        if (match == null || match.getArena() == null) return;
        if (!this.plugin.getConfig().getBoolean("item-clear.enabled", true)) return;
        String key = "duel:" + match.getMatchId();
        MatchScope scope = new MatchScope(key, match.getMatchId(), null, match.getArena());
        MatchScope old = this.scopes.put(key, scope);
        if (old != null) clearScope(old);
        clearExistingItems(scope);
        ensureTask();
    }

    public void startFFAMatch(String arenaName, DuelArena arena) {
        if (arenaName == null || arena == null) return;
        if (!this.plugin.getConfig().getBoolean("item-clear.enabled", true)) return;
        String key = "ffa:" + arenaName.toLowerCase();
        MatchScope scope = new MatchScope(key, null, arenaName.toLowerCase(), arena);
        MatchScope old = this.scopes.put(key, scope);
        if (old != null) clearScope(old);
        clearExistingItems(scope);
        ensureTask();
    }

    /** Immediate association for a drop event. Arena scanning remains authoritative. */
    public void track(Item item, Player source) {
        if (item == null || source == null || !item.isValid()) return;
        if (!this.plugin.getConfig().getBoolean("item-clear.enabled", true)) return;
        MatchScope scope = scopeForPlayer(source);
        if (scope == null) return;
        trackItem(scope, item);
        ensureTask();
    }

    private MatchScope scopeForPlayer(Player player) {
        if (this.plugin.getDuelManager() != null) {
            DuelMatch match = this.plugin.getDuelManager().getMatch(player.getUniqueId());
            if (match != null) return this.scopes.get("duel:" + match.getMatchId());
        }
        if (this.plugin.getFFAManager() != null) {
            String arena = this.plugin.getFFAManager().getPlayerArenaName(player.getUniqueId());
            if (arena != null) return this.scopes.get("ffa:" + arena.toLowerCase());
        }
        return null;
    }

    private void trackItem(MatchScope scope, Item item) {
        if (!isInsideScope(scope, item.getLocation())) return;
        int seconds = Math.max(0, this.plugin.getConfig().getInt("item-clear.delay-seconds", 30));
        if (seconds <= 0) {
            item.remove();
            return;
        }
        scope.items.putIfAbsent(item.getUniqueId(),
                new TrackedItem(System.currentTimeMillis() + seconds * 1000L));
    }

    private void clearExistingItems(MatchScope scope) {
        for (Location center : scopeCenters(scope.arena)) {
            if (center == null || center.getWorld() == null) continue;
            int radius = clearRadius();
            double radiusSquared = (double) radius * radius;
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (!(entity instanceof Item item)) continue;
                if (item.getLocation().distanceSquared(center) <= radiusSquared) {
                    scope.items.remove(item.getUniqueId());
                    item.remove();
                }
            }
        }
    }

    private void ensureTask() {
        if (this.task != null && !this.task.isCancelled()) return;
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (this.scopes.isEmpty()) {
            if (this.task != null) this.task.cancel();
            this.task = null;
            return;
        }

        long now = System.currentTimeMillis();
        Set<String> finished = new HashSet<>();
        for (MatchScope scope : this.scopes.values()) {
            if (!isScopeActive(scope)) {
                finished.add(scope.key);
                continue;
            }

            // Discover every item currently inside this match's arena. This catches
            // normal drops, death drops, dispenser-created drops, and plugin-created items.
            for (Location center : scopeCenters(scope.arena)) {
                if (center == null || center.getWorld() == null) continue;
                int radius = clearRadius();
                double radiusSquared = (double) radius * radius;
                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (!(entity instanceof Item item) || !item.isValid()) continue;
                    if (item.getLocation().distanceSquared(center) <= radiusSquared) {
                        trackItem(scope, item);
                    }
                }
            }

            int minRemaining = Integer.MAX_VALUE;
            List<UUID> expired = new ArrayList<>();
            for (Map.Entry<UUID, TrackedItem> entry : scope.items.entrySet()) {
                Item item = findItem(entry.getKey());
                if (item == null || !item.isValid()) {
                    expired.add(entry.getKey());
                    continue;
                }
                long remaining = entry.getValue().expiresAt - now;
                if (remaining <= 0L) {
                    item.remove();
                    expired.add(entry.getKey());
                    continue;
                }
                minRemaining = Math.min(minRemaining, (int) Math.ceil(remaining / 1000.0));
            }
            for (UUID uuid : expired) scope.items.remove(uuid);

            if (minRemaining != Integer.MAX_VALUE) displayCountdown(scope, minRemaining);
        }
        for (String key : finished) this.scopes.remove(key);
    }

    private boolean isScopeActive(MatchScope scope) {
        if (scope.duelMatchId != null) {
            return this.plugin.getDuelManager() != null
                    && this.plugin.getDuelManager().getMatch(scope.duelMatchId) != null;
        }
        if (scope.ffaArenaName != null) {
            if (this.plugin.getFFAManager() == null) return false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                String arena = this.plugin.getFFAManager().getPlayerArenaName(player.getUniqueId());
                if (arena != null && arena.equalsIgnoreCase(scope.ffaArenaName)) return true;
            }
            return false;
        }
        return false;
    }

    private void displayCountdown(MatchScope scope, int seconds) {
        String display = this.plugin.getConfig().getString("item-clear.display", "action-bar").toLowerCase();
        if ("none".equals(display)) return;
        String template = this.plugin.getConfig().getString("item-clear.message", "&eDropped items clear in &f{time}s");
        String message = template.replace("{time}", String.valueOf(seconds))
                .replace("{seconds}", String.valueOf(seconds));
        Component component = Component.text(message.replace('&', '\u00a7'));

        if (scope.duelMatchId != null && this.plugin.getDuelManager() != null) {
            DuelMatch match = this.plugin.getDuelManager().getMatch(scope.duelMatchId);
            if (match == null) return;
            for (DuelParticipant participant : match.getAllParticipants()) {
                Player player = Bukkit.getPlayer(participant.getUuid());
                send(player, component, display);
            }
            return;
        }

        if (scope.ffaArenaName != null && this.plugin.getFFAManager() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String arena = this.plugin.getFFAManager().getPlayerArenaName(player.getUniqueId());
                if (arena != null && arena.equalsIgnoreCase(scope.ffaArenaName)) {
                    send(player, component, display);
                }
            }
        }
    }

    private void send(Player player, Component component, String display) {
        if (player == null || !player.isOnline()) return;
        if ("chat".equals(display) || "both".equals(display)) player.sendMessage(component);
        if ("action-bar".equals(display) || "both".equals(display)) player.sendActionBar(component);
    }

    private int clearRadius() {
        return Math.max(1, this.plugin.getConfig().getInt("item-clear.match-start-clear-radius", 64));
    }

    private List<Location> scopeCenters(DuelArena arena) {
        List<Location> centers = new ArrayList<>();
        if (arena == null) return centers;
        try {
            if (arena.getSpawnPoints() != null && !arena.getSpawnPoints().isEmpty()) {
                centers.addAll(arena.getSpawnPoints().stream().map(p -> p.getLocation()).toList());
            }
        } catch (Exception ignored) {
            // Fall back to the two standard duel spawn points below.
        }
        if (centers.isEmpty()) {
            centers.add(arena.getSpawnPoint1());
            centers.add(arena.getSpawnPoint2());
        }
        return centers;
    }

    private boolean isInsideScope(MatchScope scope, Location location) {
        if (location == null || location.getWorld() == null) return false;
        int radius = clearRadius();
        double radiusSquared = (double) radius * radius;
        for (Location center : scopeCenters(scope.arena)) {
            if (center == null || center.getWorld() == null || !center.getWorld().equals(location.getWorld())) continue;
            if (center.distanceSquared(location) <= radiusSquared) return true;
        }
        return false;
    }

    private void clearScope(MatchScope scope) {
        for (Location center : scopeCenters(scope.arena)) {
            if (center == null || center.getWorld() == null) continue;
            int radius = clearRadius();
            double radiusSquared = (double) radius * radius;
            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (!(entity instanceof Item item)) continue;
                if (item.getLocation().distanceSquared(center) <= radiusSquared) item.remove();
            }
        }
        scope.items.clear();
    }

    private Item findItem(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof Item item) return item;
        }
        return null;
    }
}
'''


def patch_plugin_getter(text):
    if 'getDroppedItemClearManager()' in text:
        return text
    marker = '    private FFAManager ffaManager;'
    if marker in text:
        return text.replace(marker, marker + '\n    private com.ultimateduels.ffa.DroppedItemClearManager droppedItemClearManager;', 1)
    # Find the field area by inserting before the first initializeManagers method.
    marker = '    private boolean initializeManagers() {'
    if marker not in text:
        raise RuntimeError("Could not find initializeManagers")
    text = text.replace(marker, '    private com.ultimateduels.ffa.DroppedItemClearManager droppedItemClearManager;\n\n' + marker, 1)
    return text


def patch_plugin_getter_and_init(text):
    # Add field if absent.
    if 'private com.ultimateduels.ffa.DroppedItemClearManager droppedItemClearManager;' not in text:
        marker = '    private boolean initializeManagers() {'
        if marker not in text:
            raise RuntimeError("initializeManagers marker not found")
        text = text.replace(marker, '    private com.ultimateduels.ffa.DroppedItemClearManager droppedItemClearManager;\n\n' + marker, 1)
    # Initialize after FFAManager is available.
    marker = '''            if (this.arenaManager != null) {
                this.ffaManager = new FFAManager(this);
                this.getLogger().info("  ✓ FFAManager initialized");
            } else {'''
    # The decompiled source stores the checkmark as unicode, so use a simpler anchor.
    if 'this.droppedItemClearManager = new com.ultimateduels.ffa.DroppedItemClearManager(this);' not in text:
        anchor = '                this.getLogger().info("  \\u2713 FFAManager initialized");\n'
        if anchor in text:
            text = text.replace(anchor, anchor + '                this.droppedItemClearManager = new com.ultimateduels.ffa.DroppedItemClearManager(this);\n', 1)
        else:
            anchor = '                this.getLogger().info("  ✓ FFAManager initialized");\n'
            if anchor in text:
                text = text.replace(anchor, anchor + '                this.droppedItemClearManager = new com.ultimateduels.ffa.DroppedItemClearManager(this);\n', 1)
            else:
                raise RuntimeError("FFAManager initialization anchor not found")
    # Add public getter before registerCommands.
    if 'public com.ultimateduels.ffa.DroppedItemClearManager getDroppedItemClearManager()' not in text:
        marker = '    private void registerCommands() {'
        if marker not in text:
            raise RuntimeError("registerCommands marker not found")
        getter = '''    public com.ultimateduels.ffa.DroppedItemClearManager getDroppedItemClearManager() {
        return this.droppedItemClearManager;
    }

'''
        text = text.replace(marker, getter + marker, 1)
    return text


edit('com/ultimateduels/listeners/CommandBlockListener.java', patch_command_listener)
edit('com/ultimateduels/UltimateDuels.java', patch_listener_registration)
edit('com/ultimateduels/UltimateDuels.java', patch_plugin_getter_and_init)
edit('com/ultimateduels/duel/DuelManager.java', patch_duel_manager)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
edit('com/ultimateduels/listeners/PlayerDropListener.java', patch_player_drop_listener)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', rewrite_manager)
