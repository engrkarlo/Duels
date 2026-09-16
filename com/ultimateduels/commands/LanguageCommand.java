/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.bukkit.Sound
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.commands;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.config.LanguageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LanguageCommand
implements CommandExecutor,
TabCompleter {
    private final UltimateDuels plugin;
    private final LanguageManager languageManager;

    public LanguageCommand(UltimateDuels plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cThis command can only be used by players!");
            return true;
        }
        Player player = (Player)sender;
        if (this.languageManager == null) {
            player.sendMessage((Component)Component.text((String)"Language system is not available!", (TextColor)NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            this.showLanguageMenu(player);
            return true;
        }
        String langCode = args[0].toLowerCase();
        if (!this.languageManager.isLanguageAvailable(langCode)) {
            Component msg = this.languageManager.getComponent(player, "general.language-not-available", "language", langCode);
            player.sendMessage(msg);
            this.playSound(player, Sound.ENTITY_VILLAGER_NO);
            return true;
        }
        if (this.languageManager.getPlayerLanguage(player).equals(langCode)) {
            Component msg = this.languageManager.getComponent(player, "general.language-already-selected", "language", this.languageManager.getLanguageName(langCode));
            player.sendMessage(msg);
            this.playSound(player, Sound.ENTITY_VILLAGER_NO);
            return true;
        }
        if (this.languageManager.setPlayerLanguage(player, langCode)) {
            Component msg = this.languageManager.getComponent(player, "general.language-changed", "language", this.languageManager.getLanguageName(langCode));
            player.sendMessage(msg);
            this.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
            player.sendMessage((Component)Component.empty());
            Component currentMsg = this.languageManager.getComponent(player, "general.language-menu-current", "language", this.languageManager.getLanguageName(langCode));
            player.sendMessage(currentMsg);
            player.sendMessage((Component)Component.empty());
        } else {
            player.sendMessage((Component)Component.text((String)"Failed to change language!", (TextColor)NamedTextColor.RED));
            this.playSound(player, Sound.ENTITY_VILLAGER_NO);
        }
        return true;
    }

    private void showLanguageMenu(Player player) {
        String currentLang = this.languageManager.getPlayerLanguage(player);
        String currentLangName = this.languageManager.getLanguageName(currentLang);
        player.sendMessage((Component)Component.empty());
        Component title = this.languageManager.getComponent(player, "general.language-menu-title");
        player.sendMessage(title);
        player.sendMessage((Component)Component.empty());
        Component currentMsg = this.languageManager.getComponent(player, "general.language-menu-current", "language", currentLangName);
        player.sendMessage(currentMsg);
        player.sendMessage((Component)Component.empty());
        Component availableMsg = this.languageManager.getComponent(player, "general.language-menu-available");
        player.sendMessage(availableMsg);
        player.sendMessage((Component)Component.empty());
        for (String langCode : this.languageManager.getAvailableLanguageCodes()) {
            LanguageManager.Language lang = this.languageManager.getLanguages().get(langCode);
            if (lang == null) continue;
            boolean isCurrent = langCode.equals(currentLang);
            Component langComponent = ((TextComponent)((TextComponent)Component.text((String)"  ").append((Component)Component.text((String)(isCurrent ? "\u27a4 " : "  ")))).append((Component)Component.text((String)(lang.getName() + " "), (TextColor)(isCurrent ? NamedTextColor.GREEN : NamedTextColor.WHITE)))).append((Component)Component.text((String)("(" + langCode + ")"), (TextColor)NamedTextColor.GRAY));
            if (!isCurrent) {
                Component hoverText = this.languageManager.getComponent(player, "general.language-menu-click").append((Component)Component.text((String)(" " + lang.getName()), (TextColor)NamedTextColor.YELLOW));
                langComponent = langComponent.hoverEvent((HoverEventSource)HoverEvent.showText((Component)hoverText)).clickEvent(ClickEvent.runCommand((String)("/language " + langCode)));
            } else {
                langComponent = langComponent.append((Component)Component.text((String)" \u2713", (TextColor)NamedTextColor.GREEN));
            }
            player.sendMessage(langComponent);
        }
        player.sendMessage((Component)Component.empty());
        Component instruction = this.languageManager.getComponent(player, "general.language-menu-click");
        player.sendMessage(((TextComponent)Component.text((String)"", (TextColor)NamedTextColor.GRAY, (TextDecoration[])new TextDecoration[]{TextDecoration.ITALIC}).append(instruction)).append((Component)Component.text((String)"!")));
        player.sendMessage((Component)Component.empty());
        Component footer = this.languageManager.getComponent(player, "general.language-menu-title");
        player.sendMessage(footer);
        player.sendMessage((Component)Component.empty());
        this.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
    }

    private void playSound(Player player, Sound sound) {
        try {
            if (this.plugin.getSettingsManager() != null && this.plugin.getSettingsManager().getSettings(player.getUniqueId()).isSoundsEnabled()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return new ArrayList<String>(this.languageManager.getAvailableLanguageCodes()).stream().filter(lang -> lang.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}

