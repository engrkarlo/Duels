from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def patch_command_listener():
    path = ROOT / "com/ultimateduels/listeners/CommandBlockListener.java"
    text = path.read_text(encoding="utf-8")
    # apply_match_command_item_fixes_v3 replaces the method body while leaving the
    # original annotation in place. Collapse the resulting duplicate annotation.
    pattern = r'(?m)^    @EventHandler\([^\n]+\)\n    @EventHandler\([^\n]+\)\n    public void onCommand\('
    replacement = '    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)\n    public void onCommand('
    text, count = re.subn(pattern, replacement, text, count=1)
    if count == 0:
        # Also repair the case where annotations differ only by parameters.
        pattern = r'(?m)^    @EventHandler[^\n]*\n    @EventHandler[^\n]*\n    public void onCommand\('
        text, count = re.subn(pattern, replacement, text, count=1)
    path.write_text(text, encoding="utf-8")
    print(f"command listener annotation repair: {count}")


def patch_item_manager():
    path = ROOT / "com/ultimateduels/ffa/DroppedItemClearManager.java"
    text = path.read_text(encoding="utf-8")
    if "public void clearArena(String arenaName)" in text:
        print("clearArena already present")
        return

    marker = "    /** Immediate association for a drop event. Arena scanning remains authoritative. */"
    method = '''    /** Compatibility entry point used by FFAManager when an FFA arena starts. */
    public void clearArena(String arenaName) {
        if (arenaName == null || this.plugin.getFFAManager() == null) return;
        FFAArenaInstance instance = this.plugin.getFFAManager().getArena(arenaName);
        if (instance == null || instance.getArena() == null) return;
        MatchScope scope = this.scopes.get("ffa:" + arenaName.toLowerCase());
        if (scope == null) {
            scope = new MatchScope("ffa:" + arenaName.toLowerCase(), null,
                    arenaName.toLowerCase(), instance.getArena());
            this.scopes.put(scope.key, scope);
        }
        clearExistingItems(scope);
        ensureTask();
    }

'''
    if marker not in text:
        raise RuntimeError("DroppedItemClearManager insertion marker not found")
    text = text.replace(marker, method + marker, 1)
    path.write_text(text, encoding="utf-8")
    print("clearArena compatibility method added")


patch_command_listener()
patch_item_manager()
