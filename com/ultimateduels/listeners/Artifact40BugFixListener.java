package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Runtime safety fixes for artifact #40. These fixes deliberately sit outside the
 * decompiled classes so they can be validated independently without rewriting the
 * original plugin logic.
 */
public final class Artifact40BugFixListener implements Listener {
    private final UltimateDuels plugin;
    private final Map<UUID, JoinSnapshot> joinSnapshots = new HashMap<>();
    private final Map<UUID, CombatTag> combatTags = new HashMap<>();
    private final Map<UUID, BukkitTask> combatTasks = new HashMap<>();

    public Artifact40BugFixListener(UltimateDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) return;

        Location location = player.getLocation().clone();
        ItemStack[] contents = cloneItems(player.getInventory().getContents());
        ItemStack[] armor = cloneItems(player.getInventory().getArmorContents());
        ItemStack offhand = cloneItem(player.getInventory().getItemInOffHand());
        joinSnapshots.put(player.getUniqueId(), new JoinSnapshot(location, contents, armor, offhand));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            JoinSnapshot snapshot = joinSnapshots.remove(player.getUniqueId());
            if (!player.isOnline() || snapshot == null) return;
            if (plugin.getDuelManager() != null && plugin.getDuelManager().isInMatch(player.getUniqueId())) return;
            if (plugin.getFFAManager() != null && plugin.getFFAManager().isInFFA(player.getUniqueId())) return;

            boolean explicitTeleport = plugin.getConfig().contains("lobby.teleport-on-join")
                    && plugin.getConfig().getBoolean("lobby.teleport-on-join");
            if (explicitTeleport) return;

            String lobbyWorld = plugin.getConfig().getString("lobby.world-name",
                    plugin.getConfig().getString("lobby.world", "lobby"));
            if (snapshot.location.getWorld() == null
                    || snapshot.location.getWorld().getName().equalsIgnoreCase(lobbyWorld)) return;

