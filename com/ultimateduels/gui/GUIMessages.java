/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.gui;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.config.LanguageManager;
import com.ultimateduels.utils.TextUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GUIMessages {
    private static UltimateDuels plugin;
    private static FileConfiguration guisConfig;

    private GUIMessages() {
    }

    public static void init(@NotNull UltimateDuels pluginInstance) {
        plugin = pluginInstance;
        GUIMessages.loadGuisConfig();
    }

    public static void reload(@NotNull UltimateDuels pluginInstance) {
        plugin = pluginInstance;
        GUIMessages.loadGuisConfig();
    }

    private static void loadGuisConfig() {
        try {
            File guisFile = new File(plugin.getDataFolder(), "guis.yml");
            if (!guisFile.exists()) {
                plugin.saveResource("guis.yml", false);
            }
            guisConfig = YamlConfiguration.loadConfiguration((File)guisFile);
            plugin.getLogger().info("=== GUIMessages Debug ===");
            plugin.getLogger().info("guisConfig loaded: " + (guisConfig != null));
            if (guisConfig != null) {
                boolean hasFFATexts = guisConfig.contains("ffa-selector.texts");
                plugin.getLogger().info("ffa-selector.texts exists: " + hasFFATexts);
                if (hasFFATexts) {
                    ConfigurationSection textsSection = guisConfig.getConfigurationSection("ffa-selector.texts");
                    if (textsSection != null) {
                        plugin.getLogger().info("ffa-selector.texts keys: " + String.valueOf(textsSection.getKeys(false)));
                        plugin.getLogger().info("Sample value (title): " + guisConfig.getString("ffa-selector.texts.title"));
                    }
                } else {
                    plugin.getLogger().warning("ffa-selector.texts section NOT FOUND!");
                    plugin.getLogger().warning("Available top-level keys: " + String.valueOf(guisConfig.getKeys(false)));
                }
            }
            plugin.getLogger().info("=========================");
        }
        catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load guis.yml", e);
            guisConfig = new YamlConfiguration();
        }
    }

    @NotNull
    public static String getText(@Nullable Player player, @NotNull String guiSection, @NotNull String key, @NotNull String fallback) {
        String guiPath;
        String guiValue;
        String defaultValue;
        String langValue;
        LanguageManager langManager;
        if (plugin == null) {
            return fallback;
        }
        String langKey = "gui." + guiSection + "." + key;
        if (player != null && (langManager = plugin.getLanguageManager()) != null && (langValue = langManager.getRawMessage(player, langKey)) != null && !langValue.isEmpty() && !langValue.equals(langKey)) {
            return langValue;
        }
        if (plugin.getLanguageManager() != null && (defaultValue = plugin.getLanguageManager().getRawMessageDefault(langKey)) != null && !defaultValue.isEmpty() && !defaultValue.equals(langKey)) {
            return defaultValue;
        }
        if (guisConfig != null && (guiValue = guisConfig.getString(guiPath = guiSection + ".texts." + key)) != null && !guiValue.isEmpty()) {
            return guiValue;
        }
        return fallback;
    }

    @NotNull
    public static String getText(@NotNull String guiSection, @NotNull String key, @NotNull String fallback) {
        return GUIMessages.getText(null, guiSection, key, fallback);
    }

    @NotNull
    public static Component getComponent(@Nullable Player player, @NotNull String guiSection, @NotNull String key, @NotNull String fallback) {
        return TextUtil.parse(GUIMessages.getText(player, guiSection, key, fallback));
    }

    @NotNull
    public static String getText(@Nullable Player player, @NotNull String guiSection, @NotNull String key, @NotNull String fallback, String ... replacements) {
        String text = GUIMessages.getText(player, guiSection, key, fallback);
        return GUIMessages.applyReplacements(text, replacements);
    }

    @NotNull
    public static Component getComponent(@Nullable Player player, @NotNull String guiSection, @NotNull String key, @NotNull String fallback, String ... replacements) {
        return TextUtil.parse(GUIMessages.getText(player, guiSection, key, fallback, replacements));
    }

    @NotNull
    public static String getTitle(@Nullable Player player, @NotNull String guiSection, @NotNull String fallback) {
        String defaultValue;
        String langValue;
        if (plugin == null) {
            return fallback;
        }
        String langKey = "gui." + guiSection + ".title";
        if (player != null && plugin.getLanguageManager() != null && (langValue = plugin.getLanguageManager().getRawMessage(player, langKey)) != null && !langValue.isEmpty() && !langValue.equals(langKey)) {
            return langValue;
        }
        if (plugin.getLanguageManager() != null && (defaultValue = plugin.getLanguageManager().getRawMessageDefault(langKey)) != null && !defaultValue.isEmpty() && !defaultValue.equals(langKey)) {
            return defaultValue;
        }
        if (guisConfig != null) {
            String textsPath = guiSection + ".texts.title";
            String textsValue = guisConfig.getString(textsPath);
            if (textsValue != null && !textsValue.isEmpty()) {
                return textsValue;
            }
            String directPath = guiSection + ".title";
            String directValue = guisConfig.getString(directPath);
            if (directValue != null && !directValue.isEmpty()) {
                return directValue;
            }
        }
        return fallback;
    }

    @NotNull
    public static String getTitle(@Nullable Player player, @NotNull String guiSection, @NotNull String fallback, String ... replacements) {
        String title = GUIMessages.getTitle(player, guiSection, fallback);
        return GUIMessages.applyReplacements(title, replacements);
    }

    @NotNull
    public static Component getTitleComponent(@Nullable Player player, @NotNull String guiSection, @NotNull String fallback) {
        return TextUtil.parse(GUIMessages.getTitle(player, guiSection, fallback));
    }

    @NotNull
    public static Component getTitleComponent(@Nullable Player player, @NotNull String guiSection, @NotNull String fallback, String ... replacements) {
        return TextUtil.parse(GUIMessages.getTitle(player, guiSection, fallback, replacements));
    }

    @NotNull
    public static String getItemName(@Nullable Player player, @NotNull String guiSection, @NotNull String itemId, @NotNull String fallback) {
        String guiPath;
        String guiValue;
        String defaultValue;
        String langValue;
        String langKey = "gui." + guiSection + "." + itemId + "-name";
        if (player != null && plugin.getLanguageManager() != null && (langValue = plugin.getLanguageManager().getRawMessage(player, langKey)) != null && !langValue.isEmpty() && !langValue.equals(langKey)) {
            return langValue;
        }
        if (plugin.getLanguageManager() != null && (defaultValue = plugin.getLanguageManager().getRawMessageDefault(langKey)) != null && !defaultValue.isEmpty() && !defaultValue.equals(langKey)) {
            return defaultValue;
        }
        if (guisConfig != null && (guiValue = guisConfig.getString(guiPath = guiSection + ".items." + itemId + ".name")) != null && !guiValue.isEmpty()) {
            return guiValue;
        }
        return fallback;
    }

    @NotNull
    public static Component getItemNameComponent(@Nullable Player player, @NotNull String guiSection, @NotNull String itemId, @NotNull String fallback) {
        return TextUtil.parse(GUIMessages.getItemName(player, guiSection, itemId, fallback));
    }

    @NotNull
    public static String getItemName(@Nullable Player player, @NotNull String guiSection, @NotNull String itemId, @NotNull String fallback, String ... replacements) {
        return GUIMessages.applyReplacements(GUIMessages.getItemName(player, guiSection, itemId, fallback), replacements);
    }

    @NotNull
    public static List<String> getItemLore(@Nullable Player player, @NotNull String guiSection, @NotNull String itemId, @NotNull List<String> fallback) {
        String guiPath;
        List guiLore;
        if (guisConfig != null && !(guiLore = guisConfig.getStringList(guiPath = guiSection + ".items." + itemId + ".lore")).isEmpty()) {
            return guiLore;
        }
        return fallback;
    }

    @NotNull
    public static List<Component> getItemLoreComponents(@Nullable Player player, @NotNull String guiSection, @NotNull String itemId, @NotNull List<String> fallback) {
        List<String> lore = GUIMessages.getItemLore(player, guiSection, itemId, fallback);
        ArrayList<Component> components = new ArrayList<Component>();
        for (String line : lore) {
            components.add(TextUtil.parse(line));
        }
        return components;
    }

    @NotNull
    public static List<String> getItemLore(@Nullable Player player, @NotNull String guiSection, @NotNull String itemId, @NotNull List<String> fallback, String ... replacements) {
        List<String> lore = GUIMessages.getItemLore(player, guiSection, itemId, fallback);
        ArrayList<String> result = new ArrayList<String>();
        for (String line : lore) {
            result.add(GUIMessages.applyReplacements(line, replacements));
        }
        return result;
    }

    @NotNull
    public static String getItemMaterial(@NotNull String guiSection, @NotNull String itemId, @NotNull String fallback) {
        String guiPath;
        String material;
        if (guisConfig != null && (material = guisConfig.getString(guiPath = guiSection + ".items." + itemId + ".material")) != null && !material.isEmpty()) {
            return material;
        }
        return fallback;
    }

    public static int getItemSlot(@NotNull String guiSection, @NotNull String itemId, int fallback) {
        String guiPath;
        if (guisConfig != null && guisConfig.contains(guiPath = guiSection + ".items." + itemId + ".slot")) {
            return guisConfig.getInt(guiPath, fallback);
        }
        return fallback;
    }

    public static int getSize(@NotNull String guiSection, int fallback) {
        String path;
        if (guisConfig != null && guisConfig.contains(path = guiSection + ".size")) {
            return guisConfig.getInt(path, fallback);
        }
        return fallback;
    }

    @NotNull
    private static String applyReplacements(@NotNull String text, String ... replacements) {
        if (replacements.length % 2 != 0) {
            return text;
        }
        String result = text;
        for (int i = 0; i < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return result;
    }

    @NotNull
    public static List<String> applyReplacements(@NotNull List<String> lines, String ... replacements) {
        ArrayList<String> result = new ArrayList<String>();
        for (String line : lines) {
            result.add(GUIMessages.applyReplacements(line, replacements));
        }
        return result;
    }

    @NotNull
    public static List<Component> parseLines(@NotNull List<String> lines) {
        ArrayList<Component> components = new ArrayList<Component>();
        for (String line : lines) {
            components.add(TextUtil.parse(line));
        }
        return components;
    }

    @Nullable
    public static FileConfiguration getGuisConfig() {
        return guisConfig;
    }
}

