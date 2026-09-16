/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.ultimateduels.arena.model;

import com.ultimateduels.arena.model.DuelArena;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ArenaSetupSession {
    private final UUID playerUUID;
    private final DuelArena arena;
    private final long startTime;
    private SetupStep currentStep;
    private boolean wandMode;

    public ArenaSetupSession(@Nonnull UUID playerUUID, @Nonnull DuelArena arena) {
        this.playerUUID = playerUUID;
        this.arena = arena;
        this.startTime = System.currentTimeMillis();
        this.currentStep = SetupStep.CORNER_1;
        this.wandMode = false;
    }

    @Nonnull
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Nonnull
    public DuelArena getArena() {
        return this.arena;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - this.startTime;
    }

    @Nonnull
    public SetupStep getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(@Nonnull SetupStep step) {
        this.currentStep = step;
    }

    public void advanceStep() {
        this.currentStep = this.currentStep.next();
    }

    public boolean isWandMode() {
        return this.wandMode;
    }

    public void setWandMode(boolean wandMode) {
        this.wandMode = wandMode;
    }

    public boolean isComplete() {
        return this.currentStep == SetupStep.COMPLETE;
    }

    public int getProgress() {
        return this.currentStep.ordinal() * 100 / SetupStep.values().length;
    }

    @Nonnull
    public String getStatusMessage() {
        return "\u00a7e[Setup] \u00a77" + this.currentStep.getDisplayName() + "\n\u00a77" + this.currentStep.getInstruction();
    }

    public static enum SetupStep {
        CORNER_1("Set Corner 1", "Use /arena setcorner1 or left-click with wand"),
        CORNER_2("Set Corner 2", "Use /arena setcorner2 or right-click with wand"),
        SPAWN_1("Set Spawn Point 1", "Use /arena setspawn1"),
        SPAWN_2("Set Spawn Point 2", "Use /arena setspawn2"),
        SPECTATOR_SPAWN("Set Spectator Spawn", "Use /arena setspectator"),
        SAVE_SCHEMATIC("Save Schematic", "Use /arena saveschematic"),
        CONFIGURE_KITS("Configure Compatible Kits", "Use /arena addkit <kit>"),
        COMPLETE("Setup Complete", "Use /arena enable to enable");

        private final String displayName;
        private final String instruction;

        private SetupStep(String displayName, String instruction) {
            this.displayName = displayName;
            this.instruction = instruction;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getInstruction() {
            return this.instruction;
        }

        public SetupStep next() {
            SetupStep[] values;
            int nextOrdinal = this.ordinal() + 1;
            if (nextOrdinal < (values = SetupStep.values()).length) {
                return values[nextOrdinal];
            }
            return COMPLETE;
        }
    }
}

