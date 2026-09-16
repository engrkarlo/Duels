/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.admin;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.config.LanguageManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand
extends BaseCommand {
    private static final List<String> RELOAD_TYPES = Arrays.asList("all", "config", "kits", "arenas", "languages", "scoreboards", "test");

    public ReloadCommand(UltimateDuels plugin) {
        super(plugin, "ultimateduels.admin.reload", false);
    }

    @Override
    protected void execute(Player player, String[] args) {
        this.handleReload((CommandSender)player, args);
    }

    @Override
    protected void executeConsole(CommandSender sender, String[] args) {
        this.handleReload(sender, args);
    }

    private void handleReload(CommandSender sender, String[] args) {
        String reloadType = args.length > 0 ? args[0].toLowerCase() : "all";
        long startTime = System.currentTimeMillis();
        try {
            switch (reloadType) {
                case "test": {
                    this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                    this.sendMessage(sender, "&6&lLANGUAGE SYSTEM TEST");
                    this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                    this.sendMessage(sender, "");
                    if (this.plugin.getLanguageManager() != null) {
                        LanguageManager langMgr = this.plugin.getLanguageManager();
                        this.sendMessage(sender, "&eTest 1: Available Languages");
                        this.sendMessage(sender, "&7  Count: &f" + langMgr.getLanguages().size());
                        this.sendMessage(sender, "&7  Default: &f" + langMgr.getDefaultLanguage());
                        this.sendMessage(sender, "&7  Languages:");
                        for (Map.Entry<String, LanguageManager.Language> entry : langMgr.getLanguages().entrySet()) {
                            LanguageManager.Language lang = entry.getValue();
                            this.sendMessage(sender, "&7    - &f" + lang.getName() + " &8(&e" + entry.getKey() + "&8) &7- &f" + lang.getMessageCount() + " messages");
                        }
                        this.sendMessage(sender, "");
                        if (sender instanceof Player) {
                            Player player = (Player)sender;
                            String playerLang = langMgr.getPlayerLanguage(player);
                            this.sendMessage(sender, "&eTest 2: Your Language");
                            this.sendMessage(sender, "&7  Code: &f" + playerLang);
                            this.sendMessage(sender, "&7  Name: &f" + langMgr.getLanguageName(playerLang));
                            this.sendMessage(sender, "");
                            this.sendMessage(sender, "&eTest 3: Sample Raw Messages");
                            String prefix = langMgr.getRaw(player, "general.prefix");
                            String noPerm = langMgr.getRaw(player, "general.no-permission");
                            String victory = langMgr.getRaw(player, "duel.match.victory-title");
                            this.sendMessage(sender, "&7  Prefix: &f" + prefix);
                            this.sendMessage(sender, "&7  No-Perm: &f" + noPerm);
                            this.sendMessage(sender, "&7  Victory: &f" + victory);
                            this.sendMessage(sender, "");
                            this.sendMessage(sender, "&eTest 4: Parsed Component Message");
                            this.sendMessage(sender, "&7  Sending 'general.no-permission' as Component:");
                            player.sendMessage(langMgr.getComponent(player, "general.no-permission"));
                            this.sendMessage(sender, "");
                            this.sendMessage(sender, "&eTest 5: Message with Replacements");
                            this.sendMessage(sender, "&7  Sending 'general.player-not-found' with {player}=TestPlayer:");
                            player.sendMessage(langMgr.getComponent(player, "general.player-not-found", "player", "TestPlayer"));
                            this.sendMessage(sender, "");
                        } else {
                            this.sendMessage(sender, "&7  (Run as player to test player-specific features)");
                        }
                        this.sendMessage(sender, "&eTest 6: English Language File Info");
                        LanguageManager.Language enLang = langMgr.getLanguages().get("en");
                        if (enLang != null) {
                            this.sendMessage(sender, "&7  File: &f" + enLang.getFile().getName());
                            this.sendMessage(sender, "&7  Size: &f" + enLang.getFile().length() + " bytes");
                            this.sendMessage(sender, "&7  Messages: &f" + enLang.getMessageCount());
                            this.sendMessage(sender, "&7  Last Modified: &f" + String.valueOf(new Date(enLang.getFile().lastModified())));
                        } else {
                            this.sendMessage(sender, "&c  English language file not found!");
                        }
                    } else {
                        this.sendMessage(sender, "&c\u2717 Language system is NULL!");
                    }
                    this.sendMessage(sender, "");
                    this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                    break;
                }
                case "config": {
                    if (this.plugin.getConfigManager() != null) {
                        this.sendMessage(sender, "&eReloading config.yml...");
                        this.plugin.getConfigManager().reloadConfig();
                        this.sendMessage(sender, "&a\u2713 Config reloaded!");
                        break;
                    }
                    this.sendMessage(sender, "&c\u2717 Config system is not available!");
                    break;
                }
                case "kits": {
                    if (this.plugin.getKitManager() != null) {
                        this.plugin.getKitManager().reload();
                        int kitCount = this.plugin.getKitManager().getAllAdminKits().size();
                        this.sendMessage(sender, "&a\u2713 Kits reloaded! &7(" + kitCount + " kits)");
                        break;
                    }
                    this.sendMessage(sender, "&c\u2717 Kit system is not available!");
                    break;
                }
                case "arenas": {
                    if (this.plugin.getArenaManager() != null) {
                        this.plugin.getArenaManager().reload();
                        int arenaCount = this.plugin.getArenaManager().getAllDuelArenas().size();
                        this.sendMessage(sender, "&a\u2713 Arenas reloaded! &7(" + arenaCount + " arenas)");
                        break;
                    }
                    this.sendMessage(sender, "&c\u2717 Arena system is not available!");
                    break;
                }
                case "languages": 
                case "lang": 
                case "messages": {
                    if (this.plugin.getLanguageManager() != null) {
                        Object player;
                        this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                        this.sendMessage(sender, "&6&lRELOADING LANGUAGES");
                        this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                        this.sendMessage(sender, "");
                        LanguageManager langMgr = this.plugin.getLanguageManager();
                        int beforeCount = langMgr.getLanguages().size();
                        String beforeDefault = langMgr.getDefaultLanguage();
                        this.sendMessage(sender, "&eBEFORE Reload:");
                        this.sendMessage(sender, "&7  Languages: &f" + beforeCount);
                        this.sendMessage(sender, "&7  Default: &f" + beforeDefault);
                        if (sender instanceof Player) {
                            Player player2 = (Player)sender;
                            String beforePrefix = langMgr.getRaw(player2, "general.prefix");
                            this.sendMessage(sender, "&7  Your Prefix: &f" + beforePrefix);
                        }
                        this.sendMessage(sender, "");
                        long beforeTime = System.currentTimeMillis();
                        langMgr.reload();
                        long afterTime = System.currentTimeMillis();
                        int afterCount = langMgr.getLanguages().size();
                        String afterDefault = langMgr.getDefaultLanguage();
                        this.sendMessage(sender, "&eAFTER Reload:");
                        this.sendMessage(sender, "&7  Languages: &f" + afterCount);
                        this.sendMessage(sender, "&7  Default: &f" + afterDefault);
                        this.sendMessage(sender, "&7  Time: &f" + (afterTime - beforeTime) + "ms");
                        if (sender instanceof Player) {
                            player = (Player)sender;
                            String string = langMgr.getRaw((Player)player, "general.prefix");
                            this.sendMessage(sender, "&7  Your Prefix: &f" + string);
                        }
                        this.sendMessage(sender, "");
                        this.sendMessage(sender, "&eLoaded Languages:");
                        for (Map.Entry entry : langMgr.getLanguages().entrySet()) {
                            LanguageManager.Language lang = (LanguageManager.Language)entry.getValue();
                            this.sendMessage(sender, "&7  - &f" + lang.getName() + " &8(&e" + (String)entry.getKey() + "&8) &7- &f" + lang.getMessageCount() + " msgs");
                        }
                        this.sendMessage(sender, "");
                        if (sender instanceof Player) {
                            player = (Player)sender;
                            this.sendMessage(sender, "&eTesting parsed message:");
                            player.sendMessage(langMgr.getComponent((Player)player, "general.no-permission"));
                        }
                        this.sendMessage(sender, "");
                        this.sendMessage(sender, "&a\u2713 Languages reloaded successfully!");
                        this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                        break;
                    }
                    this.sendMessage(sender, "&c\u2717 Language system is not available!");
                    break;
                }
                case "scoreboards": 
                case "sb": {
                    if (this.plugin.getScoreboardManager() != null) {
                        this.plugin.getScoreboardManager().reloadConfig();
                        int activeBoards = this.plugin.getScoreboardManager().getActiveScoreboardCount();
                        this.sendMessage(sender, "&a\u2713 Scoreboards reloaded!");
                        this.sendMessage(sender, "&7  Active boards: &f" + activeBoards);
                        break;
                    }
                    this.sendMessage(sender, "&c\u2717 Scoreboard system is not available!");
                    break;
                }
                case "all": {
                    this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                    this.sendMessage(sender, "&6&lRELOADING ALL");
                    this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                    this.sendMessage(sender, "");
                    boolean success = this.plugin.reload();
                    long duration = System.currentTimeMillis() - startTime;
                    if (success) {
                        this.sendMessage(sender, "&a&l\u2713 UltimateDuels Reloaded Successfully!");
                        this.sendMessage(sender, "&7  Time: &f" + duration + "ms");
                        this.sendMessage(sender, "");
                        this.sendMessage(sender, "&eLoaded Components:");
                        if (this.plugin.getKitManager() != null) {
                            this.sendMessage(sender, "&7  Kits: &f" + this.plugin.getKitManager().getAllAdminKits().size());
                        }
                        if (this.plugin.getArenaManager() != null) {
                            this.sendMessage(sender, "&7  Arenas: &f" + this.plugin.getArenaManager().getAllDuelArenas().size());
                        }
                        if (this.plugin.getLanguageManager() != null) {
                            this.sendMessage(sender, "&7  Languages: &f" + this.plugin.getLanguageManager().getLanguages().size());
                        }
                        if (this.plugin.getScoreboardManager() != null) {
                            this.sendMessage(sender, "&7  Scoreboards: &f" + this.plugin.getScoreboardManager().getActiveScoreboardCount());
                        }
                        this.sendMessage(sender, "");
                        this.sendMessage(sender, "&7Use &e/duelsreload test &7to verify languages");
                    } else {
                        this.sendMessage(sender, "&c\u2717 Reload failed! Check console for errors.");
                    }
                    this.sendMessage(sender, "");
                    this.sendMessage(sender, "&e\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                    break;
                }
                default: {
                    this.sendMessage(sender, "&c\u2717 Unknown reload type: &e" + reloadType);
                    this.sendMessage(sender, "&7Valid types: &f" + String.join((CharSequence)", ", RELOAD_TYPES));
                    break;
                }
            }
        }
        catch (Exception e) {
            this.sendMessage(sender, "&c\u2717 Error during reload: " + e.getMessage());
            this.plugin.getLogger().severe("Error reloading " + reloadType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.filterCompletions(RELOAD_TYPES, args[0]);
        }
        return new ArrayList<String>();
    }
}

