package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.world.WorldRestrictionManager;
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
 * Blocks configured commands in configured worlds and prevents players who are
 * queued or actively dueling from escaping through teleport commands.
 *
 * This listener intentionally does not touch PlayerTeleportEvent. Duel cleanup
 * is therefore free to teleport winners/losers back to the lobby normally.
 */
public final class CommandBlockListener implements Listener {
    private static final Set<String> TELEPORT_COMMANDS = Set.of(
            "spawn",
            "home",
            "homes",
            "warp",
            "warps",
            "pwarp",
            "pwarps",
            "tp",
            "teleport",
            "tpa",
            "tpahere",
            "tpaccept",
            "tpdeny",
            "back",
            "rtp",
            "randomteleport",
            "top",
            "jumpto",
            "lobby",
            "hub",
            "leave",
            "duellobby",
            "ds"
    );

    private final UltimateDuels plugin;

    public CommandBlockListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        String raw = event.getMessage();
        if (raw == null) {
            return;
        }
        raw = raw.trim();
        if (raw.startsWith("/")) {
            raw = raw.substring(1).trim();
        }
        if (raw.isEmpty()) {
            return;
        }

        String[] parts = raw.split("\\s+");
        String root = normalizeCommand(parts[0]);
        if (root.isEmpty()) {
            return;
        }

        /*
         * State protection is deliberately independent of command-blocking.enabled
         * and command-blocking.worlds. A player must not be able to escape a queue
         * or active duel simply by changing the command-blocking configuration.
         */
        if (isTeleportLocked(player) && TELEPORT_COMMANDS.contains(root)) {
            event.setCancelled(true);
            player.sendMessage(this.plugin.colorize(
                    "&cYou cannot use teleport commands while you are queued or in a duel."
            ));
            this.plugin.debug("Blocked teleport command /" + root + " from " + player.getName()
                    + " (queued=" + isQueued(player.getUniqueId())
                    + ", inDuel=" + isInDuel(player.getUniqueId()) + ")");
            return;
        }

        if (!this.plugin.getConfig().getBoolean("command-blocking.enabled", false)) {
            return;
        }

        WorldRestrictionManager restrictions = this.plugin.getWorldRestrictionManager();
        if (restrictions != null && !restrictions.isPluginAllowedInWorld(player.getWorld())) {
            return;
        }

        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty()
                || worlds.stream().noneMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) {
            return;
        }

        Set<String> blocked = new HashSet<>();
        for (String configured : this.plugin.getConfig().getStringList("command-blocking.commands")) {
            String value = normalizeCommand(configured);
            if (!value.isEmpty()) {
                blocked.add(value);
            }
        }

        if (blocked.contains(root)) {
            event.setCancelled(true);
            String message = this.plugin.getConfig().getString(
                    "command-blocking.message",
                    "&cYou cannot use that command in this world."
            );
            player.sendMessage(message.replace('&', '\u00a7'));
        }
    }

    private boolean isTeleportLocked(Player player) {
        UUID uuid = player.getUniqueId();
        return isQueued(uuid) || isInDuel(uuid);
    }

    private boolean isQueued(UUID uuid) {
        return this.plugin.getQueueManager() != null
                && this.plugin.getQueueManager().isInQueue(uuid);
    }

    private boolean isInDuel(UUID uuid) {
        return this.plugin.getDuelManager() != null
                && this.plugin.getDuelManager().isInMatch(uuid);
    }

    private String normalizeCommand(String command) {
        String value = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        int namespace = value.indexOf(':');
        if (namespace >= 0) {
            value = value.substring(namespace + 1);
        }
        return value;
    }
}
