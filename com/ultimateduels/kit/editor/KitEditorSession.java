/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.ultimateduels.kit.editor;

import com.ultimateduels.kit.model.DuelKit;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitEditorSession {
    private final UUID playerUUID;
    private final String originalKitName;
    private final DuelKit editingKit;
    private final long startTime;
    private boolean modified;
    private EditorState state;

    public KitEditorSession(@Nonnull Player player, @Nonnull DuelKit kitToEdit) {
        this.playerUUID = player.getUniqueId();
        this.originalKitName = kitToEdit.getName();
        this.editingKit = kitToEdit.clone();
        this.startTime = System.currentTimeMillis();
        this.modified = false;
        this.state = EditorState.EDITING_INVENTORY;
    }

    @Nonnull
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Nonnull
    public String getOriginalKitName() {
        return this.originalKitName;
    }

    @Nonnull
    public DuelKit getEditingKit() {
        return this.editingKit;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - this.startTime;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Nonnull
    public EditorState getState() {
        return this.state;
    }

    public void setState(@Nonnull EditorState state) {
        this.state = state;
    }

    public void setInventorySlot(int slot, @Nullable ItemStack item) {
        if (slot >= 0 && slot < 36) {
            this.editingKit.setInventorySlot(slot, item);
            this.modified = true;
        }
    }

    public void setArmorSlot(int slot, @Nullable ItemStack item) {
        switch (slot) {
            case 0: {
                this.editingKit.setBoots(item);
                break;
            }
            case 1: {
                this.editingKit.setLeggings(item);
                break;
            }
            case 2: {
                this.editingKit.setChestplate(item);
                break;
            }
            case 3: {
                this.editingKit.setHelmet(item);
            }
        }
        this.modified = true;
    }

    public void setOffhand(@Nullable ItemStack item) {
        this.editingKit.setOffhand(item);
        this.modified = true;
    }

    public boolean isExpired() {
        return this.getSessionDuration() > 600000L;
    }

    public void cancel() {
        this.state = EditorState.CANCELLED;
    }

    public boolean isCancelled() {
        return this.state == EditorState.CANCELLED;
    }

    public static enum EditorState {
        EDITING_INVENTORY,
        EDITING_ARMOR,
        EDITING_OFFHAND,
        EDITING_EFFECTS,
        CONFIRMING_SAVE,
        CANCELLED;

    }
}

