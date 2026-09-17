from pathlib import Path
from zipfile import ZipFile
import shutil
import re

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'vendor' / 'UltimateDuels-7.2.0.jar'
OUT = ROOT / 'build' / 'base'

PATCHED_CLASSES = [
    'com/ultimateduels/UltimateDuels.class',
    'com/ultimateduels/listeners/BlockProtectionListener.class',
    'com/ultimateduels/listeners/CommandBlockListener.class',
    'com/ultimateduels/listeners/ListenerManager.class',
    'com/ultimateduels/listeners/LobbyListener.class',
    'com/ultimateduels/listeners/WorldChangeListener.class',
    'com/ultimateduels/listeners/PlayerJoinQuitListener.class',
    'com/ultimateduels/listeners/PlayerDeathListener.class',
    'com/ultimateduels/listeners/PlayerDropListener.class',
    'com/ultimateduels/visuals/HealthPacketSender.class',
    'com/ultimateduels/player/PlayerStateManager.class',
    'com/ultimateduels/lobby/LobbyManager.class',
    'com/ultimateduels/commands/LobbyCommand.class',
    'com/ultimateduels/ffa/FFAManager.class',
    'com/ultimateduels/ffa/DroppedItemClearManager.class',
]

if not BASE.is_file():
    raise SystemExit('Missing vendor/UltimateDuels-7.2.0.jar. Upload the original 7.2.0 JAR to vendor/ before building.')
if OUT.exists(): shutil.rmtree(OUT)
OUT.mkdir(parents=True)
with ZipFile(BASE) as jar: jar.extractall(OUT)

meta_inf = OUT / 'META-INF'
if meta_inf.exists():
    for path in meta_inf.iterdir():
        if path.suffix.upper() in {'.SF', '.RSA', '.DSA', '.EC'}: path.unlink()
for relative in PATCHED_CLASSES:
    path = OUT / relative
    if path.exists(): path.unlink()

plugin_yml = OUT / 'plugin.yml'
if plugin_yml.exists():
    text = plugin_yml.read_text(encoding='utf-8')
    text = re.sub(r'(?m)^\s*- spawn\s*$\n?', '', text)
    text = re.sub(r'(?m)^(\s*aliases:\s*\[)([^\]]*)(\])', lambda m: m.group(1) + ', '.join(x.strip() for x in m.group(2).split(',') if x.strip().lower() != 'spawn') + m.group(3), text)
    plugin_yml.write_text(text, encoding='utf-8')

config = OUT / 'config.yml'
if config.exists():
    text = config.read_text(encoding='utf-8')
    text = text.replace('  # Teleport to lobby on join\n  # Set to false if you use another plugin (like DeluxeHub) to manage spawn\n  teleport-on-join: false', '  # Teleport to the lobby when joining while already in the lobby world.\n  # Returning players in other worlds are never moved here by UltimateDuels.\n  teleport-on-join: false')
    text = text.replace('  # Players joining for first time get teleported\n  first-join-teleport: false', '  # Teleport players to the lobby on their first-ever join.\n  # Set false to let your normal server/world spawn system handle first joins.\n  first-join-teleport: false')

    command_blocking = '''\n\n# ------------------------------------------------------------\n# Command blocking\n# ------------------------------------------------------------\n# Blocks listed commands before another plugin can execute them.\n# Namespaced forms such as /bukkit:plugins are supported.\n# Blocking applies in the listed worlds and, if block-in-matches is true,\n# also while the player is in a duel, FFA arena, or spectating.\ncommand-blocking:\n  enabled: false\n  worlds:\n    - lobbyarenas\n  block-in-matches: true\n  commands:\n    - pl\n    - plugins\n    - bukkit:plugins\n    - version\n    - ver\n    - help\n    - spawn\n    - home\n    - homes\n    - fly\n    - tpa\n    - tpaccept\n    - tpadeny\n    - tp\n    - pwarp\n  message: '&cYou cannot use that command here.'\n'''
    text = re.sub(r'(?ms)\n# ------------------------------------------------------------\n# Command blocking\n# ------------------------------------------------------------.*?(?=\n\S|\Z)', '', text)
    text += command_blocking

    item_clear = '''\n# ------------------------------------------------------------\n# FFA item cleanup\n# ------------------------------------------------------------\n# Clears old item entities when an FFA arena receives its first player,\n# then tracks items intentionally dropped or dropped by FFA deaths.\n# Normal survival-world item drops are never affected.\nitem-clear:\n  enabled: true\n  # Seconds before a tracked FFA item is removed. 0 removes immediately.\n  delay-seconds: 30\n  # Clear existing item entities when an FFA arena becomes active.\n  clear-on-match-start: true\n  # Radius around each FFA spawn point used for the match-start cleanup.\n  match-start-clear-radius: 64\n  # Countdown location: action-bar, chat, both, or none.\n  display: action-bar\n  # {time} and {seconds} are replaced with the remaining seconds.\n  message: '&eDropped items clear in &f{time}s'\n'''
    text = re.sub(r'(?ms)\n# ------------------------------------------------------------\n# FFA item cleanup\n# ------------------------------------------------------------.*?(?=\n\S|\Z)', '', text)
    text += item_clear

    combat_log = '''\n# ------------------------------------------------------------\n# FFA combat timer display\n# ------------------------------------------------------------\n# display can be action-bar, chat, both, or none.\ncombat-log:\n  message: '&cYou are in combat for &e{time}s&c!'\n  display: action-bar\n'''
    text = re.sub(r'(?ms)\n# ------------------------------------------------------------\n# FFA combat timer display\n# ------------------------------------------------------------.*?(?=\n\S|\Z)', '', text)
    text += combat_log

    if not re.search(r'(?m)^ffa:\s*$', text):
        text += '\nffa:\n  # Whether FFA deaths drop the player inventory.\n  death-items-drop: false\n'
    elif not re.search(r'(?m)^ffa:\n[\s\S]*?^\s+death-items-drop:', text):
        match = re.search(r'(?m)^ffa:\s*$', text)
        text = text[:match.end()] + '\n  # Whether FFA deaths drop the player inventory.\n  death-items-drop: false' + text[match.end():]
    config.write_text(text, encoding='utf-8')

print(f'Prepared original JAR at {OUT}')
