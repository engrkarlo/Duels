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
    'com/ultimateduels/visuals/HealthPacketSender.class',
    'com/ultimateduels/player/PlayerStateManager.class',
    'com/ultimateduels/lobby/LobbyManager.class',
    'com/ultimateduels/commands/LobbyCommand.class',
    'com/ultimateduels/ffa/FFAManager.class',
]

if not BASE.is_file():
    raise SystemExit('Missing vendor/UltimateDuels-7.2.0.jar. Upload the original 7.2.0 JAR to vendor/ before building.')

if OUT.exists():
    shutil.rmtree(OUT)
OUT.mkdir(parents=True)

with ZipFile(BASE) as jar:
    jar.extractall(OUT)

meta_inf = OUT / 'META-INF'
if meta_inf.exists():
    for path in meta_inf.iterdir():
        if path.suffix.upper() in {'.SF', '.RSA', '.DSA', '.EC'}:
            path.unlink()

for relative in PATCHED_CLASSES:
    path = OUT / relative
    if path.exists():
        path.unlink()

plugin_yml = OUT / 'plugin.yml'
if plugin_yml.exists():
    text = plugin_yml.read_text(encoding='utf-8')
    text = re.sub(r'(?m)^\s*- spawn\s*$\n?', '', text)
    text = re.sub(
        r'(?m)^(\s*aliases:\s*\[)([^\]]*)(\])',
        lambda m: m.group(1) + ', '.join(x.strip() for x in m.group(2).split(',') if x.strip().lower() != 'spawn') + m.group(3),
        text,
    )
    plugin_yml.write_text(text, encoding='utf-8')

config = OUT / 'config.yml'
if config.exists():
    text = config.read_text(encoding='utf-8')
    if '\ncommand-blocking:' not in text:
        text += '''\n\ncommand-blocking:\n  enabled: false\n  worlds:\n    - lobby\n  commands:\n    - pl\n    - plugins\n    - bukkit:plugins\n    - version\n    - ver\n    - help\n    - spawn\n    - home\n    - homes\n    - fly\n    - tpa\n    - tpaccept\n    - tpadeny\n    - tp\n    - pwarp\n  message: '&cYou cannot use that command in this world.'\n'''
    if '\ncombat-log:' not in text:
        text += '''\ncombat-log:\n  message: '&cYou are in combat for &e{time}s&c!'\n  display: action-bar\n'''
    if 'death-items-drop:' not in text:
        match = re.search(r'(?m)^ffa:\s*$', text)
        if match:
            next_top = re.search(r'(?m)^\S', text[match.end():])
            insert_at = match.end() + (next_top.start() if next_top else len(text[match.end():]))
            text = text[:insert_at] + '  death-items-drop: false\n' + text[insert_at:]
        else:
            text += '\nffa:\n  death-items-drop: false\n'
    config.write_text(text, encoding='utf-8')

print(f'Prepared original JAR at {OUT}')
