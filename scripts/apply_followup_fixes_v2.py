from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding='utf-8')
    new_text = fn(text)
    if new_text != text:
        path.write_text(new_text, encoding='utf-8')
        print('patched', rel)
    else:
        print('already patched', rel)


def patch_command_block(text):
    text = text.replace('import com.ultimateduels.world.WorldRestrictionManager;\n', '')
    text = text.replace('@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)', '@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)')
    start = text.index('    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)')
    method_start = text.index('    public void onCommand(', start)
    body_end = text.index('\n    }', method_start) + len('\n    }')
    new_method = '''    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getConfig().getBoolean("command-blocking.enabled", false)) return;

        String raw = event.getMessage().trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        if (raw.isEmpty()) return;
        String root = raw.split("\\\\s+", 2)[0].toLowerCase(Locale.ROOT);
        String unnamespaced = root.contains(":") ? root.substring(root.indexOf(':') + 1) : root;

        Set<String> blocked = new HashSet<>();
        List<String> configuredCommands = this.plugin.getConfig().getStringList("command-blocking.commands");
        if (configuredCommands.isEmpty()) configuredCommands = this.plugin.getConfig().getStringList("command-blocker.commands");
        for (String configured : configuredCommands) {
            String value = configured.trim().toLowerCase(Locale.ROOT);
            if (value.startsWith("/")) value = value.substring(1);
            if (!value.isEmpty()) {
                blocked.add(value);
                if (value.contains(":")) blocked.add(value.substring(value.indexOf(':') + 1));
            }
        }
        if (!blocked.contains(root) && !blocked.contains(unnamespaced)) return;

        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty()) worlds = this.plugin.getConfig().getStringList("command-blocker.worlds");
        boolean configuredWorld = worlds.stream().anyMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()));
        boolean lobbyWorld = this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player);
        boolean blockInMatches = this.plugin.getConfig().getBoolean("command-blocking.block-in-matches", true);
        boolean inMatch = blockInMatches && ((this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInMatch(player.getUniqueId()))
                || (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId()))
                || (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(player.getUniqueId())));
        if (!configuredWorld && !lobbyWorld && !inMatch) return;

        event.setCancelled(true);
        String message = this.plugin.getConfig().getString("command-blocking.message", "&cYou cannot use that command here.");
        player.sendMessage(message.replace('&', '\\u00a7'));
    }'''
    return text[:method_start] + new_method + text[body_end:]


def patch_join_persistence(text):
    marker = '''            this.resetPlayerState(player);
            if (this.shouldTeleportToLobbyOnJoin() && (lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn()) != null) {
                player.teleport(lobbySpawn);
            }
            if (this.shouldGiveLobbyItemsOnJoin()) {
                this.plugin.getLobbyManager().giveHotbarItems(player);
            }'''
    replacement = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean firstJoin = !player.hasPlayedBefore();
            boolean firstJoinTeleport = firstJoin && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);
            boolean manageLobby = inLobbyWorld || firstJoinTeleport;

            // Returning players in normal worlds are deliberately left completely alone.
            // Paper restores their saved world/location from playerdata; UltimateDuels must
            // not clear their inventory or teleport them to the lobby during a restart/login.
            if (manageLobby) {
                this.resetPlayerState(player);
                Location targetLobby = null;
                if ((firstJoinTeleport || (inLobbyWorld && this.shouldTeleportToLobbyOnJoin()))
                        && this.plugin.getLobbyManager() != null) {
                    targetLobby = this.plugin.getLobbyManager().getLobbySpawn();
                }
                if (targetLobby != null) player.teleport(targetLobby);
                if (this.shouldGiveLobbyItemsOnJoin() && this.plugin.getLobbyManager() != null) {
                    this.plugin.getLobbyManager().giveHotbarItems(player);
                }
            }'''
    if marker in text:
        return text.replace(marker, replacement, 1)
    if 'boolean firstJoinTeleport = firstJoin && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);' in text:
        return text
    raise RuntimeError('Join persistence block was not found')


def patch_lobby_manager(text):
    old = 'this.teleportOnJoin = mainConfig.getBoolean("lobby.teleport-on-join", this.lobbyConfig.getBoolean("lobby.teleport-on-join", true));'
    new = '''this.teleportOnJoin = mainConfig.contains("lobby.teleport-on-join")
                ? mainConfig.getBoolean("lobby.teleport-on-join")
                : this.lobbyConfig.getBoolean("lobby.teleport-on-join", false);'''
    return text.replace(old, new, 1) if old in text else text


def patch_ffa_manager(text):
    if 'private DroppedItemClearManager droppedItemClearManager;' not in text:
        marker = '    private final Map<UUID, CombatData> combatTracking;'
        text = text.replace(marker, marker + '\n    private DroppedItemClearManager droppedItemClearManager;', 1)

    if 'private DroppedItemClearManager getDroppedItemClearManager()' not in text:
        marker = '    public boolean shouldDropItemsOnDeath() {'
        helper = '''    private DroppedItemClearManager getDroppedItemClearManager() {
        if (this.droppedItemClearManager == null) {
            this.droppedItemClearManager = new DroppedItemClearManager(this.plugin);
        }
        return this.droppedItemClearManager;
    }

