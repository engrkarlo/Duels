/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.GameMode
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
import com.ultimateduels.arena.model.DuelArena;
import com.ultimateduels.utils.TextUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseCommand
implements CommandExecutor,
TabCompleter {
    protected final UltimateDuels plugin;
    protected final String permission;
    protected final boolean playerOnly;

    public BaseCommand(UltimateDuels plugin) {
        this(plugin, null, true);
    }

    public BaseCommand(UltimateDuels plugin, String permission) {
        this(plugin, permission, true);
    }

    public BaseCommand(UltimateDuels plugin, String permission, boolean playerOnly) {
        this.plugin = plugin;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player;
        if (this.playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(this.colorize("&cThis command can only be used by players!"));
            return true;
        }
        if (sender instanceof Player && !this.checkWorldRestriction(player = (Player)sender)) {
            return true;
        }
        if (this.permission != null && !sender.hasPermission(this.permission)) {
            this.sendNoPermission(sender);
            return true;
        }
        try {
            if (sender instanceof Player) {
                player = (Player)sender;
                this.execute(player, args);
            } else {
                this.executeConsole(sender, args);
            }
        }
        catch (Exception e) {
            this.sendMessage(sender, "&cAn error occurred while executing this command.");
            this.plugin.getLogger().severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    protected abstract void execute(Player var1, String[] var2);

    protected void executeConsole(CommandSender sender, String[] args) {
        sender.sendMessage(this.colorize("&cThis command can only be used by players!"));
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (this.plugin.getWorldRestrictionManager() != null && !this.plugin.getWorldRestrictionManager().isPlayerInAllowedWorld(player)) {
                return new ArrayList<String>();
            }
        }
        if (this.permission != null && !sender.hasPermission(this.permission)) {
            return new ArrayList<String>();
        }
        return this.tabComplete(sender, args);
    }

    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<String>();
    }

    protected boolean checkWorldRestriction(Player player) {
        if (this.plugin.getWorldRestrictionManager() == null) {
            return true;
        }
        if (!this.plugin.getWorldRestrictionManager().canUseCommands(player)) {
            this.plugin.getWorldRestrictionManager().sendCommandBlockedMessage(player);
            return false;
        }
        return true;
    }

    protected Component colorize(String message) {
        if (message == null) {
            return Component.empty();
        }
        return TextUtil.parseLegacy(message.replace('&', '\u00a7'));
    }

    protected void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            this.sendMessage(player, message);
        } else {
            String prefix = this.plugin.getConfigManager().getPrefix();
            String fullMessage = (prefix + message).replace('&', '\u00a7');
            sender.sendMessage(TextUtil.parseLegacy(fullMessage));
        }
    }

    protected void sendMessage(Player player, String message) {
        String prefix = "";
        prefix = this.plugin.getLanguageManager() != null ? this.plugin.getLanguageManager().getPrefix(player) : this.plugin.getConfigManager().getPrefix();
        String fullMessage = (prefix + message).replace('&', '\u00a7');
        player.sendMessage(TextUtil.parseLegacy(fullMessage));
    }

    protected void sendRawMessage(Player player, String message) {
        player.sendMessage(this.colorize(message));
    }

    protected void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(this.colorize(message));
    }

    protected void sendLangMessage(Player player, String path, Object ... replacements) {
        if (this.plugin.getLanguageManager() != null) {
            this.plugin.getLanguageManager().send(player, path, replacements);
        } else {
            this.sendMessage(player, "&c[Message system unavailable]");
        }
    }

    protected String getLangRaw(Player player, String path) {
        if (this.plugin.getLanguageManager() != null) {
            return this.plugin.getLanguageManager().getRaw(player, path);
        }
        return path;
    }

    protected String getLangParsed(Player player, String path) {
        if (this.plugin.getLanguageManager() != null) {
            return this.plugin.getLanguageManager().getParsed(player, path);
        }
        return path;
    }

    protected void sendNoPermission(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            this.sendLangMessage(player, "general.no-permission", new Object[0]);
        } else {
            this.sendMessage(sender, "&cYou don't have permission to use this command!");
        }
    }

    protected void sendUsage(CommandSender sender, String usage) {
        this.sendRawMessage(sender, "&cUsage: &e" + usage);
    }

    protected void sendHeader(CommandSender sender, String title) {
        this.sendRawMessage(sender, "");
        this.sendRawMessage(sender, "&6&l" + title);
        this.sendRawMessage(sender, "&7&m                              ");
    }

    protected void sendFooter(CommandSender sender) {
        this.sendRawMessage(sender, "&7&m                              ");
        this.sendRawMessage(sender, "");
    }

    protected boolean checkPlayerState(Player player) {
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            this.sendLangMessage(player, "general.player-in-duel", new Object[0]);
            return false;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            this.sendLangMessage(player, "general.player-in-ffa", new Object[0]);
            return false;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            this.sendLangMessage(player, "general.player-spectating", new Object[0]);
            return false;
        }
        return true;
    }

    protected boolean isSpectating(Player player) {
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return false;
        }
        if (this.plugin.getPlayerStateManager() != null) {
            return this.plugin.getPlayerStateManager().hasState(player);
        }
        return false;
    }

    protected List<String> filterCompletions(List<String> completions, String partial) {
        if (partial == null || partial.isEmpty()) {
            return completions;
        }
        String lowerPartial = partial.toLowerCase();
        return completions.stream().filter(s -> s.toLowerCase().startsWith(lowerPartial)).collect(Collectors.toList());
    }

    protected List<String> getOnlinePlayerNames() {
        return this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    protected List<String> getKitNames() {
        if (this.plugin.getKitManager() == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(this.plugin.getKitManager().getAdminKitNames());
    }

    protected List<String> getArenaNames() {
        if (this.plugin.getArenaManager() == null) {
            return new ArrayList<String>();
        }
        return this.plugin.getArenaManager().getAllDuelArenas().stream().map(DuelArena::getName).collect(Collectors.toList());
    }

    protected List<String> getDuelArenaNames() {
        if (this.plugin.getArenaManager() == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(this.plugin.getArenaManager().getDuelArenaNames());
    }

    protected List<String> getFFAArenaNames() {
        if (this.plugin.getArenaManager() == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(this.plugin.getArenaManager().getFFAArenaNames());
    }

    protected int parseIntOrDefault(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected String joinArgs(String[] args, int startIndex) {
        if (startIndex >= args.length) {
            return "";
        }
        return String.join((CharSequence)" ", Arrays.copyOfRange(args, startIndex, args.length));
    }
}

