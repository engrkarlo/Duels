from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil
import re

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'vendor' / 'UltimateDuels-7.2.0.jar'
OUT = ROOT / 'build' / 'base'

if not BASE.is_file():
    raise SystemExit(
        'Missing vendor/UltimateDuels-7.2.0.jar. Upload the original 7.2.0 JAR to that path before running the build.'
    )

if OUT.exists():
    shutil.rmtree(OUT)
OUT.mkdir(parents=True)

with ZipFile(BASE) as jar:
    jar.extractall(OUT)

# A rebuilt JAR must not retain the original JAR signatures.
meta_inf = OUT / 'META-INF'
if meta_inf.exists():
    for path in meta_inf.iterdir():
        if path.suffix.upper() in {'.SF', '.RSA', '.DSA', '.EC'}:
            path.unlink()

# /spawn must no longer be the lobby command alias; /ds is supplied by the fix.
plugin_yml = OUT / 'plugin.yml'
if plugin_yml.exists():
    text = plugin_yml.read_text(encoding='utf-8')
    text = re.sub(r'(?m)^(\s*)-?\s*spawn\s*$', r'\1', text)
    text = text.replace('aliases: [spawn]', 'aliases: [ds]')
    plugin_yml.write_text(text, encoding='utf-8')

# Add the new configurable command-blocking and FFA combat-message defaults
# without replacing the user's original configuration structure.
config = OUT / 'config.yml'
if config.exists():
    text = config.read_text(encoding='utf-8')
    if '\ncommand-blocking:' not in text:
        text += '''\n\ncommand-blocking:\n  enabled: false\n  worlds:\n    - lobby\n  commands:\n    - pl\n    - plugins\n    - bukkit:plugins\n    - version\n    - ver\n    - help\n    - spawn\n    - home\n    - homes\n    - fly\n    - tpa\n    - tpaccept\n    - tpadeny\n    - tp\n    - pwarp\n  message: '&cYou cannot use that command in this world.'\n'''
    if '\ncombat-log:' not in text:
        text += '''\ncombat-log:\n  message: '&cYou are in combat for &e{time}s&c!'\n  display: action-bar\n'''
    if 'death-items-drop:' not in text:
        text += '\nffa:\n  death-items-drop: false\n'
    config.write_text(text, encoding='utf-8')

print(f'Prepared original JAR at {OUT}')
