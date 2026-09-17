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
    text = text.replace('@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)',
                        '@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)')
    old1 = '''        WorldRestrictionManager restrictions = this.plugin.getWorldRestrictionManager();
        if (restrictions != null && !restrictions.isPluginAllowedInWorld(player.getWorld())) return;
        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty() || worlds.stream().noneMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) return;'''
    old2 = '''        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty() || worlds.stream().noneMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) return;'''
    new = '''        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        boolean configuredWorld = worlds.stream().anyMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()));
        boolean lobbyWorld = this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player);
        if (!configuredWorld && !lobbyWorld) return;'''
    if old1 in text:
        text = text.replace(old1, new, 1)
    elif old2 in text:
        text = text.replace(old2, new, 1)
    elif 'boolean configuredWorld = worlds.stream().anyMatch' not in text:
        raise RuntimeError('Command blocking world check was not found')
    return text


def patch_lobby_manager(text):
    old = 'this.teleportOnJoin = mainConfig.getBoolean("lobby.teleport-on-join", this.lobbyConfig.getBoolean("lobby.teleport-on-join", true));'
    new = '''this.teleportOnJoin = mainConfig.contains("lobby.teleport-on-join")
                ? mainConfig.getBoolean("lobby.teleport-on-join")
                : this.lobbyConfig.getBoolean("lobby.teleport-on-join", false);'''
    if old in text:
        return text.replace(old, new, 1)
    return text


def patch_ffa_manager(text):
    if 'private DroppedItemClearManager droppedItemClearManager;' not in text:
        marker = '    private final Map<UUID, CombatData> combatTracking;'
        if marker not in text:
            raise RuntimeError('combatTracking field not found')
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
    return text


MANAGER = '''package com.ultimateduels.ffa;

import com.ultimateduels.UltimateDuels;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/** Tracks FFA death drops and removes only tracked items after a configurable delay. */
public final class DroppedItemClearManager {
    private final UltimateDuels plugin;
    private final Map<UUID, Long> trackedItems = new ConcurrentHashMap<>();
    private BukkitTask task;

    public DroppedItemClearManager(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    public void track(Item item, Player source) {
        if (item == null || source == null || !item.isValid()) return;
        if (!this.plugin.getConfig().getBoolean("item-clear.enabled", true)) return;
        if (this.plugin.getConfig().getBoolean("item-clear.lobby-world-only", true)) {
            if (this.plugin.getLobbyManager() == null || !this.plugin.getLobbyManager().isInLobbyWorld(source)) return;
        }
        int seconds = Math.max(0, this.plugin.getConfig().getInt("item-clear.delay-seconds", 30));
        if (seconds <= 0) {
            item.remove();
            return;
        }
        this.trackedItems.put(item.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
        this.ensureTask();
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
        long nextRemaining = Long.MAX_VALUE;
        for (Map.Entry<UUID, Long> entry : this.trackedItems.entrySet()) {
            Item item = null;
            for (World world : Bukkit.getWorlds()) {
                org.bukkit.entity.Entity entity = world.getEntity(entry.getKey());
                if (entity instanceof Item) {
                    item = (Item) entity;
                    break;
                }
            }
            long remaining = entry.getValue() - now;
            if (remaining <= 0L || item == null || !item.isValid()) {
                if (item != null && item.isValid()) item.remove();
                this.trackedItems.remove(entry.getKey());
                continue;
            }
            nextRemaining = Math.min(nextRemaining, remaining);
        }
        if (nextRemaining == Long.MAX_VALUE) return;
        int seconds = (int)Math.ceil(nextRemaining / 1000.0);
        String template = this.plugin.getConfig().getString("item-clear.actionbar", "&eDropped items clear in &f{time}s");
        String message = template.replace("{time}", String.valueOf(seconds)).replace("{seconds}", String.valueOf(seconds));
        net.kyori.adventure.text.Component actionBar = net.kyori.adventure.text.Component.text(message.replace('&', '\u00a7'));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player)) {
                player.sendActionBar(actionBar);
            }
        }
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
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)

manager = ROOT / 'com/ultimateduels/ffa/DroppedItemClearManager.java'
if not manager.exists():
    manager.write_text(MANAGER, encoding='utf-8')
    print('created', manager)
else:
    print('already exists', manager)

print('follow-up fixes v2 applied')
