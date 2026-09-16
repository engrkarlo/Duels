/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.gui;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.duel.DuelPlayerGUI;
import com.ultimateduels.gui.duel.DuelRequestGUI;
import com.ultimateduels.gui.duel.DuelSettingsGUI;
import com.ultimateduels.gui.duel.RoundSelectorGUI;
import com.ultimateduels.gui.ffa.FFASelectorGUI;
import com.ultimateduels.gui.kit.KitPreviewGUI;
import com.ultimateduels.gui.kit.KitSelectorGUI;
import com.ultimateduels.gui.kit.PlayerKitEditorGUI;
import com.ultimateduels.gui.kit.PlayerKitSelectorGUI;
import com.ultimateduels.gui.party.PartyInviteGUI;
import com.ultimateduels.gui.party.PartyMainGUI;
import com.ultimateduels.gui.party.PartyMembersGUI;
import com.ultimateduels.gui.queue.QueueKitGUI;
import com.ultimateduels.gui.queue.QueueMainGUI;
import com.ultimateduels.gui.settings.SettingsGUI;
import com.ultimateduels.gui.spectator.SpectatorGUI;
import com.ultimateduels.gui.stats.LeaderboardGUI;
import com.ultimateduels.gui.stats.StatsGUI;
import com.ultimateduels.kit.model.DuelKit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class GUIManager {
    private final UltimateDuels plugin;
    private final Map<UUID, AbstractGUI> openGUIs;

    public GUIManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.openGUIs = new ConcurrentHashMap<UUID, AbstractGUI>();
    }

    public void openQueueGUI(Player player) {
        QueueMainGUI gui = new QueueMainGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openQueueKitGUI(Player player, String kitName) {
        QueueKitGUI gui = new QueueKitGUI(this.plugin, kitName);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openDuelRequestGUI(Player player, Player target) {
        DuelRequestGUI gui = new DuelRequestGUI(this.plugin, player, target);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openDuelSettingsGUI(Player player, Player target) {
        DuelSettingsGUI gui = new DuelSettingsGUI(this.plugin, player, target);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openRoundSelectorGUI(Player player, DuelRequestGUI parentGUI) {
        RoundSelectorGUI gui = new RoundSelectorGUI(this.plugin, parentGUI);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openDuelPlayerGUI(Player player) {
        DuelPlayerGUI gui = new DuelPlayerGUI(this.plugin, player);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openKitEditorGUI(Player player) {
        PlayerKitSelectorGUI gui = new PlayerKitSelectorGUI(this.plugin, player);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openKitEditorGUI(Player player, DuelKit kit) {
        PlayerKitEditorGUI gui = new PlayerKitEditorGUI(this.plugin, player, kit);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openKitSelectorGUI(Player player, KitSelectorGUI.Mode mode) {
        KitSelectorGUI gui = new KitSelectorGUI(this.plugin, mode);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openKitPreviewGUI(Player player, DuelKit kit) {
        KitPreviewGUI gui = new KitPreviewGUI(this.plugin, kit);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openPartyGUI(Player player) {
        PartyMainGUI gui = new PartyMainGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openPartyInviteGUI(Player player) {
        PartyInviteGUI gui = new PartyInviteGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openPartyMembersGUI(Player player) {
        PartyMembersGUI gui = new PartyMembersGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openFFAGUI(Player player) {
        FFASelectorGUI gui = new FFASelectorGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openSettingsGUI(Player player) {
        SettingsGUI gui = new SettingsGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openSpectateGUI(Player player) {
        SpectatorGUI gui = new SpectatorGUI(this.plugin);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openStatsGUI(Player player) {
        this.openStatsGUI(player, player);
    }

    public void openStatsGUI(Player player, Player target) {
        StatsGUI gui = new StatsGUI(this.plugin, target);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void openLeaderboardGUI(Player player) {
        this.openLeaderboardGUI(player, "elo");
    }

    public void openLeaderboardGUI(Player player, String type) {
        LeaderboardGUI gui = new LeaderboardGUI(this.plugin, type);
        gui.open(player);
        this.registerOpenGUI(player, gui);
    }

    public void registerOpenGUI(Player player, AbstractGUI gui) {
        this.openGUIs.put(player.getUniqueId(), gui);
    }

    public AbstractGUI getOpenGUI(Player player) {
        return this.openGUIs.get(player.getUniqueId());
    }

    public void removeOpenGUI(Player player) {
        this.openGUIs.remove(player.getUniqueId());
    }

    public boolean hasOpenGUI(Player player) {
        return this.openGUIs.containsKey(player.getUniqueId());
    }

    public void closeGUI(Player player) {
        AbstractGUI gui = this.openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            player.closeInventory();
        }
    }

    public void closeAllGUIs() {
        for (UUID playerId : this.openGUIs.keySet()) {
            Player player = this.plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;
            player.closeInventory();
        }
        this.openGUIs.clear();
    }

    public void refreshGUIs() {
        for (Map.Entry<UUID, AbstractGUI> entry : this.openGUIs.entrySet()) {
            Player player = this.plugin.getServer().getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            entry.getValue().refresh(player);
        }
    }
}

