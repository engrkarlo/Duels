from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding='utf-8')
    new = fn(text)
    if new != text:
        path.write_text(new, encoding='utf-8')
        print(f'patched {rel}')
    else:
        print(f'already patched {rel}')

# Paper 26.2 API level.
def patch_plugin_yml(t):
    return t.replace("api-version: '1.21'", "api-version: '26.2'").replace(
        'aliases: [spawn, hub, leave, duellobby, l]',
        'aliases: [ds, hub, leave, duellobby, l]')
edit('plugin.yml', patch_plugin_yml)

def patch_block(t):
    old='''    private boolean isInLobby(Player player) {
        UUID playerUUID = player.getUniqueId();
        DuelManager duelManager = this.plugin.getDuelManager();
        FFAManager ffaManager = this.plugin.getFFAManager();
        if (duelManager == null || ffaManager == null) {
            return true;
        }
        return !duelManager.isInMatch(playerUUID) && !ffaManager.isInFFA(playerUUID) && !duelManager.isSpectating(playerUUID);
    }'''
    new='''    private boolean isInLobby(Player player) {
        if (player == null || this.plugin.getLobbyManager() == null) {
            return false;
        }
        return this.plugin.getLobbyManager().isInLobbyWorld(player);
    }'''
    if old in t: t=t.replace(old,new)
    return t
edit('com/ultimateduels/listeners/BlockProtectionListener.java', patch_block)

def patch_health(t):
    t=t.replace('entityTypeTextDisplay = getStaticField(entityTypeClass, "TEXT_DISPLAY");',
                'entityTypeTextDisplay = getTextDisplayEntityType(entityTypeClass);')
    if 'private static Object getTextDisplayEntityType(Class<?> type)' not in t:
        marker='    private static Object getStaticField(Class<?> type, String name) throws Exception {'
        helper='''    private static Object getTextDisplayEntityType(Class<?> type) throws Exception {
        try {
            return getStaticField(type, "TEXT_DISPLAY");
        } catch (NoSuchFieldException ignored) {
            Method byString = type.getMethod("byString", String.class);
            Object result = byString.invoke(null, "minecraft:text_display");
            if (result instanceof java.util.Optional) {
                java.util.Optional<?> optional = (java.util.Optional<?>) result;
                if (optional.isPresent()) return optional.get();
            }
            throw new NoSuchFieldException("Could not resolve minecraft:text_display from EntityType registry");
        }
    }

'''
        if marker in t: t=t.replace(marker,helper+marker)
    return t
edit('com/ultimateduels/visuals/HealthPacketSender.java', patch_health)

def patch_config(t):
    if '  death-items-drop: false' not in t:
        t=t.replace('  combat-tag-seconds: 10\n',
                    '  combat-tag-seconds: 10\n\n  # Whether inventory items are dropped when a player dies in FFA.\n  death-items-drop: false\n',1)
    t=t.replace('\n# Whether players drop inventory items when they die in FFA.\nffa:\n  death-items-drop: false\n','\n')
    if '\ncommand-blocking:\n' not in t:
        marker='\nperformance:\n'
        block='''
# Commands blocked in configured worlds. Root labels and namespaced labels are supported.
command-blocking:
  enabled: false
  worlds:
    - "lobby"
  commands:
    - "pl"
    - "plugins"
    - "bukkit:plugins"
    - "version"
    - "ver"
    - "help"
    - "spawn"
    - "home"
    - "tpaccept"
    - "tpadeny"
    - "tp"
    - "pwarp"
    - "homes"
    - "fly"
    - "tpa"
  message: "&cYou cannot use that command in this world."

# FFA combat-log status. {time} and {seconds} are replaced with remaining seconds.
combat-log:
  message: "&cYou are in combat for &e{time}s&c!"
  display: "action-bar" # action-bar, chat, both, none

'''
        t=t.replace(marker,'\n'+block+'performance:\n',1)
    return t
edit('config.yml', patch_config)

listener=ROOT/'com/ultimateduels/listeners/CommandBlockListener.java'
if not listener.exists():
    listener.write_text('''package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.world.WorldRestrictionManager;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/** Blocks configured root commands in configured worlds, including commands owned by other plugins. */
public final class CommandBlockListener implements Listener {
    private final UltimateDuels plugin;

    public CommandBlockListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getConfig().getBoolean("command-blocking.enabled", false)) return;
        WorldRestrictionManager restrictions = this.plugin.getWorldRestrictionManager();
        if (restrictions != null && !restrictions.isPluginAllowedInWorld(player.getWorld())) return;
        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty() || worlds.stream().noneMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) return;
        String raw = event.getMessage().trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        if (raw.isEmpty()) return;
        String root = raw.split("\\\\s+", 2)[0].toLowerCase(Locale.ROOT);
        String unnamespaced = root.contains(":") ? root.substring(root.indexOf(':') + 1) : root;
        Set<String> blocked = new HashSet<>();
        for (String configured : this.plugin.getConfig().getStringList("command-blocking.commands")) {
            String value = configured.trim().toLowerCase(Locale.ROOT);
            if (value.startsWith("/")) value = value.substring(1);
            if (!value.isEmpty()) blocked.add(value);
        }
        if (blocked.contains(root) || blocked.contains(unnamespaced)) {
            event.setCancelled(true);
            String message = this.plugin.getConfig().getString("command-blocking.message", "&cYou cannot use that command in this world.");
            player.sendMessage(message.replace('&', '\\u00a7'));
        }
    }
}
''', encoding='utf-8')

