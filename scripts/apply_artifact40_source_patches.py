from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

# Artifact 40's BlockProtectionListener calls LobbyManager.isInLobbyWorld(Player),
# but the decompiled LobbyManager did not contain that method. Add the method at
# build time instead of hand-editing the large decompiled class.
lobby = ROOT / 'com' / 'ultimateduels' / 'lobby' / 'LobbyManager.java'
text = lobby.read_text(encoding='utf-8')
if 'boolean isInLobbyWorld(Player player)' not in text:
    marker = '    public void sendToLobby(@Nonnull Player player) {'
    method = '''    public boolean isInLobbyWorld(Player player) {\n        if (player == null || player.getWorld() == null) {\n            return false;\n        }\n        String configured = this.plugin.getConfig().getString("lobby.world-name",\n                this.plugin.getConfig().getString("lobby.world", this.lobbyWorldName));\n        return configured != null && player.getWorld().getName().equalsIgnoreCase(configured);\n    }\n\n'''
    if marker not in text:
        raise SystemExit('Could not find LobbyManager insertion point')
    text = text.replace(marker, method + marker, 1)
    lobby.write_text(text, encoding='utf-8')

print('Artifact 40 source patches applied successfully.')
