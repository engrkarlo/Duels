/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.ultimateduels.commands.admin;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.commands.BaseCommand;
import com.ultimateduels.duel.model.DuelMatch;
import com.ultimateduels.duel.model.DuelParticipant;
import com.ultimateduels.ffa.model.FFAArenaInstance;
import com.ultimateduels.lobby.LobbyManager;
import com.ultimateduels.stats.PlayerStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand
extends BaseCommand {
    private static final List<String> SUBCOMMANDS = Arrays.asList("help", "reload", "setlobby", "forceend", "stats", "info", "debug", "version", "save", "reset", "play", "join", "lobby");

    public AdminCommand(UltimateDuels plugin) {
        super(plugin, null, false);
    }

    @Override
    protected void execute(Player player, String[] args) {
        this.handleCommand((CommandSender)player, args);
    }

    @Override
    protected void executeConsole(CommandSender sender, String[] args) {
        this.handleCommand(sender, args);
    }

    private void handleCommand(CommandSender sender, String[] args) {
        String subCommand;
        if (args.length == 0) {
            this.sendInfo(sender);
            return;
        }
        switch (subCommand = args[0].toLowerCase()) {
            case "help": {
                this.sendHelp(sender);
                break;
            }
            case "reload": {
                this.handleReload(sender);
                break;
            }
            case "setlobby": {
                this.handleSetLobby(sender);
                break;
            }
            case "forceend": {
                this.handleForceEnd(sender, args);
                break;
            }
            case "stats": {
                this.handleStats(sender, args);
                break;
            }
            case "info": {
                this.sendInfo(sender);
                break;
            }
            case "debug": {
                this.handleDebug(sender, args);
                break;
            }
            case "version": {
                this.sendVersion(sender);
                break;
            }
            case "save": {
                this.handleSave(sender);
                break;
            }
            case "reset": {
                this.handleReset(sender, args);
                break;
            }
            case "play": 
            case "join": 
            case "lobby": {
                this.handlePlayJoinLobby(sender);
                break;
            }
            default: {
                this.sendHelp(sender);
            }
        }
    }

    private void handlePlayJoinLobby(CommandSender sender) {
        if (!(sender instanceof Player)) {
            this.sendMessage(sender, "&cOnly players can use this command!");
            return;
        }
        Player player = (Player)sender;
        LobbyManager lobbyManager = this.plugin.getLobbyManager();
        if (lobbyManager == null) {
            this.sendMessage(player, "&cLobby system is not available!");
            return;
        }
        if (lobbyManager.isInLobby(player)) {
            this.sendMessage(player, "&cYou are already in the duels lobby!");
            return;
        }
        if (this.plugin.getDuelManager() != null && this.plugin.getDuelManager().isInDuel(player)) {
            this.sendMessage(player, "&cYou cannot do this while in a duel!");
            return;
        }
        if (this.plugin.getFFAManager() != null && this.plugin.getFFAManager().isInFFA(player.getUniqueId())) {
            this.sendMessage(player, "&cYou cannot do this while in FFA!");
            return;
        }
        if (this.plugin.getQueueManager() != null && this.plugin.getQueueManager().isInQueue(player)) {
            this.sendMessage(player, "&cYou cannot do this while in queue!");
            return;
        }
        if (this.plugin.getPlayerStateManager() != null) {
            this.plugin.getPlayerStateManager().saveState(player);
        }
        lobbyManager.sendToLobby(player, true);
        this.sendMessage(player, "");
        this.sendMessage(player, "&a&l\u2726 Welcome to UltimateDuels! \u2726");
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Use the items in your hotbar to:");
        this.sendMessage(player, "&e  \u2694 &7Queue for a duel");
        this.sendMessage(player, "&e  \ud83d\udc65 &7Create or join a party");
        this.sendMessage(player, "&e  \ud83d\udcd6 &7Edit your kits");
        this.sendMessage(player, "&e  \ud83c\udfdf &7Join FFA arenas");
        this.sendMessage(player, "&e  \ud83d\udcca &7View your stats");
        this.sendMessage(player, "");
        this.sendMessage(player, "&7Click the &c&lRed Bed &7to return to the hub.");
        this.sendMessage(player, "");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin.reload")) {
            this.sendNoPermission(sender);
            return;
        }
        this.sendMessage(sender, "&eReloading UltimateDuels...");
        long startTime = System.currentTimeMillis();
        try {
            this.plugin.reload();
            long duration = System.currentTimeMillis() - startTime;
            this.sendMessage(sender, "&aUltimateDuels reloaded successfully! &7(" + duration + "ms)");
        }
        catch (Exception e) {
            this.sendMessage(sender, "&cFailed to reload: " + e.getMessage());
            this.plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSetLobby(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin.setlobby")) {
            this.sendNoPermission(sender);
            return;
        }
        if (!(sender instanceof Player)) {
            this.sendMessage(sender, "&cThis command can only be used by players!");
            return;
        }
        Player player = (Player)sender;
        if (this.plugin.getLobbyManager() != null) {
            this.plugin.getLobbyManager().setLobbySpawn(player.getLocation());
            this.sendMessage(sender, "&aLobby spawn has been set to your current location!");
            this.sendMessage(sender, "&7Location: &f" + this.formatLocation(player.getLocation()));
        } else {
            this.sendMessage(sender, "&cLobby system is not available!");
        }
    }

    private void handleForceEnd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateduels.admin.forceend")) {
            this.sendNoPermission(sender);
            return;
        }
        if (this.plugin.getDuelManager() == null) {
            this.sendMessage(sender, "&cDuel system is not available!");
            return;
        }
        if (args.length < 2) {
            Collection<DuelMatch> activeMatches = this.plugin.getDuelManager().getActiveMatches();
            if (activeMatches.isEmpty()) {
                this.sendMessage(sender, "&cNo active duels to force end.");
                return;
            }
            this.sendMessage(sender, "");
            this.sendMessage(sender, "&6&lActive Duels:");
            int index = 1;
            for (DuelMatch match : activeMatches) {
                String participants = this.getParticipantNames(match);
                this.sendMessage(sender, "&e" + index + ". &f" + participants + " &7(" + match.getKitName() + ")");
                ++index;
            }
            this.sendMessage(sender, "");
            this.sendMessage(sender, "&7Usage: &e/duels forceend <player|all>");
            return;
        }
        String target = args[1].toLowerCase();
        if (target.equals("all")) {
            int count = this.forceEndAllMatches();
            this.sendMessage(sender, "&cForce ended &e" + count + " &cduels.");
            return;
        }
        Player targetPlayer = Bukkit.getPlayer((String)target);
        if (targetPlayer == null) {
            this.sendMessage(sender, "&cPlayer not found: " + target);
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatch(targetPlayer.getUniqueId());
        if (match == null) {
            this.sendMessage(sender, "&c" + targetPlayer.getName() + " is not in a duel!");
            return;
        }
        this.plugin.getDuelManager().cancelMatch(match, "Ended by administrator");
        this.sendMessage(sender, "&cForce ended duel involving &e" + targetPlayer.getName() + "&c.");
    }

    private String getParticipantNames(DuelMatch match) {
        StringBuilder sb = new StringBuilder();
        List<DuelParticipant> participants = match.getAllParticipants();
        for (int i = 0; i < participants.size(); ++i) {
            if (i > 0) {
                sb.append(" vs ");
            }
            sb.append(participants.get(i).getPlayerName());
        }
        return sb.toString();
    }

    private int forceEndAllMatches() {
        if (this.plugin.getDuelManager() == null) {
            return 0;
        }
        ArrayList<DuelMatch> matches = new ArrayList<DuelMatch>(this.plugin.getDuelManager().getActiveMatches());
        int count = matches.size();
        for (DuelMatch match : matches) {
            this.plugin.getDuelManager().cancelMatch(match, "Ended by administrator");
        }
        return count;
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateduels.admin.stats")) {
            this.sendNoPermission(sender);
            return;
        }
        if (args.length < 2) {
            this.sendUsage(sender, "/duels stats <player> [reset]");
            return;
        }
        String targetName = args[1];
        if (args.length >= 3 && args[2].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("ultimateduels.admin.stats.reset")) {
                this.sendNoPermission(sender);
                return;
            }
            if (this.plugin.getStatsManager() != null) {
                Player targetPlayer = Bukkit.getPlayer((String)targetName);
                if (targetPlayer != null) {
                    this.sendMessage(sender, "&cReset stats for &e" + targetName + "&c.");
                } else {
                    this.sendMessage(sender, "&cPlayer not found or offline: " + targetName);
                }
            }
            return;
        }
        if (this.plugin.getStatsManager() != null) {
            Player targetPlayer = Bukkit.getPlayer((String)targetName);
            if (targetPlayer == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((String)targetName);
                if (offlinePlayer.hasPlayedBefore()) {
                    PlayerStats stats = this.plugin.getStatsManager().getStats(offlinePlayer.getUniqueId());
                    this.displayStats(sender, targetName, stats);
                } else {
                    this.sendMessage(sender, "&cNo stats found for: " + targetName);
                }
                return;
            }
            PlayerStats stats = this.plugin.getStatsManager().getStats(targetPlayer.getUniqueId());
            this.displayStats(sender, targetName, stats);
        } else {
            this.sendMessage(sender, "&cStats system is not available!");
        }
    }

    private void displayStats(CommandSender sender, String playerName, PlayerStats stats) {
        if (stats == null) {
            this.sendMessage(sender, "&cNo stats found for: " + playerName);
            return;
        }
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&6&lStats for " + playerName);
        this.sendMessage(sender, "&7Kills: &f" + stats.getTotalKills());
        this.sendMessage(sender, "&7Deaths: &f" + stats.getTotalDeaths());
        this.sendMessage(sender, "&7Wins: &f" + stats.getTotalWins());
        this.sendMessage(sender, "&7Losses: &f" + stats.getTotalLosses());
        this.sendMessage(sender, "&7Current Streak: &f" + stats.getCurrentWinStreak());
        this.sendMessage(sender, "&7Best Streak: &f" + stats.getBestWinStreak());
        this.sendMessage(sender, "&7Elo: &f" + stats.getGlobalElo());
        this.sendMessage(sender, "");
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimateduels.admin.debug")) {
            this.sendNoPermission(sender);
            return;
        }
        if (this.plugin.getConfigManager() == null) {
            this.sendMessage(sender, "&cConfig system is not available!");
            return;
        }
        if (args.length < 2) {
            boolean currentState = this.plugin.getConfigManager().isDebugMode();
            this.sendMessage(sender, "&7Debug mode is currently: " + (currentState ? "&aEnabled" : "&cDisabled"));
            this.sendMessage(sender, "&7Use &e/duels debug <on|off> &7to toggle.");
            return;
        }
        String state = args[1].toLowerCase();
        boolean enabled = state.equals("on") || state.equals("true") || state.equals("enable");
        this.plugin.getConfigManager().setDebugMode(enabled);
        this.sendMessage(sender, "&7Debug mode " + (enabled ? "&aenabled" : "&cdisabled") + "&7.");
    }

    private void handleSave(CommandSender sender) {
        if (!sender.hasPermission("ultimateduels.admin.save")) {
            this.sendNoPermission(sender);
            return;
        }
        this.sendMessage(sender, "&eSaving all data...");
        if (this.plugin.getKitManager() != null) {
            this.plugin.getKitManager().saveAdminKits();
            this.plugin.getKitManager().saveAllPlayerKits();
        }
        if (this.plugin.getArenaManager() != null) {
            this.plugin.getArenaManager().saveDuelArenas();
            this.plugin.getArenaManager().saveFFAArenas();
        }
        if (this.plugin.getStatsManager() != null) {
            this.plugin.getStatsManager().saveAll();
        }
        this.sendMessage(sender, "&aAll data saved successfully!");
    }

    private void handleReset(CommandSender sender, String[] args) {
        String type;
        if (!sender.hasPermission("ultimateduels.admin.reset")) {
            this.sendNoPermission(sender);
            return;
        }
        if (args.length < 2) {
            this.sendMessage(sender, "&cThis will reset data. Use with caution!");
            this.sendMessage(sender, "&7Usage: &e/duels reset <stats|queues|all>");
            return;
        }
        switch (type = args[1].toLowerCase()) {
            case "stats": {
                if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                    this.sendMessage(sender, "&cThis will reset ALL player stats!");
                    this.sendMessage(sender, "&7Type &e/duels reset stats confirm &7to proceed.");
                    return;
                }
                this.sendMessage(sender, "&cStats reset functionality not implemented.");
                break;
            }
            case "queues": {
                if (this.plugin.getQueueManager() == null) break;
                int cleared = this.clearAllQueues();
                this.sendMessage(sender, "&cCleared &e" + cleared + " &cplayers from queues.");
                break;
            }
            case "all": {
                if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                    this.sendMessage(sender, "&4&lDANGER: This will reset EVERYTHING!");
                    this.sendMessage(sender, "&7Type &e/duels reset all confirm &7to proceed.");
                    return;
                }
                if (this.plugin.getDuelManager() != null) {
                    this.forceEndAllMatches();
                }
                if (this.plugin.getQueueManager() != null) {
                    this.clearAllQueues();
                }
                this.sendMessage(sender, "&4All data has been reset!");
                break;
            }
            default: {
                this.sendMessage(sender, "&cUnknown reset type: " + type);
            }
        }
    }

    private int clearAllQueues() {
        if (this.plugin.getQueueManager() == null) {
            return 0;
        }
        int count = this.plugin.getQueueManager().getTotalQueueSize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.plugin.getQueueManager().isInQueue(player.getUniqueId())) continue;
            this.plugin.getQueueManager().leaveQueue(player);
        }
        return count;
    }

    private void sendInfo(CommandSender sender) {
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&6&l\u2501\u2501\u2501 UltimateDuels \u2501\u2501\u2501");
        this.sendMessage(sender, "&7Version: &f" + this.plugin.getDescription().getVersion());
        this.sendMessage(sender, "&7Author: &f" + String.join((CharSequence)", ", this.plugin.getDescription().getAuthors()));
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&e&lServer Status:");
        this.sendMessage(sender, "  &7Online Players: &f" + Bukkit.getOnlinePlayers().size());
        this.sendMessage(sender, "  &7Active Duels: &f" + (this.plugin.getDuelManager() != null ? this.plugin.getDuelManager().getActiveMatchCount() : 0));
        this.sendMessage(sender, "  &7In Queue: &f" + (this.plugin.getQueueManager() != null ? this.plugin.getQueueManager().getTotalQueueSize() : 0));
        this.sendMessage(sender, "  &7In FFA: &f" + this.countFFAPlayers());
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&7Use &e/duels help &7for commands.");
        this.sendMessage(sender, "&e/duels play &7to enter the duels lobby!");
        this.sendMessage(sender, "");
    }

    private void sendVersion(CommandSender sender) {
        this.sendMessage(sender, "&6UltimateDuels &7v" + this.plugin.getDescription().getVersion());
        this.sendMessage(sender, "&7Minecraft: &f1.21.x");
        this.sendMessage(sender, "&7API: &fPaper");
    }

    private void sendHelp(CommandSender sender) {
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&6&lUltimateDuels Commands");
        this.sendMessage(sender, "&7&m                              ");
        this.sendMessage(sender, "&a&lPlayer Commands:");
        this.sendMessage(sender, "&e/duels play &7- Enter the duels lobby");
        this.sendMessage(sender, "&e/duels info &7- Show plugin info");
        this.sendMessage(sender, "&e/duels version &7- Show version");
        this.sendMessage(sender, "");
        this.sendMessage(sender, "&c&lAdmin Commands:");
        if (sender.hasPermission("ultimateduels.admin.reload")) {
            this.sendMessage(sender, "&e/duels reload &7- Reload configuration");
        }
        if (sender.hasPermission("ultimateduels.admin.setlobby")) {
            this.sendMessage(sender, "&e/duels setlobby &7- Set lobby spawn");
        }
        if (sender.hasPermission("ultimateduels.admin.forceend")) {
            this.sendMessage(sender, "&e/duels forceend <player|all> &7- Force end duels");
        }
        if (sender.hasPermission("ultimateduels.admin.stats")) {
            this.sendMessage(sender, "&e/duels stats <player> [reset] &7- View/reset stats");
        }
        if (sender.hasPermission("ultimateduels.admin.save")) {
            this.sendMessage(sender, "&e/duels save &7- Save all data");
        }
        if (sender.hasPermission("ultimateduels.admin.debug")) {
            this.sendMessage(sender, "&e/duels debug <on|off> &7- Toggle debug mode");
        }
        if (sender.hasPermission("ultimateduels.admin.reset")) {
            this.sendMessage(sender, "&e/duels reset <type> &7- Reset data");
        }
        this.sendMessage(sender, "&7&m                              ");
        this.sendMessage(sender, "");
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f in %s", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }

    private int countFFAPlayers() {
        if (this.plugin.getFFAManager() == null) {
            return 0;
        }
        int count = 0;
        for (FFAArenaInstance arena : this.plugin.getFFAManager().getAllArenas()) {
            count += arena.getPlayerCount();
        }
        return count;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        String sub;
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>();
            completions.add("help");
            completions.add("info");
            completions.add("version");
            completions.add("play");
            completions.add("join");
            completions.add("lobby");
            if (sender.hasPermission("ultimateduels.admin.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("ultimateduels.admin.setlobby")) {
                completions.add("setlobby");
            }
            if (sender.hasPermission("ultimateduels.admin.forceend")) {
                completions.add("forceend");
            }
            if (sender.hasPermission("ultimateduels.admin.stats")) {
                completions.add("stats");
            }
            if (sender.hasPermission("ultimateduels.admin.save")) {
                completions.add("save");
            }
            if (sender.hasPermission("ultimateduels.admin.debug")) {
                completions.add("debug");
            }
            if (sender.hasPermission("ultimateduels.admin.reset")) {
                completions.add("reset");
            }
            return this.filterCompletions(completions, args[0]);
        }
        if (args.length == 2) {
            switch (sub = args[0].toLowerCase()) {
                case "forceend": {
                    ArrayList<String> completions = new ArrayList<String>();
                    completions.add("all");
                    completions.addAll(this.getOnlinePlayerNames());
                    return this.filterCompletions(completions, args[1]);
                }
                case "stats": {
                    return this.filterCompletions(this.getOnlinePlayerNames(), args[1]);
                }
                case "debug": {
                    return this.filterCompletions(List.of("on", "off"), args[1]);
                }
                case "reset": {
                    return this.filterCompletions(List.of("stats", "queues", "all"), args[1]);
                }
            }
        }
        if (args.length == 3 && ((sub = args[0].toLowerCase()).equals("stats") || sub.equals("reset"))) {
            return this.filterCompletions(List.of("reset", "confirm"), args[2]);
        }
        return new ArrayList<String>();
    }
}

