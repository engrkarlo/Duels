/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.ultimateduels.gui;

import com.ultimateduels.UltimateDuels;
import com.ultimateduels.gui.GUIMessages;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GUIConfigManager {
    private final UltimateDuels plugin;
    private FileConfiguration config;
    private File configFile;

    public GUIConfigManager(@NotNull UltimateDuels plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        try {
            this.configFile = new File(this.plugin.getDataFolder(), "guis.yml");
            if (!this.configFile.exists()) {
                this.plugin.saveResource("guis.yml", false);
            }
            this.config = YamlConfiguration.loadConfiguration((File)this.configFile);
            this.plugin.getLogger().info("=== GUIConfigManager Debug ===");
            this.plugin.getLogger().info("guis.yml loaded. Checking texts sections...");
            String[] sectionsToCheck = new String[]{"ffa-selector.texts", "queue-main.texts", "settings.texts", "stats.texts"};
            boolean allFound = true;
            for (String section : sectionsToCheck) {
                boolean exists = this.config.contains(section);
                this.plugin.getLogger().info("  " + section + ": " + (exists ? "\u2713 FOUND" : "\u2717 MISSING"));
                if (exists) continue;
                allFound = false;
            }
            if (allFound) {
                this.plugin.getLogger().info("All texts sections loaded successfully!");
            } else {
                this.plugin.getLogger().warning("Some texts sections are missing - GUI text overrides may not work!");
            }
            this.plugin.getLogger().info("==============================");
            this.plugin.getLogger().info("GUI configuration loaded successfully!");
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load guis.yml!", e);
            this.config = new YamlConfiguration();
        }
    }

    public void reload() {
        this.loadConfig();
        GUIMessages.reload(this.plugin);
        this.plugin.getLogger().info("GUI configuration reloaded!");
    }

    @NotNull
    public FileConfiguration getConfig() {
        return this.config;
    }

    @Nullable
    public ConfigurationSection getSection(@NotNull String guiSection) {
        return this.config.getConfigurationSection(guiSection);
    }

    @Nullable
    public ConfigurationSection getItemsSection(@NotNull String guiSection) {
        return this.config.getConfigurationSection(guiSection + ".items");
    }

    public int getSize(@NotNull String guiSection, int defaultSize) {
        return this.config.getInt(guiSection + ".size", defaultSize);
    }

    @NotNull
    public String getRawTitle(@NotNull String guiSection, @NotNull String defaultTitle) {
        return this.config.getString(guiSection + ".title", defaultTitle);
    }

    public int getItemSlot(@NotNull String guiSection, @NotNull String itemId, int defaultSlot) {
        return this.config.getInt(guiSection + ".items." + itemId + ".slot", defaultSlot);
    }

    @NotNull
    public Material getItemMaterial(@NotNull String guiSection, @NotNull String itemId, @NotNull Material defaultMaterial) {
        String materialName = this.config.getString(guiSection + ".items." + itemId + ".material");
        if (materialName != null) {
            try {
                return Material.valueOf((String)materialName.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material '" + materialName + "' in guis.yml at " + guiSection + ".items." + itemId + ".material");
            }
        }
        return defaultMaterial;
    }

    @NotNull
    public String getItemMaterialName(@NotNull String guiSection, @NotNull String itemId, @NotNull String defaultMaterial) {
        return this.config.getString(guiSection + ".items." + itemId + ".material", defaultMaterial);
    }

    @NotNull
    public String getItemName(@NotNull String guiSection, @NotNull String itemId, @NotNull String defaultName) {
        return this.config.getString(guiSection + ".items." + itemId + ".name", defaultName);
    }

    @NotNull
    public List<String> getItemLore(@NotNull String guiSection, @NotNull String itemId, @NotNull List<String> defaultLore) {
        List lore = this.config.getStringList(guiSection + ".items." + itemId + ".lore");
        return lore.isEmpty() ? defaultLore : lore;
    }

    @NotNull
    public String getTextValue(@NotNull String guiSection, @NotNull String key, @NotNull String defaultValue) {
        return this.config.getString(guiSection + ".texts." + key, defaultValue);
    }

    public boolean hasText(@NotNull String guiSection, @NotNull String key) {
        return this.config.contains(guiSection + ".texts." + key);
    }

    @NotNull
    public Set<String> getTextKeys(@NotNull String guiSection) {
        ConfigurationSection section = this.config.getConfigurationSection(guiSection + ".texts");
        return section != null ? section.getKeys(false) : Collections.emptySet();
    }
}

