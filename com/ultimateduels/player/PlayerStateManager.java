/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.io.BukkitObjectInputStream
 *  org.bukkit.util.io.BukkitObjectOutputStream
 */
package com.ultimateduels.player;

import com.ultimateduels.UltimateDuels;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class PlayerStateManager {
    private final UltimateDuels plugin;
    private final Map<UUID, PlayerState> savedStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Map<UUID, PlayerState> lobbyStates;
    private final Set<UUID> spectatingPlayers;
    private File backupFile;

    public PlayerStateManager(UltimateDuels plugin) {
        this.plugin = plugin;
        this.savedStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.lobbyStates = new ConcurrentHashMap<UUID, PlayerState>();
        this.spectatingPlayers = ConcurrentHashMap.newKeySet();
        this.initializeBackupFile();
        this.loadPersistedStates();
    }

    private void initializeBackupFile() {
        this.backupFile = new File(this.plugin.getDataFolder(), "data/player-states.dat");
        if (!this.backupFile.getParentFile().exists()) {
            this.backupFile.getParentFile().mkdirs();
        }
    }

    public boolean saveState(@Nonnull Player player) {
        try {
            PlayerState state = this.captureState(player);
            this.savedStates.put(player.getUniqueId(), state);
            this.plugin.getLogger().fine("Saved state for player: " + player.getName());
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save state for " + player.getName(), e);
            return false;
        }
    }

    @Nonnull
    private PlayerState captureState(@Nonnull Player player) {
        PlayerState state = new PlayerState(player.getUniqueId());
        PlayerInventory inv = player.getInventory();
        state.setInventoryContents(this.cloneItemArray(inv.getContents()));
        state.setArmorContents(this.cloneItemArray(inv.getArmorContents()));
        state.setOffhandItem(inv.getItemInOffHand().clone());
        state.setExtraContents(this.cloneItemArray(inv.getExtraContents()));
        state.setHealth(player.getHealth());
        state.setMaxHealth(this.getMaxHealth(player));
        state.setFoodLevel(player.getFoodLevel());
        state.setSaturation(player.getSaturation());
        state.setExhaustion(player.getExhaustion());
        state.setLevel(player.getLevel());
        state.setExp(player.getExp());
        state.setTotalExperience(player.getTotalExperience());
        state.setEffects(new ArrayList<PotionEffect>(player.getActivePotionEffects()));
        state.setGameMode(player.getGameMode());
        state.setLocation(player.getLocation().clone());
        state.setFireTicks(player.getFireTicks());
        state.setFallDistance(player.getFallDistance());
        state.setRemainingAir(player.getRemainingAir());
        state.setAllowFlight(player.getAllowFlight());
        state.setFlying(player.isFlying());
        state.setWalkSpeed(player.getWalkSpeed());
        state.setFlySpeed(player.getFlySpeed());
        state.setScoreboardName(player.getScoreboard().equals((Object)Bukkit.getScoreboardManager().getMainScoreboard()) ? null : "custom");
        state.setSaveTime(System.currentTimeMillis());
        return state;
    }

    public boolean restoreState(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        PlayerState state = this.savedStates.get(uuid);
        if (state == null) {
            this.plugin.getLogger().warning("No saved state found for player: " + player.getName());
            return false;
        }
        try {
            this.applyState(player, state);
            this.savedStates.remove(uuid);
            this.plugin.getLogger().fine("Restored state for player: " + player.getName());
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to restore state for " + player.getName(), e);
            return false;
        }
    }

    private void applyState(@Nonnull Player player, @Nonnull PlayerState state) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (state.getInventoryContents() != null) {
            inv.setContents(state.getInventoryContents());
        }
        if (state.getArmorContents() != null) {
            inv.setArmorContents(state.getArmorContents());
        }
        if (state.getOffhandItem() != null) {
            inv.setItemInOffHand(state.getOffhandItem());
        }
        if (state.getExtraContents() != null) {
            inv.setExtraContents(state.getExtraContents());
        }
        this.setMaxHealth(player, state.getMaxHealth());
        player.setHealth(Math.min(state.getHealth(), state.getMaxHealth()));
        player.setFoodLevel(state.getFoodLevel());
        player.setSaturation(state.getSaturation());
        player.setExhaustion(state.getExhaustion());
        player.setLevel(state.getLevel());
        player.setExp(state.getExp());
        player.setTotalExperience(state.getTotalExperience());
        for (PotionEffect effect2 : state.getEffects()) {
            player.addPotionEffect(effect2);
        }
        player.setGameMode(state.getGameMode());
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setRemainingAir(state.getRemainingAir());
        player.setAllowFlight(state.isAllowFlight());
        if (state.isAllowFlight()) {
            player.setFlying(state.isFlying());
        } else {
            player.setFlying(false);
        }
        player.setNoDamageTicks(0);
        player.setInvulnerable(false);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        if (state.getLocation() != null && state.getLocation().getWorld() != null) {
            player.teleport(state.getLocation());
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.LEVITATION);
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            }
        }, 5L);
        player.updateInventory();
    }

    public boolean saveLobbyState(@Nonnull Player player) {
        try { this.lobbyStates.put(player.getUniqueId(), this.captureState(player)); return true; }
        catch (Exception e) { this.plugin.getLogger().log(Level.WARNING, "Failed to save lobby state for " + player.getName(), e); return false; }
    }

    public boolean restoreLobbyState(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        PlayerState state = this.lobbyStates.get(uuid);
        if (state == null) return false;
        try { this.applyState(player, state); this.lobbyStates.remove(uuid); return true; }
        catch (Exception e) { this.plugin.getLogger().log(Level.WARNING, "Failed to restore lobby state for " + player.getName(), e); return false; }
    }

    public boolean hasLobbyState(@Nonnull UUID uuid) { return this.lobbyStates.containsKey(uuid); }

    public boolean quickSave(@Nonnull Player player) {
        try {
            PlayerState state = new PlayerState(player.getUniqueId());
            PlayerInventory inv = player.getInventory();
            state.setInventoryContents(this.cloneItemArray(inv.getContents()));
            state.setArmorContents(this.cloneItemArray(inv.getArmorContents()));
            state.setOffhandItem(inv.getItemInOffHand().clone());
            state.setHealth(player.getHealth());
            state.setMaxHealth(this.getMaxHealth(player));
            state.setFoodLevel(player.getFoodLevel());
            state.setGameMode(player.getGameMode());
            state.setLocation(player.getLocation().clone());
            state.setSaveTime(System.currentTimeMillis());
            this.savedStates.put(player.getUniqueId(), state);
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to quick save state for " + player.getName(), e);
            return false;
        }
    }

    public void clearPlayer(@Nonnull Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        this.setMaxHealth(player, 20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(12.8f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);
        player.setNoDamageTicks(0);
        player.setInvulnerable(false);
        player.setFallDistance(0.0f);
        player.setLevel(0);
        player.setExp(0.0f);
        player.updateInventory();
    }

    public void resetForRound(@Nonnull Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(this.getMaxHealth(player));
        player.setFoodLevel(20);
        player.setSaturation(12.8f);
        player.setExhaustion(0.0f);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.updateInventory();
    }

    public void setSpectatorMode(@Nonnull Player player) {
        this.clearPlayer(player);
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        this.spectatingPlayers.add(player.getUniqueId());
    }

    public void restoreFromSpectator(@Nonnull Player player) {
        this.spectatingPlayers.remove(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        this.restoreState(player);
    }

    public boolean isSpectating(@Nonnull Player player) {
        return this.isSpectating(player.getUniqueId());
    }

    public boolean isSpectating(@Nonnull UUID uuid) {
        return this.spectatingPlayers.contains(uuid);
    }

    @Nonnull
    public Set<UUID> getSpectatingPlayers() {
        return new HashSet<UUID>(this.spectatingPlayers);
    }

    public int getSpectatingCount() {
        return this.spectatingPlayers.size();
    }

    public void removeFromSpectating(@Nonnull UUID uuid) {
        this.spectatingPlayers.remove(uuid);
    }

    public boolean hasState(@Nonnull UUID uuid) {
        return this.savedStates.containsKey(uuid);
    }

    public boolean hasState(@Nonnull Player player) {
        return this.hasState(player.getUniqueId());
    }

    @Nullable
    public PlayerState getState(@Nonnull UUID uuid) {
        return this.savedStates.get(uuid);
    }

    public void removeState(@Nonnull UUID uuid) {
        this.savedStates.remove(uuid);
    }

    @Nonnull
    public Set<UUID> getSavedStateUUIDs() {
        return new HashSet<UUID>(this.savedStates.keySet());
    }

    private void loadPersistedStates() {
        if (!this.backupFile.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.backupFile));){
            Map serializedStates = (Map)ois.readObject();
            for (Map.Entry entry : serializedStates.entrySet()) {
                try {
                    PlayerState state = this.deserializeState((byte[])entry.getValue());
                    if (state == null) continue;
                    this.savedStates.put((UUID)entry.getKey(), state);
                }
                catch (Exception e) {
                    this.plugin.getLogger().warning("Failed to deserialize state for " + String.valueOf(entry.getKey()));
                }
            }
            this.plugin.getLogger().info("\u00a7a[PlayerStateManager] Loaded " + this.savedStates.size() + " persisted states");
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load persisted states", e);
        }
    }

    public void savePersistedStates() {
        HashMap<UUID, byte[]> serializedStates = new HashMap<UUID, byte[]>();
        for (Map.Entry<UUID, PlayerState> entry : this.savedStates.entrySet()) {
            try {
                byte[] serialized = this.serializeState(entry.getValue());
                if (serialized == null) continue;
                serializedStates.put(entry.getKey(), serialized);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to serialize state for " + String.valueOf(entry.getKey()));
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.backupFile));){
            oos.writeObject(serializedStates);
            this.plugin.getLogger().info("\u00a7a[PlayerStateManager] Saved " + serializedStates.size() + " states to disk");
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save persisted states", e);
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private byte[] serializeState(@Nonnull PlayerState state) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
            byte[] byArray;
            try (BukkitObjectOutputStream boos = new BukkitObjectOutputStream((OutputStream)baos);){
                boos.writeObject((Object)state.getInventoryContents());
                boos.writeObject((Object)state.getArmorContents());
                boos.writeObject((Object)state.getOffhandItem());
                boos.writeDouble(state.getHealth());
                boos.writeDouble(state.getMaxHealth());
                boos.writeInt(state.getFoodLevel());
                boos.writeFloat(state.getSaturation());
                boos.writeObject((Object)state.getGameMode().name());
                boos.writeObject((Object)this.serializeLocation(state.getLocation()));
                boos.writeLong(state.getSaveTime());
                byArray = baos.toByteArray();
            }
            return byArray;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to serialize PlayerState", e);
            return null;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private PlayerState deserializeState(@Nonnull byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);){
            PlayerState playerState;
            try (BukkitObjectInputStream bois = new BukkitObjectInputStream((InputStream)bais);){
                PlayerState state = new PlayerState(null);
                state.setInventoryContents((ItemStack[])bois.readObject());
                state.setArmorContents((ItemStack[])bois.readObject());
                state.setOffhandItem((ItemStack)bois.readObject());
                state.setHealth(bois.readDouble());
                state.setMaxHealth(bois.readDouble());
                state.setFoodLevel(bois.readInt());
                state.setSaturation(bois.readFloat());
                state.setGameMode(GameMode.valueOf((String)((String)bois.readObject())));
                state.setLocation(this.deserializeLocation((String)bois.readObject()));
                state.setSaveTime(bois.readLong());
                playerState = state;
            }
            return playerState;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to deserialize PlayerState", e);
            return null;
        }
    }

    @Nullable
    private String serializeLocation(@Nullable Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        return String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), Float.valueOf(loc.getYaw()), Float.valueOf(loc.getPitch()));
    }

    @Nullable
    private Location deserializeLocation(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            String[] parts = str.split(";");
            return new Location(Bukkit.getWorld((String)parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
        }
        catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private ItemStack[] cloneItemArray(@Nullable ItemStack[] original) {
        if (original == null) {
            return null;
        }
        ItemStack[] cloned = new ItemStack[original.length];
        for (int i = 0; i < original.length; ++i) {
            cloned[i] = original[i] != null ? original[i].clone() : null;
        }
        return cloned;
    }

    private double getMaxHealth(@Nonnull Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return attribute != null ? attribute.getBaseValue() : 20.0;
    }

    private void setMaxHealth(@Nonnull Player player, double health) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(health);
        }
    }

    public void cleanupExpiredStates() {
        long expirationTime = System.currentTimeMillis() - 3600000L;
        Iterator<Map.Entry<UUID, PlayerState>> iterator = this.savedStates.entrySet().iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            Player player;
            Map.Entry<UUID, PlayerState> entry = iterator.next();
            if (entry.getValue().getSaveTime() >= expirationTime || (player = Bukkit.getPlayer((UUID)entry.getKey())) != null && player.isOnline()) continue;
            iterator.remove();
            ++removed;
        }
        if (removed > 0) {
            this.plugin.getLogger().info("\u00a7e[PlayerStateManager] Cleaned up " + removed + " expired states");
        }
    }

    public void restoreAllOnlinePlayers() {
        for (UUID uuid : new ArrayList<UUID>(this.savedStates.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null || !player.isOnline()) continue;
            this.restoreState(player);
        }
    }

    public void shutdown() {
        this.restoreAllOnlinePlayers();
        this.savePersistedStates();
        this.spectatingPlayers.clear();
        this.plugin.getLogger().info("\u00a7a[PlayerStateManager] Shutdown complete");
    }

    public int getSavedStateCount() {
        return this.savedStates.size();
    }

    public static class PlayerState
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID playerUUID;
        private ItemStack[] inventoryContents;
        private ItemStack[] armorContents;
        private ItemStack offhandItem;
        private ItemStack[] extraContents;
        private double health;
        private double maxHealth;
        private int foodLevel;
        private float saturation;
        private float exhaustion;
        private int level;
        private float exp;
        private int totalExperience;
        private List<PotionEffect> effects;
        private GameMode gameMode;
        private Location location;
        private int fireTicks;
        private float fallDistance;
        private int remainingAir;
        private boolean allowFlight;
        private boolean flying;
        private float walkSpeed;
        private float flySpeed;
        private String scoreboardName;
        private long saveTime;

        public PlayerState(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.effects = new ArrayList<PotionEffect>();
            this.saveTime = System.currentTimeMillis();
        }

        public UUID getPlayerUUID() {
            return this.playerUUID;
        }

        public void setPlayerUUID(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        public ItemStack[] getInventoryContents() {
            return this.inventoryContents;
        }

        public void setInventoryContents(ItemStack[] inventoryContents) {
            this.inventoryContents = inventoryContents;
        }

        public ItemStack[] getArmorContents() {
            return this.armorContents;
        }

        public void setArmorContents(ItemStack[] armorContents) {
            this.armorContents = armorContents;
        }

        public ItemStack getOffhandItem() {
            return this.offhandItem;
        }

        public void setOffhandItem(ItemStack offhandItem) {
            this.offhandItem = offhandItem;
        }

        public ItemStack[] getExtraContents() {
            return this.extraContents;
        }

        public void setExtraContents(ItemStack[] extraContents) {
            this.extraContents = extraContents;
        }

        public double getHealth() {
            return this.health;
        }

        public void setHealth(double health) {
            this.health = health;
        }

        public double getMaxHealth() {
            return this.maxHealth;
        }

        public void setMaxHealth(double maxHealth) {
            this.maxHealth = maxHealth;
        }

        public int getFoodLevel() {
            return this.foodLevel;
        }

        public void setFoodLevel(int foodLevel) {
            this.foodLevel = foodLevel;
        }

        public float getSaturation() {
            return this.saturation;
        }

        public void setSaturation(float saturation) {
            this.saturation = saturation;
        }

        public float getExhaustion() {
            return this.exhaustion;
        }

        public void setExhaustion(float exhaustion) {
            this.exhaustion = exhaustion;
        }

        public int getLevel() {
            return this.level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public float getExp() {
            return this.exp;
        }

        public void setExp(float exp) {
            this.exp = exp;
        }

        public int getTotalExperience() {
            return this.totalExperience;
        }

        public void setTotalExperience(int totalExperience) {
            this.totalExperience = totalExperience;
        }

        public List<PotionEffect> getEffects() {
            return this.effects;
        }

        public void setEffects(List<PotionEffect> effects) {
            this.effects = effects;
        }

        public GameMode getGameMode() {
            return this.gameMode;
        }

        public void setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        public Location getLocation() {
            return this.location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public int getFireTicks() {
            return this.fireTicks;
        }

        public void setFireTicks(int fireTicks) {
            this.fireTicks = fireTicks;
        }

        public float getFallDistance() {
            return this.fallDistance;
        }

        public void setFallDistance(float fallDistance) {
            this.fallDistance = fallDistance;
        }

        public int getRemainingAir() {
            return this.remainingAir;
        }

        public void setRemainingAir(int remainingAir) {
            this.remainingAir = remainingAir;
        }

        public boolean isAllowFlight() {
            return this.allowFlight;
        }

        public void setAllowFlight(boolean allowFlight) {
            this.allowFlight = allowFlight;
        }

        public boolean isFlying() {
            return this.flying;
        }

        public void setFlying(boolean flying) {
            this.flying = flying;
        }

        public float getWalkSpeed() {
            return this.walkSpeed;
        }

        public void setWalkSpeed(float walkSpeed) {
            this.walkSpeed = walkSpeed;
        }

        public float getFlySpeed() {
            return this.flySpeed;
        }

        public void setFlySpeed(float flySpeed) {
            this.flySpeed = flySpeed;
        }

        public String getScoreboardName() {
            return this.scoreboardName;
        }

        public void setScoreboardName(String scoreboardName) {
            this.scoreboardName = scoreboardName;
        }

        public long getSaveTime() {
            return this.saveTime;
        }

        public void setSaveTime(long saveTime) {
            this.saveTime = saveTime;
        }
    }
}

