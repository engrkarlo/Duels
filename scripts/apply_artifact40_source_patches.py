from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

# Artifact 40's BlockProtectionListener calls LobbyManager.isInLobbyWorld(Player),
# but the decompiled LobbyManager did not contain that method. Add it at build time
# instead of hand-editing the large decompiled class.
lobby = ROOT / 'com' / 'ultimateduels' / 'lobby' / 'LobbyManager.java'
text = lobby.read_text(encoding='utf-8')
if 'boolean isInLobbyWorld(Player player)' not in text:
    marker = '    public void sendToLobby(@Nonnull Player player) {'
    method = '''    public boolean isInLobbyWorld(Player player) {\n        if (player == null || player.getWorld() == null) {\n            return false;\n        }\n        String configured = this.plugin.getConfig().getString("lobby.world-name",\n                this.plugin.getConfig().getString("lobby.world", this.lobbyWorldName));\n        return configured != null && player.getWorld().getName().equalsIgnoreCase(configured);\n    }\n\n'''
    if marker not in text:
        raise SystemExit('Could not find LobbyManager insertion point')
    text = text.replace(marker, method + marker, 1)
    lobby.write_text(text, encoding='utf-8')

# The original command listener and the FFA death listener run at HIGHEST. Register
# the runtime guard after the existing listeners so it can enforce the final state.
listeners = ROOT / 'com' / 'ultimateduels' / 'listeners' / 'ListenerManager.java'
text = listeners.read_text(encoding='utf-8')
registration = '        this.registerListener(pm, new Artifact40BugFixListener(this.plugin));\n'
if 'new Artifact40BugFixListener' not in text:
    marker = '        this.registerListener(pm, new CommandBlockListener(this.plugin));\n'
    if marker not in text:
        raise SystemExit('Could not find ListenerManager command-listener insertion point')
    text = text.replace(marker, marker + registration, 1)
    listeners.write_text(text, encoding='utf-8')

print('Artifact 40 source patches applied successfully.')
