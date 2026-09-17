from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def edit(rel, fn):
    path = ROOT / rel
    text = path.read_text(encoding='utf-8')
    new = fn(text)
    if new != text:
        path.write_text(new, encoding='utf-8')
        print('patched', rel)
    else:
        print('already patched', rel)


def patch_reload(text):
    marker = '    public boolean reload() {\n        this.getLogger().info("Reloading UltimateDuels...");'
    if marker in text and 'this.reloadConfig();' not in text[text.index(marker):text.index(marker)+450]:
        return text.replace(marker, '    public boolean reload() {\n        this.getLogger().info("Reloading UltimateDuels...");\n        this.reloadConfig();', 1)
    return text


def patch_shutdown(text):
    old = '''            if (this.lobbyManager != null) {
                try {
                    this.teleportAllPlayersToLobby();
                }
                catch (Exception e) {
                    this.getLogger().warning("Error teleporting players to lobby: " + e.getMessage());
                }
            }
'''
    return text.replace(old, '''            // Never teleport online players during shutdown. Paper saves their actual
            // world, location and inventory; moving them here can corrupt restart state.
''', 1) if old in text else text


def patch_join(text):
    old = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean firstJoin = !player.hasPlayedBefore();
            boolean firstJoinTeleport = firstJoin && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);
            boolean manageLobby = inLobbyWorld || firstJoinTeleport;

            // Returning players in normal worlds are deliberately left completely alone.
            // Paper restores their saved world/location from player data; UltimateDuels must
            // not clear their inventory or teleport them to the lobby during a restart/login.
            if (manageLobby) {
                this.resetPlayerState(player);
                Location targetLobby = null;
                if ((firstJoinTeleport || (inLobbyWorld && this.shouldTeleportToLobbyOnJoin()))
                        && this.plugin.getLobbyManager() != null) {
                    targetLobby = this.plugin.getLobbyManager().getLobbySpawn();
                }
                if (targetLobby != null) player.teleport(targetLobby);
                if (this.shouldGiveLobbyItemsOnJoin() && this.plugin.getLobbyManager() != null) {
                    this.plugin.getLobbyManager().giveHotbarItems(player);
                }
            }'''
    new = '''            boolean inLobbyWorld = this.plugin.getLobbyManager() != null
                    && this.plugin.getLobbyManager().isInLobbyWorld(player);
            boolean firstJoin = !player.hasPlayedBefore();
            boolean firstJoinTeleport = firstJoin
                    && this.plugin.getConfig().getBoolean("lobby.first-join-teleport", false);

            // Only touch lobby state in the lobby, or on an explicitly configured first
            // join. A normal-world login after a restart must preserve Paper's saved
            // world, exact location, inventory, armor and offhand unchanged.
            if (inLobbyWorld || firstJoinTeleport) {
                this.resetPlayerState(player);
                if (firstJoinTeleport || this.shouldTeleportToLobbyOnJoin()) {
                    Location targetLobby = this.plugin.getLobbyManager().getLobbySpawn();
                    if (targetLobby != null) player.teleport(targetLobby);
                }
                if (this.shouldGiveLobbyItemsOnJoin()) {
                    this.plugin.getLobbyManager().giveHotbarItems(player);
                }
            }'''
    if old in text:
        return text.replace(old, new, 1)
    return text


def patch_prepare(text):
    old = '''  # Seconds before a tracked FFA item is removed. 0 removes immediately.
  delay-seconds: 30
  # Clear existing item entities when an FFA arena becomes active.
  clear-on-match-start: true
  # Radius around each FFA spawn point used for the match-start cleanup.
  match-start-clear-radius: 64
  # Countdown location: action-bar, chat, both, or none.
  display: action-bar
  # {time} and {seconds} are replaced with the remaining seconds.
  message: '&eDropped items clear in &f{time}s'
'''
    new = '''  # Repeating interval between match item clears. This is NOT a per-item timer.
  delay-seconds: 30
  # Clear existing item entities when a match/FFA arena becomes active.
  clear-on-match-start: true
  # Radius around spawn points used when arena bounds are not configured.
  match-start-clear-radius: 64
  # Show the countdown only this many seconds before each clear. 0 disables it.
  message-at-seconds: 3
  # Countdown location: action-bar, chat, both, or none.
  display: action-bar
  # {time} and {seconds} are replaced with the remaining seconds.
  message: '&eDropped items clear in &f{time}s'
'''
    return text.replace(old, new, 1) if old in text else text


def patch_manager(text):
    old = '''        if (this.plugin.getFFAManager() == null) return;
        try {
            Object instance = this.plugin.getFFAManager().getArena(arenaName);
            if (instance instanceof FFAArenaInstance ffa) {
                DuelArena arena = ffa.getArena();
                if (arena != null) clearArenaGeometry(arena);
            }
        } catch (Exception ignored) {
            // Existing FFA manager implementations may not expose an arena lookup.
        }
'''
    return text.replace(old, '', 1) if old in text else text

edit('com/ultimateduels/UltimateDuels.java', patch_reload)
edit('com/ultimateduels/UltimateDuels.java', patch_shutdown)
edit('com/ultimateduels/listeners/PlayerJoinQuitListener.java', patch_join)
edit('com/ultimateduels/ffa/DroppedItemClearManager.java', patch_manager)
edit('scripts/prepare_patch_jar.py', patch_prepare)
