package com.ultimateduels.listeners;

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
    public CommandBlockListener(UltimateDuels plugin) { this.plugin = plugin; }

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
        String root = raw.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
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
            player.sendMessage(message.replace('&', '\u00a7'));
        }
    }
}
