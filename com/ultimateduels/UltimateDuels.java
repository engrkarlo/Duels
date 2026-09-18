/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels;

import com.ultimateduels.api.UltimateDuelsAPI;
import com.ultimateduels.api.UltimateDuelsAPIImpl;
import com.ultimateduels.arena.ArenaManager;
import com.ultimateduels.commands.LanguageCommand;
import com.ultimateduels.commands.LobbyCommand;
import com.ultimateduels.commands.PingCommand;
import com.ultimateduels.commands.UltimateDuelsCommand;
import com.ultimateduels.commands.admin.DuelAdminCommand;
import com.ultimateduels.commands.admin.ForceDuelCommand;
import com.ultimateduels.commands.admin.ForceEndCommand;
import com.ultimateduels.commands.admin.ReloadCommand;
import com.ultimateduels.commands.admin.SetLobbyCommand;
import com.ultimateduels.commands.arena.ArenaCommand;
import com.ultimateduels.commands.arena.ArenaSetCornerCommand;
import com.ultimateduels.commands.arena.ArenaSetSpawnCommand;
import com.ultimateduels.commands.duel.AcceptCommand;
import com.ultimateduels.commands.duel.DenyCommand;
import com.ultimateduels.commands.duel.DuelCommand;
import com.ultimateduels.commands.duel.ForfeitCommand;
import com.ultimateduels.commands.ffa.FFAAdminCommand;
import com.ultimateduels.commands.ffa.FFACommand;
import com.ultimateduels.commands.ffa.LeaveFFACommand;
import com.ultimateduels.commands.kit.KitCommand;
import com.ultimateduels.commands.kit.KitEditorCommand;
import com.ultimateduels.commands.kit.KitGiveCommand;
import com.ultimateduels.commands.party.PChatCommand;
import com.ultimateduels.commands.party.PartyAcceptCommand;
import com.ultimateduels.commands.party.PartyCommand;
import com.ultimateduels.commands.party.PartyLeaveCommand;
import com.ultimateduels.commands.queue.LeaveQueueCommand;
import com.ultimateduels.commands.queue.QueueCommand;
import com.ultimateduels.commands.queue.ToggleQueueCommand;
import com.ultimateduels.commands.settings.DuelSettingsCommand;
import com.ultimateduels.commands.settings.ToggleCommand;
import com.ultimateduels.commands.settings.ToggleDuelsCommand;
import com.ultimateduels.commands.spectate.SpectateCommand;
import com.ultimateduels.commands.spectate.UnspectateCommand;
import com.ultimateduels.commands.stats.LeaderboardCommand;
import com.ultimateduels.commands.stats.StatsCommand;
import com.ultimateduels.config.ConfigManager;
import com.ultimateduels.config.LanguageManager;
import com.ultimateduels.cooldown.CooldownManager;
import com.ultimateduels.database.DatabaseManager;
import com.ultimateduels.duel.DuelManager;
import com.ultimateduels.ffa.FFAManager;
import com.ultimateduels.gui.GUIConfigManager;
import com.ultimateduels.gui.GUIListener;
import com.ultimateduels.gui.GUIManager;
import com.ultimateduels.gui.GUIMessages;
import com.ultimateduels.hooks.PlaceholderAPIHook;
import com.ultimateduels.kit.KitManager;
import com.ultimateduels.listeners.BlockProtectionListener;
import com.ultimateduels.listeners.FoodLevelListener;
import com.ultimateduels.listeners.HealthDisplayListener;
import com.ultimateduels.listeners.InventoryClickListener;
import com.ultimateduels.listeners.LobbyListener;
import com.ultimateduels.listeners.PlayerDamageListener;
import com.ultimateduels.listeners.PlayerDeathListener;
import com.ultimateduels.listeners.PlayerInteractListener;
import com.ultimateduels.listeners.PlayerJoinQuitListener;
import com.ultimateduels.listeners.PlayerMoveListener;
import com.ultimateduels.listeners.SignClickListener;
import com.ultimateduels.listeners.SpectatorListener;
import com.ultimateduels.listeners.WorldChangeListener;
import com.ultimateduels.listeners.arena.ArenaWandListener;
import com.ultimateduels.lobby.LobbyManager;
import com.ultimateduels.party.PartyManager;
import com.ultimateduels.player.PlayerDataManager;
import com.ultimateduels.player.PlayerStateManager;
import com.ultimateduels.queue.QueueManager;
import com.ultimateduels.schematic.SchematicManager;
import com.ultimateduels.scoreboard.ScoreboardManager;
import com.ultimateduels.settings.SettingsManager;
import com.ultimateduels.stats.StatsManager;
import com.ultimateduels.tasks.AutoSaveTask;
import com.ultimateduels.tasks.CooldownCleanupTask;
import com.ultimateduels.tasks.QueueMatchTask;
import com.ultimateduels.tasks.ScoreboardUpdateTask;
import com.ultimateduels.utils.MessageUtils;
import com.ultimateduels.utils.TaskScheduler;
import com.ultimateduels.utils.TextUtil;
import com.ultimateduels.visuals.HealthDisplayManager;
import com.ultimateduels.visuals.HealthPacketSender;
import com.ultimateduels.world.WorldRestrictionManager;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UltimateDuels
extends JavaPlugin {
    private static UltimateDuels instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private SchematicManager schematicManager;
    private DuelManager duelManager;
    private QueueManager queueManager;
    private PartyManager partyManager;
    private FFAManager ffaManager;
    private LobbyManager lobbyManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private SettingsManager settingsManager;
    private TaskScheduler taskScheduler;
    private PlayerStateManager playerStateManager;
    private PlayerDataManager playerDataManager;
    private CooldownManager cooldownManager;
    private HealthDisplayManager healthDisplayManager;
    private GUIManager guiManager;
    private GUIConfigManager guiConfigManager;
    private WorldRestrictionManager worldRestrictionManager;
    private UltimateDuelsAPI api;
    private QueueMatchTask queueMatchTask;
    private ScoreboardUpdateTask scoreboardUpdateTask;
    private AutoSaveTask autoSaveTask;
    private CooldownCleanupTask cooldownCleanupTask;
    private PlaceholderAPIHook placeholderAPIHook;
    private boolean worldEditEnabled = false;
    private boolean faweEnabled = false;
    private boolean fullyEnabled = false;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public void onLoad() {
        instance = this;
        this.createDirectories();
        this.getLogger().info("UltimateDuels is loading...");
    }

    public void onEnable() {
        long startTime = System.currentTimeMillis();
        this.printBanner();
        try {
            if (!this.initializeConfiguration()) {
                this.getLogger().severe("Failed to initialize configuration! Disabling plugin...");
                this.disablePlugin();
                return;
            }
            if (!this.initializeLanguageSystem()) {
                this.getLogger().severe("Failed to initialize language system!");
                this.disablePlugin();
                return;
            }
            MessageUtils.init(this);
            this.getLogger().info("  \u2713 MessageUtils initialized");
            GUIMessages.init(this);
            this.getLogger().info("  \u2713 GUIMessages initialized");
            if (!this.initializeWorldRestrictions()) {
                this.getLogger().warning("World restrictions failed to initialize - continuing without restrictions");
            }
            if (!this.initializeDatabase()) {
                this.getLogger().severe("Failed to initialize database! Disabling plugin...");
                this.disablePlugin();
                return;
            }
            this.initializeHooks();
            if (!this.initializeManagers()) {
                this.getLogger().severe("Failed to initialize managers! Disabling plugin...");
                this.disablePlugin();
                return;
            }
            this.registerCommands();
            this.registerListeners();
            this.startTasks();
            this.initializeAPI();
            Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
                if (Bukkit.getWorld((String)"ffa_world") != null) {
                    this.getLogger().info("[UltimateDuels] ffa_world detected \u2014 reloading FFA arenas...");
                    this.arenaManager.retryLoadArenas();
                    if (this.ffaManager != null) {
                        this.ffaManager.reloadArenas();
                        this.getLogger().info("[UltimateDuels] FFAManager reloaded with updated arenas.");
                    }
                } else {
                    this.getLogger().warning("[UltimateDuels] ffa_world is NOT loaded! FFA arenas will have no spawn points. Make sure ffa_world is in your server's world list.");
                }
            }, 40L);
            this.fullyEnabled = true;
            long loadTime = System.currentTimeMillis() - startTime;
            this.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
            this.getLogger().info("UltimateDuels v" + this.getDescription().getVersion() + " enabled successfully!");
            this.getLogger().info("Load time: " + loadTime + "ms");
            this.getLogger().info("Languages loaded: " + this.languageManager.getLanguages().size());
            this.getLogger().info("Default language: " + this.languageManager.getDefaultLanguage());
            if (!this.worldEditEnabled) {
                this.getLogger().warning("WorldEdit not found - Schematic features disabled!");
            }
            this.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Critical error during plugin initialization!", e);
            this.disablePlugin();
        }
    }

    public void onDisable() {
        this.getLogger().info("UltimateDuels is disabling...");
        try {
            this.stopTasks();
            if (this.statsManager != null) {
                this.getLogger().info("Saving all player stats before shutdown...");
                try {
                    this.statsManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down StatsManager: " + e.getMessage());
                }
            }
            if (this.autoSaveTask != null) {
                try {
                    this.autoSaveTask.forceSave();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error forcing save: " + e.getMessage());
                }
            }
            if (this.healthDisplayManager != null) {
                try {
                    this.healthDisplayManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down HealthDisplayManager: " + e.getMessage());
                }
            }
            if (this.duelManager != null) {
                try {
                    this.duelManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down DuelManager: " + e.getMessage());
                }
            }
            if (this.ffaManager != null) {
                try {
                    this.ffaManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down FFAManager: " + e.getMessage());
                }
            }
            if (this.queueManager != null) {
                try {
                    this.queueManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down QueueManager: " + e.getMessage());
                }
            }
            if (this.partyManager != null) {
                try {
                    this.partyManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down PartyManager: " + e.getMessage());
                }
            }
            if (this.settingsManager != null) {
                try {
                    this.settingsManager.saveAll();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error saving settings: " + e.getMessage());
                }
            }
            if (this.scoreboardManager != null) {
                try {
                    this.scoreboardManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down ScoreboardManager: " + e.getMessage());
                }
            }
            if (this.schematicManager != null && this.arenaManager != null) {
                this.getLogger().info("Restoring all arena schematics...");
                try {
                    this.schematicManager.restoreAllArenasSync();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error restoring arena schematics: " + e.getMessage());
                }
            }
            if (this.arenaManager != null) {
                try {
                    this.arenaManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down ArenaManager: " + e.getMessage());
                }
            }
            if (this.taskScheduler != null) {
                try {
                    this.taskScheduler.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down TaskScheduler: " + e.getMessage());
                }
            }
            if (this.playerDataManager != null) {
                try {
                    this.playerDataManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down PlayerDataManager: " + e.getMessage());
                }
            }
            if (this.languageManager != null) {
                try {
                    this.languageManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down LanguageManager: " + e.getMessage());
                }
            }
            if (this.lobbyManager != null) {
                try {
                    this.teleportAllPlayersToLobby();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error teleporting players to lobby: " + e.getMessage());
                }
            }
            if (this.databaseManager != null) {
                try {
                    this.databaseManager.shutdown();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error shutting down DatabaseManager: " + e.getMessage());
                }
            }
            this.getLogger().info("UltimateDuels disabled successfully!");
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Error during plugin shutdown!", e);
        }
        this.getLogger().info("UltimateDuels has been disabled.");
        instance = null;
    }

    private void teleportAllPlayersToLobby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                this.lobbyManager.teleportToLobby(player);
            }
            catch (Exception e) {
                this.getLogger().warning("Failed to teleport " + player.getName() + " to lobby: " + e.getMessage());
            }
        }
    }

    private void createDirectories() {
        String[] directories;
        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        for (String dir : directories = new String[]{"kits", "arenas", "schematics", "schematics/arenas", "player-kits", "player-settings", "data", "data/playerdata", "data/logs", "backups", "languages"}) {
            File subDir = new File(dataFolder, dir);
            if (subDir.exists()) continue;
            subDir.mkdirs();
        }
    }

    private boolean initializeConfiguration() {
        this.getLogger().info("Loading configuration files...");
        try {
            this.configManager = new ConfigManager(this);
            this.configManager.loadConfig();
            this.getLogger().info("Configuration file loaded successfully!");
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to load configuration!", e);
            return false;
        }
    }

    private boolean initializeLanguageSystem() {
        this.getLogger().info("Initializing language system...");
        try {
            this.languageManager = new LanguageManager(this);
            this.getLogger().info("Language system initialized successfully!");
            this.getLogger().info("  \u2713 " + this.languageManager.getLanguages().size() + " languages loaded");
            this.getLogger().info("  \u2713 Default language: " + this.languageManager.getDefaultLanguage());
            for (String langCode : this.languageManager.getAvailableLanguageCodes()) {
                LanguageManager.Language lang = this.languageManager.getLanguages().get(langCode);
                if (lang == null) continue;
                this.getLogger().info("    - " + lang.getName() + " (" + langCode + ") - " + lang.getMessageCount() + " messages");
            }
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize language system!", e);
            return false;
        }
    }

    private boolean initializeWorldRestrictions() {
        this.getLogger().info("Initializing world restriction system...");
        try {
            this.worldRestrictionManager = new WorldRestrictionManager(this);
            this.getLogger().info("World restriction system initialized successfully!");
            this.getLogger().info("  \u2713 Mode: " + String.valueOf((Object)this.worldRestrictionManager.getMode()));
            if (this.worldRestrictionManager.getMode() == WorldRestrictionManager.RestrictionMode.BLACKLIST) {
                this.getLogger().info("  \u2713 Blacklisted worlds: " + this.worldRestrictionManager.getBlacklistedWorlds().size());
            }
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize world restriction system!", e);
            return false;
        }
    }

    private boolean initializeDatabase() {
        this.getLogger().info("Initializing database...");
        try {
            if (this.taskScheduler == null) {
                this.taskScheduler = new TaskScheduler(this);
                this.getLogger().info("  \u2713 TaskScheduler pre-initialized for database");
            }
            this.databaseManager = new DatabaseManager(this);
            if (!this.databaseManager.initialize()) {
                return false;
            }
            this.getLogger().info("Database initialized successfully!");
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
            return false;
        }
    }

    private void initializeHooks() {
        this.getLogger().info("Initializing hooks...");
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPlugin("WorldEdit") != null) {
            this.worldEditEnabled = true;
            this.getLogger().info("\u2713 WorldEdit hooked successfully!");
        } else {
            this.worldEditEnabled = false;
            this.getLogger().warning("\u25cb WorldEdit not found. Schematic features will be disabled.");
        }
        if (pm.getPlugin("FastAsyncWorldEdit") != null) {
            this.faweEnabled = true;
            this.getLogger().info("\u2713 FastAsyncWorldEdit hooked successfully!");
        } else {
            this.getLogger().info("\u25cb FastAsyncWorldEdit not found. Using WorldEdit if available.");
        }
        if (pm.getPlugin("PlaceholderAPI") != null) {
            this.placeholderAPIHook = new PlaceholderAPIHook(this);
            this.placeholderAPIHook.register();
            this.getLogger().info("\u2713 PlaceholderAPI hooked successfully!");
        } else {
            this.getLogger().info("\u25cb PlaceholderAPI not found. Placeholders will be disabled.");
        }
    }

    private boolean initializeManagers() {
        this.getLogger().info("Initializing managers...");
        try {
            this.playerStateManager = new PlayerStateManager(this);
            this.getLogger().info("  \u2713 PlayerStateManager initialized");
            this.playerDataManager = new PlayerDataManager(this);
            this.getLogger().info("  \u2713 PlayerDataManager initialized");
            this.cooldownManager = new CooldownManager(this);
            this.getLogger().info("  \u2713 CooldownManager initialized");
            this.kitManager = new KitManager(this);
            this.kitManager.loadKits();
            this.getLogger().info("  \u2713 KitManager initialized");
            this.schematicManager = new SchematicManager(this);
            this.getLogger().info("  \u2713 SchematicManager initialized");
            try {
                this.arenaManager = new ArenaManager(this);
                this.getLogger().info("  \u2713 ArenaManager initialized");
            }
            catch (Exception e) {
                this.getLogger().warning("  \u2717 ArenaManager initialization failed: " + e.getMessage());
                this.arenaManager = null;
            }
            this.statsManager = new StatsManager(this);
            this.getLogger().info("  \u2713 StatsManager initialized");
            this.settingsManager = new SettingsManager(this);
            this.getLogger().info("  \u2713 SettingsManager initialized");
            this.lobbyManager = new LobbyManager(this);
            this.getLogger().info("  \u2713 LobbyManager initialized");
            this.partyManager = new PartyManager(this);
            this.getLogger().info("  \u2713 PartyManager initialized");
            if (this.arenaManager != null) {
                this.queueManager = new QueueManager(this);
                this.getLogger().info("  \u2713 QueueManager initialized");
            } else {
                this.getLogger().warning("  \u2717 QueueManager skipped - ArenaManager not available");
            }
            if (this.arenaManager != null) {
                this.duelManager = new DuelManager(this);
                this.getLogger().info("  \u2713 DuelManager initialized");
            } else {
                this.getLogger().warning("  \u2717 DuelManager skipped - ArenaManager not available");
            }
            if (this.arenaManager != null) {
                this.ffaManager = new FFAManager(this);
                this.getLogger().info("  \u2713 FFAManager initialized");
            } else {
                this.getLogger().warning("  \u2717 FFAManager skipped - ArenaManager not available");
            }
            this.scoreboardManager = new ScoreboardManager(this);
            this.getLogger().info("  \u2713 ScoreboardManager initialized");
            HealthPacketSender.init(this);
            this.getLogger().info("  \u2713 HealthPacketSender initialized (available=" + HealthPacketSender.isAvailable() + ")");
            this.healthDisplayManager = new HealthDisplayManager(this);
            this.healthDisplayManager.initialize();
            this.getLogger().info("  \u2713 HealthDisplayManager initialized");
            this.guiManager = new GUIManager(this);
            this.getLogger().info("  \u2713 GUIManager initialized");
            this.guiConfigManager = new GUIConfigManager(this);
            this.getLogger().info("  \u2713 GUIConfigManager initialized");
            this.getLogger().info("All managers initialized successfully!");
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize managers!", e);
            return false;
        }
    }

    private void registerCommands() {
        this.getLogger().info("Registering commands...");
        this.registerCommand("ultimateduels", new UltimateDuelsCommand(this));
        this.registerCommand("duel", new DuelCommand(this));
        this.registerCommand("accept", new AcceptCommand(this));
        this.registerCommand("deny", new DenyCommand(this));
        this.registerCommand("forfeit", new ForfeitCommand(this));
        this.registerCommand("queue", new QueueCommand(this));
        this.registerCommand("leavequeue", new LeaveQueueCommand(this));
        this.registerCommand("togglequeue", new ToggleQueueCommand(this));
        this.registerCommand("party", new PartyCommand(this));
        this.registerCommand("pchat", new PChatCommand(this));
        this.registerCommand("partyaccept", new PartyAcceptCommand(this));
        this.registerCommand("partyleave", new PartyLeaveCommand(this));
        this.registerCommand("ffa", new FFACommand(this));
        this.registerCommand("leaveffa", new LeaveFFACommand(this));
        FFAAdminCommand ffaAdminCommand = new FFAAdminCommand(this);
        PluginCommand ffaAdminCmd = this.getCommand("ffaadmin");
        if (ffaAdminCmd != null) {
            ffaAdminCmd.setExecutor((CommandExecutor)ffaAdminCommand);
            ffaAdminCmd.setTabCompleter((TabCompleter)ffaAdminCommand);
            this.getLogger().info("  \u2713 FFAAdminCommand registered");
        }
        this.registerCommand("spectate", new SpectateCommand(this));
        this.registerCommand("unspectate", new UnspectateCommand(this));
        this.registerCommand("stats", new StatsCommand(this));
        this.registerCommand("leaderboard", new LeaderboardCommand(this));
        this.registerCommand("duelsettings", new DuelSettingsCommand(this));
        this.registerCommand("toggleduels", new ToggleDuelsCommand(this));
        this.registerCommand("toggle", new ToggleCommand(this));
        this.registerCommand("kit", new KitCommand(this));
        this.registerCommand("kiteditor", new KitEditorCommand(this));
        this.registerCommand("kitgive", new KitGiveCommand(this));
        this.registerCommand("lobby", new LobbyCommand(this));
        this.registerCommand("ping", new PingCommand(this));
        this.registerCommand("language", new LanguageCommand(this));
        this.getLogger().info("  \u2713 LanguageCommand registered");
        this.registerCommand("arena", new ArenaCommand(this));
        this.registerCommand("setspawn", new ArenaSetSpawnCommand(this));
        this.registerCommand("setcorner", new ArenaSetCornerCommand(this));
        this.registerCommand("dueladmin", new DuelAdminCommand(this));
        this.registerCommand("forceduel", new ForceDuelCommand(this));
        this.registerCommand("forceend", new ForceEndCommand(this));
        this.registerCommand("setlobby", new SetLobbyCommand(this));
        this.registerCommand("duelsreload", new ReloadCommand(this));
        this.getLogger().info("All commands registered successfully!");
    }

    private void registerCommand(@NotNull String name, @NotNull Object executor) {
        PluginCommand command = this.getCommand(name);
        if (command != null) {
            if (executor instanceof CommandExecutor) {
                CommandExecutor cmdExecutor = (CommandExecutor)executor;
                command.setExecutor(cmdExecutor);
            }
            if (executor instanceof TabCompleter) {
                TabCompleter tabCompleter = (TabCompleter)executor;
                command.setTabCompleter(tabCompleter);
            }
            if ("lobby".equalsIgnoreCase(name)) {
                command.setAliases(java.util.Collections.singletonList("ds"));
            }
            if ("lobby".equalsIgnoreCase(name)) {
                command.setAliases(java.util.Collections.singletonList("ds"));
            }
            if ("lobby".equalsIgnoreCase(name)) {
                command.setAliases(java.util.Collections.singletonList("ds"));
            }
            if ("lobby".equalsIgnoreCase(name)) {
                command.setAliases(java.util.Collections.singletonList("ds"));
            }
            if ("lobby".equalsIgnoreCase(name)) {
                command.setAliases(java.util.Collections.singletonList("ds"));
            }
            if ("lobby".equalsIgnoreCase(name)) {
                command.setAliases(java.util.Collections.singletonList("ds"));
            }
        } else {
            this.getLogger().warning("Command '" + name + "' not found in plugin.yml!");
        }
    }

    private void registerListeners() {
        this.getLogger().info("Registering listeners...");
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents((Listener)new PlayerJoinQuitListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerInteractListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerMoveListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerDeathListener(this), (Plugin)this);
        pm.registerEvents((Listener)new InventoryClickListener(this), (Plugin)this);
        pm.registerEvents((Listener)new FoodLevelListener(this), (Plugin)this);
        pm.registerEvents((Listener)new HealthDisplayListener(this), (Plugin)this);
        pm.registerEvents((Listener)new PlayerDamageListener(this), (Plugin)this);
        pm.registerEvents((Listener)new BlockProtectionListener(this), (Plugin)this);
        pm.registerEvents((Listener)new LobbyListener(this), (Plugin)this);
        pm.registerEvents((Listener)new SpectatorListener(this), (Plugin)this);
        pm.registerEvents((Listener)new WorldChangeListener(this), (Plugin)this);
        pm.registerEvents((Listener)new SignClickListener(this), (Plugin)this);
        pm.registerEvents((Listener)new ArenaWandListener(this), (Plugin)this);
        pm.registerEvents((Listener)new GUIListener(), (Plugin)this);
        this.getLogger().info("All listeners registered successfully!");
    }

    private void startTasks() {
        this.getLogger().info("Starting scheduled tasks...");
        if (this.queueManager != null) {
            this.queueMatchTask = new QueueMatchTask(this, this.queueManager);
            this.queueMatchTask.runTaskTimer((Plugin)this, 20L, 20L);
            this.getLogger().info("  \u2713 QueueMatchTask started");
        }
        this.scoreboardUpdateTask = new ScoreboardUpdateTask(this, this.scoreboardManager);
        this.scoreboardUpdateTask.runTaskTimerAsynchronously((Plugin)this, 20L, 40L);
        this.getLogger().info("  \u2713 ScoreboardUpdateTask started");
        this.autoSaveTask = new AutoSaveTask(this);
        this.autoSaveTask.runTaskTimerAsynchronously((Plugin)this, 6000L, 6000L);
        this.getLogger().info("  \u2713 AutoSaveTask started");
        this.cooldownCleanupTask = new CooldownCleanupTask(this, this.cooldownManager);
        this.cooldownCleanupTask.runTaskTimer((Plugin)this, 1200L, 1200L);
        this.getLogger().info("  \u2713 CooldownCleanupTask started");
        if (this.partyManager != null) {
            this.taskScheduler.runTaskTimer(() -> this.partyManager.cleanupExpiredInvites(), 1200L, 1200L);
            this.getLogger().info("  \u2713 Party cleanup task started");
        }
        if (this.arenaManager != null) {
            this.taskScheduler.runTaskTimerAsync(() -> this.arenaManager.cleanupInactiveArenas(), 600L, 600L);
            this.getLogger().info("  \u2713 Arena cleanup task started");
        }
        this.getLogger().info("All scheduled tasks started successfully!");
    }

    private void stopTasks() {
        this.getLogger().info("Stopping scheduled tasks...");
        if (this.queueMatchTask != null) {
            try {
                this.queueMatchTask.cancel();
                this.queueMatchTask.clearProcessing();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
        if (this.scoreboardUpdateTask != null) {
            try {
                this.scoreboardUpdateTask.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
        if (this.autoSaveTask != null) {
            try {
                this.autoSaveTask.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
        if (this.cooldownCleanupTask != null) {
            try {
                this.cooldownCleanupTask.cancel();
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
        this.getLogger().info("All scheduled tasks stopped.");
    }

    private void initializeAPI() {
        this.getLogger().info("Initializing API...");
        this.api = new UltimateDuelsAPIImpl(this);
        this.getLogger().info("API initialized successfully!");
    }

    private void disablePlugin() {
        this.fullyEnabled = false;
        Bukkit.getPluginManager().disablePlugin((Plugin)this);
    }

    private void printBanner() {
        this.getLogger().info("");
        this.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        this.getLogger().info("  UltimateDuels");
        this.getLogger().info("  Version: " + this.getDescription().getVersion());
        this.getLogger().info("  Multi-Language System: \u2713 Enabled");
        this.getLogger().info("  World Restrictions: \u2713 Enabled");
        this.getLogger().info("\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
        this.getLogger().info("");
    }

    public boolean reload() {
        this.getLogger().info("Reloading UltimateDuels...");
        try {
            this.getLogger().info("Reloading configuration files...");
            this.configManager.reloadConfig();
            if (this.languageManager != null) {
                this.languageManager.reload();
                this.getLogger().info("  \u2713 Language system reloaded (" + this.languageManager.getLanguages().size() + " languages)");
            }
            this.getLogger().info("Reloading managers...");
            if (this.kitManager != null) {
                this.kitManager.reloadKits();
                this.getLogger().info("  \u2713 Kit manager reloaded");
            }
            if (this.arenaManager != null) {
                this.arenaManager.reloadArenas();
                this.getLogger().info("  \u2713 Arena manager reloaded");
            }
            if (this.scoreboardManager != null) {
                this.scoreboardManager.reloadConfig();
                this.getLogger().info("  \u2713 Scoreboard manager reloaded");
            }
            if (this.settingsManager != null) {
                this.settingsManager.reload();
                this.getLogger().info("  \u2713 Settings manager reloaded");
            }
            if (this.healthDisplayManager != null) {
                this.healthDisplayManager.reload();
                this.getLogger().info("  \u2713 Health display manager reloaded");
            }
            if (this.guiConfigManager != null) {
                this.guiConfigManager.reload();
                this.getLogger().info("  \u2713 GUI config reloaded");
            }
            if (this.worldRestrictionManager != null) {
                this.worldRestrictionManager.reload();
                this.getLogger().info("  \u2713 World restriction manager reloaded");
            }
            this.getLogger().info("Restarting tasks...");
            this.restartTasks();
            if (this.scoreboardManager != null) {
                this.getLogger().info("Forcing scoreboard refresh for all online players...");
                for (Player online : Bukkit.getOnlinePlayers()) {
                    try {
                        this.scoreboardManager.removeScoreboard(online);
                        this.scoreboardManager.createScoreboard(online);
                    }
                    catch (Exception e) {
                        this.getLogger().warning("Failed to refresh scoreboard for " + online.getName() + ": " + e.getMessage());
                    }
                }
                this.getLogger().info("  \u2713 All scoreboards refreshed");
            }
            this.getLogger().info("UltimateDuels reloaded successfully!");
            return true;
        }
        catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to reload UltimateDuels!", e);
            return false;
        }
    }

    private void restartTasks() {
        this.stopTasks();
        Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
            this.startTasks();
            this.getLogger().info("Tasks restarted successfully!");
        }, 10L);
    }

    @NotNull
    public static UltimateDuels getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UltimateDuels is not initialized!");
        }
        return instance;
    }

    @Nullable
    public static UltimateDuels getInstanceOrNull() {
        return instance;
    }

    public boolean isFullyEnabled() {
        return this.fullyEnabled;
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @NotNull
    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    @NotNull
    public WorldRestrictionManager getWorldRestrictionManager() {
        return this.worldRestrictionManager;
    }

    @NotNull
    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    @NotNull
    public KitManager getKitManager() {
        return this.kitManager;
    }

    @Nullable
    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    @NotNull
    public SchematicManager getSchematicManager() {
        return this.schematicManager;
    }

    @Nullable
    public DuelManager getDuelManager() {
        return this.duelManager;
    }

    @Nullable
    public QueueManager getQueueManager() {
        return this.queueManager;
    }

    @NotNull
    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    @Nullable
    public FFAManager getFFAManager() {
        return this.ffaManager;
    }

    @NotNull
    public LobbyManager getLobbyManager() {
        return this.lobbyManager;
    }

    @NotNull
    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    @NotNull
    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    @NotNull
    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    @NotNull
    public TaskScheduler getTaskScheduler() {
        return this.taskScheduler;
    }

    @NotNull
    public PlayerStateManager getPlayerStateManager() {
        return this.playerStateManager;
    }

    @NotNull
    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    @NotNull
    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    @NotNull
    public HealthDisplayManager getHealthDisplayManager() {
        return this.healthDisplayManager;
    }

    @NotNull
    public GUIManager getGUIManager() {
        return this.guiManager;
    }

    @NotNull
    public GUIConfigManager getGUIConfigManager() {
        return this.guiConfigManager;
    }

    @Nullable
    public QueueMatchTask getQueueMatchTask() {
        return this.queueMatchTask;
    }

    @Nullable
    public ScoreboardUpdateTask getScoreboardUpdateTask() {
        return this.scoreboardUpdateTask;
    }

    @Nullable
    public AutoSaveTask getAutoSaveTask() {
        return this.autoSaveTask;
    }

    @Nullable
    public CooldownCleanupTask getCooldownCleanupTask() {
        return this.cooldownCleanupTask;
    }

    @Nullable
    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return this.placeholderAPIHook;
    }

    public boolean isWorldEditEnabled() {
        return this.worldEditEnabled;
    }

    public boolean isFAWEEnabled() {
        return this.faweEnabled;
    }

    @NotNull
    public MiniMessage getMiniMessage() {
        return this.miniMessage;
    }

    @Nullable
    public UltimateDuelsAPI getAPI() {
        return this.api;
    }

    @NotNull
    public Component parseText(@NotNull String text) {
        return TextUtil.parse(text);
    }

    @NotNull
    public List<Component> parseText(@NotNull List<String> lines) {
        return lines.stream().map(TextUtil::parse).toList();
    }

    @NotNull
    public String toLegacy(@NotNull Component component) {
        return TextUtil.toLegacy(component);
    }

    @NotNull
    public String colorize(@NotNull String text) {
        return TextUtil.colorize(text);
    }

    public void log(@NotNull Component message) {
        this.getComponentLogger().info(message);
    }

    public void debug(@NotNull String message) {
        if (this.configManager != null && this.configManager.isDebugMode()) {
            this.getLogger().info("[DEBUG] " + message);
        }
    }

    public void debug(@NotNull Component message) {
        if (this.configManager != null && this.configManager.isDebugMode()) {
            this.getComponentLogger().info(Component.text((String)"[DEBUG] ", (TextColor)NamedTextColor.GRAY).append(message));
        }
    }

    public void forceSave() {
        if (this.autoSaveTask != null) {
            this.getLogger().info("Forcing data save...");
            this.autoSaveTask.forceSave();
        }
    }

    @NotNull
    public Map<String, Object> getTaskStatistics() {
        HashMap<String, Object> stats = new HashMap<String, Object>();
        if (this.queueMatchTask != null) {
            stats.put("queueMatchTask.running", !this.queueMatchTask.isCancelled());
        }
        if (this.scoreboardUpdateTask != null) {
            stats.put("scoreboardUpdateTask.running", !this.scoreboardUpdateTask.isCancelled());
            stats.put("scoreboardUpdateTask.tickCounter", this.scoreboardUpdateTask.getTickCounter());
        }
        if (this.autoSaveTask != null) {
            stats.put("autoSaveTask.running", !this.autoSaveTask.isCancelled());
            stats.put("autoSaveTask.saveCount", this.autoSaveTask.getSaveCount());
        }
        if (this.cooldownCleanupTask != null) {
            stats.put("cooldownCleanupTask.running", !this.cooldownCleanupTask.isCancelled());
            stats.put("cooldownCleanupTask.cleanupCount", this.cooldownCleanupTask.getCleanupCount());
            stats.put("cooldownCleanupTask.totalRemoved", this.cooldownCleanupTask.getTotalEntriesRemoved());
        }
        return stats;
    }

    @NotNull
    public Map<String, Object> getReloadStatistics() {
        HashMap<String, Object> stats = new HashMap<String, Object>();
        stats.put("plugin.fullyEnabled", this.fullyEnabled);
        stats.put("plugin.version", this.getDescription().getVersion());
        if (this.configManager != null) {
            stats.put("config.debugMode", this.configManager.isDebugMode());
        }
        if (this.languageManager != null) {
            stats.put("languages.count", this.languageManager.getLanguages().size());
            stats.put("languages.default", this.languageManager.getDefaultLanguage());
        }
        if (this.kitManager != null) {
            stats.put("kits.count", this.kitManager.getAllAdminKits().size());
        }
        if (this.arenaManager != null) {
            stats.put("arenas.count", this.arenaManager.getAllDuelArenas().size());
        }
        if (this.scoreboardManager != null) {
            stats.put("scoreboards.active", this.scoreboardManager.getActiveScoreboardCount());
        }
        if (this.worldRestrictionManager != null) {
            stats.put("worldRestrictions.mode", this.worldRestrictionManager.getMode().name());
            stats.put("worldRestrictions.blacklistedWorlds", this.worldRestrictionManager.getBlacklistedWorlds().size());
        }
        stats.put("worldEdit.enabled", this.worldEditEnabled);
        stats.put("fawe.enabled", this.faweEnabled);
        stats.put("placeholderAPI.enabled", this.placeholderAPIHook != null);
        return stats;
    }
}

