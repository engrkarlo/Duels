from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    new_text = fn(text)
    if new_text == text:
        print("already patched", rel)
    else:
        path.write_text(new_text, encoding="utf-8")
        print("patched", rel)


def replace_once(text, old, new, rel):
    if old not in text:
        raise RuntimeError(f"Expected source block was not found in {rel}; refusing to guess a patch location")
    return text.replace(old, new, 1)


def patch_join(text):
    old = '''            this.resetPlayerState(player);\n            if (this.shouldTeleportToLobbyOnJoin() && (lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn()) != null) {\n                player.teleport(lobbySpawn);\n            }\n            if (this.shouldGiveLobbyItemsOnJoin()) {\n                this.plugin.getLobbyManager().giveHotbarItems(player);\n            }'''
    new = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player);\n            boolean teleportToLobby = this.shouldTeleportToLobbyOnJoin()\n                    && this.plugin.getLobbyManager() != null\n                    && (lobbySpawn = this.plugin.getLobbyManager().getLobbySpawn()) != null;\n            if (teleportToLobby) {\n                this.resetPlayerState(player);\n                player.teleport(lobbySpawn);\n                inLobbyWorld = true;\n            } else if (inLobbyWorld) {\n                this.resetPlayerState(player);\n            }\n            if (inLobbyWorld && this.shouldGiveLobbyItemsOnJoin()) {\n                this.plugin.getLobbyManager().giveHotbarItems(player);\n            }'''
    return replace_once(text, old, new, 'com/ultimateduels/listeners/PlayerJoinQuitListener.java')


def patch_join_settings(text):
    old = '''    private boolean shouldTeleportToLobbyOnJoin() {\n        if (this.plugin.getConfig().contains("lobby.teleport-on-join")) {\n            return this.plugin.getConfig().getBoolean("lobby.teleport-on-join", true);\n        }\n        return this.plugin.getConfig().getBoolean("lobby.enabled", true);\n    }\n\n    private boolean shouldGiveLobbyItemsOnJoin() {\n        if (this.plugin.getConfig().contains("lobby.give-items-on-join")) {\n            return this.plugin.getConfig().getBoolean("lobby.give-items-on-join", true);\n        }\n        if (this.plugin.getConfig().contains("lobby.give-lobby-items")) {\n            return this.plugin.getConfig().getBoolean("lobby.give-lobby-items", true);\n        }\n        return this.plugin.getConfig().getBoolean("lobby.enabled", true);\n    }'''
    new = '''    private boolean shouldTeleportToLobbyOnJoin() {\n        if (this.plugin.getLobbyManager() != null) {\n            return this.plugin.getLobbyManager().isTeleportOnJoin();\n        }\n        return this.plugin.getConfig().getBoolean("lobby.teleport-on-join", false);\n    }\n\n    private boolean shouldGiveLobbyItemsOnJoin() {\n        if (this.plugin.getLobbyManager() != null) {\n            return this.plugin.getLobbyManager().isGiveItemsOnJoin();\n        }\n        return this.plugin.getConfig().getBoolean("lobby.give-items-on-join", false);\n    }'''
    return replace_once(text, old, new, 'com/ultimateduels/listeners/PlayerJoinQuitListener.java')


def patch_quit(text):
    old = '''        if (this.plugin.getFFAManager().isInFFA(playerId)) {\n            this.plugin.getFFAManager().removePlayer(playerId);\n        }'''
    new = '''        if (this.plugin.getFFAManager().isInFFA(playerId)) {\n            if (this.plugin.getFFAManager().isCombatTagged(playerId)) {\n                this.plugin.getFFAManager().handleCombatLogout(player);\n            } else {\n                this.plugin.getFFAManager().removePlayer(playerId);\n            }\n        }'''
    return replace_once(text, old, new, 'com/ultimateduels/listeners/PlayerJoinQuitListener.java')


def patch_decompiler_constructors(text):
    return text.replace('new BukkitRunnable(this){', 'new BukkitRunnable(){')


def patch_command_block(text):
    old = '''        WorldRestrictionManager restrictions = this.plugin.getWorldRestrictionManager();\n        if (restrictions != null && !restrictions.isPluginAllowedInWorld(player.getWorld())) return;\n        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");'''
    new = '''        List<String> worlds = this.plugin.getConfig().getStringList("command-blocking.worlds");'''
    return replace_once(text, old, new, 'com/ultimateduels/listeners/CommandBlockListener.java')


