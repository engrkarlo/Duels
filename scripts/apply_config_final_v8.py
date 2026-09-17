from pathlib import Path
import re

p = Path(__file__).resolve().parents[1] / 'scripts' / 'prepare_patch_jar.py'
t = p.read_text(encoding='utf-8')
if 'message-at-seconds:' not in t:
    t, n = re.subn(r"(match-start-clear-radius:\\s*64\\n)(\\s*# Countdown location:)", r"\\1  # Show the countdown only this many seconds before each clear. 0 disables it.\\n  message-at-seconds: 3\\n\\2", t, count=1)
    if n == 0:
        raise RuntimeError('Could not add message-at-seconds to prepare_patch_jar.py')
    p.write_text(t, encoding='utf-8')
    print('patched prepare_patch_jar.py')
else:
    print('already configured prepare_patch_jar.py')
