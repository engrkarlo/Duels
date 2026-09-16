/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.ComponentLike
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
 *  net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessagesConfig {
    private final UltimateDuels plugin;
    private final MiniMessage miniMessage;
    private File messagesFile;
    private FileConfiguration messages;
    private final Map<String, String> messageCache = new HashMap<String, String>();

    public MessagesConfig(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
    }

    public void load() {
        this.createFileIfNotExists();
        this.messageCache.clear();
        this.messages = YamlConfiguration.loadConfiguration((File)this.messagesFile);
        this.addMissingKeysOnly();
        this.cacheMessages();
        this.plugin.getLogger().info("Messages loaded successfully! (" + this.messageCache.size() + " entries)");
    }

    private void createFileIfNotExists() {
        if (!this.messagesFile.exists()) {
            try {
                this.messagesFile.getParentFile().mkdirs();
                this.plugin.saveResource("messages.yml", false);
                this.plugin.getLogger().info("Created new messages.yml file");
            }
            catch (Exception e) {
                this.plugin.getLogger().severe("Failed to create messages.yml: " + e.getMessage());
            }
        }
    }

    private void addMissingKeysOnly() {
        InputStream defaultStream = this.plugin.getResource("messages.yml");
        if (defaultStream == null) {
            return;
        }
        try {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            boolean modified = false;
            int added = 0;
            for (String key : defaultMessages.getKeys(true)) {
                if (this.messages.contains(key)) continue;
                Object value = defaultMessages.get(key);
                this.messages.set(key, value);
                modified = true;
                ++added;
                if (this.plugin.getConfigManager() == null || !this.plugin.getConfigManager().isDebugMode()) continue;
                this.plugin.getLogger().info("[MessagesConfig] Added missing key: " + key);
            }
            if (modified) {
                this.save();
                this.plugin.getLogger().info("Added " + added + " missing message keys (user changes preserved)");
            }
        }
        catch (Exception e) {
            this.plugin.getLogger().warning("Failed to check for missing message keys: " + e.getMessage());
        }
    }

    public void reload() {
        this.plugin.getLogger().info("Reloading messages.yml...");
        this.messageCache.clear();
        this.messages = YamlConfiguration.loadConfiguration((File)this.messagesFile);
        this.addMissingKeysOnly();
        this.cacheMessages();
        this.plugin.getLogger().info("Messages reloaded! (" + this.messageCache.size() + " entries cached)");
    }

    public void forceReload() {
        this.plugin.getLogger().info("=== Force Reloading messages.yml ===");
        try {
            this.messageCache.clear();
            this.plugin.getLogger().info("  \u2713 Cache cleared");
            if (!this.messagesFile.exists()) {
                this.plugin.getLogger().severe("  \u2717 messages.yml DOES NOT EXIST!");
                return;
            }
            long lastModified = this.messagesFile.lastModified();
            this.plugin.getLogger().info("  \u2713 File last modified: " + String.valueOf(new Date(lastModified)));
            this.messages = null;
            System.gc();
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.messages = YamlConfiguration.loadConfiguration((File)this.messagesFile);
            this.plugin.getLogger().info("  \u2713 Loaded configuration from disk (user edits preserved)");
            this.addMissingKeysOnly();
            this.plugin.getLogger().info("  \u2713 Missing keys checked");
            this.cacheMessages();
            this.plugin.getLogger().info("  \u2713 Messages cached: " + this.messageCache.size() + " entries");
            if (this.messageCache.isEmpty()) {
                this.plugin.getLogger().severe("  \u2717 WARNING: No messages were cached!");
            } else {
                this.plugin.getLogger().info("  \u2713 Sample messages:");
                int count = 0;
                for (Map.Entry<String, String> entry : this.messageCache.entrySet()) {
                    if (count++ >= 3) continue;
                    Object value = entry.getValue();
                    if (((String)value).length() > 50) {
                        value = ((String)value).substring(0, 50) + "...";
                    }
                    this.plugin.getLogger().info("    - " + entry.getKey() + ": " + (String)value);
                }
            }
            this.plugin.getLogger().info("=== Messages force reload complete! ===");
        }
        catch (Exception e) {
            this.plugin.getLogger().severe("  \u2717 Failed to force reload messages!");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.messages.save(this.messagesFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save messages!", e);
        }
    }

    private void cacheMessages() {
        this.messageCache.clear();
        this.cacheSection("", (ConfigurationSection)this.messages);
    }

    private void cacheSection(@NotNull String path, @NotNull ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            ConfigurationSection subSection;
            String fullPath = path.isEmpty() ? key : path + "." + key;
            Object value = section.get(key);
            if (value instanceof String) {
                String stringValue = (String)value;
                this.messageCache.put(fullPath, stringValue);
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
                this.messageCache.put(fullPath, sb.toString());
                continue;
            }
            if (!section.isConfigurationSection(key) || (subSection = section.getConfigurationSection(key)) == null) continue;
            this.cacheSection(fullPath, subSection);
        }
    }

    private String replacePrefixPlaceholder(String text) {
        if (text != null && text.contains("{prefix}")) {
            String prefix = this.messages.getString("general.prefix", "&8[&6UltimateDuels&8] ");
            return text.replace("{prefix}", prefix);
        }
        return text;
    }

    @NotNull
    public String getRaw(@NotNull String path) {
        String fromConfig = this.messages.getString(path);
        if (fromConfig != null) {
            return fromConfig;
        }
        String cached = this.messageCache.get(path);
        if (cached != null) {
            return cached;
        }
        return "<red>Missing message: " + path + "</red>";
    }

    @NotNull
    public String getRaw(@NotNull String path, @NotNull String defaultValue) {
        String fromConfig = this.messages.getString(path);
        if (fromConfig != null) {
            return fromConfig;
        }
        String cached = this.messageCache.get(path);
        return cached != null ? cached : defaultValue;
    }

    @NotNull
    public List<String> getRawList(@NotNull String path) {
        List fromConfig = this.messages.getStringList(path);
        if (fromConfig != null && !fromConfig.isEmpty()) {
            return fromConfig;
        }
        String raw = this.messageCache.get(path);
        if (raw == null) {
            return List.of("<red>Missing message: " + path + "</red>");
        }
        return Arrays.asList(raw.split("\n"));
    }

    @NotNull
    public Component get(@NotNull String path) {
        String raw = this.getRaw(path);
        raw = this.replacePrefixPlaceholder(raw);
        return this.miniMessage.deserialize((Object)raw);
    }

    @NotNull
    public Component get(@NotNull String path, Object ... replacements) {
        String raw = this.getRaw(path);
        raw = this.replacePrefixPlaceholder(raw);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            raw = raw.replace("{" + key + "}", value);
        }
        return this.miniMessage.deserialize((Object)raw);
    }

    @NotNull
    public Component get(@NotNull String path, TagResolver ... resolvers) {
        String raw = this.getRaw(path);
        raw = this.replacePrefixPlaceholder(raw);
        return this.miniMessage.deserialize(raw, resolvers);
    }

    @NotNull
    public List<Component> getList(@NotNull String path) {
        return this.getRawList(path).stream().map(line -> {
            String processed = this.replacePrefixPlaceholder((String)line);
            return this.miniMessage.deserialize((Object)processed);
        }).toList();
    }

    @NotNull
    public List<Component> getList(@NotNull String path, Object ... replacements) {
        return this.getRawList(path).stream().map(line -> {
            String processed = line;
            processed = this.replacePrefixPlaceholder(processed);
            for (int i = 0; i < replacements.length - 1; i += 2) {
                String key = String.valueOf(replacements[i]);
                String value = String.valueOf(replacements[i + 1]);
                processed = processed.replace("{" + key + "}", value);
            }
            return this.miniMessage.deserialize((Object)processed);
        }).toList();
    }

    @NotNull
    public Component getPrefixed(@NotNull String path) {
        String message = this.getRaw(path);
        if (message.contains("{prefix}")) {
            return this.get(path);
        }
        String prefix = this.getRaw("general.prefix", "&8[&6UltimateDuels&8] ");
        return this.miniMessage.deserialize((Object)(prefix + message));
    }

    @NotNull
    public Component getPrefixed(@NotNull String path, Object ... replacements) {
        String message = this.getRaw(path);
        if (message.contains("{prefix}")) {
            return this.get(path, replacements);
        }
        String prefix = this.getRaw("general.prefix", "&8[&6UltimateDuels&8] ");
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace("{" + key + "}", value);
        }
        return this.miniMessage.deserialize((Object)(prefix + message));
    }

    public void send(@NotNull Player player, @NotNull String path) {
        player.sendMessage(this.getPrefixed(path));
    }

    public void send(@NotNull Player player, @NotNull String path, Object ... replacements) {
        player.sendMessage(this.getPrefixed(path, replacements));
    }

    public void send(@NotNull Collection<? extends Player> players, @NotNull String path, Object ... replacements) {
        Component message = this.getPrefixed(path, replacements);
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public void sendRaw(@NotNull Player player, @NotNull String path) {
        player.sendMessage(this.get(path));
    }

    public void sendRaw(@NotNull Player player, @NotNull String path, Object ... replacements) {
        player.sendMessage(this.get(path, replacements));
    }

    public void sendTitle(@NotNull Player player, @NotNull String titlePath, @Nullable String subtitlePath, int fadeIn, int stay, int fadeOut) {
        Component title = this.get(titlePath);
        TextComponent subtitle = subtitlePath != null ? this.get(subtitlePath) : Component.empty();
        player.showTitle(Title.title((Component)title, (Component)subtitle, (Title.Times)Title.Times.times((Duration)Duration.ofMillis((long)fadeIn * 50L), (Duration)Duration.ofMillis((long)stay * 50L), (Duration)Duration.ofMillis((long)fadeOut * 50L))));
    }

    public void sendTitle(@NotNull Player player, @NotNull String titlePath, @Nullable String subtitlePath, int fadeIn, int stay, int fadeOut, Object ... replacements) {
        Component title = this.get(titlePath, replacements);
        TextComponent subtitle = subtitlePath != null ? this.get(subtitlePath, replacements) : Component.empty();
        player.showTitle(Title.title((Component)title, (Component)subtitle, (Title.Times)Title.Times.times((Duration)Duration.ofMillis((long)fadeIn * 50L), (Duration)Duration.ofMillis((long)stay * 50L), (Duration)Duration.ofMillis((long)fadeOut * 50L))));
    }

    public void sendActionBar(@NotNull Player player, @NotNull String path) {
        player.sendActionBar(this.get(path));
    }

    public void sendActionBar(@NotNull Player player, @NotNull String path, Object ... replacements) {
        player.sendActionBar(this.get(path, replacements));
    }

    public boolean has(@NotNull String path) {
        return this.messages.contains(path) || this.messageCache.containsKey(path);
    }

    @NotNull
    public MiniMessage getMiniMessage() {
        return this.miniMessage;
    }

    @NotNull
    public Component parse(@NotNull String raw) {
        return this.miniMessage.deserialize((Object)raw);
    }

    @NotNull
    public Component parse(@NotNull String raw, Object ... replacements) {
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            raw = raw.replace("{" + key + "}", value);
        }
        return this.miniMessage.deserialize((Object)raw);
    }

    @NotNull
    public TagResolver placeholder(@NotNull String key, @NotNull String value) {
        return Placeholder.parsed((String)key, (String)value);
    }

    @NotNull
    public TagResolver placeholder(@NotNull String key, @NotNull Component value) {
        return Placeholder.component((String)key, (ComponentLike)value);
    }

    public int getMessageCount() {
        return this.messageCache.size();
    }

    public File getMessagesFile() {
        return this.messagesFile;
    }

    public void testMessage(String path) {
        String raw = this.getRaw(path);
        this.plugin.getLogger().info("Test message '" + path + "': " + raw);
    }

    public Map<String, Object> getReloadInfo() {
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("messageCount", this.messageCache.size());
        info.put("fileExists", this.messagesFile.exists());
        info.put("fileSize", this.messagesFile.length());
        info.put("lastModified", this.messagesFile.lastModified());
        return info;
    }

    public String getFromFileDirect(String path) {
        try {
            YamlConfiguration fresh = YamlConfiguration.loadConfiguration((File)this.messagesFile);
            return fresh.getString(path, "<missing: " + path + ">");
        }
        catch (Exception e) {
            return "<error loading: " + e.getMessage() + ">";
        }
    }

    public String reloadWithStats() {
        this.reload();
        return "Messages reloaded! (" + this.messageCache.size() + " entries loaded)";
    }
}