'''
        if marker not in text:
            raise RuntimeError('shouldDropItemsOnDeath method not found')
        text = text.replace(marker, helper + marker, 1)

    raw_main = 'player.getWorld().dropItemNaturally(player.getLocation(), item.clone());'
    tracked_main = '''org.bukkit.entity.Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                    this.getDroppedItemClearManager().track(dropped, player);'''
    text = text.replace(raw_main, tracked_main)

    raw_off = 'player.getWorld().dropItemNaturally(player.getLocation(), offHand.clone());'
    tracked_off = '''org.bukkit.entity.Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), offHand.clone());
                this.getDroppedItemClearManager().track(dropped, player);'''
    text = text.replace(raw_off, tracked_off)

    marker = '''        FFAPlayerData playerData = new FFAPlayerData(uuid, player.getName(), arenaName);
        this.playerDataMap.put(uuid, playerData);'''
    replacement = '''        boolean firstPlayerInArena = arena.getPlayerCount() == 0;
        if (firstPlayerInArena && this.plugin.getConfig().getBoolean("item-clear.clear-on-match-start", true)) {
            this.getDroppedItemClearManager().clearArena(arenaName);
        }
        FFAPlayerData playerData = new FFAPlayerData(uuid, player.getName(), arenaName);
        this.playerDataMap.put(uuid, playerData);'''
    if marker in text:
        text = text.replace(marker, replacement, 1)
    return text


def patch_player_drop_listener(text):
    if 'private final DroppedItemClearManager droppedItemClearManager;' not in text:
        text = text.replace('    private final Set<UUID> ffaDroppedItems;\n', '    private final Set<UUID> ffaDroppedItems;\n    private final DroppedItemClearManager droppedItemClearManager;\n', 1)
        text = text.replace('        this.ffaDroppedItems = new HashSet<UUID>();\n', '        this.ffaDroppedItems = new HashSet<UUID>();\n        this.droppedItemClearManager = new DroppedItemClearManager(plugin);\n', 1)
    if 'import com.ultimateduels.ffa.DroppedItemClearManager;' not in text:
        text = text.replace('import com.ultimateduels.ffa.FFAArenaInstance;\n', 'import com.ultimateduels.ffa.FFAArenaInstance;\nimport com.ultimateduels.ffa.DroppedItemClearManager;\n', 1)
    old = '''        Item droppedItem = event.getItemDrop();
        this.ffaDroppedItems.add(droppedItem.getUniqueId());
        int despawnTime = this.getFFAItemDespawnTime();
        if (despawnTime > 0) {
            this.scheduleItemRemoval(droppedItem, despawnTime);
        }'''
    new = '''        Item droppedItem = event.getItemDrop();
        this.ffaDroppedItems.add(droppedItem.getUniqueId());
        this.droppedItemClearManager.track(droppedItem, player);'''
    return text.replace(old, new, 1) if old in text else text


def rewrite_manager(_text):
    return '''package com.ultimateduels.ffa;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.arena.model.DuelArena;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** Tracks FFA item drops and clears them after a configurable delay. */
public final class DroppedItemClearManager {
    private final UltimateDuels plugin;
    private final Map<UUID, TrackedDrop> trackedItems = new ConcurrentHashMap<>();
    private BukkitTask task;

