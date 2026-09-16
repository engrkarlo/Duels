/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.ultimateduels.gui.settings;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.AbstractGUI;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.settings.PlayerSettings;
import com.ultimateduels.settings.SettingsManager;
import com.ultimateduels.utils.MessageUtils;
import java.util.ArrayList;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SettingsGUI
extends AbstractGUI {
    private final SettingsManager settingsManager;
    private UUID viewerUUID;
    private static final String SECTION = "settings";

    public SettingsGUI(UltimateDuels plugin) {
        super(plugin, GUIMessages.getTitle(null, SECTION, "&b&l\u2699 Settings"), 4);
        this.settingsManager = plugin.getSettingsManager();
        this.playClickSound = false;
    }

    @Override
    public void open(Player player) {
        this.viewerUUID = player.getUniqueId();
        super.open(player);
    }

    @Override
    public void refresh(Player player) {
        this.viewerUUID = player.getUniqueId();
        super.refresh(player);
    }

    @Override
    protected void initializeItems() {
        this.fillBorder(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        if (this.settingsManager == null || this.viewerUUID == null) {
            return;
        }
        PlayerSettings settings = this.settingsManager.getSettings(this.viewerUUID);
        this.addSettingToggle(10, Material.OAK_SIGN, "Scoreboard", "Toggle the sidebar scoreboard", SettingType.SCOREBOARD, settings.isScoreboardEnabled());
        this.addSettingToggle(11, Material.DIAMOND_SWORD, "Duel Requests", "Allow others to send you duel requests", SettingType.DUEL_REQUESTS, settings.isDuelRequestsEnabled());
        this.addSettingToggle(12, Material.PLAYER_HEAD, "Party Invites", "Allow others to invite you to parties", SettingType.PARTY_INVITES, settings.isPartyInvitesEnabled());
        this.addSettingToggle(14, Material.SKELETON_SKULL, "Death Messages", "Show death messages in duels", SettingType.DEATH_MESSAGES, settings.isDeathMessagesEnabled());
        this.addSettingToggle(15, Material.ENDER_EYE, "Spectator Notifications", "Show when players spectate you", SettingType.SPECTATOR_NOTIFY, settings.isSpectatorNotifyEnabled());
        this.addSettingToggle(16, Material.BELL, "Sounds", "Play sound effects", SettingType.SOUNDS, settings.isSoundsEnabled());
        this.addSettingToggle(19, Material.HOPPER, "Auto Re-Queue", "Automatically re-queue after a duel", SettingType.AUTO_REQUEUE, settings.isAutoRequeueEnabled());
        this.addSettingToggle(20, Material.CLOCK, "Show Ping", "Display ping on scoreboard", SettingType.SHOW_PING, settings.isShowPingEnabled());
        this.addSettingToggle(21, Material.SPYGLASS, "Allow Spectators", "Let others spectate your duels", SettingType.ALLOW_SPECTATORS, settings.isAllowSpectatorsEnabled());
        String infoTitle = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "info-title", "&b&lYour Settings");
        String clickToToggle = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "click-to-toggle", "&7Click options to toggle");
        String glowingEnabled = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "glowing-enabled", "&a\u2726 Glowing &7= Enabled");
        String normalDisabled = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "normal-disabled", "&c\u2726 Normal &7= Disabled");
        this.setItem(4, this.createItem(Material.COMPARATOR, infoTitle, "", clickToToggle, "", glowingEnabled, normalDisabled));
        String resetTitle = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "reset-title", "&c&lReset to Defaults");
        String resetLine1 = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "reset-line1", "&7Reset all settings");
        String clickToReset = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "click-to-reset", "&c\u26a0 Click to reset");
        this.setItem(25, this.createItem(Material.TNT, resetTitle, "", resetLine1, "", clickToReset), event -> {
            Player player = event.getPlayer();
            if (!player.getUniqueId().equals(this.viewerUUID)) {
                return;
            }
            this.settingsManager.resetToDefaults(player);
            String resetSuccess = GUIMessages.getText(player, SECTION, "reset-success", "&a\u2714 Settings reset to defaults!");
            MessageUtils.sendMessage(player, resetSuccess);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            this.plugin.getTaskScheduler().runTaskLater(() -> {
                if (player.isOnline()) {
                    this.refresh(player);
                }
            }, 2L);
        });
        String closeTitle = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "close-title", "&c&lClose");
        String clickToClose = GUIMessages.getText(Bukkit.getPlayer((UUID)this.viewerUUID), SECTION, "click-to-close", "&7Click to close");
        this.setItem(31, this.createItem(Material.BARRIER, closeTitle, clickToClose), event -> event.getPlayer().closeInventory());
    }

    private void addSettingToggle(int slot, Material icon, String displayName, String description, SettingType type, boolean currentState) {
        int targetSlot = slot;
        Material targetIcon = icon;
        String targetName = displayName;
        String targetDesc = description;
        SettingType settingType = type;
        UUID targetPlayer = this.viewerUUID;
        ItemStack item = this.buildToggleItem(icon, displayName, description, currentState);
        this.setItem(slot, item, event -> {
            boolean playSound;
            Player player = event.getPlayer();
            if (!player.getUniqueId().equals(targetPlayer)) {
                return;
            }
            boolean newState = this.toggleSetting(player, settingType);
            if (settingType == SettingType.SCOREBOARD && this.plugin.getScoreboardManager() != null) {
                if (newState) {
                    this.plugin.getScoreboardManager().createScoreboard(player);
                } else {
                    this.plugin.getScoreboardManager().removeScoreboard(player);
                }
            }
            String enabledText = GUIMessages.getText(player, SECTION, "enabled", "&a\u2714 Enabled");
            String disabledText = GUIMessages.getText(player, SECTION, "disabled", "&c\u2718 Disabled");
            String statusLabel = GUIMessages.getText(player, SECTION, "status-label", "&7Status: {status}").replace("{status}", newState ? enabledText : disabledText);
            String hasBeenText = GUIMessages.getText(player, SECTION, "has-been", "&7{setting} has been {status}&7!").replace("{setting}", targetName).replace("{status}", newState ? "&aenabled" : "&cdisabled");
            MessageUtils.sendMessage(player, hasBeenText);
            boolean bl = playSound = settingType == SettingType.SOUNDS ? newState : this.settingsManager.hasSoundsEnabled(player);
            if (playSound) {
                Sound sound = newState ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS;
                player.playSound(player.getLocation(), sound, 0.5f, newState ? 1.5f : 0.5f);
            }
            this.inventory.setItem(targetSlot, this.buildToggleItem(targetIcon, targetName, targetDesc, newState));
        });
    }

    private boolean toggleSetting(Player player, SettingType type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.settingsManager.toggleScoreboard(player);
            case 1 -> this.settingsManager.toggleDuelRequests(player);
            case 2 -> this.settingsManager.togglePartyInvites(player);
            case 3 -> this.settingsManager.toggleDeathMessages(player);
            case 4 -> this.settingsManager.toggleSpectatorNotify(player);
            case 5 -> this.settingsManager.toggleSounds(player);
            case 6 -> this.settingsManager.toggleAutoRequeue(player);
            case 7 -> this.settingsManager.toggleShowPing(player);
            case 8 -> this.settingsManager.toggleAllowSpectators(player);
        };
    }

    private ItemStack buildToggleItem(Material icon, String name, String description, boolean enabled) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(this.plugin.parseText("&e&l" + name));
            Player player = Bukkit.getPlayer((UUID)this.viewerUUID);
            String enabledText = GUIMessages.getText(player, SECTION, "toggle-enabled", "&a\u2714 Enabled");
            String disabledText = GUIMessages.getText(player, SECTION, "toggle-disabled", "&c\u2718 Disabled");
            String statusText = GUIMessages.getText(player, SECTION, "toggle-status", "&7Status: {status}").replace("{status}", enabled ? enabledText : disabledText);
            String clickToDisable = GUIMessages.getText(player, SECTION, "click-to-disable", "&cClick to disable");
            String clickToEnable = GUIMessages.getText(player, SECTION, "click-to-enable", "&aClick to enable");
            ArrayList<Object> lore = new ArrayList<Object>();
            lore.add(Component.empty());
            lore.add(this.plugin.parseText("&7" + description));
            lore.add(Component.empty());
            lore.add(this.plugin.parseText(statusText));
            lore.add(Component.empty());
            lore.add(this.plugin.parseText(enabled ? clickToDisable : clickToEnable));
            meta.lore(lore);
            if (enabled) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    protected void onClick(Player player, int slot, ClickType clickType, ItemStack clickedItem) {
    }

    private static enum SettingType {
        SCOREBOARD,
        DUEL_REQUESTS,
        PARTY_INVITES,
        DEATH_MESSAGES,
        SPECTATOR_NOTIFY,
        SOUNDS,
        AUTO_REQUEUE,
        SHOW_PING,
        ALLOW_SPECTATORS;

    }
}

