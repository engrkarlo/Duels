/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.listeners;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.duel.model.MatchState;
import com.ultimateduels.gui.GUIManager;
import com.ultimateduels.lobby.LobbyManager;
import com.ultimateduels.utils.MessageUtils;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener
implements Listener {
    private final UltimateDuels plugin;
    private final LobbyManager lobbyManager;
    private final GUIManager guiManager;

    public PlayerInteractListener(UltimateDuels plugin) {
        this.plugin = plugin;
        this.lobbyManager = plugin.getLobbyManager();
        this.guiManager = plugin.getGUIManager();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getWorldRestrictionManager().isPlayerInAllowedWorld(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (item == null || item.getType().isAir()) {
            return;
        }
        if (this.lobbyManager != null && this.lobbyManager.isLobbyItem(item)) {
            event.setCancelled(true);
            this.handleLobbyItemClick(player, item);
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            event.setCancelled(true);
            this.handleSpectatorItemClick(player, item);
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            this.handleDuelItemInteract(player, item, event);
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            this.handleFFAItemInteract(player, item, event);
            return;
        }
    }

    private void handleLobbyItemClick(Player player, ItemStack item) {
        if (this.lobbyManager == null) {
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot use this while in a duel!");
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            MessageUtils.sendMessage(player, "&cYou cannot use this while in FFA!");
            return;
        }
        LobbyManager.LobbyItemAction action = this.lobbyManager.getLobbyItemAction(item);
        if (action == null) {
            action = LobbyManager.LobbyItemAction.NONE;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        switch (action) {
            case QUEUE_MENU: {
                if (this.guiManager != null) {
                    this.guiManager.openQueueGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case PARTY_MENU: {
                if (this.guiManager != null) {
                    this.guiManager.openPartyGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case KIT_EDITOR: {
                if (this.guiManager != null) {
                    this.guiManager.openKitEditorGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case FFA_MENU: {
                if (this.guiManager != null) {
                    this.guiManager.openFFAGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case SETTINGS_MENU: {
                if (this.guiManager != null) {
                    this.guiManager.openSettingsGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case SPECTATE_MENU: {
                if (this.guiManager != null) {
                    this.guiManager.openSpectateGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case STATS_MENU: {
                if (this.guiManager != null) {
                    this.guiManager.openStatsGUI(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cGUI system is not available!");
                break;
            }
            case SHOP_MENU: {
                MessageUtils.sendMessage(player, "&eShop coming soon!");
                break;
            }
            case LEAVE_LOBBY: {
                this.handleLeaveLobby(player);
                break;
            }
            case NONE: {
                this.handleByMaterial(player, item);
            }
        }
    }

    private void handleLeaveLobby(Player player) {
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            this.plugin.getQueueManager().leaveQueue(player);
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isSpectating(player.getUniqueId())) {
            this.plugin.getDuelManager().removeSpectator(player);
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFAArena(player)) {
            this.plugin.getFFAManager().leave(player);
            return;
        }
        MessageUtils.sendMessage(player, "&7Returning to hub...");
        player.performCommand("hub");
    }

    private void handleByMaterial(Player player, ItemStack item) {
        if (this.guiManager == null) {
            MessageUtils.sendMessage(player, "&cGUI system is not available!");
            return;
        }
        Material type = item.getType();
        switch (type) {
            case IRON_SWORD: 
            case DIAMOND_SWORD: 
            case NETHERITE_SWORD: {
                this.guiManager.openQueueGUI(player);
                break;
            }
            case SPYGLASS: {
                this.guiManager.openPartyGUI(player);
                break;
            }
            case BOOK: 
            case WRITTEN_BOOK: 
            case WRITABLE_BOOK: {
                this.guiManager.openKitEditorGUI(player);
                break;
            }
            case TOTEM_OF_UNDYING: {
                this.guiManager.openFFAGUI(player);
                break;
            }
            case COMPARATOR: {
                this.guiManager.openSettingsGUI(player);
                break;
            }
            case ENDER_EYE: {
                this.guiManager.openSpectateGUI(player);
                break;
            }
            case PLAYER_HEAD: {
                this.guiManager.openStatsGUI(player);
                break;
            }
            case EMERALD: {
                this.guiManager.openLeaderboardGUI(player);
                break;
            }
            case RED_BED: 
            case BARRIER: {
                this.handleLeaveLobby(player);
                break;
            }
            case CLOCK: {
                if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player.getUniqueId())) {
                    this.plugin.getQueueManager().leaveQueue(player);
                    break;
                }
                MessageUtils.sendMessage(player, "&cYou are not in a queue!");
            }
        }
    }

    private void handleSpectatorItemClick(Player player, ItemStack item) {
        Material type = item.getType();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        switch (type) {
            case COMPASS: {
                this.teleportToNextPlayer(player);
                break;
            }
            case PAPER: {
                this.showSpectatorPlayerList(player);
                break;
            }
            case COMPARATOR: {
                if (this.guiManager == null) break;
                this.guiManager.openSettingsGUI(player);
                break;
            }
            case RED_BED: 
            case BARRIER: 
            case RED_DYE: {
                if (this.plugin.getDuelManager() == null) break;
                this.plugin.getDuelManager().removeSpectator(player);
            }
        }
    }

    private void teleportToNextPlayer(Player spectator) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return;
        }
        for (DuelMatch match : duelManager.getActiveMatches()) {
            if (!duelManager.getMatchSpectators(match.getMatchId()).contains(spectator.getUniqueId())) continue;
            List<Player> alivePlayers = match.getAllParticipants().stream().filter(p -> p.isAlive()).map(p -> Bukkit.getPlayer((UUID)p.getUuid())).filter(p -> p != null && p.isOnline()).toList();
            if (!alivePlayers.isEmpty()) {
                Player currentTarget = spectator.getSpectatorTarget() instanceof Player ? (Player)spectator.getSpectatorTarget() : null;
                int currentIndex = -1;
                if (currentTarget != null) {
                    for (int i = 0; i < alivePlayers.size(); ++i) {
                        if (!alivePlayers.get(i).equals((Object)currentTarget)) continue;
                        currentIndex = i;
                        break;
                    }
                }
                int nextIndex = (currentIndex + 1) % alivePlayers.size();
                Player nextPlayer = alivePlayers.get(nextIndex);
                spectator.teleport(nextPlayer.getLocation());
                MessageUtils.sendMessage(spectator, "&7Now spectating: &e" + nextPlayer.getName());
            }
            return;
        }
    }

    private void showSpectatorPlayerList(Player spectator) {
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return;
        }
        for (DuelMatch match : duelManager.getActiveMatches()) {
            if (!duelManager.getMatchSpectators(match.getMatchId()).contains(spectator.getUniqueId())) continue;
            MessageUtils.sendMessage(spectator, "");
            MessageUtils.sendMessage(spectator, "&6&lPlayers in this duel:");
            for (DuelParticipant participant : match.getAllParticipants()) {
                int maxHealth;
                Player p = Bukkit.getPlayer((UUID)participant.getUuid());
                if (p == null || !p.isOnline()) continue;
                int health = (int)Math.ceil(p.getHealth());
                String healthColor = (double)health > (double)(maxHealth = (int)p.getMaxHealth()) * 0.5 ? "&a" : ((double)health > (double)maxHealth * 0.25 ? "&e" : "&c");
                String status = participant.isAlive() ? "" : " &c(DEAD)";
                MessageUtils.sendMessage(spectator, "&7- &f" + p.getName() + " " + healthColor + health + "&7/&f" + maxHealth + " &c\u2764" + status);
            }
            MessageUtils.sendMessage(spectator, "");
            return;
        }
    }

    private void handleDuelItemInteract(Player player, ItemStack item, PlayerInteractEvent event) {
        Material type;
        DuelManager duelManager = this.plugin.getDuelManager();
        if (duelManager == null) {
            return;
        }
        DuelMatch match = duelManager.getMatch(player.getUniqueId());
        if (match == null) {
            return;
        }
        if ((match.getState() == MatchState.STARTING || match.getState() == MatchState.RESETTING) && (this.isThrowable(type = item.getType()) || this.isConsumable(type))) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(player, "&cWait for the countdown!");
            return;
        }
        if (match.getState() == MatchState.ROUND_ENDING && item.getType() == Material.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    private void handleFFAItemInteract(Player player, ItemStack item, PlayerInteractEvent event) {
        Material type;
        if (this.plugin.getFFAManager() == null) {
            return;
        }
        if (this.plugin.getFFAManager().hasSpawnProtection(player.getUniqueId()) && this.isThrowable(type = item.getType()) && type != Material.ENDER_PEARL) {
            event.setCancelled(true);
            MessageUtils.sendActionBar(player, "&cYou have spawn protection!");
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Player)) {
            return;
        }
        Player target = (Player)entity;
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (this.lobbyManager != null && this.lobbyManager.isLobbyItem(item) && item.getType().name().contains("SWORD")) {
            event.setCancelled(true);
            if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(target)) {
                MessageUtils.sendMessage(player, "&c" + target.getName() + " is currently in a duel!");
                return;
            }
            if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(target.getUniqueId())) {
                MessageUtils.sendMessage(player, "&c" + target.getName() + " is currently in queue!");
                return;
            }
            if (target.equals((Object)player)) {
                MessageUtils.sendMessage(player, "&cYou cannot duel yourself!");
                return;
            }
            if (this.guiManager != null) {
                this.guiManager.openDuelRequestGUI(player, target);
            } else {
                MessageUtils.sendMessage(player, "&7Use &e/duel " + target.getName() + " <kit> &7to send a duel request.");
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    private boolean isThrowable(Material material) {
        return switch (material) {
            case Material.ENDER_PEARL, Material.SNOWBALL, Material.EGG, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.EXPERIENCE_BOTTLE, Material.TRIDENT, Material.FIREWORK_ROCKET -> true;
            default -> false;
        };
    }

    private boolean isConsumable(Material material) {
        return material.isEdible() || material == Material.POTION || material == Material.MILK_BUCKET || material == Material.HONEY_BOTTLE;
    }
}