edit('com/ultimateduels/listeners/ListenerManager.java', lambda t:
     t if 'new CommandBlockListener(this.plugin)' in t else
     t.replace('        this.registerListener(pm, new ChatListener(this.plugin));',
               '        this.registerListener(pm, new ChatListener(this.plugin));\n        this.registerListener(pm, new CommandBlockListener(this.plugin));'))

def patch_state(t):
    t=t.replace('    private final Map<UUID, PlayerState> savedStates;\n',
                '    private final Map<UUID, PlayerState> savedStates;\n    private final Map<UUID, PlayerState> lobbyStates;\n',1)
    t=t.replace('        this.savedStates = new ConcurrentHashMap<UUID, PlayerState>();\n',
                '        this.savedStates = new ConcurrentHashMap<UUID, PlayerState>();\n        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();\n',1)
    if 'public boolean saveLobbyState(' not in t:
        marker='    public boolean quickSave(@Nonnull Player player) {'
        helper='''    /** Saves the player's pre-lobby state separately from duel/FFA state. */
    public boolean saveLobbyState(@Nonnull Player player) {
        try {
            this.lobbyStates.put(player.getUniqueId(), this.captureState(player));
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save lobby state for " + player.getName(), e);
            return false;
        }
    }

    public boolean restoreLobbyState(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        PlayerState state = this.lobbyStates.get(uuid);
        if (state == null) return false;
        try {
            this.applyState(player, state);
            this.lobbyStates.remove(uuid);
            return true;
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to restore lobby state for " + player.getName(), e);
            return false;
        }
    }

    public boolean hasLobbyState(@Nonnull UUID uuid) {
        return this.lobbyStates.containsKey(uuid);
    }

'''
        if marker in t:t=t.replace(marker,helper+marker)
    return t
edit('com/ultimateduels/player/PlayerStateManager.java', patch_state)

def patch_lobby_manager(t):
    old='''        if (restoreState && this.playerStateManager != null && this.playerStateManager.hasState(uuid)) {
            this.playerStateManager.restoreState(player);
        } else if (!restoreState && !alreadyInLobbyWorld && this.playerStateManager != null && !this.playerStateManager.hasState(uuid)) {
            this.playerStateManager.saveState(player);
        }'''
    new='''        if (restoreState && this.playerStateManager != null && this.playerStateManager.hasState(uuid)) {
            this.playerStateManager.restoreState(player);
        } else if (!restoreState && !alreadyInLobbyWorld && this.playerStateManager != null && !this.playerStateManager.hasLobbyState(uuid)) {
            this.playerStateManager.saveLobbyState(player);
        }'''
    return t.replace(old,new)
edit('com/ultimateduels/lobby/LobbyManager.java', patch_lobby_manager)

def patch_lobby_listener(t):
    t=t.replace('this.plugin.getPlayerStateManager().hasState(player.getUniqueId())','this.plugin.getPlayerStateManager().hasLobbyState(player.getUniqueId())')
    t=t.replace('this.plugin.getPlayerStateManager().restoreState(player)','this.plugin.getPlayerStateManager().restoreLobbyState(player)')
    old='''        if (this.lobbyManager.isInLobbyWorld(player) && !this.lobbyManager.isInLobby(player)) {
            this.lobbyManager.sendToLobby(player, false);
        } else if (!this.lobbyManager.isInLobbyWorld(player) && this.lobbyManager.isInLobby(player)) {
            this.lobbyManager.removeFromLobby(player);
        }'''
    new='''        boolean pluginAllowed = this.plugin.getWorldRestrictionManager() == null
                || this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(player.getWorld());
        if (this.lobbyManager.isInLobbyWorld(player) && pluginAllowed && !this.lobbyManager.isInLobby(player)) {
            this.lobbyManager.sendToLobby(player, false);
        } else if (!this.lobbyManager.isInLobbyWorld(player) && this.lobbyManager.isInLobby(player)) {
            if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().hasLobbyState(player.getUniqueId())) {
                this.plugin.getPlayerStateManager().restoreLobbyState(player);
            }
            this.lobbyManager.removeFromLobby(player);
        }'''
    return t.replace(old,new)
edit('com/ultimateduels/listeners/LobbyListener.java', patch_lobby_listener)

