/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.config;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.config.LanguageManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigManager {
    private final UltimateDuels plugin;
    private FileConfiguration config;
    private File configFile;
    private boolean debugMode;
    private String serverName;
    private String websiteUrl;
    private String prefix;
    private DatabaseSettings databaseSettings;
    private LobbySettings lobbySettings;
    private DuelSettings duelSettings;
    private QueueSettings queueSettings;
    private FFASettings ffaSettings;
    private PartySettings partySettings;
    private CombatSettings combatSettings;
    private ScoreboardSettings scoreboardSettings;
    private SoundSettings soundSettings;

    public ConfigManager(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void loadConfig() {
        this.createDefaultConfigIfNotExists();
        this.config = YamlConfiguration.loadConfiguration((File)this.configFile);
        this.addMissingDefaults();
        this.cacheValues();
        this.plugin.getLogger().info("Configuration loaded successfully!");
    }

    private void createDefaultConfigIfNotExists() {
        if (!this.configFile.exists()) {
            try {
                this.configFile.getParentFile().mkdirs();
                this.plugin.saveResource("config.yml", false);
                this.plugin.getLogger().info("Created new config.yml file");
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Failed to create config.yml: " + e.getMessage());
            }
        }
    }

    private void addMissingDefaults() {
        InputStream defaultStream = this.plugin.getResource("config.yml");
        if (defaultStream == null) {
            return;
        }
        try {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            boolean modified = false;
            for (String key : defaultConfig.getKeys(true)) {
                if (this.config.contains(key)) continue;
                this.config.set(key, defaultConfig.get(key));
                modified = true;
            }
            if (modified) {
                this.saveConfig();
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to load default config: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        this.plugin.getLogger().info("Reloading config.yml...");
        this.config = YamlConfiguration.loadConfiguration((File)this.configFile);
        this.addMissingDefaults();
        this.cacheValues();
        this.plugin.getLogger().info("Configuration reloaded successfully!");
    }

    public void saveConfig() {
        try {
            this.config.save(this.configFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save configuration!", e);
        }
    }

    private void cacheValues() {
        this.debugMode = this.config.getBoolean("general.debug-mode", false);
        this.serverName = this.config.getString("general.server-name", "UltimateDuels");
        this.websiteUrl = this.config.getString("general.website", "play.ultimateduels.com");
        this.prefix = this.config.getString("general.prefix", "&8[&6UltimateDuels&8] ");
        this.loadDatabaseSettings();
        this.loadLobbySettings();
        this.loadDuelSettings();
        this.loadQueueSettings();
        this.loadFFASettings();
        this.loadPartySettings();
        this.loadCombatSettings();
        this.loadScoreboardSettings();
        this.loadSoundSettings();
    }

    private void loadDatabaseSettings() {
        ConfigurationSection section = this.config.getConfigurationSection("database");
        if (section == null) {
            this.databaseSettings = new DatabaseSettings();
            return;
        }
        this.databaseSettings = new DatabaseSettings(section.getString("type", "MYSQL"), section.getString("host", "localhost"), section.getInt("port", 3306), section.getString("database", "ultimateduels"), section.getString("username", "root"), section.getString("password", ""), section.getString("table-prefix", "ud_"), section.getBoolean("ssl", false), section.getInt("pool-size", 10), section.getLong("connection-timeout", 30000L), section.getLong("max-lifetime", 1800000L), section.getBoolean("auto-reconnect", true));
    }

    private void loadLobbySettings() {
        String worldName;
        World world;
        ConfigurationSection section = this.config.getConfigurationSection("lobby");
        if (section == null) {
            this.lobbySettings = new LobbySettings();
            return;
        }
        Location spawnLocation = null;
        ConfigurationSection spawnSection = section.getConfigurationSection("spawn");
        if (spawnSection != null && (world = Bukkit.getWorld((String)(worldName = spawnSection.getString("world", "world")))) != null) {
            spawnLocation = new Location(world, spawnSection.getDouble("x", 0.0), spawnSection.getDouble("y", 64.0), spawnSection.getDouble("z", 0.0), (float)spawnSection.getDouble("yaw", 0.0), (float)spawnSection.getDouble("pitch", 0.0));
        }
        GameMode lobbyGamemode = GameMode.ADVENTURE;
        String gamemodeStr = section.getString("lobby-gamemode", "ADVENTURE");
        try {
            lobbyGamemode = GameMode.valueOf((String)gamemodeStr.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            this.plugin.getLogger().warning("Invalid lobby-gamemode in config.yml: " + gamemodeStr + ", using ADVENTURE");
            lobbyGamemode = GameMode.ADVENTURE;
        }
        this.lobbySettings = new LobbySettings(section.getBoolean("enabled", true), section.getString("world-name", "lobby"), spawnLocation, section.getBoolean("separate-world", false), section.getBoolean("void-teleport", true), section.getInt("void-level", 0), section.getBoolean("clear-inventory", true), section.getBoolean("heal-on-join", true), section.getBoolean("clear-effects", true), section.getBoolean("prevent-damage", true), section.getBoolean("prevent-hunger", true), section.getBoolean("prevent-build", true), section.getBoolean("prevent-weather", true), section.getBoolean("give-lobby-items", true), section.getInt("hotbar-items.queue-menu-slot", 0), section.getInt("hotbar-items.party-menu-slot", 1), section.getInt("hotbar-items.kit-editor-slot", 2), section.getInt("hotbar-items.ffa-menu-slot", 3), section.getInt("hotbar-items.settings-slot", 8), lobbyGamemode);
    }

    private void loadDuelSettings() {
        ConfigurationSection section = this.config.getConfigurationSection("duels");
        if (section == null) {
            this.duelSettings = new DuelSettings();
            return;
        }
        this.duelSettings = new DuelSettings(section.getInt("request-timeout", 60), section.getInt("countdown-seconds", 3), section.getInt("post-match-delay", 3), section.getInt("max-rounds", 20), section.getInt("default-rounds", 1), section.getBoolean("allow-spectators", true), section.getInt("max-spectators-per-match", 50), section.getBoolean("broadcast-results", true), section.getBoolean("drop-items-on-death", false), section.getBoolean("keep-inventory", false), section.getBoolean("allow-pearl-damage", false), section.getBoolean("allow-self-damage", true), section.getBoolean("regen-arena-after-match", true), section.getBoolean("teleport-on-void", true), section.getInt("void-death-level", 0), section.getInt("arena-border-size", 100), section.getBoolean("prevent-escape", true), section.getDouble("escape-distance", 50.0), section.getInt("escape-countdown", 10), section.getBoolean("instant-respawn", true));
    }

    private void loadQueueSettings() {
        ConfigurationSection section = this.config.getConfigurationSection("queue");
        if (section == null) {
            this.queueSettings = new QueueSettings();
            return;
        }
        this.queueSettings = new QueueSettings(section.getBoolean("enabled", true), section.getInt("match-interval-ticks", 20), section.getInt("max-queue-time", 300), section.getBoolean("allow-cancel", true), section.getBoolean("notify-on-join", true), section.getBoolean("notify-on-match", true), section.getBoolean("ping-based-matching", false), section.getInt("max-ping-difference", 100), section.getBoolean("elo-based-matching", false), section.getInt("elo-range-initial", 100), section.getInt("elo-range-expansion", 50), section.getInt("elo-range-max", 500), section.getInt("elo-expansion-interval", 30));
    }

    private void loadFFASettings() {
        ConfigurationSection section = this.config.getConfigurationSection("ffa");
        if (section == null) {
            this.ffaSettings = new FFASettings();
            return;
        }
        this.ffaSettings = new FFASettings(section.getBoolean("enabled", true), section.getInt("respawn-delay-ticks", 60), section.getInt("spawn-protection-ticks", 60), section.getBoolean("spawn-protection-indicator", true), section.getBoolean("regen-on-kill", true), section.getDouble("regen-health-amount", 4.0), section.getBoolean("refill-on-kill", false), section.getBoolean("show-kill-messages", true), section.getBoolean("show-killstreak-messages", true), section.getInt("killstreak-announce-interval", 5), section.getInt("max-players-per-arena", 50), section.getBoolean("regen-arena-on-restart", true), section.getBoolean("allow-spectators", false), section.getInt("combat-tag-seconds", 15), section.getBoolean("allow-block-break", false), section.getBoolean("allow-block-place", false));
    }

    private void loadPartySettings() {
        ConfigurationSection section = this.config.getConfigurationSection("party");
        if (section == null) {
            this.partySettings = new PartySettings();
            return;
        }
        this.partySettings = new PartySettings(section.getBoolean("enabled", true), section.getInt("max-size", 10), section.getInt("invite-timeout", 60), section.getBoolean("friendly-fire", false), section.getBoolean("party-chat-enabled", true), section.getString("party-chat-format", "&7[&bParty&7] &f{player}&7: &7{message}"), section.getBoolean("allow-split-duels", true), section.getBoolean("allow-party-vs-party", true), section.getBoolean("leader-only-queue", true), section.getBoolean("disband-on-leader-leave", false));
    }

    private void loadCombatSettings() {
        ConfigurationSection section = this.config.getConfigurationSection("combat");
        if (section == null) {
            this.combatSettings = new CombatSettings();
            return;
        }
        this.combatSettings = new CombatSettings(section.getDouble("knockback-horizontal", 0.4), section.getDouble("knockback-vertical", 0.4), section.getDouble("sprint-knockback-multiplier", 1.0), section.getBoolean("old-pvp-mechanics", false), section.getDouble("attack-cooldown", 0.5), section.getBoolean("sweep-attack", true), section.getBoolean("shield-mechanics", true), section.getDouble("projectile-damage-multiplier", 1.0), section.getBoolean("fire-aspect-enabled", true), section.getBoolean("thorns-enabled", true), section.getDouble("critical-hit-multiplier", 1.5), section.getBoolean("totem-of-undying-works", false), section.getInt("max-combo-hits", 0), section.getInt("hit-delay-ticks", 10));
    }

    private void loadScoreboardSettings() {
        ConfigurationSection section = this.config.getConfigurationSection("scoreboard");
        if (section == null) {
            this.scoreboardSettings = new ScoreboardSettings();
            return;
        }
        this.scoreboardSettings = new ScoreboardSettings(section.getBoolean("enabled", true), section.getInt("update-interval-ticks", 10), section.getString("lobby-title", "&6&lUltimateDuels"), section.getStringList("lobby-lines"), section.getString("duel-title", "&6&lDuel"), section.getStringList("duel-lines"), section.getString("ffa-title", "&6&lFFA"), section.getStringList("ffa-lines"), section.getString("spectator-title", "&6&lSpectating"), section.getStringList("spectator-lines"), section.getBoolean("animated-title", false), section.getInt("animation-interval-ticks", 5));
    }

    private void loadSoundSettings() {
        ConfigurationSection section = this.config.getConfigurationSection("sounds");
        if (section == null) {
            this.soundSettings = new SoundSettings();
            return;
        }
        this.soundSettings = new SoundSettings(section.getBoolean("enabled", true), section.getString("countdown-tick", "BLOCK_NOTE_BLOCK_HAT"), section.getString("countdown-start", "BLOCK_NOTE_BLOCK_PLING"), section.getString("match-start", "ENTITY_ENDER_DRAGON_GROWL"), section.getString("match-end-win", "UI_TOAST_CHALLENGE_COMPLETE"), section.getString("match-end-lose", "ENTITY_WITHER_DEATH"), section.getString("round-win", "ENTITY_PLAYER_LEVELUP"), section.getString("round-lose", "ENTITY_VILLAGER_NO"), section.getString("queue-join", "BLOCK_NOTE_BLOCK_CHIME"), section.getString("queue-match-found", "ENTITY_EXPERIENCE_ORB_PICKUP"), section.getString("duel-request-receive", "ENTITY_EXPERIENCE_ORB_PICKUP"), section.getString("duel-request-accept", "ENTITY_VILLAGER_YES"), section.getString("duel-request-deny", "ENTITY_VILLAGER_NO"), section.getString("kill-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"), section.getString("death-sound", "ENTITY_PLAYER_DEATH"), section.getString("gui-click", "UI_BUTTON_CLICK"), section.getString("gui-error", "ENTITY_VILLAGER_NO"), (float)section.getDouble("volume", 1.0), (float)section.getDouble("pitch", 1.0));
    }

    @NotNull
    public FileConfiguration getConfig() {
        return this.config;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        this.config.set("general.debug-mode", (Object)debugMode);
        this.saveConfig();
    }

    @NotNull
    public String getServerName() {
        return this.serverName;
    }

    @NotNull
    public String getWebsiteUrl() {
        return this.websiteUrl;
    }

    @NotNull
    public String getPrefix() {
        LanguageManager langManager = this.plugin.getLanguageManager();
        if (langManager != null) {
            return langManager.getDefaultPrefix();
        }
        return this.prefix;
    }

    @NotNull
    public String getPrefix(@NotNull Player player) {
        LanguageManager langManager = this.plugin.getLanguageManager();
        if (langManager != null) {
            return langManager.getPrefix(player);
        }
        return this.prefix;
    }

    @NotNull
    public String getConfigPrefix() {
        return this.prefix;
    }

    @NotNull
    public DatabaseSettings getDatabaseSettings() {
        return this.databaseSettings;
    }

    @NotNull
    public LobbySettings getLobbySettings() {
        return this.lobbySettings;
    }

    @NotNull
    public DuelSettings getDuelSettings() {
        return this.duelSettings;
    }

    @NotNull
    public QueueSettings getQueueSettings() {
        return this.queueSettings;
    }

    @NotNull
    public FFASettings getFFASettings() {
        return this.ffaSettings;
    }

    @NotNull
    public PartySettings getPartySettings() {
        return this.partySettings;
    }

    @NotNull
    public CombatSettings getCombatSettings() {
        return this.combatSettings;
    }

    @NotNull
    public ScoreboardSettings getScoreboardSettings() {
        return this.scoreboardSettings;
    }

    @NotNull
    public SoundSettings getSoundSettings() {
        return this.soundSettings;
    }

    @NotNull
    public List<String> getScoreboardEnabledWorlds() {
        try {
            List<String> worlds = this.config.getStringList("scoreboard.worlds");
            return worlds != null ? worlds : Arrays.asList("world", "lobby", "duels", "duel_lobby");
        }
        catch (Exception e) {
            return Arrays.asList("world", "lobby", "duels", "duel_lobby");
        }
    }

    @NotNull
    public List<String> getLeaderboardWorlds() {
        try {
            List worlds = this.config.getStringList("scoreboard.leaderboard-worlds");
            if (worlds == null || worlds.isEmpty()) {
                return this.getScoreboardEnabledWorlds().stream().filter(world -> world.toLowerCase().contains("duel") || world.toLowerCase().contains("arena")).toList();
            }
            return worlds;
        }
        catch (Exception e) {
            return Arrays.asList("duels", "duel_lobby", "arena");
        }
    }

    public boolean isScoreboardEnabledInWorld(@NotNull String worldName) {
        return this.getScoreboardEnabledWorlds().stream().anyMatch(world -> world.equalsIgnoreCase(worldName));
    }

    public boolean isLeaderboardEnabledInWorld(@NotNull String worldName) {
        return this.getLeaderboardWorlds().stream().anyMatch(world -> world.equalsIgnoreCase(worldName));
    }

    public <T> T get(@NotNull String path, @NotNull T defaultValue) {
        Object value = this.config.get(path, defaultValue);
        try {
            return (T)value;
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public void set(@NotNull String path, @Nullable Object value) {
        this.config.set(path, value);
        this.saveConfig();
    }

    public boolean has(@NotNull String path) {
        return this.config.contains(path);
    }

    public void setLobbySpawn(@NotNull Location location) {
        this.config.set("lobby.spawn.world", (Object)location.getWorld().getName());
        this.config.set("lobby.spawn.x", (Object)location.getX());
        this.config.set("lobby.spawn.y", (Object)location.getY());
        this.config.set("lobby.spawn.z", (Object)location.getZ());
        this.config.set("lobby.spawn.yaw", (Object)Float.valueOf(location.getYaw()));
        this.config.set("lobby.spawn.pitch", (Object)Float.valueOf(location.getPitch()));
        this.saveConfig();
        this.loadLobbySettings();
    }

    @NotNull
    public Map<String, Object> getDebugInfo() {
        HashMap<String, Object> debug = new HashMap<String, Object>();
        debug.put("configFileExists", this.configFile.exists());
        debug.put("configFileSize", this.configFile.length());
        debug.put("configFileLastModified", new Date(this.configFile.lastModified()));
        debug.put("debugMode", this.debugMode);
        debug.put("scoreboardEnabledWorlds", this.getScoreboardEnabledWorlds());
        debug.put("leaderboardWorlds", this.getLeaderboardWorlds());
        debug.put("configVersion", this.config.getString("config-version", "unknown"));
        debug.put("ffaAllowBlockBreak", this.ffaSettings.allowBlockBreak());
        debug.put("ffaAllowBlockPlace", this.ffaSettings.allowBlockPlace());
        return debug;
    }

    public record DatabaseSettings(String type, String host, int port, String database, String username, String password, String tablePrefix, boolean ssl, int poolSize, long connectionTimeout, long maxLifetime, boolean autoReconnect) {
        public DatabaseSettings() {
            this("MYSQL", "localhost", 3306, "ultimateduels", "root", "", "ud_", false, 10, 30000L, 1800000L, true);
        }

        public boolean isMySQL() {
            return "MYSQL".equalsIgnoreCase(this.type);
        }

        public boolean isSQLite() {
            return "SQLITE".equalsIgnoreCase(this.type);
        }

        @NotNull
        public String getJdbcUrl() {
            if (this.isSQLite()) {
                return "jdbc:sqlite:plugins/UltimateDuels/data/database.db";
            }
            StringBuilder url = new StringBuilder("jdbc:mysql://");
            url.append(this.host).append(":").append(this.port).append("/").append(this.database);
            url.append("?useSSL=").append(this.ssl);
            url.append("&autoReconnect=").append(this.autoReconnect);
            url.append("&useUnicode=true&characterEncoding=UTF-8");
            return url.toString();
        }
    }

    public record LobbySettings(boolean enabled, String worldName, @Nullable Location spawnLocation, boolean separateWorld, boolean voidTeleport, int voidLevel, boolean clearInventory, boolean healOnJoin, boolean clearEffects, boolean preventDamage, boolean preventHunger, boolean preventBuild, boolean preventWeather, boolean giveLobbyItems, int queueMenuSlot, int partyMenuSlot, int kitEditorSlot, int ffaMenuSlot, int settingsSlot, GameMode lobbyGamemode) {
        public LobbySettings() {
            this(true, "lobby", null, false, true, 0, true, true, true, true, true, true, true, true, 0, 1, 2, 3, 8, GameMode.ADVENTURE);
        }
    }

    public record DuelSettings(int requestTimeout, int countdownSeconds, int postMatchDelay, int maxRounds, int defaultRounds, boolean allowSpectators, int maxSpectatorsPerMatch, boolean broadcastResults, boolean dropItemsOnDeath, boolean keepInventory, boolean allowPearlDamage, boolean allowSelfDamage, boolean regenArenaAfterMatch, boolean teleportOnVoid, int voidDeathLevel, int arenaBorderSize, boolean preventEscape, double escapeDistance, int escapeCountdown, boolean instantRespawn) {
        public DuelSettings() {
            this(60, 3, 3, 20, 1, true, 50, true, false, false, false, true, true, true, 0, 100, true, 50.0, 10, true);
        }
    }

    public record QueueSettings(boolean enabled, int matchIntervalTicks, int maxQueueTime, boolean allowCancel, boolean notifyOnJoin, boolean notifyOnMatch, boolean pingBasedMatching, int maxPingDifference, boolean eloBasedMatching, int eloRangeInitial, int eloRangeExpansion, int eloRangeMax, int eloExpansionInterval) {
        public QueueSettings() {
            this(true, 20, 300, true, true, true, false, 100, false, 100, 50, 500, 30);
        }
    }

    public record FFASettings(boolean enabled, int respawnDelayTicks, int spawnProtectionTicks, boolean spawnProtectionIndicator, boolean regenOnKill, double regenHealthAmount, boolean refillOnKill, boolean showKillMessages, boolean showKillstreakMessages, int killstreakAnnounceInterval, int maxPlayersPerArena, boolean regenArenaOnRestart, boolean allowSpectators, int combatTagSeconds, boolean allowBlockBreak, boolean allowBlockPlace) {
        public FFASettings() {
            this(true, 60, 60, true, true, 4.0, false, true, true, 5, 50, true, false, 15, false, false);
        }
    }

    public record PartySettings(boolean enabled, int maxSize, int inviteTimeout, boolean friendlyFire, boolean partyChatEnabled, String partyChatFormat, boolean allowSplitDuels, boolean allowPartyVsParty, boolean leaderOnlyQueue, boolean disbandOnLeaderLeave) {
        public PartySettings() {
            this(true, 10, 60, false, true, "&7[&bParty&7] &f{player}&7: &7{message}", true, true, true, false);
        }
    }

    public record CombatSettings(double knockbackHorizontal, double knockbackVertical, double sprintKnockbackMultiplier, boolean oldPvpMechanics, double attackCooldown, boolean sweepAttack, boolean shieldMechanics, double projectileDamageMultiplier, boolean fireAspectEnabled, boolean thornsEnabled, double criticalHitMultiplier, boolean totemOfUndyingWorks, int maxComboHits, int hitDelayTicks) {
        public CombatSettings() {
            this(0.4, 0.4, 1.0, false, 0.5, true, true, 1.0, true, true, 1.5, false, 0, 10);
        }
    }

    public record ScoreboardSettings(boolean enabled, int updateIntervalTicks, String lobbyTitle, List<String> lobbyLines, String duelTitle, List<String> duelLines, String ffaTitle, List<String> ffaLines, String spectatorTitle, List<String> spectatorLines, boolean animatedTitle, int animationIntervalTicks) {
        public ScoreboardSettings() {
            this(true, 10, "&6&lUltimateDuels", List.of("", "&7Player: &f{player}", ""), "&6&lDuel", List.of("", "&7Opponent: &f{opponent}", ""), "&6&lFFA", List.of("", "&7Kills: &f{kills}", ""), "&6&lSpectating", List.of("", "&7Watching: &f{target}", ""), false, 5);
        }
    }

    public record SoundSettings(boolean enabled, String countdownTick, String countdownStart, String matchStart, String matchEndWin, String matchEndLose, String roundWin, String roundLose, String queueJoin, String queueMatchFound, String duelRequestReceive, String duelRequestAccept, String duelRequestDeny, String killSound, String deathSound, String guiClick, String guiError, float volume, float pitch) {
        public SoundSettings() {
            this(true, "BLOCK_NOTE_BLOCK_HAT", "BLOCK_NOTE_BLOCK_PLING", "ENTITY_ENDER_DRAGON_GROWL", "UI_TOAST_CHALLENGE_COMPLETE", "ENTITY_WITHER_DEATH", "ENTITY_PLAYER_LEVELUP", "ENTITY_VILLAGER_NO", "BLOCK_NOTE_BLOCK_CHIME", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_VILLAGER_YES", "ENTITY_VILLAGER_NO", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_PLAYER_DEATH", "UI_BUTTON_CLICK", "ENTITY_VILLAGER_NO", 1.0f, 1.0f);
        }
    }
}

