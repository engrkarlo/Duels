from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


LISTENER = r'''package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandBlockListener implements Listener {
    private final UltimateDuels plugin;

    public CommandBlockListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = this.plugin.getConfigManager() != null
                ? this.plugin.getConfigManager().getConfig()
                : this.plugin.getConfig();

        String command = normalizeRoot(event.getMessage());
        if (command == null) return;

        if (shouldBlockInLobby(player, command, config)
                || shouldBlockInState(player, command, config)) {
            event.setCancelled(true);
            String message = getMessage(player, config);
            player.sendMessage(message.replace('&', '\\u00a7'));
        }
    }

    private boolean shouldBlockInLobby(Player player, String command, FileConfiguration config) {
        if (!config.getBoolean("lobby-command-blocking.enabled", false)) return false;

        List<String> worlds = config.getStringList("lobby-command-blocking.worlds");
        if (worlds.isEmpty()) return false;

        boolean inConfiguredLobbyWorld = worlds.stream()
                .anyMatch(world -> world != null
                        && world.trim().equalsIgnoreCase(player.getWorld().getName()));
        if (!inConfiguredLobbyWorld) return false;

        return isBlockedCommand(command, config.getStringList("lobby-command-blocking.commands"));
    }

    private boolean shouldBlockInState(Player player, String command, FileConfiguration config) {
        if (!config.getBoolean("state-command-blocking.enabled", true)) return false;

        UUID uuid = player.getUniqueId();
        String state = null;

        if (this.plugin.getQueueManager() != null
                && this.plugin.getQueueManager().isInQueue(uuid)) {
            state = "queue";
        } else if (this.plugin.getDuelManager() != null
                && this.plugin.getDuelManager().isInMatch(uuid)) {
            state = "duel";
        } else if (this.plugin.getFFAManager() != null
                && this.plugin.getFFAManager().isInFFA(uuid)) {
            state = "ffa";
        } else if (this.plugin.getDuelManager() != null
                && this.plugin.getDuelManager().isSpectating(uuid)) {
            state = "spectating";
        }

        if (state == null) return false;
        if (!config.getBoolean("state-command-blocking." + state + ".enabled", true)) return false;

        return isBlockedCommand(command,
                config.getStringList("state-command-blocking." + state + ".commands"));
    }

    private boolean isBlockedCommand(String command, List<String> configured) {
        if (configured == null || configured.isEmpty()) return false;

        Set<String> blocked = new HashSet<>();
        for (String value : configured) {
            if (value == null) continue;
            value = value.trim().toLowerCase(Locale.ROOT);
            while (value.startsWith("/")) value = value.substring(1);
            if (value.isEmpty()) continue;

            blocked.add(value);
            int colon = value.indexOf(':');
            if (colon >= 0 && colon + 1 < value.length()) {
                blocked.add(value.substring(colon + 1));
            }
        }
        return blocked.contains(command);
    }

    private String normalizeRoot(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        if (raw.isEmpty()) return null;

        String root = raw.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        int colon = root.indexOf(':');
        return colon >= 0 && colon + 1 < root.length()
                ? root.substring(colon + 1)
                : root;
    }

    private String getMessage(Player player, FileConfiguration config) {
        if (config.getBoolean("lobby-command-blocking.enabled", false)
                && config.getStringList("lobby-command-blocking.worlds").stream()
                .anyMatch(world -> world != null
                        && world.trim().equalsIgnoreCase(player.getWorld().getName()))) {
            return config.getString("lobby-command-blocking.message",
                    "&cYou cannot use that command in this world.");
        }
        return config.getString("state-command-blocking.message",
                "&cYou cannot use that command while you are in a match or queue.");
    }
}
'''

CONFIG = r'''# ------------------------------------------------------------
# Lobby command blocking
# ------------------------------------------------------------
lobby-command-blocking:
  enabled: false
  worlds:
    - lobby
  commands:
    - pl
    - plugins
    - bukkit:plugins
    - version
    - ver
    - help
    - spawn
    - home
    - homes
    - fly
    - tpa
    - tpaccept
    - tpadeny
    - tp
    - pwarp
  message: '&cYou cannot use that command in this world.'

# ------------------------------------------------------------
# Queue / match / FFA / spectator command blocking
# ------------------------------------------------------------
state-command-blocking:
  enabled: true
  message: '&cYou cannot use that command while you are in a match or queue.'

  queue:
    enabled: true
    commands:
      - spawn

  duel:
    enabled: true
    commands:
      - spawn

  ffa:
    enabled: true
    commands:
      - spawn

  spectating:
    enabled: true
    commands:
      - spawn
'''

listener_path = ROOT / 'com/ultimateduels/listeners/CommandBlockListener.java'
listener_path.write_text(LISTENER, encoding='utf-8')

ultimate = ROOT / 'com/ultimateduels/UltimateDuels.java'
text = ultimate.read_text(encoding='utf-8')
if 'import com.ultimateduels.listeners.CommandBlockListener;' not in text:
    text = text.replace(
        'import com.ultimateduels.listeners.BlockProtectionListener;\n',
        'import com.ultimateduels.listeners.BlockProtectionListener;\n'
        'import com.ultimateduels.listeners.CommandBlockListener;\n',
        1,
    )
marker = '        pm.registerEvents((Listener)new GUIListener(), (Plugin)this);'
if 'new CommandBlockListener(this)' not in text and marker in text:
    text = text.replace(
        marker,
        marker + '\n        pm.registerEvents((Listener)new CommandBlockListener(this), (Plugin)this);',
        1,
    )
ultimate.write_text(text, encoding='utf-8')

base_config = ROOT / 'build' / 'base' / 'config.yml'
if base_config.exists():
    text = base_config.read_text(encoding='utf-8')
    start = text.find('\n# ------------------------------------------------------------\n# Command blocking\n# ------------------------------------------------------------')
    if start >= 0:
        end = text.find('\n# ------------------------------------------------------------\n# FFA item cleanup', start)
        if end < 0:
            end = text.find('\n# ------------------------------------------------------------\n# FFA combat timer display', start)
        if end < 0:
            end = len(text)
        text = text[:start] + '\n' + CONFIG.rstrip() + '\n' + text[end:]
    else:
        text += '\n' + CONFIG
    base_config.write_text(text, encoding='utf-8')
else:
    raise SystemExit('build/base/config.yml was not prepared')

print('Applied isolated lobby/state command-blocking patch.')