def patch_world_change(t):
    t=t.replace('''        if (!wasInDuelLobby && isInDuelLobby) {
            this.handleEnterDuelLobby(player, toWorld);
        }''','''        boolean pluginAllowed = this.plugin.getWorldRestrictionManager() == null
                || this.plugin.getWorldRestrictionManager().isPluginAllowedInWorld(player.getWorld());
        if (!wasInDuelLobby && isInDuelLobby && pluginAllowed) {
            this.handleEnterDuelLobby(player, toWorld);
        }''')
    return t.replace('''        if (wasInDuelLobby && !isInDuelLobby) {
            this.handleLeaveDuelLobby(player, fromWorld);
        }''','''        if (wasInDuelLobby && !isInDuelLobby) {
            if (this.plugin.getPlayerStateManager() != null && this.plugin.getPlayerStateManager().hasLobbyState(uuid)) {
                this.plugin.getPlayerStateManager().restoreLobbyState(player);
            }
            this.handleLeaveDuelLobby(player, fromWorld);
        }''')
edit('com/ultimateduels/listeners/WorldChangeListener.java', patch_world_change)

edit('com/ultimateduels/commands/LobbyCommand.java', lambda t: t.replace(
'''            this.plugin.getLobbyManager().sendToLobby(player, true, true);
            MessageUtils.sendMessage(player, "&aYou have been teleported to the lobby.");''',
'''            this.plugin.getLobbyManager().sendToLobby(player, true, false);
            MessageUtils.sendMessage(player, "&aYou have been teleported to the lobby.");'''))

def patch_ffa(t):
    if 'private String combatLogMessage;' not in t:
        t=t.replace('    private boolean deathItemsDrop;\n', '    private boolean deathItemsDrop;\n    private String combatLogMessage;\n    private String combatLogDisplay;\n    private BukkitTask combatMessageTask;\n',1)
    if 'this.combatLogMessage = config.getString(' not in t:
        t=t.replace('        this.deathItemsDrop = config.getBoolean("ffa.death-items-drop", false);\n',
                    '        this.deathItemsDrop = config.getBoolean("ffa.death-items-drop", false);\n        this.combatLogMessage = config.getString("combat-log.message", "&cYou are in combat for &e{time}s&c!");\n        this.combatLogDisplay = config.getString("combat-log.display", "action-bar").toLowerCase();\n',1)
    if 'this.startCombatMessageTask();' not in t:
        t=t.replace('        this.startCleanupTask();\n','        this.startCleanupTask();\n        this.startCombatMessageTask();\n',1)
    if 'private void startCombatMessageTask()' not in t:
        marker='    private void startRegenerationTask() {'
        helper='''    private void startCombatMessageTask() {
        this.combatMessageTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, this::updateCombatMessages, 20L, 20L);
    }

    private void updateCombatMessages() {
        if (this.combatTracking.isEmpty()) return;
        long now = System.currentTimeMillis();
        long expiry = (long)this.combatTagSeconds * 1000L;
        for (Map.Entry<UUID, CombatData> entry : this.combatTracking.entrySet()) {
            CombatData data = entry.getValue();
            long remainingMs = expiry - (now - data.lastDamageTime);
            if (remainingMs <= 0L) continue;
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline() || !this.isInFFA(entry.getKey())) continue;
            int seconds = (int)Math.ceil(remainingMs / 1000.0);
            this.sendCombatMessage(player, seconds);
        }
    }

    private void sendCombatMessage(Player player, int seconds) {
        String message = this.combatLogMessage.replace("{time}", String.valueOf(seconds)).replace("{seconds}", String.valueOf(seconds)).replace("{player}", player.getName());
        if ("chat".equals(this.combatLogDisplay) || "both".equals(this.combatLogDisplay)) MessageUtils.sendMessage(player, message);
        if (!"chat".equals(this.combatLogDisplay) && !"none".equals(this.combatLogDisplay)) MessageUtils.sendActionBar(player, message);
    }

'''
        if marker in t:t=t.replace(marker,helper+marker)
    if 'this.sendCombatMessage(victim, this.combatTagSeconds);' not in t:
        t=t.replace('        combatData.setLastDamageTime(System.currentTimeMillis());\n',
                    '        combatData.setLastDamageTime(System.currentTimeMillis());\n        this.sendCombatMessage(victim, this.combatTagSeconds);\n        this.sendCombatMessage(attacker, this.combatTagSeconds);\n',1)
    if 'this.combatMessageTask.cancel();' not in t:
        t=t.replace('        if (this.cleanupTask != null) {\n            this.cleanupTask.cancel();\n        }\n',
                    '        if (this.cleanupTask != null) {\n            this.cleanupTask.cancel();\n        }\n        if (this.combatMessageTask != null) {\n            this.combatMessageTask.cancel();\n        }\n',1)
    return t
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa)

print('done')
