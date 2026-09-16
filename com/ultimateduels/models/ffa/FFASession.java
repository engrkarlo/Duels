/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.models.ffa;

import com.ultimateduels.models.ffa.FFAArena;
import com.ultimateduels.models.player.DuelPlayer;
import com.ultimateduels.models.player.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FFASession {
    private final DuelPlayer player;
    private final UUID playerId;
    private final String playerName;
    private final FFAArena arena;
    private final String arenaId;
    private int kills;
    private int deaths;
    private int killStreak;
    private int bestKillStreak;
    private int assists;
    private double damageDealt;
    private double damageTaken;
    private int arrowsShot;
    private int arrowsHit;
    private final long joinedAt;
    private long lastSpawnTime;
    private long lastKillTime;
    private long lastDeathTime;
    private long lastDamageTime;
    private UUID lastDamagerId;
    private boolean alive;
    private boolean spawnProtected;

    public FFASession(DuelPlayer player, FFAArena arena) {
        this.player = player;
        this.playerId = player.getUuid();
        this.playerName = player.getName();
        this.arena = arena;
        this.arenaId = arena.getArenaId();
        this.joinedAt = System.currentTimeMillis();
        this.alive = true;
        this.spawnProtected = false;
        player.setFfaSession(this);
        player.setState(PlayerState.IN_FFA);
    }

    public void addKill() {
        ++this.kills;
        ++this.killStreak;
        this.lastKillTime = System.currentTimeMillis();
        if (this.killStreak > this.bestKillStreak) {
            this.bestKillStreak = this.killStreak;
        }
        if (this.player.getStats() != null) {
            this.player.getStats().addFfaKill();
        }
    }

    public void addDeath() {
        ++this.deaths;
        this.killStreak = 0;
        this.lastDeathTime = System.currentTimeMillis();
        this.alive = false;
        this.spawnProtected = false;
        if (this.player.getStats() != null) {
            this.player.getStats().addFfaDeath();
        }
    }

    public void addAssist() {
        ++this.assists;
    }

    public void recordSpawn() {
        this.lastSpawnTime = System.currentTimeMillis();
        this.alive = true;
        this.spawnProtected = true;
    }

    public void endSpawnProtection() {
        this.spawnProtected = false;
    }

    public boolean hasSpawnProtection(int protectionSeconds) {
        if (!this.spawnProtected || this.lastSpawnTime == 0L) {
            return false;
        }
        long elapsed = (System.currentTimeMillis() - this.lastSpawnTime) / 1000L;
        if (elapsed >= (long)protectionSeconds) {
            this.spawnProtected = false;
            return false;
        }
        return true;
    }

    public int getRemainingProtection(int protectionSeconds) {
        if (!this.spawnProtected || this.lastSpawnTime == 0L) {
            return 0;
        }
        long elapsed = (System.currentTimeMillis() - this.lastSpawnTime) / 1000L;
        return Math.max(0, protectionSeconds - (int)elapsed);
    }

    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
    }

    public void addDamageTaken(double damage, UUID dealerId) {
        this.damageTaken += damage;
        this.lastDamageTime = System.currentTimeMillis();
        this.lastDamagerId = dealerId;
    }

    public void addArrowShot() {
        ++this.arrowsShot;
    }

    public void addArrowHit() {
        ++this.arrowsHit;
    }

    public UUID getLastDamager(long timeoutMs) {
        if (this.lastDamagerId == null || this.lastDamageTime == 0L) {
            return null;
        }
        if (System.currentTimeMillis() - this.lastDamageTime > timeoutMs) {
            return null;
        }
        return this.lastDamagerId;
    }

    public double getKDRatio() {
        if (this.deaths == 0) {
            return this.kills;
        }
        return (double)Math.round((double)this.kills / (double)this.deaths * 100.0) / 100.0;
    }

    public double getArrowAccuracy() {
        if (this.arrowsShot == 0) {
            return 0.0;
        }
        return (double)Math.round((double)this.arrowsHit / (double)this.arrowsShot * 10000.0) / 100.0;
    }

    public double getAverageDamagePerKill() {
        if (this.kills == 0) {
            return 0.0;
        }
        return (double)Math.round(this.damageDealt / (double)this.kills * 100.0) / 100.0;
    }

    public double getKillsPerMinute() {
        long minutes = this.getSessionDuration() / 60000L;
        if (minutes == 0L) {
            return this.kills;
        }
        return (double)Math.round((double)this.kills / (double)minutes * 100.0) / 100.0;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - this.joinedAt;
    }

    public int getSessionDurationSeconds() {
        return (int)(this.getSessionDuration() / 1000L);
    }

    public String getFormattedDuration() {
        int seconds = this.getSessionDurationSeconds();
        int minutes = seconds / 60;
        return String.format("%d:%02d", minutes, seconds %= 60);
    }

    public long getTimeSinceLastKill() {
        if (this.lastKillTime == 0L) {
            return -1L;
        }
        return System.currentTimeMillis() - this.lastKillTime;
    }

    public long getTimeSinceLastDeath() {
        if (this.lastDeathTime == 0L) {
            return -1L;
        }
        return System.currentTimeMillis() - this.lastDeathTime;
    }

    public DuelPlayer getPlayer() {
        return this.player;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public FFAArena getArena() {
        return this.arena;
    }

    public String getArenaId() {
        return this.arenaId;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getKillStreak() {
        return this.killStreak;
    }

    public int getBestKillStreak() {
        return this.bestKillStreak;
    }

    public int getAssists() {
        return this.assists;
    }

    public double getDamageDealt() {
        return this.damageDealt;
    }

    public double getDamageTaken() {
        return this.damageTaken;
    }

    public int getArrowsShot() {
        return this.arrowsShot;
    }

    public int getArrowsHit() {
        return this.arrowsHit;
    }

    public long getJoinedAt() {
        return this.joinedAt;
    }

    public long getLastSpawnTime() {
        return this.lastSpawnTime;
    }

    public long getLastKillTime() {
        return this.lastKillTime;
    }

    public long getLastDeathTime() {
        return this.lastDeathTime;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public boolean isSpawnProtected() {
        return this.spawnProtected;
    }

    public void end() {
        if (this.player.getStats() != null) {
            this.player.getStats().addPlaytime(this.getSessionDuration());
        }
        this.player.setFfaSession(null);
        this.player.setState(PlayerState.IN_LOBBY);
    }

    public List<String> getSummary() {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        lines.add("\u00a7e\u00a7lFFA SESSION SUMMARY");
        lines.add("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        lines.add("");
        lines.add("\u00a77Arena: \u00a7b" + this.arena.getDisplayName());
        lines.add("\u00a77Kit: \u00a7d" + this.arena.getKit().getDisplayName());
        lines.add("\u00a77Duration: \u00a7f" + this.getFormattedDuration());
        lines.add("");
        lines.add("\u00a76Statistics:");
        lines.add("  \u00a77Kills: \u00a7a" + this.kills);
        lines.add("  \u00a77Deaths: \u00a7c" + this.deaths);
        lines.add("  \u00a77K/D: \u00a7e" + this.getKDRatio());
        lines.add("  \u00a77Best Streak: \u00a76" + this.bestKillStreak);
        if (this.arrowsShot > 0) {
            lines.add("  \u00a77Arrow Accuracy: \u00a7b" + this.getArrowAccuracy() + "%");
        }
        lines.add("");
        lines.add("\u00a76\u00a7l\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac\u25ac");
        return lines;
    }

    public String getStatusLine() {
        return String.format("\u00a7a%d kills \u00a77| \u00a7c%d deaths \u00a77| \u00a76%d streak", this.kills, this.deaths, this.killStreak);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FFASession session = (FFASession)o;
        return Objects.equals(this.playerId, session.playerId) && Objects.equals(this.arenaId, session.arenaId);
    }

    public int hashCode() {
        return Objects.hash(this.playerId, this.arenaId);
    }

    public String toString() {
        return "FFASession{player=" + this.playerName + ", arena=" + this.arenaId + ", kills=" + this.kills + ", deaths=" + this.deaths + ", streak=" + this.killStreak + ", duration=" + this.getFormattedDuration() + "}";
    }
}

