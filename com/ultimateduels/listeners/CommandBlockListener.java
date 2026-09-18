package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Handles world command blocking and state-specific command blocking.
 *
 * State blocking is intentionally command-based only. It never cancels
 * PlayerTeleportEvent, so UltimateDuels can still teleport players during
 * normal match cleanup.
 */
public final class CommandBlockListener implements Listener {
    private final UltimateDuels plugin;

    public CommandBlockListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String root = normalizeCommandRoot(event.getMessage());
        if (root.isEmpty()) {
            return;
        }

        // Existing world-based command blocker. This remains independent from
        // state blocking so lobby command restrictions continue to work.
        if (this.plugin.getConfig().getBoolean("command-blocking.enabled", false)
                && isConfiguredWorld(player)
                && isConfiguredCommand(root)) {
            event.setCancelled(true);
            player.sendMessage(color(
                    this.plugin.getConfig().getString(
                            "command-blocking.message",
                            "&cYou cannot use that command in this world.")
            ));
            return;
        }

        // State-specific blocker. This is separate from command-blocking so
        // /spawn can be blocked in duels/FFA/queue without blocking /spawn
        // in the normal UltimateDuels lobby.
        if (!this.plugin.getConfig().getBoolean("state-command-blocking.enabled", true)) {
            return;
        }

        String state = getBlockedState(player);
        if (state == null) {
            return;
        }

        String path = "state-command-blocking." + state;
        if (!this.plugin.getConfig().getBoolean(path + ".enabled", true)) {
            return;
        }

        List<String> configured = this.plugin.getConfig().getStringList(path + ".commands");
        if (configured.isEmpty() && "spawn".equals(root)) {
            // Safe built-in fallback for servers upgrading from an older config.
            // Administrators can replace this by adding the state config.
            configured = List.of("spawn");
        }

        if (!containsCommand(configured, root)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(color(
                this.plugin.getConfig().getString(
                        "state-command-blocking.message",
                        "&cYou cannot use that command while you are in a match or queue.")
        ));
        this.plugin.debug("Blocked state command /" + root + " from " + player.getName()
                + " (state=" + state + ")");
    }

    private String getBlockedState(Player player) {
        UUID uuid = player.getUniqueId();

        if (this.plugin.getQueueManager() != null
                && this.plugin.getQueueManager().isInQueue(uuid)) {
            return "queue";
        }

        if (this.plugin.getDuelManager() != null
                && this.plugin.getDuelManager().isInMatch(uuid)) {
            return "duel";
        }

        if (this.plugin.getFFAManager() != null
                && this.plugin.getFFAManager().isInFFA(uuid)) {
            return "ffa";
        }

        if (this.plugin.getDuelManager() != null
                && this.plugin.getDuelManager().isSpectating(uuid)) {
            return "spectating";
        }

        return null;
    }

    private boolean isConfiguredWorld(Player player) {
        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty()) {
            worlds = this.plugin.getConfig().getStringList("command-blocker.worlds");
        }
        return worlds.stream().anyMatch(w -> w != null
                && w.trim().equalsIgnoreCase(player.getWorld().getName()));
    }

    private boolean isConfiguredCommand(String root) {
        List<String> configured = this.plugin.getConfig().getStringList("command-blocking.commands");
        if (configured.isEmpty()) {
            configured = this.plugin.getConfig().getStringList("command-blocker.commands");
        }
        return containsCommand(configured, root);
    }

    private boolean containsCommand(List<String> configured, String root) {
        Set<String> blocked = new HashSet<>();
        for (String value : configured) {
            if (value == null) {
                continue;
            }
            value = normalizeCommandRoot(value);
            if (!value.isEmpty()) {
                blocked.add(value);
            }
        }
        return blocked.contains(root);
    }

    private String normalizeCommandRoot(String command) {
        String value = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.isEmpty()) {
            return "";
        }

        int space = value.indexOf(' ');
        if (space >= 0) {
            value = value.substring(0, space);
        }

        int colon = value.indexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }
        return value;
    }

    private String color(String message) {
        return (message == null ? "" : message).replace('&', '\u00a7');
    }
}