def patch_ffa_manager(text):
    marker = '''    public boolean shouldDropItemsOnDeath() {\n        return this.deathItemsDrop;\n    }'''
    helper = '''    public boolean shouldDropItemsOnDeath() {\n        return this.deathItemsDrop;\n    }\n\n    public boolean isCombatTagged(@Nonnull UUID uuid) {\n        CombatData data = this.combatTracking.get(uuid);\n        return data != null && !data.isExpired((long)this.combatTagSeconds * 1000L);\n    }\n\n    @Nullable\n    public UUID getCombatOpponent(@Nonnull UUID uuid) {\n        CombatData data = this.combatTracking.get(uuid);\n        if (data == null || data.isExpired((long)this.combatTagSeconds * 1000L)) {\n            return null;\n        }\n        return data.getLastAttacker();\n    }\n\n    /**\n     * Applies the normal FFA death accounting to a player who disconnects while combat tagged,\n     * without allowing the normal lobby/respawn flow to run after the player has left.\n     */\n    public void handleCombatLogout(@Nonnull Player player) {\n        UUID uuid = player.getUniqueId();\n        if (!this.isInFFA(uuid)) {\n            return;\n        }\n        CombatData combatData = this.combatTracking.get(uuid);\n        if (combatData == null || combatData.isExpired((long)this.combatTagSeconds * 1000L)) {\n            this.removePlayer(uuid);\n            return;\n        }\n        UUID attackerUUID = combatData.getLastAttacker();\n        Player attacker = attackerUUID != null ? Bukkit.getPlayer(attackerUUID) : null;\n        if (this.deathItemsDrop) {\n            this.dropInventoryItems(player);\n        }\n\n        // Reuse the normal FFA death accounting (stats, killer rewards, announcements),\n        // but suppress its normal respawn/lobby action because the player is disconnecting.\n        boolean oldRespawnToLobby = this.respawnTeleportToLobby;\n        int oldRespawnDelay = this.respawnDelaySeconds;\n        this.respawnTeleportToLobby = false;\n        this.respawnDelaySeconds = 0;\n        try {\n            this.handleDeath(player, attacker);\n        } finally {\n            this.respawnTeleportToLobby = oldRespawnToLobby;\n            this.respawnDelaySeconds = oldRespawnDelay;\n        }\n\n        String arenaName = this.playerArenaMap.remove(uuid);\n        this.playerDataMap.remove(uuid);\n        this.combatTracking.remove(uuid);\n        if (arenaName != null) {\n            FFAArenaInstance arena = this.ffaArenas.get(arenaName);\n            if (arena != null) {\n                arena.removePlayer(uuid);\n                if (arena.getPlayerCount() == 0) {\n                    this.scheduleArenaRegeneration(arenaName);\n                }\n            }\n        }\n        this.removeSpawnProtection(player);\n        HealthDisplayManager healthDisplayManager = this.plugin.getHealthDisplayManager();\n        if (healthDisplayManager != null) {\n            healthDisplayManager.removeDisplayForFFA(player);\n        }\n        if (this.playerStateManager != null && this.playerStateManager.hasState(uuid)) {\n            boolean restored = this.playerStateManager.restoreState(player);\n            if (!restored) {\n                player.getInventory().clear();\n                player.getInventory().setArmorContents(null);\n                player.getInventory().setItemInOffHand(null);\n            }\n        }\n    }\n\n    public void dropInventoryItems(@Nonnull Player player) {\n        if (player.getWorld() != null) {\n            for (ItemStack item : player.getInventory().getContents()) {\n                if (item != null && !item.getType().isAir()) {\n                    player.getWorld().dropItemNaturally(player.getLocation(), item.clone());\n                }\n            }\n            for (ItemStack item : player.getInventory().getArmorContents()) {\n                if (item != null && !item.getType().isAir()) {\n                    player.getWorld().dropItemNaturally(player.getLocation(), item.clone());\n                }\n            }\n            ItemStack offHand = player.getInventory().getItemInOffHand();\n            if (offHand != null && !offHand.getType().isAir()) {\n                player.getWorld().dropItemNaturally(player.getLocation(), offHand.clone());\n            }\n        }\n        player.getInventory().clear();\n        player.getInventory().setArmorContents(null);\n        player.getInventory().setItemInOffHand(null);\n    }'''
    return replace_once(text, marker, helper, 'com/ultimateduels/ffa/FFAManager.java')


def patch_death_listener(text):
    old = '''        if (inFFA) {\n            boolean dropItems = this.ffaManager != null && this.ffaManager.shouldDropItemsOnDeath();\n            if (dropItems) {\n                return;\n            }\n            event.setCancelled(true);'''
    new = '''        if (inFFA) {\n            boolean dropItems = this.ffaManager != null && this.ffaManager.shouldDropItemsOnDeath();\n            event.setCancelled(true);\n            if (dropItems) {\n                this.ffaManager.dropInventoryItems(victim);\n            }'''
    return replace_once(text, old, new, 'com/ultimateduels/listeners/PlayerDeathListener.java')


edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_join)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_join_settings)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_quit)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_decompiler_constructors)
edit('com/ultimateduels/listeners/CommandBlockListener.java', patch_command_block)
edit('com/ultimateduels/ffa/FFAManager.java', patch_ffa_manager)
edit('com/ultimateduels/listeners/PlayerDeathListener.java', patch_death_listener)

print('runtime bug fixes applied')
