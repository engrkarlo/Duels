/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 */
package com.ultimateduels.models.player;

import com.ultimateduels.models.duel.Duel;
import com.ultimateduels.models.ffa.FFASession;
import com.ultimateduels.models.kit.Kit;
import com.ultimateduels.models.party.Party;
import com.ultimateduels.models.player.PlayerSettings;
import com.ultimateduels.models.player.PlayerState;
import com.ultimateduels.models.player.PlayerStats;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class DuelPlayer {
    private final UUID uuid;
    private final String name;
    private WeakReference<Player> playerRef;
    private PlayerStats stats;
    private PlayerSettings settings;
    private PlayerState state;
    private long stateChangedAt;
    private Duel currentDuel;
    private Party currentParty;
    private FFASession ffaSession;
    private Kit selectedKit;
    private final CombatData combatData;
    private String queuedKit;
    private long queueJoinTime;
    private ItemStack[] inventoryBackup;
    private ItemStack[] armorBackup;
    private ItemStack offhandBackup;
    private Collection<PotionEffect> effectsBackup;
    private Location locationBackup;
    private GameMode gameModeBackup;
    private long sessionStartTime;
    private long lastActivityTime;
    private final Map<String, Object> metadata;

    public DuelPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.playerRef = new WeakReference<Player>(player);
        this.state = PlayerState.IN_LOBBY;
        this.stateChangedAt = System.currentTimeMillis();
        this.combatData = new CombatData();
        this.metadata = new ConcurrentHashMap<String, Object>();
        this.sessionStartTime = System.currentTimeMillis();
        this.lastActivityTime = System.currentTimeMillis();
        this.stats = new PlayerStats(this.uuid);
        this.settings = new PlayerSettings(this.uuid);
    }

    public DuelPlayer(Player player, PlayerStats stats, PlayerSettings settings) {
        this(player);
        this.stats = stats != null ? stats : new PlayerStats(this.uuid);
        this.settings = settings != null ? settings : new PlayerSettings(this.uuid);
    }

    public Player getPlayer() {
        Player player = (Player)this.playerRef.get();
        if (!(player != null && player.isOnline() || (player = Bukkit.getPlayer((UUID)this.uuid)) == null)) {
            this.playerRef = new WeakReference<Player>(player);
        }
        return player;
    }

    public boolean isOnline() {
        Player player = this.getPlayer();
        return player != null && player.isOnline();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        Player player = this.getPlayer();
        return player != null ? player.getName() : this.name;
    }

    public String getDisplayName() {
        Player player = this.getPlayer();
        return player != null ? player.getDisplayName() : this.name;
    }

    public int getPing() {
        Player player = this.getPlayer();
        return player != null ? player.getPing() : -1;
    }

    public PlayerState getState() {
        return this.state;
    }

    public void setState(PlayerState state) {
        PlayerState oldState = this.state;
        this.state = state;
        this.stateChangedAt = System.currentTimeMillis();
        this.lastActivityTime = System.currentTimeMillis();
        this.onStateChange(oldState, state);
    }

    private void onStateChange(PlayerState oldState, PlayerState newState) {
        if (newState == PlayerState.IN_LOBBY) {
            if (oldState == PlayerState.IN_QUEUE) {
                this.queuedKit = null;
                this.queueJoinTime = 0L;
            }
            this.combatData.reset();
        }
    }

    public long getTimeInCurrentState() {
        return System.currentTimeMillis() - this.stateChangedAt;
    }

    public boolean isInState(PlayerState state) {
        return this.state == state;
    }

    public boolean isBusy() {
        return this.state != PlayerState.IN_LOBBY;
    }

    public boolean isFighting() {
        return this.state == PlayerState.FIGHTING || this.state == PlayerState.IN_COUNTDOWN || this.state == PlayerState.IN_FFA;
    }

    public PlayerStats getStats() {
        return this.stats;
    }

    public void setStats(PlayerStats stats) {
        this.stats = stats;
    }

    public PlayerSettings getSettings() {
        return this.settings;
    }

    public void setSettings(PlayerSettings settings) {
        this.settings = settings;
    }

    public CombatData getCombatData() {
        return this.combatData;
    }

    public Duel getCurrentDuel() {
        return this.currentDuel;
    }

    public void setCurrentDuel(Duel duel) {
        this.currentDuel = duel;
        if (duel != null) {
            this.setState(PlayerState.IN_COUNTDOWN);
        }
    }

    public boolean isInDuel() {
        return this.currentDuel != null;
    }

    public Party getCurrentParty() {
        return this.currentParty;
    }

    public void setCurrentParty(Party party) {
        this.currentParty = party;
    }

    public boolean isInParty() {
        return this.currentParty != null;
    }

    public boolean isPartyLeader() {
        return this.currentParty != null && this.currentParty.isLeader(this.uuid);
    }

    public FFASession getFfaSession() {
        return this.ffaSession;
    }

    public void setFfaSession(FFASession session) {
        this.ffaSession = session;
        if (session != null) {
            this.setState(PlayerState.IN_FFA);
        }
    }

    public boolean isInFFA() {
        return this.ffaSession != null;
    }

    public Kit getSelectedKit() {
        return this.selectedKit;
    }

    public void setSelectedKit(Kit kit) {
        this.selectedKit = kit;
    }

    public void applyKit(Kit kit, boolean clearFirst) {
        Player player = this.getPlayer();
        if (player == null || kit == null) {
            return;
        }
        if (clearFirst) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }
        kit.applyTo(player);
        this.selectedKit = kit;
    }

    public String getQueuedKit() {
        return this.queuedKit;
    }

    public void setQueuedKit(String kitName) {
        this.queuedKit = kitName;
        long l = this.queueJoinTime = kitName != null ? System.currentTimeMillis() : 0L;
        if (kitName != null) {
            this.setState(PlayerState.IN_QUEUE);
        }
    }

    public long getTimeInQueue() {
        if (this.queueJoinTime == 0L) {
            return 0L;
        }
        return System.currentTimeMillis() - this.queueJoinTime;
    }

    public boolean isInQueue() {
        return this.state == PlayerState.IN_QUEUE && this.queuedKit != null;
    }

    public void backupInventory() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        this.inventoryBackup = (ItemStack[])player.getInventory().getContents().clone();
        this.armorBackup = (ItemStack[])player.getInventory().getArmorContents().clone();
        this.offhandBackup = player.getInventory().getItemInOffHand().clone();
        this.effectsBackup = new ArrayList<PotionEffect>(player.getActivePotionEffects());
        this.locationBackup = player.getLocation().clone();
        this.gameModeBackup = player.getGameMode();
    }

    public void restoreInventory() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (this.inventoryBackup != null) {
            player.getInventory().setContents(this.inventoryBackup);
        }
        if (this.armorBackup != null) {
            player.getInventory().setArmorContents(this.armorBackup);
        }
        if (this.offhandBackup != null) {
            player.getInventory().setItemInOffHand(this.offhandBackup);
        }
        if (this.effectsBackup != null) {
            this.effectsBackup.forEach(arg_0 -> ((Player)player).addPotionEffect(arg_0));
        }
        if (this.gameModeBackup != null) {
            player.setGameMode(this.gameModeBackup);
        }
        this.clearBackups();
    }

    public void clearBackups() {
        this.inventoryBackup = null;
        this.armorBackup = null;
        this.offhandBackup = null;
        this.effectsBackup = null;
        this.locationBackup = null;
        this.gameModeBackup = null;
    }

    public Location getLocationBackup() {
        return this.locationBackup;
    }

    public boolean hasBackup() {
        return this.inventoryBackup != null;
    }

    public void heal() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
    }

    public void clearEffects() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void clearInventory() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
    }

    public void teleport(Location location) {
        Player player = this.getPlayer();
        if (player != null && location != null) {
            player.teleport(location);
        }
    }

    public void sendMessage(String message) {
        Player player = this.getPlayer();
        if (player != null) {
            player.sendMessage(message);
        }
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Player player = this.getPlayer();
        if (player != null) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void playSound(Sound sound, float volume, float pitch) {
        Player player = this.getPlayer();
        if (player != null && this.settings.isSoundsEnabled()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void resetForRound() {
        this.heal();
        this.clearEffects();
        this.combatData.reset();
    }

    public void resetToLobby() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        this.combatData.reset();
        this.currentDuel = null;
        this.ffaSession = null;
        this.selectedKit = null;
        this.queuedKit = null;
        this.queueJoinTime = 0L;
        if (this.hasBackup()) {
            this.restoreInventory();
        } else {
            this.clearInventory();
            this.clearEffects();
            this.heal();
        }
        this.setState(PlayerState.IN_LOBBY);
    }

    public void setMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public <T> T getMetadata(String key) {
        return (T)this.metadata.get(key);
    }

    public <T> T getMetadata(String key, T defaultValue) {
        Object value = this.metadata.get(key);
        return (T)(value != null ? value : defaultValue);
    }

    public void removeMetadata(String key) {
        this.metadata.remove(key);
    }

    public boolean hasMetadata(String key) {
        return this.metadata.containsKey(key);
    }

    public long getSessionStartTime() {
        return this.sessionStartTime;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - this.sessionStartTime;
    }

    public long getLastActivityTime() {
        return this.lastActivityTime;
    }

    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    public long getIdleTime() {
        return System.currentTimeMillis() - this.lastActivityTime;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DuelPlayer that = (DuelPlayer)o;
        return Objects.equals(this.uuid, that.uuid);
    }

    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    public String toString() {
        return "DuelPlayer{uuid=" + String.valueOf(this.uuid) + ", name='" + this.name + "', state=" + String.valueOf((Object)this.state) + ", online=" + this.isOnline() + "}";
    }

    public static class CombatData {
        private int roundKills = 0;
        private int roundDeaths = 0;
        private double damageDealt = 0.0;
        private double damageTaken = 0.0;
        private int arrowsShot = 0;
        private int arrowsHit = 0;
        private int potionsUsed = 0;
        private int goldenApplesEaten = 0;
        private int pearlsThrown = 0;
        private int hitsDealt = 0;
        private int hitsTaken = 0;
        private int criticalHits = 0;
        private long combatStartTime = 0L;
        private UUID lastDamager = null;
        private long lastDamageTime = 0L;

        public void reset() {
            this.roundKills = 0;
            this.roundDeaths = 0;
            this.damageDealt = 0.0;
            this.damageTaken = 0.0;
            this.arrowsShot = 0;
            this.arrowsHit = 0;
            this.potionsUsed = 0;
            this.goldenApplesEaten = 0;
            this.pearlsThrown = 0;
            this.hitsDealt = 0;
            this.hitsTaken = 0;
            this.criticalHits = 0;
            this.combatStartTime = System.currentTimeMillis();
            this.lastDamager = null;
            this.lastDamageTime = 0L;
        }

        public void addKill() {
            ++this.roundKills;
        }

        public void addDeath() {
            ++this.roundDeaths;
        }

        public void addDamageDealt(double damage) {
            this.damageDealt += damage;
        }

        public void addDamageTaken(double damage) {
            this.damageTaken += damage;
        }

        public void addArrowShot() {
            ++this.arrowsShot;
        }

        public void addArrowHit() {
            ++this.arrowsHit;
        }

        public void addPotionUsed() {
            ++this.potionsUsed;
        }

        public void addGoldenAppleEaten() {
            ++this.goldenApplesEaten;
        }

        public void addPearlThrown() {
            ++this.pearlsThrown;
        }

        public void addHitDealt() {
            ++this.hitsDealt;
        }

        public void addHitTaken() {
            ++this.hitsTaken;
        }

        public void addCriticalHit() {
            ++this.criticalHits;
        }

        public void setLastDamager(UUID uuid) {
            this.lastDamager = uuid;
            this.lastDamageTime = System.currentTimeMillis();
        }

        public int getRoundKills() {
            return this.roundKills;
        }

        public int getRoundDeaths() {
            return this.roundDeaths;
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

        public int getPotionsUsed() {
            return this.potionsUsed;
        }

        public int getGoldenApplesEaten() {
            return this.goldenApplesEaten;
        }

        public int getPearlsThrown() {
            return this.pearlsThrown;
        }

        public int getHitsDealt() {
            return this.hitsDealt;
        }

        public int getHitsTaken() {
            return this.hitsTaken;
        }

        public int getCriticalHits() {
            return this.criticalHits;
        }

        public UUID getLastDamager() {
            return this.lastDamager;
        }

        public long getLastDamageTime() {
            return this.lastDamageTime;
        }

        public long getCombatDuration() {
            return System.currentTimeMillis() - this.combatStartTime;
        }

        public double getArrowAccuracy() {
            if (this.arrowsShot == 0) {
                return 0.0;
            }
            return (double)this.arrowsHit / (double)this.arrowsShot * 100.0;
        }
    }
}

