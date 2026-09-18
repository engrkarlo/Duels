package com.ultimateduels.listeners;

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

/**
 * Handles command blocking independently for the lobby and for active UltimateDuels states.
 *
 * The two systems are deliberately separate:
 * - lobby-command-blocking applies only to configured lobby/world names.
 * - state-command-blocking applies to queue, duel, FFA and spectator state.
 *
 * Configuration is read from ConfigManager on every command so /udreload config changes
 * take effect without restarting the plugin.
 */
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
            String message = config.getString(
                    getMessagePath(player, config),
                    "&cYou cannot use that command here."
            );
            player.sendMessage(message.replace('&', '\u00a7'));
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

        // Check the most specific active state first.
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

        List<String> commands = config.getStringList("state-command-blocking." + state + ".commands");
        return isBlockedCommand(command, commands);
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

    private String getMessagePath(Player player, FileConfiguration config) {
        if (config.getBoolean("lobby-command-blocking.enabled", false)
                && config.getStringList("lobby-command-blocking.worlds").stream()
                .anyMatch(world -> world != null
                        && world.trim().equalsIgnoreCase(player.getWorld().getName()))) {
            return "lobby-command-blocking.message";
        }
        return "state-command-blocking.message";
    }
}
