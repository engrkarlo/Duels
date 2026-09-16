/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 */
package com.ultimateduels.visuals;

import com.ultimateduels.visuals.HealthDisplayType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class HealthDisplayConfig {
    private HealthDisplayType displayType = HealthDisplayType.TEXT_DISPLAY;
    private boolean enabled = true;
    private boolean showInDuels = true;
    private boolean showInFFA = true;
    private boolean showToSpectators = true;
    private String format = "\u2764 {current}/{max}";
    private String heartSymbol = "\u2764";
    private boolean showDecimal = false;
    private int decimalPlaces = 1;
    private TextColor highHealthColor = NamedTextColor.GREEN;
    private TextColor mediumHealthColor = NamedTextColor.YELLOW;
    private TextColor lowHealthColor = NamedTextColor.RED;
    private TextColor criticalHealthColor = TextColor.color((int)0x8B0000);
    private double heightOffset = 2.3;
    private double viewRange = 32.0;
    private float scale = 1.0f;
    private float backgroundOpacity = 0.0f;
    private int updateIntervalTicks = 1;
    private boolean smoothInterpolation = true;
    private int interpolationDuration = 3;
    private boolean pulseOnLowHealth = true;
    private boolean fadeOnFullHealth = false;
    private int fadeDelayTicks = 60;

    public static Builder builder() {
        return new Builder();
    }

    public HealthDisplayType getDisplayType() {
        return this.displayType;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isShowInDuels() {
        return this.showInDuels;
    }

    public boolean isShowInFFA() {
        return this.showInFFA;
    }

    public boolean isShowToSpectators() {
        return this.showToSpectators;
    }

    public String getFormat() {
        return this.format;
    }

    public String getHeartSymbol() {
        return this.heartSymbol;
    }

    public boolean isShowDecimal() {
        return this.showDecimal;
    }

    public int getDecimalPlaces() {
        return this.decimalPlaces;
    }

    public TextColor getHighHealthColor() {
        return this.highHealthColor;
    }

    public TextColor getMediumHealthColor() {
        return this.mediumHealthColor;
    }

    public TextColor getLowHealthColor() {
        return this.lowHealthColor;
    }

    public TextColor getCriticalHealthColor() {
        return this.criticalHealthColor;
    }

    public double getHeightOffset() {
        return this.heightOffset;
    }

    public double getViewRange() {
        return this.viewRange;
    }

    public float getScale() {
        return this.scale;
    }

    public float getBackgroundOpacity() {
        return this.backgroundOpacity;
    }

    public int getUpdateIntervalTicks() {
        return this.updateIntervalTicks;
    }

    public boolean isSmoothInterpolation() {
        return this.smoothInterpolation;
    }

    public int getInterpolationDuration() {
        return this.interpolationDuration;
    }

    public boolean isPulseOnLowHealth() {
        return this.pulseOnLowHealth;
    }

    public boolean isFadeOnFullHealth() {
        return this.fadeOnFullHealth;
    }

    public int getFadeDelayTicks() {
        return this.fadeDelayTicks;
    }

    public TextColor getColorForHealth(double currentHealth, double maxHealth) {
        double percentage = currentHealth / maxHealth * 100.0;
        if (percentage <= 10.0) {
            return this.criticalHealthColor;
        }
        if (percentage <= 33.0) {
            return this.lowHealthColor;
        }
        if (percentage <= 66.0) {
            return this.mediumHealthColor;
        }
        return this.highHealthColor;
    }

    public String formatHealth(double currentHealth, double maxHealth) {
        String max;
        String current;
        if (this.showDecimal) {
            String formatStr = "%." + this.decimalPlaces + "f";
            current = String.format(formatStr, currentHealth);
            max = String.format(formatStr, maxHealth);
        } else {
            current = String.valueOf((int)Math.ceil(currentHealth));
            max = String.valueOf((int)maxHealth);
        }
        return this.format.replace("{current}", current).replace("{max}", max).replace("{heart}", this.heartSymbol).replace("{percent}", String.valueOf((int)(currentHealth / maxHealth * 100.0)));
    }

    public static class Builder {
        private final HealthDisplayConfig config = new HealthDisplayConfig();

        public Builder displayType(HealthDisplayType type) {
            this.config.displayType = type;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.config.enabled = enabled;
            return this;
        }

        public Builder showInDuels(boolean show) {
            this.config.showInDuels = show;
            return this;
        }

        public Builder showInFFA(boolean show) {
            this.config.showInFFA = show;
            return this;
        }

        public Builder showToSpectators(boolean show) {
            this.config.showToSpectators = show;
            return this;
        }

        public Builder format(String format) {
            this.config.format = format;
            return this;
        }

        public Builder heartSymbol(String symbol) {
            this.config.heartSymbol = symbol;
            return this;
        }

        public Builder showDecimal(boolean show) {
            this.config.showDecimal = show;
            return this;
        }

        public Builder decimalPlaces(int places) {
            this.config.decimalPlaces = places;
            return this;
        }

        public Builder heightOffset(double offset) {
            this.config.heightOffset = offset;
            return this;
        }

        public Builder viewRange(double range) {
            this.config.viewRange = range;
            return this;
        }

        public Builder scale(float scale) {
            this.config.scale = scale;
            return this;
        }

        public Builder backgroundOpacity(float opacity) {
            this.config.backgroundOpacity = opacity;
            return this;
        }

        public Builder colors(TextColor high, TextColor medium, TextColor low, TextColor critical) {
            this.config.highHealthColor = high;
            this.config.mediumHealthColor = medium;
            this.config.lowHealthColor = low;
            this.config.criticalHealthColor = critical;
            return this;
        }

        public Builder smoothInterpolation(boolean smooth) {
            this.config.smoothInterpolation = smooth;
            return this;
        }

        public Builder interpolationDuration(int ticks) {
            this.config.interpolationDuration = ticks;
            return this;
        }

        public Builder updateIntervalTicks(int ticks) {
            this.config.updateIntervalTicks = ticks;
            return this;
        }

        public Builder pulseOnLowHealth(boolean pulse) {
            this.config.pulseOnLowHealth = pulse;
            return this;
        }

        public Builder fadeOnFullHealth(boolean fade) {
            this.config.fadeOnFullHealth = fade;
            return this;
        }

        public Builder fadeDelayTicks(int ticks) {
            this.config.fadeDelayTicks = ticks;
            return this;
        }

        public HealthDisplayConfig build() {
            return this.config;
        }
    }
}

