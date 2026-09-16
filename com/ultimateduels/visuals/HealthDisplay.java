/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.TextDisplay
 */
package com.ultimateduels.visuals;

import com.ultimateduels.visuals.HealthDisplayConfig;
import com.ultimateduels.visuals.HealthPacketSender;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

public class HealthDisplay {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(-1000000);
    private final UUID targetPlayerId;
    private final Player targetPlayer;
    private final HealthDisplayConfig config;
    private final int entityId;
    private final UUID entityUUID;
    private final Set<UUID> viewers = new HashSet<UUID>();
    private boolean active = false;
    private double lastHealth;
    private double lastMaxHealth;
    private long lastDamageTime;
    private long creationTime;
    private boolean isPulsing = false;
    private int pulsePhase = 0;
    private String cachedText = "";
    private Component cachedComponent = null;
    private double lastX;
    private double lastY;
    private double lastZ;
    private boolean needsRespawn = true;

    public HealthDisplay(Player targetPlayer, HealthDisplayConfig config) {
        this.targetPlayerId = targetPlayer.getUniqueId();
        this.targetPlayer = targetPlayer;
        this.config = config;
        this.lastHealth = targetPlayer.getHealth();
        this.lastMaxHealth = targetPlayer.getMaxHealth();
        this.creationTime = System.currentTimeMillis();
        this.entityId = ID_COUNTER.decrementAndGet();
        this.entityUUID = UUID.randomUUID();
    }

    public void spawn() {
        if (this.active) {
            return;
        }
        if (!this.targetPlayer.isOnline()) {
            return;
        }
        this.cachedText = this.buildText(this.lastHealth, this.lastMaxHealth);
        this.cachedComponent = this.buildComponent();
        this.needsRespawn = true;
        this.active = true;
    }

    public void remove() {
        if (!this.active) {
            return;
        }
        this.active = false;
        for (UUID id : new HashSet<UUID>(this.viewers)) {
            Player viewer = Bukkit.getPlayer((UUID)id);
            if (viewer == null || !viewer.isOnline()) continue;
            HealthPacketSender.sendDestroy(viewer, this.entityId);
        }
        this.viewers.clear();
        this.isPulsing = false;
        this.pulsePhase = 0;
        this.cachedText = "";
        this.cachedComponent = null;
    }

    public void updateHealth(double current, double max) {
        boolean shouldPulse;
        if (!this.active) {
            return;
        }
        boolean changed = Double.compare(current, this.lastHealth) != 0 || Double.compare(max, this.lastMaxHealth) != 0;
        this.lastHealth = current;
        this.lastMaxHealth = max;
        this.lastDamageTime = System.currentTimeMillis();
        double pct = max > 0.0 ? current / max : 0.0;
        boolean bl = shouldPulse = this.config.isPulseOnLowHealth() && pct <= 0.1;
        if (changed || shouldPulse != this.isPulsing) {
            this.isPulsing = shouldPulse;
            this.cachedText = this.buildText(current, max);
            this.cachedComponent = this.buildComponent();
            this.needsRespawn = true;
        }
    }

    public void updatePosition() {
        boolean moved;
        if (!this.active) {
            return;
        }
        if (!this.targetPlayer.isOnline()) {
            this.remove();
            return;
        }
        if (this.isPulsing) {
            boolean isBold;
            int prev = this.pulsePhase;
            this.pulsePhase = (this.pulsePhase + 1) % 40;
            boolean wasBold = prev % 20 < 10;
            boolean bl = isBold = this.pulsePhase % 20 < 10;
            if (wasBold != isBold) {
                this.cachedComponent = this.buildComponent();
                this.needsRespawn = true;
            }
        } else if (this.pulsePhase != 0) {
            this.pulsePhase = 0;
        }
        if (this.viewers.isEmpty()) {
            return;
        }
        Location loc = this.getDisplayLocation();
        double curX = loc.getX();
        double curY = loc.getY();
        double curZ = loc.getZ();
        boolean bl = moved = Math.abs(curX - this.lastX) > 0.001 || Math.abs(curY - this.lastY) > 0.001 || Math.abs(curZ - this.lastZ) > 0.001;
        if (!moved && !this.needsRespawn) {
            return;
        }
        this.lastX = curX;
        this.lastY = curY;
        this.lastZ = curZ;
        this.needsRespawn = false;
        for (UUID viewerId : this.viewers) {
            Player viewer = Bukkit.getPlayer((UUID)viewerId);
            if (viewer == null || !viewer.isOnline()) continue;
            HealthPacketSender.sendDestroy(viewer, this.entityId);
            HealthPacketSender.sendSpawn(viewer, this.entityId, this.entityUUID, loc, this.cachedComponent, (float)this.config.getViewRange(), this.config.getScale());
        }
    }