    private static final class TrackedDrop {
        private final long expiresAt;
        private final String arenaName;
        private TrackedDrop(long expiresAt, String arenaName) {
            this.expiresAt = expiresAt;
            this.arenaName = arenaName == null ? "" : arenaName.toLowerCase();
        }
    }

    public DroppedItemClearManager(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void track(Item item, Player source) {
        if (item == null || source == null || !item.isValid()) return;
        if (!this.plugin.getConfig().getBoolean("item-clear.enabled", true)) return;
        if (this.plugin.getFFAManager() == null || !this.plugin.getFFAManager().isInFFA(source.getUniqueId())) return;
        int seconds = Math.max(0, this.plugin.getConfig().getInt("item-clear.delay-seconds", 30));
        if (seconds <= 0) {
            item.remove();
            return;
        }
        String arena = this.plugin.getFFAManager().getPlayerArenaName(source.getUniqueId());
        this.trackedItems.put(item.getUniqueId(), new TrackedDrop(System.currentTimeMillis() + seconds * 1000L, arena));
        this.ensureTask();
    }

    public void clearArena(String arenaName) {
        FFAArenaInstance instance = this.plugin.getFFAManager() == null ? null : this.plugin.getFFAManager().getArena(arenaName);
        if (instance == null || instance.getArena() == null) return;
        DuelArena arena = instance.getArena();
        int radius = Math.max(1, this.plugin.getConfig().getInt("item-clear.match-start-clear-radius", 64));
        double radiusSquared = (double)radius * radius;
        for (org.bukkit.Location spawn : arena.getSpawnPoints()) {
            if (spawn == null || spawn.getWorld() == null) continue;
            for (Entity entity : spawn.getWorld().getNearbyEntities(spawn, radius, radius, radius)) {
                if (!(entity instanceof Item item)) continue;
                if (item.getLocation().distanceSquared(spawn) <= radiusSquared) {
                    this.trackedItems.remove(item.getUniqueId());
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
        if (this.trackedItems.isEmpty()) {
            if (this.task != null) this.task.cancel();
            this.task = null;
            return;
        }
        long now = System.currentTimeMillis();
        Map<String, Integer> arenaCountdowns = new HashMap<>();
        for (Map.Entry<UUID, TrackedDrop> entry : this.trackedItems.entrySet()) {
            Item item = this.findItem(entry.getKey());
            long remaining = entry.getValue().expiresAt - now;
            if (remaining <= 0L || item == null || !item.isValid()) {
                if (item != null && item.isValid()) item.remove();
                this.trackedItems.remove(entry.getKey());
                continue;
            }
            int seconds = (int)Math.ceil(remaining / 1000.0);
            arenaCountdowns.merge(entry.getValue().arenaName, seconds, Math::min);
        }

        String display = this.plugin.getConfig().getString("item-clear.display", "action-bar").toLowerCase();
        if ("none".equals(display)) return;
        String template = this.plugin.getConfig().getString("item-clear.message", "&eDropped items clear in &f{time}s");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.plugin.getFFAManager().isInFFA(player.getUniqueId())) continue;
            String arena = this.plugin.getFFAManager().getPlayerArenaName(player.getUniqueId());
            Integer seconds = arenaCountdowns.get(arena == null ? "" : arena.toLowerCase());
            if (seconds == null) continue;
            String message = template.replace("{time}", String.valueOf(seconds)).replace("{seconds}", String.valueOf(seconds));
            Component component = Component.text(message.replace('&', '\u00a7'));
            if ("chat".equals(display) || "both".equals(display)) player.sendMessage(component);
            if ("action-bar".equals(display) || "both".equals(display)) player.sendActionBar(component);
        }
    }

    private Item findItem(UUID uuid) {
        for (World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof Item item) return item;
        }
        return null;
    }

    public void shutdown() {
        if (this.task != null) this.task.cancel();
        this.task = null;
        this.trackedItems.clear();
    }
}
'''

edit('com/ultimateduels/listeners/CommandBlockListener.java', patch_command_block)
edit('com/ultimateduels/lobby/LobbyManager.java', patch_lobby_manager)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_join_persistence)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
edit('com/ultimateduels/listeners/PlayerDropListener.java', patch_player_drop_listener)

manager = ROOT / 'com/ultimateduels/ffa/DroppedItemClearManager.java'
manager.write_text(rewrite_manager(''), encoding='utf-8')
print('wrote', manager)
print('follow-up fixes v3 applied')