            // The original join handler clears inventory and sends returning players to
            // the lobby. Restore the exact pre-join state for returning players when
            // lobby teleport-on-join is not explicitly enabled.
            player.teleport(snapshot.location);
            player.getInventory().setContents(cloneItems(snapshot.contents));
            player.getInventory().setArmorContents(cloneItems(snapshot.armor));
            player.getInventory().setItemInOffHand(cloneItem(snapshot.offhand));
            player.updateInventory();
            plugin.getLogger().info("[Artifact40Fix] Restored returning player " + player.getName()
                    + " to " + snapshot.location.getWorld().getName() + " after join.");
        }, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getConfig().getBoolean("command-blocking.enabled", false)) return;

        List<String> worlds = plugin.getConfig().getStringList("command-blocking.worlds");
        if (worlds.isEmpty()) return;
        String worldName = player.getWorld().getName();
        String configuredLobby = plugin.getConfig().getString("lobby.world-name",
                plugin.getConfig().getString("lobby.world", "lobby"));
        boolean worldMatch = worlds.stream().anyMatch(w -> {
            String value = w.trim();
            return value.equalsIgnoreCase(worldName)
                    || (value.equalsIgnoreCase("lobby") && value.equalsIgnoreCase(configuredLobby));
        });
        if (!worldMatch) return;

        String raw = event.getMessage().trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        if (raw.isEmpty()) return;
        String root = raw.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        String unnamespaced = root.contains(":") ? root.substring(root.indexOf(':') + 1) : root;

        Set<String> blocked = new HashSet<>();
        for (String configured : plugin.getConfig().getStringList("command-blocking.commands")) {
            String value = configured.trim().toLowerCase(Locale.ROOT);
            if (value.startsWith("/")) value = value.substring(1);
            if (!value.isEmpty()) blocked.add(value);
        }
        if (!blocked.contains(root) && !blocked.contains(unnamespaced)) return;

        event.setCancelled(true);
        String message = plugin.getConfig().getString("command-blocking.message",
                "&cYou cannot use that command in this world.");
        player.sendMessage(message.replace('&', '\u00a7'));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFfaDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) return;
        FFAManager ffa = plugin.getFFAManager();
        if (ffa == null || !ffa.isInFFA(victim.getUniqueId()) || !ffa.isInFFA(attacker.getUniqueId())) return;

        long expiresAt = System.currentTimeMillis()
                + plugin.getConfig().getInt("ffa.combat-tag-seconds", 15) * 1000L;
        combatTags.put(victim.getUniqueId(), new CombatTag(attacker.getUniqueId(), expiresAt));
        startCombatDisplay(victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onFfaDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        FFAManager ffa = plugin.getFFAManager();
        if (ffa == null || !ffa.isInFFA(player.getUniqueId())) return;
        if (!plugin.getConfig().getBoolean("ffa.death-items-drop", false)) return;

        // Paper's PlayerDeathEvent exposes the mutable drop list and keep-inventory
        // flag. Force the intended combination at the end of UltimateDuels' own
        // death handling so another handler cannot silently turn it back into a keep.
        event.setKeepInventory(false);
        event.setKeepLevel(true);
        if (event.getDrops().isEmpty()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) event.getDrops().add(item.clone());
            }
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null && !item.getType().isAir()) event.getDrops().add(item.clone());
            }
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand != null && !offhand.getType().isAir()) event.getDrops().add(offhand.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        cancelCombatDisplay(uuid);
        joinSnapshots.remove(uuid);

        FFAManager ffa = plugin.getFFAManager();
        if (ffa == null || !ffa.isInFFA(uuid)) return;
        CombatTag tag = combatTags.get(uuid);
        if (tag == null || tag.expiresAt <= System.currentTimeMillis()) {
            combatTags.remove(uuid);
            ffa.removePlayer(uuid);
            return;
        }

        Player attacker = Bukkit.getPlayer(tag.attacker);
        String arenaName = ffa.getPlayerArena(uuid);
        FFAArenaInstance arena = arenaName == null ? null : ffa.getArena(arenaName);
        boolean dropItems = plugin.getConfig().getBoolean("ffa.death-items-drop", false);
        if (dropItems) dropInventory(player);

        // handleDeath already performs the normal FFA death accounting, killer reward,
        // broadcast, cleanup and lobby return. Calling it here makes a combat logout
        // behave as an actual FFA death instead of simply removing the player.
        ffa.handleDeath(player, attacker);
        if (arena != null) {
            arena.broadcast("§c" + player.getName() + " §7combat logged and was eliminated!");
        }
        combatTags.remove(uuid);
    }

    private void startCombatDisplay(Player player) {
        UUID uuid = player.getUniqueId();
        cancelCombatDisplay(uuid);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelCombatDisplay(uuid);
                return;
            }
            CombatTag tag = combatTags.get(uuid);
            if (tag == null) {
                cancelCombatDisplay(uuid);
                return;
            }
            long remaining = Math.max(0L, (tag.expiresAt - System.currentTimeMillis() + 999L) / 1000L);
            if (remaining <= 0L) {
                combatTags.remove(uuid);
                cancelCombatDisplay(uuid);
                return;
            }
            String message = plugin.getConfig().getString("combat-log.message",
                    "&cYou are in combat for &e{time}s&c!")
                    .replace("{time}", String.valueOf(remaining));
            String display = plugin.getConfig().getString("combat-log.display", "action-bar").toLowerCase(Locale.ROOT);
            message = message.replace('&', '\u00a7');
            if (display.equals("chat")) player.sendMessage(message);
            else player.sendActionBar(message);
        }, 0L, 20L);
        combatTasks.put(uuid, task);
    }

    private void cancelCombatDisplay(UUID uuid) {
        BukkitTask task = combatTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    private void dropInventory(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) world.dropItemNaturally(location, item.clone());
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) world.dropItemNaturally(location, item.clone());
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) world.dropItemNaturally(location, offhand.clone());
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) return new ItemStack[0];
        ItemStack[] result = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) result[i] = cloneItem(items[i]);
        return result;
    }

    private static final class JoinSnapshot {
        private final Location location;
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final ItemStack offhand;

        private JoinSnapshot(Location location, ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
            this.location = location;
            this.contents = contents;
            this.armor = armor;
            this.offhand = offhand;
        }
    }

    private static final class CombatTag {
        private final UUID attacker;
        private final long expiresAt;

        private CombatTag(UUID attacker, long expiresAt) {
            this.attacker = attacker;
            this.expiresAt = expiresAt;
        }
    }
}