    public void addViewer(Player viewer) {
        if (viewer.getUniqueId().equals(this.targetPlayerId)) {
            return;
        }
        if (!this.viewers.add(viewer.getUniqueId())) {
            return;
        }
        if (!this.active || !viewer.isOnline()) {
            return;
        }
        if (this.cachedComponent == null) {
            this.cachedText = this.buildText(this.lastHealth, this.lastMaxHealth);
            this.cachedComponent = this.buildComponent();
        }
        Location loc = this.getDisplayLocation();
        this.lastX = loc.getX();
        this.lastY = loc.getY();
        this.lastZ = loc.getZ();
        HealthPacketSender.sendSpawn(viewer, this.entityId, this.entityUUID, loc, this.cachedComponent, (float)this.config.getViewRange(), this.config.getScale());
    }

    public void removeViewer(Player viewer) {
        if (!this.viewers.remove(viewer.getUniqueId())) {
            return;
        }
        if (viewer.isOnline()) {
            HealthPacketSender.sendDestroy(viewer, this.entityId);
        }
    }

    public void updateViewers(Set<Player> newViewers) {
        HashSet<UUID> newIds = new HashSet<UUID>();
        for (Player p : newViewers) {
            if (p.getUniqueId().equals(this.targetPlayerId)) continue;
            newIds.add(p.getUniqueId());
        }
        this.viewers.removeIf(id -> {
            if (!newIds.contains(id)) {
                Player p = Bukkit.getPlayer((UUID)id);
                if (p != null && p.isOnline()) {
                    HealthPacketSender.sendDestroy(p, this.entityId);
                }
                return true;
            }
            return false;
        });
        for (Player viewer : newViewers) {
            this.addViewer(viewer);
        }
    }

    private Component buildComponent() {
        String text = this.cachedText.isEmpty() ? this.buildText(this.lastHealth, this.lastMaxHealth) : this.cachedText;
        TextColor color = this.config.getColorForHealth(this.lastHealth, this.lastMaxHealth);
        Component c = ((TextComponent)((TextComponent)Component.text((String)text).color(color)).decoration(TextDecoration.BOLD, false)).decoration(TextDecoration.ITALIC, false);
        if (this.isPulsing && this.pulsePhase % 20 < 10) {
            c = c.decoration(TextDecoration.BOLD, true);
        }
        return c;
    }

    private String buildText(double current, double max) {
        String maxStr;
        String currentStr;
        current = Math.max(0.0, current);
        max = Math.max(1.0, max);
        if (this.config.isShowDecimal()) {
            String fmt = "%." + this.config.getDecimalPlaces() + "f";
            currentStr = String.format(fmt, current);
            maxStr = String.format(fmt, max);
        } else {
            currentStr = String.valueOf((int)Math.ceil(current));
            maxStr = String.valueOf((int)Math.ceil(max));
        }
        String pctStr = String.format("%.0f", current / max * 100.0);
        return this.config.getFormat().replace("{current}", currentStr).replace("{max}", maxStr).replace("{percent}", pctStr);
    }

    private Location getDisplayLocation() {
        return this.targetPlayer.getLocation().clone().add(0.0, this.config.getHeightOffset(), 0.0);
    }

    public boolean shouldFade() {
        long fadeDelayMs;
        if (!this.config.isFadeOnFullHealth()) {
            return false;
        }
        if (this.lastHealth < this.lastMaxHealth) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - this.creationTime;
        return elapsed > (fadeDelayMs = (long)this.config.getFadeDelayTicks() * 50L);
    }

    public boolean isEntityValid() {
        return this.active;
    }

    public boolean isActive() {
        return this.active;
    }

    public UUID getTargetPlayerId() {
        return this.targetPlayerId;
    }

    public Player getTargetPlayer() {
        return this.targetPlayer;
    }

    public TextDisplay getTextDisplay() {
        return null;
    }

    public Set<UUID> getViewers() {
        return new HashSet<UUID>(this.viewers);
    }

    public double getLastHealth() {
        return this.lastHealth;
    }

    public double getLastMaxHealth() {
        return this.lastMaxHealth;
    }

    public long getLastDamageTime() {
        return this.lastDamageTime;
    }

    public int getEntityId() {
        return this.entityId;
    }
}

