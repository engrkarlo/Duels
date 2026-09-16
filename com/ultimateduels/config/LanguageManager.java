/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.config;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.utils.TextUtil;
import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LanguageManager {
    private final UltimateDuels plugin;
    private final MiniMessage miniMessage;
    private final Map<String, Language> languages = new ConcurrentHashMap<String, Language>();
    private final Map<UUID, String> playerLanguages = new ConcurrentHashMap<UUID, String>();
    private String defaultLanguage;
    private File languagesDir;
    public static final String ENGLISH = "en";
    public static final String RUSSIAN = "ru";
    public static final String SPANISH = "es";
    public static final String GERMAN = "de";
    public static final String FRENCH = "fr";
    public static final String PORTUGUESE = "pt";
    public static final String CHINESE = "zh";
    public static final String JAPANESE = "ja";
    public static final String KOREAN = "ko";
    public static final String ARABIC = "ar";

    public LanguageManager(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.languagesDir = new File(plugin.getDataFolder(), "languages");
        this.initialize();
    }

    private void initialize() {
        if (!this.languagesDir.exists()) {
            this.languagesDir.mkdirs();
        }
        this.defaultLanguage = this.plugin.getConfig().getString("language.default", ENGLISH);
        this.createDefaultLanguageFiles();
        this.loadAllLanguages();
        this.loadPlayerPreferences();
        this.plugin.getLogger().info("LanguageManager initialized with " + this.languages.size() + " languages");
        this.plugin.getLogger().info("Default language: " + this.defaultLanguage);
    }

    private void createDefaultLanguageFiles() {
        String[] defaultLanguages;
        for (String langCode : defaultLanguages = new String[]{ENGLISH, RUSSIAN, SPANISH, GERMAN, FRENCH, PORTUGUESE}) {
            File langFile = new File(this.languagesDir, langCode + ".yml");
            if (langFile.exists()) continue;
            try {
                String resourcePath = "languages/" + langCode + ".yml";
                InputStream resource = this.plugin.getResource(resourcePath);
                if (resource != null) {
                    this.plugin.saveResource(resourcePath, false);
                    this.plugin.getLogger().info("Created language file: " + langCode + ".yml");
                    continue;
                }
                this.createFromTemplate(langFile, langCode);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to create " + langCode + ".yml: " + e.getMessage());
            }
        }
    }

    private void createFromTemplate(File langFile, String langCode) {
        try {
            File englishFile = new File(this.languagesDir, "en.yml");
            if (englishFile.exists()) {
                YamlConfiguration template = YamlConfiguration.loadConfiguration((File)englishFile);
                template.set("language.code", (Object)langCode);
                template.set("language.name", (Object)this.getLanguageName(langCode));
                template.save(langFile);
                this.plugin.getLogger().info("Created " + langCode + ".yml from template");
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to create template for " + langCode + ": " + e.getMessage());
        }
    }

    private void loadAllLanguages() {
        this.languages.clear();
        File[] files = this.languagesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            this.plugin.getLogger().warning("No language files found! Creating English default...");
            this.createEnglishDefault();
            return;
        }
        int loaded = 0;
        for (File file : files) {
            try {
                String langCode = file.getName().replace(".yml", "");
                Language language = new Language(langCode, file);
                if (!language.load()) continue;
                this.languages.put(langCode, language);
                ++loaded;
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to load language file " + file.getName() + ": " + e.getMessage());
            }
        }
        if (loaded == 0) {
            this.plugin.getLogger().severe("No languages loaded! Creating English default...");
            this.createEnglishDefault();
        }
        if (!this.languages.containsKey(this.defaultLanguage)) {
            this.plugin.getLogger().warning("Default language '" + this.defaultLanguage + "' not found! Using English.");
            this.defaultLanguage = ENGLISH;
            if (!this.languages.containsKey(ENGLISH)) {
                this.createEnglishDefault();
            }
        }
    }

    private void createEnglishDefault() {
        try {
            File englishFile = new File(this.languagesDir, "en.yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("language.code", (Object)ENGLISH);
            config.set("language.name", (Object)"English");
            config.set("general.prefix", (Object)"&8[&6UltimateDuels&8] ");
            config.set("general.no-permission", (Object)"{prefix}&cYou don't have permission to do that!");
            config.save(englishFile);
            Language english = new Language(ENGLISH, englishFile);
            english.load();
            this.languages.put(ENGLISH, english);
            this.plugin.getLogger().info("Created default English language file");
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("Failed to create English default: " + e.getMessage());
        }
    }

    private void loadPlayerPreferences() {
        File prefsFile = new File(this.plugin.getDataFolder(), "player-languages.yml");
        if (!prefsFile.exists()) {
            return;
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration((File)prefsFile);
            for (String uuidStr : config.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String langCode = config.getString(uuidStr);
                    if (langCode == null || !this.languages.containsKey(langCode)) continue;
                    this.playerLanguages.put(uuid, langCode);
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to load player language preferences: " + e.getMessage());
        }
    }

    public void savePlayerPreferences() {
        File prefsFile = new File(this.plugin.getDataFolder(), "player-languages.yml");
        try {
            YamlConfiguration config = new YamlConfiguration();
            for (Map.Entry<UUID, String> entry : this.playerLanguages.entrySet()) {
                config.set(entry.getKey().toString(), (Object)entry.getValue());
            }
            config.save(prefsFile);
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to save player language preferences: " + e.getMessage());
        }
    }

    @NotNull
    public String getPlayerLanguage(@NotNull Player player) {
        return this.getPlayerLanguage(player.getUniqueId());
    }

    @NotNull
    public String getPlayerLanguage(@NotNull UUID uuid) {
        return this.playerLanguages.getOrDefault(uuid, this.defaultLanguage);
    }

    public boolean setPlayerLanguage(@NotNull Player player, @NotNull String langCode) {
        return this.setPlayerLanguage(player.getUniqueId(), langCode);
    }

    public boolean setPlayerLanguage(@NotNull UUID uuid, @NotNull String langCode) {
        if (!this.languages.containsKey(langCode)) {
            return false;
        }
        this.playerLanguages.put(uuid, langCode);
        this.savePlayerPreferences();
        return true;
    }

    public void resetPlayerLanguage(@NotNull Player player) {
        this.playerLanguages.remove(player.getUniqueId());
        this.savePlayerPreferences();
    }

    @NotNull
    public String getPrefix(@NotNull Player player) {
        return this.getPrefixForLanguage(this.getPlayerLanguage(player));
    }

    @NotNull
    public String getDefaultPrefix() {
        return this.getPrefixForLanguage(this.defaultLanguage);
    }

    @NotNull
    public String getPrefixForLanguage(@NotNull String langCode) {
        String prefix;
        Language defaultLang;
        String prefix2;
        Language language = this.languages.get(langCode);
        if (language != null && language.config != null && (prefix2 = language.config.getString("general.prefix")) != null && !prefix2.isEmpty()) {
            return prefix2;
        }
        if (!langCode.equals(this.defaultLanguage) && (defaultLang = this.languages.get(this.defaultLanguage)) != null && defaultLang.config != null && (prefix = defaultLang.config.getString("general.prefix")) != null && !prefix.isEmpty()) {
            return prefix;
        }
        return "&8[&6UltimateDuels&8] ";
    }

    @NotNull
    private String replaceAllPlaceholders(@NotNull Player player, @NotNull String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.contains("{prefix}")) {
            String prefix = this.getPrefix(player);
            text = text.replace("{prefix}", prefix);
        }
        if (text.contains("{player}")) {
            text = text.replace("{player}", player.getName());
        }
        return text;
    }

    @NotNull
    public String getRaw(@NotNull Player player, @NotNull String path) {
        Language defaultLang;
        String langCode = this.getPlayerLanguage(player);
        Language language = this.languages.get(langCode);
        String message = null;
        if (language != null) {
            message = language.getRaw(path);
        }
        if (message == null && (defaultLang = this.languages.get(this.defaultLanguage)) != null) {
            message = defaultLang.getRaw(path);
        }
        if (message == null) {
            return "\u00a7c[Missing: " + path + "]";
        }
        message = this.replaceAllPlaceholders(player, message);
        return message;
    }

    @NotNull
    public String getParsed(@NotNull Player player, @NotNull String path) {
        String raw = this.getRaw(player, path);
        if (raw.startsWith("\u00a7c[Missing:")) {
            return raw;
        }
        return raw.replace('&', '\u00a7');
    }

    @NotNull
    public String getParsed(@NotNull Player player, @NotNull String path, Object ... replacements) {
        String raw = this.getRaw(player, path);
        if (raw.startsWith("\u00a7c[Missing:")) {
            return raw;
        }
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            raw = raw.replace("{" + key + "}", value);
        }
        return raw.replace('&', '\u00a7');
    }

    @NotNull
    public Component getComponent(@NotNull Player player, @NotNull String path) {
        String raw = this.getRaw(player, path);
        if (raw.startsWith("\u00a7c[Missing:")) {
            return Component.text((String)raw, (TextColor)NamedTextColor.RED);
        }
        raw = raw.replace('&', '\u00a7');
        return TextUtil.parseLegacy(raw);
    }

    @NotNull
    public Component getComponent(@NotNull Player player, @NotNull String path, Object ... replacements) {
        String raw = this.getRaw(player, path);
        if (raw.startsWith("\u00a7c[Missing:")) {
            return Component.text((String)raw, (TextColor)NamedTextColor.RED);
        }
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            raw = raw.replace("{" + key + "}", value);
        }
        raw = raw.replace('&', '\u00a7');
        return TextUtil.parseLegacy(raw);
    }

    @NotNull
    public List<Component> getComponentList(@NotNull Player player, @NotNull String path) {
        List<String> messages;
        String langCode = this.getPlayerLanguage(player);
        Language language = this.languages.get(langCode);
        if (language != null && (messages = language.getRawList(path)) != null && !messages.isEmpty()) {
            return messages.stream().map(line -> {
                String processed = this.replaceAllPlaceholders(player, (String)line);
                processed = processed.replace('&', '\u00a7');
                return TextUtil.parseLegacy(processed);
            }).toList();
        }
        return List.of(Component.text((String)("Missing message list: " + path), (TextColor)NamedTextColor.RED));
    }

    @NotNull
    public Component get(@NotNull Player player, @NotNull String path) {
        return this.getComponent(player, path);
    }

    @NotNull
    public Component get(@NotNull Player player, @NotNull String path, Object ... replacements) {
        return this.getComponent(player, path, replacements);
    }

    @NotNull
    public List<Component> getList(@NotNull Player player, @NotNull String path) {
        return this.getComponentList(player, path);
    }

    public void send(@NotNull Player player, @NotNull String path) {
        player.sendMessage(this.getComponent(player, path));
    }

    public void send(@NotNull Player player, @NotNull String path, Object ... replacements) {
        player.sendMessage(this.getComponent(player, path, replacements));
    }

    public void sendPrefixed(@NotNull Player player, @NotNull String message) {
        String prefix = this.getPrefix(player);
        String fullMessage = (prefix + message).replace('&', '\u00a7');
        player.sendMessage(TextUtil.parseLegacy(fullMessage));
    }

    public void sendPrefixed(@NotNull Player player, @NotNull String message, Object ... replacements) {
        String prefix = this.getPrefix(player);
        Object fullMessage = prefix + message;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            fullMessage = ((String)fullMessage).replace("{" + key + "}", value);
        }
        fullMessage = ((String)fullMessage).replace('&', '\u00a7');
        player.sendMessage(TextUtil.parseLegacy((String)fullMessage));
    }

    public void sendTitle(@NotNull Player player, @NotNull String titlePath, @Nullable String subtitlePath, int fadeIn, int stay, int fadeOut) {
        Component title = this.getComponent(player, titlePath);
        TextComponent subtitle = subtitlePath != null ? this.getComponent(player, subtitlePath) : Component.empty();
        player.showTitle(Title.title((Component)title, (Component)subtitle, (Title.Times)Title.Times.times((Duration)Duration.ofMillis((long)fadeIn * 50L), (Duration)Duration.ofMillis((long)stay * 50L), (Duration)Duration.ofMillis((long)fadeOut * 50L))));
    }

    public void sendActionBar(@NotNull Player player, @NotNull String path) {
        player.sendActionBar(this.getComponent(player, path));
    }

    @Nullable
    public String getRawMessage(@NotNull Player player, @NotNull String key) {
        String value;
        String langCode = this.getPlayerLanguage(player);
        Language language = this.languages.get(langCode);
        if (language != null && (value = language.getRaw(key)) != null && !value.isEmpty()) {
            return value;
        }
        return this.getRawMessageDefault(key);
    }

    @Nullable
    public String getRawMessageDefault(@NotNull String key) {
        String value;
        Language defaultLang = this.languages.get(this.defaultLanguage);
        if (defaultLang != null && (value = defaultLang.getRaw(key)) != null && !value.isEmpty()) {
            return value;
        }
        return null;
    }

    @NotNull
    public Map<String, Language> getLanguages() {
        return Collections.unmodifiableMap(this.languages);
    }

    @NotNull
    public Set<String> getAvailableLanguageCodes() {
        return Collections.unmodifiableSet(this.languages.keySet());
    }

    public boolean isLanguageAvailable(@NotNull String langCode) {
        return this.languages.containsKey(langCode);
    }

    @NotNull
    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }

    public boolean setDefaultLanguage(@NotNull String langCode) {
        if (!this.languages.containsKey(langCode)) {
            return false;
        }
        this.defaultLanguage = langCode;
        this.plugin.getConfig().set("language.default", (Object)langCode);
        this.plugin.saveConfig();
        return true;
    }

    @NotNull
    public String getLanguageName(@NotNull String langCode) {
        Language lang = this.languages.get(langCode);
        if (lang != null) {
            return lang.getName();
        }
        return switch (langCode) {
            case ENGLISH -> "English";
            case RUSSIAN -> "\u0420\u0443\u0441\u0441\u043a\u0438\u0439";
            case SPANISH -> "Espa\u00f1ol";
            case GERMAN -> "Deutsch";
            case FRENCH -> "Fran\u00e7ais";
            case PORTUGUESE -> "Portugu\u00eas";
            case CHINESE -> "\u4e2d\u6587";
            case JAPANESE -> "\u65e5\u672c\u8a9e";
            case KOREAN -> "\ud55c\uad6d\uc5b4";
            case ARABIC -> "\u0627\u0644\u0639\u0631\u0628\u064a\u0629";
            default -> langCode.toUpperCase();
        };
    }

    public void reload() {
        this.plugin.getLogger().info("Reloading languages...");
        this.defaultLanguage = this.plugin.getConfig().getString("language.default", ENGLISH);
        this.loadAllLanguages();
        this.playerLanguages.clear();
        this.loadPlayerPreferences();
        this.plugin.getLogger().info("Languages reloaded! (" + this.languages.size() + " languages)");
    }

    public void shutdown() {
        this.savePlayerPreferences();
        this.languages.clear();
        this.playerLanguages.clear();
    }

    public static class Language {
        private final String code;
        private final File file;
        public FileConfiguration config;
        private String name;
        private final Map<String, String> cache = new HashMap<String, String>();

        public Language(@NotNull String code, @NotNull File file) {
            this.code = code;
            this.file = file;
        }

        public boolean load() {
            try {
                this.config = YamlConfiguration.loadConfiguration((File)this.file);
                this.name = this.config.getString("language.name", this.code.toUpperCase());
                this.cache.clear();
                this.cacheMessages("", (ConfigurationSection)this.config);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }

        private void cacheMessages(String path, ConfigurationSection section) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection subSection;
                String fullPath = path.isEmpty() ? key : path + "." + key;
                Object value = section.get(key);
                if (value instanceof String) {
                    String stringValue = (String)value;
                    this.cache.put(fullPath, stringValue);
                    continue;
                }
                if (value instanceof List) {
                    List listValue = (List)value;
                    StringBuilder sb = new StringBuilder();
                    for (Object item : listValue) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(String.valueOf(item));
                    }
                    this.cache.put(fullPath, sb.toString());
                    continue;
                }
                if (!section.isConfigurationSection(key) || (subSection = section.getConfigurationSection(key)) == null) continue;
                this.cacheMessages(fullPath, subSection);
            }
        }

        @Nullable
        public String getRaw(@NotNull String path) {
            String cached = this.cache.get(path);
            if (cached != null) {
                return cached;
            }
            return this.config.getString(path);
        }

        @Nullable
        public List<String> getRawList(@NotNull String path) {
            List fromConfig = this.config.getStringList(path);
            if (fromConfig != null && !fromConfig.isEmpty()) {
                return fromConfig;
            }
            String raw = this.cache.get(path);
            if (raw != null) {
                return Arrays.asList(raw.split("\n"));
            }
            return null;
        }

        @Nullable
        public String getMessage(@NotNull String key) {
            String value;
            if (this.cache.containsKey(key)) {
                return this.cache.get(key);
            }
            if (this.config != null && (value = this.config.getString(key)) != null) {
                return value;
            }
            return null;
        }

        @NotNull
        public String getCode() {
            return this.code;
        }

        @NotNull
        public String getName() {
            return this.name;
        }

        @NotNull
        public File getFile() {
            return this.file;
        }

        public int getMessageCount() {
            return this.cache.size();
        }
    }
}

