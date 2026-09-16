/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.format.TextColor
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 */
package com.ultimateduels.visuals;

import com.ultimateduels.visuals.HealthDisplayConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardHealthDisplay {
    private final HealthDisplayConfig config;
    private final Map<UUID, Team> playerTeams;
    private Scoreboard healthScoreboard;
    private int teamCounter;

    public ScoreboardHealthDisplay(HealthDisplayConfig config) {
        this.config = config;
        this.playerTeams = new HashMap<UUID, Team>();
        this.teamCounter = 0;
    }

    public void initialize() {
        this.healthScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void createDisplay(Player target, Set<Player> viewers) {
        Object teamName;
        if (this.playerTeams.containsKey(target.getUniqueId())) {
            this.removeDisplay(target);
        }
        if (((String)(teamName = "health_" + this.teamCounter++)).length() > 16) {
            teamName = ((String)teamName).substring(0, 16);
        }
        Team team = this.healthScoreboard.registerNewTeam((String)teamName);
        team.addEntry(target.getName());
        this.updateHealthDisplay(target, target.getHealth(), target.getMaxHealth());
        this.playerTeams.put(target.getUniqueId(), team);
        for (Player viewer : viewers) {
            if (viewer.getUniqueId().equals(target.getUniqueId())) continue;
            viewer.setScoreboard(this.healthScoreboard);
        }
    }

    public void updateHealthDisplay(Player target, double currentHealth, double maxHealth) {
        Team team = this.playerTeams.get(target.getUniqueId());
        if (team == null) {
            return;
        }
        String healthText = this.config.formatHealth(currentHealth, maxHealth);
        TextColor color = this.config.getColorForHealth(currentHealth, maxHealth);
        Component suffix = Component.text((String)(" " + healthText)).color(color);
        team.suffix(suffix);
    }

    public void removeDisplay(Player target) {
        Team team = this.playerTeams.remove(target.getUniqueId());
        if (team != null) {
            team.unregister();
        }
    }

    public void removeAll() {
        for (Team team : this.playerTeams.values()) {
            team.unregister();
        }
        this.playerTeams.clear();
    }

    public void addViewer(Player viewer) {
        viewer.setScoreboard(this.healthScoreboard);
    }

    public void removeViewer(Player viewer) {
        viewer.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}

